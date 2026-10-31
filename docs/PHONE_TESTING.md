> Current v1.16.0: [direct workouts without the age/setup gate, rear-head fix, zoom, exercise GIFs, full file/function changes and exact AI prompt](UPDATE_1_15_0.md). [Functional simulation coverage and remaining phone checks](SIMULATION_REPORT.md).

## v1.16.0 direct Trainer, zoom and guide acceptance

1. Update Standard without uninstalling; verify About shows 1.15.0/code25. Open Fitness → Trainer, select Chest and Quads and build immediately without opening Training preferences. No adult warning/checkbox should appear. Optional equipment preferences change the available movements.
2. Try Male/Female and Front/Back. Pinch or use +/− to enlarge, tap visible muscle regions and drag to pan. Fit body restores 100% and side swipes. Verify the repaired male rear skull/ears/nape and preserved selection after rotation/recreation.
3. Open Exercise & machine guide · GIFs from Trainer and manual Training. Search Leg press, enable GIFs, open Steps & GIF, Play, Pause and Restart. Check setup/steps and Source & license. Airplane mode should not prevent packaged playback. Nine variants have clips; the other movements have written steps.
4. Use a guide while a draft exists: Keep my draft should retain it; Use trainer draft should replace only after confirmation. Planned sets start unchecked. Save actual completed sets and reuse history without inheriting completion checks.
5. Save/remove a provider key and check the confirmation stays visible; do not send a real key in a report. Change several reminder intervals and reopen Settings to confirm each sticks.

The [simulation matrix](SIMULATION_REPORT.md) identifies actual UI coverage and remaining external-account/device checks; it does not claim every hardware interaction was simulated.


> Historical v1.10.0: [water quick actions, tailored exercise fields, remembered sessions, implementation and tests](UPDATE_1_10_0.md). Historical version supplements below retain their stated baselines.

> v1.9.0: [focused Home, structured training, reminder/update controls, AI providers and Drive size](UPDATE_1_9_0.md). [Exact prompts and permissions](AI_AND_PERMISSIONS.md).

> Historical v1.8.0: [interface, navigation, verified setup and changed-file/function reference](UPDATE_1_8_0.md).

## v1.8.0 interface acceptance

Update Standard over the same retained-signature package without uninstalling. Check all six tabs, the Home action cards, visible Water Back, light/dark themes and a larger Android font setting. Existing completed profiles should not be forced through setup again. To inspect a fresh setup without disturbing Standard, use the separate Preview installation and Continue locally.

Setup should show a permission check separately from actual received steps/BPM/sleep. Merely opening Samsung Health must not mark sharing complete. Skip optional Health/AI and verify the final review says Skipped. A successful Save & Test key check should remain visible until Continue; editing the key must clear its check. Native Drive success should likewise be visible after a manual connection, while already-granted automatic restore should continue as before. No real provider/health connection is proved by the synthetic emulator tests.

> Historical v1.7.0: [Drive account restore, Samsung query fixes, UI and complete changed-file/function supplement](UPDATE_1_7_0.md). This supersedes the older cloud-backup placeholder and current-version descriptions below; historical source references remain pinned to their stated baselines.

## v1.7.0 Drive restore and Samsung checks

Install the Standard v1.7.0 APK over the same signed installation. Keep Standard and Preview distinct. Sign in to the Google account containing your details and allow the native private Drive app-data prompt. Save any missing details once, then use Settings → Back up now and wait for the completed result before switching accounts. Other accounts must use their own profile/key/journal. Returning to the original account should restore birthday, measurements and keys without repeating setup. A fresh phone can restore only a previously completed backup for that account/application.

Google Auth now reports External / In production (2026-10-19), so previously unlisted accounts can request their own backup authorization; retry reconnect with a previously blocked account. This cloud-side change needs no APK rebuild. See [the public setup record](PUBLIC_GOOGLE_ACCESS.md). The Drive API and app-data/email scope declarations are enabled in the configured project. This does not replace the native permission grant on each user's phone. Google protects the portable cloud keys in transit/at rest; this is not a separately password-encrypted end-to-end backup. Local keys use Android Keystore.

Health Connect records are phone-specific. After restoring, verify this installed app's permissions. Fitness defaults to Samsung Health, offers a combined source, and labels a raw-record estimate if aggregates are zero despite records. BPM is a dated sample, not a live stream. If actual Samsung records remain missing, use Connection & sync details → Copy sync report and share the local diagnostic report, which omits profile/API-key information. See [implementation and known limits](UPDATE_1_7_0.md).

