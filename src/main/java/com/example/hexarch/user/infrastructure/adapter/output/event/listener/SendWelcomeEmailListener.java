package com.example.hexarch.user.infrastructure.adapter.output.event.listener;

import com.example.hexarch.user.domain.event.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * INFRASTRUCTURE LAYER - Event Listener
 *
 * Listener que reacciona al evento UserCreatedEvent para enviar un email de bienvenida.
 * Demuestra cómo diferentes componentes pueden reaccionar al mismo evento de dominio.
 *
 * ¿QUÉ ES UN EVENT LISTENER?
 * - Componente que "escucha" eventos publicados en la aplicación
 * - Reacciona cuando un evento específico ocurre
 * - Permite desacoplar la lógica de negocio de los side effects
 *
 * VENTAJAS DE USAR LISTENERS:
 * - ✅ Desacoplamiento: CreateUserService no sabe que se envía un email
 * - ✅ Single Responsibility: Cada listener tiene una responsabilidad única
 * - ✅ Extensible: Puedes agregar más listeners sin modificar código existente
 * - ✅ Testeable: Puedes testear el listener independientemente
 *
 * EJECUCIÓN:
 * - Por defecto: Síncrono (se ejecuta en el mismo thread y transacción)
 * - Con @Async: Asíncrono (se ejecuta en otro thread)
 * - Con @TransactionalEventListener: Control fino de cuándo ejecutar (after commit, etc.)
 *
 * ORDEN DE EJECUCIÓN:
 * Si tienes múltiples listeners:
 * - Por defecto: No hay orden garantizado
 * - Con @Order(1), @Order(2): Puedes definir el orden
 *
 * EN UN SISTEMA REAL:
 * Este listener llamaría a un servicio de email real:
 * - emailService.sendWelcomeEmail(event.email(), event.username());
 * - Podría usar SendGrid, AWS SES, Mailgun, etc.
 *
 * MANEJO DE ERRORES:
 * - Si este listener lanza una excepción, la transacción completa se rollbackea
 * - Para evitar esto, usa @TransactionalEventListener(phase = AFTER_COMMIT)
 * - O usa @Async para ejecutar fuera de la transacción
 *
 * @EventListener: Marca el método como listener de eventos
 * Spring automáticamente lo registra y lo llama cuando se publica UserCreatedEvent
 */
@Component
public class SendWelcomeEmailListener {

    private static final Logger logger = LoggerFactory.getLogger(SendWelcomeEmailListener.class);

    /**
     * Maneja el evento UserCreatedEvent enviando un email de bienvenida
     *
     * Este método se ejecuta automáticamente cuando se publica UserCreatedEvent.
     * Es síncrono por defecto (se ejecuta en el mismo thread y transacción).
     *
     * FLUJO:
     * 1. Usuario es creado en CreateUserService
     * 2. Se publica UserCreatedEvent
     * 3. Spring llama a este método automáticamente
     * 4. Se envía el email de bienvenida
     *
     * SIMULACIÓN:
     * En este ejemplo solo logueamos, pero en un sistema real aquí:
     * - Llamarías a un EmailService
     * - Enviarías un email usando SendGrid/AWS SES
     * - Incluirías un link de activación
     * - Personalizarías el mensaje con el nombre del usuario
     *
     * EJEMPLO REAL:
     * <pre>
     * {@code
     * @EventListener
     * public void onUserCreated(UserCreatedEvent event) {
     *     EmailTemplate template = emailTemplateService.getWelcomeTemplate();
     *     template.setRecipient(event.email());
     *     template.setVariable("username", event.username());
     *     template.setVariable("activationLink", generateActivationLink(event.userId()));
     *
     *     emailService.send(template);
     *     logger.info("Welcome email sent to: {}", event.email());
     * }
     * }
     * </pre>
     *
     * @param event el evento que contiene los datos del usuario creado
     */
    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        logger.info("📧 [EVENT LISTENER] User created - Sending welcome email to: {} (username: {})",
            event.email(), event.username());

        // TODO: En un sistema real, aquí llamarías a un servicio de email
        // emailService.sendWelcomeEmail(event.email(), event.username());

        // Simulación de envío de email
        try {
            // Simular un pequeño delay como si estuviéramos enviando el email
            logger.debug("Preparing welcome email template for user: {}", event.username());
            logger.debug("Email would be sent to: {}", event.email());
            logger.debug("Subject: Welcome to our platform, {}!", event.username());
            logger.debug("Body: Thank you for registering. Your account has been created successfully.");

            logger.info("✅ Welcome email sent successfully to: {}", event.email());

        } catch (Exception e) {
            // En un sistema real, aquí manejarías errores de envío
            // Podrías: reintentar, guardar en cola, enviar alerta, etc.
            logger.error("❌ Failed to send welcome email to: {}. Error: {}",
                event.email(), e.getMessage());

            // IMPORTANTE: Decide si quieres que esto falle la creación del usuario
            // Opción 1: No lanzar excepción (el usuario se crea aunque falle el email)
            // Opción 2: Lanzar excepción (rollback de la transacción completa)
            // En este caso, NO lanzamos para que el usuario se cree aunque falle el email
        }
    }
}
