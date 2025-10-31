package com.example.hexarch.user.infrastructure.adapter.output.event;

import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * UNIT TEST - LogUserEventPublisherAdapter
 *
 * Tests unitarios para el publicador de logs.
 */
@DisplayName("LogUserEventPublisherAdapter - Unit Tests")
class LogUserEventPublisherAdapterTest {

    private LogUserEventPublisherAdapter adapter;

    private UserCreatedEvent event;

    @BeforeEach
    void setUp() {
        adapter = new LogUserEventPublisherAdapter();

        event = new UserCreatedEvent(
            UUID.randomUUID(),
            "testuser",
            "test@example.com",
            Instant.now()
        );
    }

    @Test
    @DisplayName("Should log event successfully without throwing exceptions")
    void shouldLogEvent_whenCalled() {
        // WHEN & THEN - Debe ejecutarse sin lanzar excepciones
        assertDoesNotThrow(() -> adapter.publish(event));
    }

    @Test
    @DisplayName("Should handle multiple events")
    void shouldHandleMultipleEvents() {
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

        // WHEN & THEN - Todos los eventos deben loguearse correctamente
        assertDoesNotThrow(() -> {
            adapter.publish(event1);
            adapter.publish(event2);
            adapter.publish(event);
        });
    }

    @Test
    @DisplayName("Should handle event with null fields gracefully")
    void shouldHandleNullFields() {
        // GIVEN - Evento con campos null
        UserCreatedEvent eventWithNulls = new UserCreatedEvent(
            UUID.randomUUID(),
            null,
            null,
            Instant.now()
        );

        // WHEN & THEN - Debe manejar sin problemas
        assertDoesNotThrow(() -> adapter.publish(eventWithNulls));
    }

    @Test
    @DisplayName("Should handle high volume of events")
    void shouldHandleHighVolumeOfEvents() {
        // GIVEN & WHEN - Publicar muchos eventos
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 100; i++) {
                UserCreatedEvent bulkEvent = new UserCreatedEvent(
                    UUID.randomUUID(),
                    "user" + i,
                    "user" + i + "@example.com",
                    Instant.now()
                );
                adapter.publish(bulkEvent);
            }
        });
    }
}
