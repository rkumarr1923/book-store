package com.bookstore.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A book publisher.
 * Maps to the {@code publishers} table.
 */
@Entity
@Table(
        name = "publishers",
        uniqueConstraints = @UniqueConstraint(name = "uk_publishers_name", columnNames = "name")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Publisher extends BaseCreatedEntity {

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @Size(max = 500)
    @Column(name = "website", length = 500)
    private String website;

    // ── Relationships ─────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "publisher", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Book> books = new ArrayList<>();
}
