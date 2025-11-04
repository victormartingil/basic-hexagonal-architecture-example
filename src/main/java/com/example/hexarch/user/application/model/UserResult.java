package com.example.hexarch.user.application.model;

import java.time.Instant;
import java.util.UUID;

/**
 * APPLICATION LAYER - Result (Output DTO)
 *
 * Representa el RESULTADO de un caso de uso relacionado con usuarios.
 * Es el DTO que devuelve la capa de Application a quien la invocó.
 *
 * NOMENCLATURA:
 * - Formato: {Entidad}Result o {Accion}Result
 * - Ejemplos: UserResult, AuthResult, CreateProductResult
 *
 * DIFERENCIA CON RESPONSE:
 * - UserResult: se usa en la capa de Application (independiente de HTTP)
 * - UserResponse: se usa en la capa de Infrastructure (específico de REST/HTTP)
 *
 * MAPEO TÍPICO:
 * Domain (User con Value Objects e Instant)
 *   → Application (UserResult con String e Instant)
 *   → Infrastructure (UserResponse con String y formato específico)
 *
 * NOTA: En Application layer convertimos Value Objects a String
 * porque este DTO cruza la frontera de la aplicación.
 *
 * @param id identificador único del usuario
 * @param username nombre de usuario (String, no Value Object)
 * @param email email del usuario (String, no Value Object)
 * @param enabled si el usuario está habilitado
 * @param createdAt timestamp de creación (Instant en UTC)
 */
public record UserResult(
    UUID id,
    String username,
    String email,
    boolean enabled,
    Instant createdAt
) {
}
