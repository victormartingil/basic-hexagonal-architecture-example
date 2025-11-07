package com.example.hexarch.user.domain.model.valueobject;

import com.example.hexarch.shared.domain.exception.ErrorCode;
import com.example.hexarch.user.domain.exception.ValidationException;

/**
 * DOMAIN LAYER - Value Object (Java 21 Record)
 *
 * Email es un Value Object que encapsula el concepto de "email" del dominio.
 *
 * ¿QUÉ ES UN VALUE OBJECT?
 * - Objeto inmutable que representa un concepto del dominio
 * - Se identifica por su VALOR, no por identidad (no tiene ID)
 * - Encapsula validaciones y comportamiento relacionado
 * - Dos Value Objects con el mismo valor son iguales
 *
 * ¿POR QUÉ USAR RECORD (Java 21)?
 * - Inmutabilidad garantizada por el compilador
 * - Equals, hashCode y toString automáticos
 * - Más conciso (menos líneas de código)
 * - Expresividad: muestra claramente que es un Value Object
 * - Sintaxis moderna de Java
 *
 * VENTAJAS DE VALUE OBJECTS:
 * - Validación centralizada (un solo lugar)
 * - Tipo seguro (no puedes pasar cualquier String)
 * - Modelo más expresivo (User tiene Email, no String)
 * - Reutilizable en todo el dominio
 *
 * EJEMPLO SIN VALUE OBJECT (malo):
 * String email = "invalid";  // No se valida
 * user.setEmail(email);      // Acepta cualquier string
 *
 * EJEMPLO CON VALUE OBJECT (bueno):
 * Email email = Email.of("invalid");  // Lanza ValidationException
 * user = User.create(username, email); // Solo acepta Email válido
 *
 * COMPACT CONSTRUCTOR:
 * En Records, el compact constructor permite validar antes de asignar valores.
 * Es más elegante que un constructor privado + factory method.
 */
public record Email(String value) {

    /**
     * Compact Constructor - Valida el email antes de crear el record
     *
     * Este constructor se ejecuta ANTES de asignar el valor al campo.
     * Es la forma idiomática en Records de hacer validaciones.
     *
     * @throws ValidationException si el formato no es válido
     */
    public Email {
        validate(value);
    }

    /**
     * Factory method - Crea un Email validado
     *
     * Alternativa más explícita al constructor directo.
     * Útil para mantener API consistente con código legacy.
     *
     * @param value el email como string
     * @return Email validado
     * @throws ValidationException si el formato no es válido
     */
    public static Email of(String value) {
        return new Email(value);
    }

    /**
     * Valida el formato del email
     *
     * NOTA: Ahora pasa el valor actual como parámetro para mejor debugging
     *
     * @param value email a validar
     * @throws ValidationException si no es válido
     */
    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            // Pasa el valor recibido (null o vacío)
            throw new ValidationException(ErrorCode.EMAIL_EMPTY, value == null ? "null" : "\"" + value + "\"");
        }

        // Regex básico para validar email
        // Formato: nombre@dominio.extension
        if (!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            // Pasa el email inválido recibido
            throw new ValidationException(ErrorCode.EMAIL_INVALID_FORMAT, value);
        }
    }

    /**
     * Método getValue() para compatibilidad con código existente
     *
     * NOTA: En Records, puedes acceder directamente con email.value()
     * pero mantenemos getValue() para no romper código legacy.
     *
     * @return email como string
     */
    public String getValue() {
        return value;
    }

    /**
     * toString() override para mostrar solo el valor
     *
     * Por defecto, Record genera: Email[value=john@example.com]
     * Este override genera: john@example.com
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * NOTA IMPORTANTE:
     *
     * Records automáticamente generan:
     * - equals() basado en todos los campos
     * - hashCode() basado en todos los campos
     * - Constructor canónico
     * - Getters con el nombre del campo (value() en este caso)
     *
     * NO necesitas escribir manualmente:
     * - equals()
     * - hashCode()
     * - Constructor privado
     * - Getter getValue() (aunque lo mantenemos por compatibilidad)
     */
}
