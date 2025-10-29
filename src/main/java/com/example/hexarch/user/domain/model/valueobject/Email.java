package com.example.hexarch.user.domain.model.valueobject;

import com.example.hexarch.user.domain.exception.ValidationException;

import java.util.Objects;

/**
 * DOMAIN LAYER - Value Object
 *
 * Email es un Value Object que encapsula el concepto de "email" del dominio.
 *
 * ¿QUÉ ES UN VALUE OBJECT?
 * - Objeto inmutable que representa un concepto del dominio
 * - Se identifica por su VALOR, no por identidad (no tiene ID)
 * - Encapsula validaciones y comportamiento relacionado
 * - Dos Value Objects con el mismo valor son iguales
 *
 * ¿CUÁNDO USAR VALUE OBJECTS?
 * - Cuando un concepto tiene reglas de validación propias
 * - Cuando quieres hacer el modelo más expresivo
 * - Para evitar "Primitive Obsession" (usar String para todo)
 *
 * VENTAJAS:
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
 * Email email = new Email("invalid");  // Lanza ValidationException
 * user = User.create(username, email); // Solo acepta Email válido
 */
public final class Email {

    private final String value;

    /**
     * Constructor privado - usa el factory method of()
     */
    private Email(String value) {
        this.value = value;
    }

    /**
     * Factory method - Crea un Email validado
     *
     * @param value el email como string
     * @return Email validado
     * @throws ValidationException si el formato no es válido
     */
    public static Email of(String value) {
        validate(value);
        return new Email(value);
    }

    /**
     * Valida el formato del email
     *
     * @param value email a validar
     * @throws ValidationException si no es válido
     */
    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Email no puede estar vacío", "USER_004");
        }

        // Regex básico para validar email
        // Formato: nombre@dominio.extension
        if (!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("Email no tiene un formato válido", "USER_005");
        }
    }

    /**
     * Obtiene el valor del email como String
     *
     * @return email como string
     */
    public String getValue() {
        return value;
    }

    /**
     * Equals basado en el VALOR (no en identidad)
     *
     * Dos emails con el mismo valor son iguales, aunque sean objetos diferentes.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
