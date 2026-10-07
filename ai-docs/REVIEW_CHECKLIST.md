<!-- sdd-generated-metadata
doc_kind: standing-doc
generated_from: review-checklist@0.2.1
generated_by: claude-cli
approved_by: pending
updated_at: 2026-07-31T00:00:00Z
validation_status: not-run
-->
# Review-Check Catalog — kitchenSink

> Start here → root [`AGENTS.md`](../AGENTS.md) (agent entry) · router [`SPEC_INDEX.md`](SPEC_INDEX.md) · system [`ARCHITECTURE.md`](ARCHITECTURE.md). Then this doc at Review & Merge.
> Context-efficiency: link to canonical docs — don't duplicate them; load on demand, not upfront.

> Each finding records: severity (Blocking / Important / Medium / Minor), check id, file path, what's wrong, why it matters, a concrete fix. Any Blocking finding fails the gate.

## Core checks (always run)
| # | Check | What it verifies | Severity if it fails |
|---|---|---|---|
| C1 | Spec-currency + WHAT/WHY | Affected module spec under `<module>/ai-docs/` changed in the same change as code; every requirement states WHAT and WHY | Blocking |
| C2 | Contract correctness | SDK-usage/public-surface changes reflected in the owning module spec and `CONTRACTS.md`; no undocumented breaking change | Blocking |
| C3 | Code-vs-spec match | Signatures, data-flow, and architecture claims match actual code (file path) | Blocking |
| C4 | Test adequacy | Each behavior change has a test with a positive AND a negative case where practical | Important |
| C5 | Error handling + input validation | UI/SDK inputs validated; `CompletionHandler` failures handled, not swallowed | Important |
| C6 | Security baseline | No hardcoded secrets; credentials stay in `local.properties`/`gradle.properties`; nothing sensitive logged (per `SECURITY.md`) | Blocking |

## Coverage-conditional checks (run by the touched module's manifest coverage state)
| # | Check | When it applies | What it verifies | Severity |
|---|---|---|---|---|
| K1 | Regression guard | Modifying any module (all are `Untracked`) | Behavior the change claims NOT to alter still works; add characterization coverage where risky | Blocking |
| K2 | Grounding | Any module (all `Untracked`) | Claims cite real code (file path), not memory; uncovered surfaces flagged `[NEEDS HUMAN INPUT]` | Important |
| K3 | Drift threshold | Any tracked module | N/A while all modules are `Untracked`; revisit after a coverage assessment | Important |
| K4 | Coverage-state accuracy | Coverage-state change proposed | The recorded manifest coverage state matches evidence; promotion rules honored | Medium |

## Cross-cutting checks (apply at higher risk / autonomy)
| # | Check | What it verifies | Severity |
|---|---|---|---|
| X1 | Cross-model review | The artifact was validated by a different runtime than the one that generated it (generator ≠ validator) | Blocking when required |
| X2 | Observability | Logging adequate for the change; nothing sensitive logged | Medium |
| X3 | Rollout safety | Flavor/build changes are safe; no accidental credential/scope default change | Important |

## How the set is selected
1. Always run the 6 core checks.
2. Add the coverage-conditional checks whose "when it applies" matches the touched modules' manifest coverage state (currently all `Untracked` → K1/K2 apply).
3. Add the cross-cutting checks when the change is high-risk or runs at higher autonomy.

## Output
- A compliance matrix + severity-sorted findings + a verdict (Pass / Pass-with-warnings / Blocked). Draft only; a human posts.
