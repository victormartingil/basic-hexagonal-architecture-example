package com.example.hexarch.shared.infrastructure.security.jwt;

import com.example.hexarch.shared.domain.security.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT AUTHENTICATION FILTER - Filtro para validar tokens JWT en cada request
 *
 * RESPONSABILIDADES:
 * 1. Extraer token JWT del header "Authorization: Bearer {token}"
 * 2. Validar el token con JwtTokenProvider
 * 3. Extraer username y roles del token
 * 4. Crear Authentication y guardarlo en SecurityContext
 * 5. Pasar la request al siguiente filtro
 *
 * SPRING SECURITY FILTER CHAIN:
 * ```
 * HTTP Request
 *   ↓
 * SecurityContextPersistenceFilter
 *   ↓
 * *** JwtAuthenticationFilter *** ← AQUÍ ESTAMOS
 *   ↓
 * UsernamePasswordAuthenticationFilter
 *   ↓
 * FilterSecurityInterceptor (verifica autorización)
 *   ↓
 * Controller
 * ```
 *
 * FORMATO DEL HEADER:
 * ```
 * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMSJ9...
 * ```
 *
 * ARQUITECTURA HEXAGONAL:
 * - Está en infrastructure porque es un filtro HTTP (detalle técnico)
 * - NO está en domain porque Spring Security no es concepto de negocio
 * - Actúa como "adapter de entrada" que traduce HTTP a conceptos de seguridad
 *
 * OncePerRequestFilter:
 * - Garantiza que el filtro se ejecuta UNA VEZ por request
 * - Importante en aplicaciones con forwards/includes
 *
 * NOTA:
 * Este filtro NO se ejecuta en endpoints públicos (definidos en SecurityConfig)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Filtra cada request HTTP para validar JWT
     *
     * FLUJO:
     * 1. Extraer token del header Authorization
     * 2. Validar token
     * 3. Si válido: extraer username y roles, crear Authentication
     * 4. Guardar Authentication en SecurityContext
     * 5. Continuar con el siguiente filtro
     *
     * @param request     HTTP request
     * @param response    HTTP response
     * @param filterChain Cadena de filtros
     * @throws ServletException Si hay error en el filtro
     * @throws IOException      Si hay error de I/O
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Extraer token del header Authorization
            String token = extractTokenFromRequest(request);

            // 2. Validar token (si existe)
            if (token != null && jwtTokenProvider.validateToken(token)) {

                // 3. Extraer información del token
                String username = jwtTokenProvider.getUsernameFromToken(token);
                List<Role> roles = jwtTokenProvider.getRolesFromToken(token);

                // 4. Convertir roles a GrantedAuthorities de Spring Security
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                        .collect(Collectors.toList());

                // 5. Crear Authentication (UsernamePasswordAuthenticationToken)
                //    - Principal: username
                //    - Credentials: null (no necesitamos password aquí)
                //    - Authorities: roles del usuario
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                authorities
                        );

                // 6. Añadir detalles de la request (IP, session ID, etc.)
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 7. Guardar Authentication en SecurityContext
                //    Spring Security usará esto para autorización
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT authentication successful for user: {} with roles: {}",
                        username, roles);
            }

        } catch (Exception ex) {
            // Si hay cualquier error, loguear pero NO detener el request
            // El request continuará pero sin autenticación (será rechazado si requiere auth)
            log.error("Cannot set user authentication: {}", ex.getMessage());
        }

        // 8. Continuar con el siguiente filtro (siempre!)
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization
     *
     * FORMATO ESPERADO:
     * ```
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     * ```
     *
     * @param request HTTP request
     * @return Token JWT (sin "Bearer "), o null si no existe
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Validar que el header existe y tiene el formato correcto
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Quitar "Bearer " del principio (7 caracteres)
            return bearerToken.substring(7);
        }

        return null;
    }
}
