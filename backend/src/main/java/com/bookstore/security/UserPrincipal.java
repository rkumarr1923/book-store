package com.bookstore.security;

import com.bookstore.domain.User;
import com.bookstore.domain.enums.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security {@link UserDetails} implementation backed by the {@link User} entity.
 *
 * <p>The role stored in the {@code UserRole} enum is converted to a Spring
 * {@code ROLE_}-prefixed authority (e.g. {@code CUSTOMER} → {@code ROLE_CUSTOMER}).
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long   id;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final boolean active;

    private UserPrincipal(User user) {
        this.id           = user.getId();
        this.email        = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.role         = user.getRole();
        this.active       = user.isActive();
    }

    /** Factory method — creates a {@code UserPrincipal} from a {@link User} entity. */
    public static UserPrincipal from(User user) {
        return new UserPrincipal(user);
    }

    // ── UserDetails ───────────────────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /** Returns the hashed password stored in the database. */
    @Override
    public String getPassword() {
        return passwordHash;
    }

    /** Returns the email address used as the login identifier. */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
