# Hexagonal Architecture - Proyecto Educativo Completo

[![CI Tests](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/ci.yml/badge.svg)](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/ci.yml)
[![Build](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/build.yml/badge.svg)](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/build.yml)
[![Architecture](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/architecture.yml/badge.svg)](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/architecture.yml)
[![Integration Tests](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/integration-tests.yml/badge.svg)](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/integration-tests.yml)

> **Nota:** Los badges de SonarCloud están deshabilitados por defecto. Ver sección "Code Quality" para configuración opcional.

Proyecto de ejemplo que implementa **Arquitectura Hexagonal** (Ports & Adapters) con **CQRS** (Command Query Responsibility Segregation), demostrando flujos completos de **CreateUser** (Command) y **GetUser** (Query).

Este proyecto está diseñado como **plantilla y tutorial exhaustivo** para desarrolladores junior que quieran entender:
- Arquitectura Hexagonal (Ports & Adapters)
- Domain-Driven Design (DDD)
- CQRS (separación de comandos y consultas)
- API-First con OpenAPI
- Tests de arquitectura con ArchUnit
- Nomenclaturas y convenciones profesionales

## ✨ Características Destacadas

- **📋 API-First**: Especificación OpenAPI primero, código generado después
- **🔍 Swagger UI**: Documentación interactiva de la API
- **🏛️ ArchUnit Tests**: 21 tests que validan automáticamente las reglas arquitecturales
- **📊 Diagramas Mermaid**: Visualizaciones profesionales en las guías (GitHub-friendly)
- **🎯 CQRS Completo**: Ejemplos de Commands (Write) y Queries (Read)
- **📚 5000+ líneas de documentación**: Guías detalladas con ejemplos prácticos
- **✅ 54 Tests**: Unit (10), Integration (23) y Architecture (21)
- **🚀 CI/CD con GitHub Actions**: 5 workflows automatizados para validación continua
- **📊 Code Quality**: JaCoCo (cobertura 80%+) + SonarCloud (análisis continuo)
- **🔧 Spring Boot 3.5**: Java 21, Records, Lombok, MapStruct
- **🐘 PostgreSQL + Flyway**: Migraciones de BD automáticas
- **🐳 Testcontainers**: Integration tests con PostgreSQL real

## 📖 Guías Completas para Juniors

**IMPORTANTE:** Lee estas guías antes de explorar el código:

### Guías Fundamentales (Lee en orden)

1. **[01-Hexagonal-Architecture-Guide.md](docs/01-Hexagonal-Architecture-Guide.md)** - 🏛️ **EMPIEZA AQUÍ**
   - ¿Qué es y por qué usarla?
   - Las 3 capas explicadas con diagramas Mermaid
   - Puertos y Adaptadores
   - Flujo completo paso a paso
   - Ejemplos prácticos

2. **[02-DDD-Guide.md](docs/02-DDD-Guide.md)** - Guía completa de Domain-Driven Design
   - ¿Qué es DDD?
   - Building Blocks (Entity, Value Object, Aggregate, etc.)
   - Ejemplos prácticos con código del proyecto
   - Errores comunes

3. **[03-Modern-Java-Guide.md](docs/03-Modern-Java-Guide.md)** - Guía de Java Moderno
   - Optional (adiós NullPointerException)
   - Streams (procesar colecciones)
   - Lambdas y programación funcional
   - Colecciones (cuándo usar List, Set, Map)
   - Records, Inmutabilidad, var, try-with-resources

### Guías de Decisión

4. **[04-When-To-Use-This-Architecture.md](docs/04-When-To-Use-This-Architecture.md)** - ⭐ **MUY IMPORTANTE**
   - ¿Es esto sobreingeniería?
   - Cuándo SÍ usar esta arquitectura
   - Cuándo NO usar esta arquitectura
   - Comparación con otras arquitecturas (Layered, Clean, Microservicios)
   - Respuestas a preguntas frecuentes de juniors
   - Proceso de decisión (checklist)

