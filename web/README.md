# Prometheus Project: Infinity — Web Console

Mobile-first luxury UI (DEF D-Code): the **Origins** entry screen plus the **Agent
City** live 3D command map. Next.js 14 + TypeScript + Tailwind (DEF-STEEL palette:
Obsidian `#020202`, Core Fire `#FF6B00`, Infinity Cyan `#00D9FF`) + Framer Motion +
Three.js via React Three Fiber. PWA-enabled (manifest + hand-rolled service worker).

## Run

```bash
npm install
export NEXT_PUBLIC_API_BASE='https://api.prometheus.example'
npm run dev      # http://localhost:3000
```

Production: `npm run build && npm start` (standalone output, see `Dockerfile` hunt in
repo root compose later). `npm run lint` for ESLint (`next/core-web-vitals`).

`NEXT_PUBLIC_API_BASE` must be `https://` — the API client refuses plain HTTP
(except `localhost`, for development).

## Routes

| Route | Content |
|---|---|
| `/` | Origins landing: neon geometry, generated ambient drone, fast launch. |
| `/map?tab=sectors` | Agent City 3D overlay: 6 faction towers, live agent markers (5 s poll). |
| `/map?tab=signals` | Live detection-telemetry feed (5 s poll + WS live counter). |
| `/map?tab=reports` | Neural crash-report history (INF/DEF command & watch only). |
| `/map?tab=settings` | Operator session: paste a server-minted JWT (browser-local only). |

Bottom navigation (Home · Signals · Actions · Reports · Settings) maps onto these
routes — every tab is live data, no placeholder screens.

## Data & security notes

- No secrets ship with the client; tokens are minted server-side
  (`backend/README.md` → provisioning) and pasted in Settings.
- Masked agents (other factions) render as grey markers at their home sector —
  coordinates never reach the browser for them (masked server-side).
- The service worker caches the app shell + static assets; API responses are
  network-first so the HUD never renders stale city state.
