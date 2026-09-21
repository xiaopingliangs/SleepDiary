# Full App Data Contract

2026-09-21，TREATMENT_DATA_1.0 工程合同。当前基线 Room 5/BACKUP_2.1；首次落地新增治疗业务表时使用显式 Room 5→6、BACKUP_3.0，旧四代 schema/样例保持字节不变。临床算法版本与数据 schema 分开；未发布协议写 NONE，不把工程版本冒充医学批准。

## 第一阶段持久化切片

TreatmentEpisode：id（规范小写 UUID，入组 command ID）；mode；state（17 态）；pausedFromState nullable；adultScopeConfirmed；protocolVersion、metricsVersion、safetyRuleVersion、exceptionRuleVersion；currentPlanId nullable；startedAtUtc、createdAtUtc、updatedAtUtc；completedAtUtc nullable；revision 正整数；schemaVersion=TREATMENT_DATA_1.0。Phase 1 四类医疗版本均 NONE，currentPlanId/completedAtUtc 为 null。开始评估不等于治疗启动。mode 与成人范围确认必须显式提供；不保存出生日期。至多一个未完成/未退出 episode；COMPLETED/DIARY_ONLY 历史不得被重用成新 episode。

TreatmentAuditEvent：id（规范 UUID/command ID）、episodeId（外键 NO ACTION）、commandType、requestFingerprint（完整命令的规范编码 SHA-256）、fromState nullable、toState、fromRevision、toRevision、occurredAtUtc、restoreGenerationAtCommand、schemaVersion=TREATMENT_DATA_1.0。只追加、同 episode/toRevision 唯一。初始事件 0→1，后继连续，时间不得早于前事件；每次转换都能回放其状态/修订。requestFingerprint 不是身份认证。恢复保留事件历史代次，但不把它作为新本机授权。

Phase 1 不伪造临床输入/安全快照：TreatmentPlan 先建立严格的领域不可变模型，必需字段不得用空字符串/假 UUID 填充；实际计划表与可执行写入在 Phase 4 同 metrics/Safety 引用可验证后接入。故 Phase 1 的 plan 部分仅为骨架，不标记“计划激活 PASS”。首次持久化输出包括 episode+audit；后续业务集合新增时继续升级 backup 小版本，不能悄悄修改 BACKUP_3.0 定义。

## 后续实体合同（各阶段落库前补精确 DDL/schema）

| 对象 | 必需内容 | 不变量 |
|---|---|---|
| TreatmentMetricSnapshot | id、inputSnapshotId、sourceEntryIds及对应 schema/updatedAt/内容hash、from/through、algorithm/protocol版本、typed metrics、missing/validity、createdAt、revision/supersedes | 不可变；不能从今天的可变日记重算历史；NaN/Infinity拒绝；单位与舍入须有规则 |
| TreatmentSafetyCheck | id、episodeId、输入快照、规则版本/哈希、匹配规则、全部约束/缺项、disposition、创建时间、前决策ref | 旧 SAFETY_1.0 不改；不是诊断；缺项不 CONTINUE |
| ExceptionEvent/Decision | id、明确提交事实、ruleId/source/locator/version、disposition/protocolImpact/范围/时效、snapshot、supersession | 不从 comment 推断事件；不记录探索性点击 |
| TreatmentPlan | id、episodeId、revision、protocol/ruleSet/SC版本、validFrom/Until、fixedWakeTime、tibMinutes、earliestBedtime、mode/origin、input/metric/safety snapshot refs、reason、createdAt、supersedesPlanId、status、schemaVersion | 激活后业务字段不可变；有效状态由追加生命周期事件+episode指针投影，不能反写旧计划；跨episode引用拒绝 |
| TreatmentWeeklyReview | id、episode/plan、独立周期、metrics和安全/例外refs、明确提交依从/情境/SleepNeed来源、结果KEEP/EXTEND/SHORTEN/HOLD/ADAPT/CLINICIAN_REVIEW、理由/来源/版本 | 不用 WEEKLY_1.0 代替；SNQ权利未满足不得自制输入 |
| ClinicalInstruction | id、原文/录入时间、来源USER_ENTERED_CLINICIAN_INSTRUCTION、结构化明确提交值、适用范围、撤销/前项ref、版本 | 原文不自动解析成计划；不宣称验证医生身份；不能扩大Safety许可 |
| Education records | 内容稳定ID/版本、明确完成/最终反思/保存profile/复习/医生问题/最终总结、时间/修订/schema | 复用Workspace自由记录；结构化另表，不藏body中；不存点击、展开或耗时 |
| MaintenancePlan | episode、用户个人计划、版本/时间/前项ref、何时记录/不自调/求助 | 不自动判愈；复发新episode留史 |
| TreatmentControl/PlanLifecycle | 当前episode/plan指针、限制水位、用户暂停意愿、状态revision、命令/审计、版本 | 一事务一致；业务控制状态进入完整backup |

## 事务、重试和本机执行

命令包含 expectedRevision 和本机 restoreGeneration。Controller 事务内读取，先识别已提交同命令，再检查当前代次和版本；重试旧命令只能查询历史结果，不因此执行旧 plan。重复 command 内容不同拒绝。插入/更新和 audit 一事务，全字段回读；提交后再回读，失败标 mayHaveCommitted，不重复创建修订。

计划提交后续复用 InputSnapshotStore / RAW_INPUT_CAPTURE_1 和 LocalExecutionRepository；新的治疗事实独立版本编码。完整提交必须原子核对 current input hash、状态/安全水位、规则包哈希、医生指令撤销、恢复代次。预检通过本身不授权。

## BACKUP_3.0 增量

保留 2.1 envelope 全部原业务字段；新增 treatmentSchemaVersion、treatmentEpisodes、treatmentAuditEvents，按 id 排序。3.0 schema 明确 required/additionalProperties=false；本版本不含尚未落库的实体、不声称覆盖完整最终产品。后续扩展需明确新版本。

- 导入 1.0/1.1/2.0/2.1：按原解释器读取，episode/audit 为空；预览说明现有治疗记录将被替换为空。
- 导入 3.0：严格结构+语义校验版本/枚举/UUID/时间/连续审计/episode引用/唯一未结束疗程/指针；未知规则执行能力不接受为授权。Phase 1 仅支持其已实现的非处方状态。
- 预览只读；确认后全部业务对象和 AppState 单事务替换；外键顺序 audit→episode→旧表删除，父表→子表插入，所有检查点失败整库回滚；原local generation亦回滚。
- 成功恢复生成全新 LocalExecutionState.restoreGeneration 并 REQUIRES_REVIEW；该表不导出，不篡改历史事件里的旧代次。
- 导出调用schema和语义校验；只在流正常关闭后报告成功。SAF仍由用户选位置，不导出真机数据做开发测试。
- 旧migration版本常量固定为历史值；迁移不能跟随新的 current backup version。

## 回归门槛

Room 1/2/3/4/5→6 全旧字段保持；新安装与磁盘重开；非处方 episode 创建/读取/转换/幂等/并发；每事务检查点故障；五代backup round-trip/replace/坏文件零变更；旧备份替换清空新集合有提示；恢复代次变化；PNG/CSV不增加任何治疗字段。Phase 4 再加真实 active/non-active计划和跨引用迁移测试，不能用 Phase 1 空计划测试冒充。
