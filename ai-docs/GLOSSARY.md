<!-- sdd-generated-metadata
doc_kind: standing-doc
generated_from: glossary@0.2.1
generated_by: claude-cli
approved_by: pending
updated_at: 2026-07-31T00:00:00Z
validation_status: not-run
-->
# Glossary — kitchenSink

> Start here → root [`AGENTS.md`](../AGENTS.md) (agent entry) · router [`SPEC_INDEX.md`](SPEC_INDEX.md) · system [`ARCHITECTURE.md`](ARCHITECTURE.md). Then this doc; related: `CONTRACTS.md`.
> Context-efficiency: link to canonical docs — don't duplicate them; load on demand, not upfront.

> Read this before naming anything. Use the canonical name exactly; never introduce a synonym.

## Domain Terms
| Term | Definition (one or two sentences) | Authoritative location (file/type) | Notes / synonyms to avoid |
|---|---|---|---|
| `Webex` | The external Webex SDK entry object the app builds and uses for all SDK operations. | `WebexModule.kt` (`buildCrashEnabledWebex`) | not "the app"; it is the SDK client |
| `WebexRepository` | Shared repository that binds SDK observers/delegates and republishes results as `LiveData`. | `WebexRepository.kt` | not "the model" |
| `WebexViewModel` | Central ViewModel exposing SDK operations and `LiveData` streams to screens. | `WebexViewModel.kt` | — |
| `KitchenSinkApp` | The `Application` subclass; initializes Firebase and Koin and loads feature modules per login type. | `KitchenSinkApp.kt` | not "MainActivity" |
| `LoginType` | Enum of supported authentication modes: OAuth, JWT, AccessToken. | `auth/LoginActivity.kt` | — |
| `CallObjectStorage` | In-memory synchronized registry of active SDK `Call` objects keyed by call id. | `utils/CallObjectStorage.kt` | not "call cache" |
| `Space` / `Team` / `Membership` | Webex messaging domain objects surfaced by the SDK and shown in the messaging feature. | `messaging/` package; SDK types | — |
| `<feature>Module` | A Koin DI module registering a feature's ViewModels/Repositories. | e.g. `MessagingModule.kt`, `CallModule.kt` | not "Gradle module" |

## Abbreviations & Acronyms
| Abbreviation | Expansion | Meaning in this repo |
|---|---|---|
| SDK | Software Development Kit | The Cisco Webex Android SDK this app demonstrates |
| DI | Dependency Injection | Provided by Koin (`WebexModule.kt`) |
| UC / CUCM | Unified Communications / Cisco Unified Communications Manager | On-prem calling login path (`auth/`, `AndroidManifest.xml`) |
| MVVM | Model-View-ViewModel | The UI layering pattern (Activity → ViewModel → Repository) |
| ABI | Application Binary Interface | APK split dimension in `app/build.gradle` |
| FCM | Firebase Cloud Messaging | Push messaging service (`KitchenSinkFCMService`) |

## Maintenance
- When a new domain concept is introduced (new SDK feature area, event, state), add it here in the same change.
- Cross-reference: public-surface terms → `CONTRACTS.md`.
