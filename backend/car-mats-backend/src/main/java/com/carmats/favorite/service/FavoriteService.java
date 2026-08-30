package com.carmats.favorite.service;

import com.carmats.catalog.entity.Product;
import com.carmats.catalog.repository.ProductRepository;
import com.carmats.common.exception.NotFoundException;
import com.carmats.common.response.PageResponse;
import com.carmats.favorite.dto.response.FavoriteResponse;
import com.carmats.favorite.dto.response.FavoriteToggleResponse;
import com.carmats.favorite.entity.Favorite;
import com.carmats.favorite.mapper.FavoriteMapper;
import com.carmats.favorite.repository.FavoriteRepository;
import com.carmats.user.entity.User;
import com.carmats.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public FavoriteService(
            FavoriteRepository favoriteRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public FavoriteToggleResponse toggleFavorite(UUID userId, UUID productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Kullanıcı bulunamadı."));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Ürün bulunamadı."));

        Optional<Favorite> existing = favoriteRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return new FavoriteToggleResponse(productId, false, "Ürün favorilerden çıkarıldı.");
        } else {
            Favorite favorite = new Favorite(user, product);
            favoriteRepository.save(favorite);
            return new FavoriteToggleResponse(productId, true, "Ürün favorilere eklendi.");
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<FavoriteResponse> getUserFavorites(UUID userId, Pageable pageable) {
        Page<Favorite> page = favoriteRepository.findAllByUserIdWithProduct(userId, pageable);
        return PageResponse.from(page.map(FavoriteMapper::toResponse));
    }

    public void removeFavorite(UUID userId, UUID productId) {
        if (!favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new NotFoundException("FAVORITE_NOT_FOUND", "Favori kaydı bulunamadı.");
        }
        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
    }
}
