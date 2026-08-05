package com.bookstore.repository;

import com.bookstore.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Persistence operations for {@link User}.
 *
 * Query methods cover:
 * - Authentication (lookup by email or phone number)
 * - Duplicate-check on registration (existsBy email / phone)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ── Authentication ────────────────────────────────────────────────────────

    /** Find a user by email address (case-sensitive). Used for login and JWT loading. */
    Optional<User> findByEmail(String email);

    /** Find a user by phone number. Used for phone-based login. */
    Optional<User> findByPhoneNumber(String phoneNumber);

    // ── Registration duplicate checks ─────────────────────────────────────────

    /** Returns true if the email is already registered. */
    boolean existsByEmail(String email);

    /** Returns true if the phone number is already registered. */
    boolean existsByPhoneNumber(String phoneNumber);
}
