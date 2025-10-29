package com.example.hexarch.user.infrastructure.adapter.output.event;

import com.example.hexarch.user.application.port.output.UserEventPublisher;
import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * INFRASTRUCTURE LAYER - Event Publisher Adapter (Output Adapter)
 *
 * Adaptador que implementa el puerto de salida UserEventPublisher.
 * En este ejemplo, simplemente logea los eventos (implementación simple).
 *
 * IMPLEMENTACIONES REALES PODRÍAN:
 * - Publicar a Kafka: KafkaUserEventPublisherAdapter
 * - Publicar a RabbitMQ: RabbitUserEventPublisherAdapter
 * - Publicar a AWS SNS/SQS: AwsUserEventPublisherAdapter
 * - Enviar a un EventBus interno
 *
 * NOMENCLATURA:
 * - Formato: {Tecnologia}{Entidad}EventPublisherAdapter
 * - Ejemplos: KafkaUserEventPublisherAdapter, LogUserEventPublisherAdapter
 *
 * VENTAJA DE LA ABSTRACCIÓN:
 * - La Application no sabe cómo se publican los eventos
 * - Podemos cambiar la implementación sin tocar Application/Domain
 * - Ejemplo: cambiar de log a Kafka solo requiere cambiar este adapter
 *
 * ANOTACIÓN:
 * - @Component: registra como bean de Spring
 */
@Component  // Spring lo registra como bean
public class LogUserEventPublisherAdapter implements UserEventPublisher {

    // Logger para registrar eventos
    private static final Logger logger = LoggerFactory.getLogger(LogUserEventPublisherAdapter.class);

    /**
     * Publica el evento de usuario creado
     *
     * En esta implementación simple, solo logea el evento.
     * En un sistema real, esto publicaría a Kafka, RabbitMQ, etc.
     *
     * EJEMPLO DE LOG:
     * INFO - User created event published: UserCreatedEvent[userId=..., username=johndoe, ...]
     *
     * @param event evento a publicar
     */
    @Override
    public void publish(UserCreatedEvent event) {
        // Log del evento
        logger.info("User created event published: {}", event);

        // TODO: En un sistema real, aquí publicarías a Kafka:
        // kafkaTemplate.send("user-events", event.userId().toString(), event);

        // TODO: O a RabbitMQ:
        // rabbitTemplate.convertAndSend("user-exchange", "user.created", event);

        // TODO: O a AWS SNS/SQS:
        // snsClient.publish(topicArn, objectMapper.writeValueAsString(event));
    }
}
