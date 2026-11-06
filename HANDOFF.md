# SleepDiary — AI 接手入口

更新：2026-11-02（Asia/Shanghai）。先读本文，再读 [AGENTS.md](AGENTS.md)。本文是当前状态导航，不替代获批规格，也不授权新增功能。

## 1. 项目的意义与当前边界

SleepDiary 帮助用户用尽量少的交互长期保存主观睡眠事实，主动查看中性趋势、了解睡眠知识、保存个人笔记，并能把自己的记录备份或分享。重点是数据可靠、主观估计、完全离线和低打扰，不要求夜间看钟或追求分钟级精确。

当前产品是 Public Edition 的设备预览开发版：日记、历史统计、教育、个人记录与导出。完整 CBT-I 曾做架构规划及部分工程基础，但当前协议始终为 `NONE`；不是已完成的治疗产品。禁止自行补诊断、治疗用 TST/SE、睡眠窗口、药物建议或医疗 AI。不能把教育完成、Safety 历史或备份恢复解释为治疗授权。

运行时无账号、广告、云同步、遥测或网络权限；用户主动导出/分享是数据离开私有存储的路径。官方来源链接由用户主动交给系统浏览器打开。禁止添加中医、草药等内容。其他约束以 AGENTS 和 CLINICAL_BOUNDARIES 为准。

## 2. 先确认当前代码，而不是只看 Git HEAD

2026-11-02 核对：目录 `D:\Projects\SleepDiary`，分支 `master`，HEAD `7b7de395a9fc69a598cb14a6d2fb2b192c4b0385`，为 10 月 3 日 Public UX 2.0 报告提交。

**10 月 27 日分享增量仍在工作树，尚未提交。仅 checkout HEAD 会丢失这些功能。** 其规格、实现、测试和报告均存在已修改/未跟踪文件。当前交接文档也未提交。不要 reset、clean 或把未提交内容当垃圾。

另有 `meditation/` 和 `SleepDiary.zip` 未跟踪内容。meditation 是独立工作；zip 的内容及用途本轮未检查。不要修改、删除、暂存或误当最新版备份。`work/`、`app/build/` 被忽略，只在本机可用，单纯 Git 克隆不会携带测试 XML、示例 PNG 和 APK。

版本：App 0.2.2 / versionCode 4；SPEC-0.2.2；Room 7；BACKUP_3.1；DIARY_1.0、WEEKLY_1.0、WORKSPACE_1.0、EDUCATION_DATA_1.0、EDUCATION_EXPORT_1.0；CBT-I NONE。同一个 0.2.2 已产生多个不同 APK，必须用哈希区分。

## 3. 本地阅读顺序和文件用途

