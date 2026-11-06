# Contributing

Thanks for your interest in SleepDiary.

## License of contributions

By submitting a pull request or patch, you agree that your contribution is licensed under the same **PolyForm Noncommercial License 1.0.0** as the project. Do not submit code that you cannot license this way.

Commercial use requires separate written permission from the copyright holder.

## Medical and product boundaries

SleepDiary V0.1 is a subjective sleep diary. It is **not** a medical device and does not diagnose insomnia or provide CBT-I treatment, sleep restriction, medication advice, or personalized medical recommendations.

Before changing diary semantics, safety questions, weekly summaries, backup formats, or education content, read:

- `AGENTS.md`
- `CLINICAL_BOUNDARIES.md`
- `DATA_SCHEMA.md`
- `PRODUCT_SPEC.md`
- `UX_SPEC.md`
- `IMPORT_EXPORT_SPEC.md`
- `TEST_PLAN.md`

If an implementation would conflict with a specification, stop and open an issue instead of inventing a medical or product rule.

## Build and test

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

The project is offline-first. Do not add `INTERNET`, analytics, ads, telemetry, accounts, cloud storage, or third-party health SDKs.

## Privacy

Do not put real sleep records, backups, screenshots containing personal data, signing keys, `local.properties`, or device-specific secrets into issues or pull requests.
