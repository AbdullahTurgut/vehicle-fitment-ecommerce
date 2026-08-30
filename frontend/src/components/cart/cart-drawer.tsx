"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useCartStore } from "@/stores/cart-store";
import { cartApi } from "@/features/cart/cart-api";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Button } from "@/components/ui/button";
import { formatPrice } from "@/lib/utils";
import {
  ShoppingCart,
  Trash2,
  Plus,
  Minus,
  ArrowRight,
  Truck,
  Sparkles,
  ShoppingBag,
} from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";
import { useEffect } from "react";

export function CartDrawer() {
  const { isCartDrawerOpen, setCartDrawerOpen, setItemCount } = useCartStore();
  const queryClient = useQueryClient();

  const { data: cart, isLoading } = useQuery({
    queryKey: ["cart"],
    queryFn: () => cartApi.getCart(),
    enabled: isCartDrawerOpen,
  });

  useEffect(() => {
    if (cart) {
      setItemCount(cart.totalQuantity);
    }
  }, [cart, setItemCount]);

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

  const items = cart?.items || [];
  const grandTotal = cart?.grandTotal || 0;
  const subtotal = cart?.subtotal || 0;
  const freeShippingThreshold = cart?.freeShippingThreshold || 1000;
  const remainingForFreeShipping = Math.max(0, freeShippingThreshold - subtotal);
  const freeShippingProgress = Math.min(100, (subtotal / freeShippingThreshold) * 100);

  return (
    <Sheet open={isCartDrawerOpen} onOpenChange={setCartDrawerOpen}>
      <SheetContent className="w-full sm:max-w-md flex flex-col p-0">
        <SheetHeader className="p-4 border-b">
          <SheetTitle className="flex items-center gap-2 text-base font-semibold">
            <ShoppingCart className="w-5 h-5 text-accent-orange" />
            <span>Alışveriş Sepetim</span>
            {cart && cart.totalQuantity > 0 && (
              <span className="text-xs px-2 py-0.5 rounded-full bg-slate-100 text-slate-600 font-normal">
                {cart.totalQuantity} ürün
              </span>
            )}
          </SheetTitle>
        </SheetHeader>

        {/* Free Shipping Progress Bar */}
        {cart && items.length > 0 && (
          <div className="bg-orange-50/70 p-3 border-b border-orange-100 text-xs">
            <div className="flex items-center gap-1.5 font-medium text-orange-950 mb-1.5">
              <Truck className="w-4 h-4 text-accent-orange shrink-0" />
              {remainingForFreeShipping === 0 ? (
                <span className="text-emerald-700 font-semibold">
                  Tebrikler! Ücretsiz kargo hakkı kazandınız. 🎉
                </span>
              ) : (
                <span>
                  Ücretsiz kargoya <strong>{formatPrice(remainingForFreeShipping)}</strong> kaldı!
                </span>
              )}
            </div>
            <div className="w-full h-1.5 rounded-full bg-orange-200 overflow-hidden">
              <div
                className="h-full bg-accent-orange transition-all duration-300"
                style={{ width: `${freeShippingProgress}%` }}
              />
            </div>
          </div>
        )}

        {/* Cart Item List */}
        <div className="flex-1 overflow-y-auto p-4 divide-y">
          {isLoading ? (
            <div className="space-y-4 py-4">
              {[...Array(3)].map((_, i) => (
                <div key={i} className="h-20 bg-muted animate-pulse rounded-xl" />
              ))}
            </div>
          ) : items.length === 0 ? (
            <div className="h-full flex flex-col items-center justify-center text-center py-12 text-muted-foreground">
              <div className="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center text-slate-400 mb-4">
                <ShoppingBag className="w-8 h-8" />
              </div>
              <h4 className="font-semibold text-foreground text-base mb-1">Sepetiniz Boş</h4>
              <p className="text-xs text-muted-foreground max-w-xs mb-6">
                Aracınıza uygun havuzlu paspas ve bagaj havuzlarını keşfederek sepetinizi doldurun.
              </p>
              <Button
                variant="accent"
                onClick={() => setCartDrawerOpen(false)}
                asChild
              >
                <Link href="/katalog">
                  <Sparkles className="w-4 h-4 mr-2" />
                  Alışverişe Başla
                </Link>
              </Button>
            </div>
          ) : (
            items.map((item) => (
              <div key={item.id} className="py-4 flex gap-3 first:pt-0 last:pb-0">
                {/* Product Thumbnail Placeholder */}
                <div className="w-16 h-16 rounded-lg bg-slate-100 border flex items-center justify-center shrink-0 overflow-hidden">
                  {item.productImage ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img
                      src={item.productImage}
                      alt={item.productName}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <ShoppingCart className="w-6 h-6 text-slate-300" />
                  )}
                </div>

                <div className="flex-1 min-w-0">
                  <Link
                    href={`/urunler/${item.productSlug}`}
                    onClick={() => setCartDrawerOpen(false)}
                    className="text-xs font-semibold text-foreground hover:text-accent-orange line-clamp-2 leading-snug"
                  >
                    {item.productName}
                  </Link>
                  {item.variantInfo && (
                    <p className="text-[11px] text-muted-foreground mt-0.5">
                      {item.variantInfo}
                    </p>
                  )}
                  <div className="flex items-center justify-between mt-2">
                    <span className="font-bold text-sm text-slate-900">
                      {formatPrice(item.unitPrice)}
                    </span>

                    {/* Quantity Controls */}
                    <div className="flex items-center border rounded-lg overflow-hidden bg-slate-50">
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
                        className="w-7 h-7 flex items-center justify-center hover:bg-slate-200 text-slate-600 transition-colors"
                      >
                        <Minus className="w-3 h-3" />
                      </button>
                      <span className="w-8 text-center text-xs font-semibold text-slate-800">
                        {item.quantity}
                      </span>
                      <button
                        onClick={() => {
                          updateQuantityMutation.mutate({
                            itemId: item.id,
                            quantity: item.quantity + 1,
                          });
                        }}
                        disabled={
                          updateQuantityMutation.isPending ||
                          item.quantity >= item.maxStock
                        }
                        className="w-7 h-7 flex items-center justify-center hover:bg-slate-200 text-slate-600 transition-colors disabled:opacity-30"
                      >
                        <Plus className="w-3 h-3" />
                      </button>
                    </div>

                    {/* Remove button */}
                    <button
                      onClick={() => removeItemMutation.mutate(item.id)}
                      disabled={removeItemMutation.isPending}
                      className="text-slate-400 hover:text-destructive p-1 transition-colors"
                      title="Sil"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Footer Checkout Summary */}
        {cart && items.length > 0 && (
          <div className="p-4 border-t bg-slate-50/80 space-y-3">
            <div className="space-y-1.5 text-xs">
              <div className="flex justify-between text-slate-600">
                <span>Ara Toplam</span>
                <span>{formatPrice(cart.subtotal)}</span>
              </div>
              {cart.discountTotal > 0 && (
                <div className="flex justify-between text-emerald-600 font-medium">
                  <span>İndirim</span>
                  <span>-{formatPrice(cart.discountTotal)}</span>
                </div>
              )}
              <div className="flex justify-between text-slate-600">
                <span>Kargo</span>
                <span>
                  {cart.shippingTotal === 0 ? (
                    <strong className="text-emerald-600 font-semibold">ÜCRETSİZ</strong>
                  ) : (
                    formatPrice(cart.shippingTotal)
                  )}
                </span>
              </div>
              <div className="flex justify-between text-base font-bold text-slate-900 pt-2 border-t">
                <span>Genel Toplam</span>
                <span className="text-accent-orange">{formatPrice(grandTotal)}</span>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-2 pt-1">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCartDrawerOpen(false)}
                asChild
              >
                <Link href="/sepet">Sepete Git</Link>
              </Button>
              <Button
                variant="accent"
                size="sm"
                onClick={() => setCartDrawerOpen(false)}
                asChild
              >
                <Link href="/odeme" className="flex items-center justify-center gap-1.5">
                  <span>Siparişi Ver</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </Button>
            </div>
          </div>
        )}
      </SheetContent>
    </Sheet>
  );
}
