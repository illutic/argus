# Specification: Context Enrichment Feature (`:feature:enrichment`)

## 1. Overview & Objective
The Enrichment feature aggregates diagnostic context around an incoming alert across multiple external systems concurrently:
- **Telemetry Providers**: Logs, exception stacktraces, metric samples (Humio, Sentry, Firebase Crashlytics).
- **CI/CD & Source Code**: Commits, PRs, and deployment tags (GitHub).
- **Feature Flags**: Modified flags and targeting rules (LaunchDarkly).
- **Issue Tracking**: Associated bugs and incident tickets (Jira).

All diagnostic data is structured in a **provider-agnostic** representation: each provider produces an `AlertContext` (composed of a `providerKey` and a list of context strings).

---

## 2. Domain Models & Contracts

### `com.argus.domain.model.AlertContext`
```kotlin
@Serializable
data class AlertContext(
    val providerKey: String,
    val items: List<String> = emptyList(),
)
```

### `com.argus.domain.model.EnrichedAlertContext`
```kotlin
@Serializable
data class EnrichedAlertContext(
    val alert: RawAlert,
    val contexts: List<AlertContext> = emptyList(),
    val providerErrors: List<String> = emptyList(),
)
```

---

## 3. Boundary Interfaces & DI Architecture

### Public Interfaces
```kotlin
package com.argus.enrichment.service

public interface AlertEnricher {
    public suspend fun enrich(alert: RawAlert, teamConfig: TeamConfig): EnrichedAlertContext
}
```

```kotlin
package com.argus.enrichment.provider

public interface ContextProvider {
    public val key: String
    public suspend fun fetchContext(alert: RawAlert, teamConfig: TeamConfig): AlertContext
}
```

### Implementations (Internal)
- `DefaultAlertEnricher`: Asynchronously fans out over a `List<ContextProvider>`, collecting `AlertContext`s and catching provider failures into `providerErrors`.
- `ConsoleLoggingAlertEnricher`: Generates mock diagnostic context for local testing/demo mode.
- `TelemetryContextProvider`, `GitHubContextProvider`, `LaunchDarklyContextProvider`, `JiraContextProvider`: Concrete adapters implementing `ContextProvider`.

---

## 4. Concurrent Fan-Out Flow Diagram

```mermaid
flowchart TD
    A["AlertEnricher.enrich(alert, teamConfig)"] --> B["supervisorScope"]
    
    B --> C["async: ContextProvider(telemetry).fetchContext"]
    B --> D["async: ContextProvider(github).fetchContext"]
    B --> E["async: ContextProvider(launchdarkly).fetchContext"]
    B --> F["async: ContextProvider(jira).fetchContext"]
    
    C --> G["Await All & Combine Non-Empty Contexts"]
    D --> G
    E --> G
    F --> G
    
    G --> H["Return EnrichedAlertContext(alert, contexts, providerErrors)"]
```

---

## 5. State Matrix (Valid & Invalid States)

| Scenario | Condition | Expected Behavior | Output Context |
| :--- | :--- | :--- | :--- |
| **Complete Success** | All providers respond successfully with data | Aggregates all provider contexts | `EnrichedAlertContext` with non-empty `contexts` |
| **Provider Timeout / Error** | An individual provider throws an exception | Caught in `runCatching`, records error in `providerErrors` | `contexts` contains remaining providers, error noted |
| **Provider Returns Empty** | Provider finds 0 correlating items | Empty `AlertContext.items` omitted or empty | Valid `EnrichedAlertContext` |
| **All Providers Fail** | Network partition across all providers | Catches all exceptions, records all errors | `contexts = []`, all errors in `providerErrors` |

---

## 6. Test Plan & Fakes
- **Unit Tests**:
  - `AlertEnricherTest`: Verifies parallel execution, provider-agnostic aggregation, and isolated error handling.
  - `InMemoryTelemetryRegistryTest`: Verifies telemetry provider registration and resolution.
- **Fakes in `:test-fixtures`**:
  - `FakeTelemetryRegistry`
  - `FakeGitHubClient`
  - `FakeLaunchDarklyClient`
  - `FakeJiraClient`
