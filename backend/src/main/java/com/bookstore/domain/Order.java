package com.bookstore.domain;

import com.bookstore.domain.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A confirmed, paid customer purchase.
 * Maps to the {@code orders} table.
 *
 * {@code deliveryAddressSnapshot} stores the delivery address as JSONB at order placement
 * to preserve historical accuracy independently of any future address changes.
 */
@Entity
@Table(
        name = "orders",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_orders_order_number",
                columnNames = "order_number"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @NotBlank
    @Size(max = 30)
    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String orderNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_orders_user"))
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PLACED;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    @ColumnDefault("0.00")
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "delivery_charge", nullable = false, precision = 10, scale = 2)
    @ColumnDefault("0.00")
    @Builder.Default
    private BigDecimal deliveryCharge = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    @ColumnDefault("0.00")
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id",
            foreignKey = @ForeignKey(name = "fk_orders_coupon"))
    private Coupon coupon;

    /**
     * Immutable JSON snapshot of the delivery address at order placement.
     * Stored as PostgreSQL {@code jsonb}. Uses a {@link Map} to hold arbitrary address fields.
     */
    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "delivery_address_snapshot", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> deliveryAddressSnapshot;

    @NotNull
    @Column(name = "placed_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant placedAt = Instant.now();

    @NotNull
    @Column(name = "estimated_delivery_date", nullable = false)
    private LocalDate estimatedDeliveryDate;

    // ── Relationships ─────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;
}
