package com.example.hexarch.user.infrastructure.event.listener;

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
 * UNIT TEST - SendWelcomeEmailListener
 *
 * Tests unitarios para el listener que envía emails de bienvenida.
 *
 * ENFOQUE:
 * - Test unitario puro (sin Spring Context)
 * - No se mockea nada (el listener solo loguea)
 * - Verifica que el listener procesa eventos sin lanzar excepciones
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SendWelcomeEmailListener - Unit Tests")
class SendWelcomeEmailListenerTest {

    @InjectMocks
    private SendWelcomeEmailListener listener;

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
    @DisplayName("Should process event with null email gracefully")
    void shouldProcessEvent_whenEmailIsNull() {
        // GIVEN - Evento con email null
        UserCreatedEvent eventWithNullEmail = new UserCreatedEvent(
            UUID.randomUUID(),
            "testuser",
            null,
            Instant.now()
        );

        // WHEN & THEN - Debe procesar sin lanzar excepción
        assertDoesNotThrow(() -> listener.onUserCreated(eventWithNullEmail));
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
    @DisplayName("Should process multiple events successfully")
    void shouldProcessMultipleEvents() {
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

        // WHEN & THEN - Todos los eventos deben procesarse correctamente
        assertDoesNotThrow(() -> {
            listener.onUserCreated(event1);
            listener.onUserCreated(event2);
            listener.onUserCreated(event);
        });
    }
}
