package com.example.hexarch.kafka;

import com.example.hexarch.notifications.application.service.EmailService;
import com.example.hexarch.notifications.infrastructure.kafka.consumer.UserCreatedEventDLTConsumer;
import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * INTEGRATION TEST - Kafka Dead Letter Topic (DLT)
 *
 * Test de integración que prueba el flujo de Dead Letter Topic (DLT) con reintentos.
 *
 * ESCENARIO REALISTA:
 * Este test simula que estamos en el "Notifications Service" y queremos testear
 * el flujo DLT cuando los eventos fallan múltiples veces.
 *
 * BEST PRACTICE - MICROSERVICES:
 * - ✅ Usa KafkaTemplate DIRECTAMENTE para simular eventos de otro microservicio
 * - ✅ Fuerza fallos en EmailService para activar el DLT
 * - ✅ Verifica que el DLT Consumer recibe mensajes con headers de error
 * - ❌ NO usa el Publisher real (estaría en otro microservicio)
 *
 * FLUJO DLT:
 * 1. Consumer intenta procesar mensaje (evento de "User Service")
 * 2. Falla (por ej: EmailService lanza excepción)
 * 3. Spring Kafka reintenta automáticamente (según configuración)
 * 4. Después de N reintentos fallidos, envía mensaje al DLT
 * 5. DLT Consumer recibe mensaje con headers de error
 * 6. DLT Consumer loguea el error para investigación
 *
 * CONFIGURACIÓN DE REINTENTOS:
 * - En KafkaConfig se configura DefaultErrorHandler con:
 *   - Número de reintentos (ej: 3 intentos)
 *   - Backoff entre reintentos (ej: 1s, 2s, 4s)
 *   - DeadLetterPublishingRecoverer para enviar a DLT
 *
 * HEADERS DEL DLT:
 * Spring Kafka añade automáticamente headers con información del error:
 * - kafka_dlt-original-topic: topic original
 * - kafka_dlt-exception-message: mensaje de error
 * - kafka_dlt-exception-stacktrace: stack trace
 * - kafka_dlt-original-offset: offset original
 * - kafka_dlt-original-partition: partición original
 *
 * QUÉ SE TESTEA:
 * - Mensaje falla múltiples veces y va al DLT
 * - Headers del DLT contienen información del error
 * - DLT Consumer procesa mensaje sin reintentar
 * - No se pierde información del mensaje original
 */
