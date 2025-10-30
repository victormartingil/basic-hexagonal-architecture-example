# Guía de Java Moderno

## 📚 Índice

1. [Optional - Adiós a los NullPointerException](#optional---adiós-a-los-nullpointerexception)
2. [Streams - Procesamiento de Colecciones](#streams---procesamiento-de-colecciones)
3. [Lambdas y Programación Funcional](#lambdas-y-programación-funcional)
4. [Colecciones - Cuándo usar cada una](#colecciones---cuándo-usar-cada-una)
5. [Records - DTOs Inmutables](#records---dtos-inmutables)
6. [Inmutabilidad](#inmutabilidad)
7. [final Keyword - Garantizando Inmutabilidad](#final-keyword---garantizando-inmutabilidad)
8. [Inmutabilidad Profunda (Deep Immutability)](#inmutabilidad-profunda-deep-immutability)
9. [Records en Profundidad (Java 21)](#records-en-profundidad-java-21)
10. [var - Inferencia de Tipos](#var---inferencia-de-tipos-java-10)
11. [Try-with-Resources](#try-with-resources)
12. [Switch Expressions](#switch-expressions-java-14)

---

## Optional - Adiós a los NullPointerException

### ¿Qué es Optional?

`Optional<T>` es un **contenedor** que puede tener o no tener un valor. Te obliga a pensar en el caso de "no hay valor" desde el principio.

### El Problema con null

```java
// ❌ CÓDIGO PELIGROSO (pre-Optional)
public User findById(UUID id) {
    return userRepository.findById(id);  // ¿Qué pasa si no existe?
}

// Uso posterior
User user = findById(someId);
System.out.println(user.getUsername());  // 💥 NullPointerException si user es null
```

### La Solución: Optional

```java
// ✅ CÓDIGO SEGURO (con Optional)
public Optional<User> findById(UUID id) {
    return userRepository.findById(id);
}

// Uso posterior - OBLIGADO a manejar el caso "no existe"
Optional<User> userOpt = findById(someId);
if (userOpt.isPresent()) {
    User user = userOpt.get();
    System.out.println(user.getUsername());  // Seguro
}
```

### Crear Optional

```java
// 1. Optional con valor
Optional<String> opt1 = Optional.of("Hola");  // Lanza excepción si es null

// 2. Optional que puede ser null
Optional<String> opt2 = Optional.ofNullable(mightBeNull);  // No lanza excepción

// 3. Optional vacío
Optional<String> opt3 = Optional.empty();
```

### Métodos Importantes de Optional

#### 1. isPresent() / isEmpty()

```java
Optional<User> userOpt = findById(id);

// ✅ Verificar si tiene valor
if (userOpt.isPresent()) {
    User user = userOpt.get();
    // usar user
}

// ✅ Verificar si NO tiene valor (Java 11+)
if (userOpt.isEmpty()) {
    System.out.println("Usuario no encontrado");
}
```

#### 2. orElse() - Valor por defecto

```java
// ✅ Si no existe, usa un valor por defecto
User user = userOpt.orElse(defaultUser);

String username = findUsername(id).orElse("Anonymous");
```

#### 3. orElseThrow() - Lanzar excepción

```java
// ✅ Si no existe, lanza excepción personalizada
User user = userOpt.orElseThrow(() ->
    new UserNotFoundException("User not found: " + id)
);
```

**Ejemplo del proyecto:**
```java
// En el Service
@Override
public UserResult execute(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));  // ✅

    return new UserResult(...);
}
```

#### 4. ifPresent() - Ejecutar si existe

```java
// ✅ Ejecuta código solo si tiene valor
userOpt.ifPresent(user -> {
    System.out.println("Usuario: " + user.getUsername());
});

// ✅ Con método reference
userOpt.ifPresent(this::processUser);
```

#### 5. map() - Transformar el valor

```java
// ✅ Transformar el valor si existe
Optional<String> usernameOpt = userOpt.map(User::getUsername);
Optional<String> emailOpt = userOpt.map(user -> user.getEmail().getValue());

// Si userOpt está vacío, usernameOpt también estará vacío
```

#### 6. flatMap() - Transformar cuando el resultado es Optional

```java
// ✅ Cuando el mapper devuelve Optional
Optional<User> userOpt = findById(userId);
Optional<Order> orderOpt = userOpt.flatMap(user -> findLastOrder(user.getId()));

// Sin flatMap sería Optional<Optional<Order>> ❌
```

### ⚠️ Anti-patrones con Optional

```java
// ❌ NO HACER: llamar get() sin verificar
User user = userOpt.get();  // Puede lanzar NoSuchElementException

// ❌ NO HACER: usar isPresent() + get()
if (userOpt.isPresent()) {
    User user = userOpt.get();
    return user.getUsername();
}

// ✅ HACER: usar map() + orElse()
return userOpt
    .map(User::getUsername)
    .orElse("Unknown");

// ❌ NO USAR Optional como parámetro
public void updateUser(Optional<User> user) { }  // MAL

// ✅ USAR null o sobrecarga
public void updateUser(User user) { }  // BIEN
```

### Resumen: Cuándo usar Optional

| Situación | Usar Optional |
|-----------|---------------|
| ✅ Valor de retorno que puede no existir | `Optional<User> findById(UUID id)` |
| ✅ Campo de clase opcional | `private Optional<Email> secondaryEmail;` |
| ❌ Parámetros de método | NO usar, usar null o sobrecarga |
| ❌ Colecciones | NO usar `Optional<List<T>>`, usar lista vacía |

---

## Streams - Procesamiento de Colecciones

### ¿Qué son los Streams?

Los **Streams** permiten procesar colecciones de forma **funcional** y **declarativa** (dices QUÉ quieres, no CÓMO hacerlo).

### Crear Streams

```java
// Desde una List
List<User> users = Arrays.asList(...);
Stream<User> stream = users.stream();

// Desde un array
String[] names = {"John", "Jane", "Bob"};
Stream<String> stream = Arrays.stream(names);

// Stream vacío
Stream<String> empty = Stream.empty();

// Stream de valores
Stream<String> stream = Stream.of("a", "b", "c");
```

### Operaciones de Stream

#### 1. filter() - Filtrar elementos

```java
// ❌ FORMA IMPERATIVA (vieja)
List<User> activeUsers = new ArrayList<>();
for (User user : users) {
    if (user.isEnabled()) {
        activeUsers.add(user);
    }
}

// ✅ FORMA DECLARATIVA (con Streams)
List<User> activeUsers = users.stream()
    .filter(user -> user.isEnabled())
    .collect(Collectors.toList());

// Múltiples filtros
List<User> result = users.stream()
    .filter(user -> user.isEnabled())
    .filter(user -> user.getEmail().getValue().endsWith("@gmail.com"))
    .collect(Collectors.toList());
```

#### 2. map() - Transformar elementos

```java
// ❌ FORMA IMPERATIVA
List<String> usernames = new ArrayList<>();
for (User user : users) {
    usernames.add(user.getUsername().getValue());
}

// ✅ FORMA DECLARATIVA
List<String> usernames = users.stream()
    .map(user -> user.getUsername().getValue())
    .collect(Collectors.toList());

// Con method reference (más limpio)
List<String> usernames = users.stream()
    .map(User::getUsername)
    .map(Username::getValue)
    .collect(Collectors.toList());
```

#### 3. collect() - Recolectar resultados

```java
// A List
List<User> list = users.stream()
    .filter(User::isEnabled)
    .collect(Collectors.toList());

// A Set (sin duplicados)
Set<String> emails = users.stream()
    .map(user -> user.getEmail().getValue())
    .collect(Collectors.toSet());

// A Map
Map<UUID, User> userMap = users.stream()
    .collect(Collectors.toMap(User::getId, user -> user));

// Agrupar por campo
Map<Boolean, List<User>> usersByStatus = users.stream()
    .collect(Collectors.groupingBy(User::isEnabled));
```

#### 4. forEach() - Ejecutar acción para cada elemento

```java
// ✅ Imprimir cada usuario
users.stream()
    .forEach(user -> System.out.println(user.getUsername()));

// Con method reference
users.stream()
    .forEach(System.out::println);

// NOTA: Para solo iterar, mejor usar forEach directamente
users.forEach(System.out::println);  // Más simple
```

#### 5. count() - Contar elementos

```java
// Contar usuarios activos
long count = users.stream()
    .filter(User::isEnabled)
    .count();
```

#### 6. anyMatch() / allMatch() / noneMatch()

```java
// ¿Hay algún usuario activo?
boolean hasActive = users.stream()
    .anyMatch(User::isEnabled);

// ¿Todos los usuarios son activos?
boolean allActive = users.stream()
    .allMatch(User::isEnabled);

// ¿Ningún usuario es activo?
boolean noneActive = users.stream()
    .noneMatch(User::isEnabled);
```

#### 7. findFirst() / findAny()

```java
// Encontrar el primer usuario activo
Optional<User> firstActive = users.stream()
    .filter(User::isEnabled)
    .findFirst();

// Encontrar cualquier usuario activo (útil en paralelo)
Optional<User> anyActive = users.stream()
    .filter(User::isEnabled)
    .findAny();
```

#### 8. sorted() - Ordenar

```java
// Ordenar por username
List<User> sorted = users.stream()
    .sorted(Comparator.comparing(user -> user.getUsername().getValue()))
    .collect(Collectors.toList());

// Con method reference
List<User> sorted = users.stream()
    .sorted(Comparator.comparing(User::getCreatedAt))
    .collect(Collectors.toList());

// Orden inverso
List<User> sorted = users.stream()
    .sorted(Comparator.comparing(User::getCreatedAt).reversed())
    .collect(Collectors.toList());
```

#### 9. distinct() - Eliminar duplicados

```java
// Obtener emails únicos
List<String> uniqueEmails = users.stream()
    .map(user -> user.getEmail().getValue())
    .distinct()
    .collect(Collectors.toList());
```

#### 10. limit() / skip()

```java
// Primeros 10 usuarios
List<User> first10 = users.stream()
    .limit(10)
    .collect(Collectors.toList());

// Saltar los primeros 5 y tomar los siguientes 10
List<User> page2 = users.stream()
    .skip(5)
    .limit(10)
    .collect(Collectors.toList());
```

### Ejemplo Completo del Proyecto

```java
// Obtener emails de usuarios activos, ordenados, sin duplicados
public List<String> getActiveUserEmails() {
    return userRepository.findAll().stream()
        .filter(User::isEnabled)                    // Solo activos
        .map(User::getEmail)                        // Extraer Email (Value Object)
        .map(Email::getValue)                       // Extraer String del VO
        .distinct()                                 // Sin duplicados
        .sorted()                                   // Ordenar
        .collect(Collectors.toList());             // A lista
}
```

### Streams Paralelos

```java
// Para colecciones grandes, procesar en paralelo
List<String> emails = users.parallelStream()  // ← parallel
    .filter(User::isEnabled)
    .map(user -> user.getEmail().getValue())
    .collect(Collectors.toList());

// Solo usar si la operación es costosa y la colección es grande (>10k elementos)
```

### ⚠️ Anti-patrones con Streams

```java
// ❌ NO: Modificar estado externo
List<User> result = new ArrayList<>();
users.stream().forEach(user -> result.add(user));  // MAL

// ✅ USAR collect()
List<User> result = users.stream().collect(Collectors.toList());

// ❌ NO: Usar stream para operaciones simples
users.stream().forEach(System.out::println);  // Overkill

// ✅ Usar forEach directo
users.forEach(System.out::println);
```

---

## Lambdas y Programación Funcional

### ¿Qué es una Lambda?

Una **lambda** es una función anónima (sin nombre) que puedes pasar como parámetro.

### Sintaxis de Lambda

```java
// Forma completa
(parametros) -> {
    cuerpo;
    return valor;
}

// Forma corta (una expresión)
(parametros) -> expresion

// Sin parámetros
() -> System.out.println("Hola")

// Un parámetro (sin paréntesis)
x -> x * 2

// Múltiples parámetros
(x, y) -> x + y

// Con tipos explícitos
(String s) -> s.length()
```

### Ejemplos Prácticos

#### Comparar: Clase Anónima vs Lambda

```java
// ❌ VIEJO: Clase anónima (verbose)
users.sort(new Comparator<User>() {
    @Override
    public int compare(User u1, User u2) {
        return u1.getUsername().compareTo(u2.getUsername());
    }
});

// ✅ MODERNO: Lambda (conciso)
users.sort((u1, u2) -> u1.getUsername().compareTo(u2.getUsername()));

// ✅ AÚN MEJOR: Method reference
users.sort(Comparator.comparing(User::getUsername));
```

#### Interfaces Funcionales Comunes

```java
// 1. Predicate<T> - Función que devuelve boolean
Predicate<User> isActive = user -> user.isEnabled();
boolean result = isActive.test(user);

// Uso en filter
users.stream().filter(isActive).collect(Collectors.toList());

// 2. Function<T, R> - Función que transforma T en R
Function<User, String> getUsername = user -> user.getUsername().getValue();
String username = getUsername.apply(user);

// Uso en map
users.stream().map(getUsername).collect(Collectors.toList());

// 3. Consumer<T> - Función que consume un valor (sin retorno)
Consumer<User> printUser = user -> System.out.println(user);
printUser.accept(user);

// Uso en forEach
users.forEach(printUser);

// 4. Supplier<T> - Función que provee un valor (sin parámetros)
Supplier<User> defaultUser = () -> User.create("default", "default@ex.com");
User user = defaultUser.get();

// Uso en orElseGet
Optional<User> userOpt = findById(id);
User user = userOpt.orElseGet(defaultUser);

// 5. BiFunction<T, U, R> - Función con 2 parámetros
BiFunction<String, String, User> createUser = (username, email) ->
    User.create(username, email);
User user = createUser.apply("john", "john@ex.com");
```

### Method References (Referencias a Métodos)

Forma aún más corta cuando la lambda solo llama a un método:

```java
// Lambda: x -> metodo(x)
// Method Reference: Clase::metodo

// 1. Método estático
Function<String, Integer> parse = str -> Integer.parseInt(str);
Function<String, Integer> parse = Integer::parseInt;  // ✅

// 2. Método de instancia
Function<User, String> getUsername = user -> user.getUsername().getValue();
Function<User, String> getUsername = User::getUsername;  // ✅ (luego Username::getValue)

// 3. Método de objeto específico
Consumer<String> print = str -> System.out.println(str);
Consumer<String> print = System.out::println;  // ✅

// 4. Constructor
Supplier<ArrayList<String>> listSupplier = () -> new ArrayList<>();
Supplier<ArrayList<String>> listSupplier = ArrayList::new;  // ✅
```

### Ejemplos del Proyecto

```java
// En el Service
public List<UserResult> getAllActiveUsers() {
    return userRepository.findAll().stream()
        .filter(User::isEnabled)                    // Method reference
        .map(this::toResult)                        // Method reference a método de instancia
        .collect(Collectors.toList());
}

private UserResult toResult(User user) {
    return new UserResult(
        user.getId(),
        user.getUsername().getValue(),
        user.getEmail().getValue(),
        user.isEnabled(),
        user.getCreatedAt()
    );
}

// Con lambda inline
public List<String> getActiveUsernames() {
    return userRepository.findAll().stream()
        .filter(user -> user.isEnabled())           // Lambda
        .map(user -> user.getUsername().getValue()) // Lambda
        .collect(Collectors.toList());
}
```

---

## Colecciones - Cuándo usar cada una

### List vs Set vs Map

```
┌──────────────────────────────────────────────────────────────┐
│                    COLECCIONES EN JAVA                        │
└──────────────────────────────────────────────────────────────┘

LIST                    SET                     MAP
- Orden                 - Sin duplicados        - Clave → Valor
- Permite duplicados    - Sin orden garantizado - Sin claves duplicadas
- Acceso por índice     - No tiene índice       - Acceso por clave

[A, B, C, A]           {A, B, C}               {1→A, 2→B, 3→C}
```

### List - Colección Ordenada

```java
// Crear listas
List<String> list1 = new ArrayList<>();         // Más común (rápido acceso)
List<String> list2 = new LinkedList<>();        // Rápido insertar/eliminar
List<String> list3 = List.of("a", "b", "c");   // Inmutable (Java 9+)
List<String> list4 = Arrays.asList("a", "b");   // Tamaño fijo

// Operaciones
list1.add("elemento");                          // Agregar al final
list1.add(0, "elemento");                       // Agregar en posición
String item = list1.get(0);                     // Obtener por índice
list1.remove(0);                                // Eliminar por índice
list1.remove("elemento");                       // Eliminar por valor
boolean contains = list1.contains("elemento");  // Verificar si existe
int size = list1.size();                        // Tamaño
list1.clear();                                  // Vaciar
```

**Cuándo usar:**
- ✅ Cuando el orden importa
- ✅ Cuando puedes tener duplicados
- ✅ Cuando necesitas acceso por índice
- Ejemplos: lista de usuarios, historial, logs

### Set - Colección Sin Duplicados

```java
// Crear sets
Set<String> set1 = new HashSet<>();             // Más rápido, sin orden
Set<String> set2 = new LinkedHashSet<>();       // Mantiene orden de inserción
Set<String> set3 = new TreeSet<>();             // Ordenado naturalmente
Set<String> set4 = Set.of("a", "b", "c");      // Inmutable (Java 9+)

// Operaciones
set1.add("elemento");                           // Agregar (ignora duplicados)
set1.remove("elemento");                        // Eliminar
boolean contains = set1.contains("elemento");   // Verificar (muy rápido O(1))
int size = set1.size();                         // Tamaño

// Ejemplo: Eliminar duplicados de una lista
List<String> list = Arrays.asList("a", "b", "a", "c");
Set<String> unique = new HashSet<>(list);
System.out.println(unique);  // [a, b, c]
```

**Cuándo usar:**
- ✅ Cuando NO quieres duplicados
- ✅ Cuando necesitas búsqueda rápida (`contains()`)
- ✅ Cuando el orden no importa (HashSet)
- Ejemplos: IDs únicos, tags, categorías

### Map - Diccionario Clave-Valor

```java
// Crear maps
Map<String, User> map1 = new HashMap<>();       // Más común, sin orden
Map<String, User> map2 = new LinkedHashMap<>(); // Mantiene orden de inserción
Map<String, User> map3 = new TreeMap<>();       // Ordenado por clave
Map<String, User> map4 = Map.of("k1", v1, "k2", v2);  // Inmutable (Java 9+)

// Operaciones
map1.put("key", user);                          // Agregar/actualizar
User user = map1.get("key");                    // Obtener (null si no existe)
User user = map1.getOrDefault("key", defaultUser);  // Con valor por defecto
map1.remove("key");                             // Eliminar
boolean hasKey = map1.containsKey("key");       // Verificar clave
boolean hasValue = map1.containsValue(user);    // Verificar valor
int size = map1.size();                         // Tamaño

// Iterar sobre Map
for (Map.Entry<String, User> entry : map1.entrySet()) {
    String key = entry.getKey();
    User value = entry.getValue();
}

// Con Streams
map1.forEach((key, value) -> System.out.println(key + ": " + value));
```

**Cuándo usar:**
- ✅ Cuando necesitas buscar por clave
- ✅ Cuando tienes pares clave-valor
- ✅ Como caché (ID → Objeto)
- Ejemplos: usuarios por ID, configuraciones, caché

### Tabla de Decisión

| Necesitas | Usa |
|-----------|-----|
| Orden + Duplicados | `ArrayList<T>` |
| Sin duplicados + Búsqueda rápida | `HashSet<T>` |
| Sin duplicados + Orden | `LinkedHashSet<T>` |
| Buscar por clave | `HashMap<K, V>` |
| Muchas inserciones/eliminaciones | `LinkedList<T>` |
| Ordenación natural | `TreeSet<T>` o `TreeMap<K, V>` |

### Colecciones Inmutables (Java 9+)

```java
// ✅ INMUTABLES (no se pueden modificar)
List<String> list = List.of("a", "b", "c");
Set<String> set = Set.of("a", "b", "c");
Map<String, Integer> map = Map.of("a", 1, "b", 2);

// Intentar modificar lanza UnsupportedOperationException
list.add("d");  // ❌ Error en tiempo de ejecución

// Para versiones antiguas de Java
List<String> list = Collections.unmodifiableList(Arrays.asList("a", "b", "c"));
```

### Ejemplo del Proyecto

```java
// En el Repository Adapter
public class JpaUserRepositoryAdapter implements UserRepository {

    // Map como caché
    private final Map<UUID, User> cache = new HashMap<>();

    @Override
    public Optional<User> findById(UUID id) {
        // Buscar en caché primero (Map es rápido)
        if (cache.containsKey(id)) {
            return Optional.of(cache.get(id));
        }

        // Si no está en caché, buscar en BD
        return springDataRepo.findById(id)
            .map(mapper::toDomain)
            .map(user -> {
                cache.put(id, user);  // Guardar en caché
                return user;
            });
    }
}

// En el Service
public Set<String> getUniqueEmails() {
    // Set para eliminar duplicados automáticamente
    return userRepository.findAll().stream()
        .map(user -> user.getEmail().getValue())
        .collect(Collectors.toSet());  // ← Set, no List
}
```

---

## Records - DTOs Inmutables

### ¿Qué son los Records? (Java 14+)

Los **Records** son clases especiales para crear DTOs inmutables con menos código.

### Antes de Records

```java
// ❌ VIEJO: Clase con boilerplate
public final class UserResult {
    private final UUID id;
    private final String username;
    private final String email;

    public UserResult(UUID id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }

    @Override
    public boolean equals(Object o) { /* ... */ }

    @Override
    public int hashCode() { /* ... */ }

    @Override
    public String toString() { /* ... */ }
}
```

**50 líneas de código!** 😵

### Con Records

```java
// ✅ MODERNO: Record (equivalente al anterior)
public record UserResult(
    UUID id,
    String username,
    String email
) {}
```

**4 líneas!** 🎉

**El compilador genera automáticamente:**
- Constructor
- Getters (sin `get` prefix: `id()`, `username()`, `email()`)
- `equals()` y `hashCode()`
- `toString()`

### Usar Records

```java
// Crear
UserResult result = new UserResult(id, "john", "john@ex.com");

// Acceder (sin "get")
UUID id = result.id();
String username = result.username();
String email = result.email();

// toString automático
System.out.println(result);
// UserResult[id=..., username=john, email=john@ex.com]

// equals por valor
UserResult r1 = new UserResult(id, "john", "john@ex.com");
UserResult r2 = new UserResult(id, "john", "john@ex.com");
r1.equals(r2);  // true
```

### Records con Métodos

```java
public record UserResult(
    UUID id,
    String username,
    String email
) {
    // Validación en el constructor
    public UserResult {  // Constructor compacto
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
    }

    // Métodos adicionales
    public String getDisplayName() {
        return username + " (" + email + ")";
    }

    // Método estático
    public static UserResult from(User user) {
        return new UserResult(
            user.getId(),
            user.getUsername().getValue(),
            user.getEmail().getValue()
        );
    }
}
```

### Cuándo usar Records

| Situación | Usar Record |
|-----------|-------------|
| ✅ DTOs inmutables | `CreateUserCommand`, `UserResult`, `UserResponse` |
| ✅ Value Objects simples | `Coordinates(lat, lon)`, `Money(amount, currency)` |
| ✅ Resultados de queries | `UserStats(count, avgAge)` |
| ❌ Entities con comportamiento | `User` (mejor clase normal) |
| ❌ Clases mutables | Necesitas setters (usar clase normal) |

### Ejemplos del Proyecto

```java
// Command
public record CreateUserCommand(String username, String email) {}

// Result
public record UserResult(
    UUID id,
    String username,
    String email,
    boolean enabled,
    Instant createdAt
) {}

// Event
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

// REST DTO
public record CreateUserRequest(String username, String email) {}
public record UserResponse(
    String id,
    String username,
    String email,
    boolean enabled,
    Instant createdAt
) {}
```

---

## Inmutabilidad

### ¿Qué es Inmutabilidad?

Un objeto **inmutable** NO puede cambiar después de crearse.

### Por qué es Importante

```java
// ❌ MUTABLE (peligroso)
public class User {
    private String username;

    public void setUsername(String username) {
        this.username = username;
    }
}

// Problema: Cambios inesperados
User user = new User("john");
processUser(user);  // ¿Cambió el username?
System.out.println(user.getUsername());  // ¿Qué imprime?

void processUser(User user) {
    user.setUsername("modified");  // 💥 Efecto secundario!
}
```

```java
// ✅ INMUTABLE (seguro)
public final class User {
    private final String username;

    public User(String username) {
        this.username = username;
    }

    // Sin setters

    // Para "cambiar", crear uno nuevo
    public User withUsername(String newUsername) {
        return new User(newUsername);
    }
}

// Uso seguro
User user = new User("john");
User modified = user.withUsername("johnny");
// user sigue siendo "john" ✅
// modified es "johnny" ✅
```

### Crear Clases Inmutables

```java
// Checklist para inmutabilidad:
public final class Email {  // 1. Clase final (no heredable)

    private final String value;  // 2. Campos final (no cambian)

    // 3. Sin setters
    // public void setValue(String value) { }  ❌ NO

    // 4. Constructor que inicializa todo
    public Email(String value) {
        this.value = value;
    }

    // 5. Solo getters
    public String getValue() {
        return value;
    }

    // 6. Para "modificar", crear nuevo objeto
    public Email withDomain(String newDomain) {
        String[] parts = value.split("@");
        return new Email(parts[0] + "@" + newDomain);
    }
}
```

### Inmutabilidad con Colecciones

```java
// ❌ MUTABLE: La lista puede cambiar
public class Order {
    private final List<Item> items;

    public Order(List<Item> items) {
        this.items = items;  // ¡Referencia a lista mutable!
    }

    public List<Item> getItems() {
        return items;  // ¡Expone lista mutable!
    }
}

// Problema:
Order order = new Order(items);
order.getItems().add(newItem);  // 💥 Modificó el Order!

// ✅ INMUTABLE: Copias defensivas
public class Order {
    private final List<Item> items;

    public Order(List<Item> items) {
        this.items = new ArrayList<>(items);  // Copia defensiva
    }

    public List<Item> getItems() {
        return List.copyOf(items);  // Copia inmutable
        // o: Collections.unmodifiableList(items);
    }
}
```

### Beneficios de la Inmutabilidad

1. **Thread-safe**: No hay race conditions
2. **Predecible**: El objeto no cambia inesperadamente
3. **Cacheable**: Puedes guardar referencias sin preocuparte
4. **Testeable**: Fácil de probar (sin estado mutable)

---

## final Keyword - Garantizando Inmutabilidad

### ¿Qué es final?

`final` es una palabra clave que **previene modificaciones**:
- **Variables**: No se puede cambiar el valor/referencia
- **Métodos**: No se pueden sobrescribir (override)
- **Clases**: No se pueden heredar (extend)

---

### final en Variables

```java
// Variables locales
final String name = "John";
name = "Jane";  // ❌ Error de compilación

// Parámetros de método
public void processUser(final User user) {
    user = new User();  // ❌ Error de compilación
    user.setName("x");  // ✅ OK - solo la referencia es final
}

// Campos de clase
public class User {
    private final UUID id;  // ✅ Debe inicializarse en constructor
    private final String name;

    public User(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    // No hay setters - inmutable ✅
}
```

**IMPORTANTE:** `final` en una variable **NO hace el objeto inmutable**, solo la referencia:

```java
final List<String> list = new ArrayList<>();
list = new ArrayList<>();  // ❌ Error - no puedes cambiar la referencia
list.add("item");          // ✅ OK - el objeto List sigue siendo mutable

// Para inmutabilidad real, usa colecciones inmutables:
final List<String> immutableList = List.of("a", "b");
immutableList.add("c");  // ❌ UnsupportedOperationException
```

---

### final en Clases

```java
// ✅ Clase final - no se puede heredar
public final class Email {
    private final String value;

    public Email(String value) {
        this.value = value;
    }
}

// ❌ Error de compilación
public class GmailEmail extends Email {  // No se puede heredar de final
}
```

**¿Cuándo hacer una clase final?**
- ✅ Value Objects (Email, Username, Money)
- ✅ DTOs que no necesitan herencia
- ✅ Clases utilitarias (sin estado)
- ✅ Cuando quieres garantizar comportamiento inmutable

---

### final en Métodos

```java
public class BaseService {
    // ✅ Método final - no se puede sobrescribir
    public final void validateInput(String input) {
        if (input == null) throw new IllegalArgumentException();
    }

    // Método normal - se puede sobrescribir
    public void process(String input) {
        validateInput(input);
        // ... procesamiento
    }
}

public class ExtendedService extends BaseService {
    @Override
    public void validateInput(String input) {  // ❌ Error - método es final
        // ...
    }

    @Override
    public void process(String input) {  // ✅ OK - método no es final
        // ...
    }
}
```

**¿Cuándo hacer un método final?**
- ✅ Métodos críticos de seguridad
- ✅ Métodos de validación
- ✅ Template methods que no deben cambiar

---

### Ejemplo Completo del Proyecto

```java
/**
 * Value Object inmutable
 * - Clase final: No se puede heredar
 * - Campo final: No se puede modificar
 * - Sin setters: No hay mutación
 */
public final class Email {

    private final String value;  // ✅ Campo final - inmutable

    private Email(String value) {  // Constructor privado
        this.value = value;
    }

    // ✅ Factory method estático
    public static Email of(final String value) {  // ✅ Parámetro final
        validate(value);
        return new Email(value);
    }

    private static void validate(final String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Email no puede estar vacío");
        }
        if (!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("Email inválido");
        }
    }

    public String getValue() {
        return value;  // ✅ Solo getter, no setter
    }

    // ✅ Para "modificar", crear nuevo objeto
    public final Email withDomain(final String newDomain) {
        String localPart = value.split("@")[0];
        return Email.of(localPart + "@" + newDomain);
    }
}
```

Ver ejemplo real: `src/main/java/com/example/hexarch/user/domain/model/valueobject/Email.java`

---

## Inmutabilidad Profunda (Deep Immutability)

### Shallow vs Deep Immutability

```java
// ❌ SHALLOW IMMUTABILITY (solo la referencia es final)
public final class Order {
    private final List<Item> items;  // Lista es final, pero es MUTABLE
    private final Customer customer;  // Customer puede ser mutable

    public Order(List<Item> items, Customer customer) {
        this.items = items;      // ⚠️ Referencia compartida!
        this.customer = customer;  // ⚠️ Objeto mutable!
    }

    public List<Item> getItems() {
        return items;  // ⚠️ Expone lista mutable!
    }
}

// Problema:
List<Item> originalItems = new ArrayList<>();
Order order = new Order(originalItems, customer);

originalItems.add(newItem);  // 💥 Modificó el Order desde fuera!
order.getItems().clear();     // 💥 Modificó el Order desde dentro!
customer.setName("changed");   // 💥 Modificó el Customer!
```

```java
// ✅ DEEP IMMUTABILITY (todo es inmutable)
public final class Order {
    private final List<Item> items;  // Copia defensiva
    private final Customer customer;  // Inmutable

    public Order(List<Item> items, Customer customer) {
        // ✅ Copia defensiva en el constructor
        this.items = List.copyOf(items);  // Inmutable
        this.customer = customer;  // Asume que Customer es inmutable
    }

    public List<Item> getItems() {
        // ✅ Ya es inmutable por List.copyOf()
        return items;
    }

    // ✅ Para "modificar", crear nuevo Order
    public Order addItem(Item newItem) {
        List<Item> newItems = new ArrayList<>(this.items);
        newItems.add(newItem);
        return new Order(newItems, this.customer);
    }
}
```

---

### Defensive Copying (Copias Defensivas)

**Regla:** Nunca confíes en referencias que vienen de fuera o salen hacia fuera.

```java
public final class User {
    private final List<Role> roles;  // Lista de roles

    // ✅ CORRECTO: Copia defensiva en constructor
    public User(List<Role> roles) {
        this.roles = List.copyOf(roles);  // Inmutable
        // Alternativas:
        // this.roles = Collections.unmodifiableList(new ArrayList<>(roles));
        // this.roles = new ArrayList<>(roles);  // Si quieres mutable interno
    }

    // ✅ CORRECTO: Devolver copia inmutable
    public List<Role> getRoles() {
        return roles;  // Ya es inmutable por List.copyOf()
        // O si es mutable interno:
        // return List.copyOf(roles);
        // return Collections.unmodifiableList(roles);
    }
}
```

**Ejemplo Real del Proyecto:**

```java
// En User.java (Aggregate Root)
public final class User {
    private final UUID id;
    private final Username username;  // ✅ Username es inmutable
    private final Email email;        // ✅ Email es inmutable
    private final boolean enabled;
    private final Instant createdAt;  // ✅ Instant es inmutable

    // ✅ Todo es final e inmutable (deep immutability)

    // No hay setters - para "modificar", crear nuevo User
    public User withUsername(Username newUsername) {
        return new User(this.id, newUsername, this.email, this.enabled, this.createdAt);
    }
}
```

Ver código completo: `src/main/java/com/example/hexarch/user/domain/model/User.java:1-142`

---

### Colecciones Inmutables en Detalle

```java
// ========================================
// COLECCIONES INMUTABLES (Java 9+)
// ========================================

// ✅ List.of() - Inmutable, tamaño fijo, no acepta null
List<String> list = List.of("a", "b", "c");
list.add("d");     // ❌ UnsupportedOperationException
list.set(0, "x");  // ❌ UnsupportedOperationException
list.remove(0);    // ❌ UnsupportedOperationException

// ✅ Set.of() - Inmutable, sin duplicados, no acepta null
Set<String> set = Set.of("a", "b", "c");
set.add("d");  // ❌ UnsupportedOperationException

// ✅ Map.of() - Inmutable, no acepta null
Map<String, Integer> map = Map.of("a", 1, "b", 2, "c", 3);
map.put("d", 4);  // ❌ UnsupportedOperationException

// Para más de 10 elementos en Map:
Map<String, Integer> bigMap = Map.ofEntries(
    Map.entry("a", 1),
    Map.entry("b", 2),
    // ... más entradas
);

// ========================================
// ALTERNATIVAS PRE-JAVA 9
// ========================================

// Collections.unmodifiableList() - Inmutable VIEW (la original puede cambiar)
List<String> original = new ArrayList<>(Arrays.asList("a", "b"));
List<String> unmodifiable = Collections.unmodifiableList(original);

unmodifiable.add("c");  // ❌ UnsupportedOperationException
original.add("c");      // ✅ OK - modifica la original
System.out.println(unmodifiable);  // [a, b, c] ⚠️ Cambió!

// ✅ Copia inmutable real
List<String> immutable = List.copyOf(original);
original.add("d");
System.out.println(immutable);  // [a, b] ✅ No cambió

// ========================================
// COMPARACIÓN
// ========================================
```

| Método | Inmutable | Acepta null | Performance | Versión |
|--------|-----------|-------------|-------------|---------|
| `List.of()` | ✅ Sí | ❌ No | ⚡ Rápido | Java 9+ |
| `List.copyOf()` | ✅ Sí | ❌ No | ⚡ Rápido | Java 10+ |
| `Collections.unmodifiableList()` | ⚠️ View | ✅ Sí | ⚡ Rápido | Java 1.2+ |
| `new ArrayList<>()` | ❌ No | ✅ Sí | 🐌 Lento | Siempre |

**Recomendación:** Usa `List.of()`, `Set.of()`, `Map.of()` en Java 9+

---

## Records en Profundidad (Java 21)

### Compact Constructor - Validaciones

El **compact constructor** es la forma idiomática de validar Records:

```java
// ✅ Compact Constructor (sin parámetros explícitos)
public record Email(String value) {

    // ✅ Este es el compact constructor
    public Email {
        // Se ejecuta ANTES de asignar los campos
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email vacío");
        }
        if (!value.contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }
        // No necesitas asignar: this.value = value;
        // El compilador lo hace automáticamente DESPUÉS de la validación
    }
}

// ❌ Constructor canónico explícito (más verboso)
public record Email(String value) {

    public Email(String value) {  // Constructor canónico
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email vacío");
        }
        this.value = value;  // ❌ Necesitas asignación explícita
    }
}
```

**Ventaja del Compact Constructor:**
- Más conciso (no repites los parámetros)
- No necesitas asignación explícita
- El compilador garantiza que todos los campos se inicializan

---

### Records con Normalización

```java
public record Username(String value) {

    public Username {
        // Validar
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Username vacío");
        }

        // ✅ Normalizar (trim, lowercase)
        value = value.trim().toLowerCase();

        // Validar después de normalizar
        if (value.length() < 3 || value.length() > 50) {
            throw new IllegalArgumentException("Username debe tener 3-50 caracteres");
        }
    }
}

// Uso:
Username u1 = new Username("  JohnDoe  ");  // Normaliza a "johndoe"
Username u2 = new Username("johndoe");
u1.equals(u2);  // true - son iguales después de normalización
```

---

### Records vs Clases - Comparación Detallada

| Característica | Record | Clase Normal |
|----------------|--------|--------------|
| **Boilerplate** | ⚡ Mínimo (~5 líneas) | 🐌 Mucho (~50 líneas) |
| **Inmutabilidad** | ✅ Garantizada | ⚠️ Manual |
| **Herencia** | ❌ No puede extender (solo implementar interfaces) | ✅ Puede extender |
| **Campos** | ✅ Todos final, públicos (vía accessors) | ⚠️ Tú decides |
| **Constructor** | ✅ Automático (canónico + compacto) | ⚠️ Manual |
| **equals/hashCode** | ✅ Automático (por valor) | ⚠️ Manual o @EqualsAndHashCode |
| **toString** | ✅ Automático | ⚠️ Manual o @ToString |
| **Serialización** | ✅ Funciona con Jackson/JPA | ✅ Funciona |
| **Métodos adicionales** | ✅ Puedes agregar | ✅ Puedes agregar |
| **Uso típico** | DTOs, Value Objects simples, Commands/Queries | Entities, Aggregates, Services |

---

### Records con Jackson (Serialización JSON)

```java
// ✅ Records funcionan perfectamente con Jackson
public record CreateUserRequest(
    String username,
    String email
) {}

// Deserialización (JSON → Record)
String json = """
    {
        "username": "johndoe",
        "email": "john@example.com"
    }
    """;

ObjectMapper mapper = new ObjectMapper();
CreateUserRequest request = mapper.readValue(json, CreateUserRequest.class);
// ✅ Jackson usa el constructor canónico

// Serialización (Record → JSON)
String jsonOutput = mapper.writeValueAsString(request);
// {"username":"johndoe","email":"john@example.com"}

// ✅ Con Jackson annotations
public record UserResponse(
    @JsonProperty("user_id") String id,  // Cambia nombre en JSON
    String username,
    String email,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    Instant createdAt
) {}
```

---

### Records con Bean Validation

```java
// ✅ Records con validaciones de Bean Validation
public record CreateUserRequest(
    @NotBlank(message = "Username es requerido")
    @Size(min = 3, max = 50, message = "Username debe tener 3-50 caracteres")
    String username,

    @NotBlank(message = "Email es requerido")
    @Email(message = "Email debe ser válido")
    String email
) {}

// Validación automática en Controllers
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(
    @Valid @RequestBody CreateUserRequest request  // @Valid activa validaciones
) {
    // Si la validación falla, Spring lanza MethodArgumentNotValidException
    // ...
}
```

---

### Cuándo NO Usar Records

❌ **No uses Records cuando:**

1. **Necesitas herencia (extends)**
   ```java
   // ❌ Records NO pueden extender clases
   public record AdminUser(String username) extends User { }  // Error

   // ✅ Usa clase normal si necesitas herencia
   public class AdminUser extends User {
       public AdminUser(String username) {
           super(username);
       }
   }
   ```

2. **Necesitas mutabilidad (setters)**
   ```java
   // ❌ Records son inmutables
   public record User(String name) {
       public void setName(String name) {  // No tiene sentido
           this.name = name;  // ❌ final, no se puede cambiar
       }
   }

   // ✅ Usa clase normal si necesitas mutabilidad
   public class User {
       private String name;
       public void setName(String name) {
           this.name = name;
       }
   }
   ```

3. **Aggregates con lógica compleja**
   ```java
   // ❌ Record para Aggregate con mucha lógica (no idiomático)
   public record User(UUID id, String username, String email) {
       public void validateBusinessRules() { }
       public void applyDomainEvent() { }
       // ... 50 métodos más
   }

   // ✅ Usa clase normal para Aggregates
   public class User {
       // Lógica de dominio compleja
   }
   ```

4. **Necesitas control fino sobre equals/hashCode**
   ```java
   // ❌ Record usa TODOS los campos para equals
   public record User(UUID id, String username, Instant lastLogin) {}
   // lastLogin participa en equals (probablemente no quieres eso)

   // ✅ Usa clase normal si necesitas equals customizado
   public class User {
       @Override
       public boolean equals(Object o) {
           // Solo comparar por id
       }
   }
   ```

---

### Resumen: ¿Record o Clase?

```
┌─────────────────────────────────────────────────────────────┐
│                    DECISION TREE                             │
└─────────────────────────────────────────────────────────────┘

¿Es un DTO inmutable?
├─ Sí → ✅ Record
└─ No ↓

¿Es un Value Object simple?
├─ Sí → ✅ Record
└─ No ↓

¿Necesitas herencia?
├─ Sí → ❌ Clase normal
└─ No ↓

¿Necesitas mutabilidad?
├─ Sí → ❌ Clase normal
└─ No ↓

¿Tiene lógica de negocio compleja?
├─ Sí → ❌ Clase normal (Aggregate/Entity)
└─ No → ✅ Record
```

---

### Ejemplos del Proyecto con Records

```java
// ✅ Commands (CQRS Write side)
public record CreateUserCommand(String username, String email) {}

// ✅ Queries (CQRS Read side)
public record GetUserQuery(UUID userId) {}

// ✅ Results (DTOs de retorno)
public record UserResult(
    UUID id,
    String username,
    String email,
    boolean enabled,
    Instant createdAt
) {
    // Factory method
    public static UserResult from(User user) {
        return new UserResult(
            user.getId(),
            user.getUsername().getValue(),
            user.getEmail().getValue(),
            user.isEnabled(),
            user.getCreatedAt()
        );
    }
}

// ✅ Domain Events
public record UserCreatedEvent(
    UUID userId,
    String username,
    String email,
    Instant occurredAt
) {
    public static UserCreatedEvent from(User user) {
        return new UserCreatedEvent(
            user.getId(),
            user.getUsername().getValue(),
            user.getEmail().getValue(),
            Instant.now()
        );
    }
}

// ✅ REST DTOs (OpenAPI generated)
public record CreateUserRequest(
    @NotBlank String username,
    @Email String email
) {}

public record UserResponse(
    String id,
    String username,
    String email,
    boolean enabled,
    Instant createdAt
) {}
```

Ver ejemplos reales en:
- Commands: `src/main/java/.../application/port/input/`
- DTOs: `src/main/java/.../infrastructure/adapter/input/rest/dto/`
- Events: `src/main/java/.../domain/event/`

---

## var - Inferencia de Tipos (Java 10+)

### ¿Qué es var?

`var` permite al compilador **inferir el tipo** automáticamente.

### Uso Básico

```java
// Sin var (verboso)
List<String> names = new ArrayList<String>();
Map<UUID, User> userMap = new HashMap<UUID, User>();
UserResult result = new UserResult(id, username, email);

// Con var (conciso)
var names = new ArrayList<String>();
var userMap = new HashMap<UUID, User>();
var result = new UserResult(id, username, email);
```

### Cuándo usar var

```java
// ✅ Cuando el tipo es obvio
var user = new User("john", "john@ex.com");  // Obvio que es User
var users = userRepository.findAll();        // Obvio que es List<User>
var request = new CreateUserRequest("john", "john@ex.com");

// ✅ Con tipos complejos/genéricos
var map = new HashMap<UUID, Map<String, List<Order>>>();  // Evita repetir el tipo

// ❌ NO cuando el tipo no es claro
var result = process();  // ¿Qué devuelve process()?
var value = getValue();  // ¿String? Integer? Object?

// ✅ Mejor con tipo explícito cuando no es obvio
String result = process();
int value = getValue();
```

### Restricciones de var

```java
// ❌ NO se puede usar en:
var x;  // Sin inicialización
var y = null;  // Sin tipo inferible
private var field;  // Campos de clase
public var method() { }  // Tipo de retorno
var lambda = x -> x * 2;  // Lambdas sin contexto
```

### Ejemplo del Proyecto

```java
// En el Service
public UserResult execute(CreateUserCommand command) {
    // ✅ var cuando el tipo es obvio
    var username = Username.of(command.username());
    var email = Email.of(command.email());
    var user = User.create(command.username(), command.email());
    var savedUser = userRepository.save(user);
    var event = UserCreatedEvent.from(savedUser.getId(), ...);

    // ✅ var en bucles
    for (var user : users) {
        System.out.println(user.getUsername());
    }

    return new UserResult(...);
}
```

---

## Try-with-Resources

### El Problema con Recursos

```java
// ❌ VIEJO: Cerrar manualmente (propenso a errores)
FileInputStream fis = null;
try {
    fis = new FileInputStream("file.txt");
    // usar fis
} catch (IOException e) {
    e.printStackTrace();
} finally {
    if (fis != null) {
        try {
            fis.close();  // Puede fallar!
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

### Try-with-Resources

```java
// ✅ MODERNO: Cierra automáticamente
try (FileInputStream fis = new FileInputStream("file.txt")) {
    // usar fis
} catch (IOException e) {
    e.printStackTrace();
}  // fis se cierra automáticamente, incluso si hay excepción
```

### Múltiples Recursos

```java
// ✅ Múltiples recursos
try (
    var input = new FileInputStream("input.txt");
    var output = new FileOutputStream("output.txt");
    var reader = new BufferedReader(new InputStreamReader(input))
) {
    // usar input, output, reader
}  // Todos se cierran automáticamente en orden inverso
```

### Recursos Personalizados

Cualquier clase que implemente `AutoCloseable` puede usarse:

```java
public class DatabaseConnection implements AutoCloseable {
    public void open() {
        // abrir conexión
    }

    @Override
    public void close() {
        // cerrar conexión (llamado automáticamente)
    }
}

// Uso
try (var conn = new DatabaseConnection()) {
    conn.open();
    // usar conexión
}  // conn.close() llamado automáticamente
```

---

## Switch Expressions (Java 14+)

### Switch Tradicional vs Switch Expression

```java
// ❌ VIEJO: Switch statement (verboso)
String message;
switch (status) {
    case ACTIVE:
        message = "Usuario activo";
        break;
    case DISABLED:
        message = "Usuario deshabilitado";
        break;
    case PENDING:
        message = "Usuario pendiente";
        break;
    default:
        message = "Estado desconocido";
        break;
}

// ✅ MODERNO: Switch expression (conciso)
var message = switch (status) {
    case ACTIVE -> "Usuario activo";
    case DISABLED -> "Usuario deshabilitado";
    case PENDING -> "Usuario pendiente";
    default -> "Estado desconocido";
};  // Devuelve un valor!
```

### Múltiples Casos

```java
var dayType = switch (dayOfWeek) {
    case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Día laboral";
    case SATURDAY, SUNDAY -> "Fin de semana";
};
```

### Con Bloques

```java
var result = switch (value) {
    case 1 -> "Uno";
    case 2 -> {
        System.out.println("Es dos");
        yield "Dos";  // yield en lugar de return
    }
    case 3 -> "Tres";
    default -> "Otro";
};
```

### Pattern Matching (Java 17+)

```java
// Verificar tipo y castear en un paso
var description = switch (obj) {
    case String s -> "String de longitud " + s.length();
    case Integer i -> "Entero: " + i;
    case List list -> "Lista con " + list.size() + " elementos";
    case null -> "Es null";
    default -> "Tipo desconocido";
};
```

---

## Resumen: Cuándo Usar Qué

| Necesitas | Usa |
|-----------|-----|
| Evitar nulls | `Optional<T>` |
| Procesar colección | `Stream` + `filter/map/collect` |
| Función anónima | Lambda `x -> x * 2` |
| DTO inmutable | `record` |
| Lista ordenada | `ArrayList<T>` |
| Sin duplicados | `HashSet<T>` o `Set.of()` |
| Búsqueda por clave | `HashMap<K, V>` |
| Tipo obvio | `var` |
| Cerrar recursos | `try-with-resources` |
| Switch con valor | Switch expression |
| Inmutabilidad | `final` fields + no setters |

---

## Checklist de Buenas Prácticas

### ✅ DO (Hacer)

- Usar Optional para valores que pueden no existir
- Usar Streams para procesar colecciones
- Usar Lambdas para código conciso
- Usar Records para DTOs
- Usar colecciones inmutables cuando sea posible
- Usar var cuando el tipo es obvio
- Usar try-with-resources para recursos
- Preferir inmutabilidad

### ❌ DON'T (No Hacer)

- NO llamar `Optional.get()` sin verificar
- NO modificar colecciones durante iteración
- NO usar Streams para operaciones simples
- NO usar Optional como parámetro de método
- NO exponer colecciones mutables
- NO usar var cuando el tipo no es claro
- NO olvidar cerrar recursos
- NO hacer clases mutables sin razón

---

## Recursos para Seguir Aprendiendo

1. **Documentación Oficial:** [Java SE Documentation](https://docs.oracle.com/en/java/)
2. **Modern Java in Action** - Libro sobre Java 8+
3. **Effective Java** - Joshua Bloch (mejores prácticas)
4. **Java Brains** - Canal de YouTube con tutoriales

---

**¡Usa estas features modernas para escribir código más limpio, seguro y conciso!** 🚀
