package com.carmats.checkout.service;

import com.carmats.cart.entity.Cart;
import com.carmats.cart.entity.CartItem;
import com.carmats.cart.service.CartService;
import com.carmats.catalog.entity.Product;
import com.carmats.catalog.entity.ProductImage;
import com.carmats.catalog.entity.ProductStatus;
import com.carmats.catalog.repository.ProductImageRepository;
import com.carmats.checkout.dto.request.CheckoutPreviewRequest;
import com.carmats.checkout.dto.response.CheckoutItemDto;
import com.carmats.checkout.dto.response.CheckoutSummaryResponse;
import com.carmats.checkout.dto.response.CheckoutValidationResponse;
import com.carmats.common.exception.BusinessException;
import com.carmats.common.exception.NotFoundException;
import com.carmats.user.dto.response.AddressResponse;
import com.carmats.user.entity.Address;
import com.carmats.user.mapper.AddressMapper;
import com.carmats.user.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CheckoutService {

    public static final BigDecimal DEFAULT_SHIPPING_FEE = new BigDecimal("75.00");
    public static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("1000.00");

    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final ProductImageRepository productImageRepository;

    public CheckoutService(
            CartService cartService,
            AddressRepository addressRepository,
            ProductImageRepository productImageRepository
    ) {
        this.cartService = cartService;
        this.addressRepository = addressRepository;
        this.productImageRepository = productImageRepository;
    }

    public CheckoutSummaryResponse getCheckoutSummary(
            UUID userId,
            String guestToken,
            CheckoutPreviewRequest request
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

        List<CheckoutItemDto> items = new ArrayList<>();
        int totalQuantity = 0;
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();

            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new BusinessException(
                        "PRODUCT_NOT_ACTIVE",
                        "Sepetinizdeki '" + product.getName() + "' ürünü satışta değildir."
                );
            }

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BusinessException(
                        "INSUFFICIENT_STOCK",
                        "Sepetinizdeki '" + product.getName() + "' ürünü için yetersiz stok. Mevcut: " + product.getStockQuantity()
                );
            }

            String primaryImageUrl = productImageRepository.findFirstByProductIdAndPrimaryTrue(product.getId())
                    .map(ProductImage::getUrl)
                    .orElse(null);

            String variantName = item.getVehicleVariant() != null
                    ? item.getVehicleVariant().getName()
                    : null;

            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            totalQuantity += item.getQuantity();

            items.add(new CheckoutItemDto(
                    product.getId(),
                    product.getName(),
                    product.getSlug(),
                    product.getSku(),
                    primaryImageUrl,
                    item.getVehicleVariant() != null ? item.getVehicleVariant().getId() : null,
                    variantName,
                    item.getQuantity(),
                    item.getUnitPrice(),
                    lineTotal
            ));
        }

        boolean freeShippingApplied = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0;
        BigDecimal shippingFee = freeShippingApplied ? BigDecimal.ZERO : DEFAULT_SHIPPING_FEE;
        BigDecimal discountTotal = BigDecimal.ZERO;
        BigDecimal grandTotal = subtotal.add(shippingFee).subtract(discountTotal);

        AddressResponse deliveryAddress = null;
        AddressResponse billingAddress = null;

        if (userId != null) {
            List<Address> userAddresses = addressRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

            if (request != null && request.deliveryAddressId() != null) {
                deliveryAddress = userAddresses.stream()
                        .filter(a -> a.getId().equals(request.deliveryAddressId()))
                        .findFirst()
                        .map(AddressMapper::toAddressResponse)
                        .orElseThrow(() -> new NotFoundException("ADDRESS_NOT_FOUND", "Teslimat adresi bulunamadı."));
            } else {
                deliveryAddress = userAddresses.stream()
                        .filter(Address::isDefaultDelivery)
                        .findFirst()
                        .or(() -> userAddresses.stream().findFirst())
                        .map(AddressMapper::toAddressResponse)
                        .orElse(null);
            }

            AddressResponse finalDeliveryAddress = deliveryAddress;
            if (request != null && request.billingAddressId() != null) {
                billingAddress = userAddresses.stream()
                        .filter(a -> a.getId().equals(request.billingAddressId()))
                        .findFirst()
                        .map(AddressMapper::toAddressResponse)
                        .orElseThrow(() -> new NotFoundException("ADDRESS_NOT_FOUND", "Fatura adresi bulunamadı."));
            } else {
                billingAddress = userAddresses.stream()
                        .filter(Address::isDefaultBilling)
                        .findFirst()
                        .or(() -> Optional.ofNullable(finalDeliveryAddress)
                                .flatMap(da -> userAddresses.stream().filter(a -> a.getId().equals(da.id())).findFirst()))
                        .map(AddressMapper::toAddressResponse)
                        .orElse(finalDeliveryAddress);
            }
        }

        return new CheckoutSummaryResponse(
                items,
                totalQuantity,
                subtotal,
                shippingFee,
                freeShippingApplied,
                FREE_SHIPPING_THRESHOLD,
                discountTotal,
                grandTotal,
                deliveryAddress,
                billingAddress
        );
    }

    public CheckoutValidationResponse validateCheckout(
            UUID userId,
            String guestToken,
            CheckoutPreviewRequest request
    ) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        String effectiveGuestToken = (request != null && request.guestToken() != null)
                ? request.guestToken()
                : guestToken;

        Cart cart = cartService.getOrCreateCart(userId, effectiveGuestToken);

        if (cart.getItems().isEmpty()) {
            errors.add("Sepetinizde ürün bulunmamaktadır.");
            return CheckoutValidationResponse.fail(errors);
        }

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.getStatus() != ProductStatus.ACTIVE) {
                errors.add("'" + product.getName() + "' ürünü satışta değildir.");
            } else if (product.getStockQuantity() < item.getQuantity()) {
                errors.add("'" + product.getName() + "' için yetersiz stok. Mevcut stok: " + product.getStockQuantity());
            }
        }

        if (userId != null && request != null) {
            if (request.deliveryAddressId() != null
                    && addressRepository.findByIdAndUserId(request.deliveryAddressId(), userId).isEmpty()) {
                errors.add("Seçilen teslimat adresi bulunamadı.");
            }
            if (request.billingAddressId() != null
                    && addressRepository.findByIdAndUserId(request.billingAddressId(), userId).isEmpty()) {
                errors.add("Seçilen fatura adresi bulunamadı.");
            }
        }

        if (!errors.isEmpty()) {
            return CheckoutValidationResponse.fail(errors);
        }

        return CheckoutValidationResponse.ok();
    }
}
