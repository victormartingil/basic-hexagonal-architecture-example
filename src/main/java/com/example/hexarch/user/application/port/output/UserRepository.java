package com.example.hexarch.user.application.port.output;

import com.example.hexarch.user.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * APPLICATION LAYER - Output Port (Repository Interface)
 *
 * Define el contrato de persistencia que necesita la aplicación.
 * Esta interfaz representa un PUERTO DE SALIDA de la aplicación.
 *
 * INVERSIÓN DE DEPENDENCIAS:
 * - La Application Layer define la interfaz (lo que necesita)
 * - La Infrastructure Layer implementa la interfaz (cómo lo hace)
 * - La dependencia apunta hacia adentro (Infrastructure → Application)
 *
 * PRINCIPIOS:
 * - Solo tiene métodos que la Application necesita
 * - No sabe CÓMO se implementa (JPA, MongoDB, API externa, etc.)
 * - Trabaja con objetos de dominio (User), no con entities (UserEntity)
 *
 * NOMENCLATURA:
 * - Formato: {Entidad}Repository
 * - Ejemplos: UserRepository, ProductRepository, OrderRepository
 */
public interface UserRepository {

    /**
     * Guarda un usuario en el repositorio
     *
     * Si el usuario ya existe, lo actualiza.
     * Si no existe, lo crea.
     *
     * @param user usuario a guardar
     * @return usuario guardado (puede incluir datos generados como timestamps)
     */
    User save(User user);

    /**
     * Busca un usuario por su ID
     *
     * @param id identificador del usuario
     * @return Optional con el usuario si existe, Optional.empty() si no
     */
    Optional<User> findById(UUID id);

    /**
     * Busca un usuario por su username
     *
     * @param username nombre de usuario a buscar
     * @return Optional con el usuario si existe, Optional.empty() si no
     */
    Optional<User> findByUsername(String username);

    /**
     * Verifica si existe un usuario con el username dado
     *
     * @param username nombre de usuario a verificar
     * @return true si existe, false si no
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con el email dado
     *
     * @param email email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);
}
