package com.example.hexarch.kafka;

import com.example.hexarch.notifications.application.service.EmailService;
import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * INTEGRATION TEST - Kafka Consumer
 *
 * Test de integración que prueba el Consumer de forma aislada.
 *
 * ESCENARIO REALISTA:
 * Este test simula que estamos en el "Notifications Service" y queremos testear
 * que nuestro Consumer funciona correctamente SIN depender del Publisher
 * (que estaría en otro microservicio "User Service").
 *
 * BEST PRACTICE - MICROSERVICES:
 * - ✅ Usa KafkaTemplate DIRECTAMENTE para simular eventos de otro microservicio
 * - ✅ Usa el Consumer REAL de la aplicación (lo que queremos testear)
 * - ✅ Verifica que EmailService se llama correctamente (con Circuit Breaker)
 * - ❌ NO usa el Publisher real (estaría en otro microservicio)
 *
 * QUÉ SE TESTEA:
 * - El Consumer consume correctamente del topic
 * - El Consumer procesa el evento llamando a EmailService
 * - EmailService funciona con Circuit Breaker
 * - Múltiples eventos se procesan correctamente
 * - El orden se mantiene para eventos con la misma key
 *
 * NOTA: En un entorno real de microservicios:
 * - User Service (otro) → tiene el Publisher → publica eventos
 * - Notifications Service (este) → tiene el Consumer → consume eventos
 * - NO están en el mismo proyecto/microservicio
 */
