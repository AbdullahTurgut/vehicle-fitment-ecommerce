package com.carmats.favorite.service;

import com.carmats.catalog.entity.Product;
import com.carmats.catalog.repository.ProductRepository;
import com.carmats.favorite.dto.response.FavoriteResponse;
import com.carmats.favorite.dto.response.FavoriteToggleResponse;
import com.carmats.favorite.entity.Favorite;
import com.carmats.favorite.repository.FavoriteRepository;
import com.carmats.user.entity.User;
import com.carmats.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    private User testUser;
    private Product testProduct;
    private UUID userId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();

        testUser = new User("favuser@carmats.local", "pass", "Mehmet", "Demir", null);
        ReflectionTestUtils.setField(testUser, "id", userId);

        testProduct = new Product(
                null, "Bagaj Havuzu", "bagaj-havuzu", "BAG-001",
                "Açıklama", "Kısa açıklama", null, null, 10, null, false, "TPE", "Rizline"
        );
        ReflectionTestUtils.setField(testProduct, "id", productId);
    }

    @Test
    @DisplayName("Should add product to favorites when toggled and not previously favorited")
    void shouldAddFavoriteWhenNotPresent() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(favoriteRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.empty());

        FavoriteToggleResponse response = favoriteService.toggleFavorite(userId, productId);

        assertThat(response.isFavorite()).isTrue();
        assertThat(response.message()).contains("eklendi");
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    @DisplayName("Should remove product from favorites when toggled and already favorited")
    void shouldRemoveFavoriteWhenAlreadyPresent() {
        Favorite existing = new Favorite(testUser, testProduct);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(favoriteRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.of(existing));

        FavoriteToggleResponse response = favoriteService.toggleFavorite(userId, productId);

        assertThat(response.isFavorite()).isFalse();
        assertThat(response.message()).contains("çıkarıldı");
        verify(favoriteRepository).delete(existing);
    }

    @Test
    @DisplayName("Should get user favorites paginated")
    void shouldGetUserFavorites() {
        Favorite fav = new Favorite(testUser, testProduct);
        Page<Favorite> page = new PageImpl<>(List.of(fav));

        when(favoriteRepository.findAllByUserIdWithProduct(eq(userId), any())).thenReturn(page);

        var result = favoriteService.getUserFavorites(userId, PageRequest.of(0, 10));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).productName()).isEqualTo("Bagaj Havuzu");
    }
}
