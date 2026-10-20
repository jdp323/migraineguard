# How MigraineGuard works

Current behavior: v1.8.0 / code 15. This is an overview of the implemented system. For code-by-code detail use the [project guide](PROJECT_GUIDE.md), [file/function reference](FILE_REFERENCE.md) and version supplements linked below. For instructions use the [user guide](USER_GUIDE.md).

## From your device to the screen

The native Kotlin/Jetpack Compose phone app reads allowed records from Android Health Connect. A Galaxy Watch first syncs to Samsung Health on the phone; Samsung Health must then write supported records to Health Connect. Other companion apps, including compatible Fitbit setups, must do the equivalent. There is no direct live watch sensor service or Wear OS app in this repository.

Health Connect availability, Android permissions, the selected app account's phone-health opt-in and actual records are separate conditions. A success in one does not imply success in all. Setup checks each one and shows received-record checks only when it has evidence.

Daily cumulative steps use the full local calendar day, including daily Samsung buckets that end at the next midnight. Samsung is the default source; the combined Health Connect aggregate is an alternative. Aggregation preserves provider/source handling. If an aggregate is zero but usable raw records exist, conservative single-writer recovery avoids overlapping intervals and is labeled an estimate. The app never adds Samsung and combined totals together.

Heart reads include recent record buckets but exclude future samples; the display is the latest dated sample, not live pulse. Completed sleep sessions are searched across fourteen days and older results are labeled. Missing results stay unavailable through Home, Fitness, Insights and offline coaching. See [the current query supplement](UPDATE_1_7_0.md) for exact helpers and edge cases.

## What is saved where

| Location | Contents | Boundaries |
| --- | --- | --- |
| Account-local Room database | Migraine episodes, food analysis records, water history, workouts, weight entries, medications and AI sessions | Seven tables. Google accounts and the preserved local profile use separate storage namespaces. This is not a claim that every journal column is encrypted. |
| Account-local Preferences DataStore | Profile/DOB/units, goals, appearance, setup flags, connection choices and encrypted provider-key values | API key encryption uses device-bound Android Keystore; decrypted values are used only in app memory for configuration/requests. |
| Health Connect / source apps | Device health records | These remain controlled by Android and source apps; the app does not back up the full Health Connect database to Drive or GitHub. |
| Optional Google Drive appDataFolder | Current account snapshot: settings/profile, portable provider keys and seven journal tables | Hidden app-specific storage authorized by that Google account. Google transport/storage protection; no separate password/end-to-end encryption for the portable keys. |
| Optional manual encrypted file | Seven journal tables and optional profile/goals | Password-derived AES-GCM. Excludes provider keys, photo files and raw imported health records. |
| Local/source image storage | Selected meal-photo references or camera files | Snapshots do not include photo bytes. A restored URI is not a cross-device photo backup. |
| GitHub / public website | Source, docs, synthetic test screenshots and signed test APKs | No user journal or user Drive backup is intentionally uploaded here. See the limited [public exposure audit](PUBLICATION_AUDIT.md). |

Room Flow and DataStore updates drive Compose screens. Manual dependency construction is in AppContainer. MainActivity triggers foreground refresh; HealthRefreshCoordinator shares/debounces health reads. Active health screens also refresh periodically while resumed. Navigation saves/restores tab state, and persisted entries belong in repositories rather than only in a remembered form field.

## Sign-in, account switching and restore

Credential Manager selects a Google identity. Drive authorization separately grants narrow app-data access. Account identity determines the local storage namespace; the token identity and snapshot owner are checked before importing. Choosing a different account does not reuse the preceding person's profile or keys. Continue locally retains older unowned/local records instead of silently assigning them to a Google account.

On an authorized login, the app checks Drive before presenting replacement profile/key forms. A completed restored profile can skip those forms. Health permission and account opt-in are checked on each phone because backup is not Android's health permission grant.

Changes trigger automatic saves after a short debounce; backgrounding and sign-out attempt a final save. The UI distinguishes a connection response from a completed save. Network failure leaves local data available. Tokens are retained in memory and refreshed/reconnected through Google, not committed to source.

