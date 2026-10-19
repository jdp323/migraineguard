# Public Google account access and Firebase website

## Problem

The previous Google OAuth configuration was External and in Testing. Signing in with basic name/email can work for an unlisted account, while adding Drive app-data permission is blocked for that same account. Adding a few test users does not satisfy open sharing with unknown friends.

## Verified configuration

- Project: gen-lang-client-0426132502 (Default Gemini Project). The older inaccessible project is not used.
- Drive API enabled; declared scopes limited to drive.appdata and userinfo.email, both non-sensitive. No broad Drive, Gmail or Google Fit scopes.
- Existing Android clients use the retained FB:67 signer for Standard/Preview. No new credentials are required for this website.
- Google disabled Publish app while public homepage/privacy links were missing.
- User selected free Firebase Hosting and explicitly approved Firebase terms. Firebase was added to the existing project on 2026-10-19; the console showed Spark, no-cost $0/month. Google Analytics remained disabled.

## Static website

See [website/README.md](../website/README.md) for every file. Public assets are isolated in website/public; only that directory is uploaded. Policy text follows the v1.7.0 code, including local account isolation, portable AI keys inside Drive JSON, no separate cloud password encryption, provider fallback, optional health-context/photo sharing and deletion/merge limitations. No personal email, health records, keys or OAuth secrets are included.

## Deployment and publishing status

Completed on 2026-10-19 after explicit user approvals for Firebase terms, Cloud Shell credential authorization and Google public audience publishing.

- Live homepage: https://gen-lang-client-0426132502.web.app/
- Privacy: https://gen-lang-client-0426132502.web.app/privacy.html
- Terms: https://gen-lang-client-0426132502.web.app/terms.html
- Help: https://gen-lang-client-0426132502.web.app/support.html
- Official preinstalled Firebase CLI 15.32.0 deployed Hosting only. The console reported Spark/no-cost; Analytics remained disabled and no billing upgrade was performed.
- All seven public files were downloaded over HTTPS and matched the reviewed local bytes exactly. CSP, no-referrer, nosniff and camera/microphone/location denial headers are configured; live CSP/nosniff were verified. Internal links and successful/error HTTP routes were checked. Desktop homepage and privacy screenshots were inspected. Phone-width rendering has not been visually verified.
- Google Branding saved the exact homepage/privacy/terms URLs and the specific project web.app authorized domain. The existing support contact was retained; no personal email was copied into the site or repository.
- Google Audience visibly reports **In production**, External. Unknown friends no longer need the Testing allowlist.
- Verification Center says data-access verification is not required because no sensitive or restricted scopes are requested. It separately says the custom branding is not shown to users. This is Production audience access, not a claim that Google has verified the app name/brand. Domain ownership verification has not been claimed or submitted.

Each friend must grant their own app-data permission. Production does not publish backups or grant one user access to another account. Existing v1.7.0 APKs use this OAuth project; this cloud-side change requires no APK rebuild. Phone acceptance: retry Drive reconnect/authorization with a previously blocked account, complete a backup, then verify account-specific restore. Live friend-account authorization/transfer has not been performed from this VM. Google account administrator policies can still restrict individual accounts.

Private local proof is in verification/firebase (ignored by Git): in-production.png, verification-status.png, live-homepage.png, live-privacy.png and live-site-verification.json. Current public source and setup documentation are committed on codex/github-build-and-takeover.

## Distribution

The existing GitHub repository remains private. Friends without repository access cannot download private releases. Firebase Spark blocks APK hosting; do not rename/disguise APKs to bypass this or upgrade billing without user authorization. Distribution remains a separate task from Google OAuth audience publishing. The verified APK stays at the v1.7.0-debug release in the private repository.

## Primary references

- [Google Audience](https://support.google.com/cloud/answer/15549945?hl=en)
- [Production branding](https://developers.google.com/identity/protocols/oauth2/production-readiness/brand-verification)
- [Homepage requirements](https://support.google.com/cloud/answer/13807376?hl=en)
- [Drive scopes](https://developers.google.com/workspace/drive/api/guides/api-specific-auth)
- [Firebase Hosting quickstart](https://firebase.google.com/docs/hosting/quickstart)
- [Firebase pricing](https://firebase.google.com/pricing)
- [Spark executable-file restriction](https://firebase.google.com/docs/hosting/faq-and-troubleshooting)
