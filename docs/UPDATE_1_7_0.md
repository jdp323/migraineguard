# v1.7.0: account restore, Samsung reads and interface refresh

Version 1.7.0 / code 14. This supplements the full [project guide](PROJECT_GUIDE.md) and [file/function reference](FILE_REFERENCE.md). The older supplements document the retained account isolation, signing and data-preservation behavior.

## Sign-in and backup sequence

1. Credential Manager signs in to the selected Google account. AccountStorage hashes its normalized email into a separate database/settings namespace. A different account never reads the previous account's local database or keys.
2. Before onboarding/profile forms, DriveBackupScreen asks Google AuthorizationClient for drive.appdata and userinfo.email, bound with setAccount. Existing grants can return a fresh access token silently. First-time consent stays in the native Google screen on the phone.
3. DriveRestClient verifies the token's email through Google's userinfo endpoint, then lists only the app's own appDataFolder for migraineguard-account-v2.json. The decoded backup must match the current account hash and supported format/version/bounds.
4. Journal restore merges seven tables using the existing idempotent merge. Settings restore applies one atomic DataStore edit only when the incoming settings revision is at least as recent as the local revision. API keys are encrypted using this phone's Android Keystore after restore. A Google fallback display name does not count as a new user edit.
5. A completed restored profile skips the birthday/measurements/key forms and checks this phone's Health Connect permissions. Health permission/device opt-in is never copied from another phone. New accounts with no backup continue through normal setup.
6. Room table invalidations and portable preference changes trigger a backup after four quiet seconds. Before saving, the app inspects/merges the remote copy again. A stable data fingerprint excludes export timestamps, avoiding repeated uploads when data is unchanged. Existing ETags use If-Match; HTTP 412 leaves local data intact and reports the conflict.
7. Sign-out waits for a final backup attempt when connected. Failure gives a visible choice to keep the session or sign out retaining the local copy. App backgrounding makes a best-effort save in the account scope. Android may kill the process before a background network request completes; there is no claim of an always-running service. Back up now gives an explicit completed-save status.
8. Access tokens live only in memory. Already granted access is refreshed silently for later saves; if permission needs a new prompt, Settings offers reconnect. Pausing retains the cloud file. Offline, denial, malformed files and quota failures preserve local data and do not display successful backup.

## Data and security boundary

Cloud data includes the profile/name/DOB/body measurements, goals, appearance/units/reminders, all three personal AI keys, setup status, step-source selection and the seven journal tables: migraine episodes, nutrition scans, water history, workouts, weights, medications and AI sessions. Photos themselves are not uploaded: existing nutrition photo paths are local references and may not resolve on a new phone. Health Connect's underlying steps/BPM/sleep database is not copied; the destination phone reads its own permissioned Health Connect records.

Drive backups use HTTPS and Google's account/storage encryption. The JSON contains portable key values so a different phone can restore them. It is **not** separately password-encrypted or end-to-end encrypted. The screen explains this before native authorization. appDataFolder is hidden app-specific storage, not a normal visible My Drive folder. Google's account security and user-granted app-data permission are the cloud trust boundary. A password-encrypted manual journal export remains available separately and still excludes AI keys.

No developer key, OAuth Web secret, access token, real health records or personal account email is added to source/GitHub. Account hashes are ownership checks, not encryption keys. Personal Gemini/Groq/OpenRouter keys remain encrypted in local DataStore and masked in Settings. Standard/Preview have separate local data. Cloud app data belongs to the authorized Google account and OAuth application; don't assume independent cloud backups when using both builds against the same Google project.

Limits: one current cloud snapshot, 20 MB validation/read bound, no attachments, no server-side scheduler or guaranteed concurrent multi-device synchronization. Timestamp ordering can be affected by incorrectly set device clocks. Journal merge preserves additions and does not implement cloud deletion tombstones; deleting locally can be reintroduced by a later merge. Do not present this as a full bidirectional sync service. New records and profile/key restoration are the supported backup workflow.

## Samsung Health investigation

The user confirmed real Samsung records are visible inside Health Connect. The application therefore changes its reads instead of assuming missing sharing.

