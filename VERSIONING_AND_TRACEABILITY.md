# VERSIONING_AND_TRACEABILITY.md — 版本与可追溯性

## 1. 为什么单独版本化

App 版本、数据字段、计算规则、医学安全规则、未来治疗协议不是一回事。

因此不得用“App 0.2”含糊表示所有规则都更新了。

## 2. 版本对象

### 规格基线
当前：`SPEC-0.2.0`。规格文档版本独立于 App、数据库及领域版本；仅交互设计修订不改变已有数据/规则版本。

### App
`0.2.0`

### Diary schema
`DIARY_1.0`

变更条件：
- 新增/删除/改变每日核心字段含义；
- 改变时间语义；
- 改变 sleepQuality 锚点。

### Safety schema
`SAFETY_1.0`

变更条件：
- 强制问询题目变更；
- answer code 变更；
- 题目语义变更。

### Safety rule
`SAFETY_RULE_1.0`

变更条件：
- 同样的原始答案映射到不同 gate；
- 新增/改变分流逻辑。

### Weekly summary rule
`WEEKLY_1.0`

变更条件：
- 周期划分改变；
- 平均算法改变；
- 纳入/排除规则改变；
- 展示派生指标定义改变。

### Backup format
`BACKUP_2.0`

变更条件：
- JSON 根结构或恢复语义改变。

### CBT-I Protocol
V0.1：`NONE`

任何治疗功能进入前必须变成一个明确、可查证版本，例如未来的 `CBTI_PROTOCOL_1.0`。正式协议筛选后再命名。

## 3. “方法与依据”页面必须显示

```text
App version
Diary schema version
Safety schema version
Safety rule version
Weekly summary rule version
Backup format version
CBT-I protocol version
Evidence review date
```

## 4. 源头记录

每一个规则文档至少记录：
- source_id
- 来源标题
- 发布机构/作者
- 发布年份
- URL/DOI
- 它在本软件中负责哪一层
- 本软件是逐字采用、概念改写，还是只用于交叉验证

## 5. 不能做的事

禁止：
- 指南更新后直接改代码、不改 rule version；
- 修改字段含义但继续叫 `DIARY_1.0`；
- 改周总结算法但旧快照跟着静默变化；
- 用新算法重算旧 snapshot 并覆盖旧值；
- 把以后确定的 CBT-I Protocol 倒填成“V0.1 当时就用了”。

## 6. 周总结修订

原始条目被用户修改后，按 DATA_SCHEMA 第 9 节决定受影响周期：同周期仅修订已有 summary；跨周期修订已有 A 和已结束且已有 summary 的 B，未结束 B 不提前生成。起点在任何 snapshot 生成后冻结；恢复使用备份起点。对于需修订的周期：
- 保留旧 summary；
- 新 summary `revision + 1`；
- `supersedesSummaryId` 指向旧版；
- UI 默认只显示 `isCurrent=true`；
- 导出完整备份包含所有 revisions。

## 7. Room schema

Room schema JSON 是技术 schema 历史，必须纳入 Git。

它与 `DIARY_1.0` 等领域版本不同：
- Room version 是数据库实现；
- Diary schema version 是医学/数据语义。

两个都要保留。

## 8. 证据更新

V0.1 `evidence_review_date = 2026-09-08`

以后若新指南发布：
1. 先更新 `EVIDENCE.md`；
2. 判断是否影响现有规则；
3. 若影响，升级对应 rule/schema；
4. 写 migration + test；
5. 不直接修改历史数据。

## 9. SPEC-0.1.0 开发前修正记录

此前用户批准的 SPEC-0.1.0 开发前规格修正：修复日期正则、补齐安全编码与 DRAFT、版本字段，采用 Room AppState 和 RESTORE，明确周期起点与修订。
保留 App 0.1.0、DIARY_1.0、SAFETY_1.0、SAFETY_RULE_1.0、WEEKLY_1.0、BACKUP_1.0；CBT-I Protocol 仍为 NONE。不是已发布数据的迁移，也不是医学协议升级。
SELF_CHECK 只报告实际执行的规格检查；存在待确认项不得标为无条件 PASS。SPEC_MANIFEST 对除自身外的规格与示例逐一记录字节数及 SHA-256。

2026-09-09：用户批准补齐 S09 的 NONE / UNSURE / FLAGS_PRESENT 及 selectedCodes 固定编码、顺序和测试，写入本地开发优先规则。仍属 SPEC-0.1.0 开发前修正，全部领域版本保持不变，CBT-I Protocol 仍为 NONE。

## 10. SPEC-0.1.1 UX 修订记录

2026-09-09：用户批准在 UI 编码前修订交互设计，规格从 SPEC-0.1.0 升为 SPEC-0.1.1。核心原则为“默认定位，不默认作答”；时间 anchor、picker position 仅为 UI 状态。明确确认后才更新答案，新增快捷输入、精细日期调整、滚动与无障碍要求及对应 UI 测试计划。
App 仍为 0.1.0；DIARY_1.0、SAFETY_1.0、SAFETY_RULE_1.0、WEEKLY_1.0、BACKUP_1.0 不变；CBT-I Protocol = NONE。未改变医学字段、数据语义、安全分流、周总结算法或治疗规则，不需要数据迁移。
SELF_CHECK 只声明本轮实际执行的规格与完整性检查，不沿用历史测试数量作为本轮结果。SPEC_MANIFEST 更新版本，并重新计算除自身外全部规格与示例的字节数、SHA-256。历史修订记录和未改动 JSON Schema 的历史注释可保留 SPEC-0.1.0，不能当作当前基线版本。

## 11. SPEC-0.1.2 正式产品修订

用户批准渐进披露、日期定位修复、完整 V0.1 功能接入，并独立批准 AppState.hasCompletedOnboarding。App 0.1.0、DIARY_1.0、SAFETY_1.0、SAFETY_RULE_1.0、WEEKLY_1.0 和 CBT-I Protocol NONE 不变；备份结构新增字段故升级 BACKUP_1.1。Room 技术版本 1→2，无损迁移。历史 BACKUP_1.0 schema/示例不变，导入缺少该字段映射 false；新 BACKUP_1.1 必填并完整往返。不是医学规则或治疗协议升级。

## 12. SPEC-0.2.0 非治疗数据增量

2026-09-13：在用户批准继续完整版工程及所需变更后，加入独立 WORKSPACE_1.0 用户个人记录。App 0.2.0 / versionCode 2、Room 3、BACKUP_2.0；原 Diary/Safety/Safety Rule/Weekly 版本及运行时 CBT-I Protocol NONE 不变。Room 2→3 无损增加表；旧 Room 1→2 迁移保留历史 BACKUP_1.1，再继续迁移。历史 1.0/1.1 JSON schema 和示例不改写；旧格式恢复新集合为空，完整替换前告知。合同见 docs/WORKSPACE_DATA_INCREMENT.md，上一基线清单归档 docs/baselines/SPEC-0.1.2.manifest.json。
