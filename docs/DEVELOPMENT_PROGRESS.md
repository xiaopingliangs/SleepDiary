# 开发进度与中断恢复入口

更新时间：2026-10-03。本文件记录实际进度，不替代冻结规格。

## 2026-10-03 Public Edition P2A contract checkpoint

P2A contract is frozen as a separate documentation checkpoint; P2B is the next authorized checkpoint. Recovery check confirmed repository HEAD `1ab6226a1e84efaa88bfe964f475449fe662228a` before this work and found the existing draft changes intact; unrelated untracked `meditation/` was preserved untouched. Current baseline: Room 6 / BACKUP_3.0 / CBT-I Protocol NONE.

Synchronized the three explicitly authorized version/data/backup specs and related Education/Public docs. Frozen exactly five durable Education state tables, packaged-only EducationContentVersion, transient EducationExport, one-to-one EducationRecord→WorkspaceRecord, transient Preview / explicit Commit, explicit milestone progress, backup content registry validation before mutation, legacy backups restoring Education empty, and strict no-telemetry / Diary / Safety / Treatment isolation. BACKUP_1.0 / 1.1 / 2.0 / 2.1 / 3.0 definitions remain historical and immutable. Future target: Room 7 / EDUCATION_DATA_1.0 / EDUCATION_EXPORT_1.0 / BACKUP_3.1.

P2A static contract checks and separate docs commit are recorded below when completed. P1.5 verified baseline was 414 unit tests and assembleDebug PASS; those results are historical and are not P2B verification. `meditation/` remains untouched and unstaged.

## 2026-10-03 Part A 日记午夜日期与 TIB 完整性热修复

根因：新建日记的床上时间选择器默认锚定在 sleepDate 前一日 22:00；未填写字段选到 00:00 时，旧逻辑只替换时分并保留前一日日期。于是 00:00→07:00 可被保存成 31 小时。旧校验只检查各时间先后，没有校验事件日期范围或整段 TIB 上限。修复后未回答 BED/TRY 的午夜选择跨到 sleepDate；已回答字段仍保留明确日期。结构校验要求四个时间都处于 sleepDate 当日或前一日，且 BED→OUT 不超过 24 小时。

历史错误行不自动改写；最近 7/14 天与 Weekly 计算会跳过结构不一致的行。用户手动修正后，既有 revision 流程把原 summary 标为非 current，新增 revision 并保留 supersedes 链。TIB 内部仍以分钟保存，两个统计展示改为小时/分钟文字。

定向测试：DiaryValidatorTest、WeeklyTest、RecentAverageCalculatorTest、ReliabilityTest、DiaryFormTest、AverageDurationTextTest 共 71 项全部通过。本轮整体全量测试及 assembleDebug 尚未运行，计划在 Part B 完成后运行。Part A 独立提交；教育 P1.5 未提交改动继续留在工作树，未与 Part A 一同提交；独立 `meditation/` 未改、未暂存。

## 2026-10-03 Part B Public Edition Education P1.5

Full static inventory of `CBTI-Learning-Game-V4.html` (SHA-256 `7028c44b2f4fa4863625a4332dee67ba9667c31ab101182e949e3752b45d0af5`) contains 562 classified inventory units and a 462-node internal Public candidate manifest. Classification counts: 146 `PUBLIC_AS_IS`, 178 `PUBLIC_WITH_PRESENTATION_ADAPTATION`, 144 `PUBLIC_CONCEPT_ONLY`, 40 `FUTURE_TREATMENT_ONLY`, 54 `BLOCKED`. Added schema 1.1 and static Education contracts for hierarchy, layered explanations, transient Preview / explicit Commit, five-step reflection, review/bookmark concepts, sources and claims. Updated `EDUCATION_SPEC.md`, architecture, requirements matrix and V4 matrix. No Room, backup, navigation or user-persistence implementation.

Removed the unused Treatment controller property from `AppServices` (no callers existed), with a regression assertion; the UI graph has no Treatment route. This keeps the dormant Treatment foundation out of the Public UI service surface. Education tests passed: 17 total (6 legacy registry + 11 P1.5), zero failures. Candidate content is not release-cleared: V4 redistribution/source reuse permissions remain unresolved, and blocked units stay out. CBT-I Protocol remains `NONE`; no treatment behavior was enabled.