1. [AGENTS.md](AGENTS.md)：最高仓库约束、禁止行为、受保护规格列表和本机优先规则。
2. [PUBLIC_PRODUCT_SPEC.md](PUBLIC_PRODUCT_SPEC.md)、[CLINICAL_BOUNDARIES.md](CLINICAL_BOUNDARIES.md)：当前公开版范围和医学边界。Public 文档正文也包含旧基线，需同时读末尾 UX 2.0 增量。
3. [docs/SHARING_UPDATE_20261027.md](docs/SHARING_UPDATE_20261027.md)：最近代码阶段、获批范围、测试与 APK。
4. [docs/PUBLIC_UX_2.0_DEVICE_PREVIEW_PROGRESS.md](docs/PUBLIC_UX_2.0_DEVICE_PREVIEW_PROGRESS.md)：10 月 3 日晚修复后的预览；比同日 P5/P5D 报告更新。
5. [PUBLIC_ARCHITECTURE.md](PUBLIC_ARCHITECTURE.md)、[ARCHITECTURE.md](ARCHITECTURE.md)：职责边界和技术架构；实际版本以 version.properties、数据库声明和构建文件核对。
6. 涉及数据必读 [DATA_SCHEMA.md](DATA_SCHEMA.md)、[IMPORT_EXPORT_SPEC.md](IMPORT_EXPORT_SPEC.md)、[EDUCATION_USER_DATA_CONTRACT.md](EDUCATION_USER_DATA_CONTRACT.md)；涉及测试读 [TEST_PLAN.md](TEST_PLAN.md)。
7. 涉及教育读 [EDUCATION_SPEC.md](EDUCATION_SPEC.md)、[EDUCATION_CONTENT_ARCHITECTURE.md](EDUCATION_CONTENT_ARCHITECTURE.md)、[CONTEXTUAL_EDUCATION_RULES.md](CONTEXTUAL_EDUCATION_RULES.md)、[V4_PUBLIC_CONTENT_MATRIX.md](V4_PUBLIC_CONTENT_MATRIX.md)。
8. [docs/DEVELOPMENT_PROGRESS.md](docs/DEVELOPMENT_PROGRESS.md) 是阶段索引；[docs/PUBLIC_EDITION_RELEASE_BLOCKERS.md](docs/PUBLIC_EDITION_RELEASE_BLOCKERS.md) 与 [docs/PUBLIC_EDITION_P5D_DEVICE_ACCEPTANCE.md](docs/PUBLIC_EDITION_P5D_DEVICE_ACCEPTANCE.md) 保存未闭环验收与版权问题。

`docs/FULL_APP_*`、协议/临床桥接/例外架构是未来治疗工程规划及历史阶段材料，不是当前 UI 功能清单或实施授权。完整路线的 M01–M12 待人工审核项见 [docs/FULL_APP_IMPLEMENTATION_ROADMAP.md](docs/FULL_APP_IMPLEMENTATION_ROADMAP.md)。不同文档的 P0/P1 等阶段编号可能属于不同路线，不可混用。历史报告的“当前”“下一步”和 PASS 只对应该报告时间及代码。

## 4. 实际架构与代码地图

单 Gradle `:app`、单 `MainActivity`，Kotlin + Compose + Material 3 + Navigation Compose + Room + Coroutines。minSdk 26，compile/target 37，JVM 编译目标 17。无 WebView 核心 UI，无 DataStore 依赖。

代码前缀为 `app/src/main/java/app/sleepdiary/offline/`：

| 职责 | 入口与关键文件 |
|---|---|
| 依赖装配 | `SleepDiaryApplication.kt` → `application/AppContainer.kt` → `AppServices.kt`；同一惰性 Room 数据库 |
| 当前主 UI | `MainActivity.kt` → **`ui/PublicSleepDiaryApp.kt` 中的 SleepDiaryApp**；不要只因文件名去改旧 `ui/SleepDiaryApp.kt` |
| 四底部导航 | `ui/publicnav/PublicShell.kt`：今日、历史、了解、我的；内部 route 仍为 today/sleep/education/me |
| 日记录入 | `ui/diary/DiaryViewModel.kt`、DiaryForm/DiaryScreen/DiaryPickers；表单草稿与持久化数据分离 |
| 日记存储 | `data/repository/DiaryUiStore.kt`、DiaryRepository；事务保存、提交后回读 |
| 滚动统计 | `domain/weekly/RecentAverageCalculator.kt`；当前本地日期含当日的 7/14 日，不读旧周快照 |
| 固定周期周总结 | WeeklySummaryCalculator、PeriodCalculator、WeeklySummaryRepository；保留修订历史，与滚动统计不同 |
| 日记/统计分享 | `share/DiaryShare.kt`、RecentDiaryShare；数据库读取 → 本机 PNG → FileProvider → 系统 Sharesheet → 正常结束任务 |
| 教育 | EducationRegistryLoader/Registry；`ui/education/PublicEducationExperience.kt`；EducationRepository 保存明确提交的状态 |
| 本地情境匹配 | ContextualEducationMatcher；最多一个预编写体验 ID，可关闭，无医疗决策或用户数据写入权限 |
| 个人记录 | WorkspaceRepository；教育笔记关联 WorkspaceRecord，不复制正文、不分析用户文字 |
| 备份 | `data/backup/BackupRepository.kt`、BackupCodec、BackupSchemaValidator、PreparedBackupStore、SafDocumentWriter |
| 数据库 | `data/db/SleepDiaryDatabase.kt`；15 个实体，1→7 显式迁移；`app/schemas/` 保留 1–7 历史 |

