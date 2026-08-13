package com.bookstore.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A user's persistent saved-for-later book collection.
 * Maps to the {@code wishlists} table.
 * One wishlist per user is enforced by a unique constraint on {@code user_id}.
 */
@Entity
@Table(
        name = "wishlists",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_wishlists_user_id",
                columnNames = "user_id"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist extends BaseCreatedEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_wishlists_user"))
    private User user;

    // ── Relationships ─────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WishlistItem> items = new ArrayList<>();
}
