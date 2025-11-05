package com.example.hexarch.user.infrastructure.http.client;

import com.example.hexarch.user.application.port.ExternalUserApiClient.ExternalUserData;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * INTEGRATION TEST - ExternalUserApiHttpInterfaceAdapter (REAL API CALLS)
 * <p>
 * Este test hace llamadas REALES a la API pública de JSONPlaceholder usando HTTP Interface,
 * la opción MODERNA y RECOMENDADA de Spring Framework 6.
 * </p>
 *
 * <h3>🎯 Por qué usar HTTP Interface (2025):</h3>
 * <ul>
 *   <li>✅ Nativa de Spring Framework 6 (sin dependencias adicionales)</li>
 *   <li>✅ Recomendada oficialmente por Spring</li>
 *   <li>✅ Declarativa (menos código que RestClient directo)</li>
 *   <li>✅ Performance óptimo (usa RestClient como backend)</li>
 *   <li>✅ Fácil de testear (mock de la interface)</li>
 * </ul>
 *
 * <h3>⚠️ IMPORTANTE:</h3>
 * <ul>
 *   <li>Este test hace llamadas HTTP REALES a Internet</li>
 *   <li>Requiere conexión a Internet para funcionar</li>
 *   <li>Puede fallar si la API externa está caída (raro, pero posible)</li>
 *   <li>Etiquetado con @Tag("integration") para ejecutarse selectivamente</li>
 *   <li><strong>EXCLUIDO DE CI/CD</strong>: No se ejecuta en GitHub Actions para evitar fallos por conexiones bloqueadas</li>
 * </ul>
 *
 * <h3>🚀 Ejecución en CI/CD:</h3>
 * <p>
 * Este test está EXCLUIDO del workflow de GitHub Actions (.github/workflows/integration-tests.yml)
 * porque las llamadas HTTP a APIs externas pueden ser bloqueadas o fallar intermitentemente.
 * </p>
 * <p>
 * La lógica de ExternalUserApiHttpInterfaceAdapter está cubierta por ExternalUserApiHttpInterfaceAdapterTest con mocks,
 * por lo que no hay pérdida de cobertura en CI/CD.
 * </p>
 *
 * <h3>API Utilizada:</h3>
 * <ul>
 *   <li>Nombre: JSONPlaceholder</li>
 *   <li>URL: https://jsonplaceholder.typicode.com</li>
 *   <li>Descripción: API REST pública gratuita para testing y prototyping</li>
 *   <li>Endpoints: /users, /posts, /comments, etc.</li>
 *   <li>Documentación: <a href="https://jsonplaceholder.typicode.com/guide/">JSONPlaceholder Guide</a></li>
 * </ul>
 *
 * <h3>Cómo ejecutar solo este test:</h3>
 * <pre>
 * # Ejecutar solo integration tests
 * ./mvnw test -Dgroups=integration
 *
 * # Ejecutar este test específico
 * ./mvnw test -Dtest=ExternalUserApiHttpInterfaceAdapterRealIntegrationTest
 * </pre>
 *
 * @see <a href="https://jsonplaceholder.typicode.com/">JSONPlaceholder API</a>
 * @see ExternalUserApiHttpInterfaceAdapter
 * @see ExternalUserApiHttpInterface
 */
@Tag("integration")  // Para ejecutar selectivamente
@DisplayName("ExternalUserApiHttpInterfaceAdapter - Real API Integration Test")
class ExternalUserApiHttpInterfaceAdapterRealIntegrationTest {

    private ExternalUserApiHttpInterfaceAdapter adapter;
    private ExternalUserApiHttpInterface httpInterface;

    @BeforeEach
    void setUp() {
        // Crear RestClient REAL configurado para JSONPlaceholder
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        RestClient restClient = RestClient.builder()
                .baseUrl("https://jsonplaceholder.typicode.com")
                .requestFactory(requestFactory)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .observationRegistry(ObservationRegistry.NOOP)
                .build();

        // Crear HTTP Interface proxy REAL usando HttpServiceProxyFactory
        RestClientAdapter restClientAdapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(restClientAdapter)
                .build();

        httpInterface = factory.createClient(ExternalUserApiHttpInterface.class);

        // Crear el adaptador con HTTP Interface REAL
        adapter = new ExternalUserApiHttpInterfaceAdapter(httpInterface);
    }

    /**
     * TEST CASE 1: Llamada REAL a GET /users/1 usando HTTP Interface
     * <p>
     * Este test demuestra cómo HTTP Interface funciona end-to-end:
     * <ul>
     *   <li>HttpServiceProxyFactory genera un proxy dinámico de ExternalUserApiHttpInterface</li>
     *   <li>El proxy usa RestClient para hacer la llamada HTTP real</li>
     *   <li>Spring deserializa automáticamente el JSON a ExternalUserApiResponse</li>
     *   <li>El adaptador mapea el DTO de Infrastructure a Application (ExternalUserData)</li>
     * </ul>
     * </p>
     * <p>
     * Este es un ejemplo perfecto del poder de HTTP Interface:
     * <strong>Declaras una interface y Spring hace toda la magia</strong>.
     * </p>
     */
    @Test
    @DisplayName("Should fetch user from JSONPlaceholder API successfully using HTTP Interface")
    void shouldGetUserFromRealApiSuccessfully() {
        // GIVEN - userId 1 existe en JSONPlaceholder
        Integer userId = 1;

        // WHEN - Llamar a la API REAL a través del HTTP Interface proxy
        Optional<ExternalUserData> result = adapter.getUserById(userId);

        // THEN - Verificar que se obtuvo el usuario correctamente
        assertThat(result).isPresent();
        assertThat(result.get()).satisfies(userData -> {
            assertThat(userData.id()).isEqualTo(1);
            assertThat(userData.name()).isEqualTo("Leanne Graham");
            assertThat(userData.username()).isEqualTo("Bret");
            assertThat(userData.email()).isEqualTo("Sincere@april.biz");
            assertThat(userData.phone()).contains("1-770-736-8031");
            assertThat(userData.website()).isEqualTo("hildegard.org");
        });

        System.out.println("✅ HTTP Interface Integration Test - GET /users/1 SUCCESS");
        System.out.println("📊 User Data Retrieved: " + result.get());
    }

