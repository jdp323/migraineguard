# Source layout (`com.migraineguard.app`)

| Package | Role |
|---------|------|
| `di` | `AppContainer` — manual wiring of fakes / future real repos |
| `navigation` | Routes, bottom destinations, `MigraineGuardNavHost` |
| `core/design` | Material 3 theme + typography |
| `core/ui` | Shared Compose helpers (`PlaceholderScreen`, `SectionCard`) |
| `core/domain` | Pure Kotlin models |
| `core/data` | `Result` and future persistence helpers |
| `core/ai` | `AiProvider`, stub providers, `FallbackAiClient` |
| `feature/auth` | Google Sign-In gate (demo + Credential Manager placeholders) |
| `feature/home` | Dashboard |
| `feature/migraine` | Head silhouette Canvas + pain markers |
| `feature/fitness` | `HealthDataRepository` (+ fake) |
| `feature/sleep` | Sleep package marker (UI on Track tab) |
| `feature/track` | Combined Fitness/Sleep screen |
| `feature/nutrition` | Food scan stub + `NutritionAnalyzer` |
| `feature/water` | Cup counter + DataStore |
| `feature/weather` | `WeatherRepository` (+ fake) |
| `feature/insights` | Trends stub + `InsightsAnalyzer` |
| `feature/settings` | Drive sync stub + sign out |

Add new features under `feature/<name>` and register in `di` + `navigation`.
