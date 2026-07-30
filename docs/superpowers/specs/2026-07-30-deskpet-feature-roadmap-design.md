# DeskPet 功能路线图设计文档

## 概述

DeskPet（团子）是一款 Android 桌面宠物应用，当前已实现基础的宠物养育、装扮、悬浮窗、心情日记、经期追踪、健康提醒、音效等功能。本设计文档规划了从基础修复到社交传播的完整功能路线图，共 14 个功能模块，分为 L0-L3 四个递进层次。

### 设计目标

- 解决现有痛点：宠物状态不持久化、装扮视觉割裂、拍照功能缺失
- 建立情感深度：宠物记忆、习惯联动、经期行为联动、环境感知
- 丰富长线内容：家居装饰、旅行放置、成就图鉴、语音互动
- 社交传播获客：分享卡片、共同养育

### 技术约束

- 纯本地存储：Room 数据库 + SharedPreferences
- 可接入免费 API：和风天气、Hitokoto 每日一言、本地农历计算
- 社交功能后端：华为云 AGC（Cloud DB + Push Kit），永久免费档
- 经期数据物理隔离：仅存本地 Room，不经过任何网络传输
- 应用包名：`com.deskpet.app`

### 新增依赖

| 依赖 | 用途 | 引入层级 |
|------|------|---------|
| `androidx.work:work-runtime-ktx` | WorkManager 定时任务（天气刷新、日记生成） | L1 |
| `com.squareup.okhttp3:okhttp` | HTTP 网络请求（天气/每日一言 API） | L1 |
| `com.huawei.agconnect:agconnect-core` | AGC 基础能力 | L3 |
| `com.huawei.agconnect:clouddb` | AGC Cloud DB 实时数据同步 | L3 |
| `com.huawei.hms:push` | AGC Push Kit 推送通知 | L3 |

### 架构原则

分层递进，每一层是下一层的基础。L0 不做 L1 无数据根基，L1 不做 L2 无内容来源，L2 不做 L3 无分享素材。

---

## L0 基础修复层

### L0-1 宠物状态持久化

**现状**：`Pet` 的饱腹度/心情/亲密/钻石/等级仅存在于内存 `StateFlow`，App 重启后全部重置为默认值。

**设计**：

新建 `PetEntity` Room 实体，包含 Pet 的全部字段：name、species、color、level、hunger、mood、intimacy、diamonds、personalityTags、equippedHead/Glasses/Collar/Clothing/Tail/Accessory、lastInteractionTime。

新建 `PetDao` 接口：
- `getPet(): PetEntity?` — 查询当前宠物
- `updatePet(pet: PetEntity)` — 更新宠物状态
- `insertIfNotExist(pet: PetEntity)` — 首次创建时插入

`PetRepository` 修改：
- 初始化时从 Room 读取 Pet 数据填充 `_petState`
- 所有 `updatePet`/`feedPet`/`petPet`/`updateMood`/`updateHunger`/`addDiamonds` 方法在更新内存 StateFlow 的同时写入 Room
- 新增 `lastInteractionTime` 字段，每次交互时更新

**离线衰减机制**：

App 启动时检查 `lastInteractionTime` 与当前时间差，按规则衰减状态：
- 超过 4 小时：饱腹度每小时 -2
- 超过 8 小时：心情值每小时 -1
- 超过 24 小时：亲密度每小时 -0.5
- 衰减下限为 0

衰减在 App 启动时一次性计算并应用，后台运行期间不持续衰减。

### L0-2 装扮系统升级

**现状**：48 件服饰全部用 emoji 绘制，与 Canvas 手绘宠物身存在视觉风格割裂。

**设计**：

将服饰从 emoji 改为 Canvas 矢量绘制。每件服饰定义为一个 `DrawScope` 扩展函数，颜色和风格与 PetCanvas 一致。

服饰分两类实现：
- **几何服饰**（皇冠、蝴蝶结、眼镜、围巾、铃铛项圈等）：用 Path/Oval/RoundRect 绘制
- **复杂服饰**（帽子、连衣裙等）：用组合几何体拼接

