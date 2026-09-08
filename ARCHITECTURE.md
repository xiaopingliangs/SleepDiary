# ARCHITECTURE.md — Android 技术架构

## 1. 技术栈

- Kotlin
- Android 原生
- Jetpack Compose
- Material 3
- Room
- DataStore 仅在有非备份关键状态的实际需要时可选；V0.1 当前不依赖它
- Coroutines / Flow
- Android Storage Access Framework
- Android Sharesheet
- FileProvider

不使用 Flutter / React Native / WebView 作为核心 UI。

## 2. SDK

- `minSdk = 26`
- `compileSdk` 与 `targetSdk` 使用开工时本机已安装的最新稳定 Android SDK，并在第一次可构建提交中固定。
- 依赖必须使用稳定版并锁定版本。
- JDK 使用 Android Studio 自带/推荐的 JBR，除非构建系统明确要求其他稳定 JDK。

## 3. Application ID

V0.1 默认：

```text
app.sleepdiary.offline
```

在第一次安装到真机后，不应随意修改 applicationId，否则 Android 会把它视为另一个应用。

## 4. 建议包结构

单 app module 即可，内部按职责分包：

```text
app.sleepdiary.offline
├── data
│   ├── db
│   ├── entity
│   ├── dao
│   ├── repository
│   ├── export
│   └── import
├── domain
│   ├── validation
│   ├── weekly
│   ├── safety
│   └── model
├── ui
│   ├── onboarding
│   ├── safety
│   ├── diary
│   ├── weekly
│   ├── history
│   ├── settings
│   ├── evidence
│   └── theme
└── share
```

不需要提前做多模块 Gradle 工程。

## 5. 数据

### Room
保存：
- SleepDiaryEntry
- SafetyAssessment
- SafetyAnswer
- WeeklySummarySnapshot
- AppState（单例/键值，含自身 BACKUP_1.0 格式版本；全部 backup-critical 状态）

要求：
- foreign keys 明确；
- schema export 开启；
- schema JSON 提交 Git；
- migration test。

### Room AppState
以下状态全部保存在 Room，并与业务数据同事务完整恢复：
- trackingStartDate；
- 一次性提示是否看过；
- currentSafetyAssessmentId；
- lastDisplayedWeeklyPeriodIndex。
- 其他需随完整备份恢复的持久状态。

当前无其他真正需要 DataStore 的状态，因此不引入 DataStore 依赖。

## 6. Repository

UI 不直接操作 DAO。

建议：
- `DiaryRepository`
- `SafetyRepository`
- `WeeklySummaryRepository`
- `BackupRepository`

保存流程必须由 repository/service 组合事务和回读验证。

## 7. 周总结

`WeeklySummaryCalculator` 是纯函数/可单元测试组件。

输入：
- 7 日历日边界；
- 对应 SleepDiaryEntry 列表。

输出：`WeeklySummaryDraft`

规则固定标记：`WEEKLY_1.0`

不得在该类中出现任何治疗建议。

## 8. 安全分流

`SafetyGateCalculator`：
- 输入原始 SafetyAnswer；
- 输出内部 gate enum；
- 规则版本化为 `SAFETY_RULE_1.0`。

它不输出诊断名称，不做概率计算。

## 9. 导入/导出

- JSON serializer 使用稳定、轻量方案；
- JSON Schema 与 Kotlin model 保持一致；
- CSV 用标准库/轻量实现即可，无必要引入大型数据框架；
- 完整 JSON 仅 RESTORE：完整 schema/语义校验、摘要、用户确认后，在单个 Room transaction 内替换记录和 AppState；失败回滚，提交后回读。
- 无 merge/duplicate/conflict 合并语义；恢复起点沿用备份，不重算。

## 10. 权限

Manifest 不应包含：

```text
android.permission.INTERNET
android.permission.ACCESS_NETWORK_STATE
android.permission.MANAGE_EXTERNAL_STORAGE
```

V0.1 正常功能不需要这些权限。

如果构建工具或第三方库间接要求危险权限，必须停止并解释。

## 11. 生命周期与退出

App 不依赖进程常驻。

- ViewModel state 只用于未提交表单；
- 正式数据一旦保存进入 Room；
- Activity 被系统销毁不能造成已保存数据损坏；
- 保存后退出使用 `finishAndRemoveTask()` 或等价正常 Android 任务 API；
- 不使用 `System.exit`；
- 不使用 `killProcess`。

## 12. 分享

- 分享卡片由 Compose/Canvas 或 Android 绘图生成 bitmap；
- 来源必须是已持久化的 entity；
- 写到 app cache；
- FileProvider；
- Sharesheet；
- hand-off 后结束 App task。

## 13. Git

若项目目录尚无 Git：

1. 初始化仓库；
2. 先提交**仅规格文件**：`chore: add approved V0.1 specifications`
3. 再创建 Android 工程。
4. 每个稳定里程碑单独提交。
5. 未经明确授权，不创建远程仓库、不 push。

## 14. Debug APK

V0.1 个人测试阶段构建：

```text
gradlew.bat assembleDebug
```

预期 APK：

```text
app/build/outputs/apk/debug/app-debug.apk
```

发布签名、应用商店和公开分发不属于 V0.1。

## 15. 执行环境

代码、Git、构建、测试和 APK 生成默认在用户本机完成，使用当前项目目录及 AGENTS 第 I 节列出的本地工具；真机验证使用 Android 真机。云端执行须先说明具体优势、原因与上传/使用内容，获得用户确认；不得为方便整体迁移项目。
