package com.example.hexarch.user.infrastructure.http.client;

import com.example.hexarch.user.application.port.ExternalUserApiClient.ExternalUserData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * INTEGRATION TEST - ExternalUserApiFeignClient (REAL API CALLS)
 * <p>
 * Este test hace llamadas REALES a la API pública de JSONPlaceholder usando FeignClient.
 * Demuestra que la integración funciona end-to-end con la implementación declarativa de Feign.
 * </p>
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
 * La lógica de ExternalUserApiFeignClient está cubierta por ExternalUserApiFeignClientTest,
 * por lo que no hay pérdida de cobertura en CI/CD.
 * </p>
 *
 * <h3>Comparación FeignClient vs RestClient:</h3>
 * <p>
 * Este test demuestra que ambas implementaciones (FeignClient y RestClient) funcionan de forma equivalente.
 * La diferencia está en:
 * </p>
 * <ul>
 *   <li><strong>FeignClient</strong>: Declarativo (solo interface), menos código</li>
 *   <li><strong>RestClient</strong>: Imperativo (código explícito), más control</li>
 * </ul>
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
 * ./mvnw test -Dtest=ExternalUserApiFeignClientRealIntegrationTest
 * </pre>
 *
 * @see <a href="https://jsonplaceholder.typicode.com/">JSONPlaceholder API</a>
 */
@Tag("integration")  // Para ejecutar selectivamente
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.flyway.enabled=false",
                "spring.jpa.hibernate.ddl-auto=none",
                "spring.datasource.url=jdbc:h2:mem:testdb",
                "external-api.jsonplaceholder.connect-timeout=5s",
                "external-api.jsonplaceholder.read-timeout=10s"
        }
)
@ActiveProfiles("test")
@DisplayName("ExternalUserApiFeignClient - Real API Integration Test")
class ExternalUserApiFeignClientRealIntegrationTest {

    @Autowired
    @Qualifier("feignClient")
    private ExternalUserApiFeignClient jsonPlaceholderFeignClient;

    /**
     * TEST CASE 1: Llamada REAL a GET /users/1
     * <p>
     * Este test hace una llamada HTTP REAL a la API de JSONPlaceholder usando FeignClient.
     * Verifica que:
     * <ul>
     *   <li>La API responde correctamente</li>
     *   <li>Los datos se deserializan correctamente</li>
     *   <li>El mapping Infrastructure DTO → Application DTO funciona</li>
     *   <li>FeignClient maneja la configuración de timeouts y logging correctamente</li>
     * </ul>
     * </p>
     */
    @Test
    @DisplayName("Should fetch REAL user from JSONPlaceholder API - GET /users/1")
    void shouldFetchRealUser_fromJsonPlaceholderApi() {
        // WHEN - Llamada REAL a la API usando FeignClient
        System.out.println("\n🌐 [FEIGN] Making REAL HTTP call to: https://jsonplaceholder.typicode.com/users/1");

        Optional<ExternalUserData> result = jsonPlaceholderFeignClient.getUserById(1);

        // THEN - Verificar que recibimos datos reales
        assertThat(result)
                .as("Should receive user data from real API")
                .isPresent();

        ExternalUserData userData = result.get();

        // Verificar datos conocidos del usuario 1 de JSONPlaceholder
        assertThat(userData.id()).isEqualTo(1);
        assertThat(userData.name()).isEqualTo("Leanne Graham");
        assertThat(userData.username()).isEqualTo("Bret");
        assertThat(userData.email()).isEqualTo("Sincere@april.biz");
        assertThat(userData.phone()).isEqualTo("1-770-736-8031 x56442");
        assertThat(userData.website()).isEqualTo("hildegard.org");

        System.out.println("✅ [FEIGN] Successfully fetched real user:");
        System.out.println("   ID: " + userData.id());
        System.out.println("   Name: " + userData.name());
        System.out.println("   Username: " + userData.username());
        System.out.println("   Email: " + userData.email());
        System.out.println("   Website: " + userData.website());
    }

