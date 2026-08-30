"use client";

import { useState } from "react";
import { useSearchParams } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { adminApi } from "@/features/admin/admin-api";
import { shippingApi } from "@/features/shipping/shipping-api";
import { SHIPMENT_STATUS_LABELS, CARRIER_NAMES } from "@/lib/constants";
import { formatDate } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Truck, Plus, CheckCircle2, Clock, MapPin, Search } from "lucide-react";
import { toast } from "sonner";
import { Suspense } from "react";

function AdminShippingContent() {
  const searchParams = useSearchParams();
  const initialOrderNo = searchParams.get("orderNumber") || "";
  const queryClient = useQueryClient();

  // Create Shipment state
  const [orderNumber, setOrderNumber] = useState(initialOrderNo);
  const [carrier, setCarrier] = useState("YURTICI");
  const [customTrackingNo, setCustomTrackingNo] = useState("");

  // Search state
  const [searchOrderNo, setSearchOrderNo] = useState(initialOrderNo);

  // Update Status state
  const [newStatus, setNewStatus] = useState("IN_TRANSIT");
  const [eventLocation, setEventLocation] = useState("");
  const [eventDescription, setEventDescription] = useState("");

  // Fetch shipment for searched order
  const {
    data: currentShipment,
    refetch: refetchShipment,
    isLoading: loadingShipment,
  } = useQuery({
    queryKey: ["admin-shipment-by-order", searchOrderNo],
    queryFn: () => shippingApi.getShipmentByOrderNumber(searchOrderNo.trim()),
    enabled: !!searchOrderNo.trim(),
    retry: false,
  });

  const createShipmentMutation = useMutation({
    mutationFn: () =>
      adminApi.createShipment({
        orderNumber: orderNumber.trim(),
        carrier,
        trackingNumber: customTrackingNo.trim() || undefined,
      }),
    onSuccess: (shipment) => {
      toast.success(`Kargo oluşturuldu! Takip No: ${shipment.trackingNumber}`);
      setSearchOrderNo(shipment.orderNumber);
      refetchShipment();
    },
    onError: (err: any) => {
      toast.error(err.message || "Kargo oluşturulamadı.");
    },
  });

  const updateShipmentStatusMutation = useMutation({
    mutationFn: () =>
      adminApi.updateShipmentStatus(currentShipment!.id, {
        status: newStatus,
        location: eventLocation.trim() || undefined,
        description: eventDescription.trim() || "Kargo durumu güncellendi.",
      }),
    onSuccess: () => {
      toast.success("Kargo durumu güncellendi.");
      setEventLocation("");
      setEventDescription("");
      refetchShipment();
    },
    onError: (err: any) => {
      toast.error(err.message || "Durum güncellenemedi.");
    },
  });

  const handleCreateSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!orderNumber.trim()) {
      toast.error("Lütfen sipariş numarasını girin.");
      return;
    }
    createShipmentMutation.mutate();
  };

  const statusInfo = currentShipment
    ? SHIPMENT_STATUS_LABELS[currentShipment.status] || {
        label: currentShipment.status,
        color: "bg-slate-100 text-slate-800",
      }
    : null;

  return (
    <div className="space-y-8 max-w-5xl">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
          Kargo & Gönderi Yönetimi
        </h1>
        <p className="text-xs text-muted-foreground mt-0.5">
          Siparişler için kargo gönderisi oluşturun, takip kodu atayın ve rota hareketlerini güncelleyin.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* LEFT 5 COLS: CREATE SHIPMENT */}
        <div className="lg:col-span-5 p-6 rounded-3xl border bg-white shadow-sm space-y-4">
          <div className="flex items-center gap-2 font-bold text-sm text-slate-900 border-b pb-3">
            <Plus className="w-4 h-4 text-accent-orange" />
            <span>Yeni Kargo Gönderisi Oluştur</span>
          </div>

          <form onSubmit={handleCreateSubmit} className="space-y-3.5 text-xs">
            <div>
              <label className="font-semibold text-slate-700 block mb-1">
                Sipariş Numarası *
              </label>
              <Input
                required
                placeholder="ORD-20260830-XXXXXX"
                value={orderNumber}
                onChange={(e) => setOrderNumber(e.target.value)}
                className="font-mono"
              />
            </div>

            <div>
              <label className="font-semibold text-slate-700 block mb-1">
                Kargo Firması *
              </label>
              <select
                value={carrier}
                onChange={(e) => setCarrier(e.target.value)}
                className="w-full h-10 px-3 rounded-xl border text-xs bg-background"
              >
                <option value="YURTICI">Yurtiçi Kargo</option>
                <option value="ARAS">Aras Kargo</option>
                <option value="MNG">MNG Kargo</option>
                <option value="PTT">PTT Kargo</option>
                <option value="MOCK">Hızlı Gönderi (Mock)</option>
              </select>
            </div>

            <div>
              <label className="font-semibold text-slate-700 block mb-1">
                Özel Takip No (Boş bırakılırsa otomatik üretilir)
              </label>
              <Input
                placeholder="TRK-20260830-XXXXXX"
                value={customTrackingNo}
                onChange={(e) => setCustomTrackingNo(e.target.value)}
                className="font-mono"
              />
            </div>

            <Button
              type="submit"
              variant="accent"
              disabled={createShipmentMutation.isPending}
              isLoading={createShipmentMutation.isPending}
              className="w-full py-5 rounded-xl font-bold text-xs"
            >
              Kargo Gönderisi Başlat
            </Button>
          </form>
        </div>

        {/* RIGHT 7 COLS: SEARCH & UPDATE SHIPMENT */}
        <div className="lg:col-span-7 space-y-6">
          {/* Search Box */}
          <div className="p-6 rounded-3xl border bg-white shadow-sm space-y-4">
            <div className="flex items-center gap-2 font-bold text-sm text-slate-900 border-b pb-3">
              <Search className="w-4 h-4 text-accent-orange" />
              <span>Siparişe Ait Kargo Sorgula</span>
            </div>

            <div className="flex gap-2 text-xs">
              <Input
                placeholder="ORD-... numarası girin"
                value={searchOrderNo}
                onChange={(e) => setSearchOrderNo(e.target.value)}
                className="font-mono"
              />
              <Button
                variant="outline"
                size="sm"
                onClick={() => refetchShipment()}
                className="text-xs"
              >
                Sorgula
              </Button>
            </div>

            {loadingShipment && (
              <div className="h-32 bg-muted animate-pulse rounded-2xl" />
            )}

            {currentShipment && statusInfo && (
              <div className="p-5 rounded-2xl bg-slate-50/70 border space-y-4 text-xs">
                <div className="flex flex-wrap items-center justify-between gap-2 border-b pb-3">
                  <div>
                    <span className="font-bold text-slate-900 text-sm">
                      {CARRIER_NAMES[currentShipment.carrier] || currentShipment.carrier}
                    </span>
                    <p className="font-mono text-slate-500 mt-0.5">
                      Takip No: <strong>{currentShipment.trackingNumber}</strong>
                    </p>
                  </div>
                  <span
                    className={`inline-flex px-3 py-1 rounded-full font-bold border ${statusInfo.color}`}
                  >
                    {statusInfo.label}
                  </span>
                </div>

                {/* Status updater form */}
                <div className="space-y-3 pt-2">
                  <h5 className="font-bold text-slate-800">Durum ve Konum Hareketi Ekle</h5>
                  <div className="grid grid-cols-2 gap-2">
                    <select
                      value={newStatus}
                      onChange={(e) => setNewStatus(e.target.value)}
                      className="h-9 px-2 rounded-lg border text-xs bg-white"
                    >
                      <option value="PICKED_UP">Kurye Teslim Aldı (PICKED_UP)</option>
                      <option value="IN_TRANSIT">Taşıma Durumunda (IN_TRANSIT)</option>
                      <option value="OUT_FOR_DELIVERY">Dağıtıma Çıktı (OUT_FOR_DELIVERY)</option>
                      <option value="DELIVERED">Teslim Edildi (DELIVERED)</option>
                      <option value="FAILED_DELIVERY">Teslim Edilemedi (FAILED)</option>
                      <option value="RETURNED">İade Edildi (RETURNED)</option>
                    </select>

                    <Input
                      placeholder="Konum (Örn: Kadıköy Dağıtım Merkezi)"
                      value={eventLocation}
                      onChange={(e) => setEventLocation(e.target.value)}
                      className="h-9 text-xs"
                    />
                  </div>

                  <Input
                    placeholder="Açıklama (Örn: Paket transfer merkezine ulaştı)"
                    value={eventDescription}
                    onChange={(e) => setEventDescription(e.target.value)}
                    className="h-9 text-xs"
                  />

                  <Button
                    variant="accent"
                    size="sm"
                    disabled={updateShipmentStatusMutation.isPending}
                    isLoading={updateShipmentStatusMutation.isPending}
                    onClick={() => updateShipmentStatusMutation.mutate()}
                    className="w-full"
                  >
                    Kargo Hareketini Kaydet
                  </Button>
                </div>

                {/* Tracking Events timeline */}
                <div className="space-y-3 pt-3 border-t">
                  <h5 className="font-bold text-slate-800 flex items-center gap-1.5">
                    <Clock className="w-3.5 h-3.5 text-accent-orange" />
                    <span>Tarihçe</span>
                  </h5>
                  <div className="space-y-2">
                    {currentShipment.events?.map((ev, idx) => (
                      <div
                        key={ev.id || idx}
                        className="p-2.5 rounded-xl bg-white border flex items-center justify-between text-[11px]"
                      >
                        <div>
                          <span className="font-semibold text-slate-900 block">{ev.description}</span>
                          {ev.location && <span className="text-slate-500">{ev.location}</span>}
                        </div>
                        <span className="text-muted-foreground font-mono">
                          {formatDate(ev.eventDate)}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function AdminShippingPage() {
  return (
    <Suspense
      fallback={
        <div className="h-64 bg-muted animate-pulse rounded-3xl" />
      }
    >
      <AdminShippingContent />
    </Suspense>
  );
}