Part B has its own commit, `feat: freeze V4 public education candidate manifest`. Post-commit full `testDebugUnitTest` passed: 414 tests, zero failures/errors/skips; `assembleDebug` passed. APK: `D:/Projects/SleepDiary/app/build/outputs/apk/debug/app-debug.apk`, 11,138,657 bytes, SHA-256 `22c31f39343077262ffb90e087246f7c93cc1283c43b3adac42d38a8fdaebad7`. Git HEAD is Part B; tracked worktree and index are clean. The only status item is unrelated untracked `meditation/`, untouched and unstaged. CBT-I Protocol is `NONE`; no Public UI Treatment route or AppServices controller remains. The stored Treatment foundation is dormant and unchanged. Manifest permission audit found none of INTERNET, ACCESS_NETWORK_STATE or MANAGE_EXTERNAL_STORAGE. No connected-device validation was requested or run.

## 2026-09-30 Public Edition P1 Education Content Foundation

接续 Public Edition P0 提交 `1f9acf0`。新增纯静态 Education registry、schema 模型、来源 registry、规范化 SHA-256、manifest 与边界 validator；现有 `EDUCATION_TEXT_1.0` 六项内容原文迁移为不可变 `EDUCATION_CONTENT_1.0`，版本 hash 为 `b988e5d87f6a3ffc0c6963b6b6fc4350dc92012aa13c7b6cc64ddecc1eaa514c`。原 `LearningContent` 只改为兼容投影，页面布局和导航未变。

指定 V4 HTML 已按只读数据审查，文件 SHA-256 为 `7028c44b2f4fa4863625a4332dee67ba9667c31ab101182e949e3752b45d0af5`。它含开场和 10 个章节，也混合治疗阈值、行为指令、用户记录及复习状态，因此没有整体导入；逐章边界和后续候选见根目录 `EDUCATION_CONTENT_ARCHITECTURE.md`。

新增 6 项 registry 测试；完整 `testDebugUnitTest` 实际执行 394 项，0 failure/error/skipped；`assembleDebug` 通过。未改 Room、migration、Backup、导航或 Treatment，CBT-I Protocol 仍为 NONE。独立 `meditation/` 未改动、未暂存。下一阶段须单独批准 Education 用户数据合同、Room migration 与 Backup 演进；不得从本阶段自动进入。

## 2026-09-22 当天再次进入首页的最近平均数据

在既有 `TODAY` 首页增加按需展开的“最近平均数据”，用户可选最近 7 天或 14 天。窗口以当天为结束日按日历日期实时筛选已有 `SleepDiaryEntry`；缺失日期不按 0 计算，展示与既有中性周总结一致的 SOL、夜醒次数、WASO、最后醒来到离床、TIB 和主观质量平均值，不增加 TST、SE、评分或治疗判断。

保存完成后的当前会话仍停留在成功/周总结页；只有今天已有记录后再次进入应用才会进入 `TODAY`，因此没有新增启动次数、Room 或备份字段。日记、Safety、Weekly、Backup、CBT-I Protocol 和冻结规格均未改变。

新增 5 项纯计算测试和 1 项首页选择测试。完整 `testDebugUnitTest`：388 项，0 failure/error/skipped；`assembleDebug`、`assembleDebugAndroidTest` 均通过，日志 `work/recent-averages-20260922.log`。合并 Manifest 无 INTERNET、ACCESS_NETWORK_STATE、MANAGE_EXTERNAL_STORAGE。

APK：`D:/Projects/SleepDiary/app/build/outputs/apk/debug/app-debug.apk`，10,991,498 字节；SHA-256：`81dbdf77d1ca3928d115cf15b2c9575ae0ab6cfb6b8f73efd1f0a870dbdcac02`。未执行本轮真机交互验证。独立未跟踪 `meditation/` 未修改、未暂存。

## 2026-09-22 Full App Phase 0 工程合同

用户的完整实施 Brief 已原样保存为 `FULL_APP_IMPLEMENTATION_BRIEF.md`。已核对实际 Room 5 / BACKUP_2.1、快照、恢复代次和替换恢复代码，新增模块差距、17 态状态机、数据合同及 REQ 00–48 矩阵；完整版架构已增加当前执行合同，同时保留 2026-09-11 历史设计作审计。

真实治疗闭环尚未实现。V4 源缺失、SNQ 权利和各项医学具体规则分别门控，不能把六条学习内容或候选规则当作完整治疗。根冻结规格和 Android 代码本阶段未改；独立未跟踪 `meditation/` 未修改、未暂存。

下一步自动进入 Phase 1：episode / audit、非处方状态控制器和 plan 领域骨架；新增业务同步进入 BACKUP_3.0，使用无损 Room 5→6。Phase 0 是文档阶段，不重复执行旧 Android 测试，也不把历史 369 项作为本阶段新结果。中断恢复见 `FULL_APP_WORKING_CONTEXT.md`，提交可用 `git log -1 -- docs/FULL_APP_GAP_ANALYSIS.md` 定位。

## 2026-09-22 Full App Phase 1 数据与状态基础

