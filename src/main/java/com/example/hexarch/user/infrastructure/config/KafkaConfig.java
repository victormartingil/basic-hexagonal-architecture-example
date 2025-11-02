package com.example.hexarch.user.infrastructure.config;

import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * INFRASTRUCTURE LAYER - Kafka Configuration
 *
 * Configuración de Kafka para integration events entre microservicios.
 *
 * ¿QUÉ CONFIGURA ESTA CLASE?
 * 1. Producer: Para publicar eventos a Kafka (KafkaTemplate)
 * 2. Consumer: Para consumir eventos de Kafka (@KafkaListener)
 * 3. Serialización: Convierte objetos Java <-> JSON automáticamente
 *
 * PRODUCER vs CONSUMER:
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │ PRODUCER (Publisher)                                        │
 * ├─────────────────────────────────────────────────────────────┤
 * │ • Publica eventos a Kafka                                   │
 * │ • Ejemplo: KafkaUserEventPublisherAdapter                   │
 * │ • Usa: KafkaTemplate<String, UserCreatedEvent>              │
 * │ • Serializa: Object → JSON → Kafka                          │
 * └─────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │ CONSUMER (Listener)                                         │
 * ├─────────────────────────────────────────────────────────────┤
 * │ • Consume eventos de Kafka                                  │
 * │ • Ejemplo: UserEventsKafkaConsumer                          │
 * │ • Usa: @KafkaListener                                       │
 * │ • Deserializa: Kafka → JSON → Object                        │
 * └─────────────────────────────────────────────────────────────┘
 *
 * CONFIGURACIÓN IMPORTANTE:
 *
 * 1. **bootstrap-servers**: Dirección del broker Kafka (localhost:9092)
 * 2. **group-id**: Identificador del grupo de consumers
 *    - Consumers con mismo group-id comparten particiones
 *    - Cada mensaje lo procesa SOLO UN consumer del grupo
 * 3. **key-serializer**: Serializa la clave del mensaje (String)
 * 4. **value-serializer**: Serializa el valor (UserCreatedEvent → JSON)
 *
 * SERIALIZACIÓN JSON:
 * - JsonSerializer: Convierte objeto Java → JSON (producer)
 * - JsonDeserializer: Convierte JSON → objeto Java (consumer)
 * - spring.json.trusted.packages=*: Permite deserializar cualquier paquete
 *   (⚠️ En producción, especifica paquetes exactos por seguridad)
 *
 * EJEMPLO DE USO:
 *
 * Producer:
 * {@code
 * @Component
 * public class KafkaUserEventPublisherAdapter {
 *     private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;
 *
 *     public void publish(UserCreatedEvent event) {
 *         kafkaTemplate.send("user.created", event.userId().toString(), event);
 *     }
 * }
 * }
 *
 * Consumer:
 * {@code
 * @Component
 * public class UserEventsKafkaConsumer {
 *     @KafkaListener(topics = "user.created", groupId = "notifications-service")
 *     public void consume(UserCreatedEvent event) {
 *         // Procesar evento
 *     }
 * }
 * }
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Configuración del Producer (para publicar eventos)
     *
     * Define cómo se conecta al broker y cómo serializa los mensajes.
     *
     * CONFIGURACIONES CLAVE:
     * - BOOTSTRAP_SERVERS_CONFIG: Dirección del broker Kafka
     * - KEY_SERIALIZER_CLASS_CONFIG: Serializa la clave (userId) como String
     * - VALUE_SERIALIZER_CLASS_CONFIG: Serializa el evento como JSON
     *
     * @return ProducerFactory configurado para String keys y UserCreatedEvent values
     */
    @Bean
    public ProducerFactory<String, UserCreatedEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * KafkaTemplate: Bean principal para publicar eventos
     *
     * Este bean es inyectado en KafkaUserEventPublisherAdapter para publicar eventos.
     *
     * EJEMPLO:
     * kafkaTemplate.send("user.created", userId, event);
     *                     ↑ topic       ↑ key  ↑ value
     *
     * @param producerFactory factory configurado arriba
     * @return KafkaTemplate listo para usar
     */
    @Bean
    public KafkaTemplate<String, UserCreatedEvent> kafkaTemplate(
            ProducerFactory<String, UserCreatedEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * Configuración del Consumer (para consumir eventos)
     *
     * Define cómo se conecta al broker y cómo deserializa los mensajes.
     *
     * CONFIGURACIONES CLAVE:
     * - BOOTSTRAP_SERVERS_CONFIG: Dirección del broker Kafka
     * - GROUP_ID_CONFIG: Grupo de consumers (consumers con mismo ID comparten carga)
     * - KEY_DESERIALIZER_CLASS_CONFIG: Deserializa la clave como String
     * - VALUE_DESERIALIZER_CLASS_CONFIG: Deserializa el JSON como UserCreatedEvent
     * - JsonDeserializer.TRUSTED_PACKAGES: Paquetes permitidos para deserializar
     *   (usa "*" para desarrollo, especifica paquetes exactos en producción)
     *
     * AUTO_OFFSET_RESET_CONFIG:
     * - "earliest": Lee desde el principio si no hay offset guardado
     * - "latest": Lee solo mensajes nuevos
     *
     * @return ConsumerFactory configurado para String keys y UserCreatedEvent values
     */
    @Bean
    public ConsumerFactory<String, UserCreatedEvent> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "hexarch-user-service");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Configuración del deserializador JSON
        // ⚠️ En producción, reemplaza "*" con paquetes específicos:
        // config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.hexarch.user.domain.event");
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new JsonDeserializer<>(UserCreatedEvent.class, false)
        );
    }

    /**
     * Container Factory para @KafkaListener
     *
     * Esta factory es usada por @KafkaListener para crear containers que escuchan topics.
     *
     * CONCURRENCY:
     * - setConcurrency(3): Crea 3 threads para consumir mensajes en paralelo
     * - Máximo: número de particiones del topic (en nuestro caso, 3)
     * - Si tienes 1 partición, solo 1 thread consumirá (los demás estarán idle)
     *
     * DEAD LETTER TOPIC (DLT):
     * - Configurado con DefaultErrorHandler
     * - Mensajes que fallan después de 3 reintentos → van a topic DLT
     * - Topic DLT = topic original + ".dlt" (ej: "user.created.dlt")
     * - Evita loops infinitos con mensajes problemáticos
     *
     * @param consumerFactory factory configurado arriba
     * @param kafkaTemplate template para publicar a DLT
     * @return ConcurrentKafkaListenerContainerFactory para @KafkaListener
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, UserCreatedEvent> consumerFactory,
            KafkaTemplate<String, UserCreatedEvent> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        // Concurrency = Número de threads consumiendo en paralelo
        // Debe ser <= número de particiones del topic
        factory.setConcurrency(3);

        // ═══════════════════════════════════════════════════════════════
        // DEAD LETTER TOPIC (DLT) - Manejo automático de errores
        // ═══════════════════════════════════════════════════════════════

        // DeadLetterPublishingRecoverer: Publica mensajes fallidos a topic DLT
        // - Topic DLT = topic original + ".dlt"
        // - Ejemplo: "user.created" → "user.created.dlt"
        //
        // Configuración explícita del topic de destino usando BiFunction:
        // - BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> destinationResolver
        // - Retorna el TopicPartition de destino para el DLT
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (consumerRecord, exception) -> {
                    // Topic de destino = topic original + ".dlt"
                    String dltTopic = consumerRecord.topic() + ".dlt";
                    // Mantener la misma partición (o usar partición 0 para todos)
                    return new org.apache.kafka.common.TopicPartition(dltTopic, 0);
                });

        // DefaultErrorHandler: Maneja errores con reintentos automáticos
        // - FixedBackOff(1000L, 3L): 3 reintentos con 1 segundo entre cada uno
        // - Después de 3 fallos → envía a DLT usando recoverer
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(1000L, 3L)  // 3 reintentos, 1 segundo entre cada uno
        );

        // Configurar error handler en la factory
        factory.setCommonErrorHandler(errorHandler);

        // FLUJO CON ERRORES:
        // 1. Consumer falla al procesar mensaje
        // 2. Espera 1 segundo, reintenta (intento 1/3)
        // 3. Falla nuevamente, espera 1 segundo, reintenta (intento 2/3)
        // 4. Falla nuevamente, espera 1 segundo, reintenta (intento 3/3)
        // 5. Después de 3 fallos → publica a "user.created.dlt"
        // 6. Consumer continúa con el siguiente mensaje ✅

        return factory;
    }
}
