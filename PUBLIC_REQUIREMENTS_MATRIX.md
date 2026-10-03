# SleepDiary Public Edition v1.0 — Requirements Matrix

状态：P0 FROZEN
说明：`EXISTING` 表示当前基线已有主要能力；`PARTIAL` 表示可复用但需 Public Core 整合；`PLANNED` 表示后续实现；`BLOCKED` 表示发布前必须解决外部或内容问题；`FROZEN` 表示边界决定已确定但不代表实现完成。P0 状态不是测试通过声明。

## Functional requirements

| ID | Requirement | Acceptance evidence | P0 status |
|---|---|---|---|
| PUB-F-001 | 单 Activity、四个顶级区域 Today/Sleep/Education/Me | 导航测试及真机 back stack | PLANNED |
| PUB-F-002 | 首次三页说明仅完成一次 | Room/App 重启测试 | PARTIAL |
| PUB-F-003 | 首次启动不创建 TreatmentEpisode | DB 前后对比 | PLANNED |
| PUB-F-004 | 再次启动直接进入 Today | onboarding 状态测试 | PARTIAL |
| PUB-F-005 | Today 支持新建、查看和修改当天日记 | 现有保存/编辑回归 | EXISTING |
| PUB-F-006 | Sleep 支持历史查看和编辑 | UI/Repository 回归 | EXISTING |
| PUB-F-007 | Sleep 显示 WEEKLY_1.0 中性周总结 | 周期、revision 测试 | EXISTING |
| PUB-F-008 | Sleep 显示最近 7/14 天中性实时平均 | 日期窗口/缺失日测试 | EXISTING |
| PUB-F-009 | Education 显示版本化内容目录和详情 | registry/UI 测试 | PLANNED |
| PUB-F-010 | 用户明确保存 Education progress | 无隐式写入及持久化测试 | PLANNED |
| PUB-F-011 | 用户明确保存/取消稍后复习 | review state 测试 | PLANNED |
| PUB-F-012 | Education note 关联 WorkspaceRecord | 事务、外键、编辑测试 | PLANNED |
| PUB-F-013 | Me 提供个人记录和数据管理 | 导航/UI 测试 | PARTIAL |
| PUB-F-014 | 不显示 Treatment 入口或占位标签 | 全导航扫描 | PLANNED |
| PUB-F-015 | Public Edition 不生成个性化建议 | 内容和生产调用审计 | PLANNED |
| PUB-F-016 | Public 首次启动不自动进入现有 Safety 问询 | onboarding/navigation test | PLANNED |

## Neutral-statistics requirements

| ID | Requirement | Acceptance evidence | P0 status |
|---|---|---|---|
| PUB-S-001 | 缺失日期不按 0 计算 | Calculator tests | EXISTING |
| PUB-S-002 | TIB 明确不等于睡眠时长 | UI 文案测试 | EXISTING |
| PUB-S-003 | 不计算/展示治疗 TST 或 SE | UI/domain negative tests | EXISTING |
| PUB-S-004 | 不显示好坏、正常异常、改善率或趋势评级 | 全页面文本/语义检查 | PARTIAL |
| PUB-S-005 | 原始 sleepQuality 可中性显示但不转为 App 分数 | UI/模型检查 | PARTIAL |
| PUB-S-006 | 统计只读，不覆盖原始日记 | Repository/DB 对比 | EXISTING |

## Education requirements

| ID | Requirement | Acceptance evidence | P0 status |
|---|---|---|---|
| PUB-E-001 | 每项内容有稳定 contentId 和 contentVersionId | registry validation | PLANNED |
| PUB-E-002 | 每项内容有来源角色、版次和精确 locator | source completeness test | PARTIAL |
| PUB-E-003 | 中文内容标识为项目独立表述 | UI/content snapshot | PARTIAL |
| PUB-E-004 | 打开/滚动内容不写 progress | DB before/after test | PLANNED |
| PUB-E-005 | progress 只由明确操作写入 | ViewModel/repository test | PLANNED |
| PUB-E-006 | 不记录点击、耗时、连续天数或评分 | schema/code audit | PLANNED |
| PUB-E-007 | notes 不被自动分析或分类为医疗事实 | dependency/behavior test | PLANNED |
| PUB-E-008 | bookmark 不产生后台提醒或自动推荐 | WorkManager/manifest audit | PLANNED |
| PUB-E-009 | Education 不读个人日记做定制 | dependency test | PLANNED |
| PUB-E-010 | Education 不写 Treatment 数据 | transaction before/after test | PLANNED |
| PUB-E-011 | 内容来源和许可满足公开发布 | 内容审查记录 | BLOCKED |

