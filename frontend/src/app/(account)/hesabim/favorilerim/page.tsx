"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { favoriteApi } from "@/features/review/review-api";
import { cartApi } from "@/features/cart/cart-api";
import { useCartStore } from "@/stores/cart-store";
import { formatPrice } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Heart, ShoppingCart, Trash2, ShoppingBag } from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";

export default function FavoritesPage() {
  const queryClient = useQueryClient();
  const { setCartDrawerOpen, setItemCount } = useCartStore();

  const { data: favoritePage, isLoading } = useQuery({
    queryKey: ["favorites"],
    queryFn: () => favoriteApi.getFavorites(0, 20),
  });

  const removeFavoriteMutation = useMutation({
    mutationFn: (productId: string) => favoriteApi.removeFavorite(productId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["favorites"] });
      toast.success("Ürün favorilerden kaldırıldı.");
    },
    onError: (err: any) => {
      toast.error(err.message || "Favoriden kaldırılamadı.");
    },
  });

  const addToCartMutation = useMutation({
    mutationFn: (productId: string) =>
      cartApi.addToCart({ productId, quantity: 1 }),
    onSuccess: (cart) => {
      queryClient.setQueryData(["cart"], cart);
      setItemCount(cart.totalQuantity);
      toast.success("Ürün sepete eklendi!");
      setCartDrawerOpen(true);
    },
    onError: (err: any) => {
      toast.error(err.message || "Sepete eklenemedi.");
    },
  });

  const favorites = favoritePage?.content || [];

  return (
    <div className="space-y-6">
      <div className="p-6 rounded-3xl border bg-white shadow-sm space-y-6">
        <div className="border-b pb-4">
          <h2 className="font-bold text-base text-slate-900 flex items-center gap-2">
            <Heart className="w-5 h-5 text-rose-500 fill-rose-500" />
            <span>Favori Ürünlerim ({favorites.length})</span>
          </h2>
          <p className="text-xs text-muted-foreground mt-0.5">
            Beğendiğiniz ve daha sonra satın almak istediğiniz ürünlerin listesi.
          </p>
        </div>

        {isLoading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-64 bg-muted animate-pulse rounded-2xl" />
            ))}
          </div>
        ) : favorites.length === 0 ? (
          <div className="text-center py-16 text-muted-foreground space-y-3">
            <Heart className="w-12 h-12 text-slate-300 mx-auto" />
            <h4 className="font-bold text-slate-900 text-sm">Favori Ürününüz Yok</h4>
            <p className="text-xs max-w-sm mx-auto">
              Beğendiğiniz ürünlerin üzerindeki kalp simgesine tıklayarak favorilerinize ekleyebilirsiniz.
            </p>
            <Button asChild variant="accent" size="sm">
              <Link href="/katalog">Kataloğu İncele</Link>
            </Button>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
            {favorites.map((fav) => (
              <div
                key={fav.id}
                className="rounded-2xl border bg-white p-4 space-y-3 flex flex-col justify-between hover:shadow-md transition-shadow relative group"
              >
                <button
                  onClick={() => removeFavoriteMutation.mutate(fav.productId)}
                  className="absolute top-2.5 right-2.5 p-1.5 rounded-full bg-white/90 text-slate-400 hover:text-destructive shadow-sm z-10 cursor-pointer"
                  title="Favorilerden Kaldır"
                >
                  <Trash2 className="w-4 h-4" />
                </button>

                <div className="aspect-[4/3] w-full rounded-xl bg-slate-50 border overflow-hidden flex items-center justify-center">
                  {fav.primaryImageUrl ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img
                      src={fav.primaryImageUrl}
                      alt={fav.productName}
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform"
                    />
                  ) : (
                    <ShoppingBag className="w-8 h-8 text-slate-300" />
                  )}
                </div>

                <div className="space-y-1">
                  <span className="text-[10px] font-mono text-muted-foreground">
                    SKU: {fav.productSku}
                  </span>
                  <Link
                    href={`/urunler/${fav.productSlug}`}
                    className="font-bold text-xs text-slate-900 hover:text-accent-orange transition-colors line-clamp-2 block leading-snug"
                  >
                    {fav.productName}
                  </Link>
                  <div className="pt-1 font-extrabold text-sm text-slate-900">
                    {formatPrice(fav.effectivePrice)}
                  </div>
                </div>

                <Button
                  size="sm"
                  variant="accent"
                  onClick={() => addToCartMutation.mutate(fav.productId)}
                  disabled={!fav.inStock || addToCartMutation.isPending}
                  className="w-full text-xs font-semibold gap-1.5"
                >
                  <ShoppingCart className="w-3.5 h-3.5" />
                  <span>{fav.inStock ? "Sepete Ekle" : "Tükendi"}</span>
                </Button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
