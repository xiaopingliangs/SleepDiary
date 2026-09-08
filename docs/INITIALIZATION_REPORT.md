# Android 初始化验证 — 2026-09-09

## 范围
单 app 模块、单 Activity 最小标题界面、五个 Room entity、DAO、事务写入/回读仓库骨架、集中版本常量和本地测试。
未实现完整 UI、安全问询或周总结页面、导入导出、分享图片、治疗计算。CBT-I Protocol 保持 NONE。
本轮只验证用户授权的初始化范围，不代表全部 P0 或真机最终验证完成；冻结规格文件哈希全部保持不变。

## 本机环境
- 项目：D:/Projects/SleepDiary
- Git：2.55.0.windows.5（D:/Program Files/Git/cmd/git.exe）
- Android Studio：AI-261.26222.65.2614.16204760
- JBR：25.0.3+-15898627-b508.16，D:/Program Files/Android/Android Studio/jbr
- java 未加入 PATH；构建进程直接指定以上 JAVA_HOME。
- SDK：D:/Projects/androidsdk
- 已安装稳定 Platform：Android 17 / API 37.0，revision 2，PreviewSdkInt=0
- Build Tools：36.0.0；Platform Tools：37.0.1-15733141；adb：1.0.41，可用。
- 未安装额外 JDK、WSL、Flutter 或其他开发平台。只下载 Gradle/构建及测试依赖。
- Git 身份仅在本仓库配置；未设置远程仓库、未 push。

## 固定版本
minSdk 26；compileSdk / targetSdk 37（本机 Platform 37.0）；applicationId app.sleepdiary.offline。
AGP 9.1.1；Gradle Wrapper 9.3.1（带官方 SHA-256）；Kotlin / Compose Compiler 2.2.10；KSP 2.3.6。
Compose BOM 2025.08.01；Compose UI 1.9.0；Material 3 1.3.2；Room 2.8.4。
JUnit 4.13.2；Robolectric 4.16.1（本地测试模拟 API 28，未使用真机）；AndroidX Test Core 1.7.0。
依赖使用 gradle/libs.versions.toml；领域版本只在 version.properties 定义，再生成 BuildConfig/Versions。

## 验证结果
- testDebugUnitTest：通过，8 tests，0 failures，0 errors，0 skipped。
- assembleDebug：通过。
- 覆盖：建库、日记完整字段 UUID 回读、问询及答案关联、周报持久化、AppState、重复写入拒绝、外键错误事务回滚、数据库关闭重开。
- Room schema：app/schemas/app.sleepdiary.offline.data.db.SleepDiaryDatabase/1.json，技术版本 1，5 张表，字段与规格核对一致，纳入版本控制。
- merged manifest 与 APK 均无 INTERNET / ACCESS_NETWORK_STATE / MANAGE_EXTERNAL_STORAGE。
- APK 仅有 AndroidX 应用专属 DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION，无网络/广域存储权限。
- 自动备份与设备迁移禁用；运行时依赖未发现预发布版本。
- APK：D:/Projects/SleepDiary/app/build/outputs/apk/debug/app-debug.apk
- APK SHA-256：8859c49106f988c96101fe3a27f0c427e2053e9fea8c0f9f6f9df39bd0ab38ea
- 规格基线 commit：555cfcf97794a8e7b0014c4c397753b7fda5d235

## 复现
在本机 PowerShell 中设置 JAVA_HOME 为上述 JBR，GRADLE_USER_HOME 为 D:/Projects/SleepDiary/work/gradle-home，然后依次执行：
```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```
测试报告：app/build/reports/tests/testDebugUnitTest/index.html。
原始日志保存在未纳入 Git 的 work/verification.log。

## 限制
无已知阻塞问题或需另行批准的规格变更。
构建有非阻塞提示：SDK XML 元数据版本、JBR 原生访问提示、Compose 图形库未剥离调试符号；测试及打包均成功。未为消除提示安装 NDK 或额外 JDK。
真机安装、全部 P0、数据校验及业务流程属于后续阶段。本轮停止。
