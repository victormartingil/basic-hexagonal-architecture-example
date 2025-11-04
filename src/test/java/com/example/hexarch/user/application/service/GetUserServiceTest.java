package com.example.hexarch.user.application.service;

import com.example.hexarch.user.application.model.GetUserQuery;
import com.example.hexarch.user.application.model.UserResult;
import com.example.hexarch.user.application.port.UserRepository;
import com.example.hexarch.user.domain.exception.UserNotFoundException;
import com.example.hexarch.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * UNIT TEST - GetUserService
 *
 * Test unitario para el servicio GetUserService.
 * Verifica la lógica de negocio de forma aislada, mockeando las dependencias.
 *
 * CQRS - QUERY SIDE:
 * - Este test valida el lado "Query" (lectura) de CQRS
 * - GetUserService es read-only (@Transactional(readOnly = true))
 * - No modifica estado, solo consulta
 *
 * PRINCIPIOS DE UNIT TESTING:
 * - Aislamiento: mockea todas las dependencias externas
 * - Rapidez: no accede a BD, red, sistema de archivos
 * - Cobertura: prueba casos exitosos y casos de error
 * - Nomenclatura: shouldDoSomething_whenCondition
 *
 * FRAMEWORKS:
 * - JUnit 5: framework de testing
 * - Mockito: framework para crear mocks
 * - AssertJ: assertions fluidas y legibles
 *
 * ANOTACIONES:
 * - @ExtendWith(MockitoExtension.class): habilita Mockito
 * - @Mock: crea un mock de la dependencia
 * - @InjectMocks: crea la instancia a testear e inyecta los mocks
 * - @BeforeEach: se ejecuta antes de cada test
 * - @Test: marca un método como test
 * - @DisplayName: descripción legible del test
 */
@ExtendWith(MockitoExtension.class)  // Habilita Mockito para JUnit 5
@DisplayName("GetUserService - Unit Tests")
class GetUserServiceTest {

    // Mock de la dependencia
    @Mock
    private UserRepository userRepository;

    // Instancia a testear (con mocks inyectados)
    @InjectMocks
    private GetUserService getUserService;

    // Datos de prueba
    private UUID existingUserId;
    private UUID nonExistingUserId;
    private User existingUser;

    /**
     * Se ejecuta antes de cada test
     * Prepara los datos de prueba comunes
     */
    @BeforeEach
    void setUp() {
        // IDs de prueba
        existingUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        nonExistingUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        // Crear un usuario de prueba usando el método reconstitute
        // (simula un usuario que ya existe en BD)
        existingUser = User.reconstitute(
                existingUserId,
                "johndoe",           // reconstitute espera String, no Username
                "john@example.com",  // reconstitute espera String, no Email
                true,
                Instant.parse("2024-01-15T10:30:00.123Z")
        );
    }

    /**
     * TEST CASE 1: Caso exitoso - Usuario encontrado
     *
     * GIVEN: Un query con un ID de usuario que existe en el repositorio
     * WHEN: Se ejecuta el caso de uso
     * THEN:
     *   - Se retorna UserResult con los datos correctos
     *   - El repositorio fue consultado una vez
     *   - Los datos del result coinciden con el usuario encontrado
     */
    @Test
    @DisplayName("Debe retornar usuario cuando el ID existe")
    void shouldReturnUser_whenUserExists() {
        // GIVEN - Configurar el comportamiento del mock
        GetUserQuery query = new GetUserQuery(existingUserId);

        // Mock: el repositorio encuentra el usuario
        when(userRepository.findById(existingUserId))
                .thenReturn(Optional.of(existingUser));

        // WHEN - Ejecutar el método a testear
        UserResult result = getUserService.execute(query);

        // THEN - Verificar el resultado

        // 1. Verificar que el resultado no es nulo y tiene datos correctos
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(existingUserId);
        assertThat(result.username()).isEqualTo("johndoe");
        assertThat(result.email()).isEqualTo("john@example.com");
        assertThat(result.enabled()).isTrue();
        assertThat(result.createdAt()).isEqualTo(Instant.parse("2024-01-15T10:30:00.123Z"));

        // 2. Verificar que se consultó el repositorio exactamente una vez
        verify(userRepository, times(1)).findById(existingUserId);

        // 3. Verificar que no se llamaron otros métodos del repositorio
        verifyNoMoreInteractions(userRepository);
    }

