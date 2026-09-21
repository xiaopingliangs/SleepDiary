# Full App Working Context

2026-09-22：Phase 0 工程差距与合同已完成，准备校验并提交；Phase 1 尚未编码。当前基点 `f6a815f` / `a122460`，App/SPEC 0.2.2、Room 5、BACKUP_2.1、CBT-I Protocol NONE。

用户要求原文保存在 FULL_APP_IMPLEMENTATION_BRIEF.md。目标是完整治疗闭环，不是再交付六条教育说明。Phase 0 产物：FULL_APP_GAP_ANALYSIS、FULL_APP_STATE_MACHINE、FULL_APP_DATA_CONTRACT、FULL_APP_REQUIREMENTS_MATRIX（REQ 00–48），FULL_APP_ARCHITECTURE 已增加当前执行合同。

已核对：根规格、进度与工程报告、七份架构/路线、来源与模块记录、Workspace/快照/恢复/学习增量；Room 1–5 完整 SQL/索引/外键；四代备份 schema；实际 BackupCodec/Repository/Validator、InputSnapshotStore、RawInputEncoding/Decoder、PersistentActivationCheck 和 LocalExecutionRepository。不要再次从头读取这些。

阅读边界：原 PDF 仅以既有来源核对记录列出的页面为准，不宣称全文临床审核；每个医学分支编码前继续核对相应主源。V4 源在项目、下载目录和现有附件文件名中未找到，暂为 SOURCE_NOT_AVAILABLE；不猜写。独立未跟踪 `meditation/` 不修改、不暂存。

下一步：提交 Phase 0 后自动开始 Phase 1，实施 episode + audit Room 6、非处方 TreatmentStateController、不可变 plan 领域模型，并让新增业务进入 BACKUP_3.0。旧 MIGRATION_3_4 使用的备份常量必须固定为历史 BACKUP_2.1，版本分派必须保留 2.1 inputSnapshots。

医疗门控：M-BASELINE、M-METRICS、M-SAFETY、M-WINDOW、M-EXCEPTION、M-CONTENT 及 SNQ 权利。缺项只阻断对应自动动作；不使用默认临床阈值，不虚构 medicalReviewDate。旧日记、中性周报和 PNG 隐私保持，旧备份不能授权治疗。

验证：Phase 0 仅文档，不重新运行 Android 测试或构建。历史 369 项通过属于 0.2.2，不能算完整版本 PASS。最终完整真机 E2E 仍待执行。
