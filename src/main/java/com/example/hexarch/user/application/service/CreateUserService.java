package com.example.hexarch.user.application.service;

import com.example.hexarch.user.application.port.input.CreateUserCommand;
import com.example.hexarch.user.application.port.input.CreateUserUseCase;
import com.example.hexarch.user.application.port.input.UserResult;
import com.example.hexarch.user.application.port.output.UserEventPublisher;
import com.example.hexarch.user.application.port.output.UserRepository;
import com.example.hexarch.user.domain.event.UserCreatedEvent;
import com.example.hexarch.user.domain.exception.UserAlreadyExistsException;
import com.example.hexarch.user.domain.model.User;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * APPLICATION LAYER - Service (UseCase Implementation)
 *
 * Implementa el caso de uso CreateUserUseCase.
 * Orquesta la lógica de negocio usando el dominio y los puertos de salida.
 *
 * PRINCIPIOS IMPORTANTES:
 * - Orquesta: coordina diferentes componentes pero no contiene lógica de negocio compleja
 * - La lógica de negocio está en el dominio (User)
 * - No conoce detalles de infraestructura (HTTP, base de datos, etc.)
 * - Usa interfaces (puertos) para comunicarse con el exterior
 *
 * NOMENCLATURA:
 * - Formato: {Accion}{Entidad}Service
 * - Ejemplos: CreateUserService, UpdateProductService, GetOrdersService
 * - Siempre implementa su correspondiente UseCase interface
 *
 * ANOTACIONES:
 * - @Service: marca esta clase como un servicio de Spring
 * - @Transactional: asegura que toda la operación sea atómica (todo o nada)
 *
 * FLUJO TÍPICO:
 * 1. Validar precondiciones (ej: usuario no existe)
 * 2. Crear/modificar el modelo de dominio
 * 3. Persistir usando repository (output port)
 * 4. Publicar eventos usando event publisher (output port)
 * 5. Retornar resultado
 */
@Service
@Transactional  // Toda la operación es atómica: si algo falla, se hace rollback
public class CreateUserService implements CreateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateUserService.class);

    // Puertos de salida (dependencies)
    private final UserRepository userRepository;
    private final UserEventPublisher userEventPublisher;

    // Observability: Métricas custom
    private final MeterRegistry meterRegistry;
    private final String environment;

    /**
     * Constructor - Inyección de dependencias
     *
     * Spring inyecta automáticamente las implementaciones de las interfaces.
     * No usamos @Autowired porque la inyección por constructor es preferida.
     *
     * @param userRepository repositorio para persistir usuarios
     * @param userEventPublisher publicador para eventos de usuarios
     * @param meterRegistry registro de métricas de Micrometer para observability
     * @param environment entorno actual (local, dev, prod) para tags de métricas
     */
    public CreateUserService(
            UserRepository userRepository,
            UserEventPublisher userEventPublisher,
            MeterRegistry meterRegistry,
            @Value("${ENVIRONMENT:local}") String environment
    ) {
        this.userRepository = userRepository;
        this.userEventPublisher = userEventPublisher;
        this.meterRegistry = meterRegistry;
        this.environment = environment;
    }

    /**
     * Ejecuta el caso de uso de crear un usuario
     *
     * FLUJO:
     * 1. Verificar que no exista el username
     * 2. Verificar que no exista el email
     * 3. Crear el usuario usando el factory method del dominio
     * 4. Persistir el usuario
     * 5. Publicar evento de usuario creado
     * 6. Retornar el resultado
     *
     * @param command comando con username y email
     * @return resultado con los datos del usuario creado
     * @throws UserAlreadyExistsException si el username o email ya existen
     */
    @Override
    public UserResult execute(CreateUserCommand command) {

        // 📝 LOG INFO: Evento de negocio importante (inicio de operación)
        log.info("Creating user: username={}, email={}", command.username(), command.email());

        // 1. VALIDAR PRECONDICIONES (reglas de negocio)
        // Verificar que el username no esté en uso
        if (userRepository.existsByUsername(command.username())) {
            log.warn("Username already exists: {}", command.username());
            throw new UserAlreadyExistsException(command.username());
        }

        // Verificar que el email no esté en uso
        if (userRepository.existsByEmail(command.email())) {
            log.warn("Email already exists: {}", command.email());
            throw new UserAlreadyExistsException(command.email());
        }

        // 2. CREAR MODELO DE DOMINIO
        // Usar el factory method del dominio que valida y crea el usuario
        User user = User.create(command.username(), command.email());

        // 3. PERSISTIR (usando output port)
        User savedUser = userRepository.save(user);

        // 📝 LOG INFO: Usuario creado exitosamente
        log.info("User created successfully: userId={}, username={}, email={}",
                savedUser.getId(),
                savedUser.getUsername().getValue(),
                savedUser.getEmail().getValue());

        // 4. PUBLICAR EVENTO (usando output port)
        // Creamos el evento de dominio
        // Extraemos los valores de los Value Objects como String
        UserCreatedEvent event = UserCreatedEvent.from(
            savedUser.getId(),
            savedUser.getUsername().getValue(),  // Value Object → String
            savedUser.getEmail().getValue()      // Value Object → String
        );
        // Publicamos el evento (esto podría ir a Kafka, RabbitMQ, etc.)
        userEventPublisher.publish(event);

        // 📊 MÉTRICA CUSTOM: Contador de usuarios creados
        // Esta métrica se expone en /actuator/prometheus
        meterRegistry.counter("users.created.total",
                              "status", "success",
                              "environment", environment)
                     .increment();

        // 5. RETORNAR RESULTADO
        // Convertimos el modelo de dominio (User) a DTO de application (UserResult)
        // Extraemos los valores de los Value Objects (Username, Email) como String
        return new UserResult(
            savedUser.getId(),
            savedUser.getUsername().getValue(),  // Value Object → String
            savedUser.getEmail().getValue(),     // Value Object → String
            savedUser.isEnabled(),
            savedUser.getCreatedAt()             // Instant se mantiene
        );
    }
}
