# How MigraineGuard works

Current behavior: v1.16.0 / code 26. This is an overview of the implemented system. For code-by-code detail use the [project guide](PROJECT_GUIDE.md), [file/function reference](FILE_REFERENCE.md) and version supplements linked below. For instructions use the [user guide](USER_GUIDE.md).

## From your device to the screen

The native Kotlin/Jetpack Compose phone app reads allowed records from Android Health Connect. A Galaxy Watch first syncs to Samsung Health on the phone; Samsung Health must then write supported records to Health Connect. Other companion apps, including compatible Garmin and Fitbit / Google Health setups, must do the equivalent. There is no direct live watch sensor service or Wear OS app in this repository.

Health Connect availability, Android permissions, the selected app account's phone-health opt-in and actual records are separate conditions. A success in one does not imply success in all. Setup checks each one and shows received-record checks only when it has evidence.

Daily cumulative steps use the full local calendar day, including daily Samsung buckets that end at the next midnight. All-source Health Connect aggregation is the default for new profiles; existing saved Samsung preferences are preserved. Aggregation preserves provider/source handling. If an aggregate is zero but usable raw records exist, conservative single-writer recovery avoids overlapping intervals and is labeled an estimate. The app never adds Samsung and combined totals together.

Heart reads include recent record buckets but exclude future samples; the display is the latest dated sample, not live pulse. Completed sleep sessions are searched across fourteen days and older results are labeled. Missing results stay unavailable through Home, Fitness, Insights and offline coaching. See [the current query supplement](UPDATE_1_7_0.md) for exact helpers and edge cases.

## What is saved where

| Location | Contents | Boundaries |
| --- | --- | --- |
| Account-local Room database | Migraine episodes, food analysis records, water history, workouts, weight entries, medications and AI sessions | Nine journal categories including body progress photos and dated circumference logs, plus local-only water-action receipts. Google accounts and the preserved local profile use separate storage namespaces. This is not a claim that every journal column is encrypted. |
| Account-local Preferences DataStore | Profile/DOB/units, goals, appearance, setup flags, connection choices and encrypted provider-key values | API key encryption uses device-bound Android Keystore; decrypted values are used only in app memory for configuration/requests. |
| Health Connect / source apps | Device health records | These remain controlled by Android and source apps; the app does not back up the full Health Connect database to Drive or GitHub. |
| Optional Google Drive appDataFolder | Current account snapshot: settings/profile, portable provider keys, seven original journal tables and dated circumference logs; body progress only after its separate opt-in | Hidden app-specific storage authorized by that Google account. Google transport/storage protection; no separate password/end-to-end encryption for the portable keys. |
| Optional manual encrypted file | Journals, body photos and profile/goals/preferences | Password-derived AES-GCM. Excludes provider keys, meal/equipment photos and raw Health Connect records. |
| Local/source image storage | Selected meal/equipment-photo references or camera files | Meal/equipment references are not portable. Body progress uses normalized JPEG bytes in the private database and optional exports/Drive. |
| GitHub / public website | Source, docs, synthetic test screenshots and signed test APKs | No user journal or user Drive backup is intentionally uploaded here. See the limited [public exposure audit](PUBLICATION_AUDIT.md). |

Room Flow and DataStore updates drive Compose screens. Manual dependency construction is in AppContainer. MainActivity triggers foreground refresh; HealthRefreshCoordinator shares/debounces health reads. Active health screens also refresh periodically while resumed. A keyed native HorizontalPager now owns the six main tabs, with saveable drafts/scroll state and limited neighboring pages. Detail destinations stay on Navigation Compose’s back stack, including Profile and Drive. The dock reads pager position during drawing, while only the active Home/Fitness page runs its periodic refresh loop. Persisted entries still belong in repositories rather than only in UI state. [Navigation/design details](UPDATE_1_11_0.md).

## Sign-in, account switching and restore

