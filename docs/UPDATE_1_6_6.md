# v1.6.6 — account isolation and Health Connect readings

This update addresses two phone reports: steps/heart rate/sleep remained unavailable despite granted permissions, and changing Google accounts reused another user's name, API key and records.

## Account ownership and migration

Before this version, authentication changed only an in-memory AuthState. Every identity read `migraineguard.db` and `user_settings.preferences_pb`. Signing out therefore did not separate users.

Each Google login now selects a separate Room database and Preferences DataStore. The account key is a SHA-256 hash of the normalized Credential Manager account identifier. File names contain no email address. Keys remain encrypted with Android Keystore. Separate storage covers all seven tables: migraine episodes, nutrition scans, water history, workouts, weight entries, medications and AI sessions. Profile fields, goals, setup flags and all three AI provider keys are also separate.

Signing out replaces the repository graph with an empty signed-out graph, cancels the previous graph's background scope, clears the Compose/navigation state, cancels reminders and removes delivered notifications. Credential Manager's provider state is cleared on sign-out. Pending UI requests are disposed; any already completed remote request cannot be recalled. A new graph is created even when returning to the same account, so cached health/UI state is not reused.

**Older data has no trustworthy account owner.** This version preserves the original database and preferences under **Continue locally**, rather than assigning them to the next Google account. A new Google account starts empty, and can configure its own key/profile or deliberately import an encrypted backup. There is no automatic copying between accounts. Selecting Continue locally is an explicit way to reopen the shared local profile on this Android user. Google sign-in here selects local storage; this is not cloud account synchronization or a replacement for Android device security.

Login remains required again after process death, as before. Restarting does not silently restore a Google session. ReminderWorker exits while signed out and reads only the active account's settings while signed in.

## Device-wide health store

Health Connect belongs to the phone's Android user, **not** the selected Google account. Android's permission grant alone cannot establish whose watch data it contains. Every local/Google account must explicitly connect this phone's health data before the app reads it. This opt-in is stored per account and can coexist with partial Android permissions. A Google account change cannot bypass this opt-in. The Fitness screen and setup screen explain the device scope.

Returning to an account preserves that account's opt-in. Health Connect will still contain the phone owner's records; it does not switch to a different person's watch when Google login changes.

## Read behavior

- **Steps:** aggregate the full local calendar day. Query Samsung Health independently even if the combined aggregate returns empty or throws. Use Samsung's aggregate only when the combined total is unavailable, and label this fallback. Never add the two totals or sum raw records. An actual zero stays zero. An empty successful query reports no shared records rather than pretending permission was denied.
- **Heart rate:** check the individual read permission and search the last seven days across all pages. Select the latest sample timestamp, rather than just the first record in today's window. This fixes the early-morning case where the latest watch sample belongs to yesterday. Show recorded date/time, source and an explicit recorded-versus-live label.
- **Sleep:** check the individual read permission and search the last 14 days across all pages. Choose the latest completed, positive-duration session. Show its end date/time and source. Sessions older than 36 hours are labeled older and excluded from current sleep goals/Insights/offline hints through `recordedHours`.
- **Permission checks:** a failed permission query is an error, not an empty grant list. Read failures and revoked permissions are surfaced separately from successful empty queries. Optional missing permissions do not block other available types.
- **Pagination:** follow page tokens, with a 100-page safety bound. Token/IPC errors do not turn partial data into a successful current reading. Tests use a two-record page size to exercise paging.
- **Manual steps:** clear Health Connect diagnostics on the manual snapshot so the manually entered value can actually display.
- **SDK:** upgrade Health Connect from 1.1.0-alpha11 to stable 1.1.0. This requires compile SDK 36 and Android Gradle Plugin 8.9.2; minimum Android 26 and target SDK 35 remain unchanged. The Gradle wrapper remains 8.11.1. The testing library is used only by tests.

The Fitness diagnostics show the exact installed package (Standard and Preview have different grants), the per-account connection state and separate Steps/Heart rate/Sleep read grants.

## Phone troubleshooting

1. Install the new APK matching the app you are testing: Standard `com.migraineguard.app` or Preview `com.migraineguard.app.preview`. Their permissions and local records are separate.
2. Sign in or Continue locally. In Fitness, connect this phone's health data for that account.
3. In Health Connect → App permissions, allow this installed MigraineGuard app to **read** Steps, Heart rate and Sleep.
4. In Health Connect → App permissions → Samsung Health, allow **write** access for Steps, Heart rate and Sleep. Opening Samsung Health after changing permissions is necessary to trigger sharing.
5. Open Samsung Health, connect/sync the watch, and check Health Connect's stored Steps, Heart rate and Sleep entries. Then return to MigraineGuard and refresh.
6. A granted-but-empty message means the query succeeded but no matching records were returned. If Health Connect contains the records, compare the exact package, displayed timezone/date and activity source priority with the app's diagnostics. A dated old sample is not live BPM or last night's sleep.

The app cannot grant Samsung's write permissions, force the watch to sync, or create records Samsung hasn't shared. Actual Samsung/watch transfer still requires phone verification. No live account or health records are used by automated tests.

