# 桌面宠物 · 小团子 — Android 编译指南

## 一、你需要准备什么

### 1. 安装 JDK 17（必须）

这是编译 Android 项目的基础。

**Windows:**
- 下载 Oracle JDK 17 或 OpenJDK 17: https://adoptium.net/temurin/releases/?version=17
- 安装时勾选「设置 JAVA_HOME 环境变量」
- 安装后打开命令行验证: `java -version` 应显示 17.x.x

**macOS:**
```bash
brew install openjdk@17
sudo ln -sfn $(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

### 2. 安装 Android Studio（必须）

- 下载地址: https://developer.android.com/studio
- 选择你的系统版本（Windows / Mac / Linux）
- 安装时保持默认选项即可
- 首次启动会自动下载 Android SDK

### 3. 下载 Android SDK

Android Studio 首次启动后会引导你下载 SDK:
- 打开 Android Studio → Tools → SDK Manager
- 勾选 **Android 14.0 (API 34)** — 这是编译目标
- 确保勾选 **Android SDK Build-Tools 34**
- 确保勾选 **Android SDK Platform-Tools**
- 点 Apply 下载（约 2-3 GB）

---

## 二、打开项目

### 方式一: 用 Android Studio 打开（推荐）

1. 解压 `DeskPet.zip` 到任意目录（路径中不要有中文和空格）
2. 打开 Android Studio
3. 选择 `File → Open`
4. 选择解压后的 `DeskPet` 文件夹（注意是包含 `settings.gradle.kts` 的根目录）
5. 等待 Gradle Sync 完成（首次会下载依赖，约 5-15 分钟，取决于网速）
6. 如果提示下载 Gradle 8.5，点确认下载

### 方式二: 命令行编译

如果你不想用 Android Studio 的 IDE，可以直接用命令行:

**Windows:**
```cmd
cd DeskPet
gradlew.bat assembleDebug
```

**macOS / Linux:**
```bash
cd DeskPet
chmod +x gradlew
./gradlew assembleDebug
```

首次运行会自动下载 Gradle 8.5 和所有依赖。编译成功后 APK 在:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 三、编译生成 APK

### 在 Android Studio 中

1. 顶部菜单: `Build → Build Bundle(s) / APK(s) → Build APK(s)`
2. 等待右下角进度条完成
3. 完成后点通知中的 `locate` 找到 APK 文件
4. APK 路径: `app/build/outputs/apk/debug/app-debug.apk`

### 命令行

```bash
# Debug APK
./gradlew assembleDebug

# Release APK（需要签名，暂时用 debug 签名）
./gradlew assembleRelease
```

---

## 四、安装到手机

### 方法一: ADB 安装（推荐）

1. 手机开启「开发者选项」和「USB 调试」
   - 设置 → 关于手机 → 连续点击「版本号」7 次
   - 设置 → 系统 → 开发者选项 → 开启 USB 调试
2. USB 连接电脑
3. 命令行执行:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 方法二: 直接传文件安装

1. 把 `app-debug.apk` 发到手机（微信/QQ/U盘均可）
2. 手机点击安装
3. 如果提示「未知来源」，允许安装即可

### 方法三: Android Studio 直接运行

1. USB 连接手机
2. Android Studio 顶部设备下拉框选择你的手机
3. 点绿色三角形 ▶ Run 按钮

---

## 五、首次使用

1. 打开 App → 进入引导页
2. 选择宠物种类（猫/狗/兔/仓鼠）
3. 选择毛色（6 种颜色）
4. 为宠物命名 + 选择性格
5. 点击完成后会弹出悬浮窗权限申请
6. 点击「允许」→ 跳转系统设置 → 开启「显示在其他应用上层」
7. 返回 App → 小团子就浮在桌面上了

---

## 六、常见问题

### Q: Gradle Sync 失败，提示下载超时

A: 国内网络可能无法直接访问 Google Maven 仓库。在 `settings.gradle.kts` 中加上阿里云镜像:

```kotlin
pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
    }
}
```

### Q: 编译报错 "Kotlin version mismatch"

A: 确保 Android Studio 版本 >= Hedgehog (2023.1.1)。旧版 Android Studio 不支持 Kotlin 1.9.x。

### Q: 提示 "SDK location not found"

A: 创建 `local.properties` 文件在项目根目录:
```properties
sdk.dir=C:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk
```
macOS: `sdk.dir=/Users/你的用户名/Library/Android/sdk`

### Q: 编译报错找不到某个 import

A: 试一下 `Build → Clean Project`，然后 `Build → Rebuild Project`。如果仍然报错，把错误信息发出来，我帮你修。

### Q: APK 安装后闪退

A: 确认手机系统 >= Android 8.0（API 26）。查看日志:
```bash
adb logcat | grep DeskPet
```

---

## 七、项目结构

```
DeskPet/
├── settings.gradle.kts          # Gradle 配置
├── build.gradle.kts             # 项目级构建
├── gradlew / gradlew.bat        # Gradle 命令行脚本
├── gradle.properties            # Gradle 属性
├── gradle/wrapper/              # Gradle Wrapper
├── README.md                    # 本文件
└── app/
    ├── build.gradle.kts         # App 模块构建（依赖、SDK版本）
    ├── proguard-rules.pro       # 代码混淆规则
    └── src/main/
        ├── AndroidManifest.xml   # 应用清单（权限、组件声明）
        ├── res/                  # 资源文件
        │   ├── values/           # 颜色、字符串、主题
        │   ├── drawable/         # 图标
        │   ├── mipmap-anydpi-v26/ # 自适应图标
        │   └── xml/              # 备份规则
        └── java/com/deskpet/app/
            ├── DeskPetApplication.kt    # Application 入口
            ├── MainActivity.kt          # 主界面入口
            ├── data/
            │   ├── model/       # 数据模型（10个文件）
            │   ├── db/           # Room 数据库
            │   └── repository/  # 数据仓库
            ├── service/
            │   ├── PetOverlayService.kt    # 悬浮窗服务（核心）
            │   └── PetBehaviorEngine.kt     # 行为引擎
            ├── util/
            │   ├── PermissionHelper.kt     # 权限工具
            │   └── NotificationHelper.kt   # 通知工具
            └── ui/
                ├── theme/        # 颜色、字体、主题
                ├── components/   # 公共组件
                ├── navigation/   # 导航
                └── screens/      # 四个页面
                    ├── home/      # 宠物主页
                    ├── dressup/   # 装扮
                    ├── health/    # 健康
                    ├── settings/  # 设置
                    └── onboarding/ # 引导页
```

---

## 八、技术规格

| 项目 | 版本 |
|------|------|
| Kotlin | 1.9.22 |
| Compose Compiler | 1.5.8 |
| Compose BOM | 2024.02.00 |
| AGP (Android Gradle Plugin) | 8.2.2 |
| Gradle | 8.5 |
| minSdk | 26 (Android 8.0) |
| targetSdk | 34 (Android 14) |
| compileSdk | 34 |
| Java | 17 |

## 九、如果遇到编译错误

项目代码是 AI 生成的，可能存在少量编译问题。如果遇到报错:

1. 先试 `Build → Clean Project` → `Build → Rebuild Project`
2. 看错误信息，常见的修复方式:
   - **import 缺失**: 根据报错补上对应 import
   - **API 版本问题**: 某些 API 在低版本不可用，加 `@RequiresApi` 注解
   - **参数不匹配**: 检查函数签名
3. 把完整错误信息发给我，我来帮你修代码

祝你编译顺利，女朋友会喜欢的~
