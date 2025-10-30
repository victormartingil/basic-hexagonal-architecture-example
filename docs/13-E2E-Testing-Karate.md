# E2E Testing con Karate - Guía Completa

> **Objetivo**: Entender qué son los tests End-to-End (E2E), cuándo usarlos, y cómo implementarlos con Karate en este proyecto.

---

## 📚 Tabla de Contenidos

1. [La Pirámide de Testing](#1-la-pirámide-de-testing)
2. [¿Qué son los Tests E2E?](#2-qué-son-los-tests-e2e)
3. [Test Pyramid: Percentajes Recomendados](#3-test-pyramid-percentajes-recomendados)
4. [Qué Debe Testear Cada Tipo](#4-qué-debe-testear-cada-tipo)
5. [¿Por Qué Karate?](#5-por-qué-karate)
6. [Estructura de los Tests E2E](#6-estructura-de-los-tests-e2e)
7. [Cómo Ejecutar los Tests](#7-cómo-ejecutar-los-tests)
   - [7.1. Entendiendo los 3 Modos](#71-entendiendo-los-3-modos-de-ejecución)
   - [7.2. Modo 1: LOCAL](#72-modo-1-local-desarrollo-manual)
   - [7.3. Modo 2: DOCKER](#73-modo-2-docker-validación-pre-producción)
   - [7.4. Modo 3: TESTCONTAINERS 🚀](#74-modo-3-testcontainers-cicd-automático--recomendado)
   - [7.5. Ejecutar UN SOLO Scenario](#75-ejecutar-un-solo-scenario-debugging)
   - [7.6. Ejecutar en Paralelo](#76-ejecutar-en-paralelo-más-rápido)
8. [Escribir Tests con Karate](#8-escribir-tests-con-karate)
9. [Best Practices](#9-best-practices)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. La Pirámide de Testing

### 1.1. Concepto Fundamental

La **Test Pyramid** (Pirámide de Testing) es un modelo que describe la proporción óptima de diferentes tipos de tests en una aplicación robusta.

```
           /\
          /E2E\          ← Pocos (5-10%) | Lentos | Frágiles | Alto costo
         /------\
        /  INT   \       ← Algunos (15-25%) | Medios | Medianamente robustos
       /----------\
      /   UNIT     \     ← Muchos (65-80%) | Rápidos | Robustos | Bajo costo
     /--------------\
    /________________\
```

**Principios clave**:
1. **Más tests en la base**: Unit tests son rápidos y baratos → haz muchos
2. **Menos tests en la cima**: E2E tests son lentos y caros → haz pocos pero críticos
3. **Balance**: Cubrir TODOS los casos con E2E es **imposible** y **no recomendado**

### 1.2. ¿Por Qué Esta Forma de Pirámide?

```
┌─────────────────────────────────────────────────────────────┐
│ UNIT TESTS (Base de la pirámide)                          │
│                                                             │
│ Velocidad:    ⚡⚡⚡⚡⚡ Muy rápidos (ms)                   │
│ Costo:        💰 Muy baratos (ejecución + mantenimiento)   │
│ Feedback:     ⏱️  Inmediato                                 │
│ Cobertura:    🎯 Una función/clase a la vez                │
│ Fragilidad:   🛡️  Muy robustos (cambios no los rompen)    │
│ Confianza:    📊 70% - Cubren lógica de negocio            │
│                                                             │
│ → IDEAL para validar lógica, edge cases, validaciones      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ INTEGRATION TESTS (Medio de la pirámide)                   │
│                                                             │
│ Velocidad:    ⚡⚡⚡ Rápidos (segundos)                     │
│ Costo:        💰💰 Medios (requieren BD, Kafka, etc.)      │
│ Feedback:     ⏱️  Rápido (pocos minutos)                    │
│ Cobertura:    🎯 Múltiples componentes + dependencias      │
│ Fragilidad:   🛡️  Medianamente robustos                    │
│ Confianza:    📊 85% - Cubren integraciones reales         │
│                                                             │
│ → IDEAL para validar integraciones con BD, Kafka, etc.     │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ E2E TESTS (Cima de la pirámide)                            │
│                                                             │
│ Velocidad:    ⚡ Lentos (minutos)                          │
│ Costo:        💰💰💰💰 Muy caros (infraestructura + tiempo) │
│ Feedback:     ⏱️  Lento (varios minutos)                    │
│ Cobertura:    🎯 Sistema completo (caja negra)             │
│ Fragilidad:   💥 Frágiles (cambios rompen tests fácilmente)│
│ Confianza:    📊 99% - Validan flujo completo real         │
│                                                             │
│ → IDEAL para validar flujos críticos end-to-end            │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. ¿Qué son los Tests E2E?

### 2.1. Definición

**End-to-End (E2E) Testing** valida el flujo completo de la aplicación desde la perspectiva del usuario final, tratando al sistema como una **caja negra**.

```
┌────────────────────────────────────────────────────────────┐
│ USER/CLIENT                                                 │
│   ↓                                                         │
│ HTTP Request (cURL / Postman / Browser)                    │
│   ↓                                                         │
│ ┌──────────────────────────────────────────────────────┐  │
│ │  API REST (Controller)                                │  │
│ │    ↓                                                  │  │
│ │  Application Service                                  │  │
│ │    ↓                                                  │  │
│ │  Domain Logic                                         │  │
│ │    ↓                                                  │  │
│ │  Repository → PostgreSQL                             │  │
│ │    ↓                                                  │  │
│ │  Kafka Publisher → Kafka Broker                      │  │
│ └──────────────────────────────────────────────────────┘  │
│   ↓                                                         │
│ HTTP Response                                               │
│   ↓                                                         │
│ USER/CLIENT                                                 │
└────────────────────────────────────────────────────────────┘

E2E Test: Envía HTTP request → Valida HTTP response
          (No sabe qué pasa internamente)
```

**Características**:
- ✅ Testa el sistema **completo** (todos los componentes integrados)
- ✅ Valida desde la perspectiva del **usuario final**
- ✅ Usa **dependencias reales** (PostgreSQL, Kafka, etc.)
- ✅ Detecta problemas de **integración end-to-end**
- ❌ **NO** testa lógica interna (eso es responsabilidad de unit tests)

### 2.2. E2E vs Integration vs Unit

| Aspecto | Unit Test | Integration Test | E2E Test |
|---------|-----------|------------------|----------|
| **Scope** | 1 clase/función | 2-3 componentes | Sistema completo |
| **Dependencies** | Mocks | Reales (BD, Kafka) | Reales (app + BD + Kafka) |
| **Velocidad** | ⚡⚡⚡⚡⚡ ms | ⚡⚡⚡ segundos | ⚡ minutos |
| **Setup** | Ninguno | Testcontainers | App corriendo |
| **Qué valida** | Lógica de negocio | Integraciones | Flujo completo |
| **Fragilidad** | Muy robusto | Mediano | Frágil |
| **Cuántos** | Muchos (1000+) | Algunos (50-100) | Pocos (10-20) |

**Ejemplo práctico**:

```java
// UNIT TEST: Valida lógica de dominio (aislada)
@Test
void shouldCreateUserWithValidData() {
    User user = User.create(username, email);
    assertThat(user.getUsername()).isEqualTo(username);
}

// INTEGRATION TEST: Valida integración con PostgreSQL
@Test
void shouldPersistUserToDatabase() {
    User user = createUserService.execute(command);
    User saved = userRepository.findById(user.getId()).orElseThrow();
    assertThat(saved.getUsername()).isEqualTo(command.getUsername());
}

// E2E TEST (Karate): Valida flujo completo desde HTTP request
Scenario: Create user end-to-end
  Given path '/api/v1/users'
  And request { "username": "john", "email": "john@test.com" }
  When method POST
  Then status 201
  And match response.id == '#uuid'
```

---

## 3. Test Pyramid: Percentajes Recomendados

### 3.1. Distribución Recomendada

Para una aplicación robusta y escalable, la distribución recomendada es:

```
┌────────────────────────────────────────────────┐
│ E2E TESTS: 5-10%                               │
│ - 10-20 tests                                  │
│ - Flujos críticos (happy paths principales)   │
│ - Casos de error más comunes (404, 400)       │
└────────────────────────────────────────────────┘
         ↑
┌────────────────────────────────────────────────┐
│ INTEGRATION TESTS: 15-25%                      │
│ - 50-100 tests                                 │
│ - Integraciones con BD, Kafka, APIs externas  │
│ - Repository tests, Kafka publisher/consumer  │
└────────────────────────────────────────────────┘
         ↑
┌────────────────────────────────────────────────┐
│ UNIT TESTS: 65-80%                             │
│ - 500-1000+ tests                              │
│ - Lógica de dominio, validaciones, edge cases │
│ - Services, Value Objects, Aggregates         │
└────────────────────────────────────────────────┘
```

**En este proyecto** (actual):
```
Total: ~116 tests

Unit Tests:           79 tests (68%)  ← ✅ BIEN
Integration Tests:    16 tests (14%)  ← ✅ BIEN
Architecture Tests:   21 tests (18%)  ← (No cuentan para pirámide)
E2E Tests:            0 tests (0%)    ← ⚠️  AÑADIREMOS ALGUNOS
```

**Objetivo** (después de añadir E2E):
```
Total: ~126 tests

Unit Tests:           79 tests (63%)  ← ✅
Integration Tests:    16 tests (13%)  ← ✅
E2E Tests:            10 tests (8%)   ← ✅ NUEVO
Architecture Tests:   21 tests (16%)  ← (No cuentan)
```

### 3.2. ¿Por Qué No Más E2E Tests?

**Problema: Too Many E2E Tests**

```
❌ Anti-pattern: "Ice Cream Cone"
       /\
      /  \
     /    \
    /  E2E \       ← MUCHOS E2E tests (lentos, frágiles)
   /--------\
  /   INT    \     ← Pocos integration tests
 /------------\
/__  UNIT  ___\    ← Muy pocos unit tests

Problemas:
- Pipeline de CI/CD tarda 30+ minutos
- Tests se rompen con cambios mínimos
- Difícil de mantener
- Feedback lento para developers
```

**Solución: Test Pyramid (lo que usamos)**

```
✅ Pattern: "Test Pyramid"
      /\
     /E2E\         ← POCOS E2E tests (críticos)
    /------\
   /  INT   \      ← Algunos integration tests
  /----------\
 /   UNIT     \    ← MUCHOS unit tests
/--------------\

Beneficios:
- Pipeline de CI/CD tarda <5 minutos
- Tests robustos ante cambios
- Fácil de mantener
- Feedback rápido
```

---

## 4. Qué Debe Testear Cada Tipo

### 4.1. Unit Tests (65-80%)

**Qué testear**:
✅ Lógica de dominio (Value Objects, Aggregates, Domain Events)
✅ Validaciones de negocio
✅ Edge cases y casos límite
✅ Formateo y transformaciones
✅ Cálculos y algoritmos
✅ Excepciones y manejo de errores

**Qué NO testear**:
❌ Dependencias externas (BD, Kafka)
❌ HTTP requests/responses
❌ Configuración de Spring

**Ejemplos**:
```java
// ✅ TESTEAR: Lógica de dominio
@Test
void shouldNotCreateUserWithInvalidEmail() {
    assertThatThrownBy(() -> User.create("john", "invalid-email"))
        .isInstanceOf(ValidationException.class);
}

// ✅ TESTEAR: Value Objects
@Test
void shouldCreateUsernameWithValidFormat() {
    Username username = Username.of("john_doe123");
    assertThat(username.value()).isEqualTo("john_doe123");
}

// ✅ TESTEAR: Edge cases
@Test
void shouldHandleEmptyUsername() {
    assertThatThrownBy(() -> Username.of(""))
        .isInstanceOf(ValidationException.class);
}
```

### 4.2. Integration Tests (15-25%)

**Qué testear**:
✅ Integración con PostgreSQL (Repository tests)
✅ Integración con Kafka (Publisher/Consumer tests)
✅ Integración con APIs externas (con WireMock o similar)
✅ Transacciones de BD
✅ Migraciones de Flyway
✅ Serialization/Deserialization de eventos

**Qué NO testear**:
❌ Lógica de dominio pura (eso es unit test)
❌ Flujos HTTP completos (eso es E2E test)
❌ Casos de error simples (eso es unit test)

**Ejemplos**:
```java
// ✅ TESTEAR: Integración con PostgreSQL
@Test
void shouldPersistAndRetrieveUserFromDatabase() {
    User user = User.create("john", "john@test.com");
    userRepository.save(user);

    User retrieved = userRepository.findById(user.getId()).orElseThrow();
    assertThat(retrieved.getUsername()).isEqualTo("john");
}

// ✅ TESTEAR: Integración con Kafka
@Test
void shouldPublishEventToKafka() {
    UserCreatedEvent event = new UserCreatedEvent(userId, username);
    eventPublisher.publish(event);

    // Esperar a que el consumer procese el evento
    await().atMost(10, SECONDS).until(() -> {
        return kafkaConsumer.hasReceivedEvent(event.getEventId());
    });
}
```

### 4.3. E2E Tests (5-10%)

**Qué testear**:
✅ Happy paths de flujos críticos (CreateUser, GetUser)
✅ Casos de error más comunes (404 Not Found, 400 Bad Request)
✅ Validación de contratos API (response schemas)
✅ Status codes HTTP correctos
✅ Headers esperados (Location, Content-Type)

**Qué NO testear**:
❌ Todos los edge cases (eso es unit test)
❌ Todas las combinaciones de datos inválidos (eso es unit test)
❌ Lógica interna de dominio (eso es unit test)
❌ Diferentes implementaciones de repository (eso es integration test)

**Ejemplos** (Karate):
```gherkin
# ✅ TESTEAR: Happy path de CreateUser
Scenario: Create user successfully
  Given path '/api/v1/users'
  And request { "username": "john", "email": "john@test.com" }
  When method POST
  Then status 201
  And match response.id == '#uuid'
  And match response.username == 'john'

# ✅ TESTEAR: Caso de error común (400 Bad Request)
Scenario: Create user with invalid email should fail
  Given path '/api/v1/users'
  And request { "username": "john", "email": "invalid" }
  When method POST
  Then status 400

# ❌ NO TESTEAR: Todos los casos inválidos (unit test lo cubre)
# No necesitas 50 scenarios para cada formato inválido de email
```

---

## 5. ¿Por Qué Karate?

### 5.1. Ventajas de Karate

**Karate** es un framework BDD (Behavior-Driven Development) para API testing que usa sintaxis Gherkin (Given/When/Then).

```
┌─────────────────────────────────────────────────────────────┐
│ VENTAJAS DE KARATE                                         │
├─────────────────────────────────────────────────────────────┤
│ ✅ Sintaxis legible (Gherkin)                              │
│    - Developers, QA, y Product Managers pueden leerlo     │
│                                                             │
│ ✅ No requiere código Java                                 │
│    - Tests en .feature files (JSON-like DSL)              │
│                                                             │
│ ✅ Validación JSON potente                                 │
│    - match response.id == '#uuid'                         │
│    - match response.users[*].email == '#present'          │
│                                                             │
│ ✅ Integración con JUnit 5                                 │
│    - Ejecuta desde Maven, IDEs, CI/CD                     │
│                                                             │
│ ✅ Parallel execution nativo                               │
│    - Ejecuta múltiples scenarios en paralelo              │
│                                                             │
│ ✅ Reports HTML automáticos                                │
│    - target/karate-reports/karate-summary.html            │
└─────────────────────────────────────────────────────────────┘
```

### 5.2. Karate vs Otras Alternativas

| Feature | Karate | REST Assured | Postman | Playwright |
|---------|--------|--------------|---------|------------|
| **Lenguaje** | Gherkin DSL | Java | JavaScript | JavaScript/Python |
| **API Testing** | ✅ Excelente | ✅ Excelente | ✅ Bueno | ❌ No (UI) |
| **Legibilidad** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **JSON matching** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| **CI/CD integration** | ✅ | ✅ | ✅ | ✅ |
| **Parallel execution** | ✅ Nativo | ⚠️ Manual | ❌ | ✅ |
| **Learning curve** | ⭐⭐ Baja | ⭐⭐⭐ Media | ⭐ Muy baja | ⭐⭐⭐ Media |

**Por qué elegimos Karate**:
1. ✅ Sintaxis Gherkin es más legible que Java/REST Assured
2. ✅ JSON matching es más poderoso que Postman
3. ✅ Parallel execution out-of-the-box
4. ✅ Excelente para API REST (nuestro caso de uso)
5. ✅ Integración perfecta con JUnit 5 y Maven

---

## 6. Estructura de los Tests E2E

### 6.1. Estructura de Archivos

```
src/test/
├── java/com/example/hexarch/e2e/
│   ├── KarateE2ELocalTest.java          ← Runner para tests contra localhost
│   ├── KarateE2EDockerTest.java         ← Runner contra Docker Compose
│   └── KarateE2ETestcontainersTest.java ← Runner con Testcontainers (recomendado)
│
└── resources/
    ├── karate-config.js                 ← Configuración global de Karate
    │
    └── com/example/hexarch/e2e/user/    ← Feature files por bounded context
        ├── create-user.feature          ← 5 scenarios para CreateUser
        └── get-user.feature             ← 5 scenarios para GetUser
```

**⚠️ IMPORTANTE**: Los archivos `.feature` deben estar en `src/test/resources/`, no en `src/test/java/`.

### 6.2. karate-config.js

Configuración global que se ejecuta antes de cada test:

```javascript
function fn() {
  var config = {
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  };

  var env = karate.env; // System property: karate.env
  karate.log('karate.env system property was:', env);

  if (!env) {
    env = 'local'; // default si no se especifica
  }

  if (env === 'local') {
    // IMPORTANTE: Soporta baseUrl dinámica para Testcontainers
    var systemBaseUrl = karate.properties['karate.baseUrl'];
    if (systemBaseUrl) {
      config.baseUrl = systemBaseUrl;
      karate.log('Using dynamic baseUrl from system property:', config.baseUrl);
    } else {
      config.baseUrl = 'http://localhost:8080';
      karate.log('Running E2E tests against LOCAL environment:', config.baseUrl);
    }
  } else if (env === 'docker') {
    config.baseUrl = 'http://localhost:8080';
    karate.log('Running E2E tests against DOCKER environment:', config.baseUrl);
  } else if (env === 'ci') {
    config.baseUrl = 'http://localhost:8080';
    karate.log('Running E2E tests in CI environment:', config.baseUrl);
  }

  // Configuración de timeouts
  karate.configure('connectTimeout', 10000); // 10 segundos
  karate.configure('readTimeout', 10000);    // 10 segundos

  // Log level (para debugging)
  karate.configure('logPrettyRequest', true);
  karate.configure('logPrettyResponse', true);

  return config;
}
```

**Variables disponibles en todos los scenarios**:
- `baseUrl`: URL base de la API (puede ser dinámica con Testcontainers)
- `headers`: Headers comunes para todos los requests

**Cómo pasar variables personalizadas**:
```bash
# Pasar baseUrl personalizada
./mvnw test -Pe2e-tests -Dkarate.env=local -Dkarate.baseUrl=http://localhost:9090

# Pasar múltiples variables
./mvnw test -Pe2e-tests -Dkarate.env=docker -Dkarate.apiKey=secret123
```

### 6.3. Feature File (create-user.feature)

```gherkin
Feature: Create User - E2E Test
  Como cliente de la API
  Quiero poder crear nuevos usuarios
  Para que el sistema los almacene correctamente

  Background:
    * url baseUrl
    * def randomUsername = 'user_' + java.lang.System.currentTimeMillis()
    * def randomEmail = randomUsername + '@test.com'

  Scenario: Crear un usuario exitosamente
    Given path '/api/v1/users'
    And request
      """
      {
        "username": "#(randomUsername)",
        "email": "#(randomEmail)"
      }
      """
    When method POST
    Then status 201
    And match response.id == '#uuid'
    And match response.username == randomUsername
    And match response.email == randomEmail
```

**Explicación**:
- `Feature`: Describe el feature completo
- `Background`: Se ejecuta antes de cada Scenario (setup)
- `Scenario`: Un caso de test individual
- `Given/When/Then`: Sintaxis Gherkin (BDD)
- `#(variable)`: Interpolación de variables
- `'#uuid'`: Matcher de Karate (valida que es un UUID)

---

## 7. Cómo Ejecutar los Tests

### 7.1. 🔍 Entendiendo los 3 Modos de Ejecución

Existen **3 formas** de ejecutar E2E tests, cada una con sus ventajas:

```
┌────────────────────────────────────────────────────────────────┐
│ MODO 1: LOCAL (Desarrollo Manual)                             │
├────────────────────────────────────────────────────────────────┤
│ App: ./mvnw spring-boot:run (proceso local Java)              │
│ Infra: docker-compose up -d (PostgreSQL, Kafka)               │
│ Tests: Karate hace HTTP a localhost:8080                      │
│                                                                │
│ ✅ Hot reload con DevTools                                    │
│ ✅ Debugging fácil desde IDE                                  │
│ ❌ Requiere setup manual (3 terminales)                       │
│ 📋 Uso: Desarrollo día a día                                  │
└────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────┐
│ MODO 2: DOCKER (Validación Pre-Producción)                    │
├────────────────────────────────────────────────────────────────┤
│ App: docker-compose (imagen Docker)                           │
│ Infra: docker-compose (PostgreSQL, Kafka)                     │
│ Tests: Karate hace HTTP a localhost:8080 (port mapping)       │
│                                                                │
│ ✅ Entorno idéntico a producción                              │
│ ✅ Valida imagen Docker real                                  │
│ ❌ Build lento (Docker build)                                 │
│ 📋 Uso: Pre-release, validación final                         │
└────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────┐
│ MODO 3: TESTCONTAINERS (CI/CD Automático) 🚀 RECOMENDADO      │
├────────────────────────────────────────────────────────────────┤
│ App: @SpringBootTest(webEnvironment = RANDOM_PORT)            │
│ Infra: Testcontainers (PostgreSQL, Kafka automáticos)         │
│ Tests: Karate hace HTTP al puerto aleatorio de Spring         │
│                                                                │
│ ✅ Todo en un solo comando (./mvnw test)                      │
│ ✅ GRATIS en GitHub Actions (Docker preinstalado)             │
│ ✅ Auto-cleanup (contenedores se eliminan al terminar)        │
│ ✅ No requiere docker-compose.yml                             │
│ ⚡ Rápido (~5 min vs ~10 min Docker Mode)                     │
│ 📋 Uso: GitHub Actions, CI/CD pipelines                       │
└────────────────────────────────────────────────────────────────┘
```

### 7.2. Modo 1: LOCAL (desarrollo manual)

**Pre-requisitos**:
1. Tener Docker Compose corriendo (PostgreSQL + Kafka)
2. Tener la aplicación corriendo en localhost:8080

**Pasos**:

```bash
# 1. Levantar infraestructura
docker-compose up -d postgres kafka

# 2. En otra terminal: Levantar aplicación
./mvnw spring-boot:run -DskipTests

# 3. En otra terminal: Ejecutar E2E tests
./mvnw test -Dtest=KarateE2ELocalTest -Dkarate.env=local
```

**Desde IDE** (IntelliJ IDEA / VS Code):
- Click derecho en `KarateE2ELocalTest.java` → Run

**Ventajas**:
- ⚡ Rápido (no requiere Docker build)
- 🐛 Fácil debugging desde IDE
- 🔥 Hot reload con Spring Boot DevTools

**Cuándo usar**: Desarrollo día a día, debugging de features

### 7.3. Modo 2: DOCKER (validación pre-producción)

**Pre-requisitos**:
1. Tener Docker Compose completo corriendo (app + PostgreSQL + Kafka)

**Pasos**:

```bash
# 1. Build de la aplicación
./mvnw clean package -DskipTests

# 2. Build de la imagen Docker
docker build -t hexarch:latest .

# 3. Levantar stack completo
docker-compose up -d

# 4. Esperar a que la app esté ready (ver logs)
docker logs hexarch-app --follow
# Espera: "Started HexarchApplication in X seconds"

# 5. Ejecutar E2E tests contra Docker
./mvnw test -Dtest=KarateE2EDockerTest -Dkarate.env=docker
```

**Ventajas**:
- ✅ Entorno idéntico a producción
- ✅ Valida imagen Docker real
- ✅ No requiere Java local (solo Docker)

**Desventajas**:
- ❌ Build lento (~5-10 minutos)
- ❌ Debugging complejo
- ❌ Requiere docker-compose.yml configurado

**Cuándo usar**: Pre-release, validación final antes de producción

### 7.4. Modo 3: TESTCONTAINERS (CI/CD automático) 🚀 **RECOMENDADO**

**Pre-requisitos**:
1. Tener Docker instalado (pero NO requiere docker-compose corriendo)
2. Tener Maven instalado

**Pasos** (¡TODO EN UN COMANDO!):

```bash
# ✨ Un solo comando ejecuta TODO:
# - Levanta Testcontainers (PostgreSQL, Kafka)
# - Levanta la aplicación (@SpringBootTest)
# - Ejecuta tests E2E con Karate
# - Limpia todo al terminar
./mvnw test -Pe2e-tests -Dkarate.env=local
```

**¿Cómo funciona internamente?**

```java
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.kafka.enabled=false",  // ← Deshabilitar Kafka para tests REST
        "security.enabled=false"        // ← Deshabilitar security para simplificar
    }
)
@Testcontainers
public class KarateE2ETestcontainersTest {

    @LocalServerPort  // ← Puerto aleatorio de Spring Boot
    private int port;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Karate.Test
    Karate testUser() {
        // Configurar la URL dinámica con el puerto aleatorio
        String baseUrl = "http://localhost:" + port;
        System.setProperty("karate.baseUrl", baseUrl);
        System.setProperty("karate.env", "local");

        // Ejecutar todos los .feature files en el directorio user
        return Karate.run("user").relativeTo(getClass());
    }
}
```

**⚠️ IMPORTANTE sobre Kafka**: En los E2E tests, Kafka está DESHABILITADO porque solo estamos validando endpoints REST. La aplicación funciona sin Kafka gracias a `spring.kafka.enabled=false`.

**Ventajas** (por eso es RECOMENDADO):
- ⚡ **Todo automático**: Un solo comando
- 💰 **GRATIS en GitHub Actions**: Docker viene preinstalado
- 🧹 **Auto-cleanup**: Contenedores se eliminan solos
- ⚡ **Rápido**: ~5 minutos (vs ~10 min Docker Mode)
- 🎯 **No requiere setup manual**: No más 3 terminales
- 🔧 **No requiere docker-compose.yml**: Testcontainers lo maneja
- 🛡️ **Puertos aleatorios**: No hay conflictos

**Desventajas**:
- ❌ No valida imagen Docker final (solo código Java)
- ❌ Debugging menos intuitivo que Local Mode

**Cuándo usar**:
- ✅ **GitHub Actions / GitLab CI / Jenkins** (CI/CD pipelines)
- ✅ **Desarrollo rápido** (sin levantar docker-compose)
- ✅ **Pull Request validation**

**Comparación con otros modos**:

| Aspecto | Local | Docker | **Testcontainers** |
|---------|-------|--------|--------------------|
| **Setup** | 3 terminales | docker-compose up | ✅ 1 comando |
| **Velocidad** | ⚡⚡⚡ | ⚡ | ⚡⚡ |
| **CI/CD** | ❌ Manual | ✅ Funciona | ✅ **Ideal** |
| **Debugging** | ⚡⚡⚡ | ⚡ | ⚡⚡ |
| **Auto-cleanup** | ❌ Manual | ❌ Manual | ✅ Automático |
| **Costo GitHub** | N/A | ~10 min | ✅ **~5 min** |

**Ejemplo de uso en GitHub Actions**:

Ver `.github/workflows/e2e-tests.yml` - Modo `local` usa este enfoque:

```yaml
- name: 🏃 Run E2E tests (Local Mode with Testcontainers)
  run: ./mvnw test -Pe2e-tests -Dkarate.env=local
  env:
    TESTCONTAINERS_RYUK_DISABLED: false
```

**Por qué NO se llama "Testcontainers Mode" sino "Local Mode"?**

Porque desde la perspectiva de **Karate**, está haciendo requests a `localhost` (la app corre localmente con @SpringBootTest). Solo la **infraestructura** (PostgreSQL, Kafka) corre en Testcontainers.

```
┌──────────────────────────────────────────────────────┐
│ Karate (test code)                                   │
│   ↓ HTTP request a localhost:random_port            │
│ ┌────────────────────────────────────────────────┐  │
│ │ @SpringBootTest (app en proceso local)         │  │
│ │   ↓ JDBC                  ↓ Kafka               │  │
│ │ ┌──────────────┐     ┌──────────────┐          │  │
│ │ │ Testcontainer│     │ Testcontainer│          │  │
│ │ │ (PostgreSQL) │     │ (Kafka)      │          │  │
│ │ └──────────────┘     └──────────────┘          │  │
│ └────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────┘
```

### 7.5. Ejecutar UN SOLO Scenario (debugging)

```bash
# Ejecutar solo el test de Testcontainers
./mvnw test -Dtest=KarateE2ETestcontainersTest -Dkarate.env=local

# Ejecutar solo el test de Docker
./mvnw test -Dtest=KarateE2EDockerTest -Dkarate.env=docker

# Ejecutar con más logs (debugging)
./mvnw test -Dtest=KarateE2ETestcontainersTest -Dkarate.env=local -Dkarate.output.showLog=true

# Ejecutar con tags específicos
./mvnw test -Pe2e-tests -Dkarate.env=local -Dkarate.options="--tags @smoke"
```

### 7.5.1. Pasar Variables Personalizadas

```bash
# Pasar baseUrl personalizada
./mvnw test -Pe2e-tests -Dkarate.baseUrl=http://localhost:9090

# Pasar múltiples propiedades
./mvnw test -Pe2e-tests -Dkarate.env=local -Dkarate.apiKey=secret -Dkarate.timeout=30000

# Desde karate-config.js, acceder con:
var apiKey = karate.properties['karate.apiKey'];
```

### 7.6. Ejecutar en Paralelo (más rápido)

Editar `KarateE2ELocalTest.java`:

```java
@Karate.Test
Karate testParallel() {
    System.setProperty("karate.env", "local");
    return Karate.run().relativeTo(getClass()).parallel(5); // 5 threads
}
```

---

## 8. Escribir Tests con Karate

### 8.1. Sintaxis Básica

```gherkin
Feature: User API

  Background:
    * url baseUrl                    # Base URL de la API
    * def username = 'testuser'       # Definir variable

  Scenario: Create user
    Given path '/api/v1/users'       # Path del endpoint
    And request                       # Request body
      """
      {
        "username": "#(username)",
        "email": "test@test.com"
      }
      """
    When method POST                  # Método HTTP
    Then status 201                   # Validar status code
    And match response.id == '#uuid'  # Validar response
```

### 8.2. Matchers de Karate

```gherkin
# Validar tipos
And match response.id == '#uuid'        # Es un UUID válido
And match response.username == '#string' # Es un string
And match response.age == '#number'     # Es un número

# Validar valores exactos
And match response.username == 'john'   # Valor exacto
And match response.email == '#present'  # Campo presente (cualquier valor)

# Validar arrays
And match response.users == '#[3]'      # Array de 3 elementos
And match response.users[0].id == '#uuid' # Primer elemento tiene ID

# Validar objetos
And match response ==
  """
  {
    "id": "#uuid",
    "username": "#string",
    "email": "#regex ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
  }
  """

# Validar que campo NO existe
And match response.password == '#notpresent'
```

### 8.3. Variables y Reutilización

```gherkin
Background:
  * url baseUrl
  * def randomId = java.util.UUID.randomUUID()
  * def timestamp = java.lang.System.currentTimeMillis()

Scenario: Crear y obtener usuario
  # 1. Crear usuario
  Given path '/api/v1/users'
  And request { "username": "user_#(timestamp)", "email": "user@test.com" }
  When method POST
  Then status 201
  * def userId = response.id        # Guardar ID para usar después

  # 2. Obtener usuario recién creado
  Given path '/api/v1/users/' + userId
  When method GET
  Then status 200
  And match response.id == userId
```

### 8.4. Tags para Ejecutar Selectivamente

```gherkin
@smoke @critical
Scenario: Create user successfully
  # Test crítico que siempre debe pasar

@slow
Scenario: Create 1000 users in bulk
  # Test lento que solo se ejecuta en CI completo
```

**Ejecutar solo tests con tag**:
```bash
./mvnw test -Dtest=KarateE2ELocalTest -Dkarate.env=local -Dkarate.options="--tags @smoke"
```

---

## 9. Best Practices

### 9.1. Usar Background para Setup Común

```gherkin
# ✅ BIEN: Setup en Background
Feature: User API

  Background:
    * url baseUrl
    * def randomUsername = 'user_' + java.lang.System.currentTimeMillis()

  Scenario: Create user
    Given path '/api/v1/users'
    And request { "username": "#(randomUsername)", ... }
```

### 9.2. Generar Datos Dinámicos

```gherkin
# ✅ BIEN: Datos dinámicos (evita duplicados)
* def timestamp = java.lang.System.currentTimeMillis()
* def username = 'user_' + timestamp

# ❌ MAL: Datos hardcodeados (puede fallar si ya existe)
* def username = 'johndoe'
```

### 9.3. Tests Independientes

```gherkin
# ✅ BIEN: Cada scenario crea sus propios datos
Scenario: Get user
  # Crear usuario para este test
  Given path '/api/v1/users'
  And request { "username": "test_#(timestamp)", ... }
  When method POST
  * def userId = response.id

  # Obtener usuario
  Given path '/api/v1/users/' + userId
  When method GET
  Then status 200

# ❌ MAL: Depender de datos pre-existentes
Scenario: Get user
  # Asume que existe usuario con ID "12345"
  Given path '/api/v1/users/12345'
  When method GET
  Then status 200
```

### 9.4. Validar Solo lo Necesario

```gherkin
# ✅ BIEN: Validar campos críticos
And match response.id == '#uuid'
And match response.username == username
And match response.email == email

# ❌ MAL: Validar timestamp exacto (frágil)
And match response.createdAt == '2025-01-15T10:30:00.000Z'

# ✅ MEJOR: Validar que existe
And match response.createdAt == '#present'
```

### 9.5. Nombres Descriptivos

```gherkin
# ✅ BIEN: Describe QUÉ se está testeando
Scenario: Create user without username should return 400 Bad Request
Scenario: Get non-existent user should return 404 Not Found

# ❌ MAL: Nombre genérico
Scenario: Test 1
Scenario: User test
```

---

## 10. Troubleshooting

### 10.1. Error: Java 21 - GraalVM Compatibility Issues

**Problema**: Al ejecutar tests con Java 21, aparecen errores relacionados con GraalVM:
```
java.lang.NoSuchMethodError: 'void sun.misc.Unsafe.ensureClassInitialized(java.lang.Class)'
```

**Solución**: Este proyecto usa **Karate 1.5.0** con el nuevo groupId `io.karatelabs` que es compatible con Java 21.

**Verificar en `pom.xml`**:
```xml
<properties>
    <karate.version>1.5.0</karate.version>
</properties>

<dependency>
    <groupId>io.karatelabs</groupId>  <!-- ← Nuevo groupId -->
    <artifactId>karate-junit5</artifactId>
    <version>${karate.version}</version>
</dependency>
```

**Nota**: Karate 1.4.1 con `com.intuit.karate` NO es compatible con Java 21. Debes usar 1.5.0+ con `io.karatelabs`.

**Configuración adicional en `pom.xml` (maven-surefire-plugin)**:
```xml
<argLine>
    --add-opens=java.base/java.lang=ALL-UNNAMED
    --add-opens=java.base/java.util=ALL-UNNAMED
    --add-opens=java.base/sun.nio.ch=ALL-UNNAMED
    --add-opens=java.base/java.io=ALL-UNNAMED
    --add-opens=java.base/sun.misc=ALL-UNNAMED
    @{argLine}
</argLine>
<systemPropertyVariables>
    <karate.graal>false</karate.graal>  <!-- Desactivar GraalVM -->
</systemPropertyVariables>
```

### 10.2. Error: "Connection refused"

**Problema**: Karate no puede conectarse a la API.

**Solución**:
```bash
# Verificar que la app está corriendo
curl http://localhost:8080/actuator/health

# Verificar que Docker Compose está levantado
docker ps | grep postgres
docker ps | grep kafka
```

### 10.3. Error: "not found: com/example/hexarch/e2e/user.feature"

**Problema**: Karate no encuentra los archivos `.feature`.

**Causa**: Los archivos `.feature` están en la ubicación incorrecta o el código Java usa la ruta incorrecta.

**Solución**:

1. **Verificar ubicación**: Los `.feature` deben estar en `src/test/resources/com/example/hexarch/e2e/user/`
2. **Verificar código Java**: Usar `relativeTo(getClass())` correctamente:

```java
// ✅ CORRECTO: Especificar subdirectorio
return Karate.run("user").relativeTo(getClass());

// ❌ INCORRECTO: Sin subdirectorio cuando están en subcarpeta
return Karate.run().relativeTo(getClass());

// ❌ INCORRECTO: Usar classpath con Runner.path()
return Runner.path("classpath:com/example/hexarch/e2e/user");
```

3. **Estructura esperada**:
```
src/test/java/com/example/hexarch/e2e/
└── KarateE2ETestcontainersTest.java  ← getClass() apunta aquí

src/test/resources/com/example/hexarch/e2e/
└── user/
    ├── create-user.feature            ← Karate.run("user") busca aquí
    └── get-user.feature
```

### 10.4. Error: Tests pasan individualmente pero fallan en paralelo

**Problema**: Tests tienen dependencias compartidas (ej: mismo username).

**Solución**: Usar datos dinámicos:
```gherkin
# ✅ BIEN: Cada test genera su propio username único
* def timestamp = java.lang.System.currentTimeMillis()
* def username = 'user_' + timestamp
```

### 10.5. Error: "Schema validation failed"

**Problema**: El response no coincide con el matcher.

**Solución**: Revisar el matcher:
```gherkin
# ❌ Error: response.id es UUID pero matcher espera string
And match response.id == 'some-id'

# ✅ Correcto: Usar matcher de UUID
And match response.id == '#uuid'
```

### 10.6. Error: Match failed - Expected "Bad Request" but got "Validation Error"

**Problema**: Los tests fallan porque el campo `error` en la respuesta no coincide.

**Causa**: La aplicación puede devolver diferentes mensajes de error según la configuración de validación.

**Solución**: Actualizar los tests para que coincidan con la respuesta real de la API:

```gherkin
# ❌ ANTES (puede fallar)
Then status 400
And match response.error == 'Bad Request'

# ✅ DESPUÉS (correcto)
Then status 400
And match response.error == 'Validation Error'

# ✅ ALTERNATIVA: Validar solo el status code
Then status 400
And match response.status == 400
```

**Tip**: Ejecuta los tests en modo debug para ver la respuesta exacta:
```bash
./mvnw test -Pe2e-tests -Dkarate.env=local -Dkarate.output.showLog=true
```

### 10.7. Ver Logs Detallados

```bash
# Ejecutar con logs de Karate
./mvnw test -Dtest=KarateE2ELocalTest -Dkarate.env=local -Dkarate.output.showLog=true
```

### 10.8. Ver Reports HTML

Después de ejecutar los tests, Karate genera reports HTML:

```
target/karate-reports/
├── karate-summary.html    ← Abrir en navegador
├── karate-timeline.html
└── create-user.feature.html
```

---

## 🎯 Resumen

### ✅ Qué Hemos Implementado

1. **Karate E2E Tests**: Tests end-to-end con sintaxis Gherkin
2. **Tres Modos de Ejecución**:
   - **Local**: Para desarrollo rápido (app local + docker-compose)
   - **Docker**: Para validación final (app en Docker + docker-compose)
   - **Testcontainers** 🚀: Para CI/CD automático (todo en un comando)
3. **Feature Files**: create-user.feature, get-user.feature
4. **Test Pyramid**: 65% Unit, 15% Integration, 10% E2E

### 🔑 Conceptos Clave

- **Test Pyramid**: Más unit tests, menos E2E tests
- **E2E valida flujos completos**: No lógica interna
- **Tests independientes**: Cada scenario crea sus propios datos
- **Karate es legible**: Sintaxis Gherkin para BDD

### 📚 Próximos Pasos

1. Añadir más scenarios críticos (happy paths)
2. Integrar E2E tests en CI/CD pipeline (GitHub Actions)
3. Añadir tests de performance con Gatling (opcional)

---

## 📖 Referencias

- [Karate Official Docs](https://karatelabs.github.io/karate/)
- [Test Pyramid - Martin Fowler](https://martinfowler.com/articles/practical-test-pyramid.html)
- [Gherkin Syntax Reference](https://cucumber.io/docs/gherkin/reference/)
- [API Testing Best Practices](https://testautomationuniversity.com/api-testing-best-practices/)

---

**¡Felicidades!** Ahora entiendes la pirámide de testing y cómo implementar tests E2E con Karate. 🚀
