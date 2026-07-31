<!-- sdd-generated-metadata
doc_kind: standing-doc
generated_from: service-state@0.2.1
generated_by: claude-cli
approved_by: pending
updated_at: 2026-07-31T00:00:00Z
validation_status: not-run
-->
# Service State (living) — kitchenSink

> Start here → root [`AGENTS.md`](../AGENTS.md) (agent entry) · router [`SPEC_INDEX.md`](SPEC_INDEX.md) · system [`ARCHITECTURE.md`](ARCHITECTURE.md). Read this FIRST before adding a surface; stable contracts in `CONTRACTS.md`.
> Context-efficiency: link to canonical docs — don't duplicate them; load on demand, not upfront.

> Source of truth for "does X already exist?" This is a client Android app: it exposes no server endpoints and owns no datastore. The as-built surfaces below are the Android components and external integrations that exist now.

## Current Android Components (as-built)
<!-- Reality for this client app: the "surfaces" are registered Android components, not server endpoints. -->
| Component | Kind | Declared at |
|---|---|---|
| `LoginActivity` (LAUNCHER) | Activity | `AndroidManifest.xml` |
| `HomeActivity`, `SetupActivity`, `SetupCameraActivity` | Activity | `AndroidManifest.xml` |
| `auth.*` (JWT/AccessToken/OAuthWeb login), `cucm.UCLoginActivity` | Activity | `AndroidManifest.xml` |
| `calling.*` (Call, CucmCall, Dialer, LockScreen, closed captions, calendar meeting details) | Activity | `AndroidManifest.xml` |
| `messaging.*` (Messaging, search, teams/space detail, memberships, composer) | Activity | `AndroidManifest.xml` |
| `search.SearchActivity`, `webhooks.WebhooksActivity`, `extras.ExtrasActivity` | Activity | `AndroidManifest.xml` |
| `firebase.KitchenSinkFCMService` | Service (FCM) | `AndroidManifest.xml` |
| `KitchenSinkForegroundService`, `CallManagementService`, `CallRejectService` | Service | `AndroidManifest.xml` |
| `FileProvider` (`${applicationId}.provider`) | ContentProvider | `AndroidManifest.xml` |

<!-- Include if: the service publishes or consumes events -->
## Current Events
| Event / topic | Direction | Producer/consumer | Payload ref |
|---|---|---|---|
| Firebase `MESSAGING_EVENT` (push) | consume | `firebase.KitchenSinkFCMService` | `AndroidManifest.xml` |
| Webex SDK space/membership/message/calendar/call callbacks | consume | `WebexRepository` observers → `LiveData` | `WebexRepository.kt` |

## External Dependencies
| Dependency | Used for | Timeout / retry | Circuit breaker / fallback |
|---|---|---|---|
| Webex cloud (via SDK) | All Webex operations | Owned by SDK | Errors surfaced via `LiveData`/`Toast` |
| Firebase (FCM/Analytics/Crashlytics) | Push, analytics, crash reporting | Owned by Firebase SDK | App runs without push if unavailable |
| Configured `WEBHOOK_URL` | Webhooks demo target | `[NEEDS HUMAN INPUT]` | Empty default → feature inert |

## Feature Flags (current)
<!-- Reality for this client app: "flags" are build-time product flavors and BuildConfig-injected toggles, not a runtime flag service. -->
| Flag / toggle | Kind | Default / current state | Controls | Source |
|---|---|---|---|---|
| Product flavor (`full`, `meeting`, `wxc`, `message`) | Build-time flavor | `full` (canonical build/test commands target `full`) | Which Webex SDK artifact is linked and which SDK surface is available | `app/build.gradle` |
| `enableCodeCoverage` | Gradle property | `OFF` | Whether coverage instrumentation is applied at build time | `gradle.properties` |
| FedRAMP mode | Runtime toggle via `AppConfiguration` / `SettingsStore` | `[NEEDS HUMAN INPUT]` (persisted per device; no committed default confirmed) | Gates FedRAMP-specific login behavior | `auth/LoginActivity.kt` |
| `WEBHOOK_URL` | `BuildConfig` value from `local.properties` | Empty by default → webhooks demo inert | Target URL for the webhooks demo | `app/build.gradle` |

<!-- Include if: the service holds compliance certifications/obligations worth surfacing -->
## Compliance / Certifications
- FedRAMP mode is supported and gates login behavior via `AppConfiguration`/`SettingsStore` (`auth/LoginActivity.kt`); do not regress the FedRAMP toggle handling.

## Maintenance
- Update the relevant row in the same change that adds/changes/removes an Android component, external dependency, or event integration.
- Cross-reference: stable contracts → `CONTRACTS.md`; security posture → `SECURITY.md`.
