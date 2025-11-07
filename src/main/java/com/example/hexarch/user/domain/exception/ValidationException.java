package com.example.hexarch.user.domain.exception;

import com.example.hexarch.shared.domain.exception.ErrorCode;

/**
 * DOMAIN LAYER - Domain Exception
 *
 * Excepción para errores de validación de reglas de negocio.
 * Se lanza cuando los datos no cumplen con las reglas del dominio.
 *
 * CASOS DE USO:
 * - Username vacío, muy corto o muy largo
 * - Email con formato inválido
 * - Valores fuera de rango (edad negativa, precio < 0, etc.)
 * - Strings con formato incorrecto (teléfono, código postal, etc.)
 *
 * VENTAJAS DE VALIDATIONEXCEPTION vs RuntimeException:
 *
 * 1. MAPEO HTTP ESPECÍFICO:
 *    - ValidationException → HTTP 400 Bad Request (datos inválidos)
 *    - RuntimeException genérico → HTTP 500 Internal Server Error
 *    - El cliente sabe que debe corregir los datos de entrada
 *
 * 2. CÓDIGO DE ERROR ESPECÍFICO CON ENUM:
 *    - Cada validación usa ErrorCode enum (type-safe)
 *    - El cliente puede mostrar mensajes personalizados por código
 *    - Útil para internacionalización (i18n)
 *
 * 3. EXPRESIVIDAD:
 *    - throw new ValidationException(ErrorCode.USERNAME_TOO_SHORT);
 *      vs throw new RuntimeException("Username muy corto");
 *    - Comunica claramente que es un error de validación
 *
 * 4. LOGGING Y MÉTRICAS:
 *    - Fácil filtrar y contar errores de validación específicamente
 *    - Podemos tener métricas de cuántas validaciones fallan
 *
 * EJEMPLO DE USO:
 *
 * // En el Value Object
 * if (value == null || value.isBlank()) {
 *     throw new ValidationException(ErrorCode.EMAIL_EMPTY);
 * }
 *
 * // En el GlobalExceptionHandler
 * @ExceptionHandler(ValidationException.class)
 * public ResponseEntity<ErrorResponse> handle(ValidationException ex) {
 *     return ResponseEntity.status(400).body(
 *         new ErrorResponse(400, "Validation Error", ex.getMessage(), ex.getErrorCode().getCode(), ...)
 *     );
 * }
 *
 * // El cliente recibe:
 * {
 *   "status": 400,
 *   "error": "Validation Error",
 *   "message": "Email no puede estar vacío",
 *   "errorCode": "USER_004"
 * }
 */
public class ValidationException extends DomainException {

    /**
     * Constructor con ErrorCode
     *
     * @param errorCode código de error del enum ErrorCode
     */
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Constructor con ErrorCode y argumentos para formatear mensaje
     *
     * @param errorCode código de error del enum ErrorCode
     * @param args argumentos para formatear el mensaje
     */
    public ValidationException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
