package com.example.hexarch.notifications.infrastructure.kafka.consumer;

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
 * INFRASTRUCTURE LAYER - Dead Letter Topic (DLT) Consumer
 *
 * Consumer que escucha el Dead Letter Topic (DLT) para mensajes fallidos.
 *
 * ¿QUÉ ES UN DLT (DEAD LETTER TOPIC)?
 * - Topic especial para mensajes que fallaron al procesarse
 * - Evita loops infinitos con mensajes problemáticos
 * - Permite revisar/debuguear/reprocesar mensajes fallidos
 *
 * FLUJO COMPLETO:
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │ 1. Mensaje llega a UserEventsKafkaConsumer                          │
 * │    Topic: "user.created"                                            │
 * └────────────┬────────────────────────────────────────────────────────┘
 *              │
 *              ▼
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │ 2. Consumer intenta procesar el mensaje                             │
 * │    ❌ Falla (ej: email service down, error en código, etc.)         │
 * └────────────┬────────────────────────────────────────────────────────┘
 *              │
 *              ▼
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │ 3. DefaultErrorHandler reintenta automáticamente                    │
 * │    • Intento 1: espera 1 segundo, reintenta → Falla ❌              │
 * │    • Intento 2: espera 1 segundo, reintenta → Falla ❌              │
 * │    • Intento 3: espera 1 segundo, reintenta → Falla ❌              │
 * └────────────┬────────────────────────────────────────────────────────┘
 *              │
 *              ▼
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │ 4. DeadLetterPublishingRecoverer envía a DLT                        │
 * │    Topic: "user.created.dlt"                                        │
 * │    Consumer original continúa con siguiente mensaje ✅               │
 * └────────────┬────────────────────────────────────────────────────────┘
 *              │
 *              ▼
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │ 5. UserCreatedEventDLTConsumer (ESTA CLASE) recibe el mensaje      │
 * │    • Loguea el error para investigación                             │
 * │    • Opcionalmente: guarda en BD, envía alerta, reintenta manual   │
 * └─────────────────────────────────────────────────────────────────────┘
 *
 * ¿POR QUÉ USAR DLT?
 *
 * SIN DLT:
 * - Consumer se atasca reintentando el mismo mensaje infinitamente
 * - Bloquea el procesamiento de mensajes siguientes
 * - Difícil identificar/debuguear mensajes problemáticos
 *
 * CON DLT:
 * - ✅ Consumer no se bloquea (continúa con siguiente mensaje)
 * - ✅ Mensajes problemáticos se almacenan para investigación
 * - ✅ Puedes reprocesar manualmente después de fix
 * - ✅ Monitoreo: alertas si DLT crece mucho
 *
 * CASOS DE USO REALES:
 *
 * 1. **Email service down (error transitorio)**
 *    - Mensaje va a DLT
 *    - Cuando service se recupera → reprocesas DLT
 *
 * 2. **Datos inválidos (error permanente)**
 *    - Mensaje va a DLT
 *    - Investigas el problema
 *    - Corriges datos y reprocesas
 *
 * 3. **Bug en código (error permanente)**
 *    - Mensaje va a DLT
 *    - Despliegas fix
 *    - Reprocesas mensajes del DLT
 *
 * QUÉ HACER CON MENSAJES EN DLT:
 *
 * 1. **Loguear** (lo que hace esta clase)
 *    - Ver qué mensajes fallaron
 *    - Investigar causa raíz
 *
 * 2. **Guardar en BD** (recomendado para producción)
 *    - Tabla: failed_messages (id, topic, message, error, timestamp)
 *    - Dashboard para ver mensajes fallidos
 *
 * 3. **Enviar alertas** (Slack, PagerDuty, etc.)
 *    - Si DLT crece mucho → algo está mal
 *    - Alertar al equipo
 *
 * 4. **Reprocesar** (manualmente o automáticamente)
 *    - Endpoint admin: POST /admin/retry-dlt
 *    - Lee mensajes del DLT y republica al topic original
 *
 * CONFIGURACIÓN:
 * Este consumer escucha el topic "user.created.dlt" configurado automáticamente
 * por DeadLetterPublishingRecoverer en KafkaConfig.
 *
 * GROUP ID DIFERENTE:
 * - Consumer principal: group-id = "notifications-service"
 * - DLT Consumer: group-id = "notifications-service-dlt"
 * - Importante: grupos diferentes para que no interfieran
 */
