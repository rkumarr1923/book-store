package com.bookstore.domain;

import com.bookstore.domain.enums.DiscountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A discount coupon pre-configured by an admin.
 * Maps to the {@code coupons} table.
 */
@Entity
@Table(
        name = "coupons",
        uniqueConstraints = @UniqueConstraint(name = "uk_coupons_code", columnNames = "code")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends BaseCreatedEntity {

    @NotBlank
    @Size(max = 50)
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @NotNull
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    @Digits(integer = 6, fraction = 2)
    @Column(name = "discount_value", nullable = false, precision = 8, scale = 2)
    private BigDecimal discountValue;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "min_order_value", nullable = false, precision = 10, scale = 2)
    @ColumnDefault("0.00")
    @Builder.Default
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
