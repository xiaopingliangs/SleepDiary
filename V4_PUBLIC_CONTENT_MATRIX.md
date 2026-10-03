# V4 Public Content Matrix

状态：P1.5 CANDIDATE FREEZE

V4 artifact SHA-256：`7028c44b2f4fa4863625a4332dee67ba9667c31ab101182e949e3752b45d0af5`

Inventory units：562；Public manifest nodes：462

分类定义：`PUBLIC_AS_IS` 原语义可直接作为一般教育；`PUBLIC_WITH_PRESENTATION_ADAPTATION` 只调整 Web/原生呈现与交互；`PUBLIC_CONCEPT_ONLY` 只保留概念和 rationale；`FUTURE_TREATMENT_ONLY` 需要治疗状态/计算/执行；`BLOCKED` 有医学、来源、许可、安全或 provenance 阻塞。

## Counts

| Classification | Count |
|---|---:|
| PUBLIC_AS_IS | 146 |
| PUBLIC_WITH_PRESENTATION_ADAPTATION | 178 |
| PUBLIC_CONCEPT_ONLY | 144 |
| FUTURE_TREATMENT_ONLY | 40 |
| BLOCKED | 54 |

## Complete inventory

| V4 source unit | Kind | Classification | Public content ID | Claim/source refs | Reason / notes |
|---|---|---|---|---|---|
| course/v4 | COURSE | PUBLIC_WITH_PRESENTATION_ADAPTATION | COURSE-V4 |  | 保留移动端短场景结构；原 Web 路由和解锁逻辑改为原生呈现。 |
| chapter/prologue | CHAPTER | PUBLIC_WITH_PRESENTATION_ADAPTATION | CHAPTER-PROLOGUE |  | 保留章节结构，Web 解锁与路线改为原生展示语义。 |
| scene/prologue/what | SCENE | PUBLIC_AS_IS | SCENE-PROLOGUE-WHAT | M-01; AASM-2021:Recommendation 1; VADOD-2025:Recommendation 5; CN-2023:失眠的心理行为治疗 p.569 | 静态一般教育场景，原文语义可保留。 |
| scene/prologue/what/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-PROLOGUE-WHAT-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/prologue/not | SCENE | PUBLIC_AS_IS | SCENE-PROLOGUE-NOT |  | 静态一般教育场景，原文语义可保留。 |
| scene/prologue/not/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-PROLOGUE-NOT-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/prologue/adults | SCENE | PUBLIC_AS_IS | SCENE-PROLOGUE-ADULTS | M-01; SF-12; AASM-2021:Recommendation 1; VADOD-2025:Recommendation 5; CN-2023:失眠的心理行为治疗 p.569; VADOD-2025:Sidebar 3 p.31 | 静态一般教育场景，原文语义可保留。 |
| scene/prologue/adults/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-PROLOGUE-ADULTS-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/prologue/rule | SCENE | BLOCKED | — | M-08; VADOD-2025:Recommendation 5 说明 p.47 | M-08 的“必须由受训人员实施”与同一 V4 的 M-09 数字/自助交付并存，来源范围与措辞冲突，需先复核。 |
| scene/prologue/rule/detail | CONTENT_BLOCK | BLOCKED | — |  | M-08 的“必须由受训人员实施”与同一 V4 的 M-09 数字/自助交付并存，来源范围与措辞冲突，需先复核。 |
| scene/prologue/limits | SCENE | PUBLIC_AS_IS | SCENE-PROLOGUE-LIMITS | M-01; M-04; SF-12; AASM-2021:Recommendation 1; VADOD-2025:Recommendation 5; CN-2023:失眠的心理行为治疗 p.569; AASM-2021:Recommendation 1 remarks + 亚组 meta 分析; CN-2023:共病章节; VADOD-2025:Sidebar 3 p.31 | 静态一般教育场景，原文语义可保留。 |
| scene/prologue/limits/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-PROLOGUE-LIMITS-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/prologue/three_words | SCENE | PUBLIC_AS_IS | SCENE-PROLOGUE-THREE-WORDS | CI-01; SF-06; SF-07; VADOD-2025:Patient Summary / Sidebar（慢性失眠障碍的核心特征）; AASM-2021:Introduction | 静态一般教育场景，原文语义可保留。 |
| scene/prologue/three_words/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-PROLOGUE-THREE-WORDS-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/prologue/adjust | SCENE | PUBLIC_AS_IS | SCENE-PROLOGUE-ADJUST | M-09; SF-12; M-01; VADOD-2025:p.46; VADOD-2025:Sidebar 3 p.31; AASM-2021:Recommendation 1; VADOD-2025:Recommendation 5; CN-2023:失眠的心理行为治疗 p.569 | 静态一般教育场景，原文语义可保留。 |
| scene/prologue/adjust/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-PROLOGUE-ADJUST-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/prologue/opening | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-PROLOGUE-OPENING |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/prologue/opening/options/curious | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-PROLOGUE-OPENING-CURIOUS | M-02; AASM-2021:Table 3 Summary of interventions (CBT-I, Multicomponent) | 保留为假设情境中的教育选择；将原 adjustment 行为建议/例外处置留在清单中，不进入概念预览内容。 |
| scene/prologue/opening/options/struggling | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-PROLOGUE-OPENING-STRUGGLING | M-01; SF-06; AASM-2021:Recommendation 1; VADOD-2025:Recommendation 5; CN-2023:失眠的心理行为治疗 p.569; AASM-2021:Introduction | 保留为假设情境中的教育选择；将原 adjustment 行为建议/例外处置留在清单中，不进入概念预览内容。 |
| scene/prologue/opening/options/referred | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-PROLOGUE-OPENING-REFERRED | M-01; M-08; AASM-2021:Recommendation 1; VADOD-2025:Recommendation 5; CN-2023:失眠的心理行为治疗 p.569; VADOD-2025:Recommendation 5 说明 p.47 | 保留为假设情境中的教育选择；将原 adjustment 行为建议/例外处置留在清单中，不进入概念预览内容。 |
| scene/prologue/promise | SCENE | PUBLIC_AS_IS | SCENE-PROLOGUE-PROMISE |  | 静态一般教育场景，原文语义可保留。 |
| chapter/ch01 | CHAPTER | PUBLIC_WITH_PRESENTATION_ADAPTATION | CHAPTER-CH01 |  | 保留章节结构，Web 解锁与路线改为原生展示语义。 |
| scene/ch01/hook | SCENE | PUBLIC_AS_IS | SCENE-CH01-HOOK |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch01/plan | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH01-PLAN |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch01/plan/options/early | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH01-PLAN-EARLY | SR-01; SC-03; AASM-2021:Table 3 Sleep restriction therapy; AASM-2021:Table 3 Stimulus control (b) | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch01/plan/options/rest | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH01-PLAN-REST | MC-01; SC-04; SC-06; AASM-2021:Table 3 CBT-I 描述含 education about sleep regulation；睡眠限制机制（enhance sleep drive）间接支撑; AASM-2021:Table 3 Stimulus control (c); AASM-2021:Table 3 Stimulus control (e) | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch01/plan/options/fixed | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH01-PLAN-FIXED | SC-01; SC-05; AASM-2021:Recommendation 3; Table 3 Stimulus control; AASM-2021:Table 3 Stimulus control (d) | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch01/plan/options/tracking | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH01-PLAN-TRACKING | SF-09; CT-05; CN-2023:失眠的临床评估 · 主观评估 · 睡眠日记; AASM-2021:由 CT-01 的 performance anxiety 与反觉醒方法描述支撑 | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| review/ch01/plan/early | REVIEW_ITEM | PUBLIC_CONCEPT_ONLY | REVIEW-CH01-PLAN-EARLY |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| review/ch01/plan/rest | REVIEW_ITEM | PUBLIC_CONCEPT_ONLY | REVIEW-CH01-PLAN-REST |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| review/ch01/plan/tracking | REVIEW_ITEM | PUBLIC_CONCEPT_ONLY | REVIEW-CH01-PLAN-TRACKING |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch01/why_morning | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH01-WHY-MORNING | SC-05; SC-01; SF-12; AASM-2021:Table 3 Stimulus control (d); AASM-2021:Recommendation 3; Table 3 Stimulus control; VADOD-2025:Sidebar 3 p.31 | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch01/why_morning/detail | CONTENT_BLOCK | PUBLIC_CONCEPT_ONLY | CONTENT-BLOCK-CH01-WHY-MORNING-DETAIL |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch01/why_morning/cognitive-exercise | COGNITIVE_EXERCISE | PUBLIC_CONCEPT_ONLY | COGNITIVE-EXERCISE-CH01-WHY-MORNING |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch01/scale | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH01-SCALE | MC-01; SC-05; AASM-2021:Table 3 CBT-I 描述含 education about sleep regulation；睡眠限制机制（enhance sleep drive）间接支撑; AASM-2021:Table 3 Stimulus control (d) | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch01/safety | SCENE | PUBLIC_AS_IS | SCENE-CH01-SAFETY | SF-12; VADOD-2025:Sidebar 3 p.31 | 静态一般教育场景，原文语义可保留。 |
| scene/ch01/end | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH01-END |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| chapter/ch02 | CHAPTER | PUBLIC_WITH_PRESENTATION_ADAPTATION | CHAPTER-CH02 |  | 保留章节结构，Web 解锁与路线改为原生展示语义。 |
| scene/ch02/setup | SCENE | PUBLIC_AS_IS | SCENE-CH02-SETUP |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch02/three_systems | SCENE | PUBLIC_AS_IS | SCENE-CH02-THREE-SYSTEMS | MC-01; AASM-2021:Table 3 CBT-I 描述含 education about sleep regulation；睡眠限制机制（enhance sleep drive）间接支撑 | 静态一般教育场景，原文语义可保留。 |
| scene/ch02/three_systems/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH02-THREE-SYSTEMS-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch02/sim | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH02-SIM |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch02/sim/meters/pressure | SIM_METER | PUBLIC_WITH_PRESENTATION_ADAPTATION | SIM_METER-CH02-SIM-PRESSURE |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch02/sim/meters/clock | SIM_METER | PUBLIC_WITH_PRESENTATION_ADAPTATION | SIM_METER-CH02-SIM-CLOCK |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch02/sim/meters/arousal | SIM_METER | PUBLIC_WITH_PRESENTATION_ADAPTATION | SIM_METER-CH02-SIM-AROUSAL |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch02/sim/stages/mind | SIM_STAGE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SIM_STAGE-CH02-SIM-MIND |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch02/sim/outcomes/must | SIM_OUTCOME | PUBLIC_WITH_PRESENTATION_ADAPTATION | SIM_OUTCOME-CH02-SIM-MUST |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch02/sim/outcomes/clock_watch | SIM_OUTCOME | PUBLIC_WITH_PRESENTATION_ADAPTATION | SIM_OUTCOME-CH02-SIM-CLOCK-WATCH |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch02/sim/outcomes/accept | SIM_OUTCOME | PUBLIC_WITH_PRESENTATION_ADAPTATION | SIM_OUTCOME-CH02-SIM-ACCEPT |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch02/turn | SCENE | PUBLIC_AS_IS | SCENE-CH02-TURN | MC-01; AASM-2021:Table 3 CBT-I 描述含 education about sleep regulation；睡眠限制机制（enhance sleep drive）间接支撑 | 静态一般教育场景，原文语义可保留。 |
| scene/ch02/not_control | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH02-NOT-CONTROL | M-02; RL-01; CT-01; M-04; SF-12; CT-06; M-09; AASM-2021:Table 3 Summary of interventions (CBT-I, Multicomponent); AASM-2021:Table 3 Relaxation therapy; AASM-2021:Table 3 Cognitive therapy; AASM-2021:Recommendation 1 remarks + 亚组 meta 分析; CN-2023:共病章节; VADOD-2025:Sidebar 3 p.31; VADOD-2025:p.46 | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch02/not_control/detail | CONTENT_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | CONTENT-BLOCK-CH02-NOT-CONTROL-DETAIL |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch02/not_control/cognitive-exercise | COGNITIVE_EXERCISE | PUBLIC_WITH_PRESENTATION_ADAPTATION | COGNITIVE-EXERCISE-CH02-NOT-CONTROL |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch02/closing_story | SCENE | PUBLIC_AS_IS | SCENE-CH02-CLOSING-STORY |  | 静态一般教育场景，原文语义可保留。 |
| chapter/ch03 | CHAPTER | PUBLIC_WITH_PRESENTATION_ADAPTATION | CHAPTER-CH03 |  | 保留章节结构，Web 解锁与路线改为原生展示语义。 |
| scene/ch03/setup | SCENE | PUBLIC_AS_IS | SCENE-CH03-SETUP |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch03/first_choice | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH03-FIRST-CHOICE |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch03/first_choice/options/stay | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH03-FIRST-CHOICE-STAY | SC-01; SC-03; SC-10; AASM-2021:Recommendation 3; Table 3 Stimulus control; AASM-2021:Table 3 Stimulus control (b); VADOD-2025:由 Rec 23 说明「对成分进行修改可降低潜在伤害」与 adapt 原则推导；属执行弹性表述，非新增疗法 | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch03/first_choice/options/leave | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH03-FIRST-CHOICE-LEAVE | SC-01; SC-03; SC-12; SC-08; AASM-2021:Recommendation 3; Table 3 Stimulus control; AASM-2021:Table 3 Stimulus control (b); CN-2023:表 4 p.570; AASM-2021:Recommendation 3 段落; VADOD-2025:Sidebar 3 p.31; CN-2023:特殊人群 · 老年人群 | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch03/first_choice/options/phone | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH03-FIRST-CHOICE-PHONE | SC-04; SC-01; AASM-2021:Table 3 Stimulus control (c); AASM-2021:Recommendation 3; Table 3 Stimulus control | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| review/ch03/first_choice/stay | REVIEW_ITEM | PUBLIC_CONCEPT_ONLY | REVIEW-CH03-FIRST-CHOICE-STAY |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| review/ch03/first_choice/phone | REVIEW_ITEM | PUBLIC_CONCEPT_ONLY | REVIEW-CH03-FIRST-CHOICE-PHONE |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch03/loop_intro | SCENE | PUBLIC_AS_IS | SCENE-CH03-LOOP-INTRO | SC-01; SC-03; SC-11; AASM-2021:Recommendation 3; Table 3 Stimulus control; AASM-2021:Table 3 Stimulus control (b); VADOD-2025:患者沟通话术 p.132（2019 p.41 同）; VADOD-2025:Sidebar 3 p.30 | 静态一般教育场景，原文语义可保留。 |
| scene/ch03/loop | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH03-LOOP |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch03/loop/meters/awake_in_bed | SIM_METER | PUBLIC_WITH_PRESENTATION_ADAPTATION | SIM_METER-CH03-LOOP-AWAKE-IN-BED |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch03/loop/meters/bed_link | SIM_METER | PUBLIC_WITH_PRESENTATION_ADAPTATION | SIM_METER-CH03-LOOP-BED-LINK |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch03/loop/stages/round1 | SIM_STAGE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SIM_STAGE-CH03-LOOP-ROUND1 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch03/loop/outcomes/checked | SIM_OUTCOME | PUBLIC_WITH_PRESENTATION_ADAPTATION | SIM_OUTCOME-CH03-LOOP-CHECKED |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch03/loop/outcomes/scrolled | SIM_OUTCOME | PUBLIC_WITH_PRESENTATION_ADAPTATION | SIM_OUTCOME-CH03-LOOP-SCROLLED |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch03/pause | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH03-PAUSE |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch03/pause/meters/awake_in_bed | SIM_METER | PUBLIC_WITH_PRESENTATION_ADAPTATION | SIM_METER-CH03-PAUSE-AWAKE-IN-BED |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch03/pause/meters/bed_link | SIM_METER | PUBLIC_WITH_PRESENTATION_ADAPTATION | SIM_METER-CH03-PAUSE-BED-LINK |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch03/pause/stages/escape | SIM_STAGE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SIM_STAGE-CH03-PAUSE-ESCAPE |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch03/pause/outcomes/stayed | SIM_OUTCOME | PUBLIC_WITH_PRESENTATION_ADAPTATION | SIM_OUTCOME-CH03-PAUSE-STAYED |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch03/pause/outcomes/left | SIM_OUTCOME | PUBLIC_WITH_PRESENTATION_ADAPTATION | SIM_OUTCOME-CH03-PAUSE-LEFT |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch03/link_grows | SCENE | PUBLIC_AS_IS | SCENE-CH03-LINK-GROWS | SC-01; SC-03; SC-11; AASM-2021:Recommendation 3; Table 3 Stimulus control; AASM-2021:Table 3 Stimulus control (b); VADOD-2025:患者沟通话术 p.132（2019 p.41 同）; VADOD-2025:Sidebar 3 p.30 | 静态一般教育场景，原文语义可保留。 |
| scene/ch03/link_grows/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH03-LINK-GROWS-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch03/night_quiet | SCENE | PUBLIC_AS_IS | SCENE-CH03-NIGHT-QUIET | SC-12; SC-08; SC-10; CN-2023:表 4 p.570; AASM-2021:Recommendation 3 段落; VADOD-2025:Sidebar 3 p.31; CN-2023:特殊人群 · 老年人群; VADOD-2025:由 Rec 23 说明「对成分进行修改可降低潜在伤害」与 adapt 原则推导；属执行弹性表述，非新增疗法 | 静态一般教育场景，原文语义可保留。 |
| scene/ch03/night_quiet/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH03-NIGHT-QUIET-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch03/rule_reveal | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH03-RULE-REVEAL | SC-01; AASM-2021:Recommendation 3; Table 3 Stimulus control | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch03/rule_reveal/detail | CONTENT_BLOCK | PUBLIC_CONCEPT_ONLY | CONTENT-BLOCK-CH03-RULE-REVEAL-DETAIL |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch03/patient_language | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH03-PATIENT-LANGUAGE | SC-11; VADOD-2025:患者沟通话术 p.132（2019 p.41 同）; VADOD-2025:Sidebar 3 p.30 | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch03/nuance | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH03-NUANCE | SC-08; SC-09; SF-15; SF-02; SC-10; AASM-2021:Recommendation 3 段落; VADOD-2025:Sidebar 3 p.31; CN-2023:特殊人群 · 老年人群; AASM-2021:由 SC-01 的目标与 SC-08 的安全调整要求推导；非新增疗法; VADOD-2025:Sidebar 3 的 adapt（适配）原则 p.31; AASM-2021:Recommendation 3 段落（同 SC-08）; VADOD-2025:由 Rec 23 说明「对成分进行修改可降低潜在伤害」与 adapt 原则推导；属执行弹性表述，非新增疗法 | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch03/nuance/detail | CONTENT_BLOCK | PUBLIC_CONCEPT_ONLY | CONTENT-BLOCK-CH03-NUANCE-DETAIL |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch03/nuance/cognitive-exercise | COGNITIVE_EXERCISE | PUBLIC_CONCEPT_ONLY | COGNITIVE-EXERCISE-CH03-NUANCE |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch03/adapt_delay | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH03-ADAPT-DELAY | SF-12; VADOD-2025:Sidebar 3 p.31 | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch03/short | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH03-SHORT | SC-10; VADOD-2025:由 Rec 23 说明「对成分进行修改可降低潜在伤害」与 adapt 原则推导；属执行弹性表述，非新增疗法 | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| chapter/ch04 | CHAPTER | PUBLIC_CONCEPT_ONLY | CHAPTER-CH04 |  | 只保留睡眠限制的概念结构；治疗算法场景另行排除。 |
| scene/ch04/hook | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH04-HOOK | SR-03; AASM-2021:由 SR-01 / SR-02 的机制表述推导（enhance sleep drive and consolidate sleep） | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/hook/detail | CONTENT_BLOCK | PUBLIC_CONCEPT_ONLY | CONTENT-BLOCK-CH04-HOOK-DETAIL |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/tib | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH04-TIB | SF-10; CN-2023:睡眠日记章节; AASM-2021:Table 3 使用 sleep efficiency thresholds 概念 | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/tib/detail | CONTENT_BLOCK | PUBLIC_CONCEPT_ONLY | CONTENT-BLOCK-CH04-TIB-DETAIL |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/compare | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH04-COMPARE |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/compare/options/room_a | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH04-COMPARE-ROOM-A | SR-01; SR-03; SF-10; AASM-2021:Table 3 Sleep restriction therapy; AASM-2021:由 SR-01 / SR-02 的机制表述推导（enhance sleep drive and consolidate sleep）; CN-2023:睡眠日记章节; AASM-2021:Table 3 使用 sleep efficiency thresholds 概念 | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/compare/options/room_b | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH04-COMPARE-ROOM-B | SR-01; SR-02; SF-10; AASM-2021:Table 3 Sleep restriction therapy; CN-2023:睡眠日记章节; AASM-2021:Table 3 使用 sleep efficiency thresholds 概念 | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/compare/options/same | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH04-COMPARE-SAME | SR-01; SC-01; AASM-2021:Table 3 Sleep restriction therapy; AASM-2021:Recommendation 3; Table 3 Stimulus control | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| review/ch04/compare/room_b | REVIEW_ITEM | PUBLIC_CONCEPT_ONLY | REVIEW-CH04-COMPARE-ROOM-B |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| review/ch04/compare/same | REVIEW_ITEM | PUBLIC_CONCEPT_ONLY | REVIEW-CH04-COMPARE-SAME |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/why_short | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH04-WHY-SHORT |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/why_short/meters/tib | SIM_METER | PUBLIC_CONCEPT_ONLY | SIM_METER-CH04-WHY-SHORT-TIB |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/why_short/meters/efficiency | SIM_METER | PUBLIC_CONCEPT_ONLY | SIM_METER-CH04-WHY-SHORT-EFFICIENCY |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/why_short/meters/pressure | SIM_METER | PUBLIC_CONCEPT_ONLY | SIM_METER-CH04-WHY-SHORT-PRESSURE |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/why_short/stages/step | SIM_STAGE | PUBLIC_CONCEPT_ONLY | SIM_STAGE-CH04-WHY-SHORT-STEP |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/why_short/outcomes/tightened | SIM_OUTCOME | PUBLIC_CONCEPT_ONLY | SIM_OUTCOME-CH04-WHY-SHORT-TIGHTENED |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/why_short/outcomes/nochange | SIM_OUTCOME | PUBLIC_CONCEPT_ONLY | SIM_OUTCOME-CH04-WHY-SHORT-NOCHANGE |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/why_short/outcomes/overshoot | SIM_OUTCOME | PUBLIC_CONCEPT_ONLY | SIM_OUTCOME-CH04-WHY-SHORT-OVERSHOOT |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/baseline | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH04-BASELINE | VA-01; SF-09; SF-10; SR-01; VA-CBTI-2014:A2.1 Initial time in bed; CN-2023:失眠的临床评估 · 主观评估 · 睡眠日记; CN-2023:睡眠日记章节; AASM-2021:Table 3 使用 sleep efficiency thresholds 概念; AASM-2021:Table 3 Sleep restriction therapy | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/baseline/detail | CONTENT_BLOCK | PUBLIC_CONCEPT_ONLY | CONTENT-BLOCK-CH04-BASELINE-DETAIL |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/window | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH04-WINDOW | VA-01; VA-02; SR-01; SR-02; SC-05; SF-10; VA-CBTI-2014:A2.1 Initial time in bed; AASM-2021:Table 3 Sleep restriction therapy; AASM-2021:Table 3 Stimulus control (d); CN-2023:睡眠日记章节; AASM-2021:Table 3 使用 sleep efficiency thresholds 概念 | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/window/detail | CONTENT_BLOCK | PUBLIC_CONCEPT_ONLY | CONTENT-BLOCK-CH04-WINDOW-DETAIL |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/earliest | SCENE | FUTURE_TREATMENT_ONLY | — | VA-03; VA-04; VA-05; SC-02; SC-05; VA-CBTI-2014:A2.2 Fixed rise time and time in bed window; AASM-2021:Table 3 Stimulus control (a); AASM-2021:Table 3 Stimulus control (d) | 包含起床时间减卧床时长、最早上床及离床分钟数等治疗执行规则。 |
| scene/ch04/earliest/detail | CONTENT_BLOCK | FUTURE_TREATMENT_ONLY | — |  | 包含起床时间减卧床时长、最早上床及离床分钟数等治疗执行规则。 |
| scene/ch04/efficiency | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH04-EFFICIENCY | SF-10; VA-06; SR-02; MC-05; CN-2023:睡眠日记章节; AASM-2021:Table 3 使用 sleep efficiency thresholds 概念; VA-CBTI-2014:A2.6 Weekly review; AASM-2021:Table 3 Sleep restriction therapy; CN-2023:睡眠日记章节（明确公式）; AASM-2021:Table 3 sleep efficiency thresholds | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/efficiency/detail | CONTENT_BLOCK | PUBLIC_CONCEPT_ONLY | CONTENT-BLOCK-CH04-EFFICIENCY-DETAIL |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/review | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH04-REVIEW | VA-06; SR-02; SF-09; SR-01; VA-CBTI-2014:A2.6 Weekly review; AASM-2021:Table 3 Sleep restriction therapy; CN-2023:失眠的临床评估 · 主观评估 · 睡眠日记 | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/review/detail | CONTENT_BLOCK | PUBLIC_CONCEPT_ONLY | CONTENT-BLOCK-CH04-REVIEW-DETAIL |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/sleep_need | SCENE | BLOCKED | — | VA-09; VA-07; VA-CBTI-2014:Sleep Need Questionnaire（用途与评分区间）; VA-CBTI-2014:A2.6 / Appendix Weekly adjustment | 涉及受限制的 Sleep Need Questionnaire 用途与评分区间；V4 已标 RIGHTS_REVIEW_REQUIRED。 |
| scene/ch04/sleep_need/detail | CONTENT_BLOCK | BLOCKED | — |  | 涉及受限制的 Sleep Need Questionnaire 用途与评分区间；V4 已标 RIGHTS_REVIEW_REQUIRED。 |
| scene/ch04/next_cycle | SCENE | FUTURE_TREATMENT_ONLY | — | VA-07; VA-08; SR-02; SR-01; VA-CBTI-2014:A2.6 / Appendix Weekly adjustment; VA-CBTI-2014:A2.6 Weekly review; AASM-2021:Table 3 Sleep restriction therapy | 包含 Sleep Need 分数、睡眠效率阈值和每周分钟级窗口调整算法。 |
| scene/ch04/next_cycle/detail | CONTENT_BLOCK | FUTURE_TREATMENT_ONLY | — |  | 包含 Sleep Need 分数、睡眠效率阈值和每周分钟级窗口调整算法。 |
| scene/ch04/not_only_efficiency | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH04-NOT-ONLY-EFFICIENCY | VA-08; SF-12; SR-07; SR-16; SF-03; SF-04; M-06; VA-CBTI-2014:A2.6 Weekly review; VADOD-2025:Sidebar 3 p.31; AASM-2021:Recommendation 4 段落; VADOD-2025:Recommendation 23 说明 pp.28-29（2019） | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/not_only_efficiency/detail | CONTENT_BLOCK | PUBLIC_CONCEPT_ONLY | CONTENT-BLOCK-CH04-NOT-ONLY-EFFICIENCY-DETAIL |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/not_conflict | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH04-NOT-CONFLICT | SC-01; SR-03; SR-01; AASM-2021:Recommendation 3; Table 3 Stimulus control; AASM-2021:由 SR-01 / SR-02 的机制表述推导（enhance sleep drive and consolidate sleep）; AASM-2021:Table 3 Sleep restriction therapy | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/safety | SCENE | BLOCKED | — | SR-07; SR-11; M-08; SF-12; AASM-2021:Recommendation 4 段落; VADOD-2025:Sidebar 3 p.31; VADOD-2025:Recommendation 5 说明 p.47 | 高风险职业、双相、癫痫、嗜睡被合并为统一建议，且引用 SR-07 的“可能禁忌”宽泛措辞，需安全复核。 |
| scene/ch04/safety/detail | CONTENT_BLOCK | BLOCKED | — |  | 高风险职业、双相、癫痫、嗜睡被合并为统一建议，且引用 SR-07 的“可能禁忌”宽泛措辞，需安全复核。 |
| scene/ch04/safety/cognitive-exercise | COGNITIVE_EXERCISE | BLOCKED | — |  | 高风险职业、双相、癫痫、嗜睡被合并为统一建议，且引用 SR-07 的“可能禁忌”宽泛措辞，需安全复核。 |
| scene/ch04/why_careful | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH04-WHY-CAREFUL | SF-13; VADOD-2025:Recommendation 24 说明 p.29（2019） | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/who | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH04-WHO | SF-12; VADOD-2025:Sidebar 3 p.31 | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/modifiable | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH04-MODIFIABLE | SR-16; VADOD-2025:Recommendation 23 说明 pp.28-29（2019） | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/evidence | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH04-EVIDENCE | SR-14; SR-04; M-01; CN-2023:表 4 p.570; CN-2023:表 4 注释（4）; AASM-2021:Recommendation 4; AASM-2021:Recommendation 1; VADOD-2025:Recommendation 5; CN-2023:失眠的心理行为治疗 p.569 | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/pro_help | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH04-PRO-HELP | M-09; M-08; SF-16; SR-11; VADOD-2025:p.46; VADOD-2025:Recommendation 5 说明 p.47; VADOD-2025:2025 Patient's Guide p.2 | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/pro_help/detail | CONTENT_BLOCK | PUBLIC_CONCEPT_ONLY | CONTENT-BLOCK-CH04-PRO-HELP-DETAIL |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch04/end | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH04-END |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| chapter/ch05 | CHAPTER | PUBLIC_WITH_PRESENTATION_ADAPTATION | CHAPTER-CH05 |  | 保留章节结构，Web 解锁与路线改为原生展示语义。 |
| scene/ch05/hook | SCENE | PUBLIC_AS_IS | SCENE-CH05-HOOK | CT-01; AASM-2021:Table 3 Cognitive therapy | 静态一般教育场景，原文语义可保留。 |
| scene/ch05/how | SCENE | PUBLIC_AS_IS | SCENE-CH05-HOW | CT-04; AASM-2021:依据 CT-01 的机制；属于表达方式设计，不新增疗法 | 静态一般教育场景，原文语义可保留。 |
| scene/ch05/card1 | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH05-CARD1 | CT-04; CT-01; CT-02; AASM-2021:依据 CT-01 的机制；属于表达方式设计，不新增疗法; AASM-2021:Table 3 Cognitive therapy | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card1/detail | CONTENT_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | CONTENT-BLOCK-CH05-CARD1-DETAIL |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card1/cognitive-exercise | COGNITIVE_EXERCISE | PUBLIC_WITH_PRESENTATION_ADAPTATION | COGNITIVE-EXERCISE-CH05-CARD1 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card2 | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH05-CARD2 | CT-01; AASM-2021:Table 3 Cognitive therapy | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card2/detail | CONTENT_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | CONTENT-BLOCK-CH05-CARD2-DETAIL |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card2/cognitive-exercise | COGNITIVE_EXERCISE | PUBLIC_WITH_PRESENTATION_ADAPTATION | COGNITIVE-EXERCISE-CH05-CARD2 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card3 | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH05-CARD3 | M-01; M-02; AASM-2021:Recommendation 1; VADOD-2025:Recommendation 5; CN-2023:失眠的心理行为治疗 p.569; AASM-2021:Table 3 Summary of interventions (CBT-I, Multicomponent) | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card3/detail | CONTENT_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | CONTENT-BLOCK-CH05-CARD3-DETAIL |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card3/cognitive-exercise | COGNITIVE_EXERCISE | PUBLIC_WITH_PRESENTATION_ADAPTATION | COGNITIVE-EXERCISE-CH05-CARD3 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card4 | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH05-CARD4 | CT-01; CT-05; AASM-2021:Table 3 Cognitive therapy; AASM-2021:由 CT-01 的 performance anxiety 与反觉醒方法描述支撑 | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card4/detail | CONTENT_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | CONTENT-BLOCK-CH05-CARD4-DETAIL |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card4/cognitive-exercise | COGNITIVE_EXERCISE | PUBLIC_WITH_PRESENTATION_ADAPTATION | COGNITIVE-EXERCISE-CH05-CARD4 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card5 | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH05-CARD5 | CT-06; M-06; M-07; VADOD-2025:Recommendation 8; CN-2023:失眠的心理行为治疗 p.569 | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card5/detail | CONTENT_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | CONTENT-BLOCK-CH05-CARD5-DETAIL |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card5/cognitive-exercise | COGNITIVE_EXERCISE | PUBLIC_WITH_PRESENTATION_ADAPTATION | COGNITIVE-EXERCISE-CH05-CARD5 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card6 | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH05-CARD6 | CT-01; MC-01; SR-03; VA-01; AASM-2021:Table 3 Cognitive therapy; AASM-2021:Table 3 CBT-I 描述含 education about sleep regulation；睡眠限制机制（enhance sleep drive）间接支撑; AASM-2021:由 SR-01 / SR-02 的机制表述推导（enhance sleep drive and consolidate sleep）; VA-CBTI-2014:A2.1 Initial time in bed | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card6/detail | CONTENT_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | CONTENT-BLOCK-CH05-CARD6-DETAIL |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card6/cognitive-exercise | COGNITIVE_EXERCISE | PUBLIC_WITH_PRESENTATION_ADAPTATION | COGNITIVE-EXERCISE-CH05-CARD6 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card7 | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH05-CARD7 | CT-07; CT-04; CT-01; AASM-2021:Table 3 Cognitive therapy; VA-CBTI-2014:Therapist Manual（认知与依从性相关章节）; AASM-2021:依据 CT-01 的机制；属于表达方式设计，不新增疗法 | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card7/detail | CONTENT_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | CONTENT-BLOCK-CH05-CARD7-DETAIL |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card7/cognitive-exercise | COGNITIVE_EXERCISE | PUBLIC_WITH_PRESENTATION_ADAPTATION | COGNITIVE-EXERCISE-CH05-CARD7 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card8 | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH05-CARD8 | VA-11; CT-05; RL-04; VA-CBTI-2014:Therapist Manual（认知与依从性相关章节）; AASM-2021:由 CT-01 的 performance anxiety 与反觉醒方法描述支撑; AASM-2021:Table 3 Cognitive therapy; AASM-2021:Table 3 Relaxation therapy | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card8/detail | CONTENT_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | CONTENT-BLOCK-CH05-CARD8-DETAIL |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/card8/cognitive-exercise | COGNITIVE_EXERCISE | PUBLIC_WITH_PRESENTATION_ADAPTATION | COGNITIVE-EXERCISE-CH05-CARD8 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch05/wrap | SCENE | PUBLIC_AS_IS | SCENE-CH05-WRAP | CT-04; CT-01; AASM-2021:依据 CT-01 的机制；属于表达方式设计，不新增疗法; AASM-2021:Table 3 Cognitive therapy | 静态一般教育场景，原文语义可保留。 |
| scene/ch05/multicomponent | SCENE | PUBLIC_AS_IS | SCENE-CH05-MULTICOMPONENT | M-02; M-01; SR-04; CT-03; AASM-2021:Table 3 Summary of interventions (CBT-I, Multicomponent); AASM-2021:Recommendation 1; VADOD-2025:Recommendation 5; CN-2023:失眠的心理行为治疗 p.569; AASM-2021:Recommendation 4; AASM-2021:Table 3 NO RECOMMENDATIONS 区段 | 静态一般教育场景，原文语义可保留。 |
| scene/ch05/multicomponent/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH05-MULTICOMPONENT-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch05/limit | SCENE | PUBLIC_AS_IS | SCENE-CH05-LIMIT |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch05/end | SCENE | PUBLIC_AS_IS | SCENE-CH05-END |  | 静态一般教育场景，原文语义可保留。 |
| chapter/ch06 | CHAPTER | PUBLIC_WITH_PRESENTATION_ADAPTATION | CHAPTER-CH06 |  | 保留章节结构，Web 解锁与路线改为原生展示语义。 |
| scene/ch06/hook | SCENE | PUBLIC_AS_IS | SCENE-CH06-HOOK | SH-02; AASM-2021:Recommendation 6; VADOD-2025:Recommendation 7 | 静态一般教育场景，原文语义可保留。 |
| scene/ch06/hook/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH06-HOOK-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch06/hygiene_note | SCENE | PUBLIC_AS_IS | SCENE-CH06-HYGIENE-NOTE | SH-03; AASM-2021:Recommendation 6 Remarks | 静态一般教育场景，原文语义可保留。 |
| scene/ch06/hygiene_note/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH06-HYGIENE-NOTE-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch06/four_dims | SCENE | PUBLIC_AS_IS | SCENE-CH06-FOUR-DIMS | SH-05; SH-01; AASM-2021:由 SH-01 睡眠卫生的构成维度（生活方式 + 环境）现实化展开；不新增疗法，属于判断训练; AASM-2021:Table 3 Sleep hygiene | 静态一般教育场景，原文语义可保留。 |
| scene/ch06/four_dims/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH06-FOUR-DIMS-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch06/analyzer | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH06-ANALYZER | SH-05; SH-02; SH-03; AASM-2021:由 SH-01 睡眠卫生的构成维度（生活方式 + 环境）现实化展开；不新增疗法，属于判断训练; AASM-2021:Recommendation 6; VADOD-2025:Recommendation 7; AASM-2021:Recommendation 6 Remarks | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/detail | CONTENT_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | CONTENT-BLOCK-CH06-ANALYZER-DETAIL |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/stimulus | ANALYZER_DIMENSION | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER_DIMENSION-CH06-ANALYZER-STIMULUS |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/stimulus/levels/0 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-0-0 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/stimulus/levels/1 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-0-1 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/stimulus/levels/2 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-0-2 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/stimulus/levels/3 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-0-3 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/stimulus/levels/4 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-0-4 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/duration | ANALYZER_DIMENSION | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER_DIMENSION-CH06-ANALYZER-DURATION |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/duration/levels/0 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-1-0 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/duration/levels/1 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-1-1 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/duration/levels/2 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-1-2 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/duration/levels/3 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-1-3 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/duration/levels/4 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-1-4 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/emotion | ANALYZER_DIMENSION | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER_DIMENSION-CH06-ANALYZER-EMOTION |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/emotion/levels/0 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-2-0 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/emotion/levels/1 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-2-1 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/emotion/levels/2 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-2-2 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/emotion/levels/3 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-2-3 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/emotion/levels/4 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-2-4 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/delay | ANALYZER_DIMENSION | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER_DIMENSION-CH06-ANALYZER-DELAY |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/delay/levels/0 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-3-0 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/delay/levels/1 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-3-1 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/delay/levels/2 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-3-2 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/delay/levels/3 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-3-3 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/dimensions/delay/levels/4 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH06-ANALYZER-3-4 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/cases/0 | ANALYZER_CASE | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER_CASE-CH06-ANALYZER-0 | SH-05; SH-01; AASM-2021:由 SH-01 睡眠卫生的构成维度（生活方式 + 环境）现实化展开；不新增疗法，属于判断训练; AASM-2021:Table 3 Sleep hygiene | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/cases/1 | ANALYZER_CASE | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER_CASE-CH06-ANALYZER-1 | SH-05; SH-06; AASM-2021:由 SH-01 睡眠卫生的构成维度（生活方式 + 环境）现实化展开；不新增疗法，属于判断训练; AASM-2021:与 CT-05 / 第 2 章觉醒机制一致 | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/cases/2 | ANALYZER_CASE | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER_CASE-CH06-ANALYZER-2 | SH-05; SH-07; AASM-2021:由 SH-01 睡眠卫生的构成维度（生活方式 + 环境）现实化展开；不新增疗法，属于判断训练; CN-2023:倒班人群推荐条目 | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/analyzer/cases/3 | ANALYZER_CASE | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER_CASE-CH06-ANALYZER-3 | SH-06; SH-05; AASM-2021:与 CT-05 / 第 2 章觉醒机制一致; AASM-2021:由 SH-01 睡眠卫生的构成维度（生活方式 + 环境）现实化展开；不新增疗法，属于判断训练 | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/watch | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH06-WATCH |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch06/watch/options/which | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH06-WATCH-WHICH | SH-05; AASM-2021:由 SH-01 睡眠卫生的构成维度（生活方式 + 环境）现实化展开；不新增疗法，属于判断训练 | 保留为假设情境中的教育选择；将原 adjustment 行为建议/例外处置留在清单中，不进入概念预览内容。 |
| scene/ch06/watch/options/content | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH06-WATCH-CONTENT | SH-05; AASM-2021:由 SH-01 睡眠卫生的构成维度（生活方式 + 环境）现实化展开；不新增疗法，属于判断训练 | 保留为假设情境中的教育选择；将原 adjustment 行为建议/例外处置留在清单中，不进入概念预览内容。 |
| scene/ch06/watch/options/screen | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH06-WATCH-SCREEN | SH-01; SH-05; AASM-2021:Table 3 Sleep hygiene; AASM-2021:由 SH-01 睡眠卫生的构成维度（生活方式 + 环境）现实化展开；不新增疗法，属于判断训练 | 保留为假设情境中的教育选择；将原 adjustment 行为建议/例外处置留在清单中，不进入概念预览内容。 |
| review/ch06/watch/content | REVIEW_ITEM | PUBLIC_CONCEPT_ONLY | REVIEW-CH06-WATCH-CONTENT |  | 保留为假设情境中的教育选择；将原 adjustment 行为建议/例外处置留在清单中，不进入概念预览内容。 |
| scene/ch06/anchor | SCENE | PUBLIC_AS_IS | SCENE-CH06-ANCHOR | SH-07; CN-2023:倒班人群推荐条目 | 静态一般教育场景，原文语义可保留。 |
| scene/ch06/not_alone | SCENE | PUBLIC_AS_IS | SCENE-CH06-NOT-ALONE | SH-09; VADOD-2025:Recommendation 7 说明 p.50 | 静态一般教育场景，原文语义可保留。 |
| scene/ch06/not_alone/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH06-NOT-ALONE-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch06/end | SCENE | PUBLIC_AS_IS | SCENE-CH06-END |  | 静态一般教育场景，原文语义可保留。 |
| chapter/ch07 | CHAPTER | PUBLIC_WITH_PRESENTATION_ADAPTATION | CHAPTER-CH07 |  | 保留章节结构，Web 解锁与路线改为原生展示语义。 |
| scene/ch07/hook | SCENE | PUBLIC_AS_IS | SCENE-CH07-HOOK | SF-03; SF-12; AASM-2021:Recommendation 4 段落; VADOD-2025:Sidebar 3 p.31 | 静态一般教育场景，原文语义可保留。 |
| scene/ch07/hook/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH07-HOOK-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch07/distinguish | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH07-DISTINGUISH |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch07/distinguish/options/cant_control | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH07-DISTINGUISH-CANT-CONTROL | SF-03; SF-12; AASM-2021:Recommendation 4 段落; VADOD-2025:Sidebar 3 p.31 | 保留为假设情境中的教育选择；将原 adjustment 行为建议/例外处置留在清单中，不进入概念预览内容。 |
| scene/ch07/distinguish/options/tired | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH07-DISTINGUISH-TIRED | SF-03; AASM-2021:Recommendation 4 段落; VADOD-2025:Sidebar 3 p.31 | 保留为假设情境中的教育选择；将原 adjustment 行为建议/例外处置留在清单中，不进入概念预览内容。 |
| scene/ch07/distinguish/options/both_same | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH07-DISTINGUISH-BOTH-SAME | SF-03; AASM-2021:Recommendation 4 段落; VADOD-2025:Sidebar 3 p.31 | 保留为假设情境中的教育选择；将原 adjustment 行为建议/例外处置留在清单中，不进入概念预览内容。 |
| review/ch07/distinguish/tired | REVIEW_ITEM | PUBLIC_CONCEPT_ONLY | REVIEW-CH07-DISTINGUISH-TIRED |  | 保留为假设情境中的教育选择；将原 adjustment 行为建议/例外处置留在清单中，不进入概念预览内容。 |
| scene/ch07/safety_gate | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH07-SAFETY-GATE | SF-04; SF-12; VADOD-2025:Recommendation 23 说明 pp.28-29（2019）; AASM-2021:Recommendation 4 段落; VADOD-2025:Sidebar 3 p.31 | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch07/safety_gate/safety-notice | SAFETY_NOTICE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SAFETY-NOTICE-CH07-SAFETY-GATE |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch07/high_risk | SCENE | BLOCKED | — | SR-07; AASM-2021:Recommendation 4 段落; VADOD-2025:Sidebar 3 p.31 | 把高风险职业一律写成“必须先经过医生评估”，V4 自身 G-8 说明该依据未纳入指南系统评价。 |
| scene/ch07/high_risk/detail | CONTENT_BLOCK | BLOCKED | — |  | 把高风险职业一律写成“必须先经过医生评估”，V4 自身 G-8 说明该依据未纳入指南系统评价。 |
| scene/ch07/modifiable | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH07-MODIFIABLE | SR-16; VADOD-2025:Recommendation 23 说明 pp.28-29（2019） | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch07/modifiable/detail | CONTENT_BLOCK | PUBLIC_CONCEPT_ONLY | CONTENT-BLOCK-CH07-MODIFIABLE-DETAIL |  | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch07/what_to_do | SCENE | PUBLIC_AS_IS | SCENE-CH07-WHAT-TO-DO | SF-09; CN-2023:失眠的临床评估 · 主观评估 · 睡眠日记 | 静态一般教育场景，原文语义可保留。 |
| scene/ch07/end | SCENE | PUBLIC_AS_IS | SCENE-CH07-END |  | 静态一般教育场景，原文语义可保留。 |
| chapter/ch08 | CHAPTER | PUBLIC_WITH_PRESENTATION_ADAPTATION | CHAPTER-CH08 |  | 保留章节结构，Web 解锁与路线改为原生展示语义。 |
| scene/ch08/boundary | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH08-BOUNDARY | SF-06; SF-07; AASM-2021:Introduction | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch08/signals | SCENE | BLOCKED | — | SF-05; SF-12; SF-06; SF-07; CN-2023:共病章节; VADOD-2025:Sidebar 3 p.31（adapt/delay 清单）; VADOD-2025:Sidebar 3 p.31; AASM-2021:Introduction | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/detail | CONTENT_BLOCK | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/0 | CHECKLIST_GROUP | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/0/items/0 | CHECKLIST_ITEM | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/0/items/1 | CHECKLIST_ITEM | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/0/items/2 | CHECKLIST_ITEM | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/0/items/3 | CHECKLIST_ITEM | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/0/items/4 | CHECKLIST_ITEM | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/0/items/5 | CHECKLIST_ITEM | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/1 | CHECKLIST_GROUP | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/1/items/0 | CHECKLIST_ITEM | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/1/items/1 | CHECKLIST_ITEM | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/1/items/2 | CHECKLIST_ITEM | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/1/items/3 | CHECKLIST_ITEM | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/2 | CHECKLIST_GROUP | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/2/items/0 | CHECKLIST_ITEM | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/2/items/1 | CHECKLIST_ITEM | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/2/items/2 | CHECKLIST_ITEM | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/2/items/3 | CHECKLIST_ITEM | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/signals/groups/2/items/4 | CHECKLIST_ITEM | BLOCKED | — |  | 混合准筛查清单、急性自伤处置、药物和多种疾病提示；条目级来源未拆分，不能作为普通 Education checklist 发布。 |
| scene/ch08/doctor_list | SCENE | PUBLIC_CONCEPT_ONLY | SCENE-CH08-DOCTOR-LIST | SF-16; VADOD-2025:2025 Patient's Guide p.2 | 保留一般教育概念；省略治疗细节、个体执行反馈和专业决策步骤。 |
| scene/ch08/dept | SCENE | BLOCKED | — | SF-08; CN-2023:共病精神障碍章节 | “挂错了医生也会帮你转”是无来源的服务结果保证。 |
| scene/ch08/diary | SCENE | PUBLIC_AS_IS | SCENE-CH08-DIARY | SF-09; CN-2023:失眠的临床评估 · 主观评估 · 睡眠日记 | 静态一般教育场景，原文语义可保留。 |
| scene/ch08/questions | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH08-QUESTIONS | SF-09; CN-2023:失眠的临床评估 · 主观评估 · 睡眠日记 | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch08/questions/groups/0 | CHECKLIST_GROUP | PUBLIC_WITH_PRESENTATION_ADAPTATION | CHECKLIST_GROUP-CH08-QUESTIONS-0 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch08/questions/groups/0/items/0 | CHECKLIST_ITEM | PUBLIC_WITH_PRESENTATION_ADAPTATION | CHECKLIST-ITEM-CH08-QUESTIONS-0-0 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch08/questions/groups/0/items/1 | CHECKLIST_ITEM | PUBLIC_WITH_PRESENTATION_ADAPTATION | CHECKLIST-ITEM-CH08-QUESTIONS-0-1 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch08/questions/groups/0/items/2 | CHECKLIST_ITEM | PUBLIC_WITH_PRESENTATION_ADAPTATION | CHECKLIST-ITEM-CH08-QUESTIONS-0-2 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch08/questions/groups/0/items/3 | CHECKLIST_ITEM | PUBLIC_WITH_PRESENTATION_ADAPTATION | CHECKLIST-ITEM-CH08-QUESTIONS-0-3 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch08/questions/groups/0/items/4 | CHECKLIST_ITEM | PUBLIC_WITH_PRESENTATION_ADAPTATION | CHECKLIST-ITEM-CH08-QUESTIONS-0-4 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch08/questions/groups/0/items/5 | CHECKLIST_ITEM | PUBLIC_WITH_PRESENTATION_ADAPTATION | CHECKLIST-ITEM-CH08-QUESTIONS-0-5 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch08/end | SCENE | PUBLIC_AS_IS | SCENE-CH08-END |  | 静态一般教育场景，原文语义可保留。 |
| chapter/ch09 | CHAPTER | PUBLIC_WITH_PRESENTATION_ADAPTATION | CHAPTER-CH09 |  | 保留章节结构，Web 解锁与路线改为原生展示语义。 |
| scene/ch09/open | SCENE | PUBLIC_AS_IS | SCENE-CH09-OPEN | M-02; AASM-2021:Table 3 Summary of interventions (CBT-I, Multicomponent) | 静态一般教育场景，原文语义可保留。 |
| scene/ch09/notes | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH09-NOTES | M-02; CT-04; AASM-2021:Table 3 Summary of interventions (CBT-I, Multicomponent); AASM-2021:依据 CT-01 的机制；属于表达方式设计，不新增疗法 | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch09/notes/reflection | REFLECTION | PUBLIC_WITH_PRESENTATION_ADAPTATION | REFLECTION-CH09-NOTES |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch09/notes/fields/n1 | NOTE_PROMPT | PUBLIC_WITH_PRESENTATION_ADAPTATION | NOTE_PROMPT-CH09-NOTES-N1 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch09/notes/fields/n2 | NOTE_PROMPT | PUBLIC_WITH_PRESENTATION_ADAPTATION | NOTE_PROMPT-CH09-NOTES-N2 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch09/notes/fields/n3 | NOTE_PROMPT | PUBLIC_WITH_PRESENTATION_ADAPTATION | NOTE_PROMPT-CH09-NOTES-N3 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch09/goal | SCENE | PUBLIC_AS_IS | SCENE-CH09-GOAL | MC-06; CN-2023:失眠的治疗 · 总体目标和干预策略（原文列 5 条） | 静态一般教育场景，原文语义可保留。 |
| scene/ch09/goal/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH09-GOAL-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch09/end | SCENE | PUBLIC_AS_IS | SCENE-CH09-END | MC-06; CT-04; CN-2023:失眠的治疗 · 总体目标和干预策略（原文列 5 条）; AASM-2021:依据 CT-01 的机制；属于表达方式设计，不新增疗法 | 静态一般教育场景，原文语义可保留。 |
| scene/ch09/end/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH09-END-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch09/summary | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH09-SUMMARY | MC-06; M-02; CN-2023:失眠的治疗 · 总体目标和干预策略（原文列 5 条）; AASM-2021:Table 3 Summary of interventions (CBT-I, Multicomponent) | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| chapter/ch10 | CHAPTER | PUBLIC_WITH_PRESENTATION_ADAPTATION | CHAPTER-CH10 |  | 保留章节结构，Web 解锁与路线改为原生展示语义。 |
| scene/ch10/position | SCENE | PUBLIC_AS_IS | SCENE-CH10-POSITION | SH-03; SH-02; SH-04; SH-09; AASM-2021:Recommendation 6 Remarks; AASM-2021:Recommendation 6; VADOD-2025:Recommendation 7; AASM-2021:Recommendation 6 段落; VADOD-2025:Recommendation 7 说明 p.50 | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/position/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH10-POSITION-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/relax_what | SCENE | PUBLIC_AS_IS | SCENE-CH10-RELAX-WHAT | RL-01; RL-05; AASM-2021:Table 3 Relaxation therapy; AASM-2021:Table 3 Relaxation therapy（structured exercises：somatic tension 与 cognitive arousal）; VADOD-2025:CBT-I components（relaxation / arousal reduction）; VA-CBTI-2014:Therapist Manual（放松相关章节） | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/relax_what/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH10-RELAX-WHAT-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/relax_day | SCENE | PUBLIC_AS_IS | SCENE-CH10-RELAX-DAY | RL-05; RL-06; AASM-2021:Table 3 Relaxation therapy（structured exercises：somatic tension 与 cognitive arousal）; VADOD-2025:CBT-I components（relaxation / arousal reduction）; VA-CBTI-2014:Therapist Manual（放松相关章节）; AASM-2021:Table 3 Relaxation therapy（cognitive arousal 部分） | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/relax_day/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH10-RELAX-DAY-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/relax_trap | SCENE | PUBLIC_AS_IS | SCENE-CH10-RELAX-TRAP | RL-04; CT-05; VA-11; AASM-2021:Table 3 Cognitive therapy; VA-CBTI-2014:Therapist Manual（认知与依从性相关章节）; AASM-2021:Table 3 Relaxation therapy; AASM-2021:由 CT-01 的 performance anxiety 与反觉醒方法描述支撑 | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/relax_trap/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH10-RELAX-TRAP-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/relax_choice | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH10-RELAX-CHOICE |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/relax_choice/options/push_harder | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH10-RELAX-CHOICE-PUSH-HARDER | RL-04; RL-05; AASM-2021:Table 3 Cognitive therapy; VA-CBTI-2014:Therapist Manual（认知与依从性相关章节）; AASM-2021:Table 3 Relaxation therapy; AASM-2021:Table 3 Relaxation therapy（structured exercises：somatic tension 与 cognitive arousal）; VADOD-2025:CBT-I components（relaxation / arousal reduction）; VA-CBTI-2014:Therapist Manual（放松相关章节） | 保留为假设情境中的教育选择；将原 adjustment 行为建议/例外处置留在清单中，不进入概念预览内容。 |
| scene/ch10/relax_choice/options/leave_bed | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH10-RELAX-CHOICE-LEAVE-BED | SC-03; RL-04; AASM-2021:Table 3 Stimulus control (b); AASM-2021:Table 3 Cognitive therapy; VA-CBTI-2014:Therapist Manual（认知与依从性相关章节）; AASM-2021:Table 3 Relaxation therapy | 保留为假设情境中的教育选择；将原 adjustment 行为建议/例外处置留在清单中，不进入概念预览内容。 |
| scene/ch10/relax_choice/options/keep_trying | CHOICE | PUBLIC_CONCEPT_ONLY | CHOICE-CH10-RELAX-CHOICE-KEEP-TRYING | RL-04; CT-05; AASM-2021:Table 3 Cognitive therapy; VA-CBTI-2014:Therapist Manual（认知与依从性相关章节）; AASM-2021:Table 3 Relaxation therapy; AASM-2021:由 CT-01 的 performance anxiety 与反觉醒方法描述支撑 | 保留为假设情境中的教育选择；将原 adjustment 行为建议/例外处置留在清单中，不进入概念预览内容。 |
| review/ch10/relax_choice/push_harder | REVIEW_ITEM | PUBLIC_CONCEPT_ONLY | REVIEW-CH10-RELAX-CHOICE-PUSH-HARDER |  | 保留为假设情境中的教育选择；将原 adjustment 行为建议/例外处置留在清单中，不进入概念预览内容。 |
| review/ch10/relax_choice/keep_trying | REVIEW_ITEM | PUBLIC_CONCEPT_ONLY | REVIEW-CH10-RELAX-CHOICE-KEEP-TRYING |  | 保留为假设情境中的教育选择；将原 adjustment 行为建议/例外处置留在清单中，不进入概念预览内容。 |
| scene/ch10/exercise | SCENE | PUBLIC_AS_IS | SCENE-CH10-EXERCISE | EX-01; EX-02; SR-EXERCISE-INSOMNIA:Abstract / Results（physical exercise interventions on insomnia）; SR-EVENING-EXERCISE:Abstract / Results（evening high-intensity exercise, healthy adults）; AASM-2021:Table 3 Sleep hygiene（lifestyle：diet, exercise, substance use）; AASM-2021:Sleep hygiene education（不作为慢性失眠的单独治疗） | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/exercise/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH10-EXERCISE-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/caffeine | SCENE | PUBLIC_AS_IS | SCENE-CH10-CAFFEINE | CF-01; SR-CAFFEINE:Abstract / Results（caffeine and subsequent sleep）; AASM-2021:Table 3 Sleep hygiene（substance use：caffeine） | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/caffeine/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH10-CAFFEINE-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/alcohol | SCENE | PUBLIC_AS_IS | SCENE-CH10-ALCOHOL | AL-01; SR-ALCOHOL:Abstract / Results（alcohol and subsequent sleep）; VA-CBTI-2014:Therapist Manual（睡眠卫生 / 物质使用相关章节） | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/alcohol/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH10-ALCOHOL-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/alcohol_edge | SCENE | PUBLIC_AS_IS | SCENE-CH10-ALCOHOL-EDGE | AL-02; SF-12; VADOD-2025:CBT-I adaptation / delay 清单（active alcohol or substance use disorder）; AASM-2021:Table 3 CBT-I components（酒精或物质使用障碍属于需要临床判断的情形）; VADOD-2025:Sidebar 3 p.31 | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/alcohol_edge/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH10-ALCOHOL-EDGE-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/nicotine | SCENE | PUBLIC_AS_IS | SCENE-CH10-NICOTINE | NC-01; NC-02; VA-CBTI-2014:Therapist Manual（睡眠卫生 / 尼古丁相关章节）; VA-CBTI-2014:Therapist Manual（睡眠卫生 / 物质使用相关章节）; VADOD-2025:CBT-I adaptation / delay 清单（物质使用需要专业规划） | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/nicotine/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH10-NICOTINE-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/analyzer | SCENE | PUBLIC_WITH_PRESENTATION_ADAPTATION | SCENE-CH10-ANALYZER | SH-05; AL-01; AL-02; CF-01; NC-01; AASM-2021:由 SH-01 睡眠卫生的构成维度（生活方式 + 环境）现实化展开；不新增疗法，属于判断训练; SR-ALCOHOL:Abstract / Results（alcohol and subsequent sleep）; VA-CBTI-2014:Therapist Manual（睡眠卫生 / 物质使用相关章节）; VADOD-2025:CBT-I adaptation / delay 清单（active alcohol or substance use disorder）; AASM-2021:Table 3 CBT-I components（酒精或物质使用障碍属于需要临床判断的情形）; SR-CAFFEINE:Abstract / Results（caffeine and subsequent sleep）; AASM-2021:Table 3 Sleep hygiene（substance use：caffeine）; VA-CBTI-2014:Therapist Manual（睡眠卫生 / 尼古丁相关章节） | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/stimulus | ANALYZER_DIMENSION | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER_DIMENSION-CH10-ANALYZER-STIMULUS |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/stimulus/levels/0 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-0-0 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/stimulus/levels/1 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-0-1 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/stimulus/levels/2 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-0-2 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/stimulus/levels/3 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-0-3 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/stimulus/levels/4 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-0-4 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/duration | ANALYZER_DIMENSION | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER_DIMENSION-CH10-ANALYZER-DURATION |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/duration/levels/0 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-1-0 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/duration/levels/1 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-1-1 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/duration/levels/2 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-1-2 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/duration/levels/3 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-1-3 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/duration/levels/4 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-1-4 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/emotion | ANALYZER_DIMENSION | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER_DIMENSION-CH10-ANALYZER-EMOTION |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/emotion/levels/0 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-2-0 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/emotion/levels/1 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-2-1 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/emotion/levels/2 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-2-2 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/emotion/levels/3 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-2-3 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/emotion/levels/4 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-2-4 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/delay | ANALYZER_DIMENSION | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER_DIMENSION-CH10-ANALYZER-DELAY |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/delay/levels/0 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-3-0 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/delay/levels/1 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-3-1 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/delay/levels/2 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-3-2 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/delay/levels/3 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-3-3 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/dimensions/delay/levels/4 | ANALYZER_LEVEL | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER-LEVEL-CH10-ANALYZER-3-4 |  | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/cases/0 | ANALYZER_CASE | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER_CASE-CH10-ANALYZER-0 | AL-01; AL-02; SR-ALCOHOL:Abstract / Results（alcohol and subsequent sleep）; VA-CBTI-2014:Therapist Manual（睡眠卫生 / 物质使用相关章节）; VADOD-2025:CBT-I adaptation / delay 清单（active alcohol or substance use disorder）; AASM-2021:Table 3 CBT-I components（酒精或物质使用障碍属于需要临床判断的情形） | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/analyzer/cases/1 | ANALYZER_CASE | BLOCKED | — | CF-01; SR-CAFFEINE:Abstract / Results（caffeine and subsequent sleep）; AASM-2021:Table 3 Sleep hygiene（substance use：caffeine） | 咖啡因案例直接建议用户自行改变时间和用量并观察数晚，属于个体化行为建议。 |
| scene/ch10/analyzer/cases/2 | ANALYZER_CASE | PUBLIC_WITH_PRESENTATION_ADAPTATION | ANALYZER_CASE-CH10-ANALYZER-2 | NC-01; NC-02; VA-CBTI-2014:Therapist Manual（睡眠卫生 / 尼古丁相关章节）; VA-CBTI-2014:Therapist Manual（睡眠卫生 / 物质使用相关章节）; VADOD-2025:CBT-I adaptation / delay 清单（物质使用需要专业规划） | 保留互动教育语义；Web 控件、预览/提交和页面流需改为原生呈现。 |
| scene/ch10/close | SCENE | PUBLIC_AS_IS | SCENE-CH10-CLOSE | SH-02; SH-03; AASM-2021:Recommendation 6; VADOD-2025:Recommendation 7; AASM-2021:Recommendation 6 Remarks | 静态一般教育场景，原文语义可保留。 |
| scene/ch10/close/detail | CONTENT_BLOCK | PUBLIC_AS_IS | CONTENT-BLOCK-CH10-CLOSE-DETAIL |  | 静态一般教育场景，原文语义可保留。 |
| map/entry | MAP_ENTRY | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-ENTRY |  | 保留入口节点；Web 深链改为原生内容链接。 |
| map/group/drive | MAP_GROUP | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-GROUP-DRIVE | KM-01; VA-CBTI-2014:治疗成分与顺序（睡眠限制 / 刺激控制 / 认知 / 放松）; AASM-2021:Table 3（CBT-I 的多组分构成）; VADOD-2025:CBT-I components 与 adaptation / delay 清单 | 知识地图为产品整理框架，必须展示其 provenance 标签。 |
| map/branch/drive-pressure | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-DRIVE-PRESSURE |  | 保留知识节点；原生端按层级展开。 |
| map/branch/drive-tib | MAP_BRANCH | PUBLIC_CONCEPT_ONLY | MAP-BRANCH-DRIVE-TIB |  | 保留睡眠限制概念与 rationale；排除窗口公式、阈值和调整清单。 |
| map/branch/drive-day | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-DRIVE-DAY |  | 保留知识节点；原生端按层级展开。 |
| map/branch/drive-activity | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-DRIVE-ACTIVITY |  | 保留知识节点；原生端按层级展开。 |
| map/group/clock | MAP_GROUP | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-GROUP-CLOCK | KM-01; VA-CBTI-2014:治疗成分与顺序（睡眠限制 / 刺激控制 / 认知 / 放松）; AASM-2021:Table 3（CBT-I 的多组分构成）; VADOD-2025:CBT-I components 与 adaptation / delay 清单 | 知识地图为产品整理框架，必须展示其 provenance 标签。 |
| map/branch/clock-wake | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-CLOCK-WAKE |  | 保留知识节点；原生端按层级展开。 |
| map/branch/clock-regular | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-CLOCK-REGULAR |  | 保留知识节点；原生端按层级展开。 |
| map/branch/clock-cues | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-CLOCK-CUES |  | 保留知识节点；原生端按层级展开。 |
| map/group/arousal | MAP_GROUP | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-GROUP-AROUSAL | KM-01; VA-CBTI-2014:治疗成分与顺序（睡眠限制 / 刺激控制 / 认知 / 放松）; AASM-2021:Table 3（CBT-I 的多组分构成）; VADOD-2025:CBT-I components 与 adaptation / delay 清单 | 知识地图为产品整理框架，必须展示其 provenance 标签。 |
| map/branch/arousal-sc | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-AROUSAL-SC |  | 保留知识节点；原生端按层级展开。 |
| map/branch/arousal-assoc | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-AROUSAL-ASSOC |  | 保留知识节点；原生端按层级展开。 |
| map/branch/arousal-cog | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-AROUSAL-COG |  | 保留知识节点；原生端按层级展开。 |
| map/branch/arousal-relax | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-AROUSAL-RELAX |  | 保留知识节点；原生端按层级展开。 |
| map/group/support | MAP_GROUP | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-GROUP-SUPPORT | KM-01; VA-CBTI-2014:治疗成分与顺序（睡眠限制 / 刺激控制 / 认知 / 放松）; AASM-2021:Table 3（CBT-I 的多组分构成）; VADOD-2025:CBT-I components 与 adaptation / delay 清单 | 知识地图为产品整理框架，必须展示其 provenance 标签。 |
| map/branch/support-hygiene | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-SUPPORT-HYGIENE |  | 保留知识节点；原生端按层级展开。 |
| map/branch/support-substance | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-SUPPORT-SUBSTANCE |  | 保留知识节点；原生端按层级展开。 |
| map/branch/support-screen | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-SUPPORT-SCREEN |  | 保留知识节点；原生端按层级展开。 |
| map/branch/support-diary | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-SUPPORT-DIARY |  | 保留知识节点；原生端按层级展开。 |
| map/group/outer | MAP_GROUP | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-GROUP-OUTER | KM-01; VA-CBTI-2014:治疗成分与顺序（睡眠限制 / 刺激控制 / 认知 / 放松）; AASM-2021:Table 3（CBT-I 的多组分构成）; VADOD-2025:CBT-I components 与 adaptation / delay 清单 | 知识地图为产品整理框架，必须展示其 provenance 标签。 |
| map/branch/outer-tired | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-OUTER-TIRED |  | 保留知识节点；原生端按层级展开。 |
| map/branch/outer-adapt | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-OUTER-ADAPT |  | 保留知识节点；原生端按层级展开。 |
| map/branch/outer-doctor | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-OUTER-DOCTOR |  | 保留知识节点；原生端按层级展开。 |
| map/branch/outer-med | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-OUTER-MED |  | 保留知识节点；原生端按层级展开。 |
| map/branch/outer-close | MAP_BRANCH | PUBLIC_WITH_PRESENTATION_ADAPTATION | MAP-BRANCH-OUTER-CLOSE |  | 保留知识节点；原生端按层级展开。 |
| vault/what_is_cbti | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-WHAT-IS-CBTI | M-02; AASM-2021:Table 3 Summary of interventions (CBT-I, Multicomponent) | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/first_line | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-FIRST-LINE | M-01; AASM-2021:Recommendation 1; VADOD-2025:Recommendation 5; CN-2023:失眠的心理行为治疗 p.569 | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/cbti_delivery | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-CBTI-DELIVERY | M-09; VADOD-2025:p.46 | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/trained_provider | KNOWLEDGE_ENTRY | BLOCKED | — | M-08; VADOD-2025:Recommendation 5 说明 p.47 | 依赖 M-08 的绝对交付资质表述，与 M-09 的数字/自助交付冲突。 |
| vault/single_component | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-SINGLE-COMPONENT | SR-15; VADOD-2025:Sidebar 3 p.30; AASM-2021:Recommendation 3; AASM-2021:Recommendation 4 | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/sleep_pressure | KNOWLEDGE_ENTRY | BLOCKED | — | MC-01; AASM-2021:Table 3 CBT-I 描述含 education about sleep regulation；睡眠限制机制（enhance sleep drive）间接支撑 | “今天更困不是坏事”过度绝对，可能掩盖危险嗜睡；需要重写才可发布。 |
| vault/circadian | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-CIRCADIAN | MC-02; AASM-2021:Table 3 CBT-I 描述：education about sleep regulation | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/arousal | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-AROUSAL | MC-03; AASM-2021:Table 3 中 other counter-arousal methods + 认知治疗中 performance anxiety about sleeping | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/se | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-SE | VA-06; VA-CBTI-2014:A2.6 Weekly review | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/sleep_diary | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-SLEEP-DIARY | SF-09; CN-2023:失眠的临床评估 · 主观评估 · 睡眠日记 | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/wearables | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-WEARABLES | SF-09; CN-2023:失眠的临床评估 · 主观评估 · 睡眠日记 | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/night_clock | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-NIGHT-CLOCK | CT-05; AASM-2021:由 CT-01 的 performance anxiety 与反觉醒方法描述支撑 | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/stimulus_control | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-STIMULUS-CONTROL | SC-01; AASM-2021:Recommendation 3; Table 3 Stimulus control | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/sc_how | KNOWLEDGE_ENTRY | FUTURE_TREATMENT_ONLY | — | SC-12; CN-2023:表 4 p.570 | 给出刺激控制的直接执行步骤。 |
| vault/wake_time | KNOWLEDGE_ENTRY | FUTURE_TREATMENT_ONLY | — | SC-05; AASM-2021:Table 3 Stimulus control (d) | 给出固定起床的直接执行要求。 |
| vault/nap | KNOWLEDGE_ENTRY | FUTURE_TREATMENT_ONLY | — | SC-06; AASM-2021:Table 3 Stimulus control (e) | 给出白天不补觉的直接治疗要求。 |
| vault/sc_trend | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-SC-TREND | SC-10; VADOD-2025:由 Rec 23 说明「对成分进行修改可降低潜在伤害」与 adapt 原则推导；属执行弹性表述，非新增疗法 | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/sc_safety | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-SC-SAFETY | SC-08; AASM-2021:Recommendation 3 段落; VADOD-2025:Sidebar 3 p.31; CN-2023:特殊人群 · 老年人群 | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/sleep_restriction | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-SLEEP-RESTRICTION | SR-01; AASM-2021:Table 3 Sleep restriction therapy | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/srt_baseline | KNOWLEDGE_ENTRY | FUTURE_TREATMENT_ONLY | — | VA-01; VA-CBTI-2014:A2.1 Initial time in bed | 包含治疗基线计算输入。 |
| vault/srt_review | KNOWLEDGE_ENTRY | FUTURE_TREATMENT_ONLY | — | VA-06; VA-CBTI-2014:A2.6 Weekly review | 包含周度治疗复盘与调整。 |
| vault/srt_initial_tib | KNOWLEDGE_ENTRY | FUTURE_TREATMENT_ONLY | — | VA-02; VA-CBTI-2014:A2.1 Initial time in bed | 包含初始卧床时间算法与 5 小时下限。 |
| vault/srt_weekly_table | KNOWLEDGE_ENTRY | FUTURE_TREATMENT_ONLY | — | VA-07; VA-CBTI-2014:A2.6 / Appendix Weekly adjustment | 包含窗口增减决策。 |
| vault/srt_checklist | KNOWLEDGE_ENTRY | FUTURE_TREATMENT_ONLY | — | VA-08; VA-CBTI-2014:A2.6 Weekly review | 属于治疗调整前的临床检查。 |
| vault/sleep_need | KNOWLEDGE_ENTRY | BLOCKED | — | VA-09; VA-CBTI-2014:Sleep Need Questionnaire（用途与评分区间） | 涉及 Sleep Need Questionnaire 的结构和评分用途，权利状态未解决。 |
| vault/srt_reduce_awake | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-SRT-REDUCE-AWAKE | SR-03; AASM-2021:由 SR-01 / SR-02 的机制表述推导（enhance sleep drive and consolidate sleep） | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/srt_safety | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-SRT-SAFETY | SR-07; AASM-2021:Recommendation 4 段落; VADOD-2025:Sidebar 3 p.31 | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/srt_early | KNOWLEDGE_ENTRY | FUTURE_TREATMENT_ONLY | — | SR-05; AASM-2021:Recommendation 4 段落 | 属于治疗启动后的反应管理。 |
| vault/srt_hard | KNOWLEDGE_ENTRY | FUTURE_TREATMENT_ONLY | — | SR-06; AASM-2021:Recommendation 4 段落 | 属于治疗执行与依从安排。 |
| vault/cognition | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-COGNITION | CT-01; AASM-2021:Table 3 Cognitive therapy | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/cognitive_tools | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-COGNITIVE-TOOLS | CT-02; AASM-2021:Table 3 Cognitive therapy | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/cognitive_accurate | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-COGNITIVE-ACCURATE | CT-04; AASM-2021:依据 CT-01 的机制；属于表达方式设计，不新增疗法 | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/cognitive_evidence | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-COGNITIVE-EVIDENCE | CT-03; AASM-2021:Table 3 NO RECOMMENDATIONS 区段 | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/relaxation | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-RELAXATION | RL-01; AASM-2021:Table 3 Relaxation therapy | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/relaxation_status | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-RELAXATION-STATUS | RL-02; AASM-2021:Recommendation 5 | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/hygiene_scope | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-HYGIENE-SCOPE | SH-01; AASM-2021:Table 3 Sleep hygiene | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/hygiene_not_alone | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-HYGIENE-NOT-ALONE | SH-09; VADOD-2025:Recommendation 7 说明 p.50 | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/hygiene_foundation | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-HYGIENE-FOUNDATION | SH-03; AASM-2021:Recommendation 6 Remarks | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/hygiene_cn | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-HYGIENE-CN | SH-10; CN-2023:表 4 p.570 + 表 4 注释（2） | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/evening_screen | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-EVENING-SCREEN | SH-07; CN-2023:倒班人群推荐条目 | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/screen_time | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-SCREEN-TIME | SH-05; AASM-2021:由 SH-01 睡眠卫生的构成维度（生活方式 + 环境）现实化展开；不新增疗法，属于判断训练 | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/safety | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-SAFETY | SF-12; VADOD-2025:Sidebar 3 p.31 | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/mood_seizure | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-MOOD-SEIZURE | SF-13; VADOD-2025:Recommendation 24 说明 p.29（2019） | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/delay_first | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-DELAY-FIRST | SF-14; VADOD-2025:p.29（2019） | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/fall_risk | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-FALL-RISK | SF-15; VADOD-2025:Sidebar 3 p.31; AASM-2021:Recommendation 3 段落; CN-2023:特殊人群 · 老年人群 | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/sleepy_driving | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-SLEEPY-DRIVING | SF-04; VADOD-2025:Recommendation 23 说明 pp.28-29（2019）; AASM-2021:Recommendation 4 段落 | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/daytime_sleepiness | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-DAYTIME-SLEEPINESS | SF-03; AASM-2021:Recommendation 4 段落; VADOD-2025:Sidebar 3 p.31 | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/when_to_seek_help | KNOWLEDGE_ENTRY | BLOCKED | — | SF-05; CN-2023:共病章节; VADOD-2025:Sidebar 3 p.31（adapt/delay 清单） | “不是再观察就行”对多类症状作统一绝对处置，需条目级安全复核。 |
| vault/diagnosis_basis | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-DIAGNOSIS-BASIS | SF-06; AASM-2021:Introduction | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/where_to_go | KNOWLEDGE_ENTRY | BLOCKED | — | SF-08; CN-2023:共病精神障碍章节 | “挂错了医生也会帮你转”是无法保证的服务结果。 |
| vault/comorbidity | KNOWLEDGE_ENTRY | BLOCKED | — | M-04; AASM-2021:Recommendation 1 remarks + 亚组 meta 分析; CN-2023:共病章节 | “通常不是不做 CBT-I 的理由”把不同共病过度合并。 |
| vault/cn_priority | KNOWLEDGE_ENTRY | BLOCKED | — | SR-12; CN-2023:共病章节 | 对多种共病统一使用“首选治疗”表述，需逐一核对适用范围。 |
| vault/tailored_talk | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-TAILORED-TALK | SF-16; VADOD-2025:2025 Patient's Guide p.2 | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/med_elderly | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-MED-ELDERLY | SF-11; CN-2023:特殊人群 · 老年人群 | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/med_osa | KNOWLEDGE_ENTRY | BLOCKED | — | SF-17; CN-2023:p.564 | 包含具体药物类别禁忌，超出 Public Education 的一般药物边界。 |
| vault/medication | KNOWLEDGE_ENTRY | BLOCKED | — | M-08; VADOD-2025:Recommendation 5 说明 p.47 | 依赖 M-08 的绝对交付资质表述，并把药物与方案合并。 |
| vault/goals | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-GOALS | MC-06; CN-2023:失眠的治疗 · 总体目标和干预策略（原文列 5 条） | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault/maintenance | KNOWLEDGE_ENTRY | PUBLIC_AS_IS | KNOWLEDGE-VAULT-MAINTENANCE | M-03; AASM-2021:Table 3 + Discussion; CN-2023:p.569 | 一般教育知识条目，保留来源与产品表述性质。 |
| vault/early_effects | KNOWLEDGE_ENTRY | PUBLIC_CONCEPT_ONLY | KNOWLEDGE-VAULT-EARLY-EFFECTS | M-05; AASM-2021:Recommendation 1 段落 | 保留概念、机制或安全 rationale；不作为个人执行指令。 |
| vault-blocked/MC-04 | KNOWLEDGE_ENTRY | BLOCKED | — |  | 三个登记来源中均无对应表述。按内容治理规则 2（unsupported = blocked）封禁，不用"看起来科学"的话填补。 |
| claim/M-01 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-M-01 | AASM-2021:Recommendation 1; VADOD-2025:Recommendation 5; CN-2023:失眠的心理行为治疗 p.569 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/M-02 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-M-02 | AASM-2021:Table 3 Summary of interventions (CBT-I, Multicomponent) | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/M-03 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-M-03 | AASM-2021:Table 3 + Discussion; CN-2023:p.569 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/M-04 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-M-04 | AASM-2021:Recommendation 1 remarks + 亚组 meta 分析; CN-2023:共病章节 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/M-05 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-M-05 | AASM-2021:Recommendation 1 段落 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/M-06 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-M-06 |  | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/SC-01 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SC-01 | AASM-2021:Recommendation 3; Table 3 Stimulus control | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SC-02 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | AASM-2021:Table 3 Stimulus control (a) | 刺激控制执行指令。 |
| claim/SC-03 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | AASM-2021:Table 3 Stimulus control (b) | 刺激控制执行指令。 |
| claim/SC-04 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | AASM-2021:Table 3 Stimulus control (c) | 刺激控制执行指令。 |
| claim/SC-05 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | AASM-2021:Table 3 Stimulus control (d) | 刺激控制执行指令。 |
| claim/SC-06 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | AASM-2021:Table 3 Stimulus control (e) | 刺激控制执行指令。 |
| claim/SC-07 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SC-07 | AASM-2021:Recommendation 3 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SC-08 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SC-08 | AASM-2021:Recommendation 3 段落; VADOD-2025:Sidebar 3 p.31; CN-2023:特殊人群 · 老年人群 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SC-09 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SC-09 | AASM-2021:由 SC-01 的目标与 SC-08 的安全调整要求推导；非新增疗法; VADOD-2025:Sidebar 3 的 adapt（适配）原则 p.31 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SC-10 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SC-10 | VADOD-2025:由 Rec 23 说明「对成分进行修改可降低潜在伤害」与 adapt 原则推导；属执行弹性表述，非新增疗法 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SR-01 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SR-01 | AASM-2021:Table 3 Sleep restriction therapy | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SR-02 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | AASM-2021:Table 3 Sleep restriction therapy | 睡眠限制调整逻辑。 |
| claim/SR-03 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SR-03 | AASM-2021:由 SR-01 / SR-02 的机制表述推导（enhance sleep drive and consolidate sleep） | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SR-04 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SR-04 | AASM-2021:Recommendation 4 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SR-05 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | AASM-2021:Recommendation 4 段落 | 治疗启动后的反应管理。 |
| claim/SR-06 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | AASM-2021:Recommendation 4 段落 | 治疗依从安排。 |
| claim/SR-07 | EVIDENCE_CLAIM | BLOCKED | — | AASM-2021:Recommendation 4 段落; VADOD-2025:Sidebar 3 p.31 | 把多类人群统一称为“可能禁忌”，超出 adapt/delay 的细分语义。 |
| claim/SR-08 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | AASM-2021:Recommendation 4 段落末句 | 治疗选择与随访。 |
| claim/SR-09 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — |  | 已废弃治疗实施条目。 |
| claim/SR-10 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | CN-2023:表 4 p.570 + 表 4 注释（4） | 已被替代的治疗定位条目。 |
| claim/SR-11 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SR-11 |  | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SR-12 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SR-12 | CN-2023:共病章节 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/SR-13 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SR-13 | CN-2023:特殊人群章节 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/CT-01 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-CT-01 | AASM-2021:Table 3 Cognitive therapy | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/CT-02 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-CT-02 | AASM-2021:Table 3 Cognitive therapy | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/CT-03 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-CT-03 | AASM-2021:Table 3 NO RECOMMENDATIONS 区段 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/CT-04 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-CT-04 | AASM-2021:依据 CT-01 的机制；属于表达方式设计，不新增疗法 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/CT-05 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-CT-05 | AASM-2021:由 CT-01 的 performance anxiety 与反觉醒方法描述支撑 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/CT-06 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-CT-06 |  | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SH-01 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SH-01 | AASM-2021:Table 3 Sleep hygiene | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/SH-02 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SH-02 | AASM-2021:Recommendation 6; VADOD-2025:Recommendation 7 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/SH-03 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SH-03 | AASM-2021:Recommendation 6 Remarks | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/SH-04 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SH-04 | AASM-2021:Recommendation 6 段落 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/SH-05 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SH-05 | AASM-2021:由 SH-01 睡眠卫生的构成维度（生活方式 + 环境）现实化展开；不新增疗法，属于判断训练 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/SH-06 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SH-06 | AASM-2021:与 CT-05 / 第 2 章觉醒机制一致 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/SH-07 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SH-07 | CN-2023:倒班人群推荐条目 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/SH-08 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SH-08 | CN-2023:失眠的治疗 · 总体目标和干预策略 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/RL-01 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-RL-01 | AASM-2021:Table 3 Relaxation therapy | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/RL-02 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-RL-02 | AASM-2021:Recommendation 5 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/RL-03 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-RL-03 |  | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SF-01 | EVIDENCE_CLAIM | BLOCKED | — | AASM-2021:Recommendation 4 段落（同 SR-07） | 重复 SR-07 的宽泛禁忌表述。 |
| claim/SF-02 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SF-02 | AASM-2021:Recommendation 3 段落（同 SC-08） | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/SF-03 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SF-03 | AASM-2021:Recommendation 4 段落; VADOD-2025:Sidebar 3 p.31 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SF-04 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SF-04 | VADOD-2025:Recommendation 23 说明 pp.28-29（2019）; AASM-2021:Recommendation 4 段落 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SF-05 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SF-05 | CN-2023:共病章节; VADOD-2025:Sidebar 3 p.31（adapt/delay 清单） | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SF-06 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SF-06 | AASM-2021:Introduction | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SF-07 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SF-07 |  | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SF-08 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SF-08 | CN-2023:共病精神障碍章节 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SF-09 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SF-09 | CN-2023:失眠的临床评估 · 主观评估 · 睡眠日记 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SF-10 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | CN-2023:睡眠日记章节; AASM-2021:Table 3 使用 sleep efficiency thresholds 概念 | 治疗指标公式。 |
| claim/SF-11 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SF-11 | CN-2023:特殊人群 · 老年人群 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/MC-01 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-MC-01 | AASM-2021:Table 3 CBT-I 描述含 education about sleep regulation；睡眠限制机制（enhance sleep drive）间接支撑 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/MC-02 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-MC-02 | AASM-2021:Table 3 CBT-I 描述：education about sleep regulation | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/MC-03 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-MC-03 | AASM-2021:Table 3 中 other counter-arousal methods + 认知治疗中 performance anxiety about sleeping | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/MC-04 | EVIDENCE_CLAIM | BLOCKED | — |  | V4 已标记无来源，禁止使用。 |
| claim/MC-05 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | CN-2023:睡眠日记章节（明确公式）; AASM-2021:Table 3 sleep efficiency thresholds | 治疗指标公式。 |
| claim/MC-06 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-MC-06 | CN-2023:失眠的治疗 · 总体目标和干预策略（原文列 5 条） | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/M-07 | EVIDENCE_CLAIM | BLOCKED | — | VADOD-2025:Recommendation 8; CN-2023:失眠的心理行为治疗 p.569 | “CBT-I 优于药物作为一线治疗”过度概括，未限定比较范围。 |
| claim/M-08 | EVIDENCE_CLAIM | BLOCKED | — | VADOD-2025:Recommendation 5 说明 p.47 | “必须由受训人员实施”与同一 V4 的数字/自助交付条目 M-09 冲突。 |
| claim/M-09 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-M-09 | VADOD-2025:p.46 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/SC-11 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SC-11 | VADOD-2025:患者沟通话术 p.132（2019 p.41 同）; VADOD-2025:Sidebar 3 p.30 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/SC-12 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | CN-2023:表 4 p.570 | 含 15–20 分钟等直接执行步骤。 |
| claim/SC-13 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SC-13 | CN-2023:表 4 注释（3） | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/SR-14 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | CN-2023:表 4 p.570; CN-2023:表 4 注释（4） | 睡眠限制适用范围及推荐级别。 |
| claim/SR-15 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SR-15 | VADOD-2025:Sidebar 3 p.30; AASM-2021:Recommendation 3; AASM-2021:Recommendation 4 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/SR-16 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | VADOD-2025:Recommendation 23 说明 pp.28-29（2019） | 治疗成分个体化修改。 |
| claim/SH-09 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SH-09 | VADOD-2025:Recommendation 7 说明 p.50 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/SH-10 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SH-10 | CN-2023:表 4 p.570 + 表 4 注释（2） | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/SF-12 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SF-12 | VADOD-2025:Sidebar 3 p.31 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SF-13 | EVIDENCE_CLAIM | BLOCKED | — | VADOD-2025:Recommendation 24 说明 p.29（2019） | 对双相与癫痫作机制性因果及避免判断，需独立医学复核。 |
| claim/SF-14 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | VADOD-2025:p.29（2019） | 治疗延迟决策。 |
| claim/SF-15 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SF-15 | VADOD-2025:Sidebar 3 p.31; AASM-2021:Recommendation 3 段落; CN-2023:特殊人群 · 老年人群 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SF-16 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-SF-16 | VADOD-2025:2025 Patient's Guide p.2 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/SF-17 | EVIDENCE_CLAIM | BLOCKED | — | CN-2023:p.564 | 具体药物类别禁忌不进入一般 Public Education。 |
| claim/SF-18 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-SF-18 | CN-2023:核对结果：未找到该表述。指南自身可用的相关表述见 p.569 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/VA-01 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | VA-CBTI-2014:A2.1 Initial time in bed | 治疗基线和 TST 输入。 |
| claim/VA-02 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | VA-CBTI-2014:A2.1 Initial time in bed | 初始窗口下限。 |
| claim/VA-03 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | VA-CBTI-2014:A2.2 Fixed rise time and time in bed window | 个人睡眠窗口公式。 |
| claim/VA-04 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | VA-CBTI-2014:A2.2 Fixed rise time and time in bed window | 最早上床执行规则。 |
| claim/VA-05 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | VA-CBTI-2014:A2.2 Fixed rise time and time in bed window | 分钟级离床规则。 |
| claim/VA-06 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | VA-CBTI-2014:A2.6 Weekly review | 周度治疗指标计算。 |
| claim/VA-07 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | VA-CBTI-2014:A2.6 / Appendix Weekly adjustment | 周度窗口调整算法。 |
| claim/VA-08 | EVIDENCE_CLAIM | FUTURE_TREATMENT_ONLY | — | VA-CBTI-2014:A2.6 Weekly review | 治疗调整前检查。 |
| claim/VA-09 | EVIDENCE_CLAIM | BLOCKED | — | VA-CBTI-2014:Sleep Need Questionnaire（用途与评分区间） | Sleep Need Questionnaire 权利状态未解决。 |
| claim/VA-10 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-VA-10 | VA-CBTI-2014:Therapist Manual（项目采用的操作主协议） | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/CI-01 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-CI-01 | VADOD-2025:Patient Summary / Sidebar（慢性失眠障碍的核心特征） | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/EX-01 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-EX-01 | SR-EXERCISE-INSOMNIA:Abstract / Results（physical exercise interventions on insomnia）; SR-EVENING-EXERCISE:Abstract / Results（evening high-intensity exercise, healthy adults） | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/EX-02 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-EX-02 | AASM-2021:Table 3 Sleep hygiene（lifestyle：diet, exercise, substance use）; AASM-2021:Sleep hygiene education（不作为慢性失眠的单独治疗） | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/CF-01 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-CF-01 | SR-CAFFEINE:Abstract / Results（caffeine and subsequent sleep）; AASM-2021:Table 3 Sleep hygiene（substance use：caffeine） | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/AL-01 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-AL-01 | SR-ALCOHOL:Abstract / Results（alcohol and subsequent sleep）; VA-CBTI-2014:Therapist Manual（睡眠卫生 / 物质使用相关章节） | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/AL-02 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-AL-02 | VADOD-2025:CBT-I adaptation / delay 清单（active alcohol or substance use disorder）; AASM-2021:Table 3 CBT-I components（酒精或物质使用障碍属于需要临床判断的情形） | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/NC-01 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-NC-01 | VA-CBTI-2014:Therapist Manual（睡眠卫生 / 尼古丁相关章节） | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/NC-02 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-NC-02 | VA-CBTI-2014:Therapist Manual（睡眠卫生 / 物质使用相关章节）; VADOD-2025:CBT-I adaptation / delay 清单（物质使用需要专业规划） | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/RL-04 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-RL-04 | AASM-2021:Table 3 Cognitive therapy; VA-CBTI-2014:Therapist Manual（认知与依从性相关章节）; AASM-2021:Table 3 Relaxation therapy | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/RL-05 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-RL-05 | AASM-2021:Table 3 Relaxation therapy（structured exercises：somatic tension 与 cognitive arousal）; VADOD-2025:CBT-I components（relaxation / arousal reduction）; VA-CBTI-2014:Therapist Manual（放松相关章节） | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/RL-06 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-RL-06 | AASM-2021:Table 3 Relaxation therapy（cognitive arousal 部分） | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/CT-07 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-CT-07 | AASM-2021:Table 3 Cognitive therapy; VA-CBTI-2014:Therapist Manual（认知与依从性相关章节） | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/VA-11 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-VA-11 | VA-CBTI-2014:Therapist Manual（认知与依从性相关章节） | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| claim/KM-01 | EVIDENCE_CLAIM | PUBLIC_AS_IS | CLAIM-KM-01 | VA-CBTI-2014:治疗成分与顺序（睡眠限制 / 刺激控制 / 认知 / 放松）; AASM-2021:Table 3（CBT-I 的多组分构成）; VADOD-2025:CBT-I components 与 adaptation / delay 清单 | 可作为 Education provenance claim；用户可见层仍需保留性质与适用范围。 |
| claim/DT-01 | EVIDENCE_CLAIM | PUBLIC_CONCEPT_ONLY | CLAIM-DT-01 | VA-CBTI-2014:A2.1 基线（至少一周的每日睡眠日记）; AASM-2021:Table 3（sleep diary 作为 CBT-I 的评估材料）; CN-2023:失眠的临床评估 · 主观评估 · 睡眠日记 | 只作为概念或边界依据，不转成个人判断或执行规则。 |
| source/AASM-2021 | SOURCE | PUBLIC_AS_IS | SOURCE-AASM-2021 |  | 来源元数据进入独立 source registry；不复制长篇原文或问卷。 |
| source/CN-2023 | SOURCE | PUBLIC_AS_IS | SOURCE-CN-2023 |  | 来源元数据进入独立 source registry；不复制长篇原文或问卷。 |
| source/VADOD-2025 | SOURCE | PUBLIC_AS_IS | SOURCE-VADOD-2025 |  | 来源元数据进入独立 source registry；不复制长篇原文或问卷。 |
| source/VA-CBTI-2014 | SOURCE | PUBLIC_AS_IS | SOURCE-VA-CBTI-2014 |  | 来源元数据进入独立 source registry；不复制长篇原文或问卷。 |
| source/SR-EXERCISE-INSOMNIA | SOURCE | PUBLIC_AS_IS | SOURCE-SR-EXERCISE-INSOMNIA |  | 来源元数据进入独立 source registry；不复制长篇原文或问卷。 |
| source/SR-EVENING-EXERCISE | SOURCE | PUBLIC_AS_IS | SOURCE-SR-EVENING-EXERCISE |  | 来源元数据进入独立 source registry；不复制长篇原文或问卷。 |
| source/SR-CAFFEINE | SOURCE | PUBLIC_AS_IS | SOURCE-SR-CAFFEINE |  | 来源元数据进入独立 source registry；不复制长篇原文或问卷。 |
| source/SR-ALCOHOL | SOURCE | PUBLIC_AS_IS | SOURCE-SR-ALCOHOL |  | 来源元数据进入独立 source registry；不复制长篇原文或问卷。 |
| special/framework_note | SPECIAL_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | BLOCK-FRAMEWORK-NOTE |  | 产品整理框架，原生端必须持续显示 provenance。 |
| special/framework_summary | SPECIAL_BLOCK | PUBLIC_CONCEPT_ONLY | BLOCK-FRAMEWORK-SUMMARY |  | 只解释 CBT-I 组成，不触发治疗。 |
| special/map_goal | SPECIAL_BLOCK | PUBLIC_CONCEPT_ONLY | BLOCK-MAP-GOAL |  | 一般教育目标，不作为疗效承诺。 |
| special/tool_boundary | SPECIAL_BLOCK | PUBLIC_AS_IS | BLOCK-TOOL-BOUNDARY |  | Public boundary。 |
| special/cbti_boundary | SPECIAL_BLOCK | PUBLIC_AS_IS | BLOCK-CBTI-BOUNDARY |  | 一般性边界和效果差异说明。 |
| special/what_it_is_not | SPECIAL_BLOCK | PUBLIC_AS_IS | BLOCK-WHAT-IT-IS-NOT |  | 明确排除诊断、药物和个人处方。 |
| special/diary_boundary | SPECIAL_BLOCK | PUBLIC_AS_IS | BLOCK-DIARY-BOUNDARY |  | 睡眠日记教育边界。 |
| special/source_transparency | SPECIAL_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | BLOCK-SOURCE-TRANSPARENCY |  | 原生来源页按来源角色展开。 |
| special/source_role_note | SPECIAL_BLOCK | PUBLIC_AS_IS | BLOCK-SOURCE-ROLE-NOTE |  | 来源职责分离。 |
| special/audience_notice | SPECIAL_BLOCK | PUBLIC_AS_IS | BLOCK-AUDIENCE-NOTICE |  | 成人一般教育受众声明。 |
| special/home_intro | SPECIAL_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | BLOCK-HOME-INTRO |  | Web 首页文案只作为 Education 入口候选，不修改当前导航。 |
| special/home_more | SPECIAL_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | BLOCK-HOME-MORE |  | Web 首页补充内容改为原生 Education 介绍。 |
| special/privacy_notice | SPECIAL_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | BLOCK-PRIVACY-NOTICE |  | 浏览器存储措辞需改成本机 App/Room 语境，P1.5 不创建持久化。 |
| special/review_status_v2 | SPECIAL_BLOCK | BLOCKED | — |  | 同时拼接“已核对”与“尚未完成独立临床复核”，发布状态语义矛盾。 |
| special/summary_copy | SPECIAL_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | BLOCK-SUMMARY-COPY |  | 保留学习总结与导出概念，具体持久化和 SAF 在 P2 以后实现。 |
| special/storage_risk | SPECIAL_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | BLOCK-STORAGE-RISK |  | 浏览器缓存风险需改为 App 本地数据库与用户主动导出语境。 |
| special/save_help | SPECIAL_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | BLOCK-SAVE-HELP |  | 保留 Preview → Commit 说明；浏览器措辞改为本地 App。 |
| special/activity_profile_note | SPECIAL_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | BLOCK-ACTIVITY-PROFILE-NOTE |  | 保留主观判断边界，不作医学解释。 |
| special/review_copy | SPECIAL_BLOCK | PUBLIC_WITH_PRESENTATION_ADAPTATION | BLOCK-REVIEW-COPY |  | 保留复习/书签概念，不保留自动间隔调度。 |
| special/diary_template_copy | SPECIAL_BLOCK | PUBLIC_CONCEPT_ONLY | BLOCK-DIARY-TEMPLATE-COPY |  | 保留日记为何估计即可的教育说明。 |
| special/diary_template_fields | SPECIAL_BLOCK | FUTURE_TREATMENT_ONLY | — |  | 包含 TST、SE、午休和用药等超出当前 Diary schema 的字段，不能并入 Public 日记。 |
| special/diary_template_notes | SPECIAL_BLOCK | PUBLIC_CONCEPT_ONLY | BLOCK-DIARY-TEMPLATE-NOTES |  | 保留模板是学习示例的边界，不写入 Diary。 |
| special/ai_prompt | SPECIAL_BLOCK | BLOCKED | — |  | 要求外部 AI 做医疗/冲突分类，且把“正在使用酒精或药物”过宽地作为停止分析条件；Public Edition 禁止 AI 医疗建议。 |
| records/progress | USER_STATE_TYPE | PUBLIC_WITH_PRESENTATION_ADAPTATION | — |  | P2 durable：用户明确保存的 IN_PROGRESS/COMPLETED。 |
| records/current-position | USER_STATE_TYPE | PUBLIC_WITH_PRESENTATION_ADAPTATION | — |  | 仅导航便利状态，可丢失，不代表学习完成。 |
| records/committed-choices | USER_STATE_TYPE | PUBLIC_WITH_PRESENTATION_ADAPTATION | — |  | durable：只保存用户明确 Commit 的教育选择；Preview 不持久化。 |
| records/review-bookmark | USER_STATE_TYPE | PUBLIC_WITH_PRESENTATION_ADAPTATION | — |  | durable：用户明确收藏或标记复习；不自动调度。 |
| records/user-authored | USER_STATE_TYPE | PUBLIC_WITH_PRESENTATION_ADAPTATION | — |  | durable personal data：用户明确保存的文字。 |
| records/cognitive-reflection | USER_STATE_TYPE | PUBLIC_WITH_PRESENTATION_ADAPTATION | — |  | durable personal data：五步教育练习中的用户原文，不自动解释。 |
| records/telemetry | USER_STATE_TYPE | BLOCKED | — |  | Public Edition 不收集非必要 telemetry。 |
| export/education-json | EXPORT_ELEMENT | PUBLIC_WITH_PRESENTATION_ADAPTATION | EXPORT_ELEMENT-EXPORT-EDUCATION-JSON |  | 允许用户主动导出 Education 数据；P1.5 不实现。 |
| export/summary-image | EXPORT_ELEMENT | PUBLIC_WITH_PRESENTATION_ADAPTATION | EXPORT_ELEMENT-EXPORT-SUMMARY-IMAGE |  | 仅用户主动生成；字段范围须在 P2/P3 明确。 |
| export/ai-knowledge-pack | EXPORT_ELEMENT | BLOCKED | — |  | 运行时 SOURCES 与内嵌 mapping 在全部 8 个来源上存在字段漂移，且包会包含治疗阈值/规则。Public Edition 禁止 AI 医疗建议。 |
| export/ai-prompt | EXPORT_ELEMENT | BLOCKED | — |  | 要求外部 AI 进行医疗分类，且安全触发措辞过宽。 |

