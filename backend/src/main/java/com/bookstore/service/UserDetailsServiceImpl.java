package com.bookstore.service;

import com.bookstore.domain.User;
import com.bookstore.repository.UserRepository;
import com.bookstore.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security {@link UserDetailsService} implementation that resolves a user
 * by their email address (the login identifier used throughout this application).
 *
 * <p>Delegates persistence to the existing {@link UserRepository}.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load a user by email address.
     *
     * @param email the user's email (passed as the {@code username} parameter by Spring Security)
     * @return a populated {@link UserPrincipal}
     * @throws UsernameNotFoundException if no account exists for the given email
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));
        return UserPrincipal.from(user);
    }
}
