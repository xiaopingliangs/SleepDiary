# CLINICAL_BOUNDARIES.md — 医学边界与首次安全问询

## 1. V0.1 的医学定位

V0.1 是**记录工具**，不是诊断工具，也不是 CBT-I 治疗器。

首次安全问询的目的只有一个：

> 保存将来启用完整 CBT-I（尤其睡眠限制/睡眠压缩）之前可能需要医生确认、调整或延迟治疗的关键信息。

因此：

- 安全问询结果不阻止睡眠日记；
- 不输出“适合 CBT-I / 不适合 CBT-I”的医学结论；
- 不诊断双相障碍、癫痫、OSA、昼夜节律障碍或任何精神疾病；
- 不依据结果调整药物；
- V0.1 不存在睡眠限制算法。

## 2. 答案类型

除明确注明外，问题使用：

- `否`
- `是`
- `不确定`
- `不适用`（仅适用于特定问题）

“是/不确定”通常意味着未来治疗功能需要人工/医生确认，而不是判定疾病。每题实际允许的机器编码见下文；S01 仅 YES / NO。

## 3. 安全问询题目（SAFETY_1.0）

### S01 — 成人范围

**问题：** 你现在是否已满 18 岁？

**答案仅限：** YES（是）/ NO（否），不提供 UNSURE。

**动作：**
- YES：无额外标记。
- NO：`futureTreatmentGate = CLINICIAN_REVIEW`。
- 日记始终可用。

### S02 — 当前身体状况是否不稳定

**问题：** 你目前是否正处于严重身体疾病的急性期，或医生曾告诉你当前身体状况不稳定？

**辅助说明：** 不需要自己列举所有疾病。这里关心的是“当前是否处于明显不稳定/急性状态”。

**动作：**
- 是/不确定：`CLINICIAN_REVIEW`。
- 日记可用。

### S03 — 当前自伤/自杀急性安全信号

**问题：** 你目前是否有自伤或自杀的明确计划、准备行为，或担心自己无法保证自身安全？

**动作：**
- YES / UNSURE：提交本题后立即将当前已答内容（包含 S03 原始答案）持久化为 DRAFT assessment，保存当前派生 gate 至少为 DELAY_AUTOMATED_TREATMENT，立即显示安全求助页；不得等待其余题目完成。
- 安全页允许「退出应用」或「继续记录睡眠」；继续可直接进入日记，不强制补完问卷。
- 未完成问卷保持 DRAFT，后续启动不因此阻止日记；设置提供中性「安全问询尚未完成」入口。不得因未完成清除已保存 S03 信号。
- 全部问题完成后改为 COMPLETED。
- NO：继续。

**中国大陆 V0.1 安全提示文案：**

> 如果你现在可能无法保证自己的安全，请优先联系现实世界中的帮助，而不是继续研究睡眠数据。可以联系身边可信任的人、就近急诊；紧急情况可拨打 120 或 110。全国统一心理援助热线：12356。

不得在此页面做风险分级或要求用户继续完成长问卷后才显示帮助。

### S04 — 其他急性精神状态

**问题：** 你目前是否正出现明显失控、严重混乱、现实判断明显异常，或医生认为需要优先处理的急性精神症状？

**动作：**
- 是/不确定：`DELAY_AUTOMATED_TREATMENT`。
- 日记可用。

### S05 — 双相障碍/躁狂/轻躁狂诊断史

**问题：** 你是否曾被医生明确诊断过以下任一情况：**双相障碍（Bipolar disorder）、躁狂发作（Manic episode）或轻躁狂发作（Hypomanic episode）**？

**辅助说明：** 这里只问医生明确诊断，不要求你根据网络资料自行判断。

**动作：**
- 是/不确定：`CLINICIAN_REVIEW`。
- 日记可用。

### S06 — 近期“睡得少却不困 + 明显激活”信号

**问题：** 最近是否出现过连续几天明显比平时睡得少，但并不觉得困，同时出现异常兴奋、精力明显增多、活动显著增加或冲动明显增加？

**辅助说明：** 这个问题不用于诊断双相障碍，只是为了在未来进行睡眠限制前提醒医生进一步确认情绪状态。

**动作：**
- 是/不确定：`CLINICIAN_REVIEW`。

### S07 — 癫痫/反复发作性疾病

**问题：** 你是否患有**癫痫（Epilepsy）或其他反复癫痫发作性疾病**？

主问题答案：NO / YES / UNSURE。

- NO：无额外 gate，不进入 S07b。
- UNSURE：CLINICIAN_REVIEW，不进入 S07b。
- YES：进入 S07b。

**S07b：最近是否仍有发作，或医生认为控制不稳定？**

