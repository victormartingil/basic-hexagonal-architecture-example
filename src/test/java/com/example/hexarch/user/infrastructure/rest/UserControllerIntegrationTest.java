package com.example.hexarch.user.infrastructure.rest;

import com.example.hexarch.shared.domain.security.Role;
import com.example.hexarch.shared.infrastructure.security.jwt.JwtTokenProvider;
import com.example.hexarch.user.infrastructure.adapter.input.rest.dto.generated.CreateUserRequest;
import com.example.hexarch.user.infrastructure.adapter.input.rest.dto.generated.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * INTEGRATION TEST - UserController
 *
 * Test de integración que prueba el flujo completo:
 * Controller → Service → Repository → Database
 *
 * DIFERENCIA CON UNIT TEST:
 * - Unit Test: mockea dependencias, prueba lógica aislada
 * - Integration Test: usa dependencias reales (BD, Spring Context)
 *
 * TESTCONTAINERS:
 * - Levanta un contenedor Docker de PostgreSQL real para el test
 * - Asegura que el test es independiente del ambiente
 * - Limpia automáticamente después del test
 *
 * VENTAJAS:
 * - Prueba el flujo completo end-to-end
 * - Detecta problemas de integración
 * - Usa BD real (no mocks ni H2)
 * - Independiente del ambiente de desarrollo
 *
 * ANOTACIONES:
 * - @SpringBootTest: levanta el contexto completo de Spring
 * - @AutoConfigureMockMvc: configura MockMvc para hacer peticiones HTTP
 * - @Testcontainers: habilita Testcontainers
 * - @Container: define un contenedor Docker para el test
 */
@SpringBootTest  // Levanta el contexto completo de Spring Boot
@AutoConfigureMockMvc  // Configura MockMvc para hacer peticiones HTTP simuladas
@Testcontainers  // Habilita Testcontainers
@DisplayName("UserController - Integration Tests")
class UserControllerIntegrationTest {

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

    // MockMvc para hacer peticiones HTTP simuladas
    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper para serializar/deserializar JSON
    @Autowired
    private ObjectMapper objectMapper;

    // JWT Token Provider para generar tokens de autenticación
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // Token JWT para autenticar las peticiones
    private String authToken;

    /**
     * Configuración antes de cada test
     *
     * Genera un token JWT con rol ADMIN para autenticar las peticiones.
     * Los endpoints de User requieren autenticación JWT.
     */
    @BeforeEach
    void setUp() {
        // Generar token JWT con rol ADMIN (tiene todos los permisos)
        authToken = "Bearer " + jwtTokenProvider.generateToken("admin", List.of(Role.ADMIN));
    }

    /**
     * TEST CASE 1: Crear usuario exitosamente
     *
     * GIVEN: Una petición POST con datos válidos
     * WHEN: Se envía a /api/v1/users
     * THEN:
     *   - Status: 201 CREATED
     *   - Body: UserResponse con los datos del usuario creado
     *   - El usuario se guardó en la BD (verificado por Flyway + JPA)
     */
    @Test
    @DisplayName("POST /api/v1/users - Debe crear usuario exitosamente con datos válidos")
    void shouldCreateUser_whenValidRequest() throws Exception {
        // GIVEN - Crear request con datos válidos
        CreateUserRequest request = new CreateUserRequest(
                "johndoe",
                "john@example.com"
        );

        // WHEN - Enviar POST request
        MvcResult result = mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", authToken)  // JWT Token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // THEN - Verificar respuesta HTTP
                .andExpect(status().isCreated())  // 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn();

        // Verificar el body completo
        String responseBody = result.getResponse().getContentAsString();
        UserResponse response = objectMapper.readValue(responseBody, UserResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("johndoe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getEnabled()).isTrue();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getCreatedAt()).isNotNull();
    }

    /**
     * TEST CASE 2: Error - Username vacío
     *
     * GIVEN: Una petición POST con username vacío
     * WHEN: Se envía a /api/v1/users
     * THEN:
     *   - Status: 400 BAD REQUEST
     *   - Body: ErrorResponse con detalles del error de validación
     */
    @Test
    @DisplayName("POST /api/v1/users - Debe retornar 400 cuando username está vacío")
    void shouldReturn400_whenUsernameIsEmpty() throws Exception {
        // GIVEN - Request con username vacío
        CreateUserRequest request = new CreateUserRequest(
                "",  // username vacío
                "john@example.com"
        );

        // WHEN & THEN
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())  // 400 Bad Request
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.details.username").exists());
    }

    /**
     * TEST CASE 3: Error - Username muy corto
     *
     * GIVEN: Una petición POST con username de menos de 3 caracteres
     * WHEN: Se envía a /api/v1/users
     * THEN: Status 400 BAD REQUEST
     */
    @Test
    @DisplayName("POST /api/v1/users - Debe retornar 400 cuando username es muy corto")
    void shouldReturn400_whenUsernameIsTooShort() throws Exception {
        // GIVEN - Username de solo 2 caracteres
        CreateUserRequest request = new CreateUserRequest(
                "ab",  // solo 2 caracteres
                "john@example.com"
        );

        // WHEN & THEN
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.username").exists());
    }

    /**
     * TEST CASE 4: Error - Email inválido
     *
     * GIVEN: Una petición POST con email sin formato válido
     * WHEN: Se envía a /api/v1/users
     * THEN: Status 400 BAD REQUEST
     */
    @Test
    @DisplayName("POST /api/v1/users - Debe retornar 400 cuando email es inválido")
    void shouldReturn400_whenEmailIsInvalid() throws Exception {
        // GIVEN - Email sin formato válido
        CreateUserRequest request = new CreateUserRequest(
                "johndoe",
                "invalid-email"  // sin @
        );

        // WHEN & THEN
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.email").exists());
    }

    /**
     * TEST CASE 5: Error - Usuario duplicado
     *
     * GIVEN: Se crea un usuario y luego se intenta crear otro con el mismo username
     * WHEN: Se envía la segunda petición
     * THEN: Status 409 CONFLICT
     */
    @Test
    @DisplayName("POST /api/v1/users - Debe retornar 409 cuando username ya existe")
    void shouldReturn409_whenUsernameAlreadyExists() throws Exception {
        // GIVEN - Crear primer usuario
        CreateUserRequest firstRequest = new CreateUserRequest(
                "duplicateuser",
                "first@example.com"
        );

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Intentar crear segundo usuario con mismo username
        CreateUserRequest secondRequest = new CreateUserRequest(
                "duplicateuser",  // mismo username
                "second@example.com"  // diferente email
        );

        // WHEN & THEN - Debe fallar con 409 Conflict
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict())  // 409 Conflict
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.errorCode").value("USER_006"))
                .andExpect(jsonPath("$.message").value("User with username 'duplicateuser' already exists"));
    }

