package com.example.hexarch.user.domain.exception;

import com.example.hexarch.shared.domain.exception.ErrorCode;

/**
 * Base exception for all domain exceptions.
 * Uses {@link ErrorCode} for type-safe error handling.
 */
public abstract class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    public DomainException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public DomainException(ErrorCode errorCode, Object... args) {
        super(errorCode.formatMessage(args));
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getErrorCodeAsString() {
        return errorCode.getCode();
    }
}
