# Public Edition P3 progress

## P3A checkpoint — 2026-10-03

Status: implemented and verified; committed as `ea537796a155444e92ed4fd6784f61e65c703b7b` (`feat: add Public Edition app shell`).

- Replaced the prior single-screen route structure with the four public top-level destinations: Today, Sleep, Education, and Me. No Treatment route or tab exists.
- Today shows whether a diary exists, start/edit action, live neutral 7/14-day averages already supported by the app, and links into Education and Sleep.
- Sleep presents diary history, current weekly summaries, and CSV export.
- Me consolidates Workspace, complete backup/restore, CSV and Education export, clinician summary, privacy, methods, terms, versions, and retained historical Safety access.
- Restore preview displays Education collection counts, older-backup notices and dormant Treatment episode/audit counts. An integration test verifies nonzero Education and dormant Treatment counts, and confirms no clickable Treatment action.
- First-run intro ends at Today. Safety remains optional and reachable from Me. Existing Safety drafts remain persisted when leaving the flow.

Verification at its checkpoint: targeted `testDebugUnitTest` selection passed (46 tests; 0 failures). `git diff --check` passed. Final P3 regression is recorded below.

P3A changes are committed separately before beginning P3B. Unrelated untracked `meditation/` remains untouched and unstaged.

## P3B checkpoint — 2026-10-03

Status: implemented and verified; committed separately after P3A.

- Replaced the Education placeholder routes with a native Compose experience driven by the packaged, hash-verified candidate registry. No WebView or second persistence store was introduced.
- The registry parser exposes all 462 candidate nodes across 26 supported kinds. It rejects unknown kinds, broken parents, unpublished next links, invalid classifications, duplicate IDs and unresolved source references. The catalog includes 11 chapters and 98 scenes; blocked and future-treatment-only nodes are excluded.
- The Knowledge Map presents packaged map groups/branches, links published knowledge entries and existing scenes, and displays neutral learning state. Chapter and scene structure, expandable plain/deeper explanations, evidence/source details, scenarios, cognitive/reflection prompts, and official-source links are rendered from registry payloads.
- Preview remains transient. Only “确认并保存选择” writes the committed choice. Opening a scene writes its resume position and IN_PROGRESS state; only “完成本节并继续” marks it completed. Review/save/remove actions remain explicit.
- Reflection/note prompts save `WorkspaceRecord` and its one-to-one `EducationRecord` association transactionally; existing edit, unlink-while-retaining-text and delete-both behavior is preserved. Education storage remains isolated from Diary, Safety and dormant Treatment data.
- System font scaling and Material theme colors are used. No separate three-choice Education font/theme preference existed in the current native product.
- No map `nextContentId` points into unpublished material. One manifest capability remains a deliberate gap: `EXPORT_ELEMENT-EXPORT-SUMMARY-IMAGE` is represented in the candidate catalog but no summary-image generation flow is exposed in P3. Licensing review for the candidate V4 content remains a public-release blocker.

