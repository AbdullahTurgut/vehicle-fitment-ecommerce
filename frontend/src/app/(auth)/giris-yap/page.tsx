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
import { Lock, Mail, ArrowRight } from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";

export default function LoginPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { setAuth } = useAuthStore();
  const { guestToken, setGuestToken } = useCartStore();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const loginMutation = useMutation({
    mutationFn: () => authApi.login({ email: email.trim(), password }),
    onSuccess: async (authResponse) => {
      setAuth(authResponse);
      toast.success(`Hoş geldiniz, ${authResponse.user.firstName}! 👋`);

      // If guest cart exists, merge it into user cart
      if (guestToken) {
        try {
          await cartApi.mergeCart({ guestToken });
          setGuestToken(null);
        } catch {
          // ignore merge failures
        }
      }

      queryClient.invalidateQueries({ queryKey: ["cart"] });

      if (authResponse.user.roles.includes("ROLE_ADMIN")) {
        router.push("/admin");
      } else {
        router.push("/hesabim/profil");
      }
    },
    onError: (err: any) => {
      toast.error(err.message || "Giriş başarısız. Lütfen e-posta ve şifrenizi kontrol edin.");
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim() || !password) {
      toast.error("Lütfen e-posta ve şifrenizi girin.");
      return;
    }
    loginMutation.mutate();
  };

  return (
    <div className="space-y-6">
      <div className="text-center space-y-1">
        <h2 className="text-2xl font-extrabold text-slate-900 tracking-tight">
          Hesabınıza Giriş Yapın
        </h2>
        <p className="text-xs text-muted-foreground">
          Siparişlerinizi takip etmek ve kayıtlı adreslerinizi yönetmek için oturum açın.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4 text-xs">
        <div>
          <label className="font-semibold text-slate-700 block mb-1">
            E-Posta Adresi
          </label>
          <div className="relative">
            <Input
              type="email"
              required
              placeholder="ornek@domain.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="pl-10"
            />
            <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-3 pointer-events-none" />
          </div>
        </div>

        <div>
          <div className="flex items-center justify-between mb-1">
            <label className="font-semibold text-slate-700">Şifre</label>
          </div>
          <div className="relative">
            <Input
              type="password"
              required
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="pl-10"
            />
            <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-3 pointer-events-none" />
          </div>
        </div>

        <Button
          type="submit"
          variant="accent"
          size="lg"
          disabled={loginMutation.isPending}
          isLoading={loginMutation.isPending}
          className="w-full py-5 rounded-xl font-bold shadow-md shadow-orange-950/20 text-sm"
        >
          <span>Giriş Yap</span>
          <ArrowRight className="w-4 h-4 ml-2" />
        </Button>
      </form>

      <div className="text-center text-xs text-slate-600 border-t pt-4">
        Henüz hesabınız yok mu?{" "}
        <Link
          href="/kayit-ol"
          className="font-bold text-accent-orange hover:underline"
        >
          Hemen Kayıt Olun
        </Link>
      </div>
    </div>
  );
}
