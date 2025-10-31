package com.example.hexarch.e2e;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * KARATE E2E TESTS - TESTCONTAINERS MODE (RECOMENDADO) 🚀
 *
 * Este test ejecuta E2E completos usando Testcontainers + @SpringBootTest.
 * Es la forma RECOMENDADA de ejecutar E2E tests porque:
 *
 * ✅ **Todo automático**: Un solo comando lo ejecuta todo
 * ✅ **Gratis en GitHub Actions**: Docker viene preinstalado
 * ✅ **Auto-cleanup**: Contenedores se eliminan automáticamente
 * ✅ **Rápido**: ~5 minutos (vs ~10 min Docker Mode)
 * ✅ **No requiere setup manual**: No necesitas 3 terminales
 * ✅ **Puertos aleatorios**: No hay conflictos de puertos
 *
 * CÓMO FUNCIONA:
 *
 * 1. Testcontainers levanta PostgreSQL y Kafka en contenedores Docker
 * 2. Spring Boot arranca la aplicación con webEnvironment = RANDOM_PORT
 * 3. La app se conecta a los contenedores de Testcontainers
 * 4. Karate ejecuta tests E2E contra http://localhost:{randomPort}
 * 5. Todo se limpia automáticamente al terminar
 *
 * ARQUITECTURA:
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │ KARATE E2E TESTS (Este archivo)                            │
 * │ └─ HTTP Requests → http://localhost:{randomPort}           │
 * └─────────────────────┬───────────────────────────────────────┘
 *                       ▼
 * ┌─────────────────────────────────────────────────────────────┐
 * │ SPRING BOOT APP (@SpringBootTest RANDOM_PORT)              │
 * │ └─ Controllers, Services, Repositories                     │
 * │ └─ Kafka DESHABILITADO (no es necesario para tests REST)  │
 * │ └─ Security DESHABILITADO (valida funcionalidad, no auth) │
 * └─────────────┬───────────────────────────────────────────────┘
 *               ▼
 * ┌──────────────────────┐
 * │ PostgreSQL Container │
 * │ (Testcontainers)     │
 * └──────────────────────┘
 *
 * CÓMO EJECUTAR:
 *
 * Opción 1: Maven (un solo comando) 🎯 RECOMENDADO
 * ```bash
 * ./mvnw test -Pe2e-tests -Dkarate.env=local
 * ```
 *
 * Opción 2: Desde IDE (IntelliJ / VS Code)
 * - Click derecho en esta clase → Run 'KarateE2ETestcontainersTest'
 * - Espera ~30-60 segundos para que Testcontainers arranque
 *
 * Opción 3: Solo tests específicos
 * ```bash
 * ./mvnw test -Dtest=KarateE2ETestcontainersTest -Dkarate.env=local
 * ```
 *
 * QUÉ TESTEA:
 * - Flujos end-to-end completos desde HTTP hasta base de datos
 * - Validación de contratos API (schemas de request/response)
 * - Happy paths y error cases (404, 400, 409, etc.)
 * - Integración real con PostgreSQL
 * - Nota: Kafka está DESHABILITADO para estos tests (solo validamos endpoints REST)
 * - Nota: Security está DESHABILITADO para estos tests (funcionalidad, no autenticación)
 *
 * DIFERENCIAS CON OTROS TIPOS DE TESTS:
 *
 * Integration Tests (@SpringBootTest sin puerto):
 * - Testean componentes específicos (Controller, Repository, etc.)
 * - Usan MockMvc (no HTTP real)
 * - Más rápidos (~8 segundos por test)
 *
 * E2E Tests (este archivo):
 * - Testean flujos completos end-to-end
 * - Usan HTTP real (como cliente externo)
 * - Más lentos (~30-60 segundos setup + tests)
 * - Perspectiva de "caja negra"
 *
 * VENTAJAS vs LOCAL MODE (KarateE2ELocalTest):
 * - ✅ No requiere levantar app manualmente
 * - ✅ No requiere docker-compose manual
 * - ✅ Perfecto para CI/CD (GitHub Actions)
 * - ✅ Puertos aleatorios (no conflictos)
 *
 * VENTAJAS vs DOCKER MODE (KarateE2EDockerTest):
 * - ✅ Más rápido (no requiere docker build)
 * - ✅ Debugging más fácil
 * - ✅ Feedback más rápido
 *
 * DESVENTAJAS:
 * - ❌ No valida la imagen Docker final (solo código Java)
 * - ❌ Debugging menos intuitivo que LOCAL MODE manual
 *
 * CONFIGURACIÓN EN CI/CD:
 * Este test funciona perfecto en GitHub Actions / GitLab CI / Jenkins.
 * Ver .github/workflows/e2e-tests.yml para configuración.
 *
 * TROUBLESHOOTING:
 *
 * Error: "Container startup failed for image testcontainers/ryuk"
 * - Solución: Usar Rancher Desktop o configurar .testcontainers.properties
 *
 * Error: "Connection refused to localhost:XXXXX"
 * - Solución: Esperar más tiempo, la app tarda ~30-60s en arrancar
 *
 * Error: "Tests pasan individualmente pero fallan juntos"
 * - Solución: Problema de limpieza de BD, usar @DirtiesContext
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.kafka.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "security.enabled=false"
    }
)
/**
 * @DirtiesContext NO es necesario aquí porque:
 * - Cada ejecución de test E2E levanta su propia instancia de Spring Boot
 * - El contexto se crea fresh cada vez que ejecutas el test
 * - PostgreSQL Testcontainer se limpia automáticamente al terminar
 *
 * Solo sería necesario si tuvieras múltiples métodos @Test que compartieran contexto
 * y necesitaras resetear la BD entre ellos.
 */
