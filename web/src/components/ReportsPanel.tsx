"use client";

import { useCallback, useEffect, useState } from "react";
import { getErrors, type ErrorItem } from "@/lib/api";

export default function ReportsPanel({ token }: { token: string }) {
  const [items, setItems] = useState<ErrorItem[]>([]);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    if (!token) return;
    try {
      setItems(await getErrors(token, 30));
      setError("");
    } catch (e) {
      setError(e instanceof Error ? e.message : "load failed");
    }
  }, [token]);

  useEffect(() => {
    load();
    const id = setInterval(load, 10000);
    return () => clearInterval(id);
  }, [load]);

  if (!token) return <p className="hud-panel rounded-lg p-4 text-sm">Set the operator token in Settings to read crash reports.</p>;
  if (error)
    return (
      <p className="hud-panel rounded-lg p-4 text-sm text-corefire">
        {error.includes("403") ? "Restricted to INF/DEF command & watch roles." : error}
      </p>
    );
  if (items.length === 0) return <p className="hud-panel rounded-lg p-4 text-sm">No crash reports — neural log is clean.</p>;

  return (
    <ul className="flex flex-col gap-2">
      {items.map((item) => (
        <li key={item.id} className="hud-panel rounded-lg p-3 text-sm">
          <div className="flex items-center gap-2">
            <span className="font-mono text-corefire">{item.exception_type}</span>
            <span className="ml-auto text-xs text-infinitycyan/50">
              {new Date(item.occurred_at).toLocaleString()}
            </span>
          </div>
          <p className="mt-1 text-infinitycyan/80">{item.message ?? "—"}</p>
          <p className="mt-1 font-mono text-xs text-infinitycyan/60">
            {item.app} {item.app_version ?? ""}
          </p>
        </li>
      ))}
    </ul>
  );
}
