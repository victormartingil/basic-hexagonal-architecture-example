package com.example.hexarch.user.infrastructure.adapter.output.event;

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

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UNIT TEST - SpringEventUserEventPublisherAdapter
 *
 * Tests unitarios para el publicador de Spring Events.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpringEventUserEventPublisherAdapter - Unit Tests")
class SpringEventUserEventPublisherAdapterTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<UserCreatedEvent> eventCaptor;

    private SpringEventUserEventPublisherAdapter adapter;

    private UserCreatedEvent event;

    @BeforeEach
    void setUp() {
        adapter = new SpringEventUserEventPublisherAdapter(eventPublisher);

        event = new UserCreatedEvent(
            UUID.randomUUID(),
            "testuser",
            "test@example.com",
            Instant.now()
        );
    }

    @Test
    @DisplayName("Should publish event successfully")
    void shouldPublishEvent_whenCalled() {
        // WHEN
        adapter.publish(event);

        // THEN - Verificar que el evento se publicó
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        UserCreatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent).isEqualTo(event);
        assertThat(capturedEvent.userId()).isEqualTo(event.userId());
        assertThat(capturedEvent.username()).isEqualTo(event.username());
        assertThat(capturedEvent.email()).isEqualTo(event.email());
    }

    @Test
    @DisplayName("Should publish multiple events")
    void shouldPublishMultipleEvents() {
        // GIVEN - Múltiples eventos
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

        // THEN
        verify(eventPublisher, times(3)).publishEvent(any(UserCreatedEvent.class));
    }

    @Test
    @DisplayName("Should propagate exception when publishing fails")
    void shouldPropagateException_whenPublishingFails() {
        // GIVEN - El publisher lanza una excepción
        doThrow(new RuntimeException("Publishing failed"))
            .when(eventPublisher).publishEvent(any(UserCreatedEvent.class));

        // WHEN & THEN - La excepción debe propagarse
        assertThatThrownBy(() -> adapter.publish(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Publishing failed");
    }

    @Test
    @DisplayName("Should publish event with all fields correctly")
    void shouldPublishEventWithAllFields() {
        // GIVEN - Evento con todos los campos
        UUID userId = UUID.randomUUID();
        String username = "completeuser";
        String email = "complete@example.com";
        Instant timestamp = Instant.now();

        UserCreatedEvent completeEvent = new UserCreatedEvent(
            userId,
            username,
            email,
            timestamp
        );

        // WHEN
        adapter.publish(completeEvent);

        // THEN - Verificar que todos los campos se pasaron correctamente
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        UserCreatedEvent captured = eventCaptor.getValue();
        assertThat(captured.userId()).isEqualTo(userId);
        assertThat(captured.username()).isEqualTo(username);
        assertThat(captured.email()).isEqualTo(email);
        assertThat(captured.occurredAt()).isEqualTo(timestamp);
    }
}
