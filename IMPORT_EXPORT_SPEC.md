# IMPORT_EXPORT_SPEC.md — 导入、导出与分享

> 当前执行基线（2026-10-03）：`BACKUP_3.1` / Room 7 / CBT-I Protocol `NONE`。下方既有各格式定义与样例说明作为其原版本合同保留；本文末尾 P2A addendum 是当前合同及 BACKUP_3.1 增量，不得据此改写 BACKUP_1.0–3.0 的 schema 或 sample。

## 1. 目标

- 完整备份可恢复；
- CSV 可直接给 Excel/Python/通用 AI；
- 任何版本都能判断数据由哪个 schema 生成；
- 不使用云服务；
- 不需要存储广域权限。

## 2. 完整 JSON 备份

文件名：

```text
SleepDiary_backup_YYYYMMDD_HHmmss.json
```

编码：UTF-8；日期/时间使用 ISO 8601。

根对象至少包含：

```json
{
  "exportFormatVersion": "BACKUP_2.1",
  "appVersion": "0.2.1",
  "exportedAtUtc": "2026-09-08T10:30:00Z",
  "diarySchemaVersion": "DIARY_1.0",
  "safetySchemaVersion": "SAFETY_1.0",
  "safetyRuleVersion": "SAFETY_RULE_1.0",
  "weeklySummaryRuleVersion": "WEEKLY_1.0",
  "cbtiProtocolVersion": "NONE",
  "workspaceSchemaVersion": "WORKSPACE_1.0",
  "workspaceRecords": [],
  "inputSnapshots": [],
  "diaryEntries": [],
  "safetyAssessments": [],
  "weeklySummaries": [],
  "trackingState": {
    "hasCompletedOnboarding": false,
    "trackingStartDate": null,
    "hasSeenMinimalInteractionExplanation": false,
    "hasSeenSharePrivacyNotice": false,
    "lastDisplayedWeeklyPeriodIndex": 0,
    "currentSafetyAssessmentId": null
  }
}
```

规范 JSON Schema：schemas/backup-v2.1.schema.json。保留 backup-v1.schema.json、backup-v1.1.schema.json 和 backup-v2.schema.json 为历史定义，禁止覆盖。

JSON 备份中的 `safetyAssessments` 使用嵌套结构：每个 assessment 内含 `answers[]`；数据库内部仍可使用两张关系表。assessment 包含 safetySchemaVersion、safetyRuleVersion、status；每条 answer 包含 safetySchemaVersion；S09=FLAGS_PRESENT 的 detailJson 在 JSON 中为 selectedCodes 对象，NONE / UNSURE 必须为 null；保存/导出按 CLINICAL_BOUNDARIES 的固定枚举顺序规范化 selectedCodes。S10 detailJson 在 JSON 中为对象，在数据库中序列化为 JSON string。所有需完整恢复的状态在 Room AppState 中，备份仍使用 trackingState 对象。`weeklySummaries` 直接导出 snapshot 字段和 `sourceEntryIds[]`。

## 3. CSV 导出

文件名：

```text
SleepDiary_entries_YYYYMMDD_HHmmss.csv
```

- UTF-8；
- 为兼容常见中文 Excel，写 UTF-8 BOM；
- RFC 4180 风格引用；
- 一行一个 `SleepDiaryEntry`；
- 不导出安全问询；
- 不导出周总结；
- 不导出未来治疗数据。

列顺序固定：

```text
id
sleep_date
bed_entered_at_local
try_sleep_at_local
sleep_onset_latency_min
awakening_count
wake_after_sleep_onset_min
final_awakening_at_local
out_of_bed_at_local
sleep_quality_1_to_5
comment
time_zone_id_at_entry
created_at_utc
updated_at_utc
diary_schema_version
consistency_warning
```

V0.1 **不导出 TST/SE 列**。

## 4. 完整备份恢复（RESTORE）

V0.1 的 JSON 导入只提供替换式完整恢复，不提供 merge、duplicate 跳过、同 UUID 与本地内容比较或非空库合并。CSV 只供阅读/分析，不用于恢复。

