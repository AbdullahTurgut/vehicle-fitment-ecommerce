package com.carmats.order.service;

import com.carmats.cart.entity.Cart;
import com.carmats.cart.entity.CartItem;
import com.carmats.cart.service.CartService;
import com.carmats.catalog.entity.Product;
import com.carmats.catalog.entity.ProductImage;
import com.carmats.catalog.entity.ProductStatus;
import com.carmats.catalog.repository.ProductImageRepository;
import com.carmats.catalog.repository.ProductRepository;
import com.carmats.checkout.service.CheckoutService;
import com.carmats.common.response.PageResponse;
import com.carmats.common.exception.BusinessException;
import com.carmats.common.exception.NotFoundException;
import com.carmats.order.dto.request.CreateOrderRequest;
import com.carmats.order.dto.request.CustomOrderAddressDto;
import com.carmats.order.dto.request.UpdateOrderStatusRequest;
import com.carmats.order.dto.response.OrderResponse;
import com.carmats.order.dto.response.OrderSummaryResponse;
import com.carmats.order.entity.*;
import com.carmats.order.mapper.OrderMapper;
import com.carmats.order.repository.OrderRepository;
import com.carmats.user.entity.Address;
import com.carmats.user.entity.User;
import com.carmats.user.repository.AddressRepository;
import com.carmats.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            CartService cartService,
            ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            AddressRepository addressRepository,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    public OrderResponse createOrder(
            UUID userId,
            String guestToken,
            CreateOrderRequest request
    ) {
        String effectiveGuestToken = (request != null && request.guestToken() != null)
                ? request.guestToken()
                : guestToken;

        Cart cart = cartService.getOrCreateCart(userId, effectiveGuestToken);

        if (cart.getItems().isEmpty()) {
            throw new BusinessException(
                    "EMPTY_CART",
                    "Sepetinizde ürün bulunmamaktadır."
            );
        }

        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Kullanıcı bulunamadı."));
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new BusinessException(
                        "PRODUCT_NOT_ACTIVE",
                        "'" + product.getName() + "' ürünü satışta değildir."
                );
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BusinessException(
                        "INSUFFICIENT_STOCK",
                        "'" + product.getName() + "' ürünü için yetersiz stok. Mevcut: " + product.getStockQuantity()
                );
            }
            subtotal = subtotal.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        boolean freeShipping = subtotal.compareTo(CheckoutService.FREE_SHIPPING_THRESHOLD) >= 0;
        BigDecimal shippingFee = freeShipping ? BigDecimal.ZERO : CheckoutService.DEFAULT_SHIPPING_FEE;
        BigDecimal discountTotal = BigDecimal.ZERO;
        BigDecimal grandTotal = subtotal.add(shippingFee).subtract(discountTotal);

        String orderNumber = generateUniqueOrderNumber();

        Order order = new Order(
                orderNumber,
                user,
                user != null ? user.getEmail() : (request != null ? request.guestEmail() : null),
                user != null ? user.getFirstName() : (request != null ? request.guestFirstName() : null),
                user != null ? user.getLastName() : (request != null ? request.guestLastName() : null),
                user != null ? user.getPhoneNumber() : (request != null ? request.guestPhoneNumber() : null),
                OrderStatus.PENDING_PAYMENT,
                subtotal,
                shippingFee,
                discountTotal,
                grandTotal,
                request != null ? request.customerNotes() : null
        );

        // Process items & decrement stock
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            String primaryImageUrl = productImageRepository.findFirstByProductIdAndPrimaryTrue(product.getId())
                    .map(ProductImage::getUrl)
                    .orElse(null);

            String variantName = cartItem.getVehicleVariant() != null
                    ? cartItem.getVehicleVariant().getName()
                    : null;

            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    product.getName(),
                    product.getSlug(),
                    product.getSku(),
                    primaryImageUrl,
                    cartItem.getVehicleVariant(),
                    variantName,
                    cartItem.getQuantity(),
                    cartItem.getUnitPrice()
            );
            order.addItem(orderItem);
        }

        // Process addresses
        attachOrderAddresses(order, user, request);

        // Add initial status history
        order.addStatusHistory(
                null,
                OrderStatus.PENDING_PAYMENT,
                "Sipariş oluşturuldu.",
                user != null ? user.getEmail() : "GUEST"
        );

        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cartService.clearCart(userId, effectiveGuestToken);

        return OrderMapper.toOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getUserOrders(UUID userId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAllByUserId(userId, pageable);
        return PageResponse.from(orderPage.map(OrderMapper::toOrderSummaryResponse));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(UUID userId, String orderNumber) {
        Order order;
        if (userId != null) {
            order = orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
                    .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Sipariş bulunamadı."));
        } else {
            order = orderRepository.findByOrderNumberWithDetails(orderNumber)
                    .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Sipariş bulunamadı."));
        }
        return OrderMapper.toOrderResponse(order);
    }

    public OrderResponse cancelOrder(UUID userId, String orderNumber, String reason) {
        Order order;
        if (userId != null) {
            order = orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
                    .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Sipariş bulunamadı."));
        } else {
            order = orderRepository.findByOrderNumberWithDetails(orderNumber)
                    .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Sipariş bulunamadı."));
        }

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REFUNDED) {
            throw new BusinessException("ORDER_ALREADY_CANCELLED", "Bu sipariş zaten iptal edilmiştir.");
        }

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("ORDER_CANNOT_BE_CANCELLED", "Kargoya verilmiş veya teslim edilmiş siparişler iptal edilemez.");
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.addStatusHistory(
                order.getStatus(),
                OrderStatus.CANCELLED,
                reason != null ? reason : "Müşteri tarafından iptal edildi.",
                userId != null ? "CUSTOMER" : "GUEST"
        );

        Order saved = orderRepository.save(order);
        return OrderMapper.toOrderResponse(saved);
    }

    // Admin APIs
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAllByStatusFilter(status, pageable);
        return PageResponse.from(orderPage.map(OrderMapper::toOrderSummaryResponse));
    }

    @Transactional(readOnly = true)
    public OrderResponse getAdminOrderDetail(UUID orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Sipariş bulunamadı."));
        return OrderMapper.toOrderResponse(order);
    }

    public OrderResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request, String changedBy) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Sipariş bulunamadı."));

        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = request.status();

        if (oldStatus == newStatus) {
            return OrderMapper.toOrderResponse(order);
        }

        // If transitioning to CANCELLED or REFUNDED from an active state, restore stock
        if ((newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.REFUNDED)
                && (oldStatus != OrderStatus.CANCELLED && oldStatus != OrderStatus.REFUNDED)) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.addStatusHistory(oldStatus, newStatus, request.note(), changedBy);
        Order saved = orderRepository.save(order);
        return OrderMapper.toOrderResponse(saved);
    }

    private void attachOrderAddresses(Order order, User user, CreateOrderRequest request) {
        OrderAddress deliveryAddress = null;
        OrderAddress billingAddress = null;

        if (user != null) {
            List<Address> userAddresses = addressRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());

            if (request != null && request.deliveryAddressId() != null) {
                Address addr = userAddresses.stream()
                        .filter(a -> a.getId().equals(request.deliveryAddressId()))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException("ADDRESS_NOT_FOUND", "Teslimat adresi bulunamadı."));
                deliveryAddress = mapToOrderAddress(order, OrderAddressType.DELIVERY, addr);
            } else if (request != null && request.customDeliveryAddress() != null) {
                deliveryAddress = mapCustomAddressToOrderAddress(order, OrderAddressType.DELIVERY, request.customDeliveryAddress());
            } else {
                Address defaultAddr = userAddresses.stream()
                        .filter(Address::isDefaultDelivery)
                        .findFirst()
                        .or(() -> userAddresses.stream().findFirst())
                        .orElse(null);
                if (defaultAddr != null) {
                    deliveryAddress = mapToOrderAddress(order, OrderAddressType.DELIVERY, defaultAddr);
                }
            }

            if (request != null && request.billingAddressId() != null) {
                Address addr = userAddresses.stream()
                        .filter(a -> a.getId().equals(request.billingAddressId()))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException("ADDRESS_NOT_FOUND", "Fatura adresi bulunamadı."));
                billingAddress = mapToOrderAddress(order, OrderAddressType.BILLING, addr);
            } else if (request != null && request.customBillingAddress() != null) {
                billingAddress = mapCustomAddressToOrderAddress(order, OrderAddressType.BILLING, request.customBillingAddress());
            } else {
                Address defaultBilling = userAddresses.stream()
                        .filter(Address::isDefaultBilling)
                        .findFirst()
                        .orElse(null);
                if (defaultBilling != null) {
                    billingAddress = mapToOrderAddress(order, OrderAddressType.BILLING, defaultBilling);
                } else if (deliveryAddress != null) {
                    billingAddress = cloneOrderAddressForType(order, OrderAddressType.BILLING, deliveryAddress);
                }
            }
        } else if (request != null) {
            if (request.customDeliveryAddress() != null) {
                deliveryAddress = mapCustomAddressToOrderAddress(order, OrderAddressType.DELIVERY, request.customDeliveryAddress());
            }
            if (request.customBillingAddress() != null) {
                billingAddress = mapCustomAddressToOrderAddress(order, OrderAddressType.BILLING, request.customBillingAddress());
            } else if (deliveryAddress != null) {
                billingAddress = cloneOrderAddressForType(order, OrderAddressType.BILLING, deliveryAddress);
            }
        }

        if (deliveryAddress == null) {
            throw new BusinessException("DELIVERY_ADDRESS_REQUIRED", "Teslimat adresi zorunludur.");
        }
        if (billingAddress == null) {
            billingAddress = cloneOrderAddressForType(order, OrderAddressType.BILLING, deliveryAddress);
        }

        order.addAddress(deliveryAddress);
        order.addAddress(billingAddress);
    }

    private OrderAddress mapToOrderAddress(Order order, OrderAddressType type, Address addr) {
        return new OrderAddress(
                order,
                type,
                addr.getFirstName(),
                addr.getLastName(),
                addr.getPhoneNumber(),
                addr.getCity(),
                addr.getDistrict(),
                addr.getNeighborhood(),
                addr.getAddressLine(),
                addr.getPostalCode(),
                addr.getCompanyName(),
                addr.getTaxNumber(),
                addr.getTaxOffice()
        );
    }

    private OrderAddress mapCustomAddressToOrderAddress(Order order, OrderAddressType type, CustomOrderAddressDto dto) {
        return new OrderAddress(
                order,
                type,
                dto.firstName(),
                dto.lastName(),
                dto.phoneNumber(),
                dto.city(),
                dto.district(),
                dto.neighborhood(),
                dto.addressLine(),
                dto.postalCode(),
                dto.companyName(),
                dto.taxNumber(),
                dto.taxOffice()
        );
    }

    private OrderAddress cloneOrderAddressForType(Order order, OrderAddressType newType, OrderAddress source) {
        return new OrderAddress(
                order,
                newType,
                source.getFirstName(),
                source.getLastName(),
                source.getPhoneNumber(),
                source.getCity(),
                source.getDistrict(),
                source.getNeighborhood(),
                source.getAddressLine(),
                source.getPostalCode(),
                source.getCompanyName(),
                source.getTaxNumber(),
                source.getTaxOffice()
        );
    }

    private String generateUniqueOrderNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String candidate = "ORD-" + datePart + "-" + randomPart;
        while (orderRepository.existsByOrderNumber(candidate)) {
            randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            candidate = "ORD-" + datePart + "-" + randomPart;
        }
        return candidate;
    }
}
