package com.example.hexarch.user.application.port.input;

/**
 * APPLICATION LAYER - Input Port (UseCase Interface)
 *
 * Define el contrato del caso de uso "Crear Usuario".
 * Esta interfaz representa el PUERTO DE ENTRADA a la aplicación.
 *
 * PRINCIPIOS DE PUERTOS:
 * - Define QUÉ hace la aplicación, no CÓMO lo hace
 * - No tiene dependencias de frameworks
 * - Es implementada por un Service en la capa de Application
 * - Es llamada por Adapters en la capa de Infrastructure (Controllers, Consumers, etc.)
 *
 * PATRÓN COMMAND:
 * - Recibe un Command (CreateUserCommand) con los datos de entrada
 * - Devuelve un Result (UserResult) con el resultado de la operación
 */
public interface CreateUserUseCase {

    /**
     * Ejecuta el caso de uso de crear un usuario
     *
     * @param command comando con los datos necesarios para crear el usuario
     * @return resultado con el usuario creado
     * @throws UserAlreadyExistsException si el usuario ya existe
     * @throws ValidationException si los datos no son válidos
     */
    UserResult execute(CreateUserCommand command);
}
