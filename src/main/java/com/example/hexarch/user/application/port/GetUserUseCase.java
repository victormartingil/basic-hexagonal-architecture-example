package com.example.hexarch.user.application.port;

import com.example.hexarch.user.application.model.GetUserQuery;
import com.example.hexarch.user.application.model.UserResult;

/**
 * APPLICATION LAYER - Input Port (Use Case para Query)
 *
 * Define el contrato para obtener un usuario por su ID.
 *
 * NOMENCLATURA:
 * - Para Queries: Get{Entity}UseCase
 * - Para Commands: {Action}{Entity}UseCase
 *
 * CQRS:
 * - Este es el lado "Query" (lectura)
 * - CreateUserUseCase es el lado "Command" (escritura)
 *
 * RESPONSABILIDADES:
 * - Define QUÉ hace la aplicación (obtener un usuario)
 * - NO define CÓMO lo hace (eso es responsabilidad del Service)
 *
 * IMPLEMENTACIÓN:
 * - Implementado por GetUserService en la capa Application
 * - Usado por Controllers en la capa Infrastructure
 */
public interface GetUserUseCase {

    /**
     * Ejecuta la consulta para obtener un usuario por su ID.
     *
     * @param query Query con el ID del usuario a buscar
     * @return UserResult con los datos del usuario encontrado
     * @throws com.example.hexarch.user.domain.exception.UserNotFoundException si el usuario no existe
     */
    UserResult execute(GetUserQuery query);
}
