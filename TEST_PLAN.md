# TEST_PLAN.md — V0.1 测试计划

## 1. 测试等级

- **P0**：发布 debug APK 给用户前必须全部通过。
- **P1**：建议在 V0.1 初次真机使用前通过。
- **P2**：可以在后续小版本补充。

Codex 不得为了通过构建删除 P0 测试。

## 2. P0 — 数据保存

### T-DATA-001 正常保存
输入一条完整日记 → 保存 → DAO 按 UUID 回读。

要求：所有原始字段完全一致；createdAt/updatedAt 正确；schema version 正确。

### T-DATA-002 重复点击
快速双击“保存”。

要求：只能创建/更新一条记录；不出现同一 sleepDate 的意外重复。

### T-DATA-003 保存失败
人为让数据库写入失败。

要求：不显示“保存成功”；不退出；不允许分享；用户输入仍可恢复/重试。

### T-DATA-004 进程杀死后
保存成功 → 强行停止 App → 重开。要求数据仍在。

### T-DATA-005 手机重启
保存成功 → 重启设备 → 重开。要求数据仍在。

## 3. P0 — 时间和估计数据

### T-TIME-001 跨午夜
23:00 入床 → 23:30 尝试睡 → 次日 06:00 最后醒 → 07:00 离床。

要求：日期推断正确；TIB = 480 min；terminal wakefulness = 60 min。

### T-TIME-002 白天/倒班
08:00 入床 → 08:20 尝试睡 → 14:00 最后醒 → 14:30 离床。

要求：用户通过“日期不对？调整”可正确保存；不强迫识别为夜间。

### T-TIME-003 逻辑顺序错误
outOfBed 早于 finalAwakening。

要求：不能直接保存；引导用户调整日期/时间；不自动偷偷改值。

### T-TIME-004 估计内部不一致
SOL + WASO > trySleep→finalAwakening。

要求：出现中性提示；用户可返回检查；用户可选择“仍按估计保存”；保存后 `consistencyWarning=true`。

## 4. P0 — 周总结

### T-WEEK-001 完整 7 天
有 7 条记录。

要求：periodStart/End 正确；n=7；各平均值与手算一致；不出现 TST/SE；snapshot 保存 sourceEntryIds。

### T-WEEK-002 缺 1 天
7 天内只有 6 条。

要求：显示 6/7；缺失日不当作 0；平均只对 6 条求值；不出现负面评价。

### T-WEEK-003 修改历史
生成周报 → 修改其中一天。

要求：旧 summary 保留；新 summary revision+1；新 summary isCurrent=true；supersedes 指向旧 summary。

### T-WEEK-004 第7天漏记
周期结束日无记录，下次保存发生在第8天或以后。

要求：能生成此前已结束周期 summary；不把第8天数据塞进第1周。

## 5. P0 — 安全问询

### T-SAFE-001 原始答案保存
要求：每题 questionId/answerCode 正确；safety schema/rule version 保存。

### T-SAFE-002 双相
S05=YES。

要求：derived gate 至少 CLINICIAN_REVIEW；不显示诊断式结论；日记仍可用。

### T-SAFE-003 未控制癫痫
S07=YES，S07b=UNSTABLE。

要求：DELAY_AUTOMATED_TREATMENT；日记仍可用。

### T-SAFE-004 自伤/自杀急性信号
S03=YES / UNSURE（分别测试）。

要求：立即出现中国大陆安全提示；包含 120/110/12356；不要求先完成其余所有问题才看到帮助；日记可继续使用；不输出自杀风险概率/等级。

## 6. P0 — 导出/导入

### T-EXPORT-001 JSON backup
导出后使用 JSON Schema 验证。要求通过、版本信息齐全、UUID 保留。

### T-EXPORT-002 CSV
要求：UTF-8 BOM；中文备注不乱码；逗号/双引号/换行正确 escaping；不含 safety；不含 TST/SE。

### T-IMPORT-001 空库恢复
导出 → 清空 App 数据 → 导入。

要求：diary/safety/summary/tracking state 全部恢复；UUID 不变化；round-trip 关键字段完全一致。

### T-IMPORT-002 非空库替换恢复
本机与备份均有数据（包含同 UUID 不同内容）。
要求：先显示摘要、替换警告及三项操作；取消或仅导出不改变数据。确认后完整替换，不合并、不跳过重复、不生成冲突副本；备份外本地数据不残留。

