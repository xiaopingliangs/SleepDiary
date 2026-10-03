# SleepDiary Public Edition v1.0 — Education Content Architecture

状态：P1.5 完成（V4 内容候选，不代表公开发布许可已完成）

范围：静态 Education 内容基础；无用户数据、无 Room、无导航、无 Treatment 入口

既有六项静态内容继续使用 `EDUCATION_CONTENT_SCHEMA_1.0`；新增 V4 结构候选使用 `EDUCATION_CONTENT_SCHEMA_1.1`

来源 registry：`EDUCATION_SOURCES_1.0`
既有六项版本：`EDUCATION_CONTENT_1.0`；V4 候选版本：`PUBLIC_EDUCATION_CONTENT_1.0-CANDIDATE`

## 1. 输入与边界

本次有三类输入，职责不同：

1. `EDUCATION_SPEC.md` 是 Public Edition 的冻结产品合同；
2. App 中原有 `EDUCATION_TEXT_1.0` 六项短文是当前已发布语义，也是本次可直接迁移的内容；
3. `CBTI-Learning-Game-V4.html` 是原型内容输入，不是可执行规格，也不整体等同于 Public Edition 课程包。

V4 原型文件核对信息：

- 文件：`D:\DeepSeek project\CBTI game\dist\CBTI-Learning-Game-V4.html`
- SHA-256：`7028c44b2f4fa4863625a4332dee67ba9667c31ab101182e949e3752b45d0af5`
- 结构：开场加 10 个章节；同时包含静态教育、互动选择、学习记录、复习状态、睡眠限制流程及阈值说明。

原型中的脚本没有被执行。Public registry 不复制浏览器状态、用户输入、本地存储、解锁顺序或治疗算法。

## 2. Registry 结构

```text
EducationRegistry
├── EducationContentManifest
│   ├── schemaVersion
│   ├── sourceRegistryVersion
│   └── contentVersions[]
├── EducationSource[]
└── EducationContentItem[]
    ├── contentBlocks[]
    └── sourceRefs[]
```

实现位于：

```text
app.sleepdiary.offline.domain.education
├── EducationContentModels
├── EducationContentHash
├── EducationContentRegistry
└── EducationContentValidator
```

这些类型不依赖 Room、UI、Diary、Safety、Backup 或 Treatment。Registry 只有发布内容和来源元数据，不含阅读进度、收藏、笔记、答案或任何用户状态。

## 3. Content schema

### EducationContentItem

| 字段 | 规则 |
|---|---|
| `contentId` | 全局稳定；`[A-Z][A-Z0-9-]{2,63}`；发布后不复用 |
| `contentVersionId` | 指向不可变的内容版本 |
| `moduleId` | 主题分组，不代表治疗顺序 |
| `locale` | BCP-47 风格；首版固定 `zh-CN` |
| `title` | 非空 |
| `summary` | 非空；首版从原短文原句提取，不改医学含义 |
| `contentBlocks` | 首版只允许静态 `PARAGRAPH`，不含动作或计算 |
| `boundaryNotice` | 明确内容是一般学习，不是诊断、个人建议或治疗计划 |
| `boundaryKind` | `GENERAL_EDUCATION`、`SAFETY_CONTEXT` 或 `SELF_REFLECTION` |
| `sourceRefs` | 至少一个已登记来源；每项必须有可核对 locator |
| `contentHashSha256` | 该条目规范化 UTF-8 内容的 SHA-256 |

### EducationSource

来源记录包含稳定 `sourceId`、角色、原文名称、中文项目译名、作者、发布者、年份、版次、官方链接及本项目用途。首版 manifest 只登记六项迁移内容实际引用的 `VA-CBTI-2014`，不会因为 V4 提到其他资料就把未发布内容的来源冒充成已使用来源。

来源角色保留：

- `EDUCATION_PRIMARY`
- `SAFETY_CONTEXT`
- `EVIDENCE_SUPPORT`
- `LOCALIZATION_CONTEXT`

### EducationContentVersion

内容版本包含：

```text
versionId
schemaVersion
locale
releasedAtUtc
sourceRegistryVersion
migratedFromVersion
includedContentIds
contentHashSha256
```

