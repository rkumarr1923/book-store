package com.bookstore.domain;

import com.bookstore.domain.enums.PaymentMethod;
import com.bookstore.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A payment transaction record linked one-to-one with an order.
 * Maps to the {@code payments} table.
 *
 * Phase 1: {@code transactionReference} is null (simulated payment).
 * Phase 2: will hold the Razorpay/PayU gateway transaction ID.
 */
@Entity
@Table(
        name = "payments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payments_order_id",
                columnNames = "order_id"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseCreatedEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_payments_order"))
    private Order order;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @NotNull
    @DecimalMin(value = "0.01", message = "Payable amount must be greater than 0")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "payable_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal payableAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Size(max = 255)
    @Column(name = "transaction_reference", length = 255)
    private String transactionReference;

    @Column(name = "paid_at")
    private Instant paidAt;
}
