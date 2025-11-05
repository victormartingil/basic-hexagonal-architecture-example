package com.example.hexarch.user.infrastructure.http.client.dto;

/**
 * DTO de request para crear usuario en API externa (POST /users).
 * <p>
 * Este es un ejemplo didáctico de cómo realizar peticiones POST a APIs externas.
 * Aunque no se usa en el flujo principal, demuestra el patrón completo.
 * </p>
 *
 * <h3>Arquitectura Hexagonal:</h3>
 * <ul>
 *   <li>Este DTO es específico de Infrastructure (API externa)</li>
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
 * <h3>📝 Nota sobre el nombre:</h3>
 * <p>
 * Anteriormente llamado "ExternalUserApiCreateRequest" (nombre de la API de ejemplo).
 * Renombrado a "ExternalUserApiCreateRequest" para claridad:
 * <ul>
 *   <li>✅ Deja claro que es una API externa</li>
 *   <li>✅ No confunde con el formato (JSON)</li>
 *   <li>✅ Sigue estándares profesionales de naming</li>
 * </ul>
 * En producción se usa JSONPlaceholder (https://jsonplaceholder.typicode.com/) como API de ejemplo.
 * </p>
 */
public record ExternalUserApiCreateRequest(
        String name,
        String email
) {
}
