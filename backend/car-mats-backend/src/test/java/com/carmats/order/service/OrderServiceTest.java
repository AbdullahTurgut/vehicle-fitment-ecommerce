package com.carmats.order.service;

import com.carmats.cart.entity.Cart;
import com.carmats.cart.entity.CartItem;
import com.carmats.cart.service.CartService;
import com.carmats.catalog.entity.Category;
import com.carmats.catalog.entity.Product;
import com.carmats.catalog.entity.ProductStatus;
import com.carmats.catalog.repository.ProductImageRepository;
import com.carmats.catalog.repository.ProductRepository;
import com.carmats.common.exception.BusinessException;
import com.carmats.order.dto.request.CreateOrderRequest;
import com.carmats.order.dto.request.UpdateOrderStatusRequest;
import com.carmats.order.dto.response.OrderResponse;
import com.carmats.order.entity.Order;
import com.carmats.order.entity.OrderStatus;
import com.carmats.order.repository.OrderRepository;
import com.carmats.user.entity.Address;
import com.carmats.user.entity.User;
import com.carmats.user.repository.AddressRepository;
import com.carmats.user.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private Address testAddress;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User("user@carmats.local", "hash", "Ahmet", "Yılmaz", "+905551112233");
        org.springframework.test.util.ReflectionTestUtils.setField(testUser, "id", userId);
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
    @DisplayName("Should create order, deduct stock, and clear cart")
    void shouldCreateOrderSuccessfully() {
        CartItem cartItem = new CartItem(testCart, testProduct, null, 2, new BigDecimal("1200.00"));
        testCart.addItem(cartItem);

        when(cartService.getOrCreateCart(userId, null)).thenReturn(testCart);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(addressRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(testAddress));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest(
                null, null, null, null, null, null, null, null, "Kapıda zile basmayınız", null
        );

        OrderResponse response = orderService.createOrder(userId, null, request);

        assertThat(response).isNotNull();
        assertThat(response.orderNumber()).startsWith("ORD-");
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(response.subtotal()).isEqualByComparingTo(new BigDecimal("2400.00"));
        assertThat(response.items()).hasSize(1);
        assertThat(testProduct.getStockQuantity()).isEqualTo(8); // 10 - 2

        verify(productRepository).save(testProduct);
        verify(cartService).clearCart(userId, null);
    }

    @Test
    @DisplayName("Should throw BusinessException when creating order with empty cart")
    void shouldThrowWhenCartIsEmpty() {
        when(cartService.getOrCreateCart(userId, null)).thenReturn(testCart);

        CreateOrderRequest request = new CreateOrderRequest(
                null, null, null, null, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> orderService.createOrder(userId, null, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "EMPTY_CART");
    }

    @Test
    @DisplayName("Should cancel order and restore product stock")
    void shouldCancelOrderAndRestoreStock() {
        Order order = new Order(
                "ORD-20260830-ABCDEF",
                testUser,
                testUser.getEmail(),
                testUser.getFirstName(),
                testUser.getLastName(),
                testUser.getPhoneNumber(),
                OrderStatus.PENDING_PAYMENT,
                new BigDecimal("2400.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("2400.00"),
                null
        );
        order.addItem(new com.carmats.order.entity.OrderItem(
                order, testProduct, testProduct.getName(), testProduct.getSlug(),
                testProduct.getSku(), null, null, null, 2, new BigDecimal("1200.00")
        ));
        testProduct.setStockQuantity(8);

        when(orderRepository.findByOrderNumberAndUserId("ORD-20260830-ABCDEF", userId))
                .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.cancelOrder(userId, "ORD-20260830-ABCDEF", "Vazgeçtim");

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(testProduct.getStockQuantity()).isEqualTo(10); // 8 + 2 restored
        verify(productRepository).save(testProduct);
    }

    @Test
    @DisplayName("Should update order status and log history")
    void shouldUpdateOrderStatus() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(
                "ORD-20260830-ABCDEF",
                testUser,
                testUser.getEmail(),
                testUser.getFirstName(),
                testUser.getLastName(),
                testUser.getPhoneNumber(),
                OrderStatus.PAID,
                new BigDecimal("2400.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("2400.00"),
                null
        );

        when(orderRepository.findByIdWithDetails(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.SHIPPED, "Kargoya verildi.");
        OrderResponse response = orderService.updateOrderStatus(orderId, request, "admin@carmats.local");

        assertThat(response.status()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(order.getStatusHistory()).hasSize(1);
        assertThat(order.getStatusHistory().get(0).getToStatus()).isEqualTo(OrderStatus.SHIPPED);
    }
}
