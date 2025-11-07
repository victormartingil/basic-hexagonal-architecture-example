package com.example.hexarch.user.domain.exception;

import com.example.hexarch.shared.domain.exception.ErrorCode;

/**
 * Exception thrown when attempting to create a user that already exists.
 * Mapped to HTTP 409 Conflict.
 */
public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException(String username) {
        super(ErrorCode.USER_ALREADY_EXISTS, username);
    }
}
