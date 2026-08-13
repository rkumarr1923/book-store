package com.bookstore.repository;

import com.bookstore.domain.Payment;
import com.bookstore.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Persistence operations for {@link Payment}.
 *
 * Query methods cover:
 * - Payment lookup by order ID (one-to-one relationship)
 * - Payment status check for a given order
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // ── Payment lookup ────────────────────────────────────────────────────────

    /** Find the payment record associated with a given order. */
    Optional<Payment> findByOrderId(Long orderId);

    /** Find the payment record associated with a given order and validate its status. */
    Optional<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);
}