- Steps prefer Samsung Health's independent aggregate by default, with a Combined Health Connect option. Neither option adds the two totals together.
- When the relevant aggregate is empty/zero, raw records are read across the full local calendar day, including Samsung buckets ending tonight. Records are grouped by writer. Within a single writer, a sweep over interval boundaries takes the largest overlapping step density rather than summing overlaps. Disjoint intervals accumulate; intervals crossing midnight are proportionally clipped. This is deliberately labeled a conservative raw-record estimate, not the authoritative aggregate or an exact Samsung UI match.
- A positive raw Samsung count can recover an aggregate zero. Other writers are never summed with Samsung. A truly empty read stays unavailable; a genuine aggregate zero stays zero.
- Heart queries include the end of the local day so a day-long Samsung bucket is discoverable; only samples at or before now within the past seven days are considered. The latest sample carries its writer and timestamp. It is not a live watch stream.
- Sleep searches completed positive sessions over fourteen days, with timestamps and a recent/older distinction. Sessions still in progress are excluded.
- Pagination stops on both null and empty-string page tokens. Independent permission/read failures remain visible. A page cap bounds work rather than looping indefinitely.
- Fitness keeps actual cards first and connection details under an expander. Copy sync report copies version/package/timezone, permissions, sources, values, raw diagnostics and errors locally; it contains no name or API keys. This is the next useful evidence if phone readings still differ.

