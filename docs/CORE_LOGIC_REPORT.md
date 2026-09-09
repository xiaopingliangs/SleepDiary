# Core domain and reliability phase

Date: 2026-09-09

## Scope delivered

- DiaryRepository.save exposes Success, ValidationWarning and Failure. Input remains available in warning/failure results. Cancellation propagates without returning success.
- Insert/update, UUID read-back, tracking state changes and summary revisions share one Room transaction. Equality checks every persisted field, including all raw business fields. A second read after commit is required before Success. If this last read fails, the result remains Failure even though the transaction may already be committed; retrying the same UUID updates one record.
- Updates retain UUID and createdAtUtc. The injected Clock supplies UTC metadata. User estimates and event times are never corrected. consistencyWarning is derived from the current estimates and requires explicit confirmation before persistence.
- DiaryValidator checks local time ordering, date identity, numeric technical limits, quality, UUID, zone identifier and diary schema. Cross-consistency returns a confirmable warning, not a hard error. Local-time durations retain fractional-minute precision.
- SafetyGateCalculator implements frozen answer-code branches and priority. Partial S03 YES/UNSURE yields DELAY_AUTOMATED_TREATMENT. Conditional S07 YES requires S07b to calculate that branch; malformed/unsupported answer combinations are rejected rather than treated as safe.
- S09Codec uses Android platform JSON, preserves the existing SafetyAnswer model and provides exclusive NONE/UNSURE selections and canonical deduplicated, ordered FLAGS_PRESENT details. Stored duplicate/unknown/empty codes are rejected. No risk score or diagnosis is produced.
- WeeklySummaryCalculator computes only the approved fields, keeping missing days absent and Double precision. PeriodCalculator advances by calendar days, including empty weeks.
- Before the first snapshot, trackingStartDate follows the earliest stored date. Once any snapshot exists it stays frozen; records before it are rejected.
- WeeklySummaryRepository retains all revisions. Source and eligible destination revisions are transactional, with one current revision and predecessor IDs. An unfinished destination is not generated. End-of-period and missed-end-day saves can prepare the appropriate pending summary; generation does not mark it displayed. No presentation is implemented.

## Automated verification

Command executed locally: gradlew.bat testDebugUnitTest assembleDebug --console=plain

| Test suite | Cases | Result |
| --- | ---: | --- |
| Existing DatabaseTest | 8 | PASS |
| ReliabilityTest | 24 | PASS |
| DiaryValidatorTest | 12 | PASS |
| SafetyGateCalculatorTest | 48 | PASS |
| SafetyValidationTest | 3 | PASS |
| S09CodecTest | 6 | PASS |
| WeeklyTest | 8 | PASS |
| Total | 109 | PASS; 0 failures, 0 errors, 0 skipped |

101 cases added. Existing eight tests retained, with repository calls adapted to the formal SaveResult API and an injected fixed clock.

Coverage includes all persisted-field read-back mismatches, write/read failures, post-commit read failure, duplicate saves/date collision, warning confirmation, original creation-time preservation, tracking freeze, same/cross-period revisions, rollback after revisions, empty periods and delayed summaries. Pure JVM tests cover validation, periods, averages and the primary gate branches. Room and Android JSON tests run through local Robolectric; no physical device is needed for these tests.

The first run found a JUnit return-type issue, which was corrected without removing tests or weakening assertions. Final testDebugUnitTest and assembleDebug both passed.

## Integrity and scope checks

- All 15 frozen manifest file hashes and byte counts match. Frozen specifications, SELF_CHECK.json and SPEC_MANIFEST.json are unchanged.
- Room schema export remains enabled; technical schema version 1 and all five entities are unchanged. The committed schema history has no diff; no migration is needed for this phase.
- Merged Manifest has no INTERNET, ACCESS_NETWORK_STATE or MANAGE_EXTERNAL_STORAGE permission.
- No dependency or SDK version changes. No new runtime network service.
- CBT-I Protocol remains NONE. No treatment logic, full UI, import/export, sharing, FileProvider or wearable integration was implemented.
- Debug APK: D:/Projects/SleepDiary/app/build/outputs/apk/debug/app-debug.apk (10,137,430 bytes).
- Physical-device force-stop, reboot, lifecycle interactions and UI assertions are not claimed as tested. Those remain for the user-approved device/UI phase. This is not full V0.1 release acceptance.
- No blocking specification conflict or approval-required change identified. Stop after this phase.
