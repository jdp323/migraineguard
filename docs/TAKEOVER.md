# Takeover and GitHub management

## Source of truth

The connected account is jdp323. GitHub repository jdp323/migraineguard is private; its main branch is the v1.6.0 app at fe1fb1672f835ef82fd5691e002934753340110d. The original Desktop folder was an older starter copy. The earlier v0.2.0 local implementation and its audit describe that starter, not GitHub v1.6. Never replace the newer app with that older snapshot.

The current working branch is codex/github-build-and-takeover, based on the exact remote commit. Native Git cannot reach GitHub from the tool process; the authenticated GitHub plugin provides repository read/write operations instead. Remote files and trees were imported with their original Git hashes verified. The native checkout currently has shallow history at that remote commit; the complete earlier remote history remains on GitHub. The local/takeover-snapshot branch and dist/MigraineGuard-git-takeover.bundle preserve the older local work.

## Local management

From PowerShell in the project folder:

~~~powershell
.\tools\git-manage.cmd Status
.\tools\git-manage.cmd Snapshot
.\tools\git-manage.cmd Backup
~~~

The helper is a command script because PowerShell script execution is disabled here. It scopes Git ownership trust to this exact project path. Backup verifies a Git bundle in the ignored dist folder; it includes committed files only. Snapshots use a local Codex author identity. Source ignores APKs, caches, local.properties, API secrets and signing keys.

Login, CheckAccess and Fetch in the helper are native Git operations. They need a working network connection and native Git credentials, which are separate from the authenticated GitHub plugin. The repository has an encrypted DPAPI credential-store fallback configured for GCM; no password or token is committed. Connector access is verified and does not establish that native Git authentication works.

## Phone builds

The Android test APK workflow runs unit tests, lint and assembly for pull requests and main/master pushes. On a successful run, download the MigraineGuard-debug-apk artifact from GitHub Actions, extract it and install app-debug.apk. Artifact downloads for this private repository require your GitHub account.

JDK 17 or 21 is required locally; this project's Gradle 8.11.1 is incompatible with the installed Studio Java 25 runtime. The workflow uses JDK 17 and installs dependencies on a fresh runner. Local builds have restricted network access, so CI provides the full dependency and lint check.

The CI debug signing key is not a stable distribution identity. Do not uninstall an existing app with important records just to install a differently signed test APK: export/recovery support and a stable private signing key need to be established first. Google Sign-In also requires registration of the APK signing certificate on the Android OAuth client; the existing baked web client ID alone does not prove a CI build can sign in. Offline/demo mode remains available.

## Current app and next checks

The GitHub baseline has Room journals, body metrics, medication records, user preferences, a setup wizard, Health Connect import, Google Credential Manager authentication, three cloud AI adapters plus an offline rules engine, weather, and a Drive placeholder. These implementations require independent review and device testing. The Drive toggle does not upload a backup, photo selection does not send an image to an AI model, and offline rules are not a trained diagnostic model.

Next priorities: remove fabricated nutrition fallback values; preserve cancellation and provider failures; protect backups containing API keys; replace destructive database upgrade behavior with tested migrations; check Android Health Connect permission handling on Samsung S26 Ultra/Watch Ultra; verify current provider models and consent before cloud analysis.
