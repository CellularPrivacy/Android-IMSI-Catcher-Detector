export type FactionCode = "INF" | "PRM" | "DEF" | "UNT" | "EQB" | "PKB";

export interface FactionMeta {
  code: FactionCode;
  name: string;
  mandate: string;
  color: string;
}

/** Static faction registry (display metadata; live state comes from the API). */
export const FACTIONS: FactionMeta[] = [
  { code: "INF", name: "Infinity Nation", mandate: "Swarm policy · capitol", color: "#00D9FF" },
  { code: "PRM", name: "Prometheus Corp", mandate: "Trade · R&D · resources", color: "#FFB300" },
  { code: "DEF", name: "Infinity Wardens", mandate: "Shield · patrols · debug", color: "#FF6B00" },
  { code: "UNT", name: "Unity Academy", mandate: "Research · RAG archives", color: "#7C4DFF" },
  { code: "EQB", name: "Equilibrium Order", mandate: "Law · arbitration", color: "#00E676" },
  { code: "PKB", name: "Peaky Clan", mandate: "Stealth · recon", color: "#B0BEC5" },
];

export function factionColor(code: string): string {
  return FACTIONS.find((f) => f.code === code)?.color ?? "#FFFFFF";
}
