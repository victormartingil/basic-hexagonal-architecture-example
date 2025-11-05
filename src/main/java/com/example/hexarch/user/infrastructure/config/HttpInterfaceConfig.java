package com.example.hexarch.user.infrastructure.config;

import com.example.hexarch.user.infrastructure.http.client.ExternalUserApiHttpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Configuración de HTTP Interface (Spring Framework 6+ / Spring Boot 3+).
 * <p>
 * Esta configuración crea un proxy dinámico para {@link ExternalUserApiHttpInterface}
 * que usa {@link RestClient} como backend para realizar las llamadas HTTP.
 * </p>
 *
 * <h3>🎯 ¿Qué es HTTP Interface?</h3>
 * <p>
 * HTTP Interface es la forma NATIVA y MODERNA de Spring Framework 6 para crear
 * clientes HTTP declarativos. Es similar a FeignClient pero:
 * </p>
 * <ul>
 *   <li>✅ <strong>Sin dependencias adicionales</strong> - Parte del core de Spring</li>
 *   <li>✅ <strong>Activamente mantenida</strong> - Recomendada oficialmente</li>
 *   <li>✅ <strong>Flexible</strong> - Usa RestClient (síncrono) o WebClient (reactivo) como backend</li>
 *   <li>✅ <strong>Performance óptimo</strong> - Sin overhead de librerías terceras</li>
 * </ul>
 *
 * <h3>🏗️ ¿Cómo funciona?</h3>
 * <pre>
 * 1. Defines una interface con @HttpExchange/@GetExchange/@PostExchange
 *    ↓
 * 2. HttpServiceProxyFactory crea un PROXY dinámico de esa interface
 *    ↓
 * 3. El proxy usa RestClient (o WebClient) para hacer las llamadas HTTP reales
 *    ↓
 * 4. Inyectas y usas la interface como un bean normal
 * </pre>
 *
 * <h3>📐 Arquitectura de esta Configuración:</h3>
 * <pre>
 * RestClientConfig
 *   ↓ crea
 * RestClient (jsonPlaceholderRestClient)
 *   ↓ usado por
 * RestClientAdapter
 *   ↓ usado por
 * HttpServiceProxyFactory
 *   ↓ crea proxy de
 * ExternalUserApiHttpInterface
 *   ↓ inyectado en
 * ExternalUserApiHttpInterfaceAdapter
 * </pre>
 *
 * <h3>🔧 Componentes Clave:</h3>
 * <ul>
 *   <li><strong>RestClient</strong>: Cliente HTTP real que hace las llamadas</li>
 *   <li><strong>RestClientAdapter</strong>: Adaptador que conecta RestClient con HttpServiceProxyFactory</li>
 *   <li><strong>HttpServiceProxyFactory</strong>: Genera el proxy dinámico de la interface</li>
 *   <li><strong>ExternalUserApiHttpInterface</strong>: Interface declarativa con @GetExchange/@PostExchange</li>
 * </ul>
 *
 * <h3>✨ Ventajas de HTTP Interface vs FeignClient:</h3>
 * <table>
 *   <tr>
 *     <th>Aspecto</th>
 *     <th>HTTP Interface</th>
 *     <th>FeignClient</th>
 *   </tr>
 *   <tr>
 *     <td><strong>Configuración</strong></td>
 *     <td>Manual (como esta clase)</td>
 *     <td>Auto-configuración con @EnableFeignClients</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Dependencias</strong></td>
 *     <td>✅ Ninguna extra</td>
 *     <td>❌ spring-cloud-openfeign requerida</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Backend HTTP</strong></td>
 *     <td>RestClient o WebClient (tu elección)</td>
 *     <td>Feign client (fijo)</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Observability</strong></td>
 *     <td>✅ Nativa (si RestClient tiene Micrometer)</td>
 *     <td>⚠️ Requiere configuración adicional</td>
 *   </tr>
 *   <tr>
 *     <td><strong>Control</strong></td>
 *     <td>✅ Total (configuras el RestClient como quieras)</td>
 *     <td>⚠️ Limitado a opciones de Feign</td>
 *   </tr>
 * </table>
 *
 * <h3>🆚 HTTP Interface vs WebClient Reactivo:</h3>
 * <p>
 * Si necesitas reactividad, puedes cambiar fácilmente el backend:
 * </p>
 * <pre>{@code
 * // Versión Síncrona (esta clase - con RestClient)
 * @Bean
 * public ExternalUserApiHttpInterface jsonPlaceholderHttpInterface(
 *         @Qualifier("jsonPlaceholderRestClient") RestClient restClient) {
 *     RestClientAdapter adapter = RestClientAdapter.create(restClient);
 *     HttpServiceProxyFactory factory = HttpServiceProxyFactory
 *             .builderFor(adapter).build();
 *     return factory.createClient(ExternalUserApiHttpInterface.class);
 * }
 *
 * // Versión Reactiva (con WebClient) - solo cambiar esto:
 * @Bean
 * public ExternalUserApiHttpInterface jsonPlaceholderHttpInterface(
 *         WebClient webClient) {
 *     WebClientAdapter adapter = WebClientAdapter.create(webClient);
 *     HttpServiceProxyFactory factory = HttpServiceProxyFactory
 *             .builderFor(adapter).build();
 *     return factory.createClient(ExternalUserApiHttpInterface.class);
 * }
 * }</pre>
 *
 * <h3>🎓 Ejemplo de Uso:</h3>
 * <pre>{@code
 * // 1. Inyectar la interface (Spring crea el proxy automáticamente)
 * @Service
 * public class UserService {
 *     private final ExternalUserApiHttpInterface httpInterface;
 *
 *     public UserService(ExternalUserApiHttpInterface httpInterface) {
 *         this.httpInterface = httpInterface;
 *     }
 *
 *     public User getUser(Integer userId) {
 *         // 2. Usar como un objeto normal - Spring hace la magia
 *         return httpInterface.getUserById(userId);
 *     }
 * }
 * }</pre>
 *
 * <h3>⚙️ Configuración Avanzada:</h3>
 * <p>
 * Si necesitas personalizar más el comportamiento, puedes configurar:
 * </p>
 * <ul>
 *   <li><strong>Timeouts</strong>: En el RestClient (ver RestClientConfig)</li>
 *   <li><strong>Headers por defecto</strong>: En el RestClient</li>
 *   <li><strong>Interceptores</strong>: En el RestClient</li>
 *   <li><strong>Error handling</strong>: En el RestClient o en el adaptador</li>
 *   <li><strong>Observability</strong>: Agregar ObservationRegistry al RestClient</li>
 * </ul>
 *
 * <h3>📚 Referencias:</h3>
 * <ul>
 *   <li>Spring Docs: <a href="https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface">HTTP Interface</a></li>
 *   <li>Ver: {@link ExternalUserApiHttpInterface} - Interface declarativa</li>
 *   <li>Ver: {@link ExternalUserApiHttpInterfaceAdapter} - Adaptador que usa la interface</li>
 *   <li>Ver: {@link RestClientConfig} - Configuración del RestClient backend</li>
 *   <li>Ver: docs/18-HTTP-Clients-Comparison-Guide.md - Guía completa de comparación</li>
 * </ul>
 *
 * @see ExternalUserApiHttpInterface
 * @see org.springframework.web.service.invoker.HttpServiceProxyFactory
 * @see org.springframework.web.client.support.RestClientAdapter
 * @since Spring Framework 6.0 (Spring Boot 3.0+)
 */
