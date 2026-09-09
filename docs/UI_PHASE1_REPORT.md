# Android UI phase 1

Date: 2026-09-09
Frozen specification: SPEC-0.1.1
App: 0.1.0; CBT-I Protocol: NONE

## Delivered pages and flow

Single Activity with Compose Navigation: three introductory pages, daily diary, today's already-saved entry, reuse of the diary form to edit today's entry, verified save success, minimal settings, and a read-only saved-date history list. Unimplemented settings entries are explicitly disabled. Exit uses finishAndRemoveTask; no process killing.

Safety UI 尚未接入首启正式流程。
The temporary third introduction page and direct transition to the diary are explicitly authorized for this development phase. Safety models and the final frozen first-launch flow are unchanged. Weekly UI is not connected; the existing repository may prepare snapshots, but the UI does not mark them displayed. No share, import/export, FileProvider, treatment, or complete methods/history-editing page was added.

Introduction progress is held in SavedStateHandle for this temporary phase. Existing diary data skips the introduction. If the introduction is completed but no diary has ever been saved, a completely fresh cold launch can show it again; no new backup-critical database field was invented. The existing first-save explanation flag stays in Room and is acknowledged before the success-page exit completes.

## Input and reliability

All eight answers start null. Reference date, picker position and temporary dialog input are separate from confirmed form answers. Time dialogs have Cancel/Confirm; close, scroll and cancellation do not fill data. Reopening uses the confirmed value. Four time pickers use 24-hour time and 15-minute primary choices. Positioning across midnight uses confirmed neighboring event times; candidates are visible and require confirmation, and explicit date editing is available. No medical definition of bedtime or today was introduced: saved sleepDate is the out-of-bed local date.

SOL/WASO use horizontal quick choices with explicit integer alternatives; awakening count has 0–5 and 6–99 alternatives; all five quality choices remain directly accessible with no default. Comment is collapsed and optional. Missing fields scroll into view on save; no next picker opens automatically.

Time/numeric alternative input uses native Android EditText through the official AndroidView interop, with Material-themed colors and external labels. Focus, text replacement, cursor insertion, content descriptions and native text-change events are wired to the same temporary/form state. This avoids persistent Compose text-input layout waits observed during local testing. No extra input library was added.

DiaryViewModel invokes DiaryValidator and the existing formal DiaryRepository through RoomDiaryUiStore. Hard errors do not write. The consistency dialog uses the frozen wording and requires explicit confirmation; SOL/WASO remain untouched. Only repository Success changes the screen to success. Failure retains the form and UUID with a neutral retry message. Editing keeps UUID/createdAtUtc; the formal repository controls updatedAtUtc and transactional read-back.

## Tests and build

Both required tasks were run locally using the project's Gradle Wrapper and Android Studio JBR:

- testDebugUnitTest: PASS
- assembleDebug: PASS

| Added suite | Cases | Result |
| --- | ---: | --- |
| DiaryFormTest | 13 | PASS |
| DiaryViewModelTest | 14 | PASS |
| DiaryComposeTest | 12 | PASS |
| NavigationComposeTest | 3 | PASS |
| RoomUiStoreTest | 3 | PASS |

45 new tests: 42 form/ViewModel/Compose/navigation cases plus 3 Room adapter cases. With the previous 109 cases, total 154; zero failures, errors or skipped cases. The original failed assertions were retained and corrected through implementation/test-harness fixes; none were deleted or disabled.

Compose interactions and native rendering run in local Robolectric API 34; existing Room tests continue on API 28. Coverage includes cancelling anchors, explicit confirmation, reopening values, date editing, integer choices, empty forms, warning branches, failure/retry/success navigation, today's UUID, 48dp checks on representative controls, core semantics, 1.5x fonts and dark rendering.

Robolectric PNGs are generated in work/ui-phase1-renders (light.png, dark.png, large-font.png). Capture uses native View.draw rather than waiting on PixelCopy frame callbacks. These are local component renders, not physical-device screenshots or pixel-golden regression acceptance.

## Dependency and integrity checks

- Added fixed Navigation Compose 2.9.3 and Lifecycle ViewModel/SavedState 2.9.2.
- Compose UI test/manifest use the existing 2025.08.01 BOM; coroutines-test uses existing 1.10.2.
- Existing App/domain version constants, SDK configuration and Room entities/history are unchanged.
- All 15 specification-manifest hashes and byte counts match the frozen baseline. No frozen specification or SELF_CHECK change.
- Merged Manifest has no INTERNET, ACCESS_NETWORK_STATE or MANAGE_EXTERNAL_STORAGE.
- APK: D:/Projects/SleepDiary/app/build/outputs/apk/debug/app-debug.apk
- APK size: 10,538,845 bytes.

Official references checked during implementation: [Navigation releases](https://developer.android.com/jetpack/androidx/releases/navigation), [Robolectric PixelCopy behavior](https://robolectric.org/javadoc/4.16/org/robolectric/shadows/ShadowPixelCopy.html).

## Not executed and stopping point

No connectedDebugAndroidTest, physical-device installation, actual TalkBack speech/gestures, physical keyboard/IME overlap check, force-stop/reboot or real task-removal validation was performed. Actual 30–60 second completion time is not claimed. Local semantics, rendering and ViewModel tests do not replace those device checks.

No new medical field, treatment rule, frozen-specification change or further approval-required design change was made. This is completion of the authorized development slice, not full V0.1 release acceptance. Stop here; do not proceed to Safety or Weekly UI.
