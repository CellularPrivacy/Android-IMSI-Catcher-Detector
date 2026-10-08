"use client";

import { useState } from "react";
import { apiBase } from "@/lib/api";

interface Props {
  token: string;
  onSave: (token: string) => void;
}

export default function SettingsDrawer({ token, onSave }: Props) {
  const [draft, setDraft] = useState(token);
  const [endpoint, setEndpoint] = useState(() => {
    try {
      return apiBase();
    } catch (e) {
      return e instanceof Error ? e.message : "unconfigured";
    }
  });

  return (
    <div className="hud-panel rounded-lg p-4">
      <h2 className="text-sm font-bold uppercase tracking-widest text-infinitycyan">Operator session</h2>
      <p className="mt-2 break-all font-mono text-xs text-infinitycyan/70">{endpoint}</p>
      <label className="mt-4 block text-xs uppercase tracking-widest text-infinitycyan/70">
        Bearer JWT (stored only in this browser)
      </label>
      <input
        type="password"
        value={draft}
        onChange={(e) => setDraft(e.target.value.trim())}
        placeholder="paste operator or device token"
        className="mt-1 w-full rounded border border-infinitycyan/30 bg-obsidian px-3 py-2 font-mono text-sm outline-none placeholder:text-infinitycyan/30 focus:border-infinitycyan"
      />
      <div className="mt-3 flex gap-2">
        <button
          onClick={() => {
            onSave(draft);
            try {
              setEndpoint(apiBase());
            } catch (e) {
              setEndpoint(e instanceof Error ? e.message : "unconfigured");
            }
          }}
          className="rounded border border-corefire bg-corefire/10 px-4 py-2 text-xs font-bold uppercase tracking-widest text-corefire"
        >
          Connect
        </button>
        <button
          onClick={() => {
            setDraft("");
            onSave("");
          }}
          className="rounded border border-infinitycyan/30 px-4 py-2 text-xs uppercase tracking-widest text-infinitycyan/70"
        >
          Clear
        </button>
      </div>
      <p className="mt-3 text-xs text-infinitycyan/50">
        No secrets ship with the client: mint tokens server-side and paste them here. HTTPS is enforced (plain HTTP
        refused except localhost).
      </p>
    </div>
  );
}
