# SleepDiary Public Edition v1.0 — Architecture

状态：P0 FROZEN
当前基线：单 `:app`、单 Activity、Compose、Room 6、BACKUP_3.0
目标：Public Core，不启用 Treatment

## 1. 架构原则

Public Edition 使用五个严格边界：

```text
Diary Boundary
Education Boundary
Personal Data Boundary
Backup Boundary
Future Treatment Boundary (disabled)
```

各边界可以共享稳定基础设施，例如 Room、时间源和 SAF，但不能通过共享类绕过数据语义和权限边界。

## 2. 当前模块与目标包结构

保持单 Gradle `:app`，避免仅为目录美观进行高风险模块拆分。目标包结构：

```text
app.sleepdiary.offline
├── application
├── data
│   ├── backup
│   ├── dao
│   ├── db
│   ├── entity
│   └── repository
├── domain
│   ├── education
│   ├── model
│   ├── safety
│   ├── treatment       # dormant future foundation
│   ├── validation
│   └── weekly
├── share
└── ui
    ├── diary
    ├── education
    ├── me
    ├── theme
    └── publicnav
```

现有 `ui.learning` 在实现阶段迁移为 `ui.education`。现有 `WorkspaceRepository` 继续负责通用用户文字；Education 通过独立关联数据增加语义，不复制正文。

## 3. Diary boundary

Diary boundary 拥有：

- SleepDiaryEntry；
- DiaryValidator；
- 保存事务和逐字段回读；
- 历史查看和编辑；
- WeeklySummarySnapshot/WEEKLY_1.0；
- 最近 7/14 天中性实时统计。

Diary boundary 不得：

- 输出 TST、SE 或治疗窗口；
- 读取 EducationProgress 后改变日记或统计；
- 写入 TreatmentEpisode、TreatmentAuditEvent 或计划；
- 将 comment 解析为医疗事件或学习状态。

原始日记仍是 source of truth，派生统计不覆盖原始字段。

## 4. Education boundary

Education boundary 由四部分组成：

1. **Content Registry**：随 App 发布的不可变内容、版本、来源、hash 和稳定 ID。
2. **Progress Store**：只保存用户明确提交的学习进度。
3. **Record Link Store**：把 WorkspaceRecord 关联到具体内容，不复制正文。
4. **Review/Bookmark Store**：保存用户主动选择的稍后复习状态。
5. **Resume Position Store**：每课程/内容版本仅保存一个稳定场景指针；不保存滚动或浏览轨迹。

依赖方向：

```text
Education UI
  → EducationService
  → EducationRepository / EducationContentRegistry
  → Education Room entities or packaged assets
```

禁止依赖方向：

```text
Education → TreatmentStateController
Education → TreatmentPlan
Education → protocol calculation
Education → diary-derived personalization
```

Education 可以解释 CBT-I 概念，但必须使用一般性表述、来源标识和适用范围说明。它不能输出针对用户的时间、剂量、计划、适用性结论或医疗决定。

## 5. Personal data boundary

个人数据分为：

- Diary 原始记录；
- 通用 WorkspaceRecord；
- EducationProgress；
- EducationRecord 关联；
- EducationReviewState；
- 现有 Safety 与历史 Treatment foundation 数据。

规则：

- 用户文字不自动分析、分类或升级成医疗事实；
- EducationRecord 的正文继续由 WorkspaceRecord 保存；
- 旧 WorkspaceRecord 不根据标题或 kind 自动关联 Education 内容；
- 未保存草稿只存在 ViewModel/SavedState，不进入完整备份；
- 页面打开、点击、滚动、停留时间不持久化；
- 不创建学习评分、连续天数或参与度画像。

## 6. Backup boundary

当前 BACKUP_3.0 是严格、不可变的历史格式。Public Edition 新增持久 Education 集合时目标格式为 `BACKUP_3.1`。

BACKUP_3.1：

- 保留 3.0 全部字段；
- 增加 Education schema/version descriptors；
- 增加 progress、record links 和 review state；
- 保留已有 WorkspaceRecord、InputSnapshot 和休眠 Treatment 历史；
- 不包含 LocalExecutionState；
- 不把 restore 当作 Treatment 授权。

导入兼容：

- 继续接受 BACKUP_1.0、1.1、2.0、2.1、3.0；
- 旧格式恢复 Education 集合为空；
- 不从 WorkspaceRecord 推断 Education 关系；
- 所有业务数据与 AppState 在一个 Room transaction 中替换；
- 失败回滚，提交后全字段回读；
- 恢复成功后重新生成本机 restoreGeneration 并保持 `REQUIRES_REVIEW`。

独立 Education export 是只读导出投影，不是完整恢复文件，不能传给 Full Backup restore。

