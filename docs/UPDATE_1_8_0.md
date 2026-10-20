# v1.8.0: refreshed interface and verified setup

Version 1.8.0 / code 15. This extends the [project guide](PROJECT_GUIDE.md), [file reference](FILE_REFERENCE.md), [v1.7.0 account/backup supplement](UPDATE_1_7_0.md) and [public Google access record](PUBLIC_GOOGLE_ACCESS.md).

## Design and navigation

Coordinated light/dark backgrounds, Material surface-container colors, feature introductions, icon tiles and stronger headings now cover the main app and secondary screens. Home has an illustrated daily overview and a two-column action grid for meals, movement, measurements and medication. Duplicate Home metric and navigation sections were removed. Existing journal, health, AI and backup behavior remains available.

The six destinations retain their names and routes. A compact custom tab bar gives each label its full column width, avoiding the old built-in item's horizontal padding that clipped Migraine at narrow sizes. Tabs expose selected state and role to accessibility/test tools. Existing back-stack save/restore remains; page entry/pop uses short fade/translation transitions. Water now has a visible Back control. Form and account data are not changed by appearance selection.

Daily habit rings and standalone progress rings animate when values change; ring drawing is inset so rounded strokes are not clipped. Background illustrations are code-native Canvas shapes, with no image downloads, placeholder health values, remote fonts or additional graphics libraries. Unknown health readings remain unavailable. Cards no longer animate their entire height on every streamed AI chunk.

