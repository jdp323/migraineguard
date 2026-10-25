> Current v1.13.1: [glass design, rotating muscle map, session/load rules, weekly planner, exact AI prompt and changed functions](UPDATE_1_13_0.md). Previous [v1.12 movement/body/export](UPDATE_1_12_0.md).

> Historical v1.10.0: [water quick actions, tailored exercise fields, remembered sessions, implementation and tests](UPDATE_1_10_0.md). Historical version supplements below retain their stated baselines.

> v1.9.0: [focused Home, structured training, reminder/update controls, AI providers and Drive size](UPDATE_1_9_0.md). [Exact prompts and permissions](AI_AND_PERMISSIONS.md).

> Historical v1.8.0: [interface, navigation, verified setup and changed-file/function reference](UPDATE_1_8_0.md).

> Historical v1.7.0: [Drive account restore, Samsung query fixes, UI and complete changed-file/function supplement](UPDATE_1_7_0.md). This supersedes the older cloud-backup placeholder and current-version descriptions below; historical source references remain pinned to their stated baselines.

> v1.6.6 adds separate Google-account databases/preferences/API keys, safe sign-out, preserved older local records and per-account phone-health opt-in. Health reads now show individual permission/empty/error states and dated heart/sleep records. See [implementation and tests](UPDATE_1_6_6.md). Samsung/watch transport still needs phone verification.

# Implemented app flows and test scope

This update adds functional local journal backups, device-bound AI key storage, opt-in food photo analysis, explicit health-summary sharing, preserving database migrations, one-store hydration history, corrected profile unit conversion, real/unavailable weather states, and a separately installable Preview APK.

## Phone installation

Download the **Preview APK** to test alongside an older MigraineGuard APK. It uses package `com.migraineguard.app.preview`, has its own local records, and does not remove the existing app. Connect Health Connect and add your AI key in the Preview installation. If the existing app was already installed with the retained FB:67 signing key, use the standard APK to update it.

Both APKs are signed with the retained test certificate. The original temporary CI signing keys are unavailable; an APK cannot reveal their private key. Do not uninstall an old installation that contains records you need. It cannot receive the new export feature unless a build can be signed with its original key.

## Features

| Flow | Implemented behavior | Verification |
| --- | --- | --- |
| Migraine journal | Persistent episodes, symptoms, pain markers and triggers | Older database records retained; backup merge tests |
| Samsung / Google health | Phone Health Connect reads; Samsung comparison, source/refresh details; steps/distance/exercise aggregates | Unit tests and compilation; Samsung/Watch synchronization requires phone testing |
| Nutrition | Description estimates; optional Gemini photo request after explicit selection; metadata stripped and input bounded | Parser regression tests and compilation; live Gemini requires user's key |
| Water | Atomic Room counter/history; midnight day switching; legacy counter migration | Compilation and existing regression checks; runtime database tests |
| Profile / weight | Stored metrics, unit conversion and future-date validation | Conversion tests; backup restore tests |
| Medication log | Local medication labels and taken-day state | Persistence and backup restore; reminders are general check-ins, not a dose scheduler |
| AI coach / analyst | User prompt plus optional health summary; cloud fallback and local reports | Provider/cancellation tests; local summaries avoid automatic health-data uploads |
| Weather | Approximate-location Open-Meteo readings; explicit unavailable/stale states | Compilation; network/location behavior requires phone testing |
| Backups | Password-encrypted export/import, all seven journal tables, profile/goals optional on restore; merges without deleting current records | Encryption/tampering/duplicate-ID tests and Android round-trip tests |
| Account cloud backup | Private Drive app data restores profile/DOB/measurements/goals/settings/API keys and seven journal tables at login; changes, backgrounding and sign-out attempt saves | Simulated transport restore/identity/failure tests; native consent and real Google Drive transfer need phone acceptance |
| Encrypted cloud files | Android file picker can separately save a password-encrypted journal export to an available Drive provider | Manual export/import; provider keys are excluded from this separate format |
| Key storage | User API keys encrypted with Android Keystore; legacy plaintext values migrated | Android on-disk encryption and round-trip test |
| APK publishing | Tests, lint, signature verification and Android emulator tests gate Releases | GitHub Actions results and certificate reports |
| Google sign-in | Explicit account picker and actual installed-certificate diagnostics | Web/Standard/Preview clients registered; phone login confirmed; Google Audience is now External / In production, with non-sensitive app-data/email scopes. Additional-account Drive transfer still needs phone acceptance |
| Watch app | Watch data reaches the phone via Samsung Health / Fitbit and Health Connect | No separate Wear OS application or direct live watch sensor stream |

Current password-encrypted manual exports include body-progress photos and exclude API keys, meal/equipment photo files and imported Health Connect records; optional account Drive snapshots do include portable API keys as described above. They contain the local journal data you choose to export and require the password to restore. Meal/equipment photos remain in their original app/provider storage. Body-progress photos use private normalized JPEG storage and explicit export/Drive choices in v1.12.0.

The all-in-one phone app is a working development build, not a validated medical device. The Android emulator cannot verify a real Samsung/Google device data pipeline or a user's cloud credentials.

## v1.6.4 follow-up

The first-run wizard reuses the corrected shared sign-in/profile screens. Medication daily state now follows a date Flow without a database write. Health snapshots preserve unavailable steps/sleep through dashboard, Insights and offline AI. See [the file/function supplement](UPDATE_1_6_4.md) for exact changes and current access checks. Google OAuth is now registered in an accessible project after explicit approval. The original project remains inaccessible, and the user has now confirmed phone login works. Native Drive consent/transfer still requires phone testing.
