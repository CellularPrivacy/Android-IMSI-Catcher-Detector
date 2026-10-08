import { Suspense } from "react";
import type { Metadata, Viewport } from "next";
import BottomNav from "@/components/BottomNav";
import ServiceWorker from "@/components/ServiceWorker";
import "@/styles/globals.css";

export const metadata: Metadata = {
  title: "Infinity — Prometheus Project",
  description: "Origins entry + Agent City live 3D command map.",
  manifest: "/manifest.json",
};

export const viewport: Viewport = {
  themeColor: "#020202",
  width: "device-width",
  initialScale: 1,
  viewportFit: "cover",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="scanlines bg-obsidian">
        <ServiceWorker />
        <main className="mx-auto max-w-xl pb-20">{children}</main>
        <Suspense>
          <BottomNav />
        </Suspense>
      </body>
    </html>
  );
}