## 7. Public navigation

使用一个 Public App Shell 和四个顶级目的地：

```text
Today | Sleep | Education | Me
```

### Today graph

- 今日状态；
- 新建/修改记录；
- 保存结果；
- 前往 Sleep。

### Sleep graph

- 历史；
- 日记详情/编辑；
- 中性周总结；
- 最近 7/14 天中性统计。

### Education graph

- 目录；
- 内容详情；
- progress；
- bookmarks/review；
- 关联个人记录；
- 来源详情。

### Me graph

- 通用个人记录；
- 导入/导出；
- 就诊摘要；
- 隐私、方法、术语和版本。

顶级页面保留各自 back stack。Onboarding、表单、恢复确认和系统文件选择器不是第五个顶级入口。表单或确认流程中可以隐藏底部导航，防止误切换造成草稿丢失。

## 8. Future Treatment capability boundary

当前 Treatment foundation 保留但完全休眠：

- Room 中保留 treatment_episode 和 treatment_audit_event；
- 备份继续无损保存这些历史数据；
- `CBT-I Protocol = NONE`；
- Public navigation 不注册 Treatment route；
- Public UI 不提供 startBaseline、plan 或 review 操作；
- Public Education 不引用 Treatment 类型；
- 恢复历史 episode 不产生本机执行许可。

Public Edition 的生产 `AppServices` 不应向 UI 暴露 TreatmentStateController。未来如启动 Treatment 产品，应建立单独能力装配和新的已批准合同，而不是从 Education 页面调用现有控制器。

## 9. Treatment foundation compatibility

现有 foundation 不要求删除：

- 删除表会制造无必要的数据丢失和降级风险；
- 当前没有导航或生产调用者；
- 医学转换已经失败关闭；
- 旧数据可以继续 round-trip。

P1.5 已移除 Public AppServices 对 Treatment controller 的暴露，并以反射回归测试保护该边界；当前 Public navigation 无 Treatment route。P2B 继续增加 Education 写入前后 Treatment episode/audit 不变的集成测试。完整备份仍显示休眠 Treatment 数据数量，避免隐式替换。

## 10. Data and migration target

P0 不修改数据库。下一实现阶段的冻结目标为：

```text
Room 6 → Room 7
BACKUP_3.0 → BACKUP_3.1
EDUCATION_DATA_1.0
CBT-I Protocol = NONE
```

P2A 已冻结的 Room 7 目标新增五张 Education 用户状态表：

- education_progress；
- education_committed_choice；
- education_review_state；
- education_record；
- education_position。

EducationContentVersion 保存在不可变内容 registry/assets 中，不需要复制整份内容到 Room。EducationExport 是运行时投影，不是表。

迁移 6→7 只新增上述空表并保留现有十表；只将 AppState 的技术 backup format 标记更新到 BACKUP_3.1。不得推断旧学习进度、书签、内容关联或治疗状态。精确字段、键、状态机、删除、导出与兼容语义见 `EDUCATION_USER_DATA_CONTRACT.md`。

## 11. Enforcement tests

Public Edition 必须具备以下架构断言：

- Navigation graph 只有四个 Public 顶级区域；
- Production UI 没有 Treatment route 或 action；
- Education 包不导入 treatment/protocol 包；
- Education 保存前后 Treatment 表内容相同；
- 阅读内容不写数据库；
- progress/bookmark 只由明确操作写入；
- CBT-I Protocol 始终为 NONE；
- 所有导出均无后台上传；
- Manifest 保持无三项禁止权限。

## 12. Existing specification reconciliation

当前代码使用 Room 6/BACKUP_3.0。P2A 将相关规格从旧 BACKUP_2.1 当前摘要同步到 3.0，并冻结 P2B 的 3.1 合同；历史 backup schema/sample 不改写。同步范围：

- VERSIONING_AND_TRACEABILITY；
- IMPORT_EXPORT_SPEC；
- DATA_SCHEMA 的当前版本摘要；
- TEST_PLAN；
- SELF_CHECK 和 SPEC_MANIFEST。

P2B 增加严格的 `backup-v3.1.schema.json` 并保留 `backup-v3.schema.json` 原字节不变。

历史 schema、样例和历史说明不得覆盖或改写。

## Public UX 2.0 Education presentation

The packaged V4 registry remains immutable and is the source for content, stable IDs, and sources. A small curated Experience layer composes selected existing IDs into the default native user flow. It controls presentation only; it neither duplicates the registry corpus nor changes content classification. The complete Knowledge Map remains an optional route. `EducationRepository` still owns explicit progress, review, authored reflection, choice commit, and resume-position persistence. Preview is transient and never reaches Room until explicit confirmation. The Experience layer does not call Treatment, Diary, or Safety repositories.
