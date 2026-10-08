"use client";

import Link from "next/link";
import { usePathname, useSearchParams } from "next/navigation";

const ITEMS = [
  { href: "/", label: "Home", match: (p: string, t: string | null) => p === "/" },
  { href: "/map?tab=signals", label: "Signals", match: (p: string, t: string | null) => p === "/map" && t === "signals" },
  { href: "/map?tab=sectors", label: "Actions", match: (p: string, t: string | null) => p === "/map" && (t === "sectors" || t === null) },
  { href: "/map?tab=reports", label: "Reports", match: (p: string, t: string | null) => p === "/map" && t === "reports" },
  { href: "/map?tab=settings", label: "Settings", match: (p: string, t: string | null) => p === "/map" && t === "settings" },
];

export default function BottomNav() {
  const pathname = usePathname();
  const tab = useSearchParams().get("tab");
  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 border-t border-infinitycyan/30 bg-obsidian/90 backdrop-blur">
      <div className="mx-auto grid max-w-xl grid-cols-5">
        {ITEMS.map((item) => {
          const active = item.match(pathname, tab);
          return (
            <Link
              key={item.label}
              href={item.href}
              className={`py-3 text-center text-xs uppercase tracking-widest transition-colors ${
                active ? "text-corefire text-glow-fire" : "text-infinitycyan/70 hover:text-infinitycyan"
              }`}
            >
              {item.label}
            </Link>
          );
        })}
      </div>
    </nav>
  );
}
