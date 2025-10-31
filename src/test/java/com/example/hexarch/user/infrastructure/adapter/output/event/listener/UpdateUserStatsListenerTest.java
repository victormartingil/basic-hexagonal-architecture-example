package com.example.hexarch.user.infrastructure.adapter.output.event.listener;

import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * UNIT TEST - UpdateUserStatsListener
 *
 * Tests unitarios para el listener que actualiza estadísticas de usuarios.
 *
 * ENFOQUE:
 * - Test unitario puro (sin Spring Context)
 * - No se mockea nada (el listener solo loguea)
 * - Verifica que el listener procesa eventos sin lanzar excepciones
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateUserStatsListener - Unit Tests")
class UpdateUserStatsListenerTest {

    @InjectMocks
    private UpdateUserStatsListener listener;

    private UserCreatedEvent event;

    @BeforeEach
    void setUp() {
        event = new UserCreatedEvent(
            UUID.randomUUID(),
            "testuser",
            "test@example.com",
            Instant.now()
        );
    }

    @Test
    @DisplayName("Should process UserCreatedEvent successfully")
    void shouldProcessEvent_whenValidEvent() {
        // WHEN & THEN - El listener debe procesar el evento sin lanzar excepciones
        assertDoesNotThrow(() -> listener.onUserCreated(event));
    }

    @Test
    @DisplayName("Should process event with different user IDs")
    void shouldProcessEvent_withDifferentUserIds() {
        // GIVEN - Eventos con diferentes IDs
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

        // WHEN & THEN - Ambos eventos deben procesarse correctamente
        assertDoesNotThrow(() -> {
            listener.onUserCreated(event1);
            listener.onUserCreated(event2);
        });
    }

    @Test
    @DisplayName("Should process event with null username gracefully")
    void shouldProcessEvent_whenUsernameIsNull() {
        // GIVEN - Evento con username null
        UserCreatedEvent eventWithNullUsername = new UserCreatedEvent(
            UUID.randomUUID(),
            null,
            "test@example.com",
            Instant.now()
        );

        // WHEN & THEN - Debe procesar sin lanzar excepción
        assertDoesNotThrow(() -> listener.onUserCreated(eventWithNullUsername));
    }

    @Test
    @DisplayName("Should process multiple events in sequence")
    void shouldProcessMultipleEventsInSequence() {
        // GIVEN - Múltiples eventos simulando registros consecutivos
        for (int i = 0; i < 5; i++) {
            UserCreatedEvent newEvent = new UserCreatedEvent(
                UUID.randomUUID(),
                "user" + i,
                "user" + i + "@example.com",
                Instant.now()
            );

            // WHEN & THEN - Cada evento debe procesarse correctamente
            assertDoesNotThrow(() -> listener.onUserCreated(newEvent));
        }
    }

    @Test
    @DisplayName("Should process event with past timestamp")
    void shouldProcessEvent_withPastTimestamp() {
        // GIVEN - Evento con timestamp en el pasado
        UserCreatedEvent pastEvent = new UserCreatedEvent(
            UUID.randomUUID(),
            "pastuser",
            "past@example.com",
            Instant.parse("2020-01-01T00:00:00Z")
        );

        // WHEN & THEN - Debe procesar sin lanzar excepción
        assertDoesNotThrow(() -> listener.onUserCreated(pastEvent));
    }

    @Test
    @DisplayName("Should process event with future timestamp")
    void shouldProcessEvent_withFutureTimestamp() {
        // GIVEN - Evento con timestamp en el futuro
        UserCreatedEvent futureEvent = new UserCreatedEvent(
            UUID.randomUUID(),
            "futureuser",
            "future@example.com",
            Instant.parse("2030-12-31T23:59:59Z")
        );

        // WHEN & THEN - Debe procesar sin lanzar excepción
        assertDoesNotThrow(() -> listener.onUserCreated(futureEvent));
    }
}