### T-IMPORT-003 恢复回滚
在单个 Room transaction 删除旧行后、写入记录途中、写入 AppState 途中分别注入错误/终止进程。
要求：未提交则原始业务表和 AppState 全部保持；无部分新旧混合；提交后再重启则完整新数据可回读。

### T-IMPORT-004 损坏备份
非法 JSON、缺字段、错误 enum/UUID/版本、日期顺序或引用不合法。
要求：格式/语义校验失败均不改变数据库；给出可理解错误。

### T-IMPORT-005 AppState round-trip
空库和非空库分别完整备份恢复，再导出比较业务字段（忽略新的 exportedAtUtc）。
要求：全部 Entry UUID、DRAFT/COMPLETED、原始答案和版本、所有周报 revisions、trackingState 每项均一致；确认前无写入，提交后按 UUID/状态回读。

### T-IMPORT-006 起点恢复
备份起点与本机不同；另测备份条目早于备份起点。
要求：合法备份恢复其原 trackingStartDate，不重推；非法备份在事务前拒绝，本机不变。

### T-IMPORT-007 Room-only
检查恢复事务覆盖全部业务表和 AppState，故障时一起回滚；backup-critical 状态不得写 DataStore。

## 7. P0 — 分享与退出

### T-SHARE-001 分享来源
保存 → 分享。

要求：PNG 内容来自回读后的持久化记录；不含备注；不含安全问询；不含评分/建议。

### T-SHARE-002 URI
要求：使用 content://；临时读权限；不使用 file://。

### T-SHARE-003 hand-off
Sharesheet 成功显示后。

要求：本 App task 从最近任务消失；目标 App 仍能读取图片；不调用 killProcess/System.exit。

### T-EXIT-001 普通退出
保存 → 退出。

要求：task 移除；重开直接进入下一次日记/已完成状态；数据仍在。

## 8. P0 — 完全离线

### T-PRIV-001 Manifest
检查 merged manifest。

要求：无 INTERNET；无 ACCESS_NETWORK_STATE；无 MANAGE_EXTERNAL_STORAGE。

### T-PRIV-002 依赖
要求：无广告/分析/远程 crash SDK；无 Firebase 等云服务。

### T-PRIV-003 飞行模式
飞行模式下日记、周总结、设置、JSON/CSV 导出均正常。

## 9. P1 — Android 生命周期/开发者选项

### T-LIFE-001 旋转/配置变化
填写一半发生配置变化。

要求：表单尽可能保留；不产生半条数据库记录。

### T-LIFE-002 “不保留活动”
开发者选项开启 Don't keep activities。

要求：已保存数据无影响；未保存表单即使被销毁，也不能产生损坏记录。

### T-LIFE-003 后台进程限制
将后台进程限制调低，切换多个 App。

要求：已保存记录稳定；重新打开流程正确。

### T-LIFE-004 强制停止
设置 → 应用 → 强制停止。

要求：重启 App 后数据库完整。

## 10. P1 — UI

- 字体放大后无关键按钮不可见；
- TalkBack 能读出字段；
- 所有按钮 >=48dp；
- 深色模式可读；
- 不依赖红/绿说明好坏；
- 保存成功第一次解释只出现一次；
- 无连续打卡火焰/奖章/分数。

## 11. 构建通过标准

至少执行：

```bat
gradlew.bat testDebugUnitTest
gradlew.bat assembleDebug
```

真机可用后执行 instrumentation：

```bat
gradlew.bat connectedDebugAndroidTest
```

每次准备交付 APK 前保存测试结果摘要。

## 12. 本轮新增 P0 — 安全状态与编码

### T-SAFE-005 DRAFT 与即时帮助
分别提交 S03=YES、UNSURE：验证先持久化 DRAFT、已答内容和至少 DELAY_AUTOMATED_TREATMENT，再立即显示帮助，不等待其余题目。
选择退出后重开、或继续记录睡眠：日记始终可用；DRAFT/S03 信号保留；设置有中性继续入口。继续完成全部题后才变 COMPLETED。

