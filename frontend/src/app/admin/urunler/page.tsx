"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { adminApi } from "@/features/admin/admin-api";
import { vehicleApi } from "@/features/vehicle/vehicle-api";
import {
  ProductDetail,
  ProductStatus,
  VehicleBrand,
  VehicleModel,
  VehicleGeneration,
  VehicleVariant,
} from "@/types";
import { PRODUCT_STATUS_LABELS } from "@/lib/constants";
import { formatPrice } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  ShoppingBag,
  Plus,
  Edit3,
  Trash2,
  Car,
  Image as ImageIcon,
  Layers,
  Search,
  Check,
  ChevronRight,
} from "lucide-react";
import { toast } from "sonner";

export default function AdminProductsPage() {
  const queryClient = useQueryClient();

  const [search, setSearch] = useState("");
  const [selectedStatus, setSelectedStatus] = useState<ProductStatus | "">("");
  const [selectedCategoryFilter, setSelectedCategoryFilter] = useState<string>("");
  const [page, setPage] = useState<number>(0);

  // Modals state
  const [isProductModalOpen, setIsProductModalOpen] = useState(false);
  const [editingProductId, setEditingProductId] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<"info" | "images" | "features" | "compatibilities">("info");

  // Product Form state
  const [name, setName] = useState("");
  const [sku, setSku] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [basePrice, setBasePrice] = useState("");
  const [salePrice, setSalePrice] = useState("");
  const [stockQuantity, setStockQuantity] = useState("50");
  const [status, setStatus] = useState<ProductStatus>("ACTIVE");
  const [manufacturerBrand, setManufacturerBrand] = useState("OtoPaspas");
  const [material, setMaterial] = useState("TPE Orijinal Hammadde");
  const [description, setDescription] = useState("");

  // Sub-resource states
  const [newImageUrl, setNewImageUrl] = useState("");
  const [newImagePrimary, setNewImagePrimary] = useState(false);
  const [newFeatureTitle, setNewFeatureTitle] = useState("");
  const [newFeatureDesc, setNewFeatureDesc] = useState("");

  // Compatibility Form state
  const [compatBrand, setCompatBrand] = useState<VehicleBrand | null>(null);
  const [compatModel, setCompatModel] = useState<VehicleModel | null>(null);
  const [compatGen, setCompatGen] = useState<VehicleGeneration | null>(null);
  const [compatVariant, setCompatVariant] = useState<VehicleVariant | null>(null);
  const [compatStartYear, setCompatStartYear] = useState<string>("");
  const [compatEndYear, setCompatEndYear] = useState<string>("");
  const [compatNotes, setCompatNotes] = useState<string>("");

  // 1. Fetch Admin Products
  const { data: productPage, isLoading } = useQuery({
    queryKey: ["admin-products", page, search, selectedStatus, selectedCategoryFilter],
    queryFn: () =>
      adminApi.getProducts({
        page,
        size: 10,
        search: search.trim() || undefined,
        status: (selectedStatus as ProductStatus) || undefined,
        categoryId: selectedCategoryFilter || undefined,
      }),
  });

  // 2. Fetch Categories for select
  const { data: categories = [] } = useQuery({
    queryKey: ["admin-categories"],
    queryFn: () => adminApi.getCategories(),
  });

  // 3. Fetch Product Detail for editing
  const { data: editingProduct, refetch: refetchEditingProduct } = useQuery({
    queryKey: ["admin-product-detail", editingProductId],
    queryFn: () => adminApi.getProductById(editingProductId!),
    enabled: !!editingProductId,
  });

  // Vehicle Hierarchy queries for compatibility
  const { data: brands = [] } = useQuery({
    queryKey: ["vehicle-brands"],
    queryFn: () => vehicleApi.getBrands(),
    enabled: activeTab === "compatibilities" && isProductModalOpen,
  });

  const { data: models = [] } = useQuery({
    queryKey: ["vehicle-models", compatBrand?.id],
    queryFn: () => vehicleApi.getModels(compatBrand!.id),
    enabled: !!compatBrand?.id,
  });

  const { data: generations = [] } = useQuery({
    queryKey: ["vehicle-generations", compatModel?.id],
    queryFn: () => vehicleApi.getGenerations(compatModel!.id),
    enabled: !!compatModel?.id,
  });

  const { data: variants = [] } = useQuery({
    queryKey: ["vehicle-variants", compatGen?.id],
    queryFn: () => vehicleApi.getVariants(compatGen!.id),
    enabled: !!compatGen?.id,
  });

  const openNewProductModal = () => {
    setEditingProductId(null);
    setName("");
    setSku("");
    setCategoryId(categories[0]?.id || "");
    setBasePrice("");
    setSalePrice("");
    setStockQuantity("50");
    setStatus("ACTIVE");
    setManufacturerBrand("OtoPaspas");
    setMaterial("TPE Orijinal Hammadde");
    setDescription("");
    setActiveTab("info");
    setIsProductModalOpen(true);
  };

  const openEditProductModal = (product: ProductDetail) => {
    setEditingProductId(product.id);
    setName(product.name);
    setSku(product.sku);
    setCategoryId(product.category?.id || "");
    setBasePrice(String(product.basePrice));
    setSalePrice(product.salePrice ? String(product.salePrice) : "");
    setStockQuantity(String(product.stockQuantity));
    setStatus(product.inStock ? "ACTIVE" : "OUT_OF_STOCK");
    setManufacturerBrand(product.manufacturerBrand || "OtoPaspas");
    setMaterial(product.material || "TPE Orijinal Hammadde");
    setDescription(product.description || "");
    setActiveTab("info");
    setIsProductModalOpen(true);
  };

  // Mutations
  const saveProductMutation = useMutation({
    mutationFn: async () => {
      const payload = {
        name: name.trim(),
        sku: sku.trim(),
        categoryId,
        basePrice: parseFloat(basePrice),
        salePrice: salePrice ? parseFloat(salePrice) : undefined,
        stockQuantity: parseInt(stockQuantity) || 0,
        status,
        manufacturerBrand: manufacturerBrand.trim(),
        material: material.trim(),
        description: description.trim() || undefined,
      };

      if (editingProductId) {
        return adminApi.updateProduct(editingProductId, payload);
      } else {
        return adminApi.createProduct(payload);
      }
    },
    onSuccess: (res) => {
      queryClient.invalidateQueries({ queryKey: ["admin-products"] });
      queryClient.invalidateQueries({ queryKey: ["catalog-products"] });
      toast.success(editingProductId ? "Ürün güncellendi." : "Yeni ürün oluşturuldu.");
      if (!editingProductId) {
        setEditingProductId(res.id);
        setActiveTab("images");
      }
    },
    onError: (err: any) => {
      toast.error(err.message || "Ürün kaydedilemedi.");
    },
  });

  const addImageMutation = useMutation({
    mutationFn: () =>
      adminApi.addImage(editingProductId!, {
        imageUrl: newImageUrl.trim(),
        primary: newImagePrimary,
      }),
    onSuccess: () => {
      refetchEditingProduct();
      setNewImageUrl("");
      setNewImagePrimary(false);
      toast.success("Görsel eklendi.");
    },
    onError: (err: any) => {
      toast.error(err.message || "Görsel eklenemedi.");
    },
  });

  const deleteImageMutation = useMutation({
    mutationFn: (imageId: string) =>
      adminApi.deleteImage(editingProductId!, imageId),
    onSuccess: () => {
      refetchEditingProduct();
      toast.success("Görsel silindi.");
    },
  });

  const addFeatureMutation = useMutation({
    mutationFn: () =>
      adminApi.addFeature(editingProductId!, {
        title: newFeatureTitle.trim(),
        description: newFeatureDesc.trim(),
      }),
    onSuccess: () => {
      refetchEditingProduct();
      setNewFeatureTitle("");
      setNewFeatureDesc("");
      toast.success("Teknik özellik eklendi.");
    },
    onError: (err: any) => {
      toast.error(err.message || "Özellik eklenemedi.");
    },
  });

  const deleteFeatureMutation = useMutation({
    mutationFn: (featureId: string) =>
      adminApi.deleteFeature(editingProductId!, featureId),
    onSuccess: () => {
      refetchEditingProduct();
      toast.success("Özellik silindi.");
    },
  });

  const addCompatibilityMutation = useMutation({
    mutationFn: () =>
      adminApi.addCompatibility(editingProductId!, {
        variantId: compatVariant!.id,
        startYear: compatStartYear ? parseInt(compatStartYear) : undefined,
        endYear: compatEndYear ? parseInt(compatEndYear) : undefined,
        notes: compatNotes.trim() || undefined,
      }),
    onSuccess: () => {
      refetchEditingProduct();
      setCompatVariant(null);
      setCompatStartYear("");
      setCompatEndYear("");
      setCompatNotes("");
      toast.success("Araç uyumluluk eşleştirmesi eklendi! ✓");
    },
    onError: (err: any) => {
      toast.error(err.message || "Uyumluluk eklenemedi.");
    },
  });

  const deleteCompatibilityMutation = useMutation({
    mutationFn: (compatibilityId: string) =>
      adminApi.deleteCompatibility(editingProductId!, compatibilityId),
    onSuccess: () => {
      refetchEditingProduct();
      toast.success("Uyumluluk kaydı silindi.");
    },
  });

  const products = productPage?.content || [];
  const totalPages = productPage?.totalPages || 1;

  return (
    <div className="space-y-6">
      {/* Header & Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
            Ürün & Uyumluluk Yönetimi
          </h1>
          <p className="text-xs text-muted-foreground mt-0.5">
            Paspas, bagaj havuzu ve araç varyantı eşleştirmelerini tek panelden yönetin.
          </p>
        </div>
        <Button onClick={openNewProductModal} variant="accent" size="sm" className="gap-2">
          <Plus className="w-4 h-4" /> Yeni Ürün Ekle
        </Button>
      </div>

      {/* Filter Bar */}
      <div className="p-4 rounded-2xl bg-white border shadow-sm flex flex-wrap items-center gap-3 text-xs">
        <div className="relative flex-1 min-w-[200px]">
          <Input
            placeholder="Ürün adı veya SKU ile ara..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-9 h-9 text-xs"
          />
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5 pointer-events-none" />
        </div>

        <select
          value={selectedCategoryFilter}
          onChange={(e) => setSelectedCategoryFilter(e.target.value)}
          className="h-9 px-3 rounded-lg border text-xs bg-background"
        >
          <option value="">Tüm Kategoriler</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>

        <select
          value={selectedStatus}
          onChange={(e) => setSelectedStatus(e.target.value as any)}
          className="h-9 px-3 rounded-lg border text-xs bg-background"
        >
          <option value="">Tüm Durumlar</option>
          <option value="ACTIVE">Aktif / Satışta</option>
          <option value="DRAFT">Taslak</option>
          <option value="PASSIVE">Pasif</option>
          <option value="OUT_OF_STOCK">Tükendi</option>
        </select>
      </div>

      {/* Products Table */}
      <div className="rounded-3xl border bg-white shadow-sm overflow-hidden">
        {isLoading ? (
          <div className="p-8 space-y-3">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-12 bg-muted animate-pulse rounded-xl" />
            ))}
          </div>
        ) : products.length === 0 ? (
          <div className="text-center py-12 text-xs text-muted-foreground">
            Arama kriterlerine uygun ürün bulunamadı.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 font-semibold border-b">
                <tr>
                  <th className="p-4">Ürün</th>
                  <th className="p-4">SKU</th>
                  <th className="p-4">Kategori</th>
                  <th className="p-4">Fiyat</th>
                  <th className="p-4">Stok</th>
                  <th className="p-4">Durum</th>
                  <th className="p-4 text-right">İşlemler</th>
                </tr>
              </thead>
              <tbody className="divide-y text-slate-700">
                {products.map((p) => {
                  const statusLabel =
                    PRODUCT_STATUS_LABELS[p.inStock ? "ACTIVE" : "OUT_OF_STOCK"] || {
                      label: "Aktif",
                      color: "bg-emerald-100 text-emerald-800",
                    };
                  return (
                    <tr key={p.id} className="hover:bg-slate-50 transition-colors">
                      <td className="p-4 font-bold text-slate-900 flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-slate-100 border overflow-hidden flex items-center justify-center shrink-0">
                          {p.images?.[0]?.imageUrl ? (
                            // eslint-disable-next-line @next/next/no-img-element
                            <img
                              src={p.images[0].imageUrl}
                              alt={p.name}
                              className="w-full h-full object-cover"
                            />
                          ) : (
                            <ShoppingBag className="w-4 h-4 text-slate-400" />
                          )}
                        </div>
                        <span className="max-w-xs truncate">{p.name}</span>
                      </td>
                      <td className="p-4 font-mono text-slate-500">{p.sku}</td>
                      <td className="p-4 text-slate-600 font-medium">
                        {p.category?.name || "-"}
                      </td>
                      <td className="p-4 font-extrabold text-slate-900">
                        {formatPrice(p.effectivePrice)}
                      </td>
                      <td className="p-4 font-mono">{p.stockQuantity} adet</td>
                      <td className="p-4">
                        <span
                          className={`inline-flex px-2.5 py-0.5 rounded-full text-[11px] font-semibold border ${statusLabel.color}`}
                        >
                          {statusLabel.label}
                        </span>
                      </td>
                      <td className="p-4 text-right">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => openEditProductModal(p)}
                          className="h-8 gap-1 text-xs"
                        >
                          <Edit3 className="w-3.5 h-3.5" /> Yönet
                        </Button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* PRODUCT & COMPATIBILITY MANAGEMENT MODAL */}
      <Dialog open={isProductModalOpen} onOpenChange={setIsProductModalOpen}>
        <DialogContent className="sm:max-w-3xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>
              {editingProductId ? "Ürün & Uyumluluk Yönetimi" : "Yeni Ürün Oluştur"}
            </DialogTitle>
          </DialogHeader>

          {/* TABS */}
          <div className="flex border-b text-xs font-semibold">
            <button
              onClick={() => setActiveTab("info")}
              className={`px-4 py-2.5 border-b-2 transition-colors cursor-pointer ${
                activeTab === "info"
                  ? "border-accent-orange text-accent-orange"
                  : "border-transparent text-slate-500 hover:text-slate-900"
              }`}
            >
              1. Genel Bilgiler
            </button>
            <button
              onClick={() => setActiveTab("images")}
              disabled={!editingProductId}
              className={`px-4 py-2.5 border-b-2 transition-colors cursor-pointer disabled:opacity-40 ${
                activeTab === "images"
                  ? "border-accent-orange text-accent-orange"
                  : "border-transparent text-slate-500 hover:text-slate-900"
              }`}
            >
              2. Görseller ({editingProduct?.images?.length || 0})
            </button>
            <button
              onClick={() => setActiveTab("features")}
              disabled={!editingProductId}
              className={`px-4 py-2.5 border-b-2 transition-colors cursor-pointer disabled:opacity-40 ${
                activeTab === "features"
                  ? "border-accent-orange text-accent-orange"
                  : "border-transparent text-slate-500 hover:text-slate-900"
              }`}
            >
              3. Özellikler ({editingProduct?.features?.length || 0})
            </button>
            <button
              onClick={() => setActiveTab("compatibilities")}
              disabled={!editingProductId}
              className={`px-4 py-2.5 border-b-2 transition-colors cursor-pointer disabled:opacity-40 ${
                activeTab === "compatibilities"
                  ? "border-accent-orange text-accent-orange font-bold"
                  : "border-transparent text-slate-500 hover:text-slate-900"
              }`}
            >
              4. Araç Uyumlulukları ({editingProduct?.compatibilities?.length || 0})
            </button>
          </div>

          {/* TAB 1: PRODUCT INFO */}
          {activeTab === "info" && (
            <div className="space-y-4 text-xs py-2">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">
                  Ürün Adı *
                </label>
                <Input
                  required
                  placeholder="Volkswagen Passat B8 3D Havuzlu Paspas"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">
                    SKU (Stok Kodu) *
                  </label>
                  <Input
                    required
                    placeholder="PAS-VW-PAS-B8"
                    value={sku}
                    onChange={(e) => setSku(e.target.value.toUpperCase())}
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">
                    Kategori *
                  </label>
                  <select
                    value={categoryId}
                    onChange={(e) => setCategoryId(e.target.value)}
                    className="w-full h-10 px-3 rounded-lg border text-xs bg-background"
                  >
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">
                    Liste Fiyatı (TL) *
                  </label>
                  <Input
                    required
                    type="number"
                    step="0.01"
                    placeholder="1250.00"
                    value={basePrice}
                    onChange={(e) => setBasePrice(e.target.value)}
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">
                    İndirimli Fiyat (TL)
                  </label>
                  <Input
                    type="number"
                    step="0.01"
                    placeholder="999.00"
                    value={salePrice}
                    onChange={(e) => setSalePrice(e.target.value)}
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">
                    Stok Adedi *
                  </label>
                  <Input
                    required
                    type="number"
                    value={stockQuantity}
                    onChange={(e) => setStockQuantity(e.target.value)}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">
                    Üretici Marka
                  </label>
                  <Input
                    value={manufacturerBrand}
                    onChange={(e) => setManufacturerBrand(e.target.value)}
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">
                    Malzeme Türü
                  </label>
                  <Input
                    value={material}
                    onChange={(e) => setMaterial(e.target.value)}
                  />
                </div>
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">
                  Açıklama
                </label>
                <textarea
                  rows={3}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Ürünün malzeme kalitesi ve özellikleri hakkında bilgi..."
                  className="w-full p-3 rounded-xl border text-xs focus:outline-none focus:ring-2 focus:ring-accent-orange"
                />
              </div>

              <div className="flex justify-end gap-2 pt-4 border-t">
                <Button
                  type="button"
                  variant="accent"
                  disabled={saveProductMutation.isPending}
                  isLoading={saveProductMutation.isPending}
                  onClick={() => saveProductMutation.mutate()}
                >
                  {editingProductId ? "Değişiklikleri Kaydet" : "Ürünü Oluştur"}
                </Button>
              </div>
            </div>
          )}

          {/* TAB 2: IMAGES */}
          {activeTab === "images" && editingProduct && (
            <div className="space-y-4 text-xs py-2">
              <div className="p-4 rounded-2xl bg-slate-50 border space-y-3">
                <h4 className="font-bold text-xs text-slate-800">Yeni Görsel Ekle</h4>
                <div className="flex gap-2">
                  <Input
                    placeholder="Görsel URL (https://...)"
                    value={newImageUrl}
                    onChange={(e) => setNewImageUrl(e.target.value)}
                    className="flex-1"
                  />
                  <Button
                    variant="accent"
                    size="sm"
                    disabled={!newImageUrl.trim() || addImageMutation.isPending}
                    onClick={() => addImageMutation.mutate()}
                  >
                    Ekle
                  </Button>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                {editingProduct.images?.map((img) => (
                  <div
                    key={img.id}
                    className="relative aspect-video rounded-xl border bg-slate-100 overflow-hidden group"
                  >
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img
                      src={img.imageUrl}
                      alt="Product"
                      className="w-full h-full object-cover"
                    />
                    <button
                      onClick={() => deleteImageMutation.mutate(img.id)}
                      className="absolute top-2 right-2 p-1.5 rounded-full bg-red-600 text-white shadow-md cursor-pointer hover:bg-red-700"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                    {img.primary && (
                      <span className="absolute bottom-2 left-2 px-2 py-0.5 rounded bg-emerald-600 text-white text-[10px] font-bold">
                        Birincil
                      </span>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* TAB 3: FEATURES */}
          {activeTab === "features" && editingProduct && (
            <div className="space-y-4 text-xs py-2">
              <div className="p-4 rounded-2xl bg-slate-50 border space-y-3">
                <h4 className="font-bold text-xs text-slate-800">Teknik Özellik Ekle</h4>
                <div className="grid grid-cols-2 gap-3">
                  <Input
                    placeholder="Başlık (Örn: Lazer Kesim Kalıp)"
                    value={newFeatureTitle}
                    onChange={(e) => setNewFeatureTitle(e.target.value)}
                  />
                  <Input
                    placeholder="Açıklama (Örn: Milimetrik uyum sağlar)"
                    value={newFeatureDesc}
                    onChange={(e) => setNewFeatureDesc(e.target.value)}
                  />
                </div>
                <Button
                  variant="accent"
                  size="sm"
                  disabled={!newFeatureTitle.trim() || addFeatureMutation.isPending}
                  onClick={() => addFeatureMutation.mutate()}
                >
                  Özellik Ekle
                </Button>
              </div>

              <div className="space-y-2">
                {editingProduct.features?.map((f) => (
                  <div
                    key={f.id}
                    className="p-3 rounded-xl border bg-white flex items-center justify-between gap-3"
                  >
                    <div>
                      <span className="font-bold text-slate-900 block">{f.title}</span>
                      <span className="text-slate-500">{f.description}</span>
                    </div>
                    <button
                      onClick={() => deleteFeatureMutation.mutate(f.id)}
                      className="p-1 text-slate-400 hover:text-destructive cursor-pointer"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* TAB 4: VEHICLE COMPATIBILITIES */}
          {activeTab === "compatibilities" && editingProduct && (
            <div className="space-y-4 text-xs py-2">
              <div className="p-4 rounded-2xl bg-orange-50/50 border border-orange-200 space-y-3">
                <div className="flex items-center gap-2 font-bold text-xs text-orange-950">
                  <Car className="w-4 h-4 text-accent-orange" />
                  <span>Ürünü Bir Araç Varyantı ile Eşleştirin</span>
                </div>

                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                  <select
                    value={compatBrand?.id || ""}
                    onChange={(e) => {
                      const b = brands.find((x) => x.id === e.target.value);
                      setCompatBrand(b || null);
                      setCompatModel(null);
                      setCompatGen(null);
                      setCompatVariant(null);
                    }}
                    className="h-9 px-2 rounded-lg border text-xs bg-white"
                  >
                    <option value="">Marka Seçin</option>
                    {brands.map((b) => (
                      <option key={b.id} value={b.id}>
                        {b.name}
                      </option>
                    ))}
                  </select>

                  <select
                    disabled={!compatBrand}
                    value={compatModel?.id || ""}
                    onChange={(e) => {
                      const m = models.find((x) => x.id === e.target.value);
                      setCompatModel(m || null);
                      setCompatGen(null);
                      setCompatVariant(null);
                    }}
                    className="h-9 px-2 rounded-lg border text-xs bg-white disabled:opacity-50"
                  >
                    <option value="">Model Seçin</option>
                    {models.map((m) => (
                      <option key={m.id} value={m.id}>
                        {m.name}
                      </option>
                    ))}
                  </select>

                  <select
                    disabled={!compatModel}
                    value={compatGen?.id || ""}
                    onChange={(e) => {
                      const g = generations.find((x) => x.id === e.target.value);
                      setCompatGen(g || null);
                      setCompatVariant(null);
                    }}
                    className="h-9 px-2 rounded-lg border text-xs bg-white disabled:opacity-50"
                  >
                    <option value="">Kasa / Jenerasyon</option>
                    {generations.map((g) => (
                      <option key={g.id} value={g.id}>
                        {g.name}
                      </option>
                    ))}
                  </select>

                  <select
                    disabled={!compatGen}
                    value={compatVariant?.id || ""}
                    onChange={(e) => {
                      const v = variants.find((x) => x.id === e.target.value);
                      setCompatVariant(v || null);
                    }}
                    className="h-9 px-2 rounded-lg border text-xs bg-white disabled:opacity-50"
                  >
                    <option value="">Varyant Seçin</option>
                    {variants.map((v) => (
                      <option key={v.id} value={v.id}>
                        {v.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="grid grid-cols-3 gap-2">
                  <Input
                    placeholder="Başlangıç Yılı (Örn: 2015)"
                    value={compatStartYear}
                    onChange={(e) => setCompatStartYear(e.target.value)}
                  />
                  <Input
                    placeholder="Bitiş Yılı (Örn: 2024)"
                    value={compatEndYear}
                    onChange={(e) => setCompatEndYear(e.target.value)}
                  />
                  <Input
                    placeholder="Not (Örn: Sedan kasa)"
                    value={compatNotes}
                    onChange={(e) => setCompatNotes(e.target.value)}
                  />
                </div>

                <Button
                  variant="accent"
                  size="sm"
                  disabled={!compatVariant || addCompatibilityMutation.isPending}
                  onClick={() => addCompatibilityMutation.mutate()}
                  className="w-full"
                >
                  Uyumluluğu Bağla
                </Button>
              </div>

              {/* Compatibilities list */}
              <div className="space-y-2">
                {editingProduct.compatibilities?.map((c) => (
                  <div
                    key={c.id}
                    className="p-3 rounded-xl border bg-white flex items-center justify-between gap-3 text-xs"
                  >
                    <div className="flex items-center gap-2">
                      <Car className="w-4 h-4 text-emerald-600 shrink-0" />
                      <span className="font-bold text-slate-900">
                        {c.brandName} {c.modelName} {c.generationName} {c.variantName}
                      </span>
                      {(c.startYear || c.endYear) && (
                        <span className="text-slate-500 font-mono">
                          ({c.startYear || ""}-{c.endYear || "Günümüz"})
                        </span>
                      )}
                    </div>
                    <button
                      onClick={() => deleteCompatibilityMutation.mutate(c.id)}
                      className="p-1 text-slate-400 hover:text-destructive cursor-pointer"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
