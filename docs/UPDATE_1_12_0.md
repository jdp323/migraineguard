# v1.12.0 — movement breaks, private body progress and data export

Application 1.12.0 / code 21. Extends [v1.11 navigation and design](UPDATE_1_11_0.md). This guide describes the implemented controls and verified public release.

## Movement reminders

Open Settings → Reminders. Enable the master notification switch and Stand, stretch or walk. Choose an interval from 15 minutes to one day; the default is 60 minutes. Shared active hours, including overnight ranges, apply. Android notification permission and the app's notification channel must also be allowed.

On supported Health Connect installations, Allow optional background step checks requests Steps and READ_HEALTH_DATA_IN_BACKGROUND separately from ordinary setup. The current app account must also opt into this phone's health data in Fitness. The reminder reads an aggregate over the chosen interval from all Health Connect sources. At least 20 received steps suppress that occurrence. Fewer steps produces a qualified low-activity prompt. No permission, missing data, an unsupported feature or a read failure produces a general timed break reminder, without claiming you were sitting. No continuous posture sensor, live wearable connection, location monitoring or foreground tracking service is added.

WorkManager runs approximately and can be delayed by battery saving, Doze, force-stop or OEM restrictions. A 60-minute setting is not proof of 60 minutes continuously seated. Delayed watch uploads can suppress or trigger a reminder later than expected. Tapping the alert opens Fitness. Turning off movement or master notifications cancels the corresponding work; sign-out/account switch invalidates the owner and cancels old alerts.

## Body photos and composition records

Open Settings → Account → Body progress / photos / weight, or the existing Body shortcut. Add a progress photo / check-in, choose a date and Front, Side, Back or Other, then optionally record weight, waist, scale-reported body-fat percentage and observations. A photo is optional; measurement/note-only check-ins work. Save private check-in copies the selected image into this account's app-private database. The system photo picker grants access only to the selected image; no broad gallery/storage permission is requested.

Images are resized to at most 1280 pixels on the long side and JPEG re-encoded with a 360,000-byte limit per photo. Original EXIF/location metadata and source URI are not retained. Android 9+ ImageDecoder handles image orientation; the Android 8 fallback reads a sampled bitmap and applies EXIF orientation before discarding metadata. Original gallery images are unaffected. This is normalized progress media, not an archival full-resolution photo service.

Compare over time lets you select two check-ins, view photos side by side and see changes in recorded measurements and elapsed days. Use the same pose, lighting, distance and clothing for a useful comparison. Numerical analysis uses the measurements you supplied; the app does not infer body fat, health, attractiveness or diagnosis from appearance. Body progress photos are never sent to AI, and no new AI prompt/provider call is introduced.

The default is local-only body progress. The separate Include body photos in my Google Drive backup switch explicitly allows these photos, notes and measurements in the selected account's existing private Drive app-data snapshot. Drive must also be connected. Photos are protected by the app sandbox locally; the database is not claimed to have separate application-level encryption. Google's transport/storage protection applies to Drive; Drive snapshots are not separately password-encrypted or end-to-end encrypted. No developer health-data server, analytics store or photo service is used.

Delete this check-in clears its local photo, measurements and notes after confirmation. A minimal ID/date/pose/timestamp deletion marker remains so an older snapshot cannot resurrect it. Photo-enabled connected Drive propagates deletion on a successful save. Disabling photo backup excludes body progress on the next successful snapshot but does not instantly erase old cloud data, revoke authorization or erase exported copies. Follow [privacy deletion controls](../website/public/privacy.html) for each location. There is no permanent server-side photo history managed by the developer.

## Export and recovery

Open Settings → Data. The Android document picker chooses the destination: local storage, removable storage or a document provider such as your own Drive. The app does not upload exports to a developer service.

| Option | Contents | Protection / use |
| --- | --- | --- |
| Export readable data & body photos | ZIP with README, structured journal-and-settings.json, eight CSV tables and normalized body JPEG files | Not encrypted; private analysis/portability, not app import |
| Export encrypted backup | Profile, goals, preferences, all journal tables and body progress photos in .mgbak | Password-encrypted recovery; password at least eight characters; 20 MB plaintext limit |
| Import backup and keep existing records | Merges supported .mgbak journals/body progress; optional profile/goals/preferences restore | Requires the backup password; repeat restore is idempotent |
| Google Drive backup | Existing account settings, provider keys and journals; body progress only when separately enabled | Private app-data snapshot; 20 MB encoded limit; not a password-encrypted export |

