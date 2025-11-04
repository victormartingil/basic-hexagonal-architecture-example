package com.example.hexarch.user.infrastructure.event;

import com.example.hexarch.user.application.port.UserEventPublisher;
import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * INFRASTRUCTURE LAYER - Composite Event Publisher Adapter (Output Adapter)
 *
 * Adaptador que publica eventos a MÚLTIPLES DESTINOS simultáneamente:
 * 1. Spring Events (in-memory) → Listeners locales
 * 2. Kafka (message broker) → Otros microservicios
 *
 * ¿QUÉ ES DUAL PUBLISHING?
 * Publicar el mismo evento a diferentes sistemas al mismo tiempo.
 *
 * VENTAJAS:
 * - ✅ Listeners locales reaccionan inmediatamente (in-memory, rápido)
 * - ✅ Otros microservicios reciben el evento vía Kafka (durable, escalable)
 * - ✅ Lo mejor de ambos mundos
 * - ✅ Sin modificar CreateUserService (sigue usando UserEventPublisher)
 *
 * ARQUITECTURA:
 *
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ CreateUserService                                                │
 * │        ↓ publica evento                                          │
 * │ UserEventPublisher (interface)                                   │
 * └───────┼──────────────────────────────────────────────────────────┘
 *         ↓
 * ┌───────┼──────────────────────────────────────────────────────────┐
 * │       ↓                                                           │
 * │ CompositeUserEventPublisherAdapter (@Primary - ESTA CLASE)       │
 * │       ↓                                                           │
 * │       ├─→ SpringEventPublisher → SendWelcomeEmailListener        │
 * │       │                       → UpdateUserStatsListener           │
 * │       │                                                           │
 * │       └─→ KafkaTemplate → Kafka → UserEventsKafkaConsumer        │
 * │                                    (Notifications Service)        │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * FLUJO COMPLETO:
 *
 * 1. CreateUserService.execute() crea un usuario
 * 2. Publica UserCreatedEvent usando UserEventPublisher
 * 3. Spring inyecta CompositeUserEventPublisherAdapter (@Primary)
 * 4. CompositeUserEventPublisherAdapter:
 *    a) Publica a Spring Events (in-memory)
 *       → SendWelcomeEmailListener se ejecuta
 *       → UpdateUserStatsListener se ejecuta
 *    b) Publica a Kafka
 *       → Evento persiste en topic "user.created"
 *       → UserEventsKafkaConsumer (otro MS) consume el evento
 * 5. Usuario creado + emails enviados + evento en Kafka ✅
 *
 * ¿POR QUÉ USAR AMBOS?
 *
 * SPRING EVENTS (in-memory):
 * - ✅ Rápido (memoria, sin latencia de red)
 * - ✅ Side effects locales inmediatos (email, cache, stats)
 * - ✅ Simple, no requiere infraestructura externa
 * - ❌ Solo funciona en la misma JVM
 * - ❌ Se pierde si la app cae
 *
 * Ejemplo: SendWelcomeEmailListener (mismo servicio)
 *
 * KAFKA (message broker):
 * - ✅ Comunicación entre microservicios
 * - ✅ Duradero (persiste en disco)
 * - ✅ Escalable (múltiples consumers)
 * - ✅ Replay posible
 * - ❌ Latencia mayor (red, serialización)
 * - ❌ Requiere infraestructura (Kafka cluster)
 *
 * Ejemplo: UserEventsKafkaConsumer (Notifications Service, otro MS)
 *
 * ESCENARIOS DE USO:
 *
 * 1. E-COMMERCE: Order.placed event
 *    - Spring Events → actualizar stock localmente (inmediato)
 *    - Kafka → notificar warehouse service (otro MS)
 *
 * 2. USER REGISTRATION: UserCreated event
 *    - Spring Events → enviar email de bienvenida (inmediato)
 *    - Kafka → sincronizar con CRM service (otro MS)
 *
 * 3. PAYMENT: PaymentCompleted event
 *    - Spring Events → actualizar orden localmente
 *    - Kafka → notificar shipping service (otro MS)
 *
 * @Primary:
 * Esta clase tiene @Primary, por lo que es el adapter POR DEFECTO.
 * Spring inyectará automáticamente esta implementación cuando se requiera UserEventPublisher.
 *
 * OTRAS IMPLEMENTACIONES DISPONIBLES (sin @Primary):
 * - SpringEventUserEventPublisherAdapter: Solo Spring Events
 * - KafkaUserEventPublisherAdapter: Solo Kafka
 * - LogUserEventPublisherAdapter: Solo logs
 *
 * CÓMO CAMBIAR DE IMPLEMENTACIÓN:
 * Si quieres usar otra implementación:
 * 1. Quita @Primary de esta clase
 * 2. Agrega @Primary a la implementación deseada
 * 3. Reinicia la aplicación
 *
 * O usa @Qualifier en la inyección:
 * {@code
 * @Autowired
 * @Qualifier("springEventUserEventPublisherAdapter")
 * private UserEventPublisher eventPublisher;
 * }
 */
