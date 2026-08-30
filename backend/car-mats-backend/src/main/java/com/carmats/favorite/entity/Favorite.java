package com.carmats.favorite.entity;

import com.carmats.catalog.entity.Product;
import com.carmats.common.entity.BaseEntity;
import com.carmats.user.entity.User;
import jakarta.persistence.*;

@Entity
@Table(
        name = "favorites",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_favorites_user_product", columnNames = {"user_id", "product_id"})
        }
)
public class Favorite extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    protected Favorite() {
    }

    public Favorite(User user, Product product) {
        this.user = user;
        this.product = product;
    }

    public User getUser() {
        return user;
    }

    public Product getProduct() {
        return product;
    }
}
