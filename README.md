# MigraineGuard

Kotlin + Jetpack Compose + Material 3 app for tracking migraines, hydration, fitness/sleep (via **Health Connect** / Samsung Health), nutrition, weather pressure, body metrics, medications, and **AI Coach / Health Analyst** insights.

**Package:** `com.migraineguard.app`  
**Version:** 1.5.0 (versionCode 6)  
**Min SDK:** 26 · **Target / Compile SDK:** 35  
**Auth:** Google Sign-In via Credential Manager with **baked Web Client ID** (Firebase project `migraineguard-66229`). Demo mode remains for offline testing.

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

## What’s new in v1.4

| Area | Upgrade |
|------|---------|
| Health Connect sleep | Hardened sleep/fitness; soft “Sleep permission needed” |
| AI Coach / Analyst | Motivational coach + educational analyst; OfflineHealthBrain |
| Settings | Runtime Gemini / Groq / OpenRouter keys + Test connection |

---

## Google Sign-In (baked for friends)

- **Firebase project:** `migraineguard-66229`
- **Web client ID (client_type 3, baked):** `555526836064-aria393bjpcgr17j6fatl0nbeqtvqri6.apps.googleusercontent.com`
- **Package:** `com.migraineguard.app`
- **Android SHA-1 for release / box APKs:**  
  `FD:56:20:0E:83:57:C4:97:47:E4:32:10:D8:60:3F:DE:0C:64:70:14`
- Demo continue still works without Google.
- If Sign-In fails, check SHA-1 registration on the Android OAuth client (Settings → Google → Show SHA-1).

**Google Sign-In ≠ Gemini tokens.** Signing in does **not** grant Gemini API access.

---

## Per-user Gemini (never developer AI tokens)

1. After Google Sign-In or demo, if you have no Gemini key → **Connect Gemini** screen.
2. Open [Google AI Studio](https://aistudio.google.com/apikey), create a **free** key, paste, Save & Test.
3. Cloud AI (Coach / Analyst / food cloud path) uses **only your** stored key(s).
4. Without a key → **OfflineHealthBrain** only + CTA to connect Gemini.
5. Settings → AI providers: manage / remove your keys. Never ship developer keys. Do **not** use the Firebase `api_key` from `google-services.json` as a Gemini key.

Fallback order: **Gemini (user) → Groq (user) → OpenRouter (user) → OfflineHealthBrain**.

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

1. Install **Health Connect** (Play Store / system app).
2. Open MigraineGuard → **Fitness** → **Connect Samsung / Health Connect**.
3. Grant permissions. Sleep is optional.
4. Samsung: enable Health Connect sharing in Samsung Health.

---

## Medical note

Personal / educational tool. **Not a medical device.** Not a substitute for professional care.

Remote: https://github.com/jdp323/migraineguard
