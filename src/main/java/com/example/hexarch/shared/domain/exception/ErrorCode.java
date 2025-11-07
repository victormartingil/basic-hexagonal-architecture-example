package com.example.hexarch.shared.domain.exception;

/**
 * Centralized error code catalog for the application.
 * Provides type-safe error codes with associated messages.
 */
public enum ErrorCode {

    USERNAME_EMPTY("USER_001", "Username no puede estar vacío (recibido: '%s')"),
    USERNAME_TOO_SHORT("USER_002", "Username '%s' debe tener al menos 3 caracteres (actual: %d)"),
    USERNAME_TOO_LONG("USER_003", "Username '%s' no puede tener más de 50 caracteres (actual: %d)"),
    EMAIL_EMPTY("USER_004", "Email no puede estar vacío (recibido: '%s')"),
    EMAIL_INVALID_FORMAT("USER_005", "Email '%s' no tiene un formato válido"),

    USER_ALREADY_EXISTS("USER_006", "User with username '%s' already exists"),
    USER_NOT_FOUND("USER_404", "User with ID '%s' not found"),

    BEAN_VALIDATION_ERROR("VALIDATION_001", "Los datos de entrada no son válidos"),
    TYPE_MISMATCH("TYPE_MISMATCH_001", "El parámetro '%s' tiene un formato inválido. Valor recibido: '%s'. Tipo esperado: %s"),
    INTERNAL_ERROR("INTERNAL_001", "Ha ocurrido un error inesperado. Por favor, contacte al administrador.");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String formatMessage(Object... args) {
        return String.format(message, args);
    }

    @Override
    public String toString() {
        return code;
    }
}
