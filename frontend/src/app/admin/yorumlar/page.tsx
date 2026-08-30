"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { adminApi } from "@/features/admin/admin-api";
import { ReviewStatus } from "@/types";
import { REVIEW_STATUS_LABELS } from "@/lib/constants";
import { formatDate } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Star, Check, X } from "lucide-react";
import { toast } from "sonner";

export default function AdminReviewsPage() {
  const queryClient = useQueryClient();
  const [filterStatus, setFilterStatus] = useState<ReviewStatus | "">("");

  const { data: reviews = [], isLoading } = useQuery({
    queryKey: ["admin-reviews"],
    queryFn: () => adminApi.getReviews(),
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: "APPROVED" | "REJECTED" }) =>
      adminApi.updateReviewStatus(id, status),
    onSuccess: (_, { status }) => {
      queryClient.invalidateQueries({ queryKey: ["admin-reviews"] });
      toast.success(status === "APPROVED" ? "Yorum onaylandı ve yayına alındı." : "Yorum reddedildi.");
    },
    onError: (err: any) => {
      toast.error(err.message || "İşlem başarısız.");
    },
  });

  const filteredReviews = filterStatus
    ? reviews.filter((r) => r.status === filterStatus)
    : reviews;

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
            Yorum ve Değerlendirme Moderasyonu
          </h1>
          <p className="text-xs text-muted-foreground mt-0.5">
            Müşterilerin ürünlere yaptığı yorumları inceleyin, onaylayın veya reddedin.
          </p>
        </div>

        <select
          value={filterStatus}
          onChange={(e) => setFilterStatus(e.target.value as any)}
          className="h-10 px-3 rounded-xl border text-xs bg-white shadow-sm"
        >
          <option value="">Tüm Yorumlar ({reviews.length})</option>
          <option value="PENDING">
            Bekleyenler ({reviews.filter((r) => r.status === "PENDING").length})
          </option>
          <option value="APPROVED">Onaylananlar</option>
          <option value="REJECTED">Reddedilenler</option>
        </select>
      </div>

      {/* Reviews Cards List */}
      <div className="space-y-4">
        {isLoading ? (
          <div className="space-y-3">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-28 bg-muted animate-pulse rounded-2xl" />
            ))}
          </div>
        ) : filteredReviews.length === 0 ? (
          <div className="p-12 text-center rounded-3xl border bg-white text-muted-foreground text-xs">
            Görüntülenecek değerlendirme bulunamadı.
          </div>
        ) : (
          filteredReviews.map((rev) => {
            const statusInfo = REVIEW_STATUS_LABELS[rev.status] || {
              label: rev.status,
              color: "bg-slate-100 text-slate-800",
            };

            return (
              <div
                key={rev.id}
                className="p-5 rounded-3xl border bg-white shadow-sm space-y-3 text-xs"
              >
                <div className="flex flex-wrap items-center justify-between gap-2 border-b pb-3">
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-slate-900 text-sm">
                      {rev.userName}
                    </span>
                    {rev.verifiedPurchase && (
                      <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 font-semibold border border-emerald-200">
                        Doğrulanmış Alıcı
                      </span>
                    )}
                  </div>

                  <div className="flex items-center gap-3">
                    <span className="text-slate-400 font-mono text-[11px]">
                      {formatDate(rev.createdAt)}
                    </span>
                    <span
                      className={`inline-flex px-2.5 py-0.5 rounded-full font-semibold border ${statusInfo.color}`}
                    >
                      {statusInfo.label}
                    </span>
                  </div>
                </div>

                <div className="flex items-center text-amber-400">
                  {[...Array(5)].map((_, i) => (
                    <Star
                      key={i}
                      className={`w-3.5 h-3.5 ${
                        i < rev.rating ? "fill-amber-400" : "text-slate-200"
                      }`}
                    />
                  ))}
                </div>

                <p className="text-slate-700 text-xs leading-relaxed bg-slate-50/50 p-3 rounded-2xl border">
                  {rev.comment}
                </p>

                {/* Moderation Actions */}
                <div className="flex justify-end gap-2 pt-1">
                  {rev.status !== "APPROVED" && (
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={updateStatusMutation.isPending}
                      onClick={() =>
                        updateStatusMutation.mutate({
                          id: rev.id,
                          status: "APPROVED",
                        })
                      }
                      className="h-8 text-xs text-emerald-700 border-emerald-300 hover:bg-emerald-50 gap-1"
                    >
                      <Check className="w-3.5 h-3.5" /> Onayla & Yayınla
                    </Button>
                  )}

                  {rev.status !== "REJECTED" && (
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={updateStatusMutation.isPending}
                      onClick={() =>
                        updateStatusMutation.mutate({
                          id: rev.id,
                          status: "REJECTED",
                        })
                      }
                      className="h-8 text-xs text-destructive border-red-200 hover:bg-red-50 gap-1"
                    >
                      <X className="w-3.5 h-3.5" /> Reddet
                    </Button>
                  )}
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
