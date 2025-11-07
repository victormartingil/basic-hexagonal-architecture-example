package com.example.hexarch.user.domain.model.valueobject;

import com.example.hexarch.shared.domain.exception.ErrorCode;
import com.example.hexarch.user.domain.exception.ValidationException;

/**
 * DOMAIN LAYER - Value Object (Java 21 Record)
 *
 * Username es un Value Object que encapsula el concepto de "nombre de usuario" del dominio.
 *
 * ¿POR QUÉ USAR RECORD (Java 21)?
 * - Inmutabilidad garantizada por el compilador
 * - Equals, hashCode y toString automáticos
 * - Más conciso (menos líneas de código)
 * - Expresividad: muestra claramente que es un Value Object
 * - Sintaxis moderna de Java
 *
 * VENTAJAS DE VALUE OBJECT PARA USERNAME:
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
 *
 * COMPACT CONSTRUCTOR:
 * Valida automáticamente antes de crear el record.
 * Es la forma idiomática en Java 21 para Value Objects.
 */
public record Username(String value) {

    /**
     * Compact Constructor - Valida el username antes de crear el record
     *
     * Este constructor se ejecuta ANTES de asignar el valor al campo.
     * Es la forma idiomática en Records de hacer validaciones.
     *
     * @throws ValidationException si no cumple las reglas
     */
    public Username {
        validate(value);
    }

    /**
     * Factory method - Crea un Username validado
     *
     * Alternativa más explícita al constructor directo.
     * Útil para mantener API consistente con código legacy.
     *
     * @param value el username como string
     * @return Username validado
     * @throws ValidationException si no cumple las reglas
     */
    public static Username of(String value) {
        return new Username(value);
    }

    /**
     * Valida las reglas del username
     *
     * NOTA: Ahora pasa el valor actual como parámetro para mejor debugging
     *
     * @param value username a validar
     * @throws ValidationException si no cumple las reglas
     */
    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            // Pasa el valor recibido (null o vacío)
            throw new ValidationException(ErrorCode.USERNAME_EMPTY, value == null ? "null" : "\"" + value + "\"");
        }
        if (value.length() < 3) {
            // Pasa el username y su longitud actual
            throw new ValidationException(ErrorCode.USERNAME_TOO_SHORT, value, value.length());
        }
        if (value.length() > 50) {
            // Pasa el username y su longitud actual
            throw new ValidationException(ErrorCode.USERNAME_TOO_LONG, value, value.length());
        }

        // OPCIONAL: Podrías agregar más validaciones
        // Por ejemplo, solo alfanumérico:
        // if (!value.matches("^[a-zA-Z0-9_]+$")) {
        //     throw new ValidationException(ErrorCode.USERNAME_INVALID_FORMAT, value);
        // }
    }

    /**
     * Método getValue() para compatibilidad con código existente
     *
     * NOTA: En Records, puedes acceder directamente con username.value()
     * pero mantenemos getValue() para no romper código legacy.
     *
     * @return username como string
     */
    public String getValue() {
        return value;
    }

    /**
     * toString() override para mostrar solo el valor
     *
     * Por defecto, Record genera: Username[value=johndoe]
     * Este override genera: johndoe
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
     *
     * COMPARACIÓN:
     * - Clase tradicional: ~100 líneas de código
     * - Record moderno: ~125 líneas (con comentarios educativos completos)
     * - Record sin comentarios: ~40 líneas
     *
     * ¡Mucho más conciso y expresivo!
     */
}
