> For all new and modified files/functions after the baseline, also read [v1.6.6 account and health changes](UPDATE_1_6_6.md) and [v1.6.4–v1.6.5](UPDATE_1_6_4.md).

# MigraineGuard — file and function reference

Current update: [v1.6.4 changes, new files/functions and access checks](UPDATE_1_6_4.md). The detailed source-line reference below preserves the v1.6.3 baseline.

Application baseline: **v1.6.3**, source commit `61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa`, reviewed 2026-10-08. Companion: [complete project guide](PROJECT_GUIDE.md).

This reference covers every tracked project file at the application baseline, plus the documentation and generated schema/startup evidence added with this guide. Build/IDE caches, private local properties/signing keys, downloaded APKs and `.git` internals are excluded from the source inventory and explained in the guide. Source line links are pinned to the documented v1.6.3 commit, so later edits cannot make the function links point at unrelated lines. Named declarations/signatures are indexed from source; anonymous Compose callbacks and compiler-generated Room/serialization/Compose functions belong to their owning file and are not separately hand-authored functions.

## Reading the code

A `suspend fun` may wait without blocking its caller thread; repositories use it for storage/network work. `Flow<T>` emits later changes rather than only one value; UI collection is lifecycle-aware. `@Composable` describes UI and must keep long-running effects in coroutines/effects. Room `@Query`, `@Insert`, `@Update` and `@Transaction` specify generated database behavior. `@Serializable` enables backup serialization. Kotlin `?` indicates a missing value is valid, which must not silently become a fabricated reading. Functions listed twice can be an interface contract and implementation, or methods on different classes in one file.

## File index

| File | Size | Named functions |
|---|---:|---:|
| [.github/workflows/android.yml](../.github/workflows/android.yml) | 8511 bytes | 0 |
| [.gitignore](../.gitignore) | 730 bytes | 0 |
| [app/build.gradle.kts](../app/build.gradle.kts) | 4620 bytes | 1 |
| [app/google-services.json](../app/google-services.json) | 2035 bytes | 0 |
| [app/proguard-rules.pro](../app/proguard-rules.pro) | 151 bytes | 0 |
| [app/schemas/com.migraineguard.app.core.data.db.AppDatabase/4.json](../app/schemas/com.migraineguard.app.core.data.db.AppDatabase/4.json) | 11186 bytes | 0 |
| [app/src/androidTest/java/com/migraineguard/app/AppReliabilityTest.kt](../app/src/androidTest/java/com/migraineguard/app/AppReliabilityTest.kt) | 6858 bytes | 4 |
| [app/src/main/AndroidManifest.xml](../app/src/main/AndroidManifest.xml) | 4712 bytes | 0 |
| [app/src/main/java/com/migraineguard/app/core/ai/AiProvider.kt](../app/src/main/java/com/migraineguard/app/core/ai/AiProvider.kt) | 266 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/core/ai/FallbackAiClient.kt](../app/src/main/java/com/migraineguard/app/core/ai/FallbackAiClient.kt) | 6641 bytes | 10 |
| [app/src/main/java/com/migraineguard/app/core/ai/OfflineHealthBrain.kt](../app/src/main/java/com/migraineguard/app/core/ai/OfflineHealthBrain.kt) | 12553 bytes | 8 |
| [app/src/main/java/com/migraineguard/app/core/ai/Personas.kt](../app/src/main/java/com/migraineguard/app/core/ai/Personas.kt) | 2388 bytes | 2 |
| [app/src/main/java/com/migraineguard/app/core/ai/StubProviders.kt](../app/src/main/java/com/migraineguard/app/core/ai/StubProviders.kt) | 6917 bytes | 11 |
| [app/src/main/java/com/migraineguard/app/core/data/db/AppDatabase.kt](../app/src/main/java/com/migraineguard/app/core/data/db/AppDatabase.kt) | 1428 bytes | 9 |
| [app/src/main/java/com/migraineguard/app/core/data/db/BackupDao.kt](../app/src/main/java/com/migraineguard/app/core/data/db/BackupDao.kt) | 751 bytes | 7 |
| [app/src/main/java/com/migraineguard/app/core/data/db/Converters.kt](../app/src/main/java/com/migraineguard/app/core/data/db/Converters.kt) | 1916 bytes | 6 |
| [app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt](../app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt) | 4387 bytes | 33 |
| [app/src/main/java/com/migraineguard/app/core/data/db/Entities.kt](../app/src/main/java/com/migraineguard/app/core/data/db/Entities.kt) | 6381 bytes | 11 |
| [app/src/main/java/com/migraineguard/app/core/data/db/PreservingMigrations.kt](../app/src/main/java/com/migraineguard/app/core/data/db/PreservingMigrations.kt) | 1898 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt](../app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt) | 11706 bytes | 31 |
| [app/src/main/java/com/migraineguard/app/core/data/Result.kt](../app/src/main/java/com/migraineguard/app/core/data/Result.kt) | 278 bytes | 0 |
| [app/src/main/java/com/migraineguard/app/core/design/Theme.kt](../app/src/main/java/com/migraineguard/app/core/design/Theme.kt) | 3221 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/core/design/Type.kt](../app/src/main/java/com/migraineguard/app/core/design/Type.kt) | 2211 bytes | 0 |
| [app/src/main/java/com/migraineguard/app/core/domain/Models.kt](../app/src/main/java/com/migraineguard/app/core/domain/Models.kt) | 5789 bytes | 0 |
| [app/src/main/java/com/migraineguard/app/core/security/ApiKeyVault.kt](../app/src/main/java/com/migraineguard/app/core/security/ApiKeyVault.kt) | 2228 bytes | 3 |
| [app/src/main/java/com/migraineguard/app/core/security/BackupCipher.kt](../app/src/main/java/com/migraineguard/app/core/security/BackupCipher.kt) | 2267 bytes | 3 |
| [app/src/main/java/com/migraineguard/app/core/security/SigningCertificate.kt](../app/src/main/java/com/migraineguard/app/core/security/SigningCertificate.kt) | 1062 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/core/time/HealthDay.kt](../app/src/main/java/com/migraineguard/app/core/time/HealthDay.kt) | 556 bytes | 2 |
| [app/src/main/java/com/migraineguard/app/core/ui/DateOfBirthField.kt](../app/src/main/java/com/migraineguard/app/core/ui/DateOfBirthField.kt) | 3918 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/core/ui/PlaceholderScreen.kt](../app/src/main/java/com/migraineguard/app/core/ui/PlaceholderScreen.kt) | 133 bytes | 0 |
| [app/src/main/java/com/migraineguard/app/core/ui/ProgressRing.kt](../app/src/main/java/com/migraineguard/app/core/ui/ProgressRing.kt) | 1645 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/core/ui/SectionCard.kt](../app/src/main/java/com/migraineguard/app/core/ui/SectionCard.kt) | 3125 bytes | 2 |
| [app/src/main/java/com/migraineguard/app/di/AppContainer.kt](../app/src/main/java/com/migraineguard/app/di/AppContainer.kt) | 4498 bytes | 0 |
| [app/src/main/java/com/migraineguard/app/feature/auth/AuthRepository.kt](../app/src/main/java/com/migraineguard/app/feature/auth/AuthRepository.kt) | 5911 bytes | 11 |
| [app/src/main/java/com/migraineguard/app/feature/auth/SignInScreen.kt](../app/src/main/java/com/migraineguard/app/feature/auth/SignInScreen.kt) | 6680 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/feature/backup/BackupCard.kt](../app/src/main/java/com/migraineguard/app/feature/backup/BackupCard.kt) | 3638 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/feature/backup/BackupRepository.kt](../app/src/main/java/com/migraineguard/app/feature/backup/BackupRepository.kt) | 6626 bytes | 4 |
| [app/src/main/java/com/migraineguard/app/feature/body/BodyMetricsScreen.kt](../app/src/main/java/com/migraineguard/app/feature/body/BodyMetricsScreen.kt) | 7561 bytes | 2 |
| [app/src/main/java/com/migraineguard/app/feature/body/WeightRepository.kt](../app/src/main/java/com/migraineguard/app/feature/body/WeightRepository.kt) | 693 bytes | 4 |
| [app/src/main/java/com/migraineguard/app/feature/coach/AiCoachRepository.kt](../app/src/main/java/com/migraineguard/app/feature/coach/AiCoachRepository.kt) | 6054 bytes | 7 |
| [app/src/main/java/com/migraineguard/app/feature/coach/CoachScreen.kt](../app/src/main/java/com/migraineguard/app/feature/coach/CoachScreen.kt) | 12082 bytes | 3 |
| [app/src/main/java/com/migraineguard/app/feature/fitness/FitnessStub.kt](../app/src/main/java/com/migraineguard/app/feature/fitness/FitnessStub.kt) | 144 bytes | 0 |
| [app/src/main/java/com/migraineguard/app/feature/fitness/HealthAutoRefresh.kt](../app/src/main/java/com/migraineguard/app/feature/fitness/HealthAutoRefresh.kt) | 845 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt](../app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt) | 23864 bytes | 59 |
| [app/src/main/java/com/migraineguard/app/feature/fitness/HealthRefreshCoordinator.kt](../app/src/main/java/com/migraineguard/app/feature/fitness/HealthRefreshCoordinator.kt) | 3349 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/feature/fitness/PermissionsRationaleActivity.kt](../app/src/main/java/com/migraineguard/app/feature/fitness/PermissionsRationaleActivity.kt) | 3467 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/feature/fitness/StepSyncMessage.kt](../app/src/main/java/com/migraineguard/app/feature/fitness/StepSyncMessage.kt) | 1037 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/feature/fitness/WorkoutRepository.kt](../app/src/main/java/com/migraineguard/app/feature/fitness/WorkoutRepository.kt) | 2485 bytes | 5 |
| [app/src/main/java/com/migraineguard/app/feature/gemini/ConnectGeminiScreen.kt](../app/src/main/java/com/migraineguard/app/feature/gemini/ConnectGeminiScreen.kt) | 7932 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/feature/home/HomeScreen.kt](../app/src/main/java/com/migraineguard/app/feature/home/HomeScreen.kt) | 17010 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/feature/insights/InsightsAnalyzer.kt](../app/src/main/java/com/migraineguard/app/feature/insights/InsightsAnalyzer.kt) | 5423 bytes | 6 |
| [app/src/main/java/com/migraineguard/app/feature/insights/InsightsScreen.kt](../app/src/main/java/com/migraineguard/app/feature/insights/InsightsScreen.kt) | 13220 bytes | 2 |
| [app/src/main/java/com/migraineguard/app/feature/meds/MedicationRepository.kt](../app/src/main/java/com/migraineguard/app/feature/meds/MedicationRepository.kt) | 1425 bytes | 5 |
| [app/src/main/java/com/migraineguard/app/feature/meds/MedicationsScreen.kt](../app/src/main/java/com/migraineguard/app/feature/meds/MedicationsScreen.kt) | 5831 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/feature/migraine/MigraineRepository.kt](../app/src/main/java/com/migraineguard/app/feature/migraine/MigraineRepository.kt) | 899 bytes | 5 |
| [app/src/main/java/com/migraineguard/app/feature/migraine/MigraineScreen.kt](../app/src/main/java/com/migraineguard/app/feature/migraine/MigraineScreen.kt) | 26455 bytes | 7 |
| [app/src/main/java/com/migraineguard/app/feature/nutrition/FoodImage.kt](../app/src/main/java/com/migraineguard/app/feature/nutrition/FoodImage.kt) | 1999 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionAnalyzer.kt](../app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionAnalyzer.kt) | 3121 bytes | 8 |
| [app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionScreen.kt](../app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionScreen.kt) | 10640 bytes | 4 |
| [app/src/main/java/com/migraineguard/app/feature/onboarding/OnboardingScreen.kt](../app/src/main/java/com/migraineguard/app/feature/onboarding/OnboardingScreen.kt) | 4127 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/feature/profile/BodyUnits.kt](../app/src/main/java/com/migraineguard/app/feature/profile/BodyUnits.kt) | 404 bytes | 2 |
| [app/src/main/java/com/migraineguard/app/feature/profile/ProfileSetupScreen.kt](../app/src/main/java/com/migraineguard/app/feature/profile/ProfileSetupScreen.kt) | 11794 bytes | 2 |
| [app/src/main/java/com/migraineguard/app/feature/reminders/ReminderWorker.kt](../app/src/main/java/com/migraineguard/app/feature/reminders/ReminderWorker.kt) | 3952 bytes | 4 |
| [app/src/main/java/com/migraineguard/app/feature/settings/DriveSyncRepository.kt](../app/src/main/java/com/migraineguard/app/feature/settings/DriveSyncRepository.kt) | 2096 bytes | 10 |
| [app/src/main/java/com/migraineguard/app/feature/settings/SettingsScreen.kt](../app/src/main/java/com/migraineguard/app/feature/settings/SettingsScreen.kt) | 25208 bytes | 3 |
| [app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt](../app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt) | 39764 bytes | 13 |
| [app/src/main/java/com/migraineguard/app/feature/sleep/SleepStub.kt](../app/src/main/java/com/migraineguard/app/feature/sleep/SleepStub.kt) | 137 bytes | 0 |
| [app/src/main/java/com/migraineguard/app/feature/track/TrackScreen.kt](../app/src/main/java/com/migraineguard/app/feature/track/TrackScreen.kt) | 31615 bytes | 10 |
| [app/src/main/java/com/migraineguard/app/feature/water/WaterIntakeRepository.kt](../app/src/main/java/com/migraineguard/app/feature/water/WaterIntakeRepository.kt) | 3072 bytes | 6 |
| [app/src/main/java/com/migraineguard/app/feature/water/WaterScreen.kt](../app/src/main/java/com/migraineguard/app/feature/water/WaterScreen.kt) | 5524 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/feature/weather/WeatherRepository.kt](../app/src/main/java/com/migraineguard/app/feature/weather/WeatherRepository.kt) | 4497 bytes | 5 |
| [app/src/main/java/com/migraineguard/app/MainActivity.kt](../app/src/main/java/com/migraineguard/app/MainActivity.kt) | 1591 bytes | 2 |
| [app/src/main/java/com/migraineguard/app/MigraineGuardApp.kt](../app/src/main/java/com/migraineguard/app/MigraineGuardApp.kt) | 478 bytes | 1 |
| [app/src/main/java/com/migraineguard/app/navigation/MigraineGuardNavHost.kt](../app/src/main/java/com/migraineguard/app/navigation/MigraineGuardNavHost.kt) | 11046 bytes | 3 |
| [app/src/main/java/com/migraineguard/app/navigation/Routes.kt](../app/src/main/java/com/migraineguard/app/navigation/Routes.kt) | 811 bytes | 0 |
| [app/src/main/java/com/migraineguard/app/PACKAGE.md](../app/src/main/java/com/migraineguard/app/PACKAGE.md) | 428 bytes | 0 |
| [app/src/main/res/drawable-xxhdpi/head_back.png](../app/src/main/res/drawable-xxhdpi/head_back.png) | 141709 bytes | 0 |
| [app/src/main/res/drawable-xxhdpi/head_front.png](../app/src/main/res/drawable-xxhdpi/head_front.png) | 208014 bytes | 0 |
| [app/src/main/res/drawable-xxhdpi/head_left.png](../app/src/main/res/drawable-xxhdpi/head_left.png) | 212678 bytes | 0 |
| [app/src/main/res/drawable-xxhdpi/head_top.png](../app/src/main/res/drawable-xxhdpi/head_top.png) | 208014 bytes | 0 |
| [app/src/main/res/drawable/ic_launcher_foreground.xml](../app/src/main/res/drawable/ic_launcher_foreground.xml) | 618 bytes | 0 |
| [app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml](../app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml) | 270 bytes | 0 |
| [app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml](../app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml) | 270 bytes | 0 |
| [app/src/main/res/values/colors.xml](../app/src/main/res/values/colors.xml) | 211 bytes | 0 |
| [app/src/main/res/values/strings.xml](../app/src/main/res/values/strings.xml) | 1821 bytes | 0 |
| [app/src/main/res/values/themes.xml](../app/src/main/res/values/themes.xml) | 326 bytes | 0 |
| [app/src/main/res/xml/backup_rules.xml](../app/src/main/res/xml/backup_rules.xml) | 313 bytes | 0 |
| [app/src/main/res/xml/data_extraction_rules.xml](../app/src/main/res/xml/data_extraction_rules.xml) | 630 bytes | 0 |
| [app/src/main/res/xml/file_paths.xml](../app/src/main/res/xml/file_paths.xml) | 160 bytes | 0 |
| [app/src/test/java/com/migraineguard/app/core/ai/FallbackAiClientTest.kt](../app/src/test/java/com/migraineguard/app/core/ai/FallbackAiClientTest.kt) | 3087 bytes | 7 |
| [app/src/test/java/com/migraineguard/app/core/ai/NutritionIntegrityTest.kt](../app/src/test/java/com/migraineguard/app/core/ai/NutritionIntegrityTest.kt) | 3472 bytes | 7 |
| [app/src/test/java/com/migraineguard/app/core/security/BackupCipherTest.kt](../app/src/test/java/com/migraineguard/app/core/security/BackupCipherTest.kt) | 1207 bytes | 3 |
| [app/src/test/java/com/migraineguard/app/core/time/HealthDayTest.kt](../app/src/test/java/com/migraineguard/app/core/time/HealthDayTest.kt) | 1577 bytes | 5 |
| [app/src/test/java/com/migraineguard/app/feature/backup/BackupMergeTest.kt](../app/src/test/java/com/migraineguard/app/feature/backup/BackupMergeTest.kt) | 967 bytes | 2 |
| [app/src/test/java/com/migraineguard/app/feature/fitness/StepSyncMessageTest.kt](../app/src/test/java/com/migraineguard/app/feature/fitness/StepSyncMessageTest.kt) | 1031 bytes | 5 |
| [app/src/test/java/com/migraineguard/app/feature/profile/BodyUnitsTest.kt](../app/src/test/java/com/migraineguard/app/feature/profile/BodyUnitsTest.kt) | 589 bytes | 2 |
| [build.gradle.kts](../build.gradle.kts) | 310 bytes | 0 |
| [docs/FEATURE_STATUS.md](../docs/FEATURE_STATUS.md) | 3926 bytes | 0 |
| [docs/images/android-startup-v1.6.3.png](../docs/images/android-startup-v1.6.3.png) | 37837 bytes | 0 |
| [docs/PHONE_TESTING.md](../docs/PHONE_TESTING.md) | 4320 bytes | 0 |
| [docs/PROJECT_AUDIT.md](../docs/PROJECT_AUDIT.md) | 25300 bytes | 0 |
| [docs/PROJECT_GUIDE.md](../docs/PROJECT_GUIDE.md) | 30100 bytes | 0 |
| [docs/TAKEOVER.md](../docs/TAKEOVER.md) | 4522 bytes | 0 |
| [gradle.properties](../gradle.properties) | 136 bytes | 0 |
| [gradle/libs.versions.toml](../gradle/libs.versions.toml) | 3719 bytes | 0 |
| [gradle/wrapper/gradle-wrapper.jar](../gradle/wrapper/gradle-wrapper.jar) | 43583 bytes | 0 |
| [gradle/wrapper/gradle-wrapper.properties](../gradle/wrapper/gradle-wrapper.properties) | 253 bytes | 0 |
| [gradlew](../gradlew) | 8784 bytes | 0 |
| [gradlew.bat](../gradlew.bat) | 2894 bytes | 0 |
| [local.properties.example](../local.properties.example) | 627 bytes | 0 |
| [README.md](../README.md) | 6968 bytes | 0 |
| [settings.gradle.kts](../settings.gradle.kts) | 538 bytes | 0 |
| [tools/git-manage.cmd](../tools/git-manage.cmd) | 2719 bytes | 0 |
| [docs/FILE_REFERENCE.md](FILE_REFERENCE.md) | This reference | 0 |

## Detailed reference

### .github/workflows/android.yml

[Open source](../.github/workflows/android.yml) · 8511 bytes · 169 lines

**Purpose and behavior.** Builds/tests the application, restores and verifies the persistent test signer, builds Standard and Preview APKs, runs Android instrumentation in a hosted emulator, saves reports/schema/startup evidence, and publishes a new release only after successful checks. The workflow never overwrites an existing release tag.

### .gitignore

[Open source](../.gitignore) · 730 bytes · 70 lines

**Purpose and behavior.** Excludes machine-specific SDK/IDE files, build outputs, local signing material, verification downloads, distribution APKs and backup artifacts from source control. Ignoring a path does not remove something that was already committed; audit history separately.

### app/build.gradle.kts

[Open source](../app/build.gradle.kts) · 4620 bytes · 137 lines

**Purpose and behavior.** Android module build configuration. Selects Standard/Preview applicationId and label from MG_PREVIEW; sets versionCode 10/versionName 1.6.3, SDK levels, Compose/BuildConfig, Kotlin serialization/KSP/Room schema export, dependencies, test runner and explicit debug keystore path. Builds no developer AI keys into the app.

