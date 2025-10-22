package com.saferoute.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Clave segura de 256 bits para JWT (cargada desde variables de entorno)
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration; // 1 día por defecto

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

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
                .signWith(getSecretKey())
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
                .signWith(getSecretKey())
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
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
                .setSigningKey(getSecretKey())
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
                .setSigningKey(getSecretKey())
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
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