Phase 0提交 `b2952b7`。Phase 1新增TreatmentEpisode/Audit、唯一状态控制器、不可变TreatmentPlan领域骨架、Room 5→6和BACKUP_3.0。只开放非处方基线/暂停转换；所有Safety放行、窗口、计划和周调整转换返回MEDICAL_RULE_BLOCKED。Protocol保持NONE。

完整382项0失败，assembleDebug/assembleDebugAndroidTest通过；Room6导出10表，Manifest无三项禁止权限。日志`work/full-app-phase1-20260922.log`；APK SHA-256 `b0252ae7cf88d639c56211a8a202ce42a48b4981eacfcc664685150bb42dd09c`。未执行真机。详细见FULL_APP_PHASE1_REPORT.md；下一步Phase2，缺失医学定义继续门控。

## 最新完成检查点：2026-09-21 学习入口与完整工程汇报

代码提交 a1224608e1959ff3bfbd6655880fdf73be0c600a。已实现设置中的六条睡眠学习内容、来源页码、主动官方链接、返回原学习页的个人记录入口；读取不写库、不推进治疗，笔记保存/编辑/完整备份恢复和分享隔离通过。详见 ENGINEERING_REPORT_20260921.md 与 LEARNING_INCREMENT_20260915.md。
全量测试曾暴露旧 CommentBackupTest 使用不同日期导致无限等 SUCCESS；线程定位后给 RoomDiaryUiStore 增加时钟注入，测试固定时钟并加超时，默认生产时钟不变。新增来源白名单只允许指定 VA 手册。
最终 369 项测试全部通过，0 failure/error/skipped（新增 7 项）。testDebugUnitTest / assembleDebug / assembleDebugAndroidTest 均通过，36 秒；work/learning-final-20260921.log。冻结清单 24 文件、合并权限与 APK 签名验证通过。
交付 APK：D:/Projects/SleepDiary/work/deliveries/20260921/SleepDiary-0.2.2-20260921-debug.apk，10,890,127 字节；SHA-256 44615e97b171b4d88d28d181f2a9f5e6dad0c5d37ea64789339e75bac73bb86b。标准构建目录仍有同一 APK。
App/SPEC 0.2.2、Room 5、BACKUP_2.1、CBT-I Protocol NONE 不变。没有连接手机，没有执行设备测试；完整版治疗闭环仍未完成。不能把此包当作最终完整 CBT-I 成品。
下一步：补齐独立治疗指标、SC 和安全适用性合同，再接持久化 hold/审计/计划事务。学习入口本轮已完成，不再重复实现。报告和本记录随后单独提交；独立 meditation/ 未修改、未暂存。完整报告包含功能清单、历史范围、技术版本、迁移隐私、验证、剩余工作与交付哈希。

## 已被上方完成记录取代：2026-09-21 恢复验证过程

上次代码与 LearningFlowTest 已落盘，旧日志没有最终测试结论，不能记为通过。本次确认没有残留 Java 测试进程；重新运行发现 JUnit 返回类型错误，改成 runBlocking<Unit>。另修正测试对 AlertDialog 使用无滚动点击，并等待 Compose 完成状态更新后触发系统返回。没有删除测试或放宽业务断言。
新增 7 项 LearningFlowTest 已全部通过（work/learning-targeted-final-20260921.log，11 秒）。目前正在执行完整 testDebugUnitTest / assembleDebug / assembleDebugAndroidTest，日志 work/learning-full-20260921.log；最终结果尚待记录。补齐认识睡眠担忧页面安全提示的第 63 页引用。
本次 adb 没有连接设备。冻结 24 个文件哈希与字节数核对一致，独立 meditation/ 保持未跟踪。接下来解析全量结果、核对 APK/Manifest、生成完整工程汇报、提交本轮文件。不要重写已存在的学习模块。
## 最新完成检查点：认知、放松、课程与维持内容来源核对

接续 `fa91ee8c95e6ca687ffd01d8ae13633bf75074fc`。中断发生在手册阅读阶段，没有遗留代码变更；恢复后确认只有独立 meditation/ 未跟踪，再补读未完成页面。没有从头改写上一阶段的数据层。

新增 [模块来源核对](CBTI_MODULE_SOURCE_REVIEW_20260914.md)：主手册 51–52、58、61–72、111–112 页与目录核对，63/70/111/112 页视觉确认；重新核对 VA/DoD 2025 第 31 页安全覆盖。记录两份官方本机 PDF 的哈希，不宣称全文或临床审核完成。
现有认知、放松、灵活课程、维持模块形成 8 个来源条目和 6 段中文内容草稿，区分教育、用户原文记录和治疗算法。纠正思维记录/复发附录的定位，保留真实困倦驾驶风险、放松适配和不可按日期自动推进治疗的限制。内容尚未嵌入 App。

