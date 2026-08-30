import Link from "next/link";
import { Car, ShieldCheck, Lock } from "lucide-react";

export default function CheckoutLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-foreground">
      {/* Checkout Minimal Header */}
      <header className="bg-white border-b sticky top-0 z-40 shadow-xs">
        <div className="container mx-auto px-4 h-16 flex items-center justify-between">
          <Link href="/" className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-slate-900 flex items-center justify-center text-accent-orange shadow-md">
              <Car className="w-5 h-5" />
            </div>
            <span className="font-extrabold text-lg tracking-tight text-slate-900">
              OTO<span className="text-accent-orange">PASPAS</span>
            </span>
          </Link>

          <div className="flex items-center gap-2 text-xs font-semibold text-emerald-700 bg-emerald-50 px-3 py-1.5 rounded-full border border-emerald-200">
            <Lock className="w-3.5 h-3.5" />
            <span>256-Bit SSL Güvenli Ödeme</span>
          </div>
        </div>
      </header>

      {/* Checkout Content */}
      <main className="flex-1 py-8">{children}</main>

      {/* Checkout Minimal Footer */}
      <footer className="bg-white border-t py-6 text-xs text-muted-foreground text-center">
        <div className="container mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-2">
          <span>© {new Date().getFullYear()} OtoPaspas. Tüm Hakları Saklıdır.</span>
          <div className="flex items-center gap-4">
            <span className="flex items-center gap-1">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" /> %100 Güvenli Alışveriş
            </span>
            <span>Müşteri Hizmetleri: 0850 000 00 00</span>
          </div>
        </div>
      </footer>
    </div>
  );
}
