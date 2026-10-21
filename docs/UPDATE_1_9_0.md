# v1.9.0 — focused Home, training journal and connection controls

Application version 1.9.0 / code 16. Extends [v1.8.0](UPDATE_1_8_0.md), the [complete project guide](PROJECT_GUIDE.md) and [file/function baseline](FILE_REFERENCE.md).

## Use the reorganized app

Home now contains a compact daily essentials card, Add water, and four shortcuts: Train, Meals, Body and Coach. The previous pain-risk hint, migraine buttons, weather panel, energy check-in, medication shortcut, BMI card and extra AI section are removed from Home. Migraine now opens its own overview with Log migraine, Episode history, Medication, Review patterns, energy and optional weather. Weather is requested explicitly there and is context, not a headache forecast.

Fitness has **Health** and **Training** views. Health retains device connection, steps/source diagnostics, dated heart rate and completed sleep. Training contains a weekly active-day goal, named exercise entry and Training history. The previous generic type/minutes form is replaced by the structured logger; its optional calorie entry remains available. The old fixed exercise-of-day catalog remains in source for historical compatibility but is not presented as a personalized training plan.

Settings has four categories:

| Category | Contents |
| --- | --- |
| Account | Drive restore/save and measured snapshot size, Google identity/advanced setup, profile/body/medications, manual encrypted export, privacy and sign-out |
| AI | Provider dropdown, masked key, model ID, prefix suggestion, save/test/remove, explicit offline mode and legacy fallback |
| Reminders | Master permission/toggle, water/food/medication/migraine toggles and intervals, active hours, optional daily update checks |
| App | Check for updates/download page/release notes, appearance, goals, display units and installed-version information |

## Log an exercise

1. Open Fitness → Training → Log exercise.
2. Type the exercise/machine name. Matching names from a small local catalog appear as suggestions; custom names work too. Choose the activity type.
3. Optionally choose Machine photo or Camera. Photos remain local unless you explicitly enable sending one and press Identify machine. Gemini/OpenAI/Claude image paths can suggest a name; you must check it. A photo does not determine safe technique or a safe load.
4. For Strength, enter weight and reps per set. Use 0 for bodyweight. Add set copies the preceding values with completion unchecked; Remove last set adjusts the planned rows. Check the sets you actually completed. Empty unfinished rows are ignored on save; entered rows must validate.
5. Enter duration and optional calories/notes, then Save exercise. The draft remains if saving fails. Weights are stored in kg and displayed in kg/lb using your profile units; changing units converts a retained draft.
6. Training history shows the saved name/time, completed and unfinished sets, notes, optional local photo and completed lifting volume. Use as next session pre-fills its values with completion cleared. Last time compares the same normalized exercise name.
7. Set the weekly goal from 1–7 days. Progress counts distinct local calendar days with saved exercise entries since Monday, not the number of individual exercises or imported Health Connect workouts.

One saved row represents one named exercise/session entry. This is not a full multi-exercise routine builder, an automatic load-prescription engine or a machine safety assessment. The catalog and photo suggestions do not replace the machine's instructions. Photo file bytes are excluded from Drive/manual exports; references do not transfer between phones.

## AI providers after setup

Settings → AI supports Google Gemini, OpenAI/ChatGPT API, Anthropic Claude, Groq, OpenRouter, DeepSeek, xAI/Grok, Mistral, Perplexity, Together and Fireworks. Paste your own key and choose the model offered by that account. Together/Fireworks require an explicit model ID; defaults for other providers are editable. Provider availability, model access and charges can change. A ChatGPT/Claude app subscription is not an API key or a promise of API credits.

A known prefix can suggest a provider. Ambiguous keys remain unassigned; no key is tried against several websites to identify it. Selecting the suggested provider preserves the pasted value. Test selected provider sends only a tiny text request to that selected endpoint, without health context. An actual nonempty provider response checks the connection; offline fallback cannot pass. Testing does not save unsaved form changes. Save provider selects that provider for subsequent text requests. Editing clears the earlier check.

