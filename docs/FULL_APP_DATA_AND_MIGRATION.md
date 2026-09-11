# 完整版数据、审计与迁移设计

`ARCH-FULL-0.1` 工程草案，2026-09-11；关联 [总览](FULL_APP_ARCHITECTURE.md)。本文件描述未来新增设计，不修改当前 DATA_SCHEMA、Room 2、BACKUP_1.1 或任何医学语义。新业务 schema、备份和迁移在相应阶段经批准后才落地。

## 1. 不变量与历史局限

现有五表、字段值、UUID、createdAtUtc、历史 Safety 及 Weekly revision 全部保留。DIARY_1.0、SAFETY_1.0、SAFETY_RULE_1.0、WEEKLY_1.0 的旧数据不更名、不按新协议重算。AppState 的 onboarding/minimal explanation 等保持独立含义；治疗是否启动不能复用这些字段。

当前 Diary 允许原 UUID 编辑，旧周报保留汇总而只关联 source UUID。未来不能声称通过旧 UUID 可重建每次过去的日记值。以引入新机制的时间为起点，为治疗决策捕获不可变 `InputSnapshot`；旧历史只保存其实际存在的信息，并明确“原始历史输入快照不可用”。不猜测或补造过往 revisions。

当前时间差采用本地墙钟日期时间；原 timezoneId 与记录时间保持原义。未来治疗的时区、夏令时、跨区旅行和固定起床策略单独版本化，不把旧墙钟差批量改为 UTC 差。

## 2. 建议新增实体

下列字段为工程模型，不是最终数据库 DDL。所有持久化对象有 ID、各自 schemaVersion、创建时间和来源；不可变对象靠 successor 记录变化，可变投影使用 revision 乐观锁。UTC 元数据与用户本地事件时间分开。空缺用显式 UNANSWERED/UNKNOWN/NOT_APPLICABLE（按字段契约），不能一律 false。

| 实体 | 核心字段/关系 | 约束和用途 |
|---|---|---|
| TreatmentEnrollment | courseId、phase、resumePhase、mode、bundleId、stateRevision、startedAtUtc、endedAtUtc、calendarPolicyRef | mode 未确定不能默认 self-guided；同一时点至多一个可执行疗程 |
| BaselineAssessment / BaselineAnswer | assessmentId、status、answerSchemaVersion、questionId、typedValue、reportedAtUtc、source | 独立问题版本，不扩展 Diary；睡眠投诉类型不等于诊断 |
| ContextObservation | observationId、courseId?、contextSchemaVersion、kind、typedPayload、observedLocalTime?、zoneId?、reportedAtUtc、provenance | 药物变化等仅来自明确自报/人工录入，未回答与“无变化”不同 |
| SafetyEvent | eventId、sourceObservationId、schemaVersion、occurredAt?、receivedAtUtc、sequence | 原始事实不可变；纠错另建 superseding event，不静默删除 |
| SafetyDecision | decisionId、inputSnapshotId、ruleVersion、ruleHash、matchedRuleIds、constraints、unresolvedRequirements、supersedesId? | 保存当时判断，不用新规则覆盖旧结果；不是疾病诊断 |
| InputSnapshot | snapshotId、captureVersion、capturedAtUtc、sourceRefs、canonicalPayload、contentHash | 复制当次实际使用的原始字段和缺项，原 UUID + updatedAtUtc 仅作为引用而非内容替代 |
| TreatmentPlanRevision | planId、courseId、revision、supersedesPlanId?、decisionId、windowIntent、wakeTarget、timePolicyRef、effectiveRange | 时间窗口类型和单位明确；过去生效计划保留；唯一当前投影可事务更新 |
| ProtocolDecision | decisionId、commandId、courseId、inputSnapshotId、bundleId/hash、engineVersion、trace、result、beforeState、afterState、actor | 不可变；同 commandId 至多一份决策；重放需相同输入与规则包 |
| TreatmentReview / ReviewAnswer | reviewId、courseId、reviewPeriodId、start/end、status、answerVersion、submittedAtUtc | 周期按主协议独立定义，不复用 WeeklySummarySnapshot 的 periodIndex |
| TreatmentMetricSnapshot | metricSnapshotId、inputSnapshotId、metricRuleVersion、valuesWithUnits、missingness、qualityFlags | 将来 TST/SE 只能在获批治疗版本中计算，不加入 WEEKLY_1.0；定义/有效样本待审核 |
| AdherenceObservation | observationId、planRevisionId、taskId、response、reportedAtUtc | 计划与主观执行反馈分开，不能仅由睡眠时间推断违背治疗 |
| CognitiveRecord / ExerciseAttempt | recordId、courseId、instrumentOrExerciseVersion、response、reviewOfId?、consentScope | profile/思维记录/行为实验/复习；受许可及版本约束，不自动输出诊断分数 |
| LearningProgress | courseId、knowledgeNodeId、contentVersion、interactionStatus | 已学习不等于已经理解/适合治疗，不用点击次数推断疗效 |
| ExceptionEvent / ExceptionDecision | eventId、contextRefs、inputSnapshotId、exceptionRuleVersion、resolutionScope、validity、knowledgeRefs | 保存触发、待确认和结论；短期例外不覆写永久主协议 |
| ClinicianDirective | directiveId、originalText、enteredBy、claimedAuthor、verificationStatus、reason、enteredAtUtc、effectiveRange、scope、originalStateRef、supersedesId? | 自报医嘱与核验意见分开；原文不是可执行脚本，详情见医生桥接 |
| AuditEvent / PendingDomainEvent | eventId、commandId、action、actor、before/afterRef、recordedAtUtc、processingStatus | 审计追加；待处理队列可重试；不记录调试日志中的健康原文 |
| ContentPackageArchive | packageId、kind、version、hash、sourceEditions、reviewRecordRefs、payload | 保留历史重放所需包；导入归档不自动获得可信执行权限 |
| TreatmentActivationCheck | courseId、checkedInputWatermark、verifiedBundleHash、restoreGeneration、status | 本机技术可执行性检查与恢复的历史治疗状态分离，不假冒临床结论 |

