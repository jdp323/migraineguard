# MigraineGuard

Android starter app (Kotlin + Jetpack Compose + Material 3) for tracking migraines, habits, and environmental triggers.

**Package:** `com.migraineguard.app`  
**Min SDK:** 26 · **Target / Compile SDK:** 35  
**Auth:** Google Sign-In only (Credential Manager placeholders + demo toggle)

This repo was scaffolded from scratch (not cloned). Fake repositories power every feature so you can run and navigate immediately.

---

## What you get

| Area | Status |
|------|--------|
| Sign-in gate | Demo toggle; Google Identity / Credential Manager sketched |
| Bottom nav | Home, Migraine, Track, Food, Water, Insights |
| Migraine map | Interactive head silhouette (Canvas) + intensity slider |
| Water | Cup +/- with DataStore persistence |
| Fitness / Sleep / Weather / Nutrition / Insights | Interfaces + fake impls |
| AI | `FallbackAiClient` + GeminiFree → GroqFree → OpenRouterFree stubs |
| Settings | Google Drive sync interface stub |

---

## Folder map

```
migraineguard/
├── app/
│   ├── build.gradle.kts          # Module deps, BuildConfig fields
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/migraineguard/app/
│       │   ├── MigraineGuardApp.kt
│       │   ├── MainActivity.kt
│       │   ├── di/AppContainer.kt          # Manual DI / fakes
│       │   ├── navigation/                 # NavHost + routes
│       │   ├── core/
│       │   │   ├── ai/                     # AiProvider + FallbackAiClient
│       │   │   ├── data/                   # Result wrapper
│       │   │   ├── design/                 # Theme, typography
│       │   │   ├── domain/                 # Shared models
│       │   │   └── ui/                     # Shared Compose widgets
│       │   └── feature/
│       │       ├── auth/
│       │       ├── home/
│       │       ├── migraine/
│       │       ├── fitness/ + sleep/ + track/
│       │       ├── nutrition/
│       │       ├── water/
│       │       ├── weather/
│       │       ├── insights/
│       │       └── settings/
│       └── res/
├── gradle/libs.versions.toml     # Version catalog
├── local.properties.example      # Copy → local.properties (gitignored)
├── settings.gradle.kts
└── README.md
```

**Single-module `app`** with clean feature packages — easy to split into Gradle modules later.

---

## Open in Android Studio

1. Install [Android Studio](https://developer.android.com/studio) (Ladybug / newer recommended).
2. **File → Open** and select this `migraineguard` folder.
3. Let Gradle sync. If the wrapper JAR is missing, Android Studio offers to generate it, or run:
   ```bash
   gradle wrapper --gradle-version 8.11.1
   ```
   (requires a local Gradle install once).
4. Create an emulator (API 26+) or plug in a device.
5. Run the **app** configuration.

On first launch: **Continue as demo user** (or Sign in with Google — currently also demo until you add a client ID).

---

## Push to GitHub

Remote intended: https://github.com/jdp323/migraineguard

```bash
cd /path/to/migraineguard
git init
git add .
git commit -m "Initial MigraineGuard Compose starter"
git branch -M main
git remote add origin https://github.com/jdp323/migraineguard.git
git push -u origin main
```

Use SSH if you prefer: `git@github.com:jdp323/migraineguard.git`.  
Do **not** commit `local.properties` or API keys (already in `.gitignore`).

---

## Google Sign-In setup (no secrets in git)

1. Open [Google Cloud Console](https://console.cloud.google.com/).
2. Create/select a project → **APIs & Services → Credentials**.
3. Create an **OAuth 2.0 Client ID** of type **Web application** (this is the *server client ID* Credential Manager needs).
4. Also create an **Android** client ID with your app’s package `com.migraineguard.app` and SHA-1 (debug keystore: `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android`).
5. Copy `local.properties.example` → `local.properties` and set:
   ```properties
   GOOGLE_WEB_CLIENT_ID=123456789-xxxx.apps.googleusercontent.com
   sdk.dir=/path/to/Android/sdk
   ```
6. Sync Gradle. `BuildConfig.GOOGLE_WEB_CLIENT_ID` is populated from that property.
7. Replace the demo handlers in `feature/auth/SignInScreen.kt` / `MigraineGuardNavHost.kt` with a real `CredentialManager.getCredential(...)` flow (comments in `SignInScreen.kt` show the sketch).

**Never commit** client secrets or service-account JSON.

---

## AI provider fallback plan

Package: `core/ai`

1. **`AiProvider`** — `complete(prompt)` → `Result<String>`
2. **`FallbackAiClient`** — tries providers sorted by `priority` (lowest first); on failure moves to the next.
3. Stub order (most powerful free first):
   - `GeminiFree` (priority 0)
   - `GroqFree` (priority 1)
   - `OpenRouterFree` (priority 2)

Later: read keys from `local.properties` → `BuildConfig` fields already reserved:

- `GEMINI_API_KEY`
- `GROQ_API_KEY`
- `OPENROUTER_API_KEY`

Nutrition and Insights will call `FallbackAiClient` for summaries / food labels. No real HTTP in this starter.

---

## How to add a feature

1. Create `feature/<name>/` under `com.migraineguard.app`.
2. Add domain types to `core/domain` if shared.
3. Define a repository / analyzer **interface** + fake impl.
4. Register the fake in `di/AppContainer`.
5. Add a Compose screen; register a route in `navigation/Routes.kt` and `MigraineGuardNavHost`.
6. Wire bottom nav or a Home shortcut as needed.
7. Keep Android-free models in `core/domain`; keep theme tokens in `core/design`.

---

## Navigation overview

```
Signed out → SignInScreen
Signed in  → Scaffold + bottom bar
               Home (→ Settings)
               Migraine
               Track (Fitness + Sleep)
               Food
               Water
               Insights
```

---

## Known starter limitations

- Google Sign-In is **not** live; demo auth only.
- No Health Connect, camera, or GPS implementations.
- AI providers return placeholder strings.
- Drive sync is a toggle stub.
- Migraine markers are **in-memory** only (not saved).
- Gradle Wrapper JAR may need generation on first open (see above).
- Adaptive launcher icons only (`mipmap-anydpi-v26`); fine for minSdk 26.

---

## License / medical note

Personal / educational starter. Not a medical device. Migraine logging is for awareness, not diagnosis or treatment advice.
