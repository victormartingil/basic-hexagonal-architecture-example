package com.example.hexarch.shared.infrastructure.rest.auth.dto;

import java.util.List;

/**
 * LOGIN RESPONSE DTO
 *
 * Respuesta exitosa del endpoint de login.
 *
 * Contiene:
 * - Token JWT generado
 * - Tipo de token (siempre "Bearer")
 * - Username del usuario
 * - Roles asignados
 * - Tiempo de expiración en milisegundos
 *
 * EJEMPLO DE RESPUESTA:
 * ```json
 * {
 *   "token": "eyJhbGciOiJIUzI1NiJ9...",
 *   "type": "Bearer",
 *   "username": "johndoe",
 *   "roles": ["ADMIN"],
 *   "expiresIn": 86400000
 * }
 * ```
 *
 * USO DEL TOKEN:
 * Incluir en headers de requests posteriores:
 * ```
 * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 * ```
 *
 * @param token     Token JWT generado
 * @param type      Tipo de token (siempre "Bearer")
 * @param username  Username del usuario
 * @param roles     Roles del usuario
 * @param expiresIn Tiempo de expiración en milisegundos (ej: 86400000 = 24 horas)
 */
public record LoginResponse(
        String token,
        String type,
        String username,
        List<String> roles,
        Long expiresIn
) {
    /**
     * Constructor estático para crear respuesta de login
     *
     * @param token     Token JWT
     * @param username  Username
     * @param roles     Roles
     * @param expiresIn Tiempo de expiración en ms
     * @return LoginResponse con type="Bearer"
     */
    public static LoginResponse of(String token, String username, List<String> roles, Long expiresIn) {
        return new LoginResponse(token, "Bearer", username, roles, expiresIn);
    }
}
