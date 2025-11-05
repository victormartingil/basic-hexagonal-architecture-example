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
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UNIT TEST - ExternalUserApiRestClient
 * <p>
 * Test unitario para el cliente REST externo ExternalUserApiRestClient.
 * </p>
 *
 * <h3>Nota sobre Testing de RestClient:</h3>
 * <p>
 * RestClient (Spring 6) tiene una API fluida compleja que es difícil de mockear completamente.
 * Para este ejemplo didáctico, se recomienda usar uno de estos enfoques:
 * </p>
 * <ul>
 *   <li><strong>Test de Integración con WireMock</strong>: Simula un servidor HTTP real (recomendado)</li>
 *   <li><strong>Test de Integración real</strong>: Llama a la API real en un perfil de test</li>
 *   <li><strong>Contract Testing</strong>: Usa Spring Cloud Contract</li>
 * </ul>
 *
 * <p>
 * Este test demuestra que el adaptador se instancia correctamente y maneja excepciones.
 * Para tests más completos, ver {@code ExternalUserApiRestClientIntegrationTest} que usa WireMock.
 * </p>
 *
 * @see <a href="https://wiremock.org/">WireMock</a>
 * @see <a href="https://spring.io/projects/spring-cloud-contract">Spring Cloud Contract</a>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalUserApiRestClient - Unit Tests")
class ExternalUserApiRestClientTest {

    @Mock
    private RestClient restClient;

    private ExternalUserApiRestClient jsonPlaceholderClient;

    @BeforeEach
    void setUp() {
        // Crear la instancia a testear con RestClient mockeado
        jsonPlaceholderClient = new ExternalUserApiRestClient(restClient);
    }

    /**
     * TEST CASE 1: Verificar que el cliente se instancia correctamente
     * <p>
     * GIVEN: Un RestClient válido
     * WHEN: Se crea una instancia de ExternalUserApiRestClient
     * THEN: La instancia se crea sin errores
     */
    @Test
    @DisplayName("Should create ExternalUserApiRestClient successfully")
    void shouldCreateClientSuccessfully() {
        // GIVEN - RestClient mock (del setUp)

        // WHEN - Crear instancia (ya creada en setUp)
        ExternalUserApiRestClient client = new ExternalUserApiRestClient(restClient);

        // THEN - Verificar que se creó correctamente
        assertThat(client).isNotNull();
    }