Official references:
- [Read data and aggregate cumulative counts](https://developer.android.com/health-and-fitness/health-connect/read-data)
- [Health Connect releases](https://developer.android.com/jetpack/androidx/releases/health-connect)
- [Google's fake-client testing guide](https://developer.android.com/health-and-fitness/health-connect/test/unit-tests)
- [Samsung Health Connect FAQ](https://developer.samsung.com/health/health-connect-faq.html)
- [Samsung sharing integration](https://developer.samsung.com/health/blog/en/accessing-samsung-health-data-through-health-connect)

## Files and functions

### New production files

- `core/security/AccountStorage.kt`: LOCAL and SIGNED_OUT identify the two non-Google stores; google normalizes/hashes a nonblank account identifier; settingsName and databaseName map an account to its persistent files. The local names remain compatible with older installations.
- `feature/fitness/AccountHealthDataRepository.kt`: delegates platform setup operations, uses enabled to read the account's opt-in, allowDeviceHealth persists opt-in, hasAllPermissions checks opt-in plus platform grants, latestFitness/latestSleep return unconnected states without reading the shared device store until connected.

### Modified production files

- `MigraineGuardApp.kt`: containers exposes the active graph; container returns it synchronously for lifecycle/workers; onCreate creates shared auth and the signed-out graph; switchAccount selects scoped storage, cancels old work/notifications, replaces the graph and clears credential-provider state.
- `MainActivity.kt`: onCreate observes the graph and keys the entire composition by graph identity; onResume refreshes only while signed in.
- `di/AppContainer.kt`: accepts account/sharedAuth; creates scoped preferences/database/repositories and gated health access; dispose cancels its background scope.
- `feature/auth/AuthRepository.kt`: resolves client settings through the active preference supplier; signInDemo opens the preserved local store; signInWithGoogle calls activateGoogleProfile only after a returned Google credential; activateGoogleProfile swaps stores before seeding the correct profile name; signOut replaces the graph. Auth state is still in memory.
- `core/data/prefs/UserPreferencesRepository.kt`: AccountPreferenceStores.get caches one DataStore per absolute scoped file; constructor accepts an account; preferences includes deviceHealthEnabled; setDeviceHealthEnabled writes opt-in. Existing setters/getters now operate on the selected account's file.
- `core/data/db/AppDatabase.kt`: get caches one Room instance per absolute account database path, preserving migrations and schema version 4.
- `feature/water/WaterIntakeRepository.kt`: migrateLegacy skips original water DataStore migration for non-local accounts, preventing the older water counter from leaking into a Google account.
- `feature/reminders/ReminderWorker.kt`: doWork returns while signed out and reads only current-account preferences.
- `navigation/MigraineGuardNavHost.kt`: waits for loaded preferences before choosing screens; uses the signed-out graph for login/setup. The activity's composition key resets all remembered navigation/UI state when accounts change.
- `feature/setup/SetupWizardScreen.kt`: starts at Health Connect after account selection and explains phone-wide health scope; connectHealth opts this account in before asking for platform read grants.
- `feature/auth/SignInScreen.kt`: explains isolated Google stores and preserved older local records.
- `core/domain/Models.kt`: HealthSnapshot adds recorded-heart timestamp/source/status and permission summary; SleepSnapshot adds session timestamp and isRecent; recordedHours excludes older sessions.
- `feature/fitness/HealthDataRepository.kt`: allowDeviceHealth is an optional interface hook; constructor accepts injected client/status/clock/page size for synthetic tests; sdkStatusRaw uses the injected status in tests; readAll follows typed read-record pages; readFailure gives permission versus operation error messages; latestFitness and latestSleep implement the read rules above. requiredPermissions requests reads only and no unused exercise-write grant.
- `feature/fitness/StepSyncMessage.kt`: stepSyncMessage treats a null successful result as empty shared data; source-priority and Samsung-sync guidance remain.
- `feature/track/TrackScreen.kt`: connect action records account opt-in; diagnostics show package and read grants; manual-step apply clears old stepSync; sleep/heart cards show dated results and specific status text.
- `feature/home/HomeScreen.kt`: progress uses recordedHours so old sleep cannot fill today's goal.

### Tests and build files

- `core/security/AccountStorageTest.kt`: proves stable normalization, distinct accounts, no email in path, and legacy local-path compatibility.
- `AccountIsolationTest.kt`: synthetic A → sign-out → B → A → local sequence through the real activation callback. Verifies all seven B tables empty, all provider keys and profile private, health opt-in separate, signed-out graph empty, and A restored.
- `HealthReadingTest.kt`: six fake-client scenarios cover successful empty reads, missing permissions, Samsung fallback after aggregate failure, a real zero plus previous-day heart/paginated latest sleep, older sleep freshness, and operation errors/cancellation. Fakes do not prove Samsung device transport.
- `NavigationFlowTest.kt`: seeds the explicit local graph before running onboarding and navigation tests.
- `StepSyncMessageTest.kt`: asserts the corrected empty-data explanation.
- `app/build.gradle.kts`, `gradle/libs.versions.toml`: version 1.6.6/code 13, stable Health Connect and compatible build toolchain.
- `.github/workflows/android.yml`: runtime gate updated for 13 Android tests and release notes describe account isolation and health checks.

## Verification

Checks and publication results will be recorded after completion. Expected suite: 36 JVM tests and 13 Android tests. Retained APK certificate, Standard/Preview packages and APK checksums must be verified before delivery.
