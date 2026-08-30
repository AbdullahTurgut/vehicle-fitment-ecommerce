package com.carmats.review.entity;

import com.carmats.catalog.entity.Product;
import com.carmats.common.entity.BaseEntity;
import com.carmats.order.entity.Order;
import com.carmats.user.entity.User;
import jakarta.persistence.*;

@Entity
@Table(
        name = "product_reviews",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_product_reviews_user_product", columnNames = {"product_id", "user_id"})
        }
)
public class ProductReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private int rating;

    @Column(length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_verified_purchase", nullable = false)
    private boolean isVerifiedPurchase = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewStatus status = ReviewStatus.PENDING;

    protected ProductReview() {
    }

    public ProductReview(
            Product product,
            User user,
            Order order,
            int rating,
            String title,
            String comment,
            boolean isVerifiedPurchase,
            ReviewStatus status
    ) {
        this.product = product;
        this.user = user;
        this.order = order;
        this.rating = rating;
        this.title = title;
        this.comment = comment;
        this.isVerifiedPurchase = isVerifiedPurchase;
        this.status = status != null ? status : ReviewStatus.PENDING;
    }

    public Product getProduct() {
        return product;
    }

    public User getUser() {
        return user;
    }

    public Order getOrder() {
        return order;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isVerifiedPurchase() {
        return isVerifiedPurchase;
    }

    public void setVerifiedPurchase(boolean verifiedPurchase) {
        isVerifiedPurchase = verifiedPurchase;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }
}