本次仅文档：没有新增单元测试、没有重新构建/安装 APK、没有真机验证。362 项通过属于下节 fa91ee8 的历史构建结果，不是本轮新执行结果。冻结规格/版本不变，CBT-I Protocol NONE。完整治疗版尚未完成。
已实际验证冻结清单 24 个文件的 SHA-256/字节数全部一致，新文档本地链接存在，git diff --check 无错误。
下一步：落实条目 ID/页码/审核状态与教育呈现合同，复用既有 Workspace 个人记录；继续补齐 SC、治疗指标与安全条件映射，再连接正式治疗计划。不要重复实现已经完成的快照和恢复代次，不用病例数字补空缺医学参数。
本节和来源核对同次提交；用 git log -1 -- docs/CBTI_MODULE_SOURCE_REVIEW_20260914.md 找到准确提交。独立 meditation/ 未修改、未暂存。

## 最新完成检查点：SPEC-0.2.2 本机恢复代次与激活前检查

接续 413d26c。此前写入被额度限制拒绝；用户继续后检查 git status/文件存在性确认未落盘，再正常恢复实现，没有绕过审批。LocalExecutionState/DAO/Repository、PersistentActivationCheck 已完成，详细合同见 LOCAL_EXECUTION_INCREMENT.md。
App 0.2.2 / versionCode 4 / SPEC-0.2.2 / Room 5。新增本机 LOCAL_EXECUTION_1 元数据表，原七张业务表定义不变；旧 1/2/3/4→5 无损迁移通过，新 schema 5.json 已导出。
本机 restoreGeneration 在新安装首次检查时创建、旧库迁移时创建，数据库重开保持。每次成功恢复（包括相同内容）和业务数据同事务更新代次并设置 REQUIRES_REVIEW；失败时一起回滚，提交后回读。不是历史业务数据，不导出/导入，所以 BACKUP_2.1 和四代备份 schema/示例全部保持。
激活前检查事务内读取本机代次、既有快照及当前原始输入；日记/Safety 变化、缺失快照、恢复或技术错误区分返回。没有激活成功分支，数据一致仍为 REQUIRES_REVIEW；医生原文或备份字段不能解锁治疗。
新增 15 项测试（激活前检查 14、Room 4 迁移 1）。完整 362 项，0 failure/error/skipped；testDebugUnitTest / assembleDebug / assembleDebugAndroidTest 均通过，BUILD SUCCESSFUL 1m2s。日志 work/local-execution-20260914.log。
APK：D:/Projects/SleepDiary/app/build/outputs/apk/debug/app-debug.apk；10800785 字节。
SHA-256：22014cc7ef1c691e0273180e76f13cba5525a91cfa6b379d0a3386d465742bba。
旧医学文件、全部 backup schema/示例哈希保持，合并 Manifest 无禁止权限，无新运行时联网依赖。SELF_CHECK/清单已同步。没有本轮真机或 connected 测试；数据库重开不冒充真实进程终止验收。
本节与代码同次提交，可用 git log -1 -- app/src/main/java/app/sleepdiary/offline/data/entity/LocalExecutionState.kt 找回提交。独立 meditation/ 未改动/暂存。
下一步：临床 hold/决策审计与原子计划提交的正式数据合同和实现；本机恢复代次本轮已完成，不要再次从头实现。未来提交仍须在写入事务中重新校验，不能把这里的预检结果当成治疗许可。CBT-I Protocol NONE，完整临床治疗流程尚未完成，不要求用户测试中间 APK。

## 最新完成检查点：SPEC-0.2.1 快照长期保存与 BACKUP_2.1

