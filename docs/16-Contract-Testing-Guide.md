# Guía de Contract Testing

**Nota**: Este proyecto NO implementa Contract Testing (aún). Esta guía explica **qué es, cuándo usarlo y cómo implementarlo** en el futuro si tu proyecto evoluciona a microservicios.

## 📚 Índice

1. [¿Qué es Contract Testing?](#qué-es-contract-testing)
2. [¿Por qué necesitas Contract Testing?](#por-qué-necesitas-contract-testing)
3. [Contract Testing vs Otros Tests](#contract-testing-vs-otros-tests)
4. [Cuándo SÍ usar Contract Testing](#cuándo-sí-usar-contract-testing)
5. [Cuándo NO usar Contract Testing](#cuándo-no-usar-contract-testing)
6. [Herramientas Populares](#herramientas-populares)
7. [Ejemplo Conceptual](#ejemplo-conceptual)
8. [Decisión para Este Proyecto](#decisión-para-este-proyecto)

---

## ¿Qué es Contract Testing?

**Contract Testing** es una técnica de testing que valida que dos servicios (Producer y Consumer) se comunican correctamente según un **contrato** acordado.

### Analogía Simple

Imagina dos empresas:
- **Empresa A** (Producer): Fabrica piezas de coche (API)
- **Empresa B** (Consumer): Ensambla coches (Cliente de la API)

**Sin Contract Testing**:
- Empresa A cambia el tamaño de los tornillos
- Empresa B no se entera hasta producción
- Los coches no ensamblan → **¡Desastre!**

**Con Contract Testing**:
- Ambas empresas firman un **contrato**: "tornillos de 10mm"
- Si Empresa A intenta fabricar tornillos de 12mm → **test falla**
- Problema detectado **antes** de producción

### En Software

**Producer (API)**:
```json
// Contrato: Endpoint GET /users/{id} devuelve:
{
  "id": "uuid",
  "username": "string",
  "email": "string"
}
```

**Consumer (Cliente)**:
```java
// Mi código espera exactamente esto:
UserDTO user = apiClient.getUser(id);
String email = user.getEmail();  // ¿Qué pasa si el API cambió "email" a "emailAddress"?
```

**Contract Test**: Valida que Producer y Consumer están de acuerdo sobre el contrato.

---

## ¿Por qué necesitas Contract Testing?

### Problema: Integration Tests Son Caros en Microservicios

**Escenario**: 10 microservicios que se comunican entre sí.

**Integration Tests tradicionales**:
```
Para probar UserService:
  1. Levantar UserService
  2. Levantar OrderService (dependencia)
  3. Levantar PaymentService (dependencia de OrderService)
  4. Levantar NotificationService (dependencia)
  5. Levantar BD de cada servicio
  6. Ejecutar tests
```

**Problemas**:
- ⏱️ **Lento**: 10+ minutos para levantar todo
- 💰 **Caro**: Requiere mucha infraestructura
- 🐛 **Frágil**: Si PaymentService está roto, tus tests de UserService fallan
- 🔴 **No escalable**: 10 servicios = 10! combinaciones posibles

### Solución: Contract Testing

**Con Contract Testing**:
```
Para probar UserService:
  1. Levantar solo UserService
  2. Mockear OrderService usando su **contrato**
  3. Ejecutar tests
```

**Beneficios**:
- ⚡ **Rápido**: 1-2 minutos
- 💚 **Barato**: Solo 1 servicio corriendo
- 🛡️ **Robusto**: Fallo de OrderService no afecta tests de UserService
- 🎯 **Escalable**: Tests independientes por servicio

---

## Contract Testing vs Otros Tests

### Comparación

| Aspecto | Unit Test | Contract Test | Integration Test | E2E Test |
|---------|-----------|---------------|------------------|----------|
| **Qué valida** | Lógica interna | Contrato API | Integración real | Flujo completo |
| **Scope** | 1 clase | 2 servicios (contrato) | 2+ servicios reales | App completa |
| **Velocidad** | ⚡ Muy rápido (~ms) | ⚡ Rápido (~seg) | 🐢 Lento (~min) | 🐌 Muy lento (~min) |
| **Infraestructura** | Ninguna | Ninguna | Docker/BD reales | Full stack |
| **Detecta** | Bugs lógica | Cambios incompatibles | Bugs integración | Bugs end-to-end |
| **Cuándo ejecutar** | Cada commit | Cada commit | Pre-deploy | Pre-release |

### Pirámide de Testing (con Contract Testing)

```
       /\
      /E2E\           ← Pocos (10 tests) | Lentos | Frágiles
     /------\
    /Contract\        ← Algunos (50 tests) | Rápidos | Robustos ← NUEVO
   /----------\
  /Integration\       ← Varios (100 tests) | Lentos | Medios
 /--------------\
/  Unit Tests   \     ← Muchos (1000 tests) | Muy rápidos | Robustos
------------------
```

Contract Testing **reemplaza** muchos Integration Tests en arquitecturas de microservicios.

---

## Cuándo SÍ usar Contract Testing

### ✅ Escenario 1: Microservicios (3+ servicios)

**Señales**:
- Tienes 3 o más microservicios que se comunican
- Cambios en un servicio pueden romper otros
- Integration tests son lentos (>5 min)

**Ejemplo**:
```
user-service → order-service → payment-service → notification-service
```

Sin Contract Testing, necesitas levantar los 4 para test integration tests. Con Contract Testing, cada uno se testea independientemente contra contratos.

### ✅ Escenario 2: API Pública/Externa

**Señales**:
- Tu API es consumida por clientes externos (móvil, partners, etc.)
- No puedes romper el contrato sin aviso
- Necesitas garantizar backward compatibility

**Ejemplo**:
- API REST pública (como Stripe, GitHub API)
- Múltiples versiones de API en producción (`/v1`, `/v2`)

### ✅ Escenario 3: Equipos Independientes

**Señales**:
- Equipo A desarrolla User API
- Equipo B desarrolla Frontend que consume User API
- Equipos trabajan en paralelo

**Sin Contract Testing**:
- Equipo B espera a que Equipo A despliegue para probar
- Feedback lento

**Con Contract Testing**:
- Equipo A publica contrato
- Equipo B desarrolla contra contrato (mock)
- Ambos validan que cumplen contrato independientemente

### ✅ Escenario 4: CI/CD Rápido

**Señales**:
- Quieres CI/CD pipeline que tarde <5 minutos
- Integration tests lentos bloquean el pipeline

**Solución**: Reemplazar Integration Tests con Contract Tests (más rápidos).

---

## Cuándo NO usar Contract Testing

### ❌ Escenario 1: Monolito (como este proyecto)

**Características**:
- 1 aplicación desplegable
- No hay comunicación entre servicios (todo en misma JVM)
- Integration tests con Testcontainers tardan <3 minutos

**Por qué NO**:
- No hay "contrato" entre servicios (no hay servicios separados)
- Integration tests ya son suficientes
- Complejidad innecesaria

**Para este proyecto**: Los integration tests actuales (con Testcontainers) son más apropiados.

### ❌ Escenario 2: Proyecto Pequeño (1-2 servicios)

**Características**:
- Solo 1-2 microservicios
- Comunicación simple
- Integration tests rápidos

**Por qué NO**:
- Overhead no justificado
- Integration tests son suficientes

### ❌ Escenario 3: Lógica de Negocio Compleja

**Qué valida Contract Testing**:
- ✅ Estructura de request/response
- ✅ Tipos de datos
- ✅ Campos requeridos/opcionales

**Qué NO valida**:
- ❌ Lógica de negocio compleja
- ❌ Validaciones de dominio
- ❌ Flujos multi-step

**Ejemplo**:
```java
// Contract Test valida:
POST /users → 201 CREATED { "id": "uuid", "username": "string" }

// Contract Test NO valida:
- ¿Username debe ser único?
- ¿Email debe tener formato válido?
- ¿Se publica evento a Kafka?
```

Para esto necesitas **Integration Tests**.

---

## Herramientas Populares

### 1. Pact (Más Popular)

**Lenguajes**: Java, JavaScript, Python, Ruby, Go, .NET

**Funcionamiento**:
1. **Consumer** define contrato (expectations)
2. Consumer genera archivo de contrato (`.json`)
3. **Producer** ejecuta tests contra contrato
4. Si coincide → ✅ Pasa

**Ejemplo Consumer (Java)**:
```java
@Pact(consumer = "frontend", provider = "user-api")
public RequestResponsePact createUserPact(PactDslWithProvider builder) {
    return builder
        .given("user does not exist")
        .uponReceiving("a request to create user")
            .path("/api/v1/users")
            .method("POST")
            .body(new PactDslJsonBody()
                .stringType("username", "johndoe")
                .stringType("email", "john@example.com"))
        .willRespondWith()
            .status(201)
            .body(new PactDslJsonBody()
                .uuid("id")
                .stringType("username", "johndoe")
                .stringType("email", "john@example.com"))
        .toPact();
}
```

**Ejemplo Producer (Java)**:
```java
@SpringBootTest
@Provider("user-api")
@PactBroker(host = "pactbroker.example.com", port = "80")
public class UserApiContractTest {
    // Pact valida automáticamente que tu API cumple el contrato
}
```

**Ventajas**:
- ✅ Consumer-driven (consumer define contrato)
- ✅ Soporte multi-lenguaje
- ✅ Pact Broker para compartir contratos
- ✅ Gran comunidad

**Desventajas**:
- ❌ Curva de aprendizaje
- ❌ Requiere Pact Broker (infraestructura adicional)

---

### 2. Spring Cloud Contract

**Lenguajes**: Solo Java/Spring Boot

**Funcionamiento**:
1. **Producer** define contrato (Groovy DSL o YAML)
2. Spring Cloud Contract genera tests automáticamente
3. Consumer usa WireMock stubs generados

**Ejemplo Contrato (Groovy)**:
```groovy
Contract.make {
    description "should return user by id"
    request {
        method GET()
        url('/api/v1/users/123')
    }
    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body([
            id: "123",
            username: "johndoe",
            email: "john@example.com"
        ])
    }
}
```

**Ventajas**:
- ✅ Integración nativa con Spring Boot
- ✅ Genera tests automáticamente
- ✅ Producer-driven (más control)

**Desventajas**:
- ❌ Solo Java/Spring
- ❌ Menos flexible que Pact

---

### 3. OpenAPI + Validation

**Funcionamiento**:
- Define contrato en OpenAPI spec
- Valida requests/responses contra spec

**Herramientas**:
- Swagger Validator
- Schemathesis (property-based testing)

**Ventaja**: Ya tienes OpenAPI spec (este proyecto lo usa).

**Desventaja**: No es "true" contract testing (no valida consumer expectations).

---

## Ejemplo Conceptual

### Escenario: User Service + Order Service

**Architecture**:
```
Order Service (Consumer) → GET /users/{id} → User Service (Producer)
```

### Sin Contract Testing

**User Service** (Producer):
```java
// Version 1
@GetMapping("/users/{id}")
public UserResponse getUser(@PathVariable UUID id) {
    return new UserResponse(id, username, email);
}
```

**Order Service** (Consumer):
```java
// Mi código espera:
UserDTO user = userApiClient.getUser(orderId);
String email = user.getEmail();
```

**Problema**: User Service cambia el contrato:
```java
// Version 2 (BREAKING CHANGE)
public UserResponse getUser(@PathVariable UUID id) {
    return new UserResponse(id, username, emailAddress);  // ← email → emailAddress
}
```

**Resultado**:
- ✅ User Service tests pasan (no saben que Order Service se rompió)
- ❌ Order Service se rompe en producción (`user.getEmail()` devuelve null)

### Con Contract Testing

**1. Order Service define contrato** (Pact):
```java
@Pact(consumer = "order-service", provider = "user-service")
public RequestResponsePact getUserPact(PactDslWithProvider builder) {
    return builder
        .given("user exists")
        .uponReceiving("a request to get user")
            .path("/users/123")
            .method("GET")
        .willRespondWith()
            .status(200)
            .body(new PactDslJsonBody()
                .uuid("id")
                .stringType("username")
                .stringType("email"))  // ← Order Service ESPERA "email"
        .toPact();
}
```

**2. User Service ejecuta tests contra contrato**:
```bash
# User Service CI/CD pipeline
mvn test -Dtest=UserApiContractTest

# Si User Service cambió "email" a "emailAddress":
# ❌ Test falla: Contract violation - field 'email' not found
```

**Resultado**:
- 🛡️ Cambio breaking detectado **antes** de merge
- ✅ Developer de User Service ve el error
- 🔄 Coordinator entre equipos:
  - Opción A: Revertir cambio
  - Opción B: Actualizar contrato y coordinar con Order Service

---

## Decisión para Este Proyecto

### ¿Debería implementar Contract Testing ahora?

**NO**.

**Razones**:
1. **Monolito**: No hay microservicios que se comuniquen
2. **Integration Tests suficientes**: Testcontainers ya valida integraciones
3. **Overhead innecesario**: Complejidad sin beneficio

### ¿Cuándo considerar Contract Testing para este proyecto?

**Escenario futuro**: Migras a microservicios

```
hexarch-monolito (actual) → hexarch-microservicios (futuro)

Monolito:
└── hexarch (1 app)

Microservicios:
├── user-service
├── order-service
├── payment-service
└── notification-service
```

**En ese momento**:
1. Define contratos entre servicios (Pact o Spring Cloud Contract)
2. Reemplaza algunos integration tests con contract tests
3. Mantén integration tests solo para flujos críticos end-to-end

---

## Recursos

### **Pact**
- Documentación oficial: https://docs.pact.io/
- Getting started Java: https://docs.pact.io/implementation_guides/jvm
- Pact Broker: https://github.com/pact-foundation/pact_broker

### **Spring Cloud Contract**
- Documentación oficial: https://spring.io/projects/spring-cloud-contract
- Samples: https://github.com/spring-cloud-samples/spring-cloud-contract-samples

### **Artículos**
- Martin Fowler - "ContractTest": https://martinfowler.com/bliki/ContractTest.html
- ThoughtWorks Technology Radar: https://www.thoughtworks.com/radar/techniques/consumer-driven-contract-testing

---

## Conclusión

**Contract Testing**:
- ✅ **Esencial** en arquitecturas de microservicios (3+ servicios)
- ✅ **Útil** para APIs públicas con múltiples consumers
- ❌ **Innecesario** en monolitos (como este proyecto)
- ❌ **Overkill** en proyectos pequeños (1-2 servicios)

**Para este proyecto**: Integration Tests con Testcontainers son más apropiados. Si migras a microservicios en el futuro, recuerda esta guía.

**Regla de oro**: Empieza con Integration Tests. Migra a Contract Testing **solo** cuando los integration tests se vuelvan demasiado lentos o complejos.
