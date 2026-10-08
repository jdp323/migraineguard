# Phone testing and persistent signing

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
