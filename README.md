# MigraineGuard

A native Android companion for migraine journaling, movement, sleep, hydration, meals, body measurements, medication check-ins and optional educational AI coaching.

**Latest verified build: v1.16.0 (26)** · Android 8+ · Public signed test APKs

## Download and share

**[Download MigraineGuard](https://github.com/jdp323/migraineguard/releases/latest)** — share this link with friends. Open **Assets** and choose the Standard .apk. No GitHub repository invitation is needed.

- [Standard v1.16.0 APK](https://github.com/jdp323/migraineguard/releases/download/v1.16.0-debug/MigraineGuard-v1.16.0-debug.apk): normal app, com.migraineguard.app. Update in place when the signing certificate matches; keep local records.
- [Preview v1.16.0 APK](https://github.com/jdp323/migraineguard/releases/download/v1.16.0-debug/MigraineGuard-v1.16.0-preview.apk): separate installation beside Standard, with its own storage and permissions.
- [User guide](docs/USER_GUIDE.md): installation, checked setup, daily features, Google Drive restore and data controls.
- [Troubleshooting](docs/TROUBLESHOOTING.md): possible errors, what they mean and how to resolve them.
- [How it works](docs/HOW_IT_WORKS.md): device path, account isolation, AI/backup boundaries and code map.
- [Public website](https://gen-lang-client-0426132502.web.app/) · [Privacy](https://gen-lang-client-0426132502.web.app/privacy.html) · [Terms](https://gen-lang-client-0426132502.web.app/terms.html)

Do not uninstall an old installation containing records to resolve a signature conflict. Use Preview or verify a recovery export first. These are development builds, distributed outside the Play Store; app updates are downloaded from Releases.

## What's new in v1.16.0

Settings → Account → Body measurements / photos / weight now includes dated tape-measurement logs: 20 standard areas plus up to eight custom areas, cm/in, editable history and area-specific trends. Fill only the areas you measure. Old weight and photo/check-in records remain intact.

Local journals retain reliable account-separated SQLite storage. Measurements use sparse compact JSON; existing body photos migrate losslessly from Base64 text to binary JPEGs. Drive and encrypted recovery snapshots now use compact JSON with gzip when smaller. Settings → Data shows journal/photo size and offers Optimize local storage. Update all devices before using the new backup formats; the new version still reads old backups.

[Usage, storage formats, file/function map and verification](docs/UPDATE_1_16_0.md) · [Full simulation scope](docs/SIMULATION_REPORT.md) · [AI and permissions](docs/AI_AND_PERMISSIONS.md).

The direct Trainer, anatomical zoom, 35 exercise guides and nine offline GIFs from [v1.15](docs/UPDATE_1_15_0.md) remain. Data stays locally or in the user's optional private Drive; no developer health-data/photo backend is used.

## A look inside

Synthetic emulator captures from the running app. [Exact test scope and more screens](docs/UPDATE_1_16_0.md#verification).

<table><tr><td><img src="docs/screenshots/v1.16.0/measurements-history-metric.png" width="250" alt="Dated body measurement history"></td><td><img src="docs/screenshots/v1.16.0/measurements-trend-metric.png" width="250" alt="Waist measurement trend by date"></td><td><img src="docs/screenshots/v1.15.0/simulation-exercise-gif.png" width="250" alt="Offline exercise GIF guide with playback controls"></td></tr></table>

Anatomy derives from MIT-licensed react-native-body-highlighter; the exercise demo frame is Goulart / wger, CC BY-SA 4.0. [Full credits and licenses](THIRD_PARTY_NOTICES.md).

## Start in a few steps

1. Install the Standard APK, or Preview alongside an existing installation.
2. Choose Google sign-in or Continue locally. Optional Drive app-data authorization checks/restores the selected account before new profile setup.
3. Connect this phone's health data only if it belongs to this app account. Grant the record types you want and check actual received readings.
4. Optionally add your own Gemini key and Save & Test. Skipping verification preserves saved keys; no keys means the offline fallback is available. Remove keys in Settings to stop using those providers.
5. Save your profile, review checks/skips and finish. For Google backup, use Back up now and wait for a completed save before moving devices.

Google audience access is **External / In production**; each Google account authorizes its own private app-data storage. Google login does not include a Gemini key or grant Health Connect permission. Custom Google branding is not claimed to be verified. See [configuration evidence](docs/PUBLIC_GOOGLE_ACCESS.md).

## Devices and health readings

Wearable data travels through **a compatible companion app on the phone → Android Health Connect → MigraineGuard**. Samsung Health, Garmin Connect, Fitbit / Google Health and other source apps must export supported records to Health Connect. There is no direct watch app or live sensor stream.

Fitness defaults new profiles to All sources, with an optional Samsung alternative, source/timestamp diagnostics and a copyable sync report. Full-day aggregates and conservative labeled raw recovery handle supported Samsung daily buckets. Heart is a dated latest measurement, not live BPM; sleep requires completed sessions. Missing readings remain unavailable. See the [missing-data checklist](docs/TROUBLESHOOTING.md#steps-heart-rate-or-sleep-are-missing-or-different).

## Accounts, backup and privacy

A device-encrypted remembered identity restores the selected profile after restart, without storing Google API tokens. Explicit sign-out clears that session. Each Google account keeps its own local profile, settings, keys and journal. Continue locally preserves its own records. Optional Drive backup restores profile, DOB, measurements, goals, settings, personal provider keys, seven original journal tables and dated circumference logs. Body progress enters Drive only after its separate opt-in; meal/equipment photos and raw Health Connect records are excluded.

Drive snapshots use Google's transport/storage protection; portable keys inside the compressed JSON are **not separately password-encrypted or end-to-end encrypted**. Keys are encrypted with Android Keystore locally. Manual password-encrypted exports are a separate recovery format and exclude provider keys. Readable ZIP exports are not encrypted. Body progress and circumference logs have deletion markers; original journal tables retain their older merge/deletion limits. Conflict-free simultaneous-device sync is not implemented.

Cloud AI uses your selected provider/key, then offline fallback. Optional legacy Auto uses Gemini → Groq → OpenRouter → offline. Sending the health summary or a meal photo requires the corresponding explicit choice. Opening Insights does not automatically upload health context. Provider quotas/charges vary; historical class names containing Free are not a pricing promise.

[Privacy and deletion details](https://gen-lang-client-0426132502.web.app/privacy.html) · [Public source audit and historical limitations](docs/PUBLICATION_AUDIT.md)

## Verification and remaining phone checks

[Release run 41163227312, attempt 2](https://github.com/jdp323/migraineguard/actions/runs/41163227312/attempts/2) passed **100 JVM and 48 Android runtime tests**, **two focused larger-text preflights**, and **17 animation-enabled repeats at font scale 1.25**. Compilation, lint (zero errors; 122 warnings), retained signing and a seeded 300-event smoke run passed. Both anonymous public APK downloads match the tested bytes. Migration checks preserve exact saved JPEG bytes and older journals; new/legacy recovery and measurement Drive-transport fixtures pass. [Exact hashes, storage benchmark and inspected captures](docs/UPDATE_1_16_0.md#verification) · [Coverage and limits](docs/SIMULATION_REPORT.md).

Tests use synthetic data and simulated Drive transport. Actual watch synchronization, native Google account transfer, live AI and OEM delivery remain phone acceptance checks. [Phone checklist](docs/PHONE_TESTING.md) · [Feature status](docs/FEATURE_STATUS.md).

## Build and technical documentation

Use the Gradle wrapper, JDK 17 (CI) or compatible JDK 21, and Android SDK 36. Copy local.properties.example to ignored local.properties and set your SDK location. On Windows use gradlew.bat; on Linux/macOS use ./gradlew.

~~~bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
./gradlew :app:assembleDebug -PMG_PREVIEW=true
~~~

Standard output is app/build/outputs/apk/debug/app-debug.apk. Copy it before building Preview because Preview uses the same output path. A local debug signer differs from the published signer, so Google OAuth registration/update compatibility must be configured for that local package/certificate. No Firebase client configuration file is needed by the current Android build.

- [Complete project guide](docs/PROJECT_GUIDE.md): architecture, repositories, database, network boundaries and release operations.
- [File/function reference](docs/FILE_REFERENCE.md): detailed baseline reference; later supplements document changed/new functions.
- [v1.6.4](docs/UPDATE_1_6_4.md), [v1.6.6](docs/UPDATE_1_6_6.md), [v1.7.0](docs/UPDATE_1_7_0.md), [v1.8.0](docs/UPDATE_1_8_0.md), [v1.9.0–1.9.1](docs/UPDATE_1_9_0.md): implementation and verification supplements.
- [Development takeover](docs/TAKEOVER.md) and [initial audit](docs/PROJECT_AUDIT.md): historical context, local recovery and signing continuity.
- [Website files/deployment](website/README.md).

Report reproducible problems through [GitHub issues](https://github.com/jdp323/migraineguard/issues), using the redacted template in Troubleshooting. Issues are public: never attach keys, tokens, medical records or backup files.

MigraineGuard is a wellness and educational tracking tool, not a medical device or substitute for professional care. AI personas are not clinicians. Independent project; not affiliated with Google, Samsung or Fitbit.

**Multiple-device backup compatibility:** update all devices before editing/restoring this backup. Older app versions can ignore and later overwrite newer training/provider/reminder fields. Avoid simultaneous edits; Drive snapshots are not conflict-free live sync.
