<!-- sdd-generated-metadata
doc_kind: standing-doc
generated_from: contracts@0.2.1
generated_by: claude-cli
approved_by: pending
updated_at: 2026-07-31T00:00:00Z
validation_status: not-run
-->
# Contracts Catalog — kitchenSink

> Start here → root [`AGENTS.md`](../AGENTS.md) (agent entry) · router [`SPEC_INDEX.md`](SPEC_INDEX.md) · system [`ARCHITECTURE.md`](ARCHITECTURE.md). Then this root contract index; detailed contracts live with owning modules or canonical schema files. Machine source `.sdd/manifest.json`.
> Context-efficiency: link to canonical docs — don't duplicate them; load on demand, not upfront.

> Read before adding any public-facing surface — check here first. Machine source of truth: `.sdd/manifest.json`.

> **Public-surface note:** kitchenSink is a demo Android application. It does not publish a network API, an importable library/package, its own event bus, or a CLI. Its contracts are the external surfaces it **consumes** — primarily the Cisco Webex Android SDK — plus the push-messaging and webhook endpoints it integrates with. Those are captured in "Requires" below; the SDK-consumption detail lives in each owning module spec.

## Requires — what this repo depends on
| Dependency (service / package / datastore) | What is consumed | Schema / detail link | Availability assumption | Fallback on failure | Version floor |
|---|---|---|---|---|---|
| Cisco Webex Android SDK (`com.ciscowebex:webexsdk` + `-meeting`/`-wxc`/`-message`) | `Webex`, `Phone`, `Message`, `Space`, `Membership`, `People`, `CalendarMeetings`, auth/observer APIs | Upstream SDK reference (`README.md` links `webex/webex-android-sdk`) | Assumed available at build (Webex Artifactory) and at runtime (Webex cloud) | Login/SDK errors surfaced via `LiveData`/`Toast` | `3.16.3` (`app/build.gradle`) |
| Webex identity/OAuth | OAuth/JWT/Access-Token authentication | `auth/` module spec | Assumed reachable | Login-failed callback (`WebexRepository.onLoginFailed`) | `[NEEDS HUMAN INPUT]` |
| Firebase Cloud Messaging / Analytics / Crashlytics | Push messaging, analytics, crash reporting | `KitchenSinkFCMService` (`AndroidManifest.xml`); Firebase BoM `26.1.0` (`app/build.gradle`) | Requires `google-services.json` | App still runs without push | Firebase BoM `26.1.0` |
| Configured webhook endpoint (`WEBHOOK_URL`) | Webhook target used by the webhooks demo | `webhooks/` module spec; `BuildConfig.WEBHOOK_URL` (`app/build.gradle`) | Supplied via `local.properties` | Empty default → feature inert | `[NEEDS HUMAN INPUT]` |

## Compatibility & Deprecation Policy
- **Breaking-change rule:** this app has no external consumers, so there is no published-surface compatibility contract. When the pinned Webex SDK version changes (`app/build.gradle`), verify affected SDK calls in the owning module specs.
- **Deprecation:** track SDK-side deprecations from the upstream SDK; this repo carries none of its own.

<!-- Include if: a non-trivial interface needs full schema/error detail beyond this catalog -->
## Detailed Interface Docs
- Exact SDK API names/types/events are the upstream Webex Android SDK reference (`README.md`). Per-module SDK usage is summarized in each module spec's `Public Surface` / `Requires` sections.

## Maintenance
- When SDK usage or an integrated external endpoint is added/changed/removed, update the owning module spec summary and `.sdd/manifest.json` in the same change.
- Cross-reference: domain terms → `GLOSSARY.md`.
