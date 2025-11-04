package com.example.hexarch.user.infrastructure.persistence;
import com.example.hexarch.user.infrastructure.persistence.model.UserEntity;

import com.example.hexarch.user.domain.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * INTEGRATION TEST - JpaUserRepositoryAdapter
 *
 * Test de integración que prueba el adaptador de persistencia de forma aislada:
 * JpaUserRepositoryAdapter → SpringDataUserRepository → PostgreSQL
 *
 * ¿POR QUÉ TESTS ESPECÍFICOS DEL ADAPTER?
 *
 * En arquitectura hexagonal, es una buena práctica probar cada adapter por separado:
 *
 * 1. AISLAMIENTO: Si un test falla, sabes exactamente que el problema está en el adapter
 *    de persistencia, no en el controller, service o mapping.
 *
 * 2. RAPIDEZ: Tests más focalizados = debugging más rápido cuando algo falla.
 *
 * 3. COBERTURA: Puedes probar edge cases del repository que serían complejos de
 *    alcanzar desde tests end-to-end del controller.
 *
 * 4. PIRÁMIDE DE TESTING:
 *    - Unit tests (muchos): Lógica de negocio aislada con mocks
 *    - Integration tests (algunos): Cada adapter probado con sus dependencias reales
 *    - E2E tests (pocos): Flujo completo Controller → Service → Repository → DB
 *
 * DIFERENCIA CON UserControllerIntegrationTest:
 * - UserControllerIntegrationTest: Prueba el flujo completo HTTP → Service → Repository → DB
 * - JpaUserRepositoryAdapterIntegrationTest: Prueba SOLO Repository → DB (más enfocado)
 *
 * VENTAJAS:
 * - Detecta problemas específicos de persistencia (mapping, queries SQL, constraints)
 * - Más rápido que tests end-to-end
 * - Permite probar casos específicos de BD sin complejidad de HTTP/Service
 *
 * TESTCONTAINERS:
 * - Levanta PostgreSQL real en Docker para el test
 * - Asegura que el test es independiente del ambiente
 * - Limpia automáticamente después del test
 *
 * ANOTACIONES:
 * - @SpringBootTest: levanta el contexto completo de Spring
 * - @Testcontainers: habilita Testcontainers
 * - @Container: define un contenedor Docker para el test
 */
@SpringBootTest  // Levanta el contexto completo de Spring Boot
@Testcontainers  // Habilita Testcontainers
@DisplayName("JpaUserRepositoryAdapter - Integration Tests")
class JpaUserRepositoryAdapterIntegrationTest {

    /**
     * Contenedor de PostgreSQL
     *
     * Testcontainers levanta un contenedor Docker con PostgreSQL.
     * Se comparte entre todos los tests de esta clase.
     * Se destruye automáticamente al finalizar.
     */
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    /**
     * Configuración dinámica de propiedades
     *
     * Sobrescribe la configuración de application.properties con los datos
     * del contenedor de PostgreSQL que Testcontainers levantó.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    // El adapter que vamos a probar
    @Autowired
    private JpaUserRepositoryAdapter userRepository;

    // Spring Data JPA repository para limpiar datos entre tests
    @Autowired
    private SpringDataUserRepository springDataRepository;

    /**
     * Limpia la BD después de cada test para asegurar aislamiento
     */
    @AfterEach
    void cleanUp() {
        springDataRepository.deleteAll();
    }

    // ========================================
    // TESTS PARA save()
    // ========================================

    /**
     * TEST CASE 1: Guardar usuario nuevo exitosamente
     *
     * GIVEN: Un usuario de dominio válido
     * WHEN: Se llama a save()
     * THEN:
     *   - El usuario se persiste en la BD
     *   - El ID se genera automáticamente (no es null)
     *   - Los datos persisten correctamente
     */
    @Test
    @DisplayName("save() - Debe guardar usuario nuevo exitosamente")
    void shouldSaveUser_whenValidUser() {
        // GIVEN - Crear usuario de dominio
        User user = User.create("johndoe", "john@example.com");

        // WHEN - Guardar usuario
        User savedUser = userRepository.save(user);

        // THEN - Verificar que se guardó correctamente
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();  // ID generado automáticamente
        assertThat(savedUser.getUsername().getValue()).isEqualTo("johndoe");
        assertThat(savedUser.getEmail().getValue()).isEqualTo("john@example.com");
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedUser.getCreatedAt()).isNotNull();