新建 `OutfitRenderer` 对象，维护 `Map<String, DrawScope.(w, h, species) -> Unit>` 映射表，根据 outfitId 查找对应的绘制函数。每件服饰的绘制函数接收画布宽高和物种参数，内部根据物种调整位置和缩放。

保留 emoji 作为 fallback：如果某件服饰尚未实现矢量绘制，`OutfitRenderer` 降级为 emoji 渲染（复用现有的 drawIntoCanvas + Paint.drawText 逻辑）。

`PetCanvas` 的 outfit overlay 部分修改为调用 `OutfitRenderer.render(outfitId, w, h, species)` 而非直接画 emoji。

装扮预览升级：DressUpScreen 的 `DressUpPreview` 从 110dp 小窗口改为 180dp 全身展示，用户可以看清整体搭配效果。

### L0-3 拍照功能落地

**现状**：拍照仅有白色闪光动画 + "合影已保存"提示，无实际图片生成。

**设计**：

点击拍照时，将 PetCanvas + 当前装扮 + 背景渐变圆渲染到 Bitmap。使用 Compose 的 `Picture` API 离屏渲染：创建 `Picture`，在 `drawIntoCanvas` 中用 `nativeCanvas.drawPicture` 绘制完整场景。

Bitmap 生成流程：
1. 创建 1080x1080 的 `Bitmap`（`Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888)`）
2. 基于 Bitmap 创建 `Canvas(bitmap)`
3. 在 Canvas 上绘制：渐变背景圆 → 宠物身体 → 装扮 → 水印（宠物名字 + 日期 + 心情文字），复用 `PetCanvas` 的绘制逻辑（提取为可复用的 `DrawScope` 独立函数）
4. 保存到 `getExternalFilesDir(Environment.DIRECTORY_PICTURES)/deskpet_photos/` 目录，无需存储权限

保存后通过 `FileProvider` 生成 content URI，弹出系统分享面板（`Intent.ACTION_SEND`，type=`image/png`）。

闪光动画保留作为拍照反馈，在 Bitmap 生成完成后触发。

`PetViewModel.onPhoto()` 修改：实际生成 Bitmap 并保存文件，保存成功后显示 toast "合影已保存，已分享"。

### L0-4 音效系统完善

**现状**：已有 7 种程序化音效（PET/FEED/CLICK/PURCHASE/EQUIP/ERROR/LEVEL_UP）。

**设计**：

`SoundType` enum 新增：
- `GREETING` — App 启动/从后台恢复时播放（C-E 两音，轻柔上行）
- `SLEEP` — 宠物进入 SLEEPY 状态时播放（G-C 下行渐弱）
- `WAKE` — 宠物从 SLEEPY 恢复时播放（C-G 上行渐强）
- `ACHIEVEMENT` — 解锁成就时播放（C-E-G-C 琶音 + 高音收尾）
- `TRAVEL_DEPART` — 宠物出发旅行时播放（D-F-A 轻快短促）
- `TRAVEL_RETURN` — 宠物旅行归来时播放（C-E-G-C 温馨和弦）

现有 `CLICK` 细化为：
- `TAP_LIGHT` — 轻触反馈（880Hz，60ms）
- `TAP_HEAVY` — 长按反馈（440Hz，100ms）

新增习惯打卡专用音效：
- `CHECKIN` — 打卡完成时播放（E-A 两音，轻快上行，区别于 `ACHIEVEMENT` 的四音琶音）

`SoundHelper.playToneSync` 中为每种新音效定义频率序列和时长。

触发点：
- `DeskPetApplication.onCreate()` 中已有 `SoundHelper.init()`，新增播放 `GREETING`
- `PetBehaviorEngine` 中 SLEEPY 状态切换时播放 `SLEEP`/`WAKE`
- 成就解锁时播放 `ACHIEVEMENT`（L2-3 实现）
- 旅行出发/归来时播放 `TRAVEL_DEPART`/`TRAVEL_RETURN`（L2-2 实现）
- 习惯打卡完成时播放 `CHECKIN`（L1-2 实现）

---

## L1 情感引擎层

### L1-1 宠物记忆系统

**设计**：

数据层：

