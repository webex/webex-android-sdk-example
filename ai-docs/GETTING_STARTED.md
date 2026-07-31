<!-- sdd-generated-metadata
doc_kind: standing-doc
generated_from: getting-started@0.2.1
generated_by: claude-cli
approved_by: pending
updated_at: 2026-07-31T00:00:00Z
validation_status: not-run
-->
# Getting Started — kitchenSink

> Start here → root [`AGENTS.md`](../AGENTS.md) (agent entry) · router [`SPEC_INDEX.md`](SPEC_INDEX.md) · system [`ARCHITECTURE.md`](ARCHITECTURE.md). Then this doc to get a build/test loop running.
> Context-efficiency: link to canonical docs — don't duplicate them; load on demand, not upfront.

## Prerequisites
- JDK 17, Kotlin 2.1.20, Android SDK (compileSdk 34, minSdk 28), Android Gradle Plugin 8.7.3, NDK `27.2.12479018` (`buildSrc/.../Dependencies.kt`, `build.gradle`). Android Studio is the expected IDE (`README.md`).
- Access to the Webex SDK Maven registry `https://devhub.cisco.com/artifactory/webexsdk/` (or the `WebexSDK.aar` in `libs/`/`aars/`) to resolve `com.ciscowebex:webexsdk` (`build.gradle`, `README.md`).
- A `google-services.json` for Firebase (referenced by the Google Services plugin, `app/build.gradle`).

## Clone & Install
```bash
git clone <this-repo-url>
cd kitchenSink
# Open in Android Studio and let Gradle sync, or resolve from CLI:
./gradlew help
```

## Build / Run / Test
| Task | Command |
|---|---|
| Build | `./gradlew assembleFullDebug` |
| Run (local) | Install/run the `full` debug variant from Android Studio onto a device/emulator |
| Test | `./gradlew testFullDebugUnitTest` (unit); `./gradlew connectedFullDebugAndroidTest` (instrumented) |
| Lint / format | `./gradlew lintFullDebug` |

> Flavors: `full`, `meeting`, `wxc`, `message` each pull a different Webex SDK artifact (`app/build.gradle`). Substitute the flavor name in the Gradle task (e.g. `assembleMeetingDebug`).

## First-Run Verification
- After `assembleFullDebug` succeeds, launch the app; the login screen (`LoginActivity`) appears with OAuth/JWT/Access-Token options (`auth/LoginActivity.kt`). Completing OAuth requires valid credentials in `local.properties`.

<!-- Include if: the repo needs local config / env vars / secrets to run -->
## Configuration & Secrets
- In `gradle.properties`, set `SCOPE` (a default set of scopes is present).
- In `local.properties` (not committed), set: `CLIENT_ID`, `CLIENT_SECRET`, `REDIRECT_URI`, `WEBHOOK_URL`, and optionally `FEDRAMP_CLIENT_ID`/`FEDRAMP_CLIENT_SECRET`/`FEDRAMP_REDIRECT_URI` (`README.md`, `app/build.gradle`). Never hardcode these — see `SECURITY.md`.

## Where to Go Next
- Agent entry: `../AGENTS.md` · System shape: `ARCHITECTURE.md` · Routing: `SPEC_INDEX.md`
- Conventions: `patterns/` + `rules/` (and `RULES.md`).
