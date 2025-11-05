package com.example.hexarch.user.infrastructure.http.client;

import com.example.hexarch.user.application.port.ExternalUserApiClient;
import com.example.hexarch.user.infrastructure.http.client.dto.ExternalUserApiCreateRequest;
import com.example.hexarch.user.infrastructure.http.client.dto.ExternalUserApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * Implementación del Output Port ExternalUserApiClient usando HTTP Interface (DECLARATIVO MODERNO).
 * <p>
 * Este adaptador integra con la API pública JSONPlaceholder usando HTTP Interface (@HttpExchange),
 * la forma NATIVA y RECOMENDADA de Spring Framework 6 para clientes HTTP declarativos.
 * </p>
 *
 * <h3>🎯 ¿Por qué HTTP Interface es MEJOR que FeignClient en 2025?</h3>
 * <table>
 *   <tr>
 *     <th>Aspecto</th>
 *     <th>HTTP Interface</th>
 *     <th>FeignClient</th>
 *   </tr>
 *   <tr>
 *     <td><strong>Estado</strong></td>
 *     <td>✅ Activo, recomendado</td>
 *     <td>⚠️ Maintenance mode</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Dependencias</strong></td>
 *     <td>✅ Ninguna (core Spring)</td>
 *     <td>❌ +2-3 MB (spring-cloud-openfeign)</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Backend</strong></td>
 *     <td>✅ RestClient/WebClient (nativo)</td>
 *     <td>⚠️ Feign (tercero)</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Performance</strong></td>
 *     <td>✅ Óptimo (usa RestClient directamente)</td>
 *     <td>⚠️ Overhead adicional</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Observability</strong></td>
 *     <td>✅ Nativa (Micrometer)</td>
 *     <td>⚠️ Requiere configuración</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Soporte futuro</strong></td>
 *     <td>✅ Alta prioridad de Spring</td>
 *     <td>⚠️ Bajo (maintenance mode)</td>
 *   </tr>
 * </table>
 *
 * <h3>🆚 Tres Opciones en Este Proyecto:</h3>
 * <p>
 * Este proyecto implementa TODAS las opciones para comparar:
 * </p>
 * <ol>
 *   <li><strong>ExternalUserApiHttpInterfaceAdapter</strong> (esta clase - @Primary):
 *       <ul>
 *         <li>⭐ <strong>OPCIÓN POR DEFECTO</strong> (@Primary)</li>
 *         <li>✅ RECOMENDADO para Spring Boot 3+</li>
 *         <li>Cliente DECLARATIVO usando HTTP Interface nativa</li>
 *         <li>Sin dependencias adicionales</li>
 *         <li>Usa RestClient como backend</li>
 *       </ul>
 *   </li>
 *   <li><strong>ExternalUserApiFeignClient</strong>:
 *       <ul>
 *         <li>⚠️ Maintenance mode, pero aún válido</li>
 *         <li>Cliente DECLARATIVO tradicional</li>
 *         <li>Mejor para microservicios con Spring Cloud</li>
 *         <li>Requiere @Qualifier("feignClient") para usarlo</li>
 *       </ul>
 *   </li>
 *   <li><strong>ExternalUserApiRestClient</strong>:
 *       <ul>
 *         <li>✅ Activo</li>
 *         <li>Cliente IMPERATIVO con control total</li>
 *         <li>Mejor para casos complejos o debugging intensivo</li>
 *         <li>Requiere @Qualifier("restClient") para usarlo</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * <h3>🎯 HTTP Interface es @Primary (Opción por defecto):</h3>
 * <p>
 * Esta clase tiene <strong>@Primary</strong>, lo que significa que se inyecta automáticamente
 * sin necesidad de @Qualifier:
 * </p>
 * <pre>{@code
 * // Opción 1: Inyección automática (usa HTTP Interface - @Primary)
 * @Service
 * public class CreateUserService {
 *     public CreateUserService(ExternalUserApiClient client) {
 *         this.client = client;  // ✅ HTTP Interface (automático)
 *     }
 * }
 *
 * // Opción 2: Usar otra implementación con @Qualifier explícito
 * public CreateUserService(@Qualifier("feignClient") ExternalUserApiClient client) {
 *     this.client = client;  // FeignClient
 * }
 *
 * public CreateUserService(@Qualifier("restClient") ExternalUserApiClient client) {
 *     this.client = client;  // RestClient
 * }
 * }</pre>
 *
 * <h3>Arquitectura Hexagonal:</h3>
 * <ul>
 *   <li>Implementa el <strong>Output Port</strong> ExternalUserApiClient</li>
 *   <li>Pertenece a la capa de <strong>Infrastructure</strong></li>
 *   <li>Usa tecnología específica (Spring HTTP Interface + RestClient)</li>
 *   <li>Puede ser reemplazado por otra implementación sin afectar Application/Domain</li>
 * </ul>
 *
 * <h3>Ventajas de HTTP Interface:</h3>
 * <ul>
 *   <li>✅ Código muy limpio (solo interface, sin implementación)</li>
 *   <li>✅ Sin dependencias adicionales</li>
 *   <li>✅ Recomendada oficialmente por Spring</li>
 *   <li>✅ Performance óptimo (usa RestClient directamente)</li>
 *   <li>✅ Observability nativa (Micrometer)</li>
 *   <li>✅ Debugging más fácil que FeignClient</li>
 *   <li>✅ Flexible (puedes cambiar entre RestClient y WebClient)</li>
 * </ul>
 *
 * <h3>Desventajas de HTTP Interface:</h3>
 * <ul>
 *   <li>❌ No integra con Spring Cloud (Eureka, etc.)</li>
 *   <li>❌ No tiene service discovery automático</li>
 *   <li>❌ Load balancing manual (vs FeignClient con Ribbon/Spring Cloud LoadBalancer)</li>
 * </ul>
 *
 * <h3>¿Cuándo usar HTTP Interface vs FeignClient?</h3>
 * <p>
 * <strong>USA HTTP Interface (esta clase) si:</strong>
 * <ul>
 *   <li>✅ Spring Boot 3+ sin Spring Cloud</li>
 *   <li>✅ Quieres la opción moderna recomendada</li>
 *   <li>✅ No necesitas service discovery</li>
 *   <li>✅ Prefieres menos dependencias</li>
 * </ul>
 * </p>
 * <p>
 * <strong>USA FeignClient si:</strong>
 * <ul>
 *   <li>✅ Microservicios con Spring Cloud</li>
 *   <li>✅ Necesitas service discovery (Eureka, Consul)</li>
 *   <li>✅ Necesitas load balancing client-side automático</li>
 * </ul>
 * </p>
 *
 * <h3>Configuración:</h3>
 * <ul>
 *   <li>HTTP Interface: Configurada en HttpInterfaceConfig</li>
 *   <li>RestClient backend: Configurado en RestClientConfig</li>
 *   <li>URL base: ${external-api.jsonplaceholder.base-url}</li>
 *   <li>Timeouts: Heredados del RestClient configurado</li>
 * </ul>
 *
 * @see ExternalUserApiClient
 * @see ExternalUserApiHttpInterface
 * @see com.example.hexarch.user.infrastructure.config.HttpInterfaceConfig
 * @see ExternalUserApiFeignClient
 * @see ExternalUserApiRestClient
 */
