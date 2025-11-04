package com.example.hexarch.user.infrastructure.event.listener;

import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * INFRASTRUCTURE LAYER - Event Listener
 *
 * Listener que reacciona al evento UserCreatedEvent para actualizar estadísticas de usuarios.
 * Demuestra cómo múltiples listeners pueden procesar el mismo evento de forma independiente.
 *
 * MÚLTIPLES LISTENERS PARA EL MISMO EVENTO:
 * Este es el SEGUNDO listener para UserCreatedEvent. También existe:
 * - SendWelcomeEmailListener: Envía email de bienvenida
 * - (Futuros): AuditListener, AnalyticsListener, etc.
 *
 * Todos se ejecutan cuando se publica el evento, de forma independiente.
 *
 * ORDEN DE EJECUCIÓN:
 * - @Order(2): Este listener se ejecuta segundo (después del email listener que tiene @Order(1))
 * - Números más bajos = mayor prioridad
 * - Sin @Order: No hay garantía de orden
 *
 * USO REAL DE ESTADÍSTICAS:
 * En un sistema real, este listener actualizaría:
 * - Contador total de usuarios registrados
 * - Estadísticas por fecha (usuarios registrados hoy/mes/año)
 * - Segmentación de usuarios (por región, edad, etc.)
 * - KPIs para dashboards de negocio
 * - Cache de estadísticas para consultas rápidas
 *
 * PATRÓN CQRS:
 * Este listener es perfecto para CQRS:
 * - Comando (Write): CreateUser crea el usuario
 * - Query (Read): Este listener actualiza tablas denormalizadas para consultas rápidas
 * - Ejemplo: tabla "user_statistics" con contadores precalculados
 *
 * VENTAJAS DE ESTE ENFOQUE:
 * - ✅ CreateUserService no sabe nada de estadísticas
 * - ✅ Si quitas/agregas estadísticas, no tocas el core business logic
 * - ✅ Puedes agregar nuevos listeners sin modificar código existente
 * - ✅ Cada listener es independiente y testeable
 *
 * CONSIDERACIONES DE PERFORMANCE:
 * - Este listener es síncrono (se ejecuta en la misma transacción)
 * - Para operaciones lentas, usa @Async o @TransactionalEventListener(phase = AFTER_COMMIT)
 * - Si falla, puede causar rollback de la creación del usuario
 */
@Component
@Order(2)  // Se ejecuta segundo, después del email listener
public class UpdateUserStatsListener {

    private static final Logger logger = LoggerFactory.getLogger(UpdateUserStatsListener.class);

    /**
     * Maneja el evento UserCreatedEvent actualizando estadísticas
     *
     * Este método se ejecuta automáticamente cuando se publica UserCreatedEvent.
     * Se ejecuta DESPUÉS del SendWelcomeEmailListener (debido a @Order(2)).
     *
     * FLUJO:
     * 1. Usuario es creado en CreateUserService
     * 2. Se publica UserCreatedEvent
     * 3. Spring ejecuta SendWelcomeEmailListener (@Order(1))
     * 4. Spring ejecuta este listener (@Order(2))
     * 5. Estadísticas se actualizan
     *
     * SIMULACIÓN:
     * En este ejemplo solo logueamos, pero en un sistema real aquí:
     * - Incrementarías contador en Redis: INCR users:total
     * - Actualizarías tabla de estadísticas: UPDATE user_stats SET total = total + 1
     * - Registrarías métrica en sistema de monitoring (Prometheus, DataDog)
     * - Enviarías evento a analytics (Google Analytics, Mixpanel)
     *
     * EJEMPLO REAL CON REDIS:
     * <pre>
     * {@code
     * @EventListener
     * @Order(2)
     * public void onUserCreated(UserCreatedEvent event) {
     *     // Incrementar contador total
     *     redisTemplate.opsForValue().increment("stats:users:total");
     *
     *     // Incrementar contador del día
     *     String today = LocalDate.now().toString();
     *     redisTemplate.opsForValue().increment("stats:users:daily:" + today);
     *
     *     // Registrar en sistema de analytics
     *     analyticsService.track("user.registered", Map.of(
     *         "userId", event.userId(),
     *         "timestamp", event.occurredAt()
     *     ));
     *
     *     logger.info("User statistics updated for userId: {}", event.userId());
     * }
     * }
     * </pre>
     *
     * EJEMPLO REAL CON BASE DE DATOS:
     * <pre>
     * {@code
     * @EventListener
     * @Order(2)
     * public void onUserCreated(UserCreatedEvent event) {
     *     // Actualizar tabla denormalizada de estadísticas
     *     userStatsRepository.incrementTotalUsers();
     *     userStatsRepository.incrementUsersToday();
     *
     *     // Guardar en tabla de eventos para analytics
     *     UserRegistrationEvent analyticsEvent = new UserRegistrationEvent(
     *         event.userId(),
     *         event.occurredAt(),
     *         extractMetadata(event)
     *     );
     *     analyticsEventRepository.save(analyticsEvent);
     *
     *     logger.info("Analytics and statistics updated for userId: {}", event.userId());
     * }
     * }
     * </pre>
     *
     * @param event el evento que contiene los datos del usuario creado
     */
    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        logger.info("📊 [EVENT LISTENER] User created - Updating user statistics for user: {} (id: {})",
            event.username(), event.userId());

