# MigraineGuard user guide

For Android v1.11.1 (version code 20). Start here if a friend shared the app with you. No programming or Google Cloud setup is needed to use the published APK.

[Download the latest release](https://github.com/jdp323/migraineguard/releases/latest) · [Troubleshooting](TROUBLESHOOTING.md) · [How it works](HOW_IT_WORKS.md) · [Privacy](https://gen-lang-client-0426132502.web.app/privacy.html)

[v1.11: swipe navigation and the refreshed interface](UPDATE_1_11_0.md). [v1.10: water quick actions, cardio/stretch fields and returning Home](UPDATE_1_10_0.md). The previous feature-by-feature guide is [v1.9.0–1.9.1: training, reminder/AI/update settings and Drive size](UPDATE_1_9_0.md). Read [exact AI prompts and permissions](AI_AND_PERMISSIONS.md) to see what is sent and why.

## 1. Install or update

1. Open the release link on your Android phone. Expand **Assets** if needed.
2. Download **MigraineGuard-v1.11.1-debug.apk** for the normal app. The word debug identifies the current signed test build; this is not a Play Store release.
3. Open the downloaded APK. Android may ask whether your browser or file manager can install apps. Allow that source if you trust this download, install, then turn the source permission back off if you wish. Do not disable Play Protect or other device-wide protections.
4. Updating the same Standard app with the retained certificate keeps its local data. Do not uninstall an existing app just to get past an installation conflict.
5. For an incompatible older signing certificate, or to try a fresh setup beside your existing app, use **MigraineGuard-v1.11.1-preview.apk**. Preview is a separate app with its own records, permissions and setup. It does not automatically copy Standard's local data.
6. Confirm **Settings → App → About** says **1.11.1 (20)** and the intended package: Standard is com.migraineguard.app; Preview ends in .preview.

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

### Connect Samsung Health and your Galaxy Watch

The path is **watch → Samsung Health on the phone → Health Connect → MigraineGuard**. Google sign-in is not health permission.

1. Sync the watch in Samsung Health and let the phone finish receiving its records.
2. In Samsung Health's Health Connect connection, allow sharing/writing the supported types you want, especially Steps, Heart rate and Sleep. Menu names can vary by installed version.
3. In Android Health Connect, verify **actual dated records** are visible under those types. A permission switch by itself does not establish that records arrived.
4. Allow the installed MigraineGuard app to read the relevant types. Standard and Preview each need their own grant.
5. In MigraineGuard **Fitness**, choose **Connect this phone's health data** for your selected app account. This is a separate ownership choice, even when Android already granted permission.
6. Select **Samsung Health**, then **Refresh steps now** or **Refresh data**. Review source, timestamp and the status message. Select **Health Connect** to compare the combined aggregate; these are alternative sources, not totals to add together.

The same approach applies to Fitbit or other devices only if their companion app exports the required records to Health Connect. MigraineGuard does not sign into Fitbit directly or stream watch sensors. Heart rate is the latest available dated measurement, not a live pulse display. Sleep requires a completed session; an older available session is labeled. Missing data appears as unavailable, not a fabricated reading. See the [missing-data decision tree](TROUBLESHOOTING.md#steps-heart-rate-or-sleep-are-missing-or-different).

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
| Settings | Choose Account, AI, Reminders or App. Change provider/key/model after setup, control per-type reminder intervals/active hours, check updates and inspect measured Drive snapshot size. | Android notification/channel/battery settings can affect reminder delivery. |

Optional weather requires the phone's approximate-location permission and location services. Approximate coordinates are sent to Open-Meteo for that request. Unavailable or stale weather is labeled.

## 4. Keep each Google account's details

Each Google account has a separate local profile, journal and provider keys. Continue locally has its own preserved local records. Changing Google accounts is not a way to transfer one person's records to another. Health Connect belongs to the phone, so only connect its records to an account if they belong to that person.

On first Google connection, choose **Allow private Drive backup**. The app checks that account's hidden app-data area for its snapshot before asking for a new profile. A successful first manual connection shows a check and **Continue with verified Drive**. A returning account with a completed profile can proceed without re-entering its profile/key forms. Each phone still needs its own health permissions and ownership choice.

In Settings, use **Back up now** and wait for a completed save before changing devices or signing out. Changes are saved automatically while the app is active after a short debounce; background/sign-out saves are best effort. A connection check does not prove your most recent edits were uploaded: check the save result/timestamp. If sign-out reports Backup needs attention, reconnect and retry, or knowingly keep only the local copy.

Drive app-data is hidden from ordinary Drive files and belongs to the authorizing Google account. A blank Drive folder view is not evidence that backup failed. The snapshot includes profile/DOB/measurements/goals/settings, personal AI keys and seven journal tables. It excludes photo file contents and the Health Connect database.

**Cloud key storage:** Google protects transport/storage, and keys are encrypted again with Android Keystore locally. The portable keys inside the Drive JSON are not separately password-encrypted or end-to-end encrypted. Choose this optional feature only if that matches your preferences. Protect your Google account and provider accounts.

This is a current snapshot with additive journal restore, not WhatsApp-compatible backup or a conflict-free multi-device sync service. A deleted journal entry can return from an older snapshot because deletion tombstones are not implemented. Prefer one active editing device and complete a save before moving to the next.

## 5. Export a separate recovery file

Settings also provides password-encrypted journal export/import using Android's file picker. Keep the file and its password somewhere you control. The password cannot be recovered by the app. This format covers the seven journal tables and optional profile/goals; it excludes AI keys, photo files and imported Health Connect records. It is separate from automatic account Drive app-data backup. Restore merges records and avoids duplicate content rather than replacing the whole journal.

Before clearing app storage, uninstalling or testing another signing identity, verify a usable backup first. A local-only profile has no automatic Google restore unless it was separately exported; Google login does not silently claim older local records.

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
