"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { authApi } from "@/features/auth/auth-api";
import { Address } from "@/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { MapPin, Plus, Trash2, Edit3, CheckCircle2, Home } from "lucide-react";
import { toast } from "sonner";

export default function AddressesPage() {
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingAddress, setEditingAddress] = useState<Address | null>(null);

  // Form fields
  const [title, setTitle] = useState("");
  const [recipientName, setRecipientName] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [city, setCity] = useState("İstanbul");
  const [district, setDistrict] = useState("");
  const [fullAddress, setFullAddress] = useState("");
  const [defaultDelivery, setDefaultDelivery] = useState(false);

  const { data: addresses = [], isLoading } = useQuery({
    queryKey: ["user-addresses"],
    queryFn: () => authApi.getAddresses(),
  });

  const openNewModal = () => {
    setEditingAddress(null);
    setTitle("Ev Adresim");
    setRecipientName("");
    setPhoneNumber("");
    setCity("İstanbul");
    setDistrict("");
    setFullAddress("");
    setDefaultDelivery(false);
    setIsModalOpen(true);
  };

  const openEditModal = (addr: Address) => {
    setEditingAddress(addr);
    setTitle(addr.title);
    setRecipientName(addr.recipientName);
    setPhoneNumber(addr.phoneNumber);
    setCity(addr.city);
    setDistrict(addr.district);
    setFullAddress(addr.fullAddress);
    setDefaultDelivery(addr.defaultDelivery);
    setIsModalOpen(true);
  };

  const saveAddressMutation = useMutation({
    mutationFn: () => {
      const payload = {
        title: title.trim(),
        recipientName: recipientName.trim(),
        phoneNumber: phoneNumber.trim(),
        city: city.trim(),
        district: district.trim(),
        fullAddress: fullAddress.trim(),
        defaultDelivery,
        defaultBilling: defaultDelivery,
      };

      if (editingAddress) {
        return authApi.updateAddress(editingAddress.id, payload);
      } else {
        return authApi.createAddress(payload);
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user-addresses"] });
      toast.success(editingAddress ? "Adres güncellendi." : "Yeni adres eklendi.");
      setIsModalOpen(false);
    },
    onError: (err: any) => {
      toast.error(err.message || "Adres kaydedilemedi.");
    },
  });

  const deleteAddressMutation = useMutation({
    mutationFn: (id: string) => authApi.deleteAddress(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user-addresses"] });
      toast.success("Adres silindi.");
    },
    onError: (err: any) => {
      toast.error(err.message || "Adres silinemedi.");
    },
  });

  const setDefaultMutation = useMutation({
    mutationFn: (id: string) =>
      authApi.setDefaultAddress(id, { defaultDelivery: true, defaultBilling: true }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user-addresses"] });
      toast.success("Varsayılan adres güncellendi.");
    },
  });

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !recipientName.trim() || !phoneNumber.trim() || !city.trim() || !district.trim() || !fullAddress.trim()) {
      toast.error("Lütfen tüm alanları doldurun.");
      return;
    }
    saveAddressMutation.mutate();
  };

  return (
    <div className="space-y-6">
      <div className="p-6 rounded-3xl border bg-white shadow-sm space-y-6">
        <div className="flex items-center justify-between border-b pb-4">
          <div className="flex items-center gap-2 font-bold text-base text-slate-900">
            <MapPin className="w-5 h-5 text-accent-orange" />
            <span>Kayıtlı Teslimat Adreslerim ({addresses.length})</span>
          </div>
          <Button onClick={openNewModal} variant="accent" size="sm" className="gap-1.5">
            <Plus className="w-4 h-4" /> Yeni Adres Ekle
          </Button>
        </div>

        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="h-40 bg-muted animate-pulse rounded-2xl" />
            <div className="h-40 bg-muted animate-pulse rounded-2xl" />
          </div>
        ) : addresses.length === 0 ? (
          <div className="text-center py-12 text-muted-foreground space-y-3">
            <Home className="w-12 h-12 text-slate-300 mx-auto" />
            <p className="text-xs">Henüz kayıtlı bir teslimat adresiniz bulunmuyor.</p>
            <Button onClick={openNewModal} variant="outline" size="sm">
              İlk Adresinizi Ekleyin
            </Button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {addresses.map((addr) => (
              <div
                key={addr.id}
                className="p-5 rounded-2xl border bg-slate-50/50 hover:bg-slate-50 transition-colors flex flex-col justify-between space-y-3"
              >
                <div className="space-y-1.5">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-sm text-slate-900">{addr.title}</span>
                    {addr.defaultDelivery && (
                      <span className="text-[10px] px-2.5 py-0.5 rounded-full bg-emerald-100 text-emerald-800 font-semibold">
                        Varsayılan
                      </span>
                    )}
                  </div>
                  <p className="text-xs font-semibold text-slate-800">{addr.recipientName}</p>
                  <p className="text-xs text-slate-600 line-clamp-2 leading-relaxed">
                    {addr.fullAddress}
                  </p>
                  <p className="text-xs text-slate-500 font-medium">
                    {addr.district} / {addr.city}
                  </p>
                  <p className="text-xs text-slate-500 font-mono">Tel: {addr.phoneNumber}</p>
                </div>

                <div className="flex items-center justify-between pt-3 border-t text-xs">
                  {!addr.defaultDelivery ? (
                    <button
                      onClick={() => setDefaultMutation.mutate(addr.id)}
                      className="text-accent-orange hover:underline font-medium cursor-pointer"
                    >
                      Varsayılan Yap
                    </button>
                  ) : (
                    <span className="flex items-center gap-1 text-emerald-600 font-medium">
                      <CheckCircle2 className="w-3.5 h-3.5" /> Seçili
                    </span>
                  )}

                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => openEditModal(addr)}
                      className="p-1.5 text-slate-500 hover:text-slate-900 rounded-lg hover:bg-slate-200 transition-colors cursor-pointer"
                      title="Düzenle"
                    >
                      <Edit3 className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => deleteAddressMutation.mutate(addr.id)}
                      className="p-1.5 text-slate-400 hover:text-destructive rounded-lg hover:bg-red-50 transition-colors cursor-pointer"
                      title="Sil"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* ADD / EDIT ADDRESS DIALOG */}
      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent className="sm:max-w-lg">
          <DialogHeader>
            <DialogTitle>
              {editingAddress ? "Adresi Düzenle" : "Yeni Teslimat Adresi Ekle"}
            </DialogTitle>
          </DialogHeader>

          <form onSubmit={handleFormSubmit} className="space-y-3.5 text-xs py-2">
            <div>
              <label className="font-semibold text-slate-700 block mb-1">
                Adres Başlığı * (Örn: Ev, İşyeri)
              </label>
              <Input
                required
                placeholder="Ev Adresim"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">
                  Alıcı Adı Soyadı *
                </label>
                <Input
                  required
                  placeholder="Ahmet Yılmaz"
                  value={recipientName}
                  onChange={(e) => setRecipientName(e.target.value)}
                />
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">
                  Telefon *
                </label>
                <Input
                  required
                  placeholder="05XX XXX XX XX"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">İl *</label>
                <Input
                  required
                  placeholder="İstanbul"
                  value={city}
                  onChange={(e) => setCity(e.target.value)}
                />
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">İlçe *</label>
                <Input
                  required
                  placeholder="Kadıköy"
                  value={district}
                  onChange={(e) => setDistrict(e.target.value)}
                />
              </div>
            </div>

            <div>
              <label className="font-semibold text-slate-700 block mb-1">
                Açık Adres *
              </label>
              <textarea
                required
                rows={3}
                placeholder="Mahalle, Cadde, Sokak, Bina No, Daire..."
                value={fullAddress}
                onChange={(e) => setFullAddress(e.target.value)}
                className="w-full p-3 rounded-xl border border-input text-xs focus:outline-none focus:ring-2 focus:ring-accent-orange"
              />
            </div>

            <div className="flex items-center gap-2 pt-1">
              <input
                type="checkbox"
                id="defaultDelivery"
                checked={defaultDelivery}
                onChange={(e) => setDefaultDelivery(e.target.checked)}
                className="w-4 h-4 rounded text-accent-orange focus:ring-accent-orange"
              />
              <label htmlFor="defaultDelivery" className="text-slate-700 cursor-pointer">
                Varsayılan teslimat adresi olarak ayarla
              </label>
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
                disabled={saveAddressMutation.isPending}
                isLoading={saveAddressMutation.isPending}
              >
                Adresi Kaydet
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
