package com.bookstore.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * A book genre / category (e.g., Romance, Mystery).
 * Maps to the {@code genres} table.
 * The {@code slug} is the URL-safe routing identifier used by the frontend sidebar.
 */
@Entity
@Table(
        name = "genres",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_genres_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_genres_slug", columnNames = "slug")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Genre extends BaseCreatedEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank
    @Size(max = 100)
    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;
}
