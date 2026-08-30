"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { cartApi } from "@/features/cart/cart-api";
import { couponApi } from "@/features/coupon/coupon-api";
import { useCartStore } from "@/stores/cart-store";
import { formatPrice } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import {
  ShoppingCart,
  Trash2,
  Plus,
  Minus,
  ArrowRight,
  Truck,
  Tag,
  ShieldCheck,
  Sparkles,
  ShoppingBag,
} from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";

export default function CartPage() {
  const queryClient = useQueryClient();
  const { setItemCount } = useCartStore();

  const [couponCode, setCouponCode] = useState<string>("");
  const [appliedCoupon, setAppliedCoupon] = useState<{
    code: string;
    discountAmount: number;
  } | null>(null);

  const { data: cart, isLoading } = useQuery({
    queryKey: ["cart"],
    queryFn: () => cartApi.getCart(),
  });

  const updateQuantityMutation = useMutation({
    mutationFn: ({ itemId, quantity }: { itemId: string; quantity: number }) =>
      cartApi.updateQuantity(itemId, { quantity }),
    onSuccess: (updatedCart) => {
      queryClient.setQueryData(["cart"], updatedCart);
      setItemCount(updatedCart.totalQuantity);
    },
    onError: (err: any) => {
      toast.error(err.message || "Adet güncellenemedi.");
    },
  });

  const removeItemMutation = useMutation({
    mutationFn: (itemId: string) => cartApi.removeItem(itemId),
    onSuccess: (updatedCart) => {
      queryClient.setQueryData(["cart"], updatedCart);
      setItemCount(updatedCart.totalQuantity);
      toast.success("Ürün sepetten çıkarıldı.");
    },
    onError: (err: any) => {
      toast.error(err.message || "Ürün silinemedi.");
    },
  });

  const clearCartMutation = useMutation({
    mutationFn: () => cartApi.clearCart(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cart"] });
      setItemCount(0);
      toast.success("Sepet temizlendi.");
    },
  });

  const validateCouponMutation = useMutation({
    mutationFn: () =>
      couponApi.validateCoupon({
        code: couponCode.trim(),
        cartAmount: cart?.subtotal || 0,
      }),
    onSuccess: (res) => {
      if (res.valid) {
        setAppliedCoupon({
          code: res.code,
          discountAmount: res.discountAmount,
        });
        toast.success(`Kupon uygulandı! ${formatPrice(res.discountAmount)} indirim kazandınız.`);
      } else {
        toast.error(res.message || "Geçersiz kupon kodu.");
      }
    },
    onError: (err: any) => {
      toast.error(err.message || "Kupon doğrulanamadı.");
    },
  });

  const items = cart?.items || [];
  const subtotal = cart?.subtotal || 0;
  const shippingTotal = cart?.shippingTotal || 0;
  const couponDiscount = appliedCoupon?.discountAmount || cart?.discountTotal || 0;
  const grandTotal = Math.max(0, subtotal - couponDiscount + shippingTotal);

  const freeShippingThreshold = cart?.freeShippingThreshold || 1000;
  const remainingForFreeShipping = Math.max(0, freeShippingThreshold - subtotal);
  const freeShippingProgress = Math.min(100, (subtotal / freeShippingThreshold) * 100);

  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-12 space-y-4">
        <div className="h-8 bg-muted animate-pulse rounded-lg w-1/4" />
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          <div className="lg:col-span-8 h-64 bg-muted animate-pulse rounded-3xl" />
          <div className="lg:col-span-4 h-64 bg-muted animate-pulse rounded-3xl" />
        </div>
      </div>
    );
  }

  if (!cart || items.length === 0) {
    return (
      <div className="container mx-auto px-4 py-16 text-center max-w-md space-y-4">
        <div className="w-20 h-20 rounded-3xl bg-slate-100 flex items-center justify-center text-slate-400 mx-auto">
          <ShoppingBag className="w-10 h-10" />
        </div>
        <h1 className="text-2xl font-extrabold text-slate-900">Alışveriş Sepetiniz Boş</h1>
        <p className="text-xs text-muted-foreground leading-relaxed">
          Aracınıza tam oturan 3D havuzlu paspas ve bagaj havuzlarını inceleyerek sepetinizi doldurabilirsiniz.
        </p>
        <Button asChild variant="accent" size="lg" className="rounded-xl px-8">
          <Link href="/katalog">
            <Sparkles className="w-4 h-4 mr-2" />
            Alışverişe Başla
          </Link>
        </Button>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 space-y-8">
      {/* Title */}
      <div className="flex items-center justify-between border-b pb-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900">
            Alışveriş Sepeti ({cart.totalQuantity} Ürün)
          </h1>
          <p className="text-xs text-muted-foreground mt-0.5">
            Siparişinizi tamamlamadan önce sepetinizi inceleyin ve kupon kodunuzu girin.
          </p>
        </div>
        <button
          onClick={() => clearCartMutation.mutate()}
          className="text-xs text-destructive hover:underline font-semibold cursor-pointer"
        >
          Sepeti Temizle
        </button>
      </div>

      {/* Free Shipping Notice */}
      <div className="p-4 rounded-2xl bg-orange-50 border border-orange-200 text-xs sm:text-sm text-orange-950">
        <div className="flex items-center justify-between gap-2 mb-2 font-medium">
          <div className="flex items-center gap-2">
            <Truck className="w-4 h-4 text-accent-orange shrink-0" />
            {remainingForFreeShipping === 0 ? (
              <span className="text-emerald-700 font-bold">
                Tebrikler! Ücretsiz kargo hakkı kazandınız. 🚀
              </span>
            ) : (
              <span>
                Ücretsiz kargoya <strong>{formatPrice(remainingForFreeShipping)}</strong> kaldı!
              </span>
            )}
          </div>
          <span className="text-xs text-slate-500 font-mono">
            Eşik: {formatPrice(freeShippingThreshold)}
          </span>
        </div>
        <div className="w-full h-2 rounded-full bg-orange-200 overflow-hidden">
          <div
            className="h-full bg-accent-orange transition-all duration-300"
            style={{ width: `${freeShippingProgress}%` }}
          />
        </div>
      </div>

      {/* Cart Grid: Items (8 cols) + Summary (4 cols) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Left Items Table */}
        <div className="lg:col-span-8 rounded-3xl border bg-white shadow-sm overflow-hidden divide-y">
          {items.map((item) => (
            <div key={item.id} className="p-4 sm:p-6 flex flex-col sm:flex-row gap-4 sm:items-center">
              {/* Image */}
              <div className="w-20 h-20 rounded-2xl bg-slate-100 border flex items-center justify-center shrink-0 overflow-hidden">
                {item.productImage ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img
                    src={item.productImage}
                    alt={item.productName}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <ShoppingCart className="w-8 h-8 text-slate-300" />
                )}
              </div>

              {/* Info */}
              <div className="flex-1 min-w-0 space-y-1">
                <Link
                  href={`/urunler/${item.productSlug}`}
                  className="font-bold text-sm text-slate-900 hover:text-accent-orange transition-colors line-clamp-2"
                >
                  {item.productName}
                </Link>
                <div className="flex items-center gap-3 text-xs text-muted-foreground">
                  <span className="font-mono">SKU: {item.productSku}</span>
                  {item.variantInfo && <span>• {item.variantInfo}</span>}
                </div>
                <div className="text-xs font-bold text-slate-900 sm:hidden pt-1">
                  Birim: {formatPrice(item.unitPrice)}
                </div>
              </div>

              {/* Quantity Controls */}
              <div className="flex items-center justify-between sm:justify-center gap-4">
                <div className="flex items-center border rounded-xl overflow-hidden bg-slate-50">
                  <button
                    onClick={() => {
                      if (item.quantity > 1) {
                        updateQuantityMutation.mutate({
                          itemId: item.id,
                          quantity: item.quantity - 1,
                        });
                      } else {
                        removeItemMutation.mutate(item.id);
                      }
                    }}
                    disabled={updateQuantityMutation.isPending}
                    className="w-8 h-8 flex items-center justify-center hover:bg-slate-200 text-slate-600 transition-colors"
                  >
                    <Minus className="w-3.5 h-3.5" />
                  </button>
                  <span className="w-10 text-center font-bold text-xs text-slate-900">
                    {item.quantity}
                  </span>
                  <button
                    onClick={() =>
                      updateQuantityMutation.mutate({
                        itemId: item.id,
                        quantity: item.quantity + 1,
                      })
                    }
                    disabled={
                      updateQuantityMutation.isPending ||
                      item.quantity >= item.maxStock
                    }
                    className="w-8 h-8 flex items-center justify-center hover:bg-slate-200 text-slate-600 transition-colors disabled:opacity-30"
                  >
                    <Plus className="w-3.5 h-3.5" />
                  </button>
                </div>

                {/* Total Price for item */}
                <div className="text-right min-w-[90px]">
                  <div className="font-extrabold text-base text-slate-900">
                    {formatPrice(item.totalPrice)}
                  </div>
                  <div className="text-[10px] text-muted-foreground hidden sm:block">
                    {item.quantity} × {formatPrice(item.unitPrice)}
                  </div>
                </div>

                {/* Remove */}
                <button
                  onClick={() => removeItemMutation.mutate(item.id)}
                  disabled={removeItemMutation.isPending}
                  className="p-2 text-slate-400 hover:text-destructive transition-colors"
                  title="Ürünü Kaldır"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))}
        </div>

        {/* Right Summary & Coupon Box */}
        <div className="lg:col-span-4 space-y-6">
          {/* Coupon Box */}
          <div className="p-5 rounded-3xl border bg-white shadow-sm space-y-3">
            <div className="flex items-center gap-2 font-bold text-xs text-slate-800">
              <Tag className="w-4 h-4 text-accent-orange" />
              <span>İndirim Kuponu</span>
            </div>
            <div className="flex gap-2">
              <input
                type="text"
                placeholder="Kupon Kodu"
                value={couponCode}
                onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
                className="flex-1 px-3 py-2 text-xs rounded-xl border uppercase font-mono tracking-wider focus:outline-none focus:ring-2 focus:ring-accent-orange"
              />
              <Button
                variant="outline"
                size="sm"
                onClick={() => validateCouponMutation.mutate()}
                disabled={!couponCode.trim() || validateCouponMutation.isPending}
                className="text-xs shrink-0"
              >
                Uygula
              </Button>
            </div>
            {appliedCoupon && (
              <div className="p-2.5 rounded-xl bg-emerald-50 border border-emerald-200 text-xs text-emerald-800 flex items-center justify-between">
                <span>
                  <strong>{appliedCoupon.code}</strong> uygulandı (-{formatPrice(appliedCoupon.discountAmount)})
                </span>
                <button
                  onClick={() => setAppliedCoupon(null)}
                  className="text-xs text-destructive hover:underline cursor-pointer"
                >
                  Kaldır
                </button>
              </div>
            )}
          </div>

          {/* Price Summary Card */}
          <div className="p-6 rounded-3xl border bg-white shadow-sm space-y-4">
            <h3 className="font-bold text-base text-slate-900 border-b pb-3">
              Sipariş Özeti
            </h3>

            <div className="space-y-2.5 text-xs text-slate-600">
              <div className="flex justify-between">
                <span>Ara Toplam</span>
                <span className="font-medium text-slate-900">{formatPrice(subtotal)}</span>
              </div>

              {couponDiscount > 0 && (
                <div className="flex justify-between text-emerald-600 font-semibold">
                  <span>İndirim</span>
                  <span>-{formatPrice(couponDiscount)}</span>
                </div>
              )}

              <div className="flex justify-between">
                <span>Kargo Bedeli</span>
                <span>
                  {shippingTotal === 0 ? (
                    <strong className="text-emerald-600 font-bold">ÜCRETSİZ</strong>
                  ) : (
                    formatPrice(shippingTotal)
                  )}
                </span>
              </div>

              <div className="pt-3 border-t flex justify-between items-baseline">
                <span className="text-sm font-bold text-slate-900">Ödenecek Tutar</span>
                <span className="text-2xl font-extrabold text-accent-orange">
                  {formatPrice(grandTotal)}
                </span>
              </div>
            </div>

            <Button
              asChild
              variant="accent"
              size="lg"
              className="w-full py-6 rounded-xl font-bold shadow-lg shadow-orange-950/20"
            >
              <Link href="/odeme" className="flex items-center justify-center gap-2">
                <span>Ödemeye Geç</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
            </Button>

            <div className="pt-2 text-[11px] text-slate-400 space-y-1 text-center">
              <div className="flex items-center justify-center gap-1">
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" />
                <span>256-Bit SSL Güvenli Ödeme Altyapısı</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
