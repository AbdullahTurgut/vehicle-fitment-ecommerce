import { ShieldCheck, Truck, RotateCcw } from "lucide-react";

export function AnnouncementBar() {
  return (
    <div className="bg-primary text-primary-foreground text-xs py-2 px-4 border-b border-primary/20">
      <div className="container mx-auto flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-6 mx-auto md:mx-0">
          <span className="flex items-center gap-1.5 font-medium">
            <Truck className="w-3.5 h-3.5 text-accent-orange" />
            1.000 TL Üzeri Ücretsiz Kargo
          </span>
          <span className="hidden sm:flex items-center gap-1.5 text-slate-300">
            <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
            %100 Araca Birebir Uyum Garantisi
          </span>
          <span className="hidden md:flex items-center gap-1.5 text-slate-300">
            <RotateCcw className="w-3.5 h-3.5 text-amber-400" />
            14 Gün Koşulsuz İade
          </span>
        </div>
        <div className="hidden lg:flex items-center gap-4 text-slate-300">
          <span>Müşteri Destek: <strong className="text-white">0850 000 00 00</strong></span>
        </div>
      </div>
    </div>
  );
}