5. **[05-Bibliografia.md](docs/05-Bibliografia.md)** - Libros y Recursos Recomendados
   - Los 5 libros imprescindibles
   - Libros por tema (DDD, Clean Code, Testing, Java)
   - Artículos esenciales
   - Blogs y canales de YouTube
   - Recursos en español
   - Ruta de aprendizaje recomendada

### Guías de Calidad de Código

6. **[06-Code-Quality-JaCoCo-SonarQube.md](docs/06-Code-Quality-JaCoCo-SonarQube.md)** - Code Quality y Testing
   - ✅ JaCoCo: Cómo funciona y cómo medir cobertura
   - ✅ SonarQube/SonarCloud: Setup completo paso a paso
   - ✅ Exclusiones: Qué excluir y por qué
   - ✅ Reglas y Quality Gates personalizados
   - ✅ Interpretación de métricas y reportes
   - ✅ Troubleshooting y mejores prácticas

### Para Desarrolladores con IA

7. **[.ai-guidelines.md](.ai-guidelines.md)** - Guidelines para GitHub Copilot, Cursor, Claude
   - Reglas arquitecturales obligatorias
   - Nomenclatura exacta a seguir
   - Patrones de implementación
   - Checklist de validación
   - Usa este archivo como contexto para AIs que trabajen en el proyecto

---

## 📚 ¿Qué es la Arquitectura Hexagonal?

La Arquitectura Hexagonal (también conocida como Ports & Adapters) separa la aplicación en **3 capas**:

1. **Domain** (Núcleo): Lógica de negocio pura, sin dependencias externas
2. **Application** (Casos de Uso): Orquesta la lógica de negocio usando puertos
3. **Infrastructure** (Adaptadores): Implementaciones técnicas (REST, BD, etc.)

### Principios Clave

- ✅ **Inversión de Dependencias**: Las capas externas dependen de las internas, nunca al revés
- ✅ **Puertos**: Interfaces que definen contratos (Input Ports y Output Ports)
- ✅ **Adaptadores**: Implementaciones concretas de los puertos
- ✅ **Independencia**: El dominio no conoce frameworks, BD, HTTP, etc.

---

## 🏗️ Estructura del Proyecto

```
src/main/java/com/example/hexarch/
├── user/
│   ├── domain/                          # CAPA DE DOMINIO
│   │   ├── model/
│   │   │   ├── User.java               # Aggregate Root (lógica de negocio)
│   │   │   └── valueobject/            # Value Objects
│   │   │       ├── Email.java          # Value Object para email
│   │   │       └── Username.java       # Value Object para username
│   │   ├── exception/                  # Excepciones de dominio
│   │   │   ├── DomainException.java    # Excepción base
│   │   │   ├── ValidationException.java
│   │   │   └── UserAlreadyExistsException.java
│   │   └── event/
│   │       └── UserCreatedEvent.java   # Evento de dominio
│   │
│   ├── application/                     # CAPA DE APLICACIÓN
│   │   ├── port/
│   │   │   ├── input/                  # Puertos de entrada (Use Cases)
│   │   │   │   ├── CreateUserUseCase.java    # Interface del caso de uso
│   │   │   │   ├── CreateUserCommand.java    # DTO de entrada
│   │   │   │   └── UserResult.java           # DTO de salida
│   │   │   └── output/                 # Puertos de salida (necesidades)
│   │   │       ├── UserRepository.java       # Interface del repositorio
│   │   │       └── UserEventPublisher.java   # Interface del publicador
│   │   └── service/
│   │       └── CreateUserService.java  # Implementación del caso de uso
│   │
│   └── infrastructure/                  # CAPA DE INFRAESTRUCTURA
│       └── adapter/
│           ├── input/                  # Adaptadores de entrada
│           │   └── rest/
│           │       ├── UserController.java           # REST Controller
│           │       ├── dto/
│           │       │   ├── CreateUserRequest.java    # DTO REST request
│           │       │   └── UserResponse.java         # DTO REST response
│           │       └── mapper/
│           │           └── UserRestMapper.java       # Mapper REST ↔ Application
│           └── output/                 # Adaptadores de salida
│               ├── persistence/
│               │   ├── JpaUserRepositoryAdapter.java  # Implementación JPA
│               │   ├── UserEntity.java                # Entidad JPA
│               │   ├── SpringDataUserRepository.java  # Spring Data JPA
│               │   └── mapper/
│               │       └── UserEntityMapper.java      # Mapper Entity ↔ Domain
│               └── event/
│                   └── LogUserEventPublisherAdapter.java  # Implementación de eventos
│
└── shared/
    └── infrastructure/
        └── exception/
            ├── GlobalExceptionHandler.java  # Manejo global de excepciones
            └── ErrorResponse.java           # DTO de error estándar
```

