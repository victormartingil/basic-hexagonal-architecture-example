package com.example.hexarch.notifications.infrastructure.kafka.consumer;

import com.example.hexarch.notifications.application.service.EmailService;
import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * INFRASTRUCTURE LAYER - Kafka Consumer
 *
 * Consumer que escucha eventos de Kafka del topic "user.created".
 *
 * ⭐ SIMULA OTRO MICROSERVICIO:
 * Este consumer representa un "Notifications Service" separado que:
 * - Es otro bounded context (otro microservicio)
 * - Escucha eventos publicados por el "User Service"
 * - Reacciona enviando notificaciones (email, SMS, push)
 *
 * ARQUITECTURA DE MICROSERVICIOS:
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ USER SERVICE (este proyecto)                                    │
 * ├─────────────────────────────────────────────────────────────────┤
 * │ CreateUserService                                               │
 * │        ↓                                                        │
 * │ KafkaUserEventPublisherAdapter                                  │
 * │        ↓ publica a Kafka                                        │
 * └────────┼───────────────────────────────────────────────────────┘
 *          ↓
 *     ┌────────┐
 *     │ KAFKA  │  Topic: "user.created"
 *     └────┬───┘
 *          ↓ consume de Kafka
 * ┌────────┼───────────────────────────────────────────────────────┐
 * │        ↓                                                        │
 * │ UserEventsKafkaConsumer (ESTA CLASE)                            │
 * │        ↓                                                        │
 * │ NotificationService (simulado)                                  │
 * │                                                                  │
 * │ NOTIFICATIONS SERVICE (simulado en este proyecto)               │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * EN UN SISTEMA REAL:
 * - User Service y Notifications Service serían proyectos separados
 * - Cada uno con su propia base de datos
 * - Desplegados independientemente
 * - Comunicándose solo vía Kafka (async messaging)
 *
 * ESTE PROYECTO (educativo):
 * - Simula ambos servicios en el mismo proyecto
 * - Usa paquetes separados: com.example.hexarch.user vs com.example.hexarch.notifications
 * - Demuestra cómo funcionaría la comunicación
 *
 * ¿QUÉ HACE @KafkaListener?
 * - Escucha automáticamente el topic "user.created"
 * - Spring Kafka deserializa el JSON a UserCreatedEvent
 * - Ejecuta el método consume() cada vez que llega un mensaje
 * - Maneja offsets automáticamente (guarda posición de lectura)
 *
 * GROUP ID:
 * - group-id = "notifications-service"
 * - Si hay múltiples instancias con el mismo group-id:
 *   → Kafka reparte mensajes entre ellas (load balancing)
 * - Si hay múltiples group-ids diferentes:
 *   → Cada grupo recibe TODOS los mensajes (broadcast)
 *
 * EJEMPLO:
 * {@code
 * // 2 consumers con MISMO group-id
 * Consumer A (group: notifications-service) → Partition 0, 2
 * Consumer B (group: notifications-service) → Partition 1
 * ↑ Comparten carga (cada mensaje lo procesa UNO)
 *
 * // 2 consumers con DIFERENTE group-id
 * Consumer A (group: notifications-service) → Recibe TODOS
 * Consumer B (group: analytics-service)     → Recibe TODOS
 * ↑ Cada uno procesa todos los mensajes
 * }
 *
 * ORDEN DE PROCESAMIENTO:
 * - Mensajes con la misma KEY van a la misma PARTICIÓN
 * - Orden garantizado DENTRO de una partición
 * - En nuestro caso: Key = userId → eventos del mismo user en orden
 *
 * MANEJO DE ERRORES:
 * - Si este método lanza excepción → Kafka NO avanza el offset
 * - Kafka reintenta el mismo mensaje (según configuración)
 * - Para evitar loops infinitos: usa Dead Letter Topic (DLT)
 *
 * CONCURRENCY:
 * - Configurado en KafkaConfig.setConcurrency(3)
 * - 3 threads consumiendo en paralelo
 * - Máximo = número de particiones (3 en nuestro caso)
 */
