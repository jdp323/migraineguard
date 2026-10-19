# MigraineGuard public site

Static homepage, privacy, terms, support and 404 pages. No JavaScript, trackers, remote fonts, personal records, credentials, OAuth secrets or APKs. The public contact points to the previously approved support email on Google's consent screen rather than publishing an email in this repository.

## Files

- public/index.html: product overview and honest current device/service limitations.
- public/privacy.html: actual local/Health Connect/AI/Drive data flows, portable keys, consent and deletion limitations.
- public/terms.html: wellness/test-build use terms.
- public/support.html: setup, restore, health diagnostics and data controls.
- public/404.html: explicit missing page.
- public/styles.css: responsive system-font styling, keyboard focus and reduced-motion support.
- public/mark.svg: original code-native shield/pulse icon.
- firebase.json: public allowlist root and restrictive static-site response headers.
- .firebaserc: public Google project identifier; not a secret.

## Deploy

Use the official Firebase CLI signed into the project owner account. From this folder run: firebase deploy --only hosting --project gen-lang-client-0426132502. Do not use the repository root as the Hosting public directory. Keep Spark/free billing; no Analytics, Functions, Cloud Storage or App Hosting are needed. APK hosting is blocked on Spark and APKs are intentionally excluded.

Firebase activation has been approved. A live deployment and Google public OAuth publishing are separate steps; do not claim completion based on these files alone. Deploy only after inspecting the exact artifact. Google may require domain ownership or brand verification.