Room 保存日记、Safety、周总结、AppState、Workspace、InputSnapshot、本机恢复状态、休眠 Treatment episode/audit，以及 Education 的进度、已确认选择、复习、关联笔记、恢复位置五表。内容 registry 随 APK 打包且有稳定 ID/版本/hash，不把整份教育内容写进 Room。

完整 JSON 恢复只有 RESTORE：先完整校验、摘要和用户确认，再单事务替换备份业务集合与 AppState，失败回滚，提交后回读。兼容 BACKUP_1.0/1.1/2.0/2.1/3.0/3.1；旧格式缺少的教育集合恢复为空，不推断。LocalExecutionState 不由备份授予执行许可。禁止破坏性迁移。

日常入口标记和情境匹配开关为本机非备份关键 SharedPreferences；正式业务数据与需完整恢复的状态在 Room。阅读不等于完成；EducationPosition 可记录继续阅读位置，但不记录浏览轨迹，完成/选择/笔记须明确提交。

## 5. 最新可用功能与已解决问题

| 阶段/问题 | 当前实现与证据状态 |
|---|---|
| 日记核心 | 八项主观字段及可选特殊情况；历史查看/明确更正；事务与回读；CSV、JSON 恢复、图片分享 |
| 10-03 公共界面 | 今日/历史/了解/我的；取消首次说明阻挡；导出集中在我的；修复表单与底层 NavHost 重叠 |
| 时间输入 | 两个时钟锚点加相对分钟，换算回原绝对时间；跨午夜处理有自动化覆盖 |
| 旧异常记录导致备份失败 | `175921e` 后可解析旧行无损备份，原始数据不改；无效行不进入统计，陈旧周快照以 revision 替代；自动化修复通过，修复后完整真机验收未记录 |
| SAF 主线程问题 | `cd7c162`：文件选择器显式 Main 调度，有回归 |
| 教育交互 | 四个默认原生体验、可选知识地图、来源、继续阅读、明确完成/选择、复习和个人记录；不启用治疗 |
| 10-27 分享增量 | 今日已保存记录和保存成功页直接分享；特殊情况默认包含，空白显示“无记录”；无额外确认 |
| 10-27 统计分享 | 历史选中 7/14 天可生成图片；非空特殊情况按日期列出，空值略过；不再保存后自动展示周总结 |
| 10-27 统计截止日错误 | 分离当前 referenceDate 与旧记录表单日期；进入历史和分享重新读库；编辑旧日记不把 today 指向旧记录 |

## 6. 验证证据与 APK

2026-11-02 本轮仅整理文档、读取代码/状态、汇总已有 XML 和重新核对 APK 哈希；没有重新执行构建或测试，没有操作手机。

- 10-27 全量 XML：`work/sharing-full-tests-20261027/`，本轮读取确认 51 类、480 tests、0 failures/errors/skipped。
- 后补长图测试：`work/sharing-render-tests-20261027/`，6 tests、0 failures/errors/skipped。它与全量包含重复测试，**不能说有 486 个独立测试或最终全量 486 PASS**。
- 10-27 `assembleDebug`、`assembleDebugAndroidTest` 成功；编译测试 APK 不等于执行真机测试。
- 合成图片：`work/sharing-preview-20261027/` 下 daily.png、recent-7.png、recent-14.png；上轮目视检查单日与 14 日图片，无裁切。不是实际手机截图。
- APK：`app/build/outputs/apk/debug/app-debug.apk`；11,724,842 字节；SHA-256 `FF427CB97EEF15CF94EEDE182C6D87AC07301317133F4F1BCCD8D7366B5C64E4`，本轮重新核对一致。
- 旧 10-03 472 项与旧 APK 哈希是历史快照；不能用于给后续修改背书。当前仍是 Debug 设备预览包，不能称正式稳定发布。

