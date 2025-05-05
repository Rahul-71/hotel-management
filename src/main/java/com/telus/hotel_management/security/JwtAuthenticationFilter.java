package com.telus.hotel_management.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtil;
    @Autowired
    private UserDetailsService userDetailsService;

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.debug("Starting JwtAuthenticationFilter for request: {}", request.getRequestURI());

        final String authHeader = request.getHeader("Authorization");
        log.debug("Authorization header: {}", authHeader);

        String username = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            log.debug("Extracted JWT token: {}", jwt);
            username = jwtUtil.extractUsername(jwt);
            log.debug("Extracted username from JWT token: {}", username);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.debug("Attempting to authenticate user with username: {}", username);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            log.debug("Loaded user details: {}", userDetails);

            if (jwtUtil.validateJwtToken(jwt)) {
//            if (jwtUtil.validateJwtToken(jwt, userDetails)) {
                log.debug("JWT token is valid for user: {}", username);
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                log.debug("Created authentication token: {}", authToken);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Set authentication in security context");
            } else {
                log.debug("JWT token is invalid for user: {}", username);
            }
        } else {
            log.debug("No authentication required or already authenticated");
        }

        log.debug("Continuing filter chain for request: {}", request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}