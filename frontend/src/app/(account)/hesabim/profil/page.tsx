"use client";

import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { authApi } from "@/features/auth/auth-api";
import { useAuthStore } from "@/stores/auth-store";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { User, Lock, Save, KeyRound } from "lucide-react";
import { toast } from "sonner";

export default function ProfilePage() {
  const queryClient = useQueryClient();
  const { user, setUser } = useAuthStore();

  const [firstName, setFirstName] = useState(user?.firstName || "");
  const [lastName, setLastName] = useState(user?.lastName || "");
  const [phoneNumber, setPhoneNumber] = useState(user?.phoneNumber || "");

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const updateProfileMutation = useMutation({
    mutationFn: () =>
      authApi.updateProfile({
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        phoneNumber: phoneNumber.trim() || undefined,
      }),
    onSuccess: (updatedUser) => {
      setUser(updatedUser);
      toast.success("Profil bilgileriniz başarıyla güncellendi.");
    },
    onError: (err: any) => {
      toast.error(err.message || "Profil güncellenemedi.");
    },
  });

  const changePasswordMutation = useMutation({
    mutationFn: () =>
      authApi.changePassword({
        currentPassword,
        newPassword,
      }),
    onSuccess: () => {
      toast.success("Şifreniz başarıyla değiştirildi.");
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    },
    onError: (err: any) => {
      toast.error(err.message || "Şifre değiştirilemedi.");
    },
  });

  const handleProfileSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!firstName.trim() || !lastName.trim()) {
      toast.error("Ad ve soyad zorunludur.");
      return;
    }
    updateProfileMutation.mutate();
  };

  const handlePasswordSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentPassword || !newPassword) {
      toast.error("Lütfen mevcut ve yeni şifrenizi girin.");
      return;
    }
    if (newPassword.length < 6) {
      toast.error("Yeni şifre en az 6 karakter olmalıdır.");
      return;
    }
    if (newPassword !== confirmPassword) {
      toast.error("Yeni şifreler birbiriyle eşleşmiyor.");
      return;
    }
    changePasswordMutation.mutate();
  };

  return (
    <div className="space-y-8">
      {/* Profil Bilgileri */}
      <div className="p-6 rounded-3xl border bg-white shadow-sm space-y-6">
        <div className="flex items-center gap-2 font-bold text-base text-slate-900 border-b pb-3">
          <User className="w-5 h-5 text-accent-orange" />
          <span>Kişisel Bilgiler</span>
        </div>

        <form onSubmit={handleProfileSubmit} className="space-y-4 text-xs">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="font-semibold text-slate-700 block mb-1">Ad *</label>
              <Input
                required
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
              />
            </div>
            <div>
              <label className="font-semibold text-slate-700 block mb-1">Soyad *</label>
              <Input
                required
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="font-semibold text-slate-700 block mb-1">
                E-Posta (Değiştirilemez)
              </label>
              <Input disabled value={user?.email || ""} className="bg-slate-50" />
            </div>
            <div>
              <label className="font-semibold text-slate-700 block mb-1">
                Telefon Numarası
              </label>
              <Input
                placeholder="05XX XXX XX XX"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
              />
            </div>
          </div>

          <div className="flex justify-end pt-2">
            <Button
              type="submit"
              variant="accent"
              disabled={updateProfileMutation.isPending}
              isLoading={updateProfileMutation.isPending}
              className="gap-2 px-6"
            >
              <Save className="w-4 h-4" />
              Bilgileri Kaydet
            </Button>
          </div>
        </form>
      </div>

      {/* Şifre Değiştirme */}
      <div className="p-6 rounded-3xl border bg-white shadow-sm space-y-6">
        <div className="flex items-center gap-2 font-bold text-base text-slate-900 border-b pb-3">
          <KeyRound className="w-5 h-5 text-accent-orange" />
          <span>Şifre Değiştir</span>
        </div>

        <form onSubmit={handlePasswordSubmit} className="space-y-4 text-xs max-w-lg">
          <div>
            <label className="font-semibold text-slate-700 block mb-1">
              Mevcut Şifre *
            </label>
            <Input
              type="password"
              required
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
            />
          </div>

          <div>
            <label className="font-semibold text-slate-700 block mb-1">
              Yeni Şifre * (En az 6 karakter)
            </label>
            <Input
              type="password"
              required
              minLength={6}
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
            />
          </div>

          <div>
            <label className="font-semibold text-slate-700 block mb-1">
              Yeni Şifre Tekrarı *
            </label>
            <Input
              type="password"
              required
              minLength={6}
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
            />
          </div>

          <div className="flex justify-end pt-2">
            <Button
              type="submit"
              variant="outline"
              disabled={changePasswordMutation.isPending}
              isLoading={changePasswordMutation.isPending}
              className="gap-2 px-6"
            >
              <Lock className="w-4 h-4" />
              Şifreyi Güncelle
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
