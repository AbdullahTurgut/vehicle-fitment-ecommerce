"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { cartApi } from "@/features/cart/cart-api";
import { authApi } from "@/features/auth/auth-api";
import { orderApi } from "@/features/order/order-api";
import { paymentApi } from "@/features/payment/payment-api";
import { useAuthStore } from "@/stores/auth-store";
import { useCartStore } from "@/stores/cart-store";
import { formatPrice } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  ShieldCheck,
  CreditCard,
  MapPin,
  Truck,
  CheckCircle2,
  Lock,
  ArrowRight,
  Plus,
} from "lucide-react";
import { toast } from "sonner";
import Link from "next/link";

export default function CheckoutPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { isAuthenticated } = useAuthStore();
  const { setItemCount } = useCartStore();

  // Selected Address ID for logged in users
  const [selectedDeliveryAddressId, setSelectedDeliveryAddressId] = useState<string>("");
  const [sameAsBilling, setSameAsBilling] = useState<boolean>(true);

  // Form State for Guest / Custom Delivery Address
  const [recipientName, setRecipientName] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [city, setCity] = useState("İstanbul");
  const [district, setDistrict] = useState("");
  const [fullAddress, setFullAddress] = useState("");
  const [customerNote, setCustomerNote] = useState("");

  // Payment Form State
  const [cardHolderName, setCardHolderName] = useState("");
  const [cardNumber, setCardNumber] = useState("");
  const [expireMonth, setExpireMonth] = useState("");
  const [expireYear, setExpireYear] = useState("");
  const [cvc, setCvc] = useState("");

  // 1. Fetch Cart
  const { data: cart, isLoading: loadingCart } = useQuery({
    queryKey: ["cart"],
    queryFn: () => cartApi.getCart(),
  });

  // 2. Fetch User Addresses if logged in
  const { data: addresses = [], isLoading: loadingAddresses } = useQuery({
    queryKey: ["user-addresses"],
    queryFn: () => authApi.getAddresses(),
    enabled: isAuthenticated,
  });

  // Automatically set default delivery address when addresses load
  useState(() => {
    if (addresses.length > 0 && !selectedDeliveryAddressId) {
      const defaultAddr = addresses.find((a) => a.defaultDelivery) || addresses[0];
      setSelectedDeliveryAddressId(defaultAddr.id);
    }
  });

  // Processing Order & Payment
  const [isProcessing, setIsProcessing] = useState(false);

  const handleCheckoutSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!cart || cart.items.length === 0) {
      toast.error("Sepetiniz boş.");
      return;
    }

    // Validation
    if (!isAuthenticated || !selectedDeliveryAddressId) {
      if (!recipientName.trim() || !phoneNumber.trim() || !city.trim() || !district.trim() || !fullAddress.trim()) {
        toast.error("Lütfen teslimat adresi alanlarını eksiksiz doldurun.");
        return;
      }
    }

    if (!cardHolderName.trim() || !cardNumber.trim() || !expireMonth.trim() || !expireYear.trim() || !cvc.trim()) {
      toast.error("Lütfen kredi kartı bilgilerini eksiksiz girin.");
      return;
    }

    setIsProcessing(true);

    try {
      // 1. Create Order
      const createOrderPayload: any = {
        customerNote: customerNote.trim() || undefined,
      };

      if (isAuthenticated && selectedDeliveryAddressId) {
        createOrderPayload.deliveryAddressId = selectedDeliveryAddressId;
        createOrderPayload.billingAddressId = selectedDeliveryAddressId;
      } else {
        createOrderPayload.customDeliveryAddress = {
          recipientName: recipientName.trim(),
          phoneNumber: phoneNumber.trim(),
          city: city.trim(),
          district: district.trim(),
          fullAddress: fullAddress.trim(),
        };
        createOrderPayload.customBillingAddress = {
          recipientName: recipientName.trim(),
          phoneNumber: phoneNumber.trim(),
          city: city.trim(),
          district: district.trim(),
          fullAddress: fullAddress.trim(),
        };
      }

      const order = await orderApi.createOrder(createOrderPayload);

      // 2. Process Payment
      const paymentResponse = await paymentApi.processPayment({
        orderNumber: order.orderNumber,
        paymentMethod: "CREDIT_CARD",
        cardHolderName: cardHolderName.trim(),
        cardNumber: cardNumber.replace(/\s+/g, ""),
        expireMonth: expireMonth.padStart(2, "0"),
        expireYear: expireYear.length === 2 ? `20${expireYear}` : expireYear,
        cvc: cvc.trim(),
      });

      if (paymentResponse.status === "SUCCESS") {
        queryClient.invalidateQueries({ queryKey: ["cart"] });
        setItemCount(0);
        toast.success("Ödemeniz başarıyla alındı ve siparişiniz oluşturuldu! 🎉");
        router.push(`/siparis-tamamlandi/${order.orderNumber}`);
      } else {
        toast.error("Ödeme işlemi başarısız oldu. Lütfen bilgilerinizi kontrol edin.");
      }
    } catch (err: any) {
      toast.error(err.message || "Sipariş oluşturulamadı.");
    } finally {
      setIsProcessing(false);
    }
  };

  const items = cart?.items || [];
  const subtotal = cart?.subtotal || 0;
  const shippingTotal = cart?.shippingTotal || 0;
  const discountTotal = cart?.discountTotal || 0;
  const grandTotal = cart?.grandTotal || 0;

  if (loadingCart) {
    return (
      <div className="container mx-auto px-4 py-12">
        <div className="h-64 bg-muted animate-pulse rounded-3xl" />
      </div>
    );
  }

  if (!cart || items.length === 0) {
    return (
      <div className="container mx-auto px-4 py-16 text-center max-w-md space-y-4">
        <h2 className="text-2xl font-bold text-slate-900">Sepetinizde Ürün Bulunmuyor</h2>
        <p className="text-xs text-muted-foreground">
          Ödeme adımına geçmek için lütfen sepetinize ürün ekleyin.
        </p>
        <Button asChild variant="accent">
          <Link href="/katalog">Kataloğa Git</Link>
        </Button>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 max-w-6xl">
      {/* Checkout Progress Stepper */}
      <div className="flex items-center justify-center gap-3 sm:gap-6 mb-8 text-xs font-semibold text-slate-600">
        <div className="flex items-center gap-2 text-accent-orange">
          <span className="w-6 h-6 rounded-full bg-accent-orange text-white flex items-center justify-center text-xs">
            1
          </span>
          <span>Teslimat & İletişim</span>
        </div>
        <div className="w-8 sm:w-12 h-px bg-slate-300" />
        <div className="flex items-center gap-2 text-accent-orange">
          <span className="w-6 h-6 rounded-full bg-accent-orange text-white flex items-center justify-center text-xs">
            2
          </span>
          <span>Güvenli Ödeme</span>
        </div>
        <div className="w-8 sm:w-12 h-px bg-slate-300" />
        <div className="flex items-center gap-2 text-slate-400">
          <span className="w-6 h-6 rounded-full bg-slate-200 text-slate-600 flex items-center justify-center text-xs">
            3
          </span>
          <span>Onay</span>
        </div>
      </div>

      <form onSubmit={handleCheckoutSubmit}>
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          {/* LEFT 7 COLS: ADDRESS & PAYMENT FORM */}
          <div className="lg:col-span-7 space-y-6">
            {/* 1. ADRES BİLGİLERİ */}
            <div className="p-6 rounded-3xl border bg-white shadow-sm space-y-4">
              <div className="flex items-center justify-between border-b pb-3">
                <div className="flex items-center gap-2 font-bold text-base text-slate-900">
                  <MapPin className="w-5 h-5 text-accent-orange" />
                  <span>Teslimat Adresi</span>
                </div>
                {!isAuthenticated && (
                  <Link
                    href="/giris-yap"
                    className="text-xs font-semibold text-accent-orange hover:underline"
                  >
                    Kayıtlı adresiniz için Giriş Yapın
                  </Link>
                )}
              </div>

              {/* If Logged in and has saved addresses */}
              {isAuthenticated && addresses.length > 0 ? (
                <div className="space-y-3">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    {addresses.map((addr) => (
                      <div
                        key={addr.id}
                        onClick={() => setSelectedDeliveryAddressId(addr.id)}
                        className={`p-4 rounded-2xl border transition-all cursor-pointer text-xs space-y-1 ${
                          selectedDeliveryAddressId === addr.id
                            ? "border-accent-orange bg-orange-50/50 shadow-sm"
                            : "hover:border-slate-400"
                        }`}
                      >
                        <div className="flex items-center justify-between font-bold text-slate-900">
                          <span>{addr.title}</span>
                          {selectedDeliveryAddressId === addr.id && (
                            <CheckCircle2 className="w-4 h-4 text-accent-orange" />
                          )}
                        </div>
                        <p className="font-medium text-slate-800">{addr.recipientName}</p>
                        <p className="text-slate-600 line-clamp-2">{addr.fullAddress}</p>
                        <p className="text-slate-500">{addr.district} / {addr.city}</p>
                      </div>
                    ))}
                  </div>

                  <Link
                    href="/hesabim/adreslerim"
                    className="inline-flex items-center gap-1 text-xs text-accent-orange hover:underline font-semibold pt-1"
                  >
                    <Plus className="w-3.5 h-3.5" /> Yeni Adres Ekle
                  </Link>
                </div>
              ) : (
                /* Guest / Manual Address Form */
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5 text-xs">
                  <div>
                    <label className="font-semibold text-slate-700 block mb-1">
                      Ad Soyad *
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
                      Telefon Numarası *
                    </label>
                    <Input
                      required
                      type="tel"
                      placeholder="05XX XXX XX XX"
                      value={phoneNumber}
                      onChange={(e) => setPhoneNumber(e.target.value)}
                    />
                  </div>

                  <div>
                    <label className="font-semibold text-slate-700 block mb-1">
                      İl *
                    </label>
                    <Input
                      required
                      placeholder="İstanbul"
                      value={city}
                      onChange={(e) => setCity(e.target.value)}
                    />
                  </div>

                  <div>
                    <label className="font-semibold text-slate-700 block mb-1">
                      İlçe *
                    </label>
                    <Input
                      required
                      placeholder="Kadıköy"
                      value={district}
                      onChange={(e) => setDistrict(e.target.value)}
                    />
                  </div>

                  <div className="sm:col-span-2">
                    <label className="font-semibold text-slate-700 block mb-1">
                      Açık Adres (Cadde, Mahalle, Bina No, Daire) *
                    </label>
                    <textarea
                      required
                      rows={2}
                      placeholder="Örnek: Bağdat Cad. No: 12 Daire: 4"
                      value={fullAddress}
                      onChange={(e) => setFullAddress(e.target.value)}
                      className="w-full p-3 rounded-xl border border-input text-xs focus:outline-none focus:ring-2 focus:ring-accent-orange"
                    />
                  </div>

                  <div className="sm:col-span-2">
                    <label className="font-semibold text-slate-700 block mb-1">
                      Sipariş Notu (Opsiyonel)
                    </label>
                    <Input
                      placeholder="Kurye için zil, teslimat saati veya not..."
                      value={customerNote}
                      onChange={(e) => setCustomerNote(e.target.value)}
                    />
                  </div>
                </div>
              )}
            </div>

            {/* 2. ÖDEME BİLGİLERİ (KREDİ KARTI) */}
            <div className="p-6 rounded-3xl border bg-white shadow-sm space-y-4">
              <div className="flex items-center justify-between border-b pb-3">
                <div className="flex items-center gap-2 font-bold text-base text-slate-900">
                  <CreditCard className="w-5 h-5 text-accent-orange" />
                  <span>Kredi / Banka Kartı ile Ödeme</span>
                </div>
                <div className="flex items-center gap-1 text-[11px] text-slate-500 font-mono">
                  <Lock className="w-3 h-3 text-emerald-600" />
                  <span>3D Secure Korumalı</span>
                </div>
              </div>

              <div className="space-y-3.5 text-xs">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">
                    Kart Üzerindeki İsim *
                  </label>
                  <Input
                    required
                    placeholder="AHMET YILMAZ"
                    value={cardHolderName}
                    onChange={(e) => setCardHolderName(e.target.value.toUpperCase())}
                  />
                </div>

                <div>
                  <label className="font-semibold text-slate-700 block mb-1">
                    Kart Numarası *
                  </label>
                  <Input
                    required
                    maxLength={19}
                    placeholder="5555 5555 5555 5555"
                    value={cardNumber}
                    onChange={(e) => setCardNumber(e.target.value)}
                  />
                </div>

                <div className="grid grid-cols-3 gap-3">
                  <div>
                    <label className="font-semibold text-slate-700 block mb-1">
                      Ay (AA) *
                    </label>
                    <Input
                      required
                      maxLength={2}
                      placeholder="12"
                      value={expireMonth}
                      onChange={(e) => setExpireMonth(e.target.value)}
                    />
                  </div>

                  <div>
                    <label className="font-semibold text-slate-700 block mb-1">
                      Yıl (YY) *
                    </label>
                    <Input
                      required
                      maxLength={4}
                      placeholder="28"
                      value={expireYear}
                      onChange={(e) => setExpireYear(e.target.value)}
                    />
                  </div>

                  <div>
                    <label className="font-semibold text-slate-700 block mb-1">
                      CVC / CVV *
                    </label>
                    <Input
                      required
                      maxLength={4}
                      type="password"
                      placeholder="123"
                      value={cvc}
                      onChange={(e) => setCvc(e.target.value)}
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* RIGHT 5 COLS: ORDER SUMMARY & SUBMIT */}
          <div className="lg:col-span-5 space-y-6">
            <div className="p-6 rounded-3xl border bg-white shadow-sm space-y-4 sticky top-24">
              <h3 className="font-bold text-base text-slate-900 border-b pb-3">
                Sipariş Özeti ({cart.totalQuantity} Ürün)
              </h3>

              {/* Items preview */}
              <div className="max-h-56 overflow-y-auto divide-y text-xs pr-1">
                {items.map((item) => (
                  <div key={item.id} className="py-2.5 flex items-center justify-between gap-3">
                    <div className="min-w-0">
                      <p className="font-semibold text-slate-800 truncate">{item.productName}</p>
                      <p className="text-[11px] text-muted-foreground">{item.quantity} adet</p>
                    </div>
                    <span className="font-bold text-slate-900 shrink-0">
                      {formatPrice(item.totalPrice)}
                    </span>
                  </div>
                ))}
              </div>

              {/* Breakdown */}
              <div className="space-y-2 pt-3 border-t text-xs text-slate-600">
                <div className="flex justify-between">
                  <span>Ara Toplam</span>
                  <span className="font-medium text-slate-900">{formatPrice(subtotal)}</span>
                </div>

                {discountTotal > 0 && (
                  <div className="flex justify-between text-emerald-600 font-semibold">
                    <span>İndirim</span>
                    <span>-{formatPrice(discountTotal)}</span>
                  </div>
                )}

                <div className="flex justify-between">
                  <span>Kargo Bedeli</span>
                  <span>
                    {shippingTotal === 0 ? (
                      <strong className="text-emerald-600 font-bold">ÜCRETSİZ</strong>
                    ) : (
                      formatPrice(shippingTotal)
                    )}
                  </span>
                </div>

                <div className="pt-3 border-t flex justify-between items-baseline">
                  <span className="text-sm font-bold text-slate-900">Toplam Tutar</span>
                  <span className="text-2xl font-extrabold text-accent-orange">
                    {formatPrice(grandTotal)}
                  </span>
                </div>
              </div>

              <Button
                type="submit"
                variant="accent"
                size="lg"
                disabled={isProcessing}
                isLoading={isProcessing}
                className="w-full py-6 rounded-xl font-bold shadow-lg shadow-orange-950/20 text-base"
              >
                <span>Ödemeyi Tamamla ({formatPrice(grandTotal)})</span>
                <ArrowRight className="w-4 h-4 ml-2" />
              </Button>

              <div className="pt-2 text-[11px] text-slate-400 space-y-1 text-center">
                <div className="flex items-center justify-center gap-1">
                  <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" />
                  <span>Ödemeniz bankanızın güvenli 3D Secure onayına yönlendirilecektir.</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </form>
    </div>
  );
}
