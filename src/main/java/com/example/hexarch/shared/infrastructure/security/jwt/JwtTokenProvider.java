package com.example.hexarch.shared.infrastructure.security.jwt;

import com.example.hexarch.shared.domain.security.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT TOKEN PROVIDER - Componente para generar y validar tokens JWT
 *
 * RESPONSABILIDADES:
 * - Generar tokens JWT con claims (subject, roles, expiration)
 * - Validar tokens JWT (firma, expiration, formato)
 * - Extraer información del token (username, roles)
 *
 * ARQUITECTURA HEXAGONAL:
 * - Está en infrastructure porque JWT es un detalle técnico
 * - Está en "shared" porque es transversal a toda la aplicación
 * - NO está en domain porque JWT no es concepto de negocio
 *
 * JWT STRUCTURE:
 * ```
 * eyJhbGciOiJIUzI1NiJ9.           ← Header (algoritmo)
 * eyJzdWIiOiJ1c2VyMSIsInJvbGVzIjpbIkFETUlOIl19.  ← Payload (datos)
 * SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c   ← Signature (firma)
 * ```
 *
 * CLAIMS INCLUIDOS:
 * - sub (subject): username del usuario
 * - roles: lista de roles [" ADMIN", "MANAGER"]
 * - iat (issued at): timestamp de creación
 * - exp (expiration): timestamp de expiración
 *
 * ALGORITMO:
 * - HMAC-SHA256 (HS256): Firma simétrica con secret key
 * - Alternativa: RS256 (asimétrico con public/private keys)
 *
 * NOTA IMPORTANTE:
 * En este proyecto educativo, este componente incluye generación de tokens
 * PERO en un escenario real de microservicios:
 * - Auth Service: genera tokens (login endpoint)
 * - Otros Services: solo validan tokens (este componente)
 * - Este proyecto puede usarse como "Auth Service" o "Resource Service"
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    /**
     * Inicializa la clave secreta después de inyectar dependencias
     *
     * IMPORTANTE:
     * - La clave debe tener mínimo 256 bits para HS256
     * - Si el secret es muy corto, lanza WeakKeyException
     */
    @PostConstruct
    public void init() {
        // Convertir el secret string a SecretKey
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT Secret Key initialized successfully");
    }

    /**
     * Genera un token JWT para un usuario con sus roles
     *
     * @param username Username del usuario (será el "subject" del token)
     * @param roles    Lista de roles del usuario
     * @return Token JWT como String
     */
    public String generateToken(String username, List<Role> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        // Convertir roles a strings
        List<String> roleNames = roles.stream()
                .map(Role::name)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(username)                           // Usuario
                .claim("roles", roleNames)                   // Roles como claim custom
                .issuedAt(now)                               // Timestamp de creación
                .expiration(expiryDate)                      // Timestamp de expiración
                .signWith(secretKey, Jwts.SIG.HS256)        // Firma con HS256
                .compact();
    }

    /**
     * Extrae el username (subject) del token JWT
     *
     * @param token Token JWT
     * @return Username del usuario
     * @throws JwtException Si el token es inválido
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * Extrae los roles del token JWT
     *
     * @param token Token JWT
     * @return Lista de roles
     * @throws JwtException Si el token es inválido
     */
    @SuppressWarnings("unchecked")
    public List<Role> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);

        // Obtener claim "roles" como lista de strings
        List<String> roleNames = claims.get("roles", List.class);

        if (roleNames == null || roleNames.isEmpty()) {
            log.warn("Token does not contain roles claim");
            return List.of();
        }

        // Convertir strings a enum Role
        return roleNames.stream()
                .map(Role::fromString)
                .collect(Collectors.toList());
    }

    /**
     * Valida si un token JWT es válido
     *
     * VALIDACIONES:
     * 1. Firma correcta (secretKey)
     * 2. No expirado
     * 3. Formato correcto
     *
     * @param token Token JWT
     * @return true si es válido, false si no
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token format: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("JWT token is expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("JWT token is unsupported: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        } catch (io.jsonwebtoken.security.SecurityException ex) {
            log.error("JWT signature validation failed: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Extrae todos los claims del token JWT
     *
     * @param token Token JWT
     * @return Claims del token
     * @throws JwtException Si el token es inválido
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)           // Validar firma con secretKey
                .build()
                .parseSignedClaims(token)        // Parsear y validar
                .getPayload();                   // Obtener claims (payload)
    }
}