`EDUCATION_CONTENT_1.0` 从 `EDUCATION_TEXT_1.0` 迁移。发布后的版本对象不可原地改写；文字、来源、locator、边界或条目顺序变化都会改变 hash，并须创建新的内容发布版本。

## 4. 稳定 ID 与版本规则

- 标题、顺序和本地化名称不能充当 ID；
- 删除的 ID 永久保留，不分配给其他主题；
- 非语义修订仍产生新 content version 和 hash；
- 含义、受众或安全边界变化时，除升级版本外还要判断是否需要新 contentId；
- Education 内容版本与 App、Room、Backup、Diary、Safety、Weekly 和 CBT-I Protocol 分开；
- 内容版本不能激活 Treatment，也不能写入 TreatmentState 或 TreatmentPlan。

## 5. Hash 规范

条目和版本使用固定字段顺序生成规范文本。每个字段编码为：

```text
fieldName=<UTF-8 byte length>:<exact value>\n
```

列表按 manifest 顺序编码；来源按 `sourceId` 排序。条目 hash 不包含自身 hash 字段；版本 hash 包含版本元数据、来源完整元数据以及每个条目的 ID 和条目 hash。

首版版本 hash：

```text
b988e5d87f6a3ffc0c6963b6b6fc4350dc92012aa13c7b6cc64ddecc1eaa514c
```

测试固定该值，使未升级版本却改写内容的行为立即失败。

## 6. 首版迁移清单

六项正文保持原文，没有改写医学含义。新增的 summary 只取自各自原文中的完整句子；统一 boundary notice 是产品边界声明。

| contentId | moduleId | 来源 locator | boundaryKind |
|---|---|---|---|
| `COG-01` | `SLEEP_COGNITIONS` | PDF p.61, p.62, p.63 | GENERAL_EDUCATION |
| `COG-SAFETY-01` | `SLEEP_SAFETY` | PDF p.63 | SAFETY_CONTEXT |
| `ARO-01` | `PRE_SLEEP_AROUSAL` | PDF p.51, p.58 | GENERAL_EDUCATION |
| `REL-01` | `RELAXATION` | PDF p.52 | GENERAL_EDUCATION |
| `COG-03` | `PERSONAL_REFLECTION` | PDF p.65 | SELF_REFLECTION |
| `MAINT-01` | `MAINTENANCE` | PDF p.68, p.112 | SELF_REFLECTION |

原有 `LearningContent` 现在只作为兼容投影读取 registry，页面布局、导航和持久化行为没有变化。

## 7. V4 prototype boundary review — P1 historical snapshot

> 本节记录 P1 初始审查状态。完整分类和候选 manifest 已由文末的 P1.5 复核覆盖；请以 `V4_PUBLIC_CONTENT_MATRIX.md` 为当前逐项清单。

V4 章节清单已完成结构级审查，但没有整体导入：

| 原型部分 | 结果 | 原因 |
|---|---|---|
| 开场 | 待逐条迁移 | 多数是产品边界和一般教育，但仍需按条目建立 sourceRef |
| ch01 昨晚没睡好，今天怎么办 | 不进入首版 manifest | 混合固定起床、补觉等行为反馈，需拆分教育与执行指令 |
| ch02 为什么越想睡越睡不着 | 候选 Education | 睡眠机制内容可候选，但互动反馈需逐条核源 |
| ch03 床和睡眠的关系 | 不进入首版 manifest | 包含刺激控制做法和例外条件，属于治疗相邻内容 |
| ch04 睡眠限制 | 明确排除 | 包含周期、阈值、卧床时间和专业适用判断；Public Edition 不执行治疗 |
| ch05 睡眠念头 | 待拆分 | 混合一般认知教育、互动练习与个人记录 |
| ch06 手机/游戏/视频 | 待独立审查 | 原型明确包含“产品整理”判断框架，不可冒充指南结论 |
| ch07 疲劳、嗜睡与安全 | 待安全复核 | 含现实安全处置语义，不能只按普通教育文本处理 |
| ch08 什么时候找医生 | 待临床边界复核 | 含就医信号及医疗语境，不能静默迁移 |
| ch09 学习总结 | 本阶段排除 | 主要涉及用户记录、复习和导出；P1 禁止用户持久化 |
| ch10 放松与睡眠卫生 | 待逐条迁移 | 混合一般教育、研究支持内容及具体生活判断 |

