package com.bookstore.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A book author.
 * Maps to the {@code authors} table.
 *
 * Relationships:
 * - One-to-Many: books
 */
@Entity
@Table(
        name = "authors",
        uniqueConstraints = @UniqueConstraint(name = "uk_authors_name", columnNames = "name")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author extends BaseCreatedEntity {

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;

    @Size(max = 500)
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    // ── Relationships ─────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Book> books = new ArrayList<>();
}