Selected-provider mode tries that service then the on-device fallback; it does not send the same prompt to other configured services after failure. Legacy automatic fallback retains Gemini → Groq → OpenRouter → offline for existing users who select it. On-device only retains keys but stops cloud provider selection. Remove selected key and Remove all my AI keys have separate controls.

Food and equipment images use the selected image-capable provider (Gemini/OpenAI/Claude); other selections explain that photos are not enabled instead of silently trying another service. In legacy Auto, the explicit image path uses Gemini. Selecting a photo alone does not upload it. See [exact prompts, protocols and data boundaries](AI_AND_PERMISSIONS.md).

## Notifications and updates

Water, meal/photo logging, medication and migraine check-ins have separate toggles and intervals from 15 minutes to one day. Choose active start/end hours in the phone timezone; overnight windows are supported. Master notifications apply to all categories. Android permission/channel settings still control delivery. The scheduler observes saved preferences, so it does not schedule with the stale pre-toggle value.

Each notification has a stable per-kind identifier and opens its matching app screen. Food only invites you to record a meal; it never opens the camera or sends a photo automatically. A persisted account marker allows generic reminders after process restart without restoring authentication, opening health records or exposing a prior account. Explicit sign-out clears that marker, cancels work and clears notifications. Each worker rechecks the account before posting.

WorkManager is approximate and subject to battery/OS scheduling, force-stop and permissions. These are periodic check-ins, not exact dose alarms or guaranteed clock-time notifications. Actual Samsung background delivery needs phone testing.

App → Check for updates reads the public GitHub latest-release endpoint, compares numeric semantic versions and offers its release page plus notes. It rejects draft/prerelease/no-APK responses and non-repository download pages. Installed/newer local builds are distinguished from an available update. A failed check does not claim that the app is current.

The optional background checker wakes periodically while connected, refreshes public release metadata at most daily and uses the cached result between checks. A newer release can notify once per version during active hours; tapping opens the GitHub release page. Nothing downloads or installs silently. Old v1.8.0 installations first need this update before they have this checking feature. GitHub receives ordinary connection metadata, not your health records or API keys.

## Drive storage

Account → Google Drive backup displays the current snapshot's confirmed byte/KiB size, measured from a completed download/upload. Back up now refreshes it. Before the first measurement, it says Not measured yet. This is this app's current snapshot size, not total Drive account usage, total quota or every historical file. No broader Drive permission is requested. Snapshots include profile, preferences, goals, all configured provider keys and journals; photos and Health Connect source records remain excluded. Portable keys in Drive JSON are not separately password/end-to-end encrypted.

## Changed file/function reference

Application paths below start at app/src/main/java/com/migraineguard/app/.

