import { Address } from "./auth";

export interface CheckoutPreviewRequest {
  deliveryAddressId?: string;
  billingAddressId?: string;
  couponCode?: string;
}

export interface CheckoutSummaryResponse {
  items: Array<{
    productId: string;
    productName: string;
    productSku: string;
    unitPrice: number;
    quantity: number;
    totalPrice: number;
    inStock: boolean;
  }>;
  subtotal: number;
  discountTotal: number;
  shippingTotal: number;
  grandTotal: number;
  freeShippingQualified: boolean;
  deliveryAddress?: Address;
  billingAddress?: Address;
  couponCode?: string;
  validationErrors?: string[];
}

export interface CheckoutValidationResponse {
  valid: boolean;
  errors: string[];
}
