package com.example.hexarch.shared.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * CORRELATION ID FILTER
 *
 * Filtro HTTP que garantiza que cada request tenga un Correlation ID único.
 * El Correlation ID permite trazar requests a través de múltiples servicios.
 *
 * FUNCIONAMIENTO:
 * 1. Si el cliente envía header "X-Correlation-ID" → lo usa
 * 2. Si NO viene header → genera un UUID nuevo
 * 3. Añade el Correlation ID al MDC (Mapped Diagnostic Context) para logs
 * 4. Añade el header en la response para que el cliente lo vea
 *
 * USO EN MICROSERVICIOS:
 * - Service A recibe request del cliente → genera correlationId=abc-123
 * - Service A llama a Service B → envía header X-Correlation-ID: abc-123
 * - Service B procesa request → usa el mismo correlationId=abc-123
 * - Todos los logs tienen el mismo correlationId → fácil debugging
 *
 * EJEMPLO DE LOGS:
 * 2024-01-15 10:30:00 [traceId,spanId] correlationId=abc-123 INFO - Processing user creation
 * 2024-01-15 10:30:01 [traceId,spanId] correlationId=abc-123 INFO - User saved to database
 * 2024-01-15 10:30:02 [traceId,spanId] correlationId=abc-123 INFO - Event published to Kafka
 *
 * BEST PRACTICES:
 * - Usar UUID v4 para garantizar unicidad global
 * - Propagar en TODOS los headers HTTP hacia otros servicios
 * - Añadir al MDC para que aparezca automáticamente en logs
 * - Limpiar MDC al finalizar request (previene memory leaks)
 *
 * DIFERENCIA CON TRACE ID:
 * - Trace ID: generado por sistema de tracing (Zipkin/Jaeger) - granular
 * - Correlation ID: generado por aplicación - business-level tracing
 * - Ambos son complementarios: Trace ID para performance, Correlation ID para business flow
 *
 * @see <a href="https://www.enterpriseintegrationpatterns.com/patterns/messaging/CorrelationIdentifier.html">
 *     Enterprise Integration Patterns - Correlation Identifier</a>
 */
@Component
@Order(1) // Ejecutar PRIMERO (antes de otros filtros)
@Slf4j
public class CorrelationIdFilter extends OncePerRequestFilter {

    /**
     * Nombre del header HTTP para Correlation ID.
     * Estándar: X-Correlation-ID (usado por AWS, Azure, Google Cloud)
     */
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    /**
     * Clave del MDC para Correlation ID.
     * Se usa en el patrón de logs: %X{correlationId}
     */
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Obtener o generar Correlation ID
        String correlationId = extractOrGenerateCorrelationId(request);

        // 2. Añadir al MDC (Mapped Diagnostic Context) para logs
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

        // 3. Añadir header a la response (para que el cliente vea el ID)
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            // 4. Continuar con la cadena de filtros
            filterChain.doFilter(request, response);
        } finally {
            // 5. IMPORTANTE: Limpiar MDC al finalizar (evita memory leaks)
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    /**
     * Extrae Correlation ID del header o genera uno nuevo.
     *
     * ESTRATEGIA:
     * - Si el cliente envía X-Correlation-ID → confiar y usar (permite tracing cross-system)
     * - Si NO viene → generar UUID v4 nuevo
     *
     * VALIDACIÓN:
     * - Acepta cualquier string no vacío (flexible con diferentes formatos)
     * - UUID v4 recomendado pero no obligatorio (compatibilidad con sistemas legacy)
     *
     * @param request HTTP request
     * @return Correlation ID (existente o generado)
     */
    private String extractOrGenerateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            // Generar nuevo UUID v4
            correlationId = UUID.randomUUID().toString();
            log.debug("Generated new Correlation ID: {}", correlationId);
        } else {
            log.debug("Using existing Correlation ID: {}", correlationId);
        }

        return correlationId;
    }
}
