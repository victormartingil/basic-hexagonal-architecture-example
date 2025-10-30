package com.example.hexarch.user.infrastructure.adapter.output.event;

import com.example.hexarch.user.application.port.output.UserEventPublisher;
import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * INFRASTRUCTURE LAYER - Kafka Event Publisher Adapter (Output Adapter)
 *
 * Adaptador que publica eventos de dominio a Kafka como INTEGRATION EVENTS.
 * Permite comunicación asíncrona con otros microservicios.
 *
 * ¿QUÉ SON INTEGRATION EVENTS?
 * - Eventos publicados a un message broker (Kafka, RabbitMQ, etc.)
 * - Permiten comunicación ENTRE MICROSERVICIOS diferentes
 * - Persisten en disco (durables, no se pierden si la app se cae)
 * - Pueden ser reprocessados (replay)
 *
 * DIFERENCIA CON DOMAIN EVENTS (Spring Events):
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ Domain Events (SpringEventUserEventPublisherAdapter)            │
 * ├─────────────────────────────────────────────────────────────────┤
 * │ ✅ In-memory (rápido)                                            │
 * │ ✅ Mismo servicio (JVM)                                          │
 * │ ❌ No duradero (se pierde si app cae)                           │
 * │ ❌ No funciona entre microservicios                             │
 * │                                                                  │
 * │ Ejemplo: SendWelcomeEmailListener (mismo servicio)              │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ Integration Events (ESTA CLASE - KafkaUserEventPublisherAdapter)│
 * ├─────────────────────────────────────────────────────────────────┤
 * │ ✅ Duradero (persiste en Kafka)                                  │
 * │ ✅ Entre microservicios                                          │
 * │ ✅ Escalable (múltiples consumers)                              │
 * │ ✅ Replay posible (volver a procesar)                           │
 * │ ❌ Más complejo (infraestructura)                               │
 * │ ❌ Latencia mayor que in-memory                                 │
 * │                                                                  │
 * │ Ejemplo: UserEventsKafkaConsumer (otro microservicio)           │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * TOPICS Y NAMING:
 * - Usa dotted notation: "user.created", "order.placed"
 * - Más común en la industria
 * - También válido: "user-created" (hyphenated)
 *
 * KEYS Y PARTICIONES:
 * - Key = userId: Todos los eventos del mismo usuario van a la misma partición
 * - Garantiza ORDEN para eventos del mismo usuario
 * - Sin key: eventos se distribuyen aleatoriamente (sin garantía de orden)
 *
 * EJEMPLO:
 * {@code
 * // Con key (userId)
 * kafkaTemplate.send("user.created", userId, event);
 *
 * // Resultado: Orden garantizado para el mismo user
 * Partition 0: UserCreated(user=123) → UserUpdated(user=123) → UserDeleted(user=123)
 *              ↑ Todos en orden porque tienen la misma key
 * }
 *
 * SIN @Primary:
 * Esta clase NO tiene @Primary, así que NO es el adapter por defecto.
 * El adapter por defecto es SpringEventUserEventPublisherAdapter.
 *
 * Para usar ESTE adapter como principal:
 * 1. Quita @Primary de SpringEventUserEventPublisherAdapter
 * 2. Agrega @Primary a esta clase
 *
 * O mejor aún: Usa CompositeUserEventPublisherAdapter para publicar a AMBOS.
 *
 * ARQUITECTURA DE PUBLICACIÓN DUAL:
 *
 * CreateUserService
 *        ↓ (publica evento)
 * CompositeUserEventPublisherAdapter
 *        ↓
 *        ├─→ SpringEventUserEventPublisherAdapter → SendWelcomeEmailListener (local)
 *        └─→ KafkaUserEventPublisherAdapter → Kafka → UserEventsKafkaConsumer (otro MS)
 *
 * CONSUMER EN OTRO MICROSERVICIO:
 * Este adapter publica a Kafka. Luego, OTRO microservicio (Notifications Service)
 * consume los eventos:
 *
 * {@code
 * @Component
 * public class UserEventsKafkaConsumer {
 *     @KafkaListener(topics = "user.created")
 *     public void consume(UserCreatedEvent event) {
 *         notificationService.sendWelcomeEmail(event.email());
 *     }
 * }
 * }
 *
 * VENTAJAS DE KAFKA:
 * - ✅ Desacoplamiento total entre microservicios
 * - ✅ Durabilidad: eventos persistidos en disco
 * - ✅ Escalabilidad: múltiples consumers pueden procesar en paralelo
 * - ✅ Replay: puedes volver a procesar eventos históricos
 * - ✅ Tolerancia a fallos: si un consumer cae, otros siguen funcionando
 *
 * CUÁNDO USAR KAFKA:
 * - Comunicación entre microservicios independientes
 * - Necesitas durabilidad (no perder eventos)
 * - Necesitas replay de eventos
 * - Event sourcing
 * - Alto throughput de mensajes
 */
