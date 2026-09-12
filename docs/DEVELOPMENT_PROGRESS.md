# 开发进度与中断恢复入口

更新时间：2026-09-13。本文件记录实际进度，不替代冻结规格。

## 当前任务

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

下一步：继续 P1 的导航/用例分离前检查现有改动；设备接入后补齐 P0 真机缺项。医学待确认项仍需按路线文档逐条解决，不能直接启用窗口/调整算法。当前 UI 未新增完整版治疗入口。
若中断，先运行 git status 与 git diff 并检查本文件；不要重复重写已有变更。

## 后续阶段限制

本次不宣称 P0 真机验收或 P1 全部分层完成。医学规则发布、新问卷/治疗表、备份升级均需要对应的明确规格；可独立继续工程准备。

交付时只剩独立 `meditation/` 未提交；本轮代码和本进度文件分别提交，不 push。
查询本进度文件的提交：`git log -1 -- docs/DEVELOPMENT_PROGRESS.md`。
