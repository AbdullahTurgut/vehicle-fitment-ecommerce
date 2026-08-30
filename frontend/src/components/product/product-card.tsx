"use client";

import Link from "next/link";
import { ProductList } from "@/types";
import { formatPrice } from "@/lib/utils";
import { ShoppingCart, Heart, ShieldCheck, Check } from "lucide-react";
import { useCartStore } from "@/stores/cart-store";
import { useVehicleStore } from "@/stores/vehicle-store";
import { cartApi } from "@/features/cart/cart-api";
import { favoriteApi } from "@/features/review/review-api";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { useState } from "react";

interface ProductCardProps {
  product: ProductList;
  isCompatible?: boolean;
}

export function ProductCard({ product, isCompatible = false }: ProductCardProps) {
  const { setCartDrawerOpen, setItemCount } = useCartStore();
  const { selectedVehicle } = useVehicleStore();
  const queryClient = useQueryClient();
  const [isFavorited, setIsFavorited] = useState(false);

  const addToCartMutation = useMutation({
    mutationFn: () =>
      cartApi.addToCart({
        productId: product.id,
        quantity: 1,
        variantId: selectedVehicle?.variant?.id,
      }),
    onSuccess: (cart) => {
      queryClient.setQueryData(["cart"], cart);
      setItemCount(cart.totalQuantity);
      toast.success("Ürün sepete eklendi!");
      setCartDrawerOpen(true);
    },
    onError: (err: any) => {
      toast.error(err.message || "Ürün sepete eklenemedi.");
    },
  });

  const toggleFavoriteMutation = useMutation({
    mutationFn: () => favoriteApi.toggleFavorite(product.id),
    onSuccess: (res) => {
      setIsFavorited(res.favorited);
      queryClient.invalidateQueries({ queryKey: ["favorites"] });
      toast.success(res.message || (res.favorited ? "Favorilere eklendi" : "Favorilerden çıkarıldı"));
    },
    onError: (err: any) => {
      toast.error(err.message || "Favori işlemi için lütfen giriş yapın.");
    },
  });

  const discountPercent =
    product.salePrice && product.basePrice > product.salePrice
      ? Math.round(((product.basePrice - product.salePrice) / product.basePrice) * 100)
      : null;

  return (
    <div className="group relative flex flex-col rounded-2xl border bg-white shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden">
      {/* Product Image Container */}
      <div className="relative aspect-[4/3] w-full overflow-hidden bg-slate-50 border-b">
        <Link href={`/urunler/${product.slug}`} className="w-full h-full block">
          {product.primaryImageUrl ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={product.primaryImageUrl}
              alt={product.name}
              className="w-full h-full object-cover object-center group-hover:scale-105 transition-transform duration-500"
            />
          ) : (
            <div className="w-full h-full flex flex-col items-center justify-center text-slate-300 bg-slate-100">
              <ShoppingCart className="w-12 h-12 stroke-[1.5]" />
              <span className="text-[11px] font-medium text-slate-400 mt-2">Görsel Yakında</span>
            </div>
          )}
        </Link>

        {/* Badges Overlay */}
        <div className="absolute top-2.5 left-2.5 flex flex-col gap-1 z-10">
          {isCompatible && (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-600 text-white text-[11px] font-semibold shadow-md">
              <Check className="w-3 h-3 stroke-[3]" />
              Aracınıza Uygun
            </span>
          )}
          {discountPercent && (
            <span className="inline-flex items-center px-2 py-0.5 rounded-full bg-accent-orange text-white text-[11px] font-bold shadow-md">
              %{discountPercent} İndirim
            </span>
          )}
        </div>

        {/* Favorite Button */}
        <button
          onClick={(e) => {
            e.preventDefault();
            toggleFavoriteMutation.mutate();
          }}
          disabled={toggleFavoriteMutation.isPending}
          className={`absolute top-2.5 right-2.5 w-8 h-8 rounded-full flex items-center justify-center backdrop-blur-md transition-all duration-200 z-10 cursor-pointer shadow-sm ${
            isFavorited
              ? "bg-rose-50 text-rose-500"
              : "bg-white/80 text-slate-600 hover:text-rose-500 hover:bg-white"
          }`}
          title="Favoriye Ekle"
        >
          <Heart className={`w-4 h-4 ${isFavorited ? "fill-rose-500" : ""}`} />
        </button>

        {/* In Stock Badge */}
        {!product.inStock && (
          <div className="absolute inset-0 bg-white/70 backdrop-blur-xs flex items-center justify-center z-10">
            <span className="px-3 py-1 rounded-full bg-slate-900 text-white text-xs font-semibold">
              Tükendi
            </span>
          </div>
        )}
      </div>

      {/* Product Content */}
      <div className="flex flex-col flex-1 p-4">
        <div className="flex items-center gap-1 text-[11px] text-muted-foreground font-mono mb-1">
          <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" />
          <span>SKU: {product.sku}</span>
        </div>

        <Link
          href={`/urunler/${product.slug}`}
          className="font-semibold text-sm text-slate-900 hover:text-accent-orange transition-colors line-clamp-2 min-h-[40px] leading-snug mb-3"
        >
          {product.name}
        </Link>

        {/* Price and Cart button */}
        <div className="mt-auto pt-3 border-t flex items-center justify-between gap-2">
          <div className="flex flex-col">
            {product.salePrice && product.basePrice > product.salePrice && (
              <span className="text-xs text-muted-foreground line-through">
                {formatPrice(product.basePrice)}
              </span>
            )}
            <span className="text-base font-extrabold text-slate-900 leading-tight">
              {formatPrice(product.effectivePrice)}
            </span>
          </div>

          <button
            onClick={() => addToCartMutation.mutate()}
            disabled={!product.inStock || addToCartMutation.isPending}
            className="inline-flex items-center justify-center gap-1.5 px-3.5 py-2 rounded-xl bg-slate-900 text-white hover:bg-accent-orange text-xs font-semibold transition-all active:scale-95 disabled:opacity-40 disabled:hover:bg-slate-900 cursor-pointer shadow-sm"
          >
            <ShoppingCart className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">Sepete Ekle</span>
          </button>
        </div>
      </div>
    </div>
  );
}
