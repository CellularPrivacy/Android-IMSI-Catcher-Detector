"use client";

export type WsState = "live" | "connecting" | "offline";

interface Props {
  api: "up" | "down" | "unknown";
  database: string;
  ws: WsState;
  liveEvents: number;
}

function Dot({ on, color }: { on: boolean; color: string }) {
  return (
    <span
      className="inline-block h-2 w-2 rounded-full"
      style={{ background: color, opacity: on ? 1 : 0.25, boxShadow: on ? `0 0 8px ${color}` : "none" }}
    />
  );
}

export default function HudStatus({ api, database, ws, liveEvents }: Props) {
  return (
    <div className="hud-panel flex items-center gap-4 rounded-lg px-3 py-2 text-[11px] uppercase tracking-widest">
      <span className="flex items-center gap-1.5">
        <Dot on={api === "up"} color="#00D9FF" /> API {api}
      </span>
      <span className="flex items-center gap-1.5">
        <Dot on={database === "up"} color="#00E676" /> DB {database}
      </span>
      <span className="flex items-center gap-1.5">
        <Dot on={ws === "live"} color="#FF6B00" /> Stream {ws}
      </span>
      <span className="ml-auto text-infinitycyan/80">+{liveEvents} live</span>
    </div>
  );
}
