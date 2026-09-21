# Full App Requirements Matrix

2026-09-21；Phase 0。REQ 编号对应 FULL_APP_IMPLEMENTATION_BRIEF 章节，可细化后缀。状态针对完整目标；历史369项通过只证明0.2.2切片，不将存在代码标PASS。表中“拟”是计划路径，不冒充已存在文件。所有新设备验证均待执行。

| REQ | 要求 | 实现/合同文件 | DB影响 | 医学依赖 | 测试/验收 | 真机 | 状态 |
|---|---|---|---|---|---|---|---|
| 00 | 先读/差距/不重做基础 | FULL_APP_GAP_ANALYSIS.md；WORKING_CONTEXT | 无 | V4源缺失；PDF覆盖明确 | 文档/引用核对 | 不适用 | PARTIAL |
| 01 | 成人、三路径、明确治疗开始 | 拟 TreatmentStateController；STATE_MACHINE | episode | 基线/准入 | 成人/缺答/旧安装/明确确认 | 待验 | NOT_STARTED |
| 02 | 来源职责不混阈值 | EVIDENCE；PROTOCOL_SOURCE_CHECK；模块来源核对 | 规则版本 | 逐源适用性 | rule→source完整性 | 来源页待验 | PARTIAL |
| 03 | 四平面隔离 | FULL_APP_ARCHITECTURE；AppContainer | 独立对象 | 无 | 依赖/无控制旁路 | 全链待验 | PARTIAL |
| 04 | 保留既有全部行为 | Diary/Weekly/Backup/Workspace；历史369测试 | 旧表不变 | 旧规则保持 | 每阶段完整回归 | S24复验 | PARTIAL |
| 05 | 独立不可变治疗指标 | 拟 TreatmentMetricSnapshot；DATA_CONTRACT | 新表 | M-METRICS | 快照/版本/确定性/不改周报 | 待验 | BLOCKED |
| 06 | 正式基线五结果 | 拟 BaselineEngine | 基线/metrics | M-BASELINE | 缺日/边界/不一致/不能误准入 | 待验 | BLOCKED |
| 07 | 持续Safety/五行动/优先级 | 拟 TreatmentSafetyRouter；ConstraintIntersection | safety/check | M-SAFETY | 每事件/并存约束/未知/紧急路径 | 待验 | BLOCKED |
| 08 | mode独立、转述医生身份准确 | STATE_MACHINE；拟 TreatmentMode | episode | 无身份核验宣称 | mode/state分离 | 待验 | NOT_STARTED |
| 09 | 单控制器17态 | STATE_MACHINE；拟 TreatmentStateController | episode/audit | 临床转换gated | 显式允许/拒绝/并发 | 杀进程待验 | NOT_STARTED |
| 10 | episode版本和历史 | DATA_CONTRACT；拟 TreatmentEpisode | 新表 | 无算法默认值 | 新建/暂停/旧史/重试 | 待验 | NOT_STARTED |
| 11 | 激活plan不可变revision | DATA_CONTRACT；拟 TreatmentPlan | 后续plan表 | M-WINDOW | 完整引用/不可变/唯一current | 待验 | NOT_STARTED |
| 12 | 原子plan确认提交 | InputSnapshotStore；拟 PlanCommitService | plan/state/audit | 正式规则/安全 | 每水位/故障/幂等/提交后未知 | 杀进程待验 | PARTIAL |
| 13 | VA初始TIB/固定起床/周复核 | WindowRuleExtraction；拟ProtocolEngine | plan/metrics | M-METRICS/M-WINDOW | 主源边界/至少一周/不是强制上床 | 待验 | PARTIAL |
| 14 | SNQ provider权利门控 | 拟WeeklySleepNeedInputProvider | review来源 | R-SNQ | 未许可自助调整拒绝/合法来源 | 待验 | BLOCKED |
| 15 | 实际SC/清醒帮助无计时 | 拟StimulusControlFlow | 决策引用 | M-SAFETY/M-CONTENT | Safety先行/无20min钟 | 夜间路径待验 | NOT_STARTED |
| 16 | 版本化例外registry | EXCEPTION_ROUTER_ARCHITECTURE；拟ExceptionRegistry | event/decision | M-EXCEPTION | 每规则来源/范围/未知 | 待验 | BLOCKED |
| 17 | 疲劳/困倦实际体验 | 拟SleepinessObservation | 独立事实 | 对应映射待核 | 4体验/危险优先 | 待验 | NOT_STARTED |
| 18 | 仅来源锁定安全小睡 | 拟ExceptionRegistry | decision | M-EXCEPTION | 无规则不出参数 | 待验 | BLOCKED |
| 19 | 周review完整反馈闭环 | 拟WeeklyReviewEngine | review/plan | M-WINDOW/R-SNQ | 完整输入/6输出/预览确认 | 待验 | NOT_STARTED |
| 20 | 低负担无每日评分/打卡 | UX_SPEC；拟TodayScreen | 无遥测 | 无 | 导航/无隐式每日字段 | 待验 | PARTIAL |
| 21 | 五步认知/最终提交/教育profile | 拟CognitiveExercise；Workspace | 结构化教育表 | S-V4 | 探索不落库/不影响治疗 | 待验 | BLOCKED |
| 22 | 可选放松/不要求成功 | LearningContent；拟RelaxationFlow | 教育记录 | M-CONTENT | 脚本/退出/适配 | 待验 | PARTIAL |
| 23 | V4生成assets/原生共享源 | 拟education generator/assets | 无第二数据岛 | S-V4 | 源→生成一致/稳定ID | 待验 | BLOCKED |
| 24 | 原生知识map/产品综合标识 | 拟KnowledgeMap | 无治疗授权 | S-V4 | 来源标签/学习不启动 | 待验 | BLOCKED |
| 25 | Room教育最终记录无点击追踪 | WorkspaceRepository；拟EducationRepository | 新教育表 | S-V4 | 最终提交/取消/备份/无轨迹 | 待验 | PARTIAL |
| 26 | 就诊摘要逐类选择 | ClinicianSummaryService/Screen | 新引用只读 | 无诊断 | 默认隐私/预览固定/SAF | 待验 | PARTIAL |
| 27 | 医生原文→结构化→安全→确认 | Workspace；拟ClinicalInstructionService | instruction/plan | 安全/适用规则 | 原文不授权/撤销/冲突 | 待验 | NOT_STARTED |
| 28 | 维持/结束/复发留史 | 拟MaintenanceService | plan/episode | M-CONTENT | 不能按天数/旧窗口重启 | 待验 | NOT_STARTED |
| 29 | 外部AI包/逐字段opt-in无回写 | 拟AiPromptExport | 无控制写入 | 来源内容 | 默认无个人信息/无回写 | SAF待验 | NOT_STARTED |
| 30 | AI输出污染与来源构建校验 | 拟education validation task | 无 | Source Registry | 四种坏串/未知来源构建失败 | 不适用 | NOT_STARTED |
| 31 | Research未来隔离 | FULL_APP_ARCHITECTURE | 当前无 | 另行协议 | 核心无Research依赖 | 不适用 | PARTIAL |
| 32 | 五顶级页面/状态感知 | 拟RootNavigation/TodayScreen | 只读状态 | 状态机 | back/草稿/无评分 | 无障碍待验 | NOT_STARTED |
| 33 | 3.x完整备份+四代兼容 | BackupCodec/Repository；DATA_CONTRACT | 新全部业务表 | 历史版本解释 | 往返/旧备份/恢复代次/回滚 | SAF待验 | PARTIAL |
| 34 | 完全离线/无三禁权限 | AndroidManifest/现有SAF/FileProvider | 无 | 无 | merged manifest/依赖/白名单 | 分享待验 | PARTIAL |
| 35 | 规则/plan全链可追溯 | 拟SourceRegistry/Audit | audit/snapshot | 不伪造review date | 每引用/哈希/为何/来源 | 待验 | NOT_STARTED |
| 36 | 独立版本不混App | Versions；DATA_CONTRACT | 对应版本字段 | 临床规则单独发布 | 固定版本/未知拒绝 | 版本页待验 | PARTIAL |
| 37 | 无损迁移/真实历史/active | SleepDiaryDatabase；拟Migration6Test | 5→后续 | active后阶段覆盖 | 1–5升级/全部字段/active后补 | 升级待验 | PARTIAL |
| 38 | Phase0–12逐段验证提交 | Brief/WORKING_CONTEXT | 分段迁移 | 门控独立 | 每阶段diff/tests/build/commit | 最终 | PARTIAL |
| 39 | 持久检查点 | DEVELOPMENT_PROGRESS/WORKING_CONTEXT | 无 | 无 | 文件/Git证据 | 不适用 | PARTIAL |
| 40 | 完整需求矩阵有真实证据 | 本文 | 无 | 无 | 覆盖00–48及后续子项 | 分项 | PARTIAL |
| 41 | 不用死代码/注释当完成 | GAP_ANALYSIS/每阶段报告 | 无 | 无 | 真实生产路径/全量边界 | 分项 | PARTIAL |
| 42 | 中断恢复/节约重复阅读 | WORKING_CONTEXT | 无 | 不跳过核源 | status/log/diff/准确next | 不适用 | PARTIAL |
| 43 | 医学未知只阻断相应路径 | GAP_ANALYSIS台账 | blocked理由 | M各项 | 缺规则不默认/独立继续 | 待验 | PARTIAL |
| 44 | SNQ不阻断其他模块 | GAP_ANALYSIS/roadmap | 无 | R-SNQ | gate与独立工程分离 | 待验 | PARTIAL |
| 45 | 未成年禁成人算法 | 拟TreatmentStateController | 无新医学字段 | 成人明确自报 | 未答/未成年拒绝 | 待验 | NOT_STARTED |
| 46 | PNG/CSV/备份出口分离 | DiaryShare/BackupRepository | 无 | 旧隐私保持 | comment/Safety/治疗字段不入PNG | 待验 | PARTIAL |
| 47 | 完整闭环+真机才完成 | 本文/最终工程报告 | 全部 | 列出所有blocker | 端到端/迁移/审计/真机 | 未执行 | NOT_STARTED |
| 48 | Phase0产物后自动Phase1 | GAP/ARCH/STATE/DATA/MATRIX/CONTEXT | Phase1再新增 | 不等普通批准 | 文档提交后继续 | 不适用 | PARTIAL |
