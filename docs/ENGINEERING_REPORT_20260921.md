# SleepDiary 工程汇报

报告日期：2026-09-21。项目：D:/Projects/SleepDiary。

## 1. 交付结论与范围

本轮交付是 **0.2.2 开发构建：离线睡眠日记 + 个人记录 + 就诊摘要 + 学习说明**，不是已完成的完整版 CBT-I 治疗 App。治疗协议仍为 NONE。没有将教育短文、试验规则、医生原文或已恢复的备份当作治疗执行许可。

用户希望的最终完整版仍缺完整治疗闭环，详见第 7 节。本报告不把架构规划、候选算法测试或 APK 编译成功等同于临床功能已完成。

## 2. 当前工程与功能

单 app 模块；Kotlin + Compose + Material 3 + Room，单 Activity。应用级 AppContainer 装配数据库与服务，界面通过 ViewModel/Repository 访问数据。没有服务器或云端组件。

| 范围 | 已实现内容 | 限制 |
|---|---|---|
| 日记 | 八个核心字段、估计提示、跨日期时间选择、可选 comment、保存后事务回读、历史显示和编辑 | 主观记录；无治疗 TST/SE，无疗效评级 |
| 校验 | 时间顺序、非负数和规格上限、交叉不一致确认 | 不自动改写 SOL/WASO |
| 安全问询 | DRAFT/COMPLETED、S09 多选规范、既有分流、逐题说明、设置入口返回 | 分流不是诊断，也不是治疗准入许可 |
| 周总结 | 7 日历天周期、缺失日不按零、起点冻结、同/跨周期 revision | 第 30 天不额外生成月报；已有四个完整七日周期，余下日期属于下一周期 |
| 完整备份 | BACKUP_2.1 导出；兼容 1.0/1.1/2.0/2.1；schema/语义校验、预览确认、事务替换与回读 | 明文、用户选择位置；不合并；失败回滚 |
| CSV | 现有日记列和 comment 转义 | 不自动加入个人记录、安全或治疗数据 |
| PNG 分享 | 从已保存记录回读生成 PNG，FileProvider content URI、临时权限、Sharesheet 交接后正常结束 task | 明确不含 comment、安全问询、个人记录 |
| 就诊摘要 | 按日期选取、预览、主动 TXT 导出；备注与当前 Safety 独立选择 | 默认不含敏感附加项；不诊断、不生成处方、不核验医生身份 |
| 个人记录 | 学习笔记/想法/现实情境/就诊沟通原文；保存、回读、编辑 revision、冲突保护、草稿恢复 | 不从原文生成医学结论；只有完整备份包含这些记录 |
| 本轮学习入口 | 六条来源可追溯短文；目录/详情/个人记录/返回；主动浏览官方来源 | 没有学习完成评分或治疗进度授权；不是完整 CBT-I 课程 |
| 治疗工程准备 | 原始输入捕获、不可变快照、哈希校验、本机恢复代次、激活前检查、候选规则提取 | 没有激活成功分支；候选窗口不进入用户治疗界面 |

历史页、方法与依据、术语表、版本、隐私与设置导航已有独立页面。关于完整 UI、中文 IME、系统分享和文件提供器的本次真机状态见第 6 节。

## 3. 本轮新增与修复

- `ui/learning/LearningContent.kt`：六条教育说明，文本版本 EDUCATION_TEXT_1.0，来源中英文名称/年份/实页；认知页同时包含驾驶困倦风险说明。
- `ui/learning/LearningScreen.kt`：按需阅读，不预填答案、不写临床完成状态；所选页面可经 SavedState 恢复。
- `ui/SleepDiaryApp.kt`：设置中的学习入口与独立 learningNotes 路由。
- `ui/workspace/WorkspaceScreen.kt`：按来源显示“返回学习内容”或“返回设置”，保留草稿放弃确认。
- `ui/MethodsScreen.kt`：登记 VA 2014 手册官方来源，主动打开浏览器仍通过来源白名单。
- `LearningFlowTest.kt`：7 项新增回归，包含真实 Room 写入、编辑、备份恢复、隐私、导航及页面状态恢复。
- `DiaryUiStore.kt`：可注入保存服务时钟，生产默认仍为系统时钟；不改变周算法。
- `CommentBackupTest.kt`：固定 ViewModel 与 Repository 使用同一天，并给异步状态等待加超时。旧测试原来使用固定页面日期和真实数据库服务日期；时间推进后进入周报却无限等待 SUCCESS。本次通过线程栈定位，不删除测试、不放宽保存断言。