| File | Main responsibilities / changed functions |
| --- | --- |
| core/domain/ExerciseSet.kt (new), Models.kt | Serializable weightKg/reps/completed set and validation; WorkoutEntry adds exerciseName, sets and equipmentPhotoUri. |
| core/data/db/Entities.kt | WorkoutEntity adds name/setsJson/photo reference with serialization/domain conversion. |
| core/data/db/AppDatabase.kt, PreservingMigrations.kt, schema 5.json | Database v5; preserving 4→5 ADD COLUMN migration after existing 1–3→4 paths. No destructive fallback or table deletion. |
| feature/fitness/TrainingModels.kt (new) | TrainingProgress counts weekly distinct dates, normalizes names, finds previous sessions and calculates completed-set volume; local name suggestions. |
| feature/fitness/WorkoutJournal.kt (new) | Saveable exercise draft, name suggestions, unit conversion, camera/gallery reference, explicit image request, validated save/history/reuse and weekly goal UI. EquipmentPrompt defines the restricted classification request. |
| feature/fitness/WorkoutRepository.kt | save validates duration/name/set bounds before insertion. |
| feature/backup/BackupRepository.kt | Validates structured set JSON and strips equipment image references from portable snapshots/merge identity; preserves named sets. |
| core/data/prefs/UserPreferencesRepository.kt | Encrypted provider-connection map, selected mode/model IDs, workout goal, intervals/active hours, meal/update toggles, atomic setters and Drive restore. Existing three key fields remain compatible. |
| core/ai/AiCatalog.kt (new) | Provider metadata/protocols/default models and non-authoritative prefix suggestions. |
| core/ai/ConfiguredAiProvider.kt (new) | Selected service versus legacy fallback; builds authenticated Gemini/Responses/Claude/Chat requests, parses output, handles errors/cancellation and explicit supported image calls. connection/hasConfiguredCloud keep existing keys usable. |
| core/ai/StubProviders.kt | Shared cancellable transport now rejects redirects and bounds response bytes; original setup Gemini test remains. |
| core/ai/OfflineHealthBrain.kt, Personas.kt | Optional health-context JSON includes named exercises/completed weights/reps and weekly goal; coach prompt avoids invented performance/automatic load changes. |
| feature/home/HomeScreen.kt | Compact essentials and four navigation tiles; no automatic weather request or migraine-risk panel. |
| feature/migraine/MigraineScreen.kt, MigraineContext.kt (new) | Separate migraine overview, journal/history, medication/analyst links, energy check-in and explicit coarse-location weather. |
| feature/track/TrackScreen.kt | Separate Health/Training views; retains source/permission diagnostics and actual record reads. |
| feature/coach/CoachScreen.kt, feature/insights/InsightsScreen.kt | Connection prompts honor the selected provider/mode and open AI settings. |
| feature/nutrition/NutritionAnalyzer.kt, NutritionScreen.kt | Explicit image-capable selected provider instead of assuming every photo request goes to Gemini. |
| feature/settings/AiProviderSettings.kt (new) | Provider/key/model controls, prefix suggestion and real selected-endpoint tests; no cross-provider key probing. |
| feature/settings/ReminderSettings.kt (new) | Permission, per-kind toggle/interval, active-hour and update controls. |
| feature/reminders/ReminderPolicy.kt (new), ReminderWorker.kt | Pure plan/window/route policy; separate account-bound work and clickable notifications; cached daily update checks. |
| feature/settings/AppUpdates.kt (new) | Bounded public metadata fetch/cache, strict release parsing, numeric version comparison and download/notes card. |
| feature/settings/DriveSyncRepository.kt, DriveBackupScreen.kt | Confirmed backup byte size in state and Account UI. |
| feature/settings/CloudBackupFormat.kt | Validates new provider/reminder/training settings and key/model bounds; old snapshots decode using defaults. |
| feature/settings/SettingsScreen.kt | Four categories; retains account/profile/export controls and adds provider/reminder/update cards. |
| di/AppContainer.kt, MigraineGuardApp.kt | New provider routing, reactive reminder plan and durable account marker/cancellation on sign-out. |
| MainActivity.kt, navigation/MigraineGuardNavHost.kt | Consume validated reminder destinations after sign-in/setup; consistent provider-settings links and new migraine shortcuts. |
| AndroidManifest.xml, res/xml/file_paths.xml | Adds equipment-photo FileProvider path; removes unused precise-location and exercise-write permissions. |
| app/build.gradle.kts, .github/workflows/android.yml | v1.9.0/code 16, tests/build/signing/runtime gates and accurate release notes. |

## Verification status

Local checks completed before upload: **72 JVM tests, zero failures/errors; lint zero errors (104 non-blocking warnings); Standard and Android-test APK assembly**. New JVM regressions cover protocol headers/secret placement/response parsing, prefix ambiguity, semantic updates/URL rejection, training date/volume/name policies and reminder windows/routes/toggles.

Android tests extend preservation to old schema 4 workout rows, restore the new encrypted provider/goal/reminder fields through simulated Drive transport, verify confirmed snapshot size, and exercise named-set entry plus all four Settings categories. The successful hosted Android result, exact source SHA, released asset hashes and inspected screenshots will be recorded after that run completes. Twenty Android tests are required by the release gate.

No live provider keys, personal health records or real Google tokens are test fixtures. Real machine-photo recognition, provider quota/model access, Samsung/Fitbit transfer, native friend-account Drive transfer, and notification timing on a phone remain acceptance checks.
