package com.example.hexarch.shared.domain.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UNIT TEST - Role Enum
 *
 * Prueba el enum de roles del sistema.
 *
 * QUÉ SE TESTEA:
 * - Obtener authority con prefijo ROLE_ (para Spring Security)
 * - Conversión desde String (case-insensitive)
 * - Conversión desde String con prefijo ROLE_
 * - Manejo de valores inválidos
 * - Manejo de null y vacío
 */
@DisplayName("Role Enum Unit Tests")
class RoleTest {

    /**
     * TEST CASE 1: Debe devolver authority con prefijo ROLE_
     *
     * GIVEN: Un rol
     * WHEN: Se obtiene getAuthority()
     * THEN: Debe devolver el nombre con prefijo "ROLE_"
     */
    @ParameterizedTest(name = "{0} → ROLE_{0}")
    @ValueSource(strings = {"ADMIN", "MANAGER", "VIEWER", "SUPPLIER"})
    @DisplayName("Debe devolver authority con prefijo ROLE_")
    void shouldReturnAuthorityWithRolePrefix(String roleName) {
        // GIVEN
        Role role = Role.valueOf(roleName);

        // WHEN
        String authority = role.getAuthority();

        // THEN
        assertThat(authority).isEqualTo("ROLE_" + roleName);
    }

    /**
     * TEST CASE 2: Debe convertir string a Role (case-insensitive)
     *
     * GIVEN: Un string con nombre de rol en diferentes formatos
     * WHEN: Se convierte a Role
     * THEN: Debe devolver el Role correcto
     */
    @ParameterizedTest(name = "\"{0}\" → {1}")
    @CsvSource({
            "ADMIN, ADMIN",
            "admin, ADMIN",
            "Admin, ADMIN",
            "MANAGER, MANAGER",
            "manager, MANAGER",
            "VIEWER, VIEWER",
            "viewer, VIEWER",
            "SUPPLIER, SUPPLIER",
            "supplier, SUPPLIER"
    })
    @DisplayName("Debe convertir string a Role (case-insensitive)")
    void shouldConvertStringToRole(String input, Role expected) {
        // WHEN
        Role actual = Role.fromString(input);

        // THEN
        assertThat(actual).isEqualTo(expected);
    }

    /**
     * TEST CASE 3: Debe convertir string con prefijo ROLE_ a Role
     *
     * GIVEN: Un string con prefijo "ROLE_"
     * WHEN: Se convierte a Role
     * THEN: Debe ignorar el prefijo y devolver el Role correcto
     */
    @ParameterizedTest(name = "\"{0}\" → {1}")
    @CsvSource({
            "ROLE_ADMIN, ADMIN",
            "ROLE_MANAGER, MANAGER",
            "ROLE_VIEWER, VIEWER",
            "ROLE_SUPPLIER, SUPPLIER",
            "role_admin, ADMIN",  // Lowercase también
            "Role_Manager, MANAGER"
    })
    @DisplayName("Debe convertir string con prefijo ROLE_ a Role")
    void shouldConvertStringWithRolePrefixToRole(String input, Role expected) {
        // WHEN
        Role actual = Role.fromString(input);

        // THEN
        assertThat(actual).isEqualTo(expected);
    }

    /**
     * TEST CASE 4: Debe lanzar excepción para rol inválido
     *
     * GIVEN: Un string que no es un rol válido
     * WHEN: Se intenta convertir a Role
     * THEN: Debe lanzar IllegalArgumentException
     */
    @ParameterizedTest(name = "Inválido: \"{0}\"")
    @ValueSource(strings = {"INVALID", "USER", "GUEST", "SUPERADMIN", "ROLE_INVALID"})
    @DisplayName("Debe lanzar excepción para rol inválido")
    void shouldThrowExceptionForInvalidRole(String invalidRole) {
        // WHEN & THEN
        assertThatThrownBy(() -> Role.fromString(invalidRole))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid role");
    }

    /**
     * TEST CASE 5: Debe lanzar excepción para null o vacío
     *
     * GIVEN: Un string null o vacío
     * WHEN: Se intenta convertir a Role
     * THEN: Debe lanzar IllegalArgumentException
     */
    @ParameterizedTest(name = "Inválido: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})  // Espacios, tabs, newlines
    @DisplayName("Debe lanzar excepción para null, vacío o blank")
    void shouldThrowExceptionForNullOrEmptyRole(String invalidRole) {
        // WHEN & THEN
        assertThatThrownBy(() -> Role.fromString(invalidRole))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null or empty");
    }

    /**
     * TEST CASE 6: Debe tener 4 roles definidos
     *
     * GIVEN: El enum Role
     * WHEN: Se obtienen todos los valores
     * THEN: Debe haber exactamente 4 roles
     */
    @Test
    @DisplayName("Debe tener exactamente 4 roles definidos")
    void shouldHaveExactlyFourRoles() {
        // WHEN
        Role[] roles = Role.values();

        // THEN
        assertThat(roles).hasSize(4);
        assertThat(roles).containsExactly(
                Role.ADMIN,
                Role.MANAGER,
                Role.VIEWER,
                Role.SUPPLIER
        );
    }

    /**
     * TEST CASE 7: Debe mantener consistencia entre name() y getAuthority()
     *
     * GIVEN: Todos los roles
     * WHEN: Se comparan name() y getAuthority()
     * THEN: getAuthority() debe ser "ROLE_" + name()
     */
    @Test
    @DisplayName("Debe mantener consistencia entre name() y getAuthority()")
    void shouldMaintainConsistencyBetweenNameAndAuthority() {
        // GIVEN & WHEN & THEN
        for (Role role : Role.values()) {
            assertThat(role.getAuthority()).isEqualTo("ROLE_" + role.name());
        }
    }

    /**
     * TEST CASE 8: Debe ser convertible de authority a Role
     *
     * GIVEN: Un authority de Spring Security (con ROLE_)
     * WHEN: Se convierte de nuevo a Role
     * THEN: Debe devolver el Role original
     */
    @Test
    @DisplayName("Debe ser convertible de authority a Role (round-trip)")
    void shouldBeConvertibleFromAuthorityToRole() {
        // GIVEN & WHEN & THEN
        for (Role originalRole : Role.values()) {
            String authority = originalRole.getAuthority();  // "ROLE_ADMIN"
            Role convertedRole = Role.fromString(authority);
            assertThat(convertedRole).isEqualTo(originalRole);
        }
    }
}
