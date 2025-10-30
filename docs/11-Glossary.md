# 📚 Glosario de Términos - Hexarch

## Índice
- [Arquitectura y Patrones de Diseño](#arquitectura-y-patrones-de-diseño)
- [Infrastructure & Deployment](#infrastructure--deployment)
- [Seguridad](#seguridad)
- [Observabilidad y Monitorización](#observabilidad-y-monitorización)
- [Mensajería y Eventos](#mensajería-y-eventos)
- [Base de Datos](#base-de-datos)
- [Testing](#testing)
- [Java y Spring](#java-y-spring)
- [Abreviaturas Generales](#abreviaturas-generales)

---

## Arquitectura y Patrones de Diseño

### **Hexagonal Architecture** (Arquitectura Hexagonal)
Patrón arquitectónico que separa el dominio (lógica de negocio) de la infraestructura. También conocido como **Ports & Adapters**.

**Componentes**:
- **Domain**: Lógica de negocio pura (sin dependencias externas)
- **Ports**: Interfaces que definen contratos (input/output)
- **Adapters**: Implementaciones de ports (REST, Kafka, PostgreSQL)

**Ejemplo en Hexarch**:
```
Domain (User, CreateUserUseCase)
  ↓ usa
Output Port (UserRepositoryPort)
  ↓ implementado por
Output Adapter (PostgresUserRepository)
```

---

### **CQRS** (Command Query Responsibility Segregation)
Separación de operaciones de escritura (Commands) y lectura (Queries).

**Hexarch usa CQRS parcial**:
- **Commands**: `CreateUserUseCase`, `UpdateUserUseCase`
- **Queries**: `GetUserUseCase`, `FindUserByEmailUseCase`

**Beneficio**: Optimización independiente (escrituras en PostgreSQL, lecturas en cache)

---

### **DDD** (Domain-Driven Design)
Metodología que pone el dominio en el centro del diseño.

**Conceptos en Hexarch**:
- **Entity**: `User` (tiene identidad y ciclo de vida)
- **Value Object**: `Email`, `UserId` (inmutables, sin identidad)
- **Aggregate**: `User` (raíz de agregado)
- **Domain Event**: `UserCreatedEvent`
- **Factory Method**: `User.create(...)` (construcción válida)

---

### **Ports & Adapters**
Ver **Hexagonal Architecture**.

---

### **Event-Driven Architecture** (Arquitectura Dirigida por Eventos)
Sistema que reacciona a eventos en lugar de llamadas síncronas.

**Hexarch**:
- Evento: `UserCreatedEvent`
- Producer: `KafkaEventPublisher`
- Consumer: `KafkaUserEventConsumer`
- Topic: `user-events`

---

### **Clean Architecture**
Similar a Hexagonal. Prioriza independencia de frameworks y testabilidad.

---

## Infrastructure & Deployment

### **Docker**
Plataforma de contenedores que empaqueta aplicaciones con sus dependencias.

**Hexarch**: `Dockerfile` multi-stage (build + runtime) genera imagen de ~200MB.

---

### **Dockerfile Multi-Stage**
Dockerfile con múltiples fases (build, runtime) para optimizar tamaño de imagen.

**Hexarch**:
1. **Stage 1 (build)**: Maven + JDK → compila JAR
2. **Stage 2 (runtime)**: JRE + JAR → imagen final pequeña

---

### **Kubernetes (K8s)**
Orquestador de contenedores para despliegue, escalado y gestión automática.

**Objetos en Hexarch**:
- **Deployment**: Gestiona pods con la aplicación
- **Service**: Expone aplicación (LoadBalancer)
- **ConfigMap**: Variables de configuración no sensibles
- **Secret**: Variables sensibles (JWT_SECRET, DB_PASSWORD)
- **HPA**: Auto-escalado basado en CPU/memoria

---

### **HPA** (Horizontal Pod Autoscaler)
Escala automáticamente el número de pods según métricas (CPU, memoria).

**Hexarch**: 2-10 pods según CPU > 70%.

---

### **Liveness Probe**
Health check de Kubernetes que **reinicia** el pod si falla.

**Hexarch**: `GET /actuator/health/liveness`

---

### **Readiness Probe**
Health check que **detiene tráfico** al pod si falla (sin reiniciar).

**Hexarch**: `GET /actuator/health/readiness`

---

### **Graceful Shutdown**
Proceso de parada ordenada que permite completar requests en curso antes de cerrar.

**Hexarch**: 30 segundos de timeout (`spring.lifecycle.timeout-per-shutdown-phase`).

---

### **ConfigMap**
Objeto de Kubernetes para configuración no sensible (URLs, timeouts, etc.).

---

### **Secret**
Objeto de Kubernetes para datos sensibles (passwords, tokens, claves). Codificado en Base64.

**Hexarch**: JWT_SECRET, DB_PASSWORD, KAFKA_PASSWORD.

---

### **LoadBalancer**
Distribuye tráfico entre múltiples pods/instancias.

---

### **Ingress**
API Gateway de Kubernetes que gestiona acceso HTTP externo (routing, SSL).

---

## Seguridad

### **JWT** (JSON Web Token)
Token firmado que contiene claims (datos del usuario). Usado para autenticación stateless.

**Estructura**:
```
header.payload.signature
```

**Hexarch**: Generado en login, validado en cada request con filtro JWT.

---

### **JWK** (JSON Web Key)
Formato JSON para claves criptográficas (usado en JWT).

---

### **HMAC** (Hash-based Message Authentication Code)
Algoritmo de firma usado en JWT (HS256, HS512).

**Hexarch**: JWT firmado con `JWT_SECRET` (HMAC-SHA256).

---

### **RBAC** (Role-Based Access Control)
Control de acceso basado en roles (ADMIN, USER).

**Hexarch**:
- `@PreAuthorize("hasRole('ADMIN')")` → Solo admins
- `@PreAuthorize("hasRole('USER')")` → Usuarios autenticados

---

### **Bearer Token**
Token de autenticación enviado en header:
```
Authorization: Bearer <JWT>
```

---

### **Principal**
Usuario autenticado en el contexto de seguridad (Spring Security).

---

### **BCrypt**
Algoritmo de hashing para passwords (con salt automático).

**Hexarch**: `BCryptPasswordEncoder` hashea passwords antes de guardar.

---

### **Salt**
Valor aleatorio añadido al password antes de hashear (previene rainbow tables).

---

## Observabilidad y Monitorización

### **Distributed Tracing** (Trazabilidad Distribuida)
Seguimiento de requests a través de múltiples microservicios.

**Hexarch**: Micrometer Tracing + Zipkin.

---

### **Trace ID**
Identificador único que sigue un request a través de todos los servicios.

**Hexarch**: Generado automáticamente por Micrometer, visible en logs.

---

### **Span ID**
Identificador de una operación dentro de un trace (sub-operación).

---

### **Correlation ID**
ID de negocio para correlacionar logs entre servicios (independiente de Trace ID).

**Hexarch**: `X-Correlation-ID` header, propagado en `CorrelationIdFilter`.

---

### **MDC** (Mapped Diagnostic Context)
Mecanismo de SLF4J para añadir contexto a logs (trace ID, correlation ID).

**Hexarch**:
```java
MDC.put("correlationId", uuid);
log.info("User created");  // Log incluye correlationId
MDC.remove("correlationId");
```

---

### **Prometheus**
Sistema de monitorización que recolecta métricas en formato time-series.

**Hexarch**: Exporta métricas en `/actuator/prometheus`.

---

### **Grafana**
Plataforma de visualización de métricas (dashboards).

**Hexarch**: Lee métricas de Prometheus y las muestra en dashboards.

---

### **Zipkin**
Servidor de distributed tracing que visualiza traces.

**Hexarch**: Recibe traces desde Micrometer.

---

### **Actuator**
Módulo de Spring Boot que expone endpoints de management (health, metrics, info).

**Hexarch endpoints**:
- `/actuator/health` → Estado de la app
- `/actuator/prometheus` → Métricas
- `/actuator/info` → Info de la app

---

### **Health Check**
Endpoint que indica si la aplicación está saludable.

---

### **Circuit Breaker**
Patrón que previene cascadas de fallos cortando llamadas a servicios caídos.

**Hexarch**: `@CircuitBreaker(name = "emailService")` en `EmailService`.

**Estados**:
- **CLOSED**: Normal (llamadas pasan)
- **OPEN**: Servicio caído (llamadas fallan inmediatamente)
- **HALF_OPEN**: Probando recuperación

---

### **Resilience4j**
Librería de tolerancia a fallos (Circuit Breaker, Retry, RateLimiter).

---

### **Retry**
Patrón que reintenta operaciones fallidas automáticamente.

**Hexarch**: `@Retry(name = "emailService")` con 3 reintentos.

---

### **Fallback**
Método alternativo ejecutado cuando una operación falla.

**Hexarch**:
```java
@CircuitBreaker(name = "emailService", fallbackMethod = "sendEmailFallback")
```

---

## Mensajería y Eventos

### **Kafka**
Plataforma de streaming distribuida (pub/sub) para eventos.

**Hexarch**: Publica `UserCreatedEvent` al topic `user-events`.

---

### **Topic**
Canal de comunicación en Kafka donde se publican eventos.

**Hexarch**: `user-events` (1 partición, replicación factor 1).

---

### **Producer**
Aplicación que publica eventos a Kafka.

**Hexarch**: `KafkaEventPublisher`.

---

### **Consumer**
Aplicación que consume eventos de Kafka.

**Hexarch**: `KafkaUserEventConsumer`.

---

### **Partition**
División de un topic en Kafka para paralelización.

**Hexarch**: 1 partición (suficiente para desarrollo).

---

### **Replication Factor**
Número de copias de cada partición en Kafka (HA).

**Hexarch**: 1 (dev), 3 (producción recomendado).

---

### **Consumer Group**
Grupo de consumers que se reparten partitions de un topic.

**Hexarch**: `hexarch-notification-service`.

---

### **Offset**
Posición de un mensaje en una partition (como un índice).

---

### **DLT** (Dead Letter Topic)
Topic especial para mensajes que fallaron después de todos los reintentos.

**Hexarch**: `user-events.DLT` (Spring Kafka lo crea automáticamente).

---

### **DLQ** (Dead Letter Queue)
Concepto similar a DLT, usado en otros sistemas de mensajería (RabbitMQ, SQS).

---

### **Idempotency** (Idempotencia)
Propiedad de una operación que puede ejecutarse múltiples veces con el mismo resultado.

**Hexarch**: Consumers deben ser idempotentes (ejemplo: `NotificationAlreadySentException`).

---

### **At-Least-Once Delivery**
Garantía de entrega de Kafka: mensaje puede llegar 1+ veces (requiere idempotencia).

---

### **Exactly-Once Semantics**
Garantía más fuerte: mensaje procesado exactamente una vez (complejo de implementar).

---

### **AsyncAPI**
Especificación para documentar APIs asíncronas (Kafka, AMQP, WebSockets).

**Hexarch**: `asyncapi/hexarch-events.yml` documenta eventos Kafka.

---

## Base de Datos

### **PostgreSQL**
Base de datos relacional open-source (ACID, transacciones).

**Hexarch**: BD principal para persistencia de usuarios.

---

### **JPA** (Java Persistence API)
Especificación Java para ORM (mapeo objeto-relacional).

---

### **ORM** (Object-Relational Mapping)
Técnica que mapea objetos Java a tablas SQL.

**Hexarch**: `@Entity User` → tabla `users`.

---

### **Entity**
Clase Java mapeada a una tabla de base de datos.

**Hexarch**: `UserEntity` (infraestructura), `User` (dominio).

---

### **Repository**
Patrón que encapsula acceso a datos.

**Hexarch**:
- **Port**: `UserRepositoryPort` (interface de dominio)
- **Adapter**: `PostgresUserRepository` (implementación JPA)

---

### **DDL** (Data Definition Language)
SQL para definir estructura (CREATE, ALTER, DROP).

**Hexarch**: Flyway ejecuta DDL en `V1__initial_schema.sql`.

---

### **DML** (Data Manipulation Language)
SQL para manipular datos (INSERT, UPDATE, DELETE, SELECT).

---

### **Flyway**
Herramienta de migración de BD (versionado de schema).

**Hexarch**: Migraciones en `src/main/resources/db/migration/`.

**Convención**: `V{version}__{description}.sql`
- `V1__initial_schema.sql`
- `V2__add_phone_number_column.sql`

---

### **Transaction** (Transacción)
Unidad de trabajo que se ejecuta completamente o se revierte (ACID).

**Hexarch**: `@Transactional` en servicios.

---

### **ACID**
Propiedades de transacciones:
- **A**tomicity: Todo o nada
- **C**onsistency: Datos válidos siempre
- **I**solation: Transacciones aisladas
- **D**urability: Cambios permanentes

---

### **Connection Pool**
Pool de conexiones reutilizables a BD (optimiza performance).

**Hexarch**: HikariCP (default en Spring Boot).

---

## Testing

### **Unit Test** (Test Unitario)
Test de una unidad aislada (método, clase) con mocks.

**Hexarch**: `CreateUserUseCaseTest` (mockea repository).

---

### **Integration Test** (Test de Integración)
Test de componentes reales integrados (BD, Kafka).

**Hexarch**: `KafkaConsumerIntegrationTest` usa Testcontainers.

---

### **E2E Test** (End-to-End Test)
Test del flujo completo (API → BD → Kafka).

**Hexarch**: `SecurityIntegrationTest` prueba autenticación completa.

---

### **Testcontainers**
Librería que levanta contenedores Docker para tests (PostgreSQL, Kafka).

**Hexarch**:
```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
```

---

### **Mock**
Objeto falso que simula comportamiento de dependencias.

**Hexarch**: `@Mock UserRepositoryPort` en tests unitarios.

---

### **Stub**
Implementación simple de una interface para tests.

---

### **Test Coverage** (Cobertura de Tests)
Porcentaje de código ejecutado por tests.

**Hexarch**: 85%+ requerido (JaCoCo enforcer).

---

### **JaCoCo** (Java Code Coverage)
Herramienta que mide cobertura de tests.

**Hexarch**: Genera reporte en `target/site/jacoco/`.

---

### **ArchUnit**
Librería para validar arquitectura con tests.

**Hexarch**: 21 reglas arquitecturales en `HexagonalArchitectureTest`.

---

### **AssertJ**
Librería de assertions fluidas para tests.

**Ejemplo**:
```java
assertThat(user.getUsername())
    .isNotNull()
    .startsWith("john");
```

---

### **Mockito**
Framework para crear mocks en tests Java.

---

### **JUnit 5**
Framework de testing para Java.

---

## Java y Spring

### **DTO** (Data Transfer Object)
Objeto usado para transferir datos entre capas (sin lógica).

**Hexarch**:
- `CreateUserRequest` (REST → Application)
- `UserResponse` (Application → REST)

---

### **POJO** (Plain Old Java Object)
Clase Java simple sin extends/implements especiales.

---

### **DAO** (Data Access Object)
Patrón legacy para acceso a datos (similar a Repository).

---

### **Bean**
Objeto gestionado por Spring IoC container.

**Hexarch**: `@Component`, `@Service`, `@Repository` crean beans.

---

### **IoC** (Inversion of Control)
Principio donde framework controla flujo (no el programador).

**Spring**: Inyecta dependencias automáticamente.

---

### **DI** (Dependency Injection)
Técnica de IoC que inyecta dependencias desde fuera.

**Hexarch**:
```java
@Service
@RequiredArgsConstructor  // Constructor injection via Lombok
public class CreateUserUseCase {
    private final UserRepositoryPort userRepository;  // Inyectado
}
```

---

### **Constructor Injection**
DI via constructor (recomendado, permite `final` fields).

**Hexarch**: Usado en todos los servicios con `@RequiredArgsConstructor`.

---

### **Stereotype Annotations**
Anotaciones de Spring que indican rol de una clase:
- `@Component`: Bean genérico
- `@Service`: Lógica de negocio
- `@Repository`: Acceso a datos
- `@Controller` / `@RestController`: API REST

---

### **AOP** (Aspect-Oriented Programming)
Paradigma para separar cross-cutting concerns (logging, seguridad).

**Hexarch**: Usado en `CorrelationIdFilter` (ejecuta antes de cada request).

---

### **Spring Boot**
Framework que simplifica configuración de aplicaciones Spring.

---

### **Spring Data JPA**
Módulo que simplifica acceso a BD con JPA.

**Hexarch**: `JpaRepository<UserEntity, UUID>`.

---

### **Spring Security**
Framework de seguridad para autenticación y autorización.

**Hexarch**: JWT filter + RBAC con roles.

---

### **Spring Kafka**
Módulo de Spring para integrar con Kafka.

**Hexarch**: `@KafkaListener` en consumers.

---

### **Lombok**
Librería que genera código boilerplate (getters, constructors).

**Hexarch**:
- `@RequiredArgsConstructor`: Constructor con `final` fields
- `@Slf4j`: Logger automático
- `@Getter`: Getters automáticos

---

### **Record** (Java 14+)
Clase inmutable compacta para DTOs.

**Hexarch**:
```java
public record CreateUserRequest(String username, String email) {}
```

---

### **Optional**
Contenedor que puede o no contener un valor (evita `NullPointerException`).

**Hexarch**:
```java
Optional<User> user = userRepository.findByEmail(email);
user.orElseThrow(() -> new UserNotFoundException());
```

---

### **Stream API**
API funcional de Java para procesar colecciones.

---

## Abreviaturas Generales

### **API** (Application Programming Interface)
Interfaz para interactuar con un sistema.

---

### **REST** (Representational State Transfer)
Estilo arquitectónico para APIs HTTP (GET, POST, PUT, DELETE).

---

### **HTTP** (HyperText Transfer Protocol)
Protocolo de comunicación web.

---

### **JSON** (JavaScript Object Notation)
Formato de datos basado en texto (key-value pairs).

---

### **YAML** (YAML Ain't Markup Language)
Formato de datos más legible que JSON (usado en configs).

---

### **URL** (Uniform Resource Locator)
Dirección web (`https://api.example.com/users`).

---

### **UUID** (Universally Unique Identifier)
Identificador único de 128 bits (`550e8400-e29b-41d4-a716-446655440000`).

---

### **URI** (Uniform Resource Identifier)
Identificador genérico (URL es un tipo de URI).

---

### **CRUD** (Create, Read, Update, Delete)
Operaciones básicas de persistencia.

---

### **HA** (High Availability)
Sistema diseñado para estar disponible 99.9%+ del tiempo.

---

### **SLA** (Service Level Agreement)
Contrato que garantiza nivel de servicio (uptime, latencia).

---

### **SLO** (Service Level Objective)
Objetivo interno de calidad de servicio.

---

### **SLI** (Service Level Indicator)
Métrica medible de calidad (latencia p99, error rate).

---

### **CI/CD** (Continuous Integration / Continuous Deployment)
Automatización de build, tests y deployment.

**Hexarch**: GitHub Actions (`.github/workflows/`).

---

### **TDD** (Test-Driven Development)
Metodología: escribir tests antes que código.

---

### **BDD** (Behavior-Driven Development)
Metodología: escribir tests en lenguaje natural (Given-When-Then).

---

### **SOLID**
5 principios de diseño OOP:
- **S**ingle Responsibility
- **O**pen/Closed
- **L**iskov Substitution
- **I**nterface Segregation
- **D**ependency Inversion

---

### **DRY** (Don't Repeat Yourself)
Principio: evitar código duplicado.

---

### **YAGNI** (You Aren't Gonna Need It)
Principio: no añadir funcionalidad hasta que sea necesaria.

---

### **KISS** (Keep It Simple, Stupid)
Principio: mantener diseño simple.

---

### **MVP** (Minimum Viable Product)
Versión mínima funcional de un producto.

---

### **POC** (Proof of Concept)
Prototipo para validar viabilidad técnica.

---

### **DSL** (Domain-Specific Language)
Lenguaje diseñado para un dominio específico.

**Ejemplos**: SQL (queries), Gherkin (BDD), ArchUnit (tests arquitecturales).

---

### **CLI** (Command-Line Interface)
Interfaz basada en comandos de texto.

**Hexarch**: `./mvnw`, `docker`, `kubectl`.

---

### **GUI** (Graphical User Interface)
Interfaz gráfica de usuario.

---

### **IDE** (Integrated Development Environment)
Editor de código con herramientas integradas (IntelliJ, VS Code).

---

### **VCS** (Version Control System)
Sistema de control de versiones (Git, SVN).

---

### **PR** (Pull Request)
Propuesta de cambios en Git (GitHub, GitLab).

---

### **MR** (Merge Request)
Equivalente a PR en GitLab.

---

### **WIP** (Work In Progress)
Trabajo en curso (PR no listo para review).

---

### **LGTM** (Looks Good To Me)
Aprobación en code review.

---

### **RTFM** (Read The F***ing Manual)
Lee la documentación 😅

---

## Recursos Adicionales

### **¿Dónde Aprender Más?**

- **Hexagonal Architecture**: [Alistair Cockburn - Original Article](https://alistair.cockburn.us/hexagonal-architecture/)
- **DDD**: [Domain-Driven Design - Eric Evans](https://www.domainlanguage.com/)
- **Spring Boot**: [Official Docs](https://spring.io/projects/spring-boot)
- **Kubernetes**: [Kubernetes Documentation](https://kubernetes.io/docs/)
- **Kafka**: [Kafka Documentation](https://kafka.apache.org/documentation/)
- **Testcontainers**: [Testcontainers Docs](https://www.testcontainers.org/)

---

## Contribuir al Glosario

Si encuentras un término que no está aquí y debería estarlo, añádelo siguiendo el formato:

```markdown
### **TÉRMINO** (Expansión si es abreviatura)
Definición clara y concisa.

**Hexarch**: Cómo se usa en este proyecto (si aplica).

**Ejemplo** (opcional):
\`\`\`
Código o comando de ejemplo
\`\`\`
```

---

**Última actualización**: 2025-10-30
