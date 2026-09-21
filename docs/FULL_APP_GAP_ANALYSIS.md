# Full App Gap Analysis

日期：2026-09-21。基点 f6a815f（代码 a122460）。依据 FULL_APP_IMPLEMENTATION_BRIEF.md；当前 App/SPEC 0.2.2、Room 5、BACKUP_2.1、CBT-I Protocol NONE。本文是实施差距，不是临床审核或完成证明。

## 当前 → 目标 → 复用 / 修改 / 新建 / 阻塞

| 模块 | 当前 0.2.2 | 完整目标及处理 | 状态与阻塞 |
|---|---|---|---|
| 日记/历史 | 八构念、comment、事务回读、历史编辑 | 原样复用 DiaryRepository/Validator；治疗失败不否认已保存日记 | 既有自动化通过；最终设备回归待做 |
| 中性周报 | WEEKLY_1.0 七日历天/revision | 保持；治疗指标及 review 另建，不增加假想月报 | 复用，不拿它作为治疗输入快照 |
| 原始输入 | RAW_INPUT_CAPTURE_1、InputSnapshotStore、Decoder | 复用不可变字节、哈希、幂等捕获；新增治疗上下文须独立明确版本 | 已核代码；现捕获不含新治疗事实 |
| 本机执行检查 | LocalExecutionState + PersistentActivationCheck | 复用恢复代次；真正提交事务内重检；新执行授权不能来自备份 | 现只有 REQUIRES_REVIEW，无成功激活分支 |
| 成人/方式 | 无治疗入口 | 自愿入组、成年人、SELF_GUIDED/CLINICIAN_ASSISTED；DIARY_ONLY 永久可选 | 新建；不推断旧 Safety=成人准入 |
| 疗程状态 | UI route，非治疗状态 | 单控制器、17 个持久状态、独立 mode/约束/可执行性、审计 | 新建，Phase 1 |
| 疗程/计划 | 无 episode、plan 表 | 版本固定、不可变 plan revision、current pointer、幂等原子提交 | 新建；候选不是处方 |
| 治疗指标 | 没有正式 TST/SE | 独立 TreatmentMetricSnapshot 引用 InputSnapshot，不覆盖 WEEKLY | MEDICAL_RULE_BLOCKED：定义/有效样本/时区/舍入未完整冻结 |
| 基线 | 原始日记可用 | 五类结果；足够资料与安全检查；至少一周不是自动等于七条有效记录 | MEDICAL_RULE_BLOCKED：有效天数/缺日/不一致处理 |
| 持续 Safety | SAFETY_RULE_1.0 三 gate | 原版保留；独立新检查、五种 disposition、事件触发及 hold/resume | 具体映射/解除条件未冻结，不从旧 gate 直接升级 |
| Exception | ConstraintIntersection 抽象交集 | 来源锁定 registry、事件、约束合并、真实安全与生活适配区别 | 基础复用；逐规则适用性/恢复/safety nap 待核 |
| 初始 TIB | WindowRuleExtraction 仅测试用 | 复用经核规则片段，补正式输入/来源/预览/确认/原子计划 | 初始 min 300 分钟已指定；其余缺项不得猜 |
| 周调整 | SE+SNQ 候选提取，未运行 | 正式 review，至少一治疗周，Safety/依从/情境优先，新 revision | SNQ_RIGHTS_BLOCKED；不自制替代题干 |
| 刺激控制 | 部分教育说明 | 实际活动计划、按需清醒帮助，先走 Safety/Exception | 无计时器；具体适配合同待来源核对 |
| 认知 | 短文+Workspace 原文 | 五步练习、明确最终提交、教育专用 focus profile | V4 源文件缺失；不分析自由文本来授权治疗 |
| 放松 | 一条可选教育短文 | 可选来源支持脚本、停止/替代路径 | 精确脚本/适配待核，不接独立 meditation 项目 |
| V4 知识 | 六条 Kotlin 短文 | V4 源→生成 JSON/稳定 ID/来源→原生 Compose；共享内容源 | SOURCE_NOT_AVAILABLE；六条短文不能冒充集成 |
| 学习记录 | WORKSPACE_1.0 用户笔记 | 复用自由笔记；结构化最终练习/进度/复习另表、Room/备份 | 新建；不记录点击轨迹/停留时长 |
| 就诊摘要 | 日记/Safety 可选、预览锁定 TXT | 扩展用户逐类选择的疗程/计划/事件/问题 | 复用 SAF 和预览；默认不外传敏感字段 |
| 医生意见 | 原文笔记不授权 | USER_ENTERED_CLINICIAN_INSTRUCTION→结构化→Safety→预览→确认 | 新 brief 取消虚构身份核验；不能凌驾 Safety |
| 维持/复发 | 短文 | MaintenancePlan、明确完成、RELAPSE_REVIEW、新 episode 保留旧史 | 进入/完成标准未冻结；不得自动复用旧窗口 |
| AI 桥 | 无 | 通用知识包+逐字段 opt-in 上下文、来源验证 | 新建；无输入回写/联网/控制权 |
| Research | 无 | 保留未来隔离接口，本轮不实现穿戴 | 不进入治疗依赖图 |
| 导航 | 日记/设置/局部学习 | TODAY/DIARY/TREATMENT/LEARN/ME，状态驱动 | 分阶段接入，不破坏原返回/草稿 |
| Backup/迁移 | 四代导入，七业务表替换，Room 1→5 | 新 BACKUP_3.x；每新增业务表同步覆盖；本机代次不导出 | 需新增 schema/样例/版本分派/引用校验/全事务 |
| 隐私/发布 | 无网络权限、PNG 白名单、debug APK | 保持，完整 E2E+S24 验收后才可称完成 | 当前无新设备验收；旧 PASS 不替代新测试 |

