package com.example.hexarch.user.infrastructure.config;

import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Configuración de Spring RestClient para integración con APIs REST externas.
 * <p>
 * Esta clase configura el cliente HTTP con:
 * <ul>
 *   <li>Timeouts configurables</li>
 *   <li>Base URL de la API externa</li>
 *   <li>Headers por defecto</li>
 *   <li>Logging de requests/responses</li>
 *   <li>Observability con Micrometer</li>
 * </ul>
 * </p>
 *
 * <h3>Arquitectura Hexagonal:</h3>
 * <ul>
 *   <li>Esta configuración pertenece a <strong>Infrastructure</strong></li>
 *   <li>Crea el RestClient que usará JsonPlaceholderClient</li>
 *   <li>Permite cambiar la URL o configuración sin tocar la lógica de negocio</li>
 * </ul>
 *
 * <h3>Propiedades configurables (application.yml):</h3>
 * <pre>
 * external-api:
 *   jsonplaceholder:
 *     base-url: https://jsonplaceholder.typicode.com
 *     connect-timeout: 5s
 *     read-timeout: 10s
 * </pre>
 *
 * @see RestClient
 * @see com.example.hexarch.user.infrastructure.http.client.JsonPlaceholderClient
 */
@Configuration
public class RestClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(RestClientConfig.class);

    /**
     * Crea un RestClient configurado para la API de JSONPlaceholder.
     * <p>
     * Este bean se inyecta en JsonPlaceholderClient para realizar las peticiones HTTP.
     * </p>
     *
     * @param baseUrl             URL base de la API externa
     * @param connectTimeout      Timeout de conexión
     * @param readTimeout         Timeout de lectura
     * @param observationRegistry Registry para observability
     * @return RestClient configurado
     */
    @Bean(name = "jsonPlaceholderRestClient")
    public RestClient jsonPlaceholderRestClient(
            @Value("${external-api.jsonplaceholder.base-url:https://jsonplaceholder.typicode.com}") String baseUrl,
            @Value("${external-api.jsonplaceholder.connect-timeout:5s}") Duration connectTimeout,
            @Value("${external-api.jsonplaceholder.read-timeout:10s}") Duration readTimeout,
            ObservationRegistry observationRegistry
    ) {
        logger.info("🔧 Configuring RestClient for JSONPlaceholder API");
        logger.info("   Base URL: {}", baseUrl);
        logger.info("   Connect Timeout: {}", connectTimeout);
        logger.info("   Read Timeout: {}", readTimeout);

        // Configurar timeouts
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(connectTimeout)
                .withReadTimeout(readTimeout);

        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);

        // Crear RestClient con configuración
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .observationRegistry(observationRegistry)
                // Request interceptor para logging
                .requestInterceptor((request, body, execution) -> {
                    logger.debug("🌐 [REST CLIENT] {} {}", request.getMethod(), request.getURI());
                    return execution.execute(request, body);
                })
                // Response interceptor para logging
                .requestInterceptor((request, body, execution) -> {
                    var response = execution.execute(request, body);
                    logger.debug("✅ [REST CLIENT] Response: {} - {}",
                            response.getStatusCode().value(),
                            response.getStatusCode());
                    return response;
                })
                .build();

        logger.info("✅ RestClient for JSONPlaceholder API configured successfully");

        return restClient;
    }
}
