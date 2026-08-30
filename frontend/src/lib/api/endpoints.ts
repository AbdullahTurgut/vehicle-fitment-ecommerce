export const API_ENDPOINTS = {
  // Auth
  AUTH_LOGIN: "/api/v1/auth/login",
  AUTH_REGISTER: "/api/v1/auth/register",
  AUTH_REFRESH: "/api/v1/auth/refresh",
  AUTH_ME: "/api/v1/auth/me",

  // Users & Address
  USER_PROFILE: "/api/v1/users/profile",
  USER_PASSWORD: "/api/v1/users/password",
  USER_ADDRESSES: "/api/v1/users/addresses",
  USER_ADDRESS_BY_ID: (id: string) => `/api/v1/users/addresses/${id}`,
  USER_ADDRESS_DEFAULT: (id: string) => `/api/v1/users/addresses/${id}/default`,

  // Vehicles
  VEHICLE_BRANDS: "/api/v1/vehicles/brands",
  VEHICLE_MODELS: (brandId: string) => `/api/v1/vehicles/brands/${brandId}/models`,
  VEHICLE_GENERATIONS: (modelId: string) => `/api/v1/vehicles/models/${modelId}/generations`,
  VEHICLE_VARIANTS: (generationId: string) => `/api/v1/vehicles/generations/${generationId}/variants`,

  // Catalog
  CATALOG_CATEGORIES: "/api/v1/catalog/categories",
  CATALOG_PRODUCTS: "/api/v1/catalog/products",
  CATALOG_PRODUCT_BY_SLUG: (slug: string) => `/api/v1/catalog/products/${slug}`,
  CATALOG_COMPATIBLE_PRODUCTS: "/api/v1/catalog/compatible-products",

  // Cart
  CART: "/api/v1/cart",
  CART_ITEMS: "/api/v1/cart/items",
  CART_ITEM_BY_ID: (itemId: string) => `/api/v1/cart/items/${itemId}`,
  CART_MERGE: "/api/v1/cart/merge",

  // Checkout
  CHECKOUT_PREVIEW: "/api/v1/checkout/preview",
  CHECKOUT_VALIDATE: "/api/v1/checkout/validate",

  // Orders
  ORDERS: "/api/v1/orders",
  ORDER_BY_NUMBER: (orderNumber: string) => `/api/v1/orders/${orderNumber}`,
  ORDER_CANCEL: (orderNumber: string) => `/api/v1/orders/${orderNumber}/cancel`,

  // Payments
  PAYMENTS_PROCESS: "/api/v1/payments/process",
  PAYMENT_BY_ORDER: (orderNumber: string) => `/api/v1/payments/orders/${orderNumber}`,

  // Shipping
  SHIPMENT_BY_ORDER: (orderNumber: string) => `/api/v1/shipments/orders/${orderNumber}`,
  SHIPMENT_TRACK: (trackingNumber: string) => `/api/v1/shipments/track/${trackingNumber}`,

  // Coupons
  COUPONS_VALIDATE: "/api/v1/coupons/validate",

  // Reviews & Favorites
  PRODUCT_REVIEWS: (productId: string) => `/api/v1/products/${productId}/reviews`,
  FAVORITES: "/api/v1/favorites",
  FAVORITE_TOGGLE: (productId: string) => `/api/v1/favorites/${productId}`,

  // Admin APIs
  ADMIN_CATEGORIES: "/api/v1/admin/categories",
  ADMIN_CATEGORY_BY_ID: (id: string) => `/api/v1/admin/categories/${id}`,
  ADMIN_CATEGORY_STATUS: (id: string) => `/api/v1/admin/categories/${id}/status`,

  ADMIN_PRODUCTS: "/api/v1/admin/products",
  ADMIN_PRODUCT_BY_ID: (id: string) => `/api/v1/admin/products/${id}`,
  ADMIN_PRODUCT_STATUS: (id: string) => `/api/v1/admin/products/${id}/status`,
  ADMIN_PRODUCT_IMAGES: (id: string) => `/api/v1/admin/products/${id}/images`,
  ADMIN_PRODUCT_IMAGE_DELETE: (id: string, imageId: string) => `/api/v1/admin/products/${id}/images/${imageId}`,
  ADMIN_PRODUCT_FEATURES: (id: string) => `/api/v1/admin/products/${id}/features`,
  ADMIN_PRODUCT_FEATURE_DELETE: (id: string, featureId: string) => `/api/v1/admin/products/${id}/features/${featureId}`,
  ADMIN_PRODUCT_COMPATIBILITIES: (id: string) => `/api/v1/admin/products/${id}/compatibilities`,
  ADMIN_PRODUCT_COMPATIBILITY_DELETE: (id: string, compatibilityId: string) => `/api/v1/admin/products/${id}/compatibilities/${compatibilityId}`,

  ADMIN_ORDERS: "/api/v1/admin/orders",
  ADMIN_ORDER_BY_ID: (id: string) => `/api/v1/admin/orders/${id}`,
  ADMIN_ORDER_STATUS: (id: string) => `/api/v1/admin/orders/${id}/status`,

  ADMIN_SHIPMENTS: "/api/v1/admin/shipments",
  ADMIN_SHIPMENT_BY_ID: (id: string) => `/api/v1/admin/shipments/${id}`,
  ADMIN_SHIPMENT_STATUS: (id: string) => `/api/v1/admin/shipments/${id}/status`,

  ADMIN_COUPONS: "/api/v1/admin/coupons",
  ADMIN_COUPON_BY_ID: (id: string) => `/api/v1/admin/coupons/${id}`,
  ADMIN_COUPON_STATUS: (id: string) => `/api/v1/admin/coupons/${id}/status`,

  ADMIN_REVIEWS: "/api/v1/admin/reviews",
  ADMIN_REVIEW_STATUS: (id: string) => `/api/v1/admin/reviews/${id}/status`,
};
