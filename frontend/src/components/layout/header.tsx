"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/auth-store";
import { useCartStore } from "@/stores/cart-store";
import { useVehicleStore } from "@/stores/vehicle-store";
import {
  Car,
  Search,
  ShoppingCart,
  Heart,
  User as UserIcon,
  Menu,
  X,
  LogOut,
  Package,
  Settings,
  Sparkles,
} from "lucide-react";
import { Button } from "@/components/ui/button";

export function Header() {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState("");
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);

  const { user, isAuthenticated, logout, isAdmin } = useAuthStore();
  const { itemCount, setCartDrawerOpen } = useCartStore();
  const { selectedVehicle, setSelectorOpen } = useVehicleStore();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      router.push(`/katalog?search=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  return (
    <header className="sticky top-0 z-40 bg-white/95 backdrop-blur-md border-b shadow-sm">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-20 gap-4">
          {/* Logo */}
          <Link href="/" className="flex items-center gap-2.5 shrink-0 group">
            <div className="w-10 h-10 rounded-xl bg-slate-900 flex items-center justify-center text-accent-orange shadow-md group-hover:scale-105 transition-transform">
              <Car className="w-6 h-6" />
            </div>
            <div className="flex flex-col">
              <span className="font-extrabold text-xl tracking-tight text-slate-900 leading-none">
                OTO<span className="text-accent-orange">PASPAS</span>
              </span>
              <span className="text-[10px] tracking-wider text-muted-foreground uppercase font-semibold">
                3D Fitment Center
              </span>
            </div>
          </Link>

          {/* Search Bar */}
          <form
            onSubmit={handleSearch}
            className="hidden md:flex flex-1 max-w-lg items-center relative"
          >
            <input
              type="text"
              placeholder="Araç marka, model veya ürün ara... (Örn: Passat B8, Bagaj Havuzu)"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full h-11 pl-11 pr-24 rounded-full border border-slate-200 bg-slate-50/50 text-sm focus:outline-none focus:ring-2 focus:ring-accent-orange focus:border-transparent transition-all"
            />
            <Search className="w-4 h-4 text-muted-foreground absolute left-4 pointer-events-none" />
            <button
              type="submit"
              className="absolute right-1.5 px-4 py-1.5 rounded-full bg-slate-900 text-white text-xs font-semibold hover:bg-slate-800 transition-colors"
            >
              Ara
            </button>
          </form>

          {/* Actions: Vehicle Selector, Account, Favorites, Cart */}
          <div className="flex items-center gap-2 sm:gap-3">
            {/* Vehicle Selector Trigger Button */}
            <button
              onClick={() => setSelectorOpen(true)}
              className={`hidden lg:flex items-center gap-2 px-3.5 py-2 rounded-xl border text-xs font-medium transition-all ${
                selectedVehicle
                  ? "border-emerald-200 bg-emerald-50 text-emerald-800 hover:bg-emerald-100"
                  : "border-slate-200 hover:border-accent-orange hover:bg-orange-50/50 text-slate-700"
              }`}
            >
              <Car className="w-4 h-4 text-accent-orange" />
              <div className="text-left">
                <div className="text-[10px] text-muted-foreground leading-none">
                  {selectedVehicle ? "Seçili Araç" : "Uyumlu Ürün Bul"}
                </div>
                <div className="font-semibold truncate max-w-[130px]">
                  {selectedVehicle
                    ? `${selectedVehicle.brand.name} ${selectedVehicle.model.name}`
                    : "Aracını Seç"}
                </div>
              </div>
              <Sparkles className="w-3 h-3 text-accent-orange shrink-0" />
            </button>

            {/* Account Menu */}
            <div className="relative">
              {isAuthenticated && user ? (
                <div className="relative">
                  <button
                    onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                    className="flex items-center gap-1.5 p-2 rounded-xl hover:bg-slate-100 transition-colors text-sm font-medium text-slate-700"
                  >
                    <div className="w-8 h-8 rounded-full bg-slate-100 border flex items-center justify-center text-slate-700">
                      <UserIcon className="w-4 h-4" />
                    </div>
                    <span className="hidden xl:inline-block max-w-[100px] truncate">
                      {user.firstName}
                    </span>
                  </button>

                  {isUserMenuOpen && (
                    <div
                      className="absolute right-0 mt-2 w-56 rounded-2xl bg-white border shadow-xl py-2 z-50 animate-in fade-in-50 zoom-in-95"
                      onMouseLeave={() => setIsUserMenuOpen(false)}
                    >
                      <div className="px-4 py-2 border-b">
                        <p className="text-xs text-muted-foreground">Giriş yapıldı:</p>
                        <p className="text-sm font-semibold truncate">{user.email}</p>
                      </div>

                      {isAdmin() && (
                        <Link
                          href="/admin"
                          onClick={() => setIsUserMenuOpen(false)}
                          className="flex items-center gap-2.5 px-4 py-2.5 text-sm text-accent-orange font-medium hover:bg-orange-50"
                        >
                          <Settings className="w-4 h-4" />
                          Admin Yönetim Paneli
                        </Link>
                      )}

                      <Link
                        href="/hesabim/profil"
                        onClick={() => setIsUserMenuOpen(false)}
                        className="flex items-center gap-2.5 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
                      >
                        <UserIcon className="w-4 h-4 text-slate-500" />
                        Hesap Bilgilerim
                      </Link>

                      <Link
                        href="/hesabim/siparislerim"
                        onClick={() => setIsUserMenuOpen(false)}
                        className="flex items-center gap-2.5 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
                      >
                        <Package className="w-4 h-4 text-slate-500" />
                        Siparişlerim
                      </Link>

                      <Link
                        href="/hesabim/favorilerim"
                        onClick={() => setIsUserMenuOpen(false)}
                        className="flex items-center gap-2.5 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50"
                      >
                        <Heart className="w-4 h-4 text-slate-500" />
                        Favorilerim
                      </Link>

                      <div className="border-t my-1" />

                      <button
                        onClick={() => {
                          logout();
                          setIsUserMenuOpen(false);
                          router.push("/");
                        }}
                        className="w-full flex items-center gap-2.5 px-4 py-2 text-sm text-destructive hover:bg-red-50 text-left"
                      >
                        <LogOut className="w-4 h-4" />
                        Çıkış Yap
                      </button>
                    </div>
                  )}
                </div>
              ) : (
                <div className="flex items-center gap-1">
                  <Link
                    href="/giris-yap"
                    className="p-2 sm:px-3 sm:py-2 rounded-xl text-xs sm:text-sm font-semibold text-slate-700 hover:bg-slate-100 transition-colors"
                  >
                    Giriş Yap
                  </Link>
                </div>
              )}
            </div>

            {/* Favorites Icon */}
            <Link
              href="/hesabim/favorilerim"
              className="p-2 sm:p-2.5 rounded-xl text-slate-700 hover:bg-slate-100 hover:text-accent-orange transition-colors relative"
              title="Favorilerim"
            >
              <Heart className="w-5 h-5" />
            </Link>

            {/* Cart Drawer Trigger */}
            <button
              onClick={() => setCartDrawerOpen(true)}
              className="flex items-center gap-2 p-2 sm:px-3.5 sm:py-2 rounded-xl bg-slate-900 text-white hover:bg-slate-800 transition-transform active:scale-95 relative"
              title="Sepetim"
            >
              <ShoppingCart className="w-5 h-5 text-accent-orange" />
              <span className="hidden sm:inline-block text-xs font-semibold">Sepet</span>
              {itemCount > 0 && (
                <span className="absolute -top-1.5 -right-1.5 min-w-[20px] h-5 rounded-full bg-accent-orange text-white text-[11px] font-bold flex items-center justify-center px-1 shadow-sm">
                  {itemCount}
                </span>
              )}
            </button>

            {/* Mobile Menu Button */}
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="lg:hidden p-2 rounded-xl text-slate-700 hover:bg-slate-100"
            >
              {isMobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>

        {/* Navigation Categories Bar */}
        <nav className="hidden lg:flex items-center gap-8 py-2.5 border-t text-sm font-medium text-slate-700">
          <Link href="/katalog" className="hover:text-accent-orange transition-colors">
            Tüm Ürünler
          </Link>
          <Link href="/katalog?category=3d-oto-paspas" className="hover:text-accent-orange transition-colors">
            3D Havuzlu Paspas
          </Link>
          <Link href="/katalog?category=3d-bagaj-havuzu" className="hover:text-accent-orange transition-colors">
            3D Bagaj Havuzu
          </Link>
          <Link href="/siparis-takip" className="hover:text-accent-orange transition-colors ml-auto text-slate-500 text-xs">
            Kargom Nerede?
          </Link>
        </nav>
      </div>

      {/* Mobile Menu Dropdown */}
      {isMobileMenuOpen && (
        <div className="lg:hidden border-t bg-white px-4 py-4 space-y-3 shadow-lg animate-in slide-in-from-top-2">
          {/* Mobile Search */}
          <form onSubmit={handleSearch} className="relative">
            <input
              type="text"
              placeholder="Ürün veya araç ara..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full h-10 pl-10 pr-4 rounded-lg border border-slate-200 text-sm"
            />
            <Search className="w-4 h-4 text-muted-foreground absolute left-3 top-3" />
          </form>

          <button
            onClick={() => {
              setIsMobileMenuOpen(false);
              setSelectorOpen(true);
            }}
            className="w-full flex items-center justify-center gap-2 py-2.5 rounded-lg bg-accent-orange text-white font-semibold text-sm"
          >
            <Car className="w-4 h-4" />
            {selectedVehicle ? "Aracı Değiştir" : "Aracını Seç & Uyumlu Ürün Bul"}
          </button>

          <div className="space-y-1 pt-2 border-t text-sm">
            <Link
              href="/katalog"
              onClick={() => setIsMobileMenuOpen(false)}
              className="block py-2 text-slate-800 hover:text-accent-orange"
            >
              Tüm Ürünler
            </Link>
            <Link
              href="/katalog?category=3d-oto-paspas"
              onClick={() => setIsMobileMenuOpen(false)}
              className="block py-2 text-slate-800 hover:text-accent-orange"
            >
              3D Havuzlu Paspas
            </Link>
            <Link
              href="/katalog?category=3d-bagaj-havuzu"
              onClick={() => setIsMobileMenuOpen(false)}
              className="block py-2 text-slate-800 hover:text-accent-orange"
            >
              3D Bagaj Havuzu
            </Link>
            <Link
              href="/siparis-takip"
              onClick={() => setIsMobileMenuOpen(false)}
              className="block py-2 text-slate-600 hover:text-accent-orange"
            >
              Kargo ve Sipariş Takibi
            </Link>
          </div>
        </div>
      )}
    </header>
  );
}