## Personal-data and privacy requirements

| ID | Requirement | Acceptance evidence | P0 status |
|---|---|---|---|
| PUB-P-001 | 默认数据只存应用私有 Room | storage/manifest audit | EXISTING |
| PUB-P-002 | 无账号、广告、分析、遥测或云服务 | dependency/manifest audit | EXISTING |
| PUB-P-003 | 无 INTERNET/ACCESS_NETWORK_STATE/MANAGE_EXTERNAL_STORAGE | merged Manifest test | EXISTING |
| PUB-P-004 | 用户自由文本视为敏感数据 | export/privacy tests | PARTIAL |
| PUB-P-005 | Diary PNG 不含 comment、Safety、Education notes | image model tests | EXISTING |
| PUB-P-006 | Diary CSV 不自动增加 Education/Treatment 字段 | CSV header test | EXISTING |
| PUB-P-007 | 官方链接只由用户主动交给系统浏览器 | intent whitelist test | EXISTING |
| PUB-P-008 | 阅读行为不形成隐式行为画像 | schema and code audit | PLANNED |
| PUB-P-009 | 导出取消或失败不显示成功 | SAF flow tests | EXISTING |

## Data requirements

| ID | Requirement | Acceptance evidence | P0 status |
|---|---|---|---|
| PUB-D-001 | Diary/Safety/Weekly 现有 schema 语义不变 | schema/hash regression | EXISTING |
| PUB-D-002 | 新增 EDUCATION_DATA_1.0 独立版本 | version constants/test | PLANNED |
| PUB-D-003 | Room 6→7 仅新增 Education 表 | migration schema diff | PLANNED |
| PUB-D-004 | 旧十表数据全部保留 | migration fixture test | PLANNED |
| PUB-D-005 | 旧学习笔记不自动关联内容 | migration/data test | PLANNED |
| PUB-D-006 | EducationRecord 正文只保存在 WorkspaceRecord | schema/repository test | PLANNED |
| PUB-D-007 | progress 按 contentId+version 保存 | unique key test | PLANNED |
| PUB-D-008 | 内容正文来自不可变 registry，不复制进 Room | schema/content test | PLANNED |
| PUB-D-009 | 所有持久时间和版本可校验 | validation tests | PLANNED |
| PUB-D-010 | Room schema 7 导出并提交 | exported schema check | PLANNED |

## Backup and export requirements

| ID | Requirement | Acceptance evidence | P0 status |
|---|---|---|---|
| PUB-B-001 | BACKUP_3.0 历史 schema/样例不可改写 | hash regression | EXISTING |
| PUB-B-002 | 新完整备份使用 BACKUP_3.1 | exact version/schema test | PLANNED |
| PUB-B-003 | 兼容导入 1.0/1.1/2.0/2.1/3.0/3.1 | parameterized restore tests | PLANNED |
| PUB-B-004 | 旧格式恢复 Education 集合为空 | legacy restore tests | PLANNED |
| PUB-B-005 | Education 与其他业务数据单事务替换 | injected-failure rollback tests | PLANNED |
| PUB-B-006 | EducationRecord 引用 WorkspaceRecord 完整校验 | corrupt-reference tests | PLANNED |
| PUB-B-007 | 提交后完整回读并比对 | repository tests | PARTIAL |
| PUB-B-008 | 恢复不授予 Treatment 执行能力 | LocalExecutionState tests | PARTIAL |
| PUB-B-009 | 恢复预览显示 Education 和休眠 Treatment 数量 | UI flow tests | PLANNED |
| PUB-B-010 | Education export 与完整恢复格式分离 | codec/SAF tests | PLANNED |
| PUB-B-011 | Education export 不含 Diary/Safety/Treatment | exact-field tests | PLANNED |

