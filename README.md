# MigraineGuard

Kotlin + Jetpack Compose + Material 3 app for tracking migraines, hydration, fitness/sleep (via **Health Connect** / Samsung Health), nutrition, weather pressure, body metrics, medications, and AI-assisted insights.

**Package:** `com.migraineguard.app`  
**Version:** 1.2.0 (versionCode 3)  
**Min SDK:** 26 · **Target / Compile SDK:** 35  
**Auth:** Google Sign-In (Credential Manager) + demo mode · Profile setup after first sign-in

---

## What’s new in v1.2

| Area | Upgrade |
|------|---------|
| Health Connect | Connect button checks availability, Snackbars, multi-intent fallbacks, Samsung Health shortcut, never silent no-op |
| Google Sign-In | No auto-demo on Google button; runtime Web Client ID in Settings (DataStore wins over BuildConfig); clear errors |
| Profile | Blocking ProfileSetup (name, DOB, sex, height/weight, activity) → BMI on Home |
| Home | Time-based greeting, daily summary, streak, energy 1–5 check-in, BMI card |
| Goals | Steps, water, sleep hours, optional weight goal |
| Body / Meds | Weight log + sparkline; medication/supplement log with taken-today |
| Migraine | Cooler 3D-ish head (gradients, shadow, specular, depth rings, glowing cool→hot orbs, parallax); trigger tags |
| Reminders | WorkManager periodic stubs + POST_NOTIFICATIONS |

---

## Build / run

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
./gradlew :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## Google Sign-In setup (required for real Google button)

1. Google Cloud Console → APIs & Services → Credentials.
2. Create OAuth **Web** client → copy client ID (`….apps.googleusercontent.com`).
3. Create OAuth **Android** client:
   - Package: `com.migraineguard.app`
   - SHA-1 for **this box’s debug APK**:  
     `FD:56:20:0E:83:57:C4:97:47:E4:32:10:D8:60:3F:DE:0C:64:70:14`
   - Android Studio local builds often use a **different** `~/.android/debug.keystore` SHA-1 — add that too. Settings → **Show debug SHA-1 instructions**.
4. Either:
   - Paste Web client ID in **Settings → Google** (saved to DataStore, no rebuild), or
   - Set `GOOGLE_WEB_CLIENT_ID=…` in `local.properties` and rebuild.
5. Demo mode still works without a client ID. The Google button **does not** silently fall through to demo.

---

## Health Connect troubleshooting

1. Install **Health Connect** (Play Store / system app).
2. Open MigraineGuard → **Fitness** → **Connect Samsung / Health Connect**.
3. You should see Snackbar “Opening Health Connect permissions…” then the system permission sheet.
4. After grant, MigraineGuard appears under Health Connect → **Connected apps**.  
   **Sideloaded debug APKs appear after the first successful permission request.**
5. If HC is missing: Snackbar + Play Store / HC settings intents (multiple fallbacks).
6. Samsung: open Samsung Health → enable Health Connect sharing; use **Open Samsung Health** when the package is present.
7. Manifest includes health permissions (incl. `READ_HEALTH_DATA_HISTORY`), exported rationale activity with both actions, and `START_VIEW_PERMISSION_USAGE` activity-alias.

---

## Optional AI keys (`local.properties`)

`GEMINI_API_KEY`, `GROQ_API_KEY`, `OPENROUTER_API_KEY` — without keys, Food/Insights use structured stubs.

---

## Medical note

Personal / educational tool. **Not a medical device.**

Remote: https://github.com/jdp323/migraineguard
