# v1.6.4–v1.6.5 — source, access and verification updates

Review date: 2026-10-19. This supplement updates the [v1.6.3 project guide](PROJECT_GUIDE.md) and [file/function reference](FILE_REFERENCE.md). Their source line links remain a documented v1.6.3 snapshot; the changes below describe the newer implementation.

## Access checks

- Native Git read access was also tested noninteractively and still needs a local Git Credential Manager login; it cannot obtain a password. This is separate from the working connected GitHub tool.
- The connected GitHub account can read and push to the private jdp323/migraineguard repository. The current development branch is codex/github-build-and-takeover.
- Browser access to github.com is independently denied by a saved browser permission setting. No alternate browser/security-policy bypass was used. This does not prevent already-authorized repository management through the GitHub connector.
- Reloading the Google Cloud OAuth Clients page still reports missing project access for migraineguard-66229. The exact missing permissions shown are iam.serviceAccounts.list, oauthconfig.verification.get and resourcemanager.projects.get.
- A later read-only project-selector check found an accessible existing Default Gemini Project. Its OAuth platform is not configured. After the user approved their support contact, policy acceptance and OAuth setup, branding and all three clients were created in that project. Testing mode is retained and the approved account was added as its only tester.
- Permission to work on the computer does not grant the signed-in account a role in that Google project. Use the original owner account, or configure matching Web/Android clients in the accessible existing project. No request was sent to another person and no IAM access was silently expanded.
- APK signing continues to use the retained test certificate, not a new private key. No private account email, API key or client-secret value is included in this supplement. Public OAuth client identifiers are application configuration, not signing keys.

## Behavior changes

### One sign-in and profile implementation

SetupWizardScreen's SignInStep now delegates to SignInScreen and ProfileStep delegates to ProfileSetupScreen. The first-run wizard therefore inherits the corrected sign-in busy/cancellation behavior and canonical metric/imperial conversions. This removes duplicate input logic that still relabeled weight without converting it and initialized saved imperial height to a constant. Saving continues to use canonical kilograms/centimeters and existing validation.

The wizard keeps its original ordered stages and optional local/Health Connect/Gemini skip paths. The app does not claim Google login is configured just because a Web client ID exists.

### Date-driven daily state

New localDates returns a Flow<LocalDate>, samples the current device-local date every 30 seconds and emits only changes. Calendar dates handle midnight, timezone changes and clock adjustments without calculating a fixed 24-hour duration.

MedicationRepository now combines the DAO's row Flow with this date Flow. A recorded taken date from yesterday becomes unchecked even if no SQL row changed. It does not erase the historical last-taken timestamp or write a fake medication event. WaterIntakeRepository uses the same date source for its existing daily counter switch.

### Unknown readings stay unknown

HealthSnapshot.observedSteps is nullable: when stepSync exists, its nullable total is authoritative; a failed query no longer becomes a measured zero through the legacy raw steps field. A manually entered snapshot without step diagnostics still retains its actual count, including zero.

SleepSnapshot.recordedHours rejects absent/non-finite/nonpositive duration sentinels. Home no longer generates a short-sleep hint from a missing zero-hour fallback. Insights, Home, Track and offline Coach/Analyst use these availability-aware values. InsightReport.totalSteps and avgSleepHours are nullable, so reports and automatic local narrative can explicitly say unavailable. Current readings remain snapshots rather than historical period totals.

## File changes and new functions

| File | Change / function contract |
|---|---|
| core/time/LocalDates.kt | New localDates(today: () -> LocalDate, pollIntervalMillis: Long): Flow<LocalDate>. Rejects nonpositive poll interval, emits immediately, delays between checks, suppresses repeated dates and stops on cancellation. Defaults to device date and 30 seconds. |
| feature/meds/MedicationRepository.kt | Optional dates Flow constructor input supports deterministic testing. observeMedications combines dates and rows; add/setTakenToday/delete retain existing persistence behavior. |
| feature/water/WaterIntakeRepository.kt | cupsToday uses localDates plus flatMapLatest to switch the Room date subscription. Legacy migration and atomic add/remove semantics are unchanged. |
| feature/setup/SetupWizardScreen.kt | SignInStep/ProfileStep are adapters to shared screens. Existing navigation callbacks, health/key skips and completion remain in the wizard. |
| core/domain/Models.kt | New computed properties observedSteps and recordedHours; InsightReport health fields become nullable. No Room entity/schema change. |
| core/ai/OfflineHealthBrain.kt | Context JSON, coach and analyst paths consume observedSteps; absent readings do not become zero-activity advice. Sleep context uses recordedHours. |
| feature/home/HomeScreen.kt | Availability-aware steps/sleep display and short-sleep hint guard. |
| feature/track/TrackScreen.kt | Step progress uses zero only for drawing an empty progress track; labels keep unavailable as a dash. |
| feature/insights/InsightsAnalyzer.kt | Nullable health snapshot fields and explicit unavailable summary/narrative values. |
| feature/insights/InsightsScreen.kt | Handles nullable report values in overall/fitness/sleep cards; no restorative-quality claim from session duration. |
| app/build.gradle.kts | Version 1.6.4, code 11. Adds Compose instrumentation-test support through the existing Compose BOM. |
| .github/workflows/android.yml | Runtime gate expects six Android tests and release notes include the corrected setup/date/availability behavior. Standard and Preview signing/publishing remain gated by checks. |

