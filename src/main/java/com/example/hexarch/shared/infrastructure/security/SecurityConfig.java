package com.example.hexarch.shared.infrastructure.security;

import com.example.hexarch.shared.infrastructure.security.jwt.JwtAuthenticationEntryPoint;
import com.example.hexarch.shared.infrastructure.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * SECURITY CONFIGURATION - Configuración de Spring Security con JWT
 *
 * RESPONSABILIDADES:
 * 1. Configurar endpoints públicos y protegidos
 * 2. Configurar autorización por roles
 * 3. Añadir filtro JWT a la cadena de filtros
 * 4. Desactivar CSRF (API REST stateless)
 * 5. Configurar manejo de errores de autenticación
 *
 * ARQUITECTURA HEXAGONAL:
 * - Está en infrastructure porque Spring Security es detalle técnico
 * - Está en "shared" porque la seguridad es transversal
 * - Configura los "adapters de entrada" (filtros HTTP)
 *
 * STATELESS API:
 * - No usamos sesiones (SessionCreationPolicy.STATELESS)
 * - Cada request debe incluir token JWT
 * - No hay cookies de sesión
 * - Ideal para microservicios y API REST
 *
 * AUTORIZACIÓN:
 * - createUser: ADMIN o MANAGER
 * - getUser: cualquier usuario autenticado (ADMIN, MANAGER, VIEWER, SUPPLIER)
 * - actuator/health: público (sin autenticación)
 * - swagger-ui: público (para este proyecto educativo)
 *
 * CSRF:
 * - Desactivado porque usamos JWT (no cookies de sesión)
 * - JWT en header Authorization no es vulnerable a CSRF
 * - En aplicaciones con cookies de sesión, CSRF debe estar activo
 *
 * CORS:
 * - Configurado para permitir requests desde frontend
 * - En producción, especificar origins exactos (no "*")
 *
 * FILTER CHAIN:
 * ```
 * HTTP Request
 *   ↓
 * CorsFilter (permite cross-origin)
 *   ↓
 * *** JwtAuthenticationFilter *** (valida JWT, crea Authentication)
 *   ↓
 * FilterSecurityInterceptor (verifica autorización por rol)
 *   ↓
 * Controller
 * ```
 *
 * EJEMPLO DE REQUEST:
 * ```bash
 * # Request autenticado
 * curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
 *      http://localhost:8080/api/users
 *
 * # Request sin autenticación (401 Unauthorized)
 * curl http://localhost:8080/api/users
 * ```
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Habilita @PreAuthorize, @Secured, etc.
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:4200}")
    private String allowedOrigins;

    /**
     * Configura la cadena de filtros de seguridad
     *
     * CONFIGURACIÓN:
     * 1. CSRF: desactivado (API REST stateless con JWT)
     * 2. Session Management: STATELESS (no sesiones)
     * 3. Exception Handling: usar JwtAuthenticationEntryPoint para errores
     * 4. Authorization: reglas por endpoint y rol
     * 5. JWT Filter: añadir antes de UsernamePasswordAuthenticationFilter
     *
     * @param http HttpSecurity para configurar
     * @return SecurityFilterChain configurado
     * @throws Exception Si hay error en la configuración
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Desactivar CSRF (no necesario con JWT en header)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Configurar CORS (permitir requests desde frontend)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Configurar manejo de excepciones
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // 3. Configurar session management como STATELESS
                //    No crear sesiones HTTP, cada request debe tener JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Configurar autorización por endpoint
                .authorizeHttpRequests(auth -> auth
                        // ========== ENDPOINTS PÚBLICOS (sin autenticación) ==========

                        // Actuator health endpoint (para healthchecks de K8s, Docker, etc.)
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()

                        // Auth endpoint (login para obtener JWT)
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Swagger UI (para este proyecto educativo)
                        // En producción, proteger con autenticación
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // ========== ENDPOINTS PROTEGIDOS (requieren autenticación) ==========

                        // POST /api/users (createUser) → Solo ADMIN o MANAGER
                        .requestMatchers(HttpMethod.POST, "/api/users")
                        .hasAnyRole("ADMIN", "MANAGER")

                        // GET /api/users/{id} (getUser) → Cualquier usuario autenticado
                        .requestMatchers(HttpMethod.GET, "/api/users/**")
                        .authenticated()

                        // ========== TODOS LOS DEMÁS ENDPOINTS ==========

                        // Cualquier otro endpoint requiere autenticación
                        .anyRequest().authenticated()
                )

                // 5. Añadir filtro JWT ANTES del filtro de autenticación por username/password
                //    Orden importante: JWT debe ejecutarse primero
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * NOTA IMPORTANTE - ENDPOINTS PÚBLICOS:
     *
     * Swagger UI está público en este proyecto EDUCATIVO.
     *
     * En PRODUCCIÓN, deberías:
     * 1. Proteger Swagger con autenticación
     * 2. O desactivar Swagger completamente en producción
     * 3. Usar profiles: @Profile("!prod") para desactivarlo
     *
     * Ejemplo para proteger Swagger:
     * ```java
     * .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
     *     .hasRole("ADMIN")
     * ```
     */

    /**
     * Configuración de CORS (Cross-Origin Resource Sharing)
     *
     * CORS permite que aplicaciones frontend en otros dominios
     * (ej: React en localhost:3000) puedan hacer requests a esta API.
     *
     * CONFIGURACIÓN POR PROFILES:
     * - Desarrollo: Permisivo (localhost:3000, localhost:4200, etc.)
     * - Producción: Restrictivo (solo dominios específicos)
     *
     * Ver application.yaml para configurar:
     * ```yaml
     * cors:
     *   allowed-origins: https://app.example.com,https://admin.example.com
     * ```
     *
     * @return CorsConfigurationSource configurado
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (configurables por profile)
        configuration.setAllowedOrigins(Arrays.asList(this.allowedOrigins.split(",")));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Headers permitidos (incluye Authorization para JWT)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));

        // Headers expuestos al cliente
        configuration.setExposedHeaders(List.of("X-Total-Count", "Link"));

        // Permitir credenciales (cookies, Authorization headers)
        configuration.setAllowCredentials(true);

        // Tiempo de caché de la respuesta preflight OPTIONS (en segundos)
        configuration.setMaxAge(3600L); // 1 hora

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
