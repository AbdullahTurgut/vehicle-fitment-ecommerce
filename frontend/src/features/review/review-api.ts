import { apiClient } from "@/lib/api/client";
import { API_ENDPOINTS } from "@/lib/api/endpoints";
import {
  CreateReviewRequest,
  FavoriteItem,
  FavoriteToggleResponse,
  PageResponse,
  ProductReview,
  ProductReviewSummary,
} from "@/types";

export const reviewApi = {
  getProductReviews: async (productId: string): Promise<ProductReviewSummary> => {
    return apiClient<ProductReviewSummary>(API_ENDPOINTS.PRODUCT_REVIEWS(productId), {
      skipAuth: true,
    });
  },

  createReview: async (productId: string, request: CreateReviewRequest): Promise<ProductReview> => {
    return apiClient<ProductReview>(API_ENDPOINTS.PRODUCT_REVIEWS(productId), {
      method: "POST",
      body: JSON.stringify(request),
    });
  },
};

export const favoriteApi = {
  getFavorites: async (page = 0, size = 12): Promise<PageResponse<FavoriteItem>> => {
    return apiClient<PageResponse<FavoriteItem>>(API_ENDPOINTS.FAVORITES, {
      params: { page, size },
    });
  },

  toggleFavorite: async (productId: string): Promise<FavoriteToggleResponse> => {
    return apiClient<FavoriteToggleResponse>(API_ENDPOINTS.FAVORITE_TOGGLE(productId), {
      method: "POST",
    });
  },

  removeFavorite: async (productId: string): Promise<void> => {
    return apiClient<void>(API_ENDPOINTS.FAVORITE_TOGGLE(productId), {
      method: "DELETE",
    });
  },
};
