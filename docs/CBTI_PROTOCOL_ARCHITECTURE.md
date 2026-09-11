# CBT-I Protocol Engine 架构

`ARCH-FULL-0.1`，2026-09-11，工程草案。当前 CBT-I Protocol **仍为 NONE**。`CBTI_PROTOCOL_VA_1.0` 仅为候选版本名称，不是已经发布的协议。关联 [总览](FULL_APP_ARCHITECTURE.md)、[数据设计](FULL_APP_DATA_AND_MIGRATION.md)、[医学待确认项](FULL_APP_IMPLEMENTATION_ROADMAP.md)。

## 1. 单一主协议与可执行权限

主操作来源由用户指定为 VA CBT-I Therapist Manual。已找到 VA 官方 2014 年版书目信息，具体采用版次、全文逐条提取、对当前目标人群及自助模式的适用性仍待临床确认。VA/DoD 2025 是安全覆盖，不作为偷换主协议 TIB 参数的入口。AASM、SR/RCT、中国共识用于各自证据/本地化职责。

任何临床规则进入运行时前必须同时满足：来源准确定位、表达与参数完整、适用范围清楚、临床审核通过、测试与人工判例一致、规则包可信且兼容。缺一项只可作为草稿存档；不使用“常见值”“行业一般做法”补齐。

软件可以先实现纯引擎框架和无临床含义的测试规则，但不能将测试规则暴露给用户为治疗建议。

## 2. Rule Set 契约

### RuleBundle

| 字段 | 要求 |
|---|---|
| bundleId / protocolVersion | 不可变 ID 与语义版本；实际运行课程固定所用版本 |
| sourceEditionIds | 精确手册版次，链接内容登记在 Registry，不只填“VA” |
| protocolRulesVersion / safetyOverlayVersion / exceptionRulesVersion / knowledgeVersion / metricRuleVersion | 各自独立演进并形成兼容矩阵 |
| inputSchemaVersions / engineCompatibility | 允许的输入、表达式语言和解释器版本 |
| contentHash / reviewManifest | 规则字节内容、审核者、审核时间、许可、测试证据 |
| status | DRAFT → EXTRACTED → CLINICALLY_REVIEWED → VERIFIED → RELEASED；亦可 RETIRED |
| applicability | 目标人群、治疗方式、限制及未知项，不能以缺省表示所有人适用 |

### ProtocolRule

| 字段 | 要求 |
|---|---|
| rule_id / ruleVersion / protocol_version | 永久可引用的规则身份 |
| source_id / source_section / printedPage / pdfPageIndex | 必须精确到可复查位置；未核实保持 null，不能虚构页码 |
| relatedKnowledgeIds / reason | 用户解释与临床含义从同一知识节点投影 |
| inputContract / requiredInputs | 类型、单位、缺失语义、观察时间范围 |
| preconditions / expression / parameters | 有界、确定性的表达式及获批参数，全部可检查 |
| allowedPhase / outputContract | 输出目标、作用范围、理由码、需人工参与的情况 |
| safetyDependencies / conflictPolicyRef | 不能绕过安全覆盖或例外限制 |
| reviewStatus / reviewerRefs / testCaseRefs | 规则级审核和边界判例；不是仅包级“已审核”标签 |

以下是**不可执行的占位示例**，所有临床值仍空：

```json
{
  "rule_id": "VA.SRT.INITIAL_WINDOW",
  "protocol_version": "CBTI_PROTOCOL_VA_1.0-CANDIDATE",
  "source_id": "VA_CBTI_MANUAL_2014_CANDIDATE",
  "source_section": null,
  "printedPage": null,
  "pdfPageIndex": null,
  "reviewStatus": "DRAFT",
  "parameters": {"minimumBaselineDays": null, "minimumTibMin": null},
  "expression": null,
  "executable": false
}
```

发布校验必须拒绝这类占位对象。禁止 JS/eval、任意 Kotlin 脚本、下载可执行规则、正则自由文本诊断及模型调用。采用有限的类型化 predicate/action AST：只支持已审查比较、组合、单位算术和明确动作；限制深度、执行步数和输入大小。更复杂的临床判断应输出 NeedsClinicalReview，不能为了“自动完成”扩展推断。

## 3. 引擎接口与决策记录

概念接口：`evaluate(contextSnapshot, pinnedBundle, safetyConstraints, validDirectiveRefs) -> EvaluationResult`。

结果区分：`Proposal`、`NeedsInput`、`NeedsClinicalReview`、`SuppressedBySafety`、`UnsupportedRules`、`TechnicalFailure`。不把后四类转成“保持原方案”的伪成功。是否允许继续既有计划由有效安全/协议规则决定，不能在错误兜底中默认继续。

每份 Proposal 记录固定输入、原 stateRevision、安全水位、命中规则和未命中理由、计算中间量及单位、bundle/engine hash、预期动作和解释引用。Review 后用户确认由应用层重新检查水位并原子提交；UI 从持久化结果渲染，不自行做加减分钟操作。

重放使用当时输入快照和同一版本，不读取当前 Diary 内容替代旧输入。测试可注入时钟和日历策略，生产使用明确捕获的时间。以 commandId 防重复提交。每份 plan revision 保存 supersedes，不删除旧指令。

## 4. 各治疗子模块

### Baseline 与准入