Inspired by [Material 3](https://m3.material.io/) and [Android Compose motion guidance](https://developer.android.com/develop/ui/compose/animation/quick-guide); existing Compose versions are retained. Motion uses standard Compose animation APIs and follows the platform duration scale.

## What a setup check means

- Account: a real Google sign-in or explicitly selected local profile. Local mode is identified as local; it does not imply Google/Drive access.
- Health service: Health Connect availability was checked.
- Health permissions: all requested read permissions were checked for this account's phone-health opt-in. Optional denied types do not block continuing.
- Samsung sharing: a returned source, positive Samsung aggregate, dated Samsung heart sample or completed Samsung sleep record identifies Samsung Health. Installing/opening Samsung Health is not proof of sharing.
- Steps: a Health Connect step-sync result was received, including a real zero. A manual/demo count cannot prove a connection. Raw fallback remains labeled as an estimate.
- Heart/sleep: a dated heart sample or positive completed sleep session was returned. Heart is not live; older sleep is explicitly identified. Permissions without records stay unverified.
- Gemini: the saved key received a successful connection-test response. Changing the key clears that confirmation. Success stays on screen until Continue. Opening AI Studio is a manual instruction, not a completed check.
- Drive: the selected account identity and app-data lookup/restore completed. First-time manual connections show a success check and Continue. Previously granted automatic returning-user restore continues without adding a new stop.
- Profile: validated details have been saved. Save failures show an error and do not advance.

Setup has a five-part progress header and Back controls for editable earlier steps. The final review marks optional omissions as Skipped rather than turning everything green. It is a record of that setup run, not a permanent health/AI connectivity guarantee. Health checks rerun on resume/recheck and clear old evidence before reading; exceptions cannot leave previous green checks visible.

## File and function reference

Application paths are relative to app/src/main/java/com/migraineguard/app/.

| File | Responsibilities and changed functions |
| --- | --- |
| core/ui/AppChrome.kt (new) | AppTopBar shares the background/title treatment. FeatureHeader draws an adaptive themed intro with decorative Canvas circles. ActionTile exposes a complete clickable destination. CheckState distinguishes waiting/checking/verified/attention/skipped. VerificationRow gives the result an accessible icon, polite live region and stable test tag. |
| core/ui/SectionCard.kt | Coordinates spacing, icon containers and outline styling; title gets remaining row width. Removes whole-card content-size animation that could fight streamed content. |
| core/ui/DailyOverview.kt | Gradient/Canvas overview, adaptive metric columns and AnimatedContent value changes. RhythmMetric retains actual values and goals and exposes clickable habit destinations. |
| core/ui/ProgressRing.kt | Bounded animateFloatAsState progress and inset round-cap arcs. |
| core/design/Theme.kt | Coordinates page/surface-container colors in light/dark while retaining system, user theme and dynamic-color options. |
| navigation/MigraineGuardNavHost.kt | Compact selectable six-tab bar, saved tab navigation, fade/slide entry/pop transitions and Water Back callback. Existing account/Drive/health gates remain. |
| feature/home/HomeScreen.kt | New action grid, less duplicate content, greeting/date/hero hierarchy, shared top bar and corrected hydration-streak recomputation dependencies. |
| feature/setup/HealthSetupEvidence.kt (new) | Separates source/record evidence from permission state. Real zero is valid; missing, demo/manual and unavailable service readings cannot produce data checks. |
| feature/setup/HealthConnectSetup.kt (new) | HealthConnectStep owns lifecycle/recheck jobs, generation guarding, native permission launch/fallback, optional continuation and individual result rows. No health permission is inferred from Google login. |
| feature/setup/SetupProgressHeader.kt (new) | SetupCheck models each step's title/detail/state; SetupProgressHeader renders the five steps with truthful completion semantics. |
| feature/setup/SetupWizardScreen.kt | Remembered step/status progress, optional Back handling, animated step changes and final result review. GeminiStep retains the tested-key identity until editing, exposes actual success, catches save/test errors and keeps failure distinct from skip. DoneStep summarizes verified/skipped states before persisting completion. |
| feature/profile/ProfileSetupScreen.kt | Shared top bar; saving state prevents repeat submissions and catches persistence failure before onComplete. |
| feature/settings/DriveBackupScreen.kt | Shared feature intro and verified account rows. Manual connection success waits for Continue; silent auto-restore keeps its existing flow. DriveBackupCard shows connection work/result alongside actual save status. |
| feature/auth/SignInScreen.kt | Scrollable small-screen sign-in and less developer-oriented account copy. |
| feature/track/TrackScreen.kt | Feature header, width-aware metric text, waiting text for unknown steps and shared cards/chrome. Read/source/diagnostic logic retained. |
| feature/migraine/MigraineScreen.kt | Journal intro and shared chrome; head map, marker editing/history/persistence remain. |
| feature/nutrition/NutritionScreen.kt | Food intro and shared chrome/cards; photo sharing still requires explicit opt-in. |
| feature/coach/CoachScreen.kt | Companion intro, wrapped persona/action chips and quieter safety panel; sharing controls/provider routing retained. |
| feature/insights/InsightsScreen.kt | Insights intro/shared chrome; real report sections and range/mode controls retained. |
| feature/water/WaterScreen.kt | Hydration intro, animated bounded ring and visible Back callback; Room counter/history unchanged. |
| feature/body/BodyMetricsScreen.kt, feature/meds/MedicationsScreen.kt | Themed introductions and shared top bars/cards with existing history/forms. |
| feature/settings/SettingsScreen.kt | Settings intro and shared top bar/cards; existing connections, appearance, goals and privacy actions retained. |
| feature/fitness/PermissionsRationaleActivity.kt | Uses the shared top bar; permission-rationale behavior unchanged. |
| app/build.gradle.kts | Version/code increment and Compose debug test-host manifest for standalone setup verification. No new production service/library. |
| .github/workflows/android.yml | Runtime gate expects 20 Android tests and publishes new release notes after checks/signature/runtime jobs pass. |

## Validation

HealthSetupEvidenceTest adds five JVM cases: grants without records, manual/demo values, genuine zero versus writer proof, Samsung heart/sleep without steps, and stale data with unavailable service. SetupVerificationTest adds two Android cases: permissions without data never show data checkmarks, and actual records check successfully then clear after revoked access/read failure.

NavigationFlowTest keeps unit-conversion/persistence assertions, verifies skipped Health/AI on the review screen, walks all six tabs in light/dark, and captures setup, hydration and settings. Captures use the current Compose root after idle, avoiding an earlier system-screenshot transition race. Test data is synthetic; no real account, medical records or keys are used.

Local compilation, JVM tests, lint and test/APK assembly plus hosted Android execution and retained certificate verification must pass before release. Results and inspected screenshots will be added once verification completes. Real watch sync, native Google consent and live provider requests still require phone acceptance.
