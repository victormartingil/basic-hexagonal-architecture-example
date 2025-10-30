package com.example.hexarch.shared.infrastructure.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT AUTHENTICATION ENTRY POINT - Maneja errores de autenticación
 *
 * RESPONSABILIDAD:
 * Manejar errores cuando:
 * - No hay token JWT en la request
 * - El token es inválido (expirado, firma incorrecta, etc.)
 * - El token es válido pero el usuario no tiene permisos (403 Forbidden)
 *
 * SPRING SECURITY:
 * - Se invoca automáticamente cuando un usuario no autenticado intenta
 *   acceder a un endpoint protegido
 * - Es el último recurso antes de devolver error al cliente
 *
 * RESPUESTA:
 * ```json
 * {
 *   "error": "Unauthorized",
 *   "message": "Full authentication is required to access this resource",
 *   "path": "/api/users",
 *   "status": 401
 * }
 * ```
 *
 * HTTP STATUS CODES:
 * - 401 Unauthorized: No autenticado (no token o token inválido)
 * - 403 Forbidden: Autenticado pero sin permisos (rol insuficiente)
 *
 * ARQUITECTURA HEXAGONAL:
 * - Está en infrastructure porque es manejo de HTTP (detalle técnico)
 * - Traduce excepciones de seguridad a respuestas HTTP
 * - Actúa como "adapter de salida" para errores
 *
 * NOTA EDUCATIVA:
 * En un API REST profesional, los errores deben ser consistentes:
 * - Formato JSON estándar
 * - Códigos HTTP correctos
 * - Mensajes descriptivos (pero sin revelar detalles de seguridad)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * Maneja errores de autenticación y devuelve respuesta JSON
     *
     * @param request       HTTP request que causó el error
     * @param response      HTTP response donde escribir el error
     * @param authException Excepción de autenticación
     * @throws IOException      Si hay error escribiendo la respuesta
     * @throws ServletException Si hay error en el servlet
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        log.error("Unauthorized error: {} for request {}", authException.getMessage(), request.getRequestURI());

        // Configurar respuesta HTTP
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

        // Crear body de error en formato JSON
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage());
        body.put("path", request.getRequestURI());

        // Escribir JSON en el response
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