## Known unresolved issues

- `export/ai-knowledge-pack`：V4 的 claim 来自内嵌 `cbti-mapping`，导出器却序列化运行时 `SOURCES`；8 个来源均存在字段或文字漂移。它还会打包治疗阈值。Public manifest 不包含该导出。
- `special/ai_prompt` / `export/ai-prompt`：把“正在使用酒精或药物”笼统地当作停止分析信号，范围宽于“活动性酒精或物质使用障碍”；同时要求外部 AI 作医疗分类。
- `vault/sleep_pressure`：`今天更困不是坏事` 会把危险嗜睡也一并正常化。
- `scene/ch07/high_risk`、`scene/ch04/safety`、claims `SR-07` / `SF-01`：把高风险职业、双相、癫痫和日间嗜睡压成统一“禁忌/必须评估”措辞；V4 自身注明相关依据未纳入指南系统评价。
- `scene/ch08/signals`：一个 checklist 混合呼吸、神经、精神、自伤、药物和治疗延迟信息，缺少条目级来源关系与冻结的急性安全文案。
- `VA-09` / `scene/ch04/sleep_need` / `vault/sleep_need`：Sleep Need Questionnaire 权利状态未解决，未复制题目，也未进入 Public manifest。

## Source rights and release gate

This inventory maps V4's embedded source IDs and claim references; source metadata is provenance, not permission to reproduce source wording. The candidate manifest contains project-authored presentation text and bibliographic references, not copied questionnaire items or long-form source passages. No blanket permission was verified for redistribution of V4 or adaptations derived from its source material.

Before public distribution, review the applicable rights and reuse terms for every source and the V4 material itself. The AASM/JCSM page includes a permissions route ([AASM/JCSM guideline](https://doi.org/10.5664/jcsm.8986)); the Chinese guideline publisher page indicates access restrictions and a CCC permissions route ([Chinese guideline publisher page](https://rs.yiigle.com/cmaid/1512022)); and VA/DoD describes its guideline as supporting clinician judgment ([VA/DoD 2025 guideline](https://www.healthquality.va.gov/guidelines/CD/insomnia/)). These pages support source identification and scope review, not a conclusion that reuse is permitted or that the content is an app treatment rule.

Consequently, the 462-node manifest is an internal candidate, not a release-cleared content package. Questionnaire text/score/functional use, AI prompts and knowledge packs, and the blocked medical/safety composites remain excluded. Resolve each `BLOCKED` item only through its recorded review; do not silently reinterpret it as approved.
