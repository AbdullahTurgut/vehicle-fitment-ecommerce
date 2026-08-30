import { apiClient } from "@/lib/api/client";
import { API_ENDPOINTS } from "@/lib/api/endpoints";
import {
  CheckoutPreviewRequest,
  CheckoutSummaryResponse,
  CheckoutValidationResponse,
} from "@/types";

export const checkoutApi = {
  getPreview: async (request: CheckoutPreviewRequest = {}): Promise<CheckoutSummaryResponse> => {
    return apiClient<CheckoutSummaryResponse>(API_ENDPOINTS.CHECKOUT_PREVIEW, {
      method: "POST",
      body: JSON.stringify(request),
    });
  },

  validate: async (request: CheckoutPreviewRequest = {}): Promise<CheckoutValidationResponse> => {
    return apiClient<CheckoutValidationResponse>(API_ENDPOINTS.CHECKOUT_VALIDATE, {
      method: "POST",
      body: JSON.stringify(request),
    });
  },
};
