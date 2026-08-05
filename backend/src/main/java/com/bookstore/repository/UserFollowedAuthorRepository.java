package com.bookstore.repository;

import com.bookstore.domain.UserFollowedAuthor;
import com.bookstore.domain.UserFollowedAuthorId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Persistence operations for {@link UserFollowedAuthor}.
 *
 * Query methods cover:
 * - Paginated list of followed authors for a user (My Writers page)
 * - Follow existence check (to prevent duplicate follows and to verify unfollow)
 * - Deletion by composite key fields (unfollow)
 */
@Repository
public interface UserFollowedAuthorRepository extends JpaRepository<UserFollowedAuthor, UserFollowedAuthorId> {

    // ── Followed authors listing ──────────────────────────────────────────────

    /**
     * Return a paginated list of followed-author relationships for a user,
     * ordered by follow date descending (most recently followed first).
     * Used for the "My Writers" page.
     */
    Page<UserFollowedAuthor> findByUserIdOrderByFollowedAtDesc(Long userId, Pageable pageable);

    // ── Follow existence check ────────────────────────────────────────────────

    /** Returns true if the user already follows the given author. */
    boolean existsByUserIdAndAuthorId(Long userId, Long authorId);

    // ── Unfollow ──────────────────────────────────────────────────────────────

    /**
     * Delete the follow relationship between a user and an author by their individual IDs.
     * Avoids needing to construct the composite key in the service layer.
     */
    void deleteByUserIdAndAuthorId(Long userId, Long authorId);

    // ── Recommendation support ────────────────────────────────────────────────

    /**
     * Return the count of authors a user follows.
     * Used to decide whether to serve personalised or fallback recommendations.
     */
    @Query("SELECT COUNT(ufa) FROM UserFollowedAuthor ufa WHERE ufa.user.id = :userId")
    long countFollowedAuthorsByUserId(@Param("userId") Long userId);
}
