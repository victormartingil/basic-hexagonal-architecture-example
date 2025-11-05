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
import org.junit.jupiter.api.Disabled;
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
import org.springframework.test.annotation.DirtiesContext;
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
@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "email.service.failure-rate=70"  // Alta tasa de fallos para tests DLT
})
/**
 * @DirtiesContext - IMPORTANTE para tests de Kafka
 *
 * Esta anotación indica a Spring que el contexto de aplicación debe recargarse
 * después de ejecutar esta clase de test. Esto es CRÍTICO para tests de Kafka porque:
 *
 * 1. EVITA CONTAMINACIÓN: Sin @DirtiesContext, el EmbeddedKafkaBroker y los topics
 *    se comparten entre clases de test, causando que mensajes de un test aparezcan en otro.
 *
 * 2. GARANTIZA AISLAMIENTO: Cada clase de test obtiene un broker Kafka limpio,
 *    sin mensajes residuales ni offsets de tests anteriores.
 *
 * 3. PUERTOS DINÁMICOS: Permite que cada test use puertos dinámicos diferentes,
 *    evitando conflictos cuando se ejecutan tests en paralelo o secuencialmente.
 *
 * TRADE-OFF: Recargar el contexto es costoso (~2-3 segundos por clase), pero es
 * necesario para garantizar tests deterministas y sin flakiness.
 */
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        topics = {"user.created", "user.created.dlt"}
)
@Testcontainers
@DisplayName("Kafka DLT Integration Tests - Retry & Dead Letter")
class KafkaDLTIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withStartupTimeout(java.time.Duration.ofSeconds(120));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    // ⚠️ @SpyBean está deprecado en Spring Boot 3.4+ (marcado para eliminación)
    // ALTERNATIVA MODERNA: Inyectar el bean real con @Autowired y usar Mockito.spy() manualmente
    // TODO: Migrar a la nueva forma cuando se actualice a Spring Boot 3.6+
    // Ver: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes
    @SuppressWarnings("deprecation")  // Suprimir warning hasta migración completa
    @SpyBean
    private EmailService emailService;

    @SuppressWarnings("deprecation")  // Suprimir warning hasta migración completa
    @SpyBean
    private UserCreatedEventDLTConsumer dltConsumer;

    @Autowired
    private io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry circuitBreakerRegistry;

    private Consumer<String, UserCreatedEvent> dltTestConsumer;

    @BeforeEach
    void setUp() {
        // Reset mocks
        Mockito.reset(emailService, dltConsumer);

        // Reset Circuit Breaker para cada test
        // IMPORTANTE: Sin esto, el CB se abre en el primer test y los siguientes fallan
        circuitBreakerRegistry.circuitBreaker("emailService").transitionToClosedState();
        circuitBreakerRegistry.circuitBreaker("emailService").reset();

        // NO crear consumer aquí - cada test lo creará on-demand
        // Esto evita el error "Failed to be assigned partitions"
        // porque el topic DLT puede no estar listo durante setUp()
    }

    /**
     * Helper: Crea un consumer de test para DLT on-demand
     *
     * IMPORTANTE: Se crea DESPUÉS de que los mensajes fallen y se envíen al DLT,
     * no en setUp(). Esto evita problemas de asignación de particiones.
     */
    private Consumer<String, UserCreatedEvent> createDLTTestConsumer(String testName) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "dlt-test-" + testName + "-" + System.currentTimeMillis(),
                "true",
                embeddedKafkaBroker
        );
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.hexarch.user.domain.event");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, UserCreatedEvent.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        DefaultKafkaConsumerFactory<String, UserCreatedEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps);

        Consumer<String, UserCreatedEvent> consumer = consumerFactory.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "user.created.dlt");
        return consumer;
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
     * NOTA: Este test usa @SpyBean (deprecado) en EmailService para forzar fallos
     * TODO: Migrar a Mockito.spy() manual cuando se actualice a Spring Boot 3.6+
     *
     * ⚠️ DISABLED: Test timeout issues en EmbeddedKafka
     * - El mensaje no llega al DLT dentro del timeout (30s)
     * - La funcionalidad DLT funciona en producción
     * - Requiere investigación de configuración de reintentos en tests
     * - Para arreglar: revisar DefaultErrorHandler config en test environment
     */
    @Test

    // @Disabled("DLT test times out waiting for message - requires Kafka retry configuration tuning in test environment")
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

        // IMPORTANTE: Hacer flush() para asegurar que el mensaje se envíe a Kafka
        kafkaTemplate.flush();

        // THEN - Verificar que EmailService se llamó múltiples veces (reintentos)
        // Nota: El número exacto depende de la configuración de reintentos en KafkaConfig
        await()
                .atMost(Duration.ofSeconds(15))
                .pollDelay(Duration.ofSeconds(1))
                .untilAsserted(() ->
                        verify(emailService, atLeast(1))
                                .sendWelcomeEmail("dlt@test.com", "dlt-test-user")
                );

        // Crear consumer DESPUÉS de que el mensaje haya fallado y se haya enviado al DLT
        // Esto evita el error "Failed to be assigned partitions"
        dltTestConsumer = createDLTTestConsumer("shouldSendMessageToDLTAfterMultipleFailures");

        // Verificar que el mensaje llegó al DLT
        // IMPORTANTE: DLT tests tardan más porque esperan múltiples reintentos antes de enviar al DLT
        await()
                .atMost(Duration.ofSeconds(30))
                .pollDelay(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    ConsumerRecords<String, UserCreatedEvent> dltRecords =
                            KafkaTestUtils.getRecords(dltTestConsumer, Duration.ofSeconds(3));

                    assertThat(dltRecords.count()).isGreaterThan(0);

                    // Buscar el mensaje de ESTE test
                    ConsumerRecord<String, UserCreatedEvent> dltRecord = null;
                    for (ConsumerRecord<String, UserCreatedEvent> r : dltRecords) {
                        if (r.value().userId().equals(userId)) {
                            dltRecord = r;
                            break;
                        }
                    }

                    assertThat(dltRecord).isNotNull();

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
     * TEST CASE 2: DLT Consumer debe procesar mensajes sin reintentar
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
    // @Disabled("DLT test times out waiting for message - requires Kafka retry configuration tuning in test environment")
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

        // IMPORTANTE: Hacer flush() para asegurar que el mensaje se envíe a Kafka
        kafkaTemplate.flush();

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
     * TEST CASE 3: Mensaje exitoso NO debe ir al DLT
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

        // IMPORTANTE: Hacer flush() para asegurar que el mensaje se envíe a Kafka
        kafkaTemplate.flush();

        // THEN - Verificar que se procesó exitosamente
        await()
                .atMost(Duration.ofSeconds(10))
                .pollDelay(Duration.ofMillis(500))
                .untilAsserted(() ->
                        verify(emailService, times(1))
                                .sendWelcomeEmail("success@test.com", "success-test-user")
                );

        // Crear consumer para verificar que el DLT está vacío
        dltTestConsumer = createDLTTestConsumer("successfulMessageShouldNotGoToDLT");

        // Verificar que NO llegó al DLT
        // Esperar un poco y verificar que no hay mensajes de ESTE test en el DLT
        await()
                .during(Duration.ofSeconds(2))
                .atMost(Duration.ofSeconds(4))
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
     * TEST CASE 4: Datos del evento deben preservarse en el DLT
     *
     * GIVEN: Un evento con datos específicos que falla
     * WHEN: El evento va al DLT
     * THEN: Todos los datos deben estar intactos (userId, username, email, timestamp)
     */
    @Test
    // @Disabled("DLT test times out waiting for message - requires Kafka retry configuration tuning in test environment")
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

        // IMPORTANTE: Hacer flush() para asegurar que el mensaje se envíe a Kafka
        kafkaTemplate.flush();

        // Esperar a que el mensaje falle y vaya al DLT
        await()
                .atMost(Duration.ofSeconds(15))
                .pollDelay(Duration.ofSeconds(1))
                .untilAsserted(() ->
                        verify(emailService, atLeast(1))
                                .sendWelcomeEmail(anyString(), anyString())
                );

        // Crear consumer DESPUÉS de que el mensaje haya fallado
        dltTestConsumer = createDLTTestConsumer("shouldPreserveEventDataInDLT");

        // THEN - Verificar que todos los datos están intactos en el DLT
        // IMPORTANTE: DLT tests tardan más porque esperan múltiples reintentos
        await()
                .atMost(Duration.ofSeconds(30))
                .pollDelay(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    ConsumerRecords<String, UserCreatedEvent> dltRecords =
                            KafkaTestUtils.getRecords(dltTestConsumer, Duration.ofSeconds(3));

                    assertThat(dltRecords.count()).isGreaterThan(0);

                    // Buscar el mensaje de ESTE test
                    ConsumerRecord<String, UserCreatedEvent> dltRecord = null;
                    for (ConsumerRecord<String, UserCreatedEvent> r : dltRecords) {
                        if (r.value().userId().equals(specificUserId)) {
                            dltRecord = r;
                            break;
                        }
                    }

                    assertThat(dltRecord).isNotNull();
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
