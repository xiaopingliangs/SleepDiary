# SleepDiary Public Edition v1.0 — P2A Education User-Data Contract

Status: **FROZEN — P2A CONTRACT**
Contract revision: `EDUCATION_DATA_1.0`
Current baseline: Room 6 / `BACKUP_3.0`; P2B target: Room 7 / `BACKUP_3.1`
CBT-I Protocol: `NONE`

This contract is derived from `EDUCATION_CONTENT_SCHEMA_1.1`, `public_education_manifest_1_1.json`, the P1.5 V4 inventory, the existing `WorkspaceRecord` entity and the actual Room 6 / `BACKUP_3.0` implementation. It freezes storage and export semantics only. It does not add Public navigation, education UI, treatment, diagnosis, or medical rules.

## 1. Three data categories

### A. Static product content

The immutable packaged registry owns `EducationContentVersion`, chapters, scenes, knowledge maps, choices, previews, explanations, sources, evidence metadata, stable labels and content hashes. This material is not user state and is not copied into Room. `EducationContentVersion` is a version descriptor in the packaged registry; it is not a Room table.

Every user-state reference includes `contentVersionId` and stable content IDs. P2B must retain historical registry descriptors/manifests needed to interpret stored references. It must not reuse a stable ID for changed meaning. Text/body from the course must not be copied to user tables or exported with user records.

The P1.5 candidate schema's `commit.persistedAs = "EducationRecord"` is a **logical Education-record intent**, not a SQL table name. Physical mapping is kind-specific: a committed `CHOICE` maps to `education_committed_choice`; authored text maps to `workspace_record` plus `education_record`. A choice does not create an empty WorkspaceRecord or EducationRecord association. This preserves the candidate manifest without conflating selected options and user-authored text.

### B. Durable user data

P2B adds exactly five Room tables. Every row carries `educationDataSchemaVersion = EDUCATION_DATA_1.0`. Content references are checked against the exact packaged registry version and node relationship; no foreign key points from Room into an asset.

| V4 user-state concept | Durable owner | Key / uniqueness | Meaning |
|---|---|---|---|
| `records/progress` | `education_progress` | `(contentVersionId, contentId)` | Explicit user progress only |
| `records/committed-choices` | `education_committed_choice` | `(contentVersionId, promptContentId)` | The one currently committed stable option for a prompt |
| `records/review-bookmark` | `education_review_state` | `(contentVersionId, contentId)` | User-controlled saved-for-review/reviewed state |
| `records/user-authored`, `records/cognitive-reflection` | `workspace_record` + `education_record` | `education_record.workspaceRecordId` is unique and references `workspace_record.id` | User text lives once in WorkspaceRecord; EducationRecord links it to context |
| `records/current-position` | `education_position` | `(contentVersionId, courseId)` | One resume pointer to a stable scene/content ID |
| `records/telemetry` | none | — | Blocked; never collected |

`EducationContentVersion` and `EducationExport` are deliberately not additional user tables. Export is a temporary projection. There is no generic response history, analyzer profile, simulator result, click trail or treatment state in Education persistence: V4's static simulator/analyzer/checklist content does not require durable personal outputs; only explicitly committed CHOICE nodes, explicit progress, notes and a single resume pointer are retained.

## 2. Durable row contracts

### `education_progress`

Fields: `contentVersionId`, `contentId`, `state`, `updatedAtUtc`, nullable `completedAtUtc`, `educationDataSchemaVersion`.

`state` is `IN_PROGRESS` or `COMPLETED`; absence means not started. Opening, reading, scrolling, previewing, or moving the resume pointer does not create or complete progress. An explicit user action with clear learning-milestone semantics may create/update progress; a separate “save progress” button is not required. `COMPLETED` requires an explicit completion action provided by the content flow; it is never inferred from time, scroll, preview, committed choice, or automatic navigation. `COMPLETED` iff `completedAtUtc` is non-null. Explicit reopen changes state to `IN_PROGRESS` and clears `completedAtUtc`. Reset progress deletes only these rows.

“下一页 / 完成本节并继续” may complete the current unit in the same action when its UI semantics clearly say the user is completing that unit. A plain “Next” that only navigates updates the resume pointer and does not complete progress. Automatic scene advance never completes progress.

### `education_committed_choice`

Fields: `contentVersionId`, `promptContentId`, `choiceContentId`, `optionId`, `committedAtUtc`, `updatedAtUtc`, `educationDataSchemaVersion`.

For the P1.5 manifest, each choice node's `contentId` is the stable `choiceContentId`; its `parentContentId` is the `promptContentId`; `payload.choiceId` is `optionId`; and `payload.commit.commitValueId` must equal `choiceContentId`. The referenced node must be kind `CHOICE`, belong to the referenced prompt, and carry the matching stable option ID in that content version.