| answerCode | 中文选项 | gate |
|---|---|---|
| STABLE | 目前控制稳定，近期无发作，或医生认为控制稳定 | CLINICIAN_REVIEW |
| UNSTABLE | 近期仍有发作，或医生认为控制不稳定 | DELAY_AUTOMATED_TREATMENT |
| UNSURE | 不确定 | DELAY_AUTOMATED_TREATMENT |


### S08 — 明显日间嗜睡

**问题：** 白天是否经常出现“控制不住要睡着”的情况，例如开车、骑车、工作、坐着等待或与人交谈时会不由自主地打瞌睡甚至睡着？

**辅助说明：** “身体累、没有精神”不等于这里所说的“嗜睡”；这里问的是是否会真正控制不住地睡着。

选项：
- 没有
- 偶尔
- 经常
- 曾因此出现危险

**动作：**
- 经常/曾出现危险：`DELAY_AUTOMATED_TREATMENT`
- 偶尔：`CLINICIAN_REVIEW`
- 没有：无标记

### S09 — 睡眠呼吸障碍提示

**问题：** 你是否有以下任一情况？（可多选）

- 经常大声打鼾；
- 睡眠中被别人发现暂停呼吸；
- 经常憋醒或喘醒；
- 已被医生诊断阻塞性睡眠呼吸暂停（OSA）；
- 以上都没有；
- 不确定。

**动作：**
- 命中前四项或不确定：`CLINICIAN_REVIEW`。

**注意：** 这不是 STOP/STOP-BANG 的正式实施，不得把结果显示为“OSA 阳性/阴性”。

### S10 — 其他已诊断睡眠障碍

**问题：** 你是否曾被医生诊断过其他睡眠障碍？

示例（仅用于帮助识别名称）：
- 不宁腿综合征（Restless legs syndrome, RLS）
- 发作性睡病（Narcolepsy）
- REM 睡眠行为障碍（REM sleep behavior disorder, RBD）
- 梦游/睡行症（Sleepwalking）
- 夜惊（Sleep terrors）
- 昼夜节律睡眠-觉醒障碍（Circadian rhythm sleep-wake disorder）
- 其他

选项：没有 / 有 / 不确定

若“有”，允许选择一个或多个名称并可填写“其他”。

**动作：**
- 有/不确定：`CLINICIAN_REVIEW`。

### S11 — 夜间安全离床能力

**问题：** 你在夜间独自起床、离开床并再次回到床上是否安全？

**辅助说明：** 例如是否有明显跌倒风险、严重行动困难，或需要他人协助才能安全上下床。

选项：安全 / 可能不安全 / 不确定

**动作：**
- 可能不安全/不确定：`CLINICIAN_REVIEW`。

### S12 — 活动性酒精/物质使用障碍

**问题：** 你目前是否正因酒精或其他成瘾性物质使用问题接受治疗，或使用已经明显失去控制？

**辅助说明：** 这不是在问“是否偶尔喝酒/抽烟”，而是在问已经形成严重使用问题或正在接受相关治疗的情况。

**动作：**
- 是/不确定：`DELAY_AUTOMATED_TREATMENT`。

### S13 — 暴露型 PTSD 治疗

**问题：** 你现在是否正在接受以反复面对、回忆或暴露于创伤内容为核心的 PTSD 暴露治疗？

选项：NO（否）/ YES（是）/ UNSURE（不确定，包括不知道是否属于暴露治疗）/ NOT_APPLICABLE（不适用，仅没有接受 PTSD 暴露治疗）

**动作：**
- 是/不确定：`CLINICIAN_REVIEW`。

### S14 — 妊娠/产后

**问题：** 你目前是否处于妊娠期或产后阶段？

选项：否 / 是 / 不适用 / 不确定

**动作：**
- 是/不确定：`CLINICIAN_REVIEW`。

### S15 — 昼夜节律提示

**问题：** 如果完全没有上班、上学或闹钟限制，你是否往往会在明显更晚的时间自然入睡、明显更晚自然醒来，而在这种时间安排下睡眠反而相对正常？

选项：否 / 是 / 不确定

**动作：**
- 是/不确定：`CLINICIAN_REVIEW`。

**注意：** 不显示“你患有睡眠-觉醒时相延迟障碍”。只保存“值得在未来治疗前向医生说明”。

## 4. 分流结果

内部枚举：
- `NO_MAJOR_FLAG`
- `CLINICIAN_REVIEW`
- `DELAY_AUTOMATED_TREATMENT`

优先级：
`DELAY_AUTOMATED_TREATMENT` > `CLINICIAN_REVIEW` > `NO_MAJOR_FLAG`

用户界面不得显示“绿色/黄色/红色”或“通过/未通过”。

### NO_MAJOR_FLAG
> 当前问询没有发现需要特别标记的项目。当前版本仍然只是睡眠日记，并不代表医学诊断或确认你适合任何治疗。

