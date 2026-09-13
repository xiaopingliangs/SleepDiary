# DATA_SCHEMA.md — V0.1 数据模型

## 1. 总原则

- 原始记录优先于派生数据。
- 睡眠事件是**主观估计的本地时间**，不是设备测量。
- 不因估计存在小误差而强迫用户“校准”。
- 每条记录必须有稳定 UUID。
- 所有时间字段必须明确区分用户估计的睡眠事件本地时间，与 App 自动记录的创建/更新时间 UTC。
- V0.1 不保存治疗处方。
- V0.1 不计算/展示治疗用 TST、SE。

## 2. 版本常量

```text
diary_schema_version        = "DIARY_1.0"
safety_schema_version       = "SAFETY_1.0"
safety_rule_version         = "SAFETY_RULE_1.0"
weekly_summary_rule_version = "WEEKLY_1.0"
backup_format_version       = "BACKUP_1.1"
cbti_protocol_version       = "NONE"
```

这些常量必须：
- 保存在数据库/导出文件；
- 显示于“关于 → 方法与依据”；
- 不允许用 App 版本号代替。

## 3. SleepDiaryEntry

建议 Room entity：`sleep_diary_entry`

| 字段 | 类型 | 必填 | 含义 |
|---|---|---:|---|
| `id` | UUID string | 是 | 稳定记录 ID |
| `sleepDate` | ISO local date | 是 | 该主要睡眠周期结束/离床所在的本地日期 |
| `bedEnteredAtLocal` | ISO local datetime | 是 | 大约进入平时睡觉位置 |
| `trySleepAtLocal` | ISO local datetime | 是 | 大约开始真正尝试入睡 |
| `sleepOnsetLatencyMin` | integer | 是 | 估计入睡耗时（分钟） |
| `awakeningCount` | integer | 是 | 最后一次醒来前，估计夜醒次数 |
| `wakeAfterSleepOnsetMin` | integer | 是 | 上述夜醒合计估计清醒时长（分钟） |
| `finalAwakeningAtLocal` | ISO local datetime | 是 | 最后一次醒来、之后未再真正睡着 |
| `outOfBedAtLocal` | ISO local datetime | 是 | 真正离开睡觉位置开始一天 |
| `sleepQuality` | integer 1..5 | 是 | 1很差/2较差/3一般/4较好/5很好 |
| `comment` | nullable string | 否 | 特殊情况简短备注 |
| `timeZoneIdAtEntry` | IANA zone ID | 是 | 例如 `Asia/Shanghai` |
| `createdAtUtc` | ISO instant | 是 | 首次保存时间 |
| `updatedAtUtc` | ISO instant | 是 | 最近修改时间 |
| `diarySchemaVersion` | string | 是 | `DIARY_1.0` |
| `consistencyWarning` | boolean | 是 | 用户是否在交叉校验提示后仍按估计保存 |

### 3.1 日期/时间输入

内部必须保存完整 local datetime，而不只保存 `HH:mm`。

普通夜间睡眠 UI 可以只让用户滑动时间，并自动推断跨午夜日期；但必须提供一个低干扰的“调整日期和时间”入口，以支持白天睡眠、跨午夜、倒班或异常作息。

## 4. 输入范围与逻辑校验

### 必须满足的时间顺序

```text
bedEnteredAtLocal
<= trySleepAtLocal
<= finalAwakeningAtLocal
<= outOfBedAtLocal
```

如果 UI 自动推断日期导致顺序错误，应优先让用户调整日期，而不是悄悄修改用户时间。

### 数值

- `sleepOnsetLatencyMin >= 0`
- `awakeningCount >= 0`
- `wakeAfterSleepOnsetMin >= 0`
- `sleepQuality ∈ {1,2,3,4,5}`

技术输入上限：
- 时长字段最大 1440 分钟；
- 夜醒次数最大 99。

这些上限是输入保护，不是医学正常值。

### 交叉一致性

```text
attemptWindowMin = minutesBetween(trySleepAtLocal, finalAwakeningAtLocal)
estimatedWakeWithinAttemptWindow = sleepOnsetLatencyMin + wakeAfterSleepOnsetMin
```

