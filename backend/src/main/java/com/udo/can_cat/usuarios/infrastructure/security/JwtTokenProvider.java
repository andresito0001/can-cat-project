package com.udo.can_cat.usuarios.infrastructure.security;

import com.udo.can_cat.usuarios.domain.entity.Usuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${jwt.password-reset-expiration-ms:86400000}")
    private long passwordResetExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Usuario usuario, String rol) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(usuario.getId().toString())
                .claim("correo", usuario.getCorreoElectronico())
                .claim("rol", rol)
                .claim("idUsuario", usuario.getId())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Genera token para recuperación de contraseña
     */
    public String generatePasswordResetToken(Usuario usuario) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + passwordResetExpirationMs);

        return Jwts.builder()
                .subject(usuario.getId().toString())
                .claim("tipo", "PASSWORD_RESET")
                .claim("correo", usuario.getCorreoElectronico())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Valida si el token es válido
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            logger.error("Firma JWT inválida: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Token JWT malformado: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Claims vacíos: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Obtiene el ID del usuario desde el token
     */
    public Integer getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Integer.parseInt(claims.getSubject());
    }

    /**
     * Obtiene el rol desde el token
     */
    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("rol", String.class);
    }

    /**
     * Obtiene el correo desde el token
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("correo", String.class);
    }

    /**
     * Verifica si el token es de recuperación de contraseña
     */
    public boolean isPasswordResetToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return "PASSWORD_RESET".equals(claims.get("tipo", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retorna la expiración en segundos
     */
    public long getExpirationInSeconds() {
        return jwtExpirationMs / 1000;
    }
}