**Implementation and maintenance.** localOrDefault applies configuration precedence and quoting for Web OAuth BuildConfig. Empty AI fields are deliberate. The runtime namespace stays com.migraineguard.app even when Preview changes applicationId; instrumentation APK is built before the Preview output replaces app-debug.apk.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun localOrDefault(key: String, default: String = ""): String` | Build helper choosing local.properties, then Gradle project property, then supplied default; escapes backslashes/quotes for the generated BuildConfig string. AI keys use explicit empty fields rather than this helper. | [L16](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/build.gradle.kts#L16) |

### app/google-services.json

[Open source](../app/google-services.json) · 2035 bytes · 65 lines

**Purpose and behavior.** Existing Firebase/Google client configuration used to identify the original project and OAuth clients. It is client configuration, not the user's Gemini key. Project access and OAuth registration remain external requirements; do not copy its values into AI settings or publish unsanitized repository history.

### app/proguard-rules.pro

[Open source](../app/proguard-rules.pro) · 151 bytes · 5 lines

**Purpose and behavior.** Project-specific shrinker rules. Debug builds are the tested artifacts; changes here matter when release shrinking is enabled and require separate release validation.

### app/schemas/com.migraineguard.app.core.data.db.AppDatabase/4.json

[Open source](../app/schemas/com.migraineguard.app.core.data.db.AppDatabase/4.json) · 11186 bytes · 376 lines

**Purpose and behavior.** Room-generated schema version 4 from the successful build. Contains table/column/primary-key definitions, create SQL and identity hash; no journal records or user credentials. Preserve as a migration design reference.

**Schema identity:** `54104f62ecc30231c558fb4fa54b83bd` · version 4.

| Table | Primary key | Columns |
|---|---|---|
| `migraine_episodes` | `id` | `id` INTEGER NOT NULL; `timestampMillis` INTEGER NOT NULL; `overallIntensity` INTEGER NOT NULL; `notes` TEXT NOT NULL; `symptoms` TEXT NOT NULL; `markers` TEXT NOT NULL; `triggers` TEXT NOT NULL |
| `nutrition_scans` | `id` | `id` INTEGER NOT NULL; `timestampMillis` INTEGER NOT NULL; `foodLabel` TEXT NOT NULL; `caloriesEstimate` INTEGER nullable; `proteinG` REAL nullable; `carbsG` REAL nullable; `fatG` REAL nullable; `notes` TEXT NOT NULL; `imageUri` TEXT nullable; `migraineTriggerHint` TEXT nullable |
| `water_history` | `dateIso` | `dateIso` TEXT NOT NULL; `cups` INTEGER NOT NULL |
| `workouts` | `id` | `id` INTEGER NOT NULL; `timestampMillis` INTEGER NOT NULL; `typeName` TEXT NOT NULL; `durationMinutes` INTEGER NOT NULL; `calories` INTEGER nullable; `notes` TEXT NOT NULL; `fromExerciseOfDay` INTEGER NOT NULL |
| `weight_entries` | `id` | `id` INTEGER NOT NULL; `timestampMillis` INTEGER NOT NULL; `weightKg` REAL NOT NULL; `notes` TEXT NOT NULL |
| `medications` | `id` | `id` INTEGER NOT NULL; `name` TEXT NOT NULL; `dose` TEXT NOT NULL; `timeLabel` TEXT NOT NULL; `takenToday` INTEGER NOT NULL; `lastTakenMillis` INTEGER NOT NULL; `takenDateIso` TEXT NOT NULL |
| `ai_sessions` | `id` | `id` INTEGER NOT NULL; `title` TEXT NOT NULL; `personaId` TEXT NOT NULL; `summary` TEXT NOT NULL; `fullResponse` TEXT NOT NULL; `userPrompt` TEXT NOT NULL; `timestampMillis` INTEGER NOT NULL |

### app/src/androidTest/java/com/migraineguard/app/AppReliabilityTest.kt

[Open source](../app/src/androidTest/java/com/migraineguard/app/AppReliabilityTest.kt) · 6858 bytes · 103 lines

**Purpose and behavior.** Android instrumentation reliability tests on the API-35 hosted emulator. Uses synthetic databases, files and key values; it does not connect to the user phone/watch/accounts. See the four test entries below.

**Declared types/objects:** `AppReliabilityTest`.

**Project dependencies:** `core.data.db.*`, `core.data.prefs.UserPreferencesRepository`, `feature.backup.BackupRepository`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `@Test fun migrationsPreserveOldJournalAndWater()` | Builds synthetic populated SQLite databases at versions 1, 2 and 3; opens them through Room version 4 and asserts migraine notes and water counts survive and the new session table is usable. | [L24](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/androidTest/java/com/migraineguard/app/AppReliabilityTest.kt#L24) |
| `@Test fun backupRestoreIsEncryptedAndIdempotent()` | Seeds all seven tables, exports an encrypted file, checks private fixture text is absent from bytes, restores into a second database, verifies seven restored records and zero on re-import, and checks photos are excluded. | [L54](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/androidTest/java/com/migraineguard/app/AppReliabilityTest.kt#L54) |
| `@Test fun apiKeyIsEncryptedOnDiskAndCanBeRead()` | Stores a synthetic key through preferences, verifies decrypted readback, reads the DataStore file and asserts the raw fixture is absent, then clears the fixture key. | [L86](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/androidTest/java/com/migraineguard/app/AppReliabilityTest.kt#L86) |
| `@Test fun mainActivityStarts()` | Launches MainActivity with ActivityScenario and verifies the activity is not finishing. This is a startup test, not a complete navigation/UI test. | [L97](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/androidTest/java/com/migraineguard/app/AppReliabilityTest.kt#L97) |

### app/src/main/AndroidManifest.xml

[Open source](../app/src/main/AndroidManifest.xml) · 4712 bytes · 103 lines

**Purpose and behavior.** Declares the application/activity, network, camera, coarse location, notification and health permissions, Health Connect package visibility/rationale entry points and FileProvider. Applies Standard/Preview label placeholder and disables system backup. Permissions being declared does not mean a user has granted them.

**Resource/component identifiers:** `android.permission.INTERNET`, `android.permission.ACCESS_COARSE_LOCATION`, `android.permission.ACCESS_FINE_LOCATION`, `android.permission.CAMERA`, `android.permission.POST_NOTIFICATIONS`, `android.permission.RECEIVE_BOOT_COMPLETED`, `android.permission.health.READ_STEPS`, `android.permission.health.READ_SLEEP`, `android.permission.health.READ_HEART_RATE`, `android.permission.health.READ_EXERCISE`, `android.permission.health.READ_DISTANCE`, `android.permission.health.READ_TOTAL_CALORIES_BURNED`, `android.permission.health.WRITE_EXERCISE`, `android.permission.health.READ_HEALTH_DATA_HISTORY`, `android.hardware.camera`, `com.google.android.apps.healthdata`, `com.sec.android.app.shealth`, `androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE`, `androidx.health.ACTION_HEALTH_CONNECT_SETTINGS`, `androidx.health.ACTION_MANAGE_HEALTH_PERMISSIONS`, `android.health.connect.action.MANAGE_HEALTH_PERMISSIONS`, `.MigraineGuardApp`, `.MainActivity`, `android.intent.action.MAIN`, `android.intent.category.LAUNCHER`, `.feature.fitness.PermissionsRationaleActivity`, `androidx.health.connect.action.SHOW_PERMISSIONS_USAGE`, `ViewPermissionUsageActivity`, `android.intent.action.VIEW_PERMISSION_USAGE`, `android.intent.category.HEALTH_CONNECT`, `android.intent.category.HEALTH_PERMISSIONS`, `androidx.core.content.FileProvider`, `${applicationId}.fileprovider`, `android.support.FILE_PROVIDER_PATHS`.

### app/src/main/java/com/migraineguard/app/core/ai/AiProvider.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/ai/AiProvider.kt) · 266 bytes · 12 lines

**Purpose and behavior.** Defines the shared suspend prompt-completion contract, provider name and priority. Providers return Result.Success text or Result.Error rather than assuming network requests always succeed.

**Declared types/objects:** `AiProvider`.

**Project dependencies:** `core.data.Result`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `suspend fun complete(prompt: String): Result<String>` | Implements or invokes the suspend text-completion contract. Provider implementations prepare authenticated HTTP requests; fallback/local implementations handle ordered providers or deterministic local reports. See the enclosing file's algorithm notes. | [L10](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/AiProvider.kt#L10) |

### app/src/main/java/com/migraineguard/app/core/ai/FallbackAiClient.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/ai/FallbackAiClient.kt) · 6641 bytes · 149 lines

**Purpose and behavior.** Orders providers and applies per-provider timeouts, preserving parent cancellation. Builds persona prompts with separate cloud/local context, chunks completed answers for display, validates structured nutrition and provides local automatic Insights summaries.

**Declared types/objects:** `FallbackAiClient`.

**Project dependencies:** `core.data.Result`, `core.domain.NutritionScanResult`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `suspend fun complete(prompt: String, cloudPrompt: String? = null): Result<String>` | Implements or invokes the suspend text-completion contract. Provider implementations prepare authenticated HTTP requests; fallback/local implementations handle ordered providers or deterministic local reports. See the enclosing file's algorithm notes. | [L26](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/FallbackAiClient.kt#L26) |
| `suspend fun completePersona( persona: AiPersona, userMessage: String, contextJson: String, shareHealthContext: Boolean = false, onChunk: (suspend (String) -> Unit)? = null, ): Result<String>` | Builds the chosen persona prompt, separates local versus optional cloud health context, calls the provider chain and optionally emits chunks of a completed answer. | [L55](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/FallbackAiClient.kt#L55) |
| `private suspend fun streamFake(full: String, onChunk: suspend (String) -> Unit)` | Splits a completed answer into small display chunks and invokes the callback with delays. This is UI pacing, not HTTP server streaming. | [L79](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/FallbackAiClient.kt#L79) |
| `fun providerNames(): List<String>` | Returns provider names in configured priority order for diagnostics/presentation. | [L89](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/FallbackAiClient.kt#L89) |
| `fun offline(): OfflineHealthBrain?` | Exposes the configured OfflineHealthBrain instance for local context/report use. | [L91](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/FallbackAiClient.kt#L91) |
| `suspend fun analyzeNutrition(description: String, hasImage: Boolean): NutritionScanResult` | Constructs a structured nutrition request from the food description. Uses the provider chain and parses usable JSON; a photo-reference flag must not imply the model saw an image. | [L93](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/FallbackAiClient.kt#L93) |
| `suspend fun summarizeInsights(contextBlob: String): String` | Produces the automatic Insights narrative locally/offline rather than automatically transmitting the report to cloud providers. | [L106](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/FallbackAiClient.kt#L106) |
| `suspend fun testGemini(keyProvider: suspend () -> String): Result<String>` | Creates/tests a Gemini provider using the supplied key getter and a minimal text prompt, returning its success/error. | [L115](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/FallbackAiClient.kt#L115) |
| `internal fun parseNutrition(raw: String, fallbackLabel: String): NutritionScanResult` | Extracts/validates structured food JSON, preserves valid zero values and nullable unknowns, discards invalid negative/non-finite values and retains usable notes/labels. | [L119](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/FallbackAiClient.kt#L119) |
| `private fun unavailableNutrition(description: String)` | Creates a food result with unknown nutrition and honest unavailable notes rather than placeholder calories/macros. | [L142](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/FallbackAiClient.kt#L142) |

### app/src/main/java/com/migraineguard/app/core/ai/OfflineHealthBrain.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/ai/OfflineHealthBrain.kt) · 12553 bytes · 243 lines

**Purpose and behavior.** Deterministic local educational analyst/coach. ContextPack collects profile/journal/current health/food/workout data; JSON export is used only by a cloud request with explicit health sharing. Templates summarize logged data and missing readings without a network model.

**Declared types/objects:** `OfflineHealthBrain`, `ContextPack`.

**Project dependencies:** `core.data.Result`, `core.data.prefs.UserPreferences`, `core.domain.HealthSnapshot`, `core.domain.MedicationEntry`, `core.domain.MigraineEpisode`, `core.domain.NutritionScanRecord`, `core.domain.SleepSnapshot`, `core.domain.WeatherSnapshot`, `core.domain.WorkoutEntry`.

**Data constructors and stored/input fields.** Defaults shown here are source defaults, not real user records. Computed/domain properties are explained in the guide.

`ContextPack`

| Field | Type / source default |
|---|---|
| `prefs` | `UserPreferences` |
| `migraines` | `List<MigraineEpisode>` |
| `waterByDay` | `Map<String, Int>` |
| `waterGoal` | `Int` |
| `fitness` | `HealthSnapshot?` |
| `sleep` | `SleepSnapshot?` |
| `weather` | `WeatherSnapshot?` |
| `workouts` | `List<WorkoutEntry>` |
| `foods` | `List<NutritionScanRecord>` |
| `meds` | `List<MedicationEntry>` |
| `energyLevel` | `Int?` |

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `override suspend fun complete(prompt: String): Result<String>` | Implements or invokes the suspend text-completion contract. Provider implementations prepare authenticated HTTP requests; fallback/local implementations handle ordered providers or deterministic local reports. See the enclosing file's algorithm notes. | [L43](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/OfflineHealthBrain.kt#L43) |
| `fun buildContextJson(pack: ContextPack): String` | Serializes selected profile/goals, recent migraine tags, water adherence, current health/weather, workouts, food hints, medicines and energy into request context. Cloud use is separately gated. | [L53](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/OfflineHealthBrain.kt#L53) |
| `private fun looksLikeCoach(prompt: String): Boolean` | Checks persona/prompt phrases to choose the local coach template versus analyst template. | [L95](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/OfflineHealthBrain.kt#L95) |
| `private fun ageYears(dobIso: String): Int?` | Parses the DOB and computes the calendar year difference; blank/invalid dates return no age. | [L102](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/OfflineHealthBrain.kt#L102) |
| `private fun waterAdherence(pack: ContextPack): Double` | Calculates average recorded-day cups/goal, capped at full adherence. An empty history produces zero in this helper; it is not a wearable hydration measurement. | [L109](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/OfflineHealthBrain.kt#L109) |
| `private fun coachReport(pack: ContextPack, prompt: String): String` | Builds the local coach check-in from actual available steps, logged water, recent sleep/workouts and migraine context; excludes step nudges when the step diagnostic is unknown. | [L116](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/OfflineHealthBrain.kt#L116) |
| `private fun analystReport(pack: ContextPack, prompt: String): String` | Builds a descriptive educational review of profile, logged migraine/tag frequencies, hydration and current health/weather. The template does not establish clinical causation. | [L169](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/OfflineHealthBrain.kt#L169) |
| `private fun genericOffline(prompt: String): String` | Returns a generic educational response when no context pack has been collected. | [L233](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/OfflineHealthBrain.kt#L233) |

### app/src/main/java/com/migraineguard/app/core/ai/Personas.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/ai/Personas.kt) · 2388 bytes · 52 lines

**Purpose and behavior.** Defines COACH and ANALYST persona metadata, educational system prompts, the shared safety disclaimer and default session-title generation.

**Declared types/objects:** `AiPersona`, `Personas`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun systemPrompt(persona: AiPersona): String` | Selects the educational system prompt for the requested Coach or Analyst persona. | [L38](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/Personas.kt#L38) |
| `fun defaultTitle(persona: AiPersona, userPrompt: String): String` | Builds a bounded session title from persona and the user prompt, providing a persona default for empty input. | [L43](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/Personas.kt#L43) |

### app/src/main/java/com/migraineguard/app/core/ai/StubProviders.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/ai/StubProviders.kt) · 6917 bytes · 169 lines

**Purpose and behavior.** Historical filename containing actual Gemini, Groq and OpenRouter HTTP providers plus BaseAiProvider and shared OkHttp request handling. Gemini supports explicit inline-image requests. Network callbacks are bridged to cancellable coroutines.

**Implementation and maintenance.** GeminiFreeProvider, GroqFreeProvider and OpenRouterFreeProvider each define complete; inspect the linked source line to identify the provider. Resolve keys only for the user's request. Header credentials must not be printed. executeRequest propagates cancellation to OkHttp; response bodies are closed.

**Declared types/objects:** `BaseAiProvider`, `GeminiFreeProvider`, `GroqFreeProvider`, `OpenRouterFreeProvider`, `HttpResponse`.

**Project dependencies:** `core.data.Result`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun resolveApiKey(runtime: String?, buildConfig: String): String` | Prefers the runtime user key and otherwise uses the supplied build default. Application BuildConfig defaults are empty, so no developer key is supplied. | [L37](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/StubProviders.kt#L37) |
| `override suspend fun complete(prompt: String): Result<String>` | Implements or invokes the suspend text-completion contract. Provider implementations prepare authenticated HTTP requests; fallback/local implementations handle ordered providers or deterministic local reports. See the enclosing file's algorithm notes. | [L49](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/StubProviders.kt#L49) |
| `suspend fun completeWithImage(prompt: String, base64: String, mimeType: String): Result<String>` | Builds Gemini parts containing text plus explicit inline base64 image/mime data and delegates to the shared Gemini request path. | [L52](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/StubProviders.kt#L52) |
| `private suspend fun completeParts(parts: JSONArray): Result<String>` | Loads the user key, creates the Gemini generateContent request, sends the parts and extracts candidate text. Returns a bounded request error/empty-response failure when necessary. | [L59](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/StubProviders.kt#L59) |
| `suspend fun testConnection(): Result<String>` | Sends a minimal 'ok' text prompt through Gemini to validate access to that text request path. | [L78](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/StubProviders.kt#L78) |
| `override suspend fun complete(prompt: String): Result<String>` | Implements or invokes the suspend text-completion contract. Provider implementations prepare authenticated HTTP requests; fallback/local implementations handle ordered providers or deterministic local reports. See the enclosing file's algorithm notes. | [L85](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/StubProviders.kt#L85) |
| `override suspend fun complete(prompt: String): Result<String>` | Implements or invokes the suspend text-completion contract. Provider implementations prepare authenticated HTTP requests; fallback/local implementations handle ordered providers or deterministic local reports. See the enclosing file's algorithm notes. | [L103](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/StubProviders.kt#L103) |
| `private suspend fun chatCompletions( url: String, apiKey: String, model: String, prompt: String, extraHeaders: Map<String, String> = emptyMap(), ): Result<String>` | Shared Groq/OpenRouter chat-completions request builder and response parser with bearer authentication and provider-specific endpoint/model. | [L118](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/StubProviders.kt#L118) |
| `internal suspend fun executeRequest(request: Request): HttpResponse` | Enqueues OkHttp asynchronously inside suspendCancellableCoroutine, closes the response, returns code/body and cancels the underlying call when the coroutine is cancelled. | [L151](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/StubProviders.kt#L151) |
| `override fun onFailure(call: Call, e: IOException)` | OkHttp callback propagating a network failure to the coroutine only when it is still active. | [L155](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/StubProviders.kt#L155) |
| `override fun onResponse(call: Call, response: Response)` | OkHttp callback reading/closing the response and returning status/body to the active coroutine, with parse/read failures propagated. | [L159](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ai/StubProviders.kt#L159) |

### app/src/main/java/com/migraineguard/app/core/data/db/AppDatabase.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/data/db/AppDatabase.kt) · 1428 bytes · 46 lines

**Purpose and behavior.** Singleton Room database schema version 4, all seven entities, JSON type converters and DAO accessors. Registers preserving migrations; no destructive migration fallback.

**Declared types/objects:** `AppDatabase`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `abstract fun backupDao(): BackupDao` | Returns Room's generated BackupDao implementation for snapshot reads. | [L24](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/AppDatabase.kt#L24) |
| `abstract fun migraineDao(): MigraineDao` | Returns Room's generated MigraineDao implementation for journal access. | [L25](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/AppDatabase.kt#L25) |
| `abstract fun nutritionDao(): NutritionDao` | Returns Room's generated NutritionDao implementation for meal history. | [L26](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/AppDatabase.kt#L26) |
| `abstract fun waterHistoryDao(): WaterHistoryDao` | Returns Room's generated WaterHistoryDao for daily hydration records and atomic changes. | [L27](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/AppDatabase.kt#L27) |
| `abstract fun workoutDao(): WorkoutDao` | Returns Room's generated WorkoutDao for the manual activity journal. | [L28](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/AppDatabase.kt#L28) |
| `abstract fun weightDao(): WeightDao` | Returns Room's generated WeightDao for manual kilogram readings. | [L29](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/AppDatabase.kt#L29) |
| `abstract fun medicationDao(): MedicationDao` | Returns Room's generated MedicationDao for user-entered medication/check-in records. | [L30](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/AppDatabase.kt#L30) |
| `abstract fun aiSessionDao(): AiSessionDao` | Returns Room's generated AiSessionDao for saved prompt/answer sessions. | [L31](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/AppDatabase.kt#L31) |
| `fun get(context: Context): AppDatabase` | Looks up the existing value indicated by this signature. AppDatabase.get creates/caches the configured Room singleton; WaterHistoryDao.get reads one ISO-date row. | [L36](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/AppDatabase.kt#L36) |

### app/src/main/java/com/migraineguard/app/core/data/db/BackupDao.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/data/db/BackupDao.kt) · 751 bytes · 16 lines

**Purpose and behavior.** Snapshot-only reads for all seven journal tables. BackupRepository uses these within a transaction for consistent export and to compare existing rows during merge restore.

**Declared types/objects:** `BackupDao`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `@Query("SELECT * FROM migraine_episodes") suspend fun migraines(): List<MigraineEpisodeEntity>` | Reads all migraine entity rows for backup snapshot/comparison. | [L8](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/BackupDao.kt#L8) |
| `@Query("SELECT * FROM nutrition_scans") suspend fun nutrition(): List<NutritionScanEntity>` | Reads all nutrition entity rows for backup snapshot/comparison. | [L9](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/BackupDao.kt#L9) |
| `@Query("SELECT * FROM water_history") suspend fun water(): List<WaterDayEntity>` | Reads all daily hydration rows for backup snapshot/comparison. | [L10](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/BackupDao.kt#L10) |
| `@Query("SELECT * FROM workouts") suspend fun workouts(): List<WorkoutEntity>` | Reads all manual workout rows for backup snapshot/comparison. | [L11](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/BackupDao.kt#L11) |
| `@Query("SELECT * FROM weight_entries") suspend fun weights(): List<WeightEntryEntity>` | Reads all manual weight rows for backup snapshot/comparison. | [L12](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/BackupDao.kt#L12) |
| `@Query("SELECT * FROM medications") suspend fun medications(): List<MedicationEntity>` | Reads all medication/check-in rows for backup snapshot/comparison. | [L13](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/BackupDao.kt#L13) |
| `@Query("SELECT * FROM ai_sessions") suspend fun sessions(): List<AiSessionEntity>` | Reads all saved AI session rows for backup snapshot/comparison. | [L14](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/BackupDao.kt#L14) |

**Declared SQL queries.**

```kotlin
@Query("SELECT * FROM migraine_episodes") suspend fun migraines(): List<MigraineEpisodeEntity>
@Query("SELECT * FROM nutrition_scans") suspend fun nutrition(): List<NutritionScanEntity>
@Query("SELECT * FROM water_history") suspend fun water(): List<WaterDayEntity>
@Query("SELECT * FROM workouts") suspend fun workouts(): List<WorkoutEntity>
@Query("SELECT * FROM weight_entries") suspend fun weights(): List<WeightEntryEntity>
@Query("SELECT * FROM medications") suspend fun medications(): List<MedicationEntity>
@Query("SELECT * FROM ai_sessions") suspend fun sessions(): List<AiSessionEntity>
```

### app/src/main/java/com/migraineguard/app/core/data/db/Converters.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/data/db/Converters.kt) · 1916 bytes · 57 lines

**Purpose and behavior.** Room JSON conversion for symptom enums, trigger enums and normalized pain-map points. Marker DTOs isolate stored fields; enum reads keep recognized values and trigger handling tolerates absent legacy data.

**Declared types/objects:** `PainMarkerDto`, `Converters`.

**Project dependencies:** `core.domain.HeadView`, `core.domain.MigraineSymptom`, `core.domain.MigraineTrigger`, `core.domain.PainMarker`.

**Data constructors and stored/input fields.** Defaults shown here are source defaults, not real user records. Computed/domain properties are explained in the guide.

`PainMarkerDto`

| Field | Type / source default |
|---|---|
| `id` | `String` |
| `x` | `Float` |
| `y` | `Float` |
| `intensity` | `Int` |
| `viewId` | `String = HeadView.FRONT.name` |

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun symptomsToString(value: List<MigraineSymptom>): String` | Encodes symptom enum names as JSON text for the Room column. | [L25](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Converters.kt#L25) |
| `fun stringToSymptoms(value: String): List<MigraineSymptom>` | Decodes stored symptom names, retaining recognized enum values. | [L29](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Converters.kt#L29) |
| `fun triggersToString(value: List<MigraineTrigger>): String` | Encodes trigger enum names as JSON text for the Room column. | [L34](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Converters.kt#L34) |
| `fun stringToTriggers(value: String?): List<MigraineTrigger>` | Handles absent/legacy trigger data and decodes recognized trigger enum names. | [L38](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Converters.kt#L38) |
| `fun markersToString(value: List<PainMarker>): String` | Maps normalized pain points to stored marker DTOs and serializes them as JSON. | [L43](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Converters.kt#L43) |
| `fun stringToMarkers(value: String): List<PainMarker>` | Decodes marker DTOs back into domain PainMarker values, preserving view/position/intensity fields. | [L51](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Converters.kt#L51) |

### app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt) · 4387 bytes · 143 lines

**Purpose and behavior.** Room interfaces for observing/querying/inserting/deleting journal rows. WaterHistoryDao includes atomic changeCups and maximum-preserving import operations. SQL defines ordering/ranges and primary-key behavior; Room generates the actual implementations.

**Implementation and maintenance.** The same operation name is repeated across different DAO interfaces. SQL immediately preceding a declaration specifies its table, ordering and inclusive time range. @Insert/@Update/@Transaction determine generated Room behavior; changing only a method's name does not change its query.

**Declared types/objects:** `MigraineDao`, `NutritionDao`, `WaterHistoryDao`, `WorkoutDao`, `WeightDao`, `MedicationDao`, `AiSessionDao`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun observeAll(): Flow<List<MigraineEpisodeEntity>>` | Returns a Room Flow of this DAO's rows in its declared SQL order; subscribers receive subsequent database changes. | [L14](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L14) |
| `suspend fun getById(id: Long): MigraineEpisodeEntity?` | Queries a single row by primary key, returning null if it is absent. | [L17](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L17) |
| `suspend fun inRange(start: Long, end: Long): List<MigraineEpisodeEntity>` | Queries/mapping records between the supplied inclusive bounds. Timestamp-based journals use epoch milliseconds; water uses ISO dates. SQL defines exact ordering. | [L22](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L22) |
| `suspend fun insert(entity: MigraineEpisodeEntity): Long` | Writes the entity through Room's declared insert policy and returns its row ID. Callers supplying id=0 request an auto-generated key. | [L25](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L25) |
| `suspend fun delete(id: Long)` | Deletes the selected primary-key row. Repository methods delegate to the matching DAO. | [L28](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L28) |
| `fun observeRecent(limit: Int = 50): Flow<List<NutritionScanEntity>>` | Returns a limited reactive list of recent rows, mapping entities to domain values in repository implementations. | [L34](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L34) |
| `suspend fun inRange(start: Long, end: Long): List<NutritionScanEntity>` | Queries/mapping records between the supplied inclusive bounds. Timestamp-based journals use epoch milliseconds; water uses ISO dates. SQL defines exact ordering. | [L39](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L39) |
| `suspend fun insert(entity: NutritionScanEntity): Long` | Writes the entity through Room's declared insert policy and returns its row ID. Callers supplying id=0 request an auto-generated key. | [L42](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L42) |
| `suspend fun delete(id: Long)` | Deletes the selected primary-key row. Repository methods delegate to the matching DAO. | [L45](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L45) |
| `fun observeDay(dateIso: String): Flow<WaterDayEntity?>` | Observes one ISO local-date hydration record; absence is distinct from a persisted count. | [L51](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L51) |
| `suspend fun changeCups(dateIso: String, delta: Int)` | Room transaction reads the current day's count, adds the signed delta, clamps to 0–1000 and writes the result atomically. | [L54](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L54) |
| `suspend fun keepMaximum(day: WaterDayEntity)` | Room transaction preserves the greater of existing and incoming hydration values for the same date, used by legacy migration. | [L60](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L60) |
| `suspend fun upsert(day: WaterDayEntity)` | Inserts/replaces the daily hydration row for its date primary key. | [L66](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L66) |
| `suspend fun get(dateIso: String): WaterDayEntity?` | Looks up the existing value indicated by this signature. AppDatabase.get creates/caches the configured Room singleton; WaterHistoryDao.get reads one ISO-date row. | [L69](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L69) |
| `fun observeRecent(limit: Int = 30): Flow<List<WaterDayEntity>>` | Returns a limited reactive list of recent rows, mapping entities to domain values in repository implementations. | [L72](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L72) |
| `suspend fun inRange(start: String, end: String): List<WaterDayEntity>` | Queries/mapping records between the supplied inclusive bounds. Timestamp-based journals use epoch milliseconds; water uses ISO dates. SQL defines exact ordering. | [L75](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L75) |
| `fun observeAll(): Flow<List<WorkoutEntity>>` | Returns a Room Flow of this DAO's rows in its declared SQL order; subscribers receive subsequent database changes. | [L81](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L81) |
| `suspend fun inRange(start: Long, end: Long): List<WorkoutEntity>` | Queries/mapping records between the supplied inclusive bounds. Timestamp-based journals use epoch milliseconds; water uses ISO dates. SQL defines exact ordering. | [L86](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L86) |
| `suspend fun insert(entity: WorkoutEntity): Long` | Writes the entity through Room's declared insert policy and returns its row ID. Callers supplying id=0 request an auto-generated key. | [L89](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L89) |
| `suspend fun delete(id: Long)` | Deletes the selected primary-key row. Repository methods delegate to the matching DAO. | [L92](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L92) |
| `fun observeAll(): Flow<List<WeightEntryEntity>>` | Returns a Room Flow of this DAO's rows in its declared SQL order; subscribers receive subsequent database changes. | [L98](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L98) |
| `suspend fun insert(entity: WeightEntryEntity): Long` | Writes the entity through Room's declared insert policy and returns its row ID. Callers supplying id=0 request an auto-generated key. | [L101](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L101) |
| `suspend fun delete(id: Long)` | Deletes the selected primary-key row. Repository methods delegate to the matching DAO. | [L104](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L104) |
| `suspend fun latest(): WeightEntryEntity?` | Reads the newest weight row and maps it into a domain entry, or returns null. | [L107](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L107) |
| `fun observeAll(): Flow<List<MedicationEntity>>` | Returns a Room Flow of this DAO's rows in its declared SQL order; subscribers receive subsequent database changes. | [L113](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L113) |
| `suspend fun insert(entity: MedicationEntity): Long` | Writes the entity through Room's declared insert policy and returns its row ID. Callers supplying id=0 request an auto-generated key. | [L116](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L116) |
| `suspend fun update(entity: MedicationEntity)` | Updates an existing medication entity by primary key, including taken fields. | [L119](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L119) |
| `suspend fun delete(id: Long)` | Deletes the selected primary-key row. Repository methods delegate to the matching DAO. | [L122](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L122) |
| `fun observeAll(): Flow<List<AiSessionEntity>>` | Returns a Room Flow of this DAO's rows in its declared SQL order; subscribers receive subsequent database changes. | [L129](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L129) |
| `suspend fun getById(id: Long): AiSessionEntity?` | Queries a single row by primary key, returning null if it is absent. | [L132](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L132) |
| `suspend fun insert(entity: AiSessionEntity): Long` | Writes the entity through Room's declared insert policy and returns its row ID. Callers supplying id=0 request an auto-generated key. | [L135](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L135) |
| `suspend fun delete(id: Long)` | Deletes the selected primary-key row. Repository methods delegate to the matching DAO. | [L138](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L138) |
| `suspend fun clearAll()` | Deletes all saved AI sessions through the DAO. It does not remove unrelated journal tables or provider-side data. | [L141](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Daos.kt#L141) |

**Declared SQL queries.**

```kotlin
@Query("SELECT * FROM migraine_episodes ORDER BY timestampMillis DESC")
@Query("SELECT * FROM migraine_episodes WHERE id = :id LIMIT 1")
@Query(
@Query("DELETE FROM migraine_episodes WHERE id = :id")
@Query("SELECT * FROM nutrition_scans ORDER BY timestampMillis DESC LIMIT :limit")
@Query(
@Query("DELETE FROM nutrition_scans WHERE id = :id")
@Query("SELECT * FROM water_history WHERE dateIso = :dateIso LIMIT 1")
@Query("SELECT * FROM water_history WHERE dateIso = :dateIso LIMIT 1")
@Query("SELECT * FROM water_history ORDER BY dateIso DESC LIMIT :limit")
@Query("SELECT * FROM water_history WHERE dateIso BETWEEN :start AND :end ORDER BY dateIso ASC")
@Query("SELECT * FROM workouts ORDER BY timestampMillis DESC")
@Query(
@Query("DELETE FROM workouts WHERE id = :id")
@Query("SELECT * FROM weight_entries ORDER BY timestampMillis DESC")
@Query("DELETE FROM weight_entries WHERE id = :id")
@Query("SELECT * FROM weight_entries ORDER BY timestampMillis DESC LIMIT 1")
@Query("SELECT * FROM medications ORDER BY id ASC")
@Query("DELETE FROM medications WHERE id = :id")
@Query("SELECT * FROM ai_sessions ORDER BY timestampMillis DESC")
@Query("SELECT * FROM ai_sessions WHERE id = :id LIMIT 1")
@Query("DELETE FROM ai_sessions WHERE id = :id")
@Query("DELETE FROM ai_sessions")
```

### app/src/main/java/com/migraineguard/app/core/data/db/Entities.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/data/db/Entities.kt) · 6381 bytes · 203 lines

**Purpose and behavior.** Serializable Room row types and domain mappings for migraine, nutrition, water, workout, weight, medication and AI sessions. Suppresses older fabricated nutrition placeholders and interprets medication taken status using the current local date.

**Implementation and maintenance.** Multiple toDomain/fromDomain functions belong to different entities. Nutrition placeholder recognition is intentional compatibility cleanup, not an ordinary new-meal estimate. Medication takenToday is interpreted with takenDateIso. All generated IDs remain storage implementation details.

**Declared types/objects:** `MigraineEpisodeEntity`, `NutritionScanEntity`, `WaterDayEntity`, `WorkoutEntity`, `WeightEntryEntity`, `MedicationEntity`, `AiSessionEntity`.

**Project dependencies:** `core.domain.MedicationEntry`, `core.domain.MigraineEpisode`, `core.domain.MigraineSymptom`, `core.domain.MigraineTrigger`, `core.domain.NutritionScanRecord`, `core.domain.NutritionScanResult`, `core.domain.PainMarker`, `core.domain.WeightEntry`, `core.domain.WorkoutEntry`, `core.domain.WorkoutType`.

**Data constructors and stored/input fields.** Defaults shown here are source defaults, not real user records. Computed/domain properties are explained in the guide.

`MigraineEpisodeEntity`

| Field | Type / source default |
|---|---|
| `timestampMillis` | `Long` |
| `overallIntensity` | `Int` |
| `notes` | `String` |
| `symptoms` | `List<MigraineSymptom>` |
| `markers` | `List<PainMarker>` |
| `triggers` | `List<MigraineTrigger> = emptyList()` |

`NutritionScanEntity`

| Field | Type / source default |
|---|---|
| `timestampMillis` | `Long` |
| `foodLabel` | `String` |
| `caloriesEstimate` | `Int?` |
| `proteinG` | `Float?` |
| `carbsG` | `Float?` |
| `fatG` | `Float?` |
| `notes` | `String` |
| `imageUri` | `String?` |
| `migraineTriggerHint` | `String?` |

`WaterDayEntity`

| Field | Type / source default |
|---|---|
| `cups` | `Int` |

`WorkoutEntity`

| Field | Type / source default |
|---|---|
| `timestampMillis` | `Long` |
| `typeName` | `String` |
| `durationMinutes` | `Int` |
| `calories` | `Int?` |
| `notes` | `String` |
| `fromExerciseOfDay` | `Boolean = false` |

`WeightEntryEntity`

| Field | Type / source default |
|---|---|
| `timestampMillis` | `Long` |
| `weightKg` | `Float` |
| `notes` | `String = ""` |

`MedicationEntity`

| Field | Type / source default |
|---|---|
| `name` | `String` |
| `dose` | `String` |
| `timeLabel` | `String` |
| `takenToday` | `Boolean = false` |
| `lastTakenMillis` | `Long = 0` |
| `takenDateIso` | `String = ""` |

`AiSessionEntity`

| Field | Type / source default |
|---|---|
| `title` | `String` |
| `personaId` | `String` |
| `summary` | `String` |
| `fullResponse` | `String` |
| `userPrompt` | `String = ""` |
| `timestampMillis` | `Long` |

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun toDomain()` | Maps a storage entity to its shared domain model. Nutrition additionally suppresses old fabricated placeholder values; medication mapping evaluates whether its taken date is today. | [L28](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Entities.kt#L28) |
| `fun fromDomain(e: MigraineEpisode)` | Maps a domain object to the corresponding persisted entity, preserving supplied ID/fields. | [L39](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Entities.kt#L39) |
| `private fun wasPlaceholder(): Boolean` | Recognizes the old 'Structured estimate (' note prefix identifying previously fabricated nutrition values. | [L65](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Entities.kt#L65) |
| `fun toDomain()` | Maps a storage entity to its shared domain model. Nutrition additionally suppresses old fabricated placeholder values; medication mapping evaluates whether its taken date is today. | [L67](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Entities.kt#L67) |
| `fun from(result: NutritionScanResult, timestampMillis: Long)` | Builds a nutrition entity from an analysis result plus timestamp, ready for local insertion. | [L83](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Entities.kt#L83) |
| `fun toDomain()` | Maps a storage entity to its shared domain model. Nutrition additionally suppresses old fabricated placeholder values; medication mapping evaluates whether its taken date is today. | [L115](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Entities.kt#L115) |
| `fun fromDomain(e: WorkoutEntry)` | Maps a domain object to the corresponding persisted entity, preserving supplied ID/fields. | [L126](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Entities.kt#L126) |
| `fun toDomain()` | Maps a storage entity to its shared domain model. Nutrition additionally suppresses old fabricated placeholder values; medication mapping evaluates whether its taken date is today. | [L146](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Entities.kt#L146) |
| `fun fromDomain(e: WeightEntry)` | Maps a domain object to the corresponding persisted entity, preserving supplied ID/fields. | [L149](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Entities.kt#L149) |
| `fun toDomain(todayIso: String)` | Maps a storage entity to its shared domain model. Nutrition additionally suppresses old fabricated placeholder values; medication mapping evaluates whether its taken date is today. | [L169](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Entities.kt#L169) |
| `fun fromDomain(e: MedicationEntry, todayIso: String)` | Maps a domain object to the corresponding persisted entity, preserving supplied ID/fields. | [L179](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/Entities.kt#L179) |

### app/src/main/java/com/migraineguard/app/core/data/db/PreservingMigrations.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/data/db/PreservingMigrations.kt) · 1898 bytes · 25 lines

**Purpose and behavior.** Direct database version 1/2/3-to-4 preservation migrations. Creates missing tables and adds the triggers column when absent. Checks schema columns without dropping existing journal tables.

**Declared types/objects:** `PreservingMigrations`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `override fun migrate(db: SupportSQLiteDatabase)` | Runs the supported historical-to-version-4 SQL migration, creating missing tables and adding triggers without dropping populated tables. | [L10](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/db/PreservingMigrations.kt#L10) |

### app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt) · 11706 bytes · 284 lines

**Purpose and behavior.** Preferences DataStore model/keys and atomic setters. Emits UI settings/profile/goals/onboarding flags and decrypts provider keys in memory. Migrates legacy plaintext provider values into ApiKeyVault ciphertext before emitting the new preference snapshot.

**Implementation and maintenance.** The Flow transformation first encrypts any legacy plaintext provider values in a DataStore edit, then emits a decrypted in-memory UserPreferences view. Theme/goal/profile values are not protected by ApiKeyVault. Retained driveSyncEnabled is legacy state and has no active sync engine.

**Declared types/objects:** `UserPreferences`, `UserPreferencesRepository`.

**Project dependencies:** `core.domain.ActivityLevel`, `core.domain.BodyProfile`, `core.domain.SexGender`, `core.domain.ThemeMode`, `core.domain.WaterUnit`, `core.security.ApiKeyVault`.

**Data constructors and stored/input fields.** Defaults shown here are source defaults, not real user records. Computed/domain properties are explained in the guide.

`UserPreferences`

| Field | Type / source default |
|---|---|
| `themeMode` | `ThemeMode = ThemeMode.SYSTEM` |
| `dynamicColor` | `Boolean = false` |
| `waterGoalCups` | `Int = 8` |
| `waterUnit` | `WaterUnit = WaterUnit.CUPS` |
| `dailyStepGoal` | `Int = 8000` |
| `sleepGoalHours` | `Float = 8f` |
| `weightGoalKg` | `Float? = null` |
| `notificationsEnabled` | `Boolean = true` |
| `waterReminders` | `Boolean = true` |
| `migraineReminders` | `Boolean = false` |
| `medicationReminders` | `Boolean = false` |
| `driveSyncEnabled` | `Boolean = false` |
| `onboardingDone` | `Boolean = false` |
| `useCelsius` | `Boolean = true` |
| `googleWebClientId` | `String = ""` |
| `energyLevel` | `Int = 3` |
| `energyDateIso` | `String = ""` |
| `bodyProfile` | `BodyProfile = BodyProfile()` |
| `geminiApiKey` | `String = ""` |
| `groqApiKey` | `String = ""` |
| `openRouterApiKey` | `String = ""` |
| `setupComplete` | `Boolean = false` |
| `healthConnectSkipped` | `Boolean = false` |

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `suspend fun getGoogleWebClientId(): String` | Reads the configured Web OAuth client ID from the preference snapshot. | [L142](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L142) |
| `suspend fun setThemeMode(mode: ThemeMode)` | Atomically updates ThemeMode in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. It changes local settings, not a server-side account. | [L145](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L145) |
| `suspend fun setDynamicColor(enabled: Boolean)` | Atomically updates DynamicColor in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. It changes local settings, not a server-side account. | [L149](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L149) |
| `suspend fun setWaterGoalCups(goal: Int)` | Atomically updates WaterGoalCups in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. It changes local settings, not a server-side account. | [L153](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L153) |
| `suspend fun setWaterUnit(unit: WaterUnit)` | Atomically updates WaterUnit in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. It changes local settings, not a server-side account. | [L157](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L157) |
| `suspend fun setDailyStepGoal(goal: Int)` | Atomically updates DailyStepGoal in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. It changes local settings, not a server-side account. | [L161](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L161) |
| `suspend fun setSleepGoalHours(hours: Float)` | Atomically updates SleepGoalHours in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. It changes local settings, not a server-side account. | [L165](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L165) |
| `suspend fun setWeightGoalKg(kg: Float?)` | Atomically updates WeightGoalKg in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. It changes local settings, not a server-side account. | [L169](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L169) |
| `suspend fun setNotificationsEnabled(enabled: Boolean)` | Atomically updates NotificationsEnabled in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. It changes local settings, not a server-side account. | [L175](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L175) |
| `suspend fun setWaterReminders(enabled: Boolean)` | Atomically updates WaterReminders in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. It changes local settings, not a server-side account. | [L179](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L179) |
| `suspend fun setMigraineReminders(enabled: Boolean)` | Atomically updates MigraineReminders in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. It changes local settings, not a server-side account. | [L183](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L183) |
| `suspend fun setMedicationReminders(enabled: Boolean)` | Atomically updates MedicationReminders in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. It changes local settings, not a server-side account. | [L187](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L187) |
| `suspend fun setDriveSyncEnabled(enabled: Boolean)` | Atomically updates DriveSyncEnabled in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. It changes local settings, not a server-side account. | [L191](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L191) |
| `suspend fun setOnboardingDone(done: Boolean)` | Atomically updates OnboardingDone in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. It changes local settings, not a server-side account. | [L195](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L195) |
| `suspend fun setUseCelsius(use: Boolean)` | Atomically updates UseCelsius in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. It changes local settings, not a server-side account. | [L199](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L199) |
| `suspend fun setGoogleWebClientId(id: String)` | Atomically updates GoogleWebClientId in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. It changes local settings, not a server-side account. | [L203](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L203) |
| `suspend fun setEnergyLevel(level: Int, dateIso: String)` | Saves/clamps the energy check-in and its local date so another day's value is not treated as today's input. | [L207](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L207) |
| `suspend fun saveBodyProfile(profile: BodyProfile)` | Persists profile fields in DataStore using canonical centimeters/kilograms and the selected display-unit preference. | [L214](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L214) |
| `suspend fun seedProfileNameIfBlank(name: String)` | Copies a available display name into an empty profile without overwriting a name the user already entered. | [L228](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L228) |
| `suspend fun setGeminiApiKey(key: String)` | Atomically updates GeminiApiKey in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. Encrypts the user value through ApiKeyVault before storing it. | [L237](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L237) |
| `suspend fun clearGeminiApiKey()` | Clears this provider key from local preferences. It does not revoke the key in the provider project or erase previously sent cloud requests. | [L241](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L241) |
| `suspend fun setGroqApiKey(key: String)` | Atomically updates GroqApiKey in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. Encrypts the user value through ApiKeyVault before storing it. | [L245](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L245) |
| `suspend fun clearGroqApiKey()` | Clears this provider key from local preferences. It does not revoke the key in the provider project or erase previously sent cloud requests. | [L249](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L249) |
| `suspend fun setOpenRouterApiKey(key: String)` | Atomically updates OpenRouterApiKey in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. Encrypts the user value through ApiKeyVault before storing it. | [L253](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L253) |
| `suspend fun clearOpenRouterApiKey()` | Clears this provider key from local preferences. It does not revoke the key in the provider project or erase previously sent cloud requests. | [L257](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L257) |
| `suspend fun clearAllAiKeys()` | Atomically removes Gemini, Groq and OpenRouter key values from local preferences. | [L261](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L261) |
| `suspend fun setSetupComplete(done: Boolean)` | Atomically updates SetupComplete in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. It changes local settings, not a server-side account. | [L269](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L269) |
| `suspend fun setHealthConnectSkipped(skipped: Boolean)` | Atomically updates HealthConnectSkipped in Preferences DataStore using the supplied input; see its linked body for bounds/defaults. It changes local settings, not a server-side account. | [L276](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L276) |
| `suspend fun getGeminiApiKey(): String` | Returns the decrypted user Gemini key from the current preference snapshot for an authenticated request. | [L280](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L280) |
| `suspend fun getGroqApiKey(): String` | Returns the decrypted user Groq key from the current preference snapshot. | [L281](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L281) |
| `suspend fun getOpenRouterApiKey(): String` | Returns the decrypted user OpenRouter key from the current preference snapshot. | [L282](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/data/prefs/UserPreferencesRepository.kt#L282) |

### app/src/main/java/com/migraineguard/app/core/data/Result.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/data/Result.kt) · 278 bytes · 8 lines

**Purpose and behavior.** Small covariant sealed success/error wrapper shared by authentication and AI. Success carries a typed value; Error carries a user-facing message and optional cause.

**Declared types/objects:** `Result`, `Success`, `Error`.

### app/src/main/java/com/migraineguard/app/core/design/Theme.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/design/Theme.kt) · 3221 bytes · 95 lines

**Purpose and behavior.** Compose light/dark Material color schemes and preference-driven system/light/dark selection, with optional Android dynamic colors when supported.

**Project dependencies:** `core.domain.ThemeMode`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun MigraineGuardTheme( themeMode: ThemeMode = ThemeMode.SYSTEM, dynamicColor: Boolean = false, content: @Composable () -> Unit, )` | Chooses system/forced light/dark and optional dynamic Material colors from preferences, then applies shared typography/content. | [L70](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/design/Theme.kt#L70) |

### app/src/main/java/com/migraineguard/app/core/design/Type.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/design/Type.kt) · 2211 bytes · 77 lines

**Purpose and behavior.** Shared Material typography definitions used by MigraineGuardTheme.

### app/src/main/java/com/migraineguard/app/core/domain/Models.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/domain/Models.kt) · 5789 bytes · 251 lines

**Purpose and behavior.** Android-independent domain enums/data classes for profile, migraine/pain map, hydration units, nutrition, fitness diagnostics, sleep, weather, insights, workouts, weight, medications and AI sessions. BodyProfile exposes computed BMI/labels; nullable measurement fields preserve missing values.

**Implementation and maintenance.** Some historical property names remain broader than their present semantics: InsightReport.totalSteps is today's snapshot and avgSleepHours is the latest session snapshot. UI/report wording explicitly distinguishes these from period statistics. BodyProfile.bmi derives from canonical height/weight.

**Declared types/objects:** `UserProfile`, `SexGender`, `ActivityLevel`, `BodyProfile`, `HeadView`, `PainMarker`, `MigraineSymptom`, `MigraineTrigger`, `MigraineEpisode`, `HealthSnapshot`, `StepSyncSnapshot`, `SleepSnapshot`, `WeatherSnapshot`, `NutritionScanResult`, `NutritionScanRecord`, `InsightReport`, `ThemeMode`, `WaterUnit`, `WorkoutType`, `WorkoutEntry`, `ExerciseSuggestion`, `WeightEntry`, `MedicationEntry`, `AiSession`.

**Data constructors and stored/input fields.** Defaults shown here are source defaults, not real user records. Computed/domain properties are explained in the guide.

`UserProfile`

| Field | Type / source default |
|---|---|
| `id` | `String` |
| `displayName` | `String` |
| `email` | `String?` |
| `photoUrl` | `String? = null` |

`BodyProfile`

| Field | Type / source default |
|---|---|
| `displayName` | `String = ""` |
| `dateOfBirthIso` | `String = ""` |
| `sex` | `SexGender? = null` |
| `heightCm` | `Float? = null` |
| `weightKg` | `Float? = null` |
| `activityLevel` | `ActivityLevel? = null` |
| `useMetric` | `Boolean = true` |
| `profileComplete` | `Boolean = false` |

`PainMarker`

| Field | Type / source default |
|---|---|
| `id` | `String` |
| `x` | `Float` |
| `y` | `Float` |
| `intensity` | `Int` |
| `viewId` | `String = HeadView.FRONT.name` |

`MigraineEpisode`

| Field | Type / source default |
|---|---|
| `id` | `Long = 0` |
| `timestampMillis` | `Long` |
| `overallIntensity` | `Int` |
| `notes` | `String` |
| `symptoms` | `List<MigraineSymptom>` |
| `markers` | `List<PainMarker>` |
| `triggers` | `List<MigraineTrigger> = emptyList()` |

`HealthSnapshot`

| Field | Type / source default |
|---|---|
| `steps` | `Int` |
| `heartRateBpm` | `Int?` |
| `activeMinutes` | `Int` |
| `sourceLabel` | `String = "Local"` |
| `distanceMeters` | `Double? = null` |
| `caloriesBurned` | `Double? = null` |
| `caloriesSourceLabel` | `String? = null` |
| `stepSync` | `StepSyncSnapshot? = null` |

`StepSyncSnapshot`

| Field | Type / source default |
|---|---|
| `total` | `Int?` |
| `samsungTotal` | `Int?` |
| `sourcePackages` | `List<String>` |
| `readAtMillis` | `Long` |
| `message` | `String` |

`SleepSnapshot`

| Field | Type / source default |
|---|---|
| `hours` | `Float` |
| `qualityLabel` | `String` |
| `sourceLabel` | `String = "Local"` |

`WeatherSnapshot`

| Field | Type / source default |
|---|---|
| `temperatureC` | `Float` |
| `pressureHpa` | `Float` |
| `humidityPercent` | `Int` |
| `condition` | `String` |
| `locationLabel` | `String` |
| `isCachedOrFake` | `Boolean = false` |
| `observedAtMillis` | `Long = 0L` |

`NutritionScanResult`

| Field | Type / source default |
|---|---|
| `foodLabel` | `String` |
| `caloriesEstimate` | `Int?` |
| `proteinG` | `Float? = null` |
| `carbsG` | `Float? = null` |
| `fatG` | `Float? = null` |
| `notes` | `String` |
| `imageUri` | `String? = null` |
| `migraineTriggerHint` | `String? = null` |

`NutritionScanRecord`

| Field | Type / source default |
|---|---|
| `id` | `Long = 0` |
| `timestampMillis` | `Long` |
| `result` | `NutritionScanResult` |

`InsightReport`

| Field | Type / source default |
|---|---|
| `title` | `String` |
| `summary` | `String` |
| `periodLabel` | `String` |
| `migraineCount` | `Int = 0` |
| `avgIntensity` | `Float = 0f` |
| `avgWaterCups` | `Float = 0f` |
| `avgSleepHours` | `Float = 0f` |
| `pressureTrend` | `String = ""` |
| `foodScanCount` | `Int = 0` |
| `topFoods` | `List<String> = emptyList()` |
| `totalSteps` | `Int = 0` |
| `workoutCount` | `Int = 0` |
| `workoutMinutes` | `Int = 0` |
| `sideSummary` | `String = ""` |

`WorkoutEntry`

| Field | Type / source default |
|---|---|
| `id` | `Long = 0` |
| `timestampMillis` | `Long` |
| `type` | `WorkoutType` |
| `durationMinutes` | `Int` |
| `calories` | `Int? = null` |
| `notes` | `String = ""` |
| `fromExerciseOfDay` | `Boolean = false` |

`ExerciseSuggestion`

| Field | Type / source default |
|---|---|
| `id` | `String` |
| `title` | `String` |
| `type` | `WorkoutType` |
| `durationMinutes` | `Int` |
| `description` | `String` |

`WeightEntry`

| Field | Type / source default |
|---|---|
| `id` | `Long = 0` |
| `timestampMillis` | `Long` |
| `weightKg` | `Float` |
| `notes` | `String = ""` |

`MedicationEntry`

| Field | Type / source default |
|---|---|
| `id` | `Long = 0` |
| `name` | `String` |
| `dose` | `String` |
| `timeLabel` | `String` |
| `takenToday` | `Boolean = false` |
| `lastTakenMillis` | `Long = 0` |

`AiSession`

| Field | Type / source default |
|---|---|
| `id` | `Long = 0` |
| `title` | `String` |
| `personaId` | `String` |
| `summary` | `String` |
| `fullResponse` | `String` |
| `userPrompt` | `String = ""` |
| `timestampMillis` | `Long` |

### app/src/main/java/com/migraineguard/app/core/security/ApiKeyVault.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/security/ApiKeyVault.kt) · 2228 bytes · 50 lines

**Purpose and behavior.** Device-bound Android Keystore encryption for provider keys. Creates/loads a 256-bit AES key; encrypts each value with a fresh GCM IV; returns empty on unrecoverable ciphertext and temporarily supports legacy plaintext during the preferences migration.

**Declared types/objects:** `ApiKeyVault`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `private fun key(): SecretKey` | Loads the device-bound AES SecretKey from Android Keystore or generates the alias if absent. Synchronization prevents competing generation within the vault instance. | [L18](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/security/ApiKeyVault.kt#L18) |
| `fun encrypt(value: String): String` | Encrypts input with authenticated AES-GCM. ApiKeyVault uses the device keystore key and IV/version encoding; BackupCipher creates a password-derived envelope with random salt/nonce. | [L29](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/security/ApiKeyVault.kt#L29) |
| `fun decrypt(value: String): String` | Authenticates/decrypts the matching ciphertext format. BackupCipher rejects damaged/wrong-password inputs; ApiKeyVault returns empty for unusable device-bound ciphertext and recognizes legacy plaintext during migration. | [L37](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/security/ApiKeyVault.kt#L37) |

### app/src/main/java/com/migraineguard/app/core/security/BackupCipher.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/security/BackupCipher.kt) · 2267 bytes · 49 lines

**Purpose and behavior.** Portable password-encrypted backup envelope with version magic, random salt/nonce, PBKDF2-HMAC-SHA256 and authenticated AES-GCM. Bounds input size and rejects short passwords, damaged files and authentication failure.

**Implementation and maintenance.** PBKDF2 iteration count is 600000, salt 16 bytes, nonce 12 bytes, minimum password length 8 and plaintext cap 20 MB. The authenticated version/header prevents silently interpreting tampered formats. Android API 26 is supported by the chosen primitives.

**Declared types/objects:** `BackupCipher`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `private fun derive(password: CharArray, salt: ByteArray): SecretKeySpec` | Uses PBKDF2-HMAC-SHA256, the backup salt and iteration count to derive a 256-bit AES key; clears intermediate password/key material where practical. | [L17](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/security/BackupCipher.kt#L17) |
| `fun encrypt(plain: ByteArray, password: CharArray): ByteArray` | Encrypts input with authenticated AES-GCM. ApiKeyVault uses the device keystore key and IV/version encoding; BackupCipher creates a password-derived envelope with random salt/nonce. | [L25](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/security/BackupCipher.kt#L25) |
| `fun decrypt(encrypted: ByteArray, password: CharArray): ByteArray` | Authenticates/decrypts the matching ciphertext format. BackupCipher rejects damaged/wrong-password inputs; ApiKeyVault returns empty for unusable device-bound ciphertext and recognizes legacy plaintext during migration. | [L37](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/security/BackupCipher.kt#L37) |

### app/src/main/java/com/migraineguard/app/core/security/SigningCertificate.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/security/SigningCertificate.kt) · 1062 bytes · 27 lines

**Purpose and behavior.** Reads the installed application's actual signing certificate and formats SHA-1, using SigningInfo on newer Android and legacy signatures on API 26–27. Google setup errors therefore show the APK in use rather than a hardcoded old machine certificate.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun installedSigningSha1(context: Context): String` | Reads the actual package signing certificate using the appropriate Android API and formats a colon-separated SHA-1 fingerprint for OAuth registration. | [L10](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/security/SigningCertificate.kt#L10) |

### app/src/main/java/com/migraineguard/app/core/time/HealthDay.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/time/HealthDay.kt) · 556 bytes · 13 lines

**Purpose and behavior.** Local calendar-day start and exclusive next-midnight helpers. They use ZoneId/calendar arithmetic rather than fixed 24-hour offsets and support daylight-saving boundaries.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun healthDayStart(now: Instant, zone: ZoneId = ZoneId.systemDefault()): Instant` | Converts the current instant to its device-zone calendar date and returns that date's local midnight as an Instant. | [L7](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/time/HealthDay.kt#L7) |
| `fun healthDayEnd(now: Instant, zone: ZoneId = ZoneId.systemDefault()): Instant` | Returns the next local calendar midnight as an exclusive bound. Uses calendar dates rather than adding 24 hours, preserving 23/25-hour DST days. | [L11](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/time/HealthDay.kt#L11) |

### app/src/main/java/com/migraineguard/app/core/ui/DateOfBirthField.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/ui/DateOfBirthField.kt) · 3918 bytes · 111 lines

**Purpose and behavior.** Reusable Material date-picker field converting selections to an ISO local date and restricting invalid future DOB selections.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun DateOfBirthField( isoDate: String, onIsoDateChange: (String) -> Unit, modifier: Modifier = Modifier, label: String = "Date of birth", yearRange: IntRange = 1920..LocalDate.now().year, )` | Displays a DOB input/date picker, converts date selections to ISO dates and calls the supplied change callback. | [L36](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ui/DateOfBirthField.kt#L36) |

### app/src/main/java/com/migraineguard/app/core/ui/PlaceholderScreen.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/ui/PlaceholderScreen.kt) · 133 bytes · 5 lines

**Purpose and behavior.** Retained shared placeholder source. It contains no active feature implementation; inspect its declarations before reusing it.

**Declared types/objects:** `UiPlaceholders`.

### app/src/main/java/com/migraineguard/app/core/ui/ProgressRing.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/ui/ProgressRing.kt) · 1645 bytes · 48 lines

**Purpose and behavior.** Reusable circular progress visualization for dashboard/goal displays. Draws the track and progress arc using supplied values/colors rather than obtaining health data itself.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun ProgressRing( progress: Float, label: String, modifier: Modifier = Modifier, size: Dp = 160.dp, stroke: Dp = 14.dp, )` | Draws a circular track/progress arc from caller-provided progress and color/layout values. | [L18](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ui/ProgressRing.kt#L18) |

### app/src/main/java/com/migraineguard/app/core/ui/SectionCard.kt

[Open source](../app/src/main/java/com/migraineguard/app/core/ui/SectionCard.kt) · 3125 bytes · 93 lines

**Purpose and behavior.** Reusable titled Material card and empty-state presentation components. Feature screens supply content and actions.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun SectionCard( title: String, modifier: Modifier = Modifier, icon: ImageVector? = null, content: @Composable ColumnScope.() -> Unit, )` | Renders a reusable titled Material card with caller-supplied composable content. | [L25](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ui/SectionCard.kt#L25) |
| `fun EmptyState( title: String, subtitle: String, modifier: Modifier = Modifier, icon: ImageVector? = null, )` | Renders a reusable empty-history/status message for a feature screen. | [L63](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/core/ui/SectionCard.kt#L63) |

### app/src/main/java/com/migraineguard/app/di/AppContainer.kt

[Open source](../app/src/main/java/com/migraineguard/app/di/AppContainer.kt) · 4498 bytes · 89 lines

**Purpose and behavior.** Manual application composition root. Owns database, preferences, repositories, an application SupervisorJob scope, HealthRefreshCoordinator and the provider chain. Passes key getter lambdas to providers, so saved user keys are resolved at request time.

**Declared types/objects:** `AppContainer`.

**Project dependencies:** `feature.backup.BackupRepository`, `core.ai.FallbackAiClient`, `core.ai.GeminiFreeProvider`, `core.ai.GroqFreeProvider`, `core.ai.OfflineHealthBrain`, `core.ai.OpenRouterFreeProvider`, `core.data.db.AppDatabase`, `core.data.prefs.UserPreferencesRepository`, `feature.auth.AuthRepository`, `feature.auth.DefaultAuthRepository`, `feature.body.WeightRepository`, `feature.coach.AiCoachRepository`, `feature.fitness.HealthConnectHealthDataRepository`, `feature.fitness.HealthDataRepository`, `feature.fitness.HealthRefreshCoordinator`, `feature.fitness.WorkoutRepository`, `feature.insights.DefaultInsightsAnalyzer`, `feature.insights.InsightsAnalyzer`, `feature.meds.MedicationRepository`, `feature.migraine.MigraineRepository`, `feature.nutrition.AiNutritionAnalyzer`, `feature.nutrition.NutritionAnalyzer`, `feature.water.WaterIntakeRepository`, `feature.weather.OpenMeteoWeatherRepository`, `feature.weather.WeatherRepository`.

### app/src/main/java/com/migraineguard/app/feature/auth/AuthRepository.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/auth/AuthRepository.kt) · 5911 bytes · 138 lines

**Purpose and behavior.** Auth state/interface and Credential Manager implementation. Resolves runtime/baked Web client ID, provides local continuation, uses the explicit Google-button request and maps account/client/certificate failures to actionable messages. Auth is session state, not a remote journal sync system.

**Declared types/objects:** `AuthState`, `AuthRepository`, `DefaultAuthRepository`.

**Project dependencies:** `BuildConfig`, `core.data.prefs.UserPreferencesRepository`, `core.domain.UserProfile`, `core.security.installedSigningSha1`.

**Data constructors and stored/input fields.** Defaults shown here are source defaults, not real user records. Computed/domain properties are explained in the guide.

`AuthState`

| Field | Type / source default |
|---|---|
| `isSignedIn` | `Boolean = false` |
| `user` | `UserProfile? = null` |
| `isDemo` | `Boolean = false` |

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `suspend fun resolveClientId(): String?` | Chooses the runtime preference Web client ID when usable, otherwise the baked BuildConfig client, for Credential Manager. | [L30](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/auth/AuthRepository.kt#L30) |
| `suspend fun hasConfiguredClientId(): Boolean` | Checks whether a usable Web OAuth client ID exists locally; this does not prove the project's Android client/certificate is registered. | [L31](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/auth/AuthRepository.kt#L31) |
| `fun signInDemo()` | Historical method name for local continuation. Creates local auth session state without contacting Google or requiring cloud access. | [L32](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/auth/AuthRepository.kt#L32) |
| `suspend fun signInWithGoogle(activityContext: Context): Result<UserProfile>` | Uses Credential Manager's explicit Google option, parses a Google ID credential and updates auth state. Cancellation is propagated; account/OAuth failures report the installed package/certificate. | [L33](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/auth/AuthRepository.kt#L33) |
| `fun signOut()` | Clears current auth session/credential state as implemented. It does not delete the journal, clear every AI key or synchronize to a backend. | [L34](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/auth/AuthRepository.kt#L34) |
| `override suspend fun resolveClientId(): String?` | Chooses the runtime preference Web client ID when usable, otherwise the baked BuildConfig client, for Credential Manager. | [L43](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/auth/AuthRepository.kt#L43) |
| `override suspend fun hasConfiguredClientId(): Boolean` | Checks whether a usable Web OAuth client ID exists locally; this does not prove the project's Android client/certificate is registered. | [L54](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/auth/AuthRepository.kt#L54) |
| `private fun isValidClientId(id: String): Boolean` | Checks the configured client string format before using it in a Google request. | [L56](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/auth/AuthRepository.kt#L56) |
| `override fun signInDemo()` | Historical method name for local continuation. Creates local auth session state without contacting Google or requiring cloud access. | [L59](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/auth/AuthRepository.kt#L59) |
| `override suspend fun signInWithGoogle(activityContext: Context): Result<UserProfile>` | Uses Credential Manager's explicit Google option, parses a Google ID credential and updates auth state. Cancellation is propagated; account/OAuth failures report the installed package/certificate. | [L71](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/auth/AuthRepository.kt#L71) |
| `override fun signOut()` | Clears current auth session/credential state as implemented. It does not delete the journal, clear every AI key or synchronize to a backend. | [L134](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/auth/AuthRepository.kt#L134) |

### app/src/main/java/com/migraineguard/app/feature/auth/SignInScreen.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/auth/SignInScreen.kt) · 6680 bytes · 170 lines

**Purpose and behavior.** Google sign-in/local continuation screen. Controls loading/errors, passes an activity context for credentials and allows local use when Google OAuth configuration is unavailable.

**Project dependencies:** `R`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun SignInScreen( authRepository: AuthRepository, onNeedGoogleSetup: () -> Unit = {}, modifier: Modifier = Modifier, )` | Composes Google/local buttons, in-flight state and error display, calling AuthRepository actions from the screen coroutine. | [L43](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/auth/SignInScreen.kt#L43) |

### app/src/main/java/com/migraineguard/app/feature/backup/BackupCard.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/backup/BackupCard.kt) · 3638 bytes · 68 lines

**Purpose and behavior.** Settings UI for encrypted CreateDocument/OpenDocument export/restore. Keeps the password hidden, offers optional profile restoration, runs operations in a coroutine, displays result/error and clears working state.

**Project dependencies:** `core.ui.SectionCard`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun BackupCard(repository: BackupRepository)` | Composes password/restore-profile controls and SAF export/import launchers; invokes the backup repository and reports result without exposing keys. | [L17](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/backup/BackupCard.kt#L17) |

### app/src/main/java/com/migraineguard/app/feature/backup/BackupRepository.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/backup/BackupRepository.kt) · 6626 bytes · 133 lines

**Purpose and behavior.** JournalBackup payload validation plus consistent encrypted export and authenticated merge restore. Excludes keys/photos, normalizes IDs, deduplicates identical content, preserves larger water totals and optionally restores profile/goals after the journal transaction.

**Implementation and maintenance.** Database restore is one transaction. Optional profile/goal restoration happens after it in DataStore, not within the same cross-store transaction. Equality-based deduplication removes ID differences but is not a general cross-device conflict resolver. Photos/API keys are intentionally omitted.

**Declared types/objects:** `JournalBackup`, `BackupRepository`.

**Project dependencies:** `BuildConfig`, `core.data.db.*`, `core.data.prefs.UserPreferencesRepository`, `core.domain.BodyProfile`, `core.security.BackupCipher`.

**Data constructors and stored/input fields.** Defaults shown here are source defaults, not real user records. Computed/domain properties are explained in the guide.

`JournalBackup`

| Field | Type / source default |
|---|---|
| `formatVersion` | `Int = 1` |
| `createdAtMillis` | `Long` |
| `appVersion` | `String` |
| `profile` | `BodyProfile? = null` |
| `waterGoalCups` | `Int = 8` |
| `stepGoal` | `Int = 8000` |
| `sleepGoalHours` | `Float = 8f` |
| `migraines` | `List<MigraineEpisodeEntity> = emptyList()` |
| `nutrition` | `List<NutritionScanEntity> = emptyList()` |
| `water` | `List<WaterDayEntity> = emptyList()` |
| `workouts` | `List<WorkoutEntity> = emptyList()` |
| `weights` | `List<WeightEntryEntity> = emptyList()` |
| `medications` | `List<MedicationEntity> = emptyList()` |
| `sessions` | `List<AiSessionEntity> = emptyList()` |

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun validate()` | Checks backup format version, total row bounds, goals, dates, measurements and pain-marker/intensity limits before export/restore. | [L37](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/backup/BackupRepository.kt#L37) |
| `suspend fun export(uri: Uri, password: CharArray)` | Builds a consistent Room snapshot plus profile/goals, removes photo references, serializes and encrypts, then writes to the user-selected URI. | [L63](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/backup/BackupRepository.kt#L63) |
| `suspend fun restore(uri: Uri, password: CharArray, restoreProfile: Boolean): Int` | Bounds/reads/decrypts/validates the backup, transactionally merges journal content without deletion, optionally restores profile/goals and returns the inserted/updated count. | [L80](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/backup/BackupRepository.kt#L80) |
| `internal suspend fun <T> merge( incoming: List<T>, existing: List<T>, normalize: (T) -> T, insert: suspend (T) -> Unit, ): Int` | Normalizes records, compares content with a mutable set and inserts unseen values. Source ID collisions do not overwrite different existing content; identical re-imports add no duplicate. | [L122](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/backup/BackupRepository.kt#L122) |

### app/src/main/java/com/migraineguard/app/feature/body/BodyMetricsScreen.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/body/BodyMetricsScreen.kt) · 7561 bytes · 173 lines

**Purpose and behavior.** Manual weight-entry/history/trend UI with optional weight goal and profile context. Writes kilogram values via WeightRepository; formats timestamps locally.

**Project dependencies:** `core.data.prefs.UserPreferences`, `core.data.prefs.UserPreferencesRepository`, `core.domain.WeightEntry`, `core.ui.SectionCard`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun BodyMetricsScreen( weightRepository: WeightRepository, preferencesRepository: UserPreferencesRepository, onBack: () -> Unit, modifier: Modifier = Modifier, )` | Composes weight entry/history/goal presentation, observing WeightRepository and preferences and submitting canonical kilogram entries. | [L51](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/body/BodyMetricsScreen.kt#L51) |
| `private fun formatTs(millis: Long): String` | Formats the supplied epoch milliseconds into the feature's local human-readable timestamp; it has no persistence/network side effects. | [L169](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/body/BodyMetricsScreen.kt#L169) |

### app/src/main/java/com/migraineguard/app/feature/body/WeightRepository.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/body/WeightRepository.kt) · 693 bytes · 20 lines

**Purpose and behavior.** Thin Room-to-domain boundary for observing, saving, deleting and reading latest weight entries. It does not query a smart scale or Health Connect weight records.

**Declared types/objects:** `WeightRepository`.

**Project dependencies:** `core.data.db.WeightDao`, `core.data.db.WeightEntryEntity`, `core.domain.WeightEntry`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun observeEntries(): Flow<List<WeightEntry>>` | Maps the weight DAO Flow into shared WeightEntry records. | [L10](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/body/WeightRepository.kt#L10) |
| `suspend fun save(entry: WeightEntry): Long` | Converts the supplied domain journal record to its entity and inserts it via the feature DAO, returning the saved row ID. | [L13](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/body/WeightRepository.kt#L13) |
| `suspend fun delete(id: Long)` | Deletes the selected primary-key row. Repository methods delegate to the matching DAO. | [L16](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/body/WeightRepository.kt#L16) |
| `suspend fun latest(): WeightEntry?` | Reads the newest weight row and maps it into a domain entry, or returns null. | [L18](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/body/WeightRepository.kt#L18) |

### app/src/main/java/com/migraineguard/app/feature/coach/AiCoachRepository.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/coach/AiCoachRepository.kt) · 6054 bytes · 135 lines

**Purpose and behavior.** Builds the local context pack, selects persona, invokes the fallback client with the health-sharing choice and persists successful sessions. Exposes session list/detail/delete. Journal window is 14 days; manual workouts use seven days; current health/weather are snapshots.

**Declared types/objects:** `AiCoachRepository`.

**Project dependencies:** `core.ai.AiPersona`, `core.ai.FallbackAiClient`, `core.ai.OfflineHealthBrain`, `core.ai.Personas`, `core.data.Result`, `core.data.db.AiSessionDao`, `core.data.db.AiSessionEntity`, `core.data.prefs.UserPreferencesRepository`, `core.domain.AiSession`, `feature.fitness.HealthDataRepository`, `feature.fitness.WorkoutRepository`, `feature.meds.MedicationRepository`, `feature.migraine.MigraineRepository`, `feature.nutrition.NutritionAnalyzer`, `feature.water.WaterIntakeRepository`, `feature.weather.WeatherRepository`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun observeSessions(): Flow<List<AiSession>>` | Maps stored AI sessions to the Flow used by Coach history. | [L38](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/coach/AiCoachRepository.kt#L38) |
| `suspend fun getSession(id: Long): AiSession?` | Reads and maps one saved AI answer by ID, or returns null. | [L41](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/coach/AiCoachRepository.kt#L41) |
| `suspend fun buildContextPack(): OfflineHealthBrain.ContextPack` | Collects profile/goals and bounded recent journal/current health/weather snapshots for local AI; filters stale weather and preserves cancellation in optional reads. | [L43](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/coach/AiCoachRepository.kt#L43) |
| `suspend fun ask( persona: AiPersona, userMessage: String, shareHealthContext: Boolean = false, onChunk: (suspend (String) -> Unit)? = null, ): Result<AiSession>` | Builds context, calls the chosen educational persona with the cloud health-sharing choice and persists successful prompt/response/session metadata. | [L76](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/coach/AiCoachRepository.kt#L76) |
| `suspend fun deleteSession(id: Long)` | Deletes the requested local saved AI session. | [L119](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/coach/AiCoachRepository.kt#L119) |
| `private fun AiSessionEntity.toDomain()` | Maps a storage entity to its shared domain model. Nutrition additionally suppresses old fabricated placeholder values; medication mapping evaluates whether its taken date is today. | [L121](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/coach/AiCoachRepository.kt#L121) |
| `private suspend fun <T> contextRead(block: suspend () -> T): T?` | Wraps an optional context read, returning no value for ordinary failures while rethrowing coroutine cancellation. | [L132](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/coach/AiCoachRepository.kt#L132) |

### app/src/main/java/com/migraineguard/app/feature/coach/CoachScreen.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/coach/CoachScreen.kt) · 12082 bytes · 271 lines

**Purpose and behavior.** Coach/Analyst tabs, user prompt controls, explicit cloud health-summary checkbox, current response display and saved-session history/detail. runPrompt manages busy/error state and requests the chosen persona; cancellation is preserved.

**Project dependencies:** `core.ai.AiPersona`, `core.ai.Personas`, `core.data.Result`, `core.ui.SectionCard`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun CoachScreen( coachRepository: AiCoachRepository, initialPersona: AiPersona = AiPersona.COACH, hasUserGeminiKey: Boolean = false, onConnectGemini: () -> Unit = {}, modifier: Modifier = Modifier, )` | Composes persona controls, prompt entry, explicit health-summary sharing, current response and saved-session UI. | [L54](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/coach/CoachScreen.kt#L54) |
| `fun runPrompt(prompt: String, p: AiPersona = persona)` | Runs the selected AI prompt in the UI scope, updates progressive/completed answer state and resets busy in finally; errors are shown without retaining a stuck request state. | [L72](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/coach/CoachScreen.kt#L72) |
| `private fun formatTs(millis: Long): String` | Formats the supplied epoch milliseconds into the feature's local human-readable timestamp; it has no persistence/network side effects. | [L267](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/coach/CoachScreen.kt#L267) |

### app/src/main/java/com/migraineguard/app/feature/fitness/FitnessStub.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/fitness/FitnessStub.kt) · 144 bytes · 5 lines

**Purpose and behavior.** Legacy package marker. Actual fitness behavior is in HealthDataRepository, HealthRefreshCoordinator and TrackScreen; this file does not generate fitness data.

**Declared types/objects:** `FitnessFeature`.

### app/src/main/java/com/migraineguard/app/feature/fitness/HealthAutoRefresh.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/fitness/HealthAutoRefresh.kt) · 845 bytes · 25 lines

**Purpose and behavior.** Compose lifecycle helper that refreshes immediately/on resume and loops every 30 seconds only while the screen is resumed, using the shared coordinator.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun HealthAutoRefresh(coordinator: HealthRefreshCoordinator?)` | Observes lifecycle state and performs coordinator refreshes every 30 seconds only while the screen is resumed. | [L13](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthAutoRefresh.kt#L13) |

### app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt) · 23864 bytes · 574 lines

**Purpose and behavior.** Health Connect availability/permission diagnostics, settings/install intents and actual fitness/sleep reads. Cumulative day aggregates cover next local midnight, step-source comparisons are not added, and failures/missing values are reported without fabricated exercise or sleep quality.

**Implementation and maintenance.** Interface declarations and both implementations deliberately repeat method names. The active container uses HealthConnectHealthDataRepository. The retained fallback implementation reports unavailable values; it is not a mock producing believable fitness metrics. Full-day totals and Samsung comparison use the dayFilter; latest heart rate/exercise use filter ending now. Sleep looks back for actual session records.

**Declared types/objects:** `HealthConnectPackages`, `HealthConnectDiagnostics`, `HealthDataRepository`, `HealthConnectStatus`, `HealthConnectHealthDataRepository`, `FakeHealthDataRepository`.

**Project dependencies:** `core.domain.StepSyncSnapshot`, `core.domain.HealthSnapshot`, `core.domain.SleepSnapshot`, `core.time.healthDayEnd`, `core.time.healthDayStart`.

**Data constructors and stored/input fields.** Defaults shown here are source defaults, not real user records. Computed/domain properties are explained in the guide.

`HealthConnectDiagnostics`

| Field | Type / source default |
|---|---|
| `sdkStatusRaw` | `Int` |
| `sdkStatusName` | `String` |
| `providerPackage` | `String` |
| `lastError` | `String` |
| `googleHcInstalled` | `Boolean` |
| `samsungHealthInstalled` | `Boolean` |

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `suspend fun latestFitness(): HealthSnapshot` | Reads today’s cumulative steps/distance/calories, latest heart sample and exercise duration. Builds source diagnostics and nullable unavailable values; the legacy fallback implementation supplies honest unavailable snapshots. | [L46](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L46) |
| `suspend fun latestSleep(): SleepSnapshot` | Reads recent sleep sessions and maps recorded duration/source into SleepSnapshot. Missing permission/data returns an unavailable label rather than a fabricated quality score. | [L47](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L47) |
| `suspend fun availabilityStatus(): HealthConnectStatus` | Maps Health Connect SDK/provider state into the app's availability enum. | [L48](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L48) |
| `fun requiredPermissions(): Set<String>` | Returns the declared Health Connect read permission set plus the existing exercise-write permission contract. | [L49](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L49) |
| `fun openHealthConnectSettingsIntent(): Intent` | Returns the preferred usable Health Connect install/settings/manage-permission Intent. | [L50](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L50) |
| `fun openHealthConnectInstallOrSettingsIntents(): List<Intent>` | Builds ordered resolvable settings/provider/Play Store fallback intents. | [L51](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L51) |
| `fun openSamsungHealthSettingsIntent(): Intent?` | Returns an intent to Samsung Health when installed/usable, or null. | [L52](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L52) |
| `suspend fun hasAllPermissions(): Boolean` | Checks granted Health Connect permission strings against the requested set; false/errors do not prevent individual permitted metric reads. | [L53](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L53) |
| `fun sdkStatusRaw(uiContext: Context? = null): Int` | Reads SDK/provider availability with a fallback overload and records diagnostic failures. | [L54](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L54) |
| `fun sdkStatusName(raw: Int): String` | Maps numeric SDK state into a readable constant name, preserving unknown values in diagnostics. | [L55](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L55) |
| `fun providerPackage(): String` | Returns the expected Health Connect provider package identifier used for diagnostics/intents. | [L56](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L56) |
| `fun lastError(): String` | Returns the latest remembered health diagnostic message. | [L57](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L57) |
| `fun recordError(message: String)` | Stores a diagnostic failure message for the UI; it does not upload records. | [L58](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L58) |
| `fun isPackagePresent(packageName: String): Boolean` | Checks the Android package manager for the provider/companion package; false means unavailable to that query. | [L59](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L59) |
| `fun playStoreUpdateIntents(): List<Intent>` | Builds Play Store/web install/update fallbacks for Health Connect and, when appropriate, Samsung Health. | [L60](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L60) |
| `fun manageHealthPermissionsIntents(appPackage: String): List<Intent>` | Builds framework/AndroidX/provider settings intents with the current application package as the permission target. | [L61](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L61) |
| `fun diagnostics(uiContext: Context? = null): HealthConnectDiagnostics` | Combines raw SDK state, provider package, last error and companion/provider presence into HealthConnectDiagnostics. | [L62](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L62) |
| `override fun providerPackage(): String` | Returns the expected Health Connect provider package identifier used for diagnostics/intents. | [L79](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L79) |
| `override fun lastError(): String` | Returns the latest remembered health diagnostic message. | [L81](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L81) |
| `override fun recordError(message: String)` | Stores a diagnostic failure message for the UI; it does not upload records. | [L83](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L83) |
| `override fun sdkStatusName(raw: Int): String` | Maps numeric SDK state into a readable constant name, preserving unknown values in diagnostics. | [L87](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L87) |
| `override fun isPackagePresent(packageName: String): Boolean` | Checks the Android package manager for the provider/companion package; false means unavailable to that query. | [L95](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L95) |
| `override fun sdkStatusRaw(uiContext: Context?): Int` | Reads SDK/provider availability with a fallback overload and records diagnostic failures. | [L104](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L104) |
| `override fun diagnostics(uiContext: Context?): HealthConnectDiagnostics` | Combines raw SDK state, provider package, last error and companion/provider presence into HealthConnectDiagnostics. | [L117](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L117) |
| `override fun requiredPermissions(): Set<String>` | Returns the declared Health Connect read permission set plus the existing exercise-write permission contract. | [L129](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L129) |
| `override suspend fun availabilityStatus(): HealthConnectStatus` | Maps Health Connect SDK/provider state into the app's availability enum. | [L154](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L154) |
| `override suspend fun hasAllPermissions(): Boolean` | Checks granted Health Connect permission strings against the requested set; false/errors do not prevent individual permitted metric reads. | [L164](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L164) |
| `override fun openHealthConnectSettingsIntent(): Intent` | Returns the preferred usable Health Connect install/settings/manage-permission Intent. | [L176](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L176) |
| `override fun manageHealthPermissionsIntents(appPackage: String): List<Intent>` | Builds framework/AndroidX/provider settings intents with the current application package as the permission target. | [L179](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L179) |
| `override fun playStoreUpdateIntents(): List<Intent>` | Builds Play Store/web install/update fallbacks for Health Connect and, when appropriate, Samsung Health. | [L206](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L206) |
| `override fun openHealthConnectInstallOrSettingsIntents(): List<Intent>` | Builds ordered resolvable settings/provider/Play Store fallback intents. | [L235](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L235) |
| `override fun openSamsungHealthSettingsIntent(): Intent?` | Returns an intent to Samsung Health when installed/usable, or null. | [L245](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L245) |
| `private fun distinctResolvable(candidates: List<Intent>): List<Intent>` | Filters duplicate intent candidates and retains those the device can resolve before presenting fallback actions. | [L250](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L250) |
| `private fun sleepReadPermission(): String` | Obtains the Health Connect sleep-read permission string for permission checks. | [L262](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L262) |
| `private fun stepsReadPermission(): String` | Obtains the Health Connect steps-read permission string for permission checks. | [L265](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L265) |
| `private fun recordMeaningfulError(message: String, soft: Boolean = false)` | Updates health diagnostics while avoiding misleading replacement of an existing more relevant error. | [L269](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L269) |
| `private fun clearErrorIfSleepOnly()` | Clears a retained sleep-only diagnostic when it no longer describes the current fitness read. | [L276](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L276) |
| `override suspend fun latestFitness(): HealthSnapshot` | Reads today’s cumulative steps/distance/calories, latest heart sample and exercise duration. Builds source diagnostics and nullable unavailable values; the legacy fallback implementation supplies honest unavailable snapshots. | [L284](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L284) |
| `override suspend fun latestSleep(): SleepSnapshot` | Reads recent sleep sessions and maps recorded duration/source into SleepSnapshot. Missing permission/data returns an unavailable label rather than a fabricated quality score. | [L418](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L418) |
| `override suspend fun latestFitness(): HealthSnapshot` | Reads today’s cumulative steps/distance/calories, latest heart sample and exercise duration. Builds source diagnostics and nullable unavailable values; the legacy fallback implementation supplies honest unavailable snapshots. | [L490](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L490) |
| `override suspend fun latestSleep(): SleepSnapshot` | Reads recent sleep sessions and maps recorded duration/source into SleepSnapshot. Missing permission/data returns an unavailable label rather than a fabricated quality score. | [L499](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L499) |
| `override suspend fun availabilityStatus(): HealthConnectStatus` | Maps Health Connect SDK/provider state into the app's availability enum. | [L505](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L505) |
| `override fun requiredPermissions(): Set<String>` | Returns the declared Health Connect read permission set plus the existing exercise-write permission contract. | [L507](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L507) |
| `override fun openHealthConnectSettingsIntent(): Intent` | Returns the preferred usable Health Connect install/settings/manage-permission Intent. | [L509](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L509) |
| `override fun openHealthConnectInstallOrSettingsIntents(): List<Intent>` | Builds ordered resolvable settings/provider/Play Store fallback intents. | [L512](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L512) |
| `override fun openSamsungHealthSettingsIntent(): Intent?` | Returns an intent to Samsung Health when installed/usable, or null. | [L515](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L515) |
| `override suspend fun hasAllPermissions(): Boolean` | Checks granted Health Connect permission strings against the requested set; false/errors do not prevent individual permitted metric reads. | [L517](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L517) |
| `override fun sdkStatusRaw(uiContext: Context?): Int` | Reads SDK/provider availability with a fallback overload and records diagnostic failures. | [L519](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L519) |
| `override fun sdkStatusName(raw: Int): String` | Maps numeric SDK state into a readable constant name, preserving unknown values in diagnostics. | [L521](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L521) |
| `override fun providerPackage(): String` | Returns the expected Health Connect provider package identifier used for diagnostics/intents. | [L523](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L523) |
| `override fun lastError(): String` | Returns the latest remembered health diagnostic message. | [L525](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L525) |
| `override fun recordError(message: String)` | Stores a diagnostic failure message for the UI; it does not upload records. | [L527](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L527) |
| `override fun isPackagePresent(packageName: String): Boolean` | Checks the Android package manager for the provider/companion package; false means unavailable to that query. | [L531](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L531) |
| `override fun playStoreUpdateIntents(): List<Intent>` | Builds Play Store/web install/update fallbacks for Health Connect and, when appropriate, Samsung Health. | [L533](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L533) |
| `override fun manageHealthPermissionsIntents(appPackage: String): List<Intent>` | Builds framework/AndroidX/provider settings intents with the current application package as the permission target. | [L535](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L535) |
| `override fun diagnostics(uiContext: Context?): HealthConnectDiagnostics` | Combines raw SDK state, provider package, last error and companion/provider presence into HealthConnectDiagnostics. | [L538](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L538) |
| `fun Context.findActivity(): android.app.Activity?` | Unwraps ContextWrapper chains to obtain an Activity when required by Android UI/permission APIs. | [L549](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L549) |
| `fun Context.startHealthActivity(intent: Intent)` | Launches the supplied health settings/install intent using a valid activity or new-task flag as appropriate. | [L558](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L558) |
| `private inline fun <T> healthRead(block: () -> T): kotlin.Result<T>` | Wraps health operations in kotlin.Result; ordinary exceptions become failures, but CancellationException is rethrown. | [L567](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthDataRepository.kt#L567) |

### app/src/main/java/com/migraineguard/app/feature/fitness/HealthRefreshCoordinator.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/fitness/HealthRefreshCoordinator.kt) · 3349 bytes · 86 lines

**Purpose and behavior.** Shared StateFlow of health availability, permission/read snapshots, error and refresh time. Debounces normal refreshes, avoids canceling a running normal read, and performs requests in the application scope.

**Declared types/objects:** `HealthRefreshCoordinator`, `State`.

**Project dependencies:** `core.domain.HealthSnapshot`, `core.domain.SleepSnapshot`.

**Data constructors and stored/input fields.** Defaults shown here are source defaults, not real user records. Computed/domain properties are explained in the guide.

`State`

| Field | Type / source default |
|---|---|
| `fitness` | `HealthSnapshot? = null` |
| `sleep` | `SleepSnapshot? = null` |
| `lastRefreshed` | `Instant? = null` |
| `isRefreshing` | `Boolean = false` |
| `lastError` | `String? = null` |
| `permitted` | `Boolean = false` |
| `status` | `HealthConnectStatus = HealthConnectStatus.USING_FALLBACK` |

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun refresh(force: Boolean = false)` | Coordinator implementation debounces/request-manages shared health reads and emits loading/results/errors/time. TrackScreen's helper performs a direct fallback read when no coordinator was supplied. | [L48](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/HealthRefreshCoordinator.kt#L48) |

### app/src/main/java/com/migraineguard/app/feature/fitness/PermissionsRationaleActivity.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/fitness/PermissionsRationaleActivity.kt) · 3467 bytes · 81 lines

**Purpose and behavior.** Health Connect permission-rationale entry activity presenting the purpose of requested health reads. It does not grant permissions or upload health records itself.

**Declared types/objects:** `PermissionsRationaleActivity`.

**Project dependencies:** `R`, `core.design.MigraineGuardTheme`, `core.domain.ThemeMode`, `core.ui.SectionCard`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `override fun onCreate(savedInstanceState: Bundle?)` | Android lifecycle entry. Initializes the component and installs its UI/dependencies; the enclosing class distinguishes application setup, main activity rendering and Health Connect rationale rendering. | [L37](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/PermissionsRationaleActivity.kt#L37) |

### app/src/main/java/com/migraineguard/app/feature/fitness/StepSyncMessage.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/fitness/StepSyncMessage.kt) · 1037 bytes · 14 lines

**Purpose and behavior.** Pure explanation helper choosing messages for missing/failed Samsung reads, higher source totals and matched totals. Diagnostic hypotheses are guidance, not proof of what happened on the phone.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun stepSyncMessage(total: Int?, samsung: Int?, samsungReadFailed: Boolean = false): String` | Chooses an honest steps-sync explanation from nullable combined/Samsung counts and source-query failure state; never sums or multiplies counts. | [L4](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/StepSyncMessage.kt#L4) |

### app/src/main/java/com/migraineguard/app/feature/fitness/WorkoutRepository.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/fitness/WorkoutRepository.kt) · 2485 bytes · 45 lines

**Purpose and behavior.** Manual workout persistence and deterministic daily exercise suggestion selection from ExerciseCatalog.ALL. Imported Health Connect session duration remains separate from the manual journal.

**Declared types/objects:** `WorkoutRepository`, `ExerciseCatalog`.

**Project dependencies:** `core.data.db.WorkoutDao`, `core.data.db.WorkoutEntity`, `core.domain.ExerciseSuggestion`, `core.domain.WorkoutEntry`, `core.domain.WorkoutType`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun observeWorkouts(): Flow<List<WorkoutEntry>>` | Maps manual workout rows into a reactive list of WorkoutEntry objects. | [L13](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/WorkoutRepository.kt#L13) |
| `suspend fun inRange(startMs: Long, endMs: Long): List<WorkoutEntry>` | Queries/mapping records between the supplied inclusive bounds. Timestamp-based journals use epoch milliseconds; water uses ISO dates. SQL defines exact ordering. | [L16](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/WorkoutRepository.kt#L16) |
| `suspend fun save(entry: WorkoutEntry): Long` | Converts the supplied domain journal record to its entity and inserts it via the feature DAO, returning the saved row ID. | [L19](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/WorkoutRepository.kt#L19) |
| `suspend fun delete(id: Long)` | Deletes the selected primary-key row. Repository methods delegate to the matching DAO. | [L22](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/WorkoutRepository.kt#L22) |
| `fun exerciseOfTheDay(date: LocalDate = LocalDate.now()): ExerciseSuggestion` | Selects a fixed ExerciseCatalog entry by local day-of-year modulo catalog size; suggestions repeat and are not model-generated. | [L24](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/fitness/WorkoutRepository.kt#L24) |

### app/src/main/java/com/migraineguard/app/feature/gemini/ConnectGeminiScreen.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/gemini/ConnectGeminiScreen.kt) · 7932 bytes · 201 lines

**Purpose and behavior.** User-key connection screen with AI Studio link, key entry, Save & Test and offline continuation. Tests a small text request; it does not obtain Gemini tokens from Google sign-in.

**Project dependencies:** `core.ai.FallbackAiClient`, `core.data.Result`, `core.data.prefs.UserPreferencesRepository`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun ConnectGeminiScreen( preferencesRepository: UserPreferencesRepository, aiClient: FallbackAiClient, onConnected: () -> Unit, onSkipOffline: () -> Unit, modifier: Modifier = Modifier, )` | Composes user-key entry, AI Studio link, minimal connection test and offline skip, persisting the key through encrypted preferences. | [L50](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/gemini/ConnectGeminiScreen.kt#L50) |

### app/src/main/java/com/migraineguard/app/feature/home/HomeScreen.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/home/HomeScreen.kt) · 17010 bytes · 342 lines

**Purpose and behavior.** Dashboard shortcuts, today’s hydration and health cards, sync explanations, energy check-in and optional real weather. Collects repository/coordinator state, asks for weather location only after an action and excludes stale weather from hints.

**Project dependencies:** `BuildConfig`, `core.data.prefs.UserPreferencesRepository`, `core.domain.HealthSnapshot`, `core.domain.SleepSnapshot`, `core.domain.WeatherSnapshot`, `core.ui.SectionCard`, `feature.fitness.HealthDataRepository`, `feature.fitness.HealthAutoRefresh`, `feature.fitness.HealthRefreshCoordinator`, `feature.migraine.MigraineRepository`, `feature.water.WaterIntakeRepository`, `feature.weather.WeatherRepository`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun HomeScreen( userName: String, onOpenSettings: () -> Unit, onLogMigraine: () -> Unit, onOpenWater: () -> Unit, onOpenFood: () -> Unit, onOpenFitness: () -> Unit, onOpenBody: () -> Unit = {}, onOpenMeds: () -> Unit = {}, onAskCoach: () -> Unit = {}, onHealthAnalyst: () -> Unit = {}, weatherRepository: WeatherRepository, healthDataRepository: HealthDataRepository, healthRefreshCoordinator: HealthRefreshCoordinator? = null, waterIntakeRepository: WaterIntakeRepository, preferencesRepository: UserPreferencesRepository, migraineRepository: MigraineRepository, modifier: Modifier = Modifier, )` | Composes dashboard state/actions, current hydration/health, optional coarse-location weather and energy check-in; observes lifecycle-aware shared sources. | [L69](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/home/HomeScreen.kt#L69) |

### app/src/main/java/com/migraineguard/app/feature/insights/InsightsAnalyzer.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/insights/InsightsAnalyzer.kt) · 5423 bytes · 113 lines

**Purpose and behavior.** Computes migraine/food/manual workout/water statistics for a requested journal period and adds current sleep/steps/weather labeled as snapshots. Automatic narrative stays local through the offline client; no personal weather correlation is calculated.

**Implementation and maintenance.** The selected journal date range applies to migraine/water/meal/manual workout history. Health and weather additions are current snapshots. This code does not store historical health or weather samples and cannot establish trigger correlation.

**Declared types/objects:** `InsightsAnalyzer`, `DefaultInsightsAnalyzer`.

**Project dependencies:** `core.ai.FallbackAiClient`, `core.domain.HeadView`, `core.domain.InsightReport`, `feature.fitness.HealthDataRepository`, `feature.fitness.WorkoutRepository`, `feature.migraine.MigraineRepository`, `feature.nutrition.NutritionAnalyzer`, `feature.water.WaterIntakeRepository`, `feature.weather.WeatherRepository`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `suspend fun weeklyTrends(): InsightReport` | Requests the current inclusive seven-day journal range and labels the returned report Weekly trends. | [L16](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/insights/InsightsAnalyzer.kt#L16) |
| `suspend fun customRange(start: LocalDate, end: LocalDate): InsightReport` | Queries journal data in the selected calendar range, computes descriptive statistics and attaches explicitly current health/weather snapshots rather than fabricated period histories. | [L17](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/insights/InsightsAnalyzer.kt#L17) |
| `suspend fun aiNarrative(report: InsightReport): String` | Builds a summary blob from an InsightReport and requests the local automatic narrative path. | [L18](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/insights/InsightsAnalyzer.kt#L18) |
| `override suspend fun weeklyTrends(): InsightReport` | Requests the current inclusive seven-day journal range and labels the returned report Weekly trends. | [L31](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/insights/InsightsAnalyzer.kt#L31) |
| `override suspend fun customRange(start: LocalDate, end: LocalDate): InsightReport` | Queries journal data in the selected calendar range, computes descriptive statistics and attaches explicitly current health/weather snapshots rather than fabricated period histories. | [L37](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/insights/InsightsAnalyzer.kt#L37) |
| `override suspend fun aiNarrative(report: InsightReport): String` | Builds a summary blob from an InsightReport and requests the local automatic narrative path. | [L98](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/insights/InsightsAnalyzer.kt#L98) |

### app/src/main/java/com/migraineguard/app/feature/insights/InsightsScreen.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/insights/InsightsScreen.kt) · 13220 bytes · 279 lines

**Purpose and behavior.** Weekly/custom-range report UI, count/stat cards, descriptive narrative and shortcuts to Coach/Analyst/Gemini setup. load refreshes the requested report and manages the loading state.

**Declared types/objects:** `RangeMode`, `InsightMode`.

**Project dependencies:** `core.ai.FallbackAiClient`, `core.domain.InsightReport`, `core.ui.EmptyState`, `core.ui.SectionCard`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun InsightsScreen( insightsAnalyzer: InsightsAnalyzer, aiClient: FallbackAiClient, onOpenCoach: () -> Unit = {}, onOpenAnalyst: () -> Unit = {}, hasUserGeminiKey: Boolean = false, onConnectGemini: () -> Unit = {}, modifier: Modifier = Modifier, )` | Composes weekly/custom dates, report metrics/narrative and AI/setup navigation actions. | [L64](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/insights/InsightsScreen.kt#L64) |
| `fun load()` | Loads/replaces the selected Insights report and narrative in the UI coroutine, updating loading/result/error state. | [L80](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/insights/InsightsScreen.kt#L80) |

### app/src/main/java/com/migraineguard/app/feature/meds/MedicationRepository.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/meds/MedicationRepository.kt) · 1425 bytes · 42 lines

**Purpose and behavior.** Medication-label CRUD and local-day taken-state updates. Trims entered labels, persists last-taken time/date and maps older taken dates to not taken today on emission.

**Implementation and maintenance.** The current date is checked when DAO emissions are mapped. A screen left open across midnight may require reload/re-entry for the visual taken state to reset; a dedicated date-driven medication flow is future work.

**Declared types/objects:** `MedicationRepository`.

**Project dependencies:** `core.data.db.MedicationDao`, `core.data.db.MedicationEntity`, `core.domain.MedicationEntry`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `private fun todayIso(): String` | Returns the current device-zone ISO local date for medication taken-state interpretation. | [L11](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/meds/MedicationRepository.kt#L11) |
| `fun observeMedications(): Flow<List<MedicationEntry>>` | Maps medication DAO emissions into today's interpreted MedicationEntry values. | [L13](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/meds/MedicationRepository.kt#L13) |
| `suspend fun add(name: String, dose: String, timeLabel: String): Long` | Trims the user-entered medicine/dose/time labels, inserts an untaken MedicationEntity and returns its row ID. | [L16](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/meds/MedicationRepository.kt#L16) |
| `suspend fun setTakenToday(entry: MedicationEntry, taken: Boolean)` | Updates a medication's taken flag/date and last-taken timestamp; clearing taken removes its today-date marker. | [L25](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/meds/MedicationRepository.kt#L25) |
| `suspend fun delete(id: Long)` | Deletes the selected primary-key row. Repository methods delegate to the matching DAO. | [L40](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/meds/MedicationRepository.kt#L40) |

### app/src/main/java/com/migraineguard/app/feature/meds/MedicationsScreen.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/meds/MedicationsScreen.kt) · 5831 bytes · 141 lines

**Purpose and behavior.** User-entered medicine/dose/time labels, today's taken toggles and delete controls. No dosing engine, drug interaction service or exact time alarm is implemented here.

**Project dependencies:** `core.ui.SectionCard`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun MedicationsScreen( medicationRepository: MedicationRepository, onBack: () -> Unit, modifier: Modifier = Modifier, )` | Composes medication-label entry, today's taken checkboxes and delete actions backed by MedicationRepository. | [L41](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/meds/MedicationsScreen.kt#L41) |

### app/src/main/java/com/migraineguard/app/feature/migraine/MigraineRepository.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/migraine/MigraineRepository.kt) · 899 bytes · 23 lines

**Purpose and behavior.** Room/domain boundary for episode observation, ID lookup, save, delete and time-window queries. Persists pain markers/symptom/trigger lists with the converters.

**Declared types/objects:** `MigraineRepository`.

**Project dependencies:** `core.data.db.MigraineDao`, `core.data.db.MigraineEpisodeEntity`, `core.domain.MigraineEpisode`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun observeEpisodes(): Flow<List<MigraineEpisode>>` | Maps the migraine DAO Flow into domain episodes for history/UI. | [L10](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/migraine/MigraineRepository.kt#L10) |
| `suspend fun getEpisode(id: Long): MigraineEpisode?` | Reads/maps one migraine episode by primary key. | [L13](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/migraine/MigraineRepository.kt#L13) |
| `suspend fun save(episode: MigraineEpisode): Long` | Converts the supplied domain journal record to its entity and inserts it via the feature DAO, returning the saved row ID. | [L15](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/migraine/MigraineRepository.kt#L15) |
| `suspend fun delete(id: Long)` | Deletes the selected primary-key row. Repository methods delegate to the matching DAO. | [L18](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/migraine/MigraineRepository.kt#L18) |
| `suspend fun inRange(startMillis: Long, endMillis: Long): List<MigraineEpisode>` | Queries/mapping records between the supplied inclusive bounds. Timestamp-based journals use epoch milliseconds; water uses ISO dates. SQL defines exact ordering. | [L20](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/migraine/MigraineRepository.kt#L20) |

### app/src/main/java/com/migraineguard/app/feature/migraine/MigraineScreen.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/migraine/MigraineScreen.kt) · 26455 bytes · 555 lines

**Purpose and behavior.** Migraine logging/history/detail and head pain-map UI. Rotation changes anatomical view; tap points use normalized coordinates; drawing maps points to screen coordinates and intensity colors. Saves validated episode input through MigraineRepository.

**Declared types/objects:** `MigrainePane`.

**Project dependencies:** `R`, `core.domain.HeadView`, `core.domain.MigraineEpisode`, `core.domain.MigraineSymptom`, `core.domain.MigraineTrigger`, `core.domain.PainMarker`, `core.ui.EmptyState`, `core.ui.SectionCard`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun MigraineScreen( repository: MigraineRepository, modifier: Modifier = Modifier, )` | Composes log/history/detail panes, symptom/trigger/intensity fields and interactive head map; saves/deletes through the repository. | [L93](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/migraine/MigraineScreen.kt#L93) |
| `fun rotate(delta: Int)` | Changes the selected head-view index with wraparound; stored marker coordinates remain associated with their original view. | [L112](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/migraine/MigraineScreen.kt#L112) |
| `private fun HeadPainMap( view: HeadView, markers: List<PainMarker>, interactive: Boolean, intensityForTap: Float = 5f, onAddMarker: ((PainMarker) -> Unit)? = null, onSwipeRotate: ((Int) -> Unit)? = null, modifier: Modifier = Modifier, )` | Renders the selected head asset, handles normalized tap positions and paints supplied pain markers. | [L413](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/migraine/MigraineScreen.kt#L413) |
| `private fun regionHint(view: HeadView): String` | Maps the selected HeadView to an anatomical orientation hint for the user. | [L509](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/migraine/MigraineScreen.kt#L509) |
| `private fun intensityColor(intensity: Int): Color` | Chooses the display color for the supplied pain-point intensity. | [L517](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/migraine/MigraineScreen.kt#L517) |
| `private fun DrawScope.drawPainMarkers(markers: List<PainMarker>)` | Draws marker positions/intensity rings using normalized coordinates multiplied by the current drawing size. | [L525](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/migraine/MigraineScreen.kt#L525) |
| `private fun formatTs(millis: Long): String` | Formats the supplied epoch milliseconds into the feature's local human-readable timestamp; it has no persistence/network side effects. | [L551](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/migraine/MigraineScreen.kt#L551) |

### app/src/main/java/com/migraineguard/app/feature/nutrition/FoodImage.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/nutrition/FoodImage.kt) · 1999 bytes · 43 lines

**Purpose and behavior.** Bounded photo preprocessing on IO: reads the URI, decodes image bounds, downsamples to a maximum 1600-pixel side, re-encodes JPEG quality 85 to remove original metadata and returns base64/mime type. Oversized/unreadable images fail.

**Declared types/objects:** `FoodImage`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `suspend fun prepareFoodImage(context: Context, uri: Uri): FoodImage` | Reads a bounded image from URI, downscales, re-encodes JPEG without original metadata and returns bounded inline data for explicit Gemini sending. | [L15](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/nutrition/FoodImage.kt#L15) |

### app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionAnalyzer.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionAnalyzer.kt) · 3121 bytes · 57 lines

**Purpose and behavior.** Text/image analysis interface and Room-backed implementation. Explicit photo consent selects only Gemini image analysis; otherwise uses description analysis. Returns unknown nutrition on failure and persists the local result/history.

**Implementation and maintenance.** Selecting a photo alone attaches a local reference. sendPhotoToGemini must be true for actual inline-image sending; failed image analysis does not silently transmit the image to Groq/OpenRouter. Nullable macros/calories are valid absence states.

**Declared types/objects:** `NutritionAnalyzer`, `AiNutritionAnalyzer`.

**Project dependencies:** `core.ai.GeminiFreeProvider`, `core.data.Result`, `core.ai.FallbackAiClient`, `core.data.db.NutritionDao`, `core.data.db.NutritionScanEntity`, `core.domain.NutritionScanRecord`, `core.domain.NutritionScanResult`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `suspend fun analyzeFoodLabel(description: String, imageUri: String? = null, sendPhotoToGemini: Boolean = false): NutritionScanResult` | Chooses description-only fallback analysis or the explicit Gemini photo path, adds portion/uncertainty caveats and preserves unknown values on ordinary errors. | [L16](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionAnalyzer.kt#L16) |
| `fun observeRecent(limit: Int = 30): Flow<List<NutritionScanRecord>>` | Returns a limited reactive list of recent rows, mapping entities to domain values in repository implementations. | [L17](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionAnalyzer.kt#L17) |
| `suspend fun inRange(startMs: Long, endMs: Long): List<NutritionScanRecord>` | Queries/mapping records between the supplied inclusive bounds. Timestamp-based journals use epoch milliseconds; water uses ISO dates. SQL defines exact ordering. | [L18](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionAnalyzer.kt#L18) |
| `suspend fun persist(result: NutritionScanResult): Long` | Maps the nutrition analysis result to a timestamped entity and inserts it in local meal history. | [L19](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionAnalyzer.kt#L19) |
| `override suspend fun analyzeFoodLabel(description: String, imageUri: String?, sendPhotoToGemini: Boolean): NutritionScanResult` | Chooses description-only fallback analysis or the explicit Gemini photo path, adds portion/uncertainty caveats and preserves unknown values on ordinary errors. | [L28](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionAnalyzer.kt#L28) |
| `override fun observeRecent(limit: Int): Flow<List<NutritionScanRecord>>` | Returns a limited reactive list of recent rows, mapping entities to domain values in repository implementations. | [L48](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionAnalyzer.kt#L48) |
| `override suspend fun inRange(startMs: Long, endMs: Long): List<NutritionScanRecord>` | Queries/mapping records between the supplied inclusive bounds. Timestamp-based journals use epoch milliseconds; water uses ISO dates. SQL defines exact ordering. | [L51](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionAnalyzer.kt#L51) |
| `override suspend fun persist(result: NutritionScanResult): Long` | Maps the nutrition analysis result to a timestamped entity and inserts it in local meal history. | [L54](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionAnalyzer.kt#L54) |

### app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionScreen.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionScreen.kt) · 10640 bytes · 242 lines

**Purpose and behavior.** Food description, gallery/camera selection, per-photo Gemini send choice, analysis/result and recent-food history UI. Retains gallery URI permissions when available and stores camera photos in persistent app files via FileProvider.

**Project dependencies:** `core.domain.NutritionScanResult`, `core.ui.SectionCard`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun NutritionScreen( nutritionAnalyzer: NutritionAnalyzer, modifier: Modifier = Modifier, )` | Composes description/photo consent, gallery/camera actions, analysis/result display and persisted meal history. | [L61](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionScreen.kt#L61) |
| `fun createCameraUri(): Uri` | Creates a persistent app food-photo file and a FileProvider content URI usable by the camera activity. | [L78](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionScreen.kt#L78) |
| `fun launchCamera()` | Requests camera access when needed or launches TakePicture with the generated URI; handles no-camera/activity failure. | [L101](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionScreen.kt#L101) |
| `private fun formatTs(millis: Long): String` | Formats the supplied epoch milliseconds into the feature's local human-readable timestamp; it has no persistence/network side effects. | [L238](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/nutrition/NutritionScreen.kt#L238) |

### app/src/main/java/com/migraineguard/app/feature/onboarding/OnboardingScreen.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/onboarding/OnboardingScreen.kt) · 4127 bytes · 87 lines

**Purpose and behavior.** Earlier standalone tip/onboarding UI retained in source. Current first-run flow uses SetupWizardScreen; retaining this file does not imply a second active onboarding wizard.

**Declared types/objects:** `Tip`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun OnboardingScreen( onFinished: () -> Unit, modifier: Modifier = Modifier, )` | Composes the earlier standalone introductory tips. Current navigation uses the ordered setup wizard instead. | [L37](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/onboarding/OnboardingScreen.kt#L37) |

### app/src/main/java/com/migraineguard/app/feature/profile/BodyUnits.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/profile/BodyUnits.kt) · 404 bytes · 15 lines

**Purpose and behavior.** Pure measurement conversion helpers for centimeters to feet/inches and weight when toggling metric/imperial presentation.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun imperialHeight(cm: Float): Pair<Int, Int>` | Converts centimeters to rounded total inches, then splits into feet and remaining inches for editor initialization. | [L5](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/profile/BodyUnits.kt#L5) |
| `fun convertedWeight(value: Float, fromMetric: Boolean, toMetric: Boolean): Float` | Returns the same value when display units agree, otherwise converts between kilograms and pounds using the fixed factor. | [L10](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/profile/BodyUnits.kt#L10) |

### app/src/main/java/com/migraineguard/app/feature/profile/ProfileSetupScreen.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/profile/ProfileSetupScreen.kt) · 11794 bytes · 255 lines

**Purpose and behavior.** Profile editor for name, DOB, sex/activity and height/weight. Initializes from saved physical measurements, converts existing values when display units change, validates input and persists canonical metric units.

**Project dependencies:** `core.data.prefs.UserPreferencesRepository`, `core.domain.ActivityLevel`, `core.domain.BodyProfile`, `core.domain.SexGender`, `core.ui.DateOfBirthField`, `core.ui.SectionCard`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun ProfileSetupScreen( preferencesRepository: UserPreferencesRepository, prefilledName: String, initial: BodyProfile, onComplete: () -> Unit, modifier: Modifier = Modifier, )` | Composes the saved/prefilled profile form, unit conversion and validation, then persists canonical metric fields. | [L44](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/profile/ProfileSetupScreen.kt#L44) |
| `fun changeUnits(metric: Boolean)` | Converts current form values while toggling display units, preserving the physical height/weight instead of simply relabeling a number. | [L74](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/profile/ProfileSetupScreen.kt#L74) |

### app/src/main/java/com/migraineguard/app/feature/reminders/ReminderWorker.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/reminders/ReminderWorker.kt) · 3952 bytes · 97 lines

**Purpose and behavior.** Periodic six-hour generic water/migraine/medication check-ins controlled by settings. Creates a notification channel, checks permission and handles revocation between check and delivery. Disabling reminders cancels the unique WorkManager job.

**Implementation and maintenance.** Periodic interval is six hours and delivery timing is controlled by WorkManager/Android. Medication time labels are not used to schedule exact dose alarms. Runtime notification permission can be revoked between the check and notify call.

**Declared types/objects:** `ReminderWorker`.

**Project dependencies:** `R`, `core.data.prefs.UserPreferencesRepository`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `override suspend fun doWork(): Result` | Reads reminder settings, prepares enabled generic check-ins, verifies notification access and posts messages, handling permission revocation safely. | [L27](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/reminders/ReminderWorker.kt#L27) |
| `fun ensureChannel(context: Context)` | Creates the Android health reminder notification channel on supported versions. | [L60](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/reminders/ReminderWorker.kt#L60) |
| `fun canPostNotifications(context: Context): Boolean` | Checks Android 13+ POST_NOTIFICATIONS permission; older versions do not require that runtime permission. | [L72](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/reminders/ReminderWorker.kt#L72) |
| `fun schedule(context: Context, enabled: Boolean)` | Cancels disabled reminder work or creates/updates one unique six-hour WorkManager request. It does not schedule individual medication time labels. | [L80](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/reminders/ReminderWorker.kt#L80) |

### app/src/main/java/com/migraineguard/app/feature/settings/DriveSyncRepository.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/settings/DriveSyncRepository.kt) · 2096 bytes · 58 lines

**Purpose and behavior.** Legacy unwired Drive-sync abstraction. It stores old preference/timestamp state and returns honest unimplemented status rather than uploading data. Current Settings uses BackupRepository/file picker; no active automatic Drive sync occurs.

**Implementation and maintenance.** This file is not instantiated by AppContainer and no longer controls Settings. Its function listing documents retained code only. It must not be advertised as a working cloud backup service.

**Declared types/objects:** `DriveSyncRepository`, `DriveSyncRepositoryImpl`.

**Project dependencies:** `core.data.prefs.UserPreferencesRepository`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `suspend fun isSyncEnabled(): Boolean` | Reads the legacy Drive preference. The abstraction is unwired from current app behavior and does not prove any data is syncing. | [L15](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/settings/DriveSyncRepository.kt#L15) |
| `suspend fun setSyncEnabled(enabled: Boolean)` | Updates the legacy preference/status bookkeeping in the inactive Drive implementation; no uploader is implemented. | [L16](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/settings/DriveSyncRepository.kt#L16) |
| `suspend fun lastSyncLabel(): String` | Formats the legacy stored timestamp or returns no-sync status; it is not evidence of a Drive backup. | [L17](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/settings/DriveSyncRepository.kt#L17) |
| `suspend fun statusMessage(): String` | Returns an explicit not-implemented/local-backup explanation for the old Drive abstraction. | [L18](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/settings/DriveSyncRepository.kt#L18) |
| `suspend fun requestSyncNow(): String` | Returns an honest unavailable Drive-sync message. Current encrypted export is a separate file-picker workflow. | [L19](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/settings/DriveSyncRepository.kt#L19) |
| `override suspend fun isSyncEnabled(): Boolean` | Reads the legacy Drive preference. The abstraction is unwired from current app behavior and does not prove any data is syncing. | [L27](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/settings/DriveSyncRepository.kt#L27) |
| `override suspend fun setSyncEnabled(enabled: Boolean)` | Updates the legacy preference/status bookkeeping in the inactive Drive implementation; no uploader is implemented. | [L29](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/settings/DriveSyncRepository.kt#L29) |
| `override suspend fun lastSyncLabel(): String` | Formats the legacy stored timestamp or returns no-sync status; it is not evidence of a Drive backup. | [L36](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/settings/DriveSyncRepository.kt#L36) |
| `override suspend fun statusMessage(): String` | Returns an explicit not-implemented/local-backup explanation for the old Drive abstraction. | [L48](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/settings/DriveSyncRepository.kt#L48) |
| `override suspend fun requestSyncNow(): String` | Returns an honest unavailable Drive-sync message. Current encrypted export is a separate file-picker workflow. | [L51](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/settings/DriveSyncRepository.kt#L51) |

### app/src/main/java/com/migraineguard/app/feature/settings/SettingsScreen.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/settings/SettingsScreen.kt) · 25208 bytes · 535 lines

**Purpose and behavior.** Theme/units/goals/provider-key controls, Google certificate help, Health Connect links, reminder preferences, profile edit, sign-out and encrypted backup card. rescheduleReminders keeps WorkManager aligned with the settings toggles.

**Project dependencies:** `feature.backup.BackupRepository`, `feature.backup.BackupCard`, `BuildConfig`, `core.security.installedSigningSha1`, `core.ai.FallbackAiClient`, `core.data.Result`, `core.data.prefs.UserPreferences`, `core.data.prefs.UserPreferencesRepository`, `core.domain.ThemeMode`, `core.domain.WaterUnit`, `core.ui.SectionCard`, `feature.fitness.HealthDataRepository`, `feature.reminders.ReminderWorker`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun SettingsScreen( preferencesRepository: UserPreferencesRepository, backupRepository: BackupRepository, healthDataRepository: HealthDataRepository, aiClient: FallbackAiClient? = null, onSignOut: () -> Unit, onBack: () -> Unit, onEditProfile: () -> Unit = {}, onOpenBody: () -> Unit = {}, onOpenMeds: () -> Unit = {}, onConnectGemini: () -> Unit = {}, focusGoogle: Boolean = false, modifier: Modifier = Modifier, )` | Composes preferences, user-key management/testing, runtime signing help, health links, reminders, profile navigation and encrypted backup. | [L77](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/settings/SettingsScreen.kt#L77) |
| `fun rescheduleReminders()` | Schedules/cancels the unique periodic reminder worker after the relevant preference change. | [L131](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/settings/SettingsScreen.kt#L131) |
| `private fun PrefSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit)` | Renders one label/switch row and forwards a checked-state change to its supplied callback. | [L523](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/settings/SettingsScreen.kt#L523) |

### app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt) · 39764 bytes · 973 lines

**Purpose and behavior.** Ordered Welcome, Sign-in/local, Health Connect, Gemini key, Profile and Done flow. Includes permission/activity fallbacks and skips. Completion persists setupComplete; this large file contains the individual step composables and their local event helpers.

**Implementation and maintenance.** Named helper functions are documented separately; button callbacks, launchers, LaunchedEffect and per-step mutable state belong to the owning composable. Wizard ProfileStep and post-setup ProfileSetupScreen are different forms; validate changes in both where appropriate.

**Declared types/objects:** `WizardStep`.

**Project dependencies:** `R`, `core.ai.FallbackAiClient`, `core.data.Result`, `core.data.prefs.UserPreferences`, `core.data.prefs.UserPreferencesRepository`, `core.domain.ActivityLevel`, `core.domain.BodyProfile`, `core.domain.SexGender`, `core.ui.DateOfBirthField`, `core.ui.SectionCard`, `feature.auth.AuthRepository`, `feature.fitness.HealthConnectPackages`, `feature.fitness.HealthDataRepository`, `feature.fitness.findActivity`, `feature.fitness.startHealthActivity`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun SetupWizardScreen( authRepository: AuthRepository, preferencesRepository: UserPreferencesRepository, healthDataRepository: HealthDataRepository, aiClient: FallbackAiClient, onFinished: () -> Unit, modifier: Modifier = Modifier, )` | Coordinates ordered setup steps, skip choices, saved profile/key/permission status and final persisted completion. | [L96](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt#L96) |
| `private fun WelcomeStep(onNext: () -> Unit, modifier: Modifier = Modifier)` | Shows feature/safety introduction and advances to sign-in/local continuation. | [L194](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt#L194) |
| `private fun SignInStep( authRepository: AuthRepository, onNeedGoogleSetup: () -> Unit, modifier: Modifier = Modifier, )` | Provides Google and local auth paths during setup, handling in-flight state and error feedback. | [L251](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt#L251) |
| `private fun HealthConnectStep( healthDataRepository: HealthDataRepository, snackbar: SnackbarHostState, onNext: () -> Unit, onSkip: () -> Unit, modifier: Modifier = Modifier, )` | Shows export/grant instructions, SDK/permission state and connection actions with provider/install fallbacks. | [L338](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt#L338) |
| `fun refreshStatus()` | Reloads Health Connect availability/permission diagnostics for the setup health step. | [L355](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt#L355) |
| `fun handleGranted(granted: Set<String>)` | Processes a permission result, refreshes health state and presents full/partial/no-grant guidance rather than assuming every requested metric is allowed. | [L366](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt#L366) |
| `fun startIntents(intents: List<Intent>, alsoSamsung: Boolean = false)` | Attempts ordered settings/install intents, optionally opens Samsung Health and records/report failures when none work. | [L391](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt#L391) |
| `fun launchPermissions(perms: Set<String>)` | Launches the Health Connect grant contract and tries the alternative launcher/settings path on activity/provider failure. | [L413](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt#L413) |
| `fun connectHealth()` | Selects SDK-appropriate permission or install/update/settings action based on availability and handles provider exceptions. | [L433](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt#L433) |
| `private fun IllustratedStep( number: Int, title: String, body: String, content: @Composable () -> Unit, )` | Displays one numbered/icon instruction in the Health Connect tutorial. | [L584](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt#L584) |
| `private fun GeminiStep( preferencesRepository: UserPreferencesRepository, aiClient: FallbackAiClient, onNext: () -> Unit, onSkip: () -> Unit, modifier: Modifier = Modifier, )` | Handles AI Studio opening, user-key save/test and offline skip in the wizard. | [L622](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt#L622) |
| `private fun ProfileStep( preferencesRepository: UserPreferencesRepository, prefilledName: String, initial: BodyProfile, onNext: () -> Unit, modifier: Modifier = Modifier, )` | Collects and validates initial personal/profile measurements and saves them for setup completion. | [L768](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt#L768) |
| `private fun DoneStep(onFinish: () -> Unit, modifier: Modifier = Modifier)` | Shows completion and calls the finish callback that persists setup completion. | [L939](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/setup/SetupWizardScreen.kt#L939) |

### app/src/main/java/com/migraineguard/app/feature/sleep/SleepStub.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/sleep/SleepStub.kt) · 137 bytes · 5 lines

**Purpose and behavior.** Legacy sleep feature marker. Real sleep sessions are read through HealthDataRepository and displayed with the fitness/dashboard screens.

**Declared types/objects:** `SleepFeature`.

### app/src/main/java/com/migraineguard/app/feature/track/TrackScreen.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/track/TrackScreen.kt) · 31615 bytes · 664 lines

**Purpose and behavior.** Fitness/sleep diagnostics, combined/Samsung step comparison, latest heart rate/distance/calorie/source cards and manual workout log/suggestion/history. Contains Health Connect grant/settings/install fallbacks and explicit manual refresh.

**Implementation and maintenance.** The primary runtime path collects the shared HealthRefreshCoordinator state. The private refresh helper is a fallback for a null coordinator, not an independent background sync service. Health permission results may be partial; settings fallbacks are selected from current device availability.

**Project dependencies:** `R`, `core.data.prefs.UserPreferences`, `core.data.prefs.UserPreferencesRepository`, `core.domain.HealthSnapshot`, `core.domain.SleepSnapshot`, `core.domain.WorkoutEntry`, `core.domain.WorkoutType`, `core.ui.ProgressRing`, `core.ui.SectionCard`, `feature.fitness.HealthConnectDiagnostics`, `feature.fitness.HealthConnectPackages`, `feature.fitness.HealthConnectStatus`, `feature.fitness.HealthDataRepository`, `feature.fitness.HealthAutoRefresh`, `feature.fitness.HealthRefreshCoordinator`, `feature.fitness.WorkoutRepository`, `feature.fitness.findActivity`, `feature.fitness.startHealthActivity`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun TrackScreen( healthDataRepository: HealthDataRepository, healthRefreshCoordinator: HealthRefreshCoordinator? = null, workoutRepository: WorkoutRepository, preferencesRepository: UserPreferencesRepository, modifier: Modifier = Modifier, )` | Composes current fitness/sleep source diagnostics and manual workout/suggestion/history controls with connection/refresh actions. | [L83](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/track/TrackScreen.kt#L83) |
| `fun applyDiag()` | Copies current repository diagnostics into the Track screen's local SDK/provider/error fields. | [L146](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/track/TrackScreen.kt#L146) |
| `fun handleGranted(granted: Set<String>)` | Processes a permission result, refreshes health state and presents full/partial/no-grant guidance rather than assuming every requested metric is allowed. | [L154](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/track/TrackScreen.kt#L154) |
| `fun startIntents(intents: List<Intent>, alsoSamsung: Boolean = false)` | Attempts ordered settings/install intents, optionally opens Samsung Health and records/report failures when none work. | [L187](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/track/TrackScreen.kt#L187) |
| `fun openHcFallback()` | Opens the available Health Connect/manage-permission/install alternatives and optionally Samsung Health. | [L210](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/track/TrackScreen.kt#L210) |
| `fun launchPermissions(perms: Set<String>)` | Launches the Health Connect grant contract and tries the alternative launcher/settings path on activity/provider failure. | [L218](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/track/TrackScreen.kt#L218) |
| `fun connectHealth()` | Selects SDK-appropriate permission or install/update/settings action based on availability and handles provider exceptions. | [L261](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/track/TrackScreen.kt#L261) |
| `fun reload()` | Requests explicit health refresh and updates manual Track diagnostics, preferring the shared coordinator when supplied. | [L322](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/track/TrackScreen.kt#L322) |
| `private suspend fun refresh( repo: HealthDataRepository, apply: (HealthConnectStatus, HealthSnapshot?, SleepSnapshot?, Boolean) -> Unit, )` | Coordinator implementation debounces/request-manages shared health reads and emits loading/results/errors/time. TrackScreen's helper performs a direct fallback read when no coordinator was supplied. | [L652](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/track/TrackScreen.kt#L652) |
| `private fun formatTs(millis: Long): String` | Formats the supplied epoch milliseconds into the feature's local human-readable timestamp; it has no persistence/network side effects. | [L660](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/track/TrackScreen.kt#L660) |

### app/src/main/java/com/migraineguard/app/feature/water/WaterIntakeRepository.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/water/WaterIntakeRepository.kt) · 3072 bytes · 68 lines

**Purpose and behavior.** Single Room-backed hydration counter/history with one-time legacy DataStore migration. Atomic changes prevent lost taps, greater existing history survives migration, and today’s Flow changes local day every 30 seconds.

**Declared types/objects:** `WaterDay`, `WaterIntakeRepository`.

**Project dependencies:** `core.data.db.WaterDayEntity`, `core.data.db.WaterHistoryDao`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `private suspend fun migrateLegacy()` | Mutex-protected one-time copy of the old water date/count into Room, preserving the greater existing value and removing old preference fields only after success. | [L32](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/water/WaterIntakeRepository.kt#L32) |
| `fun observeHistory(limit: Int = 14): Flow<List<WaterDay>>` | Maps recent Room water-day rows into the hydration history Flow. | [L54](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/water/WaterIntakeRepository.kt#L54) |
| `suspend fun addCup()` | Migrates legacy state if necessary and atomically adds one cup to today's Room row. | [L57](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/water/WaterIntakeRepository.kt#L57) |
| `suspend fun removeCup()` | Migrates legacy state if necessary and atomically removes one cup, with DAO clamping at zero. | [L58](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/water/WaterIntakeRepository.kt#L58) |
| `suspend fun setCups(cups: Int)` | Migrates legacy data and writes the bounded explicit count for today's date. | [L59](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/water/WaterIntakeRepository.kt#L59) |
| `suspend fun averageCups(start: LocalDate, end: LocalDate): Float` | Reads water rows for the given date range and averages recorded days; missing days are not fabricated readings. | [L63](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/water/WaterIntakeRepository.kt#L63) |

### app/src/main/java/com/migraineguard/app/feature/water/WaterScreen.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/water/WaterScreen.kt) · 5524 bytes · 124 lines

**Purpose and behavior.** Hydration goal/progress, add/remove cup controls and recorded-day history. Observes the Room-backed counter and preferences; no wearable fluid sensor is used.

**Project dependencies:** `core.data.prefs.UserPreferencesRepository`, `core.domain.WaterUnit`, `core.ui.ProgressRing`, `core.ui.SectionCard`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun WaterScreen( repository: WaterIntakeRepository, preferencesRepository: UserPreferencesRepository, modifier: Modifier = Modifier, )` | Composes today’s hydration goal/progress, add/remove cup actions and recorded-day history. | [L42](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/water/WaterScreen.kt#L42) |

### app/src/main/java/com/migraineguard/app/feature/weather/WeatherRepository.kt

[Open source](../app/src/main/java/com/migraineguard/app/feature/weather/WeatherRepository.kt) · 4497 bytes · 96 lines

**Purpose and behavior.** Optional coarse-location Open-Meteo client with rounded coordinates, bounded current-location wait, 30-minute cache, explicit stale fallback and null unavailable state. Maps weather codes to labels; it does not create placeholder readings.

**Implementation and maintenance.** WeatherSnapshot.isCachedOrFake is a legacy field name now used to label stale actual cache; this implementation does not create fake samples. Cache is process-memory only. Approximate coordinate transmission is user-initiated via the Home weather permission action.

**Declared types/objects:** `WeatherRepository`, `OpenMeteoWeatherRepository`.

**Project dependencies:** `core.ai.executeRequest`, `core.domain.WeatherSnapshot`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `suspend fun currentWeather(forceRefresh: Boolean = false): WeatherSnapshot?` | Returns a recent actual cached sample, attempts location/fetch when needed, marks stale fallback after failure or returns null without a sample. | [L23](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/weather/WeatherRepository.kt#L23) |
| `override suspend fun currentWeather(forceRefresh: Boolean): WeatherSnapshot?` | Returns a recent actual cached sample, attempts location/fetch when needed, marks stale fallback after failure or returns null without a sample. | [L29](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/weather/WeatherRepository.kt#L29) |
| `private suspend fun coordinates(): Pair<Double, Double>?` | Requires optional coarse permission, uses a bounded fresh/current location fix and rounds latitude/longitude to two decimals. | [L40](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/weather/WeatherRepository.kt#L40) |
| `private suspend fun fetch(lat: Double, lon: Double): WeatherSnapshot?` | Calls Open-Meteo with approximate coordinates, parses actual weather fields/time and returns no sample on ordinary failure while preserving cancellation. | [L66](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/weather/WeatherRepository.kt#L66) |
| `private fun weatherCodeLabel(code: Int): String` | Maps the provider's numerical weather code to a short presentation label. | [L84](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/feature/weather/WeatherRepository.kt#L84) |

### app/src/main/java/com/migraineguard/app/MainActivity.kt

[Open source](../app/src/main/java/com/migraineguard/app/MainActivity.kt) · 1591 bytes · 45 lines

**Purpose and behavior.** Android activity entry point. Reads preferences for theme, renders the navigation host using the application container and requests a debounced Health Connect refresh on resume.

**Declared types/objects:** `MainActivity`.

**Project dependencies:** `core.data.prefs.UserPreferences`, `core.design.MigraineGuardTheme`, `navigation.MigraineGuardNavHost`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `override fun onCreate(savedInstanceState: Bundle?)` | Android lifecycle entry. Initializes the component and installs its UI/dependencies; the enclosing class distinguishes application setup, main activity rendering and Health Connect rationale rendering. | [L19](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/MainActivity.kt#L19) |
| `override fun onResume()` | Requests a health refresh when the main activity resumes so previously granted permissions/shared readings can be reflected. | [L37](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/MainActivity.kt#L37) |

### app/src/main/java/com/migraineguard/app/MigraineGuardApp.kt

[Open source](../app/src/main/java/com/migraineguard/app/MigraineGuardApp.kt) · 478 bytes · 18 lines

**Purpose and behavior.** Application entry point. Creates the manual dependency container and notification channel. Its container lives for the process lifetime; reminder scheduling is handled by the settings/activity flow, not this constructor.

**Declared types/objects:** `MigraineGuardApp`.

**Project dependencies:** `di.AppContainer`, `feature.reminders.ReminderWorker`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `override fun onCreate()` | Android lifecycle entry. Initializes the component and installs its UI/dependencies; the enclosing class distinguishes application setup, main activity rendering and Health Connect rationale rendering. | [L12](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/MigraineGuardApp.kt#L12) |

### app/src/main/java/com/migraineguard/app/navigation/MigraineGuardNavHost.kt

[Open source](../app/src/main/java/com/migraineguard/app/navigation/MigraineGuardNavHost.kt) · 11046 bytes · 247 lines

**Purpose and behavior.** Conditional setup/auth/profile/Gemini gates plus the main Compose NavHost. Wires each screen to container dependencies, preserves bottom-tab state, chooses Coach/Analyst persona and triggers health refresh on tab navigation.

**Declared types/objects:** `BottomNavItem`.

**Project dependencies:** `core.ai.AiPersona`, `core.data.prefs.UserPreferences`, `di.AppContainer`, `feature.auth.SignInScreen`, `feature.body.BodyMetricsScreen`, `feature.coach.CoachScreen`, `feature.gemini.ConnectGeminiScreen`, `feature.home.HomeScreen`, `feature.insights.InsightsScreen`, `feature.meds.MedicationsScreen`, `feature.migraine.MigraineScreen`, `feature.nutrition.NutritionScreen`, `feature.profile.ProfileSetupScreen`, `feature.settings.SettingsScreen`, `feature.setup.SetupWizardScreen`, `feature.track.TrackScreen`, `feature.water.WaterScreen`.

**Data constructors and stored/input fields.** Defaults shown here are source defaults, not real user records. Computed/domain properties are explained in the guide.

`BottomNavItem`

| Field | Type / source default |
|---|---|
| `dest` | `BottomDest` |
| `icon` | `ImageVector` |

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `fun MigraineGuardNavHost(container: AppContainer)` | Chooses setup/auth/profile/key gates and builds the feature NavHost, supplying shared dependencies and bottom-tab state/navigation. | [L51](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/navigation/MigraineGuardNavHost.kt#L51) |
| `fun goBottom(route: String)` | Requests a debounced health refresh and navigates with state saving/restoration and single-top behavior. | [L109](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/navigation/MigraineGuardNavHost.kt#L109) |
| `fun openCoach(persona: AiPersona)` | Sets the desired Coach/Analyst persona and navigates to the Coach route. | [L119](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/main/java/com/migraineguard/app/navigation/MigraineGuardNavHost.kt#L119) |

### app/src/main/java/com/migraineguard/app/navigation/Routes.kt

[Open source](../app/src/main/java/com/migraineguard/app/navigation/Routes.kt) · 811 bytes · 30 lines

**Purpose and behavior.** Central route-string constants and bottom-destination metadata. Changing a route requires corresponding NavHost destination and callers.

**Declared types/objects:** `Routes`, `BottomDest`.

### app/src/main/java/com/migraineguard/app/PACKAGE.md

[Open source](../app/src/main/java/com/migraineguard/app/PACKAGE.md) · 428 bytes · 12 lines

**Purpose and behavior.** Short package/module orientation note, describing the intended feature-first Kotlin structure.

### app/src/main/res/drawable-xxhdpi/head_back.png

[Open source](../app/src/main/res/drawable-xxhdpi/head_back.png) · 141709 bytes · 566 lines

**Purpose and behavior.** Back-view raster for the migraine pain map. Stored normalized marker coordinates are interpreted relative to the selected view; image replacement must preserve the intended anatomical alignment.

### app/src/main/res/drawable-xxhdpi/head_front.png

[Open source](../app/src/main/res/drawable-xxhdpi/head_front.png) · 208014 bytes · 846 lines

**Purpose and behavior.** Front-view raster for the migraine pain map. Used as the visual reference for user-entered points.

### app/src/main/res/drawable-xxhdpi/head_left.png

[Open source](../app/src/main/res/drawable-xxhdpi/head_left.png) · 212678 bytes · 823 lines

**Purpose and behavior.** Left-profile raster for the migraine pain map. Actual assets are inputs to drawing, not medical segmentation models.

### app/src/main/res/drawable-xxhdpi/head_top.png

[Open source](../app/src/main/res/drawable-xxhdpi/head_top.png) · 208014 bytes · 846 lines

**Purpose and behavior.** Top-view raster name for the pain map. The initial audit flagged asset/view alignment concerns; review actual content before replacing it or claiming precise anatomical localization.

### app/src/main/res/drawable/ic_launcher_foreground.xml

[Open source](../app/src/main/res/drawable/ic_launcher_foreground.xml) · 618 bytes · 15 lines

**Purpose and behavior.** Vector foreground artwork for adaptive launcher icons.

### app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml

[Open source](../app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml) · 270 bytes · 6 lines

**Purpose and behavior.** Round adaptive launcher icon variant.

### app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml

[Open source](../app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml) · 270 bytes · 6 lines

**Purpose and behavior.** Adaptive launcher icon joining foreground/background resources on supported Android.

### app/src/main/res/values/colors.xml

[Open source](../app/src/main/res/values/colors.xml) · 211 bytes · 7 lines

**Purpose and behavior.** Named XML color resources, including launcher background.

**Resource/component identifiers:** `ic_launcher_background`, `seed_teal`, `seed_indigo`.

### app/src/main/res/values/strings.xml

[Open source](../app/src/main/res/values/strings.xml) · 1821 bytes · 18 lines

**Purpose and behavior.** Application/Preview names and selected user-facing string resources. Many feature strings currently remain inline Kotlin and need deliberate localization work.

**Resource/component identifiers:** `app_name`, `app_version_label`, `nav_fitness`, `health_connect_rationale_title`, `health_connect_rationale`, `connect_health_connect`, `open_health_connect_settings`, `exercise_of_the_day`, `steps_today`, `workout_log`, `sign_in_subtitle`, `continue_demo`, `sign_in_google`, `preview_app_name`.

### app/src/main/res/values/themes.xml

[Open source](../app/src/main/res/values/themes.xml) · 326 bytes · 8 lines

**Purpose and behavior.** Android activity base theme surrounding Compose rendering; app-level colors/typography are applied in Theme.kt.

**Resource/component identifiers:** `Theme.MigraineGuard`, `android:statusBarColor`, `android:navigationBarColor`.

### app/src/main/res/xml/backup_rules.xml

[Open source](../app/src/main/res/xml/backup_rules.xml) · 313 bytes · 9 lines

**Purpose and behavior.** Legacy Android full-backup exclusion rules protecting private app domains. Manifest allowBackup=false additionally disables automatic backup.

### app/src/main/res/xml/data_extraction_rules.xml

[Open source](../app/src/main/res/xml/data_extraction_rules.xml) · 630 bytes · 18 lines

**Purpose and behavior.** Modern Android cloud-backup/device-transfer exclusions for private storage. Does not prevent explicitly selected encrypted export.

### app/src/main/res/xml/file_paths.xml

[Open source](../app/src/main/res/xml/file_paths.xml) · 160 bytes · 6 lines

**Purpose and behavior.** FileProvider allowed paths for camera food photos, including files/food_photos and the retained photo cache path. Sharing grants are URI-specific; this is not a broad external-files permission.

**Resource/component identifiers:** `food_cache`, `food_photos`.

### app/src/test/java/com/migraineguard/app/core/ai/FallbackAiClientTest.kt

[Open source](../app/src/test/java/com/migraineguard/app/core/ai/FallbackAiClientTest.kt) · 3087 bytes · 73 lines

**Purpose and behavior.** JVM unit test suite for FallbackAiClientTest. Uses synthetic values; function entries below explain each behavioral assertion. Runs via :app:testDebugUnitTest.

**Declared types/objects:** `TestProvider`, `FallbackAiClientTest`.

**Project dependencies:** `core.data.Result`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `override suspend fun complete(prompt: String): Result<String>` | Implements or invokes the suspend text-completion contract. Provider implementations prepare authenticated HTTP requests; fallback/local implementations handle ordered providers or deterministic local reports. See the enclosing file's algorithm notes. | [L18](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/ai/FallbackAiClientTest.kt#L18) |
| `@Test fun successUsesPriorityAndStopsFurtherTransmission()` | Checks provider priority and that a successful earlier answer prevents sending to later providers. | [L25](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/ai/FallbackAiClientTest.kt#L25) |
| `@Test fun exceptionAndBlankResponseContinueToUsableProvider()` | Checks that exceptions and blank answers permit reaching a usable fallback. | [L32](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/ai/FallbackAiClientTest.kt#L32) |
| `@Test fun providerTimeoutContinuesToFallback()` | Checks a provider's own timeout advances the chain. | [L39](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/ai/FallbackAiClientTest.kt#L39) |
| `@Test fun cancellationNeverSendsToAnotherProvider()` | Checks explicit cancellation aborts rather than sending the prompt onward. | [L47](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/ai/FallbackAiClientTest.kt#L47) |
| `@Test fun parentTimeoutNeverStartsAnotherProvider()` | Checks parent coroutine timeout is propagated and prevents a second provider request. | [L57](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/ai/FallbackAiClientTest.kt#L57) |
| `@Test fun noProvidersReportsUnavailable()` | Checks an empty provider list returns an unavailable result. | [L69](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/ai/FallbackAiClientTest.kt#L69) |

### app/src/test/java/com/migraineguard/app/core/ai/NutritionIntegrityTest.kt

[Open source](../app/src/test/java/com/migraineguard/app/core/ai/NutritionIntegrityTest.kt) · 3472 bytes · 80 lines

**Purpose and behavior.** JVM unit test suite for NutritionIntegrityTest. Uses synthetic values; function entries below explain each behavioral assertion. Runs via :app:testDebugUnitTest.

**Declared types/objects:** `NutritionIntegrityTest`.

**Project dependencies:** `core.data.Result`, `core.data.db.NutritionScanEntity`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `@Test fun offlineFailureDoesNotInventNutritionOrTriggers()` | Checks provider failure returns unknown nutrition/trigger values rather than fake estimates. | [L10](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/ai/NutritionIntegrityTest.kt#L10) |
| `@Test fun malformedProviderResponseStaysUnavailable()` | Checks non-JSON/unusable food responses preserve unknown nutrition. | [L22](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/ai/NutritionIntegrityTest.kt#L22) |
| `@Test fun validZeroNutritionIsPreserved()` | Checks true zero nutrition fields are retained instead of being converted into missing values. | [L29](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/ai/NutritionIntegrityTest.kt#L29) |
| `@Test fun negativeAndNonFiniteValuesAreDiscarded()` | Checks impossible negative/infinite/NaN values are rejected by parsing bounds. | [L38](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/ai/NutritionIntegrityTest.kt#L38) |
| `@Test fun attachedPhotoIsNeverClaimedToHaveBeenAnalyzed()` | Checks the description-only prompt does not pretend an attached local photo was seen by a model. Explicit Gemini image sending is a separate path. | [L49](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/ai/NutritionIntegrityTest.kt#L49) |
| `override suspend fun complete(prompt: String): Result<String>` | Implements or invokes the suspend text-completion contract. Provider implementations prepare authenticated HTTP requests; fallback/local implementations handle ordered providers or deterministic local reports. See the enclosing file's algorithm notes. | [L54](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/ai/NutritionIntegrityTest.kt#L54) |
| `@Test fun legacyPlaceholderNumbersAndTriggersDoNotReachInsights()` | Checks old fabricated placeholder entries are suppressed during entity/domain mapping. | [L63](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/ai/NutritionIntegrityTest.kt#L63) |

### app/src/test/java/com/migraineguard/app/core/security/BackupCipherTest.kt

[Open source](../app/src/test/java/com/migraineguard/app/core/security/BackupCipherTest.kt) · 1207 bytes · 27 lines

**Purpose and behavior.** JVM unit test suite for BackupCipherTest. Uses synthetic values; function entries below explain each behavioral assertion. Runs via :app:testDebugUnitTest.

**Declared types/objects:** `BackupCipherTest`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `@Test fun roundTripAndRandomizedExports()` | Checks a password-encrypted payload decrypts correctly and two exports of the same plaintext differ due to random salt/nonce. | [L9](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/security/BackupCipherTest.kt#L9) |
| `@Test fun wrongPasswordAndTamperingAreRejected()` | Checks password mismatch and modified ciphertext fail authentication. | [L16](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/security/BackupCipherTest.kt#L16) |
| `@Test fun invalidFilesAndShortPasswordsAreRejected()` | Checks invalid envelope input and too-short passwords are rejected. | [L22](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/security/BackupCipherTest.kt#L22) |

### app/src/test/java/com/migraineguard/app/core/time/HealthDayTest.kt

[Open source](../app/src/test/java/com/migraineguard/app/core/time/HealthDayTest.kt) · 1577 bytes · 38 lines

**Purpose and behavior.** JVM unit test suite for HealthDayTest. Uses synthetic values; function entries below explain each behavioral assertion. Runs via :app:testDebugUnitTest.

**Declared types/objects:** `HealthDayTest`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `@Test fun todayStartsAtLocalMidnightInsteadOfTwentyFourHoursAgo()` | Checks local midnight conversion against a known New York timestamp. | [L12](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/time/HealthDayTest.kt#L12) |
| `@Test fun daylightSavingDayCanContainTwentyFiveHours()` | Checks consecutive local midnights around fall-back differ by 25 hours. | [L17](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/time/HealthDayTest.kt#L17) |
| `@Test fun earlyMorningQueryIncludesTheWholeCurrentCalendarDay()` | Checks a 1:41 AM local query ends at next midnight instead of now and includes a complete ordinary calendar day. | [L22](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/time/HealthDayTest.kt#L22) |
| `@Test fun springForwardDayEndsAfterTwentyThreeHours()` | Checks full-day bounds on the 23-hour spring-forward date. | [L28](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/time/HealthDayTest.kt#L28) |
| `@Test fun fallBackDayEndsAfterTwentyFiveHours()` | Checks exclusive day-end bounds on the 25-hour fall-back date. | [L33](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/core/time/HealthDayTest.kt#L33) |

### app/src/test/java/com/migraineguard/app/feature/backup/BackupMergeTest.kt

[Open source](../app/src/test/java/com/migraineguard/app/feature/backup/BackupMergeTest.kt) · 967 bytes · 26 lines

**Purpose and behavior.** JVM unit test suite for BackupMergeTest. Uses synthetic values; function entries below explain each behavioral assertion. Runs via :app:testDebugUnitTest.

**Declared types/objects:** `BackupMergeTest`, `Record`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `@Test fun collidingSourceIdsKeepBothDistinctRecords()` | Checks ID normalization preserves distinct record contents when source/target keys collide. | [L10](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/feature/backup/BackupMergeTest.kt#L10) |
| `@Test fun repeatedRecordsAreNotDuplicated()` | Checks repeated identical incoming content is inserted only once. | [L18](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/feature/backup/BackupMergeTest.kt#L18) |

### app/src/test/java/com/migraineguard/app/feature/fitness/StepSyncMessageTest.kt

[Open source](../app/src/test/java/com/migraineguard/app/feature/fitness/StepSyncMessageTest.kt) · 1031 bytes · 26 lines

**Purpose and behavior.** JVM unit test suite for StepSyncMessageTest. Uses synthetic values; function entries below explain each behavioral assertion. Runs via :app:testDebugUnitTest.

**Declared types/objects:** `StepSyncMessageTest`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `@Test fun failedReadDoesNotClaimZeroSteps()` | Checks a failed nullable step read is explained as unavailable rather than measured zero. | [L7](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/feature/fitness/StepSyncMessageTest.kt#L7) |
| `@Test fun missingSamsungDataGivesSharingAndSyncSteps()` | Checks the absence of Samsung data produces writer-sharing/sync guidance. | [L10](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/feature/fitness/StepSyncMessageTest.kt#L10) |
| `@Test fun higherSamsungCountExplainsPriorityWithoutAddingCounts()` | Checks a higher Samsung comparison suggests source-priority review without summing duplicate totals. | [L14](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/feature/fitness/StepSyncMessageTest.kt#L14) |
| `@Test fun failedSamsungCheckIsNotReportedAsNoSamsungData()` | Checks a source-query failure is distinguished from an empty successful query. | [L19](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/feature/fitness/StepSyncMessageTest.kt#L19) |
| `@Test fun matchingCountsExplainArrivalDelay()` | Checks matching totals produce honest arrival/sync guidance. | [L22](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/feature/fitness/StepSyncMessageTest.kt#L22) |

### app/src/test/java/com/migraineguard/app/feature/profile/BodyUnitsTest.kt

[Open source](../app/src/test/java/com/migraineguard/app/feature/profile/BodyUnitsTest.kt) · 589 bytes · 18 lines

**Purpose and behavior.** JVM unit test suite for BodyUnitsTest. Uses synthetic values; function entries below explain each behavioral assertion. Runs via :app:testDebugUnitTest.

**Declared types/objects:** `BodyUnitsTest`.

**Named functions.**

| Function / signature | What it does and effects | Source |
|---|---|---|
| `@Test fun savedHeightDeterminesImperialFields()` | Checks stored centimeters determine feet/inches initialization. | [L7](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/feature/profile/BodyUnitsTest.kt#L7) |
| `@Test fun weightUnitSwitchPreservesThePhysicalWeight()` | Checks metric/imperial conversion retains physical weight within tolerance. | [L11](https://github.com/jdp323/migraineguard/blob/61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa/app/src/test/java/com/migraineguard/app/feature/profile/BodyUnitsTest.kt#L11) |

### build.gradle.kts

[Open source](../build.gradle.kts) · 310 bytes · 9 lines

**Purpose and behavior.** Root plugin declarations/version aliases with apply false; actual app configuration is in app/build.gradle.kts.

### docs/FEATURE_STATUS.md

[Open source](../docs/FEATURE_STATUS.md) · 3926 bytes · 33 lines

**Purpose and behavior.** Implemented-feature matrix with practical external/hardware limitations and verification scope.

### docs/images/android-startup-v1.6.3.png

[Open source](../docs/images/android-startup-v1.6.3.png) · 37837 bytes (binary asset)

**Purpose and behavior.** API-35 hosted-emulator startup capture after the successful instrumentation tests, showing the first-run welcome screen with synthetic/default state. This verifies startup rendering, not every screen or hardware integration.

### docs/PHONE_TESTING.md

[Open source](../docs/PHONE_TESTING.md) · 4320 bytes · 43 lines

**Purpose and behavior.** Persistent signing, Google setup, StepSync diagnostics, old-install safety, Preview package and full-day query verification.

### docs/PROJECT_AUDIT.md

[Open source](../docs/PROJECT_AUDIT.md) · 25300 bytes · 165 lines

**Purpose and behavior.** Detailed original takeover audit/file inventory and priorities, with a pointer to later fixes. Earlier findings are historical rather than proof of current defects.

### docs/PROJECT_GUIDE.md

[Open source](../docs/PROJECT_GUIDE.md) · 30100 bytes · 273 lines

**Purpose and behavior.** This release's architecture, end-to-end workflows, storage/network boundaries, exact validation, build/release operations and future maintenance guidance.

### docs/TAKEOVER.md

[Open source](../docs/TAKEOVER.md) · 4522 bytes · 38 lines

**Purpose and behavior.** Takeover baseline, branch/local snapshot and Git management instructions, build/release flow and initial unresolved tasks.

### gradle.properties

[Open source](../gradle.properties) · 136 bytes · 5 lines

**Purpose and behavior.** Gradle JVM/build/Android/Kotlin settings. Memory values should match the host; CI uses its own available resources.

### gradle/libs.versions.toml

[Open source](../gradle/libs.versions.toml) · 3719 bytes · 55 lines

**Purpose and behavior.** Version catalog for plugins and AndroidX/Compose/coroutine/Room/network dependencies. Build files use aliases; changing versions may require compatibility and runtime revalidation.

**Pinned version declarations.**

```toml
[versions]
agp = "8.7.3"
kotlin = "2.0.21"
ksp = "2.0.21-1.0.28"
coreKtx = "1.15.0"
lifecycleRuntimeKtx = "2.8.7"
activityCompose = "1.9.3"
composeBom = "2024.12.01"
navigationCompose = "2.8.5"
datastorePreferences = "1.1.1"
credentials = "1.3.0"
googleid = "1.1.1"
coroutines = "1.9.0"
room = "2.6.1"
okhttp = "4.12.0"
healthConnect = "1.1.0-alpha11"
coil = "2.7.0"
serialization = "1.7.3"
workRuntime = "2.10.0"
```

### gradle/wrapper/gradle-wrapper.jar

[Open source](../gradle/wrapper/gradle-wrapper.jar) · 43583 bytes · 152 lines

**Purpose and behavior.** Binary Gradle bootstrap, not application code. Runs the Gradle distribution pinned by wrapper properties.

### gradle/wrapper/gradle-wrapper.properties

[Open source](../gradle/wrapper/gradle-wrapper.properties) · 253 bytes · 8 lines

**Purpose and behavior.** Pinned Gradle distribution URL/validation and wrapper settings. Keep wrapper JAR/properties/scripts consistent when upgrading.

### gradlew

[Open source](../gradlew) · 8784 bytes · 253 lines

**Purpose and behavior.** Unix Gradle bootstrap script. Requires executable permission and a compatible JAVA_HOME/JDK; delegates to wrapper JAR.

### gradlew.bat

[Open source](../gradlew.bat) · 2894 bytes · 95 lines

**Purpose and behavior.** Windows Gradle bootstrap script using JAVA_HOME or the available Java executable.

### local.properties.example

[Open source](../local.properties.example) · 627 bytes · 18 lines

**Purpose and behavior.** Example local Android SDK path configuration. Copy to ignored local.properties and use the actual machine path; do not commit the user's paths.

### README.md

[Open source](../README.md) · 6968 bytes · 122 lines

**Purpose and behavior.** Project landing page with current version, release/Preview links, feature overview, build commands and historical release notes. Historical sections describe older behavior; current status is in the guide and feature matrix.

### settings.gradle.kts

[Open source](../settings.gradle.kts) · 538 bytes · 25 lines

**Purpose and behavior.** Plugin/dependency repositories, root project name and inclusion of the app module. Repository resolution configuration affects all builds.

### tools/git-manage.cmd

[Open source](../tools/git-manage.cmd) · 2719 bytes · 62 lines

**Purpose and behavior.** Windows Git helper providing Status, CheckAccess, Login, Fetch, Snapshot and Backup. Snapshot stages all non-ignored changes and commits them, so review status first. CheckAccess proves read access only; Login invokes Git Credential Manager device login; network operations still require authentication. Backup creates/verifies a bundle of committed history in ignored dist.

**Implementation and maintenance.** Control labels: Status shows local changes/branch; CheckAccess calls noninteractive ls-remote and proves read only; Login starts Credential Manager device login; Fetch uses origin; Snapshot stages non-ignored work and commits if changed; Backup writes/verifies --all history bundle. The helper does not choose which source changes are safe to publish.

### docs/FILE_REFERENCE.md

This document is the reference itself. It describes 114 files and 385 named function declarations (including repeated interface/implementation methods and test helpers). Update source links/signatures and the semantic descriptions when changing application code.

## Non-source local/generated directories

| Path/category | Why it is excluded and how it works |
|---|---|
| `.git/` | Local Git object database, references and index. Exact imported GitHub objects were hash-verified; never edit history casually. |
| `.gradle/`, `app/build/`, `build/` | Generated caches, compiled resources/classes, Room implementations and APKs. Rebuilt by Gradle; not source. |
| `.idea/`, local IDE metadata | Machine/Android Studio state; not required to understand application behavior. |
| `local.properties` | Actual local SDK path/build configuration. Keep ignored and use the example. |
| `.local-android/signing/` | Private retained keystore and prepared secret/setup files. Never include the key/base64 in Git or docs. |
| `verification/` | Downloaded test reports/APKs, release/privacy evidence and temporary archives. Explicit safe startup image/schema were copied into tracked documentation/source-schema locations. |
| `dist/` | Local APK copies, Git bundles/source archives. GitHub Releases serves published APKs. Bundles may include private history and require appropriate storage. |

## Accuracy and extension rules

The source is authoritative. This is a reviewed source reference, not a Kotlin compiler/AST specification; anonymous lambdas, getters and generated synthetic methods are explained within their enclosing classes/files rather than counted as separate named functions. Refer to the linked declaration/body for exact overload/visibility/type bounds. File sizes/line numbers describe this baseline. Runtime verification applies to the documented release and test scope, not every feature/OEM/provider.