Restore validates identity, format, size and records before importing. Journal import is additive/idempotent; colliding IDs are normalized and identical content is deduplicated. There are no deletion tombstones or conflict-free simultaneous-device synchronization. An old cloud snapshot can restore a locally deleted record. See [data removal](USER_GUIDE.md#6-disconnect-and-remove-data).

## AI and privacy boundaries

Text requests use the user's configured Gemini → Groq → OpenRouter → offline provider chain. Empty/error/timeout responses can permit fallback; cancellation does not start another transmission. The app ships empty developer-key defaults. Historical provider class names ending in Free do not guarantee current pricing.

A typed cloud prompt is sent to the selected provider. Including the local health summary requires the explicit sharing choice; it defaults off. AI sessions are saved locally under the account and may be included in its optional backup. Automatic Insights stays on-device.

A food image is sent only when the user explicitly selects the Gemini photo option. The app downsamples/re-encodes the image, bounds size and drops original metadata before sending it with the user's Gemini key. A photo request does not silently fall through to another image provider. Unusable analysis leaves nutrition estimates unknown.

Coach/Analyst are educational personas, not doctors. The illustrated pain map, correlations, exercise catalog and generated responses are not clinically validated diagnosis/treatment tools. Weather uses optional approximate coordinates sent to Open-Meteo; stale samples are labeled.

## Setup verification and UI

Shared Material light/dark surfaces, code-native gradient illustrations, animated progress rings, dashboard shortcuts and six labeled tabs provide a consistent interface. Short fade/slide transitions use Compose APIs and the platform animation scale; no network graphics or remote fonts are needed.

Setup check states distinguish waiting, checking, verified, attention and skipped. Permissions alone cannot check Steps, Heart or Sleep. Manual/demo values cannot prove a device connection. A real zero step response can be valid evidence. Key editing clears its earlier Gemini test check. Saved-profile failures do not advance. The final review preserves optional skips rather than displaying universal success. Details and every changed file are in [v1.8.0](UPDATE_1_8_0.md).

## Source map and detailed code documentation

| Path | Role | Detailed reference |
| --- | --- | --- |
| app/src/main/java/com/migraineguard/app/core | Domain models, Room/DataStore, AI contracts/providers, time/security helpers and reusable UI | [File reference](FILE_REFERENCE.md), [project guide](PROJECT_GUIDE.md) |
| feature/auth and core/security/AccountStorage.kt | Google identity, account namespaces, safe session switching | [v1.6.6](UPDATE_1_6_6.md) |
| feature/settings/*Drive* and CloudBackupFormat.kt | Google authorization, REST transport, snapshot validation and save/restore orchestration | [v1.7.0](UPDATE_1_7_0.md) |
| feature/fitness and feature/track | Health queries, permission/ownership gate, source policy, shared refresh and Fitness UI | [v1.6.6](UPDATE_1_6_6.md), [v1.7.0](UPDATE_1_7_0.md) |
| feature/setup and core/ui | Verified onboarding, progress review and shared visual components | [v1.8.0](UPDATE_1_8_0.md) |
| Other feature folders and navigation | Journals/forms, home, coaching, meals, insights, water, body, medications, settings and routes | [File reference](FILE_REFERENCE.md), later version supplements |
| app/src/test and app/src/androidTest | Pure JVM regressions and Android runtime/navigation/storage tests | [Release evidence](UPDATE_1_8_0.md#validation) |
| .github/workflows/android.yml | Build, test, lint, signing/runtime gates and APK publishing | [Project guide](PROJECT_GUIDE.md#13-build-test-and-release-operations) |
| website | Static Firebase homepage, policies and help; no user-data backend | [Website files/deploy](../website/README.md) |

The file reference preserves a v1.6.3 source baseline with named functions, constructor fields and database details. Supplements v1.6.4, v1.6.6, v1.7.0 and v1.8.0 explain subsequent changed/new functions. Read those together; old pinned source-line references do not pretend to describe the latest line numbers.

## Build and release

Use the checked-in Gradle wrapper, JDK 17 (CI) or compatible JDK 21, and Android SDK 36. The app targets SDK 35 and supports API 26+. Put the local SDK path in ignored local.properties using local.properties.example. A local debug build uses your machine's certificate; it will not have the publisher's registered Google signer or update an APK signed by a different key.

Run the unit/lint/Standard assembly tasks before making a release; build Preview with -PMG_PREVIEW=true and copy Standard before Preview replaces the same output path. Android tests need a suitable emulator/device. The hosted API-35 emulator avoids requiring a large local emulator on this resource-limited VM.

The pipeline restores its signing key from a GitHub Actions secret for eligible push builds, checks the retained SHA-1, runs checks and builds both APKs. Pull requests do not receive that secret or publish. A release requires successful build and Android runtime jobs and the retained signer. Existing version tags are not overwritten; app binary changes need a new version/code.

For v1.8.0, [run 39370737632](https://github.com/jdp323/migraineguard/actions/runs/39370737632) passed **54 JVM tests and 20 Android tests**, lint and signature checks. Released APK hashes match the inspected downloaded build artifacts. Tests use synthetic health/account data and simulated Drive transport. Real Samsung/Fitbit transfer, native authorization and live AI must still be tested on a phone; automated success is not proof of those external paths.

## Current limits

No direct watch app/live pulse, exact-dose alarm scheduler, barcode nutrition database, historical Health Connect series/correlation engine, portable photo backup or conflict-free multi-device sync is implemented. Public Google audience access is enabled, but custom branding is not represented as verified. These limits are documented so missing behavior can be diagnosed rather than mistaken for an already completed feature.
