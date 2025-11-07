package com.example.hexarch.user.domain.exception;

import com.example.hexarch.shared.domain.exception.ErrorCode;

import java.util.UUID;

/**
 * Exception thrown when a user is not found.
 * Mapped to HTTP 404 Not Found.
 */
public class UserNotFoundException extends DomainException {

    public UserNotFoundException(UUID userId) {
        super(ErrorCode.USER_NOT_FOUND, userId);
    }

    public UserNotFoundException(String username) {
        super(ErrorCode.USER_NOT_FOUND, username);
    }
}
