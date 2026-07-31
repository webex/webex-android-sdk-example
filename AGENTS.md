<!-- sdd-generated-metadata
doc_kind: agent-entry
generated_from: agents@0.2.1
generated_by: claude-cli
approved_by: pending
updated_at: 2026-07-31T00:00:00Z
validation_status: not-run
-->
# AGENTS.md — kitchenSink

> You are the agent entry point — read first. Next: router [`SPEC_INDEX.md`](ai-docs/SPEC_INDEX.md) · system [`ARCHITECTURE.md`](ai-docs/ARCHITECTURE.md). Load this + `SPEC_INDEX.md` first; pull module/standing docs on demand.
> Context-efficiency: link to canonical docs — don't duplicate them; keep this file compact.

> Cross-tool context file. Auto-loaded by AI coding agents. A module's high-level design lives in the manifest-routed module spec, source-local as `<module-path>/ai-docs/<module-name>-spec.md`, not in an `AGENTS.md`.

> **Draft (assess-only onboarding).** This doc tree was generated in assess-only mode. Code is the source of truth; every module is `Untracked` in `.sdd/manifest.json`. Confirmations marked `[NEEDS HUMAN INPUT]` are not yet answered.

## Repo Overview
**kitchenSink** is a Cisco Webex Android SDK demonstration app (a "kitchen sink" sample) that exercises Webex SDK APIs — authentication, calling/meetings, messaging, people, search, and webhooks — from one Android application (`README.md`, `settings.gradle`).

**What it is:**
- A single-module Android app (`:app`) that integrates the Webex Android SDK and shows how to call its APIs (`app/build.gradle`, `README.md`).
- A developer reference whose UI screens map onto SDK capabilities (`app/src/main/AndroidManifest.xml`).

**What it is NOT:**
- ❌ The Webex Android SDK library itself — that is an external Maven dependency `com.ciscowebex:webexsdk` resolved from the Webex Artifactory registry (`app/build.gradle`).
- ❌ A published/consumed package — this app is not itself published to a registry (`app/build.gradle` has no publish config).
- ❌ A backend service — it owns no server, datastore, or HTTP API of its own.

## Tech Stack
- Kotlin 2.1.20, Java 17 target, Android (compileSdk 34, minSdk 28), Android Gradle Plugin 8.7.3 (`buildSrc/src/main/java/com/ciscowebex/androidsdk/build/Dependencies.kt`, `app/build.gradle`).
- Koin for dependency injection; AndroidX (AppCompat, DataBinding, RecyclerView, ViewPager2, Lifecycle); RxJava2; Firebase (Messaging, Analytics, Crashlytics); Glide; Gson (`app/build.gradle`).
- Tests: JUnit4 unit tests under `app/src/test/java`; AndroidX Test / Espresso instrumented tests under `app/src/androidTest/java` (`app/build.gradle`).

## Architecture
```
Android UI (Activities/Fragments)
  → ViewModels (WebexViewModel + per-feature ViewModels)
    → Repositories (WebexRepository + per-feature repositories)
      → Cisco Webex Android SDK (external: Webex, Phone, Message, Space, People…)
```
→ Full repo architecture & component responsibilities: **[ARCHITECTURE.md](./ai-docs/ARCHITECTURE.md)**

## Module / Package Structure
```
app/src/main/java/com/ciscowebex/androidsdk/kitchensink/
├── (core)         # app shell: KitchenSinkApp, WebexRepository, WebexViewModel, DI wiring, services
├── auth/          # login flows: OAuth, JWT, Access-Token, UC/CUCM
├── calling/       # calls, meetings, closed captions, call UI/services
├── messaging/     # spaces, teams, memberships, message composer
├── person/        # current-user / person details
├── search/        # people/space search
├── webhooks/      # webhook management UI
├── extras/        # miscellaneous SDK feature demos
└── utils/         # shared helpers (SharedPrefUtils, CallObjectStorage, Constants)
```
→ Per-module docs and the spec router: **[ai-docs/SPEC_INDEX.md](./ai-docs/SPEC_INDEX.md)**

## Critical Rules
1. **Code is the source of truth.** Every module is `Untracked` in `.sdd/manifest.json`; never invent an SDK API, path, event, or constant — read the real file or the Webex SDK reference.
2. **Ask before coding.** Present a plan / Spec Summary; wait for confirmation.
3. **Never commit secrets.** `CLIENT_ID`, `CLIENT_SECRET`, `REDIRECT_URI`, `WEBHOOK_URL`, and `FEDRAMP_*` come from `local.properties`; `SCOPE` from `gradle.properties`. These are read into `BuildConfig` (`app/build.gradle`) and must never be hardcoded or logged.
4. **One SDK flavor at a time.** The four product flavors (`full`, `meeting`, `wxc`, `message`) each pull a different Webex SDK artifact; only one is active per build (`app/build.gradle`).

## Essential Commands
| Task | Command |
|---|---|
| Install | Import into Android Studio / resolve Gradle; SDK artifacts require access to `devhub.cisco.com/artifactory/webexsdk` (`build.gradle`) |
| Build | `./gradlew assembleFullDebug` (`app/build.gradle`) |
| Test | `./gradlew testFullDebugUnitTest` (unit); `./gradlew connectedFullDebugAndroidTest` (instrumented) (`app/build.gradle`) |
| Lint/format | `./gradlew lintFullDebug` (`app/build.gradle`) |

## Common Gotchas
1. **Missing credentials → non-functional login.** If `local.properties` lacks `CLIENT_ID`/`CLIENT_SECRET`/`REDIRECT_URI`, OAuth login cannot complete; the values default to empty strings in `app/build.gradle`.
2. **Wrong flavor dependency.** `fullImplementation`, `meetingImplementation`, `wxcImplementation`, and `messageImplementation` are flavor-scoped; building a flavor without its SDK artifact available fails to resolve (`app/build.gradle`).
3. **Koin module load order.** Feature Koin modules are loaded per login type in `KitchenSinkApp.loadKoinModules`; requesting a ViewModel before its module is loaded fails at runtime (`KitchenSinkApp.kt`).

## Pre-Commit Checklist
- [ ] Tests pass (`./gradlew testFullDebugUnitTest`)
- [ ] Spec/docs updated in the same change (spec-currency)
- [ ] No hardcoded secrets; credentials still sourced from `local.properties` / `gradle.properties`
- [ ] Affected module spec under `<module>/ai-docs/` updated when SDK usage changes

---
**SDD coverage:** this repo's per-module coverage state lives in `.sdd/manifest.json` (mirror in `ai-docs/SPEC_INDEX.md`). Use that state to decide whether the spec is authoritative or code must be cross-checked. All modules are currently `Untracked` (assess-only onboarding): code is authoritative.
