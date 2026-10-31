# MigraineGuard user guide

For Android v1.16.0 (version code 26). Start here if a friend shared the app with you. No programming or Google Cloud setup is needed to use the published APK.

[Download the latest release](https://github.com/jdp323/migraineguard/releases/latest) · [Troubleshooting](TROUBLESHOOTING.md) · [How it works](HOW_IT_WORKS.md) · [Privacy](https://gen-lang-client-0426132502.web.app/privacy.html)

[v1.15: direct workouts, zoom, exercise GIFs and simulation results](UPDATE_1_15_0.md). [v1.13: glass design, muscle selection and the training studio](UPDATE_1_13_0.md). [v1.12: movement breaks, body photos and data export](UPDATE_1_12_0.md). [v1.11: swipe navigation and the refreshed interface](UPDATE_1_11_0.md). [v1.10: water quick actions, cardio/stretch fields and returning Home](UPDATE_1_10_0.md). The previous feature-by-feature guide is [v1.9.0–1.9.1: training, reminder/AI/update settings and Drive size](UPDATE_1_9_0.md). Read [exact AI prompts and permissions](AI_AND_PERMISSIONS.md) to see what is sent and why.

## 1. Install or update

1. Open the release link on your Android phone. Expand **Assets** if needed.
2. Download **MigraineGuard-v1.16.0-debug.apk** for the normal app. The word debug identifies the current signed test build; this is not a Play Store release.
3. Open the downloaded APK. Android may ask whether your browser or file manager can install apps. Allow that source if you trust this download, install, then turn the source permission back off if you wish. Do not disable Play Protect or other device-wide protections.
4. Updating the same Standard app with the retained certificate keeps its local data. Do not uninstall an existing app just to get past an installation conflict.
5. For an incompatible older signing certificate, or to try a fresh setup beside your existing app, use **MigraineGuard-v1.16.0-preview.apk**. Preview is a separate app with its own records, permissions and setup. It does not automatically copy Standard's local data.
6. Confirm **Settings → App → About** says **1.15.0 (26)** and the intended package: Standard is com.migraineguard.app; Preview ends in .preview.

The app supports Android 8 and later. Health Connect additionally depends on compatible Android/Google services and source health apps on the phone; an installable app does not guarantee every integration is available. This project contains a phone app, not a separate watch app or iPhone build.

## 2. Complete setup

Long pages scroll. The progress header shows Welcome, Account, Health, AI and Profile. Back lets you revisit editable earlier steps. A check means that particular action succeeded during this setup; it is not a permanent guarantee that a service remains connected.

| Step | What to do | What the result means |
| --- | --- | --- |
| Welcome | Read the introduction and choose Start setup. | You acknowledged the welcome. |
| Account | Choose Google or Continue locally. For Google, optionally authorize private Drive app-data backup. | Google identity and Drive access are separate. Local mode works without either. |
| Health | Connect this phone's health data, grant the record types you want, then check the results. | Service availability, permissions and actual received records have separate checks. |
| AI | Optionally paste your own Gemini key and use Save & Test. Wait for the response, then Continue. | A successful response checks the key. Opening AI Studio alone does not verify it. |
| Profile | Enter your chosen name, date of birth and measurements; select units and save. | Profile is checked after validation and saving succeed. |
| Review | Review checked and skipped steps, then finish. | Optional skipped steps stay marked Skipped. You can configure them later. |

**Skipping AI verification preserves any keys already saved.** To stop cloud requests, remove your provider keys in Settings. With no configured keys, the on-device fallback remains available. A failed connection test does not receive a success check.

### Connect Samsung, Garmin, Fitbit and other devices

The path is **wearable → compatible companion app on the Android phone → Health Connect → MigraineGuard**. Google sign-in is not health permission.

1. Sync your device in Samsung Health, Garmin Connect, Fitbit / Google Health or another supported companion app.
2. In that app, enable sharing/writing supported types to Health Connect. Actual supported types depend on the app/device/version.
3. In Android Health Connect → Data and access, confirm actual dated Steps, Heart rate and Sleep records exist.
4. In MigraineGuard Fitness, connect this phone's data to this app account and grant the read types you want.
5. Choose **All sources** for the combined Health Connect total, then Refresh. This is the default for new profiles. Existing Samsung preferences are kept until you change them. Samsung Health is an optional alternative; the totals are never added together.
6. Check source/timestamps and the copyable sync report if a reading is delayed. Heart is the latest dated sample, not live pulse; sleep requires completed sessions.

No direct Garmin/Fitbit/Samsung login or watch app is used. A source must actually export records into Health Connect. [Detailed device setup](UPDATE_1_12_0.md#health-connect-across-brands) · [Missing-data checklist](TROUBLESHOOTING.md#steps-heart-rate-or-sleep-are-missing-or-different).

### Optional Gemini and other AI keys

Use your own provider account/key. You can open [Google AI Studio](https://aistudio.google.com/apikey) from setup or Settings, create a suitable key and paste it into the Gemini field. Save and test it. Google login does not include an AI subscription or Gemini quota. Provider availability, charges and limits depend on your account; do not assume the historical word Free in a source class name guarantees free usage.

Settings → AI has eleven provider choices including OpenAI/ChatGPT API and Claude, with editable model IDs and an explicit selected-provider connection test. Selected mode tries only that service then offline. Legacy Auto retains Gemini → Groq → OpenRouter → offline. The app ships no developer AI token. Never post your key in an issue, screenshot, chat transcript or source file.

## 3. Use the app each day

Swipe left/right across the main page to move between the six labeled tabs, or tap a tab to jump directly. The dock follows the page. Swiping inside the pain map/horizontal controls operates those controls. Home shortcuts open additional tools; use the toolbar or Android Back to return. Back from another main tab returns Home; migraine log/history/detail has its own pane Back. Android edge-back gestures require the phone’s system gesture-navigation mode. Drafts stay with their tab; save completed entries before force-stop/account switching. Pages and forms scroll vertically.

| Area | How to use it | How to interpret it |
| --- | --- | --- |
| Home | Check compact daily essentials, add water and open Train, Meals, Body or Coach. Migraine context and medication shortcuts are in the Migraine tab. | The summary reflects logged/received values and selected goals. An unavailable reading is not inactivity. |
| Migraine | Select a head view with chips, arrows or swipe, tap pain points, choose intensity/symptoms/triggers, add notes and Save episode. Use the history icon to reopen entries. Undo point and Clear view correct unsaved markers. | The illustrated head uses stored points across views; it is not a diagnostic scan or a live anatomical model. Tagged triggers are your observations. |
| Fitness | Health shows source readings. Training has Strength, Cardio, Stretching and Other tiles. Strength logs weight/reps/sets; treadmill logs speed/incline/distance; stretching logs hold seconds/rounds. Every type has name/duration and optional notes/calories, history, reuse and weekly goals. | Manual workouts and Health Connect exercise may overlap. The weekly training goal counts distinct days with saved exercise logs. Manual steps are an explicit override, not proof that the watch connected. |
| Coach | Choose a persona, type a question and decide whether to include the optional health summary. Review the returned response. | Cloud requests send the text you type; health context needs its sharing choice. Replies and prompts are saved to this account's journal. AI personas are educational, not clinicians. |
| Food | Describe a meal/portion and Analyze description. Optionally select Camera/Gallery and explicitly enable sending the photo to your selected image-capable provider. | Selecting a photo alone does not upload it. Estimates can be wrong or unavailable; missing calories/macros are not zero. Photo analysis is a separate Gemini/OpenAI/Claude request according to your selection. |
| Insights | Select the available mode/range and review local journal patterns and clearly labeled current snapshots. | Descriptive patterns do not prove causes. Current steps/weather are not historical averages. Opening Insights does not automatically upload a health summary. |
| Water | Use plus/minus to adjust today's cup count and watch progress toward the goal. | Water notifications also offer 1/2/3 glasses of 250 ml each. Expand the notification to see the buttons; one choice applies once. The stored quantity is integer cups; changing the display unit does not turn it into a measured sensor volume. |
| Body metrics | Save weight in the selected display unit and review history/trend. Edit profile for height, DOB and units. | Measurements are stored consistently; switching units converts them. |
| Medications | Add your own name/dose/time label, then mark taken for the day as appropriate. | This is a journal. Time labels do not schedule exact dose alarms or calculate doses. |
| Settings | Choose Account, AI, Reminders, Data or App. Change provider/key/model after setup, control per-type reminder intervals/active hours, check updates and inspect measured Drive snapshot size. | Android notification/channel/battery settings can affect reminder delivery. |

Optional weather requires the phone's approximate-location permission and location services. Approximate coordinates are sent to Open-Meteo for that request. Unavailable or stale weather is labeled.

## 4. Keep each Google account's details

Each Google account has a separate local profile, journal and provider keys. Continue locally has its own preserved local records. Changing Google accounts is not a way to transfer one person's records to another. Health Connect belongs to the phone, so only connect its records to an account if they belong to that person.

On first Google connection, choose **Allow private Drive backup**. The app checks that account's hidden app-data area for its snapshot before asking for a new profile. A successful first manual connection shows a check and **Continue with verified Drive**. A returning account with a completed profile can proceed without re-entering its profile/key forms. Each phone still needs its own health permissions and ownership choice.

In Settings, use **Back up now** and wait for a completed save before changing devices or signing out. Changes are saved automatically while the app is active after a short debounce; background/sign-out saves are best effort. A connection check does not prove your most recent edits were uploaded: check the save result/timestamp. If sign-out reports Backup needs attention, reconnect and retry, or knowingly keep only the local copy.

Drive app-data is hidden from ordinary Drive files and belongs to the authorizing Google account. A blank Drive folder view is not evidence that backup failed. The snapshot includes profile/DOB/measurements/goals/settings, personal AI keys, seven original journal tables and dated circumference logs. Body progress is included only after its separate photo-backup switch is enabled. Meal/equipment photo files and raw Health Connect records are excluded.

**Cloud key storage:** Google protects transport/storage, and keys are encrypted again with Android Keystore locally. The portable keys inside the Drive JSON are not separately password-encrypted or end-to-end encrypted. Choose this optional feature only if that matches your preferences. Protect your Google account and provider accounts.

This is a current snapshot with additive journal restore, not WhatsApp-compatible backup or a conflict-free multi-device sync service. A deleted original journal entry can return from an older snapshot; body-progress check-ins and new circumference logs have deletion markers to prevent that photo from returning. Prefer one active editing device and complete a save before moving to the next.

## 5. Export your data or a recovery file

Open **Settings → Data**. Export readable data & body photos saves a ZIP with structured JSON, eight CSV tables and body JPEGs. It is **not encrypted**; use a location you trust and review it before sharing. The ZIP is for reading/analysis, not app import.

For recovery, enter a password of at least eight characters and Export encrypted backup. Import backup and keep existing records restores supported .mgbak files without duplicating the same content. Also restore profile, goals and preferences is optional. Body progress photos are included; configured API keys, sign-in tokens, raw Health Connect data and meal/equipment photo files are excluded. Existing local keys are preserved on preference restore. Compressed and expanded snapshots each have a 20 MiB limit; an oversized backup fails visibly. Keep the password and check the file before removing any data. The Android file picker can choose local storage or a document provider such as your own Drive.

New snapshots use compact JSON and gzip only when smaller; encrypted recovery compresses before password encryption. This version reads older plain-JSON backups, but older apps cannot read the new formats. Update all devices first.

These manual formats are separate from automatic private Drive app-data backup. [Exact formats and limitations](UPDATE_1_12_0.md#export-and-recovery).

## 6. Disconnect and remove data

Signing out separates the active account; it does not erase its local journal or Drive snapshot. Pause automatic backup before deleting a cloud snapshot so a later save does not recreate it. In Drive's Manage apps controls, delete MigraineGuard hidden app data where available, then revoke its Google authorization. Clear the installed app's storage separately if you intend to remove its local copy. Standard and Preview have separate storage. Revoking access alone may leave a snapshot; uninstalling alone does not delete Drive data.

Rotate/revoke AI keys at their provider to invalidate them. Removing a local record does not retract a request already sent to a cloud provider. Health Connect/source-app records have their own deletion controls. See the [privacy policy](https://gen-lang-client-0426132502.web.app/privacy.html) for the full boundaries.

## 7. Share and report a problem

Share [the public release page](https://github.com/jdp323/migraineguard/releases/latest) with friends. Each person uses their own Google account and keys. Sharing the download link does not share your private records or Drive authorization.

For problems, start with [Troubleshooting](TROUBLESHOOTING.md). A public GitHub issue should contain only the app version/package, Android version, general device model, steps, expected/actual behavior and a redacted error. Keep sync reports and medical screenshots private; crop/redact names, account emails, exact timestamps and any health detail you do not want published. Never attach API keys, tokens, a keystore, a Drive snapshot or a journal export.

This remains a development wellness/tracking build. Automated emulator checks do not establish real watch synchronization, live cloud-provider availability or clinical accuracy.

**Multiple-device backup compatibility:** update all devices before editing/restoring this backup. Older app versions can ignore and later overwrite newer training/provider/reminder fields. Avoid simultaneous edits; Drive snapshots are not conflict-free live sync.

## Returning after an app restart

Your selected local/Google identity is remembered on this phone with device encryption. Once setup is complete, reopening the app goes to Home. Drive reconnects without blocking Home when permission was already granted; otherwise open Settings → Account to reconnect. Health permissions remain enforced in Fitness. Explicit Sign out clears the remembered identity; selecting a different/new account still opens its own records or first setup. Uninstall/clear-storage/new phones require sign-in and optional Drive restore again. Never uninstall records to troubleshoot a setup loop.

## Movement breaks and body progress

Settings → Reminders → Stand, stretch or walk enables adjustable break alerts. Optional background step access suppresses an alert after at least 20 shared steps during that interval. Without usable data it is a general timer; it cannot prove you were sitting. Active hours and Android battery/notification controls apply.

Settings → Account → Body measurements / photos / weight opens dated photos, optional measurements and notes. Choose two check-ins to compare photos and recorded weight/waist/scale-body-fat changes. Photos stay in this account on the phone by default and are never sent to AI. Separately enable Include body photos in my Google Drive backup if you want them in your own connected Drive snapshot. Use Delete this check-in to remove one; independent exports/cloud copies need their own controls.

## Fitness trainer and glass appearance

Open Fitness → Trainer. Choose Male or Female as the display illustration, swipe the figure or use Front/Back to switch sides, then tap multiple muscle regions. Coral means selected; chips provide another way to choose. The illustration does not change your account profile.

Select regions and build immediately; no adult checkbox or completed training setup is required. Training preferences is optional and lets you change experience, goal, equipment, time, frequency and areas to skip.

Pinch or use +/− to zoom to 300%; drag to pan when enlarged, and Fit body to restore side swipes.

Open Exercise & machine guide · GIFs to search all 35 movements. Nine exact variants have offline Play/Pause/Restart demos; all have setup and written steps. Use in exercise log preserves or explicitly replaces an unsaved draft. Build today’s workout shows sets/reps or holds, rest, cues and load guidance. Unknown machine/dumbbell loads remain a test-set choice; known comparable loads can be repeated conservatively. Use in exercise log fills an editable draft; confirm replacing an existing unsaved draft, then mark only completed sets and optionally record actual reps left.

Build week on this device works offline. Optional Arrange week with my AI requires the separate checkbox sharing age/height/weight, preferences and comparable recent workout sets with the selected provider. Review before saving. The latest five schedules are retained; actual journal history is separate. No photos or migraine/medication journal enters this request. Read [exact rules and limits](UPDATE_1_13_0.md).

Settings → App → Reduce transparency changes the new glass cards to solid fills. System theme, dynamic color and supported Android animation preferences remain available.

## Dated tape measurements and storage

Open Settings → Account → Body measurements / photos / weight → Log measurements. Choose YYYY-MM-DD and Core, Upper body, Lower body or Custom. Enter only the areas you measured; empty areas consume no measurement values. Your profile chooses cm/in, and an open draft retains its starting unit. Save adds the dated entry; hiding/reopening preserves a draft, and Discard requires confirmation.

The 20 standard areas include neck/shoulders/chest/abdomen(tummy)/waist/hips and separate left/right relaxed upper arms, flexed biceps, forearms, wrists, thighs, calves and ankles. Add up to eight custom areas. History lets you edit dates/values or delete; Trends selects an area and compares its first and latest values using calendar spacing. These are entered measurements, never estimates from body photos.

Settings → Data → Compact storage shows this account's database/WAL and body JPEG bytes, plus live circumference-log count. Optimize local storage reclaims free SQLite pages while preserving records. It excludes the app, preferences, caches, gallery originals and independent exports. Photos migrate to binary JPEG storage without losing their original saved JPEG bytes; compact backup JSON is compressed when smaller. [Exact format/limits and file map](UPDATE_1_16_0.md).
