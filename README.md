# MigraineGuard

## Latest update: v1.7.0 (code 14)

Per-account Google Drive app-data backup/restore now includes profile, birthday, measurements, settings, personal AI keys and journals. Sign-in checks Drive before profile setup; returning profiles check only phone health permissions. Samsung-first steps, conservative raw-record recovery, daily heart-bucket reads and clearer diagnostics address records visible in Health Connect but missing in the app. Home/Fitness and shared light/dark UI are refreshed. See [implementation, every changed file/function and limitations](docs/UPDATE_1_7_0.md).

Android app for migraine journaling, hydration, Health Connect fitness/sleep, meals, weight, medications, weather and AI coaching.

**Version:** 1.7.0 (code 14) · Android 8+ · target SDK 35.

Download builds from [GitHub Releases](https://github.com/jdp323/migraineguard/releases). Use **MigraineGuard Preview** to test alongside an older install without removing its records. Standard APK updates retain the package `com.migraineguard.app`; Preview uses `com.migraineguard.app.preview`. Both use the persistent test signing certificate.

## What changed in v1.6.5

Welcome and sign-in now display the actual installed build version. The Android navigation test checks both version labels so a hardcoded older release cannot be shown again.

## What changed in v1.6.4

First-run setup now uses the same corrected sign-in/profile screens as Settings. Unit changes convert measurements, medication taken state follows midnight/timezone date changes, and unavailable steps/sleep remain unknown in Home, Insights and local AI. New regression tests cover these cases and a hosted Android test walks through local setup and all six main tabs.

See [the v1.6.4 source/access supplement](docs/UPDATE_1_6_4.md).

## Current improvements

- Today’s cumulative steps, distance and calories query the full local calendar day, including Samsung daily buckets. Aggregates remain preferred; single-writer raw recovery is labeled as an estimate. Source comparisons and refresh timestamps explain what was received.
- Encrypted journal export/restore, preservation migrations, Android Keystore protection for AI keys, and consistent hydration history.
- Optional Gemini photo analysis with metadata removed; cloud health-summary sharing is an explicit choice. Unknown nutrition and health readings stay unknown.
- Real optional weather, profile unit conversion, offline coaching and a separate Preview install.
- CI checks compilation, unit tests, lint, APK certificates and Android emulator migration/backup/key-storage/startup tests before publishing.

Google sign-in uses the registered Web audience and both APK package/certificate clients in the owned project. Google Audience now reports **In production, External** (2026-10-19), allowing any Google account to request its own private Drive app-data access. The public [homepage](https://gen-lang-client-0426132502.web.app/), [privacy policy](https://gen-lang-client-0426132502.web.app/privacy.html) and terms are deployed on free Firebase Hosting. No APK rebuild is needed for this cloud-side change. The custom brand is not yet verified/shown; Google says data-access verification is not required for the current non-sensitive scopes. See [configuration and remaining phone checks](docs/PUBLIC_GOOGLE_ACCESS.md). The user confirmed phone login and original-account backup work; previously blocked additional accounts and cross-device restore still need phone testing. **Continue locally** works without Google. Watch/Fitbit data depends on what its companion app exports to Health Connect; real-device sync and cloud AI responses require phone testing.

See [feature status](docs/FEATURE_STATUS.md), [phone testing](docs/PHONE_TESTING.md) and [source audit](docs/PROJECT_AUDIT.md).

## What’s new in v1.6.1

Nutrition estimates remain unknown when AI analysis is unavailable; photos are local references and description analysis requires text. Steps use Health Connect aggregation for the local calendar day, and exercise minutes are no longer inferred from steps. AI requests preserve cancellation and use bounded provider timeouts. Automatic Android backup/transfer is excluded to protect journals and user keys. Notification permission revocation is handled. GitHub CI runs unit tests, lint and debug APK assembly.

See [the source review and development priorities](docs/PROJECT_AUDIT.md) for incomplete features and hardware validation requirements.

## What’s new in v1.6

| Area | Upgrade |
|------|---------|
| Setup wizard | Single ordered **SetupWizard** on first cold start (`setupComplete`): Welcome → Sign-in → Health Connect/Samsung tutorial → Gemini API key tutorial → Profile (DOB calendar) → Done |
| DOB | Material3 `DatePicker` calendar everywhere (wizard + ProfileSetup / Settings edit) — no free-text YYYY-MM-DD |
| Health refresh | `HealthRefreshCoordinator` auto-refreshes fitness/sleep on app foreground and bottom-nav tab switches (≈2s debounce); soft last-refreshed / errors |
| Settings | Still edit profile / HC / Gemini later; first-time users go through the wizard (skips allowed for HC & Gemini) |

---

## What’s new in v1.5 (historical; superseded by the current setup below)

| Area | Upgrade |
|------|---------|
| Google Sign-In | Introduced a built-in Web Client ID; current project/tester/certificate setup is listed below. |
| Firebase | Legacy `app/google-services.json` retained in source; current Credential Manager setup uses the owned project listed below. |
| Per-user Gemini | After Sign-In / demo, **ConnectGeminiScreen** if no user Gemini key. Uses YOUR free AI Studio quota — never developer tokens. |
| AI keys | BuildConfig AI defaults are empty. Cloud Coach/Analyst only with the user’s DataStore keys. OfflineHealthBrain otherwise + CTA. |
| Settings | Manage / remove your own AI keys; Google SHA-1 help for release APKs. |

---

## First-run setup wizard

On cold start, if `setupComplete` is not true, MigraineGuard runs one ordered wizard:

1. **Welcome** — what the app does (+ medical disclaimer)
2. **Sign in** — Google (baked client) or local use. Google accounts check/restore private Drive app data before the remaining steps; completed restored profiles skip key/profile forms.
3. **Health Connect / Samsung** — illustrated steps: install/open HC → grant permissions → enable Samsung Health sharing → confirm status (skip marks HC skipped)
4. **Gemini API key** — Open AI Studio → create/copy key on YOUR Google account → paste → Save & Test (skip → OfflineHealthBrain; cloud AI needs your key)
5. **Profile** — name (prefilled from Google), **DOB calendar**, height/weight/gender — validated
6. **Done** — sets `setupComplete=true` → main app

Existing v1.5 users who already finished onboarding + profile are migrated as `setupComplete` automatically.

---

## Google Sign-In configuration

- **Project:** Default Gemini Project (`gen-lang-client-0426132502`).
- **Public Web audience:** `539944675853-i6otink5q9a4tr94mo7hm85jli1edq61.apps.googleusercontent.com`.
- **Registered packages:** Standard `com.migraineguard.app` and Preview `com.migraineguard.app.preview`.
- **Retained APK SHA-1:** `FB:67:0D:1D:96:C2:8B:9E:27:0D:36:F8:D6:57:53:E3:6B:30:8D:E8`.
- Google consent is External / In production. Each user must grant their own optional Drive app-data authorization. No private Web client secret is used by the Android app.
- Original Firebase project/configuration and the older FD:56 signer are historical; they are not the current APK registration.
- Continue locally opens the preserved local profile. Google accounts have separate records and AI keys. A new Google account must configure its own profile/key and device-health connection.

**Google Sign-In does not grant Gemini API access.**

---

## Per-user Gemini (never developer AI tokens)

1. During setup (or later in Settings / Connect Gemini), open [Google AI Studio](https://aistudio.google.com/apikey), create a **free** key, paste, Save & Test.
2. Cloud AI (Coach / Analyst / food cloud path) uses **only your** stored key(s).
3. Without a key → **OfflineHealthBrain** only + CTA to connect Gemini.
4. Settings → AI providers: manage / remove your keys. Never ship developer keys. Do **not** use the Firebase `api_key` from `google-services.json` as a Gemini key.

Fallback order: **Gemini (user) → Groq (user) → OpenRouter (user) → OfflineHealthBrain**.

AI tips are educational — **not medical advice**.

---

## Build / run

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
./gradlew :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## Health Connect troubleshooting

1. Install **Health Connect** (Play Store / system app) — also covered in the setup wizard.
2. Grant permissions (wizard step or Fitness tab → Connect).
3. Allow the installed Standard/Preview app to READ Steps, Heart rate and Sleep, and Samsung Health to WRITE those types. Open Samsung Health and sync the watch after changing sharing.
4. Connect this phone’s health data once for the selected app account; Android grants alone do not choose its owner.
5. Data auto-refreshes on app resume and when switching bottom tabs.

---

## Medical note

Personal / educational tool. **Not a medical device.** Not a substitute for professional care.

Remote: https://github.com/jdp323/migraineguard

## Development takeover and GitHub APKs

See [TAKEOVER.md](docs/TAKEOVER.md) for the verified GitHub baseline, local snapshot/backup commands, CI APK downloads, signing continuity and remaining device checks.

## Complete documentation

- [Project guide](docs/PROJECT_GUIDE.md): architecture, every workflow, storage/network boundaries, security, build/release operations and actual verification limits.
- [File and function reference](docs/FILE_REFERENCE.md): 114 files, 75 Kotlin files and 385 named function declarations, with source links, constructor fields, SQL and per-function behavior. This is the v1.6.3 baseline; the linked supplements cover later changes.
- [Feature status](docs/FEATURE_STATUS.md) and [phone testing](docs/PHONE_TESTING.md): what works, external setup and real-device checks.

Current inventory is 126 tracked files, 85 Kotlin files and 423 named functions; see [v1.6.6 changes and verification](docs/UPDATE_1_6_6.md). Verified: **36 JVM tests + 13 Android emulator tests**, lint, both builds and signing checks.

Documentation baseline is v1.6.3 source commit 61cc34b3a7c9614fe6ee20944a0ab0de058b0dfa; later documentation commits do not replace the verified APKs.
