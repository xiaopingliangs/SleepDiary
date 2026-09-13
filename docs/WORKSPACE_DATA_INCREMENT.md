# SPEC-0.2.0：独立个人记录与完整备份增量

用户已批准继续完整版开发及所需工程变更。本增量是非治疗的数据和交互实现，不改变原日记构念、Safety 映射或 WEEKLY_1.0，不把用户笔记当医学判断。

版本：App 0.2.0（versionCode 2）、SPEC-0.2.0、Room 3、WORKSPACE_1.0、BACKUP_2.0；DIARY_1.0 / SAFETY_1.0 / SAFETY_RULE_1.0 / WEEKLY_1.0 / CBT-I Protocol NONE 保持。

## WorkspaceRecord

独立表 workspace_record；不扩展 SleepDiaryEntry。id 为规范小写 UUID；kind 只能为 LEARNING_NOTE、REFLECTION、CONTEXT_NOTE、CLINICIAN_NOTE。title 保留原文，0–200 Unicode 码点；body 必须非空白，最多 20000 码点，保留空格/换行/原文。两项长度是技术限制，不是医学阈值。

createdAtUtc / updatedAtUtc 是 ISO 时间，updated 不早于 created；revision 为 1–2147483647 的整数，编辑保持 UUID/createdAt 并递增 revision；workspaceSchemaVersion 固定 WORKSPACE_1.0。未知种类/版本、损坏时间、超限文本拒绝保存/恢复，不静默截断。

就诊沟通记录只代表用户实际录入的文字；不能据此验证医生身份、执行医嘱、解锁治疗或覆盖安全限制。其他种类同样不触发评分或自动解释。

## 保存和恢复

保存通过一个 Room transaction 写入、按 UUID 全字段回读，再提交后回读。编辑携带 baseRevision；并发旧稿不得覆盖新值。原命令已提交但确认丢失时，同 UUID/相同内容/下一 revision 的重试返回原记录，不创建副本。写入失败与“可能提交但未确认”分开表示；设备时间倒退时拒绝覆盖更晚记录。

编辑稿另携带原记录全字段的 SHA-256 指纹（仅表单并发检查，不进入数据库字段）；字段以 UTF-8 字节和四字节长度前缀编码，避免分隔符歧义。即使旧备份恢复出相同 UUID/revision 但不同正文，旧编辑稿也会被拒绝，不能因 revision 碰巧相同覆盖恢复的数据。该哈希只用于一致性，不认证任何人的身份。

Room 2→3 只新增独立表，并更新 AppState 的技术 backupFormatVersion；旧五表业务值保留。Room 1→2 保持历史 BACKUP_1.1 语义，再依次升至 3；禁止 destructive migration。

BACKUP_2.0 在既有根对象新增 workspaceSchemaVersion 和 workspaceRecords；新格式完整包含全部已保存个人记录。历史 BACKUP_1.0/1.1 schema 和样例保持不变，继续可导入；旧备份的个人记录集合为空。用户确认替换恢复前必须看到个人记录计数及旧格式将清空该集合的提示。全部六表在同一个事务中替换并验证，任何失败回滚。

原 CSV 和日记 PNG 不包含这些独立记录；就诊摘要也不会自动加入它们。用户仍可主动导出完整 JSON，文件位置由 SAF 选择。未保存的编辑稿只属于表单状态，不纳入备份；页面重建恢复草稿与 UUID，明确保存才写 Room。

## 验证要求

保存/编辑/重试/过期编辑/失败、磁盘重开、Room 1→3 与 2→3、三代备份兼容、全部六表回滚、未知字段/版本/内容拒绝、文本保真、编辑草稿恢复、返回路径和原有全部测试。未跑真机的项目不得宣称通过。
