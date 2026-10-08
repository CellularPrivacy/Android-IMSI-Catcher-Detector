# Prometheus Project: Infinity [Phase 2] — Monorepo Integration

This repo now hosts the full Phase 2 slice: the Android field sensor, the cloud
API, the luxury web console, and the shared wire contracts. No mocks, no local
DB simulation: every layer talks to the real thing (Postgres over TLS, HTTPS
APIs, live JWT auth).

```
┌──────────────────────┐   HTTPS + JWT    ┌──────────────────────────────┐
│  AIMSICD Android     │  POST /v1/*      │  Prometheus backend          │
│  DEF field sensor    │ ───────────────► │  FastAPI + PostgreSQL (TLS)  │
│  CellTracker ─┐      │                  │  telemetry · agents · errors │
│  SimSwapper   ├─► PrometheusUplink ────►│  sectors · WS /v1/stream     │
│  ApnGuard     │      │                  └──────────────┬───────────────┘
│  DefenderAgent┘      │                                 │ HTTPS + JWT
│  NeuralErrorLogger ──┘                                 ▼
└──────────────────────┘                  ┌──────────────────────────────┐
                                          │  Infinity web console        │
                                          │  Next.js · Origins + 3D map  │
                                          └──────────────────────────────┘
```

## 1. Repository map

| Path | Role in the plan |
|---|---|
| `AIMSICD/` | DEF-faction field node: IMSI-catcher / SIM-swap / APN-guard detection + `prometheus/` uplink. |
| `AIMSICD/…/prometheus/PrometheusUplink.java` | HTTPS telemetry + error-report uplink (stdlib + `org.json`, no new Gradle deps). |
| `AIMSICD/…/prometheus/NeuralErrorLogger.java` | Uncaught-exception hook → pending crash file → upload on next start. |
| `backend/` | FastAPI API: ingest, presence, sectors, error log, WS fan-out, device provisioning. |
| `web/` | Origins landing + Agent City 3D map + Signals/Reports/Settings (mobile-first PWA). |
| `contracts/` | JSON Schemas: `telemetry-event`, `error-report`, `agent-state`. |
| `docker-compose.yml` | One-command stack: Postgres 16 + backend. |

## 2. Role mapping (existing modules → Agent OS)

| AIMSICD module | Prometheus role |
|---|---|
| `CellTracker` (LAC/CID/neighbour/silent-SMS heuristics) | DEF sensor fusion input; owns status + countermeasure dispatch. |
| `SimSwapper` + `SimSwapAlarmReceiver` | SIM-swap tripwire → `100/101/102/104` telemetry. |
| `ApnGuard` | APN-binding tripwire → `103/107/108/109` telemetry. |
| `DefenderAgent` + LTE-spoof + firewall + traffic monitor | **Defender OS (Shield & Security)** on-device half. |
| `AutoProtector` | On-device actuators (radio cut, trail wipe). |
| `PrometheusUplink` + `NeuralErrorLogger` | Cloud uplink: telemetry stream + self-learning error loop. |

Agent OS server halves (Code Agent self-healing, Research RAG, vector patch
generation) consume `telemetry` / `error_reports` tables — schema-ready, agent
runners are follow-up work (see §6).

## 3. Factions & sectors

`INF` Infinity Nation · `PRM` Prometheus Corp · `DEF` Infinity Wardens ·
`UNT` Unity Academy · `EQB` Equilibrium Order · `PKB` Peaky Clan.
Seeded idempotently into Postgres (`sectors` table); served by
`GET /v1/map/sectors`; rendered as the 6 towers on the 3D map.

## 4. API surface (all `https://`, port 443 at the edge)

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/healthz` | no | Liveness + DB check. |
| POST | `/v1/devices/token` | `X-Provision-Key` | Mint a device JWT (`sub`=device, `roles`=[device]). |
| POST | `/v1/telemetry` | JWT | Ingest detection event → Postgres + WS fan-out. |
| GET | `/v1/telemetry/recent` | JWT | Latest events (Signals tab). |
| POST | `/v1/errors` | JWT | Ingest crash report. |
| GET | `/v1/errors/recent` | JWT, INF/DEF command/watch | Crash history (Reports tab). |
| GET | `/v1/agents` | JWT | Presence, faction-masked for the viewer. |
| POST | `/v1/agents/heartbeat` | JWT | Upsert own presence (id/faction from claims). |
| GET | `/v1/map/sectors` | JWT | 6 sectors. |
| WS | `/v1/stream?token=` | JWT (query) | Live telemetry fan-out. |

## 5. Security model

- **Transport:** HTTPS-only end to end. Backend refuses non-TLS database URLs,
  emits HSTS, restricts CORS to configured HTTPS origins. Android + web clients
  refuse plain-HTTP endpoints (loopback exempt for dev).
- **Auth:** short-lived operator JWTs; device JWTs (30 d default) minted via the
  provision key or the offline `python -m app.provision` CLI. RS256/Keycloak
  path via `OIDC_JWKS_URL` (cached JWKS, `kid` pinning).
- **RBAC/ABAC:** agent coordinates masked unless same faction or INF/DEF
  `command`/`watch`; stealth agents additionally hide from everyone except
  same-faction viewers and INF command (`backend/app/masking.py`, unit-tested).
  Error history restricted to INF/DEF command/watch.
- **No secrets on the web client:** tokens are minted server-side and pasted in
  Settings (browser-local). Android holds one device JWT in app-private prefs
  (hardening path: Android Keystore + rotation).
- **Privacy:** the uplink never sends raw subscriber identifiers — only the
  SHA-256 SIM fingerprint, same as the on-device baseline.

## 6. Verification status — honest

**Verified in this sandbox (real runs, no mocks):**
- Backend E2E **16/16 green** against real PostgreSQL (boot → provision →
  telemetry/errors ingest + history + auth denials → heartbeat → masking
  (same-faction full / cross-faction masked / INF-command full) → 6 sectors →
  WS fan-out → WS 4401 on bad token).
- `python -m unittest` green for `tests/test_masking.py` (11 tests).
- Web: `tsc --noEmit` clean, `next lint` clean, `next build` green,
  production `next start` boot + `curl /` and `/map` → HTTP 200.
- Android Java: every file under `AIMSICD/src` parses with **0 tree-sitter
  errors** (this sweep caught and fixed two real corruptions in
  `CellTracker.java`); all touched XML parsed; no duplicate string resources;
  every new `R.string` resolves; `git diff --check` clean.

**NOT verifiable here (no Android SDK/Gradle; legacy deps on dead jcenter):**
- `./gradlew assembleDebug` and the on-device uplink flow — needs CI/hardware.
- Live Keycloak RS256 path (HS256 covered) and multi-replica WS fan-out.

## 7. Follow-ups (out of this slice)

- Agent runners: Code Agent (self-heal from `error_reports`), Research/RAG
  vector match (pgvector), Defender OS prompt-injection screen.
- Keycloak realm + web OIDC login (replace pasted-token operator session).
- gRPC inter-agent transport; SpacetimeDB/Supabase vector store selection.
- Android Keystore storage for the device JWT + rotation.
- Multi-replica WS fan-out (Redis pub/sub) + rate limits on ingest.
