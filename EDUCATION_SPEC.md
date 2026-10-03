# SleepDiary Public Edition v1.0 — Education Specification

状态：P0 FROZEN；P1.5 内容模型补充见第 14 节
边界：Education only；不是诊断或治疗
目标数据版本：`EDUCATION_DATA_1.0`

## 1. 目标

Education 系统提供低负担、可追溯、完全离线的睡眠知识和 CBT-I 概念教育。用户可以按需阅读、明确保存进度、标记稍后复习并写下自己的记录。

Education 不根据个人日记生成内容，不判断治疗适用性，不改变 TreatmentState，不创建计划。

## 2. 内容层级

```text
EducationCatalog
└── EducationModule
    └── EducationContentItem
        ├── summary
        ├── content blocks
        ├── boundary notice
        └── source references
```

### EducationModule

用于组织主题，例如：

- 睡眠日记与主观估计；
- 睡眠基本知识；
- CBT-I 概念概览；
- 睡眠担忧与认知教育；
- 放松和睡前缓冲；
- 长期维持与何时寻求专业帮助。

模块顺序仅用于导航，不代表治疗顺序。用户可以任意进入，不要求解锁。

### EducationContentItem

每项必须包含：

```text
contentId
contentVersionId
moduleId
locale
title
summary
contentBlocks
boundaryNotice
sourceRefs
contentHashSha256
```

内容块只允许展示、列表、一般性示例和来源说明。不得嵌入可执行睡眠计划或个人建议。

## 3. Content IDs

- ID 全局稳定、区分大小写、发布后不得复用；
- 同一主题的非语义性文字修正可以保留 contentId 并升级 content version；
- 医学含义、目标或适用范围改变时创建新 contentId；
- 删除的 ID 永久保留在历史 registry，标记 retired，不分配给其他内容；
- progress、notes、bookmarks 和 export 均引用 contentId + contentVersionId。

现有候选 ID：

```text
COG-01
COG-SAFETY-01
ARO-01
REL-01
COG-03
MAINT-01
```

这些 ID 可以作为初始 registry 的稳定 ID，但现有六项内容不自动等同于完整 CBT-I 教育课程。

新 ID 使用大写 ASCII、数字和连字符，长度 3–64；不得包含用户信息或本地化标题。

## 4. Content versioning

### EducationContentVersion

```text
versionId
locale
releasedAtUtc
contentHashSha256
sourceRegistryVersion
includedContentIds
```

规则：

- 已发布内容版本不可原地改写；
- 任何内容文字、来源、边界声明或排序改变都生成新 hash；
- 影响含义或安全边界的变化升级内容版本；
- 历史版本 manifest 保留，确保旧 progress/record 可解释；
- Education version 与 App、Room、Backup、Treatment protocol 独立；
- 内容版本升级不自动清除或复制用户进度。

`EDUCATION_TEXT_1.0` 可作为现有内容迁移输入；Public Edition 正式发布前应统一命名为明确的 Education content version，并保留历史映射。

## 5. Sources

每个内容项至少包含一个 SourceRef：

```text
sourceId
role
titleOriginal
titleChinese
publisher
publicationYear
editionOrVersion
locator
officialUrl?
usage
```

`role` 至少区分：

- `EDUCATION_PRIMARY`
- `SAFETY_CONTEXT`
- `EVIDENCE_SUPPORT`
- `LOCALIZATION_CONTEXT`

规则：

- 来源存在不等于授权形成治疗算法；
- 页码、章节或表格定位必须可核对；
- 中文内容标识为项目独立表述，不冒充官方译本；
- 官方链接只能由用户主动交给系统浏览器；
- App 本身不申请 INTERNET 权限；
- 未确认许可的量表、问卷或长篇原文不得复制进 App；
- 来源或内容审查未完成时，对应内容不得进入 Public release manifest。

## 6. Education boundaries

Education 可以：

- 解释一般睡眠知识；
- 解释 CBT-I 包含哪些常见组成；
- 说明专业人员在安全和治疗计划中的作用；
- 使用不针对个人的示例；
- 鼓励用户在需要时向医生提问。

Education 不可以：

- 根据用户数据选择“最适合你的”内容；
- 给出个人睡眠窗口、时间、阈值或调整量；
- 宣称用户适合或不适合 CBT-I；
- 把完成课程解释为完成治疗；
- 从用户笔记推断病情、风险或治疗依从性；
- 写入任何 Treatment 表。

## 7. User progress

### EducationProgress

目标持久字段：

```text
contentId
contentVersionId
state
updatedAtUtc
completedAtUtc?
educationDataSchemaVersion
```

状态：

```text
IN_PROGRESS
COMPLETED
```

无记录表示 `NOT_STARTED`，不在数据库保存默认行。

写入规则：

- 打开、浏览、滚动或返回不写 progress；
- 用户明确点击“保存进度”才写 IN_PROGRESS；
- 用户明确点击“标记完成”才写 COMPLETED；
- 用户可以撤销完成，操作必须明确且保留一致时间语义；
- 不自动按阅读时长或滚动比例判断完成；
- 不产生分数、百分比奖励、连续天数或排行榜；
- progress 不触发推荐或治疗状态变化。

## 8. Notes and records

用户文字继续保存在 WorkspaceRecord。EducationRecord 只保存结构化关联：

```text
id
workspaceRecordId
contentId
contentVersionId
promptId?
recordKind
createdAtUtc
educationDataSchemaVersion
```

允许的初始 recordKind：

```text
NOTE
REFLECTION
QUESTION_FOR_CLINICIAN
PERSONAL_REMINDER
```

规则：

