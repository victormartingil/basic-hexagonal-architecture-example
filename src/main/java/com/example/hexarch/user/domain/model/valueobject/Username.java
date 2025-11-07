package com.example.hexarch.user.domain.model.valueobject;

import com.example.hexarch.shared.domain.exception.ErrorCode;
import com.example.hexarch.user.domain.exception.ValidationException;

/**
 * Username value object with validation (3-50 characters).
 */
public record Username(String value) {

    public Username {
        validate(value);
    }

    public static Username of(String value) {
        return new Username(value);
    }

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(ErrorCode.USERNAME_EMPTY, value == null ? "null" : "\"" + value + "\"");
        }
        if (value.length() < 3) {
            throw new ValidationException(ErrorCode.USERNAME_TOO_SHORT, value, value.length());
        }
        if (value.length() > 50) {
            throw new ValidationException(ErrorCode.USERNAME_TOO_LONG, value, value.length());
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