1. 用 Storage Access Framework 选择并读取 JSON。
2. 完整 JSON Schema 校验：语法、格式版本、必填字段、类型、枚举、UUID、日期时间。
3. 完整语义校验：字段可解析性与技术范围、答案条件与版本、关系引用、AppState、周期起点与周报修订关系；备份内标识/关系必须能完整恢复。日记的严格时间顺序、sleepDate 对应离床日和最长 24 小时跨度用于当前新建/编辑校验，不得成为拒绝无损备份既有可解析历史行的理由。完整备份保留并恢复原始时间字符串，不自动规范化；恢复后的无效行仍排除在当前统计之外，只有用户明确更正后才会进入统计。diaryEntries 有 sleepDate 早于 trackingStartDate 时失败。
4. 修改本地数据前显示恢复摘要。
5. 本机已有数据时明确提示「恢复备份将替换本机现有 SleepDiary 数据。」
6. 提供「先导出当前数据」「替换并恢复」「取消」；只有确认恢复才可修改。
7. 在一个 Room transaction 中删除需恢复的表内容，写入备份记录、全部安全问询（含 DRAFT/COMPLETED 及答案）、全部周报 revisions、全部独立个人记录、全部历史输入快照和 AppState。
8. 整个 transaction 成功才提交；提交前错误/崩溃回滚，原数据保持不变。
9. 提交后按 UUID/状态回读验证；提交后崩溃再启动应读到完整新数据，不得出现混合状态。

trackingStartDate 必须恢复备份值，不得重新推断。所有 backup-critical 状态位于 Room，禁止借助 DataStore 分次提交。恢复是明确确认的数据替换，不是破坏式数据库升级。


## 5. 导出位置

使用 Storage Access Framework：
- 用户明确选择文件位置；
- App 不默认写入公共 Download 后台目录；
- 不申请 `MANAGE_EXTERNAL_STORAGE`；
- 不申请传统外部存储权限。

导出是明文。

首次进入导出页面可显示：
> 导出的文件包含你的睡眠记录，是明文文件。请只保存到你信任的位置。

## 6. 分享图片

### 生成
- 从**数据库已保存记录**生成，不从未保存的 Compose state 生成；
- PNG；
- 临时文件放 app cache；
- 文件名：`SleepDiary_YYYY-MM-DD.png`

### 分享
- 使用 `FileProvider` 生成 `content://` URI；
- `ACTION_SEND`；
- MIME `image/png`；
- `FLAG_GRANT_READ_URI_PERMISSION`；
- Android Sharesheet。

### 退出
Sharesheet 成功交给系统后：
- 结束本 App task；
- 不调用进程强杀 API；
- 目标 App 仍可使用临时 URI 权限完成读取。

### 临时文件清理
- 下次 App 启动时清理过期分享 PNG；
- 只清理本 App 专用 share cache 目录；
- 不删除用户主动导出的文件。

## 7. 分享隐私

分享是用户主动行为。

Public UX 2.0 日记分享图（2026-10-27 用户批准增量）：
- 今日及保存成功页主动分享直接包含数据库已保存的 comment；
- 无额外预览确认和包含开关；comment 为空或空白时显示“无记录”；
- 历史最近 7/14 天 PNG 从数据库重新读取原始记录并计算中性统计，注明日期范围与有效天数；窗口内非空 comment 按日期逐条原文列出，为空略过。历史结构无效行不进入平均值，其日期内原文备注仍保留；
- 不包含安全问询；
- 不包含设备 ID；
- 不包含用户姓名/手机号；
- 不包含任何诊断。

## 8. 备份版本兼容

新导出仅 BACKUP_2.1，必须包含 inputSnapshots；1.0/1.1/2.0 的快照集合恢复为空。必须包含 workspaceSchemaVersion / workspaceRecords，字段定义见 docs/WORKSPACE_DATA_INCREMENT.md。BACKUP_1.0 / 1.1 的个人记录集合按空集合恢复，确认摘要必须告知个人记录数量和旧格式将清空该集合。导入按 exportFormatVersion 选择对应 schema；BACKUP_1.0 完整使用历史定义，hasCompletedOnboarding 恢复为 false，其余语义不变；BACKUP_1.1 必须包含 Boolean hasCompletedOnboarding 并原样恢复。不支持的版本或损坏的任一格式均不修改本地数据。Room AppState 的 backupFormatVersion 表示当前存储格式 BACKUP_2.1，不保留旧格式作为当前技术状态。

