"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { motion } from "framer-motion";

/** Generated ambient drone (WebAudio oscillators) — no audio assets needed. */
function useAmbient() {
  const ctxRef = useRef<AudioContext | null>(null);
  const [playing, setPlaying] = useState(false);

  useEffect(
    () => () => {
      ctxRef.current?.close().catch(() => undefined);
    },
    []
  );

  function toggle() {
    if (playing) {
      ctxRef.current?.close().catch(() => undefined);
      ctxRef.current = null;
      setPlaying(false);
      return;
    }
    const Ctx = window.AudioContext;
    const ctx = new Ctx();
    const master = ctx.createGain();
    master.gain.value = 0.05;
    master.connect(ctx.destination);
    [55, 82.5, 110.3].forEach((freq, i) => {
      const osc = ctx.createOscillator();
      osc.type = i === 2 ? "triangle" : "sine";
      osc.frequency.value = freq;
      const lfo = ctx.createOscillator();
      lfo.frequency.value = 0.05 + i * 0.03;
      const lfoGain = ctx.createGain();
      lfoGain.gain.value = 0.02;
      lfo.connect(lfoGain).connect(master.gain);
      osc.connect(master);
      osc.start();
      lfo.start();
    });
    ctxRef.current = ctx;
    setPlaying(true);
  }

  return { playing, toggle };
}

export default function OriginsLanding() {
  const { playing, toggle } = useAmbient();
  return (
    <div className="flex min-h-[calc(100vh-8rem)] flex-col items-center justify-center px-6 text-center">
      <motion.div
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.8 }}
        className="mb-6 h-24 w-24 rotate-45 border-2 border-corefire shadow-glow-fire"
      >
        <div className="flex h-full w-full -rotate-45 items-center justify-center">
          <span className="text-4xl text-infinitycyan text-glow-cyan">∞</span>
        </div>
      </motion.div>
      <motion.h1
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="text-4xl font-bold uppercase tracking-[0.3em] text-glow-cyan"
      >
        Infinity
      </motion.h1>
      <motion.p
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.4 }}
        className="mt-3 max-w-sm text-sm uppercase tracking-widest text-infinitycyan/70"
      >
        Prometheus Project · Phase 2 · Agent City command interface
      </motion.p>
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.6 }}
        className="mt-10 flex w-full max-w-xs flex-col gap-3"
      >
        <Link
          href="/map?tab=sectors"
          className="rounded border border-corefire bg-corefire/10 px-6 py-3 text-sm font-bold uppercase tracking-widest text-corefire shadow-glow-fire transition hover:bg-corefire/25"
        >
          Enter the City
        </Link>
        <button
          onClick={toggle}
          className="rounded border border-infinitycyan/40 px-6 py-3 text-sm uppercase tracking-widest text-infinitycyan/80 transition hover:bg-infinitycyan/10"
        >
          {playing ? "Mute ambient" : "Play ambient"}
        </button>
      </motion.div>
    </div>
  );
}
