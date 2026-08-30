import { apiClient } from "@/lib/api/client";
import { API_ENDPOINTS } from "@/lib/api/endpoints";
import { ProcessPaymentRequest, PaymentResponse } from "@/types";

export const paymentApi = {
  processPayment: async (request: ProcessPaymentRequest): Promise<PaymentResponse> => {
    return apiClient<PaymentResponse>(API_ENDPOINTS.PAYMENTS_PROCESS, {
      method: "POST",
      body: JSON.stringify(request),
    });
  },

  getPaymentByOrderNumber: async (orderNumber: string): Promise<PaymentResponse> => {
    return apiClient<PaymentResponse>(API_ENDPOINTS.PAYMENT_BY_ORDER(orderNumber));
  },
};
