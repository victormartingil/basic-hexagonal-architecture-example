package com.example.hexarch.user.infrastructure.event;

import com.example.hexarch.user.application.port.UserEventPublisher;
import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * INFRASTRUCTURE LAYER - Event Publisher Adapter (Output Adapter)
 *
 * Adaptador ALTERNATIVO que implementa el puerto de salida UserEventPublisher.
 * Esta implementación simplemente LOGUEA los eventos sin publicarlos a ningún sistema.
 *
 * ⚠️ IMPLEMENTACIÓN ALTERNATIVA:
 * Esta NO es la implementación por defecto. La implementación principal es:
 * - SpringEventUserEventPublisherAdapter (@Primary): Publica eventos usando Spring Events
 *
 * Esta implementación es útil para:
 * - Testing y desarrollo local sin necesidad de eventos
 * - Debug y troubleshooting (ver eventos en logs sin procesarlos)
 * - Ambientes donde no quieres que se ejecuten los listeners
 *
 * CÓMO CAMBIAR A ESTA IMPLEMENTACIÓN:
 * 1. Quita @Primary de SpringEventUserEventPublisherAdapter
 * 2. Agrega @Primary a esta clase
 * 3. Reinicia la aplicación
 *
 * DIFERENCIAS ENTRE IMPLEMENTACIONES:
 *
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ LogUserEventPublisherAdapter (ESTA CLASE)                                   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ ✅ Solo loguea eventos                                                       │
 * │ ✅ No ejecuta listeners (@EventListener no se ejecutan)                     │
 * │ ✅ Útil para testing sin side effects                                       │
 * │ ✅ Simple y sin dependencias                                                │
 * │ ❌ Los eventos no se procesan (no se envía email, no se actualizan stats)  │
 * └─────────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ SpringEventUserEventPublisherAdapter (@Primary - DEFAULT)                   │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ ✅ Publica eventos IN-MEMORY usando Spring ApplicationEventPublisher        │
 * │ ✅ Los listeners (@EventListener) se ejecutan automáticamente               │
 * │ ✅ Ejemplos: SendWelcomeEmailListener, UpdateUserStatsListener              │
 * │ ✅ Perfecto para eventos de dominio dentro del mismo bounded context        │
 * │ ✅ Desacopla la lógica de negocio de los side effects                       │
 * │ ❌ Solo funciona en la misma JVM (no para microservicios distribuidos)     │
 * └─────────────────────────────────────────────────────────────────────────────┘
 *
 * OTRAS IMPLEMENTACIONES POSIBLES:
 * - KafkaUserEventPublisherAdapter: Publicar a Kafka (integration events entre microservicios)
 * - RabbitMQUserEventPublisherAdapter: Publicar a RabbitMQ
 * - AwsUserEventPublisherAdapter: Publicar a AWS SNS/SQS
 * - CompositeUserEventPublisherAdapter: Publicar a múltiples destinos (Spring Events + Kafka)
 *
 * NOMENCLATURA:
 * - Formato: {Tecnologia}{Entidad}EventPublisherAdapter
 * - Ejemplos: SpringEventUserEventPublisherAdapter, KafkaUserEventPublisherAdapter, LogUserEventPublisherAdapter
 *
 * VENTAJA DE LA ABSTRACCIÓN:
 * - La Application Layer no sabe cómo se publican los eventos
 * - Podemos cambiar la implementación sin tocar Application/Domain
 * - Ejemplo: cambiar de Spring Events a Kafka solo requiere cambiar el adapter y su configuración
 * - Podemos tener múltiples implementaciones y elegir con @Primary o @Qualifier
 *
 * CUÁNDO USAR ESTA IMPLEMENTACIÓN:
 * - Tests unitarios donde no quieres que se ejecuten listeners
 * - Debugging para ver qué eventos se están publicando
 * - Ambientes de desarrollo donde prefieres logs simples
 * - Cuando estés desarrollando y no quieras side effects (emails, stats, etc.)
 */
@Component  // Spring lo registra como bean
public class LogUserEventPublisherAdapter implements UserEventPublisher {

    // Logger para registrar eventos
    private static final Logger logger = LoggerFactory.getLogger(LogUserEventPublisherAdapter.class);

    /**
     * Publica el evento de usuario creado (implementación de solo logging)
     *
     * ⚠️ IMPLEMENTACIÓN ALTERNATIVA:
     * Esta implementación solo LOGUEA el evento. No lo publica a ningún sistema.
     * Los @EventListener NO se ejecutarán con esta implementación.
     *
     * Para que los listeners (SendWelcomeEmailListener, UpdateUserStatsListener) se ejecuten,
     * usa SpringEventUserEventPublisherAdapter (la implementación @Primary por defecto).
     *
     * CUÁNDO SE USA ESTA IMPLEMENTACIÓN:
     * - Cuando LogUserEventPublisherAdapter tiene @Primary (en lugar de SpringEventUserEventPublisherAdapter)
     * - En tests donde no quieres ejecutar los listeners
     * - Durante debugging para ver eventos sin procesarlos
     *
     * EJEMPLO DE LOG:
     * INFO - [LOG ONLY] User created event published: UserCreatedEvent[userId=..., username=johndoe, ...]
     *
     * NOTA: Si ves este log y esperabas que se ejecutaran los listeners,
     * probablemente necesitas cambiar a SpringEventUserEventPublisherAdapter.
     *
     * @param event evento a publicar (solo se loguea, no se procesa)
     */
    @Override
    public void publish(UserCreatedEvent event) {
        // Solo loguea el evento - NO lo publica a ningún sistema
        logger.info("🔍 [LOG ONLY] User created event published: {}", event);
        logger.debug("⚠️  Note: This is LogUserEventPublisherAdapter - listeners will NOT execute");
        logger.debug("    To execute listeners (SendWelcomeEmailListener, UpdateUserStatsListener),");
        logger.debug("    use SpringEventUserEventPublisherAdapter (@Primary)");

        // NOTA: Estos son ejemplos de implementaciones reales que podrías hacer
        // en otros adapters (KafkaUserEventPublisherAdapter, etc.)

        // EJEMPLO: Publicar a Kafka (ver KafkaUserEventPublisherAdapter)
        // kafkaTemplate.send("user-events", event.userId().toString(), event);

        // EJEMPLO: Publicar a RabbitMQ (ver RabbitMQUserEventPublisherAdapter)
        // rabbitTemplate.convertAndSend("user-exchange", "user.created", event);

        // EJEMPLO: Publicar a AWS SNS/SQS (ver AwsUserEventPublisherAdapter)
        // snsClient.publish(topicArn, objectMapper.writeValueAsString(event));

        // EJEMPLO: Publicar a Spring Events (ver SpringEventUserEventPublisherAdapter - ACTUAL DEFAULT)
        // applicationEventPublisher.publishEvent(event);
    }
}