    /**
     * TEST CASE 2: Manejo de excepciones en getUserById
     * <p>
     * GIVEN: RestClient lanza excepción
     * WHEN: Se llama a getUserById()
     * THEN: Retorna Optional.empty() (manejo graceful de errores)
     */
    @Test
    @DisplayName("Should return empty Optional when RestClient throws exception in GET")
    void shouldHandleExceptionGracefully_whenGetFails() {
        // GIVEN - Mock del fluent API de RestClient que lanza excepción
        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyInt()))
                .thenThrow(new RestClientException("Connection refused"));

        // WHEN - Llamar al método
        Optional<ExternalUserData> result = jsonPlaceholderClient.getUserById(1);

        // THEN - Verificar que retorna empty (error manejado gracefully)
        assertThat(result).isEmpty();

        // Verificar que se intentó hacer la llamada
        verify(restClient).get();
    }

    /**
     * TEST CASE 3: Manejo de excepciones en createExternalUser
     * <p>
     * GIVEN: RestClient lanza excepción
     * WHEN: Se llama a createExternalUser()
     * THEN: Lanza RuntimeException (en POST no manejamos gracefully)
     */
    @Test
    @DisplayName("Should throw RuntimeException when RestClient throws exception in POST")
    void shouldThrowException_whenPostFails() {
        // GIVEN - Mock del fluent API de RestClient que lanza excepción
        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenThrow(new RestClientException("Connection refused"));

        // WHEN / THEN - Verificar que lanza excepción
        assertThatThrownBy(() -> jsonPlaceholderClient.createExternalUser("John", "john@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create external user")
                .hasCauseInstanceOf(RestClientException.class);

        // Verificar que se intentó hacer la llamada
        verify(restClient).post();
    }

    /**
     * TEST CASE 4: Verificar que el método de instancia está disponible
     * <p>
     * Este test verifica que los métodos públicos están disponibles.
     * El testing completo de la lógica HTTP se hace en integration tests.
     * </p>
     */
    @Test
    @DisplayName("Should implement ExternalUserApiClient interface")
    void shouldImplementExternalUserApiClient() {
        // Verificar que implementa el port correctamente
        assertThat(jsonPlaceholderClient)
                .isInstanceOf(com.example.hexarch.user.application.port.ExternalUserApiClient.class)
                .isNotNull();
    }

    /**
     * TEST CASE 5: Happy path - getUserById successful
     * <p>
     * GIVEN: RestClient retorna un usuario exitosamente
     * WHEN: Se llama a getUserById
     * THEN: Retorna Optional con ExternalUserData mapeado
     * </p>
     */
    @Test
    @DisplayName("Should get user by ID successfully and map response")
    void shouldGetUserByIdSuccessfully() {
        // GIVEN - Mock de la respuesta exitosa
        ExternalUserApiResponse mockResponse = new ExternalUserApiResponse(
                1,
                "Leanne Graham",
                "Bret",
                "Sincere@april.biz",
                null,
                "1-770-736-8031 x56442",
                "hildegard.org",
                null
        );

        // Mock del fluent API de RestClient
        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyInt())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
        when(responseSpec.body(ExternalUserApiResponse.class)).thenReturn(mockResponse);

        // WHEN
        Optional<ExternalUserData> result = jsonPlaceholderClient.getUserById(1);

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

        verify(restClient).get();
    }

    /**
     * TEST CASE 6: getUserById returns null response
     * <p>
     * GIVEN: RestClient retorna null
     * WHEN: Se llama a getUserById
     * THEN: Retorna Optional.empty()
     * </p>
     */
    @Test
    @DisplayName("Should return empty Optional when response is null in GET")
    void shouldReturnEmptyWhenResponseIsNull() {
        // GIVEN - Mock que retorna null
        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyInt())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
        when(responseSpec.body(ExternalUserApiResponse.class)).thenReturn(null);

        // WHEN
        Optional<ExternalUserData> result = jsonPlaceholderClient.getUserById(1);

        // THEN
        assertThat(result).isEmpty();
    }

    /**
     * TEST CASE 7: Happy path - createExternalUser successful
     * <p>
     * GIVEN: RestClient crea usuario exitosamente
     * WHEN: Se llama a createExternalUser
     * THEN: Retorna ExternalUserData mapeado
     * </p>
     */
    @Test
    @DisplayName("Should create external user successfully and map response")
    void shouldCreateExternalUserSuccessfully() {
        // GIVEN - Mock de la respuesta exitosa
        ExternalUserApiResponse mockResponse = new ExternalUserApiResponse(
                101,
                "John Doe",
                "johndoe",
                "john@example.com",
                null,
                null,
                null,
                null
        );

        // Mock del fluent API de RestClient
        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ExternalUserApiCreateRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
        when(responseSpec.body(ExternalUserApiResponse.class)).thenReturn(mockResponse);

        // WHEN
        ExternalUserData result = jsonPlaceholderClient.createExternalUser("John Doe", "john@example.com");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(101);
        assertThat(result.name()).isEqualTo("John Doe");
        assertThat(result.email()).isEqualTo("john@example.com");

        verify(restClient).post();
    }

    /**
     * TEST CASE 8: createExternalUser returns null response
     * <p>
     * GIVEN: RestClient retorna null al crear usuario
     * WHEN: Se llama a createExternalUser
     * THEN: Lanza RuntimeException
     * </p>
     */
    @Test
    @DisplayName("Should throw RuntimeException when response is null in POST")
    void shouldThrowExceptionWhenCreateReturnsNull() {
        // GIVEN - Mock que retorna null
        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(ExternalUserApiCreateRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
        when(responseSpec.body(ExternalUserApiResponse.class)).thenReturn(null);

        // WHEN / THEN
        assertThatThrownBy(() -> jsonPlaceholderClient.createExternalUser("John", "john@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create external user: null response");
    }
}
