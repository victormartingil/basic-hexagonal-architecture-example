package com.example.hexarch.user.application.port;

import java.util.Optional;

/**
 * Output Port para integración con APIs REST externas de usuarios.
 * <p>
 * Este port define el contrato para obtener información de usuarios desde servicios externos.
 * Es un ejemplo didáctico de cómo integrar APIs REST externas en arquitectura hexagonal.
 * </p>
 *
 * <h3>Principios de Arquitectura Hexagonal:</h3>
 * <ul>
 *   <li>Este es un <strong>Output Port</strong> (lo que la aplicación NECESITA del exterior)</li>
 *   <li>Define <strong>QUÉ</strong> necesita la aplicación, no <strong>CÓMO</strong> se obtiene</li>
 *   <li>La implementación está en Infrastructure (JsonPlaceholderClient)</li>
 *   <li>Permite cambiar la API externa sin tocar la lógica de negocio</li>
 * </ul>
 *
 * <h3>Ejemplo de uso:</h3>
 * <pre>
 * // En CreateUserService
 * ExternalUserData externalData = externalUserApiClient
 *     .getUserById(1)
 *     .orElse(ExternalUserData.empty());
 * </pre>
 *
 * @see com.example.hexarch.user.infrastructure.http.client.JsonPlaceholderClient
 */
public interface ExternalUserApiClient {

    /**
     * Obtiene información de un usuario desde una API externa por su ID.
     * <p>
     * Este método es un ejemplo didáctico de integración con APIs REST.
     * En un caso real, podrías usar esto para:
     * <ul>
     *   <li>Verificar usuarios en un sistema legacy</li>
     *   <li>Obtener datos de enriquecimiento (avatar, ubicación, etc.)</li>
     *   <li>Validar contra un servicio de identidad externo</li>
     * </ul>
     *
     * @param externalUserId ID del usuario en el sistema externo
     * @return Optional con los datos del usuario externo, o empty si no existe
     */
    Optional<ExternalUserData> getUserById(Integer externalUserId);

    /**
     * Crea un usuario en una API externa (ejemplo didáctico de POST).
     * <p>
     * Aunque no se usa en el flujo actual, este método demuestra cómo
     * realizar peticiones POST a APIs externas.
     * </p>
     *
     * @param name  Nombre del usuario
     * @param email Email del usuario
     * @return Datos del usuario creado en el sistema externo
     */
    ExternalUserData createExternalUser(String name, String email);

    /**
     * DTO que representa datos de usuario obtenidos desde una API externa.
     * <p>
     * Este record está en el puerto (Application layer) porque representa
     * el <strong>contrato</strong> de datos que necesita la aplicación.
     * </p>
     *
     * @param id       ID del usuario en el sistema externo
     * @param name     Nombre completo del usuario
     * @param username Username del usuario externo
     * @param email    Email del usuario
     * @param phone    Teléfono del usuario (puede ser null)
     * @param website  Website del usuario (puede ser null)
     */
    record ExternalUserData(
            Integer id,
            String name,
            String username,
            String email,
            String phone,
            String website
    ) {
        /**
         * Crea un ExternalUserData vacío para casos donde no se obtiene respuesta.
         */
        public static ExternalUserData empty() {
            return new ExternalUserData(null, null, null, null, null, null);
        }

        /**
         * Verifica si los datos están vacíos.
         */
        public boolean isEmpty() {
            return id == null;
        }
    }
}