@Component("jsonPlaceholderHttpInterfaceAdapter")
@Primary
@Qualifier("httpInterface")
public class ExternalUserApiHttpInterfaceAdapter implements ExternalUserApiClient {

    private static final Logger logger = LoggerFactory.getLogger(ExternalUserApiHttpInterfaceAdapter.class);

    private final ExternalUserApiHttpInterface httpInterface;

    /**
     * Constructor con inyección del HTTP Interface proxy.
     *
     * @param httpInterface Proxy generado por HttpServiceProxyFactory
     */
    public ExternalUserApiHttpInterfaceAdapter(ExternalUserApiHttpInterface httpInterface) {
        this.httpInterface = httpInterface;
        logger.info("✅ ExternalUserApiHttpInterfaceAdapter (HTTP Interface implementation) initialized");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Realiza un GET a https://jsonplaceholder.typicode.com/users/{id}
     * usando HTTP Interface declarativa.
     * </p>
     *
     * @param externalUserId ID del usuario en JSONPlaceholder (1-10 son válidos)
     * @return Optional con los datos del usuario o empty si no existe
     */
    @Override
    public Optional<ExternalUserData> getUserById(Integer externalUserId) {
        logger.info("📡 [EXTERNAL API - HTTP Interface] Fetching user from JSONPlaceholder API - userId: {}", externalUserId);

        try {
            ExternalUserApiResponse response = httpInterface.getUserById(externalUserId);

            if (response == null) {
                logger.warn("⚠️ [EXTERNAL API] Received null response from JSONPlaceholder");
                return Optional.empty();
            }

            logger.info("✅ [EXTERNAL API - HTTP Interface] User fetched successfully - username: {}, email: {}",
                    response.username(), response.email());

            // Mapear de Infrastructure DTO a Application DTO
            ExternalUserData userData = mapToExternalUserData(response);
            return Optional.of(userData);

        } catch (RestClientException e) {
            logger.error("❌ [EXTERNAL API - HTTP Interface] Error calling JSONPlaceholder API: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Realiza un POST a https://jsonplaceholder.typicode.com/users
     * usando HTTP Interface declarativa.
     * (Nota: JSONPlaceholder simula la creación pero no persiste los datos)
     * </p>
     *
     * @param name  Nombre del usuario
     * @param email Email del usuario
     * @return Datos del usuario "creado"
     */
    @Override
    public ExternalUserData createExternalUser(String name, String email) {
        logger.info("📤 [EXTERNAL API - HTTP Interface] Creating user in JSONPlaceholder API - name: {}, email: {}", name, email);

        try {
            ExternalUserApiCreateRequest request = new ExternalUserApiCreateRequest(name, email);

            ExternalUserApiResponse response = httpInterface.createUser(request);

            if (response == null) {
                logger.error("❌ [EXTERNAL API] Received null response when creating user");
                throw new RuntimeException("Failed to create external user: null response");
            }

            logger.info("✅ [EXTERNAL API - HTTP Interface] User created successfully - id: {}", response.id());

            return mapToExternalUserData(response);

        } catch (RestClientException e) {
            logger.error("❌ [EXTERNAL API - HTTP Interface] Error creating user in JSONPlaceholder: {}", e.getMessage(), e);
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