---

## 📋 Nomenclaturas

### Patrones de Nombres

| Tipo | Patrón | Ejemplo | Ubicación |
|------|--------|---------|-----------|
| **UseCase** (Interface) | `{Accion}{Entidad}UseCase` | `CreateUserUseCase` | `application/port/input/` |
| **Service** (Implementación) | `{Accion}{Entidad}Service` | `CreateUserService` | `application/service/` |
| **Command** | `{Accion}{Entidad}Command` | `CreateUserCommand` | `application/port/input/` |
| **Query** | `{Accion}{Entidad}Query` | `GetUsersQuery` | `application/port/input/` |
| **Result** | `{Entidad}Result` | `UserResult` | `application/port/input/` |
| **Repository** (Interface) | `{Entidad}Repository` | `UserRepository` | `application/port/output/` |
| **Controller** | `{Entidad}Controller` | `UserController` | `infrastructure/.../rest/` |
| **Request DTO** | `{Accion}{Entidad}Request` | `CreateUserRequest` | `infrastructure/.../dto/` |
| **Response DTO** | `{Entidad}Response` | `UserResponse` | `infrastructure/.../dto/` |
| **Entity** | `{Entidad}Entity` | `UserEntity` | `infrastructure/.../persistence/` |
| **Repository Adapter** | `Jpa{Entidad}RepositoryAdapter` | `JpaUserRepositoryAdapter` | `infrastructure/.../persistence/` |
| **Spring Data Repo** | `SpringData{Entidad}Repository` | `SpringDataUserRepository` | `infrastructure/.../persistence/` |

---

## 🔄 Flujo Completo: CreateUser

```
1. HTTP Request
   ↓
2. UserController (Infrastructure - Input Adapter)
   ├─ Valida: @Valid CreateUserRequest
   ├─ Mapper: CreateUserRequest → CreateUserCommand
   ↓
3. CreateUserService (Application - Use Case Implementation)
   ├─ Verifica: username y email únicos (UserRepository)
   ├─ Crea: User.create() (Domain)
   ├─ Guarda: userRepository.save(user) (Output Port)
   ├─ Publica: userEventPublisher.publish(event) (Output Port)
   └─ Retorna: UserResult
   ↓
4. JpaUserRepositoryAdapter (Infrastructure - Output Adapter)
   ├─ Mapper: User → UserEntity
   ├─ Persiste: SpringDataUserRepository.save()
   └─ Mapper: UserEntity → User
   ↓
5. UserController
   ├─ Mapper: UserResult → UserResponse
   └─ Retorna: ResponseEntity<UserResponse> (201 CREATED)
```

---

## 🚀 Cómo Ejecutar

### Prerrequisitos

- Java 21+
- Docker (para PostgreSQL)
- Maven (incluido con Maven Wrapper)

### 1. Levantar PostgreSQL con Docker Compose

El proyecto incluye un `docker-compose.yml` para facilitar el setup:

```bash
# Levantar PostgreSQL en background
docker-compose up -d

# Verificar que PostgreSQL esté corriendo
docker-compose ps
```

