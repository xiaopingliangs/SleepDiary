# EVIDENCE.md — V0.1 依据与反向查证

证据复核日期：**2026-09-08**

本文件只记录 V0.1 真正使用到的依据。V0.1 不因为某篇资料还讨论了其他治疗，就自动纳入那些内容。

---

## E01 — Consensus Sleep Diary（CSD）原始共识论文

**标题**  
The consensus sleep diary: standardizing prospective sleep self-monitoring

**作者**  
Carney CE, Buysse DJ, Ancoli-Israel S, Edinger JD, Krystal AD, Lichstein KL, Morin CM

**期刊/年份**  
Sleep. 2012;35(2):287–302

**DOI**  
10.5665/sleep.1642

**PubMed**  
https://pubmed.ncbi.nlm.nih.gov/22294820/

**PMC**  
https://pmc.ncbi.nlm.nih.gov/articles/PMC3250369/

**在 V0.1 中负责**
- “每日主观睡眠日记”这一测量层；
- Core 日记所覆盖的核心睡眠构念；
- 睡眠日记是前瞻性主观自我监测工具。

**本软件采用方式**
- 采用构念，不复制整份英文表格；
- 中文问法由本项目重新表述；
- 不能把本项目中文 UI 宣称为“官方授权中文版 CSD”。

---

## E02 — CSD Core 指令/表格（作者实验室公开 PDF）

**标题**  
Sleep Diary Instructions - Core / Consensus Sleep Diary-Core

**来源**  
https://drcolleencarney.com/wp-content/uploads/2016/01/Final-CSD-Core-and-instructions.pdf

**关键依据**
原说明明确：
- 建议每天填写；
- 如可能，起床后一小时内填写；
- 漏记一天应留空；
- 特殊事件可以简短备注；
- 不需要追求准确时间；
- 不应为了填写日记盯钟，只需给出最佳估计；
- 区分进入床/睡觉位置、开始尝试入睡、最终醒来、真正离床等概念。

**在 V0.1 中负责**
- 顶部“主观估计即可、不要为记录看钟”的长期提示；
- 8 个 Core 构念；
- 可选备注；
- 漏记不补签的产品方向。

**版权/许可注意**
CSD 有版权。公开信息表明其非营利用途可免费使用，但公开分发翻译版、商业用途或原表复制的具体许可仍应在正式公开发布前核查。

因此 V0.1：
- 不复刻原表布局；
- 不逐字翻译全部说明；
- 使用独立中文表述；
- 保留来源归属。

CSD 项目主页：  
https://consensussleepdiary.com/

---

## E03 — CSD 心理测量学研究

**标题**  
Psychometric Properties of the Consensus Sleep Diary in Those With Insomnia Disorder

**作者**  
Maich KHG, Lachowski AM, Carney CE

**期刊/年份**  
Behavioral Sleep Medicine. 2018;16(2):117–134（online 2016）

**DOI**  
10.1080/15402002.2016.1173556

**PubMed**  
https://pubmed.ncbi.nlm.nih.gov/27231885/

**在 V0.1 中负责**
- 支持 CSD 作为失眠人群主观睡眠追踪工具的可用性；
- 说明 SOL、WASO、TIB、TST、SE 等可以从睡眠日记形成派生指标。

**重要限制**
该研究中的 TST/SE 计算方式并不自动成为未来 CBT-I 治疗算法。

V0.1 因完整 CBT-I Protocol 尚未选定，故：
- 可以计算不具争议的 TIB 与 terminal wakefulness；
- **不展示/不使用治疗用 TST/SE**。

---

## E04 — Sleep Efficiency 定义存在方法学差异

**标题**  
Measuring Sleep Efficiency: What Should the Denominator Be?

**作者**  
Reed DL, Sacco WP

**期刊/年份**  
Journal of Clinical Sleep Medicine. 2016;12(2):263–266

**全文**  
https://pmc.ncbi.nlm.nih.gov/articles/PMC4751425/

**在 V0.1 中负责**
- 提醒项目不要在尚未选择完整 CBT-I Protocol 前把 SE 定义写死；
- 支持“原始数据长期保留，派生算法可版本化重算”的架构。

---

## E05 — VA/DoD 2025 慢性失眠/OSA 临床实践指南

**标题**  
VA/DoD Clinical Practice Guideline for the Management of Chronic Insomnia Disorder and Obstructive Sleep Apnea