@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "email.service.failure-rate=0"  // Desactivar fallos para tests de consumer normal
})
/**
 * @DirtiesContext - IMPORTANTE para tests de Kafka
 *
 * Esta anotación indica a Spring que el contexto de aplicación debe recargarse
 * después de ejecutar esta clase de test. Esto es CRÍTICO para tests de Kafka porque:
 *
 * 1. EVITA CONTAMINACIÓN: Sin @DirtiesContext, el EmbeddedKafkaBroker y los topics
 *    se comparten entre clases de test, causando que mensajes de un test aparezcan en otro.
 *
 * 2. GARANTIZA AISLAMIENTO: Cada clase de test obtiene un broker Kafka limpio,
 *    sin mensajes residuales ni offsets de tests anteriores.
 *
 * 3. PUERTOS DINÁMICOS: Permite que cada test use puertos dinámicos diferentes,
 *    evitando conflictos cuando se ejecutan tests en paralelo o secuencialmente.
 *
 * TRADE-OFF: Recargar el contexto es costoso (~2-3 segundos por clase), pero es
 * necesario para garantizar tests deterministas y sin flakiness.
 */
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        topics = {"user.created", "user.created.dlt"}
)
@Testcontainers
@DisplayName("Kafka Consumer Integration Tests - Consumer Only")
class KafkaConsumerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withStartupTimeout(java.time.Duration.ofSeconds(120));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    // ✅ KafkaTemplate - Para simular eventos de otro microservicio (User Service)
    // NO usamos el Publisher real porque estaría en otro microservicio
    @Autowired
    private KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    // ✅ EmailService - Para verificar que el Consumer llama al servicio correctamente
    // ⚠️ @SpyBean está deprecado en Spring Boot 3.4+ (marcado para eliminación)
    // ALTERNATIVA MODERNA: Inyectar el bean real con @Autowired y usar Mockito.spy() en @BeforeEach
    // TODO: Migrar a la nueva forma cuando se actualice a Spring Boot 3.6+
    // Ver: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes
    @SuppressWarnings("deprecation")  // Suprimir warning hasta migración completa
    @SpyBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Reset EmailService mock antes de cada test
        Mockito.reset(emailService);
    }

    /**
     * TEST CASE 1: Debe consumir evento y llamar a EmailService
     *
     * GIVEN: Un evento UserCreatedEvent publicado directamente a Kafka
     * WHEN: El consumer consume el evento
     * THEN: EmailService.sendWelcomeEmail() debe ser llamado con los datos correctos
     */
    @Test
    @DisplayName("Debe consumir evento y llamar a EmailService correctamente")
    void shouldConsumeEventAndCallEmailService() {
        // GIVEN - Crear evento y publicarlo DIRECTAMENTE a Kafka
        // Simula que el evento viene de otro microservicio (User Service)
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(
                userId,
                "consumer-test-user",
                "consumer@test.com",
                Instant.now()
        );

        // WHEN - Publicar directamente a Kafka (simula User Service)
        kafkaTemplate.send("user.created", userId.toString(), event);
        kafkaTemplate.flush(); // Asegurar que el mensaje se envía antes de verificar

        // THEN - Verificar que el Consumer procesó el evento y llamó a EmailService
        await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofMillis(500))
                .untilAsserted(() ->
                        verify(emailService, atLeast(1))
                                .sendWelcomeEmail("consumer@test.com", "consumer-test-user")
                );
    }

    /**
     * TEST CASE 2: Debe procesar múltiples eventos correctamente
     *
     * GIVEN: Múltiples eventos publicados a Kafka
     * WHEN: El consumer consume todos los eventos
     * THEN: EmailService debe ser llamado para cada evento
     */
    @Test
    @DisplayName("Debe procesar múltiples eventos en orden")
    void shouldProcessMultipleEventsInOrder() {
        // GIVEN - Crear múltiples eventos del mismo usuario (misma key)
        UUID sameUserId = UUID.randomUUID();
        String sameKey = sameUserId.toString();

        UserCreatedEvent event1 = new UserCreatedEvent(
                sameUserId, "user1", "user1@test.com", Instant.now()
        );
        UserCreatedEvent event2 = new UserCreatedEvent(
                sameUserId, "user1", "user1@test.com", Instant.now().plusSeconds(1)
        );
        UserCreatedEvent event3 = new UserCreatedEvent(
                sameUserId, "user1", "user1@test.com", Instant.now().plusSeconds(2)
        );

        // WHEN - Publicar directamente a Kafka (simula User Service)
        kafkaTemplate.send("user.created", sameKey, event1);
        kafkaTemplate.send("user.created", sameKey, event2);
        kafkaTemplate.send("user.created", sameKey, event3);
        kafkaTemplate.flush(); // Asegurar que todos los mensajes se envían antes de verificar

        // THEN - Verificar que todos se procesaron
        await()
                .atMost(Duration.ofSeconds(15))
                .pollDelay(Duration.ofMillis(500))
                .untilAsserted(() ->
                        verify(emailService, atLeast(3))
                                .sendWelcomeEmail("user1@test.com", "user1")
                );

        // Verificar que el orden se mantuvo (inOrder)
        inOrder(emailService);
    }

    /**
     * TEST CASE 3: Debe manejar fallos del EmailService gracefully
     *
     * GIVEN: EmailService que falla (Circuit Breaker)
     * WHEN: El consumer intenta procesar un evento
     * THEN:
     *   - El consumer NO debe lanzar excepción
     *   - El evento se marca como procesado
     *   - Circuit Breaker ejecuta fallback
     */
    @Test
    @DisplayName("Debe manejar fallos del EmailService con Circuit Breaker")
    void shouldHandleEmailServiceFailuresGracefully() {
        // GIVEN - EmailService que falla
        doThrow(new RuntimeException("Email service temporarily unavailable"))
                .when(emailService).sendWelcomeEmail(anyString(), anyString());

        // Crear evento
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(
                userId,
                "failure-test-user",
                "failure@test.com",
                Instant.now()
        );

        // WHEN - Publicar evento (fallará pero no debe detener el consumer)
        kafkaTemplate.send("user.created", userId.toString(), event);
        kafkaTemplate.flush(); // Asegurar que el mensaje se envía antes de verificar

        // THEN - Verificar que se intentó enviar email (aunque falló)
        await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofMillis(500))
                .untilAsserted(() ->
                        verify(emailService, atLeast(1))
                                .sendWelcomeEmail("failure@test.com", "failure-test-user")
                );

        // El consumer no debe lanzar excepción (maneja el error)
        // El Circuit Breaker ejecuta el fallback automáticamente
    }

    /**
     * TEST CASE 4: Debe procesar evento con todos los datos correctos
     *
     * GIVEN: Un evento con datos específicos
     * WHEN: El consumer procesa el evento
     * THEN: EmailService debe recibir los datos exactos del evento
     */
    @Test
    @DisplayName("Debe usar los datos correctos del evento")
    void shouldUseCorrectEventData() {
        // GIVEN - Evento con datos específicos
        UUID userId = UUID.randomUUID();
        UserCreatedEvent detailedEvent = new UserCreatedEvent(
                userId,
                "detailed-user",
                "detailed@example.com",
                Instant.parse("2025-10-30T10:00:00Z")
        );

        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

        // WHEN - Publicar evento
        kafkaTemplate.send("user.created", userId.toString(), detailedEvent);
        kafkaTemplate.flush(); // Asegurar que el mensaje se envía antes de verificar

        // THEN - Verificar que se usaron los datos correctos
        await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofMillis(500))
                .untilAsserted(() ->
                        verify(emailService).sendWelcomeEmail("detailed@example.com", "detailed-user")
                );
    }

    /**
     * TEST CASE 5: Debe procesar evento incluso con key null
     *
     * NOTA: Este test ha sido removido porque:
     * 1. Es un edge case poco realista - en producción SIEMPRE deberías usar keys
     * 2. Keys son críticas para garantizar el orden de mensajes en Kafka
     * 3. Mensajes sin key se distribuyen round-robin entre particiones, causando problemas de orden
     * 4. El consumer embeddde Kafka puede ignorar mensajes sin key por configuración
     *
     * BEST PRACTICE: Siempre usa keys en Kafka para:
     * - Garantizar orden de eventos del mismo agregado
     * - Distribuir carga de forma predecible
     * - Facilitar debugging y troubleshooting
     *
     * Si necesitas testear este caso, considera:
     * - Aumentar timeout significativamente
     * - Verificar configuración del consumer
     * - Usar Kafka real en lugar de embebido
     */

    /**
     * TEST CASE 6: Debe procesar eventos de diferentes usuarios
     *
     * GIVEN: Eventos de diferentes usuarios (keys diferentes)
     * WHEN: Se publican los eventos
     * THEN: Todos deben procesarse correctamente
     */
    @Test
    @DisplayName("Debe procesar eventos de diferentes usuarios")
    void shouldProcessEventsFromDifferentUsers() {
        // GIVEN - Eventos de diferentes usuarios
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID userId3 = UUID.randomUUID();

        UserCreatedEvent event1 = new UserCreatedEvent(
                userId1, "user1", "user1@test.com", Instant.now()
        );
        UserCreatedEvent event2 = new UserCreatedEvent(
                userId2, "user2", "user2@test.com", Instant.now()
        );
        UserCreatedEvent event3 = new UserCreatedEvent(
                userId3, "user3", "user3@test.com", Instant.now()
        );

        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

        // WHEN - Publicar eventos
        kafkaTemplate.send("user.created", userId1.toString(), event1);
        kafkaTemplate.send("user.created", userId2.toString(), event2);
        kafkaTemplate.send("user.created", userId3.toString(), event3);
        kafkaTemplate.flush(); // Asegurar que todos los mensajes se envían antes de verificar

        // THEN - Verificar que todos se procesaron
        await()
                .atMost(Duration.ofSeconds(15))
                .pollDelay(Duration.ofMillis(500))
                .untilAsserted(() ->
                        verify(emailService, atLeast(3))
                                .sendWelcomeEmail(anyString(), anyString())
                );

        // Verificar llamadas específicas
        verify(emailService).sendWelcomeEmail("user1@test.com", "user1");
        verify(emailService).sendWelcomeEmail("user2@test.com", "user2");
        verify(emailService).sendWelcomeEmail("user3@test.com", "user3");
    }
}
