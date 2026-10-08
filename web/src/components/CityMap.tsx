"use client";

import { useMemo, useRef } from "react";
import { Canvas, useFrame } from "@react-three/fiber";
import * as THREE from "three";
import type { Agent, Sector } from "@/lib/api";
import { factionColor } from "@/lib/factions";

const SCALE = 0.12;

interface Props {
  sectors: Sector[];
  agents: Agent[];
}

function SectorTower({ sector }: { sector: Sector }) {
  const ref = useRef<THREE.Mesh>(null);
  const height = 2 + (sector.code.length % 3);
  return (
    <mesh ref={ref} position={[sector.x * SCALE, height / 2, sector.y * SCALE]}>
      <boxGeometry args={[1.6, height, 1.6]} />
      <meshStandardMaterial
        color={factionColor(sector.code)}
        emissive={factionColor(sector.code)}
        emissiveIntensity={0.55}
        transparent
        opacity={0.9}
      />
    </mesh>
  );
}

function AgentMarker({ agent, sectors }: { agent: Agent; sectors: Sector[] }) {
  const ref = useRef<THREE.Mesh>(null);
  const position = useMemo<[number, number, number]>(() => {
    if (agent.x !== null && agent.y !== null) {
      return [agent.x * SCALE, 3.2, agent.y * SCALE];
    }
    const home = sectors.find((s) => s.code === agent.sector) ?? sectors[0];
    const hx = home ? home.x * SCALE : 0;
    const hz = home ? home.y * SCALE : 0;
    return [hx, 3.2, hz];
  }, [agent, sectors]);
  const masked = agent.x === null || agent.y === null;
  useFrame(({ clock }) => {
    if (!ref.current) return;
    const s = masked ? 0.28 : 0.34 + Math.sin(clock.elapsedTime * 3) * 0.06;
    ref.current.scale.setScalar(s);
  });
  return (
    <mesh ref={ref} position={position}>
      <sphereGeometry args={[1, 16, 16]} />
      <meshStandardMaterial
        color={masked ? "#546E7A" : factionColor(agent.faction)}
        emissive={masked ? "#000000" : factionColor(agent.faction)}
        emissiveIntensity={masked ? 0 : 0.9}
      />
    </mesh>
  );
}

function Rig({ children }: { children: React.ReactNode }) {
  const ref = useRef<THREE.Group>(null);
  useFrame((_, delta) => {
    if (ref.current) ref.current.rotation.y += delta * 0.05;
  });
  return <group ref={ref}>{children}</group>;
}

export default function CityMap({ sectors, agents }: Props) {
  const live = agents.filter((a) => a.status !== "offline");
  return (
    <div className="h-[52vh] w-full overflow-hidden rounded-lg border border-infinitycyan/25">
      <Canvas camera={{ position: [0, 14, 16], fov: 50 }} gl={{ antialias: true }}>
        <color attach="background" args={["#020202"]} />
        <fog attach="fog" args={["#020202", 18, 42]} />
        <ambientLight intensity={0.5} />
        <pointLight position={[10, 14, 10]} intensity={1.2} color="#00D9FF" />
        <pointLight position={[-10, 10, -10]} intensity={0.8} color="#FF6B00" />
        <Rig>
          <gridHelper args={[30, 30, "#00D9FF", "#0a2a33"]} />
          {sectors.map((s) => (
            <SectorTower key={s.code} sector={s} />
          ))}
          {live.map((a) => (
            <AgentMarker key={a.agent_id} agent={a} sectors={sectors} />
          ))}
        </Rig>
      </Canvas>
    </div>
  );
}