Credential Manager initially selects a Google identity. A device-encrypted local identity is remembered across process restarts; no Google password or API token is persisted. Explicit sign-out clears it. Completed accounts open Home without blocking on Drive/Health setup gates. Previously enabled Drive reconnects using already-granted authorization in the background, with reconnect errors visible in Account settings. Drive authorization separately grants narrow app-data access. Account identity determines the local storage namespace; the token identity and snapshot owner are checked before importing. Choosing a different account does not reuse the preceding person's profile or keys. Continue locally retains older unowned/local records instead of silently assigning them to a Google account.

On an authorized login, the app checks Drive before presenting replacement profile/key forms. A completed restored profile can skip those forms. Health permission and account opt-in are checked on each phone because backup is not Android's health permission grant.

Changes trigger automatic saves after a short debounce; backgrounding and sign-out attempt a final save. The UI distinguishes a connection response from a completed save. Network failure leaves local data available. Tokens are retained in memory and refreshed/reconnected through Google, not committed to source.

Restore validates identity, format, size and records before importing. Journal import is additive/idempotent; colliding IDs are normalized and identical content is deduplicated. The original seven journal categories retain additive merge limits, so old snapshots can restore their deleted entries. Body photo/check-ins and dated circumference logs have deletion tombstones. Conflict-free simultaneous-device synchronization is not implemented. See [data removal](USER_GUIDE.md#6-disconnect-and-remove-data).

## AI and privacy boundaries

Text requests use the user's selected provider and then offline fallback. Optional legacy Auto retains Gemini → Groq → OpenRouter → offline. Settings → AI supports eleven services, separate encrypted keys/model IDs, non-authoritative prefix suggestions and explicit selected-endpoint tests. Empty/error/timeout responses can permit fallback; cancellation does not start another transmission. The app ships empty developer-key defaults. Historical provider class names ending in Free do not guarantee current pricing.

A typed cloud prompt is sent to the selected provider. Including the local health summary requires the explicit sharing choice; it defaults off. AI sessions are saved locally under the account and may be included in its optional backup. Automatic Insights stays on-device.

Food/equipment images are sent only with the corresponding explicit user choice to a supported selected provider (Gemini/OpenAI/Claude); legacy Auto uses Gemini. Images are re-encoded/bounded before sending. Other selected services explain that images are not enabled; the request does not silently change providers. Unusable nutrition analysis leaves estimates unknown. Equipment classification only suggests an editable name. [Exact prompts and data fields](AI_AND_PERMISSIONS.md).

Coach/Analyst are educational personas, not doctors. The illustrated pain map, correlations, exercise catalog and generated responses are not clinically validated diagnosis/treatment tools. Weather uses optional approximate coordinates sent to Open-Meteo; stale samples are labeled.

## Setup verification and UI

Shared Material light/dark surfaces, code-native illustrations and six labeled tabs provide a consistent interface. Home is a compact daily summary; Migraine owns its journal/context. Fitness separates Health and Training, including named exercises, sets/weights/reps/history, cardio speed/incline/distance, stretch holds/rounds and a weekly active-day goal. Short fade/slide transitions use Compose APIs and the platform animation scale; no network graphics or remote fonts are needed.

Setup check states distinguish waiting, checking, verified, attention and skipped. Permissions alone cannot check Steps, Heart or Sleep. Manual/demo values cannot prove a device connection. A real zero step response can be valid evidence. Key editing clears its earlier Gemini test check. Saved-profile failures do not advance. The final review preserves optional skips rather than displaying universal success. Setup details are in [v1.8.0](UPDATE_1_8_0.md); current changed files/functions are in [v1.9.0–1.9.1](UPDATE_1_9_0.md).

## Reminder and update checks

Settings → Reminders saves per-account toggles, intervals and active hours. Separate WorkManager jobs use a durable account marker to deliver generic notifications after process restart. Sign-out clears the marker, jobs and notifications. Food/water taps open their logs; no photo is taken automatically. Android timing is approximate. Movement reminders optionally check all-source recent steps using separately granted background access; missing data produces a timed break prompt, not a sitting diagnosis.

Settings → App checks public GitHub latest-release metadata, validates the repository URL and compares numeric versions. Optional background checks cache metadata for 24 hours and notify once per newer release during active hours. A tap opens GitHub downloads; nothing installs silently. Only ordinary request metadata reaches GitHub, not health records or provider keys. Account Settings measures the current Drive snapshot bytes, not total account usage/quota.

Water notifications add three fixed glass actions. Immutable PendingIntents target a private receiver, which enqueues account-bound work. A receipt and water increment share one Room transaction, so each notification applies one choice. Invalid owner, expiry or disabled preferences reject the action. Receipts and the remembered session are excluded from journal exports/Drive. [Current changed files and limits](UPDATE_1_10_0.md).

## Training studio and shared glass design

The glass system draws a static ambient gradient field behind transparent scaffolds, tinted translucent surfaces, edge sheen and shadows. Text is not blurred; reduced transparency gives cards solid fills. The anatomical selector caches licensed vector Paths and Android Regions for male/female front/back drawings. Rendering and taps use one shared viewport transform; unsupported body details do not become rectangular targets. A swipe switches sides with a slide/crossfade. Labeled chips and accessibility actions remain. [Exact new functions and attribution](UPDATE_1_14_0.md).

Trainer preferences/plans are account-local settings. Default profiles build selected-region workouts immediately; optional preferences no longer block generation with an adult/ready checkbox. Zoom/pan and hit testing share one coordinate transform; the repaired male rear skull is an original overlay above a clipped legacy outline. All 35 catalog movements have written equipment/setup steps, and nine matched variants have offline CC BY-SA GIF playback. See the [current file/function and media map](UPDATE_1_15_0.md). Deterministic catalog/coverage/time/effort rules build a session and bounded weekly template. Current comparable actual logs and timestamped test loads drive load guidance; demographic mass is never converted into a fabricated machine load. Structured workout details now include optional actual RIR and trainer exercise ID. Explicit AI sharing can request an allowed-ID week arrangement, which is validated and assigned local prescriptions. No cloud request runs merely by opening Trainer. [Exact functions, schema, prompt, storage and limits](UPDATE_1_13_0.md).

## Body progress and export

The body-progress table uses stable UUIDs and permanent deletion markers. Selected images are resized and re-encoded locally; no body photo enters an AI prompt. Side-by-side comparisons and numerical deltas use user-entered measurements. A separate opt-in controls body contents in Drive. Settings → Data streams a readable ZIP or creates a password-encrypted recovery snapshot. Configured keys are excluded from manual exports. [Exact functions, migration, limits and formats](UPDATE_1_12_0.md).

## Source map and detailed code documentation

| Path | Role | Detailed reference |
| --- | --- | --- |
| app/src/main/java/com/migraineguard/app/core | Domain models, Room/DataStore, AI contracts/providers, time/security helpers and reusable UI | [File reference](FILE_REFERENCE.md), [project guide](PROJECT_GUIDE.md) |
| feature/auth and core/security/AccountStorage.kt | Google identity, account namespaces, safe session switching | [v1.6.6](UPDATE_1_6_6.md) |
| feature/settings/*Drive* and CloudBackupFormat.kt | Google authorization, REST transport, snapshot validation and save/restore orchestration | [v1.7.0](UPDATE_1_7_0.md) |
| feature/fitness and feature/track | Health queries, permission/ownership gate, source policy, shared refresh and Fitness UI | [v1.6.6](UPDATE_1_6_6.md), [v1.7.0](UPDATE_1_7_0.md) |
| feature/setup and core/ui | Verified onboarding, progress review and shared visual components | [v1.8.0](UPDATE_1_8_0.md) |
| Other feature folders and navigation | Journals/forms, home, coaching, meals, insights, water, body, medications, settings and routes | [File reference](FILE_REFERENCE.md), later version supplements |
| app/src/test and app/src/androidTest | Pure JVM regressions and Android runtime/navigation/storage tests | [Release evidence](UPDATE_1_9_0.md#published-v191-verification-and-screenshots) |
| .github/workflows/android.yml | Build, test, lint, signing/runtime gates and APK publishing | [Project guide](PROJECT_GUIDE.md#13-build-test-and-release-operations) |
| website | Static Firebase homepage, policies and help; no user-data backend | [Website files/deploy](../website/README.md) |

The file reference preserves a v1.6.3 source baseline with named functions, constructor fields and database details. Supplements v1.6.4, v1.6.6, v1.7.0 v1.8.0 and v1.9.0–1.9.1 and v1.10.0 explain subsequent changed/new functions. Read those together; old pinned source-line references do not pretend to describe the latest line numbers.

## Build and release

Use the checked-in Gradle wrapper, JDK 17 (CI) or compatible JDK 21, and Android SDK 36. The app targets SDK 35 and supports API 26+. Put the local SDK path in ignored local.properties using local.properties.example. A local debug build uses your machine's certificate; it will not have the publisher's registered Google signer or update an APK signed by a different key.

Run the unit/lint/Standard assembly tasks before making a release; build Preview with -PMG_PREVIEW=true and copy Standard before Preview replaces the same output path. Android tests need a suitable emulator/device. The hosted API-35 emulator avoids requiring a large local emulator on this resource-limited VM.

The pipeline restores its signing key from a GitHub Actions secret for eligible push builds, checks the retained SHA-1, runs checks and builds both APKs. Pull requests do not receive that secret or publish. A release requires successful build and Android runtime jobs and the retained signer. Existing version tags are not overwritten; app binary changes need a new version/code.

[Run 40435413413](https://github.com/jdp323/migraineguard/actions/runs/40435413413) passed 91 JVM tests, 44 Android runtime tests, 2 focused preflights and 13 animation-enabled larger-text repeats. A seeded 300-event smoke run completed without an app fatal exception; two hardware flip injections were blocked by the emulator. Both public APKs match tested hashes and retain the signer. Lint passed with 0 errors and 121 warnings. [Exact evidence and captures](UPDATE_1_15_0.md#verification) · [Functional simulation matrix](SIMULATION_REPORT.md). Actual wearable sync, native Drive transfer, live AI and phone frame rate remain acceptance checks.

## Current limits

No direct watch app/live pulse, exact-dose alarm scheduler, barcode nutrition database, historical Health Connect series/correlation engine, portable meal/equipment photo backup or conflict-free multi-device sync is implemented. Public Google audience access is enabled, but custom branding is not represented as verified. These limits are documented so missing behavior can be diagnosed rather than mistaken for an already completed feature.

## v1.16 measurement and storage pipeline

BodyMeasurementRepository writes validated dated cm maps through BodyMeasurementDao into schema 8. Maps contain only filled areas. UI converts cm/in without changing stored meaning; revision and permanent deletion markers prevent stale snapshots from resurrecting measurements. PreservingMigrations.FROM_7 transactionally copies existing Base64 photo rows into exact JPEG BLOBs, then adds the measurement table; all earlier migrations chain to it. The portable photo DTO remains compatible with old exports.

CompactJson packs minified JSON with omitted defaults using gzip only when smaller. Manual .mgbak applies existing password encryption afterward. CloudBackupFormat version 5 and manual journal version 4 accept supported older uncompressed JSON, with both packed and expanded content bounded at 20 MiB. Drive keeps the original hidden filename to find the existing snapshot and reports actual stored bytes from metadata. Readable ZIP includes body-measurements.csv in canonical cm.

JournalStorageRepository runs an explicit checkpoint/VACUUM/checkpoint under a maintenance mutex outside transactions. Sizes cover account journal/WAL and raw body JPEG payload, excluding preferences/APK/cache/independent files. Measurements enter enabled private Drive backup independently of the body-photo switch and are not added to AI prompts. [Complete file/function map and compatibility](UPDATE_1_16_0.md).
