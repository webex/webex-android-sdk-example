<!-- sdd-generated-metadata
doc_kind: standing-doc
generated_from: test-index@0.2.1
generated_by: claude-cli
approved_by: pending
updated_at: 2026-07-31T00:00:00Z
validation_status: not-run
-->
# Test Index — `kitchenSink`

> Start here → root [`AGENTS.md`](../AGENTS.md) (agent entry) · router [`SPEC_INDEX.md`](SPEC_INDEX.md) · system [`ARCHITECTURE.md`](ARCHITECTURE.md). This doc is the repo-wide map of the test surface.
> Context-efficiency: this is an INDEX, not a case list. It links to where cases live — it does not duplicate them.

## Test Surface
| Tier | Command (role) | Test directory | Framework | External deps |
|---|---|---|---|---|
| Unit | `./gradlew testFullDebugUnitTest` | `app/src/test/java` | JUnit4 | none |
| E2E / System | `./gradlew connectedFullDebugAndroidTest` | `app/src/androidTest/java` | AndroidX Test / Espresso | connected device/emulator |

> Note (assess-only): the repo ships example test scaffolding (`app/src/test/java/.../ExampleUnitTest.kt`); substantial coverage is not yet present. Instrumented UI tests use Espresso libraries declared in `app/build.gradle`.

## Where the Cases Live
- **Unit test cases** → each module's spec, "Test-Case Strategy (module)" section (see `SPEC_INDEX.md` for the module registry).
- **Instrumented / UI / system cases** → `app/src/androidTest/java`.

## Coverage / Quality Gate
- Minimum: `[NEEDS HUMAN INPUT]` · Measures: `[NEEDS HUMAN INPUT]` · Applies to: `[NEEDS HUMAN INPUT]` · Enforced in: `[NEEDS HUMAN INPUT]`.
- Evidence: `gradle.properties` sets `enableCodeCoverage=OFF`; no committed in-build coverage gate was found. A coverage/quality gate (if any) is unresolved and must be confirmed by the repo owner (`quality_gates.code_coverage.origin = unknown` in `.sdd/manifest.json`).

## QA Dependencies & Environments
- Connected device or emulator is required for instrumented tests. `gradle.properties` toggles (`enableSDKTests`, `enableReleaseTesting`, `enableReleaseUITesting`) are `OFF` by default. No external manual-QA tracker is referenced in the repo.

## Where to Go Next
- Agent entry: `../AGENTS.md` · System shape: `ARCHITECTURE.md` · Routing: `SPEC_INDEX.md`
- Machine source of truth: `.sdd/manifest.json` (`commands`, `tests`, `quality_gates`).
