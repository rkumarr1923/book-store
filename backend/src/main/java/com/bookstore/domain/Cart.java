package com.bookstore.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * An authenticated user's active shopping cart.
 * Maps to the {@code carts} table.
 * Unique constraint on {@code user_id} enforces one cart per user.
 */
@Entity
@Table(
        name = "carts",
        uniqueConstraints = @UniqueConstraint(name = "uk_carts_user_id", columnNames = "user_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_carts_user"))
    private User user;

    // ── Relationships ─────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
}