Official references used: [Android authorization and account identity](https://developer.android.com/identity/authorization), [Drive app-specific storage](https://developers.google.com/workspace/drive/api/guides/appdata), [Drive upload formats](https://developers.google.com/workspace/drive/api/guides/manage-uploads), [Health Connect reads, aggregation and pagination](https://developer.android.com/health-and-fitness/health-connect/read-data). Aggregation remains preferred; raw recovery is a labeled diagnostic fallback.

## Files and functions changed

All Kotlin application paths below are relative to app/src/main/java/com/migraineguard/app/.

| File | Responsibilities and important functions |
| --- | --- |
| core/data/prefs/UserPreferencesRepository.kt | Serializable UserPreferences adds modifiedAtMillis and stepSource. editUser advances a monotonic local revision for user changes. restoreCloudSettings restores all portable settings atomically, refuses older revisions and encrypts portable AI keys with the local vault. setStepSource validates the selected origin. Device consent and Drive opt-in stay phone-local. |
| feature/backup/BackupRepository.kt | snapshot exposes the validated seven-table snapshot for both backup formats. mergeSnapshot reuses the existing database transaction/idempotent merge; encrypted document export/import wrap these operations. |
| feature/settings/CloudBackupFormat.kt (new) | CloudAccountBackup is the versioned owner-bound envelope. validate checks account/version/key sizes and journal/profile bounds. encode/decode enforce payload size. portable strips phone consent and OAuth overrides. fingerprint excludes export timestamps only. |
| feature/settings/DriveRestClient.kt (new) | DriveBackupTransport is the test seam. verifyAccount checks the token identity. download lists exact app-data filenames and reads a bounded body/ETag. upload creates multipart metadata+media or conditionally PATCHes existing media. checked maps HTTP failures to actionable status without logging bodies/tokens. |
| feature/settings/DriveSyncRepository.kt | connect verifies identity and restores before enabling observation. journalChanges registers/removes Room invalidation callbacks for all seven tables. backupNow refreshes token, reads/merges remote state, snapshots and uploads changes under a mutex. pause/dispose cancel observers and drop in-memory tokens. DriveBackupState exposes real progress/results. |
| feature/settings/DriveBackupScreen.kt (new) | driveRequest binds the selected account/scopes. silentDriveToken renews a prior grant without opening background consent. DriveBackupScreen handles native consent resolution, automatic returning-user restore, error/local-only paths and lifecycle cancellation. DriveBackupCard exposes status, explicit save, reconnect and pause. |
| di/AppContainer.kt | Owns one DriveSyncRepository per account, supplies the saved steps origin to Health Connect, attempts a background save and disposes scoped work on account switch. |
| MainActivity.kt | onStop requests the account-scoped best-effort backup; onResume still refreshes phone health. |
| navigation/MigraineGuardNavHost.kt | Drive restore gate precedes profile/setup. Completed returning profiles check phone health permissions without redoing keys/DOB. Settings supplies cloud actions. Bottom navigation uses rounded surfaces and fade transitions. |
| feature/setup/SetupWizardScreen.kt | Waits for real saved preferences before initializing remembered form state. Reusable HealthConnectStep handles returning-user permission review. |
| feature/settings/SettingsScreen.kt | Places real backup status near the top; collapses technical Google override fields; masks all provider keys. Sign-out flushes pending backup and reports failure while preserving local data. |
| feature/fitness/StepReadingPolicy.kt (new) | StepInterval, conservativeSteps and chooseStepReading implement bounded single-writer overlap recovery and Samsung/combined selection. |
| feature/fitness/HealthDataRepository.kt | Full-day raw fallback, independent aggregates and writer diagnostics; sample-level heart filtering, completed sleep queries, null/blank page termination and existing per-type error isolation. |
| core/domain/Models.kt | StepSyncSnapshot now records raw diagnostics, estimate status and the actual combined aggregate separately from the selected total. |
| feature/track/TrackScreen.kt | Readings/source selection before technical panels; persisted origin chips, explicit estimate/source/permission messages and local copyable sync report. |
| core/design/Theme.kt, Type.kt | Updated light/dark lavender/indigo, mint and warm accent scheme, typography and rounded shapes; existing theme/dynamic-color controls honored. |
| core/ui/SectionCard.kt | Rounded surfaces, subtle outlines and animated content size shared across features. |
| core/ui/DailyOverview.kt (new) | Gradient summary with three animated progress rings and real values/goals/action callbacks; missing readings display unavailable. |
| feature/home/HomeScreen.kt | Cleaner greeting/date/streak, daily overview and quick migraine/water actions; retains feature navigation and genuine values. |
| app/build.gradle.kts | Version/code bump, Google AuthorizationClient dependency and MockWebServer test dependency. |
| .github/workflows/android.yml | Exact Android runtime test count gate, narrow-screen light/dark screenshot collection, fresh release notes and unchanged retained-signature enforcement. |

## Test code and verification

- StepReadingPolicyTest: Samsung/combined preference, raw aggregate recovery, overlap avoidance, midnight clipping, unknown versus zero and rejection of mixed writers.
- CloudBackupTest: portable profile/key serialization, account mismatch, timestamp-stable fingerprint, appData-only lookup, conditional PATCH, verified token email, multipart first creation and denied-upload failure.
- DriveRestoreTest (Android): simulated remote transport across separate account-scoped stores; DOB/height/weight/all three keys/setup/journal restore, local key ciphertext, idempotent merge and refusal of older remote preferences. These tests do not use live Google credentials.
- HealthReadingTest (Android): adds zero-aggregate Samsung raw recovery and daily heart buckets with future-sample exclusion; also checks empty page token completion, paging, previous-day data, individual permissions, empty/error states and cancellation.
- NavigationFlowTest (Android): actual first-run unit conversion and all six tabs, then Home/Fitness screenshots in explicit light/dark themes on a narrow emulator.

Local JVM tests, lint, Standard/test APK compilation and hosted Android execution must pass before publishing. Retained FB:67 signatures and package/version metadata are checked on downloaded build artifacts. The release evidence below will be appended after the hosted run finishes. Live native Google consent/Drive transfer, Samsung watch transport and cloud AI calls still require the user's phone; simulated remote tests do not prove those external services worked.

## Phone acceptance checks

Update the same package without uninstalling. Sign in to the account containing your existing details. Allow private Drive backup, save any missing fields/keys once, then use Settings → Back up now and wait for the completed status before switching accounts. A different account must show only its own profile. Return to the original account; saved birthday/measurements/keys should appear without setup forms. A fresh installation can restore only a backup previously completed for that same Google account/application.

Health permissions belong to this phone. After restore, approve/verify the installed app's Health Connect access. In Fitness try Samsung Health first, compare its aggregate/raw status, and use Copy sync report if steps/BPM/sleep remain unavailable despite actual records visible in Health Connect. Please supply the report rather than another permission-only screenshot. No real measurements can be verified from this development VM.
