package com.example.hexarch.user.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * DOMAIN LAYER - Domain Event
 *
 * Evento que representa que un usuario ha sido creado.
 * Los eventos de dominio son inmutables y representan hechos que ya ocurrieron.
 *
 * CUANDO USAR EVENTOS:
 * - Para comunicar que algo importante sucedió en el dominio
 * - Para desacoplar diferentes partes del sistema
 * - Para implementar Event Sourcing o Event-Driven Architecture
 *
 * En este ejemplo, este evento podría:
 * - Enviarse a un sistema de email para enviar correo de bienvenida
 * - Registrarse en un sistema de auditoría
 * - Publicarse en Kafka para otros microservicios
 *
 * INSTANT para occurredAt:
 * - Usamos Instant porque es un timestamp de cuando ocurrió el evento
 * - Instant es absoluto (UTC), perfecto para eventos distribuidos
 * - Permite que diferentes servicios en diferentes zonas horarias procesen el evento correctamente
 *
 * @param userId ID del usuario creado
 * @param username nombre del usuario creado
 * @param email email del usuario creado
 * @param occurredAt momento exacto en que ocurrió el evento (UTC)
 */
public record UserCreatedEvent(
    UUID userId,
    String username,
    String email,
    Instant occurredAt
) {

    /**
     * Factory method para crear el evento desde datos del usuario
     *
     * @param userId ID del usuario
     * @param username nombre de usuario
     * @param email email del usuario
     * @return nueva instancia del evento
     */
    public static UserCreatedEvent from(UUID userId, String username, String email) {
        return new UserCreatedEvent(
            userId,
            username,
            email,
            Instant.now()  // Timestamp actual en UTC
        );
    }
}
