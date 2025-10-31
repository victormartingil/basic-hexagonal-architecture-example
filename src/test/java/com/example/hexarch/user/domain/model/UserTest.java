package com.example.hexarch.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UNIT TEST - User Domain Model
 *
 * Tests unitarios para el modelo de dominio User.
 */
@DisplayName("User - Domain Model Tests")
class UserTest {

    @Test
    @DisplayName("Should create new user with create factory method")
    void shouldCreateNewUser() {
        // WHEN
        User user = User.create("newuser", "new@example.com");

        // THEN
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername().getValue()).isEqualTo("newuser");
        assertThat(user.getEmail().getValue()).isEqualTo("new@example.com");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    @DisplayName("Should reconstitute existing user with reconstitute factory method")
    void shouldReconstituteExistingUser() {
        // GIVEN
        UUID existingId = UUID.randomUUID();
        Instant existingTime = Instant.parse("2024-01-01T00:00:00Z");

        // WHEN
        User user = User.reconstitute(
            existingId,
            "existinguser",
            "existing@example.com",
            false,
            existingTime
        );

        // THEN
        assertThat(user.getId()).isEqualTo(existingId);
        assertThat(user.getUsername().getValue()).isEqualTo("existinguser");
        assertThat(user.getEmail().getValue()).isEqualTo("existing@example.com");
        assertThat(user.isEnabled()).isFalse();
        assertThat(user.getCreatedAt()).isEqualTo(existingTime);
    }

    @Test
    @DisplayName("Should disable user")
    void shouldDisableUser() {
        // GIVEN
        User enabledUser = User.create("testuser", "test@example.com");
        assertThat(enabledUser.isEnabled()).isTrue();

        // WHEN
        User disabledUser = enabledUser.disable();

        // THEN
        assertThat(disabledUser.isEnabled()).isFalse();
        // Verificar que otros campos no cambiaron
        assertThat(disabledUser.getId()).isEqualTo(enabledUser.getId());
        assertThat(disabledUser.getUsername()).isEqualTo(enabledUser.getUsername());
        assertThat(disabledUser.getEmail()).isEqualTo(enabledUser.getEmail());
        assertThat(disabledUser.getCreatedAt()).isEqualTo(enabledUser.getCreatedAt());
    }

    @Test
    @DisplayName("Should enable user")
    void shouldEnableUser() {
        // GIVEN
        User user = User.create("testuser", "test@example.com");
        User disabledUser = user.disable();
        assertThat(disabledUser.isEnabled()).isFalse();

        // WHEN
        User enabledUser = disabledUser.enable();

        // THEN
        assertThat(enabledUser.isEnabled()).isTrue();
        // Verificar que otros campos no cambiaron
        assertThat(enabledUser.getId()).isEqualTo(disabledUser.getId());
        assertThat(enabledUser.getUsername()).isEqualTo(disabledUser.getUsername());
        assertThat(enabledUser.getEmail()).isEqualTo(disabledUser.getEmail());
    }

    @Test
    @DisplayName("Should be immutable - original not affected by disable")
    void shouldBeImmutable_whenDisabling() {
        // GIVEN
        User original = User.create("testuser", "test@example.com");
        boolean originalEnabledState = original.isEnabled();

        // WHEN
        User modified = original.disable();

        // THEN - Original no debe cambiar
        assertThat(original.isEnabled()).isEqualTo(originalEnabledState);
        assertThat(modified.isEnabled()).isNotEqualTo(originalEnabledState);
    }

    @Test
    @DisplayName("Should create users with unique IDs")
    void shouldCreateUsers_withUniqueIds() {
        // WHEN
        User user1 = User.create("user1", "user1@example.com");
        User user2 = User.create("user2", "user2@example.com");

        // THEN
        assertThat(user1.getId()).isNotEqualTo(user2.getId());
    }

    @Test
    @DisplayName("Should have equals and hashCode based on ID only")
    void shouldHaveEqualsAndHashCode_basedOnId() {
        // GIVEN
        UUID sharedId = UUID.randomUUID();
        User user1 = User.reconstitute(sharedId, "user1", "user1@example.com", true, Instant.now());
        User user2 = User.reconstitute(sharedId, "user2", "user2@example.com", false, Instant.now());

        // THEN - Aunque tengan diferentes datos, son iguales porque comparten ID
        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }
}
