# Full App Phase 1 Report

日期：2026-09-22。范围：治疗数据模型、状态控制器、episode/audit、plan领域骨架、无损迁移和完整备份。没有启用 CBT-I 医学算法或 UI。

## 实现

- Room 6 新增 TreatmentEpisode 与 TreatmentAuditEvent；旧八表不改，显式 5→6 迁移，Room 1–5 均可顺序升级。
- TreatmentStateController 是唯一新状态写入口。已实际开放成人明确确认后的 BASELINE_COLLECTING、BASELINE_REVIEW、USER_PAUSED 及安全恢复到原基线状态；命令幂等、冲突、expectedRevision、恢复代次、时钟倒退、事务回读和提交后不确定均有明确结果。
- Safety放行、计划创建/确认、周复核、维持和复发等医疗转换返回 MEDICAL_RULE_BLOCKED。CBT-I Protocol 仍为 NONE。
- TreatmentPlan 领域值对象覆盖完整不可变字段，并拒绝 NONE protocol；未持久化、未激活，不冒充可执行计划。
- BACKUP_3.0 完整包含 episode/audit；继续导入1.0/1.1/2.0/2.1。旧备份替换后治疗集合为空；恢复仍在同一Room事务，成功刷新本机代次，失败回滚。旧schema和样例未改。
- appVersion/SPEC仍为0.2.2；新增 TREATMENT_DATA_1.0，BACKUP升级3.0。Diary/Safety/Weekly/Workspace版本不变。

## 验证

- 新增13项：状态/审计/幂等/冲突/成人范围/暂停恢复/医疗门控/事务失败/丢失确认/备份往返/旧格式/损坏/恢复检查点、Room5迁移和plan骨架边界。
- 完整 `testDebugUnitTest`：382项，0 failure/error/skipped。
- `assembleDebug`、`assembleDebugAndroidTest`：PASS；日志 `work/full-app-phase1-20260922.log`。
- Room schema 6已导出，共10张表。合并Manifest无INTERNET、ACCESS_NETWORK_STATE、MANAGE_EXTERNAL_STORAGE。
- APK：`D:/Projects/SleepDiary/app/build/outputs/apk/debug/app-debug.apk`，10,991,498字节，SHA-256 `b0252ae7cf88d639c56211a8a202ce42a48b4981eacfcc664685150bb42dd09c`。
- 未执行connected/真机测试，不将构建测试APK视为设备通过。

## 剩余

Phase 2 治疗指标和基线的具体算法仍受 M-METRICS/M-BASELINE 门控。可以继续实现不可变快照、provider接口和缺规则的失败关闭，但不能自行定义TST/SE、有效日或缺日阈值。V4源仍未找到；SNQ权利仍阻断依赖它的自助周调整。
