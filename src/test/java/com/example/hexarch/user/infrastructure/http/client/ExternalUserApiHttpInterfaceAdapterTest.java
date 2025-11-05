package com.example.hexarch.user.infrastructure.http.client;

import com.example.hexarch.user.application.port.ExternalUserApiClient.ExternalUserData;
import com.example.hexarch.user.infrastructure.http.client.dto.ExternalUserApiCreateRequest;
import com.example.hexarch.user.infrastructure.http.client.dto.ExternalUserApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * UNIT TEST - ExternalUserApiHttpInterfaceAdapter
 * <p>
 * Test unitario para el adaptador HTTP Interface que usa la opción MODERNA de Spring 6.
 * </p>
 *
 * <h3>🎯 Por qué HTTP Interface es la opción RECOMENDADA (2025):</h3>
 * <ul>
 *   <li>✅ Nativa de Spring Framework 6 (sin dependencias adicionales)</li>
 *   <li>✅ Recomendada oficialmente por Spring</li>
 *   <li>✅ Declarativa (menos código, menos errores)</li>
 *   <li>✅ Performance óptimo (usa RestClient directamente)</li>
 * </ul>
 *
 * <h3>Testing Strategy:</h3>
 * <p>
 * Para HTTP Interface, mockeamos la interface que Spring genera dinámicamente.
 * Esto es más simple que mockear la API fluida de RestClient.
 * </p>
 *
 * <h3>Tests Disponibles:</h3>
 * <ul>
 *   <li><strong>Unit Test</strong> (este archivo): Mock de la HTTP Interface</li>
 *   <li><strong>Integration Test</strong>: Llama a la API real de JSONPlaceholder</li>
 * </ul>
 *
 * @see ExternalUserApiHttpInterfaceAdapter
 * @see ExternalUserApiHttpInterface
 * @see ExternalUserApiHttpInterfaceAdapterRealIntegrationTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalUserApiHttpInterfaceAdapter - Unit Tests")
class ExternalUserApiHttpInterfaceAdapterTest {

    @Mock
    private ExternalUserApiHttpInterface httpInterface;

    private ExternalUserApiHttpInterfaceAdapter adapter;

    @BeforeEach
    void setUp() {
        // Crear el adaptador con la interface mockeada
        adapter = new ExternalUserApiHttpInterfaceAdapter(httpInterface);
    }

    /**
     * TEST CASE 1: Verificar que el adaptador se instancia correctamente
     * <p>
     * GIVEN: Una HTTP Interface válida
     * WHEN: Se crea una instancia de ExternalUserApiHttpInterfaceAdapter
     * THEN: La instancia se crea sin errores
     */
    @Test
    @DisplayName("Should create ExternalUserApiHttpInterfaceAdapter successfully")
    void shouldCreateAdapterSuccessfully() {
        // GIVEN - httpInterface mock (del setUp)

        // WHEN - Crear instancia (ya creada en setUp)
        ExternalUserApiHttpInterfaceAdapter newAdapter = new ExternalUserApiHttpInterfaceAdapter(httpInterface);

        // THEN - Verificar que se creó correctamente
        assertThat(newAdapter).isNotNull();
    }

    /**
     * TEST CASE 2: getUserById - Happy Path
     * <p>
     * GIVEN: Un userId válido y la API retorna un usuario
     * WHEN: Llamamos a getUserById
     * THEN: Retorna un Optional con el ExternalUserData mapeado correctamente
     */
    @Test
    @DisplayName("Should get user by ID successfully and map to ExternalUserData")
    void shouldGetUserByIdSuccessfully() {
        // GIVEN
        Integer userId = 1;
        ExternalUserApiResponse mockResponse = new ExternalUserApiResponse(
                1,
                "Leanne Graham",
                "Bret",
                "Sincere@april.biz",
                null,  // address
                "1-770-736-8031 x56442",
                "hildegard.org",
                null   // company
        );

        when(httpInterface.getUserById(userId)).thenReturn(mockResponse);

        // WHEN
        Optional<ExternalUserData> result = adapter.getUserById(userId);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get()).satisfies(userData -> {
            assertThat(userData.id()).isEqualTo(1);
            assertThat(userData.name()).isEqualTo("Leanne Graham");
            assertThat(userData.username()).isEqualTo("Bret");
            assertThat(userData.email()).isEqualTo("Sincere@april.biz");
            assertThat(userData.phone()).isEqualTo("1-770-736-8031 x56442");
            assertThat(userData.website()).isEqualTo("hildegard.org");
        });

