# Agent Engineering Guidelines — Argus

Argus is a smart monitoring service: triggers (webhook/cron/Slack) fan out to concurrent data-fetching from GitHub,
LaunchDarkly, Jira, and pluggable telemetry providers, synthesized by a local LLM (Ollama) and delivered to Slack as a
formatted summary. These guidelines mirror the conventions used across this org's Kotlin services and apply to every
contributor, human or AI agent.

## Specification-First Development & API Documentation

- **Exhaustive Documentation Requirement**: Every public API endpoint, boundary interface, request/response payload, and configuration property must be fully documented:
  - **OpenAPI Specification**: All HTTP routes, path/query parameters, request bodies, and status codes must be documented in `docs/openapi.yaml` (and kept in sync with `app/src/main/resources/openapi/documentation.yaml` to power Swagger UI at `/swagger`).
  - **Domain Payloads & Models**: Always use strict Kotlin `@Serializable` data classes with clear KDoc comments for every field and enum variant — **never** `Map<String, Any>`.
  - **Stop and Ask on Ambiguity**: If an external API's shape or behavior is ambiguous or undocumented, stop and ask for clarification rather than hallucinating a schema.
- **TODO (Documentation Enforcement)**: Implement an automated verification step (via a Gradle task or integration test in CI) to enforce that all registered Ktor routes and models have corresponding entries in `docs/openapi.yaml`.


## Single Source of Truth / Zero Hardcoding

- `gradle/libs.versions.toml` is the sole source of truth for library coordinates and plugin versions. Never hardcode a
  version string in a module `build.gradle.kts`.
- Runtime config (ports, DB paths, Ollama host, Slack tokens, poll intervals, timeouts) lives in `application.conf`,
  parsed once at startup into a typed `AppConfig` data class, with env-var substitution (`${?VAR}`) for every secret or
  host.
- Team routing/provider config lives in `config/teams/*.yaml` and is synced into SQLite as the runtime source of truth —
  never re-parsed ad hoc.
- Business states (alert severity, incident status, provider health) use typed `enum class`, never string/magic-number
  comparisons.

## Multi-Module Boundaries (feature-driven, one-directional)

```
:domain             -> Pure domain data classes, enums, pipeline contracts.
                       NO DB/HTTP/framework deps.
:feature:ingestion  -> Webhooks, Slack slash commands, normalization into RawAlert.
                       Depends ONLY on :domain.
:feature:enrichment -> Telemetry providers (Humio, Sentry, Firebase), CI/CD (GitHub),
                       feature flags (LaunchDarkly), issues (Jira), AlertEnricher.
                       Depends ONLY on :domain.
:feature:analysis   -> Rule heuristics, incident windowing, LLM triage (Ollama).
                       Depends ONLY on :domain.
:feature:alert      -> Outbound sinks (Slack Block Kit, Jira, PagerDuty).
                       Depends ONLY on :domain.
:app                -> Ktor server, Netty, HOCON config, SQLite/Exposed persistence,
                       Koin composition root. Depends on :domain and all feature modules.
:test-fixtures      -> Reusable in-memory fakes and test utilities.
                       Shared across module test suites.
```

Feature modules (`:feature:ingestion`, `:feature:enrichment`, `:feature:analysis`, `:feature:alert`)
must never depend on each other directly; cross-cutting communication flows through domain contracts and pipeline
orchestrators.

## Dependency Injection Policy (Koin) — mandatory

- Each feature module defines its own Koin definitions (e.g. `ingestionModule`,
  `enrichmentModule`, `analysisModule`, `alertModule`).
- The `:app` module aggregates them into the root `appModules()`.
- **Zero direct-passing policy**: never instantiate a provider/client directly, and never pass one as a parameter
  through route functions (`fun Route.myRoutes()`) or through `Application.kt`'s constructor chain.
- Route handlers retrieve dependencies via injection:
  `val telemetryRegistry: TelemetryRegistry by call.inject()`.
- Tests override bindings with a second Koin module passed into
  `application { module(koinModules = listOf(testModule)) }` — never wire dependencies manually in a test.
- No inline fully-qualified names — always import types explicitly.

## Feature Architecture & Interface-Driven Boundaries

Argus features are grouped under the `feature/` directory mapped to each step of the alert triage lifecycle
(`:feature:ingestion`, `:feature:enrichment`, `:feature:analysis`, `:feature:alert`). Within each feature module, code
is organized cleanly by domain concept (e.g.,
`com.argus.enrichment.telemetry`, `com.argus.analysis.llm`).

