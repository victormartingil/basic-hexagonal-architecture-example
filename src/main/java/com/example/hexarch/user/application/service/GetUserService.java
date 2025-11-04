package com.example.hexarch.user.application.service;

import com.example.hexarch.user.application.model.GetUserQuery;
import com.example.hexarch.user.application.port.GetUserUseCase;
import com.example.hexarch.user.application.model.UserResult;
import com.example.hexarch.user.application.port.UserRepository;
import com.example.hexarch.user.domain.exception.UserNotFoundException;
import com.example.hexarch.user.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * APPLICATION LAYER - Service (Query Handler)
 *
 * Implementa el caso de uso GetUserUseCase para obtener un usuario por ID.
 *
 * NOMENCLATURA:
 * - Para Queries: Get{Entity}Service
 * - Para Commands: {Action}{Entity}Service
 *
 * DIFERENCIA CON COMMAND SERVICE:
 * - Query Service: Solo LEE datos (no modifica estado)
 * - Command Service: MODIFICA datos (crea, actualiza, elimina)
 *
 * CQRS:
 * - Este es un Query Handler (lado de lectura)
 * - En arquitecturas avanzadas, podría leer de una BD optimizada para lectura
 *
 * TRANSACCIONAL:
 * - @Transactional(readOnly = true) para queries
 * - Optimización: permite a la BD usar estrategias de solo lectura
 *
 * FLUJO:
 * 1. Buscar usuario en repositorio
 * 2. Si no existe, lanzar UserNotFoundException
 * 3. Convertir User (Domain) a UserResult (Application DTO)
 * 4. Retornar resultado
 */
@Service
@Transactional(readOnly = true)  // readOnly = true para queries (optimización)
public class GetUserService implements GetUserUseCase {

    private final UserRepository userRepository;

    /**
     * Constructor - Inyección de dependencias
     *
     * @param userRepository repositorio para buscar usuarios
     */
    public GetUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Ejecuta la consulta para obtener un usuario por su ID.
     *
     * FLUJO:
     * 1. Buscar usuario en repositorio
     * 2. Si no existe, lanzar UserNotFoundException (se mapea a HTTP 404)
     * 3. Convertir User (Domain) a UserResult (Application DTO)
     * 4. Retornar resultado
     *
     * @param query Query con el ID del usuario a buscar
     * @return UserResult con los datos del usuario encontrado
     * @throws UserNotFoundException si el usuario no existe (HTTP 404)
     */
    @Override
    public UserResult execute(GetUserQuery query) {
        // 1. BUSCAR USUARIO
        // findById retorna Optional<User>
        // orElseThrow lanza excepción si no existe
        User user = userRepository.findById(query.userId())
            .orElseThrow(() -> new UserNotFoundException(query.userId()));

        // 2. CONVERTIR A RESULT DTO
        // Extraemos los valores de los Value Objects (Username, Email)
        // UserResult es un DTO de Application (no expone el modelo de dominio)
        return new UserResult(
            user.getId(),
            user.getUsername().getValue(),  // Value Object → String
            user.getEmail().getValue(),     // Value Object → String
            user.isEnabled(),
            user.getCreatedAt()             // Instant se mantiene
        );
    }
}