## 7. 未解决、未验收和需核查项

1. **真机未闭环**：S24 上本轮三个分享入口、系统接收端读取、取消/返回、长备注；备份非空导出及安全隔离状态下完整 round-trip；中文 IME、进程死亡、旋转、离线、深浅色/大字体/无障碍等仍需按测试计划补齐。10-03 P5D 真实验收曾 FAIL，后续自动化修复不自动把真机结果改成 PASS。
2. **教育版权阻塞**：V4 来源及改编单元许可链仍未清理；54 BLOCKED、40 FUTURE_TREATMENT_ONLY 不可启用。具体来源风险以 release blockers 和 content matrix 为准，不能推断公开链接等于授权。
3. **治疗未启用**：完整规则、适用性、指标、临床审核和 M01–M12 等门槛未闭环。不可因目录中有 Treatment 类就宣传治疗功能。
4. **未提交状态**：10-27 实现及本文都未形成 Git 提交；移交/复制时必须包含工作树与新文件。若以后整理提交，只暂存明确属于本阶段的文件，不包括 meditation 或未知 zip。
5. **规格历史漂移**：用户明确批准过 10-27 四份受保护规格的相关条款同步；该批准不是以后随意改全部规格的长期授权。SPEC_MANIFEST 当时只刷新五份相关文件，未证明全清单一致；许多文档前部仍写 Room 6/旧导航等历史基线。遇到真正冲突须报告，不自行改规则。
6. **待验证工程疑点（未复现，不标已修复）**：取消自动周展示后，ViewModel 不再推进 lastDisplayedWeeklyPeriodIndex；WeeklySummaryRepository.pendingAfterSave 仍按该标记选最早未展示周期。建议以合成数据验证“连续多个周期末日漏记、随后补记”是否会反复命中旧 summary，影响后续固定周期总结生成。滚动 7/14 天统计独立计算，不依赖此字段。本次交接不修改代码。

## 8. 本机接手操作

先执行只读检查并与本文比较：

```powershell
Set-Location D:\Projects\SleepDiary
git status --short --branch
git log -8 --oneline
git diff --stat
Get-Content version.properties
Get-FileHash app/build/outputs/apk/debug/app-debug.apk -Algorithm SHA256
```

按当前任务读必要规格；不要看到历史“下一步”就自动开始治疗/P6。最近已完成需求仅是三项分享与统计调整，用户还未给出新功能授权。

需要重新验证代码时的已使用本机命令：

```powershell
$env:JAVA_HOME='D:/Program Files/Android/Android Studio/jbr'
$env:GRADLE_USER_HOME='D:/Projects/SleepDiary/work/gradle-home'
./gradlew.bat testDebugUnitTest assembleDebug assembleDebugAndroidTest --offline --console=plain
```

Android SDK：`D:/Projects/androidsdk`（同时核对 local.properties）；ADB：其 platform-tools/adb.exe。路径不可用时先检查本机安装，不自行迁到云端。Gradle 曾受沙箱目录访问限制，正常申请所需执行权限，不绕过审批；离线依赖不足时按仓库规则处理。不要对真实用户库执行清空、卸载或破坏性恢复测试；先确认有效外部备份，测试优先隔离数据。

每阶段完成或长任务中断前，更新对应报告和 DEVELOPMENT_PROGRESS：范围、文件、Git 状态、实际测试/构建、APK 路径/哈希、未做项、下一步。代码变化后不得继续沿用旧 PASS。本交接不更改 AGENTS 或医学/数据规则。
