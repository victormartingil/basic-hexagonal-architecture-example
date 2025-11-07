package com.example.hexarch.user.domain.model.valueobject;

import com.example.hexarch.shared.domain.exception.ErrorCode;
import com.example.hexarch.user.domain.exception.ValidationException;

/**
 * Email value object with validation.
 */
public record Email(String value) {

    public Email {
        validate(value);
    }

    public static Email of(String value) {
        return new Email(value);
    }

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(ErrorCode.EMAIL_EMPTY, value == null ? "null" : "\"" + value + "\"");
        }

        if (!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException(ErrorCode.EMAIL_INVALID_FORMAT, value);
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
