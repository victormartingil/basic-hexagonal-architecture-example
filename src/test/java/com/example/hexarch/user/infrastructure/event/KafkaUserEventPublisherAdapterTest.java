package com.example.hexarch.user.infrastructure.event;

import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * UNIT TEST - KafkaUserEventPublisherAdapter
 *
 * Test unitario para el adaptador de publicación de eventos a Kafka.
 * Verifica que los eventos se publiquen correctamente al topic de Kafka.
 *
 * QUÉS SE TESTEA:
 * - Publicación exitosa del evento a Kafka
 * - Topic correcto ("user.created")
 * - Key correcta (userId para garantizar orden)
 * - Payload correcto (evento completo)
 * - Manejo asíncrono con CompletableFuture
 *
 * MOCKING:
 * - KafkaTemplate: Mockeado para evitar dependencia real de Kafka
 * - SendResult: Respuesta simulada de Kafka
 *
 * FRAMEWORKS:
 * - JUnit 5: framework de testing
 * - Mockito: framework para mocks
 * - AssertJ: assertions fluidas
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaUserEventPublisherAdapter - Unit Tests")
class KafkaUserEventPublisherAdapterTest {

    @Mock
    private KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    @InjectMocks
    private KafkaUserEventPublisherAdapter publisher;

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
     * TEST CASE 1: Debe publicar evento exitosamente a Kafka
     *
     * GIVEN: Un evento válido de usuario creado
     * WHEN: Se publica el evento
     * THEN:
     *   - Se llama a kafkaTemplate.send() con los parámetros correctos
     *   - Topic = "user.created"
     *   - Key = userId (para garantizar orden)
     *   - Value = evento completo
     */
    @Test
    @DisplayName("Debe publicar evento exitosamente a Kafka con topic y key correctos")
    void shouldPublishEventSuccessfully() {
        // GIVEN - Configurar mock de KafkaTemplate
        @SuppressWarnings("unchecked")
        SendResult<String, UserCreatedEvent> mockSendResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, UserCreatedEvent>> completableFuture =
                CompletableFuture.completedFuture(mockSendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any(UserCreatedEvent.class)))
                .thenReturn(completableFuture);

        // WHEN - Publicar evento
        publisher.publish(testEvent);

        // THEN - Verificar que se llamó a send() con los parámetros correctos

        // 1. Verificar que se llamó a kafkaTemplate.send()
        verify(kafkaTemplate, times(1))
                .send(anyString(), anyString(), any(UserCreatedEvent.class));

