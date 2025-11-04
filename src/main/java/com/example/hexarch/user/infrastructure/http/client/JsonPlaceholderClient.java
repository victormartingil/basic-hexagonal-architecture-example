package com.example.hexarch.user.infrastructure.http.client;

import com.example.hexarch.user.application.port.ExternalUserApiClient;
import com.example.hexarch.user.infrastructure.http.client.dto.JsonPlaceholderCreateUserRequest;
import com.example.hexarch.user.infrastructure.http.client.dto.JsonPlaceholderUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * Implementación del Output Port ExternalUserApiClient usando Spring RestClient.
 * <p>
 * Este adaptador integra con la API pública JSONPlaceholder para demostrar
 * cómo conectar con APIs REST externas siguiendo arquitectura hexagonal.
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
 * <h3>Manejo de Errores:</h3>
 * <ul>
 *   <li>404: Retorna Optional.empty()</li>
 *   <li>Otros errores: Lanza RuntimeException (podrías usar Circuit Breaker)</li>
 *   <li>Timeout: Configurado en RestClientConfig</li>
 * </ul>
 *
 * <h3>Mejoras posibles (para producción):</h3>
 * <ul>
 *   <li>Añadir Circuit Breaker (@CircuitBreaker de Resilience4j)</li>
 *   <li>Añadir Retry con backoff exponencial</li>
 *   <li>Añadir cache para reducir llamadas externas</li>
 *   <li>Añadir métricas customizadas</li>
 * </ul>
 *
 * @see ExternalUserApiClient
 * @see com.example.hexarch.user.infrastructure.config.RestClientConfig
 */
@Component
public class JsonPlaceholderClient implements ExternalUserApiClient {

    private static final Logger logger = LoggerFactory.getLogger(JsonPlaceholderClient.class);

    private final RestClient restClient;

    /**
     * Constructor con inyección del RestClient configurado.
     *
     * @param restClient RestClient bean configurado en RestClientConfig
     */
    public JsonPlaceholderClient(@Qualifier("jsonPlaceholderRestClient") RestClient restClient) {
        this.restClient = restClient;
        logger.info("✅ JsonPlaceholderClient initialized");
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
            JsonPlaceholderUserResponse response = restClient.get()
                    .uri("/users/{id}", externalUserId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, httpResponse) -> {
                        logger.warn("⚠️ [EXTERNAL API] User not found in JSONPlaceholder - userId: {}, status: {}",
                                externalUserId, httpResponse.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, httpResponse) -> {
                        logger.error("❌ [EXTERNAL API] JSONPlaceholder API error - status: {}", httpResponse.getStatusCode());
                    })
                    .body(JsonPlaceholderUserResponse.class);

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
            JsonPlaceholderCreateUserRequest request = new JsonPlaceholderCreateUserRequest(name, email);

            JsonPlaceholderUserResponse response = restClient.post()
                    .uri("/users")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, httpResponse) -> {
                        logger.error("❌ [EXTERNAL API] Failed to create user - status: {}", httpResponse.getStatusCode());
                    })
                    .body(JsonPlaceholderUserResponse.class);

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
     * Mapea el DTO de Infrastructure (JsonPlaceholderUserResponse)
     * al DTO de Application (ExternalUserData).
     * <p>
     * Este mapping aísla la capa de Application de los cambios en la API externa.
     * </p>
     *
     * @param response Response de JSONPlaceholder
     * @return ExternalUserData para Application layer
     */
    private ExternalUserData mapToExternalUserData(JsonPlaceholderUserResponse response) {
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
