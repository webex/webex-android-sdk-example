<!-- sdd-generated-metadata
doc_kind: standing-doc
generated_from: security@0.2.1
generated_by: claude-cli
approved_by: pending
updated_at: 2026-07-31T00:00:00Z
validation_status: not-run
-->
# Security Baseline — kitchenSink

> Start here → root [`AGENTS.md`](../AGENTS.md) (agent entry) · router [`SPEC_INDEX.md`](SPEC_INDEX.md) · system [`ARCHITECTURE.md`](ARCHITECTURE.md). Then this doc; module-specific security behavior lives in each owning module spec.
> Context-efficiency: link to canonical docs — don't duplicate them; load on demand, not upfront.

> Read before changing anything that touches input, identity, data, or external calls. This documents what the repo enforces today; gaps are marked `[NEEDS HUMAN INPUT]`.

## Trust Boundaries
| Boundary | Untrusted side | Trusted side | What is enforced at the crossing |
|---|---|---|---|
| Login → Webex identity | end user | Webex cloud (via SDK) | OAuth/JWT/Access-Token authentication through SDK `Authenticator` (`auth/LoginActivity.kt`) |
| App ↔ Webex cloud | network | app process | Handled by the Webex SDK; app does not implement its own transport security |
| Build-time config | developer machine | app binary | Secrets injected from `local.properties`/`gradle.properties` into `BuildConfig`, not committed (`app/build.gradle`) |

## Authentication & Authorization Model
- **Authentication:** OAuth (web), JWT, and Access-Token flows selected at login; a UC/CUCM login path exists. Identity is established through the Webex SDK `Authenticator`; `WebexRepository` implements `WebexAuthDelegate` and handles re-login/login-failure callbacks (`auth/LoginActivity.kt`, `WebexRepository.kt`).
- **Authorization:** Delegated to Webex cloud; the requested OAuth `SCOPE` is configured in `gradle.properties` and injected via `BuildConfig.SCOPE` (`app/build.gradle`, `gradle.properties`).
- **Default posture:** FedRAMP restrictions, when present, are enforced via `AppConfiguration.containsFedRampRestrictions()` / `SettingsStore.isFedRAMPEmployee()` (`auth/LoginActivity.kt`).

## Secret & Credential Handling
- Secrets source: `local.properties` (`CLIENT_ID`, `CLIENT_SECRET`, `REDIRECT_URI`, `FEDRAMP_CLIENT_ID/SECRET/REDIRECT_URI`, `WEBHOOK_URL`) and `gradle.properties` (`SCOPE`); read into `BuildConfig` at build time (`app/build.gradle`).
- Injection: the running code reads `BuildConfig.*` constants; values default to empty strings when `local.properties` is absent (`app/build.gradle`).
- Rotation: `[NEEDS HUMAN INPUT]`.
- **Hard rule:** never commit secrets, tokens, keys, or connection strings; never log them.

## Data Classification & Handling
| Data class | Examples | Storage rule | Logging rule | In transit |
|---|---|---|---|---|
| OAuth credentials | `CLIENT_ID`, `CLIENT_SECRET`, `REDIRECT_URI` | Build-time only, never committed | Never log | Handled by SDK/HTTPS |
| User email / login prefs | email, login type | Android `SharedPreferences` (device-local) (`utils/SharedPrefUtils.kt`) | Avoid logging PII | N/A (local) |
| Webex user/space/message content | SDK-fetched data | In-memory only; no app-owned datastore | Avoid logging content | Handled by SDK |

## Input Validation & Output Encoding Posture
- User inputs (e.g. login email dialog, dialer input) are passed to the SDK; validate at UI boundaries where the SDK does not (`auth/LoginActivity.kt`). No SQL/command construction exists in the app (no owned datastore).

<!-- Include if: the repo handles sessions or cookies -->
## Session & Cookie Posture
- Login type and email are persisted in Android `SharedPreferences` (`utils/SharedPrefUtils.kt`). Session/token lifecycle is owned by the Webex SDK; `WebexRepository.onReLoginRequired` signals re-authentication (`WebexRepository.kt`). Cookie/token storage internals are `[NEEDS HUMAN INPUT]` (SDK-owned).

<!-- Include if: the repo has known security-sensitive areas or accepted risks -->
## Known Sensitive Areas & Accepted Risks
| Area | Risk | Mitigation / why accepted | Owner |
|---|---|---|---|
| Sample credentials in `local.properties` | Leaked OAuth client secret if committed | `local.properties` is not committed; defaults empty | `[NEEDS HUMAN INPUT]` |
| Demo/sample nature | Not hardened for production use | This is an SDK demonstration app, not a shipping product (`README.md`) | `[NEEDS HUMAN INPUT]` |

## Reporting & Review
- Security-relevant changes require `[NEEDS HUMAN INPUT]` review path. Suspected vulnerabilities: `[NEEDS HUMAN INPUT]`.
- Cross-reference: module-specific security behavior lives in the owning module spec (notably `auth/`).
