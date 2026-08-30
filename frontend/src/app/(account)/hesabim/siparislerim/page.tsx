"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { orderApi } from "@/features/order/order-api";
import { formatPrice, formatDate } from "@/lib/utils";
import { ORDER_STATUS_LABELS } from "@/lib/constants";
import { Button } from "@/components/ui/button";
import { Package, Truck, ArrowRight, XCircle } from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";

export default function CustomerOrdersPage() {
  const queryClient = useQueryClient();

  const { data: orderPage, isLoading } = useQuery({
    queryKey: ["customer-orders"],
    queryFn: () => orderApi.getUserOrders(0, 20),
  });

  const cancelOrderMutation = useMutation({
    mutationFn: (orderNumber: string) =>
      orderApi.cancelOrder(orderNumber, "Müşteri paneli üzerinden iptal edildi."),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["customer-orders"] });
      toast.success("Siparişiniz başarıyla iptal edildi.");
    },
    onError: (err: any) => {
      toast.error(err.message || "Sipariş iptal edilemedi.");
    },
  });

  const orders = orderPage?.content || [];

  return (
    <div className="space-y-6">
      <div className="p-6 rounded-3xl border bg-white shadow-sm space-y-6">
        <div className="border-b pb-4">
          <h2 className="font-bold text-base text-slate-900 flex items-center gap-2">
            <Package className="w-5 h-5 text-accent-orange" />
            <span>Sipariş Geçmişim ({orders.length})</span>
          </h2>
          <p className="text-xs text-muted-foreground mt-0.5">
            Geçmiş ve devam eden tüm siparişlerinizi buradan inceleyebilir, kargo durumunu sorgulayabilirsiniz.
          </p>
        </div>

        {isLoading ? (
          <div className="space-y-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-32 bg-muted animate-pulse rounded-2xl" />
            ))}
          </div>
        ) : orders.length === 0 ? (
          <div className="text-center py-16 text-muted-foreground space-y-3">
            <Package className="w-12 h-12 text-slate-300 mx-auto" />
            <h4 className="font-bold text-slate-900 text-sm">Henüz Siparişiniz Yok</h4>
            <p className="text-xs max-w-sm mx-auto">
              Aracınıza tam uyumlu 3D havuzlu paspas ve bagaj havuzlarını inceleyerek ilk siparişinizi verebilirsiniz.
            </p>
            <Button asChild variant="accent" size="sm">
              <Link href="/katalog">Kataloğa Git</Link>
            </Button>
          </div>
        ) : (
          <div className="space-y-4">
            {orders.map((order) => {
              const statusInfo = ORDER_STATUS_LABELS[order.status] || {
                label: order.status,
                color: "bg-slate-100 text-slate-800",
              };
              const canCancel =
                order.status === "PENDING_PAYMENT" ||
                order.status === "PAID" ||
                order.status === "PROCESSING";

              return (
                <div
                  key={order.id}
                  className="p-5 rounded-2xl border bg-slate-50/50 hover:bg-slate-50 transition-colors space-y-4 text-xs"
                >
                  {/* Top Bar */}
                  <div className="flex flex-wrap items-center justify-between gap-2 border-b pb-3">
                    <div className="space-y-0.5">
                      <span className="text-[11px] text-muted-foreground">Sipariş No</span>
                      <p className="font-mono font-bold text-sm text-slate-900">
                        {order.orderNumber}
                      </p>
                    </div>

                    <div className="space-y-0.5">
                      <span className="text-[11px] text-muted-foreground">Sipariş Tarihi</span>
                      <p className="font-medium text-slate-700">
                        {formatDate(order.createdAt)}
                      </p>
                    </div>

                    <div className="space-y-0.5">
                      <span className="text-[11px] text-muted-foreground">Toplam Tutar</span>
                      <p className="font-extrabold text-sm text-slate-900">
                        {formatPrice(order.grandTotal)}
                      </p>
                    </div>

                    <div>
                      <span
                        className={`inline-flex px-3 py-1 rounded-full font-semibold border ${statusInfo.color}`}
                      >
                        {statusInfo.label}
                      </span>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="flex flex-wrap items-center justify-between gap-3 pt-1">
                    <div className="flex items-center gap-2">
                      <Link
                        href={`/siparis-takip?orderNumber=${order.orderNumber}`}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl border bg-white text-slate-700 hover:text-accent-orange font-semibold transition-colors"
                      >
                        <Truck className="w-3.5 h-3.5 text-accent-orange" />
                        Kargo Takip
                      </Link>

                      <Link
                        href={`/siparis-tamamlandi/${order.orderNumber}`}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl border bg-white text-slate-700 hover:text-slate-900 font-semibold transition-colors"
                      >
                        Detay İncele
                        <ArrowRight className="w-3.5 h-3.5" />
                      </Link>
                    </div>

                    {canCancel && (
                      <button
                        onClick={() => {
                          if (confirm("Bu siparişi iptal etmek istediğinizden emin misiniz?")) {
                            cancelOrderMutation.mutate(order.orderNumber);
                          }
                        }}
                        disabled={cancelOrderMutation.isPending}
                        className="inline-flex items-center gap-1 text-destructive hover:underline font-semibold cursor-pointer"
                      >
                        <XCircle className="w-3.5 h-3.5" /> Siparişi İptal Et
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
