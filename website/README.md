# MigraineGuard public site

Static homepage, privacy, terms, support and 404 pages. No JavaScript, trackers, remote fonts, personal records, credentials, OAuth secrets or APKs. The public contact points to the previously approved support email on Google's consent screen rather than publishing an email in this repository.

## Files

- public/index.html: product overview, public GitHub release download links and honest device/service limitations.
- public/privacy.html: actual local/Health Connect/AI/Drive data flows, portable keys, consent and deletion limitations.
- public/terms.html: wellness/test-build use terms.
- public/support.html: current install/setup, links to complete public guides, restore, health diagnostics and data controls.
- public/404.html: explicit missing page.
- public/styles.css: responsive system-font styling, keyboard focus and reduced-motion support.
- public/mark.svg: original code-native shield/pulse icon.
- firebase.json: public allowlist root and restrictive static-site response headers.
- .firebaserc: public Google project identifier; not a secret.

## Deploy

Use the official Firebase CLI signed into the project owner account. From this folder run: firebase deploy --only hosting --project gen-lang-client-0426132502. Do not use the repository root as the Hosting public directory. Keep Spark/free billing; no Analytics, Functions, Cloud Storage or App Hosting are needed. APK hosting is blocked on Spark and APKs are intentionally excluded.

Live at https://gen-lang-client-0426132502.web.app/ (2026-10-19). All seven deployed files matched local bytes; desktop homepage/privacy and internal/HTTP routes were checked. Google Branding saved these policy URLs; Audience reports In production, External. Verification Center requires no data-access verification for the current non-sensitive scopes, while custom branding remains unverified/not shown. See ../docs/PUBLIC_GOOGLE_ACCESS.md for exact evidence, consent steps and remaining phone checks. No APK rebuild was needed.

## v1.8.0 public distribution update (2026-10-20)

Deployed the reviewed static files from source 6c7ca9dbcba573ca7b3f6488e5c892abda626906 using the existing authorized Cloud Shell and preinstalled Firebase CLI, Hosting only, with no billing/service expansion. All seven live files returned 200 and matched the local bytes; homepage and help navigation were visually checked. The homepage now links public GitHub APKs and the help page links the complete usage, architecture and troubleshooting guides. Both APK release assets and all four new guides were also downloaded without account credentials and matched expected bytes/hashes. Desktop visual proof and redacted verification JSON stay in ignored verification/public-audit.

## v1.9.1 public distribution update (2026-10-21)

Deployed reviewed static source 77f8e1032e6439ea96d1f38d932c450fe3fe3ea2 through the existing authorized Cloud Shell and preinstalled Firebase CLI, Hosting only. All seven live files returned 200 and matched local bytes, including CSP and nosniff headers. Homepage showed v1.9.1 and its setup/help link opened the current support page. A final support-link correction points release verification to the v1.9 guide and 72 JVM / 20 Android tests. No billing or service expansion; APKs remain on GitHub. Public screenshot and file hashes are retained in ignored verification/v1.9.1.

## v1.10.0 update (2026-10-21)

Deployed reviewed static source 0d6628c629f0b7d41961172517358522a1d8f752 via the existing authorized Cloud Shell and preinstalled Firebase CLI, Hosting only. All seven live files returned 200 and matched the local bytes; restrictive CSP remained present. Help/privacy now describe the remembered device-only identity, water quick actions and activity-specific metrics. APK distribution remains public GitHub, with no billing/service expansion. Exact file hashes and browser proof stay under ignored verification/v1.10.0.

## v1.11.1 update (2026-10-22)

Deployed reviewed static source d1a9352eec922180c8cfe0ba450d920c83f4697d through the existing authorized Cloud Shell and Firebase CLI, Hosting only. All seven live files returned 200 and matched the committed bytes; CSP and nosniff headers remained present. The help page was visually verified showing v1.11.1/code 20, swipe navigation, native Back and current public screenshot/documentation links. Exact hashes and browser proof remain in ignored verification/v1.11.1. No billing or service expansion; APK downloads remain on public GitHub Releases.

## v1.12.0 update (2026-10-24)

Deployed reviewed static source 70ca75152860ea994f17365c3cc83b0e3dba49b8 through the existing authorized Cloud Shell and Firebase CLI, Hosting only. All seven live files returned 200 and matched committed bytes; CSP and nosniff remained present. Visually verified the help page showing v1.12.0/code21, 81 JVM / 33 Android tests, movement timing and optional background-read limits, local body progress with separate Drive inclusion, manual export protection and multi-brand Health Connect steps. Privacy and homepage match those implemented flows. No billing/service expansion; APKs remain public GitHub releases. Exact hashes and browser proof are retained in ignored verification/v1.12.0.
