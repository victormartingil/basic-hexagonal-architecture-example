package com.example.hexarch.user.application.port.output;

import com.example.hexarch.user.domain.event.UserCreatedEvent;

/**
 * APPLICATION LAYER - Output Port (Event Publisher Interface)
 *
 * Define el contrato para publicar eventos de dominio relacionados con usuarios.
 * Esta interfaz representa un PUERTO DE SALIDA de la aplicación.
 *
 * INVERSIÓN DE DEPENDENCIAS:
 * - La Application Layer define la interfaz (necesita publicar eventos)
 * - La Infrastructure Layer implementa la interfaz (usa Kafka, RabbitMQ, etc.)
 * - La dependencia apunta hacia adentro (Infrastructure → Application)
 *
 * EVENTOS DE DOMINIO:
 * - Representan hechos importantes que ocurrieron en el dominio
 * - Son inmutables (ya sucedieron, no se pueden cambiar)
 * - Se publican DESPUÉS de que la operación fue exitosa
 * - Permiten comunicación asíncrona entre contextos/servicios
 *
 * CASOS DE USO:
 * - Enviar email de bienvenida cuando se crea un usuario
 * - Notificar a otros microservicios de cambios
 * - Implementar Event Sourcing
 * - Auditoría y registro de eventos
 *
 * NOMENCLATURA:
 * - Formato: {Entidad}EventPublisher
 * - Ejemplos: UserEventPublisher, ProductEventPublisher, OrderEventPublisher
 */
public interface UserEventPublisher {

    /**
     * Publica el evento de usuario creado
     *
     * Este método no debe lanzar excepciones críticas que detengan el flujo.
     * Si falla la publicación, debería registrarse pero no fallar la operación.
     *
     * @param event evento a publicar
     */
    void publish(UserCreatedEvent event);
}
