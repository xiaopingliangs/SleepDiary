# IMPORT_EXPORT_SPEC.md — 导入、导出与分享

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
  "exportFormatVersion": "BACKUP_1.1",
  "appVersion": "0.1.0",
  "exportedAtUtc": "2026-09-08T10:30:00Z",
  "diarySchemaVersion": "DIARY_1.0",
  "safetySchemaVersion": "SAFETY_1.0",
  "safetyRuleVersion": "SAFETY_RULE_1.0",
  "weeklySummaryRuleVersion": "WEEKLY_1.0",
  "cbtiProtocolVersion": "NONE",
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

规范 JSON Schema：`schemas/backup-v1.1.schema.json`。保留 `schemas/backup-v1.schema.json` 为 BACKUP_1.0 历史定义，禁止覆盖。

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
3. 完整语义校验：时间顺序/数值、答案条件与版本、关系引用、AppState、周期起点与周报修订关系；备份内标识/关系必须能完整恢复。diaryEntries 有 sleepDate 早于 trackingStartDate 时失败。
4. 修改本地数据前显示恢复摘要。
5. 本机已有数据时明确提示「恢复备份将替换本机现有 SleepDiary 数据。」
6. 提供「先导出当前数据」「替换并恢复」「取消」；只有确认恢复才可修改。
7. 在一个 Room transaction 中删除需恢复的表内容，写入备份记录、全部安全问询（含 DRAFT/COMPLETED 及答案）、全部周报 revisions 和 AppState。
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

V0.1 分享图：
- 不包含自由备注；
- 不包含安全问询；
- 不包含设备 ID；
- 不包含用户姓名/手机号；
- 不包含任何诊断。

## 8. 备份版本兼容

新导出仅 BACKUP_1.1。导入按 exportFormatVersion 选择对应 schema；BACKUP_1.0 完整使用历史定义，hasCompletedOnboarding 恢复为 false，其余语义不变；BACKUP_1.1 必须包含 Boolean hasCompletedOnboarding 并原样恢复。不支持的版本或损坏的任一格式均不修改本地数据。Room AppState 的 backupFormatVersion 表示当前存储格式 BACKUP_1.1，不保留旧格式作为当前技术状态。