发现的四类边界风险：

1. 个体化表达：根据用户情况选择“最适合”的内容；
2. 治疗执行：睡眠窗口、睡眠效率阈值、分钟级调整和周度执行；
3. 诊断暗示：把选择或症状解释成疾病结论；
4. 效果承诺：暗示照做必然改善或治愈。

Validator 对上述四类表达提供自动防线。自动检查只能发现明显越界，不替代逐条来源、临床边界、语言和许可审查。

## 8. Validation contract

自动测试覆盖：

- content/source/version ID 唯一；
- 必填字段和 locale/schema/version 格式；
- sourceRef 必须指向已登记来源且 locator 非空；
- 条目 hash 和版本 hash 与规范化内容一致；
- manifest 的条目 ID 和 registry 顺序一致；
- 个体建议、治疗指令、诊断暗示和效果承诺的负向样例被拒绝；
- registry 模型不含 progress、answer 或 TreatmentPlan 字段；
- 原有六项 UI 投影仍由相同 ID、正文和页码构成。

## 9. 本阶段未改变的内容

- 未创建或修改 Room 表、DAO、migration 或 exported schema；
- 未修改 Backup format；
- 未增加 EducationProgress、EducationRecord 或 EducationReviewState 持久化；
- 未改变页面、导航或入口；
- 未创建 Treatment plan、metric 或 recommendation；
- CBT-I Protocol 继续为 `NONE`。

## 10. 下一阶段建议

P1.5 已形成静态内容候选和 P2 状态边界。下一阶段可以实现 Education 用户状态持久化，但不得顺带做最终导航或 Treatment。P2 数据类型见文末合同；Room migration 与 BACKUP_3.1 仍按 `EDUCATION_SPEC.md` 的版本要求实现。

## 11. P1.5 V4 内容候选与冻结边界

### 来源与版本

- 输入文件：`D:\DeepSeek project\CBTI game\dist\CBTI-Learning-Game-V4.html`
- 输入 SHA-256：`7028c44b2f4fa4863625a4332dee67ba9667c31ab101182e949e3752b45d0af5`
- 静态盘点：11 章（开场 + ch01–ch10）、106 场景、22 选择、59 个原型知识条目、5 组地图 / 20 分支、106 条证据声明、8 个来源；加入 56 个可展开说明、12 个五步练习、1 个反思、1 个安全提示等子单元后，共 562 个 inventory unit。
- 分类：`PUBLIC_AS_IS` 146；`PUBLIC_WITH_PRESENTATION_ADAPTATION` 178；`PUBLIC_CONCEPT_ONLY` 144；`FUTURE_TREATMENT_ONLY` 40；`BLOCKED` 54。
- 候选内容节点 462。6 个可公开的 `USER_STATE_TYPE` 只留在 inventory，不进入可显示的 `contentNodes`；被阻塞的 telemetry 定义同样不进入内容。
- Schema：`EDUCATION_CONTENT_SCHEMA_1.1`；候选 content version：`PUBLIC_EDUCATION_CONTENT_1.0-CANDIDATE`；locale `zh-CN`。
- Manifest：`app/src/main/resources/education/public_education_manifest_1_1.json`；语义内容 hash `7cdd0009ac4b8953c6af51ccae52398a5ce7b191fb50181988695c478b4b5c7a`；文件 SHA-256 `0d9d19992ea080a4b024cb7a910bf1972b043988166343a1a19732997afad443`。
- 全量分类、稳定 ID、claim/source 关系与每项理由见根目录 `V4_PUBLIC_CONTENT_MATRIX.md`。manifest 是候选数据；本阶段未接 UI、Room、Backup 或导航。

### 结构模型

```text
Course → Knowledge Map → Chapter → Scene
                                  ├─ ContentBlock / layered explanation
                                  ├─ Choice (transient Preview → explicit Commit contract)
                                  ├─ SIM / Analyzer / Checklist child units
                                  ├─ CognitiveExercise (五步结构)
                                  ├─ Reflection / NotePrompt
                                  ├─ ReviewItem / Bookmark concept
                                  └─ Source / claim references
```

