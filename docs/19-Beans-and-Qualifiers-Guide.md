# 🏷️ Spring Beans: @Primary y @Qualifier

**Guía completa sobre cómo trabajar con múltiples implementaciones de una interface**

---

## 📋 Tabla de Contenidos

1. [Introducción](#introducción)
2. [El Problema](#el-problema)
3. [Solución 1: @Primary](#solución-1-primary)
4. [Solución 2: @Qualifier](#solución-2-qualifier)
5. [Combinando @Primary + @Qualifier](#combinando-primary--qualifier)
6. [Ejemplo Real: FeignClient vs RestClient](#ejemplo-real-feignclient-vs-restclient)
7. [Cómo Cambiar de Implementación](#cómo-cambiar-de-implementación)
8. [Mejores Prácticas](#mejores-prácticas)

---

## Introducción

En Spring, cuando tienes **múltiples implementaciones** de una interface, Spring necesita saber **cuál inyectar** cuando alguien solicita la interface.

```java
// Interface (Port)
public interface ExternalUserApiClient {
    Optional<ExternalUserData> getUserById(Integer userId);
}

// Implementación 1: FeignClient
@FeignClient(...)
@Primary
@Qualifier("feignClient")
public interface JsonPlaceholderFeignClient extends ExternalUserApiClient { }

// Implementación 2: RestClient
@Component
@Qualifier("restClient")
public class JsonPlaceholderRestClient implements ExternalUserApiClient { }
```

**❓ Pregunta:** ¿Cuál se inyecta cuando alguien pide `ExternalUserApiClient`?

**✅ Respuesta:** La que tenga `@Primary` (en este caso, `FeignClient`).

---

## El Problema

Cuando Spring encuentra **múltiples beans** del mismo tipo, lanza un error:

```
NoUniqueBeanDefinitionException: No qualifying bean of type
'ExternalUserApiClient' available: expected single matching bean
but found 2: feignClient, restClient
```

### Ejemplo del problema:

```java
@Service
public class CreateUserService {

    // ❌ ERROR: Spring no sabe cuál inyectar
    private final ExternalUserApiClient externalUserApiClient;

    public CreateUserService(ExternalUserApiClient externalUserApiClient) {
        this.externalUserApiClient = externalUserApiClient;
    }
}
```

---

## Solución 1: @Primary

`@Primary` indica a Spring **cuál bean usar por defecto** cuando no se especifica nada.

### Definición:

```java
// Esta es la implementación por defecto
@FeignClient(...)
@Primary  // 👈 Bean por defecto
@Qualifier("feignClient")
public interface JsonPlaceholderFeignClient extends ExternalUserApiClient { }

// Esta NO es la por defecto
@Component
@Qualifier("restClient")
public class JsonPlaceholderRestClient implements ExternalUserApiClient { }
```

### Uso:

```java
@Service
public class CreateUserService {

    // ✅ OK: Spring inyecta JsonPlaceholderFeignClient (es @Primary)
    private final ExternalUserApiClient externalUserApiClient;

    public CreateUserService(ExternalUserApiClient externalUserApiClient) {
        this.externalUserApiClient = externalUserApiClient;
    }
}
```

### ¿Cuándo usar @Primary?

- ✅ Tienes una implementación "preferida" o "por defecto"
- ✅ La mayoría de los casos usan la misma implementación
- ✅ Quieres minimizar configuración en los servicios

---

## Solución 2: @Qualifier

`@Qualifier` permite **seleccionar explícitamente** qué bean quieres inyectar.

### Definición:

```java
@FeignClient(...)
@Qualifier("feignClient")  // 👈 Nombre del bean
public interface JsonPlaceholderFeignClient extends ExternalUserApiClient { }

@Component
@Qualifier("restClient")  // 👈 Nombre del bean
public class JsonPlaceholderRestClient implements ExternalUserApiClient { }
```

### Uso:

```java
@Service
public class CreateUserService {

    private final ExternalUserApiClient externalUserApiClient;

    // ✅ Selección explícita con @Qualifier
    public CreateUserService(
            @Qualifier("feignClient") ExternalUserApiClient externalUserApiClient
    ) {
        this.externalUserApiClient = externalUserApiClient;
    }
}
```

### ¿Cuándo usar @Qualifier?

- ✅ Quieres elegir una implementación específica
- ✅ Tienes diferentes servicios que usan diferentes implementaciones
- ✅ Necesitas control fino sobre qué bean se inyecta

---

## Combinando @Primary + @Qualifier

La estrategia más poderosa: **@Primary como default, @Qualifier para excepciones**.

```java
// Implementación por defecto
@FeignClient(...)
@Primary
@Qualifier("feignClient")
public interface JsonPlaceholderFeignClient extends ExternalUserApiClient { }

// Implementación alternativa
@Component
@Qualifier("restClient")
public class JsonPlaceholderRestClient implements ExternalUserApiClient { }
```

### Ventajas:

1. **Simplicidad por defecto**: La mayoría del código no necesita @Qualifier
2. **Flexibilidad cuando se necesita**: Puedes usar @Qualifier en casos especiales
3. **Documentación clara**: El @Primary indica cuál es la opción recomendada

### Ejemplo de uso mixto:

```java
// Servicio 1: Usa implementación por defecto (FeignClient)
@Service
public class CreateUserService {

    // ✅ FeignClient (sin @Qualifier, usa @Primary)
    public CreateUserService(ExternalUserApiClient client) {
        this.client = client;
    }
}

// Servicio 2: Usa implementación alternativa (RestClient)
@Service
public class LegacyUserService {

    // ✅ RestClient (con @Qualifier explícito)
    public LegacyUserService(
            @Qualifier("restClient") ExternalUserApiClient client
    ) {
        this.client = client;
    }
}
```

---

## Ejemplo Real: FeignClient vs RestClient

Este proyecto implementa **AMBAS** formas de consumir APIs REST:

### 1. JsonPlaceholderFeignClient (DECLARATIVO)

```java
@FeignClient(
        name = "jsonPlaceholderApi",
        url = "${external-api.jsonplaceholder.base-url}",
        configuration = FeignClientConfig.class
)
@Primary  // 👈 Por defecto
@Qualifier("feignClient")
public interface JsonPlaceholderFeignClient extends ExternalUserApiClient {

    @GetMapping("/users/{id}")
    JsonPlaceholderUserResponse getUserByIdInternal(@PathVariable Integer userId);

    // ... default methods que implementan el port
}
```

**Características:**
- ✅ Menos código (~80% menos líneas)
- ✅ Más usado en la industria
- ✅ Fácil de mantener
- ❌ Dependencia adicional (~2-3 MB)
- ❌ Menos control sobre HTTP

### 2. JsonPlaceholderRestClient (IMPERATIVO)

```java
@Component("jsonPlaceholderRestClientAdapter")
@Qualifier("restClient")
public class JsonPlaceholderRestClient implements ExternalUserApiClient {

    private final RestClient restClient;

    public JsonPlaceholderRestClient(
            @Qualifier("jsonPlaceholderRestClient") RestClient restClient
    ) {
        this.restClient = restClient;
    }

    @Override
    public Optional<ExternalUserData> getUserById(Integer userId) {
        // Código explícito con RestClient...
    }
}
```

**Características:**
- ✅ Control total sobre HTTP
- ✅ Sin dependencias adicionales
- ✅ Debugging más fácil
- ❌ Más código boilerplate
- ❌ Más trabajo para APIs con muchos endpoints

---

## Cómo Cambiar de Implementación

### Opción 1: Sin modificar código (recomendado para testing)

**application.yml** o **application-test.yml**:

```yaml
# No implementado en este proyecto, pero es posible con SpEL
spring:
  main:
    allow-bean-definition-overriding: true

# Y crear un @Configuration condicional que marque el otro como @Primary
```

### Opción 2: Con @Qualifier en el servicio

**Modificar CreateUserService.java:**

```java
@Service
public class CreateUserService implements CreateUserUseCase {

    // Cambiar de FeignClient a RestClient
    public CreateUserService(
            UserRepository userRepository,
            UserEventPublisher userEventPublisher,
            @Qualifier("restClient") ExternalUserApiClient externalUserApiClient,  // 👈 Cambio aquí
            MeterRegistry meterRegistry,
            @Value("${ENVIRONMENT:local}") String environment
    ) {
        this.userRepository = userRepository;
        this.userEventPublisher = userEventPublisher;
        this.externalUserApiClient = externalUserApiClient;
        this.meterRegistry = meterRegistry;
        this.environment = environment;
    }
}
```

### Opción 3: Cambiar el @Primary

**Modificar las clases de implementación:**

```java
// Quitar @Primary de FeignClient
@FeignClient(...)
@Qualifier("feignClient")
public interface JsonPlaceholderFeignClient extends ExternalUserApiClient { }

// Agregar @Primary a RestClient
@Component("jsonPlaceholderRestClientAdapter")
@Primary  // 👈 Ahora es el default
@Qualifier("restClient")
public class JsonPlaceholderRestClient implements ExternalUserApiClient { }
```

---

## Mejores Prácticas

### ✅ **DO**: Usa @Primary para la implementación recomendada

```java
@FeignClient(...)
@Primary  // 👈 Indica cuál es la opción recomendada
@Qualifier("feignClient")
public interface JsonPlaceholderFeignClient extends ExternalUserApiClient { }
```

**Por qué:**
- Documenta claramente cuál es la opción preferida
- Reduce configuración en servicios
- Facilita onboarding de nuevos desarrolladores

---

### ✅ **DO**: Siempre añade @Qualifier incluso con @Primary

```java
@FeignClient(...)
@Primary
@Qualifier("feignClient")  // 👈 SIEMPRE agregar @Qualifier
public interface JsonPlaceholderFeignClient extends ExternalUserApiClient { }
```

**Por qué:**
- Permite selección explícita cuando se necesite
- Documentación clara del nombre del bean
- Facilita debugging

---

### ✅ **DO**: Usa nombres descriptivos en @Qualifier

```java
// ✅ BIEN: Nombres claros y descriptivos
@Qualifier("feignClient")
@Qualifier("restClient")

// ❌ MAL: Nombres ambiguos
@Qualifier("client1")
@Qualifier("client2")
```

---

### ✅ **DO**: Documenta por qué una implementación es @Primary

```java
/**
 * <h3>🎯 ¿Por qué FeignClient es @Primary?</h3>
 * <p>
 * En la industria, FeignClient es más común porque:
 * <ul>
 *   <li>Menos código = menos errores</li>
 *   <li>Más fácil para equipos grandes</li>
 *   <li>La desventaja de +2-3 MB es insignificante</li>
 * </ul>
 * </p>
 */
@FeignClient(...)
@Primary
@Qualifier("feignClient")
public interface JsonPlaceholderFeignClient extends ExternalUserApiClient { }
```

---

### ❌ **DON'T**: No uses @Primary en múltiples beans del mismo tipo

```java
// ❌ ERROR: Dos beans con @Primary
@Primary
@Qualifier("feignClient")
public interface JsonPlaceholderFeignClient extends ExternalUserApiClient { }

@Primary  // 👈 Conflicto
@Qualifier("restClient")
public class JsonPlaceholderRestClient implements ExternalUserApiClient { }
```

**Error:**
```
NoUniqueBeanDefinitionException: more than one 'primary' bean found
```

---

### ❌ **DON'T**: No dependas de nombres de bean generados

```java
// ❌ MAL: Depende del nombre generado automáticamente
@Component  // Spring genera "jsonPlaceholderRestClient"
public class JsonPlaceholderRestClient implements ExternalUserApiClient { }

// ✅ BIEN: Nombre explícito
@Component("jsonPlaceholderRestClientAdapter")
@Qualifier("restClient")
public class JsonPlaceholderRestClient implements ExternalUserApiClient { }
```

---

### ❌ **DON'T**: No uses @Qualifier sin @Primary si hay un default claro

```java
// ❌ EVITAR: Fuerza a todos los servicios a usar @Qualifier
@Qualifier("feignClient")
public interface JsonPlaceholderFeignClient extends ExternalUserApiClient { }

@Qualifier("restClient")
public class JsonPlaceholderRestClient implements ExternalUserApiClient { }

// ✅ MEJOR: Marca el default con @Primary
@Primary
@Qualifier("feignClient")
public interface JsonPlaceholderFeignClient extends ExternalUserApiClient { }
```

---

## Patrón General para Múltiples Implementaciones

### Template a seguir:

```java
// PASO 1: Definir el puerto (interface)
public interface MyPort {
    void doSomething();
}

// PASO 2: Implementación por defecto
@Component
@Primary  // 👈 Marca como default
@Qualifier("implementationA")  // 👈 Nombre explícito
public class ImplementationA implements MyPort {
    @Override
    public void doSomething() {
        // Implementación A
    }
}

// PASO 3: Implementación alternativa
@Component
@Qualifier("implementationB")  // 👈 Nombre explícito
public class ImplementationB implements MyPort {
    @Override
    public void doSomething() {
        // Implementación B
    }
}

// PASO 4: Uso en servicios
@Service
public class MyService {

    private final MyPort myPort;

    // Opción A: Usa implementación por defecto (ImplementationA)
    public MyService(MyPort myPort) {
        this.myPort = myPort;
    }

    // Opción B: Selecciona implementación específica (ImplementationB)
    public MyService(@Qualifier("implementationB") MyPort myPort) {
        this.myPort = myPort;
    }
}
```

---

## Resumen

| Anotación    | Propósito                                    | Cuándo usar                                  |
| ------------ | -------------------------------------------- | -------------------------------------------- |
| `@Primary`   | Marca bean como default                      | Hay una implementación preferida             |
| `@Qualifier` | Selecciona bean específico                   | Necesitas elegir una implementación concreta |
| Ambas        | Default + selección explícita cuando se necesita | **Mejor práctica** (recomendado)             |

### Checklist para implementar múltiples beans:

- [ ] Todas las implementaciones tienen `@Qualifier` con nombres descriptivos
- [ ] Una implementación tiene `@Primary` (la recomendada)
- [ ] Documentado por qué esa implementación es `@Primary`
- [ ] Los servicios usan la default sin `@Qualifier` (salvo excepciones)
- [ ] Casos especiales usan `@Qualifier` explícito
- [ ] Tests verifican ambas implementaciones funcionan

---

## Referencias

- 📖 [Spring Framework Reference - @Qualifier](https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/autowired-qualifiers.html)
- 📖 [Spring Framework Reference - @Primary](https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/autowired-primary.html)
- 📖 Ejemplo en el proyecto: `CreateUserService.java:69-107`
- 📖 Ver también: `docs/18-HTTP-Clients-Comparison-Guide.md`

---

**✍️ Autor:** Spring Boot Hexagonal Architecture Demo
**📅 Última actualización:** 2025-01-04
**🏷️ Tags:** `spring`, `dependency-injection`, `beans`, `qualifier`, `primary`, `best-practices`
