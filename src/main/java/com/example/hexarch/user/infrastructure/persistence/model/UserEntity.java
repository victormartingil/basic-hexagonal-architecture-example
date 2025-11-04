package com.example.hexarch.user.infrastructure.persistence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * INFRASTRUCTURE LAYER - JPA Entity
 *
 * Entidad JPA que representa la tabla 'users' en la base de datos.
 * Es el modelo de persistencia (no el modelo de dominio).
 *
 * DIFERENCIA CON DOMAIN MODEL:
 * - UserEntity: modelo de persistencia (JPA, anotaciones de BD)
 * - User: modelo de dominio (lógica de negocio, sin dependencias de frameworks)
 *
 * PRINCIPIOS:
 * - Solo se usa en la capa de Infrastructure
 * - Contiene anotaciones JPA (@Entity, @Table, @Column, etc.)
 * - Es mutable (tiene setters) porque JPA lo requiere
 * - Se convierte a/desde el modelo de dominio usando un mapper
 *
 * NOMENCLATURA:
 * - Formato: {Entidad}Entity
 * - Ejemplos: UserEntity, ProductEntity, OrderEntity
 *
 * ANOTACIONES JPA:
 * - @Entity: marca la clase como entidad JPA
 * - @Table: especifica el nombre de la tabla
 * - @Id: marca el campo como clave primaria
 * - @Column: personaliza el mapeo de columnas
 *
 * ANOTACIONES LOMBOK:
 * - @Getter/@Setter: genera getters/setters automáticamente
 * - @NoArgsConstructor: genera constructor sin argumentos (requerido por JPA)
 * - @AllArgsConstructor: genera constructor con todos los argumentos
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    /**
     * Clave primaria de tipo UUID
     *
     * @Column(name = "id"): mapea a la columna 'id'
     * nullable = false: no puede ser nulo
     */
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * Nombre de usuario único
     *
     * unique = true: índice único en la BD
     * length = 50: longitud máxima del VARCHAR
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Email único
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Indica si el usuario está habilitado
     */
    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    /**
     * Fecha de creación del usuario
     *
     * @Column(name = "created_at"): mapea a la columna 'created_at'
     * updatable = false: no se puede modificar después de la creación
     *
     * INSTANT EN BASE DE DATOS:
     * - JPA mapea Instant a TIMESTAMP WITH TIME ZONE en PostgreSQL
     * - Se almacena siempre en UTC
     * - No hay ambigüedad de zonas horarias
     * - Ideal para auditoría en aplicaciones internacionales
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // NOTA: Lombok genera automáticamente:
    // - Getters para todos los campos
    // - Setters para todos los campos
    // - Constructor sin argumentos
    // - Constructor con todos los argumentos
}