主键建议 UUID；业务唯一约束包括 course/revision、commandId、source event/consumer、单一有效计划投影。每条决策都保存使用的 diary/safety/metric/exception/knowledge/protocol 版本集合，不能只写当前 App 版本。

Baseline 的候选输入覆盖：足够日记、睡眠投诉/模式、作息与起床需求、工作班次、驾驶和机械/电气/高处风险、fatigue 与 sleepiness、药物及变更、疼痛/身体病史、心理安全、其他睡眠障碍线索。具体题干、选项、必填条件、有效期与分流映射必须在医学审查后形成独立 schema；本表没有批准新的医学问卷。

## 3. 快照、哈希与可重放

一个快照须携带以下概念，不只存聚合数字：

```json
{
  "snapshotId": "example-only",
  "captureVersion": "DRAFT",
  "sourceRefs": [
    {"entityType": "SleepDiaryEntry", "id": "original-uuid", "schemaVersion": "DIARY_1.0", "updatedAtUtc": "original-value"}
  ],
  "payload": {"rawDiaryRows": [], "baseline": null, "context": [], "missingInputs": []},
  "canonicalizationVersion": "DRAFT",
  "contentHash": null
}
```

这是结构示意，不是可导入备份。实现时原始字符串不 trim、不 Unicode 改写、不“修正”估计；仅以确定的键顺序、数组顺序、UTF-8、数值编码生成规范表示。保留 null/空串区别，定义浮点拒绝 NaN/Infinity 及比较方式。哈希证明内容一致性，不证明资料来源可信或防止有权限的人篡改。

重放输入包含时钟取值、时区策略、规则包及 engine 版本，不能读取“今天”或在线最新知识。老引擎/规则若不再允许运行医疗输出，仍可在隔离的审计解释器中验证历史；不能把重放结果重新当成今日处方。

## 4. 写入及并发事务

### Diary 与下游分离

沿用 DiaryRepository 的原始值校验、warning 确认、UUID 回读和 trackingStartDate/日记周报规则。未来在同事务追加待处理事件，治疗运算在事务外执行。原始记录成功保存后，引擎故障只影响治疗可用性，不否认日记已落库。

### 治疗指令提交

以 `commandId + expectedStateRevision + safetyWatermark + inputHash` 防重与并发。提交事务重新检查输入是否改变、安全约束是否仍适用、人工指令是否撤销、内容包是否可信；不满足就返回 StaleProposal，保留当前有效状态。满足后共同写入 Decision、Plan、状态投影、审核轨迹，回读验证后才宣称完成。

进程在提交前死亡，事务回滚；提交后但 UI 未收到结果，重启按 commandId 找回已提交结果，不重复创建 revision。既有 SaveResult.Failure 可能表示提交后验证失败，不能把它直接解释为“数据库肯定没写入”。后续编排要提供明确的可查询提交状态。

### Safety 事件

新增已保存 Safety 事件原子提升水位并使旧未确认 proposal 失效。UI 每次展示可执行指令均检查其当前可用状态；不只在生成时检查一次。清除 hold 需要新的经审核决策，不能删除事件、重置日期或通过旧备份绕开本机激活检查。

## 5. Room 无损升级

1. 以当前 Room 2 的导出 schema 为起点。后续首次加表预计版本 3，但本轮不创建 schema/迁移；最终编号以实际提交为准。
2. 优先新增上述独立表，旧五表字段与索引语义保持。确需改旧字段须单独规格批准；不为了规范命名重建原始数据。
3. 每版显式迁移，保留导出 schema 历史；禁止 destructive fallback、卸载、删库及清空升级。
4. 无治疗历史的旧安装初始仍为 DIARY_ONLY。旧 NO_MAJOR_FLAG、onboarding=true、已有 30 天日记均不自动启动疗程。
5. 保持 applicationId 与升级签名兼容；从调试安装切换发行签名须先设计可恢复路径，不能要求卸载真实数据来测试。安装前核对本机签名与备份，私钥不写 Git。
6. 迁移失败不得用旧 APK 强行降级已迁移数据库。恢复依赖支持该数据库/备份版本的应用，先保护原文件；不提供未验证的破坏性 downgrade。

