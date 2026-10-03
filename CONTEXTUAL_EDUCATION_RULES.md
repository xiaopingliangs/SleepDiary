# Public UX 2.0 — Contextual Education Rule Registry

Status: Public UX 2.0 device-preview contract. This deterministic content-matching rule is not a clinical threshold.

## Rule `RECENT_SHORT_ESTIMATED_SLEEP_TO_COGNITIVE_EXPERIENCE_1`

- Input window: local sleep dates from reference date minus 6 days through the reference date, inclusive.
- Candidate: the most recent diary row in that window that passes current `DiaryValidator` hard validation. Invalid legacy rows are skipped; raw rows are never rewritten.
- Neutral value: interval from `trySleepAtLocal` to `finalAwakeningAtLocal`, less the user's estimated SOL and WASO. If chronology is invalid, input validation fails, or the result is negative/unrepresentable, no value is produced.
- Content mapping: when the newest valid estimate is at or below 240 minutes, offer the pre-authored `bad_night` Education Experience. The 240-minute value is solely a match to the existing V4 scenario wording (“four hours”); it is not shown to users and does not mean normal/abnormal, risk, diagnosis, or treatment eligibility.
- Output: zero or one optional question. No AI inference, popup, push notification, or personal recommendation.
- User control: enabled by default for this local preview and can be disabled in Me. Preference is local-only and is not Room/backup data.
- Privacy: all matching occurs on device. The explanation identifies the source date and says that the match is not diagnosis or treatment advice.
- Isolation: the rule reads diary rows and an explicit preference; it does not write Diary, Safety, Workspace, Education progress/choice/review state, or Treatment data. It does not consume TreatmentMetricSnapshot and does not calculate SE, a prescribed sleep window, or TreatmentPlan/State.

The derived value may be labeled only `估计睡眠时长`. It is not presented as measured/actual sleep time. Existing raw diary fields and schema remain unchanged.
