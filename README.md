# MigraineGuard

Android app for migraine journaling, hydration, Health Connect fitness/sleep, meals, weight, medications, weather and AI coaching.

**Version:** 1.6.3 (code 10) · Android 8+ · target SDK 35.

Download builds from [GitHub Releases](https://github.com/jdp323/migraineguard/releases). Use **MigraineGuard Preview** to test alongside an older install without removing its records. Standard APK updates retain the package `com.migraineguard.app`; Preview uses `com.migraineguard.app.preview`. Both use the persistent test signing certificate.

## Current improvements

- Today’s cumulative steps, distance and calories query the full local calendar day, including Samsung daily buckets. Health Connect still deduplicates steps. Source comparisons and refresh timestamps explain what was received.
- Encrypted journal export/restore, preservation migrations, Android Keystore protection for AI keys, and consistent hydration history.
- Optional Gemini photo analysis with metadata removed; cloud health-summary sharing is an explicit choice. Unknown nutrition and health readings stay unknown.
- Real optional weather, profile unit conversion, offline coaching and a separate Preview install.
- CI checks compilation, unit tests, lint, APK certificates and Android emulator migration/backup/key-storage/startup tests before publishing.

Google sign-in still requires access to the configured Google Cloud project and registration of the installed package/certificate. **Continue locally** works without Google. Watch/Fitbit data depends on what its companion app exports to Health Connect; real-device sync and cloud AI responses require phone testing.

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

## What’s new in v1.5

| Area | Upgrade |
|------|---------|
| Google Sign-In | Web Client ID baked — friends sign in out of the box (no pasting client ID). Settings can still override. Clear SHA-1 mismatch errors. |
| Firebase | `app/google-services.json` for project `migraineguard-66229` (Credential Manager path; no full Firebase Auth SDK required). |
| Per-user Gemini | After Sign-In / demo, **ConnectGeminiScreen** if no user Gemini key. Uses YOUR free AI Studio quota — never developer tokens. |
| AI keys | BuildConfig AI defaults are empty. Cloud Coach/Analyst only with the user’s DataStore keys. OfflineHealthBrain otherwise + CTA. |
| Settings | Manage / remove your own AI keys; Google SHA-1 help for release APKs. |

---

## First-run setup wizard

On cold start, if `setupComplete` is not true, MigraineGuard runs one ordered wizard:

1. **Welcome** — what the app does (+ medical disclaimer)
2. **Sign in** — Google (baked client) or Demo continue
3. **Health Connect / Samsung** — illustrated steps: install/open HC → grant permissions → enable Samsung Health sharing → confirm status (skip marks HC skipped)
4. **Gemini API key** — Open AI Studio → create/copy key on YOUR Google account → paste → Save & Test (skip → OfflineHealthBrain; cloud AI needs your key)
5. **Profile** — name (prefilled from Google), **DOB calendar**, height/weight/gender — validated
6. **Done** — sets `setupComplete=true` → main app

Existing v1.5 users who already finished onboarding + profile are migrated as `setupComplete` automatically.

---

## Google Sign-In configuration

- **Firebase project:** `migraineguard-66229`
- **Web client ID (client_type 3, baked):** `555526836064-aria393bjpcgr17j6fatl0nbeqtvqri6.apps.googleusercontent.com`
- **Package:** `com.migraineguard.app`
- **Previously documented Android SHA-1 for release / box APKs (not the CI certificate):**
  `FD:56:20:0E:83:57:C4:97:47:E4:32:10:D8:60:3F:DE:0C:64:70:14`
- Demo continue still works without Google.
- If Sign-In fails, check SHA-1 registration on the Android OAuth client (Settings → Google → Show SHA-1).

**Google Sign-In ≠ Gemini tokens.** Signing in does **not** grant Gemini API access.

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
3. Samsung: enable Health Connect sharing in Samsung Health.
4. Data auto-refreshes on app resume and when switching bottom tabs.

---

## Medical note

Personal / educational tool. **Not a medical device.** Not a substitute for professional care.

Remote: https://github.com/jdp323/migraineguard

## Development takeover and GitHub APKs

See [TAKEOVER.md](docs/TAKEOVER.md) for the verified GitHub baseline, local snapshot/backup commands, CI APK downloads, signing continuity and remaining device checks.
