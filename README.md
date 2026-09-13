# MigraineGuard

Kotlin + Jetpack Compose + Material 3 app for tracking migraines, hydration, fitness/sleep (via **Health Connect** / Samsung Health), nutrition, weather pressure, body metrics, medications, and **AI Coach / Health Analyst** insights.

**Package:** `com.migraineguard.app`  
**Version:** 1.4.0 (versionCode 5)  
**Min SDK:** 26 · **Target / Compile SDK:** 35  
**Auth:** Google Sign-In (Credential Manager) is still **demo/optional** until a Web Client ID is baked later. Demo mode works without one.

---

## What’s new in v1.4

| Area | Upgrade |
|------|---------|
| Health Connect sleep | Hardened `latestSleep()` / `latestFitness()` — catch-all, empty sessions, soft “Sleep permission needed” label; sleep failures never block fitness or crash UI |
| Fitness status UI | Last error shown only when non-blank; sleep permission/unavailable messaging is softer |
| AI Coach | Motivational habit coach (water / steps / sleep / workouts / EOD follow-ups) |
| Health Analyst | Doctor-style *educational* reviews of migraines, triggers, weather/pressure, food, fitness, sleep — always disclaims “not a doctor” |
| AI backend | Gemini → Groq → OpenRouter → **OfflineHealthBrain** (deterministic reports from real stats; useful with zero keys) |
| Settings | Runtime Gemini / Groq / OpenRouter keys (override BuildConfig) + Test connection + link to free Gemini key |
| Navigation | Coach tab + Home cards (“Ask Coach”, “Health Analyst review”) + Insights hub entry |
| History | AI sessions stored in Room (title, persona, summary, timestamp) |

---

## What’s new in v1.3

| Area | Upgrade |
|------|---------|
| Health Connect | Provider-aware permission contract + fallbacks; Connect never feels dead |
| Migraine head | Real 3D mannequin images + glowing pain markers |
| Auth | Google Sign-In remains demo/optional |

---

## AI Coach & keys (important)

- **Not medical advice.** Coach and Analyst are educational / motivational. Seek care for red flags (sudden worst headache, stroke signs, fever with stiff neck, vision loss, head injury).
- **Offline always works.** `OfflineHealthBrain` builds structured markdown reports from your on-device migraines, water, workouts, Health Connect snapshots, weather, nutrition scans, meds, energy check-ins, and profile (BMI, age from DOB).
- **Optional cloud AI:** Settings → AI providers. Paste a free **Gemini** key from [aistudio.google.com/apikey](https://aistudio.google.com/apikey). Groq / OpenRouter optional. Runtime DataStore keys **override** `local.properties` / BuildConfig.
- **Google Sign-In ≠ Gemini access.** Signing in with Google does **not** grant Gemini API access — you still need an AI Studio API key (or rely on OfflineHealthBrain).
- Fallback order: **Gemini → Groq → OpenRouter → OfflineHealthBrain**.

---

## Build / run

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
./gradlew :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## Google Sign-In (optional until Client ID is baked)

1. Google Cloud Console → APIs & Services → Credentials.
2. Create OAuth **Web** client → copy client ID (`….apps.googleusercontent.com`).
3. Create OAuth **Android** client for package `com.migraineguard.app` (+ SHA-1).
4. Paste Web client ID in **Settings → Google**, or set `GOOGLE_WEB_CLIENT_ID` in `local.properties`.
5. Demo mode still works without a client ID.

Box debug keystore SHA-1:  
`FD:56:20:0E:83:57:C4:97:47:E4:32:10:D8:60:3F:DE:0C:64:70:14`

---

## Health Connect troubleshooting

1. Install **Health Connect** (Play Store / system app).
2. Open MigraineGuard → **Fitness** → **Connect Samsung / Health Connect**.
3. Grant permissions. Sleep is optional — if sleep isn’t granted, Fitness still works and Sleep shows “Sleep permission needed”.
4. Samsung: enable Health Connect sharing in Samsung Health.

---

## Optional AI keys (`local.properties` or Settings)

`GEMINI_API_KEY`, `GROQ_API_KEY`, `OPENROUTER_API_KEY` — without keys, Coach / Food / Insights use OfflineHealthBrain or structured stubs.

---

## Medical note

Personal / educational tool. **Not a medical device.** Not a substitute for professional care.

Remote: https://github.com/jdp323/migraineguard