Both manual formats exclude configured API keys and Google tokens, phone health permissions, the raw Health Connect database, meal-photo files and exercise-equipment photo files. JSON retains structured fields; body photo IDs map to body-photos/ID.jpg. CSVs cover migraine, food, water, workouts, weight, medications, AI history and body progress. Spreadsheet formula prefixes are escaped in text cells. User-entered journal text/AI responses remain private content and can contain whatever a person typed; review before sharing. ZIP generation streams files; a large collection can still use memory for the database snapshot.

The encrypted format also excludes API keys and device/Drive/photo-sharing consent. Restoring preferences preserves keys already on this phone. Body photos are included in manual exports regardless of the Drive-photo switch, because the export button explicitly requests them. Keep the original photos and your export password. If an encrypted/Drive snapshot exceeds 20 MB, export the readable ZIP locally, retain it, and reduce the collection only after checking recovery copies. Oversized backups fail visibly; they do not silently omit photos or claim success. Cancelled/failed document writes may leave a partial file at the chosen destination; remove it and retry.

## Health Connect across brands

New profiles default to All sources. Existing saved Samsung preferences are preserved; change Fitness → Health → Steps today → All sources to switch. All sources uses Health Connect aggregation/priority; Samsung Health remains an optional alternative, never an extra number added to that total. Heart, sleep, distance and exercise reads do not require a Samsung writer. When multiple total-calorie writers exist, one identified source is selected to avoid adding overlapping totals.

1. Sync the wearable to its companion app on the Android phone.
2. In Samsung Health, Garmin Connect, Fitbit / Google Health or another compatible app, enable writing the supported types into Health Connect.
3. In Health Connect itself, confirm actual dated records exist for Steps, Heart rate, Sleep or the types you need.
4. In MigraineGuard Fitness, connect this phone's data to the current account and grant read access. Standard and Preview require separate grants.
5. Choose All sources and refresh. Check the source/timestamp diagnostics if records are delayed or missing.

Sharing varies by device, source app/version, Android version and data type. This is an integration with Android Health Connect, not a direct Garmin/Fitbit/Google/Samsung cloud API or watch app. The app cannot read types that the source has not shared. Background step access is optional for movement checks and is not required for normal foreground health reads.

