# v1.16.0 — dated measurements and compact storage

## Using body measurements

Open Settings → Account → Body measurements / photos / weight. Tap Log measurements, choose a date and fill any areas in Core, Upper body, Lower body or Custom. Units follow your profile (cm/in); an open draft retains its original unit even if profile settings change. Unused areas are omitted. Hiding/reopening the editor preserves the draft; Discard requires confirmation. No adult training preference is required.

There are 20 built-in circumference areas: neck, shoulders, chest, abdomen/tummy, waist, hips, left/right relaxed upper arms, flexed biceps, forearms, wrists, thighs, calves and ankles. Add up to eight custom areas per dated log. Weight and scale-reported body fat remain in their existing sections. These are tape measurements you enter, not image-derived estimates.

History supports multiple logs per date, editing dates/values and deletion. Trends select one area and use actual calendar spacing. The displayed change compares its first and latest recorded values, without assigning a health judgment. Old weight and photo/check-in measurements remain in their original histories.

## How storage changes

Local journals remain in account-separated Room/SQLite databases for atomic writes and reliable queries. New circumference rows store a compact JSON map of only filled areas, canonical centimetres, a date, notes and a revision. No whole-file JSON rewrite is needed for each log.

Room 7 → 8 copies each body-progress row into binary JPEG storage, preserving the exact JPEG bytes, IDs, dates, notes, measurements, revisions and deletion markers. This removes Base64's roughly one-third encoding overhead (binary payload is about 25% smaller than its Base64 representation). Actual total database savings depend on page allocation and other records. SQLite reuses freed pages; Settings → Data → Optimize local storage can reclaim unused pages with checkpoint/VACUUM. It does not remove current records. The displayed size includes this account's journal database and write-ahead log, not the APK, caches, external gallery originals, DataStore preferences or exported copies.

New Drive snapshots and password-encrypted recovery exports use compact JSON with omitted defaults and gzip when smaller. The password-encrypted outer MGBACK01/AES-GCM/PBKDF2 format is retained; compression runs before encryption. Readers auto-detect legacy uncompressed JSON after decryption or Drive download. Compressed and expanded data are each bounded at 20 MiB to prevent oversized restore/expansion. Compression is not encryption: Drive keeps its existing Google transport/storage protection, with portable provider keys in the account snapshot, not separate password encryption.

Readable ZIP exports already use compression; their JSON is now minified and ZIP compression uses the strongest standard level. body-measurements.csv contains dated canonical-cm values and deletion markers. Recovery/Drive include measurement records even when body-photo backup is off. Photo check-ins retain their independent Drive opt-in.

Manual journal format is 4 (reads 1–4); Drive envelope is 5 (reads 2–5). The historical hidden Drive filename migraineguard-account-v2.json is retained so existing accounts find the same file; its media body can now be gzip and upload MIME identifies it. Older apps cannot read the new compressed snapshot, so update all devices before using new backups. New readers restore old files. No login token or raw Health Connect record is added to exports; readable/manual exports still exclude provider keys.

## Record safety and privacy

Circumference records belong to the signed-in account's database. Save/edited revisions increase monotonically; deleted records retain an ID/date/revision marker with measurements/notes cleared. Merging older snapshots cannot resurrect them. They follow the existing enabled private Drive account backup. They are not newly added to any AI prompt or sent to a developer server. No new Health Connect/Google permission, cloud scope or billing service is needed.

## File and function map

| File (Kotlin paths relative to app/src/main/java/com/migraineguard/app) | Responsibility |
| --- | --- |
| feature/body/BodyMeasurementModels.kt | Area catalog, unit conversion/rounding, custom IDs, labels and chronological trend points |
| core/data/db/BodyMeasurementDao.kt | Dated map/revision/tombstone validation, observable history, get/put |
| feature/body/BodyMeasurementRepository.kt | Account-local saves/edits, future-date guard and monotonic deletions |
| feature/body/BodyMeasurementsPanel.kt | Grouped optional editor, saved drafts/units, custom areas, edit/delete/history and calendar-spaced chart |
| feature/body/BodyMetricsScreen.kt; navigation/MigraineGuardNavHost.kt; di/AppContainer.kt | Body-screen and account graph integration |
| core/data/db/BodyProgressDao.kt | Portable photo DTO remains compatible; storage rows use JPEG BLOBs |
| core/data/db/Converters.kt; AppDatabase.kt; PreservingMigrations.kt | Compact map codec, schema 8 and transactional 7→8 photo/measurement migration |
| core/storage/CompactJson.kt | Bounded gzip-if-smaller pack, legacy/plain detection and expansion-limited restore |
| feature/backup/BackupRepository.kt | Measurement snapshots/merge, compact recovery payload before existing encryption |
| feature/settings/CloudBackupFormat.kt | Format 5 compact envelope, old/new decoding, stable data fingerprint |
| feature/settings/DriveRestClient.kt; DriveSyncRepository.kt | Binary-safe gzip upload, actual stored-byte metadata and measurement-change sync |
| feature/backup/DataExport.kt | Compressed/minified JSON and canonical body-measurements CSV |
| feature/backup/JournalStorage.kt; JournalStorageCard.kt; feature/settings/SettingsScreen.kt | Account journal/photo size and safe explicit space reclamation |
| app/src/test/.../CompactJsonTest.kt; BodyMeasurementTest.kt; CloudBackupTest.kt | Compression, corruption/bounds, old files, units, dates, validation and binary HTTP payloads |
| app/src/androidTest/.../BodyMeasurementsTest.kt | v7 photo/journal migration and compaction; encrypted/legacy/Drive-format measurement merge; metric and imperial UI/draft/history/trend/delete flows |
| app/src/androidTest/.../DriveRestoreTest.kt; AppReliabilityTest.kt; BodyProgressTest.kt | Normal Drive measurement restore and earlier schema/photo/export regressions |
| .github/workflows/android.yml | 48 main Android tests, 17 larger-text repeats, existing preflights/smoke/signer publication gates |

## Verification

Final hosted results, measured synthetic storage comparison, screenshots and public APK hashes are recorded here after the release gates pass. Tests use disposable synthetic records and keys; real Drive authorization, wearable sync and OS/provider behavior still need the phone checks listed in SIMULATION_REPORT.md.
