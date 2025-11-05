package com.example.hexarch.user.infrastructure.http.client;

import com.example.hexarch.user.infrastructure.http.client.dto.ExternalUserApiCreateRequest;
import com.example.hexarch.user.infrastructure.http.client.dto.ExternalUserApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * HTTP Interface declarativa para JSONPlaceholder API usando @HttpExchange (Spring 6+).
 * <p>
 * Esta es la forma MODERNA y RECOMENDADA de crear clientes HTTP declarativos en Spring Boot 3+.
 * Es NATIVA de Spring Framework 6 (no requiere dependencias adicionales como FeignClient).
 * </p>
 *
 * <h3>🎯 ¿Por qué HTTP Interface es la opción RECOMENDADA en 2025?</h3>
 * <ul>
 *   <li>✅ <strong>Nativa de Spring</strong> - No requiere spring-cloud-openfeign</li>
 *   <li>✅ <strong>Moderna</strong> - Introducida en Spring Framework 6.0 (2022)</li>
 *   <li>✅ <strong>Activamente mantenida</strong> - Parte del core de Spring</li>
 *   <li>✅ <strong>Flexible</strong> - Funciona con RestClient (síncrono) o WebClient (reactivo)</li>
 *   <li>✅ <strong>Declarativa</strong> - Sintaxis limpia similar a FeignClient</li>
 *   <li>✅ <strong>Sin overhead</strong> - Usa RestClient/WebClient directamente como backend</li>
 * </ul>
 *
 * <h3>🆚 Comparación: HTTP Interface vs FeignClient vs RestClient</h3>
 * <table>
 *   <tr>
 *     <th>Característica</th>
 *     <th>HTTP Interface</th>
 *     <th>FeignClient</th>
 *     <th>RestClient</th>
 *   </tr>
 *   <tr>
 *     <td><strong>Estado</strong></td>
 *     <td>✅ Activo, recomendado</td>
 *     <td>⚠️ Maintenance mode</td>
 *     <td>✅ Activo</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Estilo</strong></td>
 *     <td>Declarativo (interface)</td>
 *     <td>Declarativo (interface)</td>
 *     <td>Imperativo (código)</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Dependencias</strong></td>
 *     <td>Ninguna extra</td>
 *     <td>spring-cloud-openfeign</td>
 *     <td>Ninguna extra</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Backend</strong></td>
 *     <td>RestClient o WebClient</td>
 *     <td>Feign (propio)</td>
 *     <td>N/A (es el cliente)</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Spring Cloud</strong></td>
 *     <td>❌ No integrado</td>
 *     <td>✅ Integrado</td>
 *     <td>❌ No integrado</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Uso recomendado</strong></td>
 *     <td>Spring Boot 3+ general</td>
 *     <td>Microservicios Spring Cloud</td>
 *     <td>Control total HTTP</td>
 *   </tr>
 * </table>
 *
 * <h3>📐 Arquitectura: HTTP Interface como Adaptador</h3>
 * <pre>
 * Application Layer (Port)
 *        ↓
 * ExternalUserApiClient (interface/port)
 *        ↓
 * ExternalUserApiHttpInterfaceAdapter (implementa el port)
 *        ↓ usa
 * ExternalUserApiHttpInterface (esta interface)
 *        ↓ proxy generado por
 * HttpServiceProxyFactory
 *        ↓ usa
 * RestClient (configurado en RestClientConfig)
 * </pre>
 *
 * <h3>🔧 ¿Cómo funciona internamente?</h3>
 * <ol>
 *   <li>Defines una interface con anotaciones @GetExchange, @PostExchange, etc.</li>
 *   <li>Spring crea un PROXY dinámico usando {@link org.springframework.web.service.invoker.HttpServiceProxyFactory}</li>
 *   <li>El proxy usa RestClient o WebClient como backend para hacer las llamadas HTTP reales</li>
 *   <li>Todo es declarativo - no escribes implementación</li>
 * </ol>
 *
 * <h3>✨ Ventajas sobre FeignClient:</h3>
 * <ul>
 *   <li>✅ <strong>Sin dependencias adicionales</strong> (~2-3 MB menos)</li>
 *   <li>✅ <strong>Recomendada oficialmente</strong> por Spring para Spring Boot 3+</li>
 *   <li>✅ <strong>Mejor soporte futuro</strong> - FeignClient está en maintenance mode</li>
 *   <li>✅ <strong>Usa RestClient directamente</strong> - mejor observability y debugging</li>
 *   <li>✅ <strong>Más flexible</strong> - puedes cambiar entre RestClient y WebClient fácilmente</li>
 * </ul>
 *
 * <h3>⚠️ Cuándo NO usar HTTP Interface (usar FeignClient):</h3>
 * <ul>
 *   <li>Si usas <strong>Spring Cloud</strong> con service discovery (Eureka, Consul)</li>
 *   <li>Si necesitas <strong>client-side load balancing</strong> automático</li>
 *   <li>Si ya tienes FeignClient en producción y funciona bien</li>
 * </ul>
 *
 * <h3>📚 Aprende más:</h3>
 * <ul>
 *   <li>Ver: {@link ExternalUserApiHttpInterfaceAdapter} - Implementación del adaptador</li>
 *   <li>Ver: {@link com.example.hexarch.user.infrastructure.config.HttpInterfaceConfig} - Configuración</li>
 *   <li>Ver: {@link ExternalUserApiFeignClient} - Comparar con FeignClient</li>
 *   <li>Ver: {@link ExternalUserApiRestClient} - Comparar con RestClient</li>
 *   <li>Docs: docs/18-HTTP-Clients-Comparison-Guide.md</li>
 * </ul>
 *
 * @since Spring Framework 6.0 (Spring Boot 3.0+)
 * @see org.springframework.web.service.annotation.HttpExchange
 * @see org.springframework.web.service.invoker.HttpServiceProxyFactory
 */
@HttpExchange
public interface ExternalUserApiHttpInterface {

    /**
     * Obtiene un usuario por ID desde JSONPlaceholder API.
     * <p>
     * Mapeo: GET https://jsonplaceholder.typicode.com/users/{id}
     * </p>
     *
     * @param userId ID del usuario (1-10 son válidos en JSONPlaceholder)
     * @return Response de JSONPlaceholder con datos del usuario
     */
    @GetExchange("/users/{id}")
    ExternalUserApiResponse getUserById(@PathVariable("id") Integer userId);

    /**
     * Crea un nuevo usuario en JSONPlaceholder API.
     * <p>
     * Mapeo: POST https://jsonplaceholder.typicode.com/users
     * </p>
     * <p>
     * Nota: JSONPlaceholder simula la creación pero no persiste los datos.
     * </p>
     *
     * @param request Datos del usuario a crear
     * @return Response de JSONPlaceholder con el usuario "creado"
     */
    @PostExchange("/users")
    ExternalUserApiResponse createUser(@RequestBody ExternalUserApiCreateRequest request);
}
