package com.example.hexarch.user.infrastructure.http.client;

import com.example.hexarch.user.application.port.ExternalUserApiClient.ExternalUserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * UNIT TEST - JsonPlaceholderClient
 * <p>
 * Test unitario para el cliente REST externo JsonPlaceholderClient.
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
 * Para tests más completos, ver {@code JsonPlaceholderClientIntegrationTest} que usa WireMock.
 * </p>
 *
 * @see <a href="https://wiremock.org/">WireMock</a>
 * @see <a href="https://spring.io/projects/spring-cloud-contract">Spring Cloud Contract</a>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JsonPlaceholderClient - Unit Tests")
class JsonPlaceholderClientTest {

    @Mock
    private RestClient restClient;

    private JsonPlaceholderClient jsonPlaceholderClient;

    @BeforeEach
    void setUp() {
        // Crear la instancia a testear con RestClient mockeado
        jsonPlaceholderClient = new JsonPlaceholderClient(restClient);
    }

    /**
     * TEST CASE 1: Verificar que el cliente se instancia correctamente
     * <p>
     * GIVEN: Un RestClient válido
     * WHEN: Se crea una instancia de JsonPlaceholderClient
     * THEN: La instancia se crea sin errores
     */
    @Test
    @DisplayName("Should create JsonPlaceholderClient successfully")
    void shouldCreateClientSuccessfully() {
        // GIVEN - RestClient mock (del setUp)

        // WHEN - Crear instancia (ya creada en setUp)
        JsonPlaceholderClient client = new JsonPlaceholderClient(restClient);

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
}