        // TODO: En un sistema real, aquí actualizarías estadísticas
        // redisTemplate.opsForValue().increment("stats:users:total");
        // userStatsRepository.incrementTotalUsers();
        // analyticsService.trackUserRegistration(event);

        // Simulación de actualización de estadísticas
        try {
            logger.debug("Incrementing total users counter...");
            logger.debug("Updating daily registration stats...");
            logger.debug("Recording user registration timestamp: {}", event.occurredAt());

            // Simular diferentes tipos de estadísticas
            logger.debug("Stats updated:");
            logger.debug("  - Total users: +1");
            logger.debug("  - Users registered today: +1");
            logger.debug("  - Last registration: {}", event.occurredAt());

            logger.info("✅ User statistics updated successfully for userId: {}", event.userId());

        } catch (Exception e) {
            // En un sistema real, aquí manejarías errores de actualización
            logger.error("❌ Failed to update statistics for userId: {}. Error: {}",
                event.userId(), e.getMessage());

            // IMPORTANTE: Decide si esto debe fallar la creación del usuario
            // Generalmente, estadísticas no son críticas, así que NO lanzamos excepción
            // El usuario se crea correctamente aunque fallen las estadísticas
        }
    }

    /**
     * EJEMPLO ADICIONAL: Listener asíncrono con @Async
     *
     * Si descomentas este método y comentas el anterior, verás cómo hacer un listener asíncrono.
     * Requiere @EnableAsync en una clase de configuración.
     *
     * VENTAJAS DE @Async:
     * - No bloquea la transacción principal
     * - El usuario se crea inmediatamente sin esperar las estadísticas
     * - Mejor performance percibida
     *
     * DESVENTAJAS DE @Async:
     * - Se ejecuta FUERA de la transacción
     * - Si la app se cae, el evento puede perderse
     * - Más difícil de debuguear
     */
    /*
    @Async
    @EventListener
    @Order(2)
    public void onUserCreatedAsync(UserCreatedEvent event) {
        logger.info("📊 [ASYNC EVENT LISTENER] Updating statistics asynchronously for user: {}",
            event.username());

        // Este código se ejecuta en otro thread
        // No bloquea la creación del usuario
        // Ideal para operaciones que pueden tardar (llamadas a APIs externas, etc.)

        try {
            // Simulación de operación lenta
            Thread.sleep(1000);
            logger.info("✅ Async statistics updated for userId: {}", event.userId());
        } catch (Exception e) {
            logger.error("❌ Failed to update async statistics: {}", e.getMessage());
        }
    }
    */
}