汇集 Diary、独立 Baseline 答案、安全重评及情境。最低日数、有效记录标准、允许的不一致估计、必要评估、固定起床协商、自助/临床协助条件都在规则集中定义。旧 Safety DRAFT 或 NO_MAJOR_FLAG 不足以放行；UNANSWERED/过期/无法核验与阴性结果不同。

UI 可表达用户主诉和时间模式，但不输出“你患有某种失眠/共病”。医生确认是流程的一部分，不是用户失败。

### Stimulus Control

按用户指定的主协议方向，内容设计需覆盖床与睡眠关联、条件性觉醒、困倦再上床、床的用途、清醒/烦躁时离床、不看钟、困倦再回床和起床安排；最终中文文案及例外随来源审核。

不实现夜间 20 分钟倒计时、震动、检查点或“计时到请起床”。任务记录是用户简短自报，不要求夜间交互，不将“是否严格执行”做成绩/惩罚。行动解释链接同一 Registry；存在跌倒等情境时先接受 Safety/Exception 约束。

### Sleep Window / SRT

观察 TIB（Diary 床上时间）与 prescribed time-in-bed/sleep opportunity（治疗允许时间）使用不同类型。最早允许上床时间与实际入床时间分开，不把 bedtime 变成必须入睡的截止要求。与 SC 联合表达，安全覆盖可限制普通起床/窗口指令。

本轮不定义初始窗口公式、最低/最高 TIB、调整步长、阈值或 sleep need。不能因为名称含“压缩”就额外实现另一套渐进压缩协议；需要主协议或独立批准。未来定义每个参数的单位、边界等号、日期归属、四舍五入、有效范围和临床来源。

### Treatment Metrics 与 Weekly Review

新增独立 metricRuleVersion；当前 WEEKLY_1.0 均值不变，日记页不出现新 TST/SE。治疗 TST/SE 的构念、分母、终末清醒、其他睡眠/午睡和不一致原始估计的处理均待逐条批准。禁止为了可计算而将负值钳制为 0、修改 SOL/WASO 或丢弃记录却不留理由。

治疗 review 周期独立于 trackingStartDate 的日记 7 天块。协议日历、有效样本、缺记、延迟 review、停疗期间是否推进、旅行/时区等规则都须审核。Review 输入包含：周期 Diary、依从反馈、经批准指标、sleep need、日间 sleepiness、安全事件、药物/疾病/压力/特殊事件及旅行倒班。不同输入不足或冲突可阻止自动调整。

Review 输出候选类型为保持、延长、缩短、暂停调整、临床 review；实际映射只来自获批主协议。无单夜自动调节、无只看 SE 的单条件捷径。用户漏记不作为零或违背治疗，不制造失败感。

### Cognitive、放松、教育与维持

认知模块采用获批任务版本：初始睡眠信念描述、思维记录、睡眠努力/控制、行为实验、真实经历复盘、后续自评和间隔复习。自定义学习题不冒充已验证量表；原量表和翻译单独确认授权。危险实验不能由自由文本或 AI 生成后执行。

放松/去觉醒内容仅在审核主协议对应内容后纳入，不自动把仓库独立 meditation 计时器当成治疗模块。教育按阶段少量出现并可主动查看，不强化必须依赖 App/音频才能睡。

维持与复发预防的入场、结束、重新评估和复课条件都应版本化。COMPLETED 不是永久安全授权；复发不自动重用旧 TIB。无 App 活跃天数、单晚分数或改善百分比驱动治疗进度。

## 5. 版本变更与临床覆盖

新疗程只能选择已发布且适用的包；既有课程 pin 版本。知识勘误、规则变更、临床覆盖分别升级，兼容矩阵决定是否能继续使用旧包。普通更新不得静默修改生效窗口。

关键安全内容更新可以使旧包技术上不可继续执行，进入重新审核流程；具体哪些变更属于强制复核由审核策略规定，不能用软件版本号推断疾病风险。旧包和决策留存供解释，退休规则不意味着删除历史。

临床指令只在明确授权范围内参与计划形成，不能凭一个 override Boolean 清除 Safety 事件或通用安全限制。约束冲突时不输出新窗口，转需核验路径，详见 [医生桥接](CLINICIAN_BRIDGE_ARCHITECTURE.md)。

## 6. 验证门槛

先构建以下工程测试，再由临床审核者提供“输入—应有动作—来源—解释”的独立金标准，不能只用实现生成自己的期望值：

- 规则发布器拒绝空参数、无来源、未审核、未知单位、循环依赖和未知 action。
- 相同输入/规则包/时间策略重放一致；数组顺序、浮点及序列化不产生不同决定。
- 每条获批规则覆盖边界值、等号、缺项、冲突、适用人群和禁止场景；每个输出可查到有效来源。
- Safety 限制不能被 UI、Exception、人工原文、旧缓存、AI 或 Research 绕过。
- 单晚变更不自动调节；review 周期不足、时区变化、迟到数据与 hold 恢复按审核判例测试。
- 原始估计保留；已提交决策不可变；并发日记修改/安全变更使旧 proposal 失效。
- 独立临床复核、受控试用、可用性和目标人群验证完成前，不声称自动治疗安全有效或准备发行。

确切阈值和算法尚未审核，当前不可能完成上述临床金标准；这不阻止工程设计完成，但阻止真实治疗规则发布。