    /**
     * TEST CASE 6: Error - Email duplicado
     *
     * GIVEN: Se crea un usuario y luego se intenta crear otro con el mismo email
     * WHEN: Se envía la segunda petición
     * THEN: Status 409 CONFLICT
     */
    @Test
    @DisplayName("POST /api/v1/users - Debe retornar 409 cuando email ya existe")
    void shouldReturn409_whenEmailAlreadyExists() throws Exception {
        // GIVEN - Crear primer usuario
        CreateUserRequest firstRequest = new CreateUserRequest(
                "firstuser",
                "duplicate@example.com"
        );

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Intentar crear segundo usuario con mismo email
        CreateUserRequest secondRequest = new CreateUserRequest(
                "seconduser",  // diferente username
                "duplicate@example.com"  // mismo email
        );

        // WHEN & THEN - Debe fallar con 409 Conflict
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.errorCode").value("USER_006"));
    }

    // ========================================
    // TESTS PARA GET /api/v1/users/{id}
    // ========================================

    /**
     * TEST CASE 7: Obtener usuario exitosamente por ID (CQRS - Query)
     *
     * GIVEN: Un usuario creado previamente en la BD
     * WHEN: Se envía GET a /api/v1/users/{id}
     * THEN:
     *   - Status: 200 OK
     *   - Body: UserResponse con los datos del usuario
     *   - Los datos coinciden con el usuario creado
     */
    @Test
    @DisplayName("GET /api/v1/users/{id} - Debe retornar usuario exitosamente cuando existe")
    void shouldGetUser_whenUserExists() throws Exception {
        // GIVEN - Crear un usuario primero
        CreateUserRequest createRequest = new CreateUserRequest(
                "getuser_test",
                "getuser@example.com"
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        // Extraer el ID del usuario creado
        String createResponseBody = createResult.getResponse().getContentAsString();
        UserResponse createdUser = objectMapper.readValue(createResponseBody, UserResponse.class);
        UUID userId = createdUser.getId();

        // WHEN - Obtener el usuario por ID
        MvcResult getResult = mockMvc.perform(get("/api/v1/users/{id}", userId)
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON))

                // THEN - Verificar respuesta HTTP
                .andExpect(status().isOk())  // 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("getuser_test"))
                .andExpect(jsonPath("$.email").value("getuser@example.com"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn();

        // Verificar el body completo
        String getResponseBody = getResult.getResponse().getContentAsString();
        UserResponse retrievedUser = objectMapper.readValue(getResponseBody, UserResponse.class);

        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getId()).isEqualTo(userId);
        assertThat(retrievedUser.getUsername()).isEqualTo("getuser_test");
        assertThat(retrievedUser.getEmail()).isEqualTo("getuser@example.com");
        assertThat(retrievedUser.getEnabled()).isTrue();
        assertThat(retrievedUser.getCreatedAt()).isNotNull();

        // Verificar que los datos son idénticos entre POST y GET
        assertThat(retrievedUser.getId()).isEqualTo(createdUser.getId());
        assertThat(retrievedUser.getUsername()).isEqualTo(createdUser.getUsername());
        assertThat(retrievedUser.getEmail()).isEqualTo(createdUser.getEmail());
        assertThat(retrievedUser.getEnabled()).isEqualTo(createdUser.getEnabled());
        // Comparar timestamps con tolerancia de 1 segundo debido a pérdida de precisión en serialización JSON
        assertThat(retrievedUser.getCreatedAt())
                .isCloseTo(createdUser.getCreatedAt(), within(1, ChronoUnit.SECONDS));
    }

    /**
     * TEST CASE 8: Error - Usuario no encontrado (404)
     *
     * GIVEN: Un ID de usuario que NO existe en la BD
     * WHEN: Se envía GET a /api/v1/users/{id}
     * THEN:
     *   - Status: 404 NOT FOUND
     *   - Body: ErrorResponse con detalles del error
     */
    @Test
    @DisplayName("GET /api/v1/users/{id} - Debe retornar 404 cuando usuario no existe")
    void shouldReturn404_whenUserDoesNotExist() throws Exception {
        // GIVEN - Un ID que NO existe
        UUID nonExistingId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/users/{id}", nonExistingId)
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())  // 404 Not Found
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("USER_404"))
                .andExpect(jsonPath("$.message").value("User with ID '" + nonExistingId + "' not found"));
    }

    /**
     * TEST CASE 9: Error - ID con formato inválido (400)
     *
     * GIVEN: Un ID con formato inválido (no es UUID)
     * WHEN: Se envía GET a /api/v1/users/{id}
     * THEN:
     *   - Status: 400 BAD REQUEST
     *   - Spring Boot maneja automáticamente el error de conversión
     */
    @Test
    @DisplayName("GET /api/v1/users/{id} - Debe retornar 400 cuando ID tiene formato inválido")
    void shouldReturn400_whenIdFormatIsInvalid() throws Exception {
        // GIVEN - Un ID con formato inválido
        String invalidId = "not-a-valid-uuid";

        // WHEN & THEN
        mockMvc.perform(get("/api/v1/users/{id}", invalidId)
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());  // 400 Bad Request
    }

    /**
     * TEST CASE 10: CQRS - Verificar que GET no modifica datos
     *
     * GIVEN: Un usuario creado
     * WHEN: Se ejecuta GET múltiples veces
     * THEN:
     *   - Todos los GET retornan los mismos datos
     *   - Los datos no cambian entre llamadas (idempotencia)
     *   - Demuestra que Query es read-only
     */
    @Test
    @DisplayName("GET /api/v1/users/{id} - Debe ser idempotente (CQRS Query)")
    void shouldBeIdempotent_whenCalledMultipleTimes() throws Exception {
        // GIVEN - Crear usuario
        CreateUserRequest createRequest = new CreateUserRequest(
                "idempotent_test",
                "idempotent@example.com"
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponseBody = createResult.getResponse().getContentAsString();
        UserResponse createdUser = objectMapper.readValue(createResponseBody, UserResponse.class);
        UUID userId = createdUser.getId();

        // WHEN - Ejecutar GET múltiples veces
        UserResponse firstGet = objectMapper.readValue(
                mockMvc.perform(get("/api/v1/users/{id}", userId)
                                .header("Authorization", authToken))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(),
                UserResponse.class
        );

        UserResponse secondGet = objectMapper.readValue(
                mockMvc.perform(get("/api/v1/users/{id}", userId)
                                .header("Authorization", authToken))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(),
                UserResponse.class
        );

        UserResponse thirdGet = objectMapper.readValue(
                mockMvc.perform(get("/api/v1/users/{id}", userId)
                                .header("Authorization", authToken))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(),
                UserResponse.class
        );

        // THEN - Todos los resultados son idénticos (idempotencia)
        assertThat(firstGet).isNotNull();
        assertThat(secondGet).isNotNull();
        assertThat(thirdGet).isNotNull();

        // Verificar que todos retornan exactamente los mismos datos
        assertThat(firstGet.getId()).isEqualTo(secondGet.getId()).isEqualTo(thirdGet.getId());
        assertThat(firstGet.getUsername()).isEqualTo(secondGet.getUsername()).isEqualTo(thirdGet.getUsername());
        assertThat(firstGet.getEmail()).isEqualTo(secondGet.getEmail()).isEqualTo(thirdGet.getEmail());
        assertThat(firstGet.getEnabled()).isEqualTo(secondGet.getEnabled()).isEqualTo(thirdGet.getEnabled());
        // Verificar createdAt (comparar Instants para ser más preciso)
        assertThat(firstGet.getCreatedAt().toInstant())
                .isEqualTo(secondGet.getCreatedAt().toInstant())
                .isEqualTo(thirdGet.getCreatedAt().toInstant());
    }
}
