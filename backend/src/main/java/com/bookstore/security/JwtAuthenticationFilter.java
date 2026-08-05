package com.bookstore.security;

import com.bookstore.common.constants.AppConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that runs once per request and validates the JWT Bearer token.
 *
 * <p>Workflow:
 * <ol>
 *   <li>Extract the {@code Authorization: Bearer <token>} header.</li>
 *   <li>Validate the token using {@link JwtTokenProvider}.</li>
 *   <li>Load the {@link UserDetails} via {@link UserDetailsService}.</li>
 *   <li>Populate the {@link SecurityContextHolder} so downstream code can call
 *       {@code SecurityContextHolder.getContext().getAuthentication()}.</li>
 * </ol>
 * If the token is absent or invalid the filter simply passes the request along
 * without setting authentication — Spring Security will enforce access rules.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider    jwtTokenProvider;
    private final UserDetailsService  userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmailFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Authenticated user '{}' via JWT", email);
        }

        filterChain.doFilter(request, response);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Extract the raw JWT string from the {@code Authorization} header.
     *
     * @param request the incoming HTTP request
     * @return the token string or {@code null} if no valid Bearer header is present
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AppConstants.Security.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header)
                && header.startsWith(AppConstants.Security.BEARER_PREFIX)) {
            return header.substring(AppConstants.Security.BEARER_PREFIX.length());
        }
        return null;
    }
}
