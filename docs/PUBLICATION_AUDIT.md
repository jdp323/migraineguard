# Public distribution and source review

Reviewed October 20, 2026, around application source 9a425749460ff052e23718eeec4c2af720ca7bb3 (v1.8.0). Documentation/unused-config cleanup commits can follow this source without changing the tested APK.

## Public availability

The GitHub API already reported visibility public when this continuation started. The release page was independently readable without a GitHub login. No visibility toggle or history rewrite was performed during this review.

- Share/download: https://github.com/jdp323/migraineguard/releases/latest
- Fixed release: https://github.com/jdp323/migraineguard/releases/tag/v1.8.0-debug
- Source/docs: https://github.com/jdp323/migraineguard
- Static information/privacy site: https://gen-lang-client-0426132502.web.app/

Source visibility does not publish a user's Drive snapshot, health records or account authorization. Each person grants their own optional app-data access. The website hosts static information and links to GitHub APK assets; APK files are not deployed to Firebase Spark Hosting.

## Checks performed

Fetched published remote branches and tags and scanned their reachable Git objects: **40 commits and 499 unique text blobs** at the start of review. The limited pattern scan checked Google API keys, GitHub/Groq/OpenRouter token forms, private-key headers and email-like strings, with redacted local results. Current filenames and build/workflow boundaries were inspected. This is not a complete security audit, entitlement review or guarantee that every possible credential format is absent.

No user medical database, Drive snapshot, personal AI provider key, GitHub token or private signing key was identified in that scan. Test-account strings are synthetic; published v1.8.0 screenshots come from emulator fixtures. Ignored local configuration, verification downloads, signing files and recovery APKs are outside the committed source.

The public release's Standard and Preview checksums were compared with the downloaded build artifacts, and both retained signing certificates were checked. The automated pipeline consumes its retained signing key from a GitHub Actions secret; it is not committed to the repository. Pull-request builds do not receive that secret or publish releases.

## Legacy configuration and history limitations

An unused app/google-services.json contained one legacy Firebase client API key repeated across two client records, old project identifiers and package configuration. The current Android build has no Google Services plugin/Firebase SDK dependency and uses explicitly configured OAuth IDs instead. The unused file is removed from the current source and ignored to avoid accidentally adding it again.

**Removal from the current tree does not remove historical copies or invalidate a key.** The original project owner should retire the unused key or confirm appropriate Firebase-only API/application restrictions and that it cannot call Gemini or other unrelated billable APIs. The original project's administration was not available in this session, so restrictions/revocation were not verified or changed. No key value is printed here.

Firebase client keys are public project identifiers under the correct restrictions, rather than database authorization secrets; Firebase Security Rules/IAM protect backend data. This distinction is documented by [Firebase](https://firebase.google.com/docs/projects/api-keys). It does not excuse publishing a private Gemini/provider credential or assuming an uninspected old project's rules/quotas are safe.

Existing Git history also includes author/committer metadata, including an account email address, and old package identifiers. History has **not** been rewritten or represented as containing no personal metadata. Removing that metadata from all branches/tags would change commit identities and invalidate the existing source references/release provenance. It is a separate history-migration decision; already copied public history cannot be recalled by a rewrite.

OAuth client IDs, Android package names, project IDs and public signing-certificate fingerprints intentionally remain visible because they are app configuration/verification identifiers, not private OAuth secrets or private signing material. Published debug-keystore password labels are not useful without the private keystore; that keystore remains a repository secret/local ignored file.

## Keep future publication clean

Keep private health data, sync reports, screenshots of actual accounts, tokens, API keys and backup files out of public issues/commits. Use synthetic fixtures for documentation. Rotate an exposed provider credential at its issuer; deleting its text from the latest commit does not make the credential private again. Use GitHub's secret-protection facilities where available and review uploads before making a release.

Removing the unused config was additionally checked by a successful local :app:assembleDebug run. No production source or OAuth registration was changed by that cleanup.
