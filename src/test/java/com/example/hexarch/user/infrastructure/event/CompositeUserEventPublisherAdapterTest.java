package com.example.hexarch.user.infrastructure.event;

import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * UNIT TEST - CompositeUserEventPublisherAdapter
 *
 * Tests unitarios para el publicador compuesto que publica eventos
 * tanto a Spring Events como a Kafka.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CompositeUserEventPublisherAdapter - Unit Tests")
class CompositeUserEventPublisherAdapterTest {

    @Mock
    private ApplicationEventPublisher springEventPublisher;

    @Mock
    private KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    @Mock
    private CompletableFuture<SendResult<String, UserCreatedEvent>> kafkaFuture;

    @Captor
    private ArgumentCaptor<UserCreatedEvent> eventCaptor;

    private CompositeUserEventPublisherAdapter adapter;

    private UserCreatedEvent event;

    @BeforeEach
    void setUp() {
        adapter = new CompositeUserEventPublisherAdapter(springEventPublisher, kafkaTemplate);

        event = new UserCreatedEvent(
            UUID.randomUUID(),
            "testuser",
            "test@example.com",
            Instant.now()
        );
    }

    @Test
    @DisplayName("Should publish event to both Spring Events and Kafka successfully")
    void shouldPublishToBothDestinations_whenSuccessful() {
        // GIVEN - Kafka devuelve un future exitoso
        when(kafkaTemplate.send(anyString(), anyString(), any(UserCreatedEvent.class)))
            .thenReturn(kafkaFuture);

        // WHEN
        adapter.publish(event);

        // THEN - Verificar que se publicó a Spring Events
        verify(springEventPublisher, times(1)).publishEvent(eventCaptor.capture());
        UserCreatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isEqualTo(event);

        // Verificar que se publicó a Kafka
        verify(kafkaTemplate, times(1)).send(
            eq("user.created"),
            eq(event.userId().toString()),
            eq(event)
        );
    }

    @Test
    @DisplayName("Should throw exception when Spring Events publication fails")
    void shouldThrowException_whenSpringEventsPublicationFails() {
        // GIVEN - Spring Events lanza una excepción
        doThrow(new RuntimeException("Spring Events error"))
            .when(springEventPublisher).publishEvent(any(UserCreatedEvent.class));

        // WHEN & THEN - Debe propagar la excepción
        assertThatThrownBy(() -> adapter.publish(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to publish event to Spring Events");

        // Verificar que NO se intentó publicar a Kafka (porque Spring Events falló primero)
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(UserCreatedEvent.class));
    }

    @Test
    @DisplayName("Should not throw exception when Kafka publication fails")
    void shouldNotThrowException_whenKafkaPublicationFails() {
        // GIVEN - Kafka lanza una excepción
        when(kafkaTemplate.send(anyString(), anyString(), any(UserCreatedEvent.class)))
            .thenThrow(new RuntimeException("Kafka error"));

        // WHEN - No debe lanzar excepción (Kafka falla de forma no crítica)
        adapter.publish(event);

        // THEN - Verificar que se publicó a Spring Events de todos modos
        verify(springEventPublisher, times(1)).publishEvent(event);
    }

    @Test
    @DisplayName("Should publish multiple events successfully")
    void shouldPublishMultipleEvents() {
        // GIVEN - Múltiples eventos y Kafka devuelve futures exitosos
        when(kafkaTemplate.send(anyString(), anyString(), any(UserCreatedEvent.class)))
            .thenReturn(kafkaFuture);

        UserCreatedEvent event1 = new UserCreatedEvent(
            UUID.randomUUID(),
            "user1",
            "user1@example.com",
            Instant.now()
        );

        UserCreatedEvent event2 = new UserCreatedEvent(
            UUID.randomUUID(),
            "user2",
            "user2@example.com",
            Instant.now()
        );

        // WHEN
        adapter.publish(event1);
        adapter.publish(event2);
        adapter.publish(event);

        // THEN - Verificar que todos los eventos se publicaron
        verify(springEventPublisher, times(3)).publishEvent(any(UserCreatedEvent.class));
        verify(kafkaTemplate, times(3)).send(
            eq("user.created"),
            anyString(),
            any(UserCreatedEvent.class)
        );
    }

    @Test
    @DisplayName("Should use correct Kafka topic and key")
    void shouldUseCorrectKafkaTopicAndKey() {
        // GIVEN
        when(kafkaTemplate.send(anyString(), anyString(), any(UserCreatedEvent.class)))
            .thenReturn(kafkaFuture);

        // WHEN
        adapter.publish(event);

        // THEN - Verificar topic y key correctos
        verify(kafkaTemplate).send(
            eq("user.created"),              // Topic correcto
            eq(event.userId().toString()),   // Key = userId
            eq(event)                         // Evento
        );
    }

    @Test
    @DisplayName("Should handle null event gracefully")
    void shouldHandleNullEvent() {
        // GIVEN - Evento null
        // WHEN & THEN - Puede lanzar NullPointerException, lo cual es esperado
        assertThatThrownBy(() -> adapter.publish(null))
            .isInstanceOf(Exception.class);
    }
}