## Treatment-disabled requirements

| ID | Requirement | Acceptance evidence | P0 status |
|---|---|---|---|
| PUB-T-001 | CBT-I Protocol 固定为 NONE | build/version test | EXISTING |
| PUB-T-002 | Public navigation 无 Treatment route | navigation graph test | PLANNED |
| PUB-T-003 | Public AppServices 不向 UI 暴露 controller | compile/dependency test | PLANNED |
| PUB-T-004 | 不创建 TreatmentMetricSnapshot 或 TreatmentPlan | schema/code audit | EXISTING |
| PUB-T-005 | Education/Diary 操作不改变 episode/audit | DB before/after tests | PLANNED |
| PUB-T-006 | 医学 transition 保持 MEDICAL_RULE_BLOCKED | controller regression | EXISTING |
| PUB-T-007 | 备份保留休眠历史但不能激活 | restore/activation tests | PARTIAL |
| PUB-T-008 | 未来 Treatment 需要独立批准、规格和能力装配 | release review checklist | FROZEN |

## Navigation and accessibility requirements

| ID | Requirement | Acceptance evidence | P0 status |
|---|---|---|---|
| PUB-U-001 | 四个顶级入口有稳定返回路径 | navigation tests | PLANNED |
| PUB-U-002 | onboarding/form/restore 流程不会误切换丢草稿 | recreation/back tests | PLANNED |
| PUB-U-003 | 主要触摸目标至少 48dp | Compose semantics tests | PARTIAL |
| PUB-U-004 | 字体放大和 TalkBack 可操作 | UI tests + S24 | PARTIAL |
| PUB-U-005 | 不使用连续打卡、奖励或失败感文案 | UI/content audit | PARTIAL |
| PUB-U-006 | Education 状态不只靠颜色表达 | semantics tests | PLANNED |

## Release and compatibility requirements

| ID | Requirement | Acceptance evidence | P0 status |
|---|---|---|---|
| PUB-C-001 | Public Edition 目标 App version 为 1.0.0 | release config check | PLANNED |
| PUB-C-002 | 当前 0.2.2 用户可无损升级 | Room/backup/device test | PLANNED |
| PUB-C-003 | 不删除休眠 Treatment foundation 数据 | migration/backup diff | FROZEN |
| PUB-C-004 | 根规格与实际 BACKUP_3.x 状态统一 | manifest/spec verification | PLANNED |
| PUB-C-005 | 自动化、迁移、离线和 S24 真机验收通过 | final release report | PLANNED |
| PUB-C-006 | 未完成内容许可或安全复核不得发布对应内容 | release gate | BLOCKED |
| PUB-C-007 | `meditation/` 独立项目不得进入构建或提交 | settings/Git audit | EXISTING |

## P0 exit criteria

P0 完成条件：

- 四份 Public Edition P0 文档建立且边界一致；
- Public Core 取代原 Treatment Phase 2 作为下一执行路线；
- Education 与 Treatment 的依赖禁止项明确；
- Room 7/BACKUP_3.1 只作为后续实现目标，本阶段不落库；
- 未解决内容项单独列出，不用代码默认值替代。

## P1.5 V4 education coverage checkpoint

以下是 P1.5 静态内容基础验收项；这些状态不表示 Public release 完成。逐单元分类和理由见 `V4_PUBLIC_CONTENT_MATRIX.md`。

| ID | Requirement | Acceptance evidence | P1.5 status |
|---|---|---|---|
| PUB-E15-001 | V4 每个逻辑教育单元均进入可追踪 inventory，并带稳定来源定位与分类 | V4 matrix completeness/count tests | PASS |
| PUB-E15-002 | 候选 manifest 保留课程、知识地图、章节、场景和内容子节点，不扁平为少量长文 | schema and manifest hierarchy tests | PASS |
| PUB-E15-003 | 静态 schema 表达分层解释、五步认知练习、情境、记录提示和复习概念 | model/schema tests | PASS |
| PUB-E15-004 | 选择内容严格分开 transient Preview 与明确 Commit 合同 | candidate manifest tests | PASS |
| PUB-E15-005 | 候选内容通过 ID、引用、完整性、hash 和治疗边界自动校验 | Education unit tests | PASS |
| PUB-E15-006 | 未解决许可、量表权利、医疗边界和 AI 越界内容清楚标为 BLOCKED 并排除于 manifest | matrix + negative manifest tests | PASS |
| PUB-E15-007 | Registry 仍是静态内容；不引入 Room、用户进度持久化、导航或 Treatment | diff and dependency audit | PASS |
| PUB-E15-008 | V4 及其来源的公开复用授权完成逐项核实 | written rights/reuse review | BLOCKED |

