import { apiClient } from "@/lib/api/client";
import { API_ENDPOINTS } from "@/lib/api/endpoints";
import { CreateOrderRequest, Order, OrderSummary, PageResponse } from "@/types";

export const orderApi = {
  createOrder: async (request: CreateOrderRequest): Promise<Order> => {
    return apiClient<Order>(API_ENDPOINTS.ORDERS, {
      method: "POST",
      body: JSON.stringify(request),
    });
  },

  getUserOrders: async (page = 0, size = 10): Promise<PageResponse<OrderSummary>> => {
    return apiClient<PageResponse<OrderSummary>>(API_ENDPOINTS.ORDERS, {
      params: { page, size },
    });
  },

  getOrderByNumber: async (orderNumber: string): Promise<Order> => {
    return apiClient<Order>(API_ENDPOINTS.ORDER_BY_NUMBER(orderNumber));
  },

  cancelOrder: async (orderNumber: string, reason?: string): Promise<Order> => {
    return apiClient<Order>(API_ENDPOINTS.ORDER_CANCEL(orderNumber), {
      method: "POST",
      body: JSON.stringify({ reason }),
    });
  },
};