        // Verificar que realmente se guardó en la BD consultando directamente
        Optional<UserEntity> entityInDb = springDataRepository.findById(savedUser.getId());
        assertThat(entityInDb).isPresent();
        assertThat(entityInDb.get().getUsername()).isEqualTo("johndoe");
        assertThat(entityInDb.get().getEmail()).isEqualTo("john@example.com");
    }

    /**
     * TEST CASE 2: Actualizar usuario existente
     *
     * GIVEN: Un usuario ya guardado en la BD
     * WHEN: Se llama a save() con el mismo ID pero datos diferentes
     * THEN: El usuario se actualiza en la BD
     *
     * NOTA: Aunque el dominio User es inmutable, JPA permite update por ID.
     */
    @Test
    @DisplayName("save() - Debe actualizar usuario existente cuando ID ya existe")
    void shouldUpdateUser_whenUserAlreadyExists() {
        // GIVEN - Crear y guardar usuario inicial
        User originalUser = User.create("johndoe", "john@example.com");
        User savedUser = userRepository.save(originalUser);
        UUID userId = savedUser.getId();

        // Crear un nuevo usuario con el mismo ID pero datos diferentes
        User updatedUser = User.reconstitute(
                userId,
                "johndoe_updated",  // username diferente
                "updated@example.com",  // email diferente
                true,
                savedUser.getCreatedAt()
        );

        // WHEN - Guardar el usuario "actualizado"
        User result = userRepository.save(updatedUser);

        // THEN - Verificar que se actualizó
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername().getValue()).isEqualTo("johndoe_updated");
        assertThat(result.getEmail().getValue()).isEqualTo("updated@example.com");

        // Verificar que solo hay UN registro en la BD (update, no insert)
        assertThat(springDataRepository.count()).isEqualTo(1);

        // Verificar consultando directamente la BD
        Optional<UserEntity> entityInDb = springDataRepository.findById(userId);
        assertThat(entityInDb).isPresent();
        assertThat(entityInDb.get().getUsername()).isEqualTo("johndoe_updated");
        assertThat(entityInDb.get().getEmail()).isEqualTo("updated@example.com");
    }

    // ========================================
    // TESTS PARA findById()
    // ========================================

    /**
     * TEST CASE 3: Buscar usuario por ID existente
     *
     * GIVEN: Un usuario guardado en la BD
     * WHEN: Se busca por su ID
     * THEN: Se retorna Optional con el usuario
     */
    @Test
    @DisplayName("findById() - Debe retornar usuario cuando ID existe")
    void shouldFindUser_whenIdExists() {
        // GIVEN - Guardar usuario
        User savedUser = userRepository.save(User.create("johndoe", "john@example.com"));
        UUID userId = savedUser.getId();

        // WHEN - Buscar por ID
        Optional<User> foundUser = userRepository.findById(userId);

        // THEN - Verificar que se encontró
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(userId);
        assertThat(foundUser.get().getUsername().getValue()).isEqualTo("johndoe");
        assertThat(foundUser.get().getEmail().getValue()).isEqualTo("john@example.com");
    }

    /**
     * TEST CASE 4: Buscar usuario por ID no existente
     *
     * GIVEN: Un ID que no existe en la BD
     * WHEN: Se busca por ese ID
     * THEN: Se retorna Optional.empty()
     */
    @Test
    @DisplayName("findById() - Debe retornar Optional.empty() cuando ID no existe")
    void shouldReturnEmpty_whenIdDoesNotExist() {
        // GIVEN - Un ID que no existe
        UUID nonExistingId = UUID.randomUUID();

        // WHEN - Buscar por ID inexistente
        Optional<User> foundUser = userRepository.findById(nonExistingId);

        // THEN - Verificar que retorna vacío
        assertThat(foundUser).isEmpty();
    }

    // ========================================
    // TESTS PARA findByUsername()
    // ========================================

    /**
     * TEST CASE 5: Buscar usuario por username existente
     *
     * GIVEN: Un usuario guardado en la BD
     * WHEN: Se busca por su username
     * THEN: Se retorna Optional con el usuario
     */
    @Test
    @DisplayName("findByUsername() - Debe retornar usuario cuando username existe")
    void shouldFindUser_whenUsernameExists() {
        // GIVEN - Guardar usuario
        userRepository.save(User.create("johndoe", "john@example.com"));

        // WHEN - Buscar por username
        Optional<User> foundUser = userRepository.findByUsername("johndoe");

        // THEN - Verificar que se encontró
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername().getValue()).isEqualTo("johndoe");
        assertThat(foundUser.get().getEmail().getValue()).isEqualTo("john@example.com");
    }

    /**
     * TEST CASE 6: Buscar usuario por username no existente
     *
     * GIVEN: Un username que no existe en la BD
     * WHEN: Se busca por ese username
     * THEN: Se retorna Optional.empty()
     */
    @Test
    @DisplayName("findByUsername() - Debe retornar Optional.empty() cuando username no existe")
    void shouldReturnEmpty_whenUsernameDoesNotExist() {
        // GIVEN - Username que no existe
        String nonExistingUsername = "nonexisting";

        // WHEN - Buscar por username inexistente
        Optional<User> foundUser = userRepository.findByUsername(nonExistingUsername);

        // THEN - Verificar que retorna vacío
        assertThat(foundUser).isEmpty();
    }

    /**
     * TEST CASE 7: findByUsername() es case-sensitive
     *
     * GIVEN: Un usuario con username "JohnDoe"
     * WHEN: Se busca por "johndoe" (lowercase)
     * THEN: NO se encuentra (case-sensitive)
     *
     * NOTA: Esto valida que la query SQL no usa ILIKE o LOWER()
     */
    @Test
    @DisplayName("findByUsername() - Debe ser case-sensitive")
    void shouldBeCaseSensitive_whenSearchingByUsername() {
        // GIVEN - Guardar usuario con username en mayúsculas/minúsculas
        userRepository.save(User.create("JohnDoe", "john@example.com"));

        // WHEN - Buscar con diferente capitalización
        Optional<User> foundUser = userRepository.findByUsername("johndoe");

        // THEN - No debe encontrar (case-sensitive)
        assertThat(foundUser).isEmpty();

        // Pero sí debe encontrar con el caso correcto
        Optional<User> foundUserCorrectCase = userRepository.findByUsername("JohnDoe");
        assertThat(foundUserCorrectCase).isPresent();
    }

    // ========================================
    // TESTS PARA existsByUsername()
    // ========================================

    /**
     * TEST CASE 8: Verificar que existe usuario por username
     *
     * GIVEN: Un usuario guardado en la BD
     * WHEN: Se verifica existencia por username
     * THEN: Retorna true
     */
    @Test
    @DisplayName("existsByUsername() - Debe retornar true cuando username existe")
    void shouldReturnTrue_whenUsernameExists() {
        // GIVEN - Guardar usuario
        userRepository.save(User.create("johndoe", "john@example.com"));

        // WHEN - Verificar existencia
        boolean exists = userRepository.existsByUsername("johndoe");

        // THEN - Debe existir
        assertThat(exists).isTrue();
    }

    /**
     * TEST CASE 9: Verificar que NO existe usuario por username
     *
     * GIVEN: Un username que no existe en la BD
     * WHEN: Se verifica existencia por username
     * THEN: Retorna false
     */
    @Test
    @DisplayName("existsByUsername() - Debe retornar false cuando username no existe")
    void shouldReturnFalse_whenUsernameDoesNotExist() {
        // GIVEN - Username que no existe
        String nonExistingUsername = "nonexisting";

        // WHEN - Verificar existencia
        boolean exists = userRepository.existsByUsername(nonExistingUsername);

        // THEN - No debe existir
        assertThat(exists).isFalse();
    }

    // ========================================
    // TESTS PARA existsByEmail()
    // ========================================

    /**
     * TEST CASE 10: Verificar que existe usuario por email
     *
     * GIVEN: Un usuario guardado en la BD
     * WHEN: Se verifica existencia por email
     * THEN: Retorna true
     */
    @Test
    @DisplayName("existsByEmail() - Debe retornar true cuando email existe")
    void shouldReturnTrue_whenEmailExists() {
        // GIVEN - Guardar usuario
        userRepository.save(User.create("johndoe", "john@example.com"));

        // WHEN - Verificar existencia
        boolean exists = userRepository.existsByEmail("john@example.com");

        // THEN - Debe existir
        assertThat(exists).isTrue();
    }

    /**
     * TEST CASE 11: Verificar que NO existe usuario por email
     *
     * GIVEN: Un email que no existe en la BD
     * WHEN: Se verifica existencia por email
     * THEN: Retorna false
     */
    @Test
    @DisplayName("existsByEmail() - Debe retornar false cuando email no existe")
    void shouldReturnFalse_whenEmailDoesNotExist() {
        // GIVEN - Email que no existe
        String nonExistingEmail = "nonexisting@example.com";

        // WHEN - Verificar existencia
        boolean exists = userRepository.existsByEmail(nonExistingEmail);

        // THEN - No debe existir
        assertThat(exists).isFalse();
    }

    /**
     * TEST CASE 12: existsByEmail() es case-insensitive (según configuración BD)
     *
     * GIVEN: Un usuario con email "John@Example.com"
     * WHEN: Se busca por "john@example.com" (lowercase)
     * THEN: Depende de la configuración de PostgreSQL (por defecto case-sensitive)
     *
     * NOTA: Este test documenta el comportamiento esperado.
     * Si la BD está configurada con collation case-insensitive, debería encontrarlo.
     */
    @Test
    @DisplayName("existsByEmail() - Documenta comportamiento case-sensitivity")
    void shouldDocumentCaseSensitivity_whenCheckingEmail() {
        // GIVEN - Guardar usuario con email en mayúsculas/minúsculas
        userRepository.save(User.create("johndoe", "John@Example.com"));

        // WHEN - Verificar con diferente capitalización
        boolean existsLowercase = userRepository.existsByEmail("john@example.com");

        // THEN - PostgreSQL por defecto es case-sensitive para strings
        // Si tu BD tiene collation case-insensitive, cambiar a isTrue()
        assertThat(existsLowercase).isFalse();

        // Pero sí debe encontrar con el caso correcto
        boolean existsCorrectCase = userRepository.existsByEmail("John@Example.com");
        assertThat(existsCorrectCase).isTrue();
    }

    // ========================================
    // TESTS DE CONSTRAINTS Y EDGE CASES
    // ========================================

    /**
     * TEST CASE 13: Guardar múltiples usuarios
     *
     * GIVEN: Varios usuarios diferentes
     * WHEN: Se guardan todos
     * THEN: Todos se persisten correctamente
     */
    @Test
    @DisplayName("save() - Debe guardar múltiples usuarios diferentes")
    void shouldSaveMultipleUsers_whenUsersAreDifferent() {
        // GIVEN & WHEN - Guardar varios usuarios
        User user1 = userRepository.save(User.create("user1", "user1@example.com"));
        User user2 = userRepository.save(User.create("user2", "user2@example.com"));
        User user3 = userRepository.save(User.create("user3", "user3@example.com"));

        // THEN - Todos deben estar en la BD
        assertThat(springDataRepository.count()).isEqualTo(3);

        // Verificar que cada uno se puede recuperar
        assertThat(userRepository.findById(user1.getId())).isPresent();
        assertThat(userRepository.findById(user2.getId())).isPresent();
        assertThat(userRepository.findById(user3.getId())).isPresent();

        // Verificar que tienen IDs únicos
        assertThat(user1.getId()).isNotEqualTo(user2.getId());
        assertThat(user2.getId()).isNotEqualTo(user3.getId());
        assertThat(user1.getId()).isNotEqualTo(user3.getId());
    }
}
