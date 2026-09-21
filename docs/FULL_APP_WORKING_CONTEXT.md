# Full App Working Context

2026-09-22：Phase 0 已提交 `b2952b7`；Phase 1 实现和验证完成，等待本阶段提交。App/SPEC 0.2.2、Room 6、BACKUP_3.0、TREATMENT_DATA_1.0、CBT-I Protocol NONE。

用户要求原文保存在 FULL_APP_IMPLEMENTATION_BRIEF.md。目标是完整治疗闭环，不是再交付六条教育说明。Phase 0 产物：FULL_APP_GAP_ANALYSIS、FULL_APP_STATE_MACHINE、FULL_APP_DATA_CONTRACT、FULL_APP_REQUIREMENTS_MATRIX（REQ 00–48），FULL_APP_ARCHITECTURE 已增加当前执行合同。

已核对：根规格、进度与工程报告、七份架构/路线、来源与模块记录、Workspace/快照/恢复/学习增量；Room 1–5 完整 SQL/索引/外键；四代备份 schema；实际 BackupCodec/Repository/Validator、InputSnapshotStore、RawInputEncoding/Decoder、PersistentActivationCheck 和 LocalExecutionRepository。不要再次从头读取这些。

阅读边界：原 PDF 仅以既有来源核对记录列出的页面为准，不宣称全文临床审核；每个医学分支编码前继续核对相应主源。V4 源在项目、下载目录和现有附件文件名中未找到，暂为 SOURCE_NOT_AVAILABLE；不猜写。独立未跟踪 `meditation/` 不修改、不暂存。

Phase 1 已完成：episode/audit、非处方 TreatmentStateController、不可变 plan 领域模型、Room 5→6、BACKUP_3.0及旧四代兼容。旧 MIGRATION_3_4 已固定历史 BACKUP_2.1；2.1 inputSnapshots继续恢复。医疗转换全部失败关闭。

医疗门控：M-BASELINE、M-METRICS、M-SAFETY、M-WINDOW、M-EXCEPTION、M-CONTENT 及 SNQ 权利。缺项只阻断对应自动动作；不使用默认临床阈值，不虚构 medicalReviewDate。旧日记、中性周报和 PNG 隐私保持，旧备份不能授权治疗。

验证：完整382项0失败；assembleDebug与assembleDebugAndroidTest通过，日志work/full-app-phase1-20260922.log。Room6共10表；APK SHA-256 b0252ae7cf88d639c56211a8a202ce42a48b4981eacfcc664685150bb42dd09c。未执行connected/真机。

下一步：提交Phase1；进入Phase2。先从已采用主源形成TREATMENT_METRICS输入和缺项合同；M-METRICS/M-BASELINE未解的路径保持MEDICAL_RULE_BLOCKED，不定义默认TST/SE或有效日阈值。