**版本**  
2025

**官方主页**  
https://www.healthquality.va.gov/guidelines/CD/insomnia/

**官方 PDF**  
https://www.healthquality.va.gov/guidelines/CD/insomnia/I-OSA-CPG_2025-Guideline_final_20250915.pdf

**在 V0.1 中负责**
首次安全问询的主要临床来源。

指南明确列出 CBT-I/BBT-I 可能需要 adaptation/delay 的情况，包括：
- medically unstable；
- active alcohol/drug use disorder；
- excessive daytime sleepiness；
- nighttime fall risk / inability to transfer；
- exposure-based PTSD treatment；
- uncontrolled seizure disorder；
- bipolar disorder；
- current acute mental health symptoms；
- pregnancy/postpartum insomnia。

**使用边界**
- 本 App 只把这些内容转成“未来治疗前需确认”的分流信息；
- 不把指南变成自助诊断工具；
- 指南本身也声明不能替代临床判断。

---

## E06 — 中国《失眠症数字疗法的中国专家共识（2024版）》

**标题**  
失眠症数字疗法的中国专家共识（2024版）

**来源**  
中华医学杂志 2024 年，第104卷第9期

**公开检索**  
https://www.sinomed.ac.cn/article.do?ui=2024308355

**PDF**  
https://health.net.cn/zb_users/upload/2024/12/202412071733548425635470.pdf

**在 V0.1 中负责**
- 中国本地数字失眠评估场景的交叉核查；
- 支持数字化评估关注睡眠模式、诱发因素、认知、日间功能、驾驶困倦和昼夜节律；
- 支持睡眠日记作为主观评估工具；
- 提醒怀疑 OSA、RLS 等其他睡眠障碍时可能需要进一步专业评估；
- 支持数字疗法应先评估、再干预、再持续评估疗效/依从性/不良反应。

**不纳入 V0.1 的内容**
该共识还列出完整 dCBT-I 的睡眠限制、刺激控制、教育、放松、认知治疗等实现细节。V0.1 明确不执行这些治疗规则。

**传统/民族医学**
即使其他中国指南存在传统/民族医学章节，V0.1 也不引用这些内容作为 CBT-I 依据。

---

## E07 — 中国统一心理援助热线

**发布机构**  
中华人民共和国国家卫生健康委员会

**通知**  
国家卫生健康委关于应用“12356”全国统一心理援助热线电话号码的通知

**官方链接**  
https://www.nhc.gov.cn/yzygj/c100068/202412/49a1a65386cd4be582d4702fd0926ee8.shtml

**在 V0.1 中负责**
仅用于首次安全问询中出现“当前明确自伤/自杀计划、准备行为或无法保证安全”时的中国大陆现实世界求助提示。

---

# Android 技术依据

## A01 — Room migration / schema history
https://developer.android.com/training/data-storage/room/migrating-db-versions

用途：数据库迁移必须测试；Room schema 应导出并纳入版本控制。

## A02 — 状态持久化的技术选择

本轮批准把全部完整备份所需状态保存在 Room AppState，随数据在单一事务中替换恢复；V0.1 无其他实际需要时移除 DataStore 依赖。依据 Room 技术约束见 A01。此项是工程决策，不是新增医学依据。

## A03 — 正常结束任务
https://developer.android.com/reference/android/app/Activity#finishAndRemoveTask()

用途：实现“记录完即消失”的产品目标；不需要、也不允许强杀 Android 进程。

## A04 — Android Sharesheet
https://developer.android.com/develop/ui/compose/sharing

用途：用户主动把当天记录 PNG 分享给其他 App。

## A05 — FileProvider
https://developer.android.com/reference/androidx/core/content/FileProvider

用途：通过 `content://` URI 安全分享临时图片；不使用 `file://`。

---

# 证据层与产品层的区别

以下内容是**本项目产品设计决策**，不是宣称由某一份指南逐条规定：

- 保存后快速退出；
- 从最近任务中移除；
- 不显示单夜评分；
- 不做连续打卡奖励；
- 第 7 天才集中反馈；
- S03 提交即持久化 DRAFT 并允许直接记录日记的交互；
- 用户确认的完整替换恢复、Room AppState 与固定周期起点；
- 分享图不包含备注；
- V0.1 暂不展示 TST/SE。

这些设计用于降低日常交互负担、避免过早锁定治疗算法，并减少对单夜数据的过度强调。
