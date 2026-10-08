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
| Cloud files | Android file picker can save the encrypted backup to an available Drive provider | Manual user-selected export; no automatic background synchronization |
| Key storage | User API keys encrypted with Android Keystore; legacy plaintext values migrated | Android on-disk encryption and round-trip test |
| APK publishing | Tests, lint, signature verification and Android emulator tests gate Releases | GitHub Actions results and certificate reports |
| Google sign-in | Explicit account picker and actual installed-certificate diagnostics | Needs project access and OAuth registration; current account lacks old project access |
| Watch app | Watch data reaches the phone via Samsung Health / Fitbit and Health Connect | No separate Wear OS application or direct live watch sensor stream |

Backups exclude API keys, photo files and imported Health Connect records. They contain the local journal data you choose to export and require the password to restore. Photos remain in their original app/provider storage.

The all-in-one phone app is a working development build, not a validated medical device. The Android emulator cannot verify a real Samsung/Google device data pipeline or a user's cloud credentials.
