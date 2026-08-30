package com.carmats.checkout.service;

import com.carmats.cart.entity.Cart;
import com.carmats.cart.entity.CartItem;
import com.carmats.cart.service.CartService;
import com.carmats.catalog.entity.Category;
import com.carmats.catalog.entity.Product;
import com.carmats.catalog.entity.ProductStatus;
import com.carmats.catalog.repository.ProductImageRepository;
import com.carmats.checkout.dto.request.CheckoutPreviewRequest;
import com.carmats.checkout.dto.response.CheckoutSummaryResponse;
import com.carmats.checkout.dto.response.CheckoutValidationResponse;
import com.carmats.common.exception.BusinessException;
import com.carmats.user.entity.Address;
import com.carmats.user.entity.User;
import com.carmats.user.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private CartService cartService;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @InjectMocks
    private CheckoutService checkoutService;

    private User testUser;
    private Cart testCart;
    private Product productFreeShipping;
    private Product productBelowThreshold;
    private Address testAddress;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User("user@carmats.local", "hash", "Ahmet", "Yılmaz", "+905551112233");
        testCart = new Cart(testUser);

        Category category = new Category("Paspas", "paspas");
        productFreeShipping = new Product(
                category,
                "Lüks Paspas Seti",
                "luks-paspas-seti",
                "LUX-001",
                "Açıklama",
                "Kısa açıklama",
                new BigDecimal("1500.00"),
                new BigDecimal("1200.00"),
                10,
                ProductStatus.ACTIVE,
                true,
                "Kauçuk",
                "Sahler"
        );

        productBelowThreshold = new Product(
                category,
                "Standart Paspas",
                "standart-paspas",
                "STD-001",
                "Açıklama",
                "Kısa açıklama",
                new BigDecimal("600.00"),
                new BigDecimal("500.00"),
                10,
                ProductStatus.ACTIVE,
                true,
                "Kauçuk",
                "Sahler"
        );

        testAddress = new Address(
                testUser,
                "Ev",
                "Ahmet",
                "Yılmaz",
                "+905551112233",
                "İstanbul",
                "Kadıköy",
                "Moda",
                "Caferağa Mah. No:1",
                "34710",
                null,
                null,
                null,
                true,
                true
        );
    }

    @Test
    @DisplayName("Should apply free shipping when subtotal >= 1000 TL")
    void shouldApplyFreeShipping() {
        CartItem item = new CartItem(testCart, productFreeShipping, null, 1, new BigDecimal("1200.00"));
        testCart.addItem(item);

        when(cartService.getOrCreateCart(userId, null)).thenReturn(testCart);
        when(addressRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(testAddress));

        CheckoutSummaryResponse summary = checkoutService.getCheckoutSummary(userId, null, null);

        assertThat(summary.subtotal()).isEqualByComparingTo(new BigDecimal("1200.00"));
        assertThat(summary.freeShippingApplied()).isTrue();
        assertThat(summary.shippingFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.grandTotal()).isEqualByComparingTo(new BigDecimal("1200.00"));
        assertThat(summary.deliveryAddress()).isNotNull();
        assertThat(summary.deliveryAddress().city()).isEqualTo("İstanbul");
    }

    @Test
    @DisplayName("Should add shipping fee when subtotal < 1000 TL")
    void shouldAddShippingFee() {
        CartItem item = new CartItem(testCart, productBelowThreshold, null, 1, new BigDecimal("500.00"));
        testCart.addItem(item);

        when(cartService.getOrCreateCart(userId, null)).thenReturn(testCart);
        when(addressRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(testAddress));

        CheckoutSummaryResponse summary = checkoutService.getCheckoutSummary(userId, null, null);

        assertThat(summary.subtotal()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(summary.freeShippingApplied()).isFalse();
        assertThat(summary.shippingFee()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(summary.grandTotal()).isEqualByComparingTo(new BigDecimal("575.00"));
    }

    @Test
    @DisplayName("Should throw BusinessException when cart is empty")
    void shouldThrowWhenCartIsEmpty() {
        when(cartService.getOrCreateCart(userId, null)).thenReturn(testCart);

        assertThatThrownBy(() -> checkoutService.getCheckoutSummary(userId, null, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "EMPTY_CART");
    }

    @Test
    @DisplayName("Should validate checkout successfully")
    void shouldValidateCheckout() {
        CartItem item = new CartItem(testCart, productFreeShipping, null, 1, new BigDecimal("1200.00"));
        testCart.addItem(item);

        when(cartService.getOrCreateCart(userId, null)).thenReturn(testCart);

        CheckoutValidationResponse response = checkoutService.validateCheckout(userId, null, null);

        assertThat(response.valid()).isTrue();
        assertThat(response.errors()).isEmpty();
    }
}
