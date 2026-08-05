package com.bookstore.service;

import com.bookstore.common.exception.DuplicateResourceException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.domain.User;
import com.bookstore.dto.request.UpdateProfileRequest;
import com.bookstore.dto.response.UserProfileResponse;
import com.bookstore.mapper.UserMapper;
import com.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user profile management.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper     userMapper;

    // ── Get Profile ────────────────────────────────────────────────────────────

    /**
     * Return the authenticated user's profile.
     *
     * @param userId the authenticated user's ID
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return userMapper.toProfileResponse(user);
    }

    // ── Update Profile ─────────────────────────────────────────────────────────

    /**
     * Update the authenticated user's profile fields.
     *
     * @param userId  the authenticated user's ID
     * @param request updated fields
     */
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Phone number uniqueness check (only if changing)
        if (request.getPhoneNumber() != null
                && !request.getPhoneNumber().isBlank()
                && !request.getPhoneNumber().equals(user.getPhoneNumber())
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException(
                    "Phone number '" + request.getPhoneNumber() + "' is already in use.");
        }

        userMapper.updateEntity(request, user);
        user = userRepository.save(user);
        return userMapper.toProfileResponse(user);
    }
}
