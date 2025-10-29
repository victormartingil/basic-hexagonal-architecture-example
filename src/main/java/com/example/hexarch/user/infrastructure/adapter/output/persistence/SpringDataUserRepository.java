package com.example.hexarch.user.infrastructure.adapter.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * INFRASTRUCTURE LAYER - Spring Data JPA Repository
 *
 * Repositorio de Spring Data JPA que proporciona operaciones CRUD automáticas.
 * Spring Data JPA genera la implementación automáticamente en tiempo de ejecución.
 *
 * RESPONSABILIDADES:
 * - Proporcionar métodos CRUD básicos (save, findById, delete, etc.)
 * - Proporcionar métodos de consulta personalizados
 * - Interactuar directamente con la base de datos
 *
 * NOMENCLATURA:
 * - Formato: SpringData{Entidad}Repository
 * - Ejemplos: SpringDataUserRepository, SpringDataProductRepository
 *
 * CONVENCIÓN DE NOMBRES DE MÉTODOS:
 * - findBy{Campo}: busca por un campo específico
 * - existsBy{Campo}: verifica si existe por un campo
 * - deleteBy{Campo}: elimina por un campo
 * - Spring Data JPA genera la query SQL automáticamente basándose en el nombre del método
 *
 * DIFERENCIA CON UserRepository:
 * - UserRepository: interfaz del dominio (output port en Application)
 * - SpringDataUserRepository: implementación técnica de Spring Data
 * - JpaUserRepositoryAdapter: adaptador que conecta ambos
 *
 * HERENCIA:
 * - JpaRepository<UserEntity, UUID>:
 *   - UserEntity: tipo de la entidad
 *   - UUID: tipo de la clave primaria
 */
public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Busca un usuario por username
     *
     * Spring Data JPA genera automáticamente la query:
     * SELECT * FROM users WHERE username = ?
     *
     * @param username nombre de usuario a buscar
     * @return Optional con la entidad si existe, Optional.empty() si no
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Verifica si existe un usuario con el username dado
     *
     * Spring Data JPA genera automáticamente la query:
     * SELECT COUNT(*) > 0 FROM users WHERE username = ?
     *
     * @param username nombre de usuario a verificar
     * @return true si existe, false si no
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con el email dado
     *
     * Spring Data JPA genera automáticamente la query:
     * SELECT COUNT(*) > 0 FROM users WHERE email = ?
     *
     * @param email email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);
}