    /**
     * TEST CASE 2: Error - Usuario no encontrado
     *
     * GIVEN: Un query con un ID de usuario que NO existe en el repositorio
     * WHEN: Se ejecuta el caso de uso
     * THEN:
     *   - Se lanza UserNotFoundException
     *   - El mensaje de la excepción contiene el ID buscado
     *   - El repositorio fue consultado una vez
     */
    @Test
    @DisplayName("Debe lanzar UserNotFoundException cuando el usuario no existe")
    void shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        // GIVEN - El usuario NO existe
        GetUserQuery query = new GetUserQuery(nonExistingUserId);

        // Mock: el repositorio NO encuentra el usuario
        when(userRepository.findById(nonExistingUserId))
                .thenReturn(Optional.empty());

        // WHEN & THEN - Verificar que se lanza la excepción
        assertThatThrownBy(() -> getUserService.execute(query))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(nonExistingUserId.toString());

        // Verificar que se consultó el repositorio
        verify(userRepository, times(1)).findById(nonExistingUserId);

        // Verificar que no se llamaron otros métodos del repositorio
        verifyNoMoreInteractions(userRepository);
    }

    /**
     * TEST CASE 3: Verificar mapeo correcto de Value Objects
     *
     * GIVEN: Un usuario con Value Objects (Username, Email)
     * WHEN: Se ejecuta el caso de uso
     * THEN:
     *   - Los Value Objects se extraen correctamente a Strings
     *   - El UserResult contiene valores primitivos/simples, no Value Objects
     */
    @Test
    @DisplayName("Debe extraer correctamente los valores de los Value Objects")
    void shouldExtractValueObjectsCorrectly() {
        // GIVEN
        GetUserQuery query = new GetUserQuery(existingUserId);
        when(userRepository.findById(existingUserId))
                .thenReturn(Optional.of(existingUser));

        // WHEN
        UserResult result = getUserService.execute(query);

        // THEN - Verificar que los valores son Strings, no Value Objects
        assertThat(result.username())
                .isInstanceOf(String.class)
                .isEqualTo("johndoe");

        assertThat(result.email())
                .isInstanceOf(String.class)
                .isEqualTo("john@example.com");

        // Verificar que createdAt es Instant (no se convierte)
        assertThat(result.createdAt()).isInstanceOf(Instant.class);
    }

    /**
     * TEST CASE 4: Verificar comportamiento con usuario deshabilitado
     *
     * GIVEN: Un usuario que existe pero está deshabilitado (enabled = false)
     * WHEN: Se ejecuta el caso de uso
     * THEN:
     *   - Se retorna el usuario con enabled = false
     *   - El servicio NO valida si el usuario está habilitado (eso es responsabilidad de otro layer)
     */
    @Test
    @DisplayName("Debe retornar usuario deshabilitado sin validaciones adicionales")
    void shouldReturnDisabledUser_withoutValidation() {
        // GIVEN - Usuario deshabilitado
        User disabledUser = User.reconstitute(
                existingUserId,
                "disableduser",           // reconstitute espera String
                "disabled@example.com",   // reconstitute espera String
                false,  // deshabilitado
                Instant.parse("2024-01-15T10:30:00.123Z")
        );

        GetUserQuery query = new GetUserQuery(existingUserId);
        when(userRepository.findById(existingUserId))
                .thenReturn(Optional.of(disabledUser));

        // WHEN
        UserResult result = getUserService.execute(query);

        // THEN - El servicio retorna el usuario deshabilitado sin error
        assertThat(result).isNotNull();
        assertThat(result.enabled()).isFalse();
        assertThat(result.username()).isEqualTo("disableduser");
    }
}