新建 `InteractionLog` Room 实体：
```
id: Long (PK, autoGenerate)
type: String  // PET, FEED, MOOD_SELECTED, PHOTO, OPEN_APP, CLOSE_APP
timestamp: Long
detail: String  // JSON 格式的附加信息，如 {"food":"小鱼干"}, {"mood":"HAPPY"}
```

新建 `InteractionLogDao`：
- `insert(log: InteractionLog)`
- `getByDateRange(start: Long, end: Long): List<InteractionLog>`
- `getRecent(days: Int): List<InteractionLog>`
- `countByTypeAndDateRange(type: String, start: Long, end: Long): Int`

新建 `PetDiary` Room 实体：
```
id: Long (PK, autoGenerate)
date: String  // "yyyy-MM-dd"
content: String
moodSnapshot: String  // 当天心情等级
```

`PetViewModel` 的 `onPet()`、`onFeed()`、`onMoodSelected()`、`onPhoto()` 方法中新增 `InteractionLog` 写入。App 前后台切换时在 `DeskPetApplication` 或 `MainActivity` 的生命周期回调中记录 OPEN_APP / CLOSE_APP。

记忆引擎：

新建 `PetMemoryEngine` 类，每天午夜（由 `WorkManager` 定时任务或 App 启动时检查上次生成日期触发）生成一条宠物日记：

日记模板匹配逻辑（按优先级）：
1. 连续互动天数 ≥3 → "已经连续 {n} 天见到主人啦，这成了我最期待的事！"
2. 当天互动 = 0 → "今天主人好忙呀…我乖乖等了一整天，明天会来看我吗？"
3. 当天互动 ≥5 → "今天主人来找我玩了 {n} 次，我是全世界最幸福的小团子！"
4. 当天有喂食 → "今天吃了{food}，主人总是知道我想吃什么～"
5. 当天心情选了"难过" → "主人今天心情不太好，我一直陪着她，希望能让她开心一点"
6. 默认 → "今天和主人在一起度过了平凡又开心的一天～"

日记生成时读取当天 InteractionLog 聚合数据，填充模板变量。

UI 层：

首页宠物上方新增"日记气泡"入口（小信封图标 💌），点击进入日记列表页。日记列表页按日期倒序排列，每条日记显示日期 + 宠物表情 emoji + 日记内容。当天日记在午夜生成前，显示"今天的故事还在书写中…"。

### L1-2 习惯养成联动

**设计**：

联动机制：

用户完成提醒打卡后，宠物获得对应奖励：

| 打卡类型 | 饱腹度 | 心情 | 亲密度 | 钻石 |
|---------|--------|------|--------|------|
| 喝水 | +3 | — | — | +1 |
| 久坐起身 | — | +5 | — | +1 |
| 护眼 | — | — | +2 | +1 |

连续打卡奖励：连续 3 天 +5 钻石，连续 7 天 +15 钻石，连续 15 天 +30 钻石，连续 30 天 +50 钻石。

打卡状态映射到宠物行为：
- 当天全部提醒完成 → 宠物显示 EXCITED 状态 + 特殊爱心粒子（金色）
- 有未完成提醒 → 宠物偶尔冒出提醒气泡（"主人记得喝水哦~"），频率不超过每小时 1 次
- 连续 0 打卡 2 天 → 宠物显示 HUNGRY 状态 + 担心表情

打卡 UI：

健康页的每个提醒卡片增加"打卡"按钮（圆形 ✓ 图标）。打卡后播放 `CHECKIN` 音效 + 宠物冒出开心气泡。新增"连续打卡"日历视图，完成的日子用宠物表情标记。

数据层：

`PetSettings` 新增字段：`lastDrinkCheckTime: Long`、`lastSitCheckTime: Long`、`lastEyeCheckTime: Long`。

新建 `HabitStreak` Room 实体：
```
habitType: String  // "DRINK", "SIT", "EYE"
currentStreak: Int
longestStreak: Int
lastCheckDate: String  // "yyyy-MM-dd"
```

打卡逻辑校验：同一提醒间隔内重复打卡无效（喝水 60 分钟、久坐 120 分钟、护眼 45 分钟）。

### L1-3 经期行为联动

**设计**：

经期阶段判定：

