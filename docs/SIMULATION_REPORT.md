# Full app simulation report — v1.15.0

This is a functional simulation on an Android emulator with synthetic accounts, records, images and keys. It is not a claim that every possible code branch, physical device, Google account or medical/fitness recommendation is verified. No real personal records or provider keys are used.

## What the audit found

The user's exact Trainer failure came from a mandatory ready/adultConfirmed gate; tests had bypassed it with preconfigured profiles. The gate is removed and a new default-profile UI journey covers it. Review also found a plan timestamp serialization edge case, guided-draft replay risk, provider feedback cleared by a settings refresh, transparent full-screen guide windows and Coach persistence coupled to scroll animations. Individual reminder edits now write only supplied fields to avoid stale-snapshot overwrites. The [release guide](UPDATE_1_15_0.md) explains the fixes, zoom, repaired rear head and accurately labeled GIF variants.

A source inventory lists 221 button/chip/toggle declaration occurrences for review. It counts source sites rather than distinct runtime buttons, since loops render multiple choices. It is an audit aid, not a fabricated click-coverage percentage: [control-site inventory](SIMULATION_CONTROL_INVENTORY.json).

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

Focused larger-text Food and Coach save preflights run before the main suite. The main suite is followed by the gesture, trainer and full simulation scenarios with system animations enabled and font scale 1.25. A fixed-seed 300-event Android Monkey run exercises additional taps, drags and navigation; runtime crash logs are inspected. This can expose crashes and layout failures but does not prove that every control or hardware path was reached.

Food and Coach submission tests dismiss the native keyboard and wait for its inset to close before tapping the real action button; database insert assertions remain required. Food is a virtualized LazyColumn, so its test scrolls the parent list to discover the offscreen submit control rather than assuming every item is already composed. The parent selector requires vertical scrolling and excludes the horizontal main-tab pager. The keyboard-open submit/IME-shortcut path is not separately simulated. Native IME animations do not run on Compose’s test clock.

Screenshots use synthetic records. Native status/navigation bars or keyboard may be outside Compose captures; some intentionally scroll to a control. External OAuth, Health Connect device data, native file/photo pickers, real cloud AI and OEM reminder delivery must not be described as proven by the emulator fixtures.

## Results

[Run 40435413413](https://github.com/jdp323/migraineguard/actions/runs/40435413413), source bf995c20f1eea49507a5de356a0b1588209ffa22: all gates passed. **91 JVM + 44 Android runtime tests**, **2 focused preflights** and **13 animation-enabled repeats at font scale 1.25** passed. Main runtime: 158.642 s; preflights: 64.296 s; repeats: 139.244 s. Lint: zero errors, 121 warnings.

The seeded smoke run reported 300 injected events and no app fatal exception. Two hardware keyboard/flip events were blocked by the emulator (/dev/input/event0 EACCES); those random injections are not counted as proven user actions.

The update-check UI performed a real public metadata request and reported that the candidate was the latest published version or newer; the capture shows installed 1.15.0 versus then-published 1.14.0. Both public 1.15.0 APK downloads now match the tested artifact hashes and retained signer. [Exact hashes, six inspected native screenshots, file/function changes and prompt](UPDATE_1_15_0.md#verification).

The review found and fixed the default-profile training gate, plan timestamp encoding, guided-draft replay risk, disappearing provider-action feedback, stale reminder edits, transparent dialog backgrounds and Coach response persistence coupled to scrolling. The test harness now handles native keyboard closure and virtualized list discovery explicitly. Passing fixtures do not convert the P entries above into verified real-device services.
