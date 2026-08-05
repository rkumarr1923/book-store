package com.bookstore.domain;

import com.bookstore.domain.enums.BookFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * A single book line item within a placed order.
 * Maps to the {@code order_items} table.
 *
 * Key design: title, coverImageUrl, format, and unitPrice are snapshotted at order placement
 * to ensure order history is accurate even if the live catalogue changes later.
 * {@code bookId} is retained to support the "Buy Again" feature.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseCreatedEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_items_order"))
    private Order order;

    /**
     * Reference to the source book — retained for "Buy Again".
     * Uses RESTRICT so deleting a book with associated order history is blocked.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_items_book"))
    private Book book;

    // ── Snapshot columns (immutable at order time) ────────────────────────────

    @NotBlank
    @Size(max = 500)
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Size(max = 500)
    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 20)
    private BookFormat format;

    @NotNull
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull
    @DecimalMin(value = "0.01", message = "Line total must be greater than 0")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;
}
