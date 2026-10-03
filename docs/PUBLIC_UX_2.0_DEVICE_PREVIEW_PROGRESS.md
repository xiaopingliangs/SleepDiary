# Public UX 2.0 Device Preview — Durable Progress

Date: 2026-10-03
Scope: first usable Public UX 2.0 build for Galaxy S24 human evaluation. Treatment/P6, remaining V4 content conversion, and RC acceptance are not started.

## Checkpoints

| Checkpoint | Result | Commit |
|---|---|---|
| 0 — legacy data integrity | Current diary writes stay strict; structurally parseable legacy rows round-trip losslessly through BACKUP_3.1; invalid rows are excluded from current statistics without raw rewriting; stale weekly summary is superseded by revision; failed SAF output is cleaned up where supported. | `175921e` |
| 1 — Public shell | Four tabs are Today / History / Understand / Me. Today no longer duplicates navigation; saved details are read-only before explicit correction; exports are centralized in Me. The overlap cause was NavHost content continuing to render below form/success overlays; the active content layer is now exclusive. | `e04a825` |
| 2 — diary input | Entry uses clock anchors plus relative durations, preserves existing absolute fields and Room schema, and handles midnight crossing (23:55 + 10 minutes → next-day 00:05). PNG sharing defaults to excluding comment and requires explicit opt-in after preview. | `9556c19` |
| 3–4 — Education experiences | Added a native presentation layer over existing V4 IDs for four experiences. The old schema renderer is reachable only through optional Knowledge Map. Choice preview stays transient; only explicit commit persists. Reflection is optional and explicitly saved. | `ab7e602` |
| 5 — contextual education | Added a deterministic, read-only local matcher and opt-out in Me. It considers the latest valid diary in the inclusive 7-day window and may map one estimate to one pre-authored Experience. Invalid/negative estimates produce no match. | `e888ccc` |
| 6 — Understand home | Added the optional “可能和你最近有关” card; both Today and Understand open the same existing Experience. | `e049cac` |
| 7 — verification corrective | Full suite exposed JSON CreateDocument launching from an IO continuation in Robolectric. Launcher is now explicitly dispatched to Android Main; tests assert main-thread dispatch. Stale UI selectors and a test-looper wait were updated to current UX. | `cd7c162` |

## Final verification

- Targeted tests for matcher, Education UX, public navigation, legacy data, and SAF passed.
- Full `testDebugUnitTest`: 472 tests, 0 failures, 50 test classes.
- `assembleDebug`: passed.
- Merged Manifest: no INTERNET, ACCESS_NETWORK_STATE, or MANAGE_EXTERNAL_STORAGE permission. The generated dynamic-receiver signature permission is present and is not one of the prohibited permissions.
- Room database remains version 7; exported schemas 1 through 7 are present. This work added no Room migration or schema change.
- Backup version remains BACKUP_3.1. Existing tests cover BACKUP_1.0–3.1 compatibility and restore behavior.
- `CBT-I Protocol = NONE`; Public top-level navigation has no Treatment route. No Treatment feature work was performed.
- Existing historical malformed diary rows were not rewritten. They remain losslessly preserved for backup and excluded from derived statistics until explicit correction.
- `meditation/` remains unrelated and unmodified/untracked.

## Preview artifact and next action

- APK: `app/build/outputs/apk/debug/app-debug.apk`
- SHA-256: `FB03310AD6B2D76F07A976AA465D9160CBAB52B5E88E79B8490E88363320CA91`
- Status: automated checks passed; this is a device preview, not an RC or physical-device acceptance result.
- Next: manually install and evaluate this APK on Galaxy S24, then report UX defects or acceptance. Do not begin P6, remaining V4 conversion, or RC verification before that feedback.
- V4 content licensing clearance remains blocked pending review.
