# Public Edition P5 RC progress

## Part A — first-install corrective

Status: implemented, targeted regression verified, and committed separately.

- Fresh install now starts in Today and consumes the existing per-local-day routing marker, entering today's Diary form. `hasCompletedOnboarding=false` no longer gates launch or replays any pages.
- The existing three-page general introduction is available at **Me / 使用说明**. It can be left at any time. Only explicitly finishing all pages persists the existing AppState completion flag; no database/schema/backup change was made.
- Existing users retain saved entry/form state and do not see an onboarding replay. No Safety or Treatment flow was added.
- Updated the Public product contract and requirements matrix to match the correction. Part B will record licensing and physical-device blockers separately from the onboarding correction.
- Commit: `0bbbac47731f19916312aeb10a044202f92f0d7d` (`fix: remove blocking first-launch introduction`).
- Targeted first-launch, optional-guide, route, and navigation regression selection passed: 32 tests, 0 failures.

## Part B — RC verification

Status: automated/static RC checks complete; physical-device acceptance remains blocked because ADB reports no connected devices.

- Full `testDebugUnitTest --offline`: PASS, 452 tests, 0 failures, 0 errors (47 XML reports).
- `assembleDebug --offline`: PASS.
- Targeted compatibility/date/export coverage: PASS. Migration and backup tests cover BACKUP_1.0 / 1.1 / 2.0 / 2.1 / 3.0 / 3.1; older backup fixtures restore education state empty. Date tests cover midnight chronology, malformed records, and empty weekly periods. Education export tests cover deterministic projection and exclusion of diary/safety/treatment data.
- Merged manifest permission audit: PASS; no `INTERNET`, `ACCESS_NETWORK_STATE`, or `MANAGE_EXTERNAL_STORAGE`.
- Packaged Room schema remains version 7; schema directory has no diff. `CBT-I Protocol` remains `NONE`. Public bottom navigation is Today / Sleep / Education / Me; static route review found no active Treatment or Safety route.
- ADB inventory: no physical device connected. Physical S24 acceptance is not claimed; see the exact outstanding checks in `PUBLIC_EDITION_RELEASE_BLOCKERS.md`.
- RC candidate: `app/build/outputs/apk/debug/app-debug.apk`; SHA-256 `2A54EAECAB80D7A934989EE8AF07EABA571646938C967389ED64E98982A992A8`.
- App `0.2.2` / versionCode `4`; Room `7`; BACKUP `3.1`; Education data `1.0`; Education export `1.0`; CBT-I Protocol `NONE`.
- P5 Part B verification is recorded in this progress file and the release-blocker file. `meditation/` was already untracked and remains untouched and unstaged.

## RC acceptance disposition

The candidate is technically built and the full automated suite passes, but P5's physical-device acceptance is still blocked. Do not describe this as a fully accepted or final v1.0 release. Education redistribution/adaptation rights are separately unresolved, so content licensing is also blocked. The final report records ending Git HEAD and working-tree status; the Part B documentation is committed separately from the Part A correction.

P5 does not authorize Treatment work. Current versions remain Room 7, BACKUP_3.1, EDUCATION_DATA_1.0, EDUCATION_EXPORT_1.0, and CBT-I Protocol `NONE`.