**Comandos útiles:**
```bash
# Ver logs de PostgreSQL
docker-compose logs postgres

# Detener PostgreSQL (mantiene los datos)
docker-compose stop

# Iniciar PostgreSQL (si ya existe)
docker-compose start

# Detener y eliminar contenedores + volúmenes (limpia todo)
docker-compose down -v
```

### 2. Compilar y Ejecutar

```bash
# Compilar (excluye integration tests que requieren Docker)
./mvnw clean install

# Ejecutar la aplicación
./mvnw spring-boot:run
```

**La aplicación estará disponible en:** `http://localhost:8080`

### 3. Probar el API

#### Opción A: Swagger UI (Recomendado)

El proyecto usa **OpenAPI Generator** (API-First approach) con Swagger UI integrado:

```
http://localhost:8080/swagger-ui.html
```

Aquí puedes:
- Ver toda la documentación de la API
- Probar endpoints interactivamente
- Ver ejemplos de request/response
- Ver los esquemas de validación

#### Opción B: cURL

```bash
# Crear usuario (Command - Write)
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com"
  }'

# Respuesta esperada (201 CREATED):
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "email": "john@example.com",
  "enabled": true,
  "createdAt": "2024-01-15T10:30:00.123Z"
}

# Obtener usuario por ID (Query - Read)
curl -X GET http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440000

# Respuesta esperada (200 OK):
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "email": "john@example.com",
  "enabled": true,
  "createdAt": "2024-01-15T10:30:00.123Z"
}
```

---

## 🧪 Tests

El proyecto incluye **3 tipos de tests** que validan diferentes aspectos:

### 1. Architecture Tests (ArchUnit) - Sin Docker

Tests que validan automáticamente las reglas de arquitectura hexagonal:

```bash
# Ejecutar tests de arquitectura
./mvnw test -Dtest=HexagonalArchitectureTest
```

**Qué validan:**
- ✅ Domain no depende de Application ni Infrastructure
- ✅ Application no depende de Infrastructure
- ✅ Separación correcta de capas (Domain → Application → Infrastructure)
- ✅ Nomenclatura correcta (Commands, Queries, UseCases, etc.)
- ✅ Ubicación correcta de clases en paquetes
- ✅ Controllers anotados con @RestController
- ✅ Services anotados con @Service
- **21 tests** que fallan si alguien viola la arquitectura

**Resultado esperado:**
```
Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 2. Unit Tests - Sin Docker

Prueban la lógica de negocio de forma aislada con mocks:

```bash
# Ejecutar solo unit tests (rápido, sin Docker)
./mvnw test

# Ejecutar un test específico
./mvnw test -Dtest=CreateUserServiceTest
```

**Qué prueban:**
- Lógica de CreateUserService
- Validaciones de dominio
- Manejo de excepciones

### 3. Integration Tests - Requieren Docker

Prueban el flujo completo con **Testcontainers** (PostgreSQL real en contenedor).
**IMPORTANTE:** Los integration tests están **desactivados por defecto** para permitir builds sin Docker.

#### ¿Qué es Testcontainers?

Testcontainers es una librería que levanta automáticamente contenedores Docker durante los tests:
- 🐳 Inicia PostgreSQL en un contenedor efímero
- 🧹 Limpia automáticamente después de los tests
- 📦 Usa la imagen oficial de PostgreSQL
- 🔒 Aislamiento total entre ejecuciones

#### Prerequisito: Docker

Antes de ejecutar integration tests, asegúrate de que Docker esté corriendo:

```bash
# Verificar que Docker está corriendo
docker info

# Si Docker no está corriendo, inícialo:
# - macOS: Abre Docker Desktop
# - Linux: sudo systemctl start docker
# - Windows: Abre Docker Desktop
```

#### Ejecutar Integration Tests

```bash
# Ejecutar TODOS los tests (incluyendo integration tests)
./mvnw test -Pintegration-tests