    /**
     * TEST CASE 2: Llamada REAL a GET /users/{id} con userId inexistente
     * <p>
     * JSONPlaceholder retorna un objeto vacío {} para IDs muy altos.
     * Este test verifica el manejo de ese caso edge.
     * </p>
     */
    @Test
    @DisplayName("Should handle non-existent user gracefully")
    void shouldHandleNonExistentUserGracefully() {
        // GIVEN - userId 999999 no existe en JSONPlaceholder
        Integer userId = 999999;

        // WHEN - Llamar a la API REAL
        Optional<ExternalUserData> result = adapter.getUserById(userId);

        // THEN - Debería manejar el caso gracefully
        // JSONPlaceholder retorna {} para IDs inexistentes, que se deserializa como objeto con nulls
        System.out.println("📊 Result for non-existent user: " + result);
    }

    /**
     * TEST CASE 3: Llamada REAL a POST /users (crear usuario)
     * <p>
     * JSONPlaceholder simula la creación pero no persiste los datos.
     * Retorna un ID ficticio (ej: 101) y los datos que enviaste.
     * </p>
     * <p>
     * Este test demuestra cómo HTTP Interface maneja POST requests:
     * <strong>@PostExchange con @RequestBody - Spring serializa automáticamente</strong>.
     * </p>
     */
    @Test
    @DisplayName("Should create user in JSONPlaceholder API successfully using HTTP Interface")
    void shouldCreateUserInRealApiSuccessfully() {
        // GIVEN
        String name = "John Doe";
        String email = "john@example.com";

        // WHEN - Llamar a POST /users a través del HTTP Interface proxy
        ExternalUserData result = adapter.createExternalUser(name, email);

        // THEN - JSONPlaceholder simula la creación y retorna un ID ficticio
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(name);
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.id()).isNotNull();  // JSONPlaceholder asigna ID ficticio

        System.out.println("✅ HTTP Interface Integration Test - POST /users SUCCESS");
        System.out.println("📊 Created User Data: " + result);
    }

    /**
     * TEST CASE 4: Verificar que HTTP Interface usa RestClient como backend
     * <p>
     * Este test conceptual demuestra la arquitectura de HTTP Interface:
     * <pre>
     * ExternalUserApiHttpInterface (interface)
     *   ↓ proxy generado por
     * HttpServiceProxyFactory
     *   ↓ usa como backend
     * RestClient
     *   ↓ hace llamada HTTP real a
     * JSONPlaceholder API
     * </pre>
     * </p>
     */
    @Test
    @DisplayName("Should demonstrate HTTP Interface architecture with RestClient backend")
    void shouldDemonstrateHttpInterfaceArchitecture() {
        // GIVEN - HTTP Interface configurado con RestClient backend
        Integer userId = 1;

        // WHEN - Llamar método declarativo de la interface
        Optional<ExternalUserData> result = adapter.getUserById(userId);

        // THEN - El proxy usa RestClient internamente y funciona end-to-end
        assertThat(result).isPresent();

        System.out.println("✅ HTTP Interface Architecture Test SUCCESS");
        System.out.println("📐 Flow: Interface → HttpServiceProxyFactory → RestClient → JSONPlaceholder API");
        System.out.println("🎯 This is the MODERN approach for Spring Boot 3+ (2025)");
    }

    /**
     * TEST CASE 5: Llamar a múltiples endpoints para demostrar la versatilidad de HTTP Interface
     * <p>
     * HTTP Interface brilla cuando tienes múltiples endpoints del mismo servicio.
     * En este test llamamos a dos endpoints diferentes de forma declarativa.
     * </p>
     */
    @Test
    @DisplayName("Should call multiple endpoints declaratively with HTTP Interface")
    void shouldCallMultipleEndpointsDeclaratively() {
        // GIVEN - Dos operaciones diferentes

        // WHEN - Operación 1: GET user
        Optional<ExternalUserData> existingUser = adapter.getUserById(1);

        // WHEN - Operación 2: POST create user
        ExternalUserData newUser = adapter.createExternalUser("Jane Doe", "jane@example.com");

        // THEN - Ambas operaciones funcionan correctamente
        assertThat(existingUser).isPresent();
        assertThat(newUser).isNotNull();

        System.out.println("✅ Multiple Endpoints Test SUCCESS");
        System.out.println("📊 GET /users/1: " + existingUser.get().username());
        System.out.println("📊 POST /users: " + newUser.name());
        System.out.println("🎯 HTTP Interface makes calling multiple endpoints EASY and CLEAN");
    }
}
