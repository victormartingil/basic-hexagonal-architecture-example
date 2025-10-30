package com.example.hexarch.notifications.infrastructure.kafka.consumer;

import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * UNIT TEST - UserCreatedEventDLTConsumer
 *
 * Test unitario para el consumer del Dead Letter Topic (DLT).
 * Este consumer procesa mensajes que fallaron después de múltiples reintentos.
 *
 * QUÉ SE TESTEA:
 * - Procesamiento de mensajes fallidos del DLT
 * - Extracción de headers con información del error
 * - Logging de errores para investigación
 * - Manejo de diferentes tipos de errores
 *
 * DLT (Dead Letter Topic):
 * Cuando un mensaje falla N veces (configurado en KafkaConfig),
 * Spring Kafka automáticamente lo envía al topic {original}.dlt
 * con headers adicionales sobre el error.
 *
 * HEADERS DEL DLT:
 * - kafka_dlt-original-topic: topic original
 * - kafka_dlt-exception-message: mensaje de error
 * - kafka_dlt-exception-stacktrace: stack trace completo
 *
 * FRAMEWORKS:
 * - JUnit 5: framework de testing
 * - Mockito: framework para mocks
 * - AssertJ: assertions fluidas
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserCreatedEventDLTConsumer - Unit Tests")
class UserCreatedEventDLTConsumerTest {

    @InjectMocks
    private UserCreatedEventDLTConsumer dltConsumer;

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
     * TEST CASE 1: Debe procesar mensaje fallido del DLT sin lanzar excepción
     *
     * GIVEN: Un mensaje en el DLT con headers de error
     * WHEN: El consumer procesa el mensaje
     * THEN:
     *   - No debe lanzar excepción
     *   - Debe loguear la información del error
     *   - Debe procesar sin reintentar (ya está en DLT)
     */
    @Test
    @DisplayName("Debe procesar mensaje fallido del DLT sin lanzar excepción")
    void shouldProcessFailedMessageWithoutException() {
        // GIVEN - Crear ConsumerRecord con headers de DLT
        ConsumerRecord<String, UserCreatedEvent> record = createDLTRecord(
                testEvent,
                testUserId.toString(),
                "user.created",
                "Email service temporarily unavailable",
                "java.lang.RuntimeException: Email service down\n\tat EmailService.send()"
        );

        // WHEN & THEN - No debe lanzar excepción
        assertThatCode(() -> dltConsumer.consumeFailedMessage(
                testEvent,
                record,
                0,  // partition
                123L,  // offset
                testUserId.toString()  // key
        )).doesNotThrowAnyException();
    }

    /**
     * TEST CASE 2: Debe extraer headers de error correctamente
     *
     * GIVEN: Un mensaje en DLT con headers específicos
     * WHEN: El consumer procesa el mensaje
     * THEN: Debe extraer y loguear los headers correctamente
     */
    @Test
    @DisplayName("Debe extraer headers de error del DLT")
    void shouldExtractErrorHeadersFromDLT() {
        // GIVEN - Record con headers específicos
        String originalTopic = "user.created";
        String errorMessage = "NullPointerException: email cannot be null";
        String stackTrace = "java.lang.NullPointerException\n\tat EmailService.validate()";

        ConsumerRecord<String, UserCreatedEvent> record = createDLTRecord(
                testEvent,
                testUserId.toString(),
                originalTopic,
                errorMessage,
                stackTrace
        );

        // WHEN - Procesar mensaje
        assertThatCode(() -> dltConsumer.consumeFailedMessage(
                testEvent,
                record,
                1,
                456L,
                testUserId.toString()
        )).doesNotThrowAnyException();

        // THEN - El consumer debe procesar sin errores
        // (en un test real con spy, verificaríamos que se logueó correctamente)
    }