## v1.6.6 account and health checks (retained background)

Update the same installed package. Sign in with account A, set its profile/key, sign out and select account B: B must start with its own profile and no A keys/logs. Return to A to verify its records remain. Older pre-isolation records are preserved under **Continue locally**; they are not automatically assigned to Google accounts.

Each account must connect this phone’s Health Connect data in Fitness. Grant the installed Standard/Preview app READ access, and Samsung Health WRITE access, for Steps, Heart rate and Sleep. Open Samsung Health and sync the watch, then refresh. Check the new package/read-permission diagnostics and dated heart/sleep readings. See [full implementation and troubleshooting](UPDATE_1_6_6.md).

# Phone testing and persistent signing

## Current Google sign-in setup (v1.6.4)

Google Auth is configured in the accessible existing project gen-lang-client-0426132502. Standard and Preview are both registered with the retained FB:67 signing certificate, and the APK uses their matching Web audience. The current cloud setup is now External / In production, superseding the earlier single-tester restriction. Install the current APK and choose your account; do not create a new API key for Google login. Old shipped Web-client overrides migrate automatically while custom settings are preserved.

Google client changes may take five minutes to a few hours to propagate. The old APK still points to the earlier project until updated. A cancellation message alone does not prove the picker was dismissed. The user subsequently confirmed phone login works; new Drive authorization/transfer remains a phone acceptance check. See [the current source/setup supplement](UPDATE_1_6_4.md).

The sections below also preserve earlier signing and troubleshooting history.

The test release workflow verifies unit tests, lint and APK compilation before publishing to GitHub Releases. Publishing requires the repository Actions secret `MG_DEBUG_KEYSTORE_BASE64`. It contains the retained local Android debug keystore (alias `androiddebugkey`, test password `android`). The private key and its base64 value are excluded from Git and must never be committed.

Push and manual checks may use that secret. Pull-request checks do not restore it and cannot publish. The publishing job verifies the APK signature against the retained test certificate before publishing; an absent secret runs checks only. Existing version tags are not overwritten.

## Google sign-in

Google Credential Manager uses the explicit Google button flow. The installed APK's package and SHA-1 must be registered as an Android OAuth client in the **same Google Cloud project** as the configured Web OAuth client. A Web client ID alone does not make sign-in ready. NoCredentialException can also mean the phone has no available Google account.

Package: `com.migraineguard.app`

Persistent test SHA-1: `FB:67:0D:1D:96:C2:8B:9E:27:0D:36:F8:D6:57:53:E3:6B:30:8D:E8`

The published v1.6.1 APK used SHA-1 `B5:99:22:24:07:8C:CD:F3:7F:58:BD:C9:6A:DE:E7:53:90:01:DE:25`. Its FD:56 instruction was a hardcoded value from an older machine and does not identify that APK.

Earlier runner keys were temporary and cannot be recovered from an APK. A build signed with the new persistent key may not install over an earlier APK. Preserve the old installation if it contains important records; do not delete it to force an update. A record export/import facility is still needed before a signing-key migration for those users. Google sign-in is optional for local health reads and user-supplied AI keys.

## Steps

Home and Fitness refresh every 30 seconds while visible and resumed. Manual refresh remains available. The main count is the Health Connect aggregate for today's local calendar day, respecting source priority and deduplication.

The Samsung-specific aggregate is a comparison only; it is never added to the main total. The UI shows both counts, contributing source packages and read time. Missing Samsung data suggests checking export permissions and synchronization; a higher Samsung count suggests checking activity source priority. Neither diagnosis proves what happened on a phone without inspecting its Health Connect data.

Samsung's watch data reaches Health Connect through Samsung Health on the phone. Samsung Health needs permission to **write Steps**, while MigraineGuard needs permission to **read Steps**. Open Samsung Health after granting sharing, use Sync with Samsung account → Sync now, then refresh MigraineGuard. Check Health Connect's Steps entries and activity priority if totals differ.

Sources:
- https://codelabs.developers.google.com/sign-in-with-google-android
- https://developer.android.com/health-and-fitness/health-connect/aggregate-data
- https://developer.samsung.com/health/health-connect-faq.html

## Preview installation

The Preview APK uses package `com.migraineguard.app.preview` and separate local storage so an old installation can be kept intact. It uses the same FB:67 test certificate. Google sign-in for Preview requires an additional Android OAuth client for that package in the same project as the Web client. The available account currently lacks access to the older migraineguard-66229 project.

