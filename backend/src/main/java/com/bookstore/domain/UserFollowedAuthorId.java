package com.bookstore.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * Composite primary key class for {@link UserFollowedAuthor}.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserFollowedAuthorId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "author_id")
    private Long authorId;
}
