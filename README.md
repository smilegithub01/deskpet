<div align="center">

# 🐱 小团子 DeskPet

### 一只住在手机桌面上的治愈系电子宠物

**喂食 · 抚摸 · 装扮 · 旅行 · 写日记 · 陪你度过每一天**

[![Android](https://img.shields.io/badge/Android-8.0%2B-34A853?logo=android&logoColor=white)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![CI](https://github.com/smilegithub01/deskpet/actions/workflows/android-ci.yml/badge.svg)](https://github.com/smilegithub01/deskpet/actions)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-ff69b4.svg)](https://github.com/smilegithub01/deskpet/pulls)

</div>

---

> 🌟 **小团子**是一只永远在你屏幕上的小宠物。它会饿、会开心、会犯困，会在你忙碌时安静等待，在你回来时蹦蹦跳跳。它记得你每天和它的互动，会写日记，会在下雨天提醒你带伞，会在你心情不好时默默陪着你。

---

## ✨ 核心特性

### 🐾 真正活着的宠物

| 特性 | 描述 |
|------|------|
| 🍖 **喂食互动** | 4 种食物可选，喂食后饱腹度提升，宠物开心进食 |
| 🤚 **抚摸反馈** | 轻触宠物触发爱心粒子，亲密度随之增长 |
| 😊 **心情系统** | 5 档心情值，宠物表情和行为随心情实时变化 |
| 😴 **自动行为** | 宠物会自己走动、发呆、打盹，偶尔冒出气泡说话 |
| 📉 **离线衰减** | 关闭 App 后宠物会逐渐变饿，重新打开时记得"等了你很久" |

### 👗 装扮系统

- **48 件服饰** 全部 Canvas 矢量手绘，与宠物风格统一无割裂
- 6 大分类：帽子、眼镜、项圈、服装、尾巴、配饰
- 自由搭配，实时预览全身效果
- 钻石商店购买，稀有度分级

### 🏠 宠物小窝

- 2D 房间场景，6 类家具自由摆放
- 舒适度 / 趣味度 / 美观度三维属性影响宠物状态
- 家具同样 Canvas 矢量绘制，风格一致

### ✈️ 旅行放置

- 短途 / 中途 / 长途三档旅行时长
- 宠物出门后寄回**手写明信片**，附目的地风景
- 旅行归来带回礼物：钻石、限定装扮、食材
- 等级解锁更多目的地

### 📖 宠物日记

- 每天自动生成一篇宠物视角的日记
- 日记内容根据当天互动智能匹配模板
- "今天主人来找我玩了 5 次，我是全世界最幸福的小团子！"

### 🏆 成就 & 图鉴

- 三大类成就：互动 / 养成 / 探索
- 服饰图鉴、明信片图鉴、物种图鉴
- 解锁成就播放专属音效 + 钻石奖励

### 💬 语音 TTS

- Android 原生 TextToSpeech，中文语音
- 语速 0.8 + 音调 1.2，更可爱的萌系声音
- 8 大触发场景：启动、抚摸、喂食、打卡、旅行归来等
- 设备不支持时自动降级为纯文字气泡

### 🎨 社交分享

- 5 种分享卡片：每日状态 / 装扮搭配 / 成就解锁 / 旅行明信片 / 打卡成就
- 3 种模板风格：清新粉、治愈绿、简约白
- 1080×1080 高清 Bitmap 渲染，一键分享到社交平台

### 🌤️ 环境感知

- **天气联动**：晴天戴墨镜、雨天撑小伞、高温打盹
- **节日联动**：春节穿新衣、中秋送祝福、生日戴生日帽
- **每日一言**：Hitokoto API 随机一句话，宠物念给你听
- **农历节气**：二十四节气养生提示

### 💊 健康关怀

- 喝水 / 久坐 / 护眼三档健康提醒
- 打卡后宠物获得奖励，连续打卡有额外钻石
- 全部提醒完成时宠物进入兴奋状态 + 金色爱心粒子
- **经期联动**（默认关闭，数据仅存本地）：经期时宠物进入安慰模式，说话更温柔

---

## 📸 功能预览

<div align="center">

| 首页 · 与宠物互动 | 装扮 · 自由搭配 | 小窝 · 家具装饰 |
|:---:|:---:|:---:|
| 喂食、抚摸、拍照、心情选择 | 48 件矢量服饰实时预览 | 6 类家具自由摆放 |

| 旅行 · 明信片收集 | 日记 · 宠物视角 | 成就 · 图鉴解锁 |
|:---:|:---:|:---:|
| 放置旅行，寄回手写信 | 每日自动生成 | 三大图鉴收集 |

</div>

> 宠物全部使用 **Canvas 矢量绘制**，无图片资源依赖，任意分辨率下都清晰锐利。

---

## 🚀 快速开始

### 下载安装

**方式一：GitHub Actions 构建（推荐）**

前往 [Actions 页面](https://github.com/smilegithub01/deskpet/actions)，选择最近的成功构建，下载 `deskpet-debug-apk` 产物。

**方式二：本地编译**

```bash
# 克隆仓库
git clone https://github.com/smilegithub01/deskpet.git
cd deskpet

# 编译 Debug APK
chmod +x gradlew
./gradlew assembleDebug

# APK 生成在
# app/build/outputs/apk/debug/app-debug.apk
```

**方式三：Release 版本**

前往 [Releases 页面](https://github.com/smilegithub01/deskpet/releases) 下载已发布的 APK。

### 安装到手机

1. 将 APK 传到手机（微信 / U 盘 / ADB 均可）
2. 点击安装，如提示「未知来源」请允许
3. 打开 App，选择宠物种类和毛色，为它取个名字
4. 授予悬浮窗权限 —— 小团子就会出现在你的桌面上了 🎉

### 首次使用

```
打开 App → 选宠物（猫/狗/兔/仓鼠）→ 选毛色 → 取名 → 选性格
    → 授予悬浮窗权限 → 小团子浮在桌面上啦！
```

---

## 🛠️ 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin 1.9.22 |
| UI 框架 | Jetpack Compose + Material3 |
| 架构 | MVVM (ViewModel + StateFlow) |
| 数据库 | Room (SQLite) |
| 后台任务 | WorkManager |
| 网络 | OkHttp 4.12 |
| 图像渲染 | Android Canvas (矢量手绘) |
| 语音 | Android TextToSpeech |
| 导航 | Navigation Compose |
| 图片加载 | Coil |

### 技术规格

```
minSdk:      26 (Android 8.0)
targetSdk:   34 (Android 14)
compileSdk:  34
Java:        17
Gradle:      8.5
AGP:         8.2.2
```

---

## 📂 项目结构

```
deskpet/
├── .github/workflows/          # CI/CD 流水线
│   ├── android-ci.yml          # 自动构建 (push/PR 触发)
│   └── release.yml             # 发布 Release (tag 触发)
├── app/src/main/java/com/deskpet/app/
│   ├── DeskPetApplication.kt   # Application 入口
│   ├── MainActivity.kt         # 主界面
│   ├── data/
│   │   ├── model/              # 20+ 数据模型 (Pet, Outfit, Postcard...)
│   │   ├── db/                 # Room 数据库 + 13 个 DAO
│   │   └── repository/         # 数据仓库层
│   ├── service/
│   │   ├── PetOverlayService.kt    # 悬浮窗服务 (核心)
│   │   ├── PetBehaviorEngine.kt    # 行为引擎 (自动行为/状态切换)
│   │   ├── PetMemoryEngine.kt      # 记忆引擎 (日记生成)
│   │   ├── AchievementEngine.kt    # 成就引擎
│   │   ├── TravelEngine.kt         # 旅行引擎
│   │   ├── PeriodPhaseEngine.kt    # 经期阶段引擎
│   │   └── EnvApiService.kt        # 环境API (天气/一言)
│   ├── util/
│   │   ├── SoundHelper.kt          # 程序化音效合成 (13种)
│   │   ├── SpeechHelper.kt         # TTS 语音
│   │   ├── PhotoHelper.kt          # 拍照渲染
│   │   ├── ShareCardRenderer.kt    # 分享卡片渲染
│   │   ├── OutfitRenderer.kt       # 服饰矢量渲染
│   │   ├── FurnitureRenderer.kt    # 家具矢量渲染
│   │   ├── LunarCalendarHelper.kt  # 农历/节气计算
│   │   └── DialogueBank.kt         # 对话文案池
│   └── ui/
│       ├── components/          # PetCanvas, OutfitRenderer, RoomScene...
│       ├── screens/             # 10+ 页面
│       │   ├── home/            # 宠物主页
│       │   ├── dressup/         # 装扮
│       │   ├── decor/           # 小窝
│       │   ├── travel/          # 旅行
│       │   ├── diary/           # 日记
│       │   ├── codex/           # 图鉴
│       │   ├── companion/       # 共同养育
│       │   ├── health/          # 健康提醒
│       │   ├── settings/        # 设置
│       │   └── onboarding/      # 引导页
│       ├── theme/               # 颜色/字体/主题
│       └── navigation/          # 导航图
└── docs/                        # 设计文档 & 路线图
```

---

## 🗺️ 功能路线图

项目按 **L0 → L3** 四层递进设计，每层是下一层的基础：

| 层级 | 名称 | 状态 | 核心内容 |
|------|------|:---:|---------|
| **L0** | 基础修复层 | ✅ 完成 | 状态持久化、装扮矢量升级、拍照落地、音效完善 |
| **L1** | 情感引擎层 | ✅ 完成 | 宠物记忆日记、习惯养成联动、经期行为联动、环境感知 |
| **L2** | 内容丰富层 | ✅ 完成 | 家居装饰、旅行放置、成就图鉴、语音 TTS |
| **L3** | 社交传播层 | ✅ 完成 | 分享卡片、共同养育 (stub) |

> 详细设计文档见 [`docs/superpowers/specs/`](docs/superpowers/specs/)

---

## 🔧 本地开发

### 环境要求

- **JDK 17**（[Adoptium Temurin](https://adoptium.net/temurin/releases/?version=17)）
- **Android Studio** Hedgehog (2023.1.1) 或更高
- **Android SDK** Platform 34 + Build-Tools 34.0.0

### 用 Android Studio 打开

1. `File → Open` 选择项目根目录
2. 等待 Gradle Sync 完成（首次约 5-15 分钟）
3. 连接手机，点击 ▶ Run

### 命令行编译

```bash
# Debug APK
./gradlew assembleDebug

# Release APK（未签名）
./gradlew assembleRelease

# 运行单元测试
./gradlew testDebugUnitTest

# Lint 检查
./gradlew lintDebug
```

### 国内网络加速

项目已配置阿里云 / 腾讯云 Maven 镜像（仅本地开发环境生效，CI 自动切换官方源）。如遇下载超时，确认 `settings.gradle.kts` 中镜像配置存在。

---

## ❓ 常见问题

<details>
<summary><b>Gradle Sync 失败，提示下载超时</b></summary>

国内网络问题。项目已内置阿里云镜像，如仍失败请检查 `settings.gradle.kts` 中的镜像地址是否可达，或配置 VPN。
</details>

<details>
<summary><b>提示 "SDK location not found"</b></summary>

在项目根目录创建 `local.properties`（此文件已在 .gitignore 中，不会提交）：
```properties
# Windows
sdk.dir=C:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk
# macOS
sdk.dir=/Users/你的用户名/Library/Android/sdk
```
</details>

<details>
<summary><b>APK 安装后闪退</b></summary>

确认手机系统 ≥ Android 8.0（API 26）。查看日志：
```bash
adb logcat | grep DeskPet
```
</details>

<details>
<summary><b>编译报错找不到 import</b></summary>

尝试 `Build → Clean Project` → `Build → Rebuild Project`。KSP（Room 编译器）偶尔需要清理缓存。
</details>

<details>
<summary><b>悬浮窗不显示</b></summary>

前往 设置 → 应用 → 小团子 → 显示在其他应用上层，确保权限已开启。
</details>

---

## 🤝 参与贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/amazing-feature`
3. 提交更改：`git commit -m 'Add amazing feature'`
4. 推送分支：`git push origin feature/amazing-feature`
5. 提交 Pull Request

---

## 📄 许可证

本项目基于 [MIT License](LICENSE) 开源，可自由使用、修改和分发。

---

<div align="center">

**如果小团子让你会心一笑，给个 ⭐ Star 吧！**

Made with 💛 for everyone who needs a little companion

</div>
