package com.example.hexarch.user.domain.exception;

import java.util.UUID;

/**
 * DOMAIN LAYER - Domain Exception
 *
 * Se lanza cuando se intenta buscar un usuario que no existe.
 *
 * NOMENCLATURA:
 * - {Entity}{Reason}Exception
 *
 * HTTP MAPPING:
 * - Esta excepción se mapea a HTTP 404 NOT FOUND en GlobalExceptionHandler
 *
 * ERROR CODE:
 * - USER_404: Usuario no encontrado
 */
public class UserNotFoundException extends DomainException {

    private static final String ERROR_CODE = "USER_404";

    public UserNotFoundException(UUID userId) {
        super(String.format("User with ID '%s' not found", userId), ERROR_CODE);
    }

    public UserNotFoundException(String username) {
        super(String.format("User with username '%s' not found", username), ERROR_CODE);
    }
}