测试开发中还修正了 Kotlin 测试方法返回非 Unit 的 JUnit 初始化错误，以及把不可滚动对话框误当滚动页面点击、未等待 Compose 回调更新就触发系统返回的测试问题。

## 4. 数据、迁移和隐私

Room 5 共八张表：SleepDiaryEntry、SafetyAssessment、SafetyAnswer、WeeklySummarySnapshot、AppState、WorkspaceRecord、InputSnapshot、LocalExecutionState。前七张业务表进入完整备份；LocalExecutionState 是本机执行元数据，成功恢复时同事务生成新代次并要求复核，不从备份继承。

Room 1→2→3→4→5 迁移和历史 schema 保留，没有 destructive migration。本轮不改表、不改备份或医学版本，不触碰独立未跟踪的 meditation/。

学习笔记使用既有 Workspace 保存路径；阅读或取消不会自动写入。正文进入完整 JSON，但不会进入日记 PNG/CSV/就诊摘要。分享 PNG 继续排除日记 comment 与 Safety，和用户最终确认的隐私边界一致。

Manifest 必须无 INTERNET、ACCESS_NETWORK_STATE、MANAGE_EXTERNAL_STORAGE；官方链接交给系统浏览器不等于 App 自身联网。没有新增分析、广告、云或 AI 依赖。导入/导出继续使用 SAF，不静默写公共目录。

## 5. 版本与构建环境

| 项目 | 实际配置 |
|---|---|
| App / applicationId | 0.2.2，versionCode 4 / app.sleepdiary.offline |
| SPEC / Room | SPEC-0.2.2 / 5 |
| Diary / Safety / Safety Rule | DIARY_1.0 / SAFETY_1.0 / SAFETY_RULE_1.0 |
| Weekly / Workspace / Backup | WEEKLY_1.0 / WORKSPACE_1.0 / BACKUP_2.1 |
| CBT-I Protocol | NONE |
| SDK | min 26，compile/target 37，build-tools 36.0.0 |
| Gradle / AGP | 9.3.1 / 9.1.1 |
| Kotlin / KSP | 2.2.10 / 2.3.6 |
| Compose BOM / Room | 2025.08.01 / 2.8.4 |
| Git | 2.55.0.windows.5 |
| 实际 Java | Android Studio 随附 JBR，OpenJDK 25.0.3；编译目标 Java/JVM 17 |
| SDK 路径 | D:/Projects/androidsdk |
| JBR 路径 | D:/Program Files/Android/Android Studio/jbr |
| Gradle 缓存 | D:/Projects/SleepDiary/work/gradle-home |

本轮使用本机环境，没有安装额外工具、迁移云端、创建远程或 push。App/SPEC 保持原值，须以本报告的 Git commit 与 APK SHA-256 区别同版本开发包。

## 6. 验证证据

最终全量结果、APK 哈希和代码提交记录在本文末尾的“交付校验记录”。9 月 15 日旧日志没有测试结束结论；9 月 21 日首次全量运行定位到旧测试无限等待后主动停止了该测试进程，该次不能算通过。最终结果以修复后的独立日志为准。

新增 7 项覆盖：全目录阅读/返回不写库；学习入口的个人记录保存/编辑/完整备份恢复/导出隔离；取消不保存；系统返回；SavedState 页面恢复；主动官方链接与未知链接拒绝；稳定 ID 和页码。

既有全量测试覆盖日记校验/事务、Safety 分支、七日周期/revision、备份 round-trip/回滚/旧格式、Room 迁移、SAF 回调、分享白名单、个人记录、不可变快照与恢复代次。通过测试不等于穷尽所有可能故障。

2026-09-21 adb 未发现连接设备。本次没有安装 APK 或执行 connectedAndroidTest。assembleDebugAndroidTest 只构建测试 APK，不代表设备测试通过。中文 IME 组合输入、S24 系统文件选择器/第三方提供器、完整 Sharesheet 交接、真实杀进程/旋转、大字体/TalkBack 仍需最终真机验收；SavedState 和 Room 重开测试不能冒充这些验证。

## 7. 完整 CBT-I 版尚未完成的工作

