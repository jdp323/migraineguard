> Baseline audit: this document records the v1.6.1 takeover findings. Later resolutions and remaining test limits are tracked in [FEATURE_STATUS.md](FEATURE_STATUS.md) and [PHONE_TESTING.md](PHONE_TESTING.md).

# MigraineGuard source review and development priorities

Reviewed 8 October 2026. Baseline: GitHub main fe1fb1672f835ef82fd5691e002934753340110d (v1.6.0). The older Desktop starter was preserved separately. This report describes the current GitHub app and the v1.6.1 fixes on codex/github-build-and-takeover.

## What the app is today

One Android application built with Kotlin, Compose and Material 3. It has a migraine journal with head-map points, hydration history, manual workouts, weight/body profile, medication records, nutrition descriptions, health import, weather and AI chat. Local journals use Room; settings and user AI keys use Preferences DataStore. Google Credential Manager offers sign-in, with demo access for testing. There is no separate Wear OS app, direct watch sensor streaming, production backend, implemented Drive backup or photo recognition pipeline.

The intended Samsung route is Galaxy Watch → Samsung Health on the phone → Health Connect → MigraineGuard. A future Fitbit should use its companion app's supported Health Connect sharing; compatible records must be verified on that device. Do not assume every proprietary watch metric is exposed through Health Connect.

## Findings and status

| Area | Source finding | v1.6.1 / remaining work |
|---|---|---|
| Nutrition | Old fallback generated calories/macros from a description hash and could invent trigger hints. | Fixed: unknown values stay null; descriptions are preserved. Known legacy placeholder records are stripped of those estimates when converted to domain objects. AI estimates remain estimates, with no validated portion recognition. |
| Food photos | Camera/gallery saves a local image reference but never sends image bytes to a model. | UI now says description analysis, requires text and explains the photo limitation. Real multimodal analysis and durable photo storage are future work. |
| AI fallback | Broad failure handling could swallow cancellation; blocking HTTP calls were difficult to cancel. | Fixed: timeout per provider, cancellation propagation, cancellation of actual HTTP calls, blank-response fallback. No live provider requests have been validated by the unit tests. |
| AI model/key handling | Gemini adapter used an obsolete model name and put the key in a URL. Local build properties could inject AI keys. | Updated Gemini model and header, sanitized HTTP error text, forced empty developer-key BuildConfig fields. User keys remain in app-private DataStore; encryption and per-request consent need design. |
| Steps | Raw records were summed over the previous 24 hours. | Fixed: local calendar day and Health Connect step aggregation. User source priority governs activity deduplication. Tests cover midnight and daylight-saving boundaries. |
| Exercise | Missing exercise data was replaced by steps divided by 100. | Removed shortcut. Current minutes come from exercise sessions. Session paging, overlap and clipping at day boundaries still need review. |
| Other health data | Heart rate, sleep, distance, calories and exercise use direct record reads; paging is not implemented. Missing steps/sleep can become zero. | Add typed availability/freshness/source states, partial-permission handling and paginated or aggregate reads. Separate read permission success from optional exercise write permission. |
| Sleep | Latest session within two days is shown; quality labels derive from duration thresholds. | This is a duration heuristic, not a measured sleep-quality score. Add source, sample date and historical nightly aggregates. |
| Insights | Range-filtered migraine/workout/food logs are real, but latest sleep is placed in avgSleepHours and today's steps in totalSteps. Current pressure is described as a trend. | Fixed displayed snapshot labels and recorded-day water wording; AI context now explicitly distinguishes current sleep/steps from period statistics. Demo weather is excluded from the insight pressure summary. Historical health/weather collection is not implemented. Frequency of food logging does not establish a trigger. |
| Weather | Without location/network, fixed demo values are returned and can be described as offline cache. | High priority: replace invented readings with explicit unavailable values or a real timestamped cache; keep them out of medical correlations and AI context. Current per-day memory cache is not historical weather. |
| Hydration | DataStore holds today's cups and Room holds daily history; writes span both stores. Average uses only recorded days. | Improve midnight rollover, transactional consistency and explanation of recorded-day averages. Distinguish not logged from confirmed zero. |
| Persistence | Room database version 4 uses fallbackToDestructiveMigration; historical schemas are not exported. | High priority: recover old schemas, implement and test migrations, add safe export/import. This increment does not change schema version. |
| Android backup | Journals and keys could travel through default backup/transfer behavior. | Automatic backup and transfer excluded. There is currently no replacement user-controlled recovery mechanism; build one before routine phone upgrades. |
| Google sign-in | A baked public web client ID is present. Auth state is memory-only; journals are not partitioned by account. | Register the actual APK certificate, verify sign-in/resume and clarify local profile/account behavior. Cloud authentication is not a cloud journal backup. |
| APK updates | CI runners generate a new default debug key. Existing phone APK signing key may differ. | Establish a stable private signing key using protected configuration. Do not uninstall an app containing important records to work around a signature mismatch. |
| Profile | Both wizard and editor toggle unit labels without converting the entered weight; imperial height initializes to 5 ft 7 regardless of stored height. Future DOB dates can be selected within the current year. | Share a tested body-input model, preserve physical values on unit changes, validate DOB and height/inches, handle locale decimals. |
| Drive | Toggle and local queued label exist, but no API upload, export or OAuth Drive scope. | Fixed user messages to say backup is unavailable and no files are uploaded. Implement encrypted export and explicit authorization before cloud backup. |
| Reminders | WorkManager posts general check-ins every six hours; medication schedules are text, not timed dose reminders. | Revoked notification permission now handled explicitly, fixing lint. Add tap destinations and an intentional reminder schedule later. |
| UI/lifecycle | Setup wizard, foreground/tab refresh and six bottom destinations are implemented. Several UI states use remember rather than saved state; setup errors and back navigation need hardware checks. | Added health-refresh cancellation cleanup. Test rotation, back, keyboard, large font, permission denial and process recreation on the phone. |