# Ejecutar un integration test específico
./mvnw test -Pintegration-tests -Dtest=UserControllerIntegrationTest

# Ejecutar con logs detallados de Testcontainers
./mvnw test -Pintegration-tests -Dorg.slf4j.simpleLogger.log.org.testcontainers=DEBUG
```

**Qué prueban:**
- ✅ **UserControllerIntegrationTest** (10 tests): Flujo HTTP completo (REST → Service → Repository → DB)
  - Serialización/Deserialización JSON
  - Validación Bean Validation
  - Casos de error (400, 404, 409)
  - CQRS: Commands y Queries

- ✅ **JpaUserRepositoryAdapterIntegrationTest** (13 tests): Persistencia aislada (Repository → DB)
  - Operaciones CRUD del adapter
  - Mapping entre Domain y Entity
  - Queries SQL y constraints
  - Edge cases (case-sensitivity, múltiples usuarios)

**¿Por qué tests del adapter por separado?**

En arquitectura hexagonal profesional, es buena práctica probar cada adapter de forma aislada:

1. **Aislamiento**: Si falla, sabes exactamente que el problema está en el adapter
2. **Rapidez**: Tests más focalizados = debugging más rápido
3. **Cobertura**: Puedes probar edge cases del repository difíciles de alcanzar desde el controller
4. **Pirámide de Testing**: Muchos unit tests, algunos integration tests por adapter, pocos end-to-end

**Output esperado:**
```
[Testcontainers] 🐳 Starting PostgreSQL container...
[Testcontainers] ✅ PostgreSQL container started: postgresql:16-alpine
...
Tests run: 54, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Troubleshooting:**

Si los tests fallan con error de Docker:
```bash
# 1. Verificar que Docker está corriendo
docker ps

# 2. Verificar que puedes descargar imágenes
docker pull postgres:16-alpine

# 3. Si usas Docker Desktop, asegúrate de que:
#    - Docker Desktop está iniciado
#    - Tienes permisos para usar Docker
#    - El socket de Docker está accesible
```

### Resumen de Comandos de Test

| Comando | Tests Ejecutados | Requiere Docker | Uso |
|---------|------------------|-----------------|-----|
| `./mvnw test` | Unit + Architecture (31 tests) | ❌ No | Build rápido, CI/CD |
| `./mvnw test -Dtest=HexagonalArchitectureTest` | Solo Architecture (21 tests) | ❌ No | Validar arquitectura |
| `./mvnw test -Dtest=CreateUserServiceTest` | Solo CreateUser unit (6 tests) | ❌ No | Test específico |
| `./mvnw test -Dtest=GetUserServiceTest` | Solo GetUser unit (4 tests) | ❌ No | Test específico |
| `./mvnw test -Pintegration-tests` | **Todos** (Unit + Integration + Architecture, 54 tests) | ✅ Sí | Validación completa |
| `./mvnw test -Pintegration-tests -Dtest=*IntegrationTest` | Solo Integration (23 tests) | ✅ Sí | Tests de integración |
| `./mvnw clean install` | Unit + Architecture (31 tests) | ❌ No | Build sin Docker |
| `./mvnw clean install -Pintegration-tests` | Todos los tests (54 tests) | ✅ Sí | Build completo |

**Desglose de tests:**
- **Unit Tests**: 10 tests (CreateUserService: 6, GetUserService: 4)
- **Architecture Tests**: 21 tests (ArchUnit)
- **Integration Tests**: 23 tests
  - UserControllerIntegrationTest: 10 tests (flujo end-to-end HTTP → DB)
  - JpaUserRepositoryAdapterIntegrationTest: 13 tests (adapter de persistencia aislado)

---

## 🚀 CI/CD con GitHub Actions

El proyecto incluye **4 workflows principales** que se ejecutan automáticamente:

### Workflows Activos

1. **🧪 CI Tests** (`ci.yml`)
   - Se ejecuta en cada push/PR
   - Tests unitarios + arquitectura (31 tests)
   - **No requiere Docker** ⚡ (rápido)
   - Tiempo: ~1-2 minutos

