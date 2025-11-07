package com.example.hexarch.user.domain.exception;

import com.example.hexarch.shared.domain.exception.ErrorCode;

/**
 * Exception for domain validation rule violations.
 * Typically mapped to HTTP 400 Bad Request.
 */
public class ValidationException extends DomainException {

    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ValidationException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
