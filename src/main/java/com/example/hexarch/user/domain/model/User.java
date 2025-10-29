package com.example.hexarch.user.domain.model;

import com.example.hexarch.user.domain.model.valueobject.Email;
import com.example.hexarch.user.domain.model.valueobject.Username;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * DOMAIN LAYER - Aggregate Root (with Lombok)
 *
 * User es el Aggregate Root del contexto de usuarios.
 * Contiene toda la lógica de negocio relacionada con un usuario.
 *
 * PRINCIPIOS IMPORTANTES:
 * - Inmutable: todos los campos son final, no hay setters
 * - Auto-validado: valida sus propias reglas de negocio
 * - Sin dependencias de frameworks: es Java puro (POJO) + Lombok para reducir boilerplate
 * - Factory methods: create() para crear nuevo, reconstitute() para reconstruir desde BD
 * - Value Objects: usa Username y Email en lugar de String (más expresivo y seguro)
 *
 * INSTANT vs LocalDateTime:
 * - Instant: punto absoluto en el tiempo (UTC), ideal para timestamps de sistema
 * - LocalDateTime: fecha/hora sin zona horaria, NO recomendado para auditoría
 * - Para createdAt usamos Instant porque es un timestamp de auditoría del sistema
 *
 * LOMBOK ANNOTATIONS:
 * - @Getter: genera getters automáticamente para todos los campos
 * - @EqualsAndHashCode: genera equals() y hashCode() basados solo en el ID
 * - @ToString: genera toString() con todos los campos
 * - @AllArgsConstructor(access = PRIVATE): genera constructor privado
 *
 * VENTAJAS DE LOMBOK:
 * - Reduce código boilerplate (getters, equals, hashCode, toString, constructor)
 * - Código más limpio y legible
 * - Menos propenso a errores (métodos generados automáticamente)
 * - Fácil mantenimiento (cambios se propagan automáticamente)
 */
@Getter  // Genera getters para todos los campos
@EqualsAndHashCode(onlyExplicitlyIncluded = true)  // equals/hashCode solo basado en campos con @Include
@ToString  // Genera toString() con todos los campos
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // Constructor privado (solo accesible desde factory methods)
public class User {

    // Campos del aggregate (todos final para inmutabilidad)
    @EqualsAndHashCode.Include  // Solo el ID se usa en equals/hashCode (identidad del aggregate)
    private final UUID id;
    private final Username username;  // Value Object (no String)
    private final Email email;        // Value Object (no String)
    private final boolean enabled;
    private final Instant createdAt;  // Instant (no LocalDateTime) para timestamps

    /**
     * Factory Method - Crear un nuevo usuario
     *
     * Se usa cuando creamos un usuario por primera vez.
     * Genera automáticamente el ID y la fecha de creación.
     *
     * NOTA: Recibe String y los convierte a Value Objects internamente.
     * Los Value Objects se encargan de la validación.
     *
     * @param username nombre de usuario único (como String)
     * @param email email del usuario (como String)
     * @return nueva instancia de User
     * @throws ValidationException si los datos no son válidos (lanzada por los Value Objects)
     */
    public static User create(String username, String email) {
        // Los Value Objects validan automáticamente al crearse
        Username usernameVO = Username.of(username);
        Email emailVO = Email.of(email);

        return new User(
            UUID.randomUUID(),           // Genera nuevo ID
            usernameVO,                  // Value Object validado
            emailVO,                     // Value Object validado
            true,                        // Por defecto, el usuario está habilitado
            Instant.now()                // Timestamp actual en UTC
        );
    }

    /**
     * Factory Method - Reconstruir usuario existente
     *
     * Se usa cuando recuperamos un usuario desde la base de datos.
     * Ya tiene ID y fecha de creación existentes.
     *
     * @param id ID existente del usuario
     * @param username nombre de usuario (como String)
     * @param email email del usuario (como String)
     * @param enabled si el usuario está habilitado
     * @param createdAt fecha de creación existente (como Instant)
     * @return instancia de User reconstruida
     */
    public static User reconstitute(UUID id, String username, String email, boolean enabled, Instant createdAt) {
        // Reconstitute NO valida (asumimos que los datos de BD son válidos)
        // Pero aún así usamos Value Objects para mantener el modelo consistente
        return new User(
            id,
            Username.of(username),
            Email.of(email),
            enabled,
            createdAt
        );
    }

    /**
     * Método de negocio - Deshabilitar usuario
     *
     * Como User es inmutable, devuelve una NUEVA instancia con el cambio aplicado.
     * No modifica el objeto actual.
     *
     * @return nueva instancia de User con enabled = false
     */
    public User disable() {
        return new User(this.id, this.username, this.email, false, this.createdAt);
    }

    /**
     * Método de negocio - Habilitar usuario
     *
     * @return nueva instancia de User con enabled = true
     */
    public User enable() {
        return new User(this.id, this.username, this.email, true, this.createdAt);
    }

    // ===========================
    // NOTA: Las validaciones ahora están en los Value Objects
    // Username.of() valida el username
    // Email.of() valida el email
    // ===========================

    // ===========================
    // GETTERS, EQUALS, HASHCODE Y TOSTRING
    // ===========================
    // Todos generados automáticamente por Lombok:
    // - getId(), getUsername(), getEmail(), isEnabled(), getCreatedAt()
    // - equals(Object) y hashCode() basados solo en el ID
    // - toString() con todos los campos
    // ===========================
}