See [FEATURE_STATUS.md](FEATURE_STATUS.md) for implemented workflows and verification limits.

## Full-day step correction

Today’s cumulative queries now end at the next local midnight. Health Connect rescales interval counts when a query clips a record; a Samsung daily bucket can otherwise show only an early-morning fraction. The reported 12–14 versus 180 is consistent with that effect, but the actual Samsung record has not been inspected. After installing, refresh and compare both the combined and Samsung-only totals. No raw source totals are added together. DST boundaries are covered by unit tests.

Reference: https://android.googlesource.com/platform/packages/modules/HealthFitness/+/8d79b9403cee11fdd9d2bea173adff0edb6bec13/tests/cts/src/android/healthconnect/cts/StepsRecordTest.java (rescaled-count aggregation tests).

## v1.9.0 acceptance checks

- Update Standard without uninstalling; confirm 1.9.0 (16), old journal rows and old workouts survive. Preview is separate.
- Home should show essentials/shortcuts, with migraine/context/history/medication tools in Migraine.
- Fitness → Training: enter a custom name, 40 kg × 8 reps, check one set, save and verify its history. Add/edit set rows and test kg/lb draft conversion, previous-name match, reuse and the distinct-day weekly goal.
- Attach an equipment photo locally, deny upload and verify no request occurs. Only with your chosen Gemini/OpenAI/Claude key and explicit consent, test name suggestion; check that UNKNOWN/failure leaves manual entry usable.
- Settings → AI: select provider/key/model, save, test a real response and change/remove it. A wrong key must fail the selected-provider test even if offline coaching works. Ambiguous sk- keys must not be tried at multiple services.
- With two accounts, confirm provider/model, training/reminder choices and journals stay separate; complete Drive save/restore and compare the displayed snapshot bytes.
- Enable Android notifications and each desired type, choose a 15-minute test interval and active hours, background the app, then test taps to Water/Food/Migraine/Medication. Force-stop and OEM battery policy can suppress work; do not describe approximate WorkManager jobs as exact alarms. Sign out and confirm prior-account reminders stop.
- App → Check for updates: verify current/newer/offline/rate-limit messages, notes and the GitHub download page. A new release notification requires a future version after installing this checking feature; no silent installation should occur.

## v1.10.0 acceptance

1. Update Standard in place; confirm 1.10.0/code18 and retained profile/key/history. Sign in once after upgrading, close/reopen and restart the phone: completed accounts should open Home. Try offline; Drive must not block Home. Explicitly sign out and choose a different account: data and keys remain separate.
2. Enable water reminders, wait through the selected interval/active window, expand notification and choose 1/2/3 glasses. Confirm 250/500/750 ml in Water. Test on the lock screen and after closing the app. Repeating another button must not duplicate the same reminder. Old-account buttons must stop after sign-out.
3. Log one strength session, treadmill speed/incline/distance, and stretching duration/hold/rounds. Confirm only relevant fields, metric/imperial conversions, history and Use as next session. Back up and restore on another updated installation; metrics transfer, photos do not.
4. Disable notifications and verify no reminders. Keep any real records; use separate fixture accounts/Preview for destructive reset tests.

## v1.11.0 gesture and visual acceptance

1. Update Standard in place. Swipe both directions through all six main tabs; tap distant tabs and confirm the dock follows. Check vertical scrolling, the pain-map rotation, sliders and text selection independently.
2. Type unsaved Food, Coach, Training and Migraine fields. Visit a distant tab and return; check drafts and scroll. Rotate/recreate the activity, then check the selected page. Save important entries before force-stop or sign-out.
3. Open Settings → Profile, Water, Body, Medications and Drive. Use toolbar Back and system edge-back with the phone in gesture mode. Confirm return to the previous detail/page; cancel a Drive check and retry without losing local data.
4. Inspect light/dark and optional dynamic color, a larger font/display scale, the Samsung keyboard, and system reduced/disabled animations. Note real phone fluidity; no fixed display refresh rate is promised.

## v1.12.0 acceptance