若 `estimatedWakeWithinAttemptWindow > attemptWindowMin`，显示中性校验，不自动改用户数据。

用户选择继续后：`consistencyWarning = true`。

## 5. V0.1 派生值

仅用于周总结和内部检查。

### 5.1 TIB（在睡觉位置的总时长）

```text
tibMin = minutesBetween(bedEnteredAtLocal, outOfBedAtLocal)
```

注意：这不是“总睡眠时间”。

### 5.2 Terminal Wakefulness

```text
terminalWakefulnessMin = minutesBetween(finalAwakeningAtLocal, outOfBedAtLocal)
```

### 5.3 V0.1 明确禁止的派生值

在 `cbti_protocol_version = NONE` 时：
- 不生成治疗用 TST；
- 不生成治疗用 SE；
- 不把任何 SE 阈值用于建议；
- 不根据 TIB/SOL/WASO 生成睡眠窗口。

如果代码出现上述逻辑，视为规格违规。

## 6. SafetyAssessment

建议两个表。

### `safety_assessment`

| 字段 | 类型 |
|---|---|
| `id` | UUID |
| `answeredAtUtc` | ISO instant |
| `safetySchemaVersion` | string |
| `derivedGate` | enum |
| `safetyRuleVersion` | string |
| `status` | enum DRAFT / COMPLETED |
| `isCurrent` | boolean |

### `safety_answer`

| 字段 | 类型 |
|---|---|
| `assessmentId` | UUID FK |
| `questionId` | string (`S01`...`S15`, including conditional `S07b`) |
| `answerCode` | string；逐题编码见 CLINICAL_BOUNDARIES；S09 仅 NONE / UNSURE / FLAGS_PRESENT |
| `safetySchemaVersion` | string，SAFETY_1.0，有意冗余 |
| `detailJson` | nullable JSON string |

必须保存原始答案。S03=YES/UNSURE 提交后立即保存 DRAFT 和已答内容、派生 gate，再展示安全页；DRAFT 不阻止日记且不能清除已保存信号。全部题目完成才标记 COMPLETED。`derivedGate` 是派生快照，未来规则更新可以重新计算，但旧 assessment 不删除。

S09 的 detailJson：NONE / UNSURE 必须为 null；FLAGS_PRESENT 必须是含非空、无重复 selectedCodes 的对象，数据库序列化为 JSON string。代码范围和保存/导出固定顺序严格遵守 CLINICAL_BOUNDARIES 第 7 节。不新增表或改变 SafetyAnswer 总体模型。

## 7. WeeklySummarySnapshot

建议 entity：`weekly_summary_snapshot`

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | UUID | summary ID |
| `periodIndex` | integer | 第几周 |
| `periodStartDate` | ISO date | 7天块开始 |
| `periodEndDate` | ISO date | 7天块结束 |
| `validEntryCount` | integer | 0..7 |
| `avgSolMin` | nullable decimal | SOL 平均 |
| `avgAwakeningCount` | nullable decimal | 夜醒次数平均 |
| `avgWasoMin` | nullable decimal | 夜醒总时长平均 |
| `avgTerminalWakefulnessMin` | nullable decimal | 最后醒来到离床平均 |
| `avgTibMin` | nullable decimal | TIB 平均 |
| `avgSleepQuality` | nullable decimal | 1..5 平均 |
| `sourceEntryIdsJson` | JSON array | 参与计算的原始 UUID |
| `diarySchemaVersion` | string | 原始日记 schema |
| `weeklySummaryRuleVersion` | string | `WEEKLY_1.0` |
| `generatedAtUtc` | instant | 生成时间 |
| `revision` | integer | 同一周期第几版 |
| `isCurrent` | boolean | 当前显示版本 |
| `supersedesSummaryId` | nullable UUID | 前一版 |

### 7.1 WEEKLY_1.0 平均规则

- 只使用该 7 日历日周期中存在的记录；
- 缺失日不填 0；
- `validEntryCount` 明确显示；
- 每个平均值只对有该字段的有效记录求算术平均；
- 小数显示 1 位；
- 原始存储建议保留 Double 精度。

