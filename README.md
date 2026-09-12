# MigraineGuard

Kotlin + Jetpack Compose + Material 3 app for tracking migraines, hydration, fitness/sleep (via **Health Connect** / Samsung Health), nutrition, weather pressure, and AI-assisted insights.

**Package:** `com.migraineguard.app`  
**Version:** 1.0.0  
**Min SDK:** 26 · **Target / Compile SDK:** 35  
**Auth:** Google Sign-In (Credential Manager) + demo mode

---

## v1 features

| Area | What ships |
|------|------------|
| Theme | Teal/indigo medical-calm Material 3; System / Light / Dark (DataStore); optional dynamic color |
| Onboarding | First-launch tip pager |
| Auth | Google Credential Manager when `GOOGLE_WEB_CLIENT_ID` is set; demo mode always works |
| Migraine | Canvas head map, multi points, intensity 1–10, symptom chips, notes, Room history + detail |
| Water | Cups UI, progress ring, daily goal, Room history |
| Track | Health Connect permissions + Samsung Health explainer; fake fallback when HC unavailable |
| Food | Camera / gallery → preview → `FallbackAiClient` (Gemini→Groq→OpenRouter) → Room recent scans |
| Weather | Location → **Open-Meteo** (free, no key); daily cache + offline fake |
| Insights | 7 / 14 / 30 day aggregates + AI narrative |
| Drive sync | Settings toggle + status TODO (builds without Drive credentials) |
| Home | Risk hints, water progress, sleep/steps, weather, quick actions |

---

## Architecture

Single-module `app` with feature packages and a manual `AppContainer` (no Hilt).

```
com.migraineguard.app/
├── di/AppContainer.kt
├── navigation/
├── core/{ai,data/db,data/prefs,design,domain,ui}
└── feature/{auth,home,migraine,track,fitness,sleep,nutrition,water,weather,insights,settings,onboarding}
```

- **Room** — migraine episodes, nutrition scans, water history  
- **DataStore** — theme, goals, notification prefs, onboarding, Drive toggle, today’s cups  
- **OkHttp** — Open-Meteo + optional AI HTTP  

---

## Build / run

1. Open this folder in **Android Studio** (Ladybug+).
2. Copy `local.properties.example` → `local.properties` and set `sdk.dir=…`.
3. Sync Gradle (`./gradlew` wrapper is included).
4. Run on a device/emulator (API 26+). **No emulator required** for development if you use a physical phone.

```bash
./gradlew :app:assembleDebug
```

Release:

```bash
./gradlew :app:assembleRelease
# Sign with your keystore (do not commit keystores)
```

---

## Optional keys (`local.properties` → BuildConfig)

| Key | Purpose |
|-----|---------|
| `GOOGLE_WEB_CLIENT_ID` | Credential Manager Google Sign-In (Web client ID) |
| `GEMINI_API_KEY` | Gemini Flash nutrition / insights |
| `GROQ_API_KEY` | Groq fallback |
| `OPENROUTER_API_KEY` | OpenRouter fallback |

Without keys, AI returns **structured stub JSON** so Food and Insights remain usable offline. **Never commit** `local.properties` or secrets.

Also create an Android OAuth client (`com.migraineguard.app` + SHA-1) in Google Cloud Console for real Sign-In.

---

## Health Connect + Samsung

1. Install **Health Connect** (or use the one bundled on recent Androids).
2. Samsung Health → allow data sharing with Health Connect.
3. In MigraineGuard **Track** or **Settings**, request permissions / open Health Connect.
4. Galaxy Watch data flows: Watch → Samsung Health → Health Connect → MigraineGuard.

If Health Connect is missing, the app shows calm **demo** fitness/sleep numbers.

---

## Weather

Uses [Open-Meteo](https://open-meteo.com/) forecast API (no API key). Needs coarse/fine location for local pressure/humidity; otherwise a demo snapshot is shown and cached per day.

---

## AI fallback order

1. Gemini (if key) else structured stub success  
2. Groq (if key)  
3. OpenRouter (if key)  

`FallbackAiClient` walks the chain for text completion, nutrition JSON, and insight narratives.

---

## Permissions used

- Internet (weather / AI)  
- Location (weather)  
- Camera (food scan)  
- Health Connect read: steps, heart rate, sleep  

---

## Known gaps

- Drive sync toggle queues locally; Drive REST upload is TODO (no credentials needed to compile).  
- Notification toggles are persisted prefs; WorkManager push not wired yet.  
- Google Sign-In needs a real Web client ID + device Google Play services.  
- Health Connect reads need a phone with HC installed and granted permissions.

---

## Medical note

Personal / educational tool. **Not a medical device.** Logging supports awareness only — not diagnosis or treatment.

---

## License

Personal project starter. Remote: https://github.com/jdp323/migraineguard
