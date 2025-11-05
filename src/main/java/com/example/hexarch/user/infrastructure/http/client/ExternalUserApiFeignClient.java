package com.example.hexarch.user.infrastructure.http.client;

import com.example.hexarch.user.application.port.ExternalUserApiClient;
import com.example.hexarch.user.infrastructure.http.client.dto.ExternalUserApiCreateRequest;
import com.example.hexarch.user.infrastructure.http.client.dto.ExternalUserApiResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

/**
 * Implementación del Output Port ExternalUserApiClient usando OpenFeign (DECLARATIVO).
 * <p>
 * ⚠️ <strong>IMPORTANTE (2025): FeignClient está en "Maintenance Mode"</strong><br/>
 * Spring recomienda usar <strong>HTTP Interface (@HttpExchange)</strong> para proyectos nuevos.
 * Esta implementación se mantiene por:
 * <ul>
 *   <li>Compatibilidad con microservicios Spring Cloud existentes</li>
 *   <li>Propósitos educativos y comparación</li>
 *   <li>Todavía funcional y soportado (NO está deprecado)</li>
 * </ul>
 * </p>
 *
 * <h3>🎯 Tres Opciones en Este Proyecto:</h3>
 * <p>
 * Este proyecto implementa TRES opciones para comparar:
 * <ul>
 *   <li><strong>ExternalUserApiHttpInterfaceAdapter</strong> (⭐ RECOMENDADO - @Primary): Cliente DECLARATIVO nativo de Spring 6</li>
 *   <li><strong>ExternalUserApiFeignClient</strong> (esta interface): Cliente DECLARATIVO tradicional (maintenance mode)</li>
 *   <li><strong>ExternalUserApiRestClient</strong>: Cliente IMPERATIVO con control total</li>
 * </ul>
 * Por defecto se usa <strong>HTTP Interface</strong> (@Primary) como la opción moderna recomendada.
 * </p>
 *
 * <h3>Arquitectura Hexagonal:</h3>
 * <ul>
 *   <li>Implementa el <strong>Output Port</strong> ExternalUserApiClient</li>
 *   <li>Pertenece a la capa de <strong>Infrastructure</strong></li>
 *   <li>Usa tecnología específica (OpenFeign)</li>
 *   <li>Puede ser reemplazado por otra implementación sin afectar Application/Domain</li>
 * </ul>
 *
 * <h3>Ventajas de FeignClient:</h3>
 * <ul>
 *   <li>✅ Código muy limpio (solo interface, sin implementación)</li>
 *   <li>✅ Menos código boilerplate (~80% menos líneas)</li>
 *   <li>✅ Fácil de mantener (cambios en API solo actualizan interface)</li>
 *   <li>✅ Integración con Spring Cloud (service discovery, load balancing)</li>
 *   <li>✅ Circuit breaker y retry integrados</li>
 * </ul>
 *
 * <h3>Desventajas de FeignClient:</h3>
 * <ul>
 *   <li>❌ Dependencia adicional (~2-3 MB + transitivas)</li>
 *   <li>❌ "Magia" (implementación oculta, debugging más difícil)</li>
 *   <li>❌ Menos control sobre HTTP (para casos edge)</li>
 * </ul>
 *
 * <h3>⚠️ ¿Por qué YA NO es @Primary?</h3>
 * <p>
 * FeignClient fue reemplazado por <strong>HTTP Interface como @Primary</strong> porque:
 * <ul>
 *   <li>⚠️ FeignClient está en <strong>maintenance mode</strong></li>
 *   <li>✅ HTTP Interface es la opción <strong>recomendada oficialmente</strong> por Spring</li>
 *   <li>✅ HTTP Interface es <strong>nativa</strong> (sin dependencias adicionales)</li>
 *   <li>✅ HTTP Interface tiene <strong>mejor performance</strong></li>
 * </ul>
 * </p>
 * <p>
 * <strong>Para usar FeignClient ahora necesitas @Qualifier explícito</strong>:
 * </p>
 * <pre>{@code
 * // Inyección con FeignClient (requiere @Qualifier ahora)
 * public CreateUserService(@Qualifier("feignClient") ExternalUserApiClient apiClient) {
 *     this.apiClient = apiClient;
 * }
 * }</pre>
 * <p>
 * <strong>USO RECOMENDADO (2025)</strong>: HTTP Interface es la mejor opción:
 * <ul>
 *   <li>✅ Nativo de Spring (sin dependencias)</li>
 *   <li>✅ Recomendado oficialmente</li>
 *   <li>✅ Mismo estilo declarativo</li>
 *   <li>✅ Performance óptimo</li>
 * </ul>
 * Solo usa FeignClient si necesitas Spring Cloud (Eureka, service discovery, etc.).
 * </p>
 *
 * <h3>Cómo cambiar a otra implementación:</h3>
 * <pre>{@code
 * // Opción 1: Cambiar a HTTP Interface (RECOMENDADO)
 * @Autowired
 * @Qualifier("httpInterface")
 * private ExternalUserApiClient apiClient;
 *
 * // Opción 2: Cambiar a RestClient (para control total)
 * @Autowired
 * @Qualifier("restClient")
 * private ExternalUserApiClient apiClient;
 *
 * // Opción 3: En constructor (recomendado)
 * public CreateUserService(@Qualifier("httpInterface") ExternalUserApiClient apiClient) {
 *     this.apiClient = apiClient;  // HTTP Interface
 * }
 * }</pre>
 *
 * <h3>Configuración:</h3>
 * <ul>
 *   <li>URL base: ${external-api.jsonplaceholder.base-url}</li>
 *   <li>Timeouts: Configurados en FeignClientConfig</li>
 *   <li>Error handling: FeignErrorDecoder custom</li>
 *   <li>Logging: Configurado en application.yml</li>
 * </ul>
 *
 * @see ExternalUserApiClient
 * @see ExternalUserApiHttpInterfaceAdapter - Opción RECOMENDADA (HTTP Interface)
 * @see ExternalUserApiRestClient - Opción alternativa (control total)
 * @see com.example.hexarch.user.infrastructure.config.FeignClientConfig
 */
