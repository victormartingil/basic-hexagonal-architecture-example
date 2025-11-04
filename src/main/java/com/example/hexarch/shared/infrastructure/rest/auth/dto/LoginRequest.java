package com.example.hexarch.shared.infrastructure.rest.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * LOGIN REQUEST DTO
 *
 * DTO para las credenciales de login.
 *
 * NOTA EDUCATIVA:
 * En un proyecto real de producción, usarías un servicio de autenticación
 * real como:
 * - Spring Security con UserDetailsService
 * - OAuth2/OIDC (Keycloak, Auth0, Okta)
 * - LDAP/Active Directory
 *
 * Este endpoint está simplificado para propósitos educativos:
 * - Genera un JWT sin validar contra base de datos
 * - Útil para testing rápido de la API
 * - En producción, SIEMPRE valida contra un user store real
 *
 * @param username Username del usuario
 * @param role     Role a asignar al token (ADMIN, MANAGER, VIEWER, SUPPLIER)
 */
public record LoginRequest(
        @NotBlank(message = "Username es requerido")
        String username,

        @NotBlank(message = "Role es requerido")
        String role
) {
}
