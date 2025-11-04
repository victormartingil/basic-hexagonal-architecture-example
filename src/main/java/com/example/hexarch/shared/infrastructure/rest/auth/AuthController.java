package com.example.hexarch.shared.infrastructure.rest.auth;

import com.example.hexarch.shared.domain.security.Role;
import com.example.hexarch.shared.infrastructure.rest.auth.dto.LoginRequest;
import com.example.hexarch.shared.infrastructure.rest.auth.dto.LoginResponse;
import com.example.hexarch.shared.infrastructure.security.jwt.JwtProperties;
import com.example.hexarch.shared.infrastructure.security.jwt.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AUTH CONTROLLER - Endpoint de autenticación
 *
 * RESPONSABILIDAD:
 * - Generar tokens JWT para testing de la API
 *
 * ⚠️ NOTA EDUCATIVA IMPORTANTE:
 * Este endpoint está SIMPLIFICADO para propósitos educativos y testing rápido.
 *
 * EN ESTE PROYECTO (educativo):
 * - NO valida credenciales contra base de datos
 * - Genera JWT solo con username + role proporcionados
 * - Útil para probar la API sin configurar usuarios
 * - Permite testing rápido del sistema de autenticación
 *
 * EN PRODUCCIÓN (proyecto real):
 * - SIEMPRE valida username/password contra base de datos
 * - Usa Spring Security UserDetailsService
 * - Implementa rate limiting (prevenir brute force)
 * - Implementa account lockout después de X intentos fallidos
 * - Usa OAuth2/OIDC (Keycloak, Auth0, Okta) si es posible
 * - Almacena passwords con BCrypt/Argon2
 * - Implementa refresh tokens
 * - Implementa 2FA/MFA si es necesario
 *
 * ARQUITECTURA HEXAGONAL:
 * - Este controller está en "shared/infrastructure" porque:
 *   - La autenticación es transversal a toda la aplicación
 *   - JWT es un detalle de infraestructura (no concepto de dominio)
 *
 * ENDPOINTS:
 * - POST /api/v1/auth/login - Generar JWT token
 *
 * EJEMPLO DE USO:
 * ```bash
 * # 1. Generar token
 * curl -X POST http://localhost:8080/api/v1/auth/login \
 *   -H "Content-Type: application/json" \
 *   -d '{"username": "johndoe", "role": "ADMIN"}'
 *
 * # Respuesta:
 * {
 *   "token": "eyJhbGciOiJIUzI1NiJ9...",
 *   "type": "Bearer",
 *   "username": "johndoe",
 *   "roles": ["ADMIN"],
 *   "expiresIn": 86400000
 * }
 *
 * # 2. Usar token en requests
 * curl -X POST http://localhost:8080/api/v1/users \
 *   -H "Content-Type: application/json" \
 *   -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
 *   -d '{"username": "newuser", "email": "user@example.com"}'
 * ```
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    /**
     * POST /api/v1/auth/login - Generar JWT token
     *
     * FLUJO:
     * 1. Recibe username + role en el body
     * 2. Valida que el role existe (ADMIN, MANAGER, VIEWER, SUPPLIER)
     * 3. Genera JWT token con el username y role
     * 4. Retorna token + información adicional
     *
     * ROLES DISPONIBLES:
     * - ADMIN: Acceso total (crear, leer, actualizar, eliminar)
     * - MANAGER: Gestión de usuarios (crear, leer, actualizar)
     * - VIEWER: Solo lectura (leer)
     * - SUPPLIER: Crear y leer sus propios recursos
     *
     * @param request Credenciales (username + role)
     * @return Token JWT + información del usuario
     * @throws IllegalArgumentException Si el role no es válido
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        log.info("Login request for username: {} with role: {}", request.username(), request.role());

        // Validar que el role exista
        Role role;
        try {
            role = Role.valueOf(request.role().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role attempted: {}", request.role());
            throw new IllegalArgumentException(
                    String.format("Role inválido: %s. Roles válidos: ADMIN, MANAGER, VIEWER, SUPPLIER",
                            request.role())
            );
        }

        // Generar token JWT
        String token = jwtTokenProvider.generateToken(
                request.username(),
                List.of(role)
        );

        log.info("JWT token generated successfully for user: {}", request.username());

        // Construir respuesta
        LoginResponse response = LoginResponse.of(
                token,
                request.username(),
                List.of(role.name()),
                jwtProperties.getExpiration()
        );

        return ResponseEntity.ok(response);
    }
}
