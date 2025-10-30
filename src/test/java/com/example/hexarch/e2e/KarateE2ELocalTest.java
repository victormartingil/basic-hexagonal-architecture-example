package com.example.hexarch.e2e;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Disabled;

/**
 * KARATE E2E TESTS - LOCAL MODE
 *
 * Runner para ejecutar tests E2E con Karate contra aplicación local.
 *
 * PRE-REQUISITOS:
 * 1. Tener la aplicación corriendo en localhost:8080
 * 2. Tener Docker Compose levantado (PostgreSQL + Kafka)
 *
 * CÓMO EJECUTAR:
 *
 * Opción 1: Desde Maven (solo estos tests)
 * ```bash
 * # En una terminal: Levantar infraestructura
 * docker-compose up -d
 *
 * # En otra terminal: Levantar aplicación
 * ./mvnw spring-boot:run -DskipTests
 *
 * # En otra terminal: Ejecutar E2E tests
 * ./mvnw test -Dtest=KarateE2ELocalTest -Dkarate.env=local
 * ```
 *
 * Opción 2: Desde IDE (IntelliJ IDEA / VS Code)
 * - Click derecho en esta clase → Run 'KarateE2ELocalTest'
 * - Asegúrate de tener la aplicación corriendo antes
 *
 * CONFIGURACIÓN:
 * - karate.env=local: Usa http://localhost:8080 como base URL
 * - Ver src/test/java/karate-config.js para más detalles
 *
 * QUÉ TESTEA:
 * - Flujos end-to-end completos desde el cliente hasta la base de datos
 * - Validación de contratos API (request/response schemas)
 * - Happy paths y casos de error (404, 400, etc.)
 * - Integración real con PostgreSQL y Kafka
 *
 * DIFERENCIA CON INTEGRATION TESTS:
 * - Integration tests: Usan Testcontainers, no requieren app corriendo
 * - E2E tests: Requieren app corriendo, testan "caja negra" completa
 */
public class KarateE2ELocalTest {

    /**
     * Ejecuta TODOS los tests E2E (.feature files) en el classpath
     *
     * IMPORTANTE: Este test está deshabilitado por defecto.
     * Para ejecutarlo, comenta la anotación @Disabled y asegúrate de:
     * 1. Tener la aplicación corriendo en localhost:8080
     * 2. Tener Docker Compose levantado
     */
    @Disabled("E2E tests requieren aplicación corriendo - ejecutar manualmente")
    @Karate.Test
    Karate testAll() {
        // System property para configurar el entorno
        System.setProperty("karate.env", "local");

        // Ejecuta todos los .feature files en com/example/hexarch/e2e
        return Karate.run().relativeTo(getClass());
    }

    /**
     * Ejecuta SOLO los tests de User (útil para desarrollo)
     */
    @Disabled("E2E tests requieren aplicación corriendo - ejecutar manualmente")
    @Karate.Test
    Karate testUser() {
        System.setProperty("karate.env", "local");
        return Karate.run("user").relativeTo(getClass());
    }

    /**
     * Ejecuta UN SOLO feature file (útil para debugging)
     *
     * Descomentar para ejecutar solo create-user.feature:
     */
    /*
    @Karate.Test
    Karate testCreateUser() {
        System.setProperty("karate.env", "local");
        return Karate.run("user/create-user.feature").relativeTo(getClass());
    }
    */

    /**
     * Ejecuta tests en paralelo (más rápido, requiere más recursos)
     *
     * Descomentar para ejecutar en paralelo:
     */
    /*
    @Karate.Test
    Karate testParallel() {
        System.setProperty("karate.env", "local");
        return Karate.run().relativeTo(getClass()).parallel(5); // 5 threads
    }
    */
}
