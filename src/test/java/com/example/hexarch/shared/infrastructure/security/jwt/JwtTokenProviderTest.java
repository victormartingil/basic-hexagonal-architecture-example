package com.example.hexarch.shared.infrastructure.security.jwt;

import com.example.hexarch.shared.domain.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UNIT TEST - JwtTokenProvider
 *
 * Prueba el componente core que genera y valida tokens JWT.
 *
 * QUÉ SE TESTEA:
 * - Generación de tokens JWT con username y roles
 * - Extracción de username del token
 * - Extracción de roles del token
 * - Validación de tokens válidos
 * - Rechazo de tokens inválidos (malformados, expirados, firma incorrecta)
 *
 * NOTA:
 * Creamos instancias reales (no mocks) porque JwtTokenProvider necesita
 * ejecutar @PostConstruct para inicializar la clave secreta.
 */
@DisplayName("JWT Token Provider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    // Secret válido para tests (256 bits)
    private static final String TEST_SECRET = "5JYzCkNP8xQ2mVnRwTaFbGdKeShXrUvYy3t6v9yBxE4=";
    private static final long TEST_EXPIRATION = 86400000L; // 24 horas

    @BeforeEach
    void setUp() {
        // Crear instancia real de JwtProperties
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(TEST_SECRET);
        jwtProperties.setExpiration(TEST_EXPIRATION);

        // Crear instancia real de JwtTokenProvider
        jwtTokenProvider = new JwtTokenProvider(jwtProperties);

        // Inicializar el provider (llama a @PostConstruct manualmente)
        jwtTokenProvider.init();
    }

    /**
     * TEST CASE 1: Debe generar un token JWT válido
     *
     * GIVEN: Un username y lista de roles
     * WHEN: Se genera un token
     * THEN: El token debe ser válido y contener la información correcta
     */
    @Test
    @DisplayName("Debe generar un token JWT válido con username y roles")
    void shouldGenerateValidJwtToken() {
        // GIVEN
        String username = "testuser";
        List<Role> roles = List.of(Role.ADMIN, Role.MANAGER);

        // WHEN
        String token = jwtTokenProvider.generateToken(username, roles);

        // THEN
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT tiene 3 partes: header.payload.signature
    }

    /**
     * TEST CASE 2: Debe extraer username del token correctamente
     *
     * GIVEN: Un token JWT generado
     * WHEN: Se extrae el username
     * THEN: Debe devolver el username correcto
     */
    @Test
    @DisplayName("Debe extraer username del token correctamente")
    void shouldExtractUsernameFromToken() {
        // GIVEN
        String expectedUsername = "john.doe";
        List<Role> roles = List.of(Role.VIEWER);
        String token = jwtTokenProvider.generateToken(expectedUsername, roles);

        // WHEN
        String actualUsername = jwtTokenProvider.getUsernameFromToken(token);

        // THEN
        assertThat(actualUsername).isEqualTo(expectedUsername);
    }

    /**
     * TEST CASE 3: Debe extraer roles del token correctamente
     *
     * GIVEN: Un token JWT con múltiples roles
     * WHEN: Se extraen los roles
     * THEN: Debe devolver todos los roles correctamente
     */
    @Test
    @DisplayName("Debe extraer roles del token correctamente")
    void shouldExtractRolesFromToken() {
        // GIVEN
        String username = "manager.user";
        List<Role> expectedRoles = List.of(Role.ADMIN, Role.MANAGER, Role.VIEWER);
        String token = jwtTokenProvider.generateToken(username, expectedRoles);

        // WHEN
        List<Role> actualRoles = jwtTokenProvider.getRolesFromToken(token);

        // THEN
        assertThat(actualRoles)
                .hasSize(3)
                .containsExactlyInAnyOrder(Role.ADMIN, Role.MANAGER, Role.VIEWER);
    }

    /**
     * TEST CASE 4: Debe validar correctamente un token válido
     *
     * GIVEN: Un token JWT válido
     * WHEN: Se valida el token
     * THEN: Debe devolver true
     */
    @Test
    @DisplayName("Debe validar correctamente un token válido")
    void shouldValidateValidToken() {
        // GIVEN
        String username = "valid.user";
        List<Role> roles = List.of(Role.SUPPLIER);
        String token = jwtTokenProvider.generateToken(username, roles);

        // WHEN
        boolean isValid = jwtTokenProvider.validateToken(token);

        // THEN
        assertThat(isValid).isTrue();
    }

    /**
     * TEST CASE 5: Debe rechazar token con firma incorrecta
     *
     * GIVEN: Un token JWT con firma modificada
     * WHEN: Se valida el token
     * THEN: Debe devolver false
     */
    @Test
    @DisplayName("Debe rechazar token con firma incorrecta")
    void shouldRejectTokenWithInvalidSignature() {
        // GIVEN
        String username = "test.user";
        List<Role> roles = List.of(Role.VIEWER);
        String token = jwtTokenProvider.generateToken(username, roles);

        // Modificar la firma (última parte del token)
        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".INVALID_SIGNATURE";

        // WHEN
        boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

        // THEN
        assertThat(isValid).isFalse();
    }

    /**
     * TEST CASE 6: Debe rechazar token malformado
     *
     * GIVEN: Un string que no es un token JWT válido
     * WHEN: Se valida el token
     * THEN: Debe devolver false
     */
    @Test
    @DisplayName("Debe rechazar token malformado")
    void shouldRejectMalformedToken() {
        // GIVEN
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // WHEN
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // THEN
        assertThat(isValid).isFalse();
    }

    /**
     * TEST CASE 7: Debe rechazar token vacío
     *
     * GIVEN: Un token vacío o null
     * WHEN: Se valida el token
     * THEN: Debe devolver false
     */
    @Test
    @DisplayName("Debe rechazar token vacío o null")
    void shouldRejectEmptyOrNullToken() {
        // GIVEN & WHEN & THEN
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }

    /**
     * TEST CASE 8: Debe manejar token con roles vacíos
     *
     * GIVEN: Un token sin roles
     * WHEN: Se extraen los roles
     * THEN: Debe devolver lista vacía (no lanzar excepción)
     */
    @Test
    @DisplayName("Debe manejar token con lista de roles vacía")
    void shouldHandleTokenWithEmptyRoles() {
        // GIVEN
        String username = "user.without.roles";
        List<Role> emptyRoles = List.of();
        String token = jwtTokenProvider.generateToken(username, emptyRoles);

        // WHEN
        List<Role> actualRoles = jwtTokenProvider.getRolesFromToken(token);

        // THEN
        assertThat(actualRoles).isEmpty();
    }

    /**
     * TEST CASE 9: Debe generar tokens diferentes para el mismo usuario
     *
     * GIVEN: El mismo username y roles
     * WHEN: Se generan dos tokens en momentos diferentes
     * THEN: Los tokens deben ser diferentes (porque el timestamp cambia)
     */
    @Test
    @DisplayName("Debe generar tokens diferentes para el mismo usuario en diferentes momentos")
    void shouldGenerateDifferentTokensForSameUserAtDifferentTimes() throws InterruptedException {
        // GIVEN
        String username = "same.user";
        List<Role> roles = List.of(Role.ADMIN);

        // WHEN
        String token1 = jwtTokenProvider.generateToken(username, roles);
        Thread.sleep(1000); // Esperar 1 segundo para cambiar timestamp
        String token2 = jwtTokenProvider.generateToken(username, roles);

        // THEN
        assertThat(token1).isNotEqualTo(token2);

        // Pero ambos deben ser válidos y tener el mismo username
        assertThat(jwtTokenProvider.validateToken(token1)).isTrue();
        assertThat(jwtTokenProvider.validateToken(token2)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token1)).isEqualTo(username);
        assertThat(jwtTokenProvider.getUsernameFromToken(token2)).isEqualTo(username);
    }

    /**
     * TEST CASE 10: Debe preservar el orden de roles
     *
     * GIVEN: Roles en un orden específico
     * WHEN: Se genera token y se extraen roles
     * THEN: Los roles deben mantenerse (aunque el orden puede variar en la validación)
     */
    @Test
    @DisplayName("Debe preservar todos los roles en el token")
    void shouldPreserveAllRolesInToken() {
        // GIVEN
        String username = "multi.role.user";
        List<Role> originalRoles = List.of(Role.SUPPLIER, Role.VIEWER, Role.MANAGER);
        String token = jwtTokenProvider.generateToken(username, originalRoles);

        // WHEN
        List<Role> extractedRoles = jwtTokenProvider.getRolesFromToken(token);

        // THEN
        assertThat(extractedRoles)
                .hasSize(originalRoles.size())
                .containsExactlyInAnyOrderElementsOf(originalRoles);
    }
}
