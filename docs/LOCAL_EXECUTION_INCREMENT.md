# SPEC-0.2.2 本机恢复代次与激活前检查

用户已批准继续完整版及必要工程变更。App 0.2.2 / versionCode 4、SPEC-0.2.2、Room 5；BACKUP_2.1 结构及所有既有领域版本不变，CBT-I Protocol NONE。

新增 local_execution_state 单例表：singletonId=0，restoreGeneration 为本机随机规范 UUID，activationStatus 当前唯一允许 REQUIRES_REVIEW，localSchemaVersion 固定 LOCAL_EXECUTION_1。旧七张业务表不改。迁移生成本机标识；新安装首次使用检查器时生成并保存；重启不自动变更。损坏元数据不静默当作有效状态。

该表是技术失效标识，不是临床原始数据、历史审核、用户设置或治疗授权。按 FULL_APP_DATA_AND_MIGRATION 第 6 节，它不进入备份也不从备份恢复；完整备份业务结构因此不变。每次成功替换恢复，在同一个 Room transaction 内生成新的 restoreGeneration 并设置 REQUIRES_REVIEW；失败时业务表和本机状态一起回滚。导出/预览/取消不改变标识。任何备份内容均不能指定本机执行权限。

PersistentActivationCheck 准备内存 token，引用已保存输入快照、其哈希及本机代次。inspect 在同一数据库事务内检查代次、快照及当前原始输入哈希。恢复代次变化、输入变化、缺失快照或技术错误分开报告；完全一致也仅返回 REQUIRES_REVIEW，没有激活成功分支。现阶段不读取任何医生自由文本解锁，不把旧 Safety gate 当成授权。

本轮没有实际治疗启动、持久化临床 hold、规则发布或计划提交；未来计划提交必须在自己的提交事务中重新检查，不能复用先前 preflight 的结果。历史快照/决定不因为本机代次改变而改写。单纯恢复相同数据也使旧 token 失效。