### CLINICIAN_REVIEW
> 你填写的部分情况，在未来如果启用完整 CBT-I 治疗功能前，建议先告诉医生并确认是否需要调整方案。当前睡眠日记可以正常使用。

### DELAY_AUTOMATED_TREATMENT
> 你填写的部分情况意味着未来不应直接由无人监管的软件启动睡眠限制等治疗功能，应先由专业人员处理或确认。当前睡眠日记可以正常使用。

## 5. 不进入首次“安全门”的内容

以下内容可能与失眠有关，但 V0.1 **不把它们作为首次强制安全问询**：

- 咖啡因；
- 尼古丁/吸烟量；
- 普通饮酒；
- 运动；
- 午睡；
- 屏幕使用；
- 房间光线/噪声；
- 一般工作压力；
- 一般焦虑；
- 慢性疼痛；
- 饮食。

原因：它们可以用于未来个性化评估/教育，但通常不是“是否可以直接启动无人监管睡眠限制”的第一道安全边界。

## 6. 药物

V0.1：
- 不询问具体药名作为日常必填；
- 不提供减药/加药建议；
- 不把任何药物改变解释为睡眠变化原因。

自由备注允许用户自行记录“医生调整了药物”等特殊情况。

未来若加入专门药物状态字段，必须另行版本化。

## 7. 机器编码与持久化

逻辑只使用机器编码，不以中文文案为判断条件。原有各题的 gate 规则保持不变；S01、S07/S07b、S13 按本次明确决议修正。

| questionId | 允许的编码 |
|---|---|
| S01 | YES / NO |
| S02、S03、S04、S05、S06、S07、S12、S15 | YES / NO / UNSURE |
| S07b（仅 S07=YES） | STABLE / UNSTABLE / UNSURE |
| S08 | NONE / OCCASIONAL / FREQUENT / DANGER_OCCURRED |
| S09（多选） | NONE / UNSURE / FLAGS_PRESENT；具体项存 detailJson.selectedCodes |
| S10 | NO / YES / UNSURE |
| S11 | SAFE / POSSIBLY_UNSAFE / UNSURE |
| S13、S14 | NO / YES / UNSURE / NOT_APPLICABLE |

S08：NONE 无额外 gate；OCCASIONAL 为 CLINICIAN_REVIEW；FREQUENT / DANGER_OCCURRED 为 DELAY_AUTOMATED_TREATMENT。
S09：NONE 不增加 gate；UNSURE / FLAGS_PRESENT 为 CLINICIAN_REVIEW。NONE 与 UNSURE 各自与所有其他选项互斥。具体项数量不影响分流，不计算风险等级，不诊断 OSA，不显示阳性/阴性。
S11：SAFE 无额外 gate；POSSIBLY_UNSAFE / UNSURE 为 CLINICIAN_REVIEW。
S13：YES / UNSURE 为 CLINICIAN_REVIEW；NO / NOT_APPLICABLE 无额外 gate。
S14：YES / UNSURE 为 CLINICIAN_REVIEW；NO / NOT_APPLICABLE 无额外 gate。

S10=YES 时 detailJson 是对象，diagnoses 仅保存用户选择的代码：
RLS / NARCOLEPSY / RBD / SLEEPWALKING / SLEEP_TERRORS / CIRCADIAN_RHYTHM_DISORDER / OTHER。
otherText 为用户输入的字符串；不根据症状自动生成 diagnosis code。示意：
```json
{"diagnoses":["OTHER"],"otherText":"用户填写的其他名称"}
```

每条 safety_answer 自身携带 safetySchemaVersion。assessment 同时携带 safetySchemaVersion、safetyRuleVersion、status（DRAFT / COMPLETED）。derivedGate 保存上述分流的版本化快照。
### S09 固定存储结构

保持现有单值 answerCode + detailJson，不新增表、不改变 SafetyAnswer 总体模型。
- NONE：用户明确选择“以上都没有”，detailJson 必须为 null。
- UNSURE：用户选择“不确定”，detailJson 必须为 null。
- FLAGS_PRESENT：用户选择至少一个具体提示项，detailJson 必须为对象 {"selectedCodes":[...]}。

selectedCodes 只允许下列代码，且至少一项、无重复；不得包含 NONE、UNSURE：
1. LOUD_SNORING
2. WITNESSED_APNEA
3. GASPING_AWAKENING
4. DIAGNOSED_OSA

保存和导出必须去重并按上述固定枚举顺序排列，确保 JSON 可重复。导入备份须满足枚举、非空、唯一约束；排序是保存/导出的规范化，不影响分流。
数据库 detailJson 保存对象的 JSON 字符串，JSON 备份中保存对象本身；NONE / UNSURE 在两者中均为 null。