## 已核对的技术风险

1. MIGRATION_3_4 引用当前 Versions.BACKUP_FORMAT。升级导出格式时必须改为历史 BACKUP_2.1 常量，不能使旧迁移跟随新版本漂移。
2. BackupCodec 用“当前版本”等号判定快照/Workspace 是否存在；增加 3.x 时应显式保留 2.1 分派，否则会丢旧快照。
3. BackupSchemaValidator 是关键词子集，未知关键词不能当已校验；新 schema 使用已实现语法或同步实现并测试。当前无资源上限，后续必须覆盖异常文件预算。
4. ProtocolInputCapture 的注释仍说没有持久表，实际 InputSnapshotStore 已存在；以代码调用和 Room schema 为准，不重复造框架。
5. 输入快照包含 AppState 技术版本，版本迁移会使旧 proposal 不再匹配，必须重新评估，不能改写旧快照“修复”哈希。
6. SELF_CHECK 记录 362 项属于恢复代次增量；当前交付报告 369 项是后续学习增量。两个都是历史证据，不能作为新完整版本测试数。

## 医学/内容门控台账

| ID | 缺项 | 仅阻断的路径 | 解锁证据 |
|---|---|---|---|
| M-BASELINE | 有效天数、允许缺失、不一致记录策略 | 自动基线准入 | 指定来源定位及明确采用合同 |
| M-METRICS | TST/SE 分母、终末清醒/午睡、舍入、时区 | 正式治疗指标/窗口 | 逐条可测试的规则合同 |
| M-SAFETY | 每触发映射、适用范围、时效、解除 | 对应连续安全/恢复分支 | VA/DoD 主源及已采用映射 |
| M-WINDOW | 未完整的上下界/时间策略/周调整前提 | 对应自动窗口动作 | VA 2014 来源锁定，不混其他指南 |
| M-EXCEPTION | 例外/安全小睡具体规则 | 对应例外建议 | 精确来源、范围、时效和解除条件 |
| M-CONTENT | 精确脚本、适配、维持/结束条件 | 对应治疗内容或自动推进 | 来源与中文语义核对 |
| R-SNQ | 使用权未确认 | 依赖 SNQ 的自动自助周调整 | 正式许可或获批合法 provider；不重构题干 |
| S-V4 | V4 源未找到，已询问路径 | 原生 V4 内容集成 | 实际源文件及完整性/来源核对 |
| V-DEVICE | 本阶段未接机 | 最终完成宣称 | 隔离测试数据的真机 E2E |

上述缺项不阻止独立数据、状态、备份及测试工程。来源核对不等于临床签字，不填写虚构 medicalReviewDate。

## 阅读/证据范围

已读当前根规格、进度与交付报告、七份架构/路线、来源检查/窗口提取/模块内容核对、Workspace/快照/本机代次/学习增量；核对 Room 1–5 SQL/索引/外键、四代 backup schema 和当前实际捕获/解码/备份/恢复代码。PDF 逐页范围以 CBTI_MODULE_SOURCE_REVIEW_20260914.md 为准；不宣称所有引用论文和两本手册已全文临床审核。尚未找到 V4 源，不能宣称阅读完成。后续各医学分支编码前仍须补读相应主源，缺源分支保持 BLOCKED。

Phase 0 产物是可执行工程合同；没有启用治疗，没有改变冻结根规格或旧 schema。