## 9. 独立个人记录隐私边界

完整 JSON 包含全部已保存个人记录；CSV 不增列、不导出该集合；日记 PNG 按第 7 节直接包含已保存 comment（空白为“无记录”），7/14 天统计 PNG 按日期列出非空 comment。PNG 始终排除 Safety 和独立个人记录。就诊摘要不会自动加入个人记录。全部七表恢复在一个 Room transaction 中替换并全量回读，错误回滚；用户选择文件位置的 SAF 流程保持不变。

## 10. 原始输入快照

inputSnapshots 使用 RAW_INPUT_CAPTURE_1，含规范 Base64 原始输入与 SHA-256。导入须同时校验外层 schema、编码版本、哈希、二进制结构、范围、必填类型、集合唯一性及排序，不能仅验证哈希。历史快照不使用当前规则重新作医学结论。完整恢复替换全部集合，预览显示快照数量；旧格式缺少的集合清空需明确告知。其他出口不加入快照。见 docs/INPUT_SNAPSHOT_STORAGE_INCREMENT.md。

## SPEC-0.2.2 本机恢复边界

BACKUP_2.1 对外结构不变。业务历史仍完整恢复；本机 local_execution_state 不是可携带的历史数据或授权，不导出、不导入。成功恢复同时生成新代次和 REQUIRES_REVIEW，失败一并回滚，提交后回读。旧 token 即使对应相同备份内容也失效；预览/导出/取消不改变代次。见 docs/LOCAL_EXECUTION_INCREMENT.md。

## P2A current contract and P2B target (2026-10-03)

Current format is `BACKUP_3.1`; its schema/example are the current `BACKUP_3.1` definitions. Current Room is 7. `BACKUP_1.0`, `1.1`, `2.0`, `2.1`, and `3.0` remain immutable historical contracts and import-compatible.

`BACKUP_3.1` contains `educationDataSchemaVersion = EDUCATION_DATA_1.0`, `educationContentVersions[]` (packaged registry descriptors only), `educationProgress[]`, `educationCommittedChoices[]`, `educationReviewStates[]`, `educationRecords[]` (one-to-one links by WorkspaceRecord ID), and `educationPositions[]`. `EducationContentVersion` is not copied as course content and is not a Room table. Preview is never exported or persisted. Legacy `BACKUP_1.0–3.0` restore Education collections empty and never infer associations from WorkspaceRecords.

Before any database mutation, import validates each exact Education `contentVersionId` and manifest hash against packaged compatible registries. An unknown version ID produces a clear “需要兼容/更新版本的 App” error; a known ID with a different hash produces a clear Education content-version incompatibility error. Neither is mislabeled as ordinary data corruption, and no title/near-ID guessing is permitted. Other malformed or dangling references fail normal semantic validation. A rejected file leaves the current database unchanged.

`EDUCATION_EXPORT_1.0` is a separate, non-restorable user-data projection. JSON contains `exportFormatVersion`, `educationDataSchemaVersion`, exact packaged version descriptors, `progress[]`, `committedChoices[]`, `reviewStates[]`, and Education records joined to their WorkspaceRecord title/body/revision. TXT is a deterministic readable projection of the same included state. Both omit course bodies/explanations/source excerpts, previews, EducationPosition, Diary, Safety, Treatment, InputSnapshot, LocalExecutionState and telemetry. Export uses user-selected SAF destination; it has no import path in P2B.

Full BACKUP_3.1 replacement restore remains prevalidated, user-confirmed, one Room transaction, rollback on any write failure and complete post-commit read-back. AppState backup marker becomes 3.1. CBT-I Protocol remains `NONE`.