接续 072822c。现已完成 InputSnapshot 独立表/DAO、InputSnapshotStore、RawInputDecoder 校验；同事务捕获/写入/回读，提交后再回读，command ID 幂等重试、范围冲突拒绝。快照在磁盘重开后保持，并不随原始日记编辑而变化。
App 0.2.1（versionCode 3）/ SPEC-0.2.1 / Room 4 / BACKUP_2.1；RAW_INPUT_CAPTURE_1，其他领域版本及 CBT-I Protocol NONE 不变。Room 1/2/3→4 迁移通过，旧六表定义及旧三份 schema 历史不变。新 schema 4.json 已导出。
完整备份支持 1.0/1.1/2.0/2.1；七张业务表同事务替换和回读。旧格式快照恢复为空，确认页面展示数量/清空提示。损坏哈希、版本、Base64 或二进制结构均拒绝；正确哈希也不能掩盖损坏 payload。新示例有一份合成空范围历史快照，无实际用户数据。
新增 14 项测试（快照 13、Room 3 迁移 1），完整 347 项，0 failure/error/skipped。testDebugUnitTest、assembleDebug、assembleDebugAndroidTest 均通过，最终 32 秒。日志 work/snapshot-storage-20260914.log / work/snapshot-storage-verified-20260914.log。回滚测试确认每个检查点确实被执行，未把早期校验失败冒充回滚；samples 目录已声明为测试输入，示例改变会触发重测。
APK：D:/Projects/SleepDiary/app/build/outputs/apk/debug/app-debug.apk，10784401 字节。
SHA-256：27c2a0267a8498757be9e6301a45796499019ad1973ff923854d6a49f002e439。
AGENTS/CLINICAL_BOUNDARIES/EVIDENCE、全部旧 backup schema/示例哈希保持；合并 Manifest 无禁止权限；未新增运行时网络或分析依赖。SELF_CHECK/清单已同步。没有本轮真机/connected 测试；不要求用户测试中间 APK。
代码及此记录同次提交，使用 git log -1 -- app/src/main/java/app/sleepdiary/offline/application/InputSnapshotStore.kt 可找到提交。独立 meditation/ 未改动/暂存。
下一步：实现本机恢复代次和持久化激活检查，随后治疗审计及原子计划提交；快照长期保存本轮已经完成，不要再次当作尚未落地。当前无面向真实用户的快照创建入口/自动治疗调用，持久化 hold、正式临床规则和完整治疗仍未完成。详细合同见 INPUT_SNAPSHOT_STORAGE_INCREMENT.md。

## 最新完成检查点：事务内原始输入捕获

接续 60a8ce2，新增 ProtocolInputCapture / RawInputEncoding，详见 PROTOCOL_INPUT_CAPTURE_20260913.md。同一 Room 事务读取范围内日记、全部 Safety 和实际 AppState；复制全原始字段，固定二进制编码及 SHA-256；不规范化用户字符串，null 与空值区别保留，字节防御性复制。
本轮只是捕获/编码，不是持久化 InputSnapshot、恢复代次或计划提交；无 UI/生产治疗调用。未修改 Room/Backup/版本/冻结医学规则，Protocol NONE。原 20 项清单哈希和字节数一致，合并 Manifest 无三项禁止权限。
新增 10 项测试。首次重复 ID 测试意外使用随机不同 ID，导致 1 项失败；修正同 ID 样本，并固定 null/Unicode 对比的其他字段。最终全量 333 项，0 failure/error/skipped；testDebugUnitTest / assembleDebug 均通过，31 秒。日志 work/input-capture-20260913.log 和 work/input-capture-verified-20260913.log。没有真机或 instrumentation 执行。
APK：D:/Projects/SleepDiary/app/build/outputs/apk/debug/app-debug.apk，10863476 字节；SHA-256：768bd78bf4223c89d0df26f027b68cb30c07a9e2173d67f053cdce39d97b1851。
代码与本节同次提交，可通过 git log -1 -- app/src/main/java/app/sleepdiary/offline/application/ProtocolInputCapture.kt 定位。独立 meditation/ 未动。
下一步是持久化 InputSnapshot/技术恢复代次/审计及单事务提交的新数据合同、Room 和备份扩展；先明确版本和恢复对象，不能塞进个人笔记表。已有本轮编码可以复用，不需重写。完整治疗仍未完成，未要求用户测试中间 APK。

## 最新完成检查点：约束合并与过期方案工程框架

接续 b750e7b，完成 ConstraintIntersection / ProposalFreshnessGuard，说明见 CONSTRAINT_GUARD_IMPLEMENTATION_20260913.md。只落实既有架构的交集、完整来源轨迹、未知/冲突拒绝和旧 proposal 水位失效；没有疾病映射、医学参数、数据库/备份扩展或 UI 接入。不是持久化治疗生命周期，也不是临床发布授权。
新增 16 项纯单元测试；最终完整 testDebugUnitTest：323 项，0 failure/error/skipped；assembleDebug 通过，BUILD SUCCESSFUL 34 秒。日志 work/constraint-guard-20260913.log。本轮未执行 instrumentation/真机。
20 份冻结清单哈希/字节数一致；版本、Room、Backup 不变，Protocol NONE；合并 Manifest 无三项禁止权限。
APK：D:/Projects/SleepDiary/app/build/outputs/apk/debug/app-debug.apk；10863476 字节；SHA-256：abf5a69d49f27f9be972462e6b306ead0f855abb0a96472b554e1b8b728ff57c。
代码与本节同次提交，可用 git log -1 -- app/src/main/java/app/sleepdiary/offline/domain/protocol/ConstraintIntersection.kt 找回提交。独立 meditation/ 未修改或暂存。
下一步继续治疗生命周期的正式数据合同与原子提交实现；本框架未来必须在同一提交事务中使用真实持久化水位，并补齐恢复代次/审计，不能把目前的纯类型当成已实现的持久化安全保持。临床适用性、规则及人工判例未齐的治疗分支仍不可发布，完整 CBT-I App 未完成。

