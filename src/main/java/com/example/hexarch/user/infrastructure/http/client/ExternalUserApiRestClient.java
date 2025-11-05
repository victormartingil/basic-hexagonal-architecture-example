package com.example.hexarch.user.infrastructure.http.client;

import com.example.hexarch.user.application.port.ExternalUserApiClient;
import com.example.hexarch.user.infrastructure.http.client.dto.ExternalUserApiCreateRequest;
import com.example.hexarch.user.infrastructure.http.client.dto.ExternalUserApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * Implementación del Output Port ExternalUserApiClient usando Spring RestClient (IMPERATIVO).
 * <p>
 * Este adaptador integra con la API pública JSONPlaceholder para demostrar
 * cómo conectar con APIs REST externas usando RestClient (control total, código explícito).
 * </p>
 *
 * <h3>🎯 Tres Opciones en Este Proyecto (2025):</h3>
 * <p>
 * Este proyecto implementa TRES opciones para comparar:
 * <ul>
 *   <li><strong>ExternalUserApiHttpInterfaceAdapter</strong> (⭐ RECOMENDADO): Cliente DECLARATIVO nativo de Spring 6</li>
 *   <li><strong>ExternalUserApiRestClient</strong> (esta clase): Cliente IMPERATIVO con control total</li>
 *   <li><strong>ExternalUserApiFeignClient</strong>: Cliente DECLARATIVO tradicional (⚠️ maintenance mode)</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Usa RestClient cuando</strong>:
 * <ul>
 *   <li>✅ Necesitas control TOTAL sobre HTTP requests</li>
 *   <li>✅ Debugging intensivo es crítico</li>
 *   <li>✅ APIs complejas o no estándar</li>
 *   <li>✅ Prefieres código imperativo explícito</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Usa HTTP Interface en lugar de RestClient cuando</strong>:
 * <ul>
 *   <li>✅ Prefieres código declarativo (menos líneas)</li>
 *   <li>✅ API REST estándar</li>
 *   <li>✅ Múltiples endpoints del mismo servicio</li>
 * </ul>
 * Por defecto se usa <strong>FeignClient</strong> (@Primary) por compatibilidad, pero considera HTTP Interface.
 * </p>
 *
 * <h3>Arquitectura Hexagonal:</h3>
 * <ul>
 *   <li>Implementa el <strong>Output Port</strong> ExternalUserApiClient</li>
 *   <li>Pertenece a la capa de <strong>Infrastructure</strong></li>
 *   <li>Usa tecnología específica (Spring RestClient)</li>
 *   <li>Puede ser reemplazado por otra implementación sin afectar Application/Domain</li>
 * </ul>
 *
 * <h3>Ventajas de RestClient:</h3>
 * <ul>
 *   <li>✅ Control total sobre HTTP requests/responses</li>
 *   <li>✅ Debugging fácil (código explícito)</li>
 *   <li>✅ Sin dependencias adicionales</li>
 *   <li>✅ Performance óptimo</li>
 * </ul>
 *
 * <h3>Desventajas de RestClient:</h3>
 * <ul>
 *   <li>❌ Más código boilerplate</li>
 *   <li>❌ Más trabajo para APIs con muchos endpoints</li>
 * </ul>
 *
 * <h3>Manejo de Errores:</h3>
 * <ul>
 *   <li>404: Retorna Optional.empty()</li>
 *   <li>Otros errores: Lanza RuntimeException (podrías usar Circuit Breaker)</li>
 *   <li>Timeout: Configurado en RestClientConfig</li>
 * </ul>
 *
 * @see ExternalUserApiClient
 * @see ExternalUserApiHttpInterfaceAdapter - Opción RECOMENDADA (declarativo, nativo)
 * @see ExternalUserApiFeignClient - Opción legacy (maintenance mode)
 * @see com.example.hexarch.user.infrastructure.config.RestClientConfig
 */
@Component("jsonPlaceholderRestClientAdapter")
@Qualifier("restClient")
public class ExternalUserApiRestClient implements ExternalUserApiClient {