Preview state exists only in UI memory and is discarded on exit/recreation unless separately restored as transient UI state; it is never included in Room, backup or Education export. Only explicit confirmation commits. A prompt has at most one committed choice per content version. Re-confirming the same option is idempotent. Selecting a different option requires explicit confirmation and replaces the current row, updating `updatedAtUtc`; there is no implied answer history. Explicit clear deletes that prompt's current committed row. Choices never affect Treatment, recommendations, progress completion or medical state.

### `education_review_state`

Fields: `contentVersionId`, `contentId`, `reviewState`, `createdAtUtc`, `updatedAtUtc`, nullable `reviewedAtUtc`, `educationDataSchemaVersion`.

States are `SAVED_FOR_REVIEW` and `REVIEWED`. No row means not saved. Save, mark reviewed, save again, and remove are explicit actions. Removing deletes the row. There is no due date, spaced schedule, push notification, streak or automatic selection. “Reviewed” means the user says they revisited educational content; it is not clinician review or comprehension.

### `education_record` and `workspace_record`

`education_record` fields: `workspaceRecordId` (primary key and foreign key to `workspace_record.id`, `ON DELETE CASCADE`), `contentVersionId`, `contentId`, nullable `promptId`, `recordKind`, `createdAtUtc`, `educationDataSchemaVersion`.

`recordKind` is `NOTE`, `REFLECTION`, `QUESTION_FOR_CLINICIAN`, or `PERSONAL_REMINDER`. `contentId` identifies the educational context; `promptId`, when present, must identify a stable note/reflection prompt in that version. Actual title/body and their revision remain exclusively in `WorkspaceRecord`; EducationRecord contains no copied text. Create/update/link/delete operations that touch both tables run transactionally and read back the relationship.

Legacy WorkspaceRecord rows remain unassociated. No association is inferred from kind, title, body or creation time. Removing only an Education association preserves the WorkspaceRecord as a general personal record. Explicitly deleting its WorkspaceRecord body removes the association by FK cascade in the same transaction. A reset of progress or bookmarks does not delete notes. Deleting an Education note from the Education feature must make the user choose whether to remove only its Education link or also delete the personal text; no implicit cross-delete.

### `education_position`

Fields: `contentVersionId`, `courseId`, `sceneContentId`, `updatedAtUtc`, `educationDataSchemaVersion`.

At most one current pointer exists per course and content version. Entering/navigating to a stable scene may replace the pointer; it stores no trail, elapsed time, scroll percentage, previewed answer or click count. The pointer is a convenience only, never progress, completion, belief or recommendation. Resetting progress does not remove it. A separate explicit clear-position action deletes the pointer. It is excluded from `EDUCATION_EXPORT_1.0` as low-value navigation state, but is included in complete backup because backup restores all durable state.

## 3. Content-version evolution

- Persist exact `contentVersionId` with every progress, choice, review, record association and position.
- Keep each released registry version's descriptor, stable IDs, choice/prompt relationships and display labels in immutable packaged assets. P1.5 candidate content remains a release candidate, and its unresolved licensing gate remains independent of generic persistence implementation.
- A corrected explanation with the same meaning creates a new immutable content version; existing user state stays on the old version. A changed meaning, prompt or choice creates a new stable ID. Never silently rebind an ID or copy a committed choice/progress/review state to a new version.
- New versions start with empty state unless the user explicitly acts in that version. There is no automatic migration or “equivalent choice” mapping in P2.
- Backup descriptors must exactly match an installed/archived immutable registry descriptor (including version ID and manifest hash). Unknown/mismatched descriptors are rejected before database mutation; do not resolve by matching title or reusing IDs.
- User-authored WorkspaceRecord text remains readable independently of whether a content version is currently the default. Its historical association is retained with its original version reference.

## 4. Delete and reset semantics

| User action | Rows removed/changed | Preserved |
|---|---|---|
| Reset learning progress | All `education_progress` rows | Choices, review state, positions and all notes/text |
| Remove bookmark/review marker | Selected `education_review_state` row | Progress, choices, position and notes/text |
| Clear committed choice | Selected prompt's `education_committed_choice` row | Other prompts, progress, review, position and notes/text |
| Clear resume position | Selected `education_position` row | All learning progress and personal text |
| Remove Education association only | `education_record` link | WorkspaceRecord title/body as an unassociated personal record |
| Delete Education note and its text | `workspace_record`; FK removes its Education link | Other Education state |
| Full BACKUP_3.1 replace restore | All durable business/AppState collections atomically replaced | Nothing from the old dataset after successful confirmed replacement; any failure rolls back all collections |

Deleting user-authored text is explicit and separate from resetting progress. No Education reset deletes Diary, Safety, Treatment history or general WorkspaceRecords.

## 5. `EDUCATION_EXPORT_1.0` (export only, not restore)

The JSON machine-readable export root is:

