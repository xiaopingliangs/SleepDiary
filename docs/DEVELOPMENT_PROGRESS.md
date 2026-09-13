# 开发进度与中断恢复入口

更新时间：2026-09-13。本文件记录实际进度，不替代冻结规格。

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
