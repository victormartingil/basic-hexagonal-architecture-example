package com.example.hexarch.e2e;

import com.intuit.karate.junit5.Karate;

/**
 * KARATE E2E TESTS - DOCKER MODE
 *
 * Runner para ejecutar tests E2E con Karate contra aplicación en Docker Compose.
 *
 * PRE-REQUISITOS:
 * 1. Tener Docker Compose completo levantado (app + PostgreSQL + Kafka)
 * 2. La aplicación debe estar corriendo en el contenedor Docker
 *
 * CÓMO EJECUTAR:
 *
 * Opción 1: Usando Docker Compose completo
 * ```bash
 * # 1. Levantar stack completo (incluye la app)
 * docker-compose up -d
 *
 * # 2. Esperar a que la app esté ready
 * docker logs hexarch-app --follow
 * # Espera hasta ver: "Started HexarchApplication in X seconds"
 *
 * # 3. Ejecutar E2E tests contra Docker
 * ./mvnw test -Dtest=KarateE2EDockerTest -Dkarate.env=docker
 * ```
 *
 * Opción 2: Build y deploy manual con Docker
 * ```bash
 * # 1. Build de la aplicación
 * ./mvnw clean package -DskipTests
 *
 * # 2. Build de la imagen Docker
 * docker build -t hexarch:latest .
 *
 * # 3. Levantar stack completo
 * docker-compose up -d
 *
 * # 4. Ejecutar E2E tests
 * ./mvnw test -Dtest=KarateE2EDockerTest -Dkarate.env=docker
 * ```
 *
 * CONFIGURACIÓN:
 * - karate.env=docker: Usa http://localhost:8080 como base URL
 *   (El puerto está mapeado desde el contenedor al host)
 * - Ver src/test/java/karate-config.js para más detalles
 *
 * CUÁNDO USAR DOCKER MODE VS LOCAL MODE:
 *
 * LOCAL MODE (KarateE2ELocalTest):
 * ✅ Desarrollo rápido (hot reload con Spring Boot DevTools)
 * ✅ Debugging fácil desde IDE
 * ✅ Feedback rápido en cambios de código
 * ❌ Requiere Java local instalado
 *
 * DOCKER MODE (KarateE2EDockerTest):
 * ✅ Entorno idéntico a producción
 * ✅ No requiere Java local (solo Docker)
 * ✅ Perfecto para CI/CD pipelines
 * ✅ Tests contra imagen Docker real
 * ❌ Build más lento (requiere Docker build)
 * ❌ Debugging más complejo
 *
 * USO EN CI/CD:
 * Este runner es ideal para GitHub Actions / GitLab CI / Jenkins.
 * Ver .github/workflows/e2e-tests.yml para ejemplo de configuración.
 *
 * TROUBLESHOOTING:
 *
 * Error: "Connection refused"
 * - Solución: Verificar que la app en Docker está corriendo:
 *   docker ps | grep hexarch-app
 *   docker logs hexarch-app
 *
 * Error: "Tests fallan pero app funciona en navegador"
 * - Solución: Verificar que el puerto 8080 está mapeado:
 *   docker-compose ps
 *   Debe mostrar: 0.0.0.0:8080->8080/tcp
 *
 * Error: "Database connection refused"
 * - Solución: Verificar networking en docker-compose.yml:
 *   La app debe poder conectarse a 'postgres:5432'
 */
public class KarateE2EDockerTest {

    /**
     * Ejecuta TODOS los tests E2E contra Docker Compose
     *
     * IMPORTANTE: Este test requiere:
     * 1. Tener Docker Compose completo levantado (app + PostgreSQL + Kafka)
     * 2. La aplicación debe estar corriendo en el contenedor Docker
     *
     * Se ejecuta automáticamente en GitHub Actions con el modo "docker".
     * Para ejecutar manualmente: ./mvnw test -Pe2e-tests-docker -Dkarate.env=docker
     */
    @Karate.Test
    Karate testAll() {
        // System property para configurar el entorno
        System.setProperty("karate.env", "docker");

        // Ejecuta todos los .feature files del subdirectorio user
        return Karate.run("user").relativeTo(getClass());
    }

    /**
     * Ejecuta SOLO los tests de User contra Docker
     * Útil para debugging de features específicas
     */
    @Karate.Test
    Karate testUser() {
        System.setProperty("karate.env", "docker");
        return Karate.run("user").relativeTo(getClass());
    }

    /**
     * Health check: Verifica que la app en Docker responde
     *
     * Útil para debugging en CI/CD.
     * Descomentar si necesitas validar connectivity antes de ejecutar tests:
     */
    /*
    @BeforeAll
    public static void waitForDockerApp() throws Exception {
        String healthUrl = "http://localhost:8080/actuator/health";
        int maxRetries = 30;
        int retryDelayMs = 2000;

        System.out.println("Waiting for Docker app to be ready...");

        for (int i = 0; i < maxRetries; i++) {
            try {
                HttpURLConnection conn = (HttpURLConnection)
                    new URL(healthUrl).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);

                if (conn.getResponseCode() == 200) {
                    System.out.println("✅ Docker app is ready!");
                    return;
                }
            } catch (Exception e) {
                System.out.println("⏳ Waiting for app... (attempt " + (i+1) + "/" + maxRetries + ")");
                Thread.sleep(retryDelayMs);
            }
        }

        throw new RuntimeException("❌ Docker app did not start in time!");
    }
    */
}
