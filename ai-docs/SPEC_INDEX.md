<!-- sdd-generated-metadata
doc_kind: standing-doc
generated_from: spec-index@0.2.1
generated_by: claude-cli
approved_by: pending
updated_at: 2026-07-31T00:00:00Z
validation_status: not-run
-->
# Spec Index — kitchenSink

> Start here → root [`AGENTS.md`](../AGENTS.md) (agent entry). This file is the router (generated at `ai-docs/SPEC_INDEX.md`); system overview in [`ARCHITECTURE.md`](ARCHITECTURE.md). Load `AGENTS.md` + this file first; pull every other doc on demand.
> Context-efficiency: link to canonical docs — don't duplicate them; route to the minimum needed per task.

> AI agent entry point after `AGENTS.md`. Load this once at session start; pull other docs on demand.
> **Source of truth:** `.sdd/manifest.json` (this file mirrors it for humans).

## Module Registry
| Module | Responsibility | Manifest coverage state | Start here |
|---|---|---|---|
| `app/src/main/java/com/ciscowebex/androidsdk/kitchensink/` (core) | App shell, Koin wiring, shared `WebexRepository`/`WebexViewModel`, services | Untracked | `app/src/main/java/com/ciscowebex/androidsdk/kitchensink/ai-docs/core-spec.md` |
| `app/src/main/java/com/ciscowebex/androidsdk/kitchensink/auth/` | Login flows (OAuth, JWT, Access Token, UC/CUCM) and login-type persistence | Untracked | `app/src/main/java/com/ciscowebex/androidsdk/kitchensink/auth/ai-docs/auth-spec.md` |
| `app/src/main/java/com/ciscowebex/androidsdk/kitchensink/calling/` | Calls, meetings, closed captions, in-call UI and services | Untracked | `app/src/main/java/com/ciscowebex/androidsdk/kitchensink/calling/ai-docs/calling-spec.md` |
| `app/src/main/java/com/ciscowebex/androidsdk/kitchensink/messaging/` | Spaces, teams, memberships, message composer | Untracked | `app/src/main/java/com/ciscowebex/androidsdk/kitchensink/messaging/ai-docs/messaging-spec.md` |
| `app/src/main/java/com/ciscowebex/androidsdk/kitchensink/person/` | Current-user / person detail retrieval | Untracked | `app/src/main/java/com/ciscowebex/androidsdk/kitchensink/person/ai-docs/person-spec.md` |
| `app/src/main/java/com/ciscowebex/androidsdk/kitchensink/search/` | People/space search | Untracked | `app/src/main/java/com/ciscowebex/androidsdk/kitchensink/search/ai-docs/search-spec.md` |
| `app/src/main/java/com/ciscowebex/androidsdk/kitchensink/webhooks/` | Webhook management UI | Untracked | `app/src/main/java/com/ciscowebex/androidsdk/kitchensink/webhooks/ai-docs/webhooks-spec.md` |
| `app/src/main/java/com/ciscowebex/androidsdk/kitchensink/extras/` | Miscellaneous SDK feature demos | Untracked | `app/src/main/java/com/ciscowebex/androidsdk/kitchensink/extras/ai-docs/extras-spec.md` |

## Task Routing
| If the task is… | Load |
|---|---|
| Understanding the system | `ARCHITECTURE.md` |
| Working in a feature area | that module's spec under `<module-path>/ai-docs/<module-name>-spec.md` |
| A change to SDK usage / public surface | the owning module spec + `CONTRACTS.md` |
| Running or changing tests | `TEST_INDEX.md` + the affected module spec |
| Updating docs after a code change | affected module specs + relevant standing indexes/contracts |

## Incident History
| INC id | Date | Module | One-line | Link |
|---|---|---|---|---|
| N/A | — | — | No incident history recorded during assess-only onboarding. | — |

## Spec Registry
| Doc | Location | Purpose |
|---|---|---|
| Patterns | `patterns/` | repo conventions, correct vs incorrect |
| Rules | `RULES.md` + `rules/` | enforceable do/don't beyond AGENTS.md critical rules |
| Glossary | `GLOSSARY.md` | ubiquitous language: term → definition → code location |
| Security | `SECURITY.md` | trust boundaries, authn/authz, secret handling, data classification |
| Contracts | `CONTRACTS.md` | root index of public-surface contracts; details live at owning modules or native contract sources |
| Service state | `SERVICE_STATE.md` | living as-built registry — read first to avoid duplicate/breaking surfaces |
| Test index | `TEST_INDEX.md` | test tiers, canonical commands, locations, frameworks, dependencies, and quality gates |
| Getting started | `GETTING_STARTED.md` | clone/build/run + configuration |
| Decision records | `adr/` | standing ADRs — why the architecture is the way it is |
| Review catalog | `REVIEW_CHECKLIST.md` | the 6-core + 4-coverage + 3-cross-cutting review checks |
