package com.example.hexarch.user.infrastructure.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuración de OpenFeign para integración con APIs REST externas.
 * <p>
 * Esta clase configura el cliente Feign con:
 * <ul>
 *   <li>Timeouts configurables</li>
 *   <li>Logging level</li>
 *   <li>Error decoder custom</li>
 *   <li>Request interceptors (headers, auth, etc.)</li>
 * </ul>
 * </p>
 *
 * <h3>🎯 ¿Por qué FeignClient?</h3>
 * <p>
 * Este proyecto implementa AMBAS opciones (FeignClient y RestClient) para comparar.
 * FeignClient es @Primary porque:
 * <ul>
 *   <li>✅ Menos código (~80% menos líneas)</li>
 *   <li>✅ Más usado en la industria</li>
 *   <li>✅ Fácil para equipos grandes</li>
 *   <li>✅ La dependencia extra (~2-3 MB) es insignificante</li>
 * </ul>
 * </p>
 *
 * <h3>Propiedades configurables (application.yml):</h3>
 * <pre>
 * external-api:
 *   jsonplaceholder:
 *     base-url: https://jsonplaceholder.typicode.com
 *     connect-timeout: 5s
 *     read-timeout: 10s
 *
 * logging:
 *   level:
 *     com.example.hexarch.user.infrastructure.http.client: DEBUG
 * </pre>
 *
 * @see ExternalUserApiFeignClient
 * @see com.example.hexarch.user.infrastructure.config.RestClientConfig
 */
@Configuration
public class FeignClientConfig {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FeignClientConfig.class);

    /**
     * Configurar timeouts para Feign clients.
     * <p>
     * Usa los mismos valores que RestClient para comparación justa.
     * </p>
     *
     * <p>
     * Nota: FeignClient config usa milisegundos (int) porque se ejecuta en un contexto
     * separado donde la conversión automática de Duration no funciona.
     * </p>
     */
    @Bean
    public Request.Options feignRequestOptions(
            @Value("${external-api.jsonplaceholder.feign.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${external-api.jsonplaceholder.feign.read-timeout-ms:10000}") int readTimeoutMs
    ) {
        logger.info("🔧 Configuring Feign timeouts");
        logger.info("   Connect Timeout: {} ms", connectTimeoutMs);
        logger.info("   Read Timeout: {} ms", readTimeoutMs);

        // Request.Options requiere millis como int
        return new Request.Options(connectTimeoutMs, readTimeoutMs);
    }

    /**
     * Configurar logging level para Feign.
     * <p>
     * Niveles disponibles:
     * <ul>
     *   <li>NONE: Sin logging</li>
     *   <li>BASIC: Request method, URL, status code, execution time</li>
     *   <li>HEADERS: BASIC + request/response headers</li>
     *   <li>FULL: HEADERS + request/response bodies (útil para debugging)</li>
     * </ul>
     * </p>
     *
     * <p>
     * <strong>Recomendación</strong>:
     * <ul>
     *   <li>Producción: BASIC</li>
     *   <li>Desarrollo: FULL</li>
     * </ul>
     * </p>
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Request Interceptor para agregar headers por defecto.
     * <p>
     * Útil para:
     * <ul>
     *   <li>Agregar headers de autenticación</li>
     *   <li>Agregar headers de tracking (X-Request-ID, etc.)</li>
     *   <li>Agregar User-Agent custom</li>
     * </ul>
     * </p>
     */
    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return requestTemplate -> {
            // Agregar headers por defecto
            requestTemplate.header("Accept", "application/json");
            requestTemplate.header("User-Agent", "Hexarch-Demo/1.0");

            // Logging
            logger.debug("🌐 [FEIGN] {} {}", requestTemplate.method(), requestTemplate.url());
        };
    }

    /**
     * Error Decoder custom para manejar errores de la API externa.
     * <p>
     * Permite convertir errores HTTP en excepciones custom de dominio.
     * </p>
     *
     * <h3>Estrategia de manejo de errores:</h3>
     * <ul>
     *   <li>404: Retornamos Optional.empty() en la interface (manejado en default methods)</li>
     *   <li>4xx: RuntimeException con mensaje descriptivo</li>
     *   <li>5xx: RuntimeException indicando problema del servidor</li>
     * </ul>
     */
    @Bean
    public ErrorDecoder feignErrorDecoder() {
        return (methodKey, response) -> {
            String errorMessage = String.format(
                    "Error calling external API: %s - Status: %d",
                    methodKey,
                    response.status()
            );

            logger.error("❌ [FEIGN] {}", errorMessage);

            // Podrías crear excepciones custom por tipo de error
            if (response.status() == 404) {
                // 404 se maneja en el default method de la interface
                return new feign.FeignException.NotFound(
                        errorMessage,
                        response.request(),
                        null,
                        null
                );
            }

            if (response.status() >= 500) {
                return new RuntimeException("External API server error: " + response.status());
            }

            return new RuntimeException(errorMessage);
        };
    }
}
