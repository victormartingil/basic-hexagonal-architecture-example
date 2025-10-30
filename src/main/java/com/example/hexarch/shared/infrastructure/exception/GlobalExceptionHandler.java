package com.example.hexarch.shared.infrastructure.exception;

import com.example.hexarch.user.domain.exception.DomainException;
import com.example.hexarch.user.domain.exception.UserAlreadyExistsException;
import com.example.hexarch.user.domain.exception.UserNotFoundException;
import com.example.hexarch.user.domain.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * INFRASTRUCTURE LAYER - Global Exception Handler
 *
 * Manejador global de excepciones para toda la aplicación.
 * Captura excepciones lanzadas en cualquier Controller y las convierte en respuestas HTTP apropiadas.
 *
 * RESPONSABILIDADES:
 * - Capturar excepciones de dominio y convertirlas a respuestas HTTP
 * - Capturar excepciones de validación (Bean Validation)
 * - Capturar excepciones no esperadas
 * - Registrar (log) errores importantes
 * - Devolver respuestas consistentes con formato ErrorResponse
 *
 * ANOTACIÓN:
 * - @RestControllerAdvice: aplica este manejador a todos los @RestController
 *   - Combina @ControllerAdvice + @ResponseBody
 *   - Intercepta excepciones de forma centralizada
 *
 * MAPEO DE EXCEPCIONES → HTTP STATUS:
 * - ValidationException → 400 Bad Request
 * - UserAlreadyExistsException → 409 Conflict
 * - UserNotFoundException → 404 Not Found
 * - MethodArgumentNotValidException → 400 Bad Request
 * - Exception (genérica) → 500 Internal Server Error
 */
@RestControllerAdvice  // Aplica a todos los REST controllers
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones de validación de dominio
     *
     * Se activa cuando el dominio lanza ValidationException.
     * Ejemplo: username vacío, email con formato incorrecto
     *
     * @param ex excepción de validación
     * @return 400 Bad Request con detalles del error
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        logger.warn("Validation error: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),      // 400
            "Validation Error",                   // error
            ex.getMessage(),                      // message
            ex.getErrorCode(),                    // errorCode (ej: USER_001)
            LocalDateTime.now(),                  // timestamp
            null                                  // details (no hay detalles adicionales)
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Maneja excepciones de usuario ya existe
     *
     * Se activa cuando se intenta crear un usuario que ya existe.
     *
     * @param ex excepción de usuario ya existe
     * @return 409 Conflict con detalles del error
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        logger.warn("User already exists: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.CONFLICT.value(),          // 409
            "Conflict",                           // error
            ex.getMessage(),                      // message
            ex.getErrorCode(),                    // errorCode (ej: USER_006)
            LocalDateTime.now(),                  // timestamp
            null                                  // details
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    /**
     * Maneja excepciones de usuario no encontrado
     *
     * Se activa cuando se busca un usuario que no existe.
     *
     * @param ex excepción de usuario no encontrado
     * @return 404 Not Found con detalles del error
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        logger.warn("User not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),         // 404
            "Not Found",                          // error
            ex.getMessage(),                      // message
            ex.getErrorCode(),                    // errorCode (ej: USER_404)
            LocalDateTime.now(),                  // timestamp
            null                                  // details
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * Maneja excepciones de validación de Bean Validation
     *
     * Se activa cuando falla @Valid en el Controller.
     * Ejemplo: @NotBlank, @Email, @Size
     *
     * @param ex excepción de validación
     * @return 400 Bad Request con detalles de cada campo que falló
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        logger.warn("Bean validation error: {}", ex.getMessage());

        // Extraer errores de validación por campo
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),       // 400
            "Validation Error",                   // error
            "Los datos de entrada no son válidos", // message
            "VALIDATION_001",                     // errorCode
            LocalDateTime.now(),                  // timestamp
            fieldErrors                           // details (mapa de errores por campo)
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Maneja excepciones de tipo de argumento incorrecto
     *
     * Se activa cuando Spring no puede convertir un path variable o query param
     * al tipo esperado. Ejemplo: pasar "not-a-uuid" cuando se espera UUID.
     *
     * @param ex excepción de tipo incorrecto
     * @return 400 Bad Request con detalles del error
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {

        logger.warn("Type mismatch error: parameter '{}' with value '{}' could not be converted to type '{}'",
                ex.getName(), ex.getValue(), ex.getRequiredType());

        String message = String.format(
                "El parámetro '%s' tiene un formato inválido. Valor recibido: '%s'. Tipo esperado: %s",
                ex.getName(),
                ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido"
        );

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),       // 400
                "Type Mismatch",                      // error
                message,                              // message
                "TYPE_MISMATCH_001",                  // errorCode
                LocalDateTime.now(),                  // timestamp
                null                                  // details
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Maneja cualquier otra excepción de dominio no capturada específicamente
     *
     * @param ex excepción de dominio
     * @return 500 Internal Server Error con detalles del error
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex) {
        logger.error("Domain exception: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),  // 500
            "Domain Error",                            // error
            ex.getMessage(),                           // message
            ex.getErrorCode(),                         // errorCode
            LocalDateTime.now(),                       // timestamp
            null                                       // details
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /**
     * Maneja excepciones no esperadas/genéricas
     *
     * Último recurso: captura cualquier excepción no manejada explícitamente.
     * NO debe exponer detalles internos al cliente por seguridad.
     *
     * @param ex excepción genérica
     * @return 500 Internal Server Error con mensaje genérico
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Log completo del error para debugging (con stack trace)
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        // Mensaje genérico para el cliente (no exponemos detalles internos)
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),  // 500
            "Internal Server Error",                   // error
            "Ha ocurrido un error inesperado. Por favor, contacte al administrador.", // message
            "INTERNAL_001",                           // errorCode
            LocalDateTime.now(),                      // timestamp
            null                                      // details (no exponemos detalles por seguridad)
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}
