package com.carmats.payment.service;

import com.carmats.common.exception.BusinessException;
import com.carmats.common.exception.NotFoundException;
import com.carmats.order.entity.Order;
import com.carmats.order.entity.OrderStatus;
import com.carmats.order.repository.OrderRepository;
import com.carmats.payment.dto.request.ProcessPaymentRequest;
import com.carmats.payment.dto.response.PaymentResponse;
import com.carmats.payment.entity.Payment;
import com.carmats.payment.entity.PaymentMethod;
import com.carmats.payment.entity.PaymentProviderType;
import com.carmats.payment.entity.PaymentStatus;
import com.carmats.payment.provider.MockPaymentProvider;
import com.carmats.payment.repository.PaymentRepository;
import com.carmats.payment.repository.PaymentTransactionRepository;
import com.carmats.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private OrderRepository orderRepository;

    @Spy
    private MockPaymentProvider mockPaymentProvider = new MockPaymentProvider();

    @InjectMocks
    private PaymentService paymentService;

    private UUID userId;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        User testUser = new User("customer@carmats.local", "hash", "Ahmet", "Yılmaz", "+905551112233");
        ReflectionTestUtils.setField(testUser, "id", userId);

        testOrder = new Order(
                "ORD-20260830-ABCDEF",
                testUser,
                null, null, null, null,
                OrderStatus.PENDING_PAYMENT,
                new BigDecimal("2000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("2000.00"),
                "Customer Note"
        );
        ReflectionTestUtils.setField(testOrder, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("Should process payment successfully with valid card")
    void shouldProcessPaymentSuccessfully() {
        ProcessPaymentRequest request = new ProcessPaymentRequest(
                testOrder.getOrderNumber(),
                PaymentMethod.CREDIT_CARD,
                "Ahmet Yılmaz",
                "5528790000000001",
                "12",
                "2028",
                "123",
                1
        );

        when(orderRepository.findByOrderNumberAndUserId(testOrder.getOrderNumber(), userId))
                .thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(testOrder.getId()))
                .thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment p = invocation.getArgument(0);
                    ReflectionTestUtils.setField(p, "id", UUID.randomUUID());
                    return p;
                });

        PaymentResponse response = paymentService.processPayment(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.cardLastFour()).isEqualTo("0001");
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository).save(testOrder);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should fail payment and throw exception when card is declined")
    void shouldFailPaymentWhenCardDeclined() {
        ProcessPaymentRequest request = new ProcessPaymentRequest(
                testOrder.getOrderNumber(),
                PaymentMethod.CREDIT_CARD,
                "Ahmet Yılmaz",
                "5528790000000002", // Ends with 0002 -> triggers mock decline
                "12",
                "2028",
                "123",
                1
        );

        when(orderRepository.findByOrderNumberAndUserId(testOrder.getOrderNumber(), userId))
                .thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(testOrder.getId()))
                .thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> paymentService.processPayment(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Kart reddedildi");

        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should reject payment when order is already paid")
    void shouldRejectPaymentWhenOrderAlreadyPaid() {
        testOrder.setStatus(OrderStatus.PAID);

        ProcessPaymentRequest request = new ProcessPaymentRequest(
                testOrder.getOrderNumber(),
                PaymentMethod.CREDIT_CARD,
                "Ahmet Yılmaz",
                "5528790000000001",
                "12",
                "2028",
                "123",
                1
        );

        when(orderRepository.findByOrderNumberAndUserId(testOrder.getOrderNumber(), userId))
                .thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> paymentService.processPayment(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("zaten tamamlanmıştır");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject payment when order is cancelled")
    void shouldRejectPaymentWhenOrderCancelled() {
        testOrder.setStatus(OrderStatus.CANCELLED);

        ProcessPaymentRequest request = new ProcessPaymentRequest(
                testOrder.getOrderNumber(),
                PaymentMethod.CREDIT_CARD,
                "Ahmet Yılmaz",
                "5528790000000001",
                "12",
                "2028",
                "123",
                1
        );

        when(orderRepository.findByOrderNumberAndUserId(testOrder.getOrderNumber(), userId))
                .thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> paymentService.processPayment(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("İptal edilmiş");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should process refund successfully")
    void shouldProcessRefundSuccessfully() {
        Payment payment = new Payment(
                testOrder,
                PaymentMethod.CREDIT_CARD,
                PaymentProviderType.MOCK,
                testOrder.getGrandTotal(),
                "TRY",
                1
        );
        payment.markSuccess("MOCK-PAY-123", "552879", "0001", "CREDIT_CARD", "MasterCard");
        ReflectionTestUtils.setField(payment, "id", UUID.randomUUID());

        when(paymentRepository.findByOrderId(testOrder.getId()))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.processRefund(testOrder.getId(), null, "Müşteri isteği", "admin@carmats.local");

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.REFUNDED);
        verify(orderRepository).save(testOrder);
    }
}
