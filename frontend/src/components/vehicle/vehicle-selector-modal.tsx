"use client";

import { useState, useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { useVehicleStore } from "@/stores/vehicle-store";
import { vehicleApi } from "@/features/vehicle/vehicle-api";
import {
  VehicleBrand,
  VehicleModel,
  VehicleGeneration,
  VehicleVariant,
} from "@/types";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Car, ChevronRight, RotateCcw, Check, Sparkles } from "lucide-react";
import { useRouter } from "next/navigation";

export function VehicleSelectorModal() {
  const { isSelectorOpen, setSelectorOpen, selectVehicle } = useVehicleStore();
  const router = useRouter();

  const [selectedBrand, setSelectedBrand] = useState<VehicleBrand | null>(null);
  const [selectedModel, setSelectedModel] = useState<VehicleModel | null>(null);
  const [selectedGen, setSelectedGen] = useState<VehicleGeneration | null>(null);
  const [selectedYear, setSelectedYear] = useState<number | null>(null);
  const [selectedVariant, setSelectedVariant] = useState<VehicleVariant | null>(null);

  // 1. Fetch Brands
  const { data: brands = [], isLoading: loadingBrands } = useQuery({
    queryKey: ["vehicle-brands"],
    queryFn: () => vehicleApi.getBrands(),
    enabled: isSelectorOpen,
  });

  // 2. Fetch Models
  const { data: models = [], isLoading: loadingModels } = useQuery({
    queryKey: ["vehicle-models", selectedBrand?.id],
    queryFn: () => vehicleApi.getModels(selectedBrand!.id),
    enabled: !!selectedBrand?.id,
  });

  // 3. Fetch Generations
  const { data: generations = [], isLoading: loadingGenerations } = useQuery({
    queryKey: ["vehicle-generations", selectedModel?.id],
    queryFn: () => vehicleApi.getGenerations(selectedModel!.id),
    enabled: !!selectedModel?.id,
  });

  // 4. Fetch Variants
  const { data: variants = [], isLoading: loadingVariants } = useQuery({
    queryKey: ["vehicle-variants", selectedGen?.id],
    queryFn: () => vehicleApi.getVariants(selectedGen!.id),
    enabled: !!selectedGen?.id,
  });

  // Calculate year options based on selected generation
  const yearOptions = useMemo(() => {
    if (!selectedGen) return [];
    const start = selectedGen.startYear || 2010;
    const currentYear = new Date().getFullYear();
    const end = selectedGen.endYear || currentYear;
    const years: number[] = [];
    for (let y = end; y >= start; y--) {
      years.push(y);
    }
    return years;
  }, [selectedGen]);

  const handleReset = () => {
    setSelectedBrand(null);
    setSelectedModel(null);
    setSelectedGen(null);
    setSelectedYear(null);
    setSelectedVariant(null);
  };

  const handleComplete = () => {
    if (!selectedBrand || !selectedModel || !selectedGen) return;

    selectVehicle({
      brand: selectedBrand,
      model: selectedModel,
      generation: selectedGen,
      variant: selectedVariant || undefined,
      year: selectedYear || undefined,
    });

    handleReset();
    setSelectorOpen(false);

    if (selectedVariant?.id) {
      router.push(`/katalog?variantId=${selectedVariant.id}${selectedYear ? `&year=${selectedYear}` : ""}`);
    } else {
      router.push("/katalog");
    }
  };

  return (
    <Dialog open={isSelectorOpen} onOpenChange={setSelectorOpen}>
      <DialogContent className="sm:max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <div className="flex items-center gap-2 text-accent-orange font-semibold text-sm">
            <Car className="w-4 h-4" />
            <span>Adım Adım Araç Seçimi</span>
          </div>
          <DialogTitle className="text-xl">Aracınıza Birebir Uyumlu Ürünleri Bulun</DialogTitle>
          <DialogDescription>
            Marka, model, kasa ve varyant seçerek aracınıza tam oturan havuzlu paspas ve bagaj havuzlarını görüntüleyin.
          </DialogDescription>
        </DialogHeader>

        {/* Selected Breadcrumb path */}
        {(selectedBrand || selectedModel || selectedGen || selectedYear) && (
          <div className="flex flex-wrap items-center gap-1.5 p-3 rounded-lg bg-muted text-xs font-medium text-slate-700">
            {selectedBrand && <span>{selectedBrand.name}</span>}
            {selectedModel && (
              <>
                <ChevronRight className="w-3.5 h-3.5 text-muted-foreground" />
                <span>{selectedModel.name}</span>
              </>
            )}
            {selectedGen && (
              <>
                <ChevronRight className="w-3.5 h-3.5 text-muted-foreground" />
                <span>{selectedGen.name}</span>
              </>
            )}
            {selectedYear && (
              <>
                <ChevronRight className="w-3.5 h-3.5 text-muted-foreground" />
                <span>{selectedYear}</span>
              </>
            )}
            {selectedVariant && (
              <>
                <ChevronRight className="w-3.5 h-3.5 text-muted-foreground" />
                <span>{selectedVariant.name}</span>
              </>
            )}
            <button
              onClick={handleReset}
              className="ml-auto inline-flex items-center gap-1 text-accent-orange hover:underline cursor-pointer"
            >
              <RotateCcw className="w-3 h-3" />
              Sıfırla
            </button>
          </div>
        )}

        <div className="space-y-6 py-2">
          {/* STEP 1: BRAND */}
          {!selectedBrand && (
            <div>
              <h4 className="text-sm font-semibold mb-3 flex items-center gap-1.5">
                <span className="w-5 h-5 rounded-full bg-primary text-primary-foreground text-xs inline-flex items-center justify-center font-bold">
                  1
                </span>
                Araç Markasını Seçin:
              </h4>
              {loadingBrands ? (
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
                  {[...Array(8)].map((_, i) => (
                    <div key={i} className="h-12 bg-muted animate-pulse rounded-lg" />
                  ))}
                </div>
              ) : (
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
                  {brands.map((brand) => (
                    <button
                      key={brand.id}
                      onClick={() => {
                        setSelectedBrand(brand);
                        setSelectedModel(null);
                        setSelectedGen(null);
                        setSelectedYear(null);
                        setSelectedVariant(null);
                      }}
                      className="p-3 text-center border rounded-xl hover:border-accent-orange hover:bg-orange-50/50 hover:text-accent-orange transition-all font-medium text-sm flex flex-col items-center justify-center gap-1 cursor-pointer group"
                    >
                      <span>{brand.name}</span>
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* STEP 2: MODEL */}
          {selectedBrand && !selectedModel && (
            <div>
              <h4 className="text-sm font-semibold mb-3 flex items-center gap-1.5">
                <span className="w-5 h-5 rounded-full bg-primary text-primary-foreground text-xs inline-flex items-center justify-center font-bold">
                  2
                </span>
                {selectedBrand.name} Modelini Seçin:
              </h4>
              {loadingModels ? (
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
                  {[...Array(6)].map((_, i) => (
                    <div key={i} className="h-12 bg-muted animate-pulse rounded-lg" />
                  ))}
                </div>
              ) : (
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
                  {models.map((model) => (
                    <button
                      key={model.id}
                      onClick={() => {
                        setSelectedModel(model);
                        setSelectedGen(null);
                        setSelectedYear(null);
                        setSelectedVariant(null);
                      }}
                      className="p-3 border rounded-xl text-left hover:border-accent-orange hover:bg-orange-50/50 hover:text-accent-orange transition-all font-medium text-sm flex items-center justify-between cursor-pointer"
                    >
                      <span>{model.name}</span>
                      <ChevronRight className="w-4 h-4 text-muted-foreground" />
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* STEP 3: GENERATION / KASA */}
          {selectedModel && !selectedGen && (
            <div>
              <h4 className="text-sm font-semibold mb-3 flex items-center gap-1.5">
                <span className="w-5 h-5 rounded-full bg-primary text-primary-foreground text-xs inline-flex items-center justify-center font-bold">
                  3
                </span>
                Kasa / Jenerasyon Seçin:
              </h4>
              {loadingGenerations ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                  {[...Array(4)].map((_, i) => (
                    <div key={i} className="h-16 bg-muted animate-pulse rounded-lg" />
                  ))}
                </div>
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                  {generations.map((gen) => (
                    <button
                      key={gen.id}
                      onClick={() => {
                        setSelectedGen(gen);
                        setSelectedYear(null);
                        setSelectedVariant(null);
                      }}
                      className="p-3.5 border rounded-xl text-left hover:border-accent-orange hover:bg-orange-50/50 transition-all font-medium text-sm flex flex-col gap-1 cursor-pointer"
                    >
                      <div className="flex items-center justify-between">
                        <span className="font-semibold text-foreground">{gen.name}</span>
                        <ChevronRight className="w-4 h-4 text-muted-foreground" />
                      </div>
                      <span className="text-xs text-muted-foreground">
                        {gen.startYear} - {gen.endYear || "Günümüz"}
                      </span>
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* STEP 4: MODEL YEAR */}
          {selectedGen && !selectedYear && (
            <div>
              <h4 className="text-sm font-semibold mb-3 flex items-center gap-1.5">
                <span className="w-5 h-5 rounded-full bg-primary text-primary-foreground text-xs inline-flex items-center justify-center font-bold">
                  4
                </span>
                Model Yılını Seçin:
              </h4>
              <div className="grid grid-cols-3 sm:grid-cols-6 gap-2">
                {yearOptions.map((year) => (
                  <button
                    key={year}
                    onClick={() => {
                      setSelectedYear(year);
                    }}
                    className="p-2.5 border rounded-lg text-center hover:border-accent-orange hover:bg-orange-50/50 hover:text-accent-orange transition-all font-medium text-sm cursor-pointer"
                  >
                    {year}
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* STEP 5: VARIANT & CONFIRM */}
          {selectedGen && selectedYear && (
            <div>
              <h4 className="text-sm font-semibold mb-3 flex items-center gap-1.5">
                <span className="w-5 h-5 rounded-full bg-primary text-primary-foreground text-xs inline-flex items-center justify-center font-bold">
                  5
                </span>
                Gövde / Varyant Tipi Seçin:
              </h4>
              {loadingVariants ? (
                <div className="space-y-2">
                  {[...Array(3)].map((_, i) => (
                    <div key={i} className="h-12 bg-muted animate-pulse rounded-lg" />
                  ))}
                </div>
              ) : variants.length > 0 ? (
                <div className="space-y-2">
                  {variants.map((v) => (
                    <button
                      key={v.id}
                      onClick={() => setSelectedVariant(v)}
                      className={`w-full p-3.5 border rounded-xl text-left transition-all text-sm flex items-center justify-between cursor-pointer ${
                        selectedVariant?.id === v.id
                          ? "border-accent-orange bg-orange-50/80 text-accent-orange font-semibold shadow-sm"
                          : "hover:border-slate-400"
                      }`}
                    >
                      <div>
                        <div className="font-medium text-foreground">{v.name}</div>
                        <div className="text-xs text-muted-foreground">
                          {[v.bodyType, v.fuelType, v.trunkType].filter(Boolean).join(" • ")}
                        </div>
                      </div>
                      {selectedVariant?.id === v.id && (
                        <span className="inline-flex items-center justify-center w-6 h-6 rounded-full bg-accent-orange text-white">
                          <Check className="w-3.5 h-3.5" />
                        </span>
                      )}
                    </button>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-muted-foreground py-2">
                  Bu kasa için tek varyant mevcuttur.
                </p>
              )}

              <div className="mt-6 pt-4 border-t flex items-center justify-between gap-3">
                <Button
                  variant="outline"
                  onClick={() => setSelectedYear(null)}
                  size="sm"
                >
                  Geri
                </Button>
                <Button
                  variant="accent"
                  onClick={handleComplete}
                  disabled={!selectedGen}
                  className="gap-2"
                >
                  <Sparkles className="w-4 h-4" />
                  Uyumlu Ürünleri Listele
                </Button>
              </div>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}