## 最新完成检查点：SPEC-0.2.0 独立个人记录与 BACKUP_2.0

接续 4f55762；恢复时确认此前额度拦截的补丁未落盘，随后完成本增量。用户已授权继续完整版工程，不等待普通技术细节批准。
已完成独立 WorkspaceRecord/Dao/Repository、设置记录页面、SavedState 草稿、保存事务及提交前后全字段回读、编辑并发指纹/修订和丢失确认重试。没有新增每日医学字段；就诊记录不是可执行医嘱。
App 0.2.0 / SPEC-0.2.0 / Room 3 / WORKSPACE_1.0 / BACKUP_2.0。旧 Diary/Safety/Weekly 规则和运行时 CBT-I Protocol NONE 不变。Room 1→2→3、2→3 升级及磁盘重开通过；旧五个 entity 定义完全一致。
完整备份导出 2.0，导入兼容 1.0/1.1/2.0，六表同事务替换/回读/失败回滚。旧格式恢复个人记录为空，确认页面已提示。CSV/PNG/就诊摘要不自动包含个人记录；PNG 继续排除 comment 和 Safety。

新增 20 项测试（数据 14、UI/状态 5、Room 2 迁移 1），最终完整 307 项，0 failure/error/skipped。testDebugUnitTest / assembleDebug / assembleDebugAndroidTest 均通过；最终日志 work/workspace-release-check-20260913.log，BUILD SUCCESSFUL 33 秒。
中间补充运行曾有 1 项独立 ViewModel 等待超时，日志 work/workspace-final-20260913.log；修正测试主线程同步后全量通过，未删除数据断言。其他日志 work/workspace-20260913.log、work/workspace-verified-20260913.log 保留。
APK：D:/Projects/SleepDiary/app/build/outputs/apk/debug/app-debug.apk，10863476 字节。
SHA-256：4cdeacd78320864c869dfa34dd113491e6f29deafb844bb78668602e29efa3c5。
没有安装到手机，没有执行本增量 connected 测试；中文 IME、旋转、真实进程终止及系统 SAF 仍在最终真机验收范围。测试 APK 构建不等于设备测试通过。
AGENTS/CLINICAL_BOUNDARIES/EVIDENCE、两代旧 backup schema/示例保持原字节哈希。原 Room 1/2 schema 不变，新 Room 3 已导出；SELF_CHECK 与清单已同步。独立 meditation/ 未改动/暂存。

本节与实现同次提交，可用 git log -1 -- app/src/main/java/app/sleepdiary/offline/data/entity/WorkspaceRecord.kt 找到精确提交；不把自身 commit hash 写进同一个待提交文件。下一次先 git status/log 和本节验证，不重做本增量。
下一步：继续按完整版架构落实正式协议适用性、医生确认/暂停/例外的非诊断生命周期合同及相应模块；候选窗口算法仍只有提取测试，没有生产调用。当前不是最终完整 CBT-I App，不要求用户测试中间版。新增治疗规则必须有对应源文和明确规格，不能从注释自行推导。

## 最新完成检查点：已获批准的就诊摘要 TXT 导出

用户在查看具体范围后明确回复“批准，继续”。此前的 TXT 审批阻塞已获授权，不再等待重复确认。
从 6a67186 接续，已添加 SummaryTextWriter 和预览锁定/系统文件创建器链路；默认排除备注/Safety，选择后预览并确认，用户决定保存位置。没有使用工具导出真实用户数据。
导出持有生成时的字符串，打开选择器后不再查库拼接；选择器取消不写入；丢失预览拒绝开流；写入和关闭失败不显示成功。运行中导出使用 ViewModel scope，旋转不重新启动导出。
新增 13 项测试（写入器 4、界面/状态 9）。完整 testDebugUnitTest / assembleDebug 实际通过：287 项，0 failure/error/skipped，BUILD SUCCESSFUL，30 秒。日志 work/clinician-export-20260913.log。
APK：D:/Projects/SleepDiary/app/build/outputs/apk/debug/app-debug.apk；10,776,126 字节。
SHA-256：e77624f0002d654faaaf5f1aed203f75418ac91f32b9f9992338bb63953167f5。
17 个冻结文件哈希/字节数一致，合并 Manifest 无禁止权限，旧 JSON/CSV/PNG 与 Room/Backup 版本未改，CBT-I Protocol NONE。
已验证假 SAF 的精确预览字节、两类敏感信息独立选择、取消、close 失败、状态恢复不重启选择器、丢失预览不打开流。状态恢复模拟/新 ViewModel 测试不等于真机旋转或真实进程死亡测试；本轮没有安装 APK 或真机导出。
代码和记录同次提交，可用 git log -1 -- app/src/main/java/app/sleepdiary/offline/application/SummaryTextWriter.kt 找回提交。独立 meditation/ 保留，未改动/暂存。
下一步仍是完整版的治疗规则、适用性和新数据合同落实；本次就诊摘要预览/TXT 模块已完成，原 TXT 授权阻塞已解除，不需要再询问。完整版治疗功能仍未全部实现。

