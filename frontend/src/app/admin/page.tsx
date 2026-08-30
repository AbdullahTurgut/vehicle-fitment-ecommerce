"use client";

import { useQuery } from "@tanstack/react-query";
import { adminApi } from "@/features/admin/admin-api";
import { ORDER_STATUS_LABELS } from "@/lib/constants";
import { formatPrice, formatDate } from "@/lib/utils";
import {
  Package,
  ShoppingBag,
  Layers,
  Star,
  Plus,
  ArrowRight,
  TrendingUp,
} from "lucide-react";
import Link from "next/link";

export default function AdminDashboardPage() {
  const { data: orderPage, isLoading: loadingOrders } = useQuery({
    queryKey: ["admin-dashboard-orders"],
    queryFn: () => adminApi.getOrders({ size: 5 }),
  });

  const { data: productPage } = useQuery({
    queryKey: ["admin-dashboard-products"],
    queryFn: () => adminApi.getProducts({ size: 1 }),
  });

  const { data: categories = [] } = useQuery({
    queryKey: ["admin-dashboard-categories"],
    queryFn: () => adminApi.getCategories(),
  });

  const { data: reviews = [] } = useQuery({
    queryKey: ["admin-dashboard-reviews"],
    queryFn: () => adminApi.getReviews(),
  });

  const pendingReviews = reviews.filter((r) => r.status === "PENDING");
  const recentOrders = orderPage?.content || [];
  const totalOrders = orderPage?.totalElements || 0;
  const totalProducts = productPage?.totalElements || 0;

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
          Admin Kontrol Paneli
        </h1>
        <p className="text-xs sm:text-sm text-muted-foreground mt-1">
          Katalog, sipariş durumları, kargo gönderileri ve müşteri yorumlarının anlık özeti.
        </p>
      </div>

      {/* KPI METRIC CARDS */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {/* Total Orders */}
        <div className="p-6 rounded-3xl border bg-white shadow-sm space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500">Toplam Sipariş</span>
            <div className="w-10 h-10 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center">
              <Package className="w-5 h-5" />
            </div>
          </div>
          <div className="text-3xl font-black text-slate-900">{totalOrders}</div>
          <p className="text-[11px] text-emerald-600 font-medium flex items-center gap-1">
            <TrendingUp className="w-3.5 h-3.5" /> E-Ticaret akışı aktif
          </p>
        </div>

        {/* Total Products */}
        <div className="p-6 rounded-3xl border bg-white shadow-sm space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500">Katalogdaki Ürünler</span>
            <div className="w-10 h-10 rounded-2xl bg-orange-50 text-accent-orange flex items-center justify-center">
              <ShoppingBag className="w-5 h-5" />
            </div>
          </div>
          <div className="text-3xl font-black text-slate-900">{totalProducts}</div>
          <p className="text-[11px] text-muted-foreground">3D Paspas & Bagaj Havuzu</p>
        </div>

        {/* Categories */}
        <div className="p-6 rounded-3xl border bg-white shadow-sm space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500">Aktif Kategoriler</span>
            <div className="w-10 h-10 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center">
              <Layers className="w-5 h-5" />
            </div>
          </div>
          <div className="text-3xl font-black text-slate-900">{categories.length}</div>
          <p className="text-[11px] text-muted-foreground">Ana ve alt kategoriler</p>
        </div>

        {/* Pending Reviews */}
        <div className="p-6 rounded-3xl border bg-white shadow-sm space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500">Bekleyen Yorumlar</span>
            <div className="w-10 h-10 rounded-2xl bg-amber-50 text-amber-500 flex items-center justify-center">
              <Star className="w-5 h-5" />
            </div>
          </div>
          <div className="text-3xl font-black text-slate-900">{pendingReviews.length}</div>
          <p className="text-[11px] text-amber-600 font-medium">
            {pendingReviews.length > 0 ? "Moderasyon onayı bekliyor" : "Tüm yorumlar incelendi"}
          </p>
        </div>
      </div>

      {/* QUICK ACTIONS */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Link
          href="/admin/urunler"
          className="p-4 rounded-2xl border bg-white hover:border-accent-orange hover:bg-orange-50/40 transition-all flex items-center justify-between group shadow-sm"
        >
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-xl bg-orange-100 text-accent-orange flex items-center justify-center">
              <Plus className="w-4 h-4" />
            </div>
            <span className="font-bold text-xs text-slate-800 group-hover:text-accent-orange">
              Yeni Ürün & Uyumluluk Ekle
            </span>
          </div>
          <ArrowRight className="w-4 h-4 text-slate-400 group-hover:text-accent-orange group-hover:translate-x-1 transition-all" />
        </Link>

        <Link
          href="/admin/kategoriler"
          className="p-4 rounded-2xl border bg-white hover:border-indigo-500 hover:bg-indigo-50/40 transition-all flex items-center justify-between group shadow-sm"
        >
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-xl bg-indigo-100 text-indigo-600 flex items-center justify-center">
              <Plus className="w-4 h-4" />
            </div>
            <span className="font-bold text-xs text-slate-800 group-hover:text-indigo-600">
              Yeni Kategori Ekle
            </span>
          </div>
          <ArrowRight className="w-4 h-4 text-slate-400 group-hover:text-indigo-600 group-hover:translate-x-1 transition-all" />
        </Link>

        <Link
          href="/admin/kuponlar"
          className="p-4 rounded-2xl border bg-white hover:border-emerald-500 hover:bg-emerald-50/40 transition-all flex items-center justify-between group shadow-sm"
        >
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-xl bg-emerald-100 text-emerald-600 flex items-center justify-center">
              <Plus className="w-4 h-4" />
            </div>
            <span className="font-bold text-xs text-slate-800 group-hover:text-emerald-600">
              İndirim Kuponu Oluştur
            </span>
          </div>
          <ArrowRight className="w-4 h-4 text-slate-400 group-hover:text-emerald-600 group-hover:translate-x-1 transition-all" />
        </Link>
      </div>

      {/* RECENT ORDERS TABLE */}
      <div className="rounded-3xl border bg-white shadow-sm overflow-hidden space-y-4 p-6">
        <div className="flex items-center justify-between border-b pb-4">
          <div>
            <h3 className="font-bold text-base text-slate-900">Son Gelen Siparişler</h3>
            <p className="text-xs text-muted-foreground mt-0.5">
              En son verilen siparişler ve teslimat durumları.
            </p>
          </div>
          <Link
            href="/admin/siparisler"
            className="text-xs font-semibold text-accent-orange hover:underline"
          >
            Tüm Siparişleri Gör
          </Link>
        </div>

        {loadingOrders ? (
          <div className="space-y-2 py-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-12 bg-muted animate-pulse rounded-xl" />
            ))}
          </div>
        ) : recentOrders.length === 0 ? (
          <div className="text-center py-8 text-xs text-muted-foreground">
            Henüz verilmiş bir sipariş bulunmamaktadır.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 font-semibold border-b">
                <tr>
                  <th className="p-3">Sipariş No</th>
                  <th className="p-3">Alıcı</th>
                  <th className="p-3">Tarih</th>
                  <th className="p-3">Tutar</th>
                  <th className="p-3">Durum</th>
                  <th className="p-3 text-right">İşlem</th>
                </tr>
              </thead>
              <tbody className="divide-y text-slate-700">
                {recentOrders.map((order) => {
                  const statusInfo = ORDER_STATUS_LABELS[order.status] || {
                    label: order.status,
                    color: "bg-slate-100 text-slate-800",
                  };
                  return (
                    <tr key={order.id} className="hover:bg-slate-50/80 transition-colors">
                      <td className="p-3 font-mono font-bold text-slate-900">
                        {order.orderNumber}
                      </td>
                      <td className="p-3 font-medium">
                        {order.deliveryAddress?.recipientName || "Misafir Müşteri"}
                      </td>
                      <td className="p-3 text-slate-500">{formatDate(order.createdAt)}</td>
                      <td className="p-3 font-extrabold text-slate-900">
                        {formatPrice(order.grandTotal)}
                      </td>
                      <td className="p-3">
                        <span
                          className={`inline-flex px-2.5 py-0.5 rounded-full text-[11px] font-semibold border ${statusInfo.color}`}
                        >
                          {statusInfo.label}
                        </span>
                      </td>
                      <td className="p-3 text-right">
                        <Link
                          href="/admin/siparisler"
                          className="font-semibold text-accent-orange hover:underline"
                        >
                          İncele
                        </Link>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
