import Link from "next/link";
import { Car, ShieldCheck, Truck, RotateCcw, Headphones, Phone, Mail, MapPin } from "lucide-react";

export function Footer() {
  return (
    <footer className="bg-slate-900 text-slate-300 mt-auto border-t border-slate-800">
      {/* Features Bar */}
      <div className="border-b border-slate-800/80 bg-slate-950/40">
        <div className="container mx-auto px-4 py-8">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-2xl bg-slate-800 flex items-center justify-center text-accent-orange shrink-0">
                <Truck className="w-6 h-6" />
              </div>
              <div>
                <h4 className="font-semibold text-white text-sm">Hızlı & Ücretsiz Kargo</h4>
                <p className="text-xs text-slate-400">1.000 TL üzeri siparişlerde kargo bedava</p>
              </div>
            </div>

            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-2xl bg-slate-800 flex items-center justify-center text-emerald-400 shrink-0">
                <ShieldCheck className="w-6 h-6" />
              </div>
              <div>
                <h4 className="font-semibold text-white text-sm">%100 Uyum Garantisi</h4>
                <p className="text-xs text-slate-400">Aracınıza birebir 3D lazer tarama kalıbı</p>
              </div>
            </div>

            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-2xl bg-slate-800 flex items-center justify-center text-amber-400 shrink-0">
                <RotateCcw className="w-6 h-6" />
              </div>
              <div>
                <h4 className="font-semibold text-white text-sm">14 Gün Kolay İade</h4>
                <p className="text-xs text-slate-400">Memnun kalmazsanız anında iade hakkı</p>
              </div>
            </div>

            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-2xl bg-slate-800 flex items-center justify-center text-blue-400 shrink-0">
                <Headphones className="w-6 h-6" />
              </div>
              <div>
                <h4 className="font-semibold text-white text-sm">Uzman Destek Hattı</h4>
                <p className="text-xs text-slate-400">Haftanın 6 günü araca özel danışmanlık</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Main Footer Links */}
      <div className="container mx-auto px-4 py-12">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-8">
          {/* Company Bio */}
          <div className="lg:col-span-2 space-y-4">
            <Link href="/" className="flex items-center gap-2.5">
              <div className="w-10 h-10 rounded-xl bg-accent-orange flex items-center justify-center text-white shadow-md">
                <Car className="w-6 h-6" />
              </div>
              <div className="flex flex-col">
                <span className="font-extrabold text-xl tracking-tight text-white leading-none">
                  OTO<span className="text-accent-orange">PASPAS</span>
                </span>
                <span className="text-[10px] tracking-wider text-slate-400 uppercase font-semibold">
                  3D Fitment Center
                </span>
              </div>
            </Link>
            <p className="text-sm text-slate-400 leading-relaxed max-w-sm">
              Türkiye'nin lider araca özel 3D havuzlu oto paspas ve bagaj havuzu platformu. Kokusuz, yüksek kenarlı, dayanıklı TPE hammadde ile aracınızı ilk günkü gibi koruyun.
            </p>
            <div className="pt-2 text-xs text-slate-400 space-y-1">
              <div className="flex items-center gap-2">
                <Phone className="w-4 h-4 text-accent-orange" />
                <span>0850 000 00 00</span>
              </div>
              <div className="flex items-center gap-2">
                <Mail className="w-4 h-4 text-accent-orange" />
                <span>destek@otopaspas.com</span>
              </div>
              <div className="flex items-center gap-2">
                <MapPin className="w-4 h-4 text-accent-orange" />
                <span>İstanbul / Türkiye</span>
              </div>
            </div>
          </div>

          {/* Quick Categories */}
          <div className="space-y-3">
            <h5 className="font-semibold text-white text-sm uppercase tracking-wider">Kategoriler</h5>
            <ul className="space-y-2 text-sm text-slate-400">
              <li>
                <Link href="/katalog?category=3d-oto-paspas" className="hover:text-white transition-colors">
                  3D Oto Paspas
                </Link>
              </li>
              <li>
                <Link href="/katalog?category=bagaj-havuzu" className="hover:text-white transition-colors">
                  3D Bagaj Havuzu
                </Link>
              </li>
              <li>
                <Link href="/katalog" className="hover:text-white transition-colors">
                  Tüm Uyumlu Ürünler
                </Link>
              </li>
              <li>
                <Link href="/katalog" className="hover:text-white transition-colors">
                  Yeni Gelenler
                </Link>
              </li>
            </ul>
          </div>

          {/* Customer Service */}
          <div className="space-y-3">
            <h5 className="font-semibold text-white text-sm uppercase tracking-wider">Müşteri Hizmetleri</h5>
            <ul className="space-y-2 text-sm text-slate-400">
              <li>
                <Link href="/siparis-takip" className="hover:text-white transition-colors">
                  Kargo ve Sipariş Takibi
                </Link>
              </li>
              <li>
                <Link href="/hesabim/siparislerim" className="hover:text-white transition-colors">
                  Sipariş Geçmişim
                </Link>
              </li>
              <li>
                <Link href="/hesabim/adreslerim" className="hover:text-white transition-colors">
                  Teslimat Adreslerim
                </Link>
              </li>
              <li>
                <Link href="/hesabim/favorilerim" className="hover:text-white transition-colors">
                  Favori Ürünlerim
                </Link>
              </li>
            </ul>
          </div>

          {/* Corporate & Legal */}
          <div className="space-y-3">
            <h5 className="font-semibold text-white text-sm uppercase tracking-wider">Kurumsal</h5>
            <ul className="space-y-2 text-sm text-slate-400">
              <li>
                <span className="hover:text-white transition-colors cursor-pointer">Hakkımızda</span>
              </li>
              <li>
                <span className="hover:text-white transition-colors cursor-pointer">Mesafeli Satış Sözleşmesi</span>
              </li>
              <li>
                <span className="hover:text-white transition-colors cursor-pointer">Gizlilik ve KVKK Politikası</span>
              </li>
              <li>
                <span className="hover:text-white transition-colors cursor-pointer">İptal ve İade Koşulları</span>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom Bar */}
        <div className="mt-12 pt-6 border-t border-slate-800 text-xs text-slate-500 flex flex-col sm:flex-row items-center justify-between gap-4">
          <p>© {new Date().getFullYear()} OtoPaspas. Tüm Hakları Saklıdır.</p>
          <div className="flex items-center gap-4">
            <span className="text-slate-400 font-medium">Güvenli Ödeme Altyapısı</span>
            <span className="px-2 py-1 rounded bg-slate-800 text-[10px] text-slate-300 font-mono">256-Bit SSL</span>
            <span className="px-2 py-1 rounded bg-slate-800 text-[10px] text-slate-300 font-mono">3D Secure</span>
          </div>
        </div>
      </div>
    </footer>
  );
}