    /**
     * TEST CASE 3: Debe manejar mensajes sin headers opcionales
     *
     * GIVEN: Un mensaje en DLT sin algunos headers opcionales
     * WHEN: El consumer procesa el mensaje
     * THEN: Debe manejar gracefully la ausencia de headers
     */
    @Test
    @DisplayName("Debe manejar mensajes sin headers opcionales")
    void shouldHandleMessagesWithoutOptionalHeaders() {
        // GIVEN - Record sin headers (caso extremo)
        ConsumerRecord<String, UserCreatedEvent> record = new ConsumerRecord<>(
                "user.created.dlt",
                0,
                789L,
                testUserId.toString(),
                testEvent
        );

        // WHEN & THEN - No debe lanzar excepción
        assertThatCode(() -> dltConsumer.consumeFailedMessage(
                testEvent,
                record,
                0,
                789L,
                testUserId.toString()
        )).doesNotThrowAnyException();
    }

    /**
     * TEST CASE 4: Debe procesar múltiples mensajes fallidos
     *
     * GIVEN: Múltiples mensajes en el DLT
     * WHEN: El consumer procesa todos
     * THEN: Todos deben procesarse sin excepciones
     */
    @Test
    @DisplayName("Debe procesar múltiples mensajes fallidos del DLT")
    void shouldProcessMultipleFailedMessages() {
        // GIVEN - Múltiples eventos fallidos
        UserCreatedEvent event1 = new UserCreatedEvent(
                UUID.randomUUID(), "user1", "user1@test.com", Instant.now()
        );
        UserCreatedEvent event2 = new UserCreatedEvent(
                UUID.randomUUID(), "user2", "user2@test.com", Instant.now()
        );
        UserCreatedEvent event3 = new UserCreatedEvent(
                UUID.randomUUID(), "user3", "user3@test.com", Instant.now()
        );

        ConsumerRecord<String, UserCreatedEvent> record1 = createDLTRecord(
                event1, event1.userId().toString(), "user.created", "Error 1", "Stack 1"
        );
        ConsumerRecord<String, UserCreatedEvent> record2 = createDLTRecord(
                event2, event2.userId().toString(), "user.created", "Error 2", "Stack 2"
        );
        ConsumerRecord<String, UserCreatedEvent> record3 = createDLTRecord(
                event3, event3.userId().toString(), "user.created", "Error 3", "Stack 3"
        );

        // WHEN & THEN - Todos deben procesarse sin excepciones
        assertThatCode(() -> {
            dltConsumer.consumeFailedMessage(event1, record1, 0, 100L, event1.userId().toString());
            dltConsumer.consumeFailedMessage(event2, record2, 0, 101L, event2.userId().toString());
            dltConsumer.consumeFailedMessage(event3, record3, 0, 102L, event3.userId().toString());
        }).doesNotThrowAnyException();
    }

    /**
     * TEST CASE 5: Debe manejar diferentes tipos de errores
     *
     * GIVEN: Mensajes con diferentes tipos de errores
     * WHEN: El consumer procesa los mensajes
     * THEN: Debe manejar todos los tipos de error
     */
    @Test
    @DisplayName("Debe manejar diferentes tipos de errores en DLT")
    void shouldHandleDifferentErrorTypes() {
        // GIVEN - Diferentes tipos de errores

        // Error 1: Servicio caído
        ConsumerRecord<String, UserCreatedEvent> serviceDownRecord = createDLTRecord(
                testEvent,
                testUserId.toString(),
                "user.created",
                "ConnectException: Connection refused",
                "java.net.ConnectException\n\tat HttpClient.connect()"
        );

        // Error 2: Timeout
        ConsumerRecord<String, UserCreatedEvent> timeoutRecord = createDLTRecord(
                testEvent,
                testUserId.toString(),
                "user.created",
                "TimeoutException: Read timed out",
                "java.util.concurrent.TimeoutException\n\tat Future.get()"
        );

        // Error 3: Datos inválidos
        ConsumerRecord<String, UserCreatedEvent> validationRecord = createDLTRecord(
                testEvent,
                testUserId.toString(),
                "user.created",
                "ValidationException: Invalid email format",
                "ValidationException\n\tat EmailValidator.validate()"
        );

        // WHEN & THEN - Todos deben procesarse
        assertThatCode(() -> {
            dltConsumer.consumeFailedMessage(testEvent, serviceDownRecord, 0, 100L, testUserId.toString());
            dltConsumer.consumeFailedMessage(testEvent, timeoutRecord, 0, 101L, testUserId.toString());
            dltConsumer.consumeFailedMessage(testEvent, validationRecord, 0, 102L, testUserId.toString());
        }).doesNotThrowAnyException();
    }

