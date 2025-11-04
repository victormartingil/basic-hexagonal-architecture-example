package com.example.hexarch.user.infrastructure.persistence.mapper;

import com.example.hexarch.user.domain.model.User;
import com.example.hexarch.user.infrastructure.persistence.model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * INFRASTRUCTURE LAYER - Entity Mapper (MapStruct)
 *
 * Mapper para convertir entre el modelo de dominio (User) y la entidad JPA (UserEntity).
 * Actúa como traductor entre la capa de Application/Domain y la base de datos.
 *
 * RESPONSABILIDADES:
 * - Convertir User (dominio) → UserEntity (persistencia)
 * - Convertir UserEntity (persistencia) → User (dominio)
 * - Aislar el dominio de los detalles de persistencia
 *
 * NOMENCLATURA:
 * - Formato: {Entidad}EntityMapper
 * - Ejemplos: UserEntityMapper, ProductEntityMapper, OrderEntityMapper
 *
 * PRINCIPIO DE SEPARACIÓN:
 * - El modelo de dominio (User) NO debe tener anotaciones JPA
 * - La entidad JPA (UserEntity) NO debe tener lógica de negocio
 * - El mapper conecta ambos mundos manteniendo la separación
 *
 * DIRECCIONES DE CONVERSIÓN:
 * - toDomain(): UserEntity → User (al leer de la BD)
 * - toEntity(): User → UserEntity (al guardar en la BD)
 *
 * MAPSTRUCT CON VALUE OBJECTS Y FACTORY METHODS:
 * - User usa Value Objects (Username, Email) pero UserEntity usa String
 * - User.reconstitute() es un factory method que no puede ser manejado por MapStruct automáticamente
 * - Usamos método default para conversiones complejas que requieren lógica custom
 *
 * VENTAJAS DE MAPSTRUCT:
 * - Type-safe: detecta errores en compile time
 * - Performance: no usa reflexión para mapeos simples
 * - Código generado visible en target/generated-sources/
 * - Permite métodos default para lógica compleja
 * - Combinación de mapeo automático + manual
 */
@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    /**
     * Convierte una entidad JPA a modelo de dominio
     *
     * Se usa cuando LEEMOS datos de la base de datos.
     * Usa el factory method reconstitute() del dominio porque el usuario ya existe.
     *
     * CONVERSIONES VALUE OBJECTS:
     * - String (username) → Username.of() (dentro de reconstitute)
     * - String (email) → Email.of() (dentro de reconstitute)
     * - UUID, boolean, Instant → Sin conversión
     *
     * MÉTODO DEFAULT:
     * Como User.reconstitute() es un factory method específico,
     * usamos un método default para tener control total de la conversión.
     *
     * @param entity entidad JPA leída de la BD
     * @return modelo de dominio
     */
    default User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        // Usamos reconstitute() porque estamos recuperando un usuario existente
        return User.reconstitute(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.isEnabled(),
                entity.getCreatedAt()
        );
    }

    /**
     * Convierte un modelo de dominio a entidad JPA
     *
     * Se usa cuando GUARDAMOS datos en la base de datos.
     * Crea una nueva UserEntity con los datos del modelo de dominio.
     *
     * CONVERSIONES VALUE OBJECTS:
     * - Username → String usando .getValue()
     * - Email → String usando .getValue()
     * - UUID, boolean, Instant → Copia directa
     *
     * MAPPINGS CON EXPRESIONES:
     * - username: extrae valor del Value Object
     * - email: extrae valor del Value Object
     * - id, enabled, createdAt: mapeo directo
     *
     * MapStruct genera la implementación automáticamente usando el
     * constructor de UserEntity.
     *
     * @param user modelo de dominio
     * @return entidad JPA lista para persistir
     */
    @Mapping(target = "username", expression = "java(user.getUsername().getValue())")
    @Mapping(target = "email", expression = "java(user.getEmail().getValue())")
    UserEntity toEntity(User user);
}
