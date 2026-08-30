package com.carmats.cart.service;

import com.carmats.cart.dto.request.AddToCartRequest;
import com.carmats.cart.dto.request.UpdateCartItemQuantityRequest;
import com.carmats.cart.dto.response.CartResponse;
import com.carmats.cart.entity.Cart;
import com.carmats.cart.entity.CartItem;
import com.carmats.cart.repository.CartItemRepository;
import com.carmats.cart.repository.CartRepository;
import com.carmats.catalog.entity.Category;
import com.carmats.catalog.entity.Product;
import com.carmats.catalog.entity.ProductStatus;
import com.carmats.catalog.repository.ProductCompatibilityRepository;
import com.carmats.catalog.repository.ProductImageRepository;
import com.carmats.catalog.repository.ProductRepository;
import com.carmats.common.exception.BusinessException;
import com.carmats.common.exception.NotFoundException;
import com.carmats.user.entity.User;
import com.carmats.user.repository.UserRepository;
import com.carmats.vehicle.entity.VehicleVariant;
import com.carmats.vehicle.repository.VehicleVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductCompatibilityRepository productCompatibilityRepository;

    @Mock
    private VehicleVariantRepository vehicleVariantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private UUID userId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();

        testUser = new User("user@carmats.local", "hash", "Ahmet", "Yılmaz", null);
        testCart = new Cart(testUser);

        Category category = new Category("Paspas", "paspas");
        testProduct = new Product(
                category,
                "3D Havuzlu Paspas",
                "3d-havuzlu-paspas",
                "PAS-001",
                "Açıklama",
                "Kısa açıklama",
                new BigDecimal("1500.00"),
                new BigDecimal("1200.00"),
                10,
                ProductStatus.ACTIVE,
                true,
                "TPE",
                "Sahler"
        );
    }

    @Test
    @DisplayName("Should return cart")
    void shouldGetCart() {
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));

        CartResponse response = cartService.getCart(userId, null);

        assertThat(response).isNotNull();
        assertThat(response.totalQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should add item to cart successfully")
    void shouldAddItemToCart() {
        AddToCartRequest request = new AddToCartRequest(productId, null, 2);

        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = cartService.addItem(userId, null, request);

        assertThat(response.items()).hasSize(1);
        assertThat(response.totalQuantity()).isEqualTo(2);
        assertThat(response.subtotal()).isEqualByComparingTo(new BigDecimal("2400.00"));
    }

    @Test
    @DisplayName("Should throw BusinessException when adding inactive product")
    void shouldThrowWhenProductInactive() {
        testProduct.setStatus(ProductStatus.PASSIVE);
        AddToCartRequest request = new AddToCartRequest(productId, null, 1);

        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> cartService.addItem(userId, null, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "PRODUCT_NOT_ACTIVE");
    }

    @Test
    @DisplayName("Should throw BusinessException when stock is insufficient")
    void shouldThrowWhenStockInsufficient() {
        AddToCartRequest request = new AddToCartRequest(productId, null, 15); // stock is 10

        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> cartService.addItem(userId, null, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "INSUFFICIENT_STOCK");
    }

    @Test
    @DisplayName("Should update cart item quantity")
    void shouldUpdateItemQuantity() {
        UUID itemId = UUID.randomUUID();
        CartItem cartItem = new CartItem(testCart, testProduct, null, 2, new BigDecimal("1200.00"));
        org.springframework.test.util.ReflectionTestUtils.setField(cartItem, "id", itemId);
        testCart.addItem(cartItem);

        UpdateCartItemQuantityRequest request = new UpdateCartItemQuantityRequest(5);

        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = cartService.updateItemQuantity(userId, null, itemId, request);

        assertThat(response.totalQuantity()).isEqualTo(5);
        assertThat(response.subtotal()).isEqualByComparingTo(new BigDecimal("6000.00"));
    }

    @Test
    @DisplayName("Should merge guest cart into user cart")
    void shouldMergeGuestCart() {
        String guestToken = "guest-123";
        Cart guestCart = new Cart(guestToken);
        CartItem guestItem = new CartItem(guestCart, testProduct, null, 3, new BigDecimal("1200.00"));
        guestCart.addItem(guestItem);

        when(cartRepository.findByGuestTokenWithItems(guestToken)).thenReturn(Optional.of(guestCart));
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = cartService.mergeCart(userId, guestToken);

        assertThat(response.items()).hasSize(1);
        assertThat(response.totalQuantity()).isEqualTo(3);
        verify(cartRepository).delete(guestCart);
    }
}
