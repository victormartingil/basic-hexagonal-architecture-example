package com.example.hexarch.user.infrastructure.event;

import com.example.hexarch.user.application.port.UserEventPublisher;
import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * INFRASTRUCTURE LAYER - Event Publisher Adapter (Output Adapter)
 *
 * Adaptador que implementa el puerto de salida UserEventPublisher usando Spring Events.
 * Publica eventos de dominio de forma IN-MEMORY usando el mecanismo de eventos de Spring.
 *
 * ¿QUÉ SON SPRING EVENTS?
 * - Mecanismo de pub/sub IN-MEMORY de Spring Framework
 * - Los eventos se publican y consumen dentro de la misma JVM
 * - No requiere infraestructura externa (Kafka, RabbitMQ, etc.)
 * - Perfecto para comunicación entre componentes de la misma aplicación
 *
 * VENTAJAS:
 * - ✅ Simple: No necesita configuración de message brokers
 * - ✅ Rápido: Todo en memoria, sin latencia de red
 * - ✅ Testeable: Fácil de testear con mocks
 * - ✅ Desacoplado: Los listeners no conocen al publisher
 * - ✅ Múltiples listeners: Varios componentes pueden reaccionar al mismo evento
 *
 * DESVENTAJAS:
 * - ❌ Solo funciona en la misma JVM (no para microservicios distribuidos)
 * - ❌ Si la app se cae, los eventos no procesados se pierden
 * - ❌ No hay replay de eventos
 *
 * CUÁNDO USAR:
 * - ✅ Aplicaciones monolíticas o microservicios sin eventos distribuidos
 * - ✅ Side effects locales (enviar email, actualizar caché, logging)
 * - ✅ Desacoplar agregados dentro del mismo bounded context
 * - ✅ Proyectos que no requieren event sourcing o event streaming
 *
 * CUÁNDO EVOLUCIONAR A KAFKA/RABBITMQ:
 * - Necesitas comunicación entre microservicios independientes
 * - Necesitas durabilidad (eventos persistidos)
 * - Necesitas replay de eventos
 * - Necesitas garantías de entrega (at-least-once, exactly-once)
 * - Necesitas event sourcing
 *
 * CÓMO FUNCIONA:
 * 1. Este adapter publica el evento usando ApplicationEventPublisher
 * 2. Spring distribuye el evento a todos los @EventListener registrados
 * 3. Los listeners se ejecutan (por defecto, síncronamente en la misma transacción)
 * 4. Si algún listener falla, la transacción se rollbackea
 *
 * LISTENERS DISPONIBLES EN ESTE PROYECTO:
 * - SendWelcomeEmailListener: Simula envío de email de bienvenida
 * - UpdateUserStatsListener: Simula actualización de estadísticas
 *
 * SIN @Primary:
 * Esta clase NO tiene @Primary, por lo que NO es el adapter por defecto.
 * El adapter por defecto es CompositeUserEventPublisherAdapter (publica a Spring Events + Kafka).
 *
 * CUÁNDO USAR ESTE ADAPTER:
 * Si quieres publicar SOLO a Spring Events (sin Kafka):
 * 1. Quita @Primary de CompositeUserEventPublisherAdapter
 * 2. Agrega @Primary a esta clase
 * 3. Reinicia la aplicación
 *
 * O usa @Qualifier en la inyección:
 * {@code
 * @Autowired
 * @Qualifier("springEventUserEventPublisherAdapter")
 * private UserEventPublisher eventPublisher;
 * }
 *
 * NOMENCLATURA:
 * - Formato: {Tecnologia}{Entidad}EventPublisherAdapter
 * - Ejemplos: SpringEventUserEventPublisherAdapter, KafkaUserEventPublisherAdapter
 */
@Component
public class SpringEventUserEventPublisherAdapter implements UserEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SpringEventUserEventPublisherAdapter.class);

    // ApplicationEventPublisher de Spring para publicar eventos
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructor con inyección de dependencias
     *
     * @param eventPublisher publicador de eventos de Spring
     */
    public SpringEventUserEventPublisherAdapter(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publica el evento de usuario creado usando Spring Events
     *
     * El evento se distribuye a todos los @EventListener que estén escuchando UserCreatedEvent.
     * Por defecto, los listeners se ejecutan síncronamente en el mismo thread.
     *
     * TRANSACCIONALIDAD:
     * - Los listeners se ejecutan dentro de la misma transacción
     * - Si un listener falla, la transacción completa se rollbackea
     * - Para ejecutar async: usar @Async en el listener
     *
     * MÚLTIPLES LISTENERS:
     * Todos los listeners registrados recibirán el evento:
     * - SendWelcomeEmailListener
     * - UpdateUserStatsListener
     * - Cualquier otro listener que agregues
     *
     * @param event evento a publicar
     */
    @Override
    public void publish(UserCreatedEvent event) {
        logger.debug("Publishing UserCreatedEvent via Spring Events: userId={}, username={}",
            event.userId(), event.username());

        // Publica el evento - Spring lo distribuye a todos los @EventListener
        eventPublisher.publishEvent(event);

        logger.debug("UserCreatedEvent published successfully. Listeners will be notified.");
    }
}
