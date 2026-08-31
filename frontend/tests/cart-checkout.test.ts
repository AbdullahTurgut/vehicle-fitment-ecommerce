import { describe, it, expect, beforeEach } from "vitest";
import { useCartStore } from "@/stores/cart-store";

describe("Cart & Checkout Business Logic", () => {
  beforeEach(() => {
    useCartStore.getState().setItemCount(0);
    useCartStore.getState().setCartDrawerOpen(false);
  });

  it("calculates 1,000 TL free shipping threshold accurately", () => {
    const calculateShipping = (subtotal: number) => {
      const FREE_SHIPPING_THRESHOLD = 1000;
      const STANDARD_SHIPPING_FEE = 49.9;
      if (subtotal >= FREE_SHIPPING_THRESHOLD) {
        return { fee: 0, isFree: true, remainingForFree: 0 };
      }
      return {
        fee: STANDARD_SHIPPING_FEE,
        isFree: false,
        remainingForFree: Number((FREE_SHIPPING_THRESHOLD - subtotal).toFixed(2)),
      };
    };

    expect(calculateShipping(500).fee).toBe(49.9);
    expect(calculateShipping(500).isFree).toBe(false);
    expect(calculateShipping(500).remainingForFree).toBe(500);

    expect(calculateShipping(1000).fee).toBe(0);
    expect(calculateShipping(1000).isFree).toBe(true);
    expect(calculateShipping(1000).remainingForFree).toBe(0);

    expect(calculateShipping(2499.9).fee).toBe(0);
    expect(calculateShipping(2499.9).isFree).toBe(true);
  });

  it("calculates percentage and fixed coupon discount accurately", () => {
    const applyCoupon = (
      subtotal: number,
      coupon: { discountType: "PERCENTAGE" | "FIXED_AMOUNT"; discountValue: number; maxDiscountAmount?: number }
    ) => {
      let discount = 0;
      if (coupon.discountType === "PERCENTAGE") {
        discount = (subtotal * coupon.discountValue) / 100;
        if (coupon.maxDiscountAmount && discount > coupon.maxDiscountAmount) {
          discount = coupon.maxDiscountAmount;
        }
      } else {
        discount = Math.min(coupon.discountValue, subtotal);
      }
      return {
        discountAmount: Number(discount.toFixed(2)),
        finalSubtotal: Number((subtotal - discount).toFixed(2)),
      };
    };

    // 10% coupon on 2000 TL = 200 TL
    const pctRes = applyCoupon(2000, { discountType: "PERCENTAGE", discountValue: 10 });
    expect(pctRes.discountAmount).toBe(200);
    expect(pctRes.finalSubtotal).toBe(1800);

    // 10% coupon with 150 TL max cap on 2000 TL = 150 TL
    const capRes = applyCoupon(2000, { discountType: "PERCENTAGE", discountValue: 10, maxDiscountAmount: 150 });
    expect(capRes.discountAmount).toBe(150);
    expect(capRes.finalSubtotal).toBe(1850);

    // 100 TL fixed discount on 500 TL = 400 TL
    const fixRes = applyCoupon(500, { discountType: "FIXED_AMOUNT", discountValue: 100 });
    expect(fixRes.discountAmount).toBe(100);
    expect(fixRes.finalSubtotal).toBe(400);
  });

  it("validates checkout form requirements", () => {
    interface CheckoutForm {
      fullName: string;
      phone: string;
      address: string;
      city: string;
      cardNumber: string;
      expiry: string;
      cvv: string;
    }

    const validateForm = (form: Partial<CheckoutForm>) => {
      const errors: Record<string, string> = {};
      if (!form.fullName?.trim()) errors.fullName = "Ad Soyad zorunludur.";
      if (!form.phone?.trim() || form.phone.length < 10) errors.phone = "Geçerli bir telefon numarası giriniz.";
      if (!form.address?.trim()) errors.address = "Teslimat adresi zorunludur.";
      if (!form.city?.trim()) errors.city = "İl seçimi zorunludur.";
      if (!form.cardNumber?.trim() || form.cardNumber.replace(/\s/g, "").length !== 16) {
        errors.cardNumber = "Geçerli 16 haneli kart numarası giriniz.";
      }
      if (!form.cvv?.trim() || form.cvv.length < 3) errors.cvv = "CVV zorunludur.";
      return {
        isValid: Object.keys(errors).length === 0,
        errors,
      };
    };

    expect(validateForm({}).isValid).toBe(false);
    expect(
      validateForm({
        fullName: "Ahmet Yılmaz",
        phone: "+905551234567",
        address: "Atatürk Cad. No: 1",
        city: "İstanbul",
        cardNumber: "1234 5678 1234 5678",
        expiry: "12/28",
        cvv: "123",
      }).isValid
    ).toBe(true);
  });
});
