package com.example.hexarch.user.domain.exception;

import com.example.hexarch.shared.domain.exception.ErrorCode;

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
 * - ErrorCode.USER_NOT_FOUND: Usuario no encontrado
 */
public class UserNotFoundException extends DomainException {

    public UserNotFoundException(UUID userId) {
        super(ErrorCode.USER_NOT_FOUND, userId);
    }

    public UserNotFoundException(String username) {
        super(ErrorCode.USER_NOT_FOUND, username);
    }
}
