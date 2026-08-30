"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { adminApi } from "@/features/admin/admin-api";
import { Order, OrderStatus } from "@/types";
import { ORDER_STATUS_LABELS } from "@/lib/constants";
import { formatPrice, formatDate } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Package, Eye, MapPin, Truck } from "lucide-react";
import { toast } from "sonner";
import Link from "next/link";

export default function AdminOrdersPage() {
  const queryClient = useQueryClient();
  const [statusFilter, setStatusFilter] = useState<OrderStatus | "">("");
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [newStatus, setNewStatus] = useState<OrderStatus>("PROCESSING");
  const [statusNote, setStatusNote] = useState("");

  const { data: orderPage, isLoading } = useQuery({
    queryKey: ["admin-orders", statusFilter],
    queryFn: () =>
      adminApi.getOrders({
        status: (statusFilter as OrderStatus) || undefined,
        size: 20,
      }),
  });

  const updateStatusMutation = useMutation({
    mutationFn: () =>
      adminApi.updateOrderStatus(selectedOrder!.id, newStatus, statusNote.trim() || undefined),
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ["admin-orders"] });
      setSelectedOrder(updated);
      setStatusNote("");
      toast.success("Sipariş durumu güncellendi.");
    },
    onError: (err: any) => {
      toast.error(err.message || "Durum güncellenemedi.");
    },
  });

  const orders = orderPage?.content || [];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
            Sipariş Yönetimi
          </h1>
          <p className="text-xs text-muted-foreground mt-0.5">
            Müşteri siparişlerini, ödeme onaylarını ve hazırlık süreçlerini yönetin.
          </p>
        </div>

        {/* Filter */}
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as any)}
          className="h-10 px-3 rounded-xl border text-xs bg-white shadow-sm"
        >
          <option value="">Tüm Sipariş Durumları</option>
          <option value="PAID">Ödeme Alındı (PAID)</option>
          <option value="PROCESSING">Hazırlanıyor (PROCESSING)</option>
          <option value="SHIPPED">Kargoya Verildi (SHIPPED)</option>
          <option value="DELIVERED">Teslim Edildi (DELIVERED)</option>
          <option value="PENDING_PAYMENT">Ödeme Bekliyor</option>
          <option value="CANCELLED">İptal Edildi</option>
        </select>
      </div>

      {/* Orders Table */}
      <div className="rounded-3xl border bg-white shadow-sm overflow-hidden">
        {isLoading ? (
          <div className="p-8 space-y-3">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-12 bg-muted animate-pulse rounded-xl" />
            ))}
          </div>
        ) : orders.length === 0 ? (
          <div className="text-center py-12 text-xs text-muted-foreground">
            Filtreye uygun sipariş bulunamadı.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 font-semibold border-b">
                <tr>
                  <th className="p-4">Sipariş No</th>
                  <th className="p-4">Müşteri / Alıcı</th>
                  <th className="p-4">Tarih</th>
                  <th className="p-4">Tutar</th>
                  <th className="p-4">Durum</th>
                  <th className="p-4 text-right">İşlemler</th>
                </tr>
              </thead>
              <tbody className="divide-y text-slate-700">
                {orders.map((order) => {
                  const statusInfo = ORDER_STATUS_LABELS[order.status] || {
                    label: order.status,
                    color: "bg-slate-100 text-slate-800",
                  };
                  return (
                    <tr key={order.id} className="hover:bg-slate-50 transition-colors">
                      <td className="p-4 font-mono font-bold text-slate-900">
                        {order.orderNumber}
                      </td>
                      <td className="p-4 font-medium">
                        {order.deliveryAddress?.recipientName || "Misafir"}
                      </td>
                      <td className="p-4 text-slate-500">{formatDate(order.createdAt)}</td>
                      <td className="p-4 font-extrabold text-slate-900">
                        {formatPrice(order.grandTotal)}
                      </td>
                      <td className="p-4">
                        <span
                          className={`inline-flex px-2.5 py-0.5 rounded-full text-[11px] font-semibold border ${statusInfo.color}`}
                        >
                          {statusInfo.label}
                        </span>
                      </td>
                      <td className="p-4 text-right">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => {
                            setSelectedOrder(order);
                            setNewStatus(order.status);
                          }}
                          className="h-8 gap-1 text-xs"
                        >
                          <Eye className="w-3.5 h-3.5" /> İncele & Yönet
                        </Button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ORDER DETAIL & STATUS DIALOG */}
      <Dialog open={!!selectedOrder} onOpenChange={(open) => !open && setSelectedOrder(null)}>
        <DialogContent className="sm:max-w-2xl max-h-[90vh] overflow-y-auto">
          {selectedOrder && (
            <>
              <DialogHeader>
                <DialogTitle className="flex items-center justify-between">
                  <span>Sipariş Detayı: {selectedOrder.orderNumber}</span>
                </DialogTitle>
              </DialogHeader>

              <div className="space-y-6 text-xs py-2">
                {/* Status Updater */}
                <div className="p-4 rounded-2xl bg-orange-50/50 border border-orange-200 space-y-3">
                  <h4 className="font-bold text-slate-900">Sipariş Durumunu Güncelle</h4>
                  <div className="flex flex-wrap gap-2">
                    <select
                      value={newStatus}
                      onChange={(e) => setNewStatus(e.target.value as OrderStatus)}
                      className="h-9 px-3 rounded-lg border text-xs bg-white"
                    >
                      <option value="PENDING_PAYMENT">Ödeme Bekliyor</option>
                      <option value="PAID">Ödeme Alındı</option>
                      <option value="PROCESSING">Hazırlanıyor</option>
                      <option value="SHIPPED">Kargoya Verildi</option>
                      <option value="DELIVERED">Teslim Edildi</option>
                      <option value="CANCELLED">İptal Edildi</option>
                      <option value="REFUNDED">İade Edildi</option>
                    </select>

                    <Input
                      placeholder="Durum güncelleme notu..."
                      value={statusNote}
                      onChange={(e) => setStatusNote(e.target.value)}
                      className="flex-1 min-w-[180px] h-9 text-xs"
                    />

                    <Button
                      variant="accent"
                      size="sm"
                      disabled={updateStatusMutation.isPending}
                      onClick={() => updateStatusMutation.mutate()}
                    >
                      Güncelle
                    </Button>
                  </div>
                </div>

                {/* Items */}
                <div className="space-y-2">
                  <h4 className="font-bold text-slate-900 flex items-center gap-1.5">
                    <Package className="w-4 h-4 text-accent-orange" />
                    <span>Sipariş Kalemleri ({selectedOrder.items?.length})</span>
                  </h4>
                  <div className="divide-y border rounded-2xl bg-slate-50/50 p-2">
                    {selectedOrder.items?.map((item) => (
                      <div key={item.id} className="p-2 flex items-center justify-between">
                        <div>
                          <p className="font-bold text-slate-900">{item.productName}</p>
                          <p className="text-muted-foreground font-mono text-[11px]">
                            SKU: {item.productSku} • {item.quantity} Adet
                          </p>
                        </div>
                        <span className="font-extrabold text-slate-900">
                          {formatPrice(item.totalPrice)}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Address & Financials */}
                <div className="grid grid-cols-2 gap-4">
                  <div className="p-3 rounded-xl border bg-white space-y-1">
                    <h5 className="font-bold text-slate-900 flex items-center gap-1">
                      <MapPin className="w-3.5 h-3.5 text-accent-orange" /> Teslimat Adresi
                    </h5>
                    <p className="font-semibold">{selectedOrder.deliveryAddress?.recipientName}</p>
                    <p className="text-slate-600">{selectedOrder.deliveryAddress?.fullAddress}</p>
                    <p className="text-slate-500">
                      {selectedOrder.deliveryAddress?.district} / {selectedOrder.deliveryAddress?.city}
                    </p>
                    <p className="text-slate-500 font-mono">Tel: {selectedOrder.deliveryAddress?.phoneNumber}</p>
                  </div>

                  <div className="p-3 rounded-xl border bg-white space-y-1">
                    <h5 className="font-bold text-slate-900">Tutar Özeti</h5>
                    <div className="flex justify-between text-slate-600">
                      <span>Ara Toplam:</span>
                      <span>{formatPrice(selectedOrder.subtotal)}</span>
                    </div>
                    <div className="flex justify-between text-slate-600">
                      <span>Kargo:</span>
                      <span>{formatPrice(selectedOrder.shippingTotal)}</span>
                    </div>
                    <div className="flex justify-between font-extrabold text-sm text-slate-900 pt-1 border-t">
                      <span>Genel Toplam:</span>
                      <span className="text-accent-orange">
                        {formatPrice(selectedOrder.grandTotal)}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="pt-2 flex justify-between items-center border-t">
                  <Link
                    href={`/admin/kargo?orderNumber=${selectedOrder.orderNumber}`}
                    className="inline-flex items-center gap-1.5 text-xs text-accent-orange font-bold hover:underline"
                  >
                    <Truck className="w-4 h-4" /> Kargo Gönderisi Oluştur
                  </Link>
                  <Button variant="outline" onClick={() => setSelectedOrder(null)}>
                    Kapat
                  </Button>
                </div>
              </div>
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
