# MigraineGuard

Kotlin + Jetpack Compose + Material 3 app for tracking migraines, hydration, fitness/sleep (via **Health Connect** / Samsung Health), nutrition, weather pressure, and AI-assisted insights.

**Package:** `com.migraineguard.app`  
**Version:** 1.1.0  
**Min SDK:** 26 · **Target / Compile SDK:** 35  
**Auth:** Google Sign-In (Credential Manager) + demo mode

---

## What’s new in v1.1

| Area | Upgrade |
|------|---------|
| Health Connect | Full `android.permission.health.*` set, rationale activity, `<queries>`, Connected apps listing |
| Head map | Rotatable Front / Left / Right / Back / Top views; markers store `viewId` |
| Insights | Mode chips: Overall · Migraine · Food · Water · Fitness · Sleep · Weather |
| Fitness hub | Steps ring + goal, workout log (Room), Exercise of the Day, weekly summary |
| Sign-In | Polished UI; Credential Manager when `GOOGLE_WEB_CLIENT_ID` is set |
| Nav | Track tab renamed **Fitness** |

---

## v1.1 features

| Area | What ships |
|------|------------|
| Theme | Teal/indigo medical-calm Material 3; System / Light / Dark (DataStore); optional dynamic color |
| Onboarding | First-launch tip pager |
| Auth | Google Credential Manager when `GOOGLE_WEB_CLIENT_ID` is set; demo mode always works |
| Migraine | Multi-view rotatable head map, multi points, intensity 1–10, symptom chips, notes, Room history + detail (shows sides) |
| Water | Cups UI, progress ring, daily goal, Room history |
| Fitness | Health Connect connect + settings buttons; steps ring vs goal; workout log; Exercise of the Day; sleep/HR cards; manual steps fallback |
| Food | Camera / gallery → preview → `FallbackAiClient` → Room recent scans |
| Weather | Location → **Open-Meteo** (free, no key); daily cache + offline fake |
| Insights | 7 / 14 / 30 / custom ranges + per-mode charts/stats + AI narrative |
| Drive sync | Settings toggle + status TODO (builds without Drive credentials) |
| Home | Risk hints, water progress, sleep/steps vs goal, weather, Fitness quick action |

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

- **Room** — migraine episodes, nutrition scans, water history, workouts  
- **DataStore** — theme, water/step goals, notification prefs, onboarding, Drive toggle  
- **OkHttp** — Open-Meteo + optional AI HTTP  
- **Health Connect** — `androidx.health.connect:connect-client`

---

## Build / run

1. Open this folder in **Android Studio** (Ladybug+).
2. Copy `local.properties.example` → `local.properties` and set `sdk.dir=…`.
3. Sync Gradle (`./gradlew` wrapper is included).
4. Run on a device/emulator (API 26+).

```bash
export JAVA_HOME=…   # JDK 17
export ANDROID_HOME=…
./gradlew :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## Optional keys (`local.properties` → BuildConfig)

| Key | Purpose |
|-----|---------|
| `GOOGLE_WEB_CLIENT_ID` | Credential Manager Google Sign-In (**Web** client ID) |
| `GEMINI_API_KEY` | Gemini Flash nutrition / insights |
| `GROQ_API_KEY` | Groq fallback |
| `OPENROUTER_API_KEY` | OpenRouter fallback |

Without keys, AI returns **structured stub JSON** so Food and Insights remain usable offline. **Never commit** `local.properties` or secrets.

### Google Sign-In setup

1. Google Cloud Console → create OAuth **Web** client → copy client ID into `GOOGLE_WEB_CLIENT_ID`.
2. Also create an **Android** OAuth client (`com.migraineguard.app` + your debug/release SHA-1).
3. Rebuild. If the ID is missing/placeholder, the Google button soft-falls back to demo mode and the build still succeeds.

---

## Health Connect + Samsung (make MigraineGuard appear)

1. Install **Health Connect** (or use the one bundled on recent Androids).
2. Open MigraineGuard → **Fitness** tab → tap **Connect Samsung / Health Connect** and grant permissions.
3. Tap **Open Health Connect settings** and confirm MigraineGuard is listed under Connected apps.
4. **Samsung Health** → allow data sharing with Health Connect (Settings → Health Connect / Data permissions).
5. Galaxy Watch data path: Watch → Samsung Health → Health Connect → MigraineGuard.

Permissions declared: Steps, Sleep, Heart rate, Exercise, Distance, Total calories burned (read) + Exercise (write).  
Rationale activity: `PermissionsRationaleActivity` (shown from Health Connect privacy screens).

If Health Connect is missing, the app shows calm **demo** fitness/sleep numbers and still accepts manual workouts / steps.

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
- Health Connect: steps, sleep, heart rate, exercise, distance, calories (read); exercise (write)

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
