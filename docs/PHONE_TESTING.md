> Current v1.7.0: [Drive account restore, Samsung query fixes, UI and complete changed-file/function supplement](UPDATE_1_7_0.md). This supersedes the older cloud-backup placeholder and current-version descriptions below; historical source references remain pinned to their stated baselines.

## v1.7.0 Drive restore and Samsung checks

Install the Standard v1.7.0 APK over the same signed installation. Keep Standard and Preview distinct. Sign in to the Google account containing your details and allow the native private Drive app-data prompt. Save any missing details once, then use Settings → Back up now and wait for the completed result before switching accounts. Other accounts must use their own profile/key/journal. Returning to the original account should restore birthday, measurements and keys without repeating setup. A fresh phone can restore only a previously completed backup for that account/application.

Google Auth remains in Testing mode; accounts must be allowed by that configuration. The Drive API and app-data/email scope declarations are enabled in the configured project. This does not replace the native permission grant on each user's phone. Google protects the portable cloud keys in transit/at rest; this is not a separately password-encrypted end-to-end backup. Local keys use Android Keystore.

Health Connect records are phone-specific. After restoring, verify this installed app's permissions. Fitness defaults to Samsung Health, offers a combined source, and labels a raw-record estimate if aggregates are zero despite records. BPM is a dated sample, not a live stream. If actual Samsung records remain missing, use Connection & sync details → Copy sync report and share the local diagnostic report, which omits profile/API-key information. See [implementation and known limits](UPDATE_1_7_0.md).

## v1.6.6 account and health checks (retained background)

Update the same installed package. Sign in with account A, set its profile/key, sign out and select account B: B must start with its own profile and no A keys/logs. Return to A to verify its records remain. Older pre-isolation records are preserved under **Continue locally**; they are not automatically assigned to Google accounts.

Each account must connect this phone’s Health Connect data in Fitness. Grant the installed Standard/Preview app READ access, and Samsung Health WRITE access, for Steps, Heart rate and Sleep. Open Samsung Health and sync the watch, then refresh. Check the new package/read-permission diagnostics and dated heart/sleep readings. See [full implementation and troubleshooting](UPDATE_1_6_6.md).

# Phone testing and persistent signing

## Current Google sign-in setup (v1.6.4)

Google Auth is configured in the accessible existing project gen-lang-client-0426132502. Standard and Preview are both registered with the retained FB:67 signing certificate, and the APK uses their matching Web audience. Testing mode is retained for the approved Google account. Install the new APK and use that account; do not create a new API key for Google login. Old shipped Web-client overrides migrate automatically while custom settings are preserved.

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
