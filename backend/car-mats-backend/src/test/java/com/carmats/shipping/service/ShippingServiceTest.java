package com.carmats.shipping.service;

import com.carmats.common.exception.BusinessException;
import com.carmats.order.entity.Order;
import com.carmats.order.entity.OrderAddress;
import com.carmats.order.entity.OrderAddressType;
import com.carmats.order.entity.OrderStatus;
import com.carmats.order.repository.OrderRepository;
import com.carmats.shipping.dto.request.CreateShipmentRequest;
import com.carmats.shipping.dto.request.UpdateShipmentStatusRequest;
import com.carmats.shipping.dto.response.ShipmentResponse;
import com.carmats.shipping.entity.Shipment;
import com.carmats.shipping.entity.ShipmentStatus;
import com.carmats.shipping.entity.ShippingCarrier;
import com.carmats.shipping.repository.ShipmentRepository;
import com.carmats.shipping.repository.ShipmentTrackingEventRepository;
import com.carmats.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentTrackingEventRepository shipmentTrackingEventRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ShippingService shippingService;

    private Order testOrder;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        User testUser = new User("customer@carmats.local", "hash", "Ahmet", "Yılmaz", "+905551112233");
        ReflectionTestUtils.setField(testUser, "id", UUID.randomUUID());

        testOrder = new Order(
                "ORD-20260830-123456",
                testUser,
                null, null, null, null,
                OrderStatus.PAID,
                new BigDecimal("2000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("2000.00"),
                null
        );
        ReflectionTestUtils.setField(testOrder, "id", orderId);

        OrderAddress address = new OrderAddress(
                testOrder,
                OrderAddressType.DELIVERY,
                "Ahmet",
                "Yılmaz",
                "+905551112233",
                "İstanbul",
                "Kadıköy",
                "Moda",
                "Caferağa Mah. No:5",
                "34710",
                null, null, null
        );
        testOrder.getAddresses().add(address);
    }

    @Test
    @DisplayName("Should create shipment successfully and update order status to SHIPPED")
    void shouldCreateShipmentSuccessfully() {
        CreateShipmentRequest request = new CreateShipmentRequest(
                orderId,
                ShippingCarrier.YURTICI,
                null,
                null
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(shipmentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(shipmentRepository.existsByTrackingNumber(anyString())).thenReturn(false);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> {
            Shipment s = inv.getArgument(0);
            ReflectionTestUtils.setField(s, "id", UUID.randomUUID());
            return s;
        });

        ShipmentResponse response = shippingService.createShipment(request, "admin@carmats.local");

        assertThat(response).isNotNull();
        assertThat(response.carrier()).isEqualTo(ShippingCarrier.YURTICI);
        assertThat(response.status()).isEqualTo(ShipmentStatus.CREATED);
        assertThat(response.trackingNumber()).startsWith("TRK-");
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        verify(orderRepository).save(testOrder);
        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    @DisplayName("Should reject shipment creation if shipment already exists")
    void shouldRejectShipmentWhenAlreadyExists() {
        CreateShipmentRequest request = new CreateShipmentRequest(
                orderId,
                ShippingCarrier.YURTICI,
                null,
                null
        );

        Shipment existing = new Shipment(
                testOrder, ShippingCarrier.YURTICI, "TRK-EXISTING",
                "Ahmet Yılmaz", "+905551112233", "Adres", "İstanbul", "Kadıköy", null, null
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(shipmentRepository.findByOrderId(orderId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> shippingService.createShipment(request, "admin@carmats.local"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("zaten bir kargo kaydı mevcuttur");

        verify(shipmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update shipment status and sync DELIVERED status to order")
    void shouldUpdateShipmentStatusAndSyncOrderDelivered() {
        Shipment shipment = new Shipment(
                testOrder, ShippingCarrier.YURTICI, "TRK-123456",
                "Ahmet Yılmaz", "+905551112233", "Adres", "İstanbul", "Kadıköy", null, null
        );
        UUID shipmentId = UUID.randomUUID();
        ReflectionTestUtils.setField(shipment, "id", shipmentId);

        UpdateShipmentStatusRequest request = new UpdateShipmentStatusRequest(
                ShipmentStatus.DELIVERED, "Kadıköy Dağıtım", "Alıcıya teslim edildi."
        );

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        ShipmentResponse response = shippingService.updateShipmentStatus(shipmentId, request, "admin@carmats.local");

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        verify(orderRepository).save(testOrder);
    }
}
