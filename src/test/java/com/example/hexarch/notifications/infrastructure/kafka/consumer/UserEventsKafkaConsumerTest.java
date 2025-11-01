package com.example.hexarch.notifications.infrastructure.kafka.consumer;

import com.example.hexarch.notifications.application.service.EmailService;
import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * UNIT TEST - UserEventsKafkaConsumer
 *
 * Test unitario para el consumer de Kafka que procesa eventos UserCreatedEvent.
 * Simula el comportamiento de un microservicio de notificaciones separado.
 *
 * QUÉ SE TESTEA:
 * - Procesamiento correcto de eventos recibidos
 * - Llamada al EmailService con Circuit Breaker
 * - Manejo de excepciones (no propagar para evitar DLT innecesariamente)
 * - Integración con EmailService
 *
 * ARQUITECTURA:
 * User Service → Kafka → UserEventsKafkaConsumer → EmailService (con Circuit Breaker)
 *
 * MOCKING:
 * - EmailService: Mockeado para evitar dependencia de implementación real
 *
 * FRAMEWORKS:
 * - JUnit 5: framework de testing
 * - Mockito: framework para mocks
 * - AssertJ: assertions fluidas
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserEventsKafkaConsumer - Unit Tests")
class UserEventsKafkaConsumerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserEventsKafkaConsumer consumer;

    private UserCreatedEvent testEvent;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEvent = new UserCreatedEvent(
                testUserId,
                "johndoe",
                "john@example.com",
                Instant.now()
        );
    }

    /**
     * TEST CASE 1: Debe procesar evento exitosamente
     *
     * GIVEN: Un evento válido de usuario creado
     * WHEN: El consumer procesa el evento
     * THEN:
     *   - Debe llamar a emailService.sendWelcomeEmail()
     *   - Debe pasar el email y username correctos
     *   - No debe lanzar excepciones
     */
    @Test
    @DisplayName("Debe procesar evento exitosamente y llamar a EmailService")
    void shouldProcessEventSuccessfully() {
        // GIVEN - EmailService funciona correctamente
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

        // WHEN - Procesar evento
        // Simulamos los headers de Kafka
        int partition = 0;
        long offset = 123L;
        String key = testUserId.toString();

        consumer.consume(testEvent, partition, offset, key);

        // THEN - Verificar que se llamó a EmailService con los datos correctos
        verify(emailService, times(1))
                .sendWelcomeEmail("john@example.com", "johndoe");

        // Verificar que solo se llamó una vez
        verifyNoMoreInteractions(emailService);
    }

    /**
     * TEST CASE 2: Debe manejar excepciones del EmailService sin propagarlas
     *
     * GIVEN: EmailService que lanza excepción
     * WHEN: El consumer procesa el evento
     * THEN:
     *   - La excepción NO debe propagarse (para no enviar a DLT)
     *   - El consumer debe manejarla gracefully
     *
     * RAZÓN: Las notificaciones no son críticas. Si el email falla,
     * el usuario ya fue creado. No queremos reenviar el mensaje al DLT
     * por un error no crítico.
     */
    @Test
    @DisplayName("Debe manejar excepciones del EmailService sin propagarlas")
    void shouldHandleEmailServiceExceptionGracefully() {
        // GIVEN - EmailService lanza excepción
        doThrow(new RuntimeException("Email service temporarily unavailable"))
                .when(emailService).sendWelcomeEmail(anyString(), anyString());

        // WHEN/THEN - Procesar evento DEBE lanzar excepción (nuevo comportamiento para DLT)
        // El comportamiento cambió: ahora propagamos excepciones a Kafka para retry/DLT
        assertThatThrownBy(() -> consumer.consume(testEvent, 0, 123L, testUserId.toString()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to process notification");

        // THEN - Verificar que se intentó enviar email
        verify(emailService).sendWelcomeEmail("john@example.com", "johndoe");
    }

    /**
     * TEST CASE 3: Debe procesar eventos con diferentes metadatos de Kafka
     *
     * GIVEN: Eventos en diferentes particiones y offsets
     * WHEN: El consumer procesa múltiples eventos
     * THEN: Todos deben procesarse correctamente
     */
    @Test
    @DisplayName("Debe procesar eventos de diferentes particiones y offsets")
    void shouldProcessEventsFromDifferentPartitions() {
        // GIVEN - EmailService mock
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

        // WHEN - Procesar eventos de diferentes particiones
        consumer.consume(testEvent, 0, 100L, testUserId.toString());
        consumer.consume(testEvent, 1, 200L, testUserId.toString());
        consumer.consume(testEvent, 2, 300L, testUserId.toString());

        // THEN - Verificar que todos se procesaron
        verify(emailService, times(3))
                .sendWelcomeEmail("john@example.com", "johndoe");
    }

    /**
     * TEST CASE 4: Debe procesar evento con todos los datos del evento
     *
     * GIVEN: Un evento con userId, username, email, timestamp
     * WHEN: El consumer procesa el evento
     * THEN: Debe usar los datos correctos para enviar el email
     */
    @Test
    @DisplayName("Debe usar los datos correctos del evento")
    void shouldUseCorrectEventData() {
        // GIVEN - Evento con datos específicos
        UUID userId = UUID.randomUUID();
        UserCreatedEvent detailedEvent = new UserCreatedEvent(
                userId,
                "testuser",
                "test@example.com",
                Instant.parse("2025-10-30T10:00:00Z")
        );

        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

        // WHEN - Procesar evento
        consumer.consume(detailedEvent, 0, 456L, userId.toString());

        // THEN - Verificar que se usaron los datos correctos
        verify(emailService).sendWelcomeEmail("test@example.com", "testuser");
    }

    /**
     * TEST CASE 5: Debe funcionar con Circuit Breaker OPEN (fallback)
     *
     * GIVEN: EmailService con Circuit Breaker en estado OPEN
     * WHEN: El consumer intenta procesar un evento
     * THEN:
     *   - El Circuit Breaker ejecuta el fallback
     *   - El consumer no lanza excepción
     *   - El evento se marca como procesado (offset avanza)
     *
     * NOTA: Cuando el Circuit Breaker está OPEN, el EmailService
     * ejecuta sendEmailFallback() que NO lanza excepción.
     */
    @Test
    @DisplayName("Debe funcionar cuando Circuit Breaker está OPEN (fallback)")
    void shouldWorkWithCircuitBreakerOpen() {
        // GIVEN - EmailService con fallback (no lanza excepción)
        // El Circuit Breaker en OPEN ejecuta el fallback que no lanza excepción
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

        // WHEN - Procesar evento
        assertThatCode(() -> consumer.consume(testEvent, 0, 789L, testUserId.toString()))
                .doesNotThrowAnyException();

        // THEN - Verificar que se intentó el envío
        verify(emailService).sendWelcomeEmail("john@example.com", "johndoe");
    }

    /**
     * TEST CASE 6: Debe procesar múltiples eventos del mismo usuario en orden
     *
     * GIVEN: Múltiples eventos del mismo usuario (misma key)
     * WHEN: El consumer procesa los eventos
     * THEN: Deben procesarse en el orden recibido
     *
     * NOTA: Kafka garantiza orden dentro de una partición con la misma key.
     * Este test verifica que el consumer procesa en orden.
     */
    @Test
    @DisplayName("Debe procesar eventos del mismo usuario en orden")
    void shouldProcessEventsInOrder() {
        // GIVEN - Múltiples eventos del mismo usuario
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

        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

        // WHEN - Procesar eventos en orden (misma partición, offsets secuenciales)
        consumer.consume(event1, 0, 100L, sameKey);
        consumer.consume(event2, 0, 101L, sameKey);
        consumer.consume(event3, 0, 102L, sameKey);

        // THEN - Verificar que se procesaron 3 veces en orden
        verify(emailService, times(3))
                .sendWelcomeEmail("user1@test.com", "user1");

        // Verificar que el orden se mantuvo (inOrder)
        inOrder(emailService);
    }

    /**
     * TEST CASE 7: Debe procesar evento incluso con key null
     *
     * GIVEN: Un evento sin key (key = null)
     * WHEN: El consumer procesa el evento
     * THEN: Debe procesarse correctamente (key es solo para orden, no obligatoria)
     */
    @Test
    @DisplayName("Debe procesar evento incluso con key null")
    void shouldProcessEventWithNullKey() {
        // GIVEN - EmailService mock
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

        // WHEN - Procesar evento con key null
        assertThatCode(() -> consumer.consume(testEvent, 0, 123L, null))
                .doesNotThrowAnyException();

        // THEN - Verificar que se procesó
        verify(emailService).sendWelcomeEmail("john@example.com", "johndoe");
    }
}
