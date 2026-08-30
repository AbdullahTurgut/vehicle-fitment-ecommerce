export interface CartItem {
  id: string;
  productId: string;
  productName: string;
  productSlug: string;
  productSku: string;
  productImage?: string;
  unitPrice: number;
  quantity: number;
  totalPrice: number;
  inStock: boolean;
  maxStock: number;
  variantId?: string;
  variantInfo?: string;
}

export interface Cart {
  id: string;
  userId?: string;
  guestToken?: string;
  items: CartItem[];
  subtotal: number;
  discountTotal: number;
  shippingTotal: number;
  grandTotal: number;
  totalQuantity: number;
  freeShippingThreshold: number;
  freeShippingQualified: boolean;
}

export interface AddToCartRequest {
  productId: string;
  quantity: number;
  variantId?: string;
}

export interface UpdateCartItemQuantityRequest {
  quantity: number;
}

export interface MergeCartRequest {
  guestToken: string;
}
