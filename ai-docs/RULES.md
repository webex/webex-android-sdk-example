<!-- sdd-generated-metadata
doc_kind: standing-doc
generated_from: rules@0.2.1
generated_by: claude-cli
approved_by: pending
updated_at: 2026-07-31T00:00:00Z
validation_status: not-run
-->
# Rules — kitchenSink

> Start here → root [`AGENTS.md`](../AGENTS.md) (agent entry, carries the critical rules) · router [`SPEC_INDEX.md`](SPEC_INDEX.md) · system [`ARCHITECTURE.md`](ARCHITECTURE.md). Then this doc; per-language detail in `rules/<language>/`.
> Context-efficiency: link to canonical docs — don't duplicate them; load on demand, not upfront.

> These rules are extracted from this repo's real conventions (file path). Assess-only onboarding: they surface current practice; they do not add new gates.

## Coverage Map (which docs/specs to trust)
| Module | Manifest coverage state | What it means here |
|---|---|---|
| `kitchensink/` (core) | Untracked | Code is source of truth; cross-check every claim against source. |
| `auth/` | Untracked | Code is source of truth; cross-check against source. |
| `calling/` | Untracked | Code is source of truth; cross-check against source. |
| `messaging/` | Untracked | Code is source of truth; cross-check against source. |
| `person/` | Untracked | Code is source of truth; cross-check against source. |
| `search/` | Untracked | Code is source of truth; cross-check against source. |
| `webhooks/` | Untracked | Code is source of truth; cross-check against source. |
| `extras/` | Untracked | Code is source of truth; cross-check against source. |

## Autonomy & Ask-First
- **May proceed:** low-risk UI/demo tweaks that do not change SDK usage, credentials handling, or build flavors.
- **Ask first / plan + confirm:** changes to authentication flows, credential/`BuildConfig` handling, Koin module wiring, product flavors, or dependency versions.
- **Never without explicit human approval:** committing credentials, changing `SCOPE`/`WEBHOOK_URL` defaults, or publishing artifacts.

## Naming
- Kotlin packages are feature-scoped under `com.ciscowebex.androidsdk.kitchensink.<feature>` (e.g. `auth`, `calling`, `messaging`) (`app/src/main/java/...`).
- Koin modules are named `<feature>Module` (e.g. `messagingModule`, `callModule`, `personModule`) (`MessagingModule.kt`, `CallModule.kt`, `PersonModule.kt`).
- MVVM roles use suffixes `Activity`, `ViewModel`, `Repository` (e.g. `LoginActivity`, `WebexViewModel`, `WebexRepository`).
- SDK event enums live on `WebexRepository` (e.g. `CallEvent`, `MessageEvent`, `SpaceEvent`) (`WebexRepository.kt`).

## Logging
- Use Android `Log` with a per-class `tag` constant (e.g. `private val tag = "WebexRepository"`) (`WebexRepository.kt`).
- Never log credentials, tokens, or `BuildConfig` secret fields (see `SECURITY.md`).

## Error Handling
- SDK calls use the callback idiom `CompletionHandler<T>`; results carry `isSuccessful` and errors (`WebexRepository.kt`).
- UI-facing failures are surfaced via `LiveData` streams (e.g. `_ucLiveData`, `_virtualBgError`) and `Toast`/dialog in activities (`WebexViewModel.kt`, `calling/CallActivity.kt`).
- Wrap risky SDK init in try/catch and log, as in `buildCrashEnabledWebex` (`WebexModule.kt`); do not swallow silently in new code.

## Imports / Dependencies
- Dependency versions are centralized in `buildSrc/.../Dependencies.kt` (the `Versions`/`Dependencies` objects); reference these rather than hardcoding versions in `app/build.gradle`.
- SDK dependencies are flavor-scoped (`fullImplementation`, `meetingImplementation`, `wxcImplementation`, `messageImplementation`); only one is active per flavor (`app/build.gradle`).

## Testing
- Unit tests: JUnit4 under `app/src/test/java`; instrumented tests: Espresso under `app/src/androidTest/java` (`app/build.gradle`).
- `[NEEDS HUMAN INPUT]` — no enforced coverage bar found; `enableCodeCoverage=OFF` in `gradle.properties`.

## Security
- Credentials (`CLIENT_ID`, `CLIENT_SECRET`, `REDIRECT_URI`, `FEDRAMP_*`, `WEBHOOK_URL`) come from `local.properties`; `SCOPE` from `gradle.properties`; both are injected into `BuildConfig` (`app/build.gradle`). Never hardcode or log them. See `SECURITY.md`.

## Spec-Currency & Drift Thresholds
- Update the affected module spec/docs in the SAME change as the code (spec-currency).
- All modules are `Untracked`; treat code as authoritative until a coverage assessment is run.

## Secrets Policy
- No hardcoded secrets/tokens/keys/connection strings — ever. Source from `local.properties`/`gradle.properties` at build time; never log them.

<!-- Include if: the repo is concurrent/async/reactive -->
## Concurrency & Async
- The app is reactive/event-driven: SDK observer callbacks are re-published through `LiveData` and RxJava2 is available (`WebexRepository.kt`, `Dependencies.kt`).
- `CallObjectStorage` guards its mutable list with `synchronized` blocks; `setCallObserver` is `@Synchronized` — preserve these guards when touching the call registry (`utils/CallObjectStorage.kt`, `WebexRepository.kt`).

## Maintenance
- Add a rule when a review correction recurs; remove it when a lint rule starts enforcing it.
- Cross-reference: patterns → `patterns/`; per-language → `rules/<language>/`.
