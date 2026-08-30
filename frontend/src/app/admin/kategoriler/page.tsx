"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { adminApi } from "@/features/admin/admin-api";
import { Category } from "@/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Layers, Plus, Edit3, CheckCircle2, XCircle } from "lucide-react";
import { toast } from "sonner";

export default function AdminCategoriesPage() {
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);

  const [name, setName] = useState("");
  const [slug, setSlug] = useState("");
  const [description, setDescription] = useState("");
  const [active, setActive] = useState(true);

  const { data: categories = [], isLoading } = useQuery({
    queryKey: ["admin-categories"],
    queryFn: () => adminApi.getCategories(),
  });

  const openNewModal = () => {
    setEditingCategory(null);
    setName("");
    setSlug("");
    setDescription("");
    setActive(true);
    setIsModalOpen(true);
  };

  const openEditModal = (cat: Category) => {
    setEditingCategory(cat);
    setName(cat.name);
    setSlug(cat.slug);
    setDescription(cat.description || "");
    setActive(cat.active ?? true);
    setIsModalOpen(true);
  };

  const saveCategoryMutation = useMutation({
    mutationFn: () => {
      const payload = {
        name: name.trim(),
        slug: slug.trim() || undefined,
        description: description.trim() || undefined,
        active,
      };
      if (editingCategory) {
        return adminApi.updateCategory(editingCategory.id, payload);
      } else {
        return adminApi.createCategory(payload);
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-categories"] });
      queryClient.invalidateQueries({ queryKey: ["catalog-categories"] });
      toast.success(editingCategory ? "Kategori güncellendi." : "Yeni kategori oluşturuldu.");
      setIsModalOpen(false);
    },
    onError: (err: any) => {
      toast.error(err.message || "Kategori kaydedilemedi.");
    },
  });

  const toggleStatusMutation = useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) =>
      adminApi.updateCategoryStatus(id, active),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-categories"] });
      queryClient.invalidateQueries({ queryKey: ["catalog-categories"] });
      toast.success("Kategori aktiflik durumu güncellendi.");
    },
    onError: (err: any) => {
      toast.error(err.message || "Durum güncellenemedi.");
    },
  });

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      toast.error("Kategori adı zorunludur.");
      return;
    }
    saveCategoryMutation.mutate();
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
            Kategori Yönetimi
          </h1>
          <p className="text-xs text-muted-foreground mt-0.5">
            E-ticaret platformundaki paspas, bagaj havuzu ve aksesuar kategorilerini yönetin.
          </p>
        </div>
        <Button onClick={openNewModal} variant="accent" size="sm" className="gap-2">
          <Plus className="w-4 h-4" /> Yeni Kategori Ekle
        </Button>
      </div>

      <div className="rounded-3xl border bg-white shadow-sm overflow-hidden">
        {isLoading ? (
          <div className="p-8 space-y-3">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-12 bg-muted animate-pulse rounded-xl" />
            ))}
          </div>
        ) : categories.length === 0 ? (
          <div className="text-center py-12 text-xs text-muted-foreground">
            Kayıtlı kategori bulunamadı.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 font-semibold border-b">
                <tr>
                  <th className="p-4">Kategori Adı</th>
                  <th className="p-4">URL Slug</th>
                  <th className="p-4">Açıklama</th>
                  <th className="p-4">Durum</th>
                  <th className="p-4 text-right">İşlemler</th>
                </tr>
              </thead>
              <tbody className="divide-y text-slate-700">
                {categories.map((cat) => (
                  <tr key={cat.id} className="hover:bg-slate-50 transition-colors">
                    <td className="p-4 font-bold text-slate-900 flex items-center gap-2">
                      <Layers className="w-4 h-4 text-accent-orange" />
                      {cat.name}
                    </td>
                    <td className="p-4 font-mono text-slate-500">{cat.slug}</td>
                    <td className="p-4 text-slate-500 max-w-xs truncate">
                      {cat.description || "-"}
                    </td>
                    <td className="p-4">
                      <button
                        onClick={() =>
                          toggleStatusMutation.mutate({
                            id: cat.id,
                            active: !(cat.active ?? true),
                          })
                        }
                        className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full font-semibold cursor-pointer ${
                          cat.active ?? true
                            ? "bg-emerald-100 text-emerald-800"
                            : "bg-slate-100 text-slate-500"
                        }`}
                      >
                        {cat.active ?? true ? (
                          <>
                            <CheckCircle2 className="w-3 h-3 text-emerald-600" /> Aktif
                          </>
                        ) : (
                          <>
                            <XCircle className="w-3 h-3 text-slate-400" /> Pasif
                          </>
                        )}
                      </button>
                    </td>
                    <td className="p-4 text-right">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => openEditModal(cat)}
                        className="h-8 gap-1 text-xs"
                      >
                        <Edit3 className="w-3.5 h-3.5" /> Düzenle
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* MODAL */}
      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>
              {editingCategory ? "Kategoriyi Düzenle" : "Yeni Kategori Oluştur"}
            </DialogTitle>
          </DialogHeader>

          <form onSubmit={handleFormSubmit} className="space-y-4 text-xs py-2">
            <div>
              <label className="font-semibold text-slate-700 block mb-1">
                Kategori Adı *
              </label>
              <Input
                required
                placeholder="3D Bagaj Havuzu"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>

            <div>
              <label className="font-semibold text-slate-700 block mb-1">
                URL Slug (Boş bırakılırsa otomatik üretilir)
              </label>
              <Input
                placeholder="3d-bagaj-havuzu"
                value={slug}
                onChange={(e) => setSlug(e.target.value)}
              />
            </div>

            <div>
              <label className="font-semibold text-slate-700 block mb-1">Açıklama</label>
              <textarea
                rows={3}
                placeholder="Kategori hakkında kısa açıklama..."
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="w-full p-3 rounded-xl border border-input text-xs focus:outline-none focus:ring-2 focus:ring-accent-orange"
              />
            </div>

            <div className="flex justify-end gap-2 pt-4 border-t">
              <Button
                type="button"
                variant="outline"
                onClick={() => setIsModalOpen(false)}
              >
                İptal
              </Button>
              <Button
                type="submit"
                variant="accent"
                disabled={saveCategoryMutation.isPending}
                isLoading={saveCategoryMutation.isPending}
              >
                Kaydet
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
