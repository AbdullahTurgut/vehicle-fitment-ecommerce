package com.carmats.payment.repository;

import com.carmats.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    List<PaymentTransaction> findAllByPaymentIdOrderByCreatedAtDesc(UUID paymentId);
}
