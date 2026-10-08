/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        obsidian: "#020202",
        corefire: "#FF6B00",
        infinitycyan: "#00D9FF",
      },
      fontFamily: {
        display: ["'Segoe UI'", "system-ui", "sans-serif"],
      },
      boxShadow: {
        "glow-fire": "0 0 24px rgba(255, 107, 0, 0.55)",
        "glow-cyan": "0 0 24px rgba(0, 217, 255, 0.45)",
      },
    },
  },
  plugins: [],
};