## 最新完成检查点：就诊摘要本机预览

接续 9800bf7。新增 ClinicianSummaryService、ClinicianSummaryScreen 与设置入口，只读事务读取选定日期日记，默认排除 comment 和 Safety；显式勾选后才加入本机预览。无治疗结论或计划，也无新持久化对象。
首次含 TXT 导出的补丁被自动审批拒绝，理由是该具体敏感负载/目的地授权不足；该补丁未落盘、没有导出数据。改为仅本机显示的安全替代方案已落盘。没有通过其他路径绕过导出拒绝。
原始用户完整架构请求第十八节明确要求医生摘要可展示/导出近期日记、safety flags 等；但未继续重试敏感明文导出，待具体范围确认。现有 JSON/CSV/PNG 链路不变。
新增 16 项测试。首次运行 274 项中，新增导航测试误对固定按钮执行滚动，失败于测试动作；修正后完整重跑通过：274 项，0 failure/error/skipped。testDebugUnitTest / assembleDebug：BUILD SUCCESSFUL，23 秒。
日志：work/clinician-preview-20260913.log（首次失败）和 work/clinician-preview-20260913-verified.log（完整重跑）。
APK：D:/Projects/SleepDiary/app/build/outputs/apk/debug/app-debug.apk，10,776,126 字节。
SHA-256：5fa50301277edf8fd11e34660830b873480295076f7e34d96409f4123d3dca33。
17 份冻结规格字节数/哈希一致，合并 Manifest 无禁止权限；旧 Room/Backup/Diary 版本不变，CBT-I Protocol NONE。未执行本轮真机验收，未安装新 APK。
详细范围、测试和 TXT 出口的具体待确认方案见 CLINICIAN_PREVIEW_IMPLEMENTATION_20260913.md。代码与记录一并提交，可通过 git log -1 -- app/src/main/java/app/sleepdiary/offline/application/ClinicianSummaryService.kt 恢复提交。
完整版仍未完成；下一步继续治疗规则/数据合同的落实和其他模块。TXT 导出仅在其具体敏感数据范围得到确认后实现。独立 meditation/ 未改动、不暂存。

## 最新完成检查点：源文候选规则层

用户已要求继续。上次代码写入被自动审批额度限制拦截，2026-09-13 再查确认未落盘、HEAD 为 d919d84；随后恢复写入，无覆盖其他工作。
已新增 WindowRuleExtraction 和对应测试，详细边界见 WINDOW_RULE_EXTRACTION_20260913.md。
无生产调用方，不启用治疗、不改旧规格/数据/备份。新增 25 项单元测试；仅验证源文提取，不是独立临床金标准。

- 首次完整运行：258 项中 2 项 SAF 测试在 @Before 等待初始加载时超时，实际导入/导出尚未执行。日志 work/window-extraction-20260913.log 保留失败记录。
- SafFlowTest 的轮询增加 runOnIdle 同步 Room 回调所在的 Main 队列，并在读取失败时直接报告错误；保留原 5 秒等待及全部数据断言。未修改生产 SAF 流程。
- 修正后完整运行 testDebugUnitTest / assembleDebug：BUILD SUCCESSFUL，38 秒；258 项，0 failure、0 error、0 skipped；其中新增 25 项、SAF 7 项均通过。
- 最终日志：work/window-extraction-20260913-verified.log。
- 冻结清单 17 文件 SHA-256/字节数一致；合并 Manifest 无三项禁止权限；Room schema、版本和依赖无变更。
- APK：D:/Projects/SleepDiary/app/build/outputs/apk/debug/app-debug.apk，10,775,247 字节。
- SHA-256：838fddaabeff266f6f986c9d1499add45ef179497f8a9d885bb35d4f50a1cfbe。
- 未安装到手机，未执行真机或 instrumentation；不要求用户验收中间版。完整治疗版尚未完成。

代码和记录同次提交；可用 git log -1 -- app/src/main/java/app/sleepdiary/offline/domain/protocol/WindowRuleExtraction.kt 查找精确提交。独立 meditation/ 继续保留，不暂存。
下一步：按 WINDOW_RULE_EXTRACTION_20260913.md 补齐治疗指标、安全适用条件和新数据合同，再连接治疗数据层；不得将候选输出直接当作已发布计划。下面是旧检查点，不能替代本节新结果。

