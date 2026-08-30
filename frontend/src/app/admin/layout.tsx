"use client";

import { useEffect } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useAuthStore } from "@/stores/auth-store";
import {
  LayoutDashboard,
  Layers,
  ShoppingBag,
  Package,
  Truck,
  Tag,
  Star,
  ArrowLeft,
  ShieldAlert,
  Car,
} from "lucide-react";
import Link from "next/link";

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const pathname = usePathname();
  const { user, isAuthenticated, isAdmin } = useAuthStore();

  useEffect(() => {
    if (!isAuthenticated) {
      router.push("/giris-yap");
    } else if (!isAdmin()) {
      router.push("/");
    }
  }, [isAuthenticated, isAdmin, router]);

  if (!isAuthenticated || !isAdmin()) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-slate-900 text-white p-4 text-center space-y-4">
        <ShieldAlert className="w-16 h-16 text-rose-500" />
        <h1 className="text-2xl font-bold">Yetkisiz Erişim</h1>
        <p className="text-sm text-slate-400 max-w-sm">
          Bu alana yalnızca sistem yöneticisi yetkisine sahip kullanıcılar erişebilir.
        </p>
        <Link
          href="/"
          className="px-6 py-2.5 rounded-xl bg-accent-orange text-white text-xs font-semibold hover:bg-orange-600"
        >
          Mağazaya Dön
        </Link>
      </div>
    );
  }

  const navItems = [
    { label: "Dashboard", href: "/admin", icon: LayoutDashboard },
    { label: "Kategoriler", href: "/admin/kategoriler", icon: Layers },
    { label: "Ürünler & Uyumluluk", href: "/admin/urunler", icon: ShoppingBag },
    { label: "Sipariş Yönetimi", href: "/admin/siparisler", icon: Package },
    { label: "Kargo & Gönderiler", href: "/admin/kargo", icon: Truck },
    { label: "İndirim Kuponları", href: "/admin/kuponlar", icon: Tag },
    { label: "Yorum Moderasyonu", href: "/admin/yorumlar", icon: Star },
  ];

  return (
    <div className="min-h-screen flex bg-slate-100 text-foreground">
      {/* Sidebar Navigation */}
      <aside className="w-64 bg-slate-900 text-slate-300 flex flex-col shrink-0 border-r border-slate-800">
        {/* Brand */}
        <div className="p-6 border-b border-slate-800 flex items-center justify-between">
          <Link href="/admin" className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-accent-orange flex items-center justify-center text-white font-bold shadow-md">
              <Car className="w-5 h-5" />
            </div>
            <div>
              <span className="font-extrabold text-sm text-white block leading-none">
                OTO<span className="text-accent-orange">PASPAS</span>
              </span>
              <span className="text-[10px] text-slate-400 font-mono">YÖNETİM PANELİ</span>
            </div>
          </Link>
        </div>

        {/* Links */}
        <nav className="p-3 space-y-1 text-xs font-medium flex-1 overflow-y-auto">
          {navItems.map((item) => {
            const Icon = item.icon;
            const active =
              item.href === "/admin"
                ? pathname === "/admin"
                : pathname.startsWith(item.href);

            return (
              <Link
                key={item.href}
                href={item.href}
                className={`flex items-center gap-3 px-4 py-3 rounded-xl transition-all ${
                  active
                    ? "bg-accent-orange text-white font-semibold shadow-md shadow-orange-950/20"
                    : "text-slate-400 hover:text-white hover:bg-slate-800/80"
                }`}
              >
                <Icon className={`w-4 h-4 ${active ? "text-white" : "text-slate-400"}`} />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        {/* Bottom Store Link */}
        <div className="p-4 border-t border-slate-800 text-xs">
          <Link
            href="/"
            className="flex items-center gap-2 px-3 py-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Mağazayı Görüntüle</span>
          </Link>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Topbar */}
        <header className="h-16 bg-white border-b px-6 flex items-center justify-between sticky top-0 z-30 shadow-xs">
          <div className="flex items-center gap-2 text-xs font-semibold text-slate-500">
            <span>Admin</span>
            <span>/</span>
            <span className="text-slate-900 font-bold capitalize">
              {pathname.split("/")[2] || "Dashboard"}
            </span>
          </div>

          <div className="flex items-center gap-3">
            <span className="text-xs text-slate-600">
              Yönetici: <strong>{user?.firstName} {user?.lastName}</strong>
            </span>
            <div className="w-8 h-8 rounded-full bg-accent-orange text-white font-bold flex items-center justify-center text-xs">
              {user?.firstName?.charAt(0)}
            </div>
          </div>
        </header>

        {/* Content Body */}
        <main className="flex-1 p-6 sm:p-8 overflow-y-auto">{children}</main>
      </div>
    </div>
  );
}