        // 2. Capturar argumentos para verificar valores exactos
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);

        verify(kafkaTemplate).send(
                topicCaptor.capture(),
                keyCaptor.capture(),
                eventCaptor.capture()
        );

        // 3. Verificar topic correcto
        assertThat(topicCaptor.getValue())
                .isEqualTo("user.created");

        // 4. Verificar key correcta (userId para garantizar orden)
        assertThat(keyCaptor.getValue())
                .isEqualTo(testUserId.toString());

        // 5. Verificar evento completo
        UserCreatedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.userId()).isEqualTo(testUserId);
        assertThat(publishedEvent.username()).isEqualTo("johndoe");
        assertThat(publishedEvent.email()).isEqualTo("john@example.com");
        assertThat(publishedEvent.occurredAt()).isNotNull();
    }

    /**
     * TEST CASE 2: Debe usar userId como key para garantizar orden
     *
     * GIVEN: Múltiples eventos del mismo usuario
     * WHEN: Se publican los eventos
     * THEN: Todos deben usar el mismo userId como key
     */
    @Test
    @DisplayName("Debe usar userId como key para garantizar orden de eventos del mismo usuario")
    void shouldUseUserIdAsKeyForOrdering() {
        // GIVEN - Configurar mock
        @SuppressWarnings("unchecked")
        SendResult<String, UserCreatedEvent> mockSendResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, UserCreatedEvent>> completableFuture =
                CompletableFuture.completedFuture(mockSendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any(UserCreatedEvent.class)))
                .thenReturn(completableFuture);

        // WHEN - Publicar múltiples eventos del mismo usuario
        UUID sameUserId = UUID.randomUUID();

        UserCreatedEvent event1 = new UserCreatedEvent(
                sameUserId, "user1", "user1@test.com", Instant.now()
        );
        UserCreatedEvent event2 = new UserCreatedEvent(
                sameUserId, "user1", "user1@test.com", Instant.now().plusSeconds(1)
        );

        publisher.publish(event1);
        publisher.publish(event2);

        // THEN - Verificar que ambos eventos usaron el mismo key (userId)
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate, times(2))
                .send(eq("user.created"), keyCaptor.capture(), any(UserCreatedEvent.class));

        // Ambos keys deben ser iguales (mismo userId)
        assertThat(keyCaptor.getAllValues())
                .hasSize(2)
                .allMatch(key -> key.equals(sameUserId.toString()));
    }

    /**
     * TEST CASE 3: Debe manejar publicación asíncrona sin bloquear
     *
     * GIVEN: Un evento a publicar
     * WHEN: Se publica el evento
     * THEN: El método debe retornar inmediatamente (no bloquear)
     */
    @Test
    @DisplayName("Debe publicar eventos de forma asíncrona sin bloquear")
    void shouldPublishAsynchronously() {
        // GIVEN - CompletableFuture que simula operación asíncrona
        CompletableFuture<SendResult<String, UserCreatedEvent>> incompleteFuture =
                new CompletableFuture<>();

        when(kafkaTemplate.send(anyString(), anyString(), any(UserCreatedEvent.class)))
                .thenReturn(incompleteFuture);

        // WHEN - Publicar evento (no debe bloquear)
        // Si el método bloquea, este test nunca terminará

        long startTime = System.currentTimeMillis();
        publisher.publish(testEvent);
        long endTime = System.currentTimeMillis();

        // THEN - Debe retornar inmediatamente (< 100ms)
        assertThat(endTime - startTime).isLessThan(100);

        // Verificar que se llamó a send()
        verify(kafkaTemplate).send(anyString(), anyString(), any(UserCreatedEvent.class));

        // Completar el future (cleanup)
        incompleteFuture.complete(null);
    }

    /**
     * TEST CASE 4: No debe lanzar excepción si KafkaTemplate falla
     *
     * GIVEN: KafkaTemplate que falla al enviar
     * WHEN: Se intenta publicar un evento
     * THEN: No debe propagarse la excepción (fire-and-forget pattern)
     *
     * NOTA: El adapter actual loguea el error pero no propaga la excepción.
     * Esto es intencional para no afectar el flujo principal del negocio.
     */
    @Test
    @DisplayName("Debe manejar errores de Kafka sin lanzar excepción")
    void shouldHandleKafkaErrorsGracefully() {
        // GIVEN - KafkaTemplate que falla
        CompletableFuture<SendResult<String, UserCreatedEvent>> failedFuture =
                new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka is down"));

        when(kafkaTemplate.send(anyString(), anyString(), any(UserCreatedEvent.class)))
                .thenReturn(failedFuture);

        // WHEN & THEN - No debe lanzar excepción
        assertThatCode(() -> publisher.publish(testEvent))
                .doesNotThrowAnyException();

        // Verificar que se intentó enviar
        verify(kafkaTemplate).send(anyString(), anyString(), any(UserCreatedEvent.class));
    }

    /**
     * TEST CASE 5: Debe publicar evento con todos los datos del dominio
     *
     * GIVEN: Un evento con todos los datos
     * WHEN: Se publica el evento
     * THEN: Todos los datos deben preservarse (userId, username, email, timestamp)
     */
    @Test
    @DisplayName("Debe preservar todos los datos del evento de dominio")
    void shouldPreserveAllEventData() {
        // GIVEN - Evento con todos los datos
        UUID userId = UUID.randomUUID();
        Instant occurredOn = Instant.parse("2025-10-30T10:00:00Z");

        UserCreatedEvent detailedEvent = new UserCreatedEvent(
                userId,
                "testuser",
                "test@example.com",
                occurredOn
        );

        @SuppressWarnings("unchecked")
        SendResult<String, UserCreatedEvent> mockSendResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, UserCreatedEvent>> completableFuture =
                CompletableFuture.completedFuture(mockSendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any(UserCreatedEvent.class)))
                .thenReturn(completableFuture);

        // WHEN - Publicar evento
        publisher.publish(detailedEvent);

        // THEN - Capturar y verificar todos los datos
        ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

        UserCreatedEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent.userId()).isEqualTo(userId);
        assertThat(capturedEvent.username()).isEqualTo("testuser");
        assertThat(capturedEvent.email()).isEqualTo("test@example.com");
        assertThat(capturedEvent.occurredAt()).isEqualTo(occurredOn);
    }

    /**
     * TEST CASE 6: Debe usar el topic correcto según el estándar
     *
     * GIVEN: Diferentes eventos
     * WHEN: Se publican
     * THEN: Deben usar el topic "user.created" (dotted notation)
     */
    @Test
    @DisplayName("Debe usar topic 'user.created' con dotted notation")
    void shouldUseCorrectTopicNaming() {
        // GIVEN - Mock setup
        @SuppressWarnings("unchecked")
        SendResult<String, UserCreatedEvent> mockSendResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, UserCreatedEvent>> completableFuture =
                CompletableFuture.completedFuture(mockSendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any(UserCreatedEvent.class)))
                .thenReturn(completableFuture);

        // WHEN - Publicar evento
        publisher.publish(testEvent);

        // THEN - Verificar que usa dotted notation
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(
                topicCaptor.capture(),
                anyString(),
                any(UserCreatedEvent.class)
        );

        // Verificar formato del topic
        String capturedTopic = topicCaptor.getValue();
        assertThat(capturedTopic)
                .isEqualTo("user.created")
                .contains(".") // Dotted notation
                .doesNotContain("-"); // No hyphenated
    }
}