@Configuration
public class HttpInterfaceConfig {

    private static final Logger logger = LoggerFactory.getLogger(HttpInterfaceConfig.class);

    /**
     * Crea el proxy de HTTP Interface para JSONPlaceholder API.
     * <p>
     * Este bean es inyectable y se puede usar como un cliente HTTP normal.
     * Spring genera dinámicamente la implementación usando el RestClient configurado.
     * </p>
     *
     * <h3>🔧 Flujo de creación:</h3>
     * <ol>
     *   <li>Inyecta el RestClient configurado (de RestClientConfig)</li>
     *   <li>Crea un RestClientAdapter que conecta RestClient con HttpServiceProxyFactory</li>
     *   <li>Crea el HttpServiceProxyFactory con el adapter</li>
     *   <li>Genera el proxy dinámico de ExternalUserApiHttpInterface</li>
     * </ol>
     *
     * <h3>💡 ¿Por qué usar @Qualifier?</h3>
     * <p>
     * Usamos @Qualifier("jsonPlaceholderRestClient") porque tenemos múltiples RestClient beans
     * en el proyecto. Esto asegura que inyectemos el correcto (el configurado para JSONPlaceholder).
     * </p>
     *
     * @param restClient RestClient configurado para JSONPlaceholder API
     * @return Proxy dinámico de ExternalUserApiHttpInterface
     * @see ExternalUserApiHttpInterface
     * @see RestClientConfig#jsonPlaceholderRestClient()
     */
    @Bean
    public ExternalUserApiHttpInterface jsonPlaceholderHttpInterface(
            @Qualifier("jsonPlaceholderRestClient") RestClient restClient) {

        logger.info("🔧 Creating HTTP Interface proxy for ExternalUserApiHttpInterface...");

        // 1. Crear adaptador que conecta RestClient con HttpServiceProxyFactory
        RestClientAdapter adapter = RestClientAdapter.create(restClient);

        // 2. Crear factory que genera proxies dinámicos
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();

        // 3. Generar el proxy de la interface
        ExternalUserApiHttpInterface httpInterface = factory.createClient(ExternalUserApiHttpInterface.class);

        logger.info("✅ HTTP Interface proxy created successfully for ExternalUserApiHttpInterface");
        logger.info("📡 Backend: RestClient (jsonPlaceholderRestClient)");
        logger.info("🎯 This is the MODERN and RECOMMENDED approach for Spring Boot 3+");

        return httpInterface;
    }
}
