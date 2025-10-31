package com.example.hexarch.user.infrastructure.adapter.output.persistence;

import com.example.hexarch.user.domain.model.User;
import com.example.hexarch.user.infrastructure.adapter.output.persistence.mapper.UserEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UNIT TEST - JpaUserRepositoryAdapter
 *
 * Tests unitarios para el adapter de repositorio JPA.
 * Usa mocks para aislar la lógica del adapter.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JpaUserRepositoryAdapter - Unit Tests")
class JpaUserRepositoryAdapterTest {

    @Mock
    private SpringDataUserRepository springDataRepository;

    @Mock
    private UserEntityMapper mapper;

    @Captor
    private ArgumentCaptor<UserEntity> entityCaptor;

    private JpaUserRepositoryAdapter adapter;

    private User domainUser;
    private UserEntity entity;
    private UUID userId;

    @BeforeEach
    void setUp() {
        adapter = new JpaUserRepositoryAdapter(springDataRepository, mapper);

        userId = UUID.randomUUID();

        // Mock domain user
        domainUser = User.reconstitute(
            userId,
            "testuser",
            "test@example.com",
            true,
            Instant.now()
        );

        // Mock entity
        entity = new UserEntity(
            userId,
            "testuser",
            "test@example.com",
            true,
            Instant.now()
        );
    }

    @Test
    @DisplayName("Should save user successfully")
    void shouldSaveUser_whenCalled() {
        // GIVEN
        when(mapper.toEntity(domainUser)).thenReturn(entity);
        when(springDataRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domainUser);

        // WHEN
        User result = adapter.save(domainUser);

        // THEN
        assertThat(result).isEqualTo(domainUser);
        verify(mapper).toEntity(domainUser);
        verify(springDataRepository).save(entity);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("Should find user by ID when exists")
    void shouldFindUserById_whenExists() {
        // GIVEN
        when(springDataRepository.findById(userId)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainUser);

        // WHEN
        Optional<User> result = adapter.findById(userId);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(domainUser);
        verify(springDataRepository).findById(userId);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("Should return empty when user not found by ID")
    void shouldReturnEmpty_whenUserNotFoundById() {
        // GIVEN
        when(springDataRepository.findById(userId)).thenReturn(Optional.empty());

        // WHEN
        Optional<User> result = adapter.findById(userId);

        // THEN
        assertThat(result).isEmpty();
        verify(springDataRepository).findById(userId);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should find user by username when exists")
    void shouldFindUserByUsername_whenExists() {
        // GIVEN
        String username = "testuser";
        when(springDataRepository.findByUsername(username)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainUser);

        // WHEN
        Optional<User> result = adapter.findByUsername(username);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(domainUser);
        verify(springDataRepository).findByUsername(username);
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("Should return empty when user not found by username")
    void shouldReturnEmpty_whenUserNotFoundByUsername() {
        // GIVEN
        String username = "nonexistent";
        when(springDataRepository.findByUsername(username)).thenReturn(Optional.empty());

        // WHEN
        Optional<User> result = adapter.findByUsername(username);

        // THEN
        assertThat(result).isEmpty();
        verify(springDataRepository).findByUsername(username);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should return true when username exists")
    void shouldReturnTrue_whenUsernameExists() {
        // GIVEN
        String username = "existinguser";
        when(springDataRepository.existsByUsername(username)).thenReturn(true);

        // WHEN
        boolean result = adapter.existsByUsername(username);

        // THEN
        assertThat(result).isTrue();
        verify(springDataRepository).existsByUsername(username);
    }

    @Test
    @DisplayName("Should return false when username does not exist")
    void shouldReturnFalse_whenUsernameDoesNotExist() {
        // GIVEN
        String username = "nonexistent";
        when(springDataRepository.existsByUsername(username)).thenReturn(false);

        // WHEN
        boolean result = adapter.existsByUsername(username);

        // THEN
        assertThat(result).isFalse();
        verify(springDataRepository).existsByUsername(username);
    }

    @Test
    @DisplayName("Should return true when email exists")
    void shouldReturnTrue_whenEmailExists() {
        // GIVEN
        String email = "existing@example.com";
        when(springDataRepository.existsByEmail(email)).thenReturn(true);

        // WHEN
        boolean result = adapter.existsByEmail(email);

        // THEN
        assertThat(result).isTrue();
        verify(springDataRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void shouldReturnFalse_whenEmailDoesNotExist() {
        // GIVEN
        String email = "nonexistent@example.com";
        when(springDataRepository.existsByEmail(email)).thenReturn(false);

        // WHEN
        boolean result = adapter.existsByEmail(email);

        // THEN
        assertThat(result).isFalse();
        verify(springDataRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("Should use mapper correctly when saving")
    void shouldUseMapperCorrectly_whenSaving() {
        // GIVEN
        when(mapper.toEntity(any(User.class))).thenReturn(entity);
        when(springDataRepository.save(any(UserEntity.class))).thenReturn(entity);
        when(mapper.toDomain(any(UserEntity.class))).thenReturn(domainUser);

        // WHEN
        adapter.save(domainUser);

        // THEN - Verificar el orden de llamadas
        verify(mapper).toEntity(domainUser);  // Primero convierte a entity
        verify(springDataRepository).save(entity);  // Luego guarda
        verify(mapper).toDomain(entity);  // Finalmente convierte de vuelta
    }
}