### T-SAFE-006 明确分支
S01 仅 YES/NO；YES 无额外 gate，NO 为 CLINICIAN_REVIEW，拒绝 UNSURE。
S07=NO 无额外 gate且无 S07b；UNSURE 为 CLINICIAN_REVIEW且无 S07b；YES 才显示 S07b。
S07b=STABLE 为 CLINICIAN_REVIEW；UNSTABLE/UNSURE 为 DELAY_AUTOMATED_TREATMENT。

### T-SAFE-007 编码与版本
逐题检查 CLINICAL_BOUNDARIES 编码表；不存中文作逻辑条件。
每条 answer 有 safetySchemaVersion；assessment 有 safetySchemaVersion、safetyRuleVersion、status。
S13“不知道是否属于”属于 UNSURE；没有接受该治疗才归 NOT_APPLICABLE。
S09 使用 NONE / UNSURE / FLAGS_PRESENT 单值编码；具体选择存 detailJson.selectedCodes，按下列专门测试验证。
S10 仅保存用户选中的 diagnoses 和 otherText，不自动生成诊断代码。

## 13. 本轮新增 P0 — 周期与修订

### T-WEEK-005 起点冻结
首条有效日记成功保存时设置起点。首份 snapshot 前修改最早记录导致最早日期改变，可更新为数据库最早日期。
任意 snapshot 生成后不得自动移动起点；修改到起点以前被拒绝，显示规定提示，数据保持不变。

### T-WEEK-006 空周期
中间连续 7 天无记录：periodIndex 前进，不强制展示空周报。恢复记录后展示已结束、有记录、未展示的最早周期；不把新记录塞回旧周期。

### T-WEEK-007 跨周期修订
保持 Entry UUID/createdAtUtc，更新 updatedAtUtc。
同周期已有 summary 才 revision+1。
A→B：A 有 summary 则修订；B 已结束且有 summary 则修订；B 未结束不提前生成。
两边历史均保留，新版 isCurrent=true，supersedesSummaryId 正确。移出最后一条时 A 的新版本可为 0 条、平均值为 null。

## 14. 开发前规格校验（不是 Android 测试通过声明）

- 用 Draft 2020-12 校验器验证 backup-v1.schema.json 及随附 JSON 示例。
- 四个 local datetime 正则能匹配示例，拒绝带时区偏移的事件时间；未改变本地时间语义。
- 校验 safety 枚举、status、每题版本、AppState 必填字段及 S10 对象。
- CSV 保持原有列顺序、UTF-8 BOM；不添加恢复能力或 TST/SE 列。
- 重新生成 SELF_CHECK 与 SPEC_MANIFEST；逐项核对字节数、SHA-256。
- S09 的编码、null/对象分支、枚举、非空、唯一性及规范化顺序均须验证；规格校验不替代 Android 实现测试。

## 15. P0 — S09 存储与交互

1. S09=NONE：answerCode=NONE，detailJson=null，不增加 gate。
2. S09=UNSURE：answerCode=UNSURE，detailJson=null，CLINICIAN_REVIEW。
3. 单选具体项：FLAGS_PRESENT，selectedCodes 仅包含所选代码，CLINICIAN_REVIEW。
4. 多选具体项：FLAGS_PRESENT；保存/导出前去重，按 LOUD_SNORING、WITNESSED_APNEA、GASPING_AWAKENING、DIAGNOSED_OSA 固定顺序保存；多次导出该对象一致，CLINICIAN_REVIEW。
5. NONE + 具体项：UI 不允许共存。
6. UNSURE + 具体项：UI 不允许共存；NONE 与 UNSURE 也互斥。
7. FLAGS_PRESENT + 空 selectedCodes：Schema/语义校验失败。
8. selectedCodes 出现未知值：Schema/语义校验失败。
9. 备份 selectedCodes 含重复、NONE 或 UNSURE：校验失败；NONE/UNSURE 带非 null detailJson、缺 detailJson、FLAGS_PRESENT 带 null 均失败。
10. 单个具体代码不能直接作为 answerCode；不按提示项数量计算等级，不诊断 OSA，不显示阳性/阴性。
11. JSON 对象与 Room detailJson 字符串往返保持选择集合；保存/导出固定排序，不改变 gate。

## 16. P0 — SPEC-0.1.1 UX 默认定位与明确作答

以下为 UI 实现阶段必须执行的验收测试；本轮仅补充规格，不代表 UI 测试已运行。UNANSWERED 属于表单状态，Room 必填字段不因此变为 nullable；未完成表单不得生成不完整记录或以默认值填充数据库。