    private static final Logger logger = LoggerFactory.getLogger(ExternalUserApiRestClient.class);

    private final RestClient restClient;

    /**
     * Constructor con inyección del RestClient configurado.
     *
     * @param restClient RestClient bean configurado en RestClientConfig
     */
    public ExternalUserApiRestClient(@Qualifier("jsonPlaceholderRestClient") RestClient restClient) {
        this.restClient = restClient;
        logger.info("✅ ExternalUserApiRestClient (RestClient implementation) initialized");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Realiza un GET a https://jsonplaceholder.typicode.com/users/{id}
     * </p>
     *
     * @param externalUserId ID del usuario en JSONPlaceholder (1-10 son válidos)
     * @return Optional con los datos del usuario o empty si no existe
     */
    @Override
    public Optional<ExternalUserData> getUserById(Integer externalUserId) {
        logger.info("📡 [EXTERNAL API] Fetching user from JSONPlaceholder API - userId: {}", externalUserId);

        try {
            ExternalUserApiResponse response = restClient.get()
                    .uri("/users/{id}", externalUserId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, httpResponse) -> {
                        logger.warn("⚠️ [EXTERNAL API] User not found in JSONPlaceholder - userId: {}, status: {}",
                                externalUserId, httpResponse.getStatusCode());
                        throw new RestClientException("User not found: " + externalUserId);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, httpResponse) -> {
                        logger.error("❌ [EXTERNAL API] JSONPlaceholder API error - status: {}", httpResponse.getStatusCode());
                        throw new RestClientException("Server error: " + httpResponse.getStatusCode());
                    })
                    .body(ExternalUserApiResponse.class);

            if (response == null) {
                logger.warn("⚠️ [EXTERNAL API] Received null response from JSONPlaceholder");
                return Optional.empty();
            }

            logger.info("✅ [EXTERNAL API] User fetched successfully - username: {}, email: {}",
                    response.username(), response.email());

            // Mapear de Infrastructure DTO a Application DTO
            ExternalUserData userData = mapToExternalUserData(response);
            return Optional.of(userData);

        } catch (RestClientException e) {
            logger.error("❌ [EXTERNAL API] Error calling JSONPlaceholder API: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Realiza un POST a https://jsonplaceholder.typicode.com/users
     * (Nota: JSONPlaceholder simula la creación pero no persiste los datos)
     * </p>
     *
     * @param name  Nombre del usuario
     * @param email Email del usuario
     * @return Datos del usuario "creado"
     */
    @Override
    public ExternalUserData createExternalUser(String name, String email) {
        logger.info("📤 [EXTERNAL API] Creating user in JSONPlaceholder API - name: {}, email: {}", name, email);

        try {
            ExternalUserApiCreateRequest request = new ExternalUserApiCreateRequest(name, email);

            ExternalUserApiResponse response = restClient.post()
                    .uri("/users")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, httpResponse) -> {
                        logger.error("❌ [EXTERNAL API] Failed to create user - status: {}", httpResponse.getStatusCode());
                    })
                    .body(ExternalUserApiResponse.class);

            if (response == null) {
                logger.error("❌ [EXTERNAL API] Received null response when creating user");
                throw new RuntimeException("Failed to create external user: null response");
            }

            logger.info("✅ [EXTERNAL API] User created successfully - id: {}", response.id());

            return mapToExternalUserData(response);

        } catch (RestClientException e) {
            logger.error("❌ [EXTERNAL API] Error creating user in JSONPlaceholder: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create external user", e);
        }
    }

    /**
     * Mapea el DTO de Infrastructure (ExternalUserApiResponse)
     * al DTO de Application (ExternalUserData).
     * <p>
     * Este mapping aísla la capa de Application de los cambios en la API externa.
     * </p>
     *
     * @param response Response de JSONPlaceholder
     * @return ExternalUserData para Application layer
     */
    private ExternalUserData mapToExternalUserData(ExternalUserApiResponse response) {
        return new ExternalUserData(
                response.id(),
                response.name(),
                response.username(),
                response.email(),
                response.phone(),
                response.website()
        );
    }
}
