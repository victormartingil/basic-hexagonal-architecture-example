package com.example.hexarch.user.infrastructure.adapter.input.rest.mapper;

import com.example.hexarch.user.application.port.input.CreateUserCommand;
import com.example.hexarch.user.application.port.input.UserResult;
import com.example.hexarch.user.infrastructure.adapter.input.rest.dto.generated.CreateUserRequest;
import com.example.hexarch.user.infrastructure.adapter.input.rest.dto.generated.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * INFRASTRUCTURE LAYER - REST Mapper (MapStruct)
 *
 * Mapper para convertir entre DTOs REST y DTOs de Application.
 * Actúa como traductor entre la capa de Infrastructure y Application.
 *
 * RESPONSABILIDADES:
 * - Convertir Request DTOs → Commands/Queries
 * - Convertir Results → Response DTOs
 * - Transformar tipos (ej: UUID ↔ String)
 * - Formatear datos si es necesario
 *
 * NOMENCLATURA:
 * - Formato: {Entidad}RestMapper
 * - Ejemplos: UserRestMapper, ProductRestMapper, OrderRestMapper
 *
 * MAPSTRUCT:
 * - @Mapper: marca esta interfaz como un mapper de MapStruct
 * - componentModel = "spring": genera un @Component para inyección
 * - MapStruct GENERA la implementación automáticamente en compile time
 * - El código generado está en target/generated-sources/annotations/
 *
 * VENTAJAS DE MAPSTRUCT:
 * - Seguridad en compile-time (detecta errores al compilar)
 * - Alto rendimiento (no usa reflexión)
 * - Código generado legible y debuggeable
 * - Reduce código boilerplate
 * - Compatible con Lombok
 *
 * CONVERSIONES PERSONALIZADAS:
 * - UUID → String: usa expression para convertir con toString()
 * - Resto de campos: mapeo automático por nombre
 */
@Mapper(componentModel = "spring")
public interface UserRestMapper {

    /**
     * Convierte CreateUserRequest (REST) → CreateUserCommand (Application)
     *
     * Este método se invoca en el Controller antes de llamar al Use Case.
     * MapStruct mapea automáticamente los campos con el mismo nombre.
     *
     * @param request DTO de la petición REST
     * @return command para el Use Case
     */
    CreateUserCommand toCommand(CreateUserRequest request);

    /**
     * Convierte UserResult (Application) → UserResponse (REST)
     *
     * Este método se invoca en el Controller después de ejecutar el Use Case.
     * Transforma el resultado de Application en la respuesta REST.
     *
     * TRANSFORMACIONES:
     * - id: mapeo directo UUID → UUID
     * - username, email, enabled: mapeo automático
     * - createdAt: Instant → OffsetDateTime usando método helper
     *
     * @param result resultado del Use Case
     * @return DTO de la respuesta REST
     */
    @Mapping(target = "createdAt", expression = "java(toOffsetDateTime(result.createdAt()))")
    UserResponse toResponse(UserResult result);

    /**
     * Convierte Instant (domain) → OffsetDateTime (OpenAPI)
     *
     * OpenAPI Generator usa OffsetDateTime para datetime fields.
     * Convertimos Instant a OffsetDateTime en UTC.
     *
     * @param instant timestamp en Instant
     * @return timestamp en OffsetDateTime (UTC)
     */
    default OffsetDateTime toOffsetDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atOffset(ZoneOffset.UTC);
    }
}