基于已有的 `PeriodLog` 数据计算当前所处周期阶段。"完整周期记录"指：至少有 2 次经期开始日期记录，且两次间隔在 21-35 天正常范围内。满足此条件才启用完整四阶段联动，否则仅启用经期期联动（第 1-5 天）。

| 阶段 | 周期天数范围 | 宠物行为 |
|------|------------|---------|
| 经期期 | 第 1-5 天 | COMFORTING 状态，减少随机走动 50%，说话频率提高，语音气泡变为"我会陪着你的~" |
| 经后期 | 第 6-13 天 | 恢复正常活力，偶尔冒出"主人今天气色好好~" |
| 排卵期 | 第 14 天前后 | HAPPY 状态，主动靠近屏幕中心 |
| 经前期 | 第 21-28 天 | 偶尔显示 HUNGRY 状态，冒出"主人想吃点什么吗？" |

经期前 3 天：宠物主动冒出"快要到了，记得准备哦"提醒（每天 1 次）。
经期结束后：宠物冒出"辛苦啦，要好好照顾自己~"。

经期通知升级：

经期预测提醒文案从通用模板改为宠物口吻：
- 预测提醒："小团子感觉主人可能快要不方便了，记得准备好需要的物品哦~"
- 经期结束："小团子松了一口气，主人辛苦啦~"

`NotificationHelper.showPeriodReminder` 修改文案，增加宠物名字变量。

数据隐私：

经期联动功能默认关闭。`PetSettings` 新增 `periodBehaviorLink: Boolean = false`。设置页经期记录开关下方增加说明文案："你的经期数据仅保存在本机，不会上传任何服务器"。开启经期行为联动需单独开关。

`PetBehaviorEngine` 中新增经期阶段判断逻辑，根据 `PeriodLog` 数据和当前日期计算阶段，影响宠物状态切换优先级。

### L1-4 环境感知（天气/节日/每日一言 API）

**设计**：

API 集成层：

