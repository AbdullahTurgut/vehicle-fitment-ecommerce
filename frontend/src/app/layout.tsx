import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { Providers } from "./providers";

const inter = Inter({
  subsets: ["latin", "latin-ext"],
  variable: "--font-inter",
});

export const metadata: Metadata = {
  title: {
    template: "%s | Oto Paspas & Bagaj Havuzu",
    default: "Oto Paspas & Bagaj Havuzu | Araca Özel 3D Havuzlu Paspas",
  },
  description:
    "Aracınıza birebir uyumlu 3D ve 5D havuzlu oto paspas ve bagaj havuzları. Lazer kesim, koku yapmayan TPE malzeme ve ücretsiz kargo avantajıyla hemen sipariş verin.",
  keywords: [
    "oto paspas",
    "bagaj havuzu",
    "3d paspas",
    "araca özel paspas",
    "havuzlu paspas",
    "araba paspası",
  ],
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="tr" className={inter.variable}>
      <body className="min-h-screen bg-background text-foreground antialiased flex flex-col">
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
