package com.example.hexarch.user.domain.model.valueobject;

import com.example.hexarch.user.domain.exception.ValidationException;

import java.util.Objects;

/**
 * DOMAIN LAYER - Value Object
 *
 * Username es un Value Object que encapsula el concepto de "nombre de usuario" del dominio.
 *
 * VENTAJAS DE USAR VALUE OBJECT PARA USERNAME:
 * - Validación centralizada (longitud, formato, etc.)
 * - Tipo seguro (no puedes pasar cualquier String)
 * - Expresivo (el código se lee mejor: User tiene Username, no String)
 * - Reutilizable
 *
 * REGLAS DE NEGOCIO ENCAPSULADAS:
 * - Longitud mínima: 3 caracteres
 * - Longitud máxima: 50 caracteres
 * - No puede estar vacío
 * - (Podrías agregar más: solo alfanumérico, sin espacios, etc.)
 */
public final class Username {

    private final String value;

    /**
     * Constructor privado - usa el factory method of()
     */
    private Username(String value) {
        this.value = value;
    }

    /**
     * Factory method - Crea un Username validado
     *
     * @param value el username como string
     * @return Username validado
     * @throws ValidationException si no cumple las reglas
     */
    public static Username of(String value) {
        validate(value);
        return new Username(value);
    }

    /**
     * Valida las reglas del username
     *
     * @param value username a validar
     * @throws ValidationException si no cumple las reglas
     */
    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Username no puede estar vacío", "USER_001");
        }
        if (value.length() < 3) {
            throw new ValidationException("Username debe tener al menos 3 caracteres", "USER_002");
        }
        if (value.length() > 50) {
            throw new ValidationException("Username no puede tener más de 50 caracteres", "USER_003");
        }

        // OPCIONAL: Podrías agregar más validaciones
        // Por ejemplo, solo alfanumérico:
        // if (!value.matches("^[a-zA-Z0-9_]+$")) {
        //     throw new ValidationException("Username solo puede contener letras, números y guiones bajos", "USER_007");
        // }
    }

    /**
     * Obtiene el valor del username como String
     *
     * @return username como string
     */
    public String getValue() {
        return value;
    }

    /**
     * Equals basado en el VALOR (no en identidad)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Username username = (Username) o;
        return Objects.equals(value, username.value);
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
