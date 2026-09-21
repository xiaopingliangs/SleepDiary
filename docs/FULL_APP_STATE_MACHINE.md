# Full App State Machine Contract

版本：TREATMENT_STATE_1.0（工程合同）；2026-09-21。依据 FULL_APP_IMPLEMENTATION_BRIEF 第 7–12、28、33 节。状态枚举不是医疗严重度排序。当前运行协议仍 NONE。

## 状态与控制权

持久状态：DIARY_ONLY、BASELINE_COLLECTING、BASELINE_REVIEW、SAFETY_REVIEW、READY_FOR_TREATMENT、PLAN_PENDING_CONFIRMATION、ACTIVE_TREATMENT、WEEKLY_REVIEW_DUE、PLAN_ADJUSTMENT_PENDING、ACTIVE_ADAPTED、CLINICIAN_REVIEW、USER_PAUSED、SAFETY_HOLD、MEDICAL_HOLD、MAINTENANCE、COMPLETED、RELAPSE_REVIEW。

Mode 独立：SELF_GUIDED / CLINICIAN_ASSISTED，由用户明确选择，缺失不默认。无 episode 的旧安装投影为 DIARY_ONLY；不因有日记/onboarding/Safety 自动入组。未成年人只在治疗入口显示简短专业帮助建议，不应用成人算法；既有日记仍可用。

所有写状态的调用经 TreatmentStateController。UI 不可直接调用 DAO；读取/教育/Research/AI 无转换入口。数据库事务同时检查 expectedRevision、本机 restoreGeneration、commandId；审计追加，回读相等才返回成功。相同 command+相同内容重试返回原结果；同 command 不同内容拒绝。提交后验证失败返回“可能已提交”，不得伪报未写入或重复 revision。

## 显式转换合同

| 当前 | 事件 | 下一状态 | 必要条件 |
|---|---|---|---|
| DIARY_ONLY/无 episode | 主动开始评估 | BASELINE_COLLECTING（新 episode） | 成年明确回答；mode 明确；无另一未结束 episode；只开始资料收集，不授权治疗 |
| BASELINE_COLLECTING | 请求检查资料 | BASELINE_REVIEW | 明确用户动作；尚不能代表资料充分 |
| BASELINE_REVIEW | 资料不足 | BASELINE_COLLECTING | BaselineEngine 版本化结果 |
| BASELINE_REVIEW | 资料符合采用规则 | SAFETY_REVIEW | 有效不可变 metrics/input；缺项规则已支持 |
| SAFETY_REVIEW | 当前准入通过 | READY_FOR_TREATMENT | 新 Safety/Exception 结果、成人/协议适用性、规则可用；旧 gate 不足以放行 |
| READY_FOR_TREATMENT | 生成候选 | PLAN_PENDING_CONFIRMATION | 候选绑定全部版本/输入/安全/状态水位 |
| PLAN_PENDING_CONFIRMATION | 用户确认 | ACTIVE_TREATMENT 或 ACTIVE_ADAPTED | 仅 PlanCommitService；同事务重检及 plan/audit/state 回读 |
| ACTIVE_TREATMENT/ACTIVE_ADAPTED | review 到期 | WEEKLY_REVIEW_DUE | 至少一治疗周及协议时间规则；不是中性周报 periodIndex |
| WEEKLY_REVIEW_DUE | 生成复核候选 | PLAN_ADJUSTMENT_PENDING | 当前完整 review 输入和合法 SleepNeed provider；可明确 gated |
| PLAN_ADJUSTMENT_PENDING | 用户确认 | ACTIVE_TREATMENT 或 ACTIVE_ADAPTED | 新不可变 plan revision 原子提交，不改旧 plan |
| 未完成 episode | 用户暂停 | USER_PAUSED | 明确动作；保留计划史和所有安全约束；不能以暂停清除 hold |
| 任一未完成 episode | 保存安全决策 | SAFETY_HOLD / MEDICAL_HOLD / CLINICIAN_REVIEW / ACTIVE_ADAPTED | 精确版本规则；URGENT_HELP 是优先行动并保持不可执行，不是疾病分级 |
| USER_PAUSED | 请求恢复 | 暂停前的 BASELINE_COLLECTING / BASELINE_REVIEW，或 SAFETY_REVIEW | Phase 1 仅恢复两个基线状态；进入过治疗/安全流程后必须回到 SAFETY_REVIEW，不能恢复旧窗口 |
| SAFETY_HOLD/MEDICAL_HOLD/CLINICIAN_REVIEW | 请求恢复 | SAFETY_REVIEW | 仅提出重评，不能清空原约束或恢复旧窗口；规则缺失则维持不可执行 |
| review 完成 | 进入维持 | MAINTENANCE | 已采用的进入条件 + 明确个人维持计划；不按日期/阅读次数自动触发 |
| MAINTENANCE | 明确结束 | COMPLETED | 结束合同及用户确认；无“治愈”判断 |
| COMPLETED | 再次请求帮助 | RELAPSE_REVIEW（新 episode） | 旧 episode 不变，新评估/新权限；不用旧窗口默认值 |
| 任一候选页 | 取消 | 原有非候选状态 | 只取消候选，已保存安全事件/hold 不撤销 |

未列出的转换一律拒绝。Phase 1 只开放“开始收集、请求检查、用户暂停、请求重评”的非处方转换；其余合同先建类型，缺实现/医学规则返回 BLOCKED，不允许 UI 传 boolean 绕过。

## 约束与实际可执行性

- 独立保存全量限制集合；显示优先级 URGENT_HELP > HOLD > CLINICIAN_REVIEW > ADAPT，不用单一枚举丢掉并存限制。
- USER_PAUSED 是意愿，不覆盖 SAFETY_HOLD：存在 hold 时仍展示 hold；暂停意愿另记审计/控制字段，解除也不自动继续。
- 任何已保存新增 Safety/Exception/临床结构化变更均使旧候选失效。日记变更也须由提交事务重新捕获比对。
- “旧计划保持”指候选技术提交失败不丢旧数据；若已有新安全限制，旧计划即使保留也不可继续执行。
- restore 保留历史状态，生成新本机代次并禁止执行。重启不清除 hold，不信任备份自带授权。
- 未发布规则、未知版本、不完整关键输入、技术错误都不能被解释为 CONTINUE。

## 测试门槛

枚举覆盖；逐条允许/禁止转换；未成年人/未答拒绝；mode 独立；旧安装无自动 episode；每 command 幂等及冲突；过期 revision/恢复代次拒绝；故障回滚与提交后不确定；暂停不清除安全限制；恢复需复核；计划不可变；教育/AI 无写入口。纯逻辑与 Room 分别测试，真机重建/杀进程另外验收。
