"use client";

import { useEffect } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useAuthStore } from "@/stores/auth-store";
import { Header } from "@/components/layout/header";
import { Footer } from "@/components/layout/footer";
import { AnnouncementBar } from "@/components/layout/announcement-bar";
import { VehicleBar } from "@/components/vehicle/vehicle-bar";
import { VehicleSelectorModal } from "@/components/vehicle/vehicle-selector-modal";
import { CartDrawer } from "@/components/cart/cart-drawer";
import {
  User as UserIcon,
  MapPin,
  Package,
  Heart,
  LogOut,
  Settings,
} from "lucide-react";
import Link from "next/link";

export default function AccountLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const pathname = usePathname();
  const { user, isAuthenticated, logout, isAdmin } = useAuthStore();

  useEffect(() => {
    // Basic client-side auth guard
    if (!isAuthenticated) {
      router.push("/giris-yap");
    }
  }, [isAuthenticated, router]);

  if (!isAuthenticated || !user) {
    return null;
  }

  const navItems = [
    { label: "Profil Bilgilerim", href: "/hesabim/profil", icon: UserIcon },
    { label: "Teslimat Adreslerim", href: "/hesabim/adreslerim", icon: MapPin },
    { label: "Siparişlerim", href: "/hesabim/siparislerim", icon: Package },
    { label: "Favori Ürünlerim", href: "/hesabim/favorilerim", icon: Heart },
  ];

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-foreground">
      <AnnouncementBar />
      <Header />
      <VehicleBar />

      <main className="flex-1 container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          {/* Account Sidebar Navigation (3 cols) */}
          <aside className="lg:col-span-3 space-y-4">
            {/* User Profile Card */}
            <div className="p-5 rounded-3xl border bg-white shadow-sm text-center space-y-2">
              <div className="w-16 h-16 rounded-full bg-slate-900 text-white flex items-center justify-center text-xl font-bold mx-auto">
                {user.firstName?.charAt(0)}
                {user.lastName?.charAt(0)}
              </div>
              <div>
                <h3 className="font-bold text-sm text-slate-900">
                  {user.firstName} {user.lastName}
                </h3>
                <p className="text-xs text-muted-foreground truncate">{user.email}</p>
              </div>
              {isAdmin() && (
                <div className="pt-2">
                  <Link
                    href="/admin"
                    className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-orange-100 text-accent-orange text-xs font-bold hover:bg-orange-200 transition-colors"
                  >
                    <Settings className="w-3.5 h-3.5" />
                    Admin Paneline Git
                  </Link>
                </div>
              )}
            </div>

            {/* Navigation Menu */}
            <nav className="rounded-3xl border bg-white p-2 shadow-sm space-y-1 text-xs font-semibold">
              {navItems.map((item) => {
                const Icon = item.icon;
                const active = pathname === item.href;
                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    className={`flex items-center gap-3 px-4 py-3 rounded-2xl transition-all ${
                      active
                        ? "bg-slate-900 text-white shadow-sm"
                        : "text-slate-700 hover:bg-slate-100"
                    }`}
                  >
                    <Icon className={`w-4 h-4 ${active ? "text-accent-orange" : "text-slate-400"}`} />
                    <span>{item.label}</span>
                  </Link>
                );
              })}

              <button
                onClick={() => {
                  logout();
                  router.push("/");
                }}
                className="w-full flex items-center gap-3 px-4 py-3 rounded-2xl text-destructive hover:bg-red-50 transition-colors text-left cursor-pointer"
              >
                <LogOut className="w-4 h-4" />
                <span>Güvenli Çıkış</span>
              </button>
            </nav>
          </aside>

          {/* Account Subpage Content (9 cols) */}
          <div className="lg:col-span-9">{children}</div>
        </div>
      </main>

      <Footer />
      <VehicleSelectorModal />
      <CartDrawer />
    </div>
  );
}