`EducationStructuredModels.kt` 只定义静态 Education 结构与未来记录合同，不依赖 Room 或 Treatment。`CHOICE` 内容的 Preview 标记 `TRANSIENT_ONLY`，Commit 要求 `EXPLICIT_USER_CONFIRMATION` 并指向稳定 commitValueId；点击、浏览或看到反馈不会在 P1 写入用户状态。所有原型的 `adjustment` 处置段不进入公开选择预览；有反馈调整段的选项归为 `PUBLIC_CONCEPT_ONLY`，矩阵保留原始 V4 单元、分类原因与原始来源定位。

### 来源和版权复核

截至 2026-10-03，只确认 bibliographic metadata、稳定链接及 V4 内嵌来源关系；这不等于获得内容复用许可，也不等于逐句重新核实全部临床主张。

- AASM 2021：Springer/JCSM 页面提供临床建议摘要，同时提供 reprints/permissions 入口；候选 registry 只收录文献元数据与项目自己的表述，不复制论文段落。公开分发前仍需确认使用范围。
- 中国 2023 指南页面显示全文受账号/机构访问限制，并提供向 CCC 申请许可入口；本候选不复制全文或长段原文。任何继续改写使用须保留单独许可审查。
- VA/DoD 2025 官方页面将指南定位为供临床专业人员辅助判断的工具，并说明不可替代临床判断。它可作书目来源，但不授权 App 将内容转换成个人治疗决定。
- VA CBT-I Therapist Manual 的 V4 元数据明确标出 Sleep Need Questionnaire 来自经许可转载的未发表工作。问卷题目、计分和使用功能均为 `BLOCKED`；手册来源仍标记 rights review，不据网页托管推断全本可自由复用。
- 四篇 PubMed 系统评价仅以引文元数据 / 摘要链接作出处；PubMed 可访问不表示文章全文许可。候选未复制摘要、表格、图像或量表题目。
- V4 自身的再分发授权和所有改编文字的来源链没有随 HTML 提供可验证的许可证，因此本 manifest 是内部候选，公开上架前仍需完成 V4 与来源许可审查。

来源页面：AASM/JCSM `https://doi.org/10.5664/jcsm.8986`；中国指南 `https://rs.yiigle.com/cmaid/1512022`；VA/DoD 2025 `https://www.healthquality.va.gov/guidelines/CD/insomnia/`；VA 手册 `https://www.mirecc.va.gov/visn19/treatmentworksforvets/docs/CBT-I-Therapist-Manual.pdf`。

### 仍排除 / 需审查的内容

完整原因逐项见矩阵。重点包括：

- 40 个治疗执行或依赖治疗状态的单元，包括初始卧床窗口、周度规则、直接刺激控制步骤、可执行的起床/午睡规则与调整情景；只可在未来经独立 Treatment 规格批准后处理。
- 54 个 `BLOCKED` 单元，包括未经许可的 Sleep Need Questionnaire、混合式就医 / 自伤 / 呼吸 / 神经安全问卷、证据范围过宽的共病和职业措辞、绝对嗜睡文案、无来源服务保证、来源冲突或 AI 医疗提示词。
- V4 的 SOURCE/mapping 与运行时 SOURCE 序列化对 8 个来源均有元数据漂移；AI 知识包会混入治疗阈值，所有 AI prompt / pack 均不进入 Public Edition。
- 个别认知练习里的具体 `adjustment` 指令被留在原型审计与分类原因中，未复制进候选 Preview payload；候选保留假设场景和概念解释，不能据此对用户做行为建议。

### P2 所需用户状态类型

静态 content version 与以下本地用户状态是不同模型：

1. `EducationProgress`：用户明确保存的进度；唯一键 contentId + contentVersionId；
2. `EducationRecord`：结构化关联，正文仍存 `WorkspaceRecord`，包含用户明确 Commit 的学习选择及自写反思/问题；
3. `EducationReviewState`：用户明确收藏或标记稍后复习；无自动间隔调度；
4. `EducationExport`：临时、导出专用快照；不持久化为治疗状态；
5. `EducationContentVersion`：静态不可变发布描述符，不是用户行。

P2 可实现上述状态与 BACKUP_3.1/独立 Education export 契约；不因此增加 Treatment route、每日提醒、AI 或个性化建议。CBT-I Protocol 保持 `NONE`。
