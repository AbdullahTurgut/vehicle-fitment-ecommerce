"use client";

import { Suspense, useState } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { catalogApi } from "@/features/catalog/catalog-api";
import { useVehicleStore } from "@/stores/vehicle-store";
import { ProductCard } from "@/components/product/product-card";
import { Button } from "@/components/ui/button";
import {
  Car,
  Filter,
  SlidersHorizontal,
  ChevronLeft,
  ChevronRight,
  Sparkles,
  Layers,
  ShoppingBag,
} from "lucide-react";

function CatalogContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const categoryParam = searchParams.get("category") || "";
  const variantIdParam = searchParams.get("variantId") || "";
  const yearParam = searchParams.get("year") ? Number(searchParams.get("year")) : undefined;
  const searchParam = searchParams.get("search") || "";

  const [currentPage, setCurrentPage] = useState<number>(0);
  const [sortBy, setSortBy] = useState<string>("featured");

  const { selectedVehicle, setSelectorOpen } = useVehicleStore();

  // Fetch Categories
  const { data: categories = [], isLoading: loadingCategories } = useQuery({
    queryKey: ["catalog-categories"],
    queryFn: () => catalogApi.getCategories(),
  });

  // Effective variantId (from URL param or selectedVehicle in Zustand)
  const effectiveVariantId = variantIdParam || selectedVehicle?.variant?.id;
  const effectiveYear = yearParam || selectedVehicle?.year;

  // Query 1: If searching or generic listing
  const {
    data: productPage,
    isLoading: loadingProducts,
  } = useQuery({
    queryKey: [
      "catalog-products",
      categoryParam,
      currentPage,
      searchParam,
      sortBy,
    ],
    queryFn: () =>
      catalogApi.getProducts({
        category: categoryParam || undefined,
        page: currentPage,
        size: 12,
      }),
    enabled: !effectiveVariantId,
  });

  // Query 2: If variant fitment is active
  const {
    data: compatibleProducts = [],
    isLoading: loadingCompatible,
  } = useQuery({
    queryKey: ["compatible-products", effectiveVariantId, effectiveYear],
    queryFn: () =>
      catalogApi.getCompatibleProducts({
        variantId: effectiveVariantId!,
        year: effectiveYear,
      }),
    enabled: !!effectiveVariantId,
  });

  const isFitmentActive = !!effectiveVariantId;
  const isLoading = isFitmentActive ? loadingCompatible : loadingProducts;

  const handleCategoryChange = (slug: string) => {
    const params = new URLSearchParams(searchParams.toString());
    if (slug) {
      params.set("category", slug);
    } else {
      params.delete("category");
    }
    params.delete("page");
    setCurrentPage(0);
    const query = params.toString();
    router.push(query ? `/katalog?${query}` : "/katalog");
  };

  // Filter & sort products
  let displayProducts = isFitmentActive ? [...compatibleProducts] : [...(productPage?.content || [])];

  if (isFitmentActive && categoryParam) {
    displayProducts = displayProducts.filter((p) => {
      if (categoryParam === "3d-oto-paspas") {
        return p.slug.includes("paspas") || p.name.toLowerCase().includes("paspas");
      } else if (categoryParam === "bagaj-havuzu") {
        return p.slug.includes("bagaj") || p.name.toLowerCase().includes("bagaj");
      }
      return true;
    });
  }

  if (searchParam) {
    const q = searchParam.toLowerCase();
    displayProducts = displayProducts.filter(
      (p) =>
        p.name.toLowerCase().includes(q) ||
        p.sku.toLowerCase().includes(q) ||
        p.slug.toLowerCase().includes(q)
    );
  }

  if (sortBy === "price-asc") {
    displayProducts.sort((a, b) => a.effectivePrice - b.effectivePrice);
  } else if (sortBy === "price-desc") {
    displayProducts.sort((a, b) => b.effectivePrice - a.effectivePrice);
  }

  const totalPages = productPage?.totalPages || 1;

  return (
    <div className="container mx-auto px-4 py-8 space-y-8">
      {/* Header / Breadcrumb */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b pb-6">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900">
            {isFitmentActive
              ? "Aracınıza Özel Uyumlu Ürünler"
              : categoryParam
              ? categories.find((c) => c.slug === categoryParam)?.name || "Kategori Ürünleri"
              : searchParam
              ? `"${searchParam}" Arama Sonuçları`
              : "Tüm Ürünler"}
          </h1>
          <p className="text-xs sm:text-sm text-muted-foreground mt-1">
            {isFitmentActive
              ? "Aşağıdaki ürünler seçili aracınızın fabrika zemin ölçülerine göre listelenmiştir."
              : "Aracınıza %100 uyumlu 3D havuzlu paspas ve bagaj havuzu seçenekleri."}
          </p>
        </div>

        {/* Sort Controls */}
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 text-xs font-semibold text-slate-600">
            <SlidersHorizontal className="w-4 h-4" />
            <span>Sırala:</span>
          </div>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="h-9 px-3 rounded-lg border border-input bg-background text-xs font-medium focus:outline-none focus:ring-2 focus:ring-accent-orange"
          >
            <option value="featured">Öne Çıkanlar</option>
            <option value="price-asc">Fiyat (Artan)</option>
            <option value="price-desc">Fiyat (Azalan)</option>
          </select>
        </div>
      </div>

      {/* FITMENT NOTICE BANNER */}
      {isFitmentActive ? (
        <div className="p-4 rounded-2xl bg-emerald-50 border border-emerald-200 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs sm:text-sm text-emerald-950">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-full bg-emerald-600 text-white flex items-center justify-center font-bold text-sm shrink-0">
              ✓
            </div>
            <div>
              <span className="font-semibold text-emerald-800">
                Filtre Aktif: Aracınıza Uyumlu Ürünler Listeleniyor
              </span>
              {selectedVehicle && (
                <p className="text-emerald-700 text-xs mt-0.5">
                  {[
                    selectedVehicle.year,
                    selectedVehicle.brand.name,
                    selectedVehicle.model.name,
                    selectedVehicle.generation.name,
                    selectedVehicle.variant?.name,
                  ].filter(Boolean).join(" ")}
                </p>
              )}
            </div>
          </div>
          <Button
            size="sm"
            variant="outline"
            onClick={() => setSelectorOpen(true)}
            className="border-emerald-300 text-emerald-800 hover:bg-emerald-100 shrink-0"
          >
            Aracı Değiştir
          </Button>
        </div>
      ) : (
        <div className="p-4 rounded-2xl bg-slate-900 text-white flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs sm:text-sm">
          <div className="flex items-center gap-2.5">
            <Car className="w-5 h-5 text-accent-orange shrink-0 animate-pulse" />
            <div>
              <span className="font-semibold">
                Aracınıza tam uyan paspası henüz seçmediniz mi?
              </span>
              <p className="text-slate-400 text-xs mt-0.5">
                Araç seçimi yaparak kalıp uyuşmazlığı riskini sıfıra indirin.
              </p>
            </div>
          </div>
          <Button
            size="sm"
            variant="accent"
            onClick={() => setSelectorOpen(true)}
            className="shrink-0"
          >
            <Sparkles className="w-3.5 h-3.5 mr-1.5" />
            Aracını Seç
          </Button>
        </div>
      )}

      {/* MAIN LAYOUT: SIDEBAR FILTERS + PRODUCT GRID */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8 items-start">
        {/* Left Sidebar */}
        <aside className="space-y-6 lg:sticky lg:top-24">
          <div className="rounded-2xl border bg-white p-5 shadow-sm space-y-4">
            <div className="flex items-center gap-2 font-bold text-sm text-slate-900 pb-3 border-b">
              <Filter className="w-4 h-4 text-accent-orange" />
              <span>Kategoriye Göre Filtrele</span>
            </div>

            <div className="space-y-1">
              <button
                onClick={() => handleCategoryChange("")}
                className={`w-full text-left px-3 py-2 rounded-lg text-xs font-medium transition-colors flex items-center justify-between cursor-pointer ${
                  categoryParam === ""
                    ? "bg-accent-orange text-white font-semibold shadow-sm"
                    : "text-slate-700 hover:bg-slate-100"
                }`}
              >
                <span>Tüm Kategoriler</span>
              </button>

              {loadingCategories ? (
                <div className="space-y-2 py-2">
                  <div className="h-8 bg-muted animate-pulse rounded-lg" />
                  <div className="h-8 bg-muted animate-pulse rounded-lg" />
                </div>
              ) : (
                categories.map((cat) => (
                  <button
                    key={cat.id}
                    onClick={() => handleCategoryChange(cat.slug)}
                    className={`w-full text-left px-3 py-2 rounded-lg text-xs font-medium transition-colors flex items-center justify-between cursor-pointer ${
                      categoryParam === cat.slug
                        ? "bg-accent-orange text-white font-semibold shadow-sm"
                        : "text-slate-700 hover:bg-slate-100"
                    }`}
                  >
                    <span>{cat.name}</span>
                  </button>
                ))
              )}
            </div>
          </div>

          {/* Quick Help Card */}
          <div className="rounded-2xl bg-gradient-to-br from-slate-900 to-slate-800 text-white p-5 space-y-3">
            <Layers className="w-8 h-8 text-accent-orange" />
            <h4 className="font-bold text-sm">Doğru Paspas Seçimi</h4>
            <p className="text-xs text-slate-300 leading-relaxed">
              Aracınızın kasa ve model yılına göre üretilen 3D paspaslar tam kapatma sağlar. Destek için bize ulaşabilirsiniz.
            </p>
            <p className="text-xs font-semibold text-accent-orange">
              Tel: 0850 000 00 00
            </p>
          </div>
        </aside>

        {/* Right Product Grid */}
        <div className="lg:col-span-3 space-y-6">
          {isLoading ? (
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4 sm:gap-6">
              {[...Array(6)].map((_, i) => (
                <div key={i} className="h-72 bg-muted animate-pulse rounded-2xl" />
              ))}
            </div>
          ) : displayProducts.length === 0 ? (
            <div className="rounded-2xl border bg-white p-12 text-center text-muted-foreground space-y-4">
              <div className="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center text-slate-400 mx-auto">
                <ShoppingBag className="w-8 h-8" />
              </div>
              <h3 className="text-lg font-bold text-slate-900">Ürün Bulunamadı</h3>
              <p className="text-xs max-w-sm mx-auto">
                Seçilen kriterlere uygun ürün bulunamadı. Filtreleri temizleyerek veya araç modelinizi değiştirerek tekrar deneyebilirsiniz.
              </p>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleCategoryChange("")}
              >
                Filtreleri Temizle
              </Button>
            </div>
          ) : (
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4 sm:gap-6">
              {displayProducts.map((product) => (
                <ProductCard
                  key={product.id}
                  product={product}
                  isCompatible={isFitmentActive}
                />
              ))}
            </div>
          )}

          {/* Pagination (only for generic product listing) */}
          {!isFitmentActive && totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 pt-6 border-t">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                disabled={currentPage === 0}
                className="gap-1"
              >
                <ChevronLeft className="w-4 h-4" /> Önceki
              </Button>
              <span className="text-xs font-semibold text-slate-600 px-3">
                Sayfa {currentPage + 1} / {totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={currentPage >= totalPages - 1}
                className="gap-1"
              >
                Sonraki <ChevronRight className="w-4 h-4" />
              </Button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default function CatalogPage() {
  return (
    <Suspense
      fallback={
        <div className="container mx-auto px-4 py-12">
          <div className="h-64 bg-muted animate-pulse rounded-2xl" />
        </div>
      }
    >
      <CatalogContent />
    </Suspense>
  );
}
