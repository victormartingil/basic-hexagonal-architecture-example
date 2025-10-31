package com.example.hexarch.user.infrastructure.adapter.output.persistence.mapper;

import com.example.hexarch.user.domain.model.User;
import com.example.hexarch.user.infrastructure.adapter.output.persistence.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UNIT TEST - UserEntityMapper
 *
 * Tests unitarios para el mapper entre User (dominio) y UserEntity (persistencia).
 */
@DisplayName("UserEntityMapper - Unit Tests")
class UserEntityMapperTest {

    private UserEntityMapper mapper;

    @BeforeEach
    void setUp() {
        // Usar la implementación generada por MapStruct
        mapper = Mappers.getMapper(UserEntityMapper.class);
    }

    @Test
    @DisplayName("Should map UserEntity to User domain model")
    void shouldMapEntityToDomain() {
        // GIVEN
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();

        UserEntity entity = new UserEntity(
            id,
            "testuser",
            "test@example.com",
            true,
            createdAt
        );

        // WHEN
        User user = mapper.toDomain(entity);

        // THEN
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getUsername().getValue()).isEqualTo("testuser");
        assertThat(user.getEmail().getValue()).isEqualTo("test@example.com");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Should map User domain model to UserEntity")
    void shouldMapDomainToEntity() {
        // GIVEN
        User user = User.create("testuser", "test@example.com");

        // WHEN
        UserEntity entity = mapper.toEntity(user);

        // THEN
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(user.getId());
        assertThat(entity.getUsername()).isEqualTo("testuser");
        assertThat(entity.getEmail()).isEqualTo("test@example.com");
        assertThat(entity.isEnabled()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(user.getCreatedAt());
    }

    @Test
    @DisplayName("Should return null when entity is null")
    void shouldReturnNull_whenEntityIsNull() {
        // WHEN
        User user = mapper.toDomain(null);

        // THEN
        assertThat(user).isNull();
    }

    @Test
    @DisplayName("Should map disabled user correctly")
    void shouldMapDisabledUser() {
        // GIVEN
        User user = User.create("testuser", "test@example.com");
        User disabledUser = user.disable();

        // WHEN
        UserEntity entity = mapper.toEntity(disabledUser);

        // THEN
        assertThat(entity.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should preserve all fields in bidirectional mapping")
    void shouldPreserveAllFields_inBidirectionalMapping() {
        // GIVEN - Crear un User original
        UUID originalId = UUID.randomUUID();
        Instant originalTime = Instant.parse("2024-01-15T10:30:00Z");

        User originalUser = User.reconstitute(
            originalId,
            "originaluser",
            "original@example.com",
            false,
            originalTime
        );

        // WHEN - Convertir a entity y luego de vuelta a domain
        UserEntity entity = mapper.toEntity(originalUser);
        User reconstructedUser = mapper.toDomain(entity);

        // THEN - Todos los campos deben preservarse
        assertThat(reconstructedUser.getId()).isEqualTo(originalId);
        assertThat(reconstructedUser.getUsername().getValue()).isEqualTo("originaluser");
        assertThat(reconstructedUser.getEmail().getValue()).isEqualTo("original@example.com");
        assertThat(reconstructedUser.isEnabled()).isFalse();
        assertThat(reconstructedUser.getCreatedAt()).isEqualTo(originalTime);
    }

    @Test
    @DisplayName("Should extract value objects correctly to strings")
    void shouldExtractValueObjects_toStrings() {
        // GIVEN
        User user = User.create("user123", "user123@example.com");

        // WHEN
        UserEntity entity = mapper.toEntity(user);

        // THEN - Los Value Objects deben convertirse a String
        assertThat(entity.getUsername()).isInstanceOf(String.class);
        assertThat(entity.getEmail()).isInstanceOf(String.class);
        assertThat(entity.getUsername()).isEqualTo("user123");
        assertThat(entity.getEmail()).isEqualTo("user123@example.com");
    }

    @Test
    @DisplayName("Should convert strings to value objects correctly")
    void shouldConvertStrings_toValueObjects() {
        // GIVEN
        UserEntity entity = new UserEntity(
            UUID.randomUUID(),
            "stringuser",
            "string@example.com",
            true,
            Instant.now()
        );

        // WHEN
        User user = mapper.toDomain(entity);

        // THEN - Los Strings deben convertirse a Value Objects
        assertThat(user.getUsername()).isNotNull();
        assertThat(user.getEmail()).isNotNull();
        assertThat(user.getUsername().getValue()).isEqualTo("stringuser");
        assertThat(user.getEmail().getValue()).isEqualTo("string@example.com");
    }
}
