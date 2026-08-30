import { apiClient } from "@/lib/api/client";
import { API_ENDPOINTS } from "@/lib/api/endpoints";
import {
  Category,
  ProductDetail,
  ProductStatus,
  ProductImage,
  ProductFeature,
  ProductCompatibility,
  Order,
  OrderStatus,
  Shipment,
  Coupon,
  ProductReview,
  PageResponse,
} from "@/types";

export interface CreateAdminCategoryRequest {
  name: string;
  slug?: string;
  description?: string;
  imageUrl?: string;
  parentId?: string;
  sortOrder?: number;
  active?: boolean;
}

export interface CreateAdminProductRequest {
  name: string;
  slug?: string;
  sku: string;
  categoryId: string;
  shortDescription?: string;
  description?: string;
  basePrice: number;
  salePrice?: number;
  stockQuantity: number;
  status: ProductStatus;
  manufacturerBrand?: string;
  material?: string;
}

export interface AddProductCompatibilityRequest {
  variantId: string;
  startYear?: number;
  endYear?: number;
  notes?: string;
}

export const adminApi = {
  // Categories
  getCategories: async (): Promise<Category[]> => {
    return apiClient<Category[]>(API_ENDPOINTS.ADMIN_CATEGORIES);
  },
  getCategoryById: async (id: string): Promise<Category> => {
    return apiClient<Category>(API_ENDPOINTS.ADMIN_CATEGORY_BY_ID(id));
  },
  createCategory: async (data: CreateAdminCategoryRequest): Promise<Category> => {
    return apiClient<Category>(API_ENDPOINTS.ADMIN_CATEGORIES, {
      method: "POST",
      body: JSON.stringify(data),
    });
  },
  updateCategory: async (id: string, data: CreateAdminCategoryRequest): Promise<Category> => {
    return apiClient<Category>(API_ENDPOINTS.ADMIN_CATEGORY_BY_ID(id), {
      method: "PUT",
      body: JSON.stringify(data),
    });
  },
  updateCategoryStatus: async (id: string, active: boolean): Promise<Category> => {
    return apiClient<Category>(API_ENDPOINTS.ADMIN_CATEGORY_STATUS(id), {
      method: "PATCH",
      body: JSON.stringify({ active }),
    });
  },

  // Products
  getProducts: async (params: { page?: number; size?: number; categoryId?: string; status?: ProductStatus; search?: string } = {}): Promise<PageResponse<ProductDetail>> => {
    return apiClient<PageResponse<ProductDetail>>(API_ENDPOINTS.ADMIN_PRODUCTS, {
      params: {
        page: params.page ?? 0,
        size: params.size ?? 10,
        categoryId: params.categoryId,
        status: params.status,
        search: params.search,
      },
    });
  },
  getProductById: async (id: string): Promise<ProductDetail> => {
    return apiClient<ProductDetail>(API_ENDPOINTS.ADMIN_PRODUCT_BY_ID(id));
  },
  createProduct: async (data: CreateAdminProductRequest): Promise<ProductDetail> => {
    return apiClient<ProductDetail>(API_ENDPOINTS.ADMIN_PRODUCTS, {
      method: "POST",
      body: JSON.stringify(data),
    });
  },
  updateProduct: async (id: string, data: CreateAdminProductRequest): Promise<ProductDetail> => {
    return apiClient<ProductDetail>(API_ENDPOINTS.ADMIN_PRODUCT_BY_ID(id), {
      method: "PUT",
      body: JSON.stringify(data),
    });
  },
  updateProductStatus: async (id: string, status: ProductStatus): Promise<ProductDetail> => {
    return apiClient<ProductDetail>(API_ENDPOINTS.ADMIN_PRODUCT_STATUS(id), {
      method: "PATCH",
      body: JSON.stringify({ status }),
    });
  },
  addImage: async (productId: string, data: { imageUrl: string; altText?: string; sortOrder?: number; primary?: boolean }): Promise<ProductImage> => {
    return apiClient<ProductImage>(API_ENDPOINTS.ADMIN_PRODUCT_IMAGES(productId), {
      method: "POST",
      body: JSON.stringify(data),
    });
  },
  deleteImage: async (productId: string, imageId: string): Promise<void> => {
    return apiClient<void>(API_ENDPOINTS.ADMIN_PRODUCT_IMAGE_DELETE(productId, imageId), {
      method: "DELETE",
    });
  },
  addFeature: async (productId: string, data: { title: string; description: string; iconName?: string; sortOrder?: number }): Promise<ProductFeature> => {
    return apiClient<ProductFeature>(API_ENDPOINTS.ADMIN_PRODUCT_FEATURES(productId), {
      method: "POST",
      body: JSON.stringify(data),
    });
  },
  deleteFeature: async (productId: string, featureId: string): Promise<void> => {
    return apiClient<void>(API_ENDPOINTS.ADMIN_PRODUCT_FEATURE_DELETE(productId, featureId), {
      method: "DELETE",
    });
  },
  addCompatibility: async (productId: string, data: AddProductCompatibilityRequest): Promise<ProductCompatibility> => {
    return apiClient<ProductCompatibility>(API_ENDPOINTS.ADMIN_PRODUCT_COMPATIBILITIES(productId), {
      method: "POST",
      body: JSON.stringify(data),
    });
  },
  deleteCompatibility: async (productId: string, compatibilityId: string): Promise<void> => {
    return apiClient<void>(API_ENDPOINTS.ADMIN_PRODUCT_COMPATIBILITY_DELETE(productId, compatibilityId), {
      method: "DELETE",
    });
  },

  // Orders
  getOrders: async (params: { page?: number; size?: number; status?: OrderStatus } = {}): Promise<PageResponse<Order>> => {
    return apiClient<PageResponse<Order>>(API_ENDPOINTS.ADMIN_ORDERS, {
      params: {
        page: params.page ?? 0,
        size: params.size ?? 10,
        status: params.status,
      },
    });
  },
  getOrderById: async (id: string): Promise<Order> => {
    return apiClient<Order>(API_ENDPOINTS.ADMIN_ORDER_BY_ID(id));
  },
  updateOrderStatus: async (id: string, status: OrderStatus, note?: string): Promise<Order> => {
    return apiClient<Order>(API_ENDPOINTS.ADMIN_ORDER_STATUS(id), {
      method: "PATCH",
      body: JSON.stringify({ status, note }),
    });
  },

  // Shipments
  createShipment: async (data: { orderNumber: string; carrier: string; trackingNumber?: string }): Promise<Shipment> => {
    return apiClient<Shipment>(API_ENDPOINTS.ADMIN_SHIPMENTS, {
      method: "POST",
      body: JSON.stringify(data),
    });
  },
  updateShipmentStatus: async (shipmentId: string, data: { status: string; location?: string; description?: string }): Promise<Shipment> => {
    return apiClient<Shipment>(API_ENDPOINTS.ADMIN_SHIPMENT_STATUS(shipmentId), {
      method: "PATCH",
      body: JSON.stringify(data),
    });
  },

  // Coupons
  getCoupons: async (): Promise<Coupon[]> => {
    return apiClient<Coupon[]>(API_ENDPOINTS.ADMIN_COUPONS);
  },
  createCoupon: async (data: Partial<Coupon>): Promise<Coupon> => {
    return apiClient<Coupon>(API_ENDPOINTS.ADMIN_COUPONS, {
      method: "POST",
      body: JSON.stringify(data),
    });
  },
  updateCouponStatus: async (id: string, active: boolean): Promise<Coupon> => {
    return apiClient<Coupon>(API_ENDPOINTS.ADMIN_COUPON_STATUS(id), {
      method: "PATCH",
      body: JSON.stringify({ active }),
    });
  },

  // Reviews
  getReviews: async (): Promise<ProductReview[]> => {
    return apiClient<ProductReview[]>(API_ENDPOINTS.ADMIN_REVIEWS);
  },
  updateReviewStatus: async (id: string, status: "APPROVED" | "REJECTED"): Promise<ProductReview> => {
    return apiClient<ProductReview>(API_ENDPOINTS.ADMIN_REVIEW_STATUS(id), {
      method: "PATCH",
      body: JSON.stringify({ status }),
    });
  },
};
