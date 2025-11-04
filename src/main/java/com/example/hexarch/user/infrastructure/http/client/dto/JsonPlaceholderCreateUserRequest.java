package com.example.hexarch.user.infrastructure.http.client.dto;

/**
 * DTO de request para crear usuario en JSONPlaceholder API (POST /users).
 * <p>
 * Este es un ejemplo didáctico de cómo realizar peticiones POST a APIs externas.
 * Aunque no se usa en el flujo principal, demuestra el patrón completo.
 * </p>
 *
 * <h3>Arquitectura Hexagonal:</h3>
 * <ul>
 *   <li>Este DTO es específico de Infrastructure (JSONPlaceholder)</li>
 *   <li>Es diferente del CreateUserCommand de Application layer</li>
 *   <li>Permite evolucionar independientemente del dominio interno</li>
 * </ul>
 *
 * <h3>Ejemplo de request JSON:</h3>
 * <pre>
 * {
 *   "name": "John Doe",
 *   "email": "john@example.com"
 * }
 * </pre>
 *
 * @see <a href="https://jsonplaceholder.typicode.com/">JSONPlaceholder API</a>
 */
public record JsonPlaceholderCreateUserRequest(
        String name,
        String email
) {
}
