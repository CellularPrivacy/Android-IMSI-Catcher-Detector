"use client";

import { Suspense, useCallback, useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import CityMap from "@/components/CityMap";
import HudStatus, { type WsState } from "@/components/HudStatus";
import ReportsPanel from "@/components/ReportsPanel";
import SettingsDrawer from "@/components/SettingsDrawer";
import SignalsPanel from "@/components/SignalsPanel";
import { getAgents, getHealth, getSectors, streamUrl, type Agent, type Sector } from "@/lib/api";
import { FACTIONS } from "@/lib/factions";

export const dynamic = "force-dynamic";

const TOKEN_KEY = "infinity-operator-token";

function MapContent() {
  const tab = useSearchParams().get("tab") ?? "sectors";
  const [token, setToken] = useState("");
  const [sectors, setSectors] = useState<Sector[]>([]);
  const [agents, setAgents] = useState<Agent[]>([]);
  const [api, setApi] = useState<"up" | "down" | "unknown">("unknown");
  const [database, setDatabase] = useState("?");
  const [ws, setWs] = useState<WsState>("offline");
  const [liveEvents, setLiveEvents] = useState(0);

  useEffect(() => {
    setToken(localStorage.getItem(TOKEN_KEY) ?? "");
  }, []);

  const saveToken = useCallback((value: string) => {
    setToken(value);
    if (value) localStorage.setItem(TOKEN_KEY, value);
    else localStorage.removeItem(TOKEN_KEY);
  }, []);

  useEffect(() => {
    let cancelled = false;
    async function poll() {
      try {
        const health = await getHealth();
        if (!cancelled) {
          setApi(health.status === "ok" ? "up" : "down");
          setDatabase(health.database);
        }
      } catch {
        if (!cancelled) {
          setApi("down");
          setDatabase("?");
        }
      }
    }
    poll();
    const id = setInterval(poll, 15000);
    return () => {
      cancelled = true;
      clearInterval(id);
    };
  }, []);

  useEffect(() => {
    if (!token || tab !== "sectors") return;
    let cancelled = false;
    async function poll() {
      try {
        const [s, a] = await Promise.all([getSectors(token), getAgents(token)]);
        if (!cancelled) {
          setSectors(s);
          setAgents(a);
        }
      } catch {
        /* HUD keeps last-known state; panels surface errors. */
      }
    }
    poll();
    const id = setInterval(poll, 5000);
    return () => {
      cancelled = true;
      clearInterval(id);
    };
  }, [token, tab]);

  useEffect(() => {
    if (!token) {
      setWs("offline");
      return;
    }
    let socket: WebSocket | null = null;
    let closed = false;
    let retry = 0;
    let timer = 0;

    function connect() {
      if (closed) return;
      setWs("connecting");
      try {
        socket = new WebSocket(streamUrl(token));
      } catch {
        schedule();
        return;
      }
      socket.onopen = () => {
        retry = 0;
        setWs("live");
      };
      socket.onmessage = () => setLiveEvents((n) => n + 1);
      socket.onclose = () => {
        setWs("offline");
        schedule();
      };
      socket.onerror = () => socket?.close();
    }

    function schedule() {
      retry = Math.min(retry + 1, 6);
      timer = window.setTimeout(connect, 1000 * 2 ** retry);
    }

    connect();
    return () => {
      closed = true;
      window.clearTimeout(timer);
      socket?.close();
    };
  }, [token]);

  return (
    <div className="flex flex-col gap-3 px-4 pt-4">
      <HudStatus api={api} database={database} ws={ws} liveEvents={liveEvents} />

      {tab === "sectors" && (
        <>
          {!token && (
            <p className="hud-panel rounded-lg p-4 text-sm">
              Set the operator token in Settings to load live sectors & agents.
            </p>
          )}
          <CityMap sectors={sectors} agents={agents} />
          <ul className="grid grid-cols-2 gap-2">
            {(sectors.length > 0
              ? sectors.map((s) => ({ code: s.code, name: s.name }))
              : FACTIONS
            ).map((s) => (
              <li key={s.code} className="hud-panel rounded-lg px-3 py-2 text-xs">
                <span className="font-bold text-infinitycyan">[{s.code}]</span> {s.name}
              </li>
            ))}
          </ul>
        </>
      )}

      {tab === "signals" && <SignalsPanel token={token} />}
      {tab === "reports" && <ReportsPanel token={token} />}
      {tab === "settings" && <SettingsDrawer token={token} onSave={saveToken} />}
    </div>
  );
}

export default function MapPage() {
  return (
    <Suspense
      fallback={
        <div className="px-4 pt-4">
          <p className="hud-panel rounded-lg p-4 text-sm uppercase tracking-widest text-infinitycyan/70">
            Loading city…
          </p>
        </div>
      }
    >
      <MapContent />
    </Suspense>
  );
}
