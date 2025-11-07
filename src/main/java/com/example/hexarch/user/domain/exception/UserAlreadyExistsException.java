package com.example.hexarch.user.domain.exception;

import com.example.hexarch.shared.domain.exception.ErrorCode;

/**
 * DOMAIN LAYER - Domain Exception
 *
 * Excepción que se lanza cuando se intenta crear un usuario que ya existe.
 * Representa una violación de la regla de negocio de unicidad.
 *
 * Esta excepción será capturada en la capa de Infrastructure y convertida
 * en una respuesta HTTP 409 Conflict.
 */
public class UserAlreadyExistsException extends DomainException {

    /**
     * Constructor con el username que causó el conflicto
     *
     * @param username nombre de usuario que ya existe
     */
    public UserAlreadyExistsException(String username) {
        super(ErrorCode.USER_ALREADY_EXISTS, username);
    }
}
