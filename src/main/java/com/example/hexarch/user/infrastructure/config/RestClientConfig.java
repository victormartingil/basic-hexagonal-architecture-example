package com.example.hexarch.user.infrastructure.config;

import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
 * <h3>🎯 RestClient: Backend para HTTP Interface y Uso Directo</h3>
 * <p>
 * Este RestClient configurado sirve para <strong>DOS propósitos</strong>:
 * </p>
 * <ol>
 *   <li><strong>Backend de HTTP Interface</strong> (⭐ RECOMENDADO):
 *       <ul>
 *         <li>HTTP Interface usa este RestClient para hacer llamadas HTTP reales</li>
 *         <li>Configurado en HttpInterfaceConfig</li>
 *         <li>Opción declarativa moderna</li>
 *       </ul>
 *   </li>
 *   <li><strong>Uso directo imperativo</strong> (para control total):
 *       <ul>
 *         <li>ExternalUserApiRestClient usa este bean directamente</li>
 *         <li>Control total sobre cada request</li>
 *         <li>Mejor para debugging y casos complejos</li>
 *       </ul>
 *   </li>
 * </ol>
 * <p>
 * <strong>Por qué RestClient (Spring 6.1+)</strong>:
 * <ul>
 *   <li><strong>Moderno</strong>: API fluida y expresiva (mejor que RestTemplate)</li>
 *   <li><strong>Sin dependencias extra</strong>: Incluido en Spring Boot 3.2+</li>
 *   <li><strong>Síncrono</strong>: Código simple y directo para casos de uso no reactivos</li>
 *   <li><strong>Observability</strong>: Soporte nativo para Micrometer tracing</li>
 *   <li><strong>Reemplazo oficial</strong>: Spring recomienda migrar de RestTemplate a RestClient</li>
 * </ul>
 * </p>
 *
 * <h3>📊 Comparación con Alternativas:</h3>
 * <table border="1">
 *   <tr>
 *     <th>Cliente</th>
 *     <th>Estado</th>
 *     <th>Notas</th>
 *   </tr>
 *   <tr>
 *     <td><strong>HTTP Interface</strong></td>
 *     <td>✅ Activo, recomendado</td>
 *     <td>Usa RestClient como backend. Opción declarativa moderna</td>
 *   </tr>
 *   <tr>
 *     <td><strong>RestClient</strong></td>
 *     <td>✅ Activo (este config)</td>
 *     <td>Backend de HTTP Interface y uso directo</td>
 *   </tr>
 *   <tr>
 *     <td><strong>FeignClient</strong></td>
 *     <td>⚠️ Maintenance Mode</td>
 *     <td>Requiere dependencia adicional. Solo recomendado para Spring Cloud</td>
 *   </tr>
 *   <tr>
 *     <td><strong>WebClient</strong></td>
 *     <td>✅ Activo</td>
 *     <td>Overkill para este proyecto. Solo si necesitas reactividad (Mono/Flux)</td>
 *   </tr>
 *   <tr>
 *     <td><strong>RestTemplate</strong></td>
 *     <td>⚠️ Maintenance Mode</td>
 *     <td>Legacy. Spring recomienda migrar a RestClient</td>
 *   </tr>
 * </table>
 *
 * <h3>📚 Para aprender más:</h3>
 * <p>
 * Ver guía completa: <code>docs/18-HTTP-Clients-Comparison-Guide.md</code><br/>
 * Compara RestClient, RestTemplate, WebClient y FeignClient con ejemplos de código.
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
 * @see HttpInterfaceConfig - Usa este RestClient como backend (RECOMENDADO)
 * @see com.example.hexarch.user.infrastructure.http.client.ExternalUserApiRestClient - Uso directo
 * @see <a href="https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-restclient">Spring RestClient Documentation</a>
 */
@Configuration
public class RestClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(RestClientConfig.class);

    /**
     * Crea un RestClient configurado para la API de JSONPlaceholder.
     * <p>
     * Este bean se inyecta en ExternalUserApiRestClient para realizar las peticiones HTTP.
     * Usado cuando se quiere la implementación con RestClient (imperativa, control total).
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

        // Configurar timeouts usando SimpleClientHttpRequestFactory (no deprecado)
        // Reemplaza ClientHttpRequestFactorySettings (deprecado en Spring Boot 3.4+)
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

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
