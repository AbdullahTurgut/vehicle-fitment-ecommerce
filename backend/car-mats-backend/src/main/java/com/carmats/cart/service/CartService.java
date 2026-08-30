package com.carmats.cart.service;

import com.carmats.cart.dto.request.AddToCartRequest;
import com.carmats.cart.dto.request.UpdateCartItemQuantityRequest;
import com.carmats.cart.dto.response.CartResponse;
import com.carmats.cart.entity.Cart;
import com.carmats.cart.entity.CartItem;
import com.carmats.cart.mapper.CartMapper;
import com.carmats.cart.repository.CartItemRepository;
import com.carmats.cart.repository.CartRepository;
import com.carmats.catalog.entity.Product;
import com.carmats.catalog.entity.ProductImage;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductCompatibilityRepository productCompatibilityRepository;
    private final VehicleVariantRepository vehicleVariantRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            ProductCompatibilityRepository productCompatibilityRepository,
            VehicleVariantRepository vehicleVariantRepository,
            UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productCompatibilityRepository = productCompatibilityRepository;
        this.vehicleVariantRepository = vehicleVariantRepository;
        this.userRepository = userRepository;
    }

    public CartResponse getCart(UUID userId, String guestToken) {
        Cart cart = getOrCreateCart(userId, guestToken);
        return mapToCartResponse(cart);
    }

    public CartResponse addItem(UUID userId, String guestToken, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId, guestToken);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() ->
                        new NotFoundException(
                                "PRODUCT_NOT_FOUND",
                                "Ürün bulunamadı."
                        )
                );

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BusinessException(
                    "PRODUCT_NOT_ACTIVE",
                    "Bu ürün şu anda satışta değildir."
            );
        }

        if (product.getStockQuantity() < request.quantity()) {
            throw new BusinessException(
                    "INSUFFICIENT_STOCK",
                    "Yetersiz stok. Mevcut stok: " + product.getStockQuantity()
            );
        }

        VehicleVariant variant = null;
        if (request.vehicleVariantId() != null) {
            variant = vehicleVariantRepository.findById(request.vehicleVariantId())
                    .orElseThrow(() ->
                            new NotFoundException(
                                    "VEHICLE_VARIANT_NOT_FOUND",
                                    "Araç varyantı bulunamadı."
                            )
                    );

            if (!productCompatibilityRepository.existsByProductIdAndVehicleVariantId(product.getId(), variant.getId())) {
                throw new BusinessException(
                        "INCOMPATIBLE_VEHICLE",
                        "Bu ürün seçilen araç varyantı ile uyumlu değildir."
                );
            }
        }

        BigDecimal effectivePrice = product.getSalePrice() != null
                ? product.getSalePrice()
                : product.getBasePrice();

        VehicleVariant finalVariant = variant;
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId())
                        && Objects.equals(
                        item.getVehicleVariant() != null ? item.getVehicleVariant().getId() : null,
                        finalVariant != null ? finalVariant.getId() : null
                ))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.quantity();
            if (newQuantity > product.getStockQuantity()) {
                throw new BusinessException(
                        "INSUFFICIENT_STOCK",
                        "Sepetteki toplam adet mevcut stoğu aşıyor. Mevcut stok: " + product.getStockQuantity()
                );
            }
            item.setQuantity(newQuantity);
            item.setUnitPrice(effectivePrice);
        } else {
            CartItem newItem = new CartItem(cart, product, finalVariant, request.quantity(), effectivePrice);
            cart.addItem(newItem);
        }

        Cart saved = cartRepository.save(cart);
        return mapToCartResponse(saved);
    }

    public CartResponse updateItemQuantity(UUID userId, String guestToken, UUID itemId, UpdateCartItemQuantityRequest request) {
        Cart cart = getOrCreateCart(userId, guestToken);

        CartItem item = cart.getItems().stream()
                .filter(i -> Objects.equals(i.getId(), itemId))
                .findFirst()
                .orElseThrow(() ->
                        new NotFoundException(
                                "CART_ITEM_NOT_FOUND",
                                "Sepet kalemi bulunamadı."
                        )
                );

        if (request.quantity() > item.getProduct().getStockQuantity()) {
            throw new BusinessException(
                    "INSUFFICIENT_STOCK",
                    "Yetersiz stok. Mevcut stok: " + item.getProduct().getStockQuantity()
            );
        }

        item.setQuantity(request.quantity());
        Cart saved = cartRepository.save(cart);
        return mapToCartResponse(saved);
    }

    public CartResponse removeItem(UUID userId, String guestToken, UUID itemId) {
        Cart cart = getOrCreateCart(userId, guestToken);

        CartItem item = cart.getItems().stream()
                .filter(i -> Objects.equals(i.getId(), itemId))
                .findFirst()
                .orElseThrow(() ->
                        new NotFoundException(
                                "CART_ITEM_NOT_FOUND",
                                "Sepet kalemi bulunamadı."
                        )
                );

        cart.removeItem(item);
        Cart saved = cartRepository.save(cart);
        return mapToCartResponse(saved);
    }

    public void clearCart(UUID userId, String guestToken) {
        Cart cart = getOrCreateCart(userId, guestToken);
        cart.clearItems();
        cartRepository.save(cart);
    }

    public CartResponse mergeCart(UUID userId, String guestToken) {
        if (userId == null) {
            throw new BusinessException("UNAUTHORIZED", "Sepet birleştirme işlemi için oturum açılmalıdır.");
        }

        if (guestToken == null || guestToken.isBlank()) {
            return getCart(userId, null);
        }

        Optional<Cart> guestCartOpt = cartRepository.findByGuestTokenWithItems(guestToken);
        if (guestCartOpt.isEmpty() || guestCartOpt.get().getItems().isEmpty()) {
            return getCart(userId, null);
        }

        Cart guestCart = guestCartOpt.get();
        Cart userCart = getOrCreateCart(userId, null);

        for (CartItem guestItem : guestCart.getItems()) {
            Optional<CartItem> userItemOpt = userCart.getItems().stream()
                    .filter(ui -> ui.getProduct().getId().equals(guestItem.getProduct().getId())
                            && Objects.equals(
                            ui.getVehicleVariant() != null ? ui.getVehicleVariant().getId() : null,
                            guestItem.getVehicleVariant() != null ? guestItem.getVehicleVariant().getId() : null
                    ))
                    .findFirst();

            if (userItemOpt.isPresent()) {
                CartItem userItem = userItemOpt.get();
                int combinedQty = userItem.getQuantity() + guestItem.getQuantity();
                int maxAllowed = Math.min(combinedQty, guestItem.getProduct().getStockQuantity());
                userItem.setQuantity(maxAllowed);
            } else {
                CartItem newUserItem = new CartItem(
                        userCart,
                        guestItem.getProduct(),
                        guestItem.getVehicleVariant(),
                        guestItem.getQuantity(),
                        guestItem.getUnitPrice()
                );
                userCart.addItem(newUserItem);
            }
        }

        cartRepository.delete(guestCart);
        Cart saved = cartRepository.save(userCart);
        return mapToCartResponse(saved);
    }

    public Cart getOrCreateCart(UUID userId, String guestToken) {
        if (userId != null) {
            return cartRepository.findByUserIdWithItems(userId)
                    .orElseGet(() -> {
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Kullanıcı bulunamadı."));
                        return cartRepository.save(new Cart(user));
                    });
        }

        String effectiveToken = (guestToken != null && !guestToken.isBlank())
                ? guestToken
                : UUID.randomUUID().toString();

        return cartRepository.findByGuestTokenWithItems(effectiveToken)
                .orElseGet(() -> cartRepository.save(new Cart(effectiveToken)));
    }

    private CartResponse mapToCartResponse(Cart cart) {
        Map<UUID, String> primaryImageMap = new HashMap<>();
        for (CartItem item : cart.getItems()) {
            UUID pId = item.getProduct().getId();
            if (!primaryImageMap.containsKey(pId)) {
                String imageUrl = productImageRepository.findFirstByProductIdAndPrimaryTrue(pId)
                        .map(ProductImage::getUrl)
                        .orElse(null);
                primaryImageMap.put(pId, imageUrl);
            }
        }
        return CartMapper.toCartResponse(cart, primaryImageMap);
    }
}
