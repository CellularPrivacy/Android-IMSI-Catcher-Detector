# Prometheus shared contracts (JSON Schema, draft 2020-12)

Single source of truth for cross-process payloads. Producers and consumers must
all conform; the backend mirrors these in `backend/app/models.py` (pydantic).

| Schema | Producer | Consumer |
|---|---|---|
| `telemetry-event.schema.json` | AIMSICD Android (`prometheus/…`) | `POST /v1/telemetry`, WS fan-out, web Signals |
| `error-report.schema.json` | Android / backend / web crash hooks | `POST /v1/errors`, web Reports, vector DB |
| `agent-state.schema.json` | `POST /v1/agents/heartbeat` | `GET /v1/agents` (masked), Agent City 3D map |