        verify(httpInterface).getUserById(userId);
    }

    /**
     * TEST CASE 3: getUserById - Usuario no encontrado (404)
     * <p>
     * GIVEN: Un userId que no existe
     * WHEN: La API lanza RestClientException
     * THEN: Retorna Optional.empty() (manejo graceful de errores)
     */
    @Test
    @DisplayName("Should return empty Optional when user not found (404)")
    void shouldReturnEmptyWhenUserNotFound() {
        // GIVEN
        Integer userId = 999;
        when(httpInterface.getUserById(userId))
                .thenThrow(new RestClientException("404 Not Found"));

        // WHEN
        Optional<ExternalUserData> result = adapter.getUserById(userId);

        // THEN
        assertThat(result).isEmpty();
        verify(httpInterface).getUserById(userId);
    }

    /**
     * TEST CASE 4: getUserById - Error de conexión
     * <p>
     * GIVEN: Un userId válido pero la API está caída
     * WHEN: Llamamos a getUserById y la API lanza RestClientException
     * THEN: Retorna Optional.empty() (manejo graceful de errores)
     */
    @Test
    @DisplayName("Should handle connection errors gracefully and return empty")
    void shouldHandleConnectionErrorsGracefully() {
        // GIVEN
        Integer userId = 1;
        when(httpInterface.getUserById(userId))
                .thenThrow(new RestClientException("Connection refused"));

        // WHEN
        Optional<ExternalUserData> result = adapter.getUserById(userId);

        // THEN
        assertThat(result).isEmpty();
        verify(httpInterface).getUserById(userId);
    }

    /**
     * TEST CASE 5: getUserById - Response nulo
     * <p>
     * GIVEN: La API retorna null (caso edge)
     * WHEN: Llamamos a getUserById
     * THEN: Retorna Optional.empty()
     */
    @Test
    @DisplayName("Should return empty Optional when API returns null")
    void shouldReturnEmptyWhenApiReturnsNull() {
        // GIVEN
        Integer userId = 1;
        when(httpInterface.getUserById(userId)).thenReturn(null);

        // WHEN
        Optional<ExternalUserData> result = adapter.getUserById(userId);

        // THEN
        assertThat(result).isEmpty();
        verify(httpInterface).getUserById(userId);
    }

    /**
     * TEST CASE 6: createExternalUser - Happy Path
     * <p>
     * GIVEN: Datos válidos de un usuario a crear
     * WHEN: Llamamos a createExternalUser
     * THEN: Retorna el ExternalUserData del usuario creado
     */
    @Test
    @DisplayName("Should create external user successfully")
    void shouldCreateExternalUserSuccessfully() {
        // GIVEN
        String name = "John Doe";
        String email = "john@example.com";
        ExternalUserApiResponse mockResponse = new ExternalUserApiResponse(
                101,
                name,
                "johndoe",
                email,
                null,
                null,
                null,
                null
        );

        when(httpInterface.createUser(any(ExternalUserApiCreateRequest.class)))
                .thenReturn(mockResponse);

        // WHEN
        ExternalUserData result = adapter.createExternalUser(name, email);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(101);
        assertThat(result.name()).isEqualTo(name);
        assertThat(result.email()).isEqualTo(email);

        verify(httpInterface).createUser(any(ExternalUserApiCreateRequest.class));
    }

    /**
     * TEST CASE 7: createExternalUser - Error en la API
     * <p>
     * GIVEN: Datos de usuario pero la API falla
     * WHEN: Llamamos a createExternalUser y la API lanza excepción
     * THEN: Lanza RuntimeException (propaga el error)
     */
    @Test
    @DisplayName("Should throw RuntimeException when create user fails")
    void shouldThrowExceptionWhenCreateUserFails() {
        // GIVEN
        String name = "John Doe";
        String email = "john@example.com";
        when(httpInterface.createUser(any(ExternalUserApiCreateRequest.class)))
                .thenThrow(new RestClientException("500 Internal Server Error"));

        // WHEN & THEN
        assertThatThrownBy(() -> adapter.createExternalUser(name, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create external user");

        verify(httpInterface).createUser(any(ExternalUserApiCreateRequest.class));
    }

    /**
     * TEST CASE 8: createExternalUser - Response nulo
     * <p>
     * GIVEN: La API retorna null al crear usuario
     * WHEN: Llamamos a createExternalUser
     * THEN: Lanza RuntimeException con mensaje claro
     */
    @Test
    @DisplayName("Should throw RuntimeException when API returns null on create")
    void shouldThrowExceptionWhenApiReturnsNullOnCreate() {
        // GIVEN
        String name = "John Doe";
        String email = "john@example.com";
        when(httpInterface.createUser(any(ExternalUserApiCreateRequest.class)))
                .thenReturn(null);

        // WHEN & THEN
        assertThatThrownBy(() -> adapter.createExternalUser(name, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create external user: null response");

        verify(httpInterface).createUser(any(ExternalUserApiCreateRequest.class));
    }
}
