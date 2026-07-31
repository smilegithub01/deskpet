# Sub1：语音对话系统重构

> 目标：接入DialogueBank替换硬编码，扩充文案至每场景8-15条，按状态/性格切换

## 现状问题
1. DialogueBank.kt 已定义28句但从未被调用（死代码）
2. PetViewModel.kt 仅2句硬编码：抚摸→"好舒服呀～"、喂食→"真好吃～"
3. 对话不随宠物状态/性格变化
4. 悬浮窗仅播音效不显示气泡

## 实施方案

### 1. 扩充 DialogueBank 文案池
每场景扩充至8-15条，新增3个场景：

| 场景 | 现有 | 目标 | 新增 |
|------|------|------|------|
| greeting 启动问候 | 5 | 10 | +5 |
| pet 抚摸 | 5 | 10 | +5 |
| feed 喂食 | 5 | 10 | +5 |
| periodLink 经期联动 | 3 | 8 | +5 |
| dailyQuote 每日一言 | 3 | 5 | +2 |
| checkin 打卡完成 | 4 | 8 | +4 |
| travelReturn 旅行归来 | 3 | 8 | +5 |
| **新增** sleepy 困倦 | 0 | 8 | +8 |
| **新增** hungry 饥饿 | 0 | 8 | +8 |
| **新增** playing 玩耍 | 0 | 8 | +8 |
| **合计** | 28 | 83 | +55 |

### 2. 按宠物状态选择对话
新增 `getLineForState(state, personalityTags)` 函数：
- IDLE → greeting
- HAPPY → pet
- EATING → feed
- SLEEPY → sleepy
- HUNGRY → hungry
- PLAYING → playing

### 3. PetViewModel 接入
替换硬编码为 DialogueBank 调用：
- `speak("好舒服呀～")` → `speak(DialogueBank.pet(personalityTags))`
- `speak("真好吃～")` → `speak(DialogueBank.feed(personalityTags))`
- 启动问候用 `DialogueBank.greeting(personalityTags)`
- 新增：状态变化时触发对应对话

### 4. 悬浮窗增加语音气泡
PetOverlayService 中点击宠物时：
- 播放 SoundHelper 音效（保留）
- 新增：显示文字气泡 + TTS 播报
- 文案从 DialogueBank.pet() 随机选取

## 涉及文件
- `util/DialogueBank.kt` — 扩充文案池+新增场景函数
- `ui/screens/home/PetViewModel.kt` — 替换硬编码为DialogueBank调用
- `service/PetOverlayService.kt` — 增加语音气泡+TTS