@Testcontainers
public class KarateE2ETestcontainersTest {

    /**
     * Puerto aleatorio asignado por Spring Boot
     * Se usa para configurar Karate con la URL correcta
     */
    @LocalServerPort
    private int port;

    /**
     * Contenedor de PostgreSQL para los tests
     * Testcontainers levanta automáticamente un contenedor Docker
     *
     * CONFIGURACIÓN PARA CI/CD:
     * - startupTimeout: 120 segundos (vs 60 default) para ambientes con recursos limitados
     */
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withStartupTimeout(java.time.Duration.ofSeconds(120));

    /**
     * Configuración dinámica de Spring Boot
     * Conecta la app a los contenedores de Testcontainers
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    /**
     * Setup antes de ejecutar los tests
     * Configura las system properties para Karate
     */
    @BeforeAll
    public static void setUp() {
        System.out.println("========================================");
        System.out.println("🚀 Starting E2E Tests with Testcontainers");
        System.out.println("========================================");
        System.out.println("✅ PostgreSQL container starting...");
        System.out.println("❌ Kafka: DISABLED (not needed for REST E2E tests)");
    }

    /**
     * Espera a que la aplicación Spring Boot esté lista
     *
     * IMPORTANTE: En CI/CD (GitHub Actions) la aplicación tarda más en arrancar
     * debido a recursos limitados. Este método espera hasta 120 segundos.
     */
    private void waitForApplicationToBeReady() {
        String healthUrl = "http://localhost:" + port + "/actuator/health";
        int maxAttempts = 60; // 60 intentos * 2 segundos = 120 segundos máximo
        int attempt = 0;

        System.out.println("⏳ Waiting for application to be ready at " + healthUrl);

        while (attempt < maxAttempts) {
            try {
                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(healthUrl))
                        .timeout(java.time.Duration.ofSeconds(5))
                        .GET()
                        .build();

                java.net.http.HttpResponse<String> response = client.send(request,
                        java.net.http.HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200 && response.body().contains("UP")) {
                    System.out.println("✅ Application ready after " + (attempt * 2) + " seconds");
                    return;
                }
            } catch (Exception e) {
                // Aplicación aún no está lista, continuar esperando
            }

            attempt++;
            try {
                Thread.sleep(2000); // Esperar 2 segundos entre intentos
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for application", e);
            }
        }

        throw new RuntimeException("Application did not start within 120 seconds");
    }

    /**
     * Cleanup después de ejecutar los tests
     */
    @AfterAll
    public static void tearDown() {
        System.out.println("========================================");
        System.out.println("✅ E2E Tests completed");
        System.out.println("🧹 Cleaning up Testcontainers...");
        System.out.println("========================================");
    }

    /**
     * Ejecuta TODOS los tests E2E de User (.feature files)
     *
     * Este test:
     * 1. Configura Karate con la URL dinámica (puerto aleatorio)
     * 2. Ejecuta todos los .feature files en el directorio user/
     * 3. Genera reporte HTML en target/karate-reports
     * 4. Falla el test si algún scenario falla
     */
    @Karate.Test
    Karate testUser() {
        // Esperar a que la aplicación esté lista antes de ejecutar tests
        waitForApplicationToBeReady();

        // Configurar la URL base de Karate con el puerto aleatorio
        String baseUrl = "http://localhost:" + port;
        System.setProperty("karate.baseUrl", baseUrl);
        System.setProperty("karate.env", "local");

        System.out.println("========================================");
        System.out.println("🥒 Running Karate E2E Tests");
        System.out.println("========================================");
        System.out.println("📍 Base URL: " + baseUrl);
        System.out.println("🧪 Test Location: classpath:com/example/hexarch/e2e/user");
        System.out.println("========================================");

        // Ejecutar todos los .feature files en el directorio user
        // Usa classpath para buscar los .feature files en el paquete correcto
        return Karate.run("classpath:com/example/hexarch/e2e/user");
    }
}
