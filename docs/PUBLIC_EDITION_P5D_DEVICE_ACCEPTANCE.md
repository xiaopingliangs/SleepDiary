# Public Edition P5D — Galaxy S24 Device Acceptance

Date: 2026-10-03
Disposition: **TECHNICAL DEVICE ACCEPTANCE: FAIL**. This is not a final v1.0 release. Education content licensing remains **BLOCKED**.

## Device and candidate

- Exact ADB executable used: `D:\Projects\androidsdk\platform-tools\adb.exe`.
- ADB target: `RFCX10F9QVD`, state `device`.
- Device: Samsung Galaxy S24, model `SM-S9210`, Android 16, API 36.
- Candidate installed in place with `adb install -r`; app data was preserved. App version `0.2.2`, versionCode `4`; Room 7; BACKUP_3.1; Education data/export `1.0`; CBT-I Protocol `NONE`.
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`; SHA-256 after final assembly: `CDCAFFE260C7E226336490A4771A74602E8BE89C46A59D905F43E76E0FC71E4D`.
- Before testing, an external BACKUP_3.0 copy of the existing data was saved to the device Downloads folder. No app data was cleared, no existing diary row was edited, and no restore was attempted.

## Device results

| Area | Result | Evidence / limitation |
|---|---|---|
| Install and launch | Verified on device | Candidate installed over the existing app without clearing data. App launched to Today and showed the existing saved-today route. A fresh-data installation was not attempted because the existing database could not yet be exported in the current format. |
| Same-day route / duplicate avoidance | Partially verified on device | Force-stop and relaunch returned to Today and showed the saved-today action. No new diary entry was created. A database-level before/after count was not independently queried. |
| Lifecycle | Partially verified on device | Force-stop/reopen, Activity return from SAF, Sharesheet return, and orientation change were exercised. Recent-app swipe-away, browser-source return, and process recreation were not tested. |
| Diary midnight cases A/B/C | Not verified on device; automated-only | No raw entry was created or edited for these cases. Existing malformed records were found. The existing automated date/validator coverage had passed previously; no physical chronology claim is made. |
| Recent 7/14-day statistics | Verified on device, with a defect | 7-day and 14-day views rendered their neutral averages and human-readable TIB (`7小时9分钟` and `7小时14分钟` in the observed views). These views exclude structurally invalid rows. |
| Weekly summary | Failed on device | The persisted 2026-09-21—2026-09-27 summary shows `10小时36分钟` average TIB across 7 nights. One malformed 2026-09-25 row contributes an extra 24 hours to that persisted aggregate. The stored summary was not silently rewritten. |
| Education map, chapter, scene, sources | Partially verified on device | Map and scene opened; reading, scrolling, source expansion, and return navigation worked. Blocked-content reachability was not exhaustively explored. |
| Open scene → progress behavior | Verified on device for the tested scene | Education JSON export immediately after opening a scene had empty `progress`, `committedChoices`, `reviewStates`, and `records`. Position may update independently. Explicit completion was not tested. |
| Preview → Commit | Not verified on device | No choice was previewed or committed in the live user database. |
| Review/bookmark and Education note/Chinese IME | Not verified on device | No user learning state or note was created. |
| Education summary PNG / receiver | Verified on device | A 42 KB PNG was generated and displayed in the Android Sharesheet. Samsung Messages opened the attached image successfully. It showed only the selected education topic and did not include diary, safety, treatment, notes, or a filesystem path. The message was not sent. |
| BACKUP_3.1 export | Failed on device | SAF created `SleepDiary_backup_20261003_154451.json` at 0 bytes, and the app reported that the operation was not confirmed. Cause is the existing malformed diary rows failing semantic validation during export; see defect analysis below. |
| BACKUP_3.1 round-trip / replace rollback | Blocked | No valid current backup could be produced, so destructive restore and rollback were not attempted. The older BACKUP_3.0 copy was preserved and was not imported. |
| Education JSON and TXT export | Verified on device | SAF produced a 222-byte EDUCATION_EXPORT_1.0 JSON and a 152-byte Chinese TXT. The tested export had no diary, safety, treatment, course-body, or telemetry payload. Its user-state collections were empty. |
| Offline use | Partially verified on device | With Wi-Fi and mobile data disabled, Today, 7/14-day statistics, and Education reading opened. Diary create/edit, history, note editing, backup export, Education export, and PNG generation were not all repeated offline. Network settings were restored afterward. |
| Font/layout/accessibility | Partially verified on device | Screens were inspected at system font scales 1.0 and 1.3; bottom navigation and scrolling worked. Rotation was exercised. Dark/light mode, Chinese IME, all long scenes, and full touch-target/accessibility review remain unverified. Original device settings were restored: font scale 1.3, auto-rotate on, Wi-Fi on, mobile data on, airplane mode off. |
| Public boundary | Partially verified | Today/Sleep/Education/Me navigation was visible; no active Safety or Treatment path was observed in the tested Public screens. BuildConfig remains `CBTI_PROTOCOL = NONE`. This was not an exhaustive screen-by-screen audit. |

## Defects and disposition

Two existing diary rows have date/time chronology inconsistent with their `sleepDate` and exceed a 24-hour in-bed span:

- `sleepDate=2026-09-20`, `bedEnteredAtLocal=2026-09-19T00:00`, `outOfBedAtLocal=2026-09-20T07:00`.
- `sleepDate=2026-09-25`, `bedEnteredAtLocal=2026-09-24T00:00`, `outOfBedAtLocal=2026-09-25T06:30`.

The current DiaryValidator rejects these structures. `BackupCodec.validate` requires every diary item to pass that validator. `BackupRepository.export()` encodes then previews/validates the generated JSON before returning it to the SAF writer. The SAF target is created before the repository failure, leaving a zero-byte file. This is why the app refuses to export the current real database as BACKUP_3.1.

The persisted weekly snapshot containing the September 25 row still includes it. The recent-average screen filters invalid rows, but the already persisted snapshot remains contaminated until a user-authorized manual correction causes the relevant summary to be regenerated. No raw data or summary was changed during this acceptance run.

No source fix was made because safely resolving this requires an explicit decision about correcting the two original records and/or the frozen backup contract’s handling of legacy structurally invalid raw data. Do not clear the app or restore the older backup until a valid external backup is confirmed. The old BACKUP_3.0 copy remains on the device Downloads folder.

## Automated and repository checks

- Full unit tests rerun with `--rerun-tasks`: **452 tests, 0 failures, 0 errors, 0 skipped**.
- `assembleDebug --offline`: **PASS**.
- Merged manifest check: no `INTERNET`, `ACCESS_NETWORK_STATE`, or `MANAGE_EXTERNAL_STORAGE` permissions.
- No source, schema, or medical-spec changes were made. CBT-I Protocol remains `NONE`.
- Start HEAD: `0996b73c93499a7e237838d6946e734ed74caf76`.
- No physical-device fix commit. This report is the durable P5D acceptance record.
- `meditation/` is unrelated pre-existing untracked content and remains untouched.

## Remaining P5D blockers

1. Resolve the two malformed diary rows through an explicit user-directed correction path, then verify weekly snapshot supersession and non-empty BACKUP_3.1 export.
2. Verify BACKUP_3.1 restore round-trip and rollback on a safe test state after a valid external backup exists.
3. Complete the remaining Education persistence, note/IME, offline, and accessibility device flows.
4. Content licensing clearance remains blocked as recorded in `PUBLIC_EDITION_RELEASE_BLOCKERS.md`.

Do not start P6 or Treatment as part of this checkpoint.
