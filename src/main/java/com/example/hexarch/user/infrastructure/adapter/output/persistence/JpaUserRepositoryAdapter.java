package com.example.hexarch.user.infrastructure.adapter.output.persistence;

import com.example.hexarch.user.application.port.output.UserRepository;
import com.example.hexarch.user.domain.model.User;
import com.example.hexarch.user.infrastructure.adapter.output.persistence.mapper.UserEntityMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * INFRASTRUCTURE LAYER - Repository Adapter (Output Adapter)
 *
 * Adaptador que implementa el puerto de salida UserRepository usando JPA.
 * Conecta la capa de Application con la base de datos.
 *
 * PATRÓN ADAPTER:
 * - UserRepository: interfaz que necesita la Application (output port)
 * - JpaUserRepositoryAdapter: implementación usando JPA (adapter)
 * - SpringDataUserRepository: repositorio técnico de Spring Data
 * - UserEntityMapper: traductor entre dominio y persistencia
 *
 * FLUJO DE DATOS:
 * 1. Application llama a UserRepository.save(user)
 * 2. JpaUserRepositoryAdapter convierte User → UserEntity
 * 3. SpringDataUserRepository persiste UserEntity en la BD
 * 4. JpaUserRepositoryAdapter convierte UserEntity → User
 * 5. Devuelve User a la Application
 *
 * NOMENCLATURA:
 * - Formato: Jpa{Entidad}RepositoryAdapter
 * - Ejemplos: JpaUserRepositoryAdapter, JpaProductRepositoryAdapter
 *
 * ANOTACIÓN:
 * - @Repository: marca esta clase como repositorio de Spring
 *   - Habilita traducción de excepciones JPA a excepciones de Spring
 *   - Permite inyección de dependencias
 */
@Repository  // Marca como repositorio de Spring
public class JpaUserRepositoryAdapter implements UserRepository {

    // Repositorio técnico de Spring Data JPA
    private final SpringDataUserRepository springDataRepository;

    // Mapper para convertir entre dominio y persistencia
    private final UserEntityMapper mapper;

    /**
     * Constructor - Inyección de dependencias
     *
     * @param springDataRepository repositorio de Spring Data
     * @param mapper mapper para conversión de entidades
     */
    public JpaUserRepositoryAdapter(
            SpringDataUserRepository springDataRepository,
            UserEntityMapper mapper
    ) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    /**
     * Guarda un usuario en la base de datos
     *
     * FLUJO:
     * 1. Convierte User (dominio) → UserEntity (JPA)
     * 2. Persiste usando Spring Data JPA
     * 3. Convierte UserEntity guardado → User (dominio)
     * 4. Devuelve User a la Application
     *
     * @param user modelo de dominio a guardar
     * @return modelo de dominio guardado
     */
    @Override
    public User save(User user) {
        // 1. Dominio → Entidad
        UserEntity entity = mapper.toEntity(user);

        // 2. Persistir en BD
        UserEntity savedEntity = springDataRepository.save(entity);

        // 3. Entidad → Dominio
        return mapper.toDomain(savedEntity);
    }

    /**
     * Busca un usuario por su ID
     *
     * @param id identificador del usuario
     * @return Optional con el usuario si existe, Optional.empty() si no
     */
    @Override
    public Optional<User> findById(UUID id) {
        // Busca la entidad y la convierte a dominio si existe
        return springDataRepository.findById(id)
                .map(mapper::toDomain);  // Conversión solo si existe
    }

    /**
     * Busca un usuario por su username
     *
     * @param username nombre de usuario a buscar
     * @return Optional con el usuario si existe, Optional.empty() si no
     */
    @Override
    public Optional<User> findByUsername(String username) {
        // Busca la entidad y la convierte a dominio si existe
        return springDataRepository.findByUsername(username)
                .map(mapper::toDomain);  // Conversión solo si existe
    }

    /**
     * Verifica si existe un usuario con el username dado
     *
     * @param username nombre de usuario a verificar
     * @return true si existe, false si no
     */
    @Override
    public boolean existsByUsername(String username) {
        return springDataRepository.existsByUsername(username);
    }

    /**
     * Verifica si existe un usuario con el email dado
     *
     * @param email email a verificar
     * @return true si existe, false si no
     */
    @Override
    public boolean existsByEmail(String email) {
        return springDataRepository.existsByEmail(email);
    }
}
