package com.bookstore.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * A single book entry (with quantity) inside a shopping cart.
 * Maps to the {@code cart_items} table.
 * The unique constraint {@code (cart_id, book_id)} prevents duplicate entries.
 */
@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cart_items_cart_book",
                columnNames = {"cart_id", "book_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem extends BaseCreatedEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_items_cart"))
    private Cart cart;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_items_book"))
    private Book book;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;
}