## Architecture and file map

Application entry points: MainActivity configures Compose and lifecycle refresh; MigraineGuardApp owns AppContainer. AppContainer constructs Room-backed repositories, preferences, AI providers, weather and Health Connect components. Navigation gates setup/sign-in/profile editing and routes the primary screens.

Core domain models represent journals and snapshots. Entities and converters map them into Room; DAOs provide flows and date queries. Repositories isolate storage/network access, but some composables directly coordinate saves and validation. Extract shared profile inputs and health-range calculations before adding more persona screens.

AI personas are prompts, not independently trained medical specialists. OfflineHealthBrain is a deterministic local rules engine. Cloud completion is selected by provider priority and uses user-configured keys. Context includes summaries from journals/profile/medications; cancellation tests do not establish medical correctness or provider privacy terms.

Suggested product direction: keep MigraineGuard as the migraine-focused hub while making Track, Food, Water and Coach coherent parts of the same personal health timeline. A health analyst should explain observed patterns and missing evidence; a fitness coach should adapt to user goals and available activity/sleep data. Add food portions and reliable nutrition sources before promising accurate calories, and preserve an understandable offline experience.

## Delivery order

1. Finish CI validation of v1.6.1 and retain the APK/test/lint artifacts. Verify signing compatibility before installing on an existing app.
2. Establish stable private signing, journal export/import and non-destructive upgrade migrations.
3. Test Samsung sharing and partial permissions. Add missing-data/freshness states, real paginated/aggregated health history and safe weather handling.
4. Correct historical insight labels/calculations and profile unit conversions. Add regression tests around these data transformations.
5. Add explicit cloud-analysis context preview/consent, provider configuration, structured nutrition responses and optional real photo analysis.
6. Finish reliable reminder schedules and user-controlled encrypted backup; then polish navigation, accessibility and cross-device support.

## Validation boundaries

The initial GitHub baseline compiled but failed lint on notification permission handling. The current increment adds 14 JVM tests for nutrition integrity, fallback priority/failure/timeouts/cancellation, and local-day boundaries. GitHub Actions runs unit tests, lint and debug assembly on the actual PR branch. Results are recorded on the PR and workflow; device integration and live cloud calls require separate verification. No phone was attached during the source review. The local native Git network is restricted; authenticated GitHub connector operations are verified and local history is preserved.

