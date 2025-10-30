package com.example.hexarch.kafka;

import com.example.hexarch.user.domain.event.UserCreatedEvent;
import com.example.hexarch.user.infrastructure.adapter.output.event.KafkaUserEventPublisherAdapter;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * INTEGRATION TEST - Kafka Publisher (Producer)
 *
 * Test de integración que prueba el Publisher de forma aislada.
 *
 * ESCENARIO REALISTA:
 * Este test simula que estamos en el "User Service" y queremos testear
 * que nuestro Publisher funciona correctamente SIN depender del Consumer
 * (que estaría en otro microservicio "Notifications Service").
 *
 * BEST PRACTICE - MICROSERVICES:
 * - ✅ Usa el Publisher REAL de la aplicación (lo que queremos testear)
 * - ✅ Usa un TEST CONSUMER para verificar que el mensaje llegó a Kafka
 * - ❌ NO usa el Consumer real de la aplicación (estaría en otro microservicio)
 *
 * QUÉ SE TESTEA:
 * - El Publisher publica correctamente al topic
 * - El topic y key son correctos
 * - El payload está completo y correcto
 * - Múltiples eventos mantienen el orden (por key)
 * - Keys diferentes para usuarios diferentes
 *
 * NOTA: En un entorno real de microservicios:
 * - User Service (este) → tiene el Publisher → publica eventos
 * - Notifications Service (otro) → tiene el Consumer → consume eventos
 * - NO están en el mismo proyecto/microservicio
 */
