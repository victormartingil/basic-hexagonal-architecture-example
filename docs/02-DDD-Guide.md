# Guía de Domain-Driven Design (DDD)

## 📚 Índice

1. [¿Qué es DDD?](#qué-es-ddd)
2. [¿Por qué usar DDD?](#por-qué-usar-ddd)
3. [Conceptos Fundamentales](#conceptos-fundamentales)
4. [Building Blocks de DDD](#building-blocks-de-ddd)
5. [Ejemplos Prácticos en el Proyecto](#ejemplos-prácticos)
6. [Errores Comunes](#errores-comunes)
7. [Eventos: Domain Events vs Integration Events](#eventos-domain-events-vs-integration-events)
8. [Cuándo Usar Cada Concepto](#cuándo-usar-cada-concepto)

---

## ¿Qué es DDD?

**Domain-Driven Design** (Diseño Orientado al Dominio) es una forma de diseñar software poniendo el **dominio del negocio** en el centro.

### Analogía Simple

Imagina que estás construyendo una casa:

- **SIN DDD**: Te enfocas en los materiales (Spring, JPA, REST) y construyes la casa alrededor de ellos
- **CON DDD**: Primero entiendes cómo vive la familia (dominio), diseñas la casa para ellos, y luego eliges los materiales

### En Pocas Palabras

DDD dice: **"Modela tu software como el negocio funciona en la vida real"**

- Si en el negocio hay "Usuarios", en el código hay un objeto `User`
- Si un "Email debe ser válido", el código valida el email
- Si "No pueden haber dos usuarios con el mismo username", el código lo previene

---

## ¿Por qué usar DDD?

### Problemas que DDD Resuelve

❌ **Sin DDD:**
```java
// Lógica de negocio esparcida por todos lados
@Controller
public class UserController {
    @PostMapping("/users")
    public User create(@RequestBody String username, String email) {
        // ¿El email es válido? (Validación en el Controller)
        if (!email.contains("@")) throw new Exception("Invalid email");

        // ¿El usuario ya existe? (Lógica de negocio en el Controller)
        if (userRepo.existsByUsername(username)) throw new Exception("Exists");

        // Crear usuario (sin validaciones)
        User user = new User();
        user.setUsername(username);  // ¿Qué pasa si username está vacío?
        user.setEmail(email);
        return userRepo.save(user);
    }
}
```

**Problemas:**
- Lógica de negocio en el Controller (¡no debería estar ahí!)
- Difícil de testear
- Difícil de reutilizar
- Fácil olvidar validaciones

✅ **Con DDD:**
```java
// Lógica de negocio en el Dominio
public class User {  // Aggregate Root
    private Username username;  // Value Object (auto-valida)
    private Email email;        // Value Object (auto-valida)

    public static User create(String username, String email) {
        // Las validaciones están en Username y Email
        return new User(Username.of(username), Email.of(email));
    }
}

// Controller solo coordina
@Controller
public class UserController {
    @PostMapping("/users")
    public UserResponse create(@RequestBody CreateUserRequest request) {
        CreateUserCommand command = mapper.toCommand(request);
        UserResult result = createUserUseCase.execute(command);  // ¡Simple!
        return mapper.toResponse(result);
    }
}
```

**Ventajas:**
- ✅ Lógica de negocio centralizada en el Dominio
- ✅ Fácil de testear (sin frameworks)
- ✅ Reutilizable
- ✅ Imposible olvidar validaciones

---

## Conceptos Fundamentales

### 1. El Dominio (Domain)

**Definición:** El dominio es **el problema del negocio** que tu software resuelve.

**Ejemplos:**
- E-commerce: productos, carritos, órdenes, pagos
- Banking: cuentas, transacciones, préstamos
- Nuestra app: usuarios, autenticación, perfiles

### 2. Lenguaje Ubicuo (Ubiquitous Language)

**Definición:** Usar **los mismos términos** en el código que usa el negocio.

**Ejemplo:**
- ❌ Negocio dice "Cliente", código usa `Person`
- ✅ Negocio dice "Cliente", código usa `Customer`

En nuestro proyecto:
- Negocio: "Usuario", "Email", "Username"
- Código: `User`, `Email`, `Username` ✅

### 3. Bounded Context

**Definición:** Un límite donde un concepto tiene un significado específico.

**Ejemplo:**
- En **Ventas**: "Cliente" = persona que compra
- En **Soporte**: "Cliente" = persona que reporta problemas
- En **Marketing**: "Cliente" = lead o prospecto

Son el mismo "Cliente" pero con significados diferentes → Bounded Contexts diferentes.

---

## Building Blocks de DDD

DDD define tipos específicos de objetos. Aquí está cada uno:

### 1. Entity (Entidad)

**¿Qué es?**
Un objeto que tiene **identidad única** y puede cambiar en el tiempo.

**Características:**
- Tiene un **ID único** (UUID, Long, etc.)
- Dos entities con el mismo ID son **la misma**, aunque sus datos sean diferentes
- Es **mutable** (puede cambiar)

**Ejemplo:**
```java
// User es una Entity
User user1 = new User(UUID.fromString("123..."), "john", "john@ex.com");
User user2 = new User(UUID.fromString("123..."), "johnny", "johnny@ex.com");

// Son el mismo usuario (mismo ID), aunque los datos cambien
user1.equals(user2);  // true (mismo ID)
```

**Cuándo usar:**
- Cuando necesitas rastrear algo a lo largo del tiempo
- Cuando la identidad importa más que los datos
- Ejemplos: User, Order, Product, Invoice

**En nuestro proyecto:**
- `User` es una Entity con ID único

---

### 2. Value Object (Objeto de Valor)

**¿Qué es?**
Un objeto que **NO tiene identidad**, solo valor. Dos Value Objects con el mismo valor son **intercambiables**.

**Características:**
- **Sin ID** (se identifica por su valor)
- **Inmutable** (no cambia, se crea uno nuevo)
- Dos Value Objects con el mismo valor son **iguales**

**Ejemplo:**
```java
// Email es un Value Object
Email email1 = Email.of("john@example.com");
Email email2 = Email.of("john@example.com");

email1.equals(email2);  // true (mismo valor = son iguales)
email1 == email2;       // false (objetos diferentes, pero iguales en valor)
```

**Analogía:**
- Dos billetes de 10€ son **intercambiables** (Value Object)
- Tu DNI es **único** y te identifica (Entity)

**Cuándo usar:**
- Cuando solo el **valor** importa, no la identidad
- Cuando tiene **reglas de validación** propias
- Para evitar "Primitive Obsession" (usar String para todo)

**Ejemplos comunes:**
- `Money` (cantidad + moneda)
- `Email` (string con validación)
- `Address` (calle, ciudad, código postal)
- `DateRange` (desde, hasta)
- `PhoneNumber` (string con formato)

**En nuestro proyecto:**
```java
// Username es un Value Object
public final class Username {
    private final String value;

    public static Username of(String value) {
        validate(value);  // Valida al crear
        return new Username(value);
    }

    private static void validate(String value) {
        if (value.length() < 3) {
            throw new ValidationException("Too short");
        }
    }
}
```

---

### 3. Aggregate Root (Raíz de Agregado)

**¿Qué es?**
Una **Entity especial** que es el punto de entrada a un grupo de objetos relacionados (el Agregado).

**Características:**
- Es una **Entity** (tiene ID)
- **Controla el acceso** a objetos relacionados
- **Garantiza la consistencia** de todo el grupo
- Solo el Aggregate Root puede ser accedido desde fuera

**Ejemplo Visual:**
```
┌─────────────────────────────────────┐
│  Order (Aggregate Root)             │
│  - id: UUID                          │
│  - totalAmount: Money                │
│  └─ orderItems: List<OrderItem>     │  ← No se acceden directamente
│     └─ product: Product              │
└─────────────────────────────────────┘
         ↑
    Solo acceso
    por aquí
```

**Reglas:**
- ✅ `order.addItem(product, quantity)` → A través del Aggregate Root
- ❌ `orderItem.setQuantity(5)` → NO acceder directamente

**¿Por qué?**
Para garantizar que **las reglas de negocio se cumplan siempre**.

**Ejemplo:**
```java
public class Order {  // Aggregate Root
    private List<OrderItem> items;

    // ✅ Método público para agregar items (valida reglas)
    public void addItem(Product product, int quantity) {
        if (quantity <= 0) throw new ValidationException("Invalid quantity");
        if (items.size() >= 100) throw new BusinessException("Too many items");
        items.add(new OrderItem(product, quantity));
        recalculateTotal();  // Mantiene consistencia
    }

    // ❌ No hay setItems() público
}
```

**En nuestro proyecto:**
- `User` es un Aggregate Root simple (sin agregados relacionados)
- Controla sus propios `Username` y `Email`

---

### 4. Domain Event (Evento de Dominio)

**¿Qué es?**
Algo importante que **ya sucedió** en el dominio.

**Características:**
- **Inmutable** (es un hecho del pasado, no cambia)
- Nombrado en **pasado** (UserCreated, OrderPlaced, PaymentProcessed)
- Contiene toda la información del evento

**Ejemplo:**
```java
// Evento: "Un usuario fue creado"
public record UserCreatedEvent(
    UUID userId,
    String username,
    String email,
    Instant occurredAt
) {
    public static UserCreatedEvent from(UUID userId, String username, String email) {
        return new UserCreatedEvent(userId, username, email, Instant.now());
    }
}
```

**¿Para qué sirven?**
1. **Comunicar** a otras partes del sistema que algo pasó
2. **Desacoplar** componentes (el que publica no sabe quién escucha)
3. **Auditoría** (registro de lo que pasó)
4. **Event Sourcing** (reconstruir el estado desde eventos)

**Ejemplo de uso:**
```java
// En el Service
User user = User.create(username, email);
userRepository.save(user);

// Publicar evento
UserCreatedEvent event = UserCreatedEvent.from(user.getId(), ...);
eventPublisher.publish(event);  // Otros servicios pueden escuchar
```

**Quién puede escuchar:**
- Servicio de Email → envía correo de bienvenida
- Servicio de Auditoría → registra el evento
- Servicio de Analytics → cuenta usuarios nuevos

---

### 5. Domain Service (Servicio de Dominio)

**¿Qué es?**
Lógica de dominio que **no pertenece a ninguna Entity** específica.

**Cuándo usar:**
- Cuando la operación involucra **múltiples Aggregates**
- Cuando no hay un objeto natural donde poner la lógica

**Ejemplo:**
```java
// ❌ No poner en User (no es su responsabilidad)
public class User {
    public void transferMoneyTo(User other, Money amount) {  // NO
        // Involucra dos usuarios... ¿dónde va?
    }
}

// ✅ Crear un Domain Service
@Service
public class MoneyTransferService {
    public void transfer(User from, User to, Money amount) {
        from.withdraw(amount);  // Lógica en cada User
        to.deposit(amount);
        // La coordinación está en el Service
    }
}
```

**En nuestro proyecto:**
- No tenemos Domain Services (nuestro dominio es simple)
- `CreateUserService` es un **Application Service**, no Domain Service

**Diferencia:**
- **Domain Service**: Lógica de negocio pura (sin frameworks)
- **Application Service**: Orquestación de casos de uso (con @Service de Spring)

---

### 6. Repository (Repositorio)

**¿Qué es?**
Una **abstracción** para acceder a Aggregates, como si fuera una colección en memoria.

**Características:**
- Es una **interfaz** (no implementación)
- Trabaja con **Aggregates**, no con tablas
- Oculta los detalles de persistencia

**Ejemplo:**
```java
// Interfaz (en Application/Domain)
public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByUsername(String username);
}

// Uso (parece una colección)
User user = userRepository.findById(id).orElseThrow();
user.disable();
userRepository.save(user);  // Como agregar a una lista
```

**Ventajas:**
- El dominio no sabe si es SQL, NoSQL, API, archivo, etc.
- Fácil cambiar la implementación
- Fácil mockear en tests

---

### 7. Factory (Fábrica)

**¿Qué es?**
Lógica para **crear objetos complejos**.

**Cuándo usar:**
- Cuando crear un objeto requiere lógica compleja
- Cuando hay múltiples formas de crear un objeto

**Ejemplo:**
```java
public class User {
    // Factory Method estático
    public static User create(String username, String email) {
        validate(username, email);
        return new User(
            UUID.randomUUID(),
            Username.of(username),
            Email.of(email),
            true,  // enabled por defecto
            Instant.now()
        );
    }

    // Factory Method para reconstruir desde BD
    public static User reconstitute(UUID id, String username, ...) {
        return new User(id, Username.of(username), ...);
    }
}
```

---

## Ejemplos Prácticos en el Proyecto

### Estructura DDD en Nuestro Código

```
user/domain/
├── model/
│   └── User.java              ← Entity & Aggregate Root
├── valueobject/
│   ├── Email.java             ← Value Object
│   └── Username.java          ← Value Object
├── event/
│   └── UserCreatedEvent.java  ← Domain Event
└── exception/
    ├── DomainException.java
    └── ValidationException.java
```

### Ejemplo Completo: Crear Usuario

```java
// 1. Value Objects (auto-validan)
Username username = Username.of("johndoe");  // Valida longitud
Email email = Email.of("john@example.com");  // Valida formato

// 2. Aggregate Root (Factory Method)
User user = User.create("johndoe", "john@example.com");
// Internamente crea los Value Objects y valida

// 3. Repository (abstracción de persistencia)
userRepository.save(user);

// 4. Domain Event (comunicar lo que pasó)
UserCreatedEvent event = UserCreatedEvent.from(
    user.getId(),
    user.getUsername().getValue(),
    user.getEmail().getValue()
);
eventPublisher.publish(event);
```

---

## Errores Comunes

### ❌ Error 1: Poner Lógica de Negocio Fuera del Dominio

```java
// MAL: Lógica en el Controller
@PostMapping("/users")
public User create(@RequestBody String username) {
    if (username.length() < 3) {  // ¡Validación en el Controller!
        throw new Exception("Too short");
    }
    return userRepository.save(new User(username));
}

// BIEN: Lógica en el Dominio
User user = User.create(username);  // User valida internamente
```

### ❌ Error 2: Entities Anémicas

```java
// MAL: Entity sin comportamiento (solo getters/setters)
public class User {
    private String username;
    public void setUsername(String username) { this.username = username; }
    public String getUsername() { return username; }
}

// BIEN: Entity con comportamiento
public class User {
    private Username username;

    public void changeUsername(String newUsername) {
        validateCanChange();  // Lógica de negocio
        this.username = Username.of(newUsername);
        addEvent(new UsernameChangedEvent(...));
    }
}
```

### ❌ Error 3: Value Objects Mutables

```java
// MAL: Value Object mutable
public class Email {
    private String value;
    public void setValue(String value) { this.value = value; }  // NO!
}

// BIEN: Value Object inmutable
public final class Email {
    private final String value;  // final!
    public Email(String value) { this.value = value; }
    // Sin setters
}
```

### ❌ Error 4: Acceder Directamente a Objetos del Agregado

```java
// MAL: Modificar OrderItem directamente
OrderItem item = order.getItems().get(0);
item.setQuantity(5);  // ¡Bypasea las reglas del Order!

// BIEN: Modificar a través del Aggregate Root
order.changeItemQuantity(itemId, 5);  // Order valida y mantiene consistencia
```

---

## Eventos: Domain Events vs Integration Events

### ¿Qué son los Eventos?

Un **evento** es algo que **ya pasó** en el sistema. Es un hecho inmutable del pasado.

**Ejemplos:**
- ✅ `UserCreatedEvent` - "Un usuario fue creado"
- ✅ `OrderPlacedEvent` - "Un pedido fue realizado"
- ✅ `PaymentCompletedEvent` - "Un pago se completó"

**Características:**
- 🕐 **Tiempo pasado**: "UserCreated", no "CreateUser"
- 🔒 **Inmutable**: No se pueden modificar
- 📢 **Comunicación**: Avisan a otros componentes

---

### 🎯 Eventos vs Llamadas Síncronas: ¿Cuándo Usar Eventos?

#### ❌ NO uses eventos cuando:

```java
// INCORRECTO: Validación crítica como evento
public void createUser(Username username, Email email) {
    User user = User.create(username, email);
    userRepository.save(user);

    // ❌ MAL: Validar email como evento
    eventPublisher.publish(new ValidateEmailEvent(email));

    // Problema: ¿Qué pasa si falla? El usuario ya está guardado
}
```

**No uses eventos para:**
- ❌ Validaciones que pueden fallar
- ❌ Operaciones que DEBEN ejecutarse (críticas)
- ❌ Cuando necesitas el resultado inmediatamente
- ❌ Transacciones distribuidas (2-phase commit)

#### ✅ SÍ usa eventos cuando:

```java
// CORRECTO: Side effects no críticos como eventos
public UserResult createUser(CreateUserCommand command) {
    // 1. Lógica crítica: síncrona
    User user = User.create(command.username(), command.email());
    userRepository.save(user);

    // 2. Side effects: eventos (pueden fallar sin afectar la creación)
    eventPublisher.publish(new UserCreatedEvent(
        user.getId(),
        user.getUsername(),
        user.getEmail()
    ));

    return UserResult.success(user);
}

// Listeners reaccionan independientemente
@EventListener
public void onUserCreated(UserCreatedEvent event) {
    emailService.sendWelcome(event.email());  // Si falla, usuario igual existe
}
```

**Usa eventos para:**
- ✅ Notificaciones (emails, SMS, push)
- ✅ Estadísticas/Analytics (no críticas)
- ✅ Auditoría/Logging
- ✅ Sincronización con otros servicios
- ✅ Desacoplar componentes
- ✅ Procesos que pueden ejecutarse después

**Regla de oro:** Si puede fallar y no debe afectar la operación principal → evento

---

### 📦 Tipos de Eventos: Domain vs Integration

Hay **dos tipos principales** de eventos en microservicios:

#### 1️⃣ Domain Events (Eventos de Dominio)

**Qué son:** Eventos **internos** dentro del mismo servicio/bounded context

**Tecnología:** Spring Events (in-memory), EventBus interno

**Ejemplo en este proyecto:**
```java
// Publisher
@Component
@Primary
public class SpringEventUserEventPublisherAdapter implements UserEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public void publish(UserCreatedEvent event) {
        eventPublisher.publishEvent(event);  // ✅ In-memory
    }
}

// Listeners (mismo servicio)
@Component
public class SendWelcomeEmailListener {
    @EventListener  // ✅ Se ejecuta automáticamente
    public void onUserCreated(UserCreatedEvent event) {
        emailService.sendWelcome(event.email());
    }
}

@Component
@Order(2)  // Se ejecuta después del listener anterior
public class UpdateUserStatsListener {
    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        statsService.incrementTotalUsers();
    }
}
```

**Características:**
- ✅ Mismo proceso (JVM)
- ✅ Rápido (memoria)
- ✅ Simple (sin infraestructura externa)
- ❌ Si la app se cae, eventos se pierden
- ❌ Solo para el mismo servicio

**Cuándo usarlos:**
- Desacoplar lógica dentro del mismo servicio
- Side effects locales (email, cache, stats)
- No necesitas durabilidad

---

#### 2️⃣ Integration Events (Eventos de Integración)

**Qué son:** Eventos **entre servicios** diferentes (microservicios)

**Tecnología:** Kafka, RabbitMQ, AWS SNS/SQS, Google Pub/Sub

**Ejemplo en este proyecto:**
```java
// Publisher (User Service)
@Component
public class KafkaUserEventPublisherAdapter implements UserEventPublisher {
    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    public void publish(UserCreatedEvent event) {
        kafkaTemplate.send(
            "user.created",              // Topic
            event.userId().toString(),    // Key (para ordenamiento)
            event                         // Event
        );
    }
}

// Consumer (Notifications Service - otro microservicio)
@Component
public class UserEventsKafkaConsumer {
    @KafkaListener(topics = "user.created")
    public void consume(UserCreatedEvent event) {
        // Este código está en OTRO microservicio
        notificationService.sendWelcomeEmail(event.email());
    }
}
```

**Características:**
- ✅ Entre servicios diferentes
- ✅ Duradero (persistido en Kafka)
- ✅ Escalable (múltiples consumers)
- ✅ Replay posible (volver a procesar eventos)
- ✅ Comunicación asíncrona
- ❌ Más complejo (infraestructura)
- ❌ Latencia mayor que in-memory

**Cuándo usarlos:**
- Comunicar bounded contexts diferentes
- Sincronizar datos entre microservicios
- Event sourcing
- Necesitas durabilidad/replay

---

### 🔑 Kafka: Particiones, Claves y Ordenamiento

#### ¿Por qué importan las claves (keys)?

Kafka usa la **clave** para decidir a qué **partición** enviar el mensaje.

**Sin clave:**
```java
// ❌ Sin clave: orden NO garantizado
kafkaTemplate.send("user.created", event);

// Resultado: Eventos del mismo usuario en particiones diferentes
// Partition 0: UserUpdated(userId=123)
// Partition 2: UserCreated(userId=123)  ← ¡Desorden!
// Partition 1: UserDeleted(userId=123)
```

**Con clave:**
```java
// ✅ Con clave (userId): orden garantizado para el mismo usuario
kafkaTemplate.send(
    "user.created",
    event.userId().toString(),  // ← Key = userId
    event
);

// Resultado: Todos los eventos del mismo usuario en la MISMA partición
// Partition 0: UserCreated(userId=123) → UserUpdated(userId=123) → UserDeleted(userId=123)
//              ↑ Orden garantizado
```

**Regla:**
> Mensajes con la **misma clave** van a la **misma partición** y se procesan **en orden**

---

#### Cómo elegir la clave

| **Caso de Uso**              | **Clave Recomendada**        | **Por qué**                                      |
|------------------------------|------------------------------|--------------------------------------------------|
| Eventos de Usuario           | `userId`                     | Procesar eventos del mismo usuario en orden     |
| Eventos de Pedido            | `orderId`                    | Procesar eventos del mismo pedido en orden      |
| Eventos de Cuenta Bancaria   | `accountId`                  | Operaciones de la misma cuenta en orden         |
| Eventos de Chat              | `conversationId`             | Mensajes de la misma conversación en orden      |
| Logs genéricos               | `null` o random              | No importa el orden                              |

**Ejemplo real:**
```java
@Component
public class KafkaUserEventPublisherAdapter implements UserEventPublisher {

    public void publish(UserCreatedEvent event) {
        // ✅ CORRECTO: userId como clave
        kafkaTemplate.send(
            "user.created",
            event.userId().toString(),  // ← Todos los eventos del mismo user en orden
            event
        );
    }
}
```

---

#### Particiones: ¿Cuántas crear?

**Regla simple:**
```
Particiones = Número de consumers que quieres en paralelo
```

**Ejemplo:**
- 1 partición = 1 consumer máximo (sin paralelismo)
- 3 particiones = hasta 3 consumers en paralelo
- 10 particiones = hasta 10 consumers en paralelo

**Más particiones = más paralelismo = más throughput**

**Pero cuidado:**
- ❌ Demasiadas particiones = overhead (complejidad, más archivos)
- ✅ Empieza con 3-6 particiones, escala según necesidad

---

### 🏗️ Implementación Dual: Spring Events + Kafka

En este proyecto, publicamos a **ambos sistemas simultáneamente**:

```java
@Component
public class CompositeUserEventPublisherAdapter implements UserEventPublisher {

    private final ApplicationEventPublisher springEventPublisher;  // In-memory
    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;  // Kafka

    @Override
    public void publish(UserCreatedEvent event) {
        // 1. Publicar in-memory (para listeners locales)
        springEventPublisher.publishEvent(event);

        // 2. Publicar a Kafka (para otros microservicios)
        kafkaTemplate.send("user.created", event.userId().toString(), event);
    }
}
```

**Por qué dual publishing:**
- ✅ Listeners locales (SendWelcomeEmailListener) se ejecutan inmediatamente
- ✅ Otros microservicios (Notifications Service) reciben el evento vía Kafka
- ✅ Lo mejor de ambos mundos

---

### 📋 Mejores Prácticas

#### 1. Nombrado de Topics

```java
// ✅ RECOMENDADO: Dotted notation (más común)
"user.created"
"user.updated"
"order.placed"
"payment.completed"

// ✅ También válido: Hyphenated
"user-created"
"order-placed"

// ✅ Namespaced (más formal)
"com.example.user.created"

// ❌ Evitar: Mezclados o confusos
"userCreated"
"User_Created"
"create_user"
```

**Recomendación:** Usa **dotted notation** (`user.created`) - es lo más común en la industria

---

#### 2. Estructura del Evento

```java
// ✅ CORRECTO: Record inmutable, tiempo pasado, datos completos
public record UserCreatedEvent(
    UUID userId,           // ID del aggregate
    String username,       // Datos necesarios
    String email,
    Instant occurredAt,    // ✅ Timestamp importante
    UUID correlationId     // ✅ Para tracing/debugging
) {
    // Factory method
    public static UserCreatedEvent from(User user) {
        return new UserCreatedEvent(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            Instant.now(),
            UUID.randomUUID()
        );
    }
}

// ❌ INCORRECTO: Evento con solo el ID
public record UserCreatedEvent(UUID userId) {}
// Problema: Consumer necesita llamar a User Service para obtener datos
```

**Regla:** Incluye **todos los datos que los consumers necesitan** para evitar llamadas síncronas

---

#### 3. Orden de Ejecución (Spring Events)

```java
// Sin @Order: orden aleatorio
@EventListener
public void listener1(UserCreatedEvent event) { }

@EventListener
public void listener2(UserCreatedEvent event) { }

// Con @Order: orden garantizado
@EventListener
@Order(1)  // ✅ Se ejecuta primero
public void sendEmail(UserCreatedEvent event) { }

@EventListener
@Order(2)  // ✅ Se ejecuta segundo
public void updateStats(UserCreatedEvent event) { }
```

---

#### 4. Manejo de Errores

```java
@EventListener
public void onUserCreated(UserCreatedEvent event) {
    try {
        emailService.send(event.email());
    } catch (Exception e) {
        // ⚠️ Decisión importante: ¿Qué hacer si falla?

        // Opción 1: Loguear y continuar (evento no crítico)
        logger.error("Failed to send email: {}", e.getMessage());
        // No lanzar excepción → usuario se crea igual

        // Opción 2: Lanzar excepción (evento crítico)
        throw new EmailException("Cannot create user without email", e);
        // Lanza excepción → rollback de la transacción completa
    }
}
```

**Usa `@TransactionalEventListener` para control fino:**
```java
// ✅ Se ejecuta DESPUÉS del commit (aunque falle, usuario ya existe)
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onUserCreated(UserCreatedEvent event) {
    emailService.send(event.email());
    // Si falla, no afecta la creación del usuario
}
```

---

### 🎯 Comparación Rápida

| **Aspecto**           | **Domain Events (Spring)**       | **Integration Events (Kafka)**     |
|-----------------------|----------------------------------|------------------------------------|
| **Alcance**           | Mismo servicio (JVM)             | Entre servicios (microservicios)   |
| **Tecnología**        | Spring ApplicationEventPublisher | Kafka, RabbitMQ, SNS/SQS           |
| **Velocidad**         | ⚡ Muy rápido (memoria)          | 🐌 Más lento (red)                |
| **Durabilidad**       | ❌ Se pierde si app cae          | ✅ Persistido en disco            |
| **Orden garantizado** | ✅ Sí (con @Order)               | ✅ Sí (misma key + misma partition)|
| **Complejidad**       | 🟢 Simple                        | 🟡 Media (infraestructura)        |
| **Cuándo usar**       | Side effects locales             | Comunicar microservicios           |
| **Ejemplo**           | SendWelcomeEmailListener         | Notifications Service (otro MS)    |

---

### 📂 Archivos en el Proyecto

**Domain Events (Spring Events):**
- `SpringEventUserEventPublisherAdapter.java` - Publisher (@Primary)
- `SendWelcomeEmailListener.java` - Listener de email
- `UpdateUserStatsListener.java` - Listener de estadísticas

**Integration Events (Kafka):**
- `KafkaUserEventPublisherAdapter.java` - Publisher a Kafka
- `UserEventsKafkaConsumer.java` - Consumer (Notifications Service simulado)
- `UserCreatedEventDLTConsumer.java` - Consumer para Dead Letter Topic
- `KafkaConfig.java` - Configuración con DLT automático
- `docker-compose.yml` - Kafka + Zookeeper

---

### 💀 Dead Letter Topic (DLT) - Manejo de Errores

**¿Qué es un Dead Letter Topic?**

Un DLT es un topic especial donde se envían mensajes que **fallaron al procesarse** después de múltiples reintentos.

**Problema sin DLT:**
```java
@KafkaListener(topics = "user.created")
public void consume(UserCreatedEvent event) {
    emailService.send(event.email());  // ❌ Falla siempre
}

// Resultado:
// 1. Consumer falla
// 2. Kafka reintenta → Falla
// 3. Kafka reintenta → Falla
// 4. Loop infinito 🔄
// 5. Consumer bloqueado, no procesa mensajes siguientes ❌
```

**Solución con DLT:**
```java
// Configuración en KafkaConfig
DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
DefaultErrorHandler errorHandler = new DefaultErrorHandler(
    recoverer,
    new FixedBackOff(1000L, 3L)  // 3 reintentos, 1 segundo entre cada uno
);
factory.setCommonErrorHandler(errorHandler);

// Resultado:
// 1. Consumer falla
// 2. Espera 1s, reintenta (1/3) → Falla
// 3. Espera 1s, reintenta (2/3) → Falla
// 4. Espera 1s, reintenta (3/3) → Falla
// 5. Mensaje va a "user.created.dlt" ✅
// 6. Consumer continúa con siguiente mensaje ✅
```

---

#### Flujo Completo con DLT

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. Mensaje en topic "user.created"                              │
│    UserCreatedEvent publicado                                   │
└────────┬────────────────────────────────────────────────────────┘
         ↓
┌────────┴────────────────────────────────────────────────────────┐
│ 2. UserEventsKafkaConsumer intenta procesar                     │
│    ❌ Falla (email service down, bug, datos inválidos...)       │
└────────┬────────────────────────────────────────────────────────┘
         ↓
┌────────┴────────────────────────────────────────────────────────┐
│ 3. DefaultErrorHandler reintenta automáticamente                │
│    • Espera 1 segundo → Reintento 1/3 → ❌ Falla               │
│    • Espera 1 segundo → Reintento 2/3 → ❌ Falla               │
│    • Espera 1 segundo → Reintento 3/3 → ❌ Falla               │
└────────┬────────────────────────────────────────────────────────┘
         ↓
┌────────┴────────────────────────────────────────────────────────┐
│ 4. DeadLetterPublishingRecoverer publica a DLT                  │
│    Topic: "user.created.dlt"                                    │
│    Headers agregados:                                           │
│    • kafka_dlt-original-topic: "user.created"                   │
│    • kafka_dlt-exception-message: "Service unavailable"         │
│    • kafka_dlt-exception-stacktrace: "..."                      │
└────────┬────────────────────────────────────────────────────────┘
         ↓
┌────────┴────────────────────────────────────────────────────────┐
│ 5. Consumer original continúa con siguiente mensaje ✅           │
│    No se bloquea, sigue procesando                              │
└─────────────────────────────────────────────────────────────────┘

         ↓ (en paralelo)

┌─────────────────────────────────────────────────────────────────┐
│ 6. UserCreatedEventDLTConsumer recibe mensaje fallido           │
│    • Loguea el error para investigación                         │
│    • Opcionalmente: guarda en BD, envía alerta                  │
└─────────────────────────────────────────────────────────────────┘
```

---

#### Configuración en este Proyecto

**KafkaConfig.java:**
```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent>
    kafkaListenerContainerFactory(
        ConsumerFactory<String, UserCreatedEvent> consumerFactory,
        KafkaTemplate<String, UserCreatedEvent> kafkaTemplate) {

    ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);

    // ✨ DLT AUTOMÁTICO
    DeadLetterPublishingRecoverer recoverer =
        new DeadLetterPublishingRecoverer(kafkaTemplate);

    DefaultErrorHandler errorHandler = new DefaultErrorHandler(
        recoverer,
        new FixedBackOff(1000L, 3L)  // 3 reintentos, 1s entre cada uno
    );

    factory.setCommonErrorHandler(errorHandler);

    return factory;
}
```

**Consumer para DLT:**
```java
@Component
public class UserCreatedEventDLTConsumer {

    @KafkaListener(
        topics = "user.created.dlt",
        groupId = "notifications-service-dlt"
    )
    public void consumeFailedMessage(
            @Payload UserCreatedEvent event,
            ConsumerRecord<String, UserCreatedEvent> record) {

        // Loguear mensaje fallido
        logger.error("Failed message: {}", event);

        // Extraer información del error
        String error = getHeader(record, "kafka_dlt-exception-message");
        logger.error("Error: {}", error);

        // Guardar en BD para análisis posterior (recomendado)
        // failedMessageRepository.save(event, error);
    }
}
```

---

#### Casos de Uso Reales

**1. Error Transitorio (Service Down)**
```java
// Email service está caído temporalmente
@KafkaListener(topics = "user.created")
public void consume(UserCreatedEvent event) {
    emailService.send(event.email());  // ❌ Timeout
}

// Resultado:
// 1. Falla 3 veces → mensaje va a DLT
// 2. Email service se recupera
// 3. Reprocesas mensajes del DLT manualmente
// 4. ✅ Emails enviados exitosamente
```

**2. Error Permanente (Datos Inválidos)**
```java
@KafkaListener(topics = "user.created")
public void consume(UserCreatedEvent event) {
    emailService.send(event.email());  // ❌ Email inválido
}

// Resultado:
// 1. Falla 3 veces → mensaje va a DLT
// 2. Investigas: email es "invalid@"
// 3. Corriges datos en BD
// 4. Reprocesas mensaje con datos corregidos
```

**3. Bug en Código**
```java
@KafkaListener(topics = "user.created")
public void consume(UserCreatedEvent event) {
    String name = event.username().toUpperCase();  // ❌ NullPointerException
}

// Resultado:
// 1. Falla 3 veces → mensaje va a DLT
// 2. Identificas el bug
// 3. Despliegas fix
// 4. Reprocesas mensajes del DLT
// 5. ✅ Todos procesados correctamente
```

---

#### Qué Hacer con Mensajes en DLT

**Opción 1: Loguear (básico)**
```java
@KafkaListener(topics = "user.created.dlt")
public void consumeDLT(UserCreatedEvent event) {
    logger.error("Failed event: {}", event);
    // Ver logs y debuguear manualmente
}
```

**Opción 2: Guardar en BD (recomendado)**
```java
@KafkaListener(topics = "user.created.dlt")
public void consumeDLT(UserCreatedEvent event, ConsumerRecord record) {
    String error = getHeader(record, "kafka_dlt-exception-message");

    failedMessageRepository.save(new FailedMessage(
        "user.created",
        event.userId().toString(),
        objectMapper.writeValueAsString(event),
        error,
        Instant.now()
    ));

    // Dashboard para ver mensajes fallidos
}
```

**Opción 3: Enviar Alertas**
```java
@KafkaListener(topics = "user.created.dlt")
public void consumeDLT(UserCreatedEvent event) {
    // Alerta a Slack/PagerDuty
    alertService.sendAlert(
        "DLT Alert: Failed to process user " + event.username(),
        AlertSeverity.HIGH
    );
}
```

**Opción 4: Reprocesar Automáticamente**
```java
@RestController
@RequestMapping("/admin/dlt")
public class DLTController {

    @PostMapping("/retry")
    public String retryDLT() {
        // Lee mensajes del DLT
        List<UserCreatedEvent> events = dltService.getFailedMessages();

        // Republica al topic original
        events.forEach(event -> {
            kafkaTemplate.send("user.created", event.userId(), event);
        });

        return "Retried " + events.size() + " messages";
    }
}
```

---

#### Mejores Prácticas para DLT

1. **Monitorear el tamaño del DLT**
   - Si crece mucho → algo está mal
   - Configurar alertas (ej: si DLT > 100 mensajes)

2. **Guardar mensajes en BD**
   - No solo loguear
   - Permite análisis posterior y dashboard

3. **Diferentes reintentos según error**
   - Errors transitorios: más reintentos (5-10)
   - Errors permanentes: menos reintentos (2-3)

4. **Backoff exponencial para errors transitorios**
   ```java
   // En lugar de FixedBackOff(1000L, 3L)
   new ExponentialBackOff(1000L, 2.0)  // 1s, 2s, 4s, 8s...
   ```

5. **Separar DLTs por tipo de error**
   ```java
   // DLT para errores transitorios (reintentables)
   "user.created.dlt.retry"

   // DLT para errores permanentes (no reintentables)
   "user.created.dlt.permanent"
   ```

---

#### Comandos Útiles

**Ver mensajes en DLT:**
```bash
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic user.created.dlt \
  --from-beginning \
  --property print.headers=true
```

**Contar mensajes en DLT:**
```bash
kafka-run-class.sh kafka.tools.GetOffsetShell \
  --broker-list localhost:9092 \
  --topic user.created.dlt
```

**Consumir con headers (ver información del error):**
```bash
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic user.created.dlt \
  --from-beginning \
  --property print.key=true \
  --property print.headers=true \
  --property print.timestamp=true
```

---

### ⚡ Circuit Breaker - Resiliencia ante Fallos

**¿Qué es un Circuit Breaker?**

Un Circuit Breaker (Disyuntor) es un patrón de resiliencia que **previene cascading failures** (fallos en cascada) cuando un servicio externo está caído o lento.

**Analogía Simple:**

Como el interruptor eléctrico de tu casa:
- Hay un **cortocircuito** → el interruptor se **abre** automáticamente (protege)
- Después de un tiempo → intentas **cerrarlo** (reconectar)
- Si funciona → **sigue conectado** (CLOSED ✅)
- Si sigue fallando → se vuelve a **abrir** (OPEN ❌)

**Problema sin Circuit Breaker:**

```java
// Email Service está caído
@KafkaListener(topics = "user.created")
public void consume(UserCreatedEvent event) {
    emailService.send(event.email());  // ❌ Timeout de 30 segundos cada vez
}

// Resultado:
// 1. Cada mensaje espera 30s de timeout ⏱️
// 2. Threads bloqueados esperando ❌
// 3. Kafka consumer no procesa otros mensajes 🔄
// 4. Todo el sistema se vuelve lento 💀
// 5. Cascading failure: todo se cae ⚠️
```

**Solución con Circuit Breaker:**

```java
@Service
public class EmailService {

    @CircuitBreaker(name = "emailService", fallbackMethod = "sendEmailFallback")
    public void sendWelcomeEmail(String email, String username) {
        // Intenta enviar email
        externalEmailService.send(email, username);
    }

    // Método fallback (se ejecuta si Circuit está OPEN)
    private void sendEmailFallback(String email, String username, Exception ex) {
        logger.warn("Circuit breaker OPEN - Email not sent");
        // Guardar en cola para reintentar después
        emailQueueRepository.save(new PendingEmail(email, username));
    }
}

// Resultado:
// 1. Primeras llamadas fallan → Circuit detecta alta tasa de fallos
// 2. Circuit cambia a OPEN ✅
// 3. Llamadas subsecuentes NO esperan timeout → Fail-fast ⚡
// 4. Llama a fallback inmediatamente
// 5. Threads liberados, sistema sigue funcionando ✅
// 6. Después de N segundos → Circuit prueba reconectar (HALF_OPEN)
```

---

#### Estados del Circuit Breaker

El Circuit Breaker funciona como una **máquina de estados finitos** con 3 estados:

```
┌──────────────────────────────────────────────────────────────┐
│ CLOSED (Cerrado) - Estado Normal                             │
│                                                               │
│ • Todas las llamadas pasan al servicio ✅                    │
│ • Monitorea tasa de fallos continuamente                     │
│ • Si fallos >= threshold → Cambia a OPEN                     │
│                                                               │
│ Ejemplo:                                                      │
│   10 llamadas → 2 fallos (20%) → Circuit sigue CLOSED        │
│   10 llamadas → 6 fallos (60%) → Circuit cambia a OPEN ❌    │
└──────────────┬───────────────────────────────────────────────┘
               │ (demasiados fallos)
               ↓
┌──────────────┴───────────────────────────────────────────────┐
│ OPEN (Abierto) - Protección Activa                           │
│                                                               │
│ • NO ejecuta el servicio ❌                                  │
│ • Falla inmediatamente (fail-fast) ⚡                        │
│ • Llama a método fallback                                    │
│ • Después de timeout → Cambia a HALF_OPEN                    │
│                                                               │
│ Ejemplo:                                                      │
│   Llamada 1 → Fallback inmediato (sin esperar timeout)       │
│   Llamada 2 → Fallback inmediato                             │
│   ... (pasan 10 segundos) ...                                │
│   → Circuit cambia a HALF_OPEN para probar si se recuperó    │
└──────────────┬───────────────────────────────────────────────┘
               │ (después de waitDurationInOpenState)
               ↓
┌──────────────┴───────────────────────────────────────────────┐
│ HALF_OPEN (Semi-abierto) - Probando Recuperación            │
│                                                               │
│ • Permite N llamadas de prueba (ej: 3)                       │
│ • Si funcionan → Cambia a CLOSED ✅                          │
│ • Si fallan → Vuelve a OPEN ❌                               │
│                                                               │
│ Ejemplo:                                                      │
│   Llamada prueba 1 → ✅ Éxito                                │
│   Llamada prueba 2 → ✅ Éxito                                │
│   Llamada prueba 3 → ✅ Éxito                                │
│   → Circuit cambia a CLOSED (servicio recuperado) ✅         │
│                                                               │
│   O bien:                                                     │
│   Llamada prueba 1 → ❌ Fallo                                │
│   Llamada prueba 2 → ❌ Fallo                                │
│   → Circuit vuelve a OPEN (servicio aún caído) ❌            │
└──────────────────────────────────────────────────────────────┘
```

---

#### Flujo Completo con Circuit Breaker

**Escenario: Email Service se cae durante 1 minuto**

```
T=0s   UserEventsKafkaConsumer recibe 20 eventos
       ↓
       EmailService.sendWelcomeEmail(...)
       ↓
       Circuit Breaker está CLOSED (estado normal)
       ↓
       Llama a servicio externo de email
       ↓
T=1s   ❌ Fallo 1/10 (email service down)
T=2s   ❌ Fallo 2/10
T=3s   ❌ Fallo 3/10
T=4s   ❌ Fallo 4/10
T=5s   ❌ Fallo 5/10  → minimum-number-of-calls alcanzado
T=6s   ❌ Fallo 6/10  → 60% failure rate (> 50% threshold)
       ↓
       ⚠️  Circuit Breaker detecta: "6/10 fallos = 60% > 50%"
       ↓
       Circuit cambia a OPEN ❌
       ═════════════════════════════════════════════════════════

T=7s   🔔 Llega evento 11
       ↓
       EmailService.sendWelcomeEmail(...)
       ↓
       Circuit Breaker está OPEN
       ↓
       ⚡ NO llama al servicio → Falla inmediatamente (fail-fast)
       ↓
       Ejecuta sendEmailFallback() → Guarda en cola
       ↓
       ✅ Consumer continúa sin bloquearse

T=8s   🔔 Llega evento 12 → Fallback inmediato ⚡
T=9s   🔔 Llega evento 13 → Fallback inmediato ⚡
...
T=17s  (Pasan 10 segundos en estado OPEN)
       ↓
       Circuit cambia a HALF_OPEN (probando recuperación)
       ═════════════════════════════════════════════════════════

T=18s  🔔 Llega evento 14 (llamada de prueba 1/3)
       ↓
       Circuit está HALF_OPEN → Permite llamada de prueba
       ↓
       ✅ Email service se recuperó → Éxito

T=19s  🔔 Llega evento 15 (llamada de prueba 2/3)
       ↓
       ✅ Éxito

T=20s  🔔 Llega evento 16 (llamada de prueba 3/3)
       ↓
       ✅ Éxito
       ↓
       Circuit detecta: "3/3 pruebas exitosas"
       ↓
       Circuit cambia a CLOSED ✅
       ═════════════════════════════════════════════════════════

T=21s  🔔 Eventos 17-20 → Todo funciona normal ✅
```

**Resumen:**
- Eventos 1-10: Enviados con fallos (circuit CLOSED)
- Eventos 11-13: Fallback inmediato, consumer no se bloquea (circuit OPEN)
- Eventos 14-16: Pruebas de recuperación (circuit HALF_OPEN)
- Eventos 17-20: Funcionamiento normal (circuit CLOSED)

---

#### Configuración en este Proyecto

**pom.xml:**
```xml
<!-- Resilience4j (Circuit Breaker, Retry, Rate Limiter) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.1.0</version>
</dependency>
```

**application.yaml:**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      emailService:  # Nombre del circuit breaker
        # Tipo de ventana para calcular tasa de fallos
        # COUNT_BASED = últimas N llamadas
        # TIME_BASED = llamadas en últimos N segundos
        sliding-window-type: COUNT_BASED

        # Tamaño de la ventana deslizante
        # Evalúa tasa de fallos sobre las últimas 10 llamadas
        sliding-window-size: 10

        # Número mínimo de llamadas antes de evaluar
        # Si hay < 5 llamadas, NO abre el circuit (aunque todas fallen)
        # Evita abrir circuit con datos insuficientes
        minimum-number-of-calls: 5

        # Porcentaje de fallos para abrir el circuit
        # Si >= 50% de las últimas 10 llamadas fallan → OPEN
        failure-rate-threshold: 50

        # Tiempo en estado OPEN antes de cambiar a HALF_OPEN
        # Después de 10s, permite llamadas de prueba
        wait-duration-in-open-state: 10s

        # Número de llamadas permitidas en HALF_OPEN
        # Permite 3 llamadas de prueba para ver si se recuperó
        permitted-number-of-calls-in-half-open-state: 3

        # Tasa de llamadas lentas para considerar fallo
        # Si >= 50% son lentas → también abre circuit
        slow-call-rate-threshold: 50

        # Duración para considerar una llamada "lenta"
        # Si tarda > 5s → cuenta como fallo
        slow-call-duration-threshold: 5s

        # Exponer métricas en /actuator/health
        register-health-indicator: true

        # Excepciones que cuentan como fallos
        record-exceptions:
          - java.lang.RuntimeException
          - java.io.IOException
          - java.util.concurrent.TimeoutException
```

**Significado de los parámetros:**

| Parámetro | Valor | Significado |
|-----------|-------|-------------|
| `sliding-window-size` | 10 | Evalúa últimas 10 llamadas |
| `minimum-number-of-calls` | 5 | Necesita ≥5 llamadas para decidir |
| `failure-rate-threshold` | 50% | Si ≥50% fallan → OPEN |
| `wait-duration-in-open-state` | 10s | Espera 10s antes de HALF_OPEN |
| `permitted-number-of-calls-in-half-open-state` | 3 | 3 pruebas en HALF_OPEN |
| `slow-call-duration-threshold` | 5s | Si tarda >5s → cuenta como fallo |

**Ejemplo de decisión:**

```
Llamadas: ✅ ✅ ❌ ✅ ❌ ❌ ❌ ❌ ✅ ❌
          1  2  3  4  5  6  7  8  9  10

Análisis:
- Total llamadas: 10 ≥ minimum-number-of-calls (5) ✅
- Fallos: 6/10 = 60%
- 60% ≥ failure-rate-threshold (50%) ✅
- Decisión: Circuit cambia a OPEN ❌
```

---

#### Implementación en Código

**EmailService.java:**
```java
package com.example.hexarch.notifications.application.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    /**
     * Envía email de bienvenida con protección de Circuit Breaker
     *
     * @CircuitBreaker:
     *   - name: "emailService" (debe coincidir con application.yaml)
     *   - fallbackMethod: se ejecuta si circuit está OPEN o si falla
     */
    @CircuitBreaker(name = "emailService", fallbackMethod = "sendEmailFallback")
    public void sendWelcomeEmail(String email, String username) {
        logger.info("📧 [EMAIL SERVICE] Sending welcome email to: {}", email);

        // En producción: llamada a servicio externo
        // sendGridClient.send(email, template);
        // sesClient.sendEmail(request);
        // mailgunClient.send(message);

        // Este método puede lanzar excepciones:
        // - RuntimeException (servicio caído)
        // - TimeoutException (timeout)
        // - IOException (red inestable)

        externalEmailService.send(email, username);

        logger.info("✅ [EMAIL SERVICE] Email sent successfully");
    }

    /**
     * Método fallback: se ejecuta cuando Circuit está OPEN
     *
     * IMPORTANTE:
     * - Debe tener la MISMA FIRMA que el método original
     * - Más un parámetro Exception al final
     * - NO debe lanzar excepciones
     */
    private void sendEmailFallback(String email, String username, Exception ex) {
        logger.warn("⚠️  [EMAIL SERVICE - FALLBACK] Circuit breaker is OPEN");
        logger.warn("    Email: {}", email);
        logger.warn("    Reason: {}", ex != null ? ex.getMessage() : "Circuit breaker OPEN");

        // OPCIONES EN PRODUCCIÓN:

        // 1. Guardar en cola para reintentar después (recomendado)
        emailQueueRepository.save(new PendingEmail(email, username, Instant.now()));
        logger.info("Email queued for retry when service recovers");

        // 2. Usar servicio alternativo
        // try {
        //     backupEmailService.send(email, username);
        //     logger.info("Email sent via backup service");
        // } catch (Exception e) {
        //     logger.error("Backup service also failed");
        // }

        // 3. Enviar alerta
        // if (isCritical(email)) {
        //     alertService.sendAlert(
        //         "Email Circuit Breaker OPEN",
        //         "Failed to send email to: " + email,
        //         AlertSeverity.HIGH
        //     );
        // }

        // 4. Incrementar métrica
        // meterRegistry.counter("email.circuit_breaker.fallback").increment();

        logger.info("✅ [EMAIL SERVICE - FALLBACK] Request handled gracefully");
    }
}
```

**Integración con Kafka Consumer:**
```java
@Component
public class UserEventsKafkaConsumer {

    private final EmailService emailService;  // Con Circuit Breaker

    public UserEventsKafkaConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "user.created", groupId = "notifications-service")
    public void consume(@Payload UserCreatedEvent event) {
        logger.info("📨 Received UserCreatedEvent: {}", event);

        try {
            // ⚡ CIRCUIT BREAKER PROTECTION
            // Esta llamada está protegida por Circuit Breaker
            // Si EmailService falla repetidamente:
            // 1. Circuit Breaker detecta tasa de fallos alta
            // 2. Cambia a estado OPEN
            // 3. Llama a sendEmailFallback() en lugar de sendWelcomeEmail()
            // 4. Consumer NO se bloquea esperando timeouts
            // 5. Puede seguir procesando otros mensajes ✅
            emailService.sendWelcomeEmail(event.email(), event.username());

            logger.info("✅ Notification sent successfully");

        } catch (Exception e) {
            // Circuit Breaker ya manejó el fallo con fallback
            // Aquí decides qué hacer con el mensaje de Kafka:

            // Opción 1: NO lanzar excepción → Kafka avanza offset (mensaje "se pierde")
            logger.error("Failed to process notification: {}", e.getMessage());

            // Opción 2: Lanzar excepción → Kafka reintenta o envía a DLT
            // throw new RuntimeException("Failed to process notification", e);
        }
    }
}
```

---

#### Ventajas del Circuit Breaker

**1. Fail-Fast (Fallo Rápido)**
```java
// Sin Circuit Breaker:
emailService.send(email);  // Espera 30s de timeout ⏱️

// Con Circuit Breaker (en estado OPEN):
emailService.send(email);  // Falla en ~1ms ⚡
```

**2. Protección de Recursos**
```
Sin Circuit Breaker:
- Thread 1: bloqueado 30s esperando timeout
- Thread 2: bloqueado 30s esperando timeout
- Thread 3: bloqueado 30s esperando timeout
- ...
- Thread pool exhausted ❌
- Sistema completo bloqueado ❌

Con Circuit Breaker (OPEN):
- Thread 1: falla en 1ms, liberado inmediatamente ✅
- Thread 2: falla en 1ms, liberado inmediatamente ✅
- Thread 3: falla en 1ms, liberado inmediatamente ✅
- ...
- Thread pool disponible ✅
- Sistema sigue funcionando ✅
```

**3. Permite que Servicios se Recuperen**
```
Email Service está caído:
- Sin Circuit Breaker: bombardeado con requests
  → No puede recuperarse (overload)

- Con Circuit Breaker: requests bloqueados (OPEN state)
  → Servicio tiene tiempo para recuperarse
  → Circuit prueba reconexión gradualmente (HALF_OPEN)
```

**4. Graceful Degradation (Degradación Elegante)**
```java
// Sistema sigue funcionando con funcionalidad reducida
@CircuitBreaker(fallbackMethod = "sendEmailFallback")
public void sendWelcomeEmail(String email, String username) {
    externalEmailService.send(email, username);
}

// Fallback: guardar para enviar después
private void sendEmailFallback(String email, String username, Exception ex) {
    emailQueueRepository.save(new PendingEmail(email, username));
    // Usuario fue creado ✅ (email se enviará después)
}
```

**5. Previene Cascading Failures**
```
          ┌────────────┐
          │   USER     │
          │  SERVICE   │
          └─────┬──────┘
                │
                ↓
       ┌────────┴───────┐
       │ NOTIFICATIONS  │  ← Circuit Breaker aquí ⚡
       │    SERVICE     │
       └────────┬───────┘
                │
                ↓
       ┌────────┴───────┐
       │  EMAIL SERVICE │  ← Caído ❌
       │   (SendGrid)   │
       └────────────────┘

Sin Circuit Breaker:
- Email Service caído → Notifications bloqueado → User Service bloqueado
- Todo el sistema se cae ❌

Con Circuit Breaker:
- Email Service caído → Circuit se abre → Notifications usa fallback
- User Service y Notifications siguen funcionando ✅
```

---

#### Cuándo Usar Circuit Breaker

**✅ USA Circuit Breaker cuando:**

1. **Llamadas a Servicios Externos**
   ```java
   @CircuitBreaker(name = "sendgrid")
   public void sendEmail() { sendGridClient.send(...); }

   @CircuitBreaker(name = "stripe")
   public void processPayment() { stripeClient.charge(...); }

   @CircuitBreaker(name = "s3")
   public void uploadFile() { s3Client.putObject(...); }
   ```

2. **Llamadas entre Microservicios**
   ```java
   @CircuitBreaker(name = "userService")
   public User getUser(Long id) { restTemplate.get("/users/" + id); }

   @CircuitBreaker(name = "inventoryService")
   public Stock getStock(Long productId) { inventoryClient.getStock(productId); }
   ```

3. **Operaciones que Pueden Fallar por Red/Disponibilidad**
   ```java
   @CircuitBreaker(name = "database")
   public List<User> findAll() { jdbcTemplate.query(...); }  // BD remota

   @CircuitBreaker(name = "cache")
   public String getValue(String key) { redisTemplate.get(key); }  // Redis remoto
   ```

4. **Integraciones con Third-Party Services**
   ```java
   @CircuitBreaker(name = "twilio")
   public void sendSMS() { twilioClient.send(...); }

   @CircuitBreaker(name = "google-maps")
   public Location geocode(String address) { mapsClient.geocode(address); }
   ```

**❌ NO USES Circuit Breaker cuando:**

1. **Lógica de Negocio Local**
   ```java
   // NO ❌
   @CircuitBreaker(name = "validation")
   public void validateUser(User user) {
       if (user.getEmail() == null) throw new ValidationException();
   }
   ```

2. **Operaciones que DEBEN Ejecutarse Siempre**
   ```java
   // NO ❌
   @CircuitBreaker(name = "createOrder")
   public Order createOrder(CreateOrderCommand command) {
       // Crear orden es crítico, no puede fallar con fallback
   }
   ```

3. **Validaciones Críticas**
   ```java
   // NO ❌
   @CircuitBreaker(name = "auth")
   public boolean authenticate(String username, String password) {
       // Autenticación no puede tener fallback (riesgo de seguridad)
   }
   ```

4. **Operaciones Síncronas que Requieren Resultado Inmediato**
   ```java
   // NO ❌
   @CircuitBreaker(name = "payment", fallbackMethod = "paymentFallback")
   public PaymentResult processPayment(PaymentRequest request) {
       return stripeClient.charge(request);
   }

   private PaymentResult paymentFallback(PaymentRequest request, Exception ex) {
       // ¿Qué retornar? ¿Éxito falso? ¿Fallo? ❌
       // Mejor: reintentar o encolar, NO usar Circuit Breaker
   }
   ```

---

#### Mejores Prácticas

**1. Ajustar Thresholds según el Servicio**

```yaml
# Servicio crítico (email): tolerante a fallos
emailService:
  failure-rate-threshold: 50          # Permite 50% de fallos
  wait-duration-in-open-state: 10s    # Recuperación rápida

# Servicio no crítico (analytics): estricto
analyticsService:
  failure-rate-threshold: 20          # Solo 20% de fallos
  wait-duration-in-open-state: 60s    # Recuperación lenta
```

**2. Implementar Fallbacks Inteligentes**

```java
// Fallback 1: Guardar para reintentar (recomendado)
private void sendEmailFallback(String email, String username, Exception ex) {
    emailQueueRepository.save(new PendingEmail(email, username));
}

// Fallback 2: Usar servicio alternativo
private void sendEmailFallback(String email, String username, Exception ex) {
    backupEmailService.send(email, username);
}

// Fallback 3: Retornar valor por defecto
private UserProfile getUserProfileFallback(Long userId, Exception ex) {
    return UserProfile.defaultProfile(userId);
}

// Fallback 4: Retornar caché
private Product getProductFallback(Long productId, Exception ex) {
    return productCache.get(productId)
        .orElse(Product.unavailable(productId));
}
```

**3. Monitorear Estado del Circuit**

```java
@RestController
@RequestMapping("/admin/circuit-breaker")
public class CircuitBreakerController {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @GetMapping("/status")
    public Map<String, String> getStatus() {
        return circuitBreakerRegistry.getAllCircuitBreakers()
            .stream()
            .collect(Collectors.toMap(
                CircuitBreaker::getName,
                cb -> cb.getState().toString()
            ));
    }

    @GetMapping("/metrics/{name}")
    public CircuitBreakerMetrics getMetrics(@PathVariable String name) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(name);
        CircuitBreaker.Metrics metrics = cb.getMetrics();

        return new CircuitBreakerMetrics(
            cb.getState().toString(),
            metrics.getNumberOfSuccessfulCalls(),
            metrics.getNumberOfFailedCalls(),
            metrics.getFailureRate()
        );
    }
}
```

**4. Exponer en Health Endpoint**

```yaml
# application.yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics
  endpoint:
    health:
      show-details: always
  health:
    circuitbreakers:
      enabled: true

resilience4j:
  circuitbreaker:
    instances:
      emailService:
        register-health-indicator: true  # ← Importante
```

**Resultado en `/actuator/health`:**
```json
{
  "status": "UP",
  "components": {
    "circuitBreakers": {
      "status": "UP",
      "details": {
        "emailService": {
          "status": "UP",
          "state": "CLOSED",
          "failureRate": "0.0%",
          "slowCallRate": "0.0%"
        }
      }
    }
  }
}
```

**5. Logs Estructurados**

```java
@CircuitBreaker(name = "emailService", fallbackMethod = "sendEmailFallback")
public void sendWelcomeEmail(String email, String username) {
    logger.info("Attempting to send email",
        Map.of(
            "email", email,
            "username", username,
            "circuit", "emailService",
            "state", circuitBreakerRegistry.circuitBreaker("emailService").getState()
        )
    );

    externalEmailService.send(email, username);
}

private void sendEmailFallback(String email, String username, Exception ex) {
    logger.warn("Circuit breaker fallback executed",
        Map.of(
            "email", email,
            "circuit", "emailService",
            "state", "OPEN",
            "reason", ex.getMessage()
        )
    );
}
```

**6. Testing Circuit Breaker**

```java
@SpringBootTest
class EmailServiceCircuitBreakerTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Test
    void shouldOpenCircuitAfterFailures() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("emailService");

        // Estado inicial: CLOSED
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // Provocar 10 fallos (threshold: 50% = 5 fallos de 10)
        for (int i = 0; i < 10; i++) {
            try {
                emailService.sendWelcomeEmail("test@test.com", "test");
            } catch (Exception e) {
                // Ignorar
            }
        }

        // Circuit debe estar OPEN después de 6 fallos
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void shouldCallFallbackWhenCircuitIsOpen() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("emailService");

        // Forzar circuit a OPEN
        cb.transitionToOpenState();

        // Llamar servicio → debe ejecutar fallback sin lanzar excepción
        emailService.sendWelcomeEmail("test@test.com", "test");

        // Verificar que se guardó en cola (fallback)
        verify(emailQueueRepository).save(any(PendingEmail.class));
    }

    @Test
    void shouldTransitionToHalfOpenAfterWaitDuration() throws InterruptedException {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("emailService");

        // Forzar a OPEN
        cb.transitionToOpenState();
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Esperar wait-duration-in-open-state (10s en config)
        Thread.sleep(11000);

        // Siguiente llamada → Circuit debe estar HALF_OPEN
        try {
            emailService.sendWelcomeEmail("test@test.com", "test");
        } catch (Exception e) {
            // Ignorar
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);
    }
}
```

---

#### Combinación: Circuit Breaker + DLT

Circuit Breaker y DLT son **complementarios** y se usan juntos:

```java
@Component
public class UserEventsKafkaConsumer {

    private final EmailService emailService;  // ← Con Circuit Breaker

    @KafkaListener(topics = "user.created")
    public void consume(UserCreatedEvent event) {
        // Circuit Breaker protege la llamada
        emailService.sendWelcomeEmail(event.email(), event.username());

        // Si Circuit está OPEN:
        // 1. Ejecuta fallback (guarda en cola)
        // 2. NO lanza excepción
        // 3. Consumer continúa → Kafka avanza offset ✅

        // Si Circuit está CLOSED y falla:
        // 1. Lanza excepción
        // 2. DefaultErrorHandler reintenta 3 veces
        // 3. Después de 3 fallos → mensaje va a DLT
    }
}
```

**Flujo Combinado:**

```
Evento 1: EmailService falla
         ↓
         Circuit CLOSED → Lanza excepción
         ↓
         DefaultErrorHandler reintenta 3 veces
         ↓
         Sigue fallando → Mensaje va a DLT ✅

Eventos 2-6: EmailService sigue fallando
         ↓
         Circuit detecta 60% de fallos
         ↓
         Circuit cambia a OPEN ❌

Eventos 7-10: Llegan mientras Circuit está OPEN
         ↓
         Circuit ejecuta fallback (guarda en cola)
         ↓
         NO lanza excepción
         ↓
         Consumer NO se bloquea ✅
         ↓
         Kafka avanza offset (no van a DLT) ✅

Después de 10s: Circuit cambia a HALF_OPEN
Eventos 11-13: Pruebas de recuperación
         ↓
         Si EmailService se recuperó → Circuit → CLOSED ✅
         ↓
         Todo vuelve a la normalidad
```

**Resumen de Protecciones:**

| Escenario | Circuit Breaker | DLT |
|-----------|----------------|-----|
| Email service caído | Fail-fast con fallback ⚡ | - |
| Email service intermitente | Abre circuit si >50% fallan | Mensajes fallidos → DLT |
| Bug en consumer | - | Después de 3 reintentos → DLT |
| Datos inválidos | - | No puede procesar → DLT |
| Alta latencia (>5s) | Slow calls → Abre circuit | - |

---

## Cuándo Usar Cada Concepto

### ¿Entity o Value Object?

**Usa Entity cuando:**
- ✅ Necesitas rastrear algo a lo largo del tiempo
- ✅ La identidad importa
- ✅ Puede cambiar
- Ejemplos: User, Order, Invoice, Account

**Usa Value Object cuando:**
- ✅ Solo el valor importa
- ✅ Es inmutable
- ✅ Tiene validaciones propias
- Ejemplos: Money, Email, Address, DateRange

### ¿Aggregate Root o Entity Simple?

**Usa Aggregate Root cuando:**
- ✅ Controla un grupo de objetos relacionados
- ✅ Necesitas garantizar consistencia del grupo
- Ejemplos: Order (con OrderItems), ShoppingCart (con CartItems)

**Usa Entity simple cuando:**
- ✅ No tiene objetos relacionados
- ✅ No necesita controlar consistencia de grupo
- Ejemplos: User simple, Category, Tag

### ¿Domain Service o Application Service?

**Domain Service cuando:**
- ✅ Lógica de negocio pura
- ✅ Sin frameworks
- ✅ Involucra múltiples Aggregates
- Ejemplo: `MoneyTransferDomainService`

**Application Service cuando:**
- ✅ Orquesta casos de uso
- ✅ Usa frameworks (Spring, etc.)
- ✅ Coordina dominio y puertos
- Ejemplo: `CreateUserService`, `ProcessOrderService`

---

## Resumen Visual

```
┌─────────────────────────────────────────────────────────────┐
│                    DOMAIN LAYER                              │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Entity     │  │ Value Object │  │    Event     │     │
│  │              │  │              │  │              │     │
│  │  • Has ID    │  │  • No ID     │  │  • Inmutable │     │
│  │  • Mutable   │  │  • Inmutable │  │  • Pasado    │     │
│  │  • Identity  │  │  • Equality  │  │  • Comunica  │     │
│  │              │  │    by value  │  │              │     │
│  │  User        │  │  Email       │  │  UserCreated │     │
│  │  Order       │  │  Money       │  │  OrderPlaced │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           Aggregate Root                              │  │
│  │  • Entity + grupo de objetos                         │  │
│  │  • Punto de entrada único                            │  │
│  │  • Garantiza consistencia                            │  │
│  │                                                       │  │
│  │  Order { id, items[], total }                        │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  Repository  │  │   Factory    │  │Domain Service│     │
│  │  (Interface) │  │              │  │              │     │
│  │              │  │  • Crea      │  │  • Lógica    │     │
│  │  • Save      │  │    objetos   │  │    multi-    │     │
│  │  • Find      │  │    complejos │  │    aggregate │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

---

## Recursos para Seguir Aprendiendo

1. **Libro:** "Domain-Driven Design" - Eric Evans (El original, denso pero completo)
2. **Libro:** "Implementing Domain-Driven Design" - Vaughn Vernon (Más práctico)
3. **Libro:** "Domain-Driven Design Distilled" - Vaughn Vernon (Resumen ejecutivo)
4. **Online:** [DDD Reference](http://domainlanguage.com/ddd/reference/) - Eric Evans

---

**¡Recuerda!** DDD no es obligatorio para todo. Úsalo cuando:
- ✅ El dominio es complejo
- ✅ Hay muchas reglas de negocio
- ✅ El proyecto va a crecer

Para CRUDs simples, puede ser overkill. 🎯