- 文字由用户明确保存；
- 未保存草稿不进入 Room 或 backup；
- 用户文字不被自动解释；
- `QUESTION_FOR_CLINICIAN` 只是用户准备的问题，不代表医生意见；
- 修改正文沿用 WorkspaceRecord revision 和并发校验；
- 不复制正文到 EducationRecord；
- 旧 WorkspaceRecord 保持无关联，不根据 title/body 猜测。

## 9. Bookmarks and review

UI 使用“稍后复习”或“收藏”，持久模型为 EducationReviewState：

```text
contentId
contentVersionId
reviewState
updatedAtUtc
reviewedAtUtc?
educationDataSchemaVersion
```

状态：

```text
SAVED_FOR_REVIEW
REVIEWED
```

规则：

- 必须由用户明确设置；
- 无记录表示未收藏；
- 不根据时间、日记或统计自动加入复习；
- 不产生推送或后台提醒；
- “Review”仅指复习教育内容，不是临床审核。

## 10. Education export

Education export 是用户主动创建的独立 JSON 文件，格式版本：`EDUCATION_EXPORT_1.0`。

模型包含：

```text
exportFormatVersion
educationDataSchemaVersion
exportedAtUtc
contentVersionDescriptors
progress
reviewStates
educationRecords joined with WorkspaceRecord content
```

规则：

- 默认不包含 Diary、Safety、Treatment、InputSnapshot 或 LocalExecutionState；
- 显示导出预览和记录数量；
- 由用户通过 SAF 选择保存位置；
- MIME type 使用 `application/json`，UTF-8 编码；
- 文件为明文时必须提示隐私；
- 取消或写入失败不得显示成功；
- export 不具有恢复整个 App 的权限；
- 完整恢复使用 BACKUP_3.1；
- Diary CSV 和 PNG 不加入 Education notes。

## 11. Privacy and local-only behavior

- 所有 progress、bookmark 和 note 默认只在 Room；
- 无账号、云同步、遥测或远程内容服务；
- 不记录阅读时长、点击轨迹、滚动位置或搜索历史；
- 官方链接只在用户点击时交给系统浏览器；
- 分享或导出前由用户选择明确的数据范围；
- Education 自由文本视为敏感个人数据。

## 12. Accessibility and interaction

- 主要触摸目标至少 48dp；
- 支持字体放大和 TalkBack；
- 标题、正文、来源和按钮有清晰语义顺序；
- 不使用只靠颜色表达状态；
- progress 和 bookmark 操作有明确、可撤销状态；
- 不使用连续打卡、奖励动画、羞耻或失败文案；
- 夜间不主动提醒用户打开 Education。

## 13. Required tests

- content ID/version/hash 唯一且稳定；
- 每项来源存在并具有 locator；
- 未明确操作时阅读不写数据库；
- progress 明确保存、完成、撤销和重启保持；
- bookmark 明确保存、取消和重启保持；
- note 保存、编辑、并发冲突和关联读取；
- 旧 WorkspaceRecord 不被自动关联；
- Education 操作不改变 Diary、Safety 或 Treatment；
- BACKUP_3.1 round-trip 和旧格式恢复；
- Education export 精确范围、escaping、取消和失败；
- 大字体、TalkBack、返回和配置变化；
- Manifest 继续无禁止权限。

## 14. P1.5 V4 覆盖与静态交互模型补充

本节补充静态内容表达能力，不改变 P0 用户状态、Room、导航或备份约束。

### 14.1 内容层级

静态 registry 必须能表达：

```text
Course → KnowledgeMap → Chapter → Scene
                                  ├── CONTENT_BLOCK / layered explanation
                                  ├── CHOICE: transient Preview → explicit Commit
                                  ├── SIMULATOR / ANALYZER / CHECKLIST child units
                                  ├── COGNITIVE_EXERCISE (five steps)
                                  ├── REFLECTION / NOTE_PROMPT
                                  ├── REVIEW_ITEM / BOOKMARK concept
                                  └── sourceRefs / claimRefs
```

内容节点有稳定 ID、不可变内容版本、locale、顺序、父子关系、来源与 claim 引用、payload 和逐节点 SHA-256。manifest 另有 schemaVersion、contentVersionId、sourceRegistryVersion、node 列表及规范化内容 hash。每个 locale 独立校验；翻译不隐式改变原语言版本。

### 14.2 交互内容边界

- 选择内容的 Preview 只能是临时展示；离开、返回或重新打开不得视为用户已选择。
- 只有明确 Commit 操作才能形成未来的 EducationRecord；P1.5 不实现写入。
- Commit 仅记录用户在教育场景中的明确选择，不驱动个性化推荐、处方、睡眠时程或 Treatment 状态。
- 展开通俗/专业解释、知识地图定位、场景顺序、现实例子、复习和书签概念可作为静态结构表达；只有满足 Public 内容分类和来源审查的单元才可进入候选清单。
- 五步认知结构和用户自写提示可以作为通用学习/反思界面；不得自动判断答案正确性或推断疾病。

### 14.3 P1.5 候选记录

`V4_PUBLIC_CONTENT_MATRIX.md` 是逐逻辑单元的审查记录。由 `CBTI-Learning-Game-V4.html` 静态迁移出的候选 manifest 使用 `EDUCATION_CONTENT_SCHEMA_1.1`；完整计数、分类、hash 和排除理由见该矩阵及 `EDUCATION_CONTENT_ARCHITECTURE.md` 第 11 节。

候选不等于公开发布许可。不得将未通过来源许可、临床边界和逐条内容审查的节点标为发布完成；被排除单元不得以隐藏入口或导出包形式提供。
