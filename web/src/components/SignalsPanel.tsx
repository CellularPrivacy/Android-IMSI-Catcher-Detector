"use client";

import { useCallback, useEffect, useState } from "react";
import { getTelemetry, type TelemetryItem } from "@/lib/api";

const THREAT_COLORS: Record<string, string> = {
  DANGER: "#FF3D00",
  HIGH: "#FF6B00",
  MEDIUM: "#FFB300",
  OK: "#00E676",
  IDLE: "#546E7A",
};

export default function SignalsPanel({ token }: { token: string }) {
  const [items, setItems] = useState<TelemetryItem[]>([]);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    if (!token) return;
    try {
      setItems(await getTelemetry(token, 30));
      setError("");
    } catch (e) {
      setError(e instanceof Error ? e.message : "load failed");
    }
  }, [token]);

  useEffect(() => {
    load();
    const id = setInterval(load, 5000);
    return () => clearInterval(id);
  }, [load]);

  if (!token) return <p className="hud-panel rounded-lg p-4 text-sm">Set the operator token in Settings to stream signals.</p>;
  if (error) return <p className="hud-panel rounded-lg p-4 text-sm text-corefire">{error}</p>;
  if (items.length === 0) return <p className="hud-panel rounded-lg p-4 text-sm">No signals yet — the swarm is quiet.</p>;

  return (
    <ul className="flex flex-col gap-2">
      {items.map((item) => (
        <li key={item.id} className="hud-panel rounded-lg p-3 text-sm">
          <div className="flex items-center gap-2">
            <span
              className="rounded px-2 py-0.5 text-[11px] font-bold uppercase tracking-widest"
              style={{ background: THREAT_COLORS[item.threat_level] ?? "#546E7A", color: "#020202" }}
            >
              {item.threat_level}
            </span>
            <span className="text-infinitycyan/70">DF-{item.event_id}</span>
            <span className="ml-auto text-xs text-infinitycyan/50">
              {new Date(item.occurred_at).toLocaleString()}
            </span>
          </div>
          <p className="mt-1.5">{item.description ?? "—"}</p>
          <p className="mt-1 text-xs text-infinitycyan/60">
            {[item.faction && `[${item.faction}]`, item.device_id, item.apn && `APN ${item.apn}`]
              .filter(Boolean)
              .join(" · ")}
          </p>
        </li>
      ))}
    </ul>
  );
}
