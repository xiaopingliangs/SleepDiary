# Public Edition RC / Release Blockers

Status: RC candidate built; automated/static verification passes. Physical-device acceptance and Education content rights clearance remain blocked. Do not describe the candidate as a final Public Edition v1.0 release.

## P5 RC verification snapshot — 2026-10-03

- RC APK: `app/build/outputs/apk/debug/app-debug.apk` (App `0.2.2`, versionCode `4`); SHA-256 `2A54EAECAB80D7A934989EE8AF07EABA571646938C967389ED64E98982A992A8`.
- `testDebugUnitTest --offline`: 452 tests, 0 failures, 0 errors. `assembleDebug --offline`: passed.
- Runtime versions: Room 7, BACKUP_3.1, EDUCATION_DATA_1.0, EDUCATION_EXPORT_1.0, CBT-I Protocol `NONE`.
- Merged Manifest contains none of `INTERNET`, `ACCESS_NETWORK_STATE`, `MANAGE_EXTERNAL_STORAGE`. Public bottom navigation is Today / Sleep / Education / Me; no active Treatment or Safety route was found in the public route graph.
- `adb devices -l` returned no device. Every device-only item below is still unverified; automated tests and APK build are not substitutes.

## Outstanding physical-device acceptance

On a connected Galaxy S24 (or other physical Android device), verify:

- Fresh install enters today's diary; save, fully exit, same-day reopen, and existing-entry edit do not duplicate records or create Safety/Treatment/Education state accidentally.
- Genuine daily first launch versus second launch across force-stop, swipe-away, process death, Activity recreation, rotation, Sharesheet return, SAF picker return, source-link return, date changes, and timezone changes. Confirm activity recreation does not consume the daily route and raw timestamps are unchanged.
- Diary chronology and displayed TIB/statistics/weekly summary for 23:45→07:00, 00:00→07:00, and 23:45→try sleep 00:00→07:00.
- Education map, chapter/scene navigation, sources, Preview/Commit, explicit completion, resume position, bookmark/review, note create/edit/delete, and blocked-content inaccessibility.
- Education summary PNG generation, `content://` FileProvider reading by a receiver, Sharesheet cancellation/return, and absence of Diary, Safety, Treatment, notes, or filesystem paths in the image/share payload.
- Meaningful-data BACKUP_3.1 export, safe clean-install/restore-preview/confirmed restore, exact round-trip of Diary, Workspace, and all Education states, plus no active Safety/Treatment UI. Do not clear data before verifying an external backup.
- Real-user Education JSON/TXT export and offline operation of diary, statistics, Education, notes, backup, export, and PNG generation with device networking disabled.
- Galaxy S24 visual/accessibility pass at normal and increased font sizes, dark/light mode, Chinese IME, keyboard show/hide, long-scene scrolling, navigation/back, rotation, background/foreground, and relaunch. Check clipping, unreachable controls, touch targets, blank screens, and state resets.

Legacy backup versions and Education export are covered by automated fixtures/tests, but no physical restore/export was run in this checkpoint.

## Content and source rights — unresolved

- **V4 source HTML and adapted material:** no verifiable redistribution license or complete permission chain is included with `CBTI-Learning-Game-V4.html`. Confirm rights for the source and each retained/adapted unit before public distribution. Adaptation status is recorded per unit in `V4_PUBLIC_CONTENT_MATRIX.md`; permission status remains unresolved.
- **VA CBT-I Therapist Manual:** bibliographic metadata and an official link are recorded, but the manual is marked for rights review. Its Sleep Need Questionnaire is excluded and blocked; do not publish its questions, scoring, or use flow without explicit permission and separate review.
- **AASM/JCSM 2021:** candidate content uses project-authored wording and bibliographic metadata rather than copied paper passages. Confirm any redistribution/adaptation scope required for the final app.
- **Chinese 2023 guideline:** access is restricted and the source indicates a permission application route. Candidate content does not copy the full text or long passages; any derived wording still requires rights review.
- **VA/DoD 2025 guideline:** bibliographic use is represented; clinical decision tools must not be turned into Public Edition personal treatment decisions. Confirm source and derivative-content scope before release.
- **Four PubMed-indexed reviews:** candidate usage is bibliographic metadata and links only; PubMed availability does not grant full-text rights. Confirm that citations and any adapted claims are accurate and permitted.
- **Blocked inventory:** all 54 `BLOCKED` units in `V4_PUBLIC_CONTENT_MATRIX.md` remain excluded. Categories include the unlicensed Sleep Need Questionnaire, mixed medical/safety questionnaires, overbroad comorbidity and occupational language, absolute sleepiness claims, unsupported service guarantees, source conflicts, and AI medical prompts.
- **Future-treatment inventory:** all 40 `FUTURE_TREATMENT_ONLY` units remain excluded from Public execution. They include sleep-window and weekly adjustment rules, executable stimulus-control steps, and treatment-state-dependent scenarios.

Required closeout: rights-holder/authorized counsel review of the source material and each retained/adapted unit; record the decision and scope before calling the content cleared. No permission is inferred here.

## Physical-device RC verification — outstanding

P5 requested device evidence for daily routing and lifecycle, native Education and learning state, summary PNG/FileProvider/Sharesheet, BACKUP_3.1 round-trip, Education exports, offline operation, and Galaxy S24 layout/IME/accessibility. These checks remain open until executed on a connected physical device. Automated tests and APK construction do not replace them.

## P5D Galaxy S24 execution — 2026-10-03

The device became available and P5D was executed. Technical device acceptance is **FAIL**, not merely untested: BACKUP_3.1 export on the existing database produces a zero-byte SAF file, and an already persisted weekly summary shows an inflated average TIB. Both findings trace to two malformed historical diary rows rejected by the current structural validator. No raw data was rewritten and no restore was attempted. See [PUBLIC_EDITION_P5D_DEVICE_ACCEPTANCE.md](PUBLIC_EDITION_P5D_DEVICE_ACCEPTANCE.md) for per-test evidence and limitations.

The earlier P5D items not exercised remain open, including a safe BACKUP_3.1 round-trip, Education note/IME and persistence flows, and full offline/accessibility checks. Keep Education licensing clearance **BLOCKED** as above. Do not begin P6 or Treatment from this checkpoint.
