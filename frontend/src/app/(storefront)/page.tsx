"use client";

import { useQuery } from "@tanstack/react-query";
import { catalogApi } from "@/features/catalog/catalog-api";
import { useVehicleStore } from "@/stores/vehicle-store";
import { ProductCard } from "@/components/product/product-card";
import { Button } from "@/components/ui/button";
import {
  Car,
  Sparkles,
  ShieldCheck,
  Truck,
  RotateCcw,
  Award,
  Layers,
  CheckCircle2,
  ArrowRight,
} from "lucide-react";
import Link from "next/link";

export default function HomePage() {
  const { selectedVehicle, setSelectorOpen } = useVehicleStore();

  // Fetch Featured/Latest Products
  const { data: productPage, isLoading: loadingProducts } = useQuery({
    queryKey: ["home-featured-products"],
    queryFn: () => catalogApi.getProducts({ size: 8 }),
  });

  // If vehicle is selected, fetch compatible products
  const { data: compatibleProducts = [], isLoading: loadingCompatible } = useQuery({
    queryKey: [
      "home-compatible-products",
      selectedVehicle?.variant?.id,
      selectedVehicle?.year,
    ],
    queryFn: () =>
      catalogApi.getCompatibleProducts({
        variantId: selectedVehicle!.variant!.id,
        year: selectedVehicle?.year,
      }),
    enabled: !!selectedVehicle?.variant?.id,
  });

  const products = productPage?.content || [];

  return (
    <div className="space-y-16 pb-16">
      {/* HERO SECTION */}
      <section className="relative bg-gradient-to-br from-slate-950 via-slate-900 to-slate-800 text-white overflow-hidden py-16 sm:py-24">
        {/* Ambient glow decoration */}
        <div className="absolute top-0 right-1/4 w-96 h-96 bg-accent-orange/10 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute bottom-0 left-10 w-80 h-80 bg-blue-500/10 rounded-full blur-3xl pointer-events-none" />

        <div className="container mx-auto px-4 relative z-10">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
            {/* Left Content */}
            <div className="lg:col-span-7 space-y-6 text-center lg:text-left">
              <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-accent-orange/20 border border-accent-orange/30 text-accent-orange text-xs font-semibold">
                <Sparkles className="w-4 h-4" />
                <span>%100 Lazer Kesim • Araca Birebir Uyum</span>
              </div>

              <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight leading-[1.15] text-white">
                Aracınıza Özel <br />
                <span className="text-transparent bg-clip-text bg-gradient-to-r from-orange-400 via-amber-300 to-orange-500">
                  3D Havuzlu Paspas & Bagaj Havuzu
                </span>
              </h1>

              <p className="text-base sm:text-lg text-slate-300 max-w-xl mx-auto lg:mx-0 leading-relaxed font-light">
                Kokusuz orijinal TPE malzemeden üretilen, 4-5 cm yüksek kenarlarıyla sıvı ve çamuru hapseden yeni nesil koruma.
              </p>

              {/* Action buttons */}
              <div className="flex flex-col sm:flex-row items-center justify-center lg:justify-start gap-4 pt-2">
                <Button
                  size="lg"
                  variant="accent"
                  onClick={() => setSelectorOpen(true)}
                  className="w-full sm:w-auto text-base px-8 py-6 shadow-xl shadow-orange-950/40 rounded-xl"
                >
                  <Car className="w-5 h-5 mr-2" />
                  {selectedVehicle ? "Seçili Aracı Değiştir" : "Hemen Aracını Seç"}
                </Button>
                <Button
                  size="lg"
                  variant="outline"
                  asChild
                  className="w-full sm:w-auto text-base px-8 py-6 rounded-xl border-slate-700 bg-slate-900/50 text-white hover:bg-slate-800"
                >
                  <Link href="/katalog">Kataloğu İncele</Link>
                </Button>
              </div>

              {/* Trust bullets */}
              <div className="pt-6 grid grid-cols-3 gap-4 border-t border-slate-800/80 text-xs text-slate-400">
                <div className="flex items-center gap-2 justify-center lg:justify-start">
                  <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span>Kokusuz TPE Hammadde</span>
                </div>
                <div className="flex items-center gap-2 justify-center lg:justify-start">
                  <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span>Yüksek Sıvı Bariyeri</span>
                </div>
                <div className="flex items-center gap-2 justify-center lg:justify-start">
                  <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                  <span>2 Yıl Birebir Değişim</span>
                </div>
              </div>
            </div>

            {/* Right Fast Selector Card */}
            <div className="lg:col-span-5">
              <div className="rounded-3xl border border-slate-700/60 bg-slate-900/80 backdrop-blur-xl p-6 sm:p-8 shadow-2xl space-y-6">
                <div className="space-y-2">
                  <span className="text-xs uppercase tracking-wider text-accent-orange font-bold">
                    Hızlı Uyum Sihirbazı
                  </span>
                  <h3 className="text-xl font-bold text-white">
                    Aracınız için en doğru paspası bulun
                  </h3>
                  <p className="text-xs text-slate-400">
                    Kalıp uyumsuzluğuna son. Marka ve modelinizi seçerek aracınıza tam oturan ürünleri listeleyin.
                  </p>
                </div>

                {selectedVehicle ? (
                  <div className="p-4 rounded-2xl bg-emerald-950/60 border border-emerald-800/80 space-y-3">
                    <div className="flex items-center gap-2 text-emerald-400 text-xs font-semibold">
                      <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
                      Aktif Seçili Araç:
                    </div>
                    <div className="text-base font-bold text-white">
                      {[
                        selectedVehicle.year,
                        selectedVehicle.brand.name,
                        selectedVehicle.model.name,
                        selectedVehicle.generation.name,
                      ].filter(Boolean).join(" ")}
                    </div>
                    {selectedVehicle.variant && (
                      <div className="text-xs text-emerald-200">
                        Varyant: {selectedVehicle.variant.name}
                      </div>
                    )}
                    <Button
                      variant="accent"
                      size="sm"
                      className="w-full mt-2"
                      onClick={() => setSelectorOpen(true)}
                    >
                      Aracı Değiştir
                    </Button>
                  </div>
                ) : (
                  <div className="space-y-4 pt-2">
                    <button
                      onClick={() => setSelectorOpen(true)}
                      className="w-full p-4 rounded-2xl border border-slate-700 bg-slate-800/60 hover:border-accent-orange hover:bg-slate-800 transition-all text-left flex items-center justify-between group cursor-pointer"
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-slate-700 flex items-center justify-center text-accent-orange group-hover:scale-105 transition-transform">
                          <Car className="w-5 h-5" />
                        </div>
                        <div>
                          <div className="text-xs text-slate-400">1. Adım</div>
                          <div className="font-semibold text-sm text-white">
                            Marka, Model ve Yıl Seçin
                          </div>
                        </div>
                      </div>
                      <ArrowRight className="w-5 h-5 text-accent-orange group-hover:translate-x-1 transition-transform" />
                    </button>

                    <Button
                      variant="accent"
                      size="lg"
                      onClick={() => setSelectorOpen(true)}
                      className="w-full rounded-xl py-6 font-semibold"
                    >
                      Seçime Başla
                    </Button>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* COMPATIBLE PRODUCTS (IF VEHICLE SELECTED) */}
      {selectedVehicle && (
        <section className="container mx-auto px-4">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-8">
            <div>
              <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-emerald-600">
                <span className="w-2 h-2 rounded-full bg-emerald-600" />
                Aracınıza Birebir Uyumlu
              </div>
              <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900 mt-1">
                {selectedVehicle.brand.name} {selectedVehicle.model.name} İçin Ürünler
              </h2>
            </div>
            <Link
              href={`/katalog?variantId=${selectedVehicle.variant?.id || ""}`}
              className="text-sm font-semibold text-accent-orange hover:underline flex items-center gap-1"
            >
              Tümünü Gör <ArrowRight className="w-4 h-4" />
            </Link>
          </div>

          {loadingCompatible ? (
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 sm:gap-6">
              {[...Array(4)].map((_, i) => (
                <div key={i} className="h-72 bg-muted animate-pulse rounded-2xl" />
              ))}
            </div>
          ) : compatibleProducts.length > 0 ? (
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 sm:gap-6">
              {compatibleProducts.map((product) => (
                <ProductCard key={product.id} product={product} isCompatible={true} />
              ))}
            </div>
          ) : (
            <div className="p-8 text-center rounded-2xl bg-slate-50 border text-muted-foreground">
              Bu varyant için henüz uyumlu ürün tanımlanmamış olabilir.
            </div>
          )}
        </section>
      )}

      {/* CATEGORY SHORTCUTS */}
      <section className="container mx-auto px-4">
        <div className="text-center max-w-2xl mx-auto mb-10 space-y-2">
          <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900">
            Popüler Kategoriler
          </h2>
          <p className="text-sm text-muted-foreground">
            Aracınızın zeminini ve bagajını kir, toz ve sıvı dökülmelerine karşı koruyan ana ürün grupları.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* 3D Paspas */}
          <Link
            href="/katalog?category=3d-oto-paspas"
            className="group relative overflow-hidden rounded-3xl border bg-gradient-to-br from-slate-900 to-slate-800 p-8 text-white shadow-md hover:shadow-2xl transition-all duration-300"
          >
            <div className="space-y-3 relative z-10">
              <span className="px-3 py-1 rounded-full bg-accent-orange text-white text-xs font-bold uppercase tracking-wider">
                Çok Satan
              </span>
              <h3 className="text-2xl font-bold">3D Havuzlu Oto Paspas</h3>
              <p className="text-xs text-slate-300 max-w-xs">
                Aracın zemin şekline tam oturan, kaymaz tabanlı, yüksek havuz kenarlı paspas takımları.
              </p>
              <span className="inline-flex items-center gap-1.5 text-accent-orange text-sm font-semibold pt-4 group-hover:translate-x-1 transition-transform">
                Ürünleri İncele <ArrowRight className="w-4 h-4" />
              </span>
            </div>
            <Layers className="absolute -bottom-4 -right-4 w-40 h-40 text-slate-800/40 pointer-events-none group-hover:scale-110 transition-transform" />
          </Link>

          {/* 3D Bagaj Havuzu */}
          <Link
            href="/katalog?category=bagaj-havuzu"
            className="group relative overflow-hidden rounded-3xl border bg-gradient-to-br from-slate-800 to-slate-900 p-8 text-white shadow-md hover:shadow-2xl transition-all duration-300"
          >
            <div className="space-y-3 relative z-10">
              <span className="px-3 py-1 rounded-full bg-emerald-600 text-white text-xs font-bold uppercase tracking-wider">
                Maksimum Koruma
              </span>
              <h3 className="text-2xl font-bold">3D Bagaj Havuzu</h3>
              <p className="text-xs text-slate-300 max-w-xs">
                Bagaj zeminini lekelere ve ağır yüklere karşı koruyan, kolay temizlenebilir koruyucu zemin.
              </p>
              <span className="inline-flex items-center gap-1.5 text-emerald-400 text-sm font-semibold pt-4 group-hover:translate-x-1 transition-transform">
                Ürünleri İncele <ArrowRight className="w-4 h-4" />
              </span>
            </div>
            <Car className="absolute -bottom-4 -right-4 w-40 h-40 text-slate-800/40 pointer-events-none group-hover:scale-110 transition-transform" />
          </Link>

          {/* Tam Koruma Seti */}
          <Link
            href="/katalog"
            className="group relative overflow-hidden rounded-3xl border bg-gradient-to-br from-orange-950/80 to-slate-900 p-8 text-white shadow-md hover:shadow-2xl transition-all duration-300"
          >
            <div className="space-y-3 relative z-10">
              <span className="px-3 py-1 rounded-full bg-amber-500 text-slate-950 text-xs font-bold uppercase tracking-wider">
                Avantajlı Set
              </span>
              <h3 className="text-2xl font-bold">Paspas + Bagaj Setleri</h3>
              <p className="text-xs text-slate-300 max-w-xs">
                Havuzlu paspas ve bagaj havuzunu birlikte alarak ek indirim ve ücretsiz kargo kazanın.
              </p>
              <span className="inline-flex items-center gap-1.5 text-amber-400 text-sm font-semibold pt-4 group-hover:translate-x-1 transition-transform">
                Tüm Setleri Gör <ArrowRight className="w-4 h-4" />
              </span>
            </div>
            <Award className="absolute -bottom-4 -right-4 w-40 h-40 text-orange-900/30 pointer-events-none group-hover:scale-110 transition-transform" />
          </Link>
        </div>
      </section>

      {/* FEATURED PRODUCTS */}
      <section className="container mx-auto px-4">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-8">
          <div>
            <div className="text-xs font-semibold uppercase tracking-wider text-accent-orange">
              Öne Çıkan Ürünler
            </div>
            <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900 mt-1">
              En Çok Tercih Edilen 3D Paspaslar
            </h2>
          </div>
          <Link
            href="/katalog"
            className="text-sm font-semibold text-accent-orange hover:underline flex items-center gap-1"
          >
            Tüm Kataloğu Gör <ArrowRight className="w-4 h-4" />
          </Link>
        </div>

        {loadingProducts ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 sm:gap-6">
            {[...Array(8)].map((_, i) => (
              <div key={i} className="h-72 bg-muted animate-pulse rounded-2xl" />
            ))}
          </div>
        ) : products.length > 0 ? (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 sm:gap-6">
            {products.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        ) : (
          <div className="p-12 text-center rounded-2xl bg-slate-50 border text-muted-foreground">
            Henüz listelenecek ürün bulunamadı.
          </div>
        )}
      </section>

      {/* WHY US / TECHNOLOGY & TRUST */}
      <section className="bg-slate-900 text-white py-16">
        <div className="container mx-auto px-4">
          <div className="text-center max-w-2xl mx-auto mb-12 space-y-2">
            <span className="text-xs uppercase tracking-wider text-accent-orange font-bold">
              Neden Biz?
            </span>
            <h2 className="text-3xl font-extrabold">Otomotiv Standartlarında Üretim</h2>
            <p className="text-sm text-slate-400">
              Piyasadaki üniversal paspasların aksine, aracınızın zemin geometrisine 0.1 mm hassasiyetle uyum sağlar.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div className="p-6 rounded-2xl bg-slate-800/60 border border-slate-700/60 space-y-3">
              <div className="w-12 h-12 rounded-xl bg-orange-500/20 text-accent-orange flex items-center justify-center font-bold text-lg">
                01
              </div>
              <h4 className="text-lg font-bold">3D Lazer Tarama</h4>
              <p className="text-xs text-slate-400 leading-relaxed">
                Her aracın zemin formu orijinal fabrikasyon kalıpları lazerle taranarak birebir çıkarılır.
              </p>
            </div>

            <div className="p-6 rounded-2xl bg-slate-800/60 border border-slate-700/60 space-y-3">
              <div className="w-12 h-12 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center font-bold text-lg">
                02
              </div>
              <h4 className="text-lg font-bold">Kokusuz TPE Malzeme</h4>
              <p className="text-xs text-slate-400 leading-relaxed">
                Güneş altında veya sıcak havada kesinlikle koku yapmaz. Geri dönüştürülebilir ve çevre dostudur.
              </p>
            </div>

            <div className="p-6 rounded-2xl bg-slate-800/60 border border-slate-700/60 space-y-3">
              <div className="w-12 h-12 rounded-xl bg-blue-500/20 text-blue-400 flex items-center justify-center font-bold text-lg">
                03
              </div>
              <h4 className="text-lg font-bold">4-5 cm Yüksek Havuz</h4>
              <p className="text-xs text-slate-400 leading-relaxed">
                Dökülen sıvı, kar, çamur ve tozu havuz içinde tutarak aracınızın orijinal halısına geçişini engeller.
              </p>
            </div>

            <div className="p-6 rounded-2xl bg-slate-800/60 border border-slate-700/60 space-y-3">
              <div className="w-12 h-12 rounded-xl bg-amber-500/20 text-amber-400 flex items-center justify-center font-bold text-lg">
                04
              </div>
              <h4 className="text-lg font-bold">Orijinal Sabitleme Klipsi</h4>
              <p className="text-xs text-slate-400 leading-relaxed">
                Aracın tabanındaki orijinal klips yuvalarına tam kilitlenir, sürüş esnasında pedal altına kayma yapmaz.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
