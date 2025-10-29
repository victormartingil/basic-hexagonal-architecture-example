package com.example.hexarch.user.application.port.input;

/**
 * APPLICATION LAYER - Command (Input DTO)
 *
 * Representa una INTENCIÓN de crear un usuario.
 * Es un DTO (Data Transfer Object) inmutable que transporta datos entre capas.
 *
 * NOMENCLATURA:
 * - Formato: {Accion}{Entidad}Command
 * - Ejemplos: CreateUserCommand, UpdateUserCommand, DeleteUserCommand
 *
 * CUANDO USAR COMMAND vs QUERY:
 * - COMMAND: Para operaciones que MODIFICAN estado (Create, Update, Delete)
 * - QUERY: Para operaciones de solo LECTURA (Get, Find, List)
 *
 * VENTAJAS DEL RECORD:
 * - Inmutable por defecto (todos los campos son final)
 * - Equals, hashCode y toString generados automáticamente
 * - Constructor canónico generado automáticamente
 * - Sintaxis concisa y clara
 *
 * @param username nombre de usuario a crear
 * @param email email del usuario a crear
 */
public record CreateUserCommand(
    String username,
    String email
) {
}