@FeignClient(
        name = "jsonPlaceholderApi",
        url = "${external-api.jsonplaceholder.base-url:https://jsonplaceholder.typicode.com}",
        configuration = com.example.hexarch.user.infrastructure.config.FeignClientConfig.class
)
@Qualifier("feignClient")
public interface ExternalUserApiFeignClient extends ExternalUserApiClient {

    /**
     * {@inheritDoc}
     * <p>
     * Implementación con Feign: GET /users/{id}
     * </p>
     */
    @Override
    @GetMapping("/users/{id}")
    default Optional<ExternalUserData> getUserById(Integer userId) {
        try {
            ExternalUserApiResponse response = getUserByIdInternal(userId);
            return Optional.of(mapToExternalUserData(response));
        } catch (Exception e) {
            // 404 o cualquier error retorna empty
            return Optional.empty();
        }
    }

    /**
     * Método interno para llamar a la API.
     * Feign genera la implementación automáticamente.
     */
    @GetMapping("/users/{id}")
    ExternalUserApiResponse getUserByIdInternal(@PathVariable("id") Integer userId);

    /**
     * {@inheritDoc}
     * <p>
     * Implementación con Feign: POST /users
     * </p>
     */
    @Override
    @PostMapping("/users")
    default ExternalUserData createExternalUser(String name, String email) {
        ExternalUserApiCreateRequest request = new ExternalUserApiCreateRequest(
                name,
                email
        );

        ExternalUserApiResponse response = createUserInternal(request);
        return mapToExternalUserData(response);
    }

    /**
     * Método interno para crear usuario.
     * Feign genera la implementación automáticamente.
     */
    @PostMapping("/users")
    ExternalUserApiResponse createUserInternal(@RequestBody ExternalUserApiCreateRequest request);

    /**
     * Mapea response de JSONPlaceholder a nuestro dominio.
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
