# Argus

Smart monitoring service: triggers (webhook/cron/Slack) fan out to concurrent data-fetching from GitHub, LaunchDarkly,
Jira, and pluggable telemetry providers, synthesized by a local LLM (Ollama) and delivered to Slack as a formatted
summary.

See [docs/PLAN.md](docs/PLAN.md) for the scaffold rationale and [AGENTS.md](AGENTS.md)
for engineering guidelines (module boundaries, DI policy, testing standards).

## Getting Started

### Local Configuration

Copy the example environment file:

```bash
cp .env.example .env
```

### Option A: Run via Docker Compose (includes local Ollama container)

```bash
docker compose up -d --build
curl localhost:8080/health
```

To view logs:

```bash
docker compose logs -f app
```

### Option B: Build & Run Locally with Gradle

```bash
./gradlew build
./gradlew :app:run
curl localhost:8080/health
```

### API Documentation & Swagger UI

Once the service is running, explore the interactive OpenAPI documentation:
- **Swagger UI**: [http://localhost:8080/swagger](http://localhost:8080/swagger)
- **OpenAPI UI**: [http://localhost:8080/openapi](http://localhost:8080/openapi)
- **OpenAPI Specification**: [`docs/openapi.yaml`](docs/openapi.yaml)



## How to Use

### 1. Configure Team Profiles

Teams are defined in YAML files under [`config/teams/`](config/teams/). Create a profile for your team (e.g.
`config/teams/backend-core.yaml`):

```yaml
teamId: backend-core
jiraPrefix: CORE
slackChannelId: C0123456789
repoLayers:
  - api-gateway
  - auth-service
telemetry:
  - humio
  - sentry
```

### 2. Configure Credentials (`.env`)

Set your API keys and tokens in `.env`:

```ini
PORT=8080
ARGUS_DB_PATH=argus.db
OLLAMA_HOST=http://localhost:11434
OLLAMA_MODEL=gpt-oss:20b

SLACK_BOT_TOKEN=xoxb-your-slack-token
GITHUB_TOKEN=ghp_your_github_token
LAUNCHDARKLY_TOKEN=sdk-your-launchdarkly-token
JIRA_BASE_URL=https://your-org.atlassian.net
JIRA_TOKEN=your-jira-api-token
```

### 3. Send Alert Triggers

Argus accepts inbound alert triggers from webhooks, Slack slash commands, or scheduled tasks:

#### Webhook Trigger

```bash
curl -X POST http://localhost:8080/triggers/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "teamId": "example-team",
    "source": "sentry",
    "title": "UnhandledException in PaymentGateway",
    "payload": "{\"exception\": \"ConnectionTimeout\", \"service\": \"payment\"}"
  }'
```

#### Slack Slash Command Trigger

```bash
curl -X POST http://localhost:8080/triggers/slack \
  -H "Content-Type: application/json" \
  -d '{
    "teamId": "example-team",
    "command": "/triage",
    "text": "investigate latest payment failures"
  }'
```

### 4. Verify System & AI Status

* **Service Health**: `curl http://localhost:8080/health` (returns `200 OK`)
* **Ollama API**: `curl http://localhost:11434/api/version`
* **List Ollama Models**: `docker compose exec ollama ollama list`
* **View Real-Time Logs**: `docker compose logs -f app`

## Modules

- `domain` — Pure domain data classes, enums, and pipeline contracts (zero framework dependencies).
- `feature/`
    - `ingestion` (`:feature:ingestion`) — Inbound webhook endpoints, Slack command receivers, and payload
      normalization.
    - `enrichment` (`:feature:enrichment`) — Context providers and telemetry integrations (Humio, Sentry, GitHub,
      LaunchDarkly, Jira).
    - `analysis` (`:feature:analysis`) — Heuristic scoring, incident windowing, and LLM prompt synthesis
      (`OllamaClient`).
    - `alert` (`:feature:alert`) — Outbound delivery sinks (Slack Block Kit notifications, Jira ticket creation).
- `app` (`:app`) — Server runtime, HOCON config, SQLite/Exposed persistence, and Koin composition root.
- `test-fixtures` (`:test-fixtures`) — In-memory fakes for unit and integration testing.

## Tech Stack

| Layer                  | Technology                                                  |
|------------------------|-------------------------------------------------------------|
| **Language & Runtime** | Kotlin (JVM)                                                |
| **Framework**          | Ktor (Server, Client, & Coroutines)                         |
| **Database & ORM**     | SQLite (Embedded) via JetBrains Exposed ORM                 |
| **Configuration**      | HOCON (`application.conf`) & YAML (`kotlinx.serialization`) |
| **AI Integration**     | Direct LLM Provider SDK (Single-pass Gemini / OpenAI)       |
| **Extensibility**      | Custom `TelemetryRegistry` Factory Pattern                  |

## System Architecture Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                             TRIGGER LAYER                               │
│     Inbound Webhook Alert  │  Daily Cron Job  │  Slack Slash Command    │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        KTOR INGESTION SERVICE                           │
│   1. Resolve time window (Incident-relative vs. Daily lookback)         │
│   2. Fetch Team Config & Feature Boundaries from SQLite                 │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│               CONCURRENT DATA FETCHING (Kotlin Coroutines)              │
│   ├── GitHub API          ──> Merged PRs across dynamic repo layers     │
│   ├── LaunchDarkly API    ──> Active flag rollouts & targeting changes  │
│   ├── Jira API            ──> Epic scope & ticket context (SUBT-XXX)    │
│   └── TelemetryRegistry   ──> Dynamic execution of matched providers    │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      CONTEXT ASSEMBLY & AI SYNTHESIS                    │
│   1. Filter out non-code/documentation noise                            │
│   2. Inject pre-fetched payload into structured LLM prompt              │
│   3. Perform cross-boundary analysis via single-pass SDK call           │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                              DELIVERY                                   │
│   Format structured response into Slack Block Kit & post to channel     │
└─────────────────────────────────────────────────────────────────────────┘

```

## Telemetry Provider Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       BOOTSTRAP & VALIDATION                            │
│   1. Application startup registers supported TelemetryFactories         │
│   2. System reads team YAML config files                                │
│   3. Missing/Unregistered provider references trigger immediate crash   │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          TelemetryRegistry                              │
└───────┬────────────────────────────┬────────────────────────────┬───────┘
        │                            │                            │
        ▼                            ▼                            ▼
┌──────────────┐             ┌──────────────┐             ┌──────────────┐
│ Humio        │             │ Firebase     │             │ Sentry       │
│ Provider     │             │ Provider     │             │ Provider     │
└──────────────┘             └──────────────┘             └──────────────┘

```

See [docs/PLAN.md](docs/PLAN.md) for the full implementation plan (configuration layer, telemetry ingestion engine, data
aggregation pipeline, and Slack dispatch).

