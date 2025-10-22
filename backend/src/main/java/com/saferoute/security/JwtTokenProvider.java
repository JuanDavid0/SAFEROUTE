package com.saferoute.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Clave segura de 256 bits para JWT
    private final String jwtSecret = "SafeRouteSecretKeyForJWTThatMustBeAtLeast256BitsLongToBeSecure";
    private final long jwtExpiration = 86400000; // 1 día
    private final SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());

    public String generateToken(Authentication authentication) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("roles", userPrincipal.getAuthorities().toString())
                .claim("userId", userPrincipal.getId())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Genera un token JWT temporal para autenticación OTP
     * Este token tiene una duración más corta y solo permite operaciones limitadas
     */
    public String generateOtpToken(String cedula) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000); // 1 hora (más corto que el token normal)

        return Jwts.builder()
                .setSubject(cedula)
                .claim("roles", "[ROLE_OTP_VERIFIED]")
                .claim("tokenType", "OTP")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Extrae la cédula del token JWT (para tokens OTP, el subject es la cédula)
     */
    public String getCedulaFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Extrae el tipo de token del JWT
     */
    public String getTokenTypeFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("tokenType", String.class);
    }

    /**
     * Verifica si el token es un token OTP
     */
    public boolean isOtpToken(String token) {
        try {
            String tokenType = getTokenTypeFromJWT(token);
            return "OTP".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene todos los claims del token
     */
    public Claims getClaimsFromJWT(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