## Official integration references

- [Samsung Health Connect data flow and synchronization](https://developer.samsung.com/health/blog/en/accessing-samsung-health-data-through-health-connect)
- [Samsung Health Connect troubleshooting](https://developer.samsung.com/health/health-connect-faq.html)
- [Android Health Connect aggregation and source priority](https://developer.android.com/health-and-fitness/health-connect/aggregate-data)
- [Current Gemini model catalog](https://ai.google.dev/gemini-api/docs/models)
- [Google Health companion app connections](https://support.google.com/googlehealth/answer/14236613?hl=en)

## Tracked file inventory

The inventory includes source, resources, build configuration, wrapper binaries and test tooling. Generated build output, local settings, caches and old backups are ignored. Binary assets were verified by Git blob hash; source review is not runtime certification.

| File | Responsibility |
|---|---|
| [.github/workflows/android.yml](../.github/workflows/android.yml) | CI build, tests and APK artifacts |
| [.gitignore](../.gitignore) | Generated/sensitive-file exclusions |
| [README.md](../README.md) | Project documentation |
| [app/build.gradle.kts](../app/build.gradle.kts) | Build/dependency tooling |
| app/google-services.json (historical file; removed from current source) | Public Firebase client configuration (not a Gemini key) |
| [app/proguard-rules.pro](../app/proguard-rules.pro) | Project configuration |
| [app/src/main/AndroidManifest.xml](../app/src/main/AndroidManifest.xml) | Permissions and Android components |
| [app/src/main/java/com/migraineguard/app/MainActivity.kt](../app/src/main/java/com/migraineguard/app/MainActivity.kt) | Application entry point |
| [app/src/main/java/com/migraineguard/app/MigraineGuardApp.kt](../app/src/main/java/com/migraineguard/app/MigraineGuardApp.kt) | Application entry point |
| [app/src/main/java/com/migraineguard/app/PACKAGE.md](../app/src/main/java/com/migraineguard/app/PACKAGE.md) | Project documentation |
| [app/src/main/java/com/migraineguard/app/core/ai/AiProvider.kt](../app/src/main/java/com/migraineguard/app/core/ai/AiProvider.kt) | AI provider, persona or local inference logic |
| [app/src/main/java/com/migraineguard/app/core/ai/FallbackAiClient.kt](../app/src/main/java/com/migraineguard/app/core/ai/FallbackAiClient.kt) | AI provider, persona or local inference logic |
| [app/src/main/java/com/migraineguard/app/core/ai/OfflineHealthBrain.kt](../app/src/main/java/com/migraineguard/app/core/ai/OfflineHealthBrain.kt) | AI provider, persona or local inference logic |
| [app/src/main/java/com/migraineguard/app/core/ai/Personas.kt](../app/src/main/java/com/migraineguard/app/core/ai/Personas.kt) | AI provider, persona or local inference logic |
| [app/src/main/java/com/migraineguard/app/core/ai/StubProviders.kt](../app/src/main/java/com/migraineguard/app/core/ai/StubProviders.kt) | AI provider, persona or local inference logic |
| [app/src/main/java/com/migraineguard/app/core/data/Result.kt](../app/src/main/java/com/migraineguard/app/core/data/Result.kt) | Project configuration |
| [app/src/main/java/com/migraineguard/app/core/data/db/AppDatabase.kt](../app/src/main/java/com/migraineguard/app/core/data/db/AppDatabase.kt) | Room persistence and domain mapping |
| [app/src/main/java/com/migraineguard/app/core/data/db/Converters.kt](../app/src/main/java/com/migraineguard/app/core/data/db/Converters.kt) | Room persistence and domain mapping |
| [app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt](../app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt) | Room persistence and domain mapping |
| [app/src/main/java/com/migraineguard/app/core/data/db/Entities.kt](../app/src/main/java/com/migraineguard/app/core/data/db/Entities.kt) | Room persistence and domain mapping |
| [app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt](../app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt) | Preferences and user configuration |
| [app/src/main/java/com/migraineguard/app/core/design/Theme.kt](../app/src/main/java/com/migraineguard/app/core/design/Theme.kt) | Theme and typography |
| [app/src/main/java/com/migraineguard/app/core/design/Type.kt](../app/src/main/java/com/migraineguard/app/core/design/Type.kt) | Theme and typography |
| [app/src/main/java/com/migraineguard/app/core/domain/Models.kt](../app/src/main/java/com/migraineguard/app/core/domain/Models.kt) | Shared domain records |
| [app/src/main/java/com/migraineguard/app/core/time/HealthDay.kt](../app/src/main/java/com/migraineguard/app/core/time/HealthDay.kt) | Calendar-day calculation |
| [app/src/main/java/com/migraineguard/app/core/ui/DateOfBirthField.kt](../app/src/main/java/com/migraineguard/app/core/ui/DateOfBirthField.kt) | Reusable Compose control |
| [app/src/main/java/com/migraineguard/app/core/ui/PlaceholderScreen.kt](../app/src/main/java/com/migraineguard/app/core/ui/PlaceholderScreen.kt) | Reusable Compose control |
| [app/src/main/java/com/migraineguard/app/core/ui/ProgressRing.kt](../app/src/main/java/com/migraineguard/app/core/ui/ProgressRing.kt) | Reusable Compose control |
| [app/src/main/java/com/migraineguard/app/core/ui/SectionCard.kt](../app/src/main/java/com/migraineguard/app/core/ui/SectionCard.kt) | Reusable Compose control |
| [app/src/main/java/com/migraineguard/app/di/AppContainer.kt](../app/src/main/java/com/migraineguard/app/di/AppContainer.kt) | Application dependency wiring |
| [app/src/main/java/com/migraineguard/app/feature/auth/AuthRepository.kt](../app/src/main/java/com/migraineguard/app/feature/auth/AuthRepository.kt) | Feature auth: AuthRepository |
| [app/src/main/java/com/migraineguard/app/feature/auth/SignInScreen.kt](../app/src/main/java/com/migraineguard/app/feature/auth/SignInScreen.kt) | Feature auth: SignInScreen |
| [app/src/main/java/com/migraineguard/app/feature/body/BodyMetricsScreen.kt](../app/src/main/java/com/migraineguard/app/feature/body/BodyMetricsScreen.kt) | Feature body: BodyMetricsScreen |
| [app/src/main/java/com/migraineguard/app/feature/body/WeightRepository.kt](../app/src/main/java/com/migraineguard/app/feature/body/WeightRepository.kt) | Feature body: WeightRepository |
| [app/src/main/java/com/migraineguard/app/feature/coach/AiCoachRepository.kt](../app/src/main/java/com/migraineguard/app/feature/coach/AiCoachRepository.kt) | Feature coach: AiCoachRepository |
| [app/src/main/java/com/migraineguard/app/feature/coach/CoachScreen.kt](../app/src/main/java/com/migraineguard/app/feature/coach/CoachScreen.kt) | Feature coach: CoachScreen |
| [app/src/main/java/com/migraineguard/app/feature/fitness/FitnessStub.kt](../app/src/main/java/com/migraineguard/app/feature/fitness/FitnessStub.kt) | Feature fitness: FitnessStub |
| [app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt](../app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt) | Feature fitness: HealthDataRepository |
| [app/src/main/java/com/migraineguard/app/feature/fitness/HealthRefreshCoordinator.kt](../app/src/main/java/com/migraineguard/app/feature/fitness/HealthRefreshCoordinator.kt) | Feature fitness: HealthRefreshCoordinator |
| [app/src/main/java/com/migraineguard/app/feature/fitness/PermissionsRationaleActivity.kt](../app/src/main/java/com/migraineguard/app/feature/fitness/PermissionsRationaleActivity.kt) | Feature fitness: PermissionsRationaleActivity |
| [app/src/main/java/com/migraineguard/app/feature/fitness/WorkoutRepository.kt](../app/src/main/java/com/migraineguard/app/feature/fitness/WorkoutRepository.kt) | Feature fitness: WorkoutRepository |
| [app/src/main/java/com/migraineguard/app/feature/gemini/ConnectGeminiScreen.kt](../app/src/main/java/com/migraineguard/app/feature/gemini/ConnectGeminiScreen.kt) | Feature gemini: ConnectGeminiScreen |
| [app/src/main/java/com/migraineguard/app/feature/home/HomeScreen.kt](../app/src/main/java/com/migraineguard/app/feature/home/HomeScreen.kt) | Feature home: HomeScreen |
| [app/src/main/java/com/migraineguard/app/feature/insights/InsightsAnalyzer.kt](../app/src/main/java/com/migraineguard/app/feature/insights/InsightsAnalyzer.kt) | Feature insights: InsightsAnalyzer |
| [app/src/main/java/com/migraineguard/app/feature/insights/InsightsScreen.kt](../app/src/main/java/com/migraineguard/app/feature/insights/InsightsScreen.kt) | Feature insights: InsightsScreen |
| [app/src/main/java/com/migraineguard/app/feature/meds/MedicationRepository.kt](../app/src/main/java/com/migraineguard/app/feature/meds/MedicationRepository.kt) | Feature meds: MedicationRepository |
| [app/src/main/java/com/migraineguard/app/feature/meds/MedicationsScreen.kt](../app/src/main/java/com/migraineguard/app/feature/meds/MedicationsScreen.kt) | Feature meds: MedicationsScreen |
| [app/src/main/java/com/migraineguard/app/feature/migraine/MigraineRepository.kt](../app/src/main/java/com/migraineguard/app/feature/migraine/MigraineRepository.kt) | Feature migraine: MigraineRepository |
| [app/src/main/java/com/migraineguard/app/feature/migraine/MigraineScreen.kt](../app/src/main/java/com/migraineguard/app/feature/migraine/MigraineScreen.kt) | Feature migraine: MigraineScreen |
| [app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionAnalyzer.kt](../app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionAnalyzer.kt) | Feature nutrition: NutritionAnalyzer |
| [app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionScreen.kt](../app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionScreen.kt) | Feature nutrition: NutritionScreen |
| [app/src/main/java/com/migraineguard/app/feature/onboarding/OnboardingScreen.kt](../app/src/main/java/com/migraineguard/app/feature/onboarding/OnboardingScreen.kt) | Feature onboarding: OnboardingScreen |
| [app/src/main/java/com/migraineguard/app/feature/profile/ProfileSetupScreen.kt](../app/src/main/java/com/migraineguard/app/feature/profile/ProfileSetupScreen.kt) | Feature profile: ProfileSetupScreen |
| [app/src/main/java/com/migraineguard/app/feature/reminders/ReminderWorker.kt](../app/src/main/java/com/migraineguard/app/feature/reminders/ReminderWorker.kt) | Feature reminders: ReminderWorker |
| [app/src/main/java/com/migraineguard/app/feature/settings/DriveSyncRepository.kt](../app/src/main/java/com/migraineguard/app/feature/settings/DriveSyncRepository.kt) | Feature settings: DriveSyncRepository |
| [app/src/main/java/com/migraineguard/app/feature/settings/SettingsScreen.kt](../app/src/main/java/com/migraineguard/app/feature/settings/SettingsScreen.kt) | Feature settings: SettingsScreen |
| [app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt](../app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt) | Feature setup: SetupWizardScreen |
| [app/src/main/java/com/migraineguard/app/feature/sleep/SleepStub.kt](../app/src/main/java/com/migraineguard/app/feature/sleep/SleepStub.kt) | Feature sleep: SleepStub |
| [app/src/main/java/com/migraineguard/app/feature/track/TrackScreen.kt](../app/src/main/java/com/migraineguard/app/feature/track/TrackScreen.kt) | Feature track: TrackScreen |
| [app/src/main/java/com/migraineguard/app/feature/water/WaterIntakeRepository.kt](../app/src/main/java/com/migraineguard/app/feature/water/WaterIntakeRepository.kt) | Feature water: WaterIntakeRepository |
| [app/src/main/java/com/migraineguard/app/feature/water/WaterScreen.kt](../app/src/main/java/com/migraineguard/app/feature/water/WaterScreen.kt) | Feature water: WaterScreen |
| [app/src/main/java/com/migraineguard/app/feature/weather/WeatherRepository.kt](../app/src/main/java/com/migraineguard/app/feature/weather/WeatherRepository.kt) | Feature weather: WeatherRepository |
| [app/src/main/java/com/migraineguard/app/navigation/MigraineGuardNavHost.kt](../app/src/main/java/com/migraineguard/app/navigation/MigraineGuardNavHost.kt) | Routes and screen navigation |
| [app/src/main/java/com/migraineguard/app/navigation/Routes.kt](../app/src/main/java/com/migraineguard/app/navigation/Routes.kt) | Routes and screen navigation |
| [app/src/main/res/drawable-xxhdpi/head_back.png](../app/src/main/res/drawable-xxhdpi/head_back.png) | Head-map artwork |
| [app/src/main/res/drawable-xxhdpi/head_front.png](../app/src/main/res/drawable-xxhdpi/head_front.png) | Head-map artwork |
| [app/src/main/res/drawable-xxhdpi/head_left.png](../app/src/main/res/drawable-xxhdpi/head_left.png) | Head-map artwork |
| [app/src/main/res/drawable-xxhdpi/head_top.png](../app/src/main/res/drawable-xxhdpi/head_top.png) | Head-map artwork |
| [app/src/main/res/drawable/ic_launcher_foreground.xml](../app/src/main/res/drawable/ic_launcher_foreground.xml) | Android resource configuration |
| [app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml](../app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml) | Android resource configuration |
| [app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml](../app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml) | Android resource configuration |
| [app/src/main/res/values/colors.xml](../app/src/main/res/values/colors.xml) | Android resource configuration |
| [app/src/main/res/values/strings.xml](../app/src/main/res/values/strings.xml) | Android resource configuration |
| [app/src/main/res/values/themes.xml](../app/src/main/res/values/themes.xml) | Android resource configuration |
| [app/src/main/res/xml/backup_rules.xml](../app/src/main/res/xml/backup_rules.xml) | Android resource configuration |
| [app/src/main/res/xml/data_extraction_rules.xml](../app/src/main/res/xml/data_extraction_rules.xml) | Android resource configuration |
| [app/src/main/res/xml/file_paths.xml](../app/src/main/res/xml/file_paths.xml) | Android resource configuration |
| [app/src/test/java/com/migraineguard/app/core/ai/FallbackAiClientTest.kt](../app/src/test/java/com/migraineguard/app/core/ai/FallbackAiClientTest.kt) | Regression tests |
| [app/src/test/java/com/migraineguard/app/core/ai/NutritionIntegrityTest.kt](../app/src/test/java/com/migraineguard/app/core/ai/NutritionIntegrityTest.kt) | Regression tests |
| [app/src/test/java/com/migraineguard/app/core/time/HealthDayTest.kt](../app/src/test/java/com/migraineguard/app/core/time/HealthDayTest.kt) | Regression tests |
| [build.gradle.kts](../build.gradle.kts) | Build/dependency tooling |
| [docs/TAKEOVER.md](../docs/TAKEOVER.md) | Development handover |
| [gradle.properties](../gradle.properties) | Build/dependency tooling |
| [gradle/libs.versions.toml](../gradle/libs.versions.toml) | Build/dependency tooling |
| [gradle/wrapper/gradle-wrapper.jar](../gradle/wrapper/gradle-wrapper.jar) | Build/dependency tooling |
| [gradle/wrapper/gradle-wrapper.properties](../gradle/wrapper/gradle-wrapper.properties) | Build/dependency tooling |
| [gradlew](../gradlew) | Build/dependency tooling |
| [gradlew.bat](../gradlew.bat) | Build/dependency tooling |
| [local.properties.example](../local.properties.example) | Project configuration |
| [settings.gradle.kts](../settings.gradle.kts) | Build/dependency tooling |
| [tools/git-manage.cmd](../tools/git-manage.cmd) | Local Git management |