Official references checked October 24, 2026: [Android reading/background permissions and aggregation](https://developer.android.com/health-and-fitness/health-connect/read-data), [Garmin Health Connect sharing](https://support.garmin.com/en-GB/?faq=JToBEy0jfe6pIygark2Ui5), [Google Health connected devices and Health Connect](https://support.google.com/googlehealth/answer/14236613?hl=en). These explain the platform path; real device acceptance remains necessary.

## Code and function map

All Kotlin paths below are relative to app/src/main/java/com/migraineguard/app/.

| File / functions | Responsibility |
| --- | --- |
| feature/reminders/MovementCheck.kt: supported, allowed, recentSteps; MovementPolicy | Feature/grant gates, all-source interval aggregate, missing-data distinction, 20-step suppression and qualified copy |
| feature/reminders/ReminderPolicy.kt: plan, route | Includes optional movement interval and Fitness destination |
| feature/reminders/ReminderWorker.kt: doWork, schedule | Owner/master/hours checks; optional background read, activity suppression and approximate periodic alert |
| feature/settings/ReminderSettings.kt | Master/type switches, movement interval, explicit background permission, status and limitations |
| core/data/prefs/UserPreferencesRepository.kt | Account-scoped movement controls and photo-Drive choice, restore handling; new-profile combined source default |
| core/data/db/BodyProgressDao.kt: BodyProgressEntity.validate, observe, all, get, put | Stable IDs, validated normalized image/measurement records and deletion markers |
| core/data/db/AppDatabase.kt; PreservingMigrations.kt: FROM_6 | Room schema 7; adds body_progress without dropping prior tables; preserves versions 1–6 |
| feature/body/BodyProgressRepository.kt: add, normalizeImage, remove | Selected-photo normalization, local writes and deletion markers |
| feature/body/BodyProgressPanel.kt: BodyProgressPanel, ProgressChoice, ProgressPhoto | Dated check-in form, timeline, comparisons, measurement deltas, explicit Drive toggle and deletion confirmation |
| feature/body/BodyMetricsScreen.kt; di/AppContainer.kt; navigation/MigraineGuardNavHost.kt | Integrates the account-scoped progress repository into the existing Body route |
| feature/backup/ExportPrivacy.kt | Removes configured credentials/device consent from manual settings and escapes spreadsheet formulas |
| feature/backup/DataExport.kt: exportReadable | Streams ZIP, structured JSON, eight CSV tables and normalized JPEG files |
| feature/backup/DataExportCard.kt; BackupCard.kt | Android document picker, unencrypted-export explanation, encrypted recovery and visible outcomes |
| feature/backup/BackupRepository.kt: snapshot, validate, mergeSnapshot, export, restore | Journal format 2 accepts version 1; optional body inclusion; stable-ID/deletion-aware photo merges; manual preference recovery without replacing keys |
| feature/settings/CloudBackupFormat.kt; DriveSyncRepository.kt | Cloud format 3 accepts 2; same private app-data filename/scope; explicit photo inclusion and body-table observation |
| feature/settings/SettingsScreen.kt; DriveBackupScreen.kt | Data settings group, body entry point, multi-brand setup and accurate backup size/content language |
| feature/fitness/HealthDataRepository.kt; StepSyncMessage.kt; feature/setup/HealthConnectSetup.kt; feature/track/TrackScreen.kt | Generic source copy, no Samsung requirement, combined default, identified calorie source and optional Samsung diagnostics |
| app/src/main/AndroidManifest.xml | Declares optional READ_HEALTH_DATA_IN_BACKGROUND and removes the unused extended-history declaration; no broad gallery permission |
| feature/fitness/PermissionsRationaleActivity.kt; app/src/main/res/values/strings.xml | Current system-visible rationale for optional reads, local/Drive storage and explicit AI choices; removes obsolete write/Drive-placeholder text |
| app/src/test and app/src/androidTest | Movement/export validation, non-Samsung reads, migrations 1–6, photo normalization/recovery/deletion, credential exclusion and new-screen runtime captures |

## Compatibility and limits

Room 6 → 7 preserves existing records. Manual recovery format 2 reads old format 1; Drive format 3 reads old format 2. Older apps cannot read the new formats; update every device before editing/restoring the new snapshots. Existing Standard/Preview package separation and retained signing remain. No new developer backend, provider endpoint or Google OAuth scope is added. New body data is intentionally absent from previous releases' documented backup contracts.

Raw Health Connect histories are still owned by their source/platform and are not duplicated into manual exports. Photo deletion markers are implemented for the new stable-ID body table; older journal tables retain their existing merge/deletion limitations. Backups are snapshots rather than conflict-free live multi-device sync. Do not edit the same account simultaneously on multiple devices.

## Published verification and screenshots

[Release v1.12.0](https://github.com/jdp323/migraineguard/releases/tag/v1.12.0-debug) was built from source `aa32d3281997769c3917406ed1dbc387fe304561`. [Run 40121259096](https://github.com/jdp323/migraineguard/actions/runs/40121259096) passed all gates: **81 JVM tests, 33 Android runtime tests**, lint with zero errors, Standard and Preview assembly and retained signing checks. The full Android suite took 109.381 seconds. Both gesture tests passed again with system animation scales at 1.0 in 19.349 seconds.

The runtime suite includes normalized JPEG save/restore and idempotence, encrypted photo recovery, stable-ID deletion propagation, no configured credentials in readable exports, CSV formula escaping, Drive photo opt-in/off, movement interval UI, migration preservation from schemas 1–6, all-source non-Samsung steps/heart/sleep and the existing navigation/account tests. Drive transport uses an in-memory fixture; this is not proof of a live account transfer or wearable sync. Actual background permission prompts and OEM reminder timing remain phone checks. No frame-rate certification or body-image AI inference is claimed.

Both public release APKs were downloaded without authentication, matched the built artifacts and report version 1.12.0/code 21:

| Asset | SHA-256 |
| --- | --- |
| Standard: MigraineGuard-v1.12.0-debug.apk | `27c62a099f366c542d20beda12072c8aa70f1780e1237607e3837ad6c8277d36` |
| Preview: MigraineGuard-v1.12.0-preview.apk | `3e41832322a89bf869722a87a3bafb5a3530c885d614ea951fafbe31a368d61e` |

Both retain SHA-1 `FB:67:0D:1D:96:C2:8B:9E:27:0D:36:F8:D6:57:53:E3:6B:30:8D:E8`. Standard is com.migraineguard.app; Preview is com.migraineguard.app.preview. Update the compatible package in place without uninstalling records.

Six genuine API-35 captures below were visually inspected. Profiles, notes and any images used by tests are synthetic. The body overview capture shows the private timeline entry point; runtime tests also exercise the comparison below it. These captures are screenshots of the app, not design mockups.

<table><tr><td><img src="images/v1.12.0/body-progress-overview.png" width="280" alt="body progress overview"></td><td><img src="images/v1.12.0/settings-movement.png" width="280" alt="settings movement"></td></tr></table>

<table><tr><td><img src="images/v1.12.0/settings-data-export.png" width="280" alt="settings data export"></td><td><img src="images/v1.12.0/fitness-light.png" width="280" alt="fitness light"></td></tr></table>

<table><tr><td><img src="images/v1.12.0/setup-health.png" width="280" alt="setup health"></td><td><img src="images/v1.12.0/settings-updates.png" width="280" alt="settings updates"></td></tr></table>
