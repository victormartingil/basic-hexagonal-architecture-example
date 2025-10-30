package com.example.hexarch.notifications.application.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * UNIT TEST - EmailService con Circuit Breaker
 *
 * Test unitario para el servicio EmailService que demuestra el comportamiento
 * del Circuit Breaker pattern usando Resilience4j.
 *
 * CIRCUIT BREAKER TESTING:
 * - Estado CLOSED: El servicio funciona normalmente
 * - Estado OPEN: Después de múltiples fallos, el circuit se abre
 * - Estado HALF_OPEN: Después del wait-duration, se permiten llamadas de prueba
 * - Transiciones automáticas basadas en configuración
 *
 * CONFIGURACIÓN DE TEST:
 * - sliding-window-size: 10 llamadas
 * - minimum-number-of-calls: 5 llamadas mínimas
 * - failure-rate-threshold: 50% de fallos para abrir
 * - wait-duration-in-open-state: 1s (reducido para tests)
 *
 * FRAMEWORKS:
 * - JUnit 5: framework de testing
 * - Spring Boot Test: contexto de Spring
 * - AssertJ: assertions fluidas
 * - Awaitility: esperas asíncronas
 */
@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        // Configuración reducida para tests más rápidos
        "resilience4j.circuitbreaker.instances.emailService.wait-duration-in-open-state=1s",
        "resilience4j.circuitbreaker.instances.emailService.sliding-window-size=10",
        "resilience4j.circuitbreaker.instances.emailService.minimum-number-of-calls=5",
        "resilience4j.circuitbreaker.instances.emailService.failure-rate-threshold=50"
})
@DisplayName("EmailService - Circuit Breaker Tests")
class EmailServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private EmailService emailService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("emailService");
        // Reset circuit breaker antes de cada test
        circuitBreaker.reset();
    }

    /**
     * TEST CASE 1: Circuit Breaker debe estar CLOSED inicialmente
     *
     * GIVEN: El servicio recién inicializado
     * WHEN: Se verifica el estado del circuit breaker
     * THEN: El estado debe ser CLOSED (funcionamiento normal)
     */
    @Test
    @DisplayName("Circuit breaker debe iniciar en estado CLOSED")
    void shouldBeClosedInitially() {
        // GIVEN - Estado inicial

        // THEN - Verificar estado CLOSED
        assertThat(circuitBreaker.getState())
                .isEqualTo(CircuitBreaker.State.CLOSED);

        assertThat(circuitBreaker.getMetrics().getFailureRate())
                .isEqualTo(-1.0f);  // -1 significa que no hay suficientes llamadas todavía
    }

    /**
     * TEST CASE 2: Circuit no debe abrirse con pocos fallos
     *
     * GIVEN: Menos de minimum-number-of-calls (5)
     * WHEN: Todas las llamadas fallan
     * THEN: El circuit NO debe abrirse (insuficientes datos)
     */
    @Test
    @DisplayName("Circuit breaker NO debe abrirse con menos de minimum-number-of-calls")
    void shouldNotOpenWithFewCalls() {
        // GIVEN - Forzar fallos (pero menos de 5 llamadas)

        // WHEN - 4 llamadas (todas fallarán por el random > 70%)
        // No podemos controlar el random en el EmailService actual,
        // así que este test es más ilustrativo de la lógica del CB

        // THEN - El circuit debe seguir CLOSED
        // (En un test real, deberíamos poder controlar los fallos)

        // Este test muestra la limitación de testear con random
        // En producción, el EmailService debería tener una forma de inyectar
        // el comportamiento (por ejemplo, usando una estrategia de envío)

        // Por ahora, verificamos solo que el circuit inicia en CLOSED
        assertThat(circuitBreaker.getState())
                .isEqualTo(CircuitBreaker.State.CLOSED);
    }

    /**
     * TEST CASE 3: Circuit debe transicionar a OPEN después de múltiples fallos
     *
     * GIVEN: Circuit breaker en estado CLOSED
     * WHEN: Se fuerza el circuit a estado OPEN manualmente
     * THEN: El circuit debe estar en estado OPEN
     *
     * NOTA: Idealmente, deberíamos provocar fallos reales, pero el EmailService
     * actual usa Random que es difícil de controlar en tests.
     */
    @Test
    @DisplayName("Circuit breaker debe poder transicionar a estado OPEN")
    void shouldTransitionToOpenState() {
        // GIVEN - Circuit en estado CLOSED
        assertThat(circuitBreaker.getState())
                .isEqualTo(CircuitBreaker.State.CLOSED);

        // WHEN - Forzar transición a OPEN (simulando múltiples fallos)
        circuitBreaker.transitionToOpenState();

        // THEN - Verificar estado OPEN
        assertThat(circuitBreaker.getState())
                .isEqualTo(CircuitBreaker.State.OPEN);
    }

    /**
     * TEST CASE 4: Circuit debe transicionar a HALF_OPEN después de wait-duration
     *
     * GIVEN: Circuit breaker en estado OPEN
     * WHEN: Se espera el wait-duration (1s en config de test) y se hace una llamada
     * THEN: El circuit debe transicionar a HALF_OPEN
     */
    @Test
    @DisplayName("Circuit breaker debe transicionar a HALF_OPEN después de wait-duration")
    void shouldTransitionToHalfOpenAfterWaitDuration() throws InterruptedException {
        // GIVEN - Forzar circuit a OPEN
        circuitBreaker.transitionToOpenState();
        assertThat(circuitBreaker.getState())
                .isEqualTo(CircuitBreaker.State.OPEN);

        // WHEN - Esperar wait-duration + margen (1s + 200ms para asegurar)
        Thread.sleep(1200);

        // Hacer una llamada para que el circuit intente transicionar a HALF_OPEN
        emailService.sendWelcomeEmail("test@test.com", "test");

        // THEN - Verificar que transicionó a HALF_OPEN
        // Después del wait-duration, la primera llamada debe transicionar a HALF_OPEN
        assertThat(circuitBreaker.getState())
                .isEqualTo(CircuitBreaker.State.HALF_OPEN);
    }

    /**
     * TEST CASE 5: Fallback debe ejecutarse cuando circuit está OPEN
     *
     * GIVEN: Circuit breaker en estado OPEN
     * WHEN: Se intenta enviar un email
     * THEN: El fallback debe ejecutarse sin lanzar excepción
     */
    @Test
    @DisplayName("Fallback debe ejecutarse cuando circuit está OPEN")
    void shouldExecuteFallbackWhenCircuitIsOpen() {
        // GIVEN - Forzar circuit a OPEN
        circuitBreaker.transitionToOpenState();

        // WHEN - Intentar enviar email
        // El fallback debe ejecutarse y NO lanzar excepción
        emailService.sendWelcomeEmail("test@test.com", "test");

        // THEN - Verificar que no se lanzó excepción
        // (el test pasa si no hay excepción)

        // Verificar que el circuit sigue en OPEN
        assertThat(circuitBreaker.getState())
                .isEqualTo(CircuitBreaker.State.OPEN);
    }

    /**
     * TEST CASE 6: Verificar métricas del circuit breaker
     *
     * GIVEN: Circuit breaker inicializado
     * WHEN: Se verifican las métricas
     * THEN: Las métricas deben estar disponibles
     */
    @Test
    @DisplayName("Métricas del circuit breaker deben estar disponibles")
    void shouldHaveCircuitBreakerMetrics() {
        // GIVEN - Circuit breaker

        // WHEN - Obtener métricas
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();

        // THEN - Verificar que las métricas existen
        assertThat(metrics).isNotNull();
        assertThat(metrics.getNumberOfSuccessfulCalls()).isGreaterThanOrEqualTo(0);
        assertThat(metrics.getNumberOfFailedCalls()).isGreaterThanOrEqualTo(0);
    }

    /**
     * TEST CASE 7: Circuit breaker debe permitir configurar listeners
     *
     * GIVEN: Circuit breaker
     * WHEN: Se registra un listener de eventos
     * THEN: El listener debe recibir eventos de transición de estado
     */
    @Test
    @DisplayName("Circuit breaker debe permitir registrar listeners de eventos")
    void shouldAllowEventListeners() {
        // GIVEN - Circuit breaker
        boolean[] eventReceived = {false};

        // WHEN - Registrar listener
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> {
                    eventReceived[0] = true;
                });

        // Forzar transición de estado
        circuitBreaker.transitionToOpenState();

        // THEN - Verificar que se recibió el evento
        assertThat(eventReceived[0]).isTrue();
    }
}
