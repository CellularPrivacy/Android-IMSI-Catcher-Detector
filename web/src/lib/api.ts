/** Typed HTTPS-only client for the Prometheus Infinity backend. */

export interface Sector {
  code: string;
  name: string;
  mandate: string;
  x: number;
  y: number;
}

export interface Agent {
  agent_id: string;
  faction: string;
  role: string | null;
  x: number | null;
  y: number | null;
  sector: string | null;
  status: "online" | "offline" | "stealth";
  masked: boolean;
  last_seen: string;
}

export interface TelemetryItem {
  id: number;
  event_id: number;
  device_id: string | null;
  faction: string | null;
  threat_level: string;
  description: string | null;
  apn: string | null;
  occurred_at: string;
}

export interface ErrorItem {
  id: number;
  app: string;
  app_version: string | null;
  exception_type: string;
  message: string | null;
  occurred_at: string;
}

export function apiBase(): string {
  const base = (process.env.NEXT_PUBLIC_API_BASE ?? "").replace(/\/$/, "");
  if (!base) throw new Error("NEXT_PUBLIC_API_BASE is not configured.");
  const url = new URL(base);
  const isLocal = url.hostname === "localhost" || url.hostname === "127.0.0.1";
  if (url.protocol !== "https:" && !isLocal) {
    throw new Error("Refusing plain-HTTP API base (HTTPS required): " + base);
  }
  return base;
}

async function authed(path: string, token: string): Promise<Response> {
  const res = await fetch(apiBase() + path, {
    headers: { Authorization: `Bearer ${token}` },
    cache: "no-store",
  });
  if (!res.ok) throw new Error(`API ${path} → HTTP ${res.status}`);
  return res;
}

export async function getHealth(): Promise<{ status: string; database: string }> {
  const res = await fetch(apiBase() + "/healthz", { cache: "no-store" });
  if (!res.ok) throw new Error(`API /healthz → HTTP ${res.status}`);
  return res.json();
}

export async function getSectors(token: string): Promise<Sector[]> {
  return (await authed("/v1/map/sectors", token)).json();
}

export async function getAgents(token: string): Promise<Agent[]> {
  return (await authed("/v1/agents", token)).json();
}

export async function getTelemetry(token: string, limit = 30): Promise<TelemetryItem[]> {
  return (await authed(`/v1/telemetry/recent?limit=${limit}`, token)).json();
}

export async function getErrors(token: string, limit = 30): Promise<ErrorItem[]> {
  return (await authed(`/v1/errors/recent?limit=${limit}`, token)).json();
}

export function streamUrl(token: string): string {
  const base = new URL(apiBase());
  base.protocol = base.protocol === "https:" ? "wss:" : "ws:";
  base.pathname = "/v1/stream";
  base.search = `?token=${encodeURIComponent(token)}`;
  return base.toString();
}
