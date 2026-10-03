# Public Edition P5 RC progress

## Part A — first-install corrective

Status: implemented and targeted regression verified; ready for its separate commit.

- Fresh install now starts in Today and consumes the existing per-local-day routing marker, entering today's Diary form. `hasCompletedOnboarding=false` no longer gates launch or replays any pages.
- The existing three-page general introduction is available at **Me / 使用说明**. It can be left at any time. Only explicitly finishing all pages persists the existing AppState completion flag; no database/schema/backup change was made.
- Existing users retain saved entry/form state and do not see an onboarding replay. No Safety or Treatment flow was added.
- Updated the Public product contract and requirements matrix to match the correction. Part B will record licensing and physical-device blockers separately from the onboarding correction.
- Targeted first-launch, optional-guide, route, and navigation regression selection passed: 32 tests, 0 failures.

## Part B — RC verification

Not yet complete. Next steps after the Part A commit: run the full test suite, verify legacy backup and Education export coverage, inspect the merged manifest and Room schema, rebuild the RC APK, record versions/hash/HEAD/status, and document physical-device checks as blocked because ADB currently reports no connected devices.

P5 does not authorize Treatment work. Current versions remain Room 7, BACKUP_3.1, EDUCATION_DATA_1.0, EDUCATION_EXPORT_1.0, and CBT-I Protocol `NONE`.
