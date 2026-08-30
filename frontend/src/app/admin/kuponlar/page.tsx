"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { adminApi } from "@/features/admin/admin-api";
import { Coupon, DiscountType } from "@/types";
import { formatPrice } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Tag, Plus, CheckCircle2, XCircle } from "lucide-react";
import { toast } from "sonner";

export default function AdminCouponsPage() {
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Form states
  const [code, setCode] = useState("");
  const [discountType, setDiscountType] = useState<DiscountType>("PERCENTAGE");
  const [discountValue, setDiscountValue] = useState("");
  const [minOrderAmount, setMinOrderAmount] = useState("");
  const [maxDiscountAmount, setMaxDiscountAmount] = useState("");
  const [usageLimit, setUsageLimit] = useState("100");

  const { data: coupons = [], isLoading } = useQuery({
    queryKey: ["admin-coupons"],
    queryFn: () => adminApi.getCoupons(),
  });

  const openNewModal = () => {
    setCode("");
    setDiscountType("PERCENTAGE");
    setDiscountValue("");
    setMinOrderAmount("");
    setMaxDiscountAmount("");
    setUsageLimit("100");
    setIsModalOpen(true);
  };

  const createCouponMutation = useMutation({
    mutationFn: () =>
      adminApi.createCoupon({
        code: code.trim().toUpperCase(),
        discountType,
        discountValue: parseFloat(discountValue),
        minOrderAmount: minOrderAmount ? parseFloat(minOrderAmount) : undefined,
        maxDiscountAmount: maxDiscountAmount ? parseFloat(maxDiscountAmount) : undefined,
        usageLimit: usageLimit ? parseInt(usageLimit) : undefined,
        active: true,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-coupons"] });
      toast.success("İndirim kuponu oluşturuldu.");
      setIsModalOpen(false);
    },
    onError: (err: any) => {
      toast.error(err.message || "Kupon oluşturulamadı.");
    },
  });

  const toggleStatusMutation = useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) =>
      adminApi.updateCouponStatus(id, active),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-coupons"] });
      toast.success("Kupon durumu güncellendi.");
    },
    onError: (err: any) => {
      toast.error(err.message || "Durum güncellenemedi.");
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!code.trim() || !discountValue) {
      toast.error("Kupon kodu ve indirim değeri zorunludur.");
      return;
    }
    createCouponMutation.mutate();
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
            İndirim Kuponu Yönetimi
          </h1>
          <p className="text-xs text-muted-foreground mt-0.5">
            Kampanya ve sepet indirim kuponlarını tanımlayın, kullanım limitlerini belirleyin.
          </p>
        </div>
        <Button onClick={openNewModal} variant="accent" size="sm" className="gap-2">
          <Plus className="w-4 h-4" /> Yeni Kupon Tanımla
        </Button>
      </div>

      {/* Coupons Table */}
      <div className="rounded-3xl border bg-white shadow-sm overflow-hidden">
        {isLoading ? (
          <div className="p-8 space-y-3">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-12 bg-muted animate-pulse rounded-xl" />
            ))}
          </div>
        ) : coupons.length === 0 ? (
          <div className="text-center py-12 text-xs text-muted-foreground">
            Tanımlı indirim kuponu bulunamadı.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 font-semibold border-b">
                <tr>
                  <th className="p-4">Kupon Kodu</th>
                  <th className="p-4">İndirim Türü / Değeri</th>
                  <th className="p-4">Min. Sepet Tutarı</th>
                  <th className="p-4">Kullanım / Limit</th>
                  <th className="p-4">Durum</th>
                  <th className="p-4 text-right">İşlem</th>
                </tr>
              </thead>
              <tbody className="divide-y text-slate-700">
                {coupons.map((coupon) => (
                  <tr key={coupon.id} className="hover:bg-slate-50 transition-colors">
                    <td className="p-4 font-mono font-bold text-slate-900 flex items-center gap-2">
                      <Tag className="w-4 h-4 text-accent-orange" />
                      {coupon.code}
                    </td>
                    <td className="p-4 font-semibold text-slate-800">
                      {coupon.discountType === "PERCENTAGE"
                        ? `%{coupon.discountValue}`
                        : formatPrice(coupon.discountValue)}
                    </td>
                    <td className="p-4 text-slate-600">
                      {coupon.minOrderAmount ? formatPrice(coupon.minOrderAmount) : "Limit Yok"}
                    </td>
                    <td className="p-4 font-mono text-slate-600">
                      {coupon.usageCount} / {coupon.usageLimit || "∞"}
                    </td>
                    <td className="p-4">
                      <span
                        className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full font-semibold ${
                          coupon.active
                            ? "bg-emerald-100 text-emerald-800"
                            : "bg-slate-100 text-slate-500"
                        }`}
                      >
                        {coupon.active ? (
                          <>
                            <CheckCircle2 className="w-3 h-3 text-emerald-600" /> Aktif
                          </>
                        ) : (
                          <>
                            <XCircle className="w-3 h-3 text-slate-400" /> Pasif
                          </>
                        )}
                      </span>
                    </td>
                    <td className="p-4 text-right">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() =>
                          toggleStatusMutation.mutate({
                            id: coupon.id,
                            active: !coupon.active,
                          })
                        }
                        className="h-8 text-xs"
                      >
                        {coupon.active ? "Pasifleştir" : "Aktifleştir"}
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* CREATE DIALOG */}
      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Yeni İndirim Kuponu Oluştur</DialogTitle>
          </DialogHeader>

          <form onSubmit={handleSubmit} className="space-y-4 text-xs py-2">
            <div>
              <label className="font-semibold text-slate-700 block mb-1">
                Kupon Kodu *
              </label>
              <Input
                required
                placeholder="YAZ10"
                value={code}
                onChange={(e) => setCode(e.target.value.toUpperCase())}
                className="font-mono uppercase"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">
                  İndirim Türü *
                </label>
                <select
                  value={discountType}
                  onChange={(e) => setDiscountType(e.target.value as DiscountType)}
                  className="w-full h-10 px-3 rounded-xl border text-xs bg-background"
                >
                  <option value="PERCENTAGE">Yüzde (%)</option>
                  <option value="FIXED_AMOUNT">Sabit Tutar (TL)</option>
                </select>
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">
                  İndirim Değeri *
                </label>
                <Input
                  required
                  type="number"
                  step="0.01"
                  placeholder={discountType === "PERCENTAGE" ? "10" : "100.00"}
                  value={discountValue}
                  onChange={(e) => setDiscountValue(e.target.value)}
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">
                  Min. Sepet Tutarı (TL)
                </label>
                <Input
                  type="number"
                  placeholder="500.00"
                  value={minOrderAmount}
                  onChange={(e) => setMinOrderAmount(e.target.value)}
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">
                  Maks. İndirim (TL)
                </label>
                <Input
                  type="number"
                  placeholder="250.00"
                  value={maxDiscountAmount}
                  onChange={(e) => setMaxDiscountAmount(e.target.value)}
                />
              </div>
            </div>

            <div>
              <label className="font-semibold text-slate-700 block mb-1">
                Toplam Kullanım Limiti
              </label>
              <Input
                type="number"
                value={usageLimit}
                onChange={(e) => setUsageLimit(e.target.value)}
              />
            </div>

            <div className="flex justify-end gap-2 pt-4 border-t">
              <Button
                type="button"
                variant="outline"
                onClick={() => setIsModalOpen(false)}
              >
                İptal
              </Button>
              <Button
                type="submit"
                variant="accent"
                disabled={createCouponMutation.isPending}
                isLoading={createCouponMutation.isPending}
              >
                Kuponu Kaydet
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