@Component
public class KafkaUserEventPublisherAdapter implements UserEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(KafkaUserEventPublisherAdapter.class);

    // Topic de Kafka donde se publican los eventos
    // Dotted notation es el estándar más común en la industria
    private static final String TOPIC_USER_CREATED = "user.created";

    // KafkaTemplate: Bean de Spring Kafka para publicar mensajes
    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    /**
     * Constructor con inyección de dependencias
     *
     * @param kafkaTemplate template configurado en KafkaConfig
     */
    public KafkaUserEventPublisherAdapter(KafkaTemplate<String, UserCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publica el evento de usuario creado a Kafka
     *
     * FUNCIONAMIENTO:
     * 1. Convierte el evento a JSON (automático con JsonSerializer)
     * 2. Envía a Kafka con userId como KEY (para garantizar orden)
     * 3. Kafka lo distribuye a los consumers suscritos al topic "user.created"
     * 4. Devuelve CompletableFuture para manejo asíncrono
     *
     * KEY = userId:
     * - Todos los eventos del mismo usuario van a la MISMA PARTICIÓN
     * - Garantiza que los eventos se procesen EN ORDEN para cada usuario
     * - Ejemplo: Si tienes UserCreated, UserUpdated, UserDeleted del mismo user,
     *   se procesarán en ese orden exacto
     *
     * TOPIC = "user.created":
     * - Naming: dotted notation (más común)
     * - Alternativa: "user-created" (hyphenated)
     * - Consumers se suscriben a este topic para recibir los eventos
     *
     * ASÍNCRONO:
     * - send() devuelve CompletableFuture<SendResult>
     * - No bloquea (fire and forget)
     * - Puedes agregar callbacks para manejar success/failure
     *
     * EJEMPLO DE USO CON CALLBACKS:
     * {@code
     * CompletableFuture<SendResult<String, UserCreatedEvent>> future = send(...);
     * future.whenComplete((result, ex) -> {
     *     if (ex == null) {
     *         logger.info("Evento enviado exitosamente");
     *     } else {
     *         logger.error("Error enviando evento: {}", ex.getMessage());
     *     }
     * });
     * }
     *
     * @param event evento a publicar
     */
    @Override
    public void publish(UserCreatedEvent event) {
        logger.info("📤 [KAFKA PUBLISHER] Publishing UserCreatedEvent to Kafka: userId={}, username={}",
                event.userId(), event.username());

        try {
            // Publicar a Kafka
            // - Topic: "user.created"
            // - Key: userId (para garantizar orden de eventos del mismo usuario)
            // - Value: event (se serializa automáticamente a JSON)
            CompletableFuture<SendResult<String, UserCreatedEvent>> future = kafkaTemplate.send(
                    TOPIC_USER_CREATED,
                    event.userId().toString(),  // ← KEY: userId (garantiza orden)
                    event                        // ← VALUE: evento completo
            );

            // Callback para loguear resultado (opcional)
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    // Éxito: evento publicado correctamente
                    logger.info("✅ [KAFKA PUBLISHER] Event published successfully to topic '{}', " +
                                    "partition={}, offset={}",
                            TOPIC_USER_CREATED,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());

                    logger.debug("Event details: userId={}, username={}, email={}",
                            event.userId(), event.username(), event.email());

                } else {
                    // Error: falló la publicación
                    logger.error("❌ [KAFKA PUBLISHER] Failed to publish event to Kafka: {}",
                            ex.getMessage(), ex);

                    // TODO: En un sistema real, aquí podrías:
                    // - Guardar en una "dead letter queue" (DLQ)
                    // - Reintentar con backoff exponencial
                    // - Enviar alerta a monitoreo
                    // - Guardar en BD para procesamiento posterior
                }
            });

        } catch (Exception e) {
            // Captura excepciones síncronas (ej: Kafka no disponible)
            logger.error("❌ [KAFKA PUBLISHER] Exception while sending event to Kafka: {}",
                    e.getMessage(), e);

            // TODO: Decidir qué hacer con el error
            // Opción 1: Lanzar excepción (fallará la creación del usuario)
            // Opción 2: Loguear y continuar (usuario se crea aunque falle Kafka)
            // En este caso, solo logueamos (opción 2)
        }
    }

    /**
     * NOTAS SOBRE TOPICS:
     *
     * Para crear topics manualmente (opcional, Kafka los crea automáticamente):
     *
     * {@code
     * kafka-topics.sh --create \
     *   --bootstrap-server localhost:9092 \
     *   --topic user.created \
     *   --partitions 3 \
     *   --replication-factor 1
     * }
     *
     * Para listar topics existentes:
     * {@code
     * kafka-topics.sh --list --bootstrap-server localhost:9092
     * }
     *
     * Para ver mensajes del topic (consumer de consola):
     * {@code
     * kafka-console-consumer.sh \
     *   --bootstrap-server localhost:9092 \
     *   --topic user.created \
     *   --from-beginning
     * }
     */
}
