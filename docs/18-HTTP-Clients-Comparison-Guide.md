# Guía Completa: HTTP Clients en Spring Boot 3+ (2025)

## 📚 Índice

1. [Introducción - Estado Actual 2025](#introducción---estado-actual-2025)
2. [HTTP Interface - LA OPCIÓN RECOMENDADA](#http-interface---la-opción-recomendada)
3. [RestClient - Cliente HTTP Imperativo](#restclient---cliente-http-imperativo)
4. [FeignClient - Cliente HTTP Declarativo (Maintenance Mode)](#feignclient---cliente-http-declarativo-maintenance-mode)
5. [Comparación Directa: Código Lado a Lado](#comparación-directa-código-lado-a-lado)
6. [¿Cuál Elegir? Casos de Uso Reales](#cuál-elegir-casos-de-uso-reales)
7. [Tabla de Decisión](#tabla-de-decisión)
8. [Ventajas y Desventajas](#ventajas-y-desventajas)
9. [Mejores Prácticas](#mejores-prácticas)
10. [Otras Opciones](#otras-opciones-resttemplate-y-webclient)

---

## Introducción - Estado Actual 2025

En Spring Boot 3+ (Spring Framework 6), cuando necesitas consumir una API REST externa, ahora tienes **TRES opciones principales**:

### 🌟 1. HTTP Interface (@HttpExchange) - ⭐ **RECOMENDADO**
```java
// Declarativo, nativo de Spring, sin dependencias adicionales
@GetExchange("/users/{id}")
User getUser(@PathVariable Long id);
```
**Estado**: ✅ Activo, recomendado oficialmente por Spring

### 2. RestClient - Control Total
```java
// Imperativo, control total sobre HTTP
User user = restClient.get()
        .uri("/users/{id}", userId)
        .retrieve()
        .body(User.class);
```
**Estado**: ✅ Activo

### 3. FeignClient - Legacy pero Funcional
```java
// Declarativo tradicional, requiere spring-cloud-openfeign
@FeignClient(name = "api")
public interface UserClient {
    @GetMapping("/users/{id}")
    User getUser(@PathVariable Long id);
}
```
**Estado**: ⚠️ **Maintenance Mode** (menos desarrollo activo)

---

## ⚡ Cambio Importante: FeignClient en Maintenance Mode

**Actualización 2025**: FeignClient **NO está oficialmente deprecado**, pero está en **"maintenance mode"**:
- ⚠️ Menos desarrollo activo
- ✅ Sigue siendo funcional y soportado
- ✅ Válido para microservicios con Spring Cloud
- ⚠️ Spring recomienda HTTP Interface para nuevos proyectos

**La nueva opción recomendada es HTTP Interface** - nativa de Spring Framework 6, sin dependencias adicionales.

---

## HTTP Interface - LA OPCIÓN RECOMENDADA

### ¿Qué es HTTP Interface?

HTTP Interface es la forma **NATIVA y MODERNA** de Spring Framework 6 (Spring Boot 3+) para crear clientes HTTP declarativos. Es similar a FeignClient pero **mejor en casi todo**:

| Aspecto | HTTP Interface | FeignClient |
|---------|----------------|-------------|
| **Estado** | ✅ Activo, recomendado | ⚠️ Maintenance mode |
| **Dependencias** | ✅ Ninguna (core Spring) | ❌ spring-cloud-openfeign |
| **Backend** | RestClient o WebClient | Feign (tercero) |
| **Performance** | ✅ Óptimo | ⚠️ Overhead adicional |
| **Desde** | Spring Framework 6.0 | Spring Cloud Netflix |

### Configuración

**1. NO necesitas agregar dependencias** (ya está en Spring Boot 3+)

**2. Define la interface con @HttpExchange:**

```java
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface UserApiInterface {

    @GetExchange("/users/{id}")
    User getUser(@PathVariable Long id);

    @PostExchange("/users")
    User createUser(@RequestBody CreateUserRequest request);

    @DeleteExchange("/users/{id}")
    void deleteUser(@PathVariable Long id);
}
```

**3. Configura el HttpServiceProxyFactory:**

```java
@Configuration
public class HttpInterfaceConfig {

    @Bean
    public UserApiInterface userApiInterface(RestClient restClient) {
        // Crear adaptador
        RestClientAdapter adapter = RestClientAdapter.create(restClient);

        // Crear factory
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();

        // Generar proxy
        return factory.createClient(UserApiInterface.class);
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl("https://api.example.com")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
```

**4. Inyectar y usar:**

```java
@Service
public class UserService {

    private final UserApiInterface userApi;

    public UserService(UserApiInterface userApi) {
        this.userApi = userApi;
    }

    public User getUser(Long id) {
        return userApi.getUser(id);  // ✨ Simple y declarativo
    }
}
```

### ¿Cómo funciona internamente?

```
1. Defines interface con @GetExchange/@PostExchange
   ↓
2. HttpServiceProxyFactory crea un PROXY dinámico
   ↓
3. El proxy usa RestClient (o WebClient) para hacer llamadas HTTP reales
   ↓
4. Inyectas y usas la interface como un bean normal
```

### Ventajas de HTTP Interface ✅

1. **Sin Dependencias Extra**: Incluido en Spring Boot 3+ (core)
2. **Recomendado Oficialmente**: Por Spring para proyectos nuevos
3. **Declarativo**: Sintaxis limpia (solo interface)
4. **Flexible**: Usa RestClient (síncrono) o WebClient (reactivo) como backend
5. **Performance Óptimo**: Sin overhead de librerías terceras
6. **Observability Nativa**: Si el RestClient tiene Micrometer configurado
7. **Control Total**: Configuras el RestClient como quieras
8. **Fácil Testing**: Mock fácil de la interface

### Desventajas de HTTP Interface ❌

1. **Configuración Manual**: No tiene auto-configuración (vs FeignClient con @EnableFeignClients)
2. **Sin Spring Cloud Integration**: No service discovery automático
3. **Relativamente Nueva**: Menos ejemplos y recursos que FeignClient (pero está madurando rápido)

### Ejemplo Completo en Este Proyecto

Ver implementación real en:
- `JsonPlaceholderHttpInterface.java` - Interface declarativa
- `JsonPlaceholderHttpInterfaceAdapter.java` - Adaptador del port
- `HttpInterfaceConfig.java` - Configuración

```java
// 1. Interface HTTP
@HttpExchange
public interface JsonPlaceholderHttpInterface {
    @GetExchange("/users/{id}")
    JsonPlaceholderUserResponse getUserById(@PathVariable("id") Integer userId);

    @PostExchange("/users")
    JsonPlaceholderUserResponse createUser(@RequestBody CreateUserRequest request);
}

// 2. Adaptador que implementa el port
@Component
@Qualifier("httpInterface")
public class JsonPlaceholderHttpInterfaceAdapter implements ExternalUserApiClient {

    private final JsonPlaceholderHttpInterface httpInterface;

    public JsonPlaceholderHttpInterfaceAdapter(JsonPlaceholderHttpInterface httpInterface) {
        this.httpInterface = httpInterface;
    }

    @Override
    public Optional<ExternalUserData> getUserById(Integer userId) {
        try {
            JsonPlaceholderUserResponse response = httpInterface.getUserById(userId);
            return Optional.of(mapToExternalUserData(response));
        } catch (RestClientException e) {
            return Optional.empty();
        }
    }
}

// 3. Configuración
@Configuration
public class HttpInterfaceConfig {
    @Bean
    public JsonPlaceholderHttpInterface jsonPlaceholderHttpInterface(
            @Qualifier("jsonPlaceholderRestClient") RestClient restClient) {

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter).build();

        return factory.createClient(JsonPlaceholderHttpInterface.class);
    }
}
```

### HTTP Interface con WebClient (Reactivo)

Si necesitas reactividad, solo cambia el adapter:

```java
@Bean
public UserApiInterface userApiInterface(WebClient webClient) {
    // Cambiar a WebClientAdapter
    WebClientAdapter adapter = WebClientAdapter.create(webClient);

    HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(adapter)
            .build();

    return factory.createClient(UserApiInterface.class);
}

// La interface puede retornar Mono/Flux
@HttpExchange
public interface UserApiInterface {
    @GetExchange("/users/{id}")
    Mono<User> getUser(@PathVariable Long id);  // Reactivo
}
```

---

## La Decisión Real: RestClient vs FeignClient

### RestClient (Spring 6.1+)
```java
// Escribes el código HTTP manualmente
User user = restClient.get()
        .uri("/users/{id}", userId)
        .retrieve()
        .body(User.class);
```

**Filosofía**: "Yo controlo cada detalle del HTTP request"

### FeignClient (OpenFeign)
```java
// Solo defines una interface, Feign hace el resto
@FeignClient(name = "user-api", url = "https://api.example.com")
public interface UserClient {
    @GetMapping("/users/{id}")
    User getUser(@PathVariable Long id);
}

// Usar
User user = userClient.getUser(userId); // ✨ Magia
```

**Filosofía**: "Menos código, que Spring haga el trabajo pesado"

---

## RestClient - Cliente HTTP Imperativo

### ¿Qué es?

- Cliente HTTP moderno introducido en Spring 6.1 (2023)
- API fluida similar a WebClient pero **síncrona**
- Control total sobre requests/responses
- Sin dependencias adicionales (incluido en Spring Boot 3.2+)

### Configuración

```java
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient apiRestClient() {
        // Configurar timeouts
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        return RestClient.builder()
                .baseUrl("https://api.example.com")
                .requestFactory(requestFactory)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("User-Agent", "MyApp/1.0")
                .build();
    }
}
```

### Uso Básico

```java
@Service
public class UserService {

    private final RestClient restClient;

    public UserService(RestClient restClient) {
        this.restClient = restClient;
    }

    // GET request
    public User getUser(Long id) {
        return restClient.get()
                .uri("/users/{id}", id)
                .retrieve()
                .body(User.class);
    }

    // POST request
    public User createUser(CreateUserRequest request) {
        return restClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(User.class);
    }

    // DELETE request
    public void deleteUser(Long id) {
        restClient.delete()
                .uri("/users/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    // Manejo de errores
    public Optional<User> getUserSafe(Long id) {
        try {
            return Optional.ofNullable(
                restClient.get()
                    .uri("/users/{id}", id)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                        (req, res) -> { /* handle */ })
                    .body(User.class)
            );
        } catch (RestClientException e) {
            log.error("Error fetching user", e);
            return Optional.empty();
        }
    }
}
```

### Ventajas de RestClient ✅

1. **Control Total**: Manejas cada aspecto del HTTP request
2. **Debugging Fácil**: Ves exactamente qué está pasando
3. **Flexible**: Puedes personalizar todo (headers, interceptores, etc.)
4. **Sin Dependencias Extra**: Incluido en Spring Boot 3.2+
5. **Observability**: Integración nativa con Micrometer
6. **Interceptores**: Fácil agregar logging, autenticación, etc.

### Desventajas de RestClient ❌

1. **Más Código**: Necesitas escribir cada request manualmente
2. **Boilerplate**: Más líneas de código para casos simples
3. **Mantenimiento**: Cambios en la API requieren actualizar código

---

## FeignClient - Cliente HTTP Declarativo (Maintenance Mode)

### ⚠️ Estado Actual (2025)

**FeignClient está en "Maintenance Mode":**
- ⚠️ Menos desarrollo activo (Spring prioriza HTTP Interface)
- ✅ **NO está deprecado** - sigue siendo funcional y soportado
- ✅ Válido para microservicios existentes con Spring Cloud
- ⚠️ Para proyectos nuevos, Spring recomienda HTTP Interface

### ¿Qué es?

- Cliente HTTP **declarativo** de OpenFeign (Netflix OSS)
- Solo defines **interfaces**, Feign genera la implementación
- Popular en microservicios con Spring Cloud (legacy)
- Integración con Spring Cloud (service discovery, load balancing)

### Setup

```xml
<!-- 1. Agregar dependencia -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

```java
// 2. Habilitar Feign en tu aplicación
@SpringBootApplication
@EnableFeignClients
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Uso Básico

```java
// 3. Define la interface (NO implementación)
@FeignClient(
    name = "user-api",
    url = "https://api.example.com",
    configuration = FeignConfig.class
)
public interface UserClient {

    @GetMapping("/users/{id}")
    User getUser(@PathVariable("id") Long id);

    @PostMapping("/users")
    User createUser(@RequestBody CreateUserRequest request);

    @DeleteMapping("/users/{id}")
    void deleteUser(@PathVariable("id") Long id);

    @GetMapping("/users")
    List<User> getAllUsers(@RequestParam("page") int page,
                          @RequestParam("size") int size);
}

// 4. Inyectar y usar (Feign implementa la interface automáticamente)
@Service
public class UserService {

    private final UserClient userClient;

    public UserService(UserClient userClient) {
        this.userClient = userClient;
    }

    public User getUser(Long id) {
        return userClient.getUser(id);  // ✨ Simple!
    }
}
```

### Configuración Avanzada

```java
@Configuration
public class FeignConfig {

    // Configurar timeouts
    @Bean
    public Request.Options options() {
        return new Request.Options(
            Duration.ofSeconds(5),  // connectTimeout
            Duration.ofSeconds(10)  // readTimeout
        );
    }

    // Agregar headers por defecto
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("User-Agent", "MyApp/1.0");
            requestTemplate.header("Accept", "application/json");
        };
    }

    // Manejo de errores custom
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            if (response.status() == 404) {
                return new UserNotFoundException("User not found");
            }
            return new FeignException.FeignClientException(
                response.status(),
                "Error calling API",
                response.request(),
                response.body()
            );
        };
    }

    // Logging
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // NONE, BASIC, HEADERS, FULL
    }
}
```

### Ventajas de FeignClient ✅

1. **Menos Código**: Solo defines interfaces
2. **Declarativo**: API clara y fácil de leer
3. **Integración Spring Cloud**: Service discovery, load balancing automático
4. **Retry Automático**: Con Spring Retry
5. **Circuit Breaker**: Integración fácil con Resilience4j
6. **Mantenimiento**: Cambios en API solo actualizan la interface

### Desventajas de FeignClient ❌

1. **Dependencia Extra**: Requiere spring-cloud-starter-openfeign
2. **Magic**: Implementación oculta, debugging más difícil
3. **Menos Control**: Más difícil personalizar comportamiento complejo
4. **Learning Curve**: Configuración puede ser confusa al inicio
5. **Overhead**: Un poco más de overhead que RestClient

---

## Comparación Directa: Código Lado a Lado

### Caso 1: GET request simple

#### HTTP Interface (⭐ RECOMENDADO)
```java
// Interface
@GetExchange("/users/{id}")
User getUser(@PathVariable Long id);

// Uso
User user = userApi.getUser(userId);
```

#### RestClient
```java
User user = restClient.get()
        .uri("/users/{id}", userId)
        .retrieve()
        .body(User.class);
```

#### FeignClient
```java
@GetMapping("/users/{id}")
User getUser(@PathVariable Long id);

// Uso
User user = userClient.getUser(userId);
```

**Ganador**: HTTP Interface y FeignClient empatan (ambos declarativos, pero HTTP Interface es nativo) ✨

---

### Caso 2: POST request con body

#### HTTP Interface (⭐ RECOMENDADO)
```java
// Interface
@PostExchange("/users")
User createUser(@RequestBody CreateUserRequest request);

// Uso
User user = userApi.createUser(createRequest);
```

#### RestClient
```java
User user = restClient.post()
        .uri("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .body(createRequest)
        .retrieve()
        .body(User.class);
```

#### FeignClient
```java
@PostMapping("/users")
User createUser(@RequestBody CreateUserRequest request);

// Uso
User user = userClient.createUser(createRequest);
```

**Ganador**: HTTP Interface (nativo, sin deps) > FeignClient (deps adicionales) ✨

---

### Caso 3: Headers dinámicos

#### HTTP Interface
```java
// Interface
@GetExchange("/users/{id}")
User getUser(@PathVariable Long id,
             @RequestHeader("Authorization") String auth,
             @RequestHeader("X-Request-ID") String requestId);

// Uso
User user = userApi.getUser(userId, "Bearer " + token, requestId);
```

#### RestClient
```java
User user = restClient.get()
        .uri("/users/{id}", userId)
        .header("Authorization", "Bearer " + token)
        .header("X-Request-ID", requestId)
        .retrieve()
        .body(User.class);
```

#### FeignClient
```java
@GetMapping("/users/{id}")
User getUser(@PathVariable Long id,
             @RequestHeader("Authorization") String auth,
             @RequestHeader("X-Request-ID") String requestId);

// Uso
User user = userClient.getUser(userId, "Bearer " + token, requestId);
```

**Ganador**: RestClient (más flexible, headers en tiempo de ejecución) 🎯

---

### Caso 4: Manejo de errores complejo

#### RestClient
```java
try {
    User user = restClient.get()
            .uri("/users/{id}", userId)
            .retrieve()
            .onStatus(status -> status.value() == 404,
                (req, res) -> {
                    throw new UserNotFoundException("User not found");
                })
            .onStatus(HttpStatusCode::is5xxServerError,
                (req, res) -> {
                    throw new ServerException("Server error");
                })
            .body(User.class);
    return Optional.of(user);
} catch (RestClientException e) {
    log.error("Error fetching user", e);
    return Optional.empty();
}
```

#### FeignClient
```java
// Configurar ErrorDecoder una vez
@Bean
public ErrorDecoder errorDecoder() {
    return (methodKey, response) -> {
        if (response.status() == 404) {
            return new UserNotFoundException("User not found");
        }
        if (response.status() >= 500) {
            return new ServerException("Server error");
        }
        return new Exception("Unknown error");
    };
}

// Uso simple
try {
    User user = userClient.getUser(userId);
    return Optional.of(user);
} catch (UserNotFoundException e) {
    log.error("User not found", e);
    return Optional.empty();
}
```

**Ganador**: Empate (Feign más limpio una vez configurado, RestClient más explícito)

---

### Caso 5: Query params complejos

#### RestClient
```java
List<User> users = restClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/users")
            .queryParam("page", page)
            .queryParam("size", size)
            .queryParam("sort", "name")
            .queryParam("status", "ACTIVE")
            .build())
        .retrieve()
        .body(new ParameterizedTypeReference<List<User>>() {});
```

#### FeignClient
```java
@GetMapping("/users")
List<User> getUsers(@RequestParam("page") int page,
                    @RequestParam("size") int size,
                    @RequestParam("sort") String sort,
                    @RequestParam("status") String status);

// Uso
List<User> users = userClient.getUsers(page, size, "name", "ACTIVE");
```

**Ganador**: FeignClient (mucho más limpio) ✨

---

## ¿Cuál Elegir? Casos de Uso Reales

### ✅ Usa RestClient cuando:

#### 1. Necesitas Control Total
```java
// Ejemplo: Necesitas custom retry logic
public User getUserWithRetry(Long id) {
    int attempts = 0;
    while (attempts < 3) {
        try {
            return restClient.get()
                    .uri("/users/{id}", id)
                    .retrieve()
                    .body(User.class);
        } catch (RestClientException e) {
            attempts++;
            if (attempts >= 3) throw e;
            Thread.sleep(1000 * attempts); // backoff exponencial
        }
    }
}
```

#### 2. APIs Complejas/No Estándar
```java
// Ejemplo: API que usa headers custom para autenticación
User user = restClient.get()
        .uri("/users/{id}", userId)
        .header("X-API-Key", apiKey)
        .header("X-Signature", generateSignature(payload))
        .header("X-Timestamp", timestamp)
        .retrieve()
        .body(User.class);
```

#### 3. Debugging y Troubleshooting Intensivo
```java
// Puedes ver cada paso
log.debug("Calling API: GET /users/{}", userId);
User user = restClient.get()
        .uri("/users/{id}", userId)
        .retrieve()
        .body(User.class);
log.debug("Received user: {}", user);
```

#### 4. Proyecto Sin Spring Cloud
- No necesitas service discovery
- No usas Eureka/Consul
- API REST simple entre servicios

#### 5. Necesitas Streaming/Responses Grandes
```java
// Puedes procesar response stream by stream
restClient.get()
        .uri("/large-file")
        .exchange((request, response) -> {
            // Procesar stream sin cargar todo en memoria
            InputStream inputStream = response.getBody();
            // ...
        });
```

---

### ✅ Usa FeignClient cuando:

#### 1. Microservicios con Spring Cloud
```java
// Service discovery automático con Eureka
@FeignClient(name = "user-service") // ← nombre del servicio, no URL
public interface UserClient {
    @GetMapping("/users/{id}")
    User getUser(@PathVariable Long id);
}

// Feign + Eureka encuentra automáticamente la instancia correcta
```

#### 2. APIs REST Bien Definidas
```java
// API con OpenAPI/Swagger bien documentada
@FeignClient(name = "payment-api", url = "https://api.stripe.com")
public interface PaymentClient {
    @PostMapping("/v1/charges")
    Charge createCharge(@RequestBody CreateChargeRequest request);

    @GetMapping("/v1/charges/{id}")
    Charge getCharge(@PathVariable String id);

    @PostMapping("/v1/refunds")
    Refund createRefund(@RequestBody CreateRefundRequest request);
}
```

#### 3. Múltiples Endpoints del Mismo Servicio
```java
// Cuando tienes MUCHOS endpoints de la misma API
@FeignClient(name = "github-api", url = "https://api.github.com")
public interface GitHubClient {
    @GetMapping("/users/{username}")
    User getUser(@PathVariable String username);

    @GetMapping("/users/{username}/repos")
    List<Repository> getRepositories(@PathVariable String username);

    @GetMapping("/repos/{owner}/{repo}")
    Repository getRepository(@PathVariable String owner,
                           @PathVariable String repo);

    @GetMapping("/repos/{owner}/{repo}/issues")
    List<Issue> getIssues(@PathVariable String owner,
                         @PathVariable String repo);

    // ... 20 endpoints más
}
```

#### 4. Necesitas Circuit Breaker/Retry Fácil
```java
// Integración simple con Resilience4j
@FeignClient(
    name = "user-api",
    url = "https://api.example.com",
    fallback = UserClientFallback.class
)
public interface UserClient {
    @GetMapping("/users/{id}")
    User getUser(@PathVariable Long id);
}

// Fallback automático
@Component
public class UserClientFallback implements UserClient {
    @Override
    public User getUser(Long id) {
        return User.builder()
                .id(id)
                .name("Default User")
                .build();
    }
}
```

#### 5. Team Nuevo en Spring
- FeignClient es más declarativo (fácil de entender)
- Menos código = menos errores
- Interface clara = documentación implícita

---

## Tabla de Decisión

| Criterio | HTTP Interface | RestClient | FeignClient | Ganador |
|----------|----------------|------------|-------------|---------|
| **Estado (2025)** | ✅ Activo, recomendado | ✅ Activo | ⚠️ Maintenance mode | HTTP Interface ⭐ |
| **Cantidad de código** | Menos (interface) | Más (imperativo) | Menos (interface) | HTTP Interface ⭐ |
| **Control sobre HTTP** | Medio | Total | Limitado | RestClient 🎯 |
| **Debugging** | Medio | Fácil | Más difícil | RestClient 🎯 |
| **APIs complejas** | Bueno | Excelente | Limitado | RestClient 🎯 |
| **APIs estándar** | Excelente | Bueno | Excelente | HTTP Interface ⭐ |
| **Spring Cloud** | ❌ No integrado | ❌ No integrado | ✅ Integrado | FeignClient ✨ |
| **Service Discovery** | Manual | Manual | Automático | FeignClient ✨ |
| **Load Balancing** | Manual | Manual | Automático | FeignClient ✨ |
| **Circuit Breaker** | Manual | Manual | Integrado | FeignClient ✨ |
| **Dependencias** | ✅ Ninguna (core) | ✅ Ninguna | ❌ +2-3 MB | HTTP Interface ⭐ |
| **Performance** | Óptimo | Óptimo | Overhead | HTTP Interface ⭐ |
| **Observability** | Nativo (via RestClient) | Nativo | Requiere config | HTTP Interface ⭐ |
| **Learning Curve** | Baja | Baja | Media | HTTP Interface ⭐ |
| **Boilerplate** | Bajo (interface) | Alto | Bajo (interface) | HTTP Interface ⭐ |
| **Configuración** | Manual | Simple | Auto-config | RestClient 🎯 |
| **Recomendación Spring** | ✅ Sí (oficial) | ✅ Sí | ⚠️ Solo Spring Cloud | HTTP Interface ⭐ |

### Resumen de Puntuación:

- **HTTP Interface**: ⭐⭐⭐⭐⭐ (Mejor para Spring Boot 3+ general)
- **RestClient**: 🎯🎯🎯🎯 (Mejor para control total)
- **FeignClient**: ✨✨✨ (Solo para Spring Cloud microservices)

---

## Ventajas y Desventajas

### HTTP Interface (⭐ RECOMENDADO para Spring Boot 3+)

| ✅ Pros | ❌ Contras |
|---------|-----------|
| **Nativo de Spring** - sin deps | Configuración manual (no auto-config) |
| **Recomendado oficialmente** | Sin integración Spring Cloud |
| Código muy limpio (interface) | Relativamente nuevo (menos ejemplos) |
| Performance óptimo | Load balancing manual |
| Flexible (RestClient o WebClient) | Service discovery manual |
| Observability nativa |  |
| Fácil testing (mock interface) |  |

**Resumen**: **Mejor opción general** para Spring Boot 3+ sin Spring Cloud.

---

### RestClient

| ✅ Pros | ❌ Contras |
|---------|-----------|
| Control total sobre HTTP | Más código boilerplate |
| Sin dependencias extra | Más trabajo para casos simples |
| Debugging fácil | Más mantenimiento |
| Flexible para casos complejos | No integra con Spring Cloud |
| Performance óptimo | Load balancing manual |
| Observability nativa | Circuit breaker manual |

**Resumen**: Mejor cuando necesitas **control total y flexibilidad**.

---

### FeignClient (⚠️ Maintenance Mode)

| ✅ Pros | ❌ Contras |
|---------|-----------|
| Código muy limpio | **Maintenance mode** |
| Declarativo (solo interfaces) | Dependencia adicional (+2-3 MB) |
| **Integración Spring Cloud** | "Magic" (implementación oculta) |
| Service discovery automático | Debugging más difícil |
| Load balancing integrado | Menos control HTTP |
| Circuit breaker fácil | Overhead mínimo |

**Resumen**: **Solo para microservicios con Spring Cloud** (Eureka, etc.).

---

## Mejores Prácticas

### Para RestClient

```java
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient apiClient(ObservationRegistry registry) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));

        return RestClient.builder()
                .baseUrl("https://api.example.com")
                .requestFactory(factory)
                .defaultHeader("Accept", "application/json")
                .observationRegistry(registry) // ← Observability
                .requestInterceptor((request, body, execution) -> {
                    log.debug("Request: {} {}", request.getMethod(), request.getURI());
                    var response = execution.execute(request, body);
                    log.debug("Response: {}", response.getStatusCode());
                    return response;
                })
                .build();
    }
}
```

### Para FeignClient

```java
// 1. Separar configuración
@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC; // Producción: BASIC, Dev: FULL
    }

    @Bean
    public Request.Options options() {
        return new Request.Options(
            Duration.ofSeconds(5),
            Duration.ofSeconds(10)
        );
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }
}

// 2. Usar fallbacks para resiliencia
@FeignClient(
    name = "user-api",
    fallback = UserClientFallback.class
)
public interface UserClient {
    @GetMapping("/users/{id}")
    User getUser(@PathVariable Long id);
}

// 3. Implementar fallback
@Component
public class UserClientFallback implements UserClient {
    @Override
    public User getUser(Long id) {
        // Retornar valor por defecto o lanzar excepción custom
        throw new ServiceUnavailableException("User service is down");
    }
}
```

---

## ¿Qué Implementa Este Proyecto?

Este proyecto implementa **AMBAS** opciones (FeignClient y RestClient) con propósitos educativos:

### 📦 Implementaciones Disponibles

1. **JsonPlaceholderFeignClient** (@Primary)
   - Cliente declarativo usando OpenFeign
   - **Default**: Se inyecta automáticamente sin @Qualifier
   - Menos código (~80% menos líneas)
   - Ideal para: Microservicios, APIs con muchos endpoints

2. **JsonPlaceholderRestClient**
   - Cliente imperativo usando Spring RestClient
   - Requiere `@Qualifier("restClient")` para seleccionarlo
   - Control total sobre HTTP
   - Ideal para: APIs simples, casos donde se necesita debugging detallado

### 🔄 Cómo Elegir Entre Ambas

Puedes cambiar entre implementaciones usando `@Qualifier`:

```java
// Opción 1: Usar FeignClient (default, sin @Qualifier)
@Service
public class CreateUserService {
    public CreateUserService(ExternalUserApiClient client) {
        this.client = client;  // ✅ FeignClient
    }
}

// Opción 2: Usar RestClient (con @Qualifier explícito)
@Service
public class CreateUserService {
    public CreateUserService(
        @Qualifier("restClient") ExternalUserApiClient client
    ) {
        this.client = client;  // ✅ RestClient
    }
}
```

### 🎯 ¿Por Qué FeignClient es @Primary?

FeignClient es la opción **por defecto** porque:

1. ✅ **Más usado en la industria** - Estándar en microservicios
2. ✅ **Menos código** - ~80% menos líneas que RestClient
3. ✅ **Más fácil de mantener** - Solo interface, sin implementación
4. ✅ **Peso insignificante** - +2-3 MB no es relevante en la mayoría de casos

**RestClient** está disponible para casos donde:
- ❌ No quieres dependencias adicionales
- ✅ Necesitas control total sobre HTTP
- ✅ Debugging detallado es crítico
- ✅ API muy simple (1-2 endpoints)

### 📚 Aprende Más

- Ver: `docs/19-Beans-and-Qualifiers-Guide.md` - Cómo cambiar entre implementaciones
- Ver: Código en `CreateUserService.java:69-107` - Documentación del patrón @Primary/@Qualifier

---

## Otras Opciones: RestTemplate y WebClient

Aunque RestClient y FeignClient son las opciones principales, existen otras dos:

### RestTemplate (Legacy)

**Estado**: ⚠️ **Maintenance Mode** (deprecado desde Spring 5.0)

**¿Por qué existe?**
- Cliente HTTP original de Spring (desde 2009)
- Antes de RestClient, era la única opción síncrona

**¿Deberías usarlo?**
- ❌ NO para proyectos nuevos
- ⚠️ Solo si mantienes código legacy
- ✅ Planea migración a RestClient

**Ejemplo**:
```java
// API verbosa y antigua
RestTemplate restTemplate = new RestTemplate();
String url = "https://api.example.com/users/" + userId;
User user = restTemplate.getForObject(url, User.class);
```

**Migración a RestClient**:
```java
// Antes (RestTemplate)
User user = restTemplate.getForObject(url, User.class);

// Después (RestClient)
User user = restClient.get()
        .uri("/users/{id}", userId)
        .retrieve()
        .body(User.class);
```

---

### WebClient (Reactivo)

**Estado**: ✅ **Activo** (introducido en Spring 5.0)

**¿Cuándo usarlo?**
- ✅ **SOLO** si tu app es reactiva (Spring WebFlux)
- ✅ Alta concurrencia (miles de requests/segundo)
- ✅ Streaming de datos

**¿Por qué NO usarlo normalmente?**
- ❌ **Overkill** para apps tradicionales (Spring MVC)
- ❌ Curva de aprendizaje alta (Mono/Flux)
- ❌ Más complejo de debuggear

**Ejemplo**:
```java
// API reactiva - retorna Mono (1 elemento) o Flux (N elementos)
Mono<User> userMono = webClient.get()
        .uri("/users/{id}", userId)
        .retrieve()
        .bodyToMono(User.class);

// Convertir a síncrono (bloquea thread - NO recomendado)
User user = userMono.block();

// Uso reactivo (recomendado solo en apps WebFlux)
userMono.subscribe(
    user -> log.info("User: {}", user),
    error -> log.error("Error", error)
);
```

**Cuándo considerar WebClient**:
```java
// Caso 1: App completamente reactiva
@RestController
public class UserController {

    private final WebClient webClient;

    @GetMapping("/users/{id}")
    public Mono<User> getUser(@PathVariable Long id) {
        return webClient.get()
                .uri("/users/{id}", id)
                .retrieve()
                .bodyToMono(User.class); // ← Retorna Mono directamente
    }
}

// Caso 2: Llamadas paralelas (mejor que threads)
Mono<User> userMono = webClient.get().uri("/users/{id}", id).retrieve().bodyToMono(User.class);
Mono<Orders> ordersMono = webClient.get().uri("/orders/{id}", id).retrieve().bodyToMono(Orders.class);

Mono.zip(userMono, ordersMono)
    .map(tuple -> new UserWithOrders(tuple.getT1(), tuple.getT2()))
    .subscribe(result -> log.info("Result: {}", result));
```

---

## Conclusión y Recomendaciones Finales (2025)

### Decisión Rápida

```
¿Año 2025 con Spring Boot 3+?
├─ SÍ → HTTP Interface ⭐ (recomendado por Spring)
└─ NO → Actualiza primero

¿Usas Spring Cloud (Eureka, Consul)?
├─ SÍ → FeignClient ✨ (único caso donde FeignClient gana)
└─ NO → HTTP Interface ⭐

¿Necesitas control TOTAL sobre HTTP?
├─ SÍ → RestClient 🎯
└─ NO → HTTP Interface ⭐

¿Tienes >10 endpoints de la misma API?
├─ SÍ → HTTP Interface ⭐
└─ NO → HTTP Interface ⭐ o RestClient 🎯

¿Tu app es completamente reactiva (WebFlux)?
├─ SÍ → WebClient o HTTP Interface con WebClient backend
└─ NO → HTTP Interface ⭐
```

### Recomendación por Tipo de Proyecto (2025)

| Tipo de Proyecto | Recomendación | Razón |
|------------------|---------------|-------|
| **Spring Boot 3+ nuevo** | **HTTP Interface ⭐** | Nativo, recomendado oficialmente |
| **Monolito tradicional** | HTTP Interface ⭐ | Simple, sin deps extra |
| **Microservicios (Spring Cloud)** | FeignClient ✨ | Service discovery, load balancing |
| **Microservicios (sin Spring Cloud)** | HTTP Interface ⭐ | Declarativo, sin overhead |
| **API Gateway** | RestClient 🎯 | Control total sobre routing |
| **Backend for Frontend (BFF)** | HTTP Interface ⭐ | Múltiples servicios, menos código |
| **Aplicación reactiva** | HTTP Interface + WebClient | Flexible, mismo código |
| **Control total necesario** | RestClient 🎯 | Debugging, casos complejos |
| **Proyecto legacy** | RestTemplate → RestClient | Migra gradualmente |

### Recomendación Actualizada 2025

**Para la mayoría de proyectos Spring Boot 3+**: **HTTP Interface** ⭐
- ✅ Nativo de Spring (sin deps)
- ✅ Recomendado oficialmente
- ✅ Declarativo (menos código)
- ✅ Performance óptimo
- ✅ Flexible (RestClient o WebClient)

**Para control total**: **RestClient** 🎯
- ✅ Más control
- ✅ Debugging más fácil
- ✅ APIs complejas

**Solo para Spring Cloud**: **FeignClient** ✨
- ✅ Service discovery automático
- ✅ Load balancing integrado
- ⚠️ Maintenance mode (pero funcional)

---

## 📊 Tabla Comparativa Completa: HTTP Interface vs RestClient vs FeignClient

### Comparación Lado a Lado (2025)

| Característica | HTTP Interface ⭐ | RestClient 🎯 | FeignClient ✨ |
|----------------|-------------------|---------------|----------------|
| **📅 Estado (2025)** | ✅ Activo, recomendado | ✅ Activo | ⚠️ Maintenance mode |
| **🎯 Recomendación Spring** | ✅ Sí (oficial para SB3+) | ✅ Sí | ⚠️ Solo Spring Cloud |
| **📦 Dependencias** | Ninguna (core Spring) | Ninguna | spring-cloud-openfeign |
| **💾 Tamaño deps** | 0 KB | 0 KB | +2-3 MB |
| **🎨 Estilo** | Declarativo (interface) | Imperativo (código) | Declarativo (interface) |
| **📝 Cantidad código** | ⭐ Mínimo | ❌ Más | ⭐ Mínimo |
| **🔧 Configuración** | Manual (HttpServiceProxyFactory) | Simple (bean) | Auto (@EnableFeignClients) |
| **⚙️ Backend HTTP** | RestClient o WebClient | N/A (es el cliente) | Feign (propio) |
| **🚀 Performance** | ⭐ Óptimo | ⭐ Óptimo | ⚠️ Overhead |
| **🎮 Control HTTP** | 🟡 Medio | ⭐ Total | ❌ Limitado |
| **🐛 Debugging** | 🟡 Medio | ⭐ Fácil | ❌ Difícil (magia) |
| **📊 Observability** | ⭐ Nativa (via RestClient) | ⭐ Nativa | ⚠️ Requiere config |
| **☁️ Spring Cloud** | ❌ No | ❌ No | ✅ Sí |
| **🔍 Service Discovery** | ❌ Manual | ❌ Manual | ✅ Automático |
| **⚖️ Load Balancing** | ❌ Manual | ❌ Manual | ✅ Automático |
| **🔄 Circuit Breaker** | ❌ Manual | ❌ Manual | ✅ Integrado |
| **📖 Curva aprendizaje** | ⭐ Baja | ⭐ Baja | 🟡 Media |
| **🧪 Testing** | ⭐ Fácil (mock interface) | 🟡 Mock RestClient | ⭐ Fácil (mock interface) |
| **🔄 Reactivo** | ✅ Sí (con WebClient) | ❌ No (solo síncrono) | ⚠️ Limitado |
| **📚 Documentación** | 🟡 Buena (nueva) | ⭐ Excelente | ⭐ Excelente |
| **👥 Comunidad** | 🟡 Creciendo | ⭐ Grande | ⭐ Grande |
| **🏆 Casos de uso** | APIs REST estándar | Control total, debugging | Spring Cloud microservices |

### Matriz de Decisión: ¿Cuándo usar cada uno?

| Escenario | HTTP Interface | RestClient | FeignClient |
|-----------|----------------|------------|-------------|
| **Proyecto nuevo Spring Boot 3+** | ✅ Primera opción | 🟡 Si necesitas control | ❌ No recomendado |
| **Microservicios Spring Cloud** | ❌ No integrado | ❌ No integrado | ✅ Primera opción |
| **Microservicios sin Spring Cloud** | ✅ Primera opción | 🟡 Alternativa | ❌ Overkill |
| **API REST simple** | ✅ Perfecto | ✅ También funciona | ❌ Overkill |
| **API REST compleja** | 🟡 Bueno | ✅ Excelente | ❌ Limitado |
| **Múltiples endpoints** | ✅ Excelente | ❌ Tedioso | ✅ Excelente |
| **Debugging intensivo** | 🟡 Medio | ✅ Excelente | ❌ Difícil |
| **Performance crítica** | ✅ Óptimo | ✅ Óptimo | 🟡 Overhead |
| **Sin deps adicionales** | ✅ Sí | ✅ Sí | ❌ No |
| **Aplicación reactiva** | ✅ Con WebClient | ❌ No | 🟡 Limitado |
| **Service discovery** | ❌ No | ❌ No | ✅ Sí |
| **Headers dinámicos** | 🟡 Con parámetros | ✅ Excelente | 🟡 Con parámetros |

### Ejemplo de Código Comparativo

```java
// ========================================
// HTTP Interface (⭐ RECOMENDADO para Spring Boot 3+)
// ========================================

// 1. Interface
@HttpExchange
public interface UserApi {
    @GetExchange("/users/{id}")
    User getUser(@PathVariable Long id);
}

// 2. Configuración
@Bean
public UserApi userApi(RestClient restClient) {
    RestClientAdapter adapter = RestClientAdapter.create(restClient);
    HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(adapter).build();
    return factory.createClient(UserApi.class);
}

// 3. Uso
User user = userApi.getUser(1L);

// ========================================
// RestClient (🎯 Para control total)
// ========================================

// 1. Bean
@Bean
public RestClient restClient() {
    return RestClient.builder()
            .baseUrl("https://api.example.com")
            .build();
}

// 2. Uso directo
User user = restClient.get()
        .uri("/users/{id}", 1L)
        .retrieve()
        .body(User.class);

// ========================================
// FeignClient (✨ Solo para Spring Cloud)
// ========================================

// 1. Habilitar Feign
@EnableFeignClients
@SpringBootApplication
public class App { }

// 2. Interface
@FeignClient(name = "user-api", url = "https://api.example.com")
public interface UserClient {
    @GetMapping("/users/{id}")
    User getUser(@PathVariable Long id);
}

// 3. Uso
User user = userClient.getUser(1L);
```

### Resumen: ¿Cuál elegir?

#### ⭐ HTTP Interface - PRIMERA OPCIÓN (2025)
```
✅ Úsalo si:
- Spring Boot 3+ sin Spring Cloud
- Quieres la opción moderna recomendada
- Prefieres código declarativo
- No necesitas service discovery

❌ No uses si:
- Necesitas Spring Cloud (usa FeignClient)
- Necesitas control total HTTP (usa RestClient)
```

#### 🎯 RestClient - CONTROL TOTAL
```
✅ Úsalo si:
- Necesitas control total sobre HTTP
- Debugging intensivo
- APIs complejas/no estándar
- Preferencia por código imperativo

❌ No uses si:
- Prefieres código declarativo (usa HTTP Interface)
- Tienes muchos endpoints (usa HTTP Interface)
```

#### ✨ FeignClient - SOLO SPRING CLOUD
```
✅ Úsalo si:
- Microservicios con Spring Cloud
- Necesitas service discovery (Eureka, Consul)
- Load balancing client-side
- Ya lo tienes en producción

❌ No uses si:
- Proyecto nuevo sin Spring Cloud (usa HTTP Interface)
- Necesitas control total (usa RestClient)
```

---

## Referencias

- [Spring HTTP Interface Documentation](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface)
- [Spring RestClient Documentation](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-restclient)
- [OpenFeign Documentation](https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/)
- [Spring RestTemplate Documentation](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-resttemplate)
- [Spring WebClient Documentation](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)
- [Migración de RestTemplate a RestClient](https://spring.io/blog/2023/07/13/new-in-spring-6-1-restclient)
