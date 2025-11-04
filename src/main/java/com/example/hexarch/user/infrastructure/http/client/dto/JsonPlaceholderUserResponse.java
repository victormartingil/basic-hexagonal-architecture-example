package com.example.hexarch.user.infrastructure.http.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO de respuesta de la API externa JSONPlaceholder para endpoint GET /users/{id}.
 * <p>
 * Este DTO pertenece a la capa de <strong>Infrastructure</strong> porque representa
 * el contrato específico de la API externa (JSONPlaceholder).
 * </p>
 *
 * <h3>Arquitectura Hexagonal:</h3>
 * <ul>
 *   <li>Este DTO es específico de la tecnología (JSONPlaceholder API)</li>
 *   <li>Se mapea al ExternalUserData del Application layer</li>
 *   <li>Si cambiamos de API externa, solo cambiamos este DTO y el mapper</li>
 * </ul>
 *
 * <h3>Ejemplo de respuesta JSON de JSONPlaceholder:</h3>
 * <pre>
 * {
 *   "id": 1,
 *   "name": "Leanne Graham",
 *   "username": "Bret",
 *   "email": "Sincere@april.biz",
 *   "address": { ... },
 *   "phone": "1-770-736-8031 x56442",
 *   "website": "hildegard.org",
 *   "company": { ... }
 * }
 * </pre>
 *
 * @see <a href="https://jsonplaceholder.typicode.com/users">JSONPlaceholder Users API</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JsonPlaceholderUserResponse(
        Integer id,
        String name,
        String username,
        String email,
        Address address,
        String phone,
        String website,
        Company company
) {
    /**
     * Address nested object de JSONPlaceholder.
     * Solo incluimos los campos que nos interesan para el ejemplo.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Address(
            String street,
            String suite,
            String city,
            String zipcode
    ) {
    }

    /**
     * Company nested object de JSONPlaceholder.
     * Solo incluimos los campos que nos interesan para el ejemplo.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Company(
            String name,
            String catchPhrase,
            String bs
    ) {
    }
}
