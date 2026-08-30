export type OrderStatus =
  | "PENDING_PAYMENT"
  | "PAID"
  | "PROCESSING"
  | "SHIPPED"
  | "DELIVERED"
  | "CANCELLED"
  | "REFUNDED";

export interface OrderItem {
  id: string;
  productId: string;
  productName: string;
  productSku: string;
  productImage?: string;
  unitPrice: number;
  quantity: number;
  totalPrice: number;
}

export interface OrderAddress {
  recipientName: string;
  phoneNumber: string;
  city: string;
  district: string;
  neighborhood?: string;
  fullAddress: string;
  postalCode?: string;
}

export interface OrderStatusHistory {
  id: string;
  status: OrderStatus;
  notes?: string;
  createdAt: string;
}

export interface Order {
  id: string;
  orderNumber: string;
  userId?: string;
  status: OrderStatus;
  subtotal: number;
  discountTotal: number;
  shippingTotal: number;
  grandTotal: number;
  deliveryAddress: OrderAddress;
  billingAddress: OrderAddress;
  items: OrderItem[];
  statusHistory: OrderStatusHistory[];
  createdAt: string;
}

export interface OrderSummary {
  id: string;
  orderNumber: string;
  status: OrderStatus;
  grandTotal: number;
  itemCount: number;
  createdAt: string;
}

export interface CreateOrderRequest {
  deliveryAddressId?: string;
  billingAddressId?: string;
  customDeliveryAddress?: OrderAddress;
  customBillingAddress?: OrderAddress;
  couponCode?: string;
  customerNote?: string;
}
