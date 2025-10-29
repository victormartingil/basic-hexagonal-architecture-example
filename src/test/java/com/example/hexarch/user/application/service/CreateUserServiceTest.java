package com.example.hexarch.user.application.service;

import com.example.hexarch.user.application.port.input.CreateUserCommand;
import com.example.hexarch.user.application.port.input.UserResult;
import com.example.hexarch.user.application.port.output.UserEventPublisher;
import com.example.hexarch.user.application.port.output.UserRepository;
import com.example.hexarch.user.domain.event.UserCreatedEvent;
import com.example.hexarch.user.domain.exception.UserAlreadyExistsException;
import com.example.hexarch.user.domain.exception.ValidationException;
import com.example.hexarch.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UNIT TEST - CreateUserService
 *
 * Test unitario para el servicio CreateUserService.
 * Verifica la lógica de negocio de forma aislada, mockeando las dependencias.
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
@DisplayName("CreateUserService - Unit Tests")
class CreateUserServiceTest {

    // Mocks de las dependencias
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventPublisher userEventPublisher;

    // Instancia a testear (con mocks inyectados)
    @InjectMocks
    private CreateUserService createUserService;

    // Datos de prueba
    private CreateUserCommand validCommand;

    /**
     * Se ejecuta antes de cada test
     * Prepara los datos de prueba comunes
     */
    @BeforeEach
    void setUp() {
        // Crear un command válido para usar en los tests
        validCommand = new CreateUserCommand("johndoe", "john@example.com");
    }

    /**
     * TEST CASE 1: Caso exitoso
     *
     * GIVEN: Un command válido con username y email únicos
     * WHEN: Se ejecuta el caso de uso
     * THEN:
     *   - Se crea y guarda el usuario
     *   - Se publica el evento UserCreatedEvent
     *   - Se retorna UserResult con los datos correctos
     */
    @Test
    @DisplayName("Debe crear usuario exitosamente cuando los datos son válidos")
    void shouldCreateUser_whenValidCommand() {
        // GIVEN - Configurar el comportamiento de los mocks

        // Mock: el username NO existe
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);

        // Mock: el email NO existe
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);

        // Mock: guardar retorna el usuario guardado
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            // Retorna el mismo usuario que se le pasó
            return invocation.getArgument(0);
        });

        // WHEN - Ejecutar el método a testear
        UserResult result = createUserService.execute(validCommand);

        // THEN - Verificar el resultado

        // 1. Verificar que el resultado no es nulo y tiene datos correctos
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("johndoe");
        assertThat(result.email()).isEqualTo("john@example.com");
        assertThat(result.enabled()).isTrue();
        assertThat(result.id()).isNotNull();
        assertThat(result.createdAt()).isNotNull();

        // 2. Verificar que se verificó la existencia del username
        verify(userRepository).existsByUsername("johndoe");

        // 3. Verificar que se verificó la existencia del email
        verify(userRepository).existsByEmail("john@example.com");

        // 4. Verificar que se guardó el usuario
        verify(userRepository).save(any(User.class));

        // 5. Verificar que se publicó el evento
        // Capturamos el evento publicado para verificar su contenido
        ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(userEventPublisher).publish(eventCaptor.capture());

        UserCreatedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.username()).isEqualTo("johndoe");
        assertThat(publishedEvent.email()).isEqualTo("john@example.com");
        assertThat(publishedEvent.userId()).isNotNull();
    }

    /**
     * TEST CASE 2: Error - Username ya existe
     *
     * GIVEN: Un command con un username que ya existe
     * WHEN: Se ejecuta el caso de uso
     * THEN: Se lanza UserAlreadyExistsException
     */
    @Test
    @DisplayName("Debe lanzar UserAlreadyExistsException cuando el username ya existe")
    void shouldThrowUserAlreadyExistsException_whenUsernameExists() {
        // GIVEN - El username YA existe
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        // WHEN & THEN - Verificar que se lanza la excepción
        assertThatThrownBy(() -> createUserService.execute(validCommand))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("johndoe");

        // Verificar que NO se intentó guardar ni publicar evento
        verify(userRepository, never()).save(any(User.class));
        verify(userEventPublisher, never()).publish(any(UserCreatedEvent.class));
    }

    /**
     * TEST CASE 3: Error - Email ya existe
     *
     * GIVEN: Un command con un email que ya existe
     * WHEN: Se ejecuta el caso de uso
     * THEN: Se lanza UserAlreadyExistsException
     */
    @Test
    @DisplayName("Debe lanzar UserAlreadyExistsException cuando el email ya existe")
    void shouldThrowUserAlreadyExistsException_whenEmailExists() {
        // GIVEN
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);  // username OK
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);  // email YA existe

        // WHEN & THEN
        assertThatThrownBy(() -> createUserService.execute(validCommand))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("john@example.com");

        // Verificar que NO se intentó guardar ni publicar evento
        verify(userRepository, never()).save(any(User.class));
        verify(userEventPublisher, never()).publish(any(UserCreatedEvent.class));
    }

    /**
     * TEST CASE 4: Error - Username vacío (validación de dominio)
     *
     * GIVEN: Un command con username vacío
     * WHEN: Se ejecuta el caso de uso
     * THEN: Se lanza ValidationException
     */
    @Test
    @DisplayName("Debe lanzar ValidationException cuando el username está vacío")
    void shouldThrowValidationException_whenUsernameIsEmpty() {
        // GIVEN - Command con username vacío
        CreateUserCommand invalidCommand = new CreateUserCommand("", "john@example.com");

        when(userRepository.existsByUsername("")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> createUserService.execute(invalidCommand))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Username no puede estar vacío");

        // Verificar que NO se guardó ni publicó evento
        verify(userRepository, never()).save(any(User.class));
        verify(userEventPublisher, never()).publish(any(UserCreatedEvent.class));
    }

    /**
     * TEST CASE 5: Error - Username muy corto (validación de dominio)
     *
     * GIVEN: Un command con username de menos de 3 caracteres
     * WHEN: Se ejecuta el caso de uso
     * THEN: Se lanza ValidationException
     */
    @Test
    @DisplayName("Debe lanzar ValidationException cuando el username es muy corto")
    void shouldThrowValidationException_whenUsernameIsTooShort() {
        // GIVEN - Username de solo 2 caracteres
        CreateUserCommand invalidCommand = new CreateUserCommand("ab", "john@example.com");

        when(userRepository.existsByUsername("ab")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> createUserService.execute(invalidCommand))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("debe tener al menos 3 caracteres");
    }

    /**
     * TEST CASE 6: Error - Email inválido (validación de dominio)
     *
     * GIVEN: Un command con email con formato inválido
     * WHEN: Se ejecuta el caso de uso
     * THEN: Se lanza ValidationException
     */
    @Test
    @DisplayName("Debe lanzar ValidationException cuando el email es inválido")
    void shouldThrowValidationException_whenEmailIsInvalid() {
        // GIVEN - Email sin formato válido
        CreateUserCommand invalidCommand = new CreateUserCommand("johndoe", "invalid-email");

        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("invalid-email")).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> createUserService.execute(invalidCommand))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email no tiene un formato válido");
    }
}
