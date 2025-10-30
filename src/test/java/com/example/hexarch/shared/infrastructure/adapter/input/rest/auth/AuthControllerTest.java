package com.example.hexarch.shared.infrastructure.adapter.input.rest.auth;

import com.example.hexarch.shared.domain.security.Role;
import com.example.hexarch.shared.infrastructure.security.jwt.JwtProperties;
import com.example.hexarch.shared.infrastructure.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UNIT TEST - AuthController
 *
 * Tests unitarios para el controller de autenticación.
 *
 * TESTING STRATEGY:
 * - @WebMvcTest: Solo carga el controller (no contexto completo)
 * - MockMvc: Simula requests HTTP sin levantar servidor
 * - @MockBean: Mockea dependencias (JwtTokenProvider, JwtProperties)
 *
 * QUÉ SE PRUEBA:
 * - Login exitoso con roles válidos
 * - Validación de request (campos requeridos)
 * - Manejo de roles inválidos
 * - Estructura de respuesta correcta
 *
 * NO SE PRUEBA (eso se hace en IntegrationTest):
 * - Seguridad real (filtros JWT)
 * - Base de datos
 * - Kafka
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)  // Desactivar filtros de seguridad para test unitario
@DisplayName("AuthController - Unit Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtProperties jwtProperties;

    /**
     * NESTED CLASS: Login Success Tests
     *
     * Prueba flujos exitosos de login
     */
    @Nested
    @DisplayName("POST /api/v1/auth/login - Success Cases")
    class LoginSuccessTests {

        @Test
        @DisplayName("Debe generar token JWT exitosamente con rol ADMIN")
        void shouldGenerateTokenSuccessfullyWithAdminRole() throws Exception {
            // GIVEN - Mock del token provider
            String mockToken = "eyJhbGciOiJIUzI1NiJ9.mock.token";
            Long expiresIn = 86400000L; // 24 horas

            when(jwtTokenProvider.generateToken(eq("johndoe"), any()))
                    .thenReturn(mockToken);
            when(jwtProperties.getExpiration()).thenReturn(expiresIn);

            // WHEN & THEN - POST /api/v1/auth/login
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "johndoe",
                                        "role": "ADMIN"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.token", is(mockToken)))
                    .andExpect(jsonPath("$.type", is("Bearer")))
                    .andExpect(jsonPath("$.username", is("johndoe")))
                    .andExpect(jsonPath("$.roles", hasSize(1)))
                    .andExpect(jsonPath("$.roles[0]", is("ADMIN")))
                    .andExpect(jsonPath("$.expiresIn", is(expiresIn.intValue())));

            // Verificar que se llamó al token provider
            verify(jwtTokenProvider).generateToken(eq("johndoe"), eq(List.of(Role.ADMIN)));
        }

        @Test
        @DisplayName("Debe generar token JWT con rol MANAGER")
        void shouldGenerateTokenWithManagerRole() throws Exception {
            // GIVEN
            String mockToken = "manager.token";
            when(jwtTokenProvider.generateToken(anyString(), any())).thenReturn(mockToken);
            when(jwtProperties.getExpiration()).thenReturn(86400000L);

            // WHEN & THEN
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "manager",
                                        "role": "MANAGER"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roles[0]", is("MANAGER")));
        }

        @Test
        @DisplayName("Debe generar token JWT con rol VIEWER")
        void shouldGenerateTokenWithViewerRole() throws Exception {
            // GIVEN
            String mockToken = "viewer.token";
            when(jwtTokenProvider.generateToken(anyString(), any())).thenReturn(mockToken);
            when(jwtProperties.getExpiration()).thenReturn(86400000L);

            // WHEN & THEN
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "viewer",
                                        "role": "VIEWER"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roles[0]", is("VIEWER")));
        }

        @Test
        @DisplayName("Debe generar token JWT con rol SUPPLIER")
        void shouldGenerateTokenWithSupplierRole() throws Exception {
            // GIVEN
            String mockToken = "supplier.token";
            when(jwtTokenProvider.generateToken(anyString(), any())).thenReturn(mockToken);
            when(jwtProperties.getExpiration()).thenReturn(86400000L);

            // WHEN & THEN
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "supplier",
                                        "role": "SUPPLIER"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roles[0]", is("SUPPLIER")));
        }

        @Test
        @DisplayName("Debe aceptar rol en minúsculas y convertirlo a mayúsculas")
        void shouldAcceptLowercaseRoleAndConvertToUppercase() throws Exception {
            // GIVEN
            String mockToken = "admin.token";
            when(jwtTokenProvider.generateToken(anyString(), any())).thenReturn(mockToken);
            when(jwtProperties.getExpiration()).thenReturn(86400000L);

            // WHEN & THEN - Enviar "admin" en minúsculas
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "johndoe",
                                        "role": "admin"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roles[0]", is("ADMIN")));
        }
    }

    /**
     * NESTED CLASS: Validation Tests
     *
     * Prueba validaciones de Bean Validation (@NotBlank, etc.)
     */
    @Nested
    @DisplayName("POST /api/v1/auth/login - Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Debe rechazar request sin username")
        void shouldRejectRequestWithoutUsername() throws Exception {
            // WHEN & THEN
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "role": "ADMIN"
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debe rechazar request sin role")
        void shouldRejectRequestWithoutRole() throws Exception {
            // WHEN & THEN
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "johndoe"
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debe rechazar request con username vacío")
        void shouldRejectRequestWithEmptyUsername() throws Exception {
            // WHEN & THEN
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "",
                                        "role": "ADMIN"
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debe rechazar request con role vacío")
        void shouldRejectRequestWithEmptyRole() throws Exception {
            // WHEN & THEN
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "johndoe",
                                        "role": ""
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * NESTED CLASS: Error Handling Tests
     *
     * Prueba manejo de errores (roles inválidos, etc.)
     */
    @Nested
    @DisplayName("POST /api/v1/auth/login - Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Debe rechazar role inválido")
        void shouldRejectInvalidRole() throws Exception {
            // WHEN & THEN
            // Nota: En test unitario sin GlobalExceptionHandler, devuelve 500
            // En integration test, devolvería 400 con el handler completo
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "johndoe",
                                        "role": "INVALID_ROLE"
                                    }
                                    """))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Debe rechazar role con espacios")
        void shouldRejectRoleWithSpaces() throws Exception {
            // WHEN & THEN
            // Nota: En test unitario sin GlobalExceptionHandler, devuelve 500
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "username": "johndoe",
                                        "role": "ADMIN USER"
                                    }
                                    """))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Debe rechazar request con Content-Type incorrecto")
        void shouldRejectRequestWithWrongContentType() throws Exception {
            // WHEN & THEN
            // Nota: En test unitario sin GlobalExceptionHandler, devuelve 500
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("plain text"))
                    .andExpect(status().is5xxServerError());
        }
    }
}