    /**
     * TEST CASE 2: Llamada REAL a GET /users/10 (último usuario)
     * <p>
     * JSONPlaceholder tiene usuarios del 1 al 10.
     * Este test verifica que FeignClient funciona con diferentes IDs.
     * </p>
     */
    @Test
    @DisplayName("Should fetch REAL user from JSONPlaceholder API - GET /users/10")
    void shouldFetchDifferentRealUser() {
        // WHEN - Llamada REAL a la API (usuario 10)
        System.out.println("\n🌐 [FEIGN] Making REAL HTTP call to: https://jsonplaceholder.typicode.com/users/10");

        Optional<ExternalUserData> result = jsonPlaceholderFeignClient.getUserById(10);

        // THEN - Verificar que recibimos datos
        assertThat(result).isPresent();

        ExternalUserData userData = result.get();

        // Verificar datos del usuario 10
        assertThat(userData.id()).isEqualTo(10);
        assertThat(userData.name()).isEqualTo("Clementina DuBuque");
        assertThat(userData.username()).isEqualTo("Moriah.Stanton");

        System.out.println("✅ [FEIGN] Successfully fetched user 10:");
        System.out.println("   Name: " + userData.name());
        System.out.println("   Username: " + userData.username());
    }

    /**
     * TEST CASE 3: Usuario no existente (404)
     * <p>
     * JSONPlaceholder solo tiene usuarios del 1 al 10.
     * El ID 999 no existe y debe retornar 404.
     * Este test verifica que el ErrorDecoder custom de FeignClientConfig maneja correctamente el 404.
     * </p>
     */
    @Test
    @DisplayName("Should return empty when user does not exist - GET /users/999")
    void shouldReturnEmpty_whenUserDoesNotExist() {
        // WHEN - Llamada REAL a usuario inexistente
        System.out.println("\n🌐 [FEIGN] Making REAL HTTP call to: https://jsonplaceholder.typicode.com/users/999");

        Optional<ExternalUserData> result = jsonPlaceholderFeignClient.getUserById(999);

        // THEN - Debe retornar empty (404 Not Found)
        assertThat(result)
                .as("Should return empty for non-existent user")
                .isEmpty();

        System.out.println("✅ [FEIGN] Correctly handled 404 - returned Optional.empty()");
    }

    /**
     * TEST CASE 4: Llamada REAL a POST /users
     * <p>
     * JSONPlaceholder simula la creación (no persiste realmente).
     * Retorna el objeto creado con un ID generado.
     * Este test verifica que FeignClient maneja POST requests correctamente.
     * </p>
     */
    @Test
    @DisplayName("Should create user (simulated) - POST /users")
    void shouldCreateUser_simulatedByJsonPlaceholder() {
        // WHEN - Llamada REAL de creación
        System.out.println("\n🌐 [FEIGN] Making REAL HTTP POST to: https://jsonplaceholder.typicode.com/users");

        ExternalUserData result = jsonPlaceholderFeignClient.createExternalUser(
                "Test User",
                "test@example.com"
        );

        // THEN - JSONPlaceholder retorna el usuario "creado" con ID 11
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(11);  // JSONPlaceholder siempre retorna 11 para POST
        assertThat(result.name()).isEqualTo("Test User");
        assertThat(result.email()).isEqualTo("test@example.com");

        System.out.println("✅ [FEIGN] Successfully called POST /users (simulated creation):");
        System.out.println("   ID: " + result.id());
        System.out.println("   Name: " + result.name());
        System.out.println("   Email: " + result.email());
        System.out.println("\n   ⚠️ Note: JSONPlaceholder simulates creation but doesn't persist data");
    }

    /**
     * TEST CASE 5: Verificar que todos los usuarios (1-10) son accesibles
     * <p>
     * Este test verifica que podemos acceder a todos los usuarios disponibles usando FeignClient.
     * JSONPlaceholder proporciona 10 usuarios de prueba.
     * </p>
     */
    @Test
    @DisplayName("Should fetch all available users (1-10) from real API")
    void shouldFetchAllAvailableUsers() {
        System.out.println("\n🌐 [FEIGN] Fetching all users from JSONPlaceholder API...");

        int successCount = 0;

        // Intentar obtener usuarios del 1 al 10
        for (int userId = 1; userId <= 10; userId++) {
            Optional<ExternalUserData> result = jsonPlaceholderFeignClient.getUserById(userId);

            if (result.isPresent()) {
                successCount++;
                ExternalUserData user = result.get();
                System.out.printf("   ✅ User %d: %s (@%s)%n",
                        user.id(), user.name(), user.username());
            }
        }

        // Verificar que obtuvimos los 10 usuarios
        assertThat(successCount)
                .as("Should successfully fetch all 10 users from JSONPlaceholder")
                .isEqualTo(10);

        System.out.println("\n✅ [FEIGN] Successfully fetched all 10 users from real API");
    }
}
