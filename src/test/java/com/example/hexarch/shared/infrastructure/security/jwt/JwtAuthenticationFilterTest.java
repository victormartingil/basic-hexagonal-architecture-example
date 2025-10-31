package com.example.hexarch.shared.infrastructure.security.jwt;

import com.example.hexarch.shared.domain.security.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UNIT TEST - JwtAuthenticationFilter
 *
 * Tests unitarios para el filtro de autenticación JWT.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter - Unit Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        // Limpiar el contexto de seguridad antes de cada test
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should authenticate successfully with valid token")
    void shouldAuthenticateSuccessfully_withValidToken() throws ServletException, IOException {
        // GIVEN
        String token = "valid-token";
        String username = "testuser";
        List<Role> roles = List.of(Role.VIEWER);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(username);
        when(jwtTokenProvider.getRolesFromToken(token)).thenReturn(roles);

        // WHEN
        filter.doFilterInternal(request, response, filterChain);

        // THEN
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(username);
    }

    @Test
    @DisplayName("Should not authenticate when no token provided")
    void shouldNotAuthenticate_whenNoTokenProvided() throws ServletException, IOException {
        // GIVEN
        when(request.getHeader("Authorization")).thenReturn(null);

        // WHEN
        filter.doFilterInternal(request, response, filterChain);

        // THEN
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("Should not authenticate with invalid token")
    void shouldNotAuthenticate_withInvalidToken() throws ServletException, IOException {
        // GIVEN
        String token = "invalid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        // WHEN
        filter.doFilterInternal(request, response, filterChain);

        // THEN
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtTokenProvider, never()).getUsernameFromToken(anyString());
    }

    @Test
    @DisplayName("Should not authenticate with malformed header")
    void shouldNotAuthenticate_withMalformedHeader() throws ServletException, IOException {
        // GIVEN - Header sin "Bearer " prefix
        when(request.getHeader("Authorization")).thenReturn("invalid-header");

        // WHEN
        filter.doFilterInternal(request, response, filterChain);

        // THEN
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("Should handle exception gracefully")
    void shouldHandleExceptionGracefully() throws ServletException, IOException {
        // GIVEN - Token provider lanza excepción
        String token = "token-that-causes-exception";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenThrow(new RuntimeException("Token validation failed"));

        // WHEN - No debe lanzar excepción
        filter.doFilterInternal(request, response, filterChain);

        // THEN - El filtro continúa pero sin autenticación
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should authenticate with admin role")
    void shouldAuthenticate_withAdminRole() throws ServletException, IOException {
        // GIVEN
        String token = "admin-token";
        String username = "admin";
        List<Role> roles = List.of(Role.ADMIN);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(username);
        when(jwtTokenProvider.getRolesFromToken(token)).thenReturn(roles);

        // WHEN
        filter.doFilterInternal(request, response, filterChain);

        // THEN
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
            .hasSize(1)
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Should authenticate with multiple roles")
    void shouldAuthenticate_withMultipleRoles() throws ServletException, IOException {
        // GIVEN
        String token = "multi-role-token";
        String username = "poweruser";
        List<Role> roles = List.of(Role.VIEWER, Role.ADMIN);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(username);
        when(jwtTokenProvider.getRolesFromToken(token)).thenReturn(roles);

        // WHEN
        filter.doFilterInternal(request, response, filterChain);

        // THEN
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).hasSize(2);
    }
}