@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"user.created", "user.created.dlt"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9093",
                "port=9093"
        }
)
@Testcontainers
@DisplayName("Kafka Publisher Integration Tests - Producer Only")
class KafkaPublisherIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Kafka - usar el broker embebido
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9093");
    }

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    // ✅ Publisher REAL - Lo que queremos testear
    @Autowired
    private KafkaUserEventPublisherAdapter publisher;

    // ✅ Test Consumer - Para verificar que el mensaje llegó a Kafka
    // NO es el Consumer de la aplicación (ese estaría en otro microservicio)
    private Consumer<String, UserCreatedEvent> testConsumer;

    @BeforeEach
    void setUp() {
        // Configurar un TEST CONSUMER para verificar mensajes en Kafka
        // Este consumer simula "leer directamente de Kafka" sin usar el Consumer de la aplicación
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "test-publisher-group",
                "true",
                embeddedKafkaBroker
        );
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.hexarch.user.domain.event");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, UserCreatedEvent.class.getName());

        DefaultKafkaConsumerFactory<String, UserCreatedEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps);

        testConsumer = consumerFactory.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(testConsumer, "user.created");
    }

    @AfterEach
    void tearDown() {
        if (testConsumer != null) {
            testConsumer.close();
        }
    }

    /**
     * TEST CASE 1: Debe publicar evento a Kafka con topic y key correctos
     *
     * GIVEN: Un evento UserCreatedEvent
     * WHEN: El publisher publica el evento
     * THEN:
     *   - El evento se almacena en Kafka
     *   - Topic = "user.created"
     *   - Key = userId (para garantizar orden)
     *   - Payload completo y correcto
     */
    @Test
    @DisplayName("Debe publicar evento a Kafka con topic y key correctos")
    void shouldPublishEventToKafkaSuccessfully() {
        // GIVEN - Crear evento
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(
                userId,
                "publisher-test-user",
                "publisher@test.com",
                Instant.now()
        );

        // WHEN - Publicar evento usando el Publisher REAL
        publisher.publish(event);

        // THEN - Verificar que llegó a Kafka usando el TEST CONSUMER
        ConsumerRecords<String, UserCreatedEvent> records =
                KafkaTestUtils.getRecords(testConsumer, Duration.ofSeconds(10));

        assertThat(records.count()).isGreaterThan(0);

        ConsumerRecord<String, UserCreatedEvent> record = records.iterator().next();

        // Verificar topic
        assertThat(record.topic()).isEqualTo("user.created");

        // Verificar key (debe ser userId para garantizar orden)
        assertThat(record.key()).isEqualTo(userId.toString());

        // Verificar payload
        UserCreatedEvent receivedEvent = record.value();
        assertThat(receivedEvent.userId()).isEqualTo(userId);
        assertThat(receivedEvent.username()).isEqualTo("publisher-test-user");
        assertThat(receivedEvent.email()).isEqualTo("publisher@test.com");
        assertThat(receivedEvent.occurredAt()).isNotNull();
    }

    /**
     * TEST CASE 2: Múltiples eventos del mismo usuario deben usar la misma key
     *
     * GIVEN: Múltiples eventos del mismo usuario (misma key)
     * WHEN: Se publican todos los eventos
     * THEN: Todos deben tener el mismo userId como key (garantiza orden)
     *
     * NOTA: Kafka garantiza orden dentro de una partición para mensajes con la misma key
     */
    @Test
    @DisplayName("Debe usar userId como key para garantizar orden de eventos del mismo usuario")
    void shouldUseUserIdAsKeyForOrdering() {
        // GIVEN - Crear múltiples eventos del mismo usuario
        UUID sameUserId = UUID.randomUUID();

        UserCreatedEvent event1 = new UserCreatedEvent(
                sameUserId,
                "user-v1",
                "user@test.com",
                Instant.now()
        );

        UserCreatedEvent event2 = new UserCreatedEvent(
                sameUserId,
                "user-v2",
                "user@test.com",
                Instant.now().plusSeconds(1)
        );

        UserCreatedEvent event3 = new UserCreatedEvent(
                sameUserId,
                "user-v3",
                "user@test.com",
                Instant.now().plusSeconds(2)
        );

        // WHEN - Publicar eventos
        publisher.publish(event1);
        publisher.publish(event2);
        publisher.publish(event3);

        // THEN - Verificar que todos llegaron con la misma key
        ConsumerRecords<String, UserCreatedEvent> records =
                KafkaTestUtils.getRecords(testConsumer, Duration.ofSeconds(10));

        assertThat(records.count()).isGreaterThanOrEqualTo(3);

        // Verificar que todos tienen la misma key (userId)
        for (ConsumerRecord<String, UserCreatedEvent> record : records) {
            if (record.value().userId().equals(sameUserId)) {
                assertThat(record.key()).isEqualTo(sameUserId.toString());
            }
        }
    }

    /**
     * TEST CASE 3: Eventos de diferentes usuarios deben usar keys diferentes
     *
     * GIVEN: Eventos de diferentes usuarios
     * WHEN: Se publican los eventos
     * THEN: Cada evento debe tener una key diferente (userId diferente)
     */
    @Test
    @DisplayName("Debe usar keys diferentes para usuarios diferentes")
    void shouldUseDifferentKeysForDifferentUsers() {
        // GIVEN - Crear eventos de diferentes usuarios
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        UserCreatedEvent event1 = new UserCreatedEvent(
                userId1,
                "user1",
                "user1@test.com",
                Instant.now()
        );

        UserCreatedEvent event2 = new UserCreatedEvent(
                userId2,
                "user2",
                "user2@test.com",
                Instant.now()
        );

        // WHEN - Publicar eventos
        publisher.publish(event1);
        publisher.publish(event2);

        // THEN - Verificar que llegaron con keys diferentes
        ConsumerRecords<String, UserCreatedEvent> records =
                KafkaTestUtils.getRecords(testConsumer, Duration.ofSeconds(10));

        assertThat(records.count()).isGreaterThanOrEqualTo(2);

        // Verificar que las keys son diferentes
        boolean foundUser1 = false;
        boolean foundUser2 = false;

        for (ConsumerRecord<String, UserCreatedEvent> record : records) {
            if (record.value().userId().equals(userId1)) {
                assertThat(record.key()).isEqualTo(userId1.toString());
                foundUser1 = true;
            }
            if (record.value().userId().equals(userId2)) {
                assertThat(record.key()).isEqualTo(userId2.toString());
                foundUser2 = true;
            }
        }

        assertThat(foundUser1).isTrue();
        assertThat(foundUser2).isTrue();
    }

    /**
     * TEST CASE 4: Debe preservar todos los datos del evento
     *
     * GIVEN: Un evento con todos los datos
     * WHEN: Se publica el evento
     * THEN: Todos los datos deben preservarse (userId, username, email, timestamp)
     */
    @Test
    @DisplayName("Debe preservar todos los datos del evento de dominio")
    void shouldPreserveAllEventData() {
        // GIVEN - Evento con todos los datos
        UUID userId = UUID.randomUUID();
        Instant occurredOn = Instant.parse("2025-10-30T10:00:00Z");

        UserCreatedEvent detailedEvent = new UserCreatedEvent(
                userId,
                "detailed-user",
                "detailed@example.com",
                occurredOn
        );

        // WHEN - Publicar evento
        publisher.publish(detailedEvent);

        // THEN - Verificar que todos los datos están intactos
        ConsumerRecords<String, UserCreatedEvent> records =
                KafkaTestUtils.getRecords(testConsumer, Duration.ofSeconds(10));

        assertThat(records.count()).isGreaterThan(0);

        ConsumerRecord<String, UserCreatedEvent> record = records.iterator().next();
        UserCreatedEvent capturedEvent = record.value();

        assertThat(capturedEvent.userId()).isEqualTo(userId);
        assertThat(capturedEvent.username()).isEqualTo("detailed-user");
        assertThat(capturedEvent.email()).isEqualTo("detailed@example.com");
        assertThat(capturedEvent.occurredAt()).isEqualTo(occurredOn);
    }
}