如果 `validEntryCount = 0`，无需强制展示空周报。

## 8. TrackingState

使用 Room 单例/键值 AppState 表保存，随完整备份在同一 Room 事务中恢复。表本身保存对应 backupFormatVersion（当前 BACKUP_2.0）；JSON 根 exportFormatVersion 表示其备份格式版本。JSON 对外仍使用 trackingState 对象名；下列六个键在完整备份中均必填。尚无日记时 trackingStartDate 为 null，尚无当前问询时 currentSafetyAssessmentId 为 null。

保存：

| 键 | 含义 |
|---|---|
| `hasCompletedOnboarding` | Boolean，默认 false；仅表示首次三页说明完成，与日记、问询及首次保存说明独立；完成说明立即持久化 true |
| `trackingStartDate` | 第一次成功保存有效日记时设置；冻结规则见下文 |
| `hasSeenMinimalInteractionExplanation` | 首次保存说明是否看过 |
| `hasSeenSharePrivacyNotice` | 可选的一次性分享隐私提示 |
| `lastDisplayedWeeklyPeriodIndex` | 最近已展示周总结 |
| `currentSafetyAssessmentId` | 当前安全问询 ID |

## 9. 修改策略与周期起点

- 修改 SleepDiaryEntry 保持 UUID、createdAtUtc 不变，更新 updatedAtUtc。
- 第一次成功保存有效日记时设置 trackingStartDate。
- 第一份 WeeklySummarySnapshot 生成前，修改现有最早记录 sleepDate 导致最早日期变化时，允许把 trackingStartDate 更新为数据库当前最早 sleepDate。
- 一旦任何 WeeklySummarySnapshot 生成，trackingStartDate 永久固定。
- 完整恢复使用备份中的 trackingStartDate，不从记录重新推断；记录早于该日期则语义校验失败。
- 已有 snapshot 后，不允许把记录移到 trackingStartDate 之前；提示：“这个日期早于当前记录周期的起点。为了保持既有周数据可追溯，当前版本不能把历史记录移动到周期起点以前。”

同周期修改：已有 summary 才生成新 revision。
从周期 A 移到 B：A 已有 summary 则生成新 revision；B 已结束且已有 summary 才生成新 revision；B 未结束不提前生成。
所有修订保留旧 snapshot，旧 isCurrent=false，新 revision+1、isCurrent=true、supersedesSummaryId 指向前版；源 Entry UUID 不变。

空周期 periodIndex 照常前进，不强制展示；后续只展示已结束、有记录、未展示的最早周期，不把新记录塞回过去周期。


## 10. 漏记

V0.1：
- 不显示补签；
- 不因少一天给负面提醒；
- 不把缺失日自动复制、插值或估算；
- 不自动生成虚假数据。

## 11. SPEC-0.1.2 AppState 无损扩展

Room 技术版本由 1 升为 2，新增 hasCompletedOnboarding NOT NULL DEFAULT 0。迁移保留全部五张表的数据；旧安装默认 false，不从日记或问询推断 true。禁止 destructive migration。
新导出使用 BACKUP_1.1，trackingState.hasCompletedOnboarding 必填。导入 BACKUP_1.0 时采用原 schema，缺少该字段正常兼容为 false；导入 BACKUP_1.1 完整保留 true/false。两者均经完整校验、用户确认、单事务替换和回读验证。其他医学字段和规则不变。

## 12. SPEC-0.2.0 独立 WorkspaceRecord

新增独立 workspace_record 表，字段、技术长度、枚举、修订和校验以 docs/WORKSPACE_DATA_INCREMENT.md 的 WorkspaceRecord 合同为准。它不扩展每日八个构念，不分类 comment，不是医疗计划或医生身份凭据。全部已保存记录纳入 BACKUP_2.0；未保存表单草稿不纳入。Room 3 的 2→3 迁移只新增该表并更新 AppState 技术备份版本，保留旧五表业务数据。第 11 节是历史 0.1.2 迁移记录。
