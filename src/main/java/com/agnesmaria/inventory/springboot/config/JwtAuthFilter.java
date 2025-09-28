package com.agnesmaria.inventory.springboot.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import com.agnesmaria.inventory.springboot.service.JwtService;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_HEADER = "Authorization";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Get Authorization header
        final String authHeader = request.getHeader(AUTH_HEADER);

        // 2. Skip if no Bearer token
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("No Bearer token found in request");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. Extract JWT token
            final String jwt = authHeader.substring(BEARER_PREFIX.length());
            log.debug("Extracted JWT token: {}", jwt);

            // 4. Extract username from JWT
            final String username = jwtService.extractUsername(jwt);

            // 5. Validate token and set authentication
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.debug("Authenticating user: {}", username);

                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    log.debug("Token is valid for user: {}", username);

                    // Extract roles from the token
                    List<String> roles = jwtService.extractRoles(jwt);
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                    log.debug("Extracted roles from token: {}", authorities);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        authorities // Gunakan authorities dari token
                    );

                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Authenticated user: {} with roles: {}", username, authorities);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process JWT token: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }

        // 6. Continue filter chain
        filterChain.doFilter(request, response);
    }
}