Kotlin source paths in the table are relative to app/src/main/java/com/migraineguard/app unless they start with app/ or .github/.

### New test files

| File | Named functions and behavior |
|---|---|
| app/src/test/java/com/migraineguard/app/core/time/LocalDatesTest.kt | midnightAndClockChangesEmitWithoutDatabaseUpdates verifies unchanged dates emit once, date advances are observed and a backward clock/date change is emitted. Uses virtual coroutine time. |
| app/src/test/java/com/migraineguard/app/feature/meds/MedicationDayTest.kt | takenStateResetsAtMidnightWithNoRowWrite injects a date Flow and a synthetic DAO; verifies yesterday's taken state becomes false with no row write. MemoryMedicationDao.observeAll/insert/update/delete supply isolated in-memory behavior. |
| app/src/test/java/com/migraineguard/app/core/domain/ReadingAvailabilityTest.kt | missingHealthReadDoesNotBecomeMeasuredZero; actualZeroAndManualReadingsRemainAvailable; absentOrInvalidSleepCannotTriggerShortSleepHints; emptyInsightReportKeepsHealthSnapshotsUnknown cover data-availability semantics and report defaults. |
| app/src/androidTest/java/com/migraineguard/app/NavigationFlowTest.kt | awaitText bounds UI waiting. onboardingPreservesWeightAndMainTabsOpen uses a synthetic profile, follows local first-run skips, toggles imperial/metric units, verifies the saved 70 kg value and selects all six main tabs in the real activity. It never calls Google login or sends a real key/health summary. |

These are behavioral tests for data loss/misrepresentation and an end-to-end UI regression, rather than tests of trivial formatting. The existing 29 JVM and four Android reliability tests are retained, with an additional Android OAuth-override migration test, for an expected total of 35 JVM tests and six Android tests.

## Upgrade/data safety and remaining verification

The Room schema stays version 4; the journal entities and portable backup format are unchanged. Standard and Preview package IDs are unchanged. An installation using the retained FB:67 signing key can accept a normal higher-version update; incompatible older installations should still be kept intact with Preview installed alongside.

Google registration has now been completed in the accessible owned project. Actual phone sign-in (after installing this APK and allowing propagation), Samsung/watch export accuracy and live Gemini responses remain phone verification items. A successful emulator run does not validate those services or make the application a clinically validated device.