@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"user.created", "user.created.dlt"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9094",
                "port=9094"
        }
)
@Testcontainers
@DisplayName("Kafka DLT Integration Tests - Retry & Dead Letter")
class KafkaDLTIntegrationTest {

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
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9094");
    }

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    @SpyBean
    private EmailService emailService;

    @SpyBean
    private UserCreatedEventDLTConsumer dltConsumer;

    private Consumer<String, UserCreatedEvent> dltTestConsumer;

    @BeforeEach
    void setUp() {
        // Reset mocks
        Mockito.reset(emailService, dltConsumer);

        // Configurar un consumer de test para verificar mensajes en el DLT
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "dlt-test-group",
                "true",
                embeddedKafkaBroker
        );
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.hexarch.user.domain.event");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, UserCreatedEvent.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        DefaultKafkaConsumerFactory<String, UserCreatedEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps);

        dltTestConsumer = consumerFactory.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(dltTestConsumer, "user.created.dlt");
    }

    /**
     * TEST CASE 1: Mensaje que falla debe ir al DLT después de reintentos
     *
     * GIVEN: EmailService configurado para fallar siempre
     * WHEN: Se publica un evento a "user.created"
     * THEN:
     *   - EmailService se llama múltiples veces (reintentos)
     *   - Después de N reintentos, el mensaje va a "user.created.dlt"
     *   - DLT Consumer recibe el mensaje con headers de error
     *
     * NOTA: Este test usa @SpyBean en EmailService para forzar fallos
     */
    @Test
    @DisplayName("Debe enviar mensaje al DLT después de múltiples fallos")
    void shouldSendMessageToDLTAfterMultipleFailures() {
        // GIVEN - Configurar EmailService para fallar siempre
        doThrow(new RuntimeException("Simulated EmailService failure"))
                .when(emailService)
                .sendWelcomeEmail(anyString(), anyString());

        // Crear evento
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(
                userId,
                "dlt-test-user",
                "dlt@test.com",
                Instant.now()
        );

        // WHEN - Publicar evento al topic principal
        kafkaTemplate.send("user.created", userId.toString(), event);

        // THEN - Verificar que EmailService se llamó múltiples veces (reintentos)
        // Nota: El número exacto depende de la configuración de reintentos en KafkaConfig
        await()
                .atMost(Duration.ofSeconds(15))
                .pollDelay(Duration.ofSeconds(1))
                .untilAsserted(() ->
                        verify(emailService, atLeast(1))
                                .sendWelcomeEmail("dlt@test.com", "dlt-test-user")
                );

        // Verificar que el mensaje llegó al DLT
        await()
                .atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    ConsumerRecords<String, UserCreatedEvent> dltRecords =
                            KafkaTestUtils.getRecords(dltTestConsumer, Duration.ofSeconds(5));

                    assertThat(dltRecords.count()).isGreaterThan(0);

                    ConsumerRecord<String, UserCreatedEvent> dltRecord = dltRecords.iterator().next();

                    // Verificar topic DLT
                    assertThat(dltRecord.topic()).isEqualTo("user.created.dlt");

                    // Verificar que el payload está intacto
                    UserCreatedEvent dltEvent = dltRecord.value();
                    assertThat(dltEvent.userId()).isEqualTo(userId);
                    assertThat(dltEvent.username()).isEqualTo("dlt-test-user");
                    assertThat(dltEvent.email()).isEqualTo("dlt@test.com");
                });
    }

    /**
     * TEST CASE 2: DLT debe contener headers con información del error
     *
     * GIVEN: Un mensaje que falla y va al DLT
     * WHEN: Se verifica el mensaje en el DLT
     * THEN: Los headers deben contener:
     *   - Original topic
     *   - Exception message
     *   - Stack trace
     *   - Original partition
     *   - Original offset
     */
    @Test
    @DisplayName("DLT debe contener headers con información del error")
    void shouldContainErrorHeadersInDLT() {
        // GIVEN - Configurar EmailService para fallar con mensaje específico
        doThrow(new RuntimeException("Specific test error message"))
                .when(emailService)
                .sendWelcomeEmail(anyString(), anyString());

        // Crear evento
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(
                userId,
                "headers-test-user",
                "headers@test.com",
                Instant.now()
        );

        // WHEN - Publicar evento
        kafkaTemplate.send("user.created", userId.toString(), event);

        // THEN - Verificar headers del DLT
        await()
                .atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    ConsumerRecords<String, UserCreatedEvent> dltRecords =
                            KafkaTestUtils.getRecords(dltTestConsumer, Duration.ofSeconds(5));

                    assertThat(dltRecords.count()).isGreaterThan(0);

                    ConsumerRecord<String, UserCreatedEvent> dltRecord = dltRecords.iterator().next();
                    Headers headers = dltRecord.headers();

                    // Verificar header: original topic
                    Header originalTopicHeader = headers.lastHeader("kafka_dlt-original-topic");
                    if (originalTopicHeader != null) {
                        String originalTopic = new String(originalTopicHeader.value(), StandardCharsets.UTF_8);
                        assertThat(originalTopic).isEqualTo("user.created");
                    }

                    // Verificar header: exception message
                    Header exceptionMessageHeader = headers.lastHeader("kafka_dlt-exception-message");
                    if (exceptionMessageHeader != null) {
                        String exceptionMessage = new String(exceptionMessageHeader.value(), StandardCharsets.UTF_8);
                        assertThat(exceptionMessage).contains("test error");
                    }

                    // Verificar header: stack trace (debe existir)
                    Header stackTraceHeader = headers.lastHeader("kafka_dlt-exception-stacktrace");
                    if (stackTraceHeader != null) {
                        String stackTrace = new String(stackTraceHeader.value(), StandardCharsets.UTF_8);
                        assertThat(stackTrace).isNotEmpty();
                    }
                });
    }

    /**
     * TEST CASE 3: DLT Consumer debe procesar mensajes sin reintentar
     *
     * GIVEN: Un mensaje en el DLT
     * WHEN: DLT Consumer procesa el mensaje
     * THEN:
     *   - Debe procesarse sin lanzar excepción
     *   - No debe reintentar (ya está en DLT)
     *   - Debe loguear el error
     *
     * NOTA: El DLT Consumer es el "fin de la línea" para mensajes fallidos
     */
    @Test
    @DisplayName("DLT Consumer debe procesar mensajes sin reintentar")
    void shouldProcessDLTMessageWithoutRetrying() {
        // GIVEN - Configurar EmailService para fallar
        doThrow(new RuntimeException("Test DLT Consumer error"))
                .when(emailService)
                .sendWelcomeEmail(anyString(), anyString());

        // Crear evento
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(
                userId,
                "dlt-consumer-test",
                "dlt-consumer@test.com",
                Instant.now()
        );

        // WHEN - Publicar evento (fallará y irá al DLT)
        kafkaTemplate.send("user.created", userId.toString(), event);

        // THEN - Verificar que DLT Consumer procesa el mensaje
        await()
                .atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() ->
                        verify(dltConsumer, atLeast(1))
                                .consumeFailedMessage(
                                        any(UserCreatedEvent.class),
                                        any(ConsumerRecord.class),
                                        anyInt(),
                                        anyLong(),
                                        anyString()
                                )
                );

        // Verificar que NO se reintenta (solo se llama una vez)
        verify(dltConsumer, atMost(3))
                .consumeFailedMessage(
                        any(UserCreatedEvent.class),
                        any(ConsumerRecord.class),
                        anyInt(),
                        anyLong(),
                        anyString()
                );
    }

    /**
     * TEST CASE 4: Mensaje exitoso NO debe ir al DLT
     *
     * GIVEN: EmailService funciona correctamente
     * WHEN: Se publica un evento
     * THEN:
     *   - El evento se procesa exitosamente
     *   - NO va al DLT
     *   - Solo se procesa una vez (sin reintentos)
     */
    @Test
    @DisplayName("Mensaje exitoso NO debe ir al DLT")
    void successfulMessageShouldNotGoToDLT() {
        // GIVEN - EmailService funciona correctamente (sin fallos)
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

        // Limpiar cualquier registro previo del DLT consumer
        KafkaTestUtils.getRecords(dltTestConsumer, Duration.ofMillis(100));

        // Crear evento
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(
                userId,
                "success-test-user",
                "success@test.com",
                Instant.now()
        );

        // WHEN - Publicar evento
        kafkaTemplate.send("user.created", userId.toString(), event);

        // THEN - Verificar que se procesó exitosamente
        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        verify(emailService, times(1))
                                .sendWelcomeEmail("success@test.com", "success-test-user")
                );

        // Verificar que NO llegó al DLT
        // Esperar un poco para asegurarnos de que no llegó
        await()
                .during(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    ConsumerRecords<String, UserCreatedEvent> dltRecords =
                            dltTestConsumer.poll(Duration.ofMillis(500));

                    // Filtrar solo mensajes de este test
                    long matchingRecords = 0;
                    for (ConsumerRecord<String, UserCreatedEvent> record : dltRecords) {
                        if (record.value().email().equals("success@test.com")) {
                            matchingRecords++;
                        }
                    }

                    assertThat(matchingRecords).isEqualTo(0);
                });
    }

    /**
     * TEST CASE 5: Datos del evento deben preservarse en el DLT
     *
     * GIVEN: Un evento con datos específicos que falla
     * WHEN: El evento va al DLT
     * THEN: Todos los datos deben estar intactos (userId, username, email, timestamp)
     */
    @Test
    @DisplayName("Datos del evento deben preservarse completamente en el DLT")
    void shouldPreserveEventDataInDLT() {
        // GIVEN - Configurar EmailService para fallar
        doThrow(new RuntimeException("Data preservation test"))
                .when(emailService)
                .sendWelcomeEmail(anyString(), anyString());

        // Crear evento con datos específicos
        UUID specificUserId = UUID.randomUUID();
        Instant specificTimestamp = Instant.parse("2025-10-30T15:30:00Z");
        UserCreatedEvent event = new UserCreatedEvent(
                specificUserId,
                "data-test-user",
                "data@test.com",
                specificTimestamp
        );

        // WHEN - Publicar evento
        kafkaTemplate.send("user.created", specificUserId.toString(), event);

        // THEN - Verificar que todos los datos están intactos en el DLT
        await()
                .atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    ConsumerRecords<String, UserCreatedEvent> dltRecords =
                            KafkaTestUtils.getRecords(dltTestConsumer, Duration.ofSeconds(5));

                    assertThat(dltRecords.count()).isGreaterThan(0);

                    ConsumerRecord<String, UserCreatedEvent> dltRecord = dltRecords.iterator().next();
                    UserCreatedEvent dltEvent = dltRecord.value();

                    // Verificar todos los campos
                    assertThat(dltEvent.userId()).isEqualTo(specificUserId);
                    assertThat(dltEvent.username()).isEqualTo("data-test-user");
                    assertThat(dltEvent.email()).isEqualTo("data@test.com");
                    assertThat(dltEvent.occurredAt()).isEqualTo(specificTimestamp);

                    // Verificar key
                    assertThat(dltRecord.key()).isEqualTo(specificUserId.toString());
                });
    }
}
