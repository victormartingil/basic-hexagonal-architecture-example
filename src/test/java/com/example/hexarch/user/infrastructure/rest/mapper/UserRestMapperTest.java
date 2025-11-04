package com.example.hexarch.user.infrastructure.rest.mapper;

import com.example.hexarch.user.application.model.CreateUserCommand;
import com.example.hexarch.user.application.model.UserResult;
import com.example.hexarch.user.infrastructure.adapter.input.rest.dto.generated.CreateUserRequest;
import com.example.hexarch.user.infrastructure.adapter.input.rest.dto.generated.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UNIT TEST - UserRestMapper
 *
 * Tests unitarios para el mapper de REST.
 * Verifica las conversiones entre DTOs REST y DTOs de aplicación.
 */
@DisplayName("UserRestMapper - Unit Tests")
class UserRestMapperTest {

    private UserRestMapper mapper;

    @BeforeEach
    void setUp() {
        // Usar la implementación generada por MapStruct
        mapper = Mappers.getMapper(UserRestMapper.class);
    }

    @Test
    @DisplayName("Should map CreateUserRequest to CreateUserCommand")
    void shouldMapRequestToCommand() {
        // GIVEN
        CreateUserRequest request = new CreateUserRequest(
            "testuser",
            "test@example.com"
        );

        // WHEN
        CreateUserCommand command = mapper.toCommand(request);

        // THEN
        assertThat(command).isNotNull();
        assertThat(command.username()).isEqualTo("testuser");
        assertThat(command.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should map UserResult to UserResponse")
    void shouldMapResultToResponse() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        UserResult result = new UserResult(
            userId,
            "testuser",
            "test@example.com",
            true,
            createdAt
        );

        // WHEN
        UserResponse response = mapper.toResponse(result);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getEnabled()).isTrue();
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getCreatedAt().toInstant()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Should convert Instant to OffsetDateTime in UTC")
    void shouldConvertInstantToOffsetDateTime() {
        // GIVEN
        Instant instant = Instant.parse("2024-01-15T10:30:00Z");

        // WHEN
        OffsetDateTime offsetDateTime = mapper.toOffsetDateTime(instant);

        // THEN
        assertThat(offsetDateTime).isNotNull();
        assertThat(offsetDateTime.toInstant()).isEqualTo(instant);
        assertThat(offsetDateTime.getOffset()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    @DisplayName("Should return null when Instant is null")
    void shouldReturnNull_whenInstantIsNull() {
        // WHEN
        OffsetDateTime result = mapper.toOffsetDateTime(null);

        // THEN
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should map request with minimum username length")
    void shouldMapRequest_withMinimumUsernameLength() {
        // GIVEN - Username mínimo de 3 caracteres
        CreateUserRequest request = new CreateUserRequest(
            "abc",
            "test@example.com"
        );

        // WHEN
        CreateUserCommand command = mapper.toCommand(request);

        // THEN
        assertThat(command.username()).isEqualTo("abc");
    }

    @Test
    @DisplayName("Should map request with maximum username length")
    void shouldMapRequest_withMaximumUsernameLength() {
        // GIVEN - Username máximo de 50 caracteres
        String longUsername = "a".repeat(50);
        CreateUserRequest request = new CreateUserRequest(
            longUsername,
            "test@example.com"
        );

        // WHEN
        CreateUserCommand command = mapper.toCommand(request);

        // THEN
        assertThat(command.username()).isEqualTo(longUsername);
        assertThat(command.username()).hasSize(50);
    }

    @Test
    @DisplayName("Should map result with disabled user")
    void shouldMapResult_withDisabledUser() {
        // GIVEN - Usuario deshabilitado
        UserResult result = new UserResult(
            UUID.randomUUID(),
            "disableduser",
            "disabled@example.com",
            false,  // Usuario deshabilitado
            Instant.now()
        );

        // WHEN
        UserResponse response = mapper.toResponse(result);

        // THEN
        assertThat(response.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should preserve all fields when mapping")
    void shouldPreserveAllFields_whenMapping() {
        // GIVEN - Result con todos los campos
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Instant createdAt = Instant.parse("2024-01-15T10:30:00Z");

        UserResult result = new UserResult(
            userId,
            "completeuser",
            "complete@example.com",
            true,
            createdAt
        );

        // WHEN
        UserResponse response = mapper.toResponse(result);

        // THEN - Verificar que todos los campos se mapearon correctamente
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo("completeuser");
        assertThat(response.getEmail()).isEqualTo("complete@example.com");
        assertThat(response.getEnabled()).isTrue();
        assertThat(response.getCreatedAt().toInstant()).isEqualTo(createdAt);
    }
}