| 编号 | 场景 | 预期 |
|---|---|---|
| T-UX-001 | 未填写字段打开 picker 后直接关闭、返回或取消；另测滚动后取消 | 仍为 UNANSWERED / 未填写；不更新表单已回答值，不新建或修改 SleepDiaryEntry |
| T-UX-002 | 分别打开四个时间 picker，初始位于约 22:00、22:30、06:00、06:30–07:00 anchor | anchor 仅定位；字段未回答，数据库无该默认答案；仅浏览所有 anchor 后仍不能保存不完整记录 |
| T-UX-003 | 在 anchor 处直接明确确认，或滚动选择后确认 | 仅在确认后写入 ViewModel/form state 的已回答值；不等于已保存数据库，仍需执行正式保存事务 |
| T-UX-004 | 逐个点击 SOL/WASO quick choice；进入“其他”选择后确认或取消 | 整数写入正确字段、单位为分钟；SOL/WASO 不串值，不预选；“其他”仅打开或取消不回答，技术范围沿用 DATA_SCHEMA |
| T-UX-005 | 夜醒次数点击 0–5；“更多”选择 6、99 及越界值；取消选择器 | 仅保存明确整数；6–99 有效，越界不能确认，不保存“5+”或“更多”；取消不生成答案 |
| T-UX-006 | 新表单睡眠质量与逐个点击五个选项 | 没有默认值，不默认“一般”；很差到很好分别写入 1–5 |
| T-UX-007 | “日期不对？调整”输入白天睡眠、倒班、跨午夜；精细输入非 15 分钟倍数 | 保留完整 local datetime，支持所选日期；不要求秒，不强行取整，仍应用既有时间顺序校验 |
| T-UX-008 | 检查每日输入字段及界面文案 | 仅现有 8 个核心构念和可选备注，不新增 TST、SE、总睡眠时长或“睡了几个小时”字段 |
| T-UX-009 | 系统字体放大并启用 TalkBack 操作全部输入、确认、取消和日期调整 | 字段名、单位、答案/未填写状态可识别；选项可访问且可操作，不遮挡关键操作 |
| T-UX-010 | 检查时间入口、quick choices、质量选项、“更多”及主要操作 | 每个主要触摸目标至少 48dp |
| T-UX-011 | 确认一个字段后导航；纵向滚动表单及横向 quick choices | 可轻微滚动到下个未填写字段，不自动弹出下一 picker；时间滚轮置于 sheet/dialog，不与主页面纵向滚动冲突 |
| T-UX-012 | 回到已回答/已有数据库记录的字段，打开并取消 picker | 保留原值；只有明确选择或确认才修改表单，不因 anchor 覆盖已保存值 |
| T-UX-013 | 检查填写完成和数据展示效果 | 无连续打卡、完成动画奖励、睡眠评分、红绿评价或“今天比昨天好/差” |

## 17. P0 — SPEC-0.1.2 完整功能

保留既有 154 个自动测试覆盖，交互预期按新批准规格同步。新增：完整安全流程、DRAFT 与 S03 立即帮助、S09 多选互斥；日期 anchor 和前值参考、取消不写入、直接 DatePicker；结构化时间冲突与定位/即时清除；历史日期选择与当天保护/继续查看；周总结展示状态；证据元数据与官方链接 Intent；JSON 两版本完整 schema/语义校验、替换确认、故障回滚、AppState round-trip；CSV BOM/转义；分享 PNG 隐私、content URI/FileProvider；所有禁止权限不存在。

### 首次说明与迁移

1. 新安装 hasCompletedOnboarding=false。
2. 完成三页立即持久化 true。
3. 重启 true 保持。
4. 无 DiaryEntry 也不重复说明。
5. SafetyAssessment 为 DRAFT 也不重复说明。
6. Room 1→2 保留全部五张表数据，新增非空列默认 false，不推断 true。
7. BACKUP_1.1 导出包含该字段。
8. BACKUP_1.1 true/false 均 round-trip 保留。
9. BACKUP_1.0 正常导入，该字段 false。
10. 两版本损坏均不修改现有数据库。
11. BACKUP_1.1 替换恢复仍为单 Room transaction，全量回读校验。

运行 testDebugUnitTest 和 assembleDebug。设备依赖测试如未执行须明确列出，不以单元测试替代真机验收。
