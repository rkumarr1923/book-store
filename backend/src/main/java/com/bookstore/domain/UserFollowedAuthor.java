package com.bookstore.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;

/**
 * Join entity for the many-to-many follow relationship between users and authors.
 * Maps to the {@code user_followed_authors} table.
 * Uses a composite primary key {@link UserFollowedAuthorId}.
 *
 * {@code followedAt} records when the follow action took place —
 * used to order the My Writers list by recency.
 */
@Entity
@Table(name = "user_followed_authors")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFollowedAuthor implements Serializable {

    @EmbeddedId
    private UserFollowedAuthorId id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_followed_authors_user"))
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("authorId")
    @JoinColumn(name = "author_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_followed_authors_author"))
    private Author author;

    @CreatedDate
    @Column(name = "followed_at", nullable = false, updatable = false)
    private Instant followedAt;
}