新建 `EnvApiService` 类，统一管理三个数据源：
- **天气**：和风天气开发版（免费，每日 1000 次调用），需在 [dev.qweather.com](https://dev.qweather.com) 注册获取 API Key。根据 IP 定位获取当前天气。API: `https://devapi.qweather.com/v7/weather/now?location={lng},{lat}&key={apiKey}`。API Key 存入 `BuildConfig` 或 `local.properties`，不硬编码在源码中。
- **每日一言**：Hitokoto API（`https://v1.hitokoto.cn`），返回随机一句话 JSON
- **节日节气**：本地计算，使用农历转换算法（引入 `LunarCalendar` 工具类），无需 API

API 调用策略：
- 天气：每 3 小时刷新 1 次，使用 `WorkManager` PeriodicWorkRequest
- 每日一言：每天首次打开 App 时获取
- 节日节气：每天 0 点计算
- 所有 API 调用失败时静默降级，不影响核心功能
- 使用 OkHttp 发起网络请求（已在依赖树中或新增依赖）

API 数据缓存：

新建 `EnvCache` Room 实体：
```
key: String  // "weather", "daily_quote", "festival"
value: String  // JSON
updatedAt: Long
```

天气联动：

| 天气 | 宠物造型 | 语音气泡 |
|------|---------|---------|
| 晴天 | 偶尔戴墨镜 | "今天阳光真好~" |
| 雨天 | 头顶小伞 | "下雨啦，出门记得带伞哦~" |
| 雪天 | 身上落雪花粒子 | "好漂亮呀！主人一起看雪吗？" |
| 高温 >35°C | SLEEPY 状态 | "好热…主人记得多喝水~" |
| 降温 ≥5°C vs 昨日 | — | "今天变冷了，多穿一件哦~" |

天气造型通过 `PetCanvas` 的 outfit overlay 层实现，天气特效作为临时装扮叠加。

节日联动：

| 节日 | 宠物行为 |
|------|---------|
| 春节/中秋/端午 | 自动穿戴节日限定装扮 + 节日祝福气泡 |
| 情人节/七夕 | "主人今天有人陪吗？不管怎样我都在哦~" |
| 用户生日 | 生日祝福 + 特殊生日帽装扮（引导页可选填生日） |
| 二十四节气 | 对应养生提示（"立秋了，记得润肺哦~"） |

每日一言：

每天首次打开 App 时，宠物冒出一句话（来自 Hitokoto API）。用户可点击"换一句"刷新，每天最多 3 次。内容保存在当天 `PetDiary` 中作为引言。

---

## L2 内容丰富层

### L2-1 家居装饰 / 宠物小窝

**设计**：

小窝页面：

底部导航新增"小窝"Tab。小窝页面是一个 2D 房间场景，用 Canvas 绘制：背景墙 + 地板 + 4x3 网格的可摆放家具。宠物在房间中央，根据家具风格改变姿态（有床则躺着、有玩具则玩耍）。

家具系统：

6 类家具：墙纸（3 种）、地板（3 种）、床铺（4 种）、桌椅（4 种）、装饰品（6 种）、玩具（4 种）。

每件家具有属性：price（钻石）、requiredLevel、comfort（舒适度）、fun（趣味度）、beauty（美观度）。家具用 Canvas 矢量绘制，风格与 PetCanvas 统一。

家具商店 UI 复用衣橱页的卡片+购买流程。

属性影响机制：

- **舒适度**：影响宠物在小窝中的心情恢复速度（每 10 点 +10% 恢复速度）
- **趣味度**：影响自动行为时进入 PLAYING 状态的概率（每 10 点 +5%）
- **美观度**：影响拍照时的背景加成（高美观度房间拍照自动添加装饰边框）

属性影响有上限（舒适度上限 50 点、趣味度上限 30 点、美观度上限 30 点），防止数值膨胀。

数据层：

新建数据类和实体：
- `FurnitureItem`（id, name, category, price, requiredLevel, comfort, fun, beauty, drawFunction key）
- `RoomLayout` Room 实体（slotIndex: Int, furnitureId: String），持久化家具摆放
- `OwnedFurniture` 持久化已购买家具 ID 列表（复用 SharedPreferences，类似 ownedOutfits）

`PetRepository` 新增方法：`purchaseFurniture()`、`placeFurniture()`、`removeFurniture()`、`getRoomLayout()`、`getOwnedFurniture()`。

### L2-2 旅行放置系统

**设计**：

旅行机制：

首页新增"送宠物出门"入口（旅行背包图标）。选择目的地和出行时长：

| 类型 | 时长范围 | 示例目的地 |
|------|---------|-----------|
| 短途 | 30 分钟 - 1 小时 | 公园、咖啡馆、书店 |
| 中途 | 2-4 小时 | 海边、山林、古镇 |
| 长途 | 6-12 小时 | 雪山、沙漠、星空 |

宠物出门后，首页宠物位置显示"旅行中"占位卡（信封图标 + 倒计时），悬浮窗宠物隐藏。旅行期间宠物可能寄回 1-2 张明信片（通知推送），内容是宠物口吻的见闻。宠物回来后自动回到首页/悬浮窗，带回 1-3 件礼物。

礼物类型：钻石（主要收益）、限定装扮（低概率）、家具（低概率）、食材（比普通食物饱腹加成更高）。

明信片系统：

每张明信片 = 目的地背景图（Canvas 绘制的简笔风景）+ 宠物旅行造型 + 宠物手写文字。保存在 `Postcard` Room 实体（id, destination, date, drawData, message, gifts）。可查看历史明信片收集，可分享（复用 L0-3 的分享机制）。

目的地解锁：

初始解锁 2 个短途目的地。宠物等级达到 5/10/15 级分别解锁中途/长途/特殊目的地。特殊目的地需要特定装扮触发（如太空目的地需太空帽）。

旅行与状态联动：

旅行期间饱腹度正常衰减（L0-1 衰减机制），回来时饱腹度过低显示 HUNGRY。旅行归来后心情值 +10。用户在旅行期间打开 App 查看明信片，亲密度 +2。

旅行到时自动结算：App 启动或悬浮窗恢复时检查 `TravelLog.returnTime`，若已超过返回时间则立即结算旅行（发放礼物、生成明信片、恢复宠物到首页）。若用户超过 72 小时未打开 App，旅行仍按时结算，礼物和明信片存入数据库待用户下次打开时领取，宠物饱腹度按 L0-1 衰减规则计算。

数据层：

新建 `TravelDestination` 数据类（id, name, durationRange, requiredLevel, requiredOutfit, sceneDrawKey, postcardTemplates, giftPool）。

新建 `TravelLog` Room 实体（destinationId, departTime, returnTime, postcardsReceived, giftsReceived, completed）。App 重启后根据 departTime 恢复倒计时。

`PetRepository` 新增 `startTravel()`、`checkTravelReturn()`、`receivePostcard()` 方法。

### L2-3 成就与图鉴系统

**设计**：

成就系统：

三大类成就：

**互动成就**：
- 累计抚摸 50/100/500 次
- 累计喂食 30/100/300 次
- 累计拍照 10/50/100 张

**养成成就**：
- 宠物达到 Lv.5/10/20
- 亲密度达到 80/100
- 连续登录 7/30/100 天

**探索成就**：
- 解锁全部 4 种物种
- 收集 10/20/48 件服饰
- 收集 5/15/30 件家具
- 收集 10/30 张明信片

每个成就解锁时弹出庆祝弹窗 + 播放 `ACHIEVEMENT` 音效 + 奖励钻石。

成就进度基于 L1-1 的 `InteractionLog` 数据实时计算，不额外存储进度。新建 `AchievementDao` 记录已解锁的成就 ID + 解锁时间。

图鉴系统：

三类图鉴：
- **服饰图鉴**：48 件服饰的收集状态（已拥有显示彩色，未拥有显示剪影）
- **明信片图鉴**：所有目的地的明信片收集状态（已收到显示彩色，未收到显示问号）
- **物种图鉴**：4 种物种的不同颜色变体收集状态

图鉴页面入口：设置页新增"图鉴"入口。图鉴数据基于已拥有的服饰/家具/明信片列表推导，不额外存储。

### L2-4 语音 TTS 互动

**设计**：

TTS 引擎：

使用 Android 原生 `TextToSpeech`。设置中文语音引擎，语速 0.8（更可爱），音调 1.2（更萌）。如果设备支持，优先选择"儿童声"或"女声"语音包。

设备兼容性处理：TTS 初始化时检查 `TextToSpeech.ENG_AVAILABLE` 状态，若设备无中文语音包，自动降级为仅显示文字气泡（不播放语音），并在设置页提示"当前设备不支持语音朗读，已切换为文字模式"。`SpeechHelper.init()` 回调中设置 `isTtsAvailable` 标志位，所有 `speak()` 调用前先检查此标志。

新建 `SpeechHelper` 类（与 `SoundHelper` 并列），管理 TTS 生命周期：
- `init(context)` — 初始化 TTS 引擎，设置语言和参数
- `speak(text: String)` — 播放语音
- `stop()` — 停止当前播放
- `isSpeaking: Boolean` — 当前是否在播放
- `setEnabled(value: Boolean)` — 开关

语音触发场景：

| 场景 | 文案池示例 |
|------|-----------|
| App 启动 | "主人你来啦~" / "早安~" / "辛苦一天啦~" |
| 抚摸时 | "好舒服呀~" / "再摸摸我嘛~" / "最喜欢主人了~" |
| 喂食时 | "小鱼干！我最爱了~" / "好香好香~" |
| 经期联动 | "我会陪着你的~" |
| 每日一言 | 用 TTS 念出当天的 Hitokoto 句子 |
| 打卡完成 | "主人真棒~" |
| 旅行归来 | "我回来啦！想我了吗？" |

文案池按场景组织，每个场景 8-15 条文案。文案根据宠物性格标签（PersonalityTag）微调用词：活泼型用"！"多，文静型用"~"多。

语音气泡 + TTS 协同：

现有文字语音气泡保留，TTS 与气泡同步触发。设置页新增"语音"开关（与"音效"开关独立），默认关闭。TTS 播放时如果音效正在播放，等待音效结束后再播放语音。

---

## L3 社交传播层

### L3-1 社交分享卡片

**设计**：

分享卡片类型：

| 卡片类型 | 内容 | 触发入口 |
|---------|------|---------|
| 每日宠物状态卡 | 宠物全身像 + 名字 + 等级 + 今日心情 + 日记摘录 | 首页分享按钮 |
| 装扮搭配卡 | 宠物穿戴全套装扮 + "今日穿搭" + 装扮清单 | 衣橱页"分享搭配" |
| 成就解锁卡 | 成就图标 + 名称 + "我解锁了xxx！" | 成就解锁弹窗 |
| 旅行明信片 | 目的地风景 + 宠物造型 + 手写文字 | 明信片页"分享" |
| 打卡成就卡 | 连续天数 + 宠物庆祝造型 | 打卡连续天数达成 |

卡片渲染：

复用 L0-3 的 Bitmap 离屏渲染机制。卡片尺寸：正方形 1080x1080（微信/小红书）+ 长图 1080x1920（微博）。3 种模板风格：清新粉、治愈绿、简约白。渲染时自动添加"团子"品牌水印（左下角小图标 + 文字）。

分享流程：

生成 Bitmap → 保存到应用专属目录 → 通过 FileProvider 生成 content URI → 弹出系统分享面板。分享面板底部增加"保存到相册"选项。

数据层：

新建 `ShareCardRenderer` 工具类，接收卡片类型 + 数据 + 模板风格，输出 Bitmap。分享记录写入 `InteractionLog`（type: SHARE），用于成就统计。设置页新增"分享水印"开关。

### L3-2 共同养育（华为云 AGC）

**设计**：

技术方案：华为云 AGC Cloud DB（实时数据同步）+ Push Kit（推送通知）。永久免费档，国内节点。

AGC 集成：

- 将 `agconnect-services.json` 放入 `app/` 目录
- `app/build.gradle.kts` 新增 AGC SDK 依赖
- `DeskPetApplication.onCreate()` 中初始化 AGC

Cloud DB 数据模型：

新建 ObjectType 类（AGC Cloud DB 要求）：
- `CompanionPet`：pairCode（配对码）、ownerId、petName、petSpecies、petColor、petLevel、petMood、lastUpdate
- `GiftMessage`：giftId、fromId、toId、giftType、giftContent、timestamp、received
- `PetInteraction`：interactionId、fromId、toId、type（PET/PAT/FEED）、timestamp

配对机制：

1. 用户 A 在设置页点击"邀请闺蜜/伴侣一起养"
2. 生成 6 位随机配对码，写入 Cloud DB `CompanionPet` 记录（ownerId = A 的设备 ID）
3. 生成"宠物邀请卡"（宠物照片 + 名字 + 配对码 + 二维码），通过系统分享发送给 B
4. B 在引导页选择"我有邀请码"，输入配对码
5. Cloud DB 查询配对码，匹配成功后双方建立连接
6. 配对信息本地存储（`CompanionLink` Room 实体：pairCode, companionName, companionPetSnapshot, linkedDate）

实时互动：

- **送礼物**：A 点击"给 {B的宠物} 送礼物" → 选择礼物 → 写入 Cloud DB `GiftMessage` → B 端监听到新记录 → 推送通知 + 打开 App 后宠物冒出气泡
- **状态互看**：双方宠物状态变更时同步写入 Cloud DB `CompanionPet` → 对方监听到更新 → 角落显示对方宠物小头像 + 状态
- **云撸**：A 点击对方宠物头像"远程摸摸" → 写入 `PetInteraction`(type=PET) → B 端监听到 → 宠物冒爱心粒子 + "{A}摸了摸你~"
- 每日远程互动上限 3 次（含送礼物和云撸，发送方计数；接收方无限制）

推送通知：

礼物/互动到达时通过 AGC Push Kit 推送通知。通知文案使用宠物口吻。非华为设备上 Push Kit 送达率可能降低，但用户打开 App 后仍能看到未读消息（应用内消息列表）。

隐私与安全：

- 配对码仅 Cloud DB 验证，不含用户个人信息
- 同步的数据仅包含宠物名字 + 状态 + 礼物内容，不含任何健康/经期数据
- 经期数据继续只存本地 Room，与 Cloud DB 物理隔离
- 设置页可随时"解除配对"，删除本地 `CompanionLink` 和 Cloud DB 中的配对记录
- 配对码 7 天未使用自动失效（Cloud DB 设置 TTL）

数据层：

本地新建 `CompanionLink` Room 实体（pairCode, companionName, companionPetName, companionPetSpecies, companionPetColor, linkedDate）。

新建 `CompanionRepository` 类，封装 Cloud DB 的 CRUD 操作：
- `createPair(pet: Pet): String` — 生成配对码并写入 Cloud DB
- `joinPair(code: String): Boolean` — 加入配对
- `sendGift(toId: String, gift: GiftData)` — 发送礼物
- `sendInteraction(toId: String, type: String)` — 发送互动
- `listenForUpdates(onUpdate: (data) -> Unit)` — 监听对方更新
- `unpair()` — 解除配对

---

## 完整路线图总览

| 层级 | 功能 | 核心价值 | 依赖 |
|------|------|---------|------|
| L0 | 状态持久化 | 养了不白养 | 无 |
| L0 | 装扮系统升级 | 视觉统一不割裂 | 无 |
| L0 | 拍照功能落地 | 真正能拍能分享 | 装扮系统 |
| L0 | 音效系统完善 | 听觉反馈全覆盖 | 无 |
| L1 | 宠物记忆系统 | 宠物"记得你" | L0 状态持久化 |
| L1 | 习惯养成联动 | 照顾自己=照顾宠物 | L0 状态持久化 |
| L1 | 经期行为联动 | 核心差异化 | L0 状态持久化 |
| L1 | 环境感知 | 宠物"知道外面的世界" | L0 状态持久化 |
| L2 | 家居装饰小窝 | 空间定制+长线内容 | L0 持久化 |
| L2 | 旅行放置系统 | 放置钩子+收集驱动 | L0 持久化, L1 记忆 |
| L2 | 成就与图鉴 | 长线目标+收集动力 | L1 记忆, L2 旅行 |
| L2 | 语音 TTS 互动 | 情感传递升级 | L0 音效, L1 各联动 |
| L3 | 社交分享卡片 | 传播获客 | L0 拍照, L2 成就 |
| L3 | 共同养育 | 社交裂变 | L3 分享卡片, AGC |

### 新增 Room 实体清单

| 实体 | 所属功能 | 用途 |
|------|---------|------|
| PetEntity | L0-1 | 持久化宠物状态 |
| InteractionLog | L1-1 | 记录交互事件 |
| PetDiary | L1-1 | 宠物日记 |
| HabitStreak | L1-2 | 连续打卡记录 |
| EnvCache | L1-4 | API 数据缓存 |
| RoomLayout | L2-1 | 家具摆放 |
| TravelLog | L2-2 | 旅行记录 |
| Postcard | L2-2 | 明信片收集 |
| CompanionLink | L3-2 | 配对信息 |

### 新增工具类清单

| 类 | 所属功能 | 用途 |
|----|---------|------|
| OutfitRenderer | L0-2 | 服饰矢量渲染映射表 |
| PhotoHelper | L0-3 | Bitmap 离屏渲染+保存 |
| PetMemoryEngine | L1-1 | 日记生成引擎 |
| EnvApiService | L1-4 | 天气/每日一言/节日 API |
| LunarCalendar | L1-4 | 农历节气计算 |
| FurnitureRenderer | L2-1 | 家具矢量渲染映射表 |
| ShareCardRenderer | L3-1 | 分享卡片渲染 |
| CompanionRepository | L3-2 | AGC Cloud DB 封装 |
| SpeechHelper | L2-4 | TTS 语音管理 |

### 新增页面清单

| 页面 | 所属功能 | 导航入口 |
|------|---------|---------|
| 日记列表页 | L1-1 | 首页信封图标 |
| 连续打卡日历页 | L1-2 | 健康页打卡区 |
| 小窝页 | L2-1 | 底部导航新 Tab |
| 家具商店页 | L2-1 | 小窝页"商店"按钮 |
| 旅行出发页 | L2-2 | 首页旅行图标 |
| 旅行中状态页 | L2-2 | 首页旅行占位卡 |
| 明信片相册页 | L2-2 | 小窝页或图鉴入口 |
| 成就图鉴页 | L2-3 | 设置页入口 |
| 配对邀请页 | L3-2 | 设置页入口 |
| 应用内消息列表页 | L3-2 | 首页消息图标 |
