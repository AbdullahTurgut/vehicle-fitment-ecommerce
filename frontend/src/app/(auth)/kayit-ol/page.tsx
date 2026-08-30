"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { authApi } from "@/features/auth/auth-api";
import { cartApi } from "@/features/cart/cart-api";
import { useAuthStore } from "@/stores/auth-store";
import { useCartStore } from "@/stores/cart-store";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Lock, Mail, User, Phone, ArrowRight } from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";

export default function RegisterPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { setAuth } = useAuthStore();
  const { guestToken, setGuestToken } = useCartStore();

  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [password, setPassword] = useState("");

  const registerMutation = useMutation({
    mutationFn: () =>
      authApi.register({
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        email: email.trim(),
        phoneNumber: phoneNumber.trim() || undefined,
        password,
      }),
    onSuccess: async (authResponse) => {
      setAuth(authResponse);
      toast.success("Hesabınız başarıyla oluşturuldu! Hoş geldiniz. 🎉");

      if (guestToken) {
        try {
          await cartApi.mergeCart({ guestToken });
          setGuestToken(null);
        } catch {
          // ignore merge errors
        }
      }

      queryClient.invalidateQueries({ queryKey: ["cart"] });
      router.push("/hesabim/profil");
    },
    onError: (err: any) => {
      toast.error(err.message || "Kayıt başarısız. Lütfen bilgilerinizi kontrol edin.");
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!firstName.trim() || !lastName.trim() || !email.trim() || !password) {
      toast.error("Lütfen zorunlu alanları doldurun.");
      return;
    }
    if (password.length < 6) {
      toast.error("Şifre en az 6 karakter olmalıdır.");
      return;
    }
    registerMutation.mutate();
  };

  return (
    <div className="space-y-6">
      <div className="text-center space-y-1">
        <h2 className="text-2xl font-extrabold text-slate-900 tracking-tight">
          Yeni Hesap Oluşturun
        </h2>
        <p className="text-xs text-muted-foreground">
          Kolayca üye olarak indirimlerden ve hızlı sipariş takibinden faydalanın.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-3.5 text-xs">
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="font-semibold text-slate-700 block mb-1">Ad *</label>
            <div className="relative">
              <Input
                required
                placeholder="Ahmet"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                className="pl-9"
              />
              <User className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-3 pointer-events-none" />
            </div>
          </div>
          <div>
            <label className="font-semibold text-slate-700 block mb-1">Soyad *</label>
            <Input
              required
              placeholder="Yılmaz"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
            />
          </div>
        </div>

        <div>
          <label className="font-semibold text-slate-700 block mb-1">
            E-Posta Adresi *
          </label>
          <div className="relative">
            <Input
              type="email"
              required
              placeholder="ahmet@domain.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="pl-9"
            />
            <Mail className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-3 pointer-events-none" />
          </div>
        </div>

        <div>
          <label className="font-semibold text-slate-700 block mb-1">
            Telefon Numarası (Opsiyonel)
          </label>
          <div className="relative">
            <Input
              type="tel"
              placeholder="05XX XXX XX XX"
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
              className="pl-9"
            />
            <Phone className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-3 pointer-events-none" />
          </div>
        </div>

        <div>
          <label className="font-semibold text-slate-700 block mb-1">
            Şifre * (En az 6 karakter)
          </label>
          <div className="relative">
            <Input
              type="password"
              required
              minLength={6}
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="pl-9"
            />
            <Lock className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-3 pointer-events-none" />
          </div>
        </div>

        <Button
          type="submit"
          variant="accent"
          size="lg"
          disabled={registerMutation.isPending}
          isLoading={registerMutation.isPending}
          className="w-full py-5 rounded-xl font-bold shadow-md shadow-orange-950/20 text-sm mt-2"
        >
          <span>Kayıt Ol</span>
          <ArrowRight className="w-4 h-4 ml-2" />
        </Button>
      </form>

      <div className="text-center text-xs text-slate-600 border-t pt-4">
        Zaten bir hesabınız var mı?{" "}
        <Link
          href="/giris-yap"
          className="font-bold text-accent-orange hover:underline"
        >
          Giriş Yapın
        </Link>
      </div>
    </div>
  );
}