@Component
@Primary  // Esta es la implementación por defecto
public class CompositeUserEventPublisherAdapter implements UserEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(CompositeUserEventPublisherAdapter.class);

    // Spring Events (in-memory)
    private final ApplicationEventPublisher springEventPublisher;

    // Kafka (message broker)
    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    /**
     * Constructor con inyección de dependencias
     *
     * @param springEventPublisher publicador de Spring Events (in-memory)
     * @param kafkaTemplate        template de Kafka (message broker)
     */
    public CompositeUserEventPublisherAdapter(
            ApplicationEventPublisher springEventPublisher,
            KafkaTemplate<String, UserCreatedEvent> kafkaTemplate) {
        this.springEventPublisher = springEventPublisher;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publica el evento a AMBOS destinos: Spring Events + Kafka
     *
     * ORDEN DE PUBLICACIÓN:
     * 1. Primero Spring Events (in-memory) - Rápido, síncrono
     * 2. Después Kafka (message broker) - Más lento, asíncrono
     *
     * ¿POR QUÉ EN ESE ORDEN?
     * - Spring Events se ejecutan en la misma transacción
     * - Si falla, hace rollback de la creación del usuario
     * - Kafka es asíncrono (fire and forget)
     * - Si Kafka falla, el usuario ya está creado (no crítico)
     *
     * TRANSACCIONALIDAD:
     * - Spring Events: Dentro de la misma transacción de BD
     *   → Si listener falla, rollback completo
     * - Kafka: Fuera de la transacción (async)
     *   → Si falla, usuario igual se crea (solo se loguea el error)
     *
     * MANEJO DE ERRORES:
     * - Si Spring Events falla → se propaga la excepción (rollback)
     * - Si Kafka falla → se captura y loguea (no afecta la transacción)
     *
     * ALTERNATIVA: Publicar solo a Kafka:
     * Si prefieres que los listeners locales NO se ejecuten,
     * usa KafkaUserEventPublisherAdapter directamente (quita @Primary de aquí).
     *
     * @param event evento a publicar
     */
    @Override
    public void publish(UserCreatedEvent event) {
        logger.info("🔀 [COMPOSITE PUBLISHER] Publishing UserCreatedEvent to MULTIPLE destinations:");
        logger.info("    1. Spring Events (in-memory) → Local listeners");
        logger.info("    2. Kafka (message broker) → Other microservices");
        logger.info("    Event: userId={}, username={}, email={}",
                event.userId(), event.username(), event.email());

        // ═══════════════════════════════════════════════════════════════
        // 1. PUBLICAR A SPRING EVENTS (in-memory)
        // ═══════════════════════════════════════════════════════════════
        try {
            logger.debug("📡 [COMPOSITE] Publishing to Spring Events...");

            springEventPublisher.publishEvent(event);

            logger.info("✅ [COMPOSITE] Event published to Spring Events successfully");
            logger.debug("    → SendWelcomeEmailListener will execute");
            logger.debug("    → UpdateUserStatsListener will execute");

        } catch (Exception e) {
            // Error en Spring Events: CRÍTICO
            // Si un listener local falla, NO queremos crear el usuario
            logger.error("❌ [COMPOSITE] Failed to publish to Spring Events: {}", e.getMessage(), e);

            // Propagar excepción → rollback de la transacción completa
            throw new RuntimeException("Failed to publish event to Spring Events", e);
        }

        // ═══════════════════════════════════════════════════════════════
        // 2. PUBLICAR A KAFKA (message broker)
        // ═══════════════════════════════════════════════════════════════
        try {
            logger.debug("📤 [COMPOSITE] Publishing to Kafka...");

            kafkaTemplate.send(
                    "user.created",                  // Topic
                    event.userId().toString(),       // Key (para ordenamiento)
                    event                             // Value
            ).whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("✅ [COMPOSITE] Event published to Kafka successfully");
                    logger.debug("    → Topic: user.created");
                    logger.debug("    → Partition: {}", result.getRecordMetadata().partition());
                    logger.debug("    → Offset: {}", result.getRecordMetadata().offset());
                    logger.debug("    → UserEventsKafkaConsumer (Notifications Service) will consume");
                } else {
                    logger.error("❌ [COMPOSITE] Failed to publish to Kafka: {}", ex.getMessage(), ex);
                }
            });

        } catch (Exception e) {
            // Error en Kafka: NO CRÍTICO
            // El usuario ya está creado (Spring Events ejecutado)
            // Solo logueamos el error, no afectamos la creación del usuario
            logger.error("❌ [COMPOSITE] Exception while publishing to Kafka: {}", e.getMessage(), e);

            // NO propagar excepción → usuario se crea aunque falle Kafka
            // En producción, podrías:
            // - Guardar en una cola de reintentos
            // - Enviar alerta a monitoreo
            // - Usar Outbox Pattern (guardar evento en BD para procesar después)
        }

        logger.info("🏁 [COMPOSITE] Event publishing completed");
        logger.info("─────────────────────────────────────────────────────────────────");
    }

    /**
     * PATRÓN ALTERNATIVO: Outbox Pattern
     *
     * Si necesitas garantía total de que el evento llegue a Kafka:
     *
     * 1. Guardar evento en tabla "outbox" en la MISMA TRANSACCIÓN del usuario
     * 2. Un job separado lee la tabla outbox y publica a Kafka
     * 3. Si falla, reintenta automáticamente
     *
     * {@code
     * @Override
     * public void publish(UserCreatedEvent event) {
     *     // 1. Publicar a Spring Events (in-memory)
     *     springEventPublisher.publishEvent(event);
     *
     *     // 2. Guardar en outbox (misma transacción)
     *     OutboxMessage outboxMessage = new OutboxMessage(
     *         "user.created",
     *         event.userId().toString(),
     *         objectMapper.writeValueAsString(event)
     *     );
     *     outboxRepository.save(outboxMessage);
     *
     *     // 3. Un job separado publicará a Kafka después
     *     // (fuera de esta transacción)
     * }
     * }
     *
     * VENTAJAS DEL OUTBOX PATTERN:
     * - ✅ Garantiza que el evento llegue a Kafka (eventualmente)
     * - ✅ Transaccionalidad: evento guardado en BD con el usuario
     * - ✅ Reintentos automáticos si falla Kafka
     * - ❌ Más complejo (requiere tabla outbox + job)
     */

    /**
     * PATRÓN ALTERNATIVO: Async Spring Events
     *
     * Si quieres que Spring Events también sea asíncrono:
     *
     * {@code
     * @Configuration
     * @EnableAsync
     * public class AsyncConfig {
     *     @Bean
     *     public Executor asyncExecutor() {
     *         ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
     *         executor.setCorePoolSize(3);
     *         executor.setMaxPoolSize(10);
     *         executor.setQueueCapacity(100);
     *         executor.setThreadNamePrefix("async-event-");
     *         executor.initialize();
     *         return executor;
     *     }
     * }
     *
     * @Component
     * public class SendWelcomeEmailListener {
     *     @Async
     *     @EventListener
     *     public void onUserCreated(UserCreatedEvent event) {
     *         // Se ejecuta en otro thread (asíncrono)
     *         emailService.send(event.email());
     *     }
     * }
     * }
     *
     * VENTAJAS:
     * - ✅ No bloquea la transacción principal
     * - ✅ Mejor performance percibida
     * - ❌ Si falla, no hace rollback del usuario
     * - ❌ Se ejecuta FUERA de la transacción
     */
}
