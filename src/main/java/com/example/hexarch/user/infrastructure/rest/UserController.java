package com.example.hexarch.user.infrastructure.rest;

import com.example.hexarch.user.application.model.CreateUserCommand;
import com.example.hexarch.user.application.port.CreateUserUseCase;
import com.example.hexarch.user.application.model.GetUserQuery;
import com.example.hexarch.user.application.port.GetUserUseCase;
import com.example.hexarch.user.application.model.UserResult;
import com.example.hexarch.user.infrastructure.adapter.input.rest.dto.generated.CreateUserRequest;
import com.example.hexarch.user.infrastructure.adapter.input.rest.dto.generated.UserResponse;
import com.example.hexarch.user.infrastructure.rest.mapper.UserRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * INFRASTRUCTURE LAYER - Input Adapter (REST Controller)
 *
 * Adaptador de entrada que expone endpoints REST para operaciones de usuarios.
 * Conecta el mundo exterior (HTTP) con la aplicación (Use Cases).
 *
 * RESPONSABILIDADES:
 * - Recibir peticiones HTTP
 * - Validar datos de entrada (@Valid)
 * - Convertir DTOs REST → Commands/Queries (usando mapper)
 * - Invocar Use Cases
 * - Convertir Results → DTOs REST (usando mapper)
 * - Devolver respuestas HTTP apropiadas
 *
 * NO DEBE:
 * - Contener lógica de negocio
 * - Acceder directamente a repositorios
 * - Conocer detalles del dominio
 *
 * NOMENCLATURA:
 * - Formato: {Entidad}Controller
 * - Ejemplos: UserController, ProductController, OrderController
 *
 * ANOTACIONES:
 * - @RestController: indica que es un controlador REST
 * - @RequestMapping: define la ruta base del controlador
 */
@RestController
@RequestMapping("/api/v1/users")  // Ruta base: /api/v1/users
public class UserController {

    // Use Cases que vamos a invocar
    private final CreateUserUseCase createUserUseCase;
    private final GetUserUseCase getUserUseCase;

    // Mapper para convertir entre DTOs REST y Commands/Queries/Results
    private final UserRestMapper mapper;

    /**
     * Constructor - Inyección de dependencias
     *
     * @param createUserUseCase caso de uso para crear usuarios
     * @param getUserUseCase caso de uso para obtener usuarios
     * @param mapper mapper para conversión de DTOs
     */
    public UserController(
            CreateUserUseCase createUserUseCase,
            GetUserUseCase getUserUseCase,
            UserRestMapper mapper
    ) {
        this.createUserUseCase = createUserUseCase;
        this.getUserUseCase = getUserUseCase;
        this.mapper = mapper;
    }

    /**
     * Endpoint: POST /api/v1/users
     *
     * Crea un nuevo usuario.
     *
     * FLUJO:
     * 1. Recibe CreateUserRequest (DTO REST) en el body
     * 2. @Valid valida el request usando Bean Validation
     * 3. Mapper convierte CreateUserRequest → CreateUserCommand
     * 4. Invoca el Use Case
     * 5. Mapper convierte UserResult → UserResponse
     * 6. Devuelve 201 CREATED con el UserResponse
     *
     * @param request datos del usuario a crear
     * @return 201 CREATED con el usuario creado
     * @throws UserAlreadyExistsException si el usuario ya existe (manejado por GlobalExceptionHandler)
     * @throws ValidationException si los datos no son válidos (manejado por GlobalExceptionHandler)
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {

        // 1. Convertir REST DTO → Application Command
        CreateUserCommand command = mapper.toCommand(request);

        // 2. Ejecutar caso de uso
        UserResult result = createUserUseCase.execute(command);

        // 3. Convertir Application Result → REST DTO
        UserResponse response = mapper.toResponse(result);

        // 4. Devolver respuesta HTTP 201 CREATED
        return ResponseEntity
                .status(HttpStatus.CREATED)  // 201 Created
                .body(response);
    }

    /**
     * Endpoint: GET /api/v1/users/{id}
     *
     * Obtiene un usuario por su ID.
     *
     * FLUJO:
     * 1. Recibe el ID del usuario en la URL
     * 2. Crea GetUserQuery con el ID
     * 3. Invoca el Use Case
     * 4. Mapper convierte UserResult → UserResponse
     * 5. Devuelve 200 OK con el UserResponse
     *
     * CQRS:
     * - Este es el lado "Query" (lectura)
     * - createUser es el lado "Command" (escritura)
     *
     * @param id ID del usuario a buscar
     * @return 200 OK con el usuario encontrado
     * @throws UserNotFoundException si el usuario no existe (manejado por GlobalExceptionHandler → 404)
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {

        // 1. Crear Query con el ID
        GetUserQuery query = new GetUserQuery(id);

        // 2. Ejecutar caso de uso
        UserResult result = getUserUseCase.execute(query);

        // 3. Convertir Application Result → REST DTO
        UserResponse response = mapper.toResponse(result);

        // 4. Devolver respuesta HTTP 200 OK
        return ResponseEntity.ok(response);
    }

    // NOTA: Aquí se pueden agregar más endpoints:
    // - GET /api/v1/users - listar usuarios (paginado)
    // - PUT /api/v1/users/{id} - actualizar usuario
    // - DELETE /api/v1/users/{id} - eliminar usuario
    // - PATCH /api/v1/users/{id}/disable - deshabilitar usuario
    // Cada uno invocaría su correspondiente Use Case
}
