"use client";

import { use } from "react";
import { useQuery } from "@tanstack/react-query";
import { orderApi } from "@/features/order/order-api";
import { formatPrice, formatDate } from "@/lib/utils";
import { ORDER_STATUS_LABELS } from "@/lib/constants";
import { Button } from "@/components/ui/button";
import {
  CheckCircle2,
  Package,
  MapPin,
  Calendar,
  CreditCard,
  Truck,
  ArrowRight,
  Sparkles,
} from "lucide-react";
import Link from "next/link";

export default function OrderSuccessPage({
  params,
}: {
  params: Promise<{ orderNumber: string }>;
}) {
  const { orderNumber } = use(params);

  const { data: order, isLoading } = useQuery({
    queryKey: ["order-detail", orderNumber],
    queryFn: () => orderApi.getOrderByNumber(orderNumber),
  });

  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-16 text-center">
        <div className="h-64 bg-muted animate-pulse rounded-3xl max-w-xl mx-auto" />
      </div>
    );
  }

  if (!order) {
    return (
      <div className="container mx-auto px-4 py-16 text-center space-y-4">
        <h2 className="text-2xl font-bold text-slate-900">Sipariş Bulunamadı</h2>
        <Button asChild variant="accent">
          <Link href="/">Anasayfaya Dön</Link>
        </Button>
      </div>
    );
  }

  const statusInfo = ORDER_STATUS_LABELS[order.status] || {
    label: order.status,
    color: "bg-slate-100 text-slate-800",
  };

  return (
    <div className="container mx-auto px-4 py-12 max-w-3xl space-y-8">
      {/* SUCCESS HERO HEADER */}
      <div className="text-center space-y-3">
        <div className="w-16 h-16 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center mx-auto shadow-sm">
          <CheckCircle2 className="w-10 h-10" />
        </div>
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-50 text-emerald-700 text-xs font-semibold">
          <Sparkles className="w-3.5 h-3.5" /> Ödeme Başarıyla Alındı
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 tracking-tight">
          Siparişiniz Başarıyla Oluşturuldu!
        </h1>
        <p className="text-xs sm:text-sm text-muted-foreground max-w-md mx-auto">
          Sipariş onayınız ve fatura detaylarınız e-posta adresinize gönderildi. Ürünleriniz hazırlanıp kargoya verildiğinde SMS ile bilgilendirileceksiniz.
        </p>
      </div>

      {/* ORDER SUMMARY CARD */}
      <div className="rounded-3xl border bg-white shadow-sm overflow-hidden divide-y">
        {/* Top summary row */}
        <div className="p-6 bg-slate-50/50 flex flex-wrap items-center justify-between gap-4">
          <div>
            <span className="text-xs text-muted-foreground block">Sipariş Numarası</span>
            <span className="font-mono font-extrabold text-base sm:text-lg text-slate-900">
              {order.orderNumber}
            </span>
          </div>
          <div>
            <span className="text-xs text-muted-foreground block">Sipariş Tarihi</span>
            <span className="font-medium text-xs sm:text-sm text-slate-800">
              {formatDate(order.createdAt)}
            </span>
          </div>
          <div>
            <span className="text-xs text-muted-foreground block">Durum</span>
            <span className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-semibold border ${statusInfo.color}`}>
              {statusInfo.label}
            </span>
          </div>
        </div>

        {/* Ordered items */}
        <div className="p-6 space-y-4">
          <h4 className="font-bold text-sm text-slate-900 flex items-center gap-2">
            <Package className="w-4 h-4 text-accent-orange" />
            <span>Sipariş Edilen Ürünler ({order.items.length})</span>
          </h4>

          <div className="divide-y text-xs">
            {order.items.map((item) => (
              <div key={item.id} className="py-3 flex items-center justify-between gap-4">
                <div className="min-w-0">
                  <p className="font-semibold text-slate-900 truncate">{item.productName}</p>
                  <p className="text-[11px] text-muted-foreground font-mono">
                    SKU: {item.productSku} • {item.quantity} Adet
                  </p>
                </div>
                <span className="font-bold text-slate-900 shrink-0">
                  {formatPrice(item.totalPrice)}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Address and Financials */}
        <div className="p-6 grid grid-cols-1 sm:grid-cols-2 gap-6 text-xs">
          <div className="space-y-1.5">
            <h5 className="font-bold text-slate-900 flex items-center gap-1.5">
              <MapPin className="w-4 h-4 text-accent-orange" /> Teslimat Adresi
            </h5>
            <p className="font-semibold text-slate-800">{order.deliveryAddress.recipientName}</p>
            <p className="text-slate-600">{order.deliveryAddress.fullAddress}</p>
            <p className="text-slate-500">
              {order.deliveryAddress.district} / {order.deliveryAddress.city}
            </p>
            <p className="text-slate-500 font-mono">Tel: {order.deliveryAddress.phoneNumber}</p>
          </div>

          <div className="space-y-2 bg-slate-50 p-4 rounded-2xl">
            <div className="flex justify-between text-slate-600">
              <span>Ara Toplam</span>
              <span className="font-medium text-slate-900">{formatPrice(order.subtotal)}</span>
            </div>
            {order.discountTotal > 0 && (
              <div className="flex justify-between text-emerald-600 font-semibold">
                <span>İndirim</span>
                <span>-{formatPrice(order.discountTotal)}</span>
              </div>
            )}
            <div className="flex justify-between text-slate-600">
              <span>Kargo</span>
              <span>
                {order.shippingTotal === 0 ? (
                  <strong className="text-emerald-600 font-bold">ÜCRETSİZ</strong>
                ) : (
                  formatPrice(order.shippingTotal)
                )}
              </span>
            </div>
            <div className="pt-2 border-t flex justify-between font-bold text-sm text-slate-900">
              <span>Toplam Ödenen</span>
              <span className="text-accent-orange text-base">{formatPrice(order.grandTotal)}</span>
            </div>
          </div>
        </div>
      </div>

      {/* ACTION BUTTONS */}
      <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4">
        <Button asChild variant="outline" size="lg" className="w-full sm:w-auto rounded-xl">
          <Link href="/hesabim/siparislerim">Siparişlerimi Görüntüle</Link>
        </Button>
        <Button asChild variant="accent" size="lg" className="w-full sm:w-auto rounded-xl">
          <Link href="/katalog" className="flex items-center gap-2">
            <span>Alışverişe Devam Et</span>
            <ArrowRight className="w-4 h-4" />
          </Link>
        </Button>
      </div>
    </div>
  );
}
