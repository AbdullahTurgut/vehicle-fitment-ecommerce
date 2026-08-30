"use client";

import { useVehicleStore } from "@/stores/vehicle-store";
import { Car, ChevronRight, X, Sparkles } from "lucide-react";
import Link from "next/link";

export function VehicleBar() {
  const { selectedVehicle, setSelectorOpen, clearVehicle } = useVehicleStore();

  if (!selectedVehicle) {
    return (
      <div className="bg-slate-900 text-white py-2.5 px-4 shadow-inner">
        <div className="container mx-auto flex flex-col sm:flex-row items-center justify-between gap-2 text-sm">
          <div className="flex items-center gap-2 text-slate-300">
            <Car className="w-4 h-4 text-accent-orange animate-pulse" />
            <span>Aracınıza birebir uyumlu ürünleri listelemek için araç seçimi yapın:</span>
          </div>
          <button
            onClick={() => setSelectorOpen(true)}
            className="inline-flex items-center gap-1.5 px-4 py-1 rounded-full bg-accent-orange text-white text-xs font-semibold hover:bg-accent-orange/90 transition-transform active:scale-95 shadow-sm"
          >
            <Sparkles className="w-3.5 h-3.5" />
            Aracını Seç
            <ChevronRight className="w-3 h-3" />
          </button>
        </div>
      </div>
    );
  }

  const vehicleName = [
    selectedVehicle.year,
    selectedVehicle.brand.name,
    selectedVehicle.model.name,
    selectedVehicle.generation.name,
    selectedVehicle.variant?.name,
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <div className="bg-emerald-900/90 text-emerald-100 py-2 px-4 border-b border-emerald-800/50 backdrop-blur-sm">
      <div className="container mx-auto flex flex-wrap items-center justify-between gap-2 text-xs sm:text-sm">
        <div className="flex items-center gap-2">
          <span className="inline-flex items-center justify-center w-5 h-5 rounded-full bg-emerald-500 text-white font-bold text-xs">
            ✓
          </span>
          <span className="text-emerald-300">Seçili Aracınız:</span>
          <strong className="text-white font-semibold">{vehicleName}</strong>
        </div>
        <div className="flex items-center gap-3">
          <Link
            href={`/katalog?variantId=${selectedVehicle.variant?.id || ""}`}
            className="text-white underline hover:text-emerald-200 text-xs font-medium"
          >
            Uyumlu Ürünleri Gör
          </Link>
          <button
            onClick={() => setSelectorOpen(true)}
            className="px-2.5 py-0.5 rounded bg-emerald-800 text-emerald-100 hover:bg-emerald-700 text-xs font-medium transition-colors"
          >
            Değiştir
          </button>
          <button
            onClick={clearVehicle}
            title="Araç seçimini temizle"
            className="p-1 rounded text-emerald-300 hover:text-white hover:bg-emerald-800 transition-colors"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>
  );
}
