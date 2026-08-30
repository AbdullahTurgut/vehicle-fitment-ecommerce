"use client";

import { Suspense, useState } from "react";
import { useSearchParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { shippingApi } from "@/features/shipping/shipping-api";
import { SHIPMENT_STATUS_LABELS, CARRIER_NAMES } from "@/lib/constants";
import { formatDate } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Truck,
  Search,
  CheckCircle2,
  Clock,
  MapPin,
  PackageCheck,
  AlertCircle,
} from "lucide-react";

function TrackingContent() {
  const searchParams = useSearchParams();
  const initialTrackingNo = searchParams.get("tracking") || "";
  const initialOrderNo = searchParams.get("orderNumber") || "";

  const [queryInput, setQueryInput] = useState(initialTrackingNo || initialOrderNo);
  const [activeQuery, setActiveQuery] = useState(initialTrackingNo || initialOrderNo);

  const isTrackingNumber = activeQuery.startsWith("TRK-") || activeQuery.length > 10;

  const {
    data: shipment,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["shipment-tracking", activeQuery],
    queryFn: () => {
      if (isTrackingNumber) {
        return shippingApi.getShipmentByTrackingNumber(activeQuery.trim());
      } else {
        return shippingApi.getShipmentByOrderNumber(activeQuery.trim());
      }
    },
    enabled: !!activeQuery.trim(),
    retry: false,
  });

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (queryInput.trim()) {
      setActiveQuery(queryInput.trim());
    }
  };

  const statusInfo = shipment
    ? SHIPMENT_STATUS_LABELS[shipment.status] || {
        label: shipment.status,
        color: "bg-slate-100 text-slate-800",
      }
    : null;

  const carrierName = shipment ? CARRIER_NAMES[shipment.carrier] || shipment.carrier : "";

  return (
    <div className="container mx-auto px-4 py-12 max-w-3xl space-y-8">
      {/* Header */}
      <div className="text-center space-y-2">
        <div className="w-14 h-14 rounded-2xl bg-orange-50 text-accent-orange flex items-center justify-center mx-auto shadow-sm">
          <Truck className="w-7 h-7" />
        </div>
        <h1 className="text-3xl font-extrabold text-slate-900">
          Kargo ve Sipariş Takibi
        </h1>
        <p className="text-xs sm:text-sm text-muted-foreground max-w-md mx-auto">
          Sipariş numaranızı (Örn: ORD-...) veya kargo takip kodunuzu (Örn: TRK-...) girerek anlık kargo durumunu öğrenin.
        </p>
      </div>

      {/* Search Input Form */}
      <form onSubmit={handleSearch} className="max-w-xl mx-auto flex gap-2">
        <div className="relative flex-1">
          <Input
            placeholder="Takip no veya Sipariş no girin (ORD-... / TRK-...)"
            value={queryInput}
            onChange={(e) => setQueryInput(e.target.value)}
            className="h-12 pl-11 text-xs sm:text-sm rounded-xl font-mono uppercase"
          />
          <Search className="w-4 h-4 text-slate-400 absolute left-4 top-4 pointer-events-none" />
        </div>
        <Button type="submit" variant="accent" size="lg" className="rounded-xl px-6 font-bold text-xs sm:text-sm">
          Sorgula
        </Button>
      </form>

      {/* Tracking Result */}
      {isLoading && (
        <div className="p-8 rounded-3xl border bg-white shadow-sm space-y-4 max-w-xl mx-auto">
          <div className="h-6 bg-muted animate-pulse rounded-lg w-1/3" />
          <div className="h-20 bg-muted animate-pulse rounded-2xl" />
          <div className="h-32 bg-muted animate-pulse rounded-2xl" />
        </div>
      )}

      {error && (
        <div className="p-6 rounded-3xl border bg-white shadow-sm text-center max-w-xl mx-auto space-y-2 text-xs">
          <AlertCircle className="w-8 h-8 text-amber-500 mx-auto" />
          <h4 className="font-bold text-sm text-slate-900">Gönderi Kaydı Bulunamadı</h4>
          <p className="text-muted-foreground">
            Girdiğiniz numara ile eşleşen bir kargo kaydı bulunamadı. Lütfen numarayı doğru yazdığınızdan emin olun.
          </p>
        </div>
      )}

      {shipment && statusInfo && (
        <div className="p-6 sm:p-8 rounded-3xl border bg-white shadow-sm space-y-6 max-w-2xl mx-auto">
          {/* Shipment summary */}
          <div className="flex flex-wrap items-center justify-between gap-4 border-b pb-6">
            <div>
              <span className="text-xs text-muted-foreground block">Kargo Firması</span>
              <h3 className="font-extrabold text-base text-slate-900">{carrierName}</h3>
              <p className="text-xs text-slate-500 font-mono mt-0.5">
                Takip No: <strong>{shipment.trackingNumber}</strong>
              </p>
            </div>

            <div>
              <span className="text-xs text-muted-foreground block mb-1">Mevcut Durum</span>
              <span
                className={`inline-flex px-3 py-1 rounded-full text-xs font-bold border ${statusInfo.color}`}
              >
                {statusInfo.label}
              </span>
            </div>
          </div>

          {/* Timeline */}
          <div className="space-y-4">
            <h4 className="font-bold text-sm text-slate-900 flex items-center gap-2">
              <Clock className="w-4 h-4 text-accent-orange" />
              <span>Kargo Hareketleri ve Rota Tarihçesi</span>
            </h4>

            {shipment.events && shipment.events.length > 0 ? (
              <div className="relative pl-6 space-y-6 before:absolute before:left-2.5 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-200">
                {shipment.events.map((event, idx) => (
                  <div key={event.id || idx} className="relative space-y-1 text-xs">
                    <div className="absolute -left-6 top-0.5 w-5 h-5 rounded-full bg-slate-900 text-white flex items-center justify-center text-[10px] font-bold">
                      {idx === 0 ? "✓" : "•"}
                    </div>
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <span className="font-bold text-slate-900 text-sm">
                        {event.description}
                      </span>
                      <span className="text-muted-foreground font-mono text-[11px]">
                        {formatDate(event.eventDate)}
                      </span>
                    </div>
                    {event.location && (
                      <p className="text-slate-500 flex items-center gap-1">
                        <MapPin className="w-3 h-3 text-accent-orange" />
                        {event.location}
                      </p>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-xs text-muted-foreground py-2">
                Henüz kargo hareketi kaydedilmemiş.
              </p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default function TrackingPage() {
  return (
    <Suspense
      fallback={
        <div className="container mx-auto px-4 py-12 max-w-3xl">
          <div className="h-64 bg-muted animate-pulse rounded-3xl" />
        </div>
      }
    >
      <TrackingContent />
    </Suspense>
  );
}
