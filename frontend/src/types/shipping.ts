export type ShippingCarrier = "YURTICI" | "ARAS" | "MNG" | "PTT" | "MOCK";
export type ShipmentStatus =
  | "CREATED"
  | "PICKED_UP"
  | "IN_TRANSIT"
  | "OUT_FOR_DELIVERY"
  | "DELIVERED"
  | "FAILED_DELIVERY"
  | "RETURNED";

export interface ShipmentTrackingEvent {
  id: string;
  status: ShipmentStatus;
  location?: string;
  description: string;
  eventDate: string;
}

export interface Shipment {
  id: string;
  orderNumber: string;
  carrier: ShippingCarrier;
  trackingNumber: string;
  status: ShipmentStatus;
  trackingUrl?: string;
  shippedAt?: string;
  deliveredAt?: string;
  events: ShipmentTrackingEvent[];
}

export interface CreateShipmentRequest {
  orderNumber: string;
  carrier: ShippingCarrier;
  trackingNumber?: string;
}

export interface UpdateShipmentStatusRequest {
  status: ShipmentStatus;
  location?: string;
  description?: string;
}