Never let one feature's logic reach directly into another feature's internal classes — cross-feature calls must use
public domain interfaces in `:domain`.

### Interface-Driven Boundaries & Zero Conditional Environment Logic

Every boundary must be strictly abstracted behind an interface:

- **`ingestion`**: `AlertIngestor` (`DefaultAlertIngestor`, `ConsoleLoggingAlertIngestor`)
- **`enrichment`**: `AlertEnricher` (`DefaultAlertEnricher`, `ConsoleLoggingAlertEnricher`), `TelemetryRegistry`,
  `GitHubClient`, `LaunchDarklyClient`, `JiraClient`
- **`analysis`**: `TriageEngine` (`LlmTriageEngine`, `RuleBasedTriageEngine`), `LlmClient` (`OllamaClient`,
  `ConsoleEchoLlmClient`)
- **`alert`**: `AlertSink` (`SlackAlertSink`, `ConsoleAlertSink`)

**No conditional branching for environments**:
Never write `if (env == "local")` or `if (useSlack)` in business logic or handlers. All environmental variants (e.g.,
printing alerts to the console vs. sending via Slack) must be swapped exclusively at configuration time via **Koin
module composition** (`appModules()` vs `localAppModules()`).

### Minimal Visibility by Default (`internal` / `private`)

Visibility of components must be kept as restricted as possible:

- Default to `internal` or `private` for all implementation classes, helper methods, data models, and repository
  functions.
- Only domain data contracts, boundary interfaces (e.g. `AlertIngestor`, `AlertEnricher`, `TriageEngine`, `AlertSink`,
  `TelemetryRegistry`), public Koin modules, and top-level route extensions should be `public`.
- Concrete service implementations (e.g. `DefaultAlertIngestor`, `DefaultAlertEnricher`, `LlmTriageEngine`,
  `SlackAlertSink`, `ConsoleAlertSink`, `InMemoryTelemetryRegistry`) must be marked `internal`.

## Gradle Convention Plugins & Version Catalog

- Never hardcode version strings/coordinates in a module build file; use
  `libs.<dependency>` / `libs.bundles.<bundle>`.
- Always use **Type-Safe Project Accessors** (`projects.<module>`, e.g. `projects.domain`, `projects.feature.ingestion`,
  `projects.testFixtures`) instead of string-based `project(":<module>")` declarations.
- Add new libraries to `gradle/libs.versions.toml` first.
- `id("argus.kotlin.library")` — library modules: JVM 21 toolchain, Kotlin serialization plugin, coroutine/serialization
  opt-ins, JUnit5 + testing deps.
- `id("argus.ktor.app")` — the runnable server app: Netty, Ktor plugins, Koin, Logback structured logging, Micrometer
  Prometheus metrics.

## Kotlin & Coroutines Standards

Never let an unhandled exception crash a coroutine or request — catch at service/routing boundaries and surface via Ktor
`StatusPages`. Use `Flow`
with `flowOn(Dispatchers.IO)` for streaming provider responses. Structured SLF4J/Logback logging
(`logger.info("...", teamId)`) — never `println()`. Stub implementations return an explicit `TODO()`/`NotImplemented`
result rather than fake data, so nothing silently pretends to work.

## Testability & Test-First Development (TDD)

- **Test-First Requirement**: Tests must be written before implementation commences. Never write production code without
  an accompanying test suite defining expected behavior first.
- **Valid and Invalid State Coverage**: Every unit of business logic must have test cases covering both:
    - **Happy paths (valid states)**: Nominal payload formats, successful external responses, expected triage decisions,
      and complete context payloads.
    - **Edge cases and error paths (invalid states)**: Malformed payloads, timeouts, missing fields, unregistered
      telemetry providers, network failures, and empty data sets.
- **Stop and Ask on Ambiguity**: If there are open questions or ambiguities regarding what a feature does, its business
  rules, or its data contracts, **stop and ask for clarification** before continuing rather than assuming or
  hallucinating requirements.
- **Fakes over Mocks**: Prefer reusable in-memory fakes (`FakeTelemetryRegistry`, `FakeGitHubClient`, `FakeAlertSink`)
  implementing the real domain interfaces, living in `:test-fixtures`. Zero cloud dependencies in unit tests.
- **Evidence-Based Certainty**: Run `./gradlew check test` and confirm all tests pass before claiming completion.

## Commit Convention

AI agent contributions include the trailer
`Co-Authored-By: Claude <noreply@anthropic.com>`.