2. **🏗️ Build** (`build.yml`)
   - Se ejecuta en cada push/PR
   - Compila y genera JAR ejecutable
   - Sube artifact (disponible 7 días)
   - Tiempo: ~1-2 minutos

3. **🏛️ Architecture Validation** (`architecture.yml`)
   - Se ejecuta en cada push/PR
   - Solo tests de ArchUnit (21 reglas)
   - Valida arquitectura hexagonal
   - Tiempo: ~30-60 segundos

4. **🐳 Integration Tests** (`integration-tests.yml`)
   - Se ejecuta:
     - Manualmente desde GitHub UI
     - En PRs hacia `main`
     - Semanalmente (lunes 3am)
   - Todos los tests con Testcontainers (54 tests)
   - Tiempo: ~3-5 minutos

### Workflow Opcional (Deshabilitado)

5. **📊 SonarCloud Analysis** (`sonarcloud.yml.disabled`) - **OPCIONAL**
   - ❌ Deshabilitado por defecto
   - Requiere cuenta gratuita en SonarCloud
   - Requiere configuración de secrets
   - Ver [`SONARCLOUD_SETUP.md`](.github/workflows/SONARCLOUD_SETUP.md) para habilitarlo

### Estrategia de CI/CD

```
Pull Request → main/develop
│
├─→ ✅ CI Tests (Unit + Architecture)
├─→ ✅ Build (Verifica compilación)
├─→ ✅ Architecture (Valida reglas)
└─→ ✅ Integration Tests (Solo en PRs a main)
```

### Cómo Usar los Workflows

**Ver estado de workflows:**
- Ve a la pestaña "Actions" en GitHub
- Los badges en el README muestran el estado actual

**Ejecutar Integration Tests manualmente:**
1. Ve a "Actions" → "Integration Tests (with Docker)"
2. Click en "Run workflow"
3. Selecciona la rama y ejecuta

**Ver detalles de un workflow fallido:**
1. Click en el badge rojo del README
2. Selecciona el workflow fallido
3. Revisa los logs de cada step

### Documentación Completa

Ver [`.github/workflows/README.md`](.github/workflows/README.md) para:
- Detalles de cada workflow
- Configuración de badges personalizados
- Cómo habilitar SonarCloud (opcional)
- Troubleshooting común
- Mejoras futuras

---

## 📊 Code Quality: JaCoCo + SonarCloud (Opcional)

El proyecto incluye herramientas de análisis de calidad de código:

### JaCoCo - Code Coverage (Incluido)

Mide qué porcentaje del código está cubierto por tests. **Funciona localmente sin configuración adicional**.

**Ejecutar localmente:**
```bash
# Generar reporte de cobertura
./mvnw clean test

# Ver reporte HTML
open target/site/jacoco/index.html
```

**Métricas:**
- ✅ **Line Coverage**: % de líneas ejecutadas
- ✅ **Branch Coverage**: % de ramas if/else cubiertas
- ✅ **Cobertura mínima**: 80% (configurable en `pom.xml`)

**Exclusiones configuradas:**
- Código generado por OpenAPI
- JPA Entities (solo mapeo DB)
- Clases de configuración

### SonarCloud - Análisis Continuo (OPCIONAL)

> **⚠️ NOTA:** SonarCloud es **OPCIONAL** para el aprendizaje. Requiere cuenta gratuita pero es el estándar en empresas grandes.

**¿Qué es SonarCloud?**
Analiza calidad del código detectando bugs, vulnerabilities y code smells. En empresas profesionales es estándar para:
- Code reviews automatizados
- Detectar problemas de seguridad
- Mantener estándares de código
- Tracking de deuda técnica

**¿Cuándo configurarlo?**
- ✅ Si quieres aprender herramientas empresariales
- ✅ Si vas a hacer el proyecto público en GitHub
- ✅ Si quieres mostrar métricas de calidad en tu portfolio
- ❌ NO es necesario para aprender arquitectura hexagonal