P1.5 的 `PUBLIC_AS_IS` / `PUBLIC_WITH_PRESENTATION_ADAPTATION` / `PUBLIC_CONCEPT_ONLY` 表示内容适配候选类别，不是许可结论。来源核准前不得将候选 manifest 视作公开发布包。

## P2A Education user-data contract checkpoint

P2A means contract frozen; it does not mean Room/backup implementation has passed.

| ID | Requirement | Acceptance evidence | P2A status |
|---|---|---|---|
| PUB-E2A-001 | Content versions/body remain immutable packaged static assets; no Room copy | registry/data model boundary review | FROZEN |
| PUB-E2A-002 | Progress is explicit and version-bound; clear milestone action may commit completion; no implicit completion | Education user-data contract | FROZEN |
| PUB-E2A-003 | V4 Preview is transient; explicit Commit stores one stable option per prompt/version | manifest-to-row mapping contract | FROZEN |
| PUB-E2A-004 | Review/bookmark state is user controlled and has no due scheduling | state/deletion contract | FROZEN |
| PUB-E2A-005 | EducationRecord links WorkspaceRecord one-to-one without duplicating text; legacy notes remain unassociated | FK, uniqueness and legacy semantics | FROZEN |
| PUB-E2A-006 | Resume uses one stable position pointer per course/version, distinct from progress | position contract | FROZEN |
| PUB-E2A-007 | Dwell, click, scroll, trail, score, streak and inferred interest telemetry are not collected | data-boundary contract | FROZEN |
| PUB-E2A-008 | EducationExport_1.0 contains user Education data, not course body or other domains | exact fields and exclusions | FROZEN |
| PUB-E2A-009 | BACKUP_3.1 preserves BACKUP_3.0 and carries all durable Education state atomically | version/migration/restore contract | FROZEN |
| PUB-E2A-010 | Room 6→7 adds exactly five Education tables and preserves existing user state | migration contract | FROZEN |
| PUB-E2A-011 | Pre-Education backups restore Education state empty without inference | compatibility contract | FROZEN |

The frozen fields, keys, transition/deletion behavior, content-version evolution, migration and test contract are in `EDUCATION_USER_DATA_CONTRACT.md`.

## P2B implementation acceptance

| ID | Requirement | Acceptance evidence | P2B status |
|---|---|---|---|
| PUB-D-002–010 | Education schema versioning, Room 6→7, five tables, retained prior state, explicit repository operations, schema export | `MigrationTest`, `EducationPersistenceTest`, exported Room schema 7 | PASS |
| PUB-B-002–006 | BACKUP_3.1 export, legacy import, empty Education state for old formats, atomic restore and reference validation | `EducationPersistenceTest`, `BackupTest`, Room transaction rollback tests | PASS |
| PUB-B-007 | Restore commit followed by database readback | restore repository and reliability tests | PASS |
| PUB-B-008 | Restore does not activate Treatment | backup compatibility and isolation tests; CBT-I Protocol remains NONE | PASS |
| PUB-B-009 | Restore preview reports Education counts and legacy-format behavior | implementation updated; UI-level acceptance remains for P3 | PARTIAL |
| PUB-B-010 | Education export follows EDUCATION_EXPORT_1.0 schema and excludes other domains/course body | `EducationPersistenceTest` schema, determinism and privacy checks | PASS |

P2B unit tests: 428 passed, zero failures/errors/skips. `assembleDebug` passed. This does not include connected-device validation or the P3 user-facing Education/navigation experience.
