# Prometheus Project: Infinity — Backend API

FastAPI service owning live city state: detection telemetry ingest, agent presence
with faction masking, the 6 map sectors, neural error reports, and a WebSocket
telemetry fan-out. PostgreSQL is the system of record (direct cloud connection —
no local DB simulation, no seed fakes: empty tables return empty lists).

## Run

```bash
pip install -r requirements.txt
export DATABASE_URL='postgresql://user:pass@host:5432/prometheus?sslmode=require'
export JWT_SECRET='long-random-secret'
export PROVISION_KEY='another-long-random-secret'
export CORS_ORIGINS='https://city.prometheus.example'
uvicorn app.main:app --host 0.0.0.0 --port 8443
```

Or via the root `docker-compose.yml` (postgres + backend).

## Environment

| Variable | Required | Meaning |
|---|---|---|
| `DATABASE_URL` | yes | PostgreSQL DSN. `sslmode=require` (or stricter) is enforced. |
| `JWT_SECRET` | yes* | HS256 secret for operator + device tokens. *Not needed if `OIDC_JWKS_URL` is set. |
| `JWT_ISSUER` | no | Expected `iss` claim (`prometheus-infinity`). |
| `JWT_TTL_SECONDS` | no | Operator token lifetime hint (900). Tokens are minted externally. |
| `DEVICE_JWT_TTL_SECONDS` | no | Device token lifetime (30 days). |
| `OIDC_JWKS_URL` | no | Keycloak/Auth0 JWKS URL — switches verification to RS256. |
| `PROVISION_KEY` | yes | Shared secret for `POST /v1/devices/token`. |
| `CORS_ORIGINS` | no | Comma-separated HTTPS origins (default: none allowed). |

## Auth & roles

- `Authorization: Bearer <JWT>` everywhere except `/healthz` and `/`.
- Claims: `sub` (user or device id), `faction` (INF/PRM/DEF/UNT/EQB/PKB), `roles`
  (e.g. `command`, `watch`, `device`, `operator`).
- Agent coordinates are masked unless the viewer shares the agent's faction, or
  holds `command`/`watch` in INF/DEF. Stealth agents additionally hide from
  everyone except same-faction viewers and INF command. Pure function
  `app/masking.py`, covered by `tests/test_masking.py` (`python -m unittest`).
- Error-report history (`GET /v1/errors/recent`) requires `command`/`watch` in
  INF or DEF.

## Provisioning a field node (Android DEF sensor)

```bash
# 1. Mint a device token (operator machine, provision key never leaves the server):
curl -s -X POST https://api.prometheus.example/v1/devices/token \
  -H 'X-Provision-Key: <PROVISION_KEY>' \
  -H 'Content-Type: application/json' \
  -d '{"device_id":"def-sensor-01","faction":"DEF"}'
# → {"token":"<JWT>","expires_at":"..."}

# 2. Paste endpoint + token into the Android app:
#    Settings → Protection → Prometheus Uplink.
```

Or offline: `python -m app.provision --device-id def-sensor-01 --faction DEF`
(with `JWT_SECRET` + `DEVICE_JWT_TTL_SECONDS` in env).

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/healthz` | no | Liveness + DB `SELECT 1`. |
| POST | `/v1/devices/token` | provision key | Mint a device JWT. |
| POST | `/v1/telemetry` | JWT | Ingest a detection event; fans out to WS. |
| GET | `/v1/telemetry/recent?limit=` | JWT | Latest events, newest first. |
| POST | `/v1/errors` | JWT | Ingest a crash report. |
| GET | `/v1/errors/recent?limit=` | JWT command/watch INF/DEF | Crash history. |
| GET | `/v1/agents` | JWT | Presence list, faction-masked for the viewer. |
| POST | `/v1/agents/heartbeat` | JWT | Upsert own presence (id/faction taken from claims). |
| GET | `/v1/map/sectors` | JWT | The 6 faction sectors (seeded in Postgres). |
| WS | `/v1/stream?token=` | JWT (query) | Live telemetry fan-out. |

Payload shapes: `../contracts/*.schema.json` (mirrored by `app/models.py`).
