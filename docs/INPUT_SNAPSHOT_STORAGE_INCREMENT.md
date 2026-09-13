# SPEC-0.2.1 原始输入快照持久化增量

用户已批准继续完整版及必要工程变更。本增量只保存既有原始输入，不启用治疗、医学映射或新每日字段。

App 0.2.1 / SPEC-0.2.1 / Room 4 / BACKUP_2.1；Diary、Safety、Safety Rule、Weekly、Workspace 版本不变，CBT-I Protocol NONE。RAW_INPUT_CAPTURE_1 同时标识快照编码及持久化合同。

独立 input_snapshot 表：id（规范小写 UUID，同时为 capture command ID）、capturedAtUtc（Instant）、fromDate / throughDate（含首尾的 ISO 日期）、captureVersion、payloadBase64（规范标准 Base64，不换行）、contentHash（规范小写 SHA-256）。原输入二进制包含日期范围、版本、日记全部字段、Safety 及实际 AppState；不含独立个人笔记。无指向可编辑原记录的外键，不用今天的数据修正历史。快照不可编辑/单独删除，仅完整替换恢复可替换集合。

捕获、写入和回读在一个 Room transaction；提交后再回读。固定 command ID 重试返回已有快照，即使原始日记后来发生变化也不重复捕获。同 ID 改变范围拒绝。事务内失败回滚，提交后确认失败明确返回可能已提交。当前无自动捕获/治疗 UI 调用。

Room 3→4 新增独立表，旧六表定义不改；技术 AppState backupFormatVersion 更新。历史迁移固定其当时备份版本。完整导出 BACKUP_2.1 新增 inputSnapshots，按 id 排序；导入兼容 1.0/1.1/2.0/2.1。旧格式该集合为空，确认页提示。全部七张业务表单事务替换并回读，损坏哈希/编码/版本/结构/重复 ID 拒绝，不能只凭正确哈希接受任意字节。旧备份 schema/示例字节保持。

历史快照仅检查结构、编码及可表示的数据类型，不用当前安全算法重算历史结论；其内容一致性不是医学有效性或身份认证。CSV、日记 PNG、就诊摘要不加入快照。当前尚未实现恢复代次、持久化 hold、治疗审计和计划提交；备份恢复不能因此解锁治疗。
