# Public Google account access and Firebase website

## Problem

Google OAuth is External but in Testing. Signing in with basic name/email can work for an unlisted account, while adding Drive app-data permission is blocked for that same account. Adding a few test users does not satisfy open sharing with unknown friends.

## Verified configuration

- Project: gen-lang-client-0426132502 (Default Gemini Project). The older inaccessible project is not used.
- Drive API enabled; declared scopes limited to drive.appdata and userinfo.email, both non-sensitive. No broad Drive, Gmail or Google Fit scopes.
- Existing Android clients use the retained FB:67 signer for Standard/Preview. No new credentials are required for this website.
- Google disabled Publish app while public homepage/privacy links were missing.
- User selected free Firebase Hosting and explicitly approved Firebase terms. Firebase was added to the existing project on 2026-10-19; the console showed Spark, no-cost $0/month. Google Analytics remained disabled.

## Static website

See [website/README.md](../website/README.md) for every file. Public assets are isolated in website/public; only that directory is uploaded. Policy text follows the v1.7.0 code, including local account isolation, portable AI keys inside Drive JSON, no separate cloud password encryption, provider fallback, optional health-context/photo sharing and deletion/merge limitations. No personal email, health records, keys or OAuth secrets are included.

## Deployment and publishing status

The website is prepared and locally previewed. Cloud Shell is waiting for an explicit credential-authorization approval. It requests permission to make Google Cloud API calls with the signed-in account, including future calls. No live deployment, domain verification or public OAuth switch is claimed yet.

Next: deploy Hosting only, verify the public pages, add exact URLs/owned authorized domain in Branding, then prepare Google Audience's Production publishing confirmation. Complete any domain/brand verification Google actually requires. Moving to Production does not itself mean Google has verified the brand. Each friend must still grant their own app-data permission; it does not make any user's backup public.

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
