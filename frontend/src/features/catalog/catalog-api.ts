import { apiClient } from "@/lib/api/client";
import { API_ENDPOINTS } from "@/lib/api/endpoints";
import { Category, ProductDetail, ProductList, PageResponse } from "@/types";

export interface GetProductsParams {
  category?: string;
  search?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export interface GetCompatibleProductsParams {
  variantId: string;
  year?: number;
}

export const catalogApi = {
  getCategories: async (): Promise<Category[]> => {
    return apiClient<Category[]>(API_ENDPOINTS.CATALOG_CATEGORIES, {
      skipAuth: true,
    });
  },

  getProducts: async (params: GetProductsParams = {}): Promise<PageResponse<ProductList>> => {
    return apiClient<PageResponse<ProductList>>(API_ENDPOINTS.CATALOG_PRODUCTS, {
      params: {
        category: params.category,
        page: params.page ?? 0,
        size: params.size ?? 12,
      },
      skipAuth: true,
    });
  },

  getProductBySlug: async (slug: string): Promise<ProductDetail> => {
    return apiClient<ProductDetail>(API_ENDPOINTS.CATALOG_PRODUCT_BY_SLUG(slug), {
      skipAuth: true,
    });
  },

  getCompatibleProducts: async (params: GetCompatibleProductsParams): Promise<ProductList[]> => {
    return apiClient<ProductList[]>(API_ENDPOINTS.CATALOG_COMPATIBLE_PRODUCTS, {
      params: {
        variantId: params.variantId,
        year: params.year,
      },
      skipAuth: true,
    });
  },
};
