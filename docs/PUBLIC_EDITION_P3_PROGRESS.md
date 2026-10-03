# Public Edition P3 progress

## P3A checkpoint — 2026-10-03

Status: implemented and verified; P3B has not started in this checkpoint.

- Replaced the prior single-screen route structure with the four public top-level destinations: Today, Sleep, Education, and Me. No Treatment route or tab exists.
- Today shows whether a diary exists, start/edit action, live neutral 7/14-day averages already supported by the app, and links into Education and Sleep.
- Sleep presents diary history, current weekly summaries, and CSV export.
- Me consolidates Workspace, complete backup/restore, CSV and Education export, clinician summary, privacy, methods, terms, versions, and retained historical Safety access.
- Restore preview displays Education collection counts, older-backup notices and dormant Treatment episode/audit counts. An integration test verifies nonzero Education and dormant Treatment counts, and confirms no clickable Treatment action.
- First-run intro ends at Today. Safety remains optional and reachable from Me. Existing Safety drafts remain persisted when leaving the flow.

Verification: targeted `testDebugUnitTest` selection passed (46 tests; 0 failures). `git diff --check` passed. Full suite/build are reserved for final P3 verification.

P3A changes are committed separately before beginning P3B. Unrelated untracked `meditation/` remains untouched and unstaged.

Next: implement the registry-driven native Education map and scene reader using Room 7 persistence; keep Preview transient until explicit Commit; test Room isolation and durable position/progress/review/notes. Content licensing review remains a public-release blocker.
