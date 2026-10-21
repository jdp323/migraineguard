> New controls and fixes: [v1.9.1 guide](UPDATE_1_9_0.md), [AI prompts and permissions](AI_AND_PERMISSIONS.md). Settings → App → Check for updates opens the current GitHub release.

# Troubleshooting MigraineGuard

Applies to v1.9.1 (17). Check **Settings → App → About** first. The Standard and Preview apps have different storage and permission grants. Preserve records before uninstalling, clearing storage or switching APK identities.

[User guide](USER_GUIDE.md) · [Latest download](https://github.com/jdp323/migraineguard/releases/latest) · [Technical guide](HOW_IT_WORKS.md)

## Install, update and downloads

| Symptom | Likely cause | What to do |
| --- | --- | --- |
| Release link says 404 | Old/wrong link, an older private access state or a stale browser page | Open the public latest-release link above in a fresh tab. Repository visibility is public as checked October 21, 2026. Download from Assets, not a historical Actions run. |
| Phone shows an older release | Cached page, old bookmark or an older installed APK | Open the specific v1.9.1-debug release and select its APK. Check installed Settings → App → About after updating. Source-code ZIPs are not installable APKs. |
| App not installed / package conflicts | Different signing certificate, wrong package, unsupported Android or insufficient storage | Keep the existing app/data. Try Preview alongside it. Standard updates require the same package and retained signer. Ask for help before uninstalling an app with records. |
| Android warns about installing from this source | APK is sideloaded rather than installed through Play | Review the GitHub URL and APK identity. Allow the specific browser/file manager only if you trust the file. Do not disable device-wide security controls. |
| APK appears to download as a ZIP | You downloaded source code or an Actions artifact | Use the .apk release asset. Do not rename a source ZIP to .apk. |
| Standard and Preview do not share data | Separate Android packages | This is expected. Each needs its own setup/permissions. Use a supported export/restore or that account's Drive snapshot where appropriate. |

## Google sign-in and Drive backup

| Message or behavior | Check | Fix or next step |
| --- | --- | --- |
| Sign-in cancelled | Was the account picker dismissed, or a system prompt interrupted? | Try again and complete the picker. Check internet, device Google accounts, Play services and automatic date/time. A repeatable cancellation needs the exact redacted error and installed version; it does not by itself identify a certificate failure. |
| No Google accounts available | Device account/credential-provider state | Add the intended Google account in Android settings and retry the explicit picker. Continue locally is available while troubleshooting. |
| APK signing certificate SHA-1 is not registered | Installed package and actual runtime certificate | In Settings → Account → Advanced sign-in setup → Show SHA-1, compare the installed APK to the published certificate. A locally rebuilt APK normally has a different signer. The maintainer must register that package/signer in the same Cloud project as its Web OAuth client. Never send a private signing key. |
| Access blocked / app has not completed verification | Old Testing configuration, different client override or account policy | The published configuration is External/In production. Remove an unintended optional client override and use the published APK. Workspace/Advanced Protection policies can still block access; use the account administrator's permitted approach. Do not add friends as testers as a substitute for fixing the published configuration. |
| Google displays a warning or unfamiliar app name | Custom branding is not verified/shown | Production audience access and branding verification are different. Check the intended app, requested email/app-data scope and public privacy page; authorize only if comfortable. The app does not require broad access to ordinary Drive files. |
| Login works but backup is disconnected | Login does not grant Drive app-data permission | Settings → Connect / restore Drive or Reconnect Google Drive, select the same account and finish authorization. Wait for the real result. |
| Name, DOB or keys missing after reinstall | Wrong Google account, no successful backup, paused/disconnected backup or local-only data | Use the original account and check Drive before entering a replacement profile. On the old device, Back up now and wait for success if it is available. Local-only data does not automatically belong to a new Google login. |
| Backup is hidden from Drive's file list | appDataFolder is app-specific hidden storage | Use MigraineGuard's save/restore status. Ordinary My Drive is not where this snapshot appears. |
| Connection is checked but recent changes are missing | Connection verification is separate from completed save | On the editing device, Back up now and wait for the save timestamp. Then reconnect/restore on the receiving device. Prefer one editing device at a time. |
| 401 / authorization expired / reconnect required | Expired/revoked token or missing grant | Reconnect the selected account. Tokens are kept in memory; do not paste tokens into settings or issues. |
| 403 / permission denied | Account policy, insufficient scope, disabled service or wrong OAuth project | Retry the correct published client/account. Maintainer checks Drive API and narrow app-data scope; organization policy requires its administrator. Never request broad Drive access as a workaround. |
| Network/server/quota error | Connectivity or provider limits | Keep the local copy, retry later and verify a completed save before moving devices. Repeated retries do not establish that a save succeeded. |
| Backup validation/size error | Snapshot owner/content/format/size is invalid | Preserve the old snapshot and local copy. The importer rejects wrong owners, malformed contents and files over the limit. Do not change identity fields by hand to force another account's data in. |
| Deleted entries come back | Additive merge from an older snapshot | Pause backup, remove the intended cloud copy through Drive Manage apps, and handle local copies separately. Deletion tombstones are not implemented. Signing out alone does not erase data. |
| Another person's profile/keys appear | Unexpected account/restore identity or old build | Stop using or sending that account's AI keys. Confirm account email and version, preserve evidence privately and report immediately. Account-isolation tests cover synthetic cases; do not overwrite/delete records to conceal a repeatable issue. |

A Google console error shown to the maintainer is separate from a phone-user error. The active OAuth project is gen-lang-client-0426132502. The legacy migraineguard-66229 project is not used by the current sign-in flow.

## Steps, heart rate or sleep are missing or different

Follow these checks in order; a permission switch alone is insufficient.

1. **Are actual records present in Health Connect?** Inspect Data and access → the relevant type → actual dated entries and source. If records exist only on the watch or in Samsung Health, sync that path first. MigraineGuard cannot read data that was never exported to Health Connect.
2. **Is this app/account connected?** In Fitness, choose Connect this phone's health data for the current account. Android grants and Google login are separate from this ownership choice.
3. **Does the installed package have each read grant?** Check Steps, Heart rate and Sleep individually. Preview is not Standard. If you changed permissions, return to the app and refresh.
4. **What day/source is being compared?** Today's steps use the phone's local calendar day. Compare the same date/timezone and Samsung-only versus combined Health Connect. Never add those two overlapping totals. Manual steps do not prove health synchronization.
5. **Are the timestamps suitable?** Heart is the latest recorded sample across the recent read window, not live BPM. Future samples are excluded. Sleep uses completed sessions across a fourteen-day window; an older session is labeled. An unfinished night or absent source session may remain unavailable.
6. **Refresh and read the actual status.** Use Refresh steps now / Refresh data. Inspect selected-source total, Samsung shared today, combined aggregate and read timestamp. A raw recovery is deliberately labeled an estimate and avoids summing overlapping writers/intervals.
7. **If records still disagree, collect a redacted report.** Fitness → Connection & sync details → Copy sync report. Include installed version/package, selected source, permission status, record type, general date and error. Review the copied text before sharing privately; a public issue is not a safe place for medical/account data.

| Result | Meaning |
| --- | --- |
| — / unavailable / no count received | A usable reading was not returned. It does not mean you took no steps or had no heart activity. |
| Real zero | The read returned zero for that selected source/window. It still needs source/timestamp interpretation. |
| Samsung sharing unverified, permissions checked | Permissions exist but Samsung-origin records were not received in the checks. |
| Manual value shown | A user entry is active; it is not a Samsung reading. |
| Estimate labeled | Aggregate was unavailable/zero and bounded single-writer raw recovery was used. It may differ from source-app calculations. |
| SDK unavailable / update required | Health Connect provider/device support needs attention before record reads can succeed. |
| Permission/security error | Recheck that type's grant and account opt-in, then refresh. Do not repeatedly reinstall without preserving data. |

The previous 12–14-versus-180 report motivated full-day queries and Samsung bucket/raw handling. Emulator tests reproduce interval/query cases; matching this phone's current Samsung/watch totals still requires real-device testing. No multiplier or invented pulse/sleep value is used to hide missing data.

## AI, meals, weather and reminders

| Problem | What to check and do |
| --- | --- |
| Invalid key / 400 or 401 | Use a key from the intended provider, save it and test. A Firebase client API key or Google OAuth client ID is not a Gemini key. Rotate an exposed key at the provider. |
| AI 403, 404, 429 or timeout | Check provider project/API permissions, model availability, region, quota/billing and internet. Model access can change. Test selected provider is a small text request, not proof that image requests or all future calls work. |
| Offline answer despite a saved key | A cloud request may have failed and fallen through the configured chain. Inspect the displayed provider/status. Offline answers do not prove the cloud key worked. |
| Skipping setup still leaves a key | Expected: Skip verification preserves saved settings. Remove all keys in Settings to stop using those providers. |
| Food estimates blank or — | No usable analysis was returned. Add a clear description/portion and retry; do not interpret missing values as zero. |
| Selected photo not analyzed | Selecting a photo keeps it local. Explicitly enable photo analysis and submit; the selected Gemini/OpenAI/Claude key/model and network are required. The app does not silently send the image to another provider. |
| Restored food photo unavailable | Photo bytes are not included in account snapshots or encrypted journal exports. Old local URI references are not portable image storage. |
| Weather unavailable/stale | Allow optional approximate location, enable phone location services and retry with internet. Stale labels reflect old actual samples; a successful health connection does not grant location. |
| Reminders late/missing | Check the app toggles, Android notification permission/channel and Samsung background/battery policy. WorkManager is periodic; the current app does not provide exact medication-time alarms. |
| Encrypted export cannot restore | Use the correct password and supported file. Wrong password, tampering, malformed data or size limits cause rejection. There is no password-recovery backdoor. Keep the original file; do not overwrite it during diagnosis. |
| Large text or short screen hides a button | Scroll the current page/form. Setup controls are tested on a narrow emulator; report font/display scale and a redacted screenshot if a control remains inaccessible. |

## A useful bug report

Use this template in a [GitHub issue](https://github.com/jdp323/migraineguard/issues/new) after removing sensitive details:

- App version/code and Standard or Preview:
- Android version and phone/watch model:
- Screen and actions that reproduce it:
- Expected behavior:
- Actual behavior and redacted exact error:
- Relevant source/permission state, without medical records:
- Does it still happen after a refresh/reopen?

Do not attach keys, tokens, full sync reports, exact private health records, a Drive snapshot, journal export or signing keystore to a public issue. Use the developer contact displayed on Google's authorization screen for private follow-up.