Final verification for both P3 checkpoints: `testDebugUnitTest` passed (439 tests, 0 failures); `assembleDebug` passed; merged Manifest contains no INTERNET, ACCESS_NETWORK_STATE, or MANAGE_EXTERNAL_STORAGE permission; `version.properties` remains `cbtiProtocolVersion=NONE`; public navigation contains no Treatment destination or action. APK: `app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `50810bf0b6034d3f7b399d6af2298661524b0de9ef57afeba9f7b95bdd97c1a`. Untracked `meditation/` was left untouched and unstaged.

P3B targeted education/shell regression: 32 tests passed after stabilizing selectors for a review-state button. Full suite: 439 passed, 0 failed. `assembleDebug`: passed. Content licensing review and the unimplemented summary-image export remain release follow-ups; no Treatment or P4 work was started.

## P3 corrective checkpoint — 2026-10-03

Status: implemented and targeted regression verified; committed independently before P4.

- Corrected Education semantics: opening/navigating/resuming a scene updates only `EducationPosition`; it no longer creates or changes `EducationProgress`. The explicit completion action continues to write `COMPLETED`. Regression coverage opens a scene, navigates away, recreates the Activity, resumes by position, and verifies the progress collection remains unchanged/empty.
- Removed Safety questionnaire/history actions and the active Safety screen from the Public shell. Legacy Safety page state is no longer restored into a visible Public overlay. Safety entities, repositories, migration history and BACKUP_3.1 compatibility are unchanged; an existing Safety draft remains intact and visible counts in backup restore preview remain supported.
- Targeted corrective UI/Education tests passed: 32 tests, 0 failures. No Room schema or backup contract changes.

Next: P4 first-open routing and Public Edition product integration. Treatment remains dormant; no medical rules were changed.

## P4 Public Edition integration checkpoint — 2026-10-03

Status: implemented; full automated verification and debug APK build passed. This checkpoint is committed separately from the P3 corrective checkpoint.

- Added a private SharedPreferences routing marker (`lastDailyAutoDiaryRouteDate`) that claims each newer local calendar date once. The first route for a day opens that date's existing entry for editing, or a new entry for that date; same-day later launches stay on Today. The marker is private UI routing state and does not alter Room, diary fields, backup data, Safety, Education progress, or Treatment.
- `MainActivity` supplies the device's current system zone and reevaluates on foreground return. Saved editing state wins on Activity/process recreation. The chosen timezone policy is monotonic: if the system date moves backward after a date was claimed, that earlier date does not re-trigger; advancing beyond the high-water date becomes eligible. Diary timestamps are never rewritten.
- Retained the existing three-page general first-run explanation. Finishing it routes into the daily diary-first flow. It does not start Safety or Treatment and does not create a TreatmentEpisode. Restored legacy Safety/Treatment records do not alter Public navigation or routing.
- Added integration coverage for fresh-first launch/save/second launch, opening an existing date without duplication, second/third same-day launches, midnight rollover, SavedStateHandle editing restoration, same-day return from an external UI, and timezone movement. A new Room + BACKUP_3.1 + Public shell integration test restores persisted Safety and Treatment history, confirms both remain in Room, then visits every Public destination and verifies no Safety/Treatment workflow appears. Education position/progress, explicit completion, and inaccessible Safety history are also covered by existing and corrective regression tests.
- Added local Education summary-image sharing. The page only shares chapter titles the user explicitly selects; PNG creation is local, uses the app's existing cache/FileProvider configuration and Sharesheet intent helper, and includes no course body, personal notes, Diary, Safety, or Treatment data. An empty selection cannot be exported. Robolectric runs on Windows and its host-path canonicalization rejects FileProvider's configured cache root, so the JVM regression isolates PNG creation and content-URI chooser construction; actual Android FileProvider handoff remains unverified until a device is attached.
- Fixed the Education root back action binding so “← 返回” returns to Today instead of targeting the summary-share callback. Added/retained neutral empty states for Today, Sleep history, Education progress, and personal notes. Public top-level navigation remains Today / Sleep / Education / Me; no Treatment tab, controller, or active Safety workflow is exposed.
- No Room schema, backup format, frozen medical/data specification, or manifest permission changed. Room remains 7, backup remains BACKUP_3.1, and `cbtiProtocolVersion=NONE`.

Verification for P4: targeted UI/education/routing/share tests passed; the added legacy-data integration test passed; final full `testDebugUnitTest` passed (452 tests, 0 failures); final `assembleDebug` passed. Merged manifest has none of INTERNET, ACCESS_NETWORK_STATE, or MANAGE_EXTERNAL_STORAGE; exported Room schema has no diff; backup compatibility regressions are included in the full suite. Debug APK: `app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `93c925588ebf30a8b668f937c73e8fefc458d9c0c5ed41d74938fe1fefeb94d7`.

P4 release status remains pre-RC. Physical-device verification is outstanding (no device was attached during this run), and redistribution/licensing review of candidate Education content remains outstanding. Unrelated untracked `meditation/` was left untouched and unstaged.