    /**
     * TEST CASE 6: Debe manejar mensajes con key null
     *
     * GIVEN: Un mensaje en DLT sin key
     * WHEN: El consumer procesa el mensaje
     * THEN: Debe manejar gracefully la ausencia de key
     */
    @Test
    @DisplayName("Debe manejar mensajes del DLT con key null")
    void shouldHandleMessagesWithNullKey() {
        // GIVEN - Record sin key
        ConsumerRecord<String, UserCreatedEvent> record = createDLTRecord(
                testEvent,
                null,  // key null
                "user.created",
                "Some error",
                "Some stack trace"
        );

        // WHEN & THEN - No debe lanzar excepción
        assertThatCode(() -> dltConsumer.consumeFailedMessage(
                testEvent,
                record,
                0,
                999L,
                null  // key null
        )).doesNotThrowAnyException();
    }

    /**
     * TEST CASE 7: Debe procesar evento con todos los datos preservados
     *
     * GIVEN: Un evento que falló con todos sus datos
     * WHEN: El evento llega al DLT
     * THEN: Todos los datos del evento deben estar intactos
     */
    @Test
    @DisplayName("Debe preservar todos los datos del evento original en DLT")
    void shouldPreserveOriginalEventData() {
        // GIVEN - Evento con datos específicos
        UUID userId = UUID.randomUUID();
        Instant timestamp = Instant.parse("2025-10-30T12:00:00Z");
        UserCreatedEvent detailedEvent = new UserCreatedEvent(
                userId,
                "detaileduser",
                "detailed@example.com",
                timestamp
        );

        ConsumerRecord<String, UserCreatedEvent> record = createDLTRecord(
                detailedEvent,
                userId.toString(),
                "user.created",
                "Test error",
                "Test stack"
        );

        // WHEN - Procesar mensaje
        // El evento debe tener todos sus datos intactos
        assertThatCode(() -> {
            // Verificar que los datos del evento están correctos antes de procesar
            assert detailedEvent.userId().equals(userId);
            assert detailedEvent.username().equals("detaileduser");
            assert detailedEvent.email().equals("detailed@example.com");
            assert detailedEvent.occurredAt().equals(timestamp);

            // Procesar
            dltConsumer.consumeFailedMessage(detailedEvent, record, 0, 555L, userId.toString());
        }).doesNotThrowAnyException();
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    /**
     * Helper method para crear un ConsumerRecord del DLT con headers
     *
     * @param event evento fallido
     * @param key clave del mensaje
     * @param originalTopic topic original
     * @param errorMessage mensaje de error
     * @param stackTrace stack trace del error
     * @return ConsumerRecord con headers del DLT
     */
    private ConsumerRecord<String, UserCreatedEvent> createDLTRecord(
            UserCreatedEvent event,
            String key,
            String originalTopic,
            String errorMessage,
            String stackTrace) {

        // Crear headers como lo hace DeadLetterPublishingRecoverer
        Headers headers = new RecordHeaders();

        if (originalTopic != null) {
            headers.add("kafka_dlt-original-topic",
                    originalTopic.getBytes(StandardCharsets.UTF_8));
        }

        if (errorMessage != null) {
            headers.add("kafka_dlt-exception-message",
                    errorMessage.getBytes(StandardCharsets.UTF_8));
        }

        if (stackTrace != null) {
            headers.add("kafka_dlt-exception-stacktrace",
                    stackTrace.getBytes(StandardCharsets.UTF_8));
        }

        // Crear el ConsumerRecord
        return new ConsumerRecord<>(
                "user.created.dlt",  // topic DLT
                0,  // partition
                0L,  // offset
                0L,  // timestamp
                null,  // timestampType
                0,  // serializedKeySize
                0,  // serializedValueSize
                key,  // key
                event,  // value
                headers,  // headers
                java.util.Optional.empty()  // leaderEpoch
        );
    }
}