## 6. 完整备份演进

当前导出仍 BACKUP_1.1；历史 BACKUP_1.0 schema 和样例逐字节保留。完整版新增完整恢复对象后必须升级 format，建议下一代采用 **BACKUP_2.0（提议，未启用）**，正式版本在数据规格阶段批准。

建议新 envelope 分为 `exportFormatVersion`、`schemaVersionMap`、`legacyDiaryData`、`treatmentData`、`contentArchive`；实际命名可在冻结新 schema 时确定。应用可执行能力不是备份文件能授予的权限。

| 输入 | 转换策略 | 恢复后的执行边界 |
|---|---|---|
| BACKUP_1.0 | 按原解释器读取；onboarding=false；治疗集合为空 | 日记正常，未加入治疗 |
| BACKUP_1.1 | 全量恢复原五表及 onboarding；治疗集合为空 | 不推断已有疗程或低风险 |
| 新格式且全部版本受支持 | 校验所有表、引用、revision、内容哈希及历史版本后替换 | 历史状态保留；当前治疗输出须重新完成本机激活/适用性检查 |
| 未知新格式/必需 schema/规则解释器 | 拒绝完整恢复并说明不支持 | 不清表、不部分导入、不改变现有数据 |

旧备份替换一个已经有新治疗数据的安装，预览必须明确“新疗程数据也将被替换为空”，不能保留原治疗表搭配另一套 Diary。取消则零修改。确认后全部业务表与旧 AppState 在单一 Room 事务中替换；外键顺序、归档内容、current 指针、decision 链及字段回读一起验证，任一失败回滚。

新格式保留恢复文件中的历史疗程状态；另生成本机 restore generation/技术激活检查记录。它只表示“本机尚未确认当前可执行性”，不篡改备份里的原临床决定。复核前不执行恢复的窗口建议。历史包可归档展示，但任意用户文件即使哈希正确也不能作为可信规则包运行。

当前 BackupCodec 用当前 Safety 规则做语义校验；新增规则时必须建立历史版本分派，按声明版本验证，不能用新规则否决或重标旧答案。当前 SchemaValidator 是支持特定关键词的本地实现，未来添加 schema 语法须先实现校验能力并做独立一致性测试，不能只更新 schema 文件就宣称支持。

文件大小、嵌套深度、记录数及内存预算作为明确技术限制设计并测试；超过限制明确拒绝且原库不变，不静默截断备注或舍弃旧 revisions。公共文件的写入成功以流关闭结果为准，取消和失败不显示导出成功。

## 7. 出口隐私不合并

| 出口 | 默认内容与约束 |
|---|---|
| 现有日记 PNG | 原八字段/日期等冻结白名单；无 comment、Safety、治疗/医生/情境信息 |
| 现有 CSV | 按冻结列含 comment，正确转义；不是完整恢复格式 |
| 完整 JSON | 含备份范围全部敏感数据，用户主动选择位置，恢复是替换 |
| 医生摘要（未来） | 用户逐类选择、预览；明确允许后才包含备注/Safety/药物等 |
| AI 咨询模板（未来） | 默认通用说明；用户主动选择个人摘要，不写回引擎 |

每个出口使用自己的专用 DTO 与序列化器，禁止复用完整 entity 通用反射导出。SAF、FileProvider 与临时读权限继续沿用；不新增运行时网络、云备份、广告或遥测。

## 8. 必须建立的验收矩阵

- Room 1→2→未来当前版及 2→未来当前版：五表逐字段、UUID、时间、备注 null/空串/Unicode、Safety 历史、周报 revision、AppState 全部保留。
- 每种支持备份版本的 round-trip、非空库 replace、取消/坏文件/未知版本拒绝；删除、分批写入、状态写入、提交前各检查点故障均回滚。
- 新 treatment 表存在时导入旧格式：确认前不动数据，确认后全部替换且无孤儿引用；不悄悄保留旧治疗。
- Diary 编辑不改变旧决策输入快照；新 review 使用新快照；旧值确实不可恢复时明确标注。
- 重复 command、同时 edit/review/Safety event、提交前后进程被杀：无双 revision、无过期窗口、无半库恢复。
- 恢复包中的未信任规则不运行；历史版本回放与新准入隔离；restore 后旧安全/计划不能绕过本机复核。
- 时区、DST、旅行、跨午夜与日历周期测试在医学/时间语义批准后按边界逐项验证。
- 所有输出白名单、CSV 转义、JSON comment 往返、SAF 取消/流失败、PNG 排除敏感字段继续作为回归门槛。

本轮仅制定矩阵，不声称这些未来迁移和治疗测试已经通过。
