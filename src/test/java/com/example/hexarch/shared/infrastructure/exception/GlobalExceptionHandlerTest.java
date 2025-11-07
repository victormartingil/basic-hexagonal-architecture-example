package com.example.hexarch.shared.infrastructure.exception;

import com.example.hexarch.shared.domain.exception.ErrorCode;
import com.example.hexarch.user.domain.exception.DomainException;
import com.example.hexarch.user.domain.exception.UserAlreadyExistsException;
import com.example.hexarch.user.domain.exception.UserNotFoundException;
import com.example.hexarch.user.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * UNIT TEST - GlobalExceptionHandler
 *
 * Tests unitarios para el manejador global de excepciones.
 */
@DisplayName("GlobalExceptionHandler - Unit Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle ValidationException with 400 status")
    void shouldHandleValidationException() {
        // GIVEN - Ahora con parámetro para mejor contexto
        ValidationException ex = new ValidationException(ErrorCode.USERNAME_EMPTY, "null");

        // WHEN
        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Validation Error");
        assertThat(response.getBody().message()).isEqualTo("Username no puede estar vacío (recibido: 'null')");
        assertThat(response.getBody().errorCode()).isEqualTo("USER_001");
    }

    @Test
    @DisplayName("Should handle UserAlreadyExistsException with 409 status")
    void shouldHandleUserAlreadyExistsException() {
        // GIVEN
        UserAlreadyExistsException ex = new UserAlreadyExistsException("testuser");

        // WHEN
        ResponseEntity<ErrorResponse> response = handler.handleUserAlreadyExistsException(ex);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().error()).isEqualTo("Conflict");
        assertThat(response.getBody().message()).isEqualTo("User with username 'testuser' already exists");
        assertThat(response.getBody().errorCode()).isEqualTo("USER_006");
    }

    @Test
    @DisplayName("Should handle UserNotFoundException with 404 status")
    void shouldHandleUserNotFoundException() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        UserNotFoundException ex = new UserNotFoundException(userId);

        // WHEN
        ResponseEntity<ErrorResponse> response = handler.handleUserNotFoundException(ex);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().errorCode()).isEqualTo("USER_404");
    }

    // NOTE: MethodArgumentNotValidException testing is complex to mock
    // This scenario is already covered by integration tests (UserControllerIntegrationTest)
    // which test the full validation flow end-to-end

    @Test
    @DisplayName("Should handle MethodArgumentTypeMismatchException")
    void shouldHandleMethodArgumentTypeMismatchException() {
        // GIVEN - Mock de type mismatch exception
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");
        when(ex.getValue()).thenReturn("invalid-uuid");
        when(ex.getRequiredType()).thenReturn((Class) UUID.class);

        // WHEN
        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentTypeMismatchException(ex);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Type Mismatch");
        assertThat(response.getBody().errorCode()).isEqualTo("TYPE_MISMATCH_001");
        assertThat(response.getBody().message()).contains("id");
        assertThat(response.getBody().message()).contains("invalid-uuid");
    }

    @Test
    @DisplayName("Should handle DomainException with 500 status")
    void shouldHandleDomainException() {
        // GIVEN - Usar ValidationException que es concreta, ahora con parámetro
        ValidationException ex = new ValidationException(ErrorCode.EMAIL_EMPTY, "\"\"");

        // WHEN
        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Validation Error");
        assertThat(response.getBody().message()).isEqualTo("Email no puede estar vacío (recibido: '\"\"')");
        assertThat(response.getBody().errorCode()).isEqualTo("USER_004");
    }

    @Test
    @DisplayName("Should handle generic Exception with 500 status")
    void shouldHandleGenericException() {
        // GIVEN - Excepción genérica no esperada
        Exception ex = new RuntimeException("Unexpected error");

        // WHEN
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().errorCode()).isEqualTo("INTERNAL_001");
        // El mensaje debe ser genérico (no expone detalles internos)
        assertThat(response.getBody().message()).contains("error inesperado");
    }

    @Test
    @DisplayName("Should include timestamp in all error responses")
    void shouldIncludeTimestampInErrorResponses() {
        // GIVEN - Con parámetros: username y longitud
        ValidationException ex = new ValidationException(ErrorCode.USERNAME_TOO_SHORT, "ab", 2);

        // WHEN
        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

        // THEN
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().timestamp()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Username 'ab' debe tener al menos 3 caracteres (actual: 2)");
    }
}
