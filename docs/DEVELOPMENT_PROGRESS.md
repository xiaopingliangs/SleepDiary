# 开发进度与中断恢复入口

更新时间：2026-09-13。本文件记录实际进度，不替代冻结规格。

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
