export interface Category {
  id: string;
  name: string;
  slug: string;
  description?: string;
  imageUrl?: string;
  parentId?: string;
  sortOrder?: number;
  active?: boolean;
}

export interface ProductImage {
  id: string;
  imageUrl: string;
  altText?: string;
  sortOrder: number;
  primary: boolean;
}

export interface ProductFeature {
  id: string;
  title: string;
  description: string;
  iconName?: string;
  sortOrder: number;
}

export interface ProductCompatibility {
  id: string;
  variantId: string;
  variantName?: string;
  generationName?: string;
  modelName?: string;
  brandName?: string;
  startYear?: number;
  endYear?: number;
  notes?: string;
}

export interface ProductList {
  id: string;
  name: string;
  slug: string;
  sku: string;
  basePrice: number;
  salePrice?: number;
  effectivePrice: number;
  stockQuantity: number;
  inStock: boolean;
  primaryImageUrl?: string;
  featured?: boolean;
}

export interface ProductDetail {
  id: string;
  name: string;
  slug: string;
  sku: string;
  shortDescription?: string;
  description?: string;
  basePrice: number;
  salePrice?: number;
  effectivePrice: number;
  stockQuantity: number;
  inStock: boolean;
  manufacturerBrand?: string;
  material?: string;
  category?: Category;
  images: ProductImage[];
  features: ProductFeature[];
  compatibilities?: ProductCompatibility[];
  averageRating?: number;
  reviewCount?: number;
}

export type ProductStatus = "DRAFT" | "ACTIVE" | "PASSIVE" | "OUT_OF_STOCK";

export interface AdminProductSummary {
  id: string;
  name: string;
  slug: string;
  sku: string;
  basePrice: number;
  salePrice?: number;
  stockQuantity: number;
  status: ProductStatus;
  primaryImageUrl?: string;
  categoryName?: string;
}
