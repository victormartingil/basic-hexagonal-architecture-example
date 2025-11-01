package com.example.hexarch.notifications.application.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * APPLICATION LAYER - Email Service con Circuit Breaker
 *
 * Servicio que simula el envío de emails con protección de Circuit Breaker.
 *
 * ¿QUÉ ES CIRCUIT BREAKER?
 * Patrón de resiliencia que previene cascading failures (fallos en cascada)
 * cuando un servicio externo está caído o lento.
 *
 * ANALOGÍA:
 * Como un interruptor eléctrico en tu casa:
 * - Si hay un cortocircuito → el interruptor se abre (protege)
 * - Después de un tiempo → intentas cerrarlo (reconectar)
 * - Si funciona → sigue funcionando (CLOSED)
 * - Si sigue fallando → se vuelve a abrir (OPEN)
 *
 * ESTADOS DEL CIRCUIT BREAKER:
 *
 * ┌──────────────────────────────────────────────────────────────┐
 * │ CLOSED (Cerrado) - Estado normal                             │
 * │ • Todas las llamadas pasan al servicio                       │
 * │ • Monitorea tasa de fallos                                   │
 * │ • Si fallos > threshold → Cambia a OPEN                      │
 * └────────────┬─────────────────────────────────────────────────┘
 *              │ (demasiados fallos)
 *              ↓
 * ┌──────────────────────────────────────────────────────────────┐
 * │ OPEN (Abierto) - Servicio protegido                          │
 * │ • NO permite llamadas al servicio                            │
 * │ • Falla inmediatamente (fail-fast)                           │
 * │ • Llama a método fallback                                    │
 * │ • Después de timeout → Cambia a HALF_OPEN                    │
 * └────────────┬─────────────────────────────────────────────────┘
 *              │ (después de waitDurationInOpenState)
 *              ↓
 * ┌──────────────────────────────────────────────────────────────┐
 * │ HALF_OPEN (Semi-abierto) - Probando recuperación            │
 * │ • Permite N llamadas de prueba                               │
 * │ • Si funciona → Cambia a CLOSED ✅                           │
 * │ • Si falla → Vuelve a OPEN ❌                                │
 * └──────────────────────────────────────────────────────────────┘
 *
 * PROBLEMA SIN CIRCUIT BREAKER:
 *
 * Email Service caído → Consumer sigue llamando → Timeout tras timeout
 * → Recursos bloqueados → Latencia alta → Todo el sistema se degrada ❌
 *
 * SOLUCIÓN CON CIRCUIT BREAKER:
 *
 * Email Service caído → Circuit Breaker detecta → Abre el circuito
 * → Falla rápido con fallback → Recursos liberados → Sistema sigue funcionando ✅
 *
 * VENTAJAS:
 * - ✅ Fail-fast: no espera timeouts largos
 * - ✅ Protege recursos (threads, connections)
 * - ✅ Permite que servicios se recuperen
 * - ✅ Graceful degradation (degradación elegante)
 * - ✅ Evita cascading failures
 *
 * CUÁNDO USAR:
 * - Llamadas a servicios externos (APIs, email, SMS)
 * - Llamadas entre microservicios
 * - Operaciones que pueden fallar por red/disponibilidad
 * - Integraciones con third-party services
 *
 * CUÁNDO NO USAR:
 * - Lógica de negocio local (no hay llamadas externas)
 * - Operaciones que DEBEN ejecutarse siempre
 * - Validaciones críticas
 *
 * CONFIGURACIÓN EN ESTE SERVICIO:
 * - Name: "emailService" (nombre del circuit breaker)
 * - Fallback: sendEmailFallback() (método que se ejecuta si circuit está OPEN)
 * - Configuración: application.yaml (thresholds, timeouts, etc.)
 *
 * EJEMPLO DE USO:
 * {@code
 * @Autowired
 * private EmailService emailService;
 *
 * public void processUser(UserCreatedEvent event) {
 *     // Circuit Breaker protege esta llamada automáticamente
 *     emailService.sendWelcomeEmail(event.email(), event.username());
 *
 *     // Si Email Service está caído:
 *     // - Circuit Breaker detecta fallos
 *     // - Después de N fallos → Abre el circuito
 *     // - Llama a sendEmailFallback() en lugar de sendWelcomeEmail()
 *     // - Consumer continúa procesando sin bloquearse
 * }
 * }
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final Random random = new Random();

    @Value("${email.service.failure-rate:30}")
    private int failureRatePercentage;

    /**
     * Envía email de bienvenida con protección de Circuit Breaker
     *
     * @CircuitBreaker:
     * - name: "emailService" (debe coincidir con configuración en application.yaml)
     * - fallbackMethod: "sendEmailFallback" (método que se ejecuta si circuit está OPEN)
     *
     * FLUJO NORMAL (Circuit CLOSED):
     * 1. Método se ejecuta normalmente
     * 2. Si falla ocasionalmente → Circuit sigue CLOSED
     * 3. Si tasa de fallos > threshold → Circuit cambia a OPEN
     *
     * FLUJO CON CIRCUIT OPEN:
     * 1. Circuit Breaker NO ejecuta este método
     * 2. Llama inmediatamente a sendEmailFallback()
     * 3. Después de waitDuration → Circuit cambia a HALF_OPEN
     * 4. Permite N llamadas de prueba
     * 5. Si funcionan → Circuit cambia a CLOSED
     *
     * SIMULACIÓN:
     * Este método simula un servicio de email que puede fallar:
     * - 30% de probabilidad de fallo (configurable)
     * - En un sistema real: llamada a SendGrid, AWS SES, Mailgun, etc.
     *
     * @param email destinatario
     * @param username nombre del usuario
     * @throws RuntimeException si el servicio falla (simulado)
     */
    // NOTA: No usamos fallbackMethod para permitir que excepciones se propaguen a Kafka
    // Kafka manejará reintentos y DLT. Circuit Breaker aún registra métricas.
    @CircuitBreaker(name = "emailService")
    public void sendWelcomeEmail(String email, String username) {
        logger.info("📧 [EMAIL SERVICE] Attempting to send welcome email to: {}", email);

        // ════════════════════════════════════════════════════════════
        // SIMULACIÓN DE FALLO (para demostrar Circuit Breaker)
        // ════════════════════════════════════════════════════════════
        // En un sistema real, aquí llamarías a un servicio externo:
        // sendGridClient.send(email, template);
        // sesClient.sendEmail(request);
        // mailgunClient.send(message);

        // Simular fallo aleatorio (configurable via property email.service.failure-rate)
        // Default: 30%, Tests: 0% (set in test properties)
        if (random.nextInt(100) < failureRatePercentage) {
            logger.error("❌ [EMAIL SERVICE] Failed to send email - Service temporarily unavailable");
            throw new RuntimeException("Email service temporarily unavailable");
        }

        // Simular latencia de servicio externo
        try {
            Thread.sleep(100);  // 100ms de latencia
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logger.info("✅ [EMAIL SERVICE] Welcome email sent successfully to: {}", email);
        logger.info("    Subject: Welcome to our platform, {}!", username);
        logger.info("    Body: Thank you for registering. Your account has been created.");
    }

    /**
     * Método fallback que se ejecuta cuando el Circuit Breaker está OPEN
     *
     * CUÁNDO SE EJECUTA:
     * - Circuit Breaker está en estado OPEN (demasiados fallos)
     * - Circuit Breaker está en estado HALF_OPEN y las pruebas fallan
     * - El método original lanza una excepción
     *
     * IMPORTANTE:
     * - Debe tener la MISMA FIRMA que el método original
     * - Más un parámetro Exception al final (opcional pero recomendado)
     * - NO debe lanzar excepciones (o se propaga el error)
     *
     * QUÉ HACER EN EL FALLBACK:
     * 1. Loguear que se usó fallback (para monitoreo)
     * 2. Retornar valor por defecto / vacío
     * 3. Guardar en cola para reintentar después
     * 4. Enviar alerta si es crítico
     * 5. NO fallar (graceful degradation)
     *
     * EJEMPLOS DE FALLBACK:
     *
     * Opción 1: Loguear y continuar (este ejemplo)
     * {@code
     * public void sendEmailFallback(String email, String username, Exception ex) {
     *     logger.warn("Fallback: Email not sent due to circuit breaker");
     * }
     * }
     *
     * Opción 2: Guardar en cola para reintentar
     * {@code
     * public void sendEmailFallback(String email, String username, Exception ex) {
     *     emailQueueRepository.save(new PendingEmail(email, username));
     *     logger.info("Email queued for retry later");
     * }
     * }
     *
     * Opción 3: Usar servicio alternativo
     * {@code
     * public void sendEmailFallback(String email, String username, Exception ex) {
     *     backupEmailService.send(email, username);  // Servicio backup
     * }
     * }
     *
     * @param email destinatario
     * @param username nombre del usuario
     * @param ex excepción que causó el fallo (puede ser null)
     */
    private void sendEmailFallback(String email, String username, Exception ex) {
        logger.warn("⚠️  [EMAIL SERVICE - FALLBACK] Circuit breaker is OPEN or service failed");
        logger.warn("    Email: {}", email);
        logger.warn("    Username: {}", username);
        logger.warn("    Reason: {}", ex != null ? ex.getMessage() : "Circuit breaker OPEN");

        // ════════════════════════════════════════════════════════════
        // ACCIONES RECOMENDADAS EN PRODUCCIÓN
        // ════════════════════════════════════════════════════════════

        // 1. GUARDAR EN COLA (recomendado)
        // Guardar email en BD/Redis para reintentar después
        // emailQueueRepository.save(new PendingEmail(email, username, Instant.now()));
        // logger.info("Email queued for retry when service recovers");

        // 2. USAR SERVICIO ALTERNATIVO
        // Si tienes múltiples proveedores de email (SendGrid + Mailgun)
        // try {
        //     backupEmailService.send(email, username);
        //     logger.info("Email sent via backup service");
        // } catch (Exception e) {
        //     logger.error("Backup service also failed: {}", e.getMessage());
        // }

        // 3. ENVIAR ALERTA
        // Si es crítico, alertar al equipo
        // if (isCritical(email)) {
        //     alertService.sendAlert(
        //         "Email Circuit Breaker OPEN",
        //         "Failed to send email to: " + email,
        //         AlertSeverity.HIGH
        //     );
        // }

        // 4. INCREMENTAR MÉTRICA
        // meterRegistry.counter("email.circuit_breaker.fallback").increment();

        logger.info("✅ [EMAIL SERVICE - FALLBACK] Request handled gracefully (email not sent)");
    }

    /**
     * EJEMPLO: Método para verificar estado del Circuit Breaker
     *
     * Útil para health checks y monitoring:
     *
     * {@code
     * @Autowired
     * private CircuitBreakerRegistry circuitBreakerRegistry;
     *
     * public CircuitBreakerState getCircuitBreakerState() {
     *     CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("emailService");
     *     return cb.getState();  // CLOSED, OPEN, HALF_OPEN
     * }
     * }
     */

    /**
     * EJEMPLO: Endpoint para forzar cambio de estado (testing)
     *
     * {@code
     * @RestController
     * @RequestMapping("/admin/circuit-breaker")
     * public class CircuitBreakerController {
     *
     *     @Autowired
     *     private CircuitBreakerRegistry registry;
     *
     *     @PostMapping("/email/transition-to-open")
     *     public String forceOpen() {
     *         registry.circuitBreaker("emailService").transitionToOpenState();
     *         return "Circuit breaker opened manually";
     *     }
     *
     *     @PostMapping("/email/transition-to-closed")
     *     public String forceClosed() {
     *         registry.circuitBreaker("emailService").transitionToClosedState();
     *         return "Circuit breaker closed manually";
     *     }
     *
     *     @GetMapping("/email/state")
     *     public String getState() {
     *         return registry.circuitBreaker("emailService")
     *             .getState()
     *             .toString();
     *     }
     * }
     * }
     */
}