1. Update Standard in place to 1.12.0/code21; check existing account records and all main tab gestures. In Fitness choose All sources. Verify actual Samsung, Garmin, Fitbit / Google Health or another source’s dated records in Health Connect before comparing values.
2. In Settings → Reminders enable movement, choose 30/60/90 minutes and suitable active hours. Try permission denied/unsupported and optional background granted. Timed fallback must not claim sitting; at least 20 shared recent steps should suppress an occurrence. Check notification/channel and OEM battery controls; timing is approximate.
3. Add front/side progress photos with dates, notes and optional measurements; restart and compare two check-ins. Confirm local retention and account separation. Use synthetic/non-sensitive photos when reporting public issues. Body photos must never trigger AI traffic.
4. With your own connected Drive, leave photo inclusion off and save; enable it explicitly, save and restore on another updated installation. Delete a fixture check-in, save and reconnect to confirm it stays deleted. Test a temporary network failure; local data must remain.
5. Settings → Data: save/read a ZIP locally, inspect JSON/CSV/JPEG files, confirm configured provider keys are absent. Export encrypted .mgbak and restore on a fixture installation; wrong password must fail. Verify body photos and preferences while existing keys remain. Check cancellation, insufficient disk space and oversized snapshots without deleting real records.
6. Disable movement/master notifications and sign out; old-account alerts must stop. Test a larger font, keyboard, light/dark themes and Android photo picker on the actual phone.

## Historical v1.13.1 acceptance

1. Update Standard in place to 1.13.1/code23. Inspect glass surfaces, ambient backgrounds, sharp text and dock in light/dark/dynamic color. Enable Reduce transparency, larger fonts and Android reduced/disabled animations. Check contrast and real phone fluidity; no fixed frame rate is promised.
2. Fitness → Trainer: rotate slowly/quickly both directions; switch Front/Back; tap chest and thighs and add multiple chips. Confirm selected regions persist across views and that a figure drag does not change main tabs. Outside the figure, main pager and Android edge-back still work. Test labeled controls with TalkBack.
3. Save adult training preferences and build a session with your actual equipment/time. Verify excluded areas are not used. Unknown load must remain a test-set choice. Test metric/imperial and a confirmed comfortable load; compare with the same equipment’s actual logged sets and RIR. Do not test unfamiliar heavy loads to validate software.
4. Use in exercise log: retain or explicitly replace an unsaved draft; check manual load/reps/set edits, timed holds and actual RIR. Only checked real sets should save as completed. Switch Fitness modes and main tabs, rotate/recreate, and review supported draft retention.
5. Build/review/save an on-device week, reopen it and log a real day. Logged markers must follow actual matching exercise/date; future schedules must not fabricate workouts. Confirm latest-five retention and that removing a plan keeps actual history.
6. Without the training-sharing checkbox, no AI planner request should run. With a selected provider/key and explicit consent, request a week and inspect its allowed exercises/time/rest arrangement. Missing keys/errors/unsuitable responses must label a local alternative. Live provider response quality still requires real-key testing.
7. Back up/restore a fixture account through encrypted .mgbak and connected private Drive on updated installations. Verify trainer preferences/plans, glass setting, actual RIR/trainer IDs and account isolation. Manual export must exclude configured keys and body photos must never enter trainer AI. Update all devices for manual format3/Drive format4 before restoring.

## v1.14.0 acceptance

1. Confirm 1.14.0/code24. Fitness → Trainer should show the flat anatomical illustration, not a faceted 3D mannequin. Try both Male/Female drawings and Front/Back in light/dark themes, larger fonts and TalkBack. This display choice must not alter your account profile.
2. Tap a filled chest region, add quads, deselect/reselect, switch sides and illustrations. Coral highlights and labeled chips must agree. Select glutes/hamstrings on Back. Head/joints/unsupported details must not trigger a different target.
3. Swipe within the map to switch sides, outside it to navigate main tabs. Check native Back and disabled/reduced system animations. Selections must remain across Fitness modes.
4. Generate a multi-target session and use it in the actual editable exercise log. Existing weekly planning, load/history rules, backup formats, account isolation and AI explicit consent still follow the v1.13 checklist; no permission should be newly requested by opening the map.

## v1.16 measurement/compact-storage acceptance

1. Update Standard in place. Check 1.16.0 (26), existing photos/notes/weight and journal history.
2. Add dated waist/chest/both biceps and thighs, a custom area, and a second date; check trends, edit, delete and draft Discard. Repeat in inches.
3. Export an encrypted recovery file and readable ZIP through your actual document provider. Verify body-measurements.csv uses cm and that a second installation restores old and new supported files. Do not remove originals before verifying.
4. Complete a private Drive save, sign into the same account on another updated installation and check measurements/profile/keys plus the separate photo opt-in. Actual Google transport is outside the deterministic emulator fixture. Avoid simultaneous edits.
5. Read Compact storage, Optimize and confirm all records remain. Exports/gallery originals are separate copies.
