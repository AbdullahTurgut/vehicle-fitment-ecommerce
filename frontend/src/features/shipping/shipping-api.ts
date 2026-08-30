import { apiClient } from "@/lib/api/client";
import { API_ENDPOINTS } from "@/lib/api/endpoints";
import { Shipment } from "@/types";

export const shippingApi = {
  getShipmentByOrderNumber: async (orderNumber: string): Promise<Shipment> => {
    return apiClient<Shipment>(API_ENDPOINTS.SHIPMENT_BY_ORDER(orderNumber));
  },

  getShipmentByTrackingNumber: async (trackingNumber: string): Promise<Shipment> => {
    return apiClient<Shipment>(API_ENDPOINTS.SHIPMENT_TRACK(trackingNumber), {
      skipAuth: true,
    });
  },
};
