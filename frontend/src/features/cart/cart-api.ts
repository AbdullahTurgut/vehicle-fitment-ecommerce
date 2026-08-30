import { apiClient } from "@/lib/api/client";
import { API_ENDPOINTS } from "@/lib/api/endpoints";
import { Cart, AddToCartRequest, UpdateCartItemQuantityRequest, MergeCartRequest } from "@/types";

export const cartApi = {
  getCart: async (): Promise<Cart> => {
    return apiClient<Cart>(API_ENDPOINTS.CART);
  },

  addToCart: async (request: AddToCartRequest): Promise<Cart> => {
    return apiClient<Cart>(API_ENDPOINTS.CART_ITEMS, {
      method: "POST",
      body: JSON.stringify(request),
    });
  },

  updateQuantity: async (itemId: string, request: UpdateCartItemQuantityRequest): Promise<Cart> => {
    return apiClient<Cart>(API_ENDPOINTS.CART_ITEM_BY_ID(itemId), {
      method: "PUT",
      body: JSON.stringify(request),
    });
  },

  removeItem: async (itemId: string): Promise<Cart> => {
    return apiClient<Cart>(API_ENDPOINTS.CART_ITEM_BY_ID(itemId), {
      method: "DELETE",
    });
  },

  clearCart: async (): Promise<void> => {
    return apiClient<void>(API_ENDPOINTS.CART, {
      method: "DELETE",
    });
  },

  mergeCart: async (request: MergeCartRequest): Promise<Cart> => {
    return apiClient<Cart>(API_ENDPOINTS.CART_MERGE, {
      method: "POST",
      body: JSON.stringify(request),
    });
  },
};