## 当前任务

最新任务（2026-09-13）：用户要求直接推进最终完整版，仅真机验收最终整合版；初始窗口重点建议先面诊由医生决定。规则必须来自各模块指定指南。见 `PROTOCOL_SOURCE_CHECK_20260913.md`。
用户进一步确认以指定指南、自助式使用和人性化框架为目标，不因问卷使用范围的核查暂停整个项目；医生重点介入睡眠限制及涉及安全健康的意外、例外。SNQ 原文/翻译使用范围仍未核实，作为独立内容项跟踪，不代表用户已选择“全部周度调整只能由医生录入”的产品路径，也不代表已获得第三方授权。现已新增未接入运行流程的候选规则提取代码，详见顶部最新检查点。下文保留此前已完成的工程切片及测试记录。

用户批准在架构已明确的范围内开始完整版开发。架构基点为 `3d73df4b4d2d4e46e707c30d5ae8f5a965d7d27f`。
本次实施首个工程切片：将数据库和服务装配移到应用级容器，保持日记功能和所有医学/数据版本不变。

## 已确认

- 七份完整版架构文档已提交。医学待确认项 M01–M12 仍见 FULL_APP_IMPLEMENTATION_ROADMAP.md，不得用默认阈值补齐。
- 2026-09-13 adb 未识别到设备；不声称已补齐 S24 中文 IME、完整 Sharesheet 或真实进程终止验收。
- 开始时唯一未提交内容是独立 `meditation/`，不修改、不暂存。
- 原应用为 SPEC-0.1.2 / BACKUP_1.1 / Room 2 / CBT-I Protocol NONE。

## 当前检查点

状态：首个工程切片已完成并提交。
代码提交：`22919b2290f38a0f504f3ff92b27e95a3d03bbaf`。

- 新增 SleepDiaryApplication、AppContainer；数据库使用同一原文件与原 Room 2 迁移，服务按应用生命周期统一装配。
- AppServices 从 Compose 页面文件移至 application 包；MainActivity 使用统一容器，删除 Activity 静态数据库持有逻辑。
- 新增 AppContainerTest 3 项：Manifest 注册/应用容器、日记与备份共用一个惰性数据库/应用 Context、关闭并重建容器后的磁盘数据与 onboarding 保留。
- 原有 SAF 测试只调整 AppServices 导入；原断言保持。
- testDebugUnitTest 实际执行通过：233 项，0 failure、0 error、0 skipped。
- assembleDebug 实际执行通过；构建日志：`work/full-app-foundation-20260913.log`。
- 合并 Manifest 无 INTERNET / ACCESS_NETWORK_STATE / MANAGE_EXTERNAL_STORAGE，Application 注册正确。
- 冻结清单 17 文件 SHA-256 和字节数全部一致；Room schema、版本和依赖目录未修改。
- 本轮无设备连接，未安装新 APK。容器重建测试不等于真机进程终止验收；旧 IME 与完整 Sharesheet 未验收项继续保留。

新 APK：`D:/Projects/SleepDiary/app/build/outputs/apk/debug/app-debug.apk`。
SHA-256：`b0ab47692b949b342d3edb538dafabda64b1d6e0ee332ad5404f4525945b3b55`。
构建前旧 APK 副本：`D:/Projects/SleepDiary/work/baselines/app-debug-before-foundation-20260913.apk`。
旧 SHA-256：`a330c88b613a6be76a80d87f582f256984223a11e198f7fe977c59930e943e6a`。
这些 APK/日志在本地工作目录，未提交 Git；后续构建可能更新标准输出 APK，应先核对哈希。

下一步：从已保存的 VA 2014 主手册及 VA/DoD 2025 安全覆盖继续逐条形成可审查的规则与适用条件，再实现对应领域逻辑及边界测试；不因 SNQ 单项核查停止其他模块，也不自行替换问卷并沿用原计分。继续按路线文档解决医学待确认项，不能直接把草稿规则标为临床审核通过。当前 UI 未新增完整版治疗入口。用户只验收最终整合版，过程中优先自动化验证并明确保留未执行的真机项目。
若中断，先运行 git status 与 git diff 并检查本文件；不要重复重写已有变更。

## 后续阶段限制

本次不宣称 P0 真机验收或 P1 全部分层完成。医学规则发布、新问卷/治疗表、备份升级均需要对应的明确规格；可独立继续工程准备。

交付时只剩独立 `meditation/` 未提交；本轮代码和本进度文件分别提交，不 push。
查询本进度文件的提交：`git log -1 -- docs/DEVELOPMENT_PROGRESS.md`。
