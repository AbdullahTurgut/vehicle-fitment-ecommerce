"use client";

import { use, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { catalogApi } from "@/features/catalog/catalog-api";
import { reviewApi } from "@/features/review/review-api";
import { cartApi } from "@/features/cart/cart-api";
import { useCartStore } from "@/stores/cart-store";
import { useVehicleStore } from "@/stores/vehicle-store";
import { useAuthStore } from "@/stores/auth-store";
import { formatPrice, formatDate } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  ShoppingCart,
  ShieldCheck,
  Truck,
  RotateCcw,
  Star,
  Check,
  Plus,
  Minus,
  MessageCircle,
  Car,
  Sparkles,
  Info,
  Layers,
  ChevronRight,
} from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";

export default function ProductDetailPage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = use(params);
  const queryClient = useQueryClient();

  const [quantity, setQuantity] = useState<number>(1);
  const [selectedImageIndex, setSelectedImageIndex] = useState<number>(0);
  const [reviewRating, setReviewRating] = useState<number>(5);
  const [reviewComment, setReviewComment] = useState<string>("");
  const [isReviewModalOpen, setIsReviewModalOpen] = useState<boolean>(false);

  const { setCartDrawerOpen, setItemCount } = useCartStore();
  const { selectedVehicle, setSelectorOpen } = useVehicleStore();
  const { isAuthenticated } = useAuthStore();

  // 1. Fetch Product Detail
  const {
    data: product,
    isLoading: loadingProduct,
    error: productError,
  } = useQuery({
    queryKey: ["product-detail", slug],
    queryFn: () => catalogApi.getProductBySlug(slug),
  });

  // 2. Fetch Product Reviews
  const { data: reviewSummary, isLoading: loadingReviews } = useQuery({
    queryKey: ["product-reviews", product?.id],
    queryFn: () => reviewApi.getProductReviews(product!.id),
    enabled: !!product?.id,
  });

  // Add to Cart Mutation
  const addToCartMutation = useMutation({
    mutationFn: () =>
      cartApi.addToCart({
        productId: product!.id,
        quantity,
        variantId: selectedVehicle?.variant?.id,
      }),
    onSuccess: (cart) => {
      queryClient.setQueryData(["cart"], cart);
      setItemCount(cart.totalQuantity);
      toast.success("Ürün başarıyla sepete eklendi!");
      setCartDrawerOpen(true);
    },
    onError: (err: any) => {
      toast.error(err.message || "Ürün sepete eklenemedi.");
    },
  });

  // Create Review Mutation
  const createReviewMutation = useMutation({
    mutationFn: () =>
      reviewApi.createReview(product!.id, {
        rating: reviewRating,
        comment: reviewComment,
      }),
    onSuccess: () => {
      toast.success("Yorumunuz iletildi! Yönetici onayından sonra yayınlanacaktır.");
      setIsReviewModalOpen(false);
      setReviewComment("");
      queryClient.invalidateQueries({ queryKey: ["product-reviews", product?.id] });
    },
    onError: (err: any) => {
      toast.error(err.message || "Yorum gönderilemedi. Lütfen tekrar deneyin.");
    },
  });

  if (loadingProduct) {
    return (
      <div className="container mx-auto px-4 py-12">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-12">
          <div className="h-96 bg-muted animate-pulse rounded-3xl" />
          <div className="space-y-4">
            <div className="h-8 bg-muted animate-pulse rounded-lg w-3/4" />
            <div className="h-4 bg-muted animate-pulse rounded-lg w-1/4" />
            <div className="h-16 bg-muted animate-pulse rounded-lg" />
            <div className="h-12 bg-muted animate-pulse rounded-xl" />
          </div>
        </div>
      </div>
    );
  }

  if (productError || !product) {
    return (
      <div className="container mx-auto px-4 py-16 text-center space-y-4">
        <h2 className="text-2xl font-bold text-slate-900">Ürün Bulunamadı</h2>
        <p className="text-sm text-muted-foreground">
          Aradığınız ürün mevcut değil veya yayından kaldırılmış olabilir.
        </p>
        <Button asChild variant="accent">
          <Link href="/katalog">Kataloğa Dön</Link>
        </Button>
      </div>
    );
  }

  const images = product.images?.length > 0 ? product.images : [];
  const currentImage = images[selectedImageIndex]?.imageUrl || product.images?.[0]?.imageUrl;
  const whatsappNumber = process.env.NEXT_PUBLIC_WHATSAPP_NUMBER || "905550000000";
  const whatsappText = encodeURIComponent(
    `Merhaba, ${product.name} (SKU: ${product.sku}) hakkında bilgi almak ve sipariş vermek istiyorum.`
  );

  return (
    <div className="container mx-auto px-4 py-8 space-y-12">
      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 text-xs text-muted-foreground">
        <Link href="/" className="hover:text-foreground">
          Anasayfa
        </Link>
        <ChevronRight className="w-3.5 h-3.5" />
        <Link href="/katalog" className="hover:text-foreground">
          Katalog
        </Link>
        {product.category && (
          <>
            <ChevronRight className="w-3.5 h-3.5" />
            <Link
              href={`/katalog?category=${product.category.slug}`}
              className="hover:text-foreground"
            >
              {product.category.name}
            </Link>
          </>
        )}
        <ChevronRight className="w-3.5 h-3.5" />
        <span className="font-semibold text-slate-900 truncate max-w-xs">
          {product.name}
        </span>
      </nav>

      {/* PRODUCT TOP SECTION: GALLERY + BUY BOX */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-12 items-start">
        {/* Left: Image Gallery (5 cols) */}
        <div className="lg:col-span-6 space-y-4">
          <div className="relative aspect-[4/3] w-full rounded-3xl overflow-hidden bg-slate-50 border shadow-sm">
            {currentImage ? (
              // eslint-disable-next-line @next/next/no-img-element
              <img
                src={currentImage}
                alt={product.name}
                className="w-full h-full object-cover object-center transition-all duration-300"
              />
            ) : (
              <div className="w-full h-full flex flex-col items-center justify-center text-slate-300 bg-slate-100">
                <ShoppingCart className="w-16 h-16 stroke-[1.5]" />
                <span className="text-xs font-medium text-slate-400 mt-2">
                  Görsel Yakında Eklenecek
                </span>
              </div>
            )}

            {/* Badges */}
            <div className="absolute top-4 left-4 flex flex-col gap-1.5 z-10">
              {product.inStock ? (
                <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-emerald-600 text-white text-xs font-semibold shadow-md">
                  <Check className="w-3.5 h-3.5 stroke-[3]" /> Stokta Var
                </span>
              ) : (
                <span className="inline-flex items-center px-3 py-1 rounded-full bg-slate-900 text-white text-xs font-semibold shadow-md">
                  Tükendi
                </span>
              )}
            </div>
          </div>

          {/* Thumbnails */}
          {images.length > 1 && (
            <div className="flex gap-3 overflow-x-auto pb-2">
              {images.map((img, idx) => (
                <button
                  key={img.id || idx}
                  onClick={() => setSelectedImageIndex(idx)}
                  className={`w-20 h-20 rounded-xl border-2 overflow-hidden shrink-0 transition-all cursor-pointer ${
                    selectedImageIndex === idx
                      ? "border-accent-orange shadow-md scale-105"
                      : "border-slate-200 hover:border-slate-400 opacity-70 hover:opacity-100"
                  }`}
                >
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img
                    src={img.imageUrl}
                    alt={img.altText || product.name}
                    className="w-full h-full object-cover"
                  />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Right: Buy Box & Product Info (6 cols) */}
        <div className="lg:col-span-6 space-y-6">
          <div className="space-y-2 border-b pb-6">
            <div className="flex items-center gap-3 text-xs text-muted-foreground">
              <span className="font-mono">SKU: {product.sku}</span>
              {product.manufacturerBrand && (
                <>
                  <span>•</span>
                  <span>Marka: <strong>{product.manufacturerBrand}</strong></span>
                </>
              )}
              {product.material && (
                <>
                  <span>•</span>
                  <span>Malzeme: <strong>{product.material}</strong></span>
                </>
              )}
            </div>

            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 leading-tight">
              {product.name}
            </h1>

            {/* Rating Stars Summary */}
            {reviewSummary && reviewSummary.totalReviews > 0 && (
              <div className="flex items-center gap-2 pt-1">
                <div className="flex items-center text-amber-400">
                  {[...Array(5)].map((_, i) => (
                    <Star
                      key={i}
                      className={`w-4 h-4 ${
                        i < Math.round(reviewSummary.averageRating)
                          ? "fill-amber-400"
                          : "text-slate-200"
                      }`}
                    />
                  ))}
                </div>
                <span className="text-xs font-bold text-slate-700">
                  {reviewSummary.averageRating.toFixed(1)}
                </span>
                <span className="text-xs text-muted-foreground">
                  ({reviewSummary.totalReviews} değerlendirme)
                </span>
              </div>
            )}
          </div>

          {/* FITMENT CHECK BOX */}
          <div className="p-4 rounded-2xl bg-slate-50 border space-y-2">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-xs font-bold text-slate-800">
                <Car className="w-4 h-4 text-accent-orange" />
                <span>Araç Uyumluluk Durumu</span>
              </div>
              <button
                onClick={() => setSelectorOpen(true)}
                className="text-xs text-accent-orange font-semibold hover:underline cursor-pointer"
              >
                {selectedVehicle ? "Aracı Değiştir" : "Araç Seç"}
              </button>
            </div>

            {selectedVehicle ? (
              <p className="text-xs text-emerald-700 font-medium flex items-center gap-1.5">
                <Check className="w-4 h-4 text-emerald-600 shrink-0" />
                Seçili aracınız:{" "}
                <strong>
                  {selectedVehicle.brand.name} {selectedVehicle.model.name} {selectedVehicle.generation.name}
                </strong>
              </p>
            ) : (
              <p className="text-xs text-muted-foreground">
                Bu ürünün aracınıza tam oturup oturmayacağını doğrulamak için araç modelinizi seçebilirsiniz.
              </p>
            )}
          </div>

          {/* PRICING */}
          <div className="space-y-1">
            {product.salePrice && product.basePrice > product.salePrice && (
              <div className="flex items-center gap-2">
                <span className="text-sm text-muted-foreground line-through">
                  {formatPrice(product.basePrice)}
                </span>
                <span className="text-xs px-2 py-0.5 rounded-full bg-orange-100 text-accent-orange font-bold">
                  İndirimli Fiyat
                </span>
              </div>
            )}
            <div className="text-3xl sm:text-4xl font-extrabold text-slate-900 tracking-tight">
              {formatPrice(product.effectivePrice)}
            </div>
            <p className="text-xs text-emerald-600 font-medium">
              KDV Dahil • 1.000 TL üzeri Ücretsiz Kargo
            </p>
          </div>

          {/* QUANTITY & ACTIONS */}
          <div className="space-y-4 pt-2 border-t">
            <div className="flex items-center gap-4">
              {/* Quantity Selector */}
              <div className="flex items-center border rounded-xl overflow-hidden bg-slate-50">
                <button
                  onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                  className="w-10 h-10 flex items-center justify-center hover:bg-slate-200 text-slate-700 transition-colors"
                >
                  <Minus className="w-4 h-4" />
                </button>
                <span className="w-12 text-center font-bold text-sm text-slate-900">
                  {quantity}
                </span>
                <button
                  onClick={() =>
                    setQuantity((q) => Math.min(product.stockQuantity || 10, q + 1))
                  }
                  className="w-10 h-10 flex items-center justify-center hover:bg-slate-200 text-slate-700 transition-colors"
                >
                  <Plus className="w-4 h-4" />
                </button>
              </div>

              {/* Add To Cart Button */}
              <Button
                size="lg"
                variant="accent"
                onClick={() => addToCartMutation.mutate()}
                disabled={!product.inStock || addToCartMutation.isPending}
                className="flex-1 py-6 text-base font-bold rounded-xl shadow-lg shadow-orange-950/20"
              >
                <ShoppingCart className="w-5 h-5 mr-2" />
                Sepete Ekle ({formatPrice(product.effectivePrice * quantity)})
              </Button>
            </div>

            {/* Fast WhatsApp CTA */}
            <a
              href={`https://wa.me/${whatsappNumber}?text=${whatsappText}`}
              target="_blank"
              rel="noopener noreferrer"
              className="w-full flex items-center justify-center gap-2 py-3 rounded-xl border border-emerald-500 bg-emerald-50/50 text-emerald-800 hover:bg-emerald-100 font-semibold text-xs transition-colors"
            >
              <MessageCircle className="w-4 h-4 text-emerald-600" />
              WhatsApp ile Hızlı Sipariş Ver
            </a>
          </div>

          {/* GUARANTEES BOX */}
          <div className="grid grid-cols-2 gap-3 pt-4 border-t text-xs text-slate-600">
            <div className="flex items-center gap-2 p-2.5 rounded-xl bg-slate-50 border">
              <Truck className="w-4 h-4 text-accent-orange shrink-0" />
              <span>Aynı Gün Hızlı Kargo</span>
            </div>
            <div className="flex items-center gap-2 p-2.5 rounded-xl bg-slate-50 border">
              <ShieldCheck className="w-4 h-4 text-emerald-600 shrink-0" />
              <span>%100 Birebir Kalıp Uyumu</span>
            </div>
            <div className="flex items-center gap-2 p-2.5 rounded-xl bg-slate-50 border">
              <RotateCcw className="w-4 h-4 text-amber-600 shrink-0" />
              <span>14 Gün Koşulsuz İade</span>
            </div>
            <div className="flex items-center gap-2 p-2.5 rounded-xl bg-slate-50 border">
              <Info className="w-4 h-4 text-blue-600 shrink-0" />
              <span>Kokusuz TPE Malzeme</span>
            </div>
          </div>
        </div>
      </div>

      {/* PRODUCT FEATURES & SPECIFICATIONS */}
      {product.features && product.features.length > 0 && (
        <section className="space-y-6 pt-8 border-t">
          <div className="space-y-1">
            <span className="text-xs uppercase tracking-wider text-accent-orange font-bold">
              Teknik Özellikler
            </span>
            <h3 className="text-2xl font-extrabold text-slate-900">
              Ürün Detayları ve Malzeme Kalitesi
            </h3>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {product.features.map((feature) => (
              <div
                key={feature.id}
                className="p-6 rounded-2xl border bg-white shadow-sm space-y-2 hover:border-slate-300 transition-colors"
              >
                <div className="w-10 h-10 rounded-xl bg-orange-50 text-accent-orange flex items-center justify-center font-bold">
                  <Layers className="w-5 h-5" />
                </div>
                <h4 className="font-bold text-base text-slate-900">{feature.title}</h4>
                <p className="text-xs text-muted-foreground leading-relaxed">
                  {feature.description}
                </p>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* CUSTOMER REVIEWS & RATINGS SECTION */}
      <section className="space-y-8 pt-8 border-t">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <span className="text-xs uppercase tracking-wider text-accent-orange font-bold">
              Müşteri Deneyimi
            </span>
            <h3 className="text-2xl font-extrabold text-slate-900">
              Müşteri Değerlendirmeleri ve Yorumlar
            </h3>
          </div>

          <Dialog open={isReviewModalOpen} onOpenChange={setIsReviewModalOpen}>
            <DialogTrigger asChild>
              <Button variant="outline" className="gap-2 shrink-0">
                <Star className="w-4 h-4 text-amber-500 fill-amber-500" />
                Yorum Yap
              </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-md">
              <DialogHeader>
                <DialogTitle>Ürünü Değerlendirin</DialogTitle>
              </DialogHeader>
              <div className="space-y-4 py-2">
                <div>
                  <label className="text-xs font-semibold text-slate-700 block mb-2">
                    Puanınız:
                  </label>
                  <div className="flex items-center gap-2">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <button
                        key={star}
                        type="button"
                        onClick={() => setReviewRating(star)}
                        className="p-1 text-amber-400 hover:scale-110 transition-transform cursor-pointer"
                      >
                        <Star
                          className={`w-7 h-7 ${
                            star <= reviewRating ? "fill-amber-400" : "text-slate-200"
                          }`}
                        />
                      </button>
                    ))}
                  </div>
                </div>

                <div>
                  <label className="text-xs font-semibold text-slate-700 block mb-2">
                    Yorumunuz:
                  </label>
                  <textarea
                    rows={4}
                    value={reviewComment}
                    onChange={(e) => setReviewComment(e.target.value)}
                    placeholder="Ürünün kalitesi, uyumu ve kargo hızı hakkındaki deneyiminizi paylaşın..."
                    className="w-full p-3 rounded-xl border border-input text-xs focus:outline-none focus:ring-2 focus:ring-accent-orange"
                  />
                </div>

                <Button
                  variant="accent"
                  className="w-full"
                  disabled={!reviewComment.trim() || createReviewMutation.isPending}
                  onClick={() => createReviewMutation.mutate()}
                >
                  Yorumu Gönder
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        </div>

        {/* Reviews List */}
        {loadingReviews ? (
          <div className="space-y-4">
            {[...Array(2)].map((_, i) => (
              <div key={i} className="h-24 bg-muted animate-pulse rounded-2xl" />
            ))}
          </div>
        ) : reviewSummary && reviewSummary.reviews.length > 0 ? (
          <div className="space-y-4">
            {reviewSummary.reviews.map((rev) => (
              <div
                key={rev.id}
                className="p-5 rounded-2xl border bg-white shadow-sm space-y-2"
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-sm text-slate-900">
                      {rev.userName}
                    </span>
                    {rev.verifiedPurchase && (
                      <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 font-semibold border border-emerald-200">
                        Doğrulanmış Alıcı
                      </span>
                    )}
                  </div>
                  <span className="text-xs text-muted-foreground">
                    {formatDate(rev.createdAt)}
                  </span>
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

                <p className="text-xs text-slate-700 leading-relaxed pt-1">
                  {rev.comment}
                </p>
              </div>
            ))}
          </div>
        ) : (
          <div className="p-8 text-center rounded-2xl bg-slate-50 border text-muted-foreground text-xs">
            Bu ürün için henüz yorum yapılmamış. İlk değerlendirmeyi siz yapın!
          </div>
        )}
      </section>
    </div>
  );
}
