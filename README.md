# 睡眠日记 V0.1 — 开发规格总览

规格版本：`SPEC-0.1.0`  
目标应用版本：`0.1.0`  
工作名称：**睡眠日记 / SleepDiary**  
默认 Android applicationId：`app.sleepdiary.offline`

## 1. 这是什么

V0.1 是一个**离线、极简、可追溯的主观睡眠日记**。

它只做四件事：

1. 首次启动进行一次安全问询（S03 急性信号可保留 DRAFT 并直接记录日记），用于标记“未来若启用完整 CBT-I 治疗功能时，哪些情况需要先由医生确认或调整方案”；
2. 每天以尽量少的交互记录标准化的主观睡眠数据；
3. 每完成一个 7 天记录周期，生成一次中性的周总结；
4. 支持本地保存、完整 JSON 备份、CSV 导出、经用户确认的 JSON 完整替换恢复，以及把当天记录生成中性图片后调用 Android 系统分享。

**V0.1 不进行 CBT-I 治疗，不诊断失眠，不给睡眠窗口，不给睡眠限制建议，不调整药物。**

## 2. V0.1 的核心设计原则

- **主观估计是正确的数据来源，不追求分钟级精确。**
- 不鼓励夜间看钟、看手环或查看睡眠分期。
- 原始记录是唯一事实来源（source of truth）。
- 每条原始记录必须长期保留，任何未来算法只能从原始记录重新计算，不能覆盖原始值。
- 不给单个夜晚打分，不显示“变好/变差”评价，不做连续打卡奖励。
- 日常使用完成后尽快退出应用，降低软件和睡眠数据的存在感。
- 每 7 个日历日才集中展示一次中性趋势。
- 每一条医学/测量依据都必须能从“关于 → 方法与依据”反向查证。
- 运行时完全离线；Manifest 不申请 `INTERNET` 权限；无账号、无广告、无分析 SDK、无遥测。
- 医学规则与 UI/代码实现分离；未来治疗协议未确定前，不允许开发者或 AI 自行补出治疗逻辑。

## 3. 规格文件优先级

若文件之间出现冲突，**不得自行选择一个版本继续写代码**。停止并报告冲突。

优先级仅用于理解约束强弱：

1. `AGENTS.md`
2. `CLINICAL_BOUNDARIES.md`
3. `DATA_SCHEMA.md`
4. `PRODUCT_SPEC.md`
5. `UX_SPEC.md`
6. `IMPORT_EXPORT_SPEC.md`
7. `ARCHITECTURE.md`
8. `VERSIONING_AND_TRACEABILITY.md`
9. `TEST_PLAN.md`
10. `EVIDENCE.md`

## 4. 文件目录

```text
SleepDiary/
├── AGENTS.md
├── README.md
├── PRODUCT_SPEC.md
├── CLINICAL_BOUNDARIES.md
├── DATA_SCHEMA.md
├── EVIDENCE.md
├── UX_SPEC.md
├── IMPORT_EXPORT_SPEC.md
├── ARCHITECTURE.md
├── VERSIONING_AND_TRACEABILITY.md
├── TEST_PLAN.md
├── SPEC_MANIFEST.json
├── SELF_CHECK.json
├── schemas/
│   └── backup-v1.schema.json
└── samples/
    ├── backup-v1.example.json
    └── diary-v1.example.csv
```

## 5. 给 Codex 的开工顺序

Codex 开工前必须：

1. 阅读 `AGENTS.md`；
2. 顺序阅读其余规格；
3. 输出一份不超过 1 页的“实现计划 + 发现的规格冲突/未知项”；
4. 如果没有冲突，仍等待用户确认后再创建 Android 工程；
5. 先完成数据层与测试，再完成 UI；
6. 先让单元测试通过，再构建 APK；
7. 不得为了通过测试而删测试、放宽医学/数据规则或跳过失败分支。
8. 本轮仅修订规格、Schema、示例、测试计划、自检和哈希；完成后停止，不创建 Android 工程。

## 6. V0.1 明确不做

- CBT-I 睡眠限制或睡眠压缩；
- 自动计算治疗用睡眠效率并据此调整卧床窗口；
- 固定起床时间处方；
- 刺激控制治疗指令；
- 认知重构治疗；
- 睡眠卫生课程；
- AI 建议；
- 药物建议；
- 智能闹钟；
- Muse/手环/EEG/PSG/健康平台接入；
- REM/深睡分期；
- “睡眠评分”；
- 单夜与前一夜比较；
- 云同步；
- 账号；
- 后台联网；
- 推送提醒；
- 公开发布或医疗器械宣传。

## 7. 未来版本的硬性边界

第一次出现任何以下行为前，必须先正式选定并版本化一个完整的 CBT-I 操作协议（`protocol_version != NONE`）：

- 告诉用户几点上床或几点起床；
- 计算治疗用睡眠窗口；
- 因睡眠效率改变下一周卧床时间；
- 自动执行刺激控制；
- 生成个体化 CBT-I 治疗建议。

V0.1 必须把上述能力保持为不存在，而不是“隐藏但已实现”。