@Component
public class UserEventsKafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventsKafkaConsumer.class);

    // EmailService con Circuit Breaker protection
    private final EmailService emailService;

    /**
     * Constructor con inyección de EmailService
     *
     * @param emailService servicio de email con Circuit Breaker
     */
    public UserEventsKafkaConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Consume eventos de creación de usuario del topic "user.created"
     *
     * @KafkaListener:
     * - topics: Topic(s) a escuchar
     * - groupId: Grupo de consumers (para load balancing)
     * - containerFactory: Factory configurada en KafkaConfig
     *
     * @Payload: El evento deserializado automáticamente desde JSON
     * @Header: Metadatos del mensaje (partition, offset, key, etc.)
     *
     * FLUJO:
     * 1. User Service publica UserCreatedEvent a Kafka
     * 2. Kafka persiste el mensaje en el topic "user.created"
     * 3. Este consumer lee el mensaje
     * 4. Spring Kafka deserializa JSON → UserCreatedEvent
     * 5. Se ejecuta este método automáticamente
     * 6. Procesamos el evento (enviar email, SMS, push)
     * 7. Si no hay excepciones, Kafka avanza el offset (marca como procesado)
     *
     * SIMULACIÓN:
     * En este ejemplo solo logueamos, pero en un sistema real aquí:
     * - Llamarías a un EmailService
     * - Enviarías SMS via Twilio
     * - Enviarías push notifications
     * - Registrarías en analytics
     *
     * @param event   el evento de usuario creado
     * @param partition la partición de donde viene el mensaje
     * @param offset el offset del mensaje en la partición
     * @param key la key del mensaje (en nuestro caso, userId)
     */
    @KafkaListener(
            topics = "user.created",
            groupId = "notifications-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload UserCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        logger.info("📨 [KAFKA CONSUMER - NOTIFICATIONS SERVICE] Received UserCreatedEvent from Kafka");
        logger.info("    Topic: user.created");
        logger.info("    Partition: {} | Offset: {} | Key: {}", partition, offset, key);
        logger.info("    Event: userId={}, username={}, email={}",
                event.userId(), event.username(), event.email());

        try {
            // ═══════════════════════════════════════════════════════════
            // SIMULACIÓN DE NOTIFICATIONS SERVICE
            // ═══════════════════════════════════════════════════════════
            // En un microservicio real de notificaciones, aquí harías:

            logger.info("🔔 [NOTIFICATIONS SERVICE] Processing user registration notification");

            // 1. Enviar email de bienvenida
            logger.info("   ✉️  Sending welcome email to: {}", event.email());

            // ⚡ CIRCUIT BREAKER PROTECTION
            // Esta llamada está protegida por Circuit Breaker (@CircuitBreaker en EmailService)
            // Si EmailService falla repetidamente:
            // 1. Circuit Breaker detecta tasa de fallos alta
            // 2. Cambia a estado OPEN
            // 3. Llama a sendEmailFallback() en lugar de sendWelcomeEmail()
            // 4. Previene cascading failures
            emailService.sendWelcomeEmail(event.email(), event.username());

            // 2. Enviar SMS de confirmación (opcional)
            // if (event.phoneNumber() != null) {
            //     logger.info("   📱 Sending SMS confirmation to: {}", event.phoneNumber());
            //     smsService.sendWelcomeSMS(event.phoneNumber(), event.username());
            // }

            // 3. Enviar push notification (si tiene app móvil)
            // logger.info("   📲 Sending push notification to user: {}", event.userId());
            // pushService.sendPushNotification(event.userId(), "Welcome to our platform!");

            // 4. Registrar en analytics/CRM
            // logger.info("   📊 Registering user in CRM: {}", event.email());
            // crmService.createContact(event.email(), event.username());

            logger.info("✅ [NOTIFICATIONS SERVICE] User registration notification sent successfully");
            logger.info("─────────────────────────────────────────────────────────────────");

        } catch (Exception e) {
            // Manejo de errores
            logger.error("❌ [NOTIFICATIONS SERVICE] Failed to process notification: {}",
                    e.getMessage(), e);

            // ⚠️ IMPORTANTE: Decidir qué hacer con el error
            //
            // OPCIÓN 1: Lanzar excepción (IMPLEMENTADA)
            // - Kafka NO avanzará el offset
            // - Reintentará el mismo mensaje (según configuración en KafkaConfig)
            // - Después de N reintentos → Kafka enviará a DLT automáticamente
            // - DefaultErrorHandler con DeadLetterPublishingRecoverer maneja esto
            //
            // OPCIÓN 2: Loguear y continuar (NO lanzar excepción)
            // - Kafka avanza el offset (mensaje marcado como procesado)
            // - El mensaje "se pierde" (no se reintenta)
            // - Útil si el error no es crítico
            //
            // OPCIÓN 3: Enviar a Dead Letter Topic (DLT) manualmente
            // - Envías el mensaje problemático a otro topic manualmente
            // - Kafka avanza el offset
            // - Más control pero más código
            //
            // En este ejemplo: Opción 1 (lanzar excepción)
            // Permite que Kafka maneje reintentos y DLT automáticamente
            throw new RuntimeException("Failed to process notification", e);
            // Las notificaciones no son críticas, el usuario ya está creado
        }
    }

    /**
     * EJEMPLO ALTERNATIVO: Consumer con ConsumerRecord
     *
     * Si necesitas acceso completo al registro (headers, timestamp, etc.):
     *
     * {@code
     * @KafkaListener(topics = "user.created", groupId = "notifications-service")
     * public void consumeWithRecord(ConsumerRecord<String, UserCreatedEvent> record) {
     *     UserCreatedEvent event = record.value();
     *     String key = record.key();
     *     int partition = record.partition();
     *     long offset = record.offset();
     *     long timestamp = record.timestamp();
     *
     *     logger.info("Received event: {}", event);
     *     logger.info("Metadata: partition={}, offset={}, timestamp={}",
     *         partition, offset, timestamp);
     *
     *     // Acceder a headers custom
     *     record.headers().forEach(header -> {
     *         logger.info("Header: {} = {}", header.key(), new String(header.value()));
     *     });
     * }
     * }
     */

    /**
     * EJEMPLO: Consumer en batch (procesar múltiples mensajes juntos)
     *
     * Más eficiente si tienes alto throughput:
     *
     * {@code
     * @KafkaListener(topics = "user.created", groupId = "notifications-service")
     * public void consumeBatch(List<UserCreatedEvent> events) {
     *     logger.info("Received batch of {} events", events.size());
     *
     *     events.forEach(event -> {
     *         // Procesar evento
     *         logger.info("Processing event: {}", event);
     *     });
     *
     *     logger.info("Batch processed successfully");
     * }
     * }
     */

    /**
     * COMANDOS ÚTILES DE KAFKA:
     *
     * Ver mensajes del topic en tiempo real:
     * {@code
     * kafka-console-consumer.sh \
     *   --bootstrap-server localhost:9092 \
     *   --topic user.created \
     *   --from-beginning
     * }
     *
     * Ver consumer groups:
     * {@code
     * kafka-consumer-groups.sh \
     *   --bootstrap-server localhost:9092 \
     *   --list
     * }
     *
     * Ver lag (mensajes pendientes) de un grupo:
     * {@code
     * kafka-consumer-groups.sh \
     *   --bootstrap-server localhost:9092 \
     *   --group notifications-service \
     *   --describe
     * }
     *
     * Reset offsets (volver a procesar desde el principio):
     * {@code
     * kafka-consumer-groups.sh \
     *   --bootstrap-server localhost:9092 \
     *   --group notifications-service \
     *   --topic user.created \
     *   --reset-offsets --to-earliest \
     *   --execute
     * }
     */
}
