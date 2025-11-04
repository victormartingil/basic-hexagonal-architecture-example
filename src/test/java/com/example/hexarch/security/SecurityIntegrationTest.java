package com.example.hexarch.security;

import com.example.hexarch.shared.domain.security.Role;
import com.example.hexarch.shared.infrastructure.security.jwt.JwtTokenProvider;
import com.example.hexarch.user.application.port.UserRepository;
import com.example.hexarch.user.domain.model.User;
import com.example.hexarch.user.infrastructure.persistence.SpringDataUserRepository;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * INTEGRATION TEST - JWT Security
 *
 * Test de integración que prueba la seguridad JWT end-to-end.
 *
 * QUÉ SE TESTEA:
 * - Endpoints públicos (sin autenticación)
 * - Endpoints protegidos con autenticación
 * - Autorización por rol (ADMIN, MANAGER pueden createUser, etc.)
 * - Rechazo de requests sin token (401 Unauthorized)
 * - Rechazo de requests con token inválido (401 Unauthorized)
 * - Rechazo de requests con rol insuficiente (403 Forbidden)
 *
 * ESCENARIO:
 * - POST /api/v1/users → Solo ADMIN o MANAGER
 * - GET /api/v1/users/{id} → Cualquier usuario autenticado
 * - GET /actuator/health → Público (sin autenticación)
 *
 * BEST PRACTICE:
 * En un escenario real de microservicios:
 * - Auth Service genera los tokens JWT (login endpoint)
 * - User Service (este) solo valida tokens
 * - Este test simula tokens generados por Auth Service
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("JWT Security Integration Tests")
class SecurityIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withStartupTimeout(java.time.Duration.ofSeconds(120));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpringDataUserRepository springDataUserRepository;

    private String adminToken;
    private String managerToken;
    private String viewerToken;
    private UUID existingUserId;

    @BeforeEach
    void setUp() {
        // Limpiar la BD antes de cada test para evitar violaciones de unicidad
        // IMPORTANTE: En CI/CD los tests pueden ejecutarse en paralelo o sin limpieza
        // Usamos SpringDataUserRepository directamente porque tiene el método deleteAll() de JpaRepository
        springDataUserRepository.deleteAll();

        // Generar tokens JWT para diferentes roles
        adminToken = "Bearer " + jwtTokenProvider.generateToken("admin.user", List.of(Role.ADMIN));
        managerToken = "Bearer " + jwtTokenProvider.generateToken("manager.user", List.of(Role.MANAGER));
        viewerToken = "Bearer " + jwtTokenProvider.generateToken("viewer.user", List.of(Role.VIEWER));

        // Crear usuario existente para tests de GET
        User existingUser = User.create("existing_user", "existing@test.com");
        User savedUser = userRepository.save(existingUser);
        existingUserId = savedUser.getId();
    }

    /**
     * TEST CASE 1: Endpoint público debe ser accesible sin autenticación
     */
    @Test
    @DisplayName("GET /actuator/health debe ser accesible sin token")
    void shouldAccessPublicEndpointWithoutAuthentication() throws Exception {
        // WHEN & THEN
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    /**
     * TEST CASE 2: POST /api/v1/users sin token debe devolver 401
     */
    @Test
    @DisplayName("POST /api/v1/users sin token debe devolver 401 Unauthorized")
    void shouldReturn401WhenCreatingUserWithoutToken() throws Exception {
        // GIVEN
        String requestBody = """
                {
                    "username": "newuser",
                    "email": "newuser@test.com"
                }
                """;

        // WHEN & THEN
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    /**
     * TEST CASE 3: POST /api/v1/users con token inválido debe devolver 401
     */
    @Test
    @DisplayName("POST /api/v1/users con token inválido debe devolver 401")
    void shouldReturn401WhenCreatingUserWithInvalidToken() throws Exception {
        // GIVEN
        String requestBody = """
                {
                    "username": "newuser",
                    "email": "newuser@test.com"
                }
                """;
        String invalidToken = "Bearer invalid.jwt.token";

        // WHEN & THEN
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", invalidToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    /**
     * TEST CASE 4: POST /api/v1/users con ADMIN debe funcionar (200 o 201)
     */
    @Test
    @DisplayName("POST /api/v1/users con rol ADMIN debe crear usuario exitosamente")
    void shouldCreateUserWithAdminRole() throws Exception {
        // GIVEN
        String requestBody = """
                {
                    "username": "admin_created_user",
                    "email": "admin.created@test.com"
                }
                """;

        // WHEN & THEN
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("admin_created_user"))
                .andExpect(jsonPath("$.email").value("admin.created@test.com"));
    }

    /**
     * TEST CASE 5: POST /api/v1/users con MANAGER debe funcionar
     */
    @Test
    @DisplayName("POST /api/v1/users con rol MANAGER debe crear usuario exitosamente")
    void shouldCreateUserWithManagerRole() throws Exception {
        // GIVEN
        String requestBody = """
                {
                    "username": "manager_created_user",
                    "email": "manager.created@test.com"
                }
                """;

        // WHEN & THEN
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("manager_created_user"));
    }

    /**
     * TEST CASE 6: POST /api/v1/users con VIEWER debe devolver 403 Forbidden
     */
    @Test
    @DisplayName("POST /api/v1/users con rol VIEWER debe devolver 403 Forbidden")
    void shouldReturn403WhenCreatingUserWithViewerRole() throws Exception {
        // GIVEN
        String requestBody = """
                {
                    "username": "viewer_attempt",
                    "email": "viewer@test.com"
                }
                """;

        // WHEN & THEN
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    /**
     * TEST CASE 7: GET /api/v1/users/{id} sin token debe devolver 401
     */
    @Test
    @DisplayName("GET /api/v1/users/{id} sin token debe devolver 401 Unauthorized")
    void shouldReturn401WhenGettingUserWithoutToken() throws Exception {
        // WHEN & THEN
        mockMvc.perform(get("/api/v1/users/" + existingUserId))
                .andExpect(status().isUnauthorized());
    }

    /**
     * TEST CASE 8: GET /api/v1/users/{id} con VIEWER debe funcionar
     */
    @Test
    @DisplayName("GET /api/v1/users/{id} con rol VIEWER debe obtener usuario")
    void shouldGetUserWithViewerRole() throws Exception {
        // WHEN & THEN
        mockMvc.perform(get("/api/v1/users/" + existingUserId)
                        .header("Authorization", viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingUserId.toString()))
                .andExpect(jsonPath("$.username").value("existing_user"));
    }

    /**
     * TEST CASE 9: GET /api/v1/users/{id} con ADMIN debe funcionar
     */
    @Test
    @DisplayName("GET /api/v1/users/{id} con rol ADMIN debe obtener usuario")
    void shouldGetUserWithAdminRole() throws Exception {
        // WHEN & THEN
        mockMvc.perform(get("/api/v1/users/" + existingUserId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("existing_user"));
    }

    /**
     * TEST CASE 10: Token con múltiples roles debe tener todos los permisos
     */
    @Test
    @DisplayName("Token con múltiples roles (ADMIN + MANAGER) debe funcionar")
    void shouldWorkWithMultipleRoles() throws Exception {
        // GIVEN
        String multiRoleToken = "Bearer " + jwtTokenProvider.generateToken(
                "multi.role.user",
                List.of(Role.ADMIN, Role.MANAGER, Role.VIEWER)
        );

        String requestBody = """
                {
                    "username": "multi_role_created",
                    "email": "multi@test.com"
                }
                """;

        // WHEN & THEN - Puede crear (ADMIN/MANAGER)
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", multiRoleToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        // WHEN & THEN - Puede leer (cualquier rol autenticado)
        mockMvc.perform(get("/api/v1/users/" + existingUserId)
                        .header("Authorization", multiRoleToken))
                .andExpect(status().isOk());
    }
}
