"""PostgreSQL access. Direct cloud connection, no local simulation.

Tables are created (if missing) at startup; the 6 faction sectors are seeded
idempotently. Empty tables return empty lists — never fabricated rows.
"""

from contextlib import contextmanager

import psycopg2
import psycopg2.extras
from psycopg2.pool import ThreadedConnectionPool

from .config import settings

SCHEMA_SQL = """
CREATE TABLE IF NOT EXISTS telemetry (
    id          BIGSERIAL PRIMARY KEY,
    event_id    INTEGER NOT NULL,
    device_id   TEXT,
    faction     TEXT,
    threat      TEXT NOT NULL,
    description TEXT,
    cell_mcc    INTEGER,
    cell_mnc    INTEGER,
    cell_lac    INTEGER,
    cell_cid    INTEGER,
    cell_rat    TEXT,
    apn         TEXT,
    sim_fp      TEXT,
    app_version TEXT,
    occurred_at TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_telemetry_occurred ON telemetry (occurred_at DESC);

CREATE TABLE IF NOT EXISTS error_reports (
    id             BIGSERIAL PRIMARY KEY,
    app            TEXT NOT NULL,
    app_version    TEXT,
    os             TEXT,
    device_id      TEXT,
    exception_type TEXT NOT NULL,
    message        TEXT,
    stack_trace    TEXT NOT NULL,
    breadcrumbs    TEXT[] NOT NULL DEFAULT '{}',
    occurred_at    TIMESTAMPTZ NOT NULL,
    received_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_errors_occurred ON error_reports (occurred_at DESC);

CREATE TABLE IF NOT EXISTS agents (
    agent_id  TEXT PRIMARY KEY,
    faction   TEXT NOT NULL,
    role      TEXT,
    x         DOUBLE PRECISION,
    y         DOUBLE PRECISION,
    sector    TEXT,
    status    TEXT NOT NULL,
    last_seen TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS sectors (
    code    TEXT PRIMARY KEY,
    name    TEXT NOT NULL,
    mandate TEXT NOT NULL,
    x       DOUBLE PRECISION NOT NULL,
    y       DOUBLE PRECISION NOT NULL
);
"""

SECTORS = [
    ("INF", "Infinity Nation", "Public spaces, swarm policy, central capitol.", 0.0, 0.0),
    ("PRM", "Prometheus Corp", "Trade, economy, R&D labs, resource control.", 60.0, 20.0),
    ("DEF", "Infinity Wardens", "Patrols, shield, security logs, infra debug.", -60.0, 20.0),
    ("UNT", "Unity Academy", "Research, training, knowledge bases, RAG archives.", -40.0, -55.0),
    ("EQB", "Equilibrium Order", "Law, arbitration, load balancing, contracts.", 40.0, -55.0),
    ("PKB", "Peaky Clan", "Hidden ops, recon, stealth and anti-surveillance.", 0.0, 65.0),
]

_pool = None


def init_pool():
    """Create the connection pool and ensure schema + sector seed. Raises on failure."""
    global _pool
    if _pool is not None:
        return
    _pool = ThreadedConnectionPool(1, 10, settings.DATABASE_URL)
    with _cursor() as cur:
        cur.execute(SCHEMA_SQL)
        for code, name, mandate, x, y in SECTORS:
            cur.execute(
                "INSERT INTO sectors (code, name, mandate, x, y)"
                " VALUES (%s, %s, %s, %s, %s)"
                " ON CONFLICT (code) DO NOTHING",
                (code, name, mandate, x, y),
            )


def close_pool():
    global _pool
    if _pool is not None:
        _pool.closeall()
        _pool = None


def check():
    """Return True when a trivial query succeeds."""
    try:
        with _cursor() as cur:
            # RealDictCursor returns a dict, not a tuple — alias the column.
            cur.execute("SELECT 1 AS ok")
            return cur.fetchone()["ok"] == 1
    except Exception:
        return False


@contextmanager
def _cursor():
    conn = _pool.getconn()
    try:
        with conn:
            with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
                yield cur
    finally:
        _pool.putconn(conn)


def insert_telemetry(event):
    cell = event.get("cell") or {}
    with _cursor() as cur:
        cur.execute(
            "INSERT INTO telemetry (event_id, device_id, faction, threat, description,"
            " cell_mcc, cell_mnc, cell_lac, cell_cid, cell_rat, apn, sim_fp,"
            " app_version, occurred_at)"
            " VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"
            " RETURNING id",
            (
                event["event_id"],
                event.get("device_id"),
                event.get("faction"),
                event["threat_level"],
                event.get("description"),
                cell.get("mcc"),
                cell.get("mnc"),
                cell.get("lac"),
                cell.get("cid"),
                cell.get("rat"),
                event.get("apn"),
                event.get("sim_fingerprint_sha256"),
                event.get("app_version"),
                event["occurred_at"],
            ),
        )
        return cur.fetchone()["id"]


def list_recent_telemetry(limit):
    with _cursor() as cur:
        cur.execute(
            "SELECT id, event_id, device_id, faction, threat AS threat_level,"
            " description, cell_mcc AS mcc, cell_mnc AS mnc, cell_lac AS lac,"
            " cell_cid AS cid, cell_rat AS rat, apn, app_version,"
            " occurred_at, received_at"
            " FROM telemetry ORDER BY occurred_at DESC LIMIT %s",
            (limit,),
        )
        return [dict(row) for row in cur.fetchall()]


def insert_error(report):
    with _cursor() as cur:
        cur.execute(
            "INSERT INTO error_reports (app, app_version, os, device_id, exception_type,"
            " message, stack_trace, breadcrumbs, occurred_at)"
            " VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s) RETURNING id",
            (
                report["app"],
                report.get("app_version"),
                report.get("os"),
                report.get("device_id"),
                report["exception_type"],
                report.get("message"),
                report["stack_trace"],
                report.get("breadcrumbs") or [],
                report["occurred_at"],
            ),
        )
        return cur.fetchone()["id"]


def list_recent_errors(limit):
    with _cursor() as cur:
        cur.execute(
            "SELECT id, app, app_version, os, device_id, exception_type, message,"
            " stack_trace, breadcrumbs, occurred_at, received_at"
            " FROM error_reports ORDER BY occurred_at DESC LIMIT %s",
            (limit,),
        )
        return [dict(row) for row in cur.fetchall()]


def upsert_agent(agent):
    with _cursor() as cur:
        cur.execute(
            "INSERT INTO agents (agent_id, faction, role, x, y, sector, status, last_seen)"
            " VALUES (%s, %s, %s, %s, %s, %s, %s, now())"
            " ON CONFLICT (agent_id) DO UPDATE SET"
            " faction = EXCLUDED.faction, role = EXCLUDED.role,"
            " x = EXCLUDED.x, y = EXCLUDED.y, sector = EXCLUDED.sector,"
            " status = EXCLUDED.status, last_seen = now()",
            (
                agent["agent_id"],
                agent["faction"],
                agent.get("role"),
                agent.get("x"),
                agent.get("y"),
                agent.get("sector"),
                agent["status"],
            ),
        )


def list_agents():
    with _cursor() as cur:
        cur.execute(
            "SELECT agent_id, faction, role, x, y, sector, status, last_seen"
            " FROM agents ORDER BY last_seen DESC"
        )
        return [dict(row) for row in cur.fetchall()]


def list_sectors():
    with _cursor() as cur:
        cur.execute("SELECT code, name, mandate, x, y FROM sectors ORDER BY code")
        return [dict(row) for row in cur.fetchall()]
