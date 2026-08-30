export type ReviewStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface ProductReview {
  id: string;
  productId: string;
  userId: string;
  userName: string;
  rating: number;
  comment: string;
  verifiedPurchase: boolean;
  status: ReviewStatus;
  createdAt: string;
}

export interface RatingDistribution {
  star1: number;
  star2: number;
  star3: number;
  star4: number;
  star5: number;
}

export interface ProductReviewSummary {
  averageRating: number;
  totalReviews: number;
  distribution: RatingDistribution;
  reviews: ProductReview[];
}

export interface CreateReviewRequest {
  rating: number;
  comment: string;
}

export interface FavoriteItem {
  id: string;
  productId: string;
  productName: string;
  productSlug: string;
  productSku: string;
  basePrice: number;
  salePrice?: number;
  effectivePrice: number;
  primaryImageUrl?: string;
  inStock: boolean;
  createdAt: string;
}

export interface FavoriteToggleResponse {
  productId: string;
  favorited: boolean;
  message: string;
}