**Setup (Requiere cuenta gratuita en SonarCloud):**

1. Crear cuenta en [SonarCloud](https://sonarcloud.io) (gratis para proyectos open source)
2. Importar tu repositorio de GitHub
3. Obtener el token y project key
4. Añadir secrets en GitHub:
   - `SONAR_TOKEN` (Settings → Secrets → Actions)
   - `SONAR_PROJECT_KEY`
   - `SONAR_ORGANIZATION`
5. Descomentar el workflow `.github/workflows/sonarcloud.yml`
6. Actualizar badges en el README

**Ejecutar localmente (opcional):**
```bash
# Análisis completo (reemplaza <tu-token> con tu token de SonarCloud)
./mvnw clean verify sonar:sonar \
  -Dsonar.token=<tu-token>
```

**Workflow automático (si está habilitado):**
- Por defecto está **DESHABILITADO** (archivo `.yml.disabled`)
- Si lo habilitas, se ejecuta en cada push/PR
- Verifica Quality Gate
- Genera reporte en SonarCloud dashboard

### Documentación Completa

Ver **[`docs/06-Code-Quality-JaCoCo-SonarQube.md`](docs/06-Code-Quality-JaCoCo-SonarQube.md)** para:
- ✅ Cómo funciona JaCoCo (paso a paso)
- ✅ Configurar SonarCloud desde cero
- ✅ Exclusiones recomendadas (qué y por qué)
- ✅ Reglas de SonarQube y Quality Gates
- ✅ Interpretar reportes y métricas
- ✅ Troubleshooting común
- ✅ Mejores prácticas por capa

---

## 📖 Conceptos Clave para Juniors

### 1. Domain Layer (Dominio)

- **Sin frameworks**: Solo Java puro
- **Inmutable**: Objetos no cambian después de crearse
- **Factory Methods**: `create()` para nuevo, `reconstitute()` para existente
- **Validaciones**: El dominio se valida a sí mismo
- **Value Objects**: Conceptos del dominio con validación propia (Email, Username)
- **Instant para timestamps**: Usa `Instant` (UTC) en lugar de `LocalDateTime`

**Ejemplo:**
```java
User user = User.create("johndoe", "john@example.com");
// Si los datos son inválidos, lanza ValidationException
// Internamente crea Value Objects: Username y Email
```

#### Value Objects

Los **Value Objects** encapsulan conceptos del dominio con sus propias reglas de validación:

```java
// En lugar de:
String email = "invalid";  // No se valida
user.setEmail(email);      // Acepta cualquier string

// Usamos:
Email email = Email.of("invalid");  // Lanza ValidationException
// Solo se pueden crear emails válidos
```

**Ventajas:**
- Validación centralizada (un solo lugar)
- Tipo seguro (no puedes pasar cualquier String)
- Expresivo (el código es más claro)
- Reutilizable

#### Instant vs LocalDateTime

Para timestamps de auditoría (createdAt, updatedAt), **usa `Instant`**:

| Tipo | Cuándo usar | Ejemplo |
|------|-------------|---------|
| **Instant** | Timestamps de sistema/auditoría | `createdAt`, `updatedAt`, eventos |
| **LocalDateTime** | Fechas sin zona horaria específica | "El evento es a las 10:00" |
| **ZonedDateTime** | Fechas con zona horaria específica | "Reunión a las 10:00 CET" |

**Por qué Instant:**
- Representa un punto absoluto en UTC
- No depende de zonas horarias
- Perfecto para aplicaciones internacionales
- Se serializa a ISO-8601: `"2024-01-15T10:30:00.123Z"`

### 2. Application Layer (Aplicación)

- **Orquesta**: Coordina dominio y puertos, no contiene lógica compleja
- **Input Ports**: Interfaces que expone (Use Cases)
- **Output Ports**: Interfaces que necesita (Repositories, Event Publishers)
- **Commands/Queries**: DTOs que transportan datos

### 3. Infrastructure Layer (Infraestructura)

- **Adaptadores de Entrada**: Controllers, Consumers, etc.
- **Adaptadores de Salida**: Repositories, Event Publishers, HTTP Clients
- **Detalles Técnicos**: JPA, REST, Kafka, etc.

### 4. Inversión de Dependencias

```
Infrastructure → Application → Domain
   (depende)       (depende)
```

El flujo de datos va: Infrastructure → Application → Domain → Application → Infrastructure

Pero las **dependencias** apuntan hacia adentro.

---

## 🎯 Ventajas de Esta Arquitectura

1. **Testeable**: Fácil mockear dependencias en tests unitarios
2. **Mantenible**: Cambios técnicos no afectan la lógica de negocio
3. **Flexible**: Puedes cambiar BD, framework, etc. sin tocar el dominio
4. **Clara**: Separación de responsabilidades evidente
5. **Expresiva**: Value Objects y excepciones de dominio hacen el código más legible
6. **Internacional**: Uso de Instant garantiza manejo correcto de zonas horarias

---

## 📝 Notas Adicionales

### Migraciones con Flyway

Las migraciones SQL están en `src/main/resources/db/migration/`:
- `V1__create_users_table.sql`: Crea la tabla users

Flyway las ejecuta automáticamente al iniciar la aplicación.

### Configuración

La configuración de la aplicación está en `src/main/resources/application.yaml`:
- Base de datos (PostgreSQL)
- JPA/Hibernate
- Flyway (migraciones)
- Logging
- Jackson (JSON)
- Actuator (monitoreo)

### Logs

Los logs muestran:
- SQL ejecutado (queries)
- Eventos publicados
- Errores detallados

Configura el nivel en `application.yaml`.

---

## 📚 Referencias y Recursos Adicionales

### Documentación del Proyecto

- **[04-When-To-Use-This-Architecture.md](docs/04-When-To-Use-This-Architecture.md)** - Guía de decisión: cuándo usar o no esta arquitectura
- **[05-Bibliografia.md](docs/05-Bibliografia.md)** - Libros, artículos y recursos recomendados para profundizar
- **[.ai-guidelines.md](.ai-guidelines.md)** - Guidelines para herramientas de IA (GitHub Copilot, Cursor, Claude)

### Referencias Externas

- [Hexagonal Architecture - Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/) (artículo original)
- [Domain-Driven Design - Eric Evans](https://www.domainlanguage.com/ddd/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Testcontainers](https://www.testcontainers.org/)

### Libros Recomendados (Top 5)

1. **"Clean Code"** - Robert C. Martin
2. **"Domain-Driven Design Distilled"** - Vaughn Vernon
3. **"Clean Architecture"** - Robert C. Martin
4. **"Get Your Hands Dirty on Clean Architecture"** - Tom Hombergs
5. **"Effective Java"** - Joshua Bloch

Ver [05-Bibliografia.md](docs/05-Bibliografia.md) para la lista completa con enlaces y descripciones.

---

## 🤔 ¿Preguntas Frecuentes?

**¿Es esto sobreingeniería para mi proyecto?**
→ Lee [04-When-To-Use-This-Architecture.md](docs/04-When-To-Use-This-Architecture.md)

**¿Por qué tantos DTOs y archivos?**
→ Lee la sección "Respuestas a Preguntas Frecuentes" en [04-When-To-Use-This-Architecture.md](docs/04-When-To-Use-This-Architecture.md)

**¿Qué libros debería leer?**
→ Revisa [05-Bibliografia.md](docs/05-Bibliografia.md) para una guía completa

**¿Cómo configuro GitHub Copilot para este proyecto?**
→ Usa [.ai-guidelines.md](.ai-guidelines.md) como contexto en tu IDE

---

**¡Feliz aprendizaje! 🚀**
