# MigraineGuard

Kotlin + Jetpack Compose + Material 3 app for tracking migraines, hydration, fitness/sleep (via **Health Connect** / Samsung Health), nutrition, weather pressure, body metrics, medications, and AI-assisted insights.

**Package:** `com.migraineguard.app`  
**Version:** 1.3.0 (versionCode 4)  
**Min SDK:** 26 · **Target / Compile SDK:** 35  
**Auth:** Google Sign-In (Credential Manager) is still **demo/optional** until a Web Client ID is baked later. Demo mode works without one.

---

## What’s new in v1.3

| Area | Upgrade |
|------|---------|
| Health Connect | Provider-aware permission contract (`com.google.android.apps.healthdata`) + fallback contract; Connect never feels dead (Toast + Snackbar “Checking Health Connect…”); `getSdkStatus`; Play Store + Samsung Health when provider update required; manage-permissions / settings fallbacks; visible status card (raw SDK int, provider package, last error) |
| Migraine head | Real 3D mannequin images (front / left / mirrored-right / back / top placeholder); Canvas overlay only for glowing cool→hot pain markers + tap; rotate chevrons + swipe |
| Auth | Google Sign-In remains demo/optional — Client ID is **not** baked in this build |

---

## What’s new in v1.2

| Area | Upgrade |
|------|---------|
| Health Connect | Connect button checks availability, Snackbars, multi-intent fallbacks, Samsung Health shortcut |
| Google Sign-In | Runtime Web Client ID in Settings (DataStore wins over BuildConfig) |
| Profile | Blocking ProfileSetup (name, DOB, sex, height/weight, activity) → BMI on Home |
| Home | Time-based greeting, daily summary, streak, energy 1–5 check-in, BMI card |
| Goals / Body / Meds | Steps, water, sleep hours, optional weight goal; weight log; medications |
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

## Google Sign-In (optional until Client ID is baked)

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
5. Demo mode still works without a client ID. This v1.3.0 build does **not** bake a production Client ID.

---

## Health Connect troubleshooting

1. Install **Health Connect** (Play Store / system app).
2. Open MigraineGuard → **Fitness** → **Connect Samsung / Health Connect**.
3. You should immediately see Toast + Snackbar **“Checking Health Connect…”**.
4. If SDK status is `SDK_AVAILABLE`, the provider-aware permission sheet opens (full permission set). If that launch fails, a fallback contract and then HC settings / `ACTION_MANAGE_HEALTH_PERMISSIONS` are tried.
5. If status is `SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED`, Play Store opens for `com.google.android.apps.healthdata` and Samsung Health is opened when present.
6. The **Health Connect status** card shows raw SDK int, provider package, and last error so a tap is never silent.
7. After grant, MigraineGuard appears under Health Connect → **Connected apps**. Sideloaded debug APKs appear after the first successful permission request.
8. Samsung: enable Health Connect sharing in Samsung Health; use **Open Samsung Health** when the package is present.
9. Manifest keeps the exported rationale activity, `VIEW_PERMISSION_USAGE` alias, queries for Health Connect + Samsung Health, and manage-permissions intents.

---

## Optional AI keys (`local.properties`)

`GEMINI_API_KEY`, `GROQ_API_KEY`, `OPENROUTER_API_KEY` — without keys, Food/Insights use structured stubs.

---

## Medical note

Personal / educational tool. **Not a medical device.**

Remote: https://github.com/jdp323/migraineguard
