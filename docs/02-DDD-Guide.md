# Guía de Domain-Driven Design (DDD)

## 📚 Índice

1. [¿Qué es DDD?](#qué-es-ddd)
2. [¿Por qué usar DDD?](#por-qué-usar-ddd)
3. [Conceptos Fundamentales](#conceptos-fundamentales)
4. [Building Blocks de DDD](#building-blocks-de-ddd)
5. [Ejemplos Prácticos en el Proyecto](#ejemplos-prácticos)
6. [Errores Comunes](#errores-comunes)
7. [Cuándo Usar Cada Concepto](#cuándo-usar-cada-concepto)

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