Release verification is complete: [v1.6.4-debug](https://github.com/jdp323/migraineguard/releases/tag/v1.6.4-debug), source fc7faca08b37a7335130748f3cf6b718185cba15, [successful CI run](https://github.com/jdp323/migraineguard/actions/runs/39199310051). All 35 JVM tests, Android lint/build/signature checks and all six API-35 Android tests passed. Downloaded artifacts were hash-verified; package IDs/version 1.6.4/code 11 and retained signatures were independently checked. The compiled DEX contains the new public Web client ID and no Web client secret pattern. The first added navigation test run caught a selector mismatch: the control label is Imperial (ft/in · lb), not Imperial. The test now uses the actual UI labels; publishing stayed blocked during the failure.

## Cloud model compatibility check

Google still lists gemini-3.5-flash-lite as an available multimodal model. The existing request uses the documented generateContent endpoint, x-goog-api-key header and a user contents/parts payload; it does not send the deprecated sampling parameters or prefilled model turns. This confirms configuration compatibility with the documentation, not successful use of a personal key/quota. Live text/photo testing still needs the phone or a supplied test account/key.

References: https://ai.google.dev/gemini-api/docs/models/gemini-3.5-flash-lite and https://ai.google.dev/gemini-api/docs/generate-content/whats-new-gemini-3.6 .

## Current source inventory

The updated project contains 120 tracked files including this supplement, 80 Kotlin source/test files and 399 named function declarations (counting interfaces/implementations separately). The frozen reference covers the 114-file baseline; this supplement covers the five added source/test files and itself, as well as changed behavior in existing files.

New source links:

- [LocalDates.kt](../app/src/main/java/com/migraineguard/app/core/time/LocalDates.kt)
- [LocalDatesTest.kt](../app/src/test/java/com/migraineguard/app/core/time/LocalDatesTest.kt)
- [MedicationDayTest.kt](../app/src/test/java/com/migraineguard/app/feature/meds/MedicationDayTest.kt)
- [ReadingAvailabilityTest.kt](../app/src/test/java/com/migraineguard/app/core/domain/ReadingAvailabilityTest.kt)
- [NavigationFlowTest.kt](../app/src/androidTest/java/com/migraineguard/app/NavigationFlowTest.kt)

## Registered Google sign-in configuration

The original project remains inaccessible; the active application Web client now belongs to the accessible existing Default Gemini Project (gen-lang-client-0426132502). A Web audience client and Android clients for com.migraineguard.app and com.migraineguard.app.preview were created with explicit approval. Both Android clients use the retained FB:67:0D:1D:96:C2:8B:9E:27:0D:36:F8:D6:57:53:E3:6B:30:8D:E8 certificate. The app remains in Testing; use the approved tester account. No app publishing/verification, Google billing activation or other account access was enabled.

The APK contains only the public Web client ID. It does not contain the Web client secret. The old google-services.json is historical unused Firebase client configuration; the build does not apply the Google Services plugin, and Credential Manager uses the explicit BuildConfig Web ID.

Additional changed functions:

- UserPreferencesRepository.preferences now removes a saved override only when it exactly equals the obsolete shipped Web ID and the build uses the new ID. Custom user-entered clients remain intact.
- DefaultAuthRepository.signInWithGoogle now explains that a cancellation may mean account selection did not finish; it preserves the original exception and offers local continuation. A cancellation alone does not prove the user dismissed the picker.
- AppReliabilityTest.legacyOAuthOverrideMigratesButCustomClientIsPreserved verifies the legacy override clears, a custom ID remains, and the previous test setting is restored.

Google states client changes can take five minutes to a few hours to take effect. A successful registration does not constitute an actual phone login test.

## Verified release files

| APK | SHA-256 |
|---|---|
| Standard | ce830a48b185356cbbc4aeb38a81b9dd45be89d6850c750a2be1bb19e39e2593 |
| Preview | c0a138865cc512fd71a49e252c528d535a91ee76ec5d5d2c1425c0507c7cf131 |

Both digests match GitHub Release asset digests. The release includes the signing-certificate report and SHA256SUMS. Documentation-only commits following the application source do not replace the verified release binaries. The current private repository is still managed through the connected GitHub tool; the separately saved browser block for github.com remains unresolved.

## v1.6.5 displayed-version correction

Final screenshot inspection found that v1.6.4 was correctly packaged as version 1.6.4/code 11 but its welcome text still used a hardcoded v1.6.3 label. The follow-up uses a formatted string resource plus BuildConfig.VERSION_NAME in both WelcomeStep and SignInScreen, version 1.6.5/code 12. NavigationFlowTest asserts the displayed version at both points before continuing. Existing OAuth registration, package IDs, signing key, journal schema and data behavior are unchanged.

Files changed: app/build.gradle.kts; app/src/main/res/values/strings.xml; feature/auth/SignInScreen.kt; feature/setup/SetupWizardScreen.kt; app/src/androidTest/java/com/migraineguard/app/NavigationFlowTest.kt; release notes in .github/workflows/android.yml. No named functions or source files were added, so inventory counts remain unchanged.

### Final v1.6.5 verification

[Latest verified release v1.6.5-debug](https://github.com/jdp323/migraineguard/releases/tag/v1.6.5-debug) is built from source 0020c6437d8c5f2cf7835d99844a520d6f04d796. [CI run 39200463322](https://github.com/jdp323/migraineguard/actions/runs/39200463322) passed all 35 JVM tests, lint/build/signing checks and all six Android tests. The navigation test verifies the welcome and sign-in version labels, unit conversion and all six main tabs. The actual post-test screenshot was inspected and shows v1.6.5.

| v1.6.5 APK | Verified SHA-256 |
|---|---|
| Standard | 68dbde1e129fce64f4cdf239ff772f58da5e8e0a5ee6f534daa8a9acff4412b1 |
| Preview | ecfb8e7e1fbe08e32d4fb0c564a9261a012b903d516f154a70cb2d91d39f24b2 |

Both downloaded APKs have code 12, version 1.6.5, the expected Standard/Preview package IDs and the retained FB:67 signer; release asset digests match. Current public client registration and actual phone/Watch/Gemini verification limits are unchanged.