```text
exportFormatVersion = EDUCATION_EXPORT_1.0
educationDataSchemaVersion = EDUCATION_DATA_1.0
exportedAtUtc
contentVersionDescriptors[]
progress[]
committedChoices[]
reviewStates[]
records[] (EducationRecord joined with its WorkspaceRecord title/body/revision)
```

It may include stable IDs and the minimum stable labels needed to read committed choices and references; it must not include the full course body, explanations, long source excerpts, copied questionnaire content or source files. TXT is a deterministic human-readable projection of the same Education user records and selected stable labels. There is no Education import/restore in P2. JSON/TXT export omits Diary, Safety, Treatment, InputSnapshot, LocalExecutionState, arbitrary previews, telemetry and EducationPosition. It uses an explicit user-selected SAF destination when UI is later added; cancellation/failure is not success. Content licensing is not expanded by export.

## 6. `BACKUP_3.1` (complete restore format)

The current exact format is `BACKUP_3.0` (`schemas/backup-v3.schema.json`). It is immutable historical input/output and already carries Diary, Safety, Weekly, AppState, WorkspaceRecord, InputSnapshot, Treatment schema/episode/audit collections; it does not carry `LocalExecutionState`. P2B exports `BACKUP_3.1` and imports `BACKUP_1.0`, `1.1`, `2.0`, `2.1`, `3.0` and `3.1`.

`BACKUP_3.1` preserves every `BACKUP_3.0` root field and adds:

```text
educationDataSchemaVersion = EDUCATION_DATA_1.0
educationContentVersions[]       # immutable descriptors only; never course bodies
educationProgress[]
educationCommittedChoices[]
educationReviewStates[]
educationRecords[]                # links WorkspaceRecord rows by stable ID
educationPositions[]
```

Each row carries `educationDataSchemaVersion`. All Education state must round-trip exactly. `LocalExecutionState` remains device-local and excluded; restore rotates it according to the existing restore contract. Existing `treatmentDataSchemaVersion`, `treatmentEpisodes`, and `treatmentAuditEvents` in 3.0 remain intact and must round-trip unchanged; their presence never enables Treatment.

For `BACKUP_1.0` through `BACKUP_3.0`, Education descriptors and collections are absent and restore as empty. Do not infer progress, choice, bookmark, record association or position from historic WorkspaceRecords. The 3.0 schema/example remain byte-identical. Unknown `contentVersionId` (not present in any packaged compatible registry) fails before mutation with an explicit “需要兼容/更新版本的 App” compatibility error. A known version ID with a mismatching manifest hash fails before mutation with an explicit Education content-version incompatibility error. Neither is reported as ordinary data corruption. Dangling WorkspaceRecord reference, duplicate key, invalid enum/version or malformed relationship fails full validation before mutation.

Replace restore remains user-confirmed, one Room transaction across every restorable table and AppState, with pre-commit validation, injected-failure rollback tests and post-commit full read-back. Restore preview reports Education collection counts independently and explains that old backups restore Education state empty. Migration is not restore and never clears/replaces existing data.

## 7. Room 6 → 7 implementation boundary

Room 7 adds only these Education persistence tables: `education_progress`, `education_committed_choice`, `education_review_state`, `education_record`, `education_position`. The EducationRecord FK/indexes are specified above. Do not alter SleepDiaryEntry, Safety, Weekly, treatment tables/state, WorkspaceRecord field meaning or CBT-I Protocol.

Migration 6→7 creates empty Education tables and leaves all existing rows/relationships byte-for-byte semantically unchanged. It may update only the technical `AppState.backupFormatVersion` marker from 3.0 to 3.1, matching existing migration conventions; all user-controlled AppState values remain unchanged. No derived Education state is seeded from old notes. No destructive migration or fallback is permitted.

The complete upgrade path is `1→2→3→4→5→6→7`; each earlier migration and exported schema remains unchanged. Room 7 exported schema is added to Git.

## 8. Repository isolation and acceptance tests

EducationRepository owns validation, transactional updates and read-back. It may depend on Room entities/DAOs, packaged Education registry and WorkspaceRecord operations. It must not depend on `TreatmentStateController`, `domain.protocol`, TreatmentPlan, TreatmentMetricSnapshot, Safety routing or diary-based personalization.

P2B tests must prove: all migration starts through Room 6→7 preserve prior data and create empty Education state; progress explicit transitions; Preview leaves no committed row; explicit Commit persists/replaces one stable choice; review operations and deletion; position is not progress; EducationRecord/WorkspaceRecord atomic linkage and separate deletion semantics; old WorkspaceRecords remain unassociated; exact `BACKUP_3.1` round-trip; `1.0–3.0` legacy empty Education state; strict invalid-reference rejection and rollback; EducationExport JSON/TXT excludes course body and non-Education domains; Education writes leave Diary/Safety/TreatmentEpisode/TreatmentAuditEvent unchanged.

No analytics or engagement telemetry is implemented or allowed. Opening content may update the one resume pointer only; it cannot write any other state implicitly.
