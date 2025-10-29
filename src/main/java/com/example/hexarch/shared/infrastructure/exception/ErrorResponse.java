package com.example.hexarch.shared.infrastructure.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * INFRASTRUCTURE LAYER - Error Response DTO
 *
 * DTO estándar para respuestas de error HTTP.
 * Proporciona un formato consistente para todos los errores de la API.
 *
 * VENTAJAS:
 * - Formato consistente para todos los errores
 * - Incluye información útil para el cliente
 * - Incluye timestamp para debugging
 * - Incluye errorCode para identificación programática
 * - Puede incluir detalles adicionales (ej: errores de validación por campo)
 *
 * EJEMPLO JSON:
 * {
 *   "status": 400,
 *   "error": "Validation Error",
 *   "message": "Username debe tener al menos 3 caracteres",
 *   "errorCode": "USER_002",
 *   "timestamp": "2024-01-15T10:30:00",
 *   "details": {
 *     "username": "Username debe tener al menos 3 caracteres"
 *   }
 * }
 *
 * @param status código de estado HTTP (400, 404, 500, etc.)
 * @param error tipo de error (Validation Error, Not Found, etc.)
 * @param message mensaje descriptivo del error
 * @param errorCode código único del error para identificación (USER_001, PRODUCT_002, etc.)
 * @param timestamp momento en que ocurrió el error
 * @param details detalles adicionales (ej: errores de validación por campo)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)  // No incluye campos null en el JSON
public record ErrorResponse(
    int status,
    String error,
    String message,
    String errorCode,
    LocalDateTime timestamp,
    Map<String, String> details
) {
}
