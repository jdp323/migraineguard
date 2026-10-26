# Full app simulation report — v1.15.0

This is a functional simulation on an Android emulator with synthetic accounts, records, images and keys. It is not a claim that every possible code branch, physical device, Google account or medical/fitness recommendation is verified. No real personal records or provider keys are used.

## What the audit found

The user's exact Trainer failure came from a mandatory ready/adultConfirmed gate; tests had bypassed it with preconfigured profiles. The gate is removed and a new default-profile UI journey covers it. Review also found a plan timestamp serialization edge case and guided-draft replay risk. The [release guide](UPDATE_1_15_0.md) explains the fixes, zoom, repaired rear head and accurately labeled GIF variants.

A source inventory lists control declaration sites for review. It counts source sites rather than distinct runtime buttons, since loops render multiple choices. It is an audit aid, not a fabricated click-coverage percentage: [control-site inventory](SIMULATION_CONTROL_INVENTORY.json).

## Functional coverage matrix

D = actual rendered UI action with state/persistence assertions; R = Android repository/platform simulation; U = JVM logic/HTTP/parser test; P = needs a connected real phone, account, permission UI or service.

| Feature / controls | Coverage and evidence |
| --- | --- |
| Local setup: Start, local account, skip health/AI, metric/imperial, Save, review, Enter | D — NavigationFlowTest; checked skips/data checks also covered in SetupVerificationTest |
| Main Home/Migraine/Fitness/Coach/Food/Insights tabs, dock, swipe, Back, recreation, drafts | D — NavigationFlowTest and SwipeNavigationTest |
| Returning session and sign-out/account separation | D/R — SessionAndWaterTest and AccountIsolationTest; encrypted remembered identity and all original account-local tables/keys |
| Google sign-in, account chooser, consent | P — native account/registered-signer acceptance; no fake claim of live OAuth verification |
| Migraine point input, rotate, Undo, Clear, symptoms/triggers, save/history/open/delete | D — FullAppSimulationTest plus existing navigation/gesture coverage |
| Water add/remove/history and notification quick actions | D/R — FullAppSimulationTest; atomic 1/2/3-glass receipts, duplicate/wrong-account/expiry rejection in SessionAndWaterTest |
| Food description, offline analysis, saved history | D — FullAppSimulationTest; parsing and fallback logic U |
| Camera/gallery and actual image-provider response | R/U/P — fixture image preparation/metadata/consent/parser tests; native picker, camera hardware and real image AI remain P |
| Health Connect steps, BPM, sleep, refresh/error/empty/source handling | R/U — ten HealthReadingTest scenarios with synthetic Samsung and non-Samsung writers; actual wearable transfer/OS grants remain P |
| Default Trainer: select/clear, Build without setup, actual log, history reuse, optional equipment preferences | D — FullAppSimulationTest starts with false legacy setup/age flags |
| Anatomical male/female front/back region selection and swipe ownership | D — TrainerFlowTest; direct taps on actual rendered vector paths |
| Zoom buttons, pinch, pan, fit, zoomed hits, rear head, recreation | D/U — FullAppSimulationTest and AnatomyZoomTest |
| Session constraints, exclusions, time, experience, confirmed/history-based load advice, progression | U/R — TrainerEngineTest and trainer runtime tests; physical technique/load suitability remains P |
| Strength reps/sets/completion, actual RIR, cardio speed/incline/distance, stretch/hold rounds | D/R — NavigationFlowTest, TrainerFlowTest, FullAppSimulationTest and metric serialization tests |
| Exercise guide: search, GIF filter, steps, Play/Pause/Restart, Use, preserve/replace draft | D — FullAppSimulationTest; all nine assets also decode and change frames in native Android Movie |
| Every catalog exercise has equipment and setup guidance | U — AnatomyZoomTest; unrepresented GIF variants explicitly show text-only guidance |
| Week build/review/save/reopen/remove, latest-five retention, actual logged markers | D/R/U — trainer UI and plan/persistence tests; individual phone acceptance still needed for all account/backup timing combinations |
| AI week consent, allowed IDs, recovery/time validation, invalid reply fallback, cancellation | R/U — TrainerFlowTest and TrainerEngineTest with deterministic provider fixtures; real-key provider quality remains P |
| Weight input, invalid zero, valid save and profile update | D — FullAppSimulationTest |
| Body photo normalization, note check-ins, compare, deletion markers | D/R — BodyProgressTest |
| Readable export, encrypted recovery, wrong password, idempotent restore, original migrations | R/U/D — AppReliabilityTest, BodyProgressTest and existing format tests; real document-provider destination UX remains P |
| Private Drive restore/merge, key/profile recovery, failed/cancelled check, body-photo opt-in/deletion | R — DriveRestoreTest with simulated transport; native friend-account transfer and authorization remain P |
| Medication add, taken state, delete | D — FullAppSimulationTest |
| Reminder channel switches, interval menus, invalid/valid active hours | D — FullAppSimulationTest; worker timing/background access logic R/U; Samsung/OEM delivery remains P |
| AI provider menu, model/key save/remove, on-device mode | D/R/U — FullAppSimulationTest, encryption/account and provider protocol tests; live Test selected provider remains P |
| Appearance/reduced transparency and settings navigation | D — FullAppSimulationTest and NavigationFlowTest; TalkBack and broad large-font/device acceptance remain P |
| Manual Check for updates and reported result | D/U — FullAppSimulationTest invokes the public metadata check and records its actual success/rate-limit/network status; parser/version/URL tests U |
| Offline Coach question, persisted response and Insights navigation | D/R — FullAppSimulationTest; online provider responses require the user's key/account P |
| Weather | U/R — existing parsing/cache/unavailable logic; real location permission/GPS/network pressure readings remain P |
| Public download/share/install | APK/signer/hash checks plus anonymous asset downloads at release; install/update compatibility requires the retained package/certificate; actual phone acceptance remains P |

## Stress and accessibility-sized replay

The main suite is followed by the gesture, trainer and full simulation scenarios with system animations enabled and font scale 1.25. A fixed-seed 300-event Android Monkey run exercises additional taps, drags and navigation; runtime crash logs are inspected. This can expose crashes and layout failures but does not prove that every control or hardware path was reached.

Screenshots use synthetic records. Native status/navigation bars or keyboard may be outside Compose captures; some intentionally scroll to a control. External OAuth, Health Connect device data, native file/photo pickers, real cloud AI and OEM reminder delivery must not be described as proven by the emulator fixtures.

## Results

Pending the final hosted run. Exact source, test counts, repeat counts, smoke result, real update-check result and screenshot links will be recorded here after verification.
