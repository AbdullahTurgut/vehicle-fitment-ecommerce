import { apiClient } from "@/lib/api/client";
import { API_ENDPOINTS } from "@/lib/api/endpoints";
import { CouponValidationResponse, ValidateCouponRequest } from "@/types";

export const couponApi = {
  validateCoupon: async (request: ValidateCouponRequest): Promise<CouponValidationResponse> => {
    return apiClient<CouponValidationResponse>(API_ENDPOINTS.COUPONS_VALIDATE, {
      method: "POST",
      body: JSON.stringify(request),
    });
  },
};
