package com.saferoute.config;

import com.saferoute.security.CustomUserDetailsService;
import com.saferoute.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Log para debugging
        logger.debug("Processing request: " + requestPath);

        // Skip filter for public endpoints (cualquier ruta que empiece con /auth)
        if (requestPath.startsWith("/auth") || requestPath.equals("/error")) {
            logger.debug("Skipping JWT filter for public endpoint: " + requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {

                // Verificar si es un token OTP
                if (tokenProvider.isOtpToken(jwt)) {
                    // Token OTP: el subject es la cédula del cliente
                    handleOtpToken(jwt, request);
                } else {
                    // Token normal: cargar UserDetails desde la BD
                    handleRegularToken(jwt, request);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Maneja tokens OTP donde el subject es la cédula del cliente
     */
    private void handleOtpToken(String jwt, HttpServletRequest request) {
        String cedula = tokenProvider.getCedulaFromJWT(jwt);
        Claims claims = tokenProvider.getClaimsFromJWT(jwt);

        // Obtener roles del token (ROLE_OTP_VERIFIED)
        // Puede ser String o List dependiendo de cómo se creó el token
        Object rolesObj = claims.get("roles");
        List<String> roles;

        if (rolesObj instanceof String roleStr) {
            // Si es un String único, convertirlo a lista
            roles = Collections.singletonList(roleStr);
        } else if (rolesObj instanceof List<?>) {
            // Si ya es una lista, usarla directamente
            @SuppressWarnings("unchecked")
            List<String> rolesList = (List<String>) rolesObj;
            roles = rolesList;
        } else {
            // Si es null u otro tipo, lista vacía
            roles = Collections.emptyList();
        }

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        // Crear autenticación con la cédula como principal
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                cedula, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        logger.debug("OTP token authenticated for cedula: " + cedula);
    }

    /**
     * Maneja tokens regulares cargando UserDetails desde la BD
     */
    private void handleRegularToken(String jwt, HttpServletRequest request) {
        String username = tokenProvider.getUsernameFromJWT(jwt);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        logger.debug("Regular token authenticated for username: " + username);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