| 模块 | 当前状态 | 仍需完成 |
|---|---|---|
| 治疗指标/基线 | 原始日记可用，捕获/快照已实现 | 独立治疗指标定义、有效样本、适用性和缺失数据规则 |
| 初始窗口/周度调整 | 主手册部分算法已提取并做候选测试，未接入运行 | 完整输入和上下界、适用条件、人工判断、独立判例、原子计划提交；首次面诊重点提醒 |
| 持续 Safety/Exception | 旧分流可用，约束交集与预检准备已做 | 完整新路由/暂停/恢复合同、现实例外、所有支持场景的来源核对 |
| 刺激控制/放松 | 来源部分已读，教育说明可读 | 确切任务/脚本、适配与退出路径、独立验证 |
| 认知/课程/维持 | 六条教育短文与个人原文记录 | 完整练习、课程推进/结束条件、复发评估；不能靠阅读次数或日期自动授权 |
| 医生介入 | 用户原文记录、就诊摘要与导出 | 正式临床指令权限/核验/撤销/冲突机制；原文不解锁治疗 |
| 发布 | 可构建开发 APK | 最终整合、真机全流程、临床内容适用性审核与发布责任 |

下一步优先补齐治疗指标、SC 与安全适用条件的具体可审查合同，再连接临床 hold、审计和计划事务；不要继续重复已经完成的快照/恢复代次。未定义规则不能拿病例数值、一般说明或代理记忆填充。

## 8. 中断恢复与仓库

本轮基于 c2c7473 继续，未 reset/clean/删除 worktree 或覆盖独立工作。开始时已有学习相关未提交文件；按真实日志恢复，而非重做项目。恢复入口始终为 DEVELOPMENT_PROGRESS.md。

可复现构建：在项目根目录设置上述 JAVA_HOME 与 GRADLE_USER_HOME，运行 `gradlew.bat testDebugUnitTest assembleDebug assembleDebugAndroidTest --console=plain`。设备验收需另行连接手机；不对用户现有数据库执行破坏性测试。

相关详细记录：LEARNING_INCREMENT_20260915.md、CBTI_MODULE_SOURCE_REVIEW_20260914.md、INPUT_SNAPSHOT_STORAGE_INCREMENT.md、LOCAL_EXECUTION_INCREMENT.md、FULL_APP_IMPLEMENTATION_ROADMAP.md。较早文档中的“当前版本/尚未实现”描述属于当时状态，以本报告与最新恢复检查点为准。

## 9. 交付校验记录

- 代码提交：`a1224608e1959ff3bfbd6655880fdf73be0c600a`（feat: add sleep learning with private notes and verified navigation）。本报告/恢复记录随后单独提交；可用 `git log -1 -- docs/ENGINEERING_REPORT_20260921.md` 查找报告提交。
- `testDebugUnitTest`：369 项，0 failure、0 error、0 skipped；新增 LearningFlowTest 7 项全部通过。
- `assembleDebug`：通过。`assembleDebugAndroidTest`：通过，未在设备执行。
- 最终日志：`D:/Projects/SleepDiary/work/learning-final-20260921.log`；BUILD SUCCESSFUL，36 秒。JUnit XML：`app/build/test-results/testDebugUnitTest/`。
- 最终 APK：`D:/Projects/SleepDiary/work/deliveries/20260921/SleepDiary-0.2.2-20260921-debug.apk`。
- 标准构建输出：`D:/Projects/SleepDiary/app/build/outputs/apk/debug/app-debug.apk`。
- 大小：10,890,127 字节；SHA-256：`44615e97b171b4d88d28d181f2a9f5e6dad0c5d37ea64789339e75bac73bb86b`。
- APK 签名验证通过，Android Debug 证书 SHA-256：`3078614c42158d1608e1ba7f97e4e0a4e4a6ad8f9e89daa91e073cdf34844b61`。这是调试包，不是正式发行签名；与手机现有安装是否相同未在设备核验。不要为安装而卸载清空现有数据。
- 合并 Manifest 未出现三项禁止权限；仅有应用自己的 DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION。
- SPEC_MANIFEST 的 24 个文件 SHA-256 和字节数全部一致；Room 历史 schema、依赖版本、备份定义未改变。git diff --check 通过。
- 完整回归首次完成时发现旧来源数量断言为 7；修正为八条、且唯一新增 URL 必须为已经选定的官方 VA 手册，随后全量 369 项通过。没有删除失败测试。
- 本轮不创建远程、不 push；报告提交完成后本轮无未提交改动，只有原有独立 meditation/ 未跟踪。
- 真机验证未执行，原因是没有连接设备；不能据此宣称完整版或稳定发行版已完成。