@Component
public class UserCreatedEventDLTConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserCreatedEventDLTConsumer.class);

    /**
     * Consume mensajes fallidos del Dead Letter Topic
     *
     * Este método se ejecuta cuando un mensaje falla después de N reintentos
     * y es enviado automáticamente al topic DLT por DeadLetterPublishingRecoverer.
     *
     * INFORMACIÓN DISPONIBLE:
     * - event: El evento original que falló
     * - partition, offset, key: Metadatos de Kafka
     * - Headers adicionales agregados por DeadLetterPublishingRecoverer:
     *   • kafka_dlt-original-topic: Topic original ("user.created")
     *   • kafka_dlt-exception-message: Mensaje de error
     *   • kafka_dlt-exception-stacktrace: Stack trace completo
     *
     * QUÉ HACER AQUÍ:
     * 1. ✅ Loguear para debugging (lo que hacemos ahora)
     * 2. 💾 Guardar en tabla failed_messages (recomendado producción)
     * 3. 🔔 Enviar alerta si es crítico
     * 4. 📊 Incrementar métrica de errores (Prometheus/DataDog)
     * 5. 🔄 Opcionalmente: reintentar con lógica custom
     *
     * @param event   el evento que falló
     * @param record  registro completo con headers y metadata
     * @param partition partición del DLT
     * @param offset  offset del mensaje en DLT
     * @param key     clave del mensaje (userId)
     */
    @KafkaListener(
            topics = "user.created.dlt",
            groupId = "notifications-service-dlt",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeFailedMessage(
            @Payload UserCreatedEvent event,
            ConsumerRecord<String, UserCreatedEvent> record,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        logger.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        logger.error("💀 [DLT CONSUMER] Failed message received in Dead Letter Topic");
        logger.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        logger.error("    Topic: user.created.dlt");
        logger.error("    Partition: {} | Offset: {} | Key: {}", partition, offset, key);
        logger.error("    Event: userId={}, username={}, email={}",
                event.userId(), event.username(), event.email());

        // Extraer headers con información del error
        String originalTopic = getHeaderValue(record, "kafka_dlt-original-topic");
        String exceptionMessage = getHeaderValue(record, "kafka_dlt-exception-message");
        String exceptionStacktrace = getHeaderValue(record, "kafka_dlt-exception-stacktrace");

        logger.error("    Original Topic: {}", originalTopic);
        logger.error("    Exception: {}", exceptionMessage);

        if (exceptionStacktrace != null && !exceptionStacktrace.isEmpty()) {
            logger.error("    Stack Trace (first 500 chars):");
            logger.error("    {}", exceptionStacktrace.substring(0, Math.min(500, exceptionStacktrace.length())));
        }

        logger.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // ═══════════════════════════════════════════════════════════════
        // ACCIONES RECOMENDADAS PARA PRODUCCIÓN
        // ═══════════════════════════════════════════════════════════════

        try {
            // 1. GUARDAR EN BASE DE DATOS (recomendado para producción)
            // failedMessageRepository.save(new FailedMessage(
            //     originalTopic,
            //     event.userId().toString(),
            //     objectMapper.writeValueAsString(event),
            //     exceptionMessage,
            //     exceptionStacktrace,
            //     Instant.now()
            // ));

            // 2. ENVIAR ALERTA (Slack, PagerDuty, etc.)
            // if (shouldAlert(event)) {
            //     alertService.sendAlert(
            //         "DLT Alert",
            //         "Failed to process UserCreatedEvent for user: " + event.username(),
            //         AlertSeverity.HIGH
            //     );
            // }

            // 3. INCREMENTAR MÉTRICA DE ERRORES (Prometheus, DataDog)
            // meterRegistry.counter("kafka.dlt.messages",
            //     "topic", originalTopic,
            //     "error_type", getErrorType(exceptionMessage)
            // ).increment();

            // 4. REINTENTAR CON LÓGICA CUSTOM (si aplica)
            // if (isRetryable(exceptionMessage)) {
            //     logger.info("Scheduling retry for event: {}", event.userId());
            //     retryScheduler.scheduleRetry(event, Duration.ofMinutes(5));
            // }

            logger.info("✅ [DLT CONSUMER] Failed message processed and logged");

        } catch (Exception e) {
            // Error procesando mensaje del DLT
            // ⚠️ Importante: NO lanzar excepción aquí
            // Si falla, el mensaje iría a... ¿otro DLT del DLT? 😅
            logger.error("❌ [DLT CONSUMER] Error processing DLT message: {}", e.getMessage(), e);
        }
    }

    /**
     * Extrae valor de un header del registro de Kafka
     *
     * @param record registro de Kafka
     * @param headerName nombre del header
     * @return valor del header como String, o null si no existe
     */
    private String getHeaderValue(ConsumerRecord<String, UserCreatedEvent> record, String headerName) {
        org.apache.kafka.common.header.Header header = record.headers().lastHeader(headerName);
        if (header != null) {
            return new String(header.value());
        }
        return null;
    }

    /**
     * EJEMPLO: Endpoint para reprocesar mensajes del DLT
     *
     * En un sistema real, podrías crear un endpoint admin para reprocesar mensajes:
     *
     * {@code
     * @RestController
     * @RequestMapping("/admin/dlt")
     * public class DLTAdminController {
     *
     *     @PostMapping("/retry")
     *     public ResponseEntity<String> retryDLTMessages(
     *             @RequestParam String topic,
     *             @RequestParam(required = false) Integer maxMessages) {
     *
     *         // 1. Consumir mensajes del DLT
     *         List<UserCreatedEvent> failedMessages = dltService.getFailedMessages(topic, maxMessages);
     *
     *         // 2. Republicar al topic original
     *         failedMessages.forEach(event -> {
     *             kafkaTemplate.send("user.created", event.userId().toString(), event);
     *         });
     *
     *         return ResponseEntity.ok("Retried " + failedMessages.size() + " messages");
     *     }
     * }
     * }
     */

    /**
     * EJEMPLO: Dashboard de monitoreo de DLT
     *
     * Podrías crear un endpoint para ver estado del DLT:
     *
     * {@code
     * @GetMapping("/admin/dlt/stats")
     * public DLTStats getDLTStats() {
     *     return new DLTStats(
     *         failedMessageRepository.count(),              // Total mensajes en DLT
     *         failedMessageRepository.countToday(),         // Mensajes hoy
     *         failedMessageRepository.findTopErrors(10)     // Top 10 errores
     *     );
     * }
     * }
     */

    /**
     * COMANDOS ÚTILES DE KAFKA PARA DLT:
     *
     * Ver mensajes del DLT:
     * {@code
     * kafka-console-consumer.sh \
     *   --bootstrap-server localhost:9092 \
     *   --topic user.created.dlt \
     *   --from-beginning
     * }
     *
     * Contar mensajes en DLT:
     * {@code
     * kafka-run-class.sh kafka.tools.GetOffsetShell \
     *   --broker-list localhost:9092 \
     *   --topic user.created.dlt
     * }
     *
     * Eliminar topic DLT (CUIDADO - borra todos los mensajes):
     * {@code
     * kafka-topics.sh \
     *   --bootstrap-server localhost:9092 \
     *   --delete \
     *   --topic user.created.dlt
     * }
     */
}
