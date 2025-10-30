# Hexagonal Architecture - Proyecto Educativo Completo

[![CI Tests](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/ci.yml/badge.svg)](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/ci.yml)
[![Build](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/build.yml/badge.svg)](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/build.yml)
[![Architecture](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/architecture.yml/badge.svg)](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/architecture.yml)
[![Integration Tests](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/integration-tests.yml/badge.svg)](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/integration-tests.yml)

> **Nota:** Los badges de SonarCloud están deshabilitados por defecto. Ver sección "Code Quality" para configuración opcional.

Proyecto de ejemplo que implementa **Arquitectura Hexagonal** (Ports & Adapters) con **CQRS** (Command Query Responsibility Segregation), demostrando flujos completos de **CreateUser** (Command) y **GetUser** (Query).

Este proyecto está diseñado como **plantilla y tutorial exhaustivo** para desarrolladores que quieran entender:
- Arquitectura Hexagonal (Ports & Adapters)
- Domain-Driven Design (DDD)
- CQRS (separación de comandos y consultas)
- API-First con OpenAPI
- Tests de arquitectura con ArchUnit
- Nomenclaturas y convenciones profesionales

## ✨ Características Destacadas

- **📋 API-First**: Especificación OpenAPI (REST) + AsyncAPI (Eventos Kafka)
- **🔍 Swagger UI**: Documentación interactiva de la API REST
- **📡 AsyncAPI**: Documentación completa de eventos Kafka (user.events, DLT)
- **🏛️ ArchUnit Tests**: 21 tests que validan automáticamente las reglas arquitecturales
- **📊 Diagramas Mermaid**: Visualizaciones profesionales en las guías (GitHub-friendly)
- **🎯 CQRS Completo**: Ejemplos de Commands (Write) y Queries (Read)
- **📡 Domain Events**: Spring Events (in-memory) + Kafka (async integration events)
- **🔄 Apache Kafka**: Producer/Consumer con reintentos y Dead Letter Topic (DLT)
- **🛡️ Circuit Breaker**: Resilience4j para prevenir fallos en cascada (fallback automático)
- **🔐 Spring Security + JWT**: Autenticación stateless con roles (ADMIN, MANAGER, VIEWER, SUPPLIER)
- **📚 7000+ líneas de documentación**: Guías detalladas con ejemplos prácticos
- **✅ 116 Tests**: Unit (79), Integration (16) y Architecture (21)
  - Kafka tests separados por Publisher/Consumer siguiendo best practices de microservicios
  - Security tests completos (JWT + autorización por roles)
- **🚀 CI/CD con GitHub Actions**: 5 workflows automatizados para validación continua
- **📊 Code Quality**: JaCoCo (cobertura 80%+) + SonarCloud (análisis continuo)
- **🔧 Spring Boot 3.5**: Java 21, Records, Lombok, MapStruct
- **🐘 PostgreSQL + Flyway**: Migraciones de BD automáticas
- **🐳 Testcontainers**: Integration tests con PostgreSQL y Embedded Kafka

## 📖 Guías Completas

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
   - **Domain Events vs Integration Events (Spring Events + Kafka)**
   - **Circuit Breaker Pattern (Resilience4j)** - Estados, configuración, fallbacks
   - Dead Letter Topic (DLT) - Manejo de mensajes fallidos en Kafka
   - Particiones, claves y ordenamiento en Kafka
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
   - Respuestas a preguntas frecuentes
   - Proceso de decisión (checklist)

### Guías de Prácticas y Convenciones

5. **[05-Conventional-Commits-Guide.md](docs/05-Conventional-Commits-Guide.md)** - 📝 Conventional Commits
   - ¿Qué son los Conventional Commits?
   - Formato completo: tipo(scope): [ticket] descripción
   - Tipos de commits (feat, fix, docs, refactor, etc.)
   - 100+ ejemplos prácticos por categoría
   - Pre-commit hooks automáticos
   - Integración con herramientas (semantic-release, changelog)
   - Best practices y errores comunes

### Guías de Implementación

6. **[06-Spring-Security-JWT.md](docs/06-Spring-Security-JWT.md)** - 🔐 Spring Security + JWT
   - ✅ Conceptos: Autenticación vs Autorización, Stateless vs Stateful
   - ✅ ¿Qué es JWT?: Estructura, firma, funcionamiento con diagramas
   - ✅ Spring Security: Arquitectura de filtros, SecurityContext
   - ✅ Implementación completa: Roles (ADMIN, MANAGER, VIEWER, SUPPLIER)
   - ✅ Autorización por endpoint: Matriz de permisos
   - ✅ Flujos completos con diagramas Mermaid
   - ✅ Ejemplos prácticos: curl, Postman, testing
   - ✅ Best Practices: Secret key, HTTPS, refresh tokens
   - ✅ Troubleshooting: Solución a errores comunes

### Guías de Calidad de Código

7. **[12-Code-Quality-JaCoCo-SonarQube.md](docs/12-Code-Quality-JaCoCo-SonarQube.md)** - Code Quality y Testing
   - ✅ JaCoCo: Cómo funciona y cómo medir cobertura
   - ✅ SonarQube/SonarCloud: Setup completo paso a paso
   - ✅ Exclusiones: Qué excluir y por qué
   - ✅ Reglas y Quality Gates personalizados
   - ✅ Interpretación de métricas y reportes
   - ✅ Troubleshooting y mejores prácticas

### Recursos y Referencias

8. **[08-Bibliografia.md](docs/08-Bibliografia.md)** - 📚 Libros y Recursos Recomendados
   - Los 5 libros imprescindibles
   - Libros por tema (DDD, Clean Code, Testing, Java)
   - Artículos esenciales
   - Blogs y canales de YouTube
   - Recursos en español
   - Ruta de aprendizaje recomendada

### Para Desarrolladores con IA

9. **[.ai-guidelines.md](.ai-guidelines.md)** - Guidelines para GitHub Copilot, Cursor, Claude
   - Reglas arquitecturales obligatorias
   - Nomenclatura exacta a seguir
   - Patrones de implementación
   - Checklist de validación
   - Usa este archivo como contexto para AIs que trabajen en el proyecto

---

## 🎓 Cómo Aprender con Este Repositorio

Este repositorio está diseñado para el **aprendizaje autodidacta progresivo**. Cada guía está numerada y estructurada para construir conocimiento de forma incremental.

### 📖 Orden de Estudio Recomendado

#### **Fase 1: Fundamentos** (4-6 horas)
Comprende los conceptos base antes de escribir código.

1. **[01-Hexagonal-Architecture.md](docs/01-Hexagonal-Architecture.md)** - _Tiempo: 1-1.5h_
   - Qué es Arquitectura Hexagonal (Ports & Adapters)
   - Capas: Domain, Application, Infrastructure
   - Inversión de dependencias
   - **Acción**: Leer + Analizar estructura del proyecto User

2. **[02-DDD-Tactical-Patterns.md](docs/02-DDD-Tactical-Patterns.md)** - _Tiempo: 1.5-2h_
   - Value Objects, Entities, Aggregates
   - Domain Events, Repository pattern
   - **Acción**: Revisar User.java, Email.java, Username.java

3. **[03-Testing-Strategies.md](docs/03-Testing-Strategies.md)** - _Tiempo: 1-1.5h_
   - Pirámide de testing: Unit, Integration, E2E
   - Testcontainers para integration tests
   - **Acción**: Ejecutar `./mvnw test` y analizar tests

4. **[04-Kafka-Integration.md](docs/04-Kafka-Integration.md)** - _Tiempo: 1-1.5h_
   - Event-Driven Architecture con Kafka
   - Dead Letter Topic (DLT) y Circuit Breaker
   - **Acción**: Revisar KafkaProducerService y tests

#### **Fase 2: Convenciones** (1 hora)
Establece el estándar de código profesional.

5. **[05-Conventional-Commits-Guide.md](docs/05-Conventional-Commits-Guide.md)** - _Tiempo: 30-45 min_
   - Formato de commits: `type(scope): [TICKET-123] description`
   - Semantic Versioning
   - **Acción**: Revisar historial `git log --oneline`

#### **Fase 3: Implementación Avanzada** (2-3 horas)
Aplica seguridad y autenticación en microservicios.

6. **[06-Spring-Security-JWT.md](docs/06-Spring-Security-JWT.md)** - _Tiempo: 2-2.5h_
   - Spring Security Filter Chain
   - JWT (JSON Web Tokens) stateless authentication
   - Role-Based Access Control (RBAC)
   - **Acción**: Ejecutar SecurityIntegrationTest, probar endpoints con Postman

#### **Fase 4: Calidad de Código** (1-2 horas)
Mide y asegura la calidad del código.

7. **[12-Code-Quality-JaCoCo-SonarQube.md](docs/12-Code-Quality-JaCoCo-SonarQube.md)** - _Tiempo: 1-1.5h_
   - JaCoCo: Cobertura de tests (85%+ required)
   - SonarQube: Análisis estático de código
   - **Acción**: Ejecutar `./mvnw clean verify`, revisar reportes

#### **Fase 5: Referencias** (consulta según necesidad)
Material complementario y recursos externos.

8. **[08-Bibliografia.md](docs/08-Bibliografia.md)** - _Tiempo: Variable_
   - Libros, artículos, videos
   - Documentación oficial
   - Cursos recomendados

### 🎯 Rutas de Aprendizaje por Nivel

#### **🟢 Junior (0-2 años experiencia)**
**Objetivo**: Comprender los fundamentos y patrones básicos.

- **Tiempo estimado**: 12-16 horas (distribuido en 2-3 semanas)
- **Enfoque**: Leer guías 01-03 → Ejecutar tests → Leer código existente → Modificar pequeños cambios
- **Recomendación**: No intentes implementar desde cero. Primero comprende el código existente.
- **Práctica**:
  1. Añadir un nuevo Value Object (ej: `PhoneNumber`)
  2. Crear un nuevo endpoint simple (ej: `GET /api/users/count`)
  3. Escribir tests unitarios para tus cambios

#### **🟡 Mid-Level (2-5 años experiencia)**
**Objetivo**: Comprender decisiones arquitectónicas y patrones avanzados.

- **Tiempo estimado**: 8-12 horas (distribuido en 1-2 semanas)
- **Enfoque**: Leer todas las guías → Analizar decisiones de diseño → Implementar nuevas features
- **Recomendación**: Enfócate en el **por qué** de cada patrón, no solo en el **cómo**.
- **Práctica**:
  1. Implementar un nuevo Bounded Context (ej: `Product`)
  2. Añadir circuit breaker a una nueva integración
  3. Configurar autenticación JWT end-to-end

#### **🔴 Senior (5+ años experiencia)**
**Objetivo**: Evaluar arquitectura como template para producción.

- **Tiempo estimado**: 4-6 horas (rápida lectura analítica)
- **Enfoque**: Revisar decisiones arquitectónicas → Identificar trade-offs → Proponer mejoras
- **Recomendación**: Cuestiona cada decisión. ¿Es válida para tu contexto empresarial?
- **Evaluación**:
  1. ¿La separación de capas es correcta para tu organización?
  2. ¿El manejo de eventos escala para tu volumetría?
  3. ¿La estrategia de testing cubre casos de producción?

### 🧠 Conceptos Clave por Guía

| Guía | Conceptos Principales | Dificultad |
|------|----------------------|------------|
| **01-Hexagonal** | Inversión de dependencias, Ports & Adapters, Capas limpias | ⭐⭐ |
| **02-DDD** | Value Objects, Aggregates, Domain Events, Ubiquitous Language | ⭐⭐⭐ |
| **03-Testing** | Test Pyramid, Testcontainers, Mocking vs Real dependencies | ⭐⭐ |
| **04-Kafka** | Event-Driven, DLT, Circuit Breaker, Async processing | ⭐⭐⭐⭐ |
| **05-Commits** | Conventional Commits, Semantic Versioning, Git best practices | ⭐ |
| **06-Security** | JWT, Spring Security, Stateless auth, RBAC | ⭐⭐⭐⭐ |
| **07-Quality** | Code coverage, Static analysis, Quality gates | ⭐⭐ |
| **08-Bibliografia** | N/A - Material de consulta | N/A |

### 🛠️ Patrones de Diseño Implementados

Este proyecto demuestra los siguientes patrones enterprise:

- **Hexagonal Architecture** (Ports & Adapters)
- **Domain-Driven Design** (Tactical patterns)
- **Repository Pattern** (abstracción de persistencia)
- **Factory Pattern** (`User.create()`, `Email.of()`)
- **Strategy Pattern** (múltiples adaptadores para mismo puerto)
- **Observer Pattern** (Domain Events con Kafka)
- **Command Query Responsibility Segregation (CQRS)** (separación UseCase Input/Output)
- **Circuit Breaker Pattern** (resiliencia en eventos)
- **Dead Letter Queue Pattern** (manejo de errores en eventos)

### ✅ Mejores Prácticas Aplicadas

El código sigue estándares de **empresas Fortune 500**:

1. **Arquitectura**:
   - ✅ Separación clara de responsabilidades (Domain, Application, Infrastructure)
   - ✅ Inversión de dependencias (Dependency Inversion Principle)
   - ✅ Código independiente de frameworks (Domain sin Spring)

2. **Código Limpio**:
   - ✅ Inmutabilidad por defecto (Records, `final` fields)
   - ✅ Value Objects para validaciones de dominio
   - ✅ Naming explicito (no abreviaturas, no comentarios innecesarios)

3. **Testing**:
   - ✅ Cobertura 85%+ (JaCoCo enforced)
   - ✅ Tests independientes (no comparten estado)
   - ✅ Tests de integración con infraestructura real (Testcontainers)

4. **Seguridad**:
   - ✅ Autenticación JWT stateless (no sesiones HTTP)
   - ✅ Autorización por roles (RBAC)
   - ✅ Secrets externalizados (application.yaml, nunca hardcoded)

5. **Resiliencia**:
   - ✅ Circuit Breaker para dependencias externas
   - ✅ Dead Letter Topic para eventos fallidos
   - ✅ Retry con backoff exponencial

6. **Observabilidad Completa** (Los 3 Pilares):
   - ✅ **Logs**: Estructurados con Correlation ID + Trace ID (SLF4J + Logback)
   - ✅ **Métricas**: Prometheus + Grafana + métricas customizadas de negocio
   - ✅ **Trazas**: Distributed tracing con Zipkin + Micrometer
   - ✅ Spring Actuator (health, metrics, prometheus)
   - 📖 **Guía completa**: [docs/07-Monitoring-Observability.md](docs/07-Monitoring-Observability.md)

---

## 📊 Observabilidad: Monitorizar la Aplicación

### ¿Qué es Observabilidad?

La observabilidad te permite **entender qué está pasando** dentro de tu aplicación en producción mediante **3 pilares**:

#### 1️⃣ **LOGS** 📝 - ¿Qué pasó?
Eventos discretos con timestamp que describen acciones:
```
2024-01-15 10:30:00 [f47ac10b,1a2b3c4d] 550e8400 INFO - User created: userId=123
│                   │           │        │     │
Timestamp           TraceId     SpanId   CorrId Level → Message
```

**Cuándo usar cada nivel**:
- `INFO` → Eventos de negocio importantes (usuario creado, pedido completado)
- `WARN` → Problemas recuperables (reintentos, configuración subóptima)
- `ERROR` → Errores críticos que requieren atención

**Dónde ver logs**:
- 🖥️ **Desarrollo**: Consola (stdout)
- 🏢 **Producción**: Grafana Loki (recomendado) o ELK Stack (Kibana)

#### 2️⃣ **MÉTRICAS** 📈 - ¿Cómo está funcionando?
Valores numéricos agregados en el tiempo:

| Métrica | Tipo | Ejemplo |
|---------|------|---------|
| `users.created.total` | Counter | Total usuarios creados |
| `http.server.requests.seconds` | Histogram | Latencia de requests |
| `jvm.memory.used` | Gauge | Memoria JVM usada |

**Dónde ver métricas**:
```bash
# Endpoint de Prometheus
curl http://localhost:8080/actuator/prometheus

# Dashboards en Grafana
http://localhost:3000
```

#### 3️⃣ **TRAZAS DISTRIBUIDAS** 🔗 - ¿Dónde está el cuello de botella?
Seguimiento de un request a través de múltiples servicios:
```
POST /api/v1/users (250ms total)
  ├─ CreateUserUseCase (200ms)
  │  ├─ PostgreSQL INSERT (40ms)
  │  └─ Kafka publish (150ms) ← 60% del tiempo
  └─ Response (10ms)
```

**Dónde ver traces**: Zipkin UI → `http://localhost:9411`

---

### 🚀 Setup Rápido de Observabilidad Local

**1. Levantar stack completo** (Prometheus + Grafana + Loki + Zipkin):
```bash
docker-compose -f docker-compose-observability.yml up -d
```

**2. Ejecutar aplicación**:
```bash
./mvnw spring-boot:run
```

**3. Acceder a dashboards**:
- **Grafana**: http://localhost:3000 (admin/admin)
  - Métricas + Logs + Traces unificados
- **Prometheus**: http://localhost:9090
  - Queries PromQL
- **Zipkin**: http://localhost:9411
  - Distributed tracing

**4. Generar tráfico** (probar endpoints):
```bash
# Crear usuario
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username": "johndoe", "email": "john@example.com"}'

# Ver métricas
curl http://localhost:8080/actuator/prometheus | grep users_created
```

**5. Ver en Grafana**:
- **Logs**: Explore → Datasource: Loki → Query: `{job="hexarch", level="INFO"}`
- **Métricas**: Explore → Datasource: Prometheus → Query: `rate(http_server_requests_seconds_count[5m])`
- **Traces**: Explore → Datasource: Zipkin → Buscar por traceId

---

### 📚 Documentación Completa de Observabilidad

Para aprender en profundidad sobre logs, métricas y trazas:
- **Guía completa**: [docs/07-Monitoring-Observability.md](docs/07-Monitoring-Observability.md)
  - Los 3 pilares explicados
  - Cuándo usar INFO/DEBUG/WARN/ERROR
  - ELK vs Loki (comparativa)
  - Setup de Grafana + Loki + Promtail
  - Queries LogQL y PromQL
  - Correlación Logs + Métricas + Traces
  - Best practices de producción

---

### 💡 Consejos de Aprendizaje

1. **Lee el código antes de ejecutarlo**: Intenta predecir qué hace cada clase.
2. **Ejecuta los tests primero**: `./mvnw test` - Los tests son documentación viva.
3. **Modifica y rompe cosas**: Cambia algo y ve qué test falla. Aprenderás las dependencias.
4. **Usa `.ai-guidelines.md`**: Si trabajas con IAs (Claude, ChatGPT), este archivo les da el contexto completo.
5. **Commit frecuentemente**: Practica Conventional Commits desde el inicio.

### 📊 Tiempo Total Estimado

- **Junior**: 12-16 horas (distribuir en 2-3 semanas)
- **Mid-Level**: 8-12 horas (distribuir en 1-2 semanas)
- **Senior**: 4-6 horas (lectura analítica intensiva)

**Recomendación**: No intentes absorber todo en un día. La arquitectura limpia se aprende **practicando** iterativamente.

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
- Docker (para PostgreSQL y Kafka)
- Maven (incluido con Maven Wrapper)

### 1. Levantar PostgreSQL y Kafka con Docker Compose

El proyecto incluye un `docker-compose.yml` que levanta **PostgreSQL** y **Apache Kafka** (con Zookeeper):

```bash
# Levantar todos los servicios en background
docker-compose up -d

# Verificar que todos los servicios estén corriendo
docker-compose ps

# Deberías ver:
# - postgres (Puerto 5432)
# - zookeeper (Puerto 2181)
# - kafka (Puerto 9092)
```

**Comandos útiles:**
```bash
# Ver logs de PostgreSQL
docker-compose logs postgres

# Ver logs de Kafka
docker-compose logs kafka

# Ver logs en tiempo real (follow)
docker-compose logs -f postgres kafka

# Detener todos los servicios (mantiene los datos)
docker-compose stop

# Iniciar todos los servicios (si ya existen)
docker-compose start

# Detener y eliminar contenedores + volúmenes (limpia todo)
docker-compose down -v
```

### 2. Configurar Variables de Entorno (Opcional pero Recomendado)

El proyecto usa **JWT (JSON Web Tokens)** para autenticación. Por defecto, usa un secret de desarrollo, pero **en producción DEBES usar un secret propio**.

#### Opción A: Variable de Entorno (Recomendado para Producción)

**Linux/macOS:**
```bash
# Generar un secret seguro (256 bits para HMAC-SHA256)
export JWT_SECRET=$(openssl rand -base64 32)

# O usar un secret específico
export JWT_SECRET="tu-secret-super-seguro-de-al-menos-32-caracteres"

# Opcional: Configurar tiempo de expiración (en milisegundos)
export JWT_EXPIRATION=3600000  # 1 hora (recomendado para producción)
```

**Windows (PowerShell):**
```powershell
# Establecer variables de entorno
$env:JWT_SECRET = "tu-secret-super-seguro-de-al-menos-32-caracteres"
$env:JWT_EXPIRATION = "3600000"
```

**Docker:**
```bash
# Ejecutar con variables de entorno
docker run -e JWT_SECRET=your-secret -e JWT_EXPIRATION=3600000 hexarch:latest
```

**Kubernetes:**
```yaml
# secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: hexarch-secrets
type: Opaque
stringData:
  jwt-secret: "your-base64-encoded-secret"
---
# deployment.yaml
env:
  - name: JWT_SECRET
    valueFrom:
      secretKeyRef:
        name: hexarch-secrets
        key: jwt-secret
```

#### Opción B: Usar Secret por Defecto (Solo Desarrollo)

Si **NO** defines `JWT_SECRET`, la aplicación usa un secret de desarrollo por defecto:
- ⚠️ **Solo para desarrollo local y educación**
- ⚠️ **NUNCA usar en producción**
- ⚠️ **El secret está en el código (application.yaml)**

#### ¿Por Qué Externalizar el Secret?

1. **Seguridad**: Secretos no deben estar en el código fuente
2. **Rotación**: Puedes cambiar el secret sin recompilar
3. **Ambientes**: Diferentes secrets para dev/staging/prod
4. **Compliance**: Estándares de seguridad requieren secrets manager
5. **Auditoría**: Cambios de secrets quedan registrados

#### Best Practices para Producción

**Nivel Enterprise (Recomendado):**
- **AWS**: AWS Secrets Manager + AWS Systems Manager Parameter Store
- **Azure**: Azure Key Vault
- **GCP**: Google Secret Manager
- **HashiCorp**: Vault
- **Kubernetes**: External Secrets Operator

**Nivel Básico (Mínimo Aceptable):**
- Variables de entorno inyectadas por orchestrator (Kubernetes, Docker Swarm)
- CI/CD pipeline secrets (GitHub Actions Secrets, GitLab CI/CD Variables)
- `.env` files con `.gitignore` (solo desarrollo local, nunca commitear)

### 3. Compilar y Ejecutar

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

#### Opción C: AsyncAPI - Documentación de Eventos Kafka

El proyecto incluye **AsyncAPI specification** para documentar todos los eventos Kafka:

**Archivo**: `src/main/resources/asyncapi/hexarch-events.yaml`

**Visualizar documentación**:
1. Instala AsyncAPI CLI globalmente:
   ```bash
   npm install -g @asyncapi/cli
   ```

2. Genera HTML interactivo:
   ```bash
   asyncapi generate html src/main/resources/asyncapi/hexarch-events.yaml -o asyncapi-docs
   ```

3. O usa AsyncAPI Studio online:
   - Visita: https://studio.asyncapi.com/
   - Importa el archivo `hexarch-events.yaml`

**Qué incluye:**
- ✅ Especificación completa de `user.events` topic
- ✅ Schema de `UserCreatedEvent` con validaciones
- ✅ Dead Letter Topic (DLT) documentation
- ✅ Headers de distributed tracing (traceId, spanId, correlationId)
- ✅ Ejemplos de payloads
- ✅ Consumer groups y estrategia de offset
- ✅ Configuración de producción vs desarrollo

**Eventos disponibles:**
- `UserCreatedEvent`: Se publica cuando se crea un usuario
- Topics: `user.events` (principal), `user.events.dlt` (fallidos)

**¿Por qué AsyncAPI no genera código (como OpenAPI)?**

**Decisión consciente**: AsyncAPI se usa como **documentación formal** del contrato, pero NO genera código.

**Razones**:
1. **Código educativo**: Los eventos son POJOs simples que juniors pueden leer y modificar fácilmente
2. **AsyncAPI generators menos maduros**: Comparado con OpenAPI, las herramientas de generación para eventos Kafka en Java son menos maduras
3. **Simplicidad**: Los eventos (`UserCreatedEvent`) son más simples que endpoints REST - no justifica generación
4. **Flexibilidad**: Código explícito permite explicar cada decisión de diseño

**En producción enterprise**: Podrías considerar generación desde AsyncAPI si tienes:
- Múltiples servicios con decenas de eventos
- Necesidad de strict contract enforcement
- Equipo senior familiarizado con code generation tools

#### Opción D: Bruno / Postman Collections

Para una experiencia profesional de testing, importa las colecciones preconfigurables:

```
📁 api-collections/
├── bruno/hexarch-api/      # Colección de Bruno (Git-friendly, open source)
│   ├── Users/               # Endpoints de usuarios
│   ├── Monitoring/          # Endpoints de actuator
│   └── environments/        # Entornos (local, production)
├── postman/                 # Colección de Postman
│   ├── hexarch-api-collection.json
│   └── hexarch-environments.json
├── README.md                # 📖 Documentación completa + Quick Start
└── TESTING_GUIDE.md         # 📚 Guía completa de testing

Ver: api-collections/README.md
```

**Características:**
- ✅ Todos los endpoints documentados con ejemplos
- ✅ Tests automáticos incluidos y explicados
- ✅ Variables de entorno que se actualizan automáticamente
- ✅ Quick Start de 5 minutos integrado
- ✅ Guía completa de testing desde cero
- ✅ Ejemplos de cURL incluidos
- ✅ Listo para importar y usar

**Quick Start:**
```bash
# Bruno (Recomendado)
1. Descargar: https://www.usebruno.com/
2. Open Collection → api-collections/bruno/hexarch-api
3. Elegir entorno "local"
4. Ejecutar "Create User" → userId se guarda automáticamente

# Postman
1. Import → api-collections/postman/hexarch-api-collection.json
2. Import → api-collections/postman/hexarch-environments.json
3. Elegir entorno "Local"
4. Ejecutar "Create User"
```

**📚 Guías disponibles:**
- **[README.md](api-collections/README.md)** - Quick Start + Endpoints + Ejemplos de cURL
- **[TESTING_GUIDE.md](api-collections/TESTING_GUIDE.md)** - Guía completa de testing desde cero
  - ⭐ Incluye "Acciones Principales": 11 recetas para setear variables, acceder a datos, etc.
  - Diferencia entre tests y scripts
  - Mejores prácticas profesionales

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

# Ejecutar tests específicos
./mvnw test -Dtest=CreateUserServiceTest
./mvnw test -Dtest=EmailServiceTest
./mvnw test -Dtest=KafkaUserEventPublisherAdapterTest
```

**Qué prueban:**

**User Service (10 tests):**
- Lógica de CreateUserService (6 tests)
- Lógica de GetUserService (4 tests)
- Validaciones de dominio
- Manejo de excepciones

**Kafka Integration (27 tests):**
- **EmailServiceTest** (7 tests): Circuit Breaker con Resilience4j
  - Estados: CLOSED → OPEN → HALF_OPEN
  - Transiciones automáticas
  - Fallback execution
  - Métricas y listeners
- **KafkaUserEventPublisherAdapter** (6 tests): Publicación a Kafka
  - Topic y key correctos
  - Ordenamiento por userId
  - Publicación asíncrona
  - Manejo de errores
- **UserEventsKafkaConsumer** (7 tests): Consumo de eventos
  - Procesamiento exitoso
  - Manejo de excepciones
  - Orden de mensajes
  - Null key handling
- **UserCreatedEventDLTConsumer** (7 tests): Dead Letter Topic
  - Procesamiento de mensajes fallidos
  - Extracción de headers de error
  - Manejo graceful sin reintentos

**Total Unit Tests: 37 tests**

### 3. Integration Tests - Requieren Docker

Prueban el flujo completo con **Testcontainers** (PostgreSQL y Embedded Kafka en contenedores).
**IMPORTANTE:** Los integration tests están **desactivados por defecto** para permitir builds sin Docker.

#### ¿Qué es Testcontainers?

Testcontainers es una librería que levanta automáticamente contenedores Docker durante los tests:
- 🐳 Inicia PostgreSQL en un contenedor efímero
- ☕ Inicia Embedded Kafka (Spring Kafka Test)
- 🧹 Limpia automáticamente después de los tests
- 📦 Usa imágenes oficiales (PostgreSQL, Kafka)
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

**User Service (2 tests):**
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

**Kafka Integration (15 tests) - Requiere Docker funcionando:**

**BEST PRACTICE - Microservices Testing:**
Los tests están separados para simular arquitectura de microservicios real:
- **Publisher tests**: Testan "User Service" sin depender del Consumer (estaría en otro microservicio)
- **Consumer tests**: Testan "Notifications Service" sin depender del Publisher (estaría en otro microservicio)

- ✅ **KafkaPublisherIntegrationTest** (4 tests): Publisher aislado (User Service)
  - Usa Publisher REAL + Test Consumer (NO Consumer de la app)
  - Simula testear Publisher sin tener Consumer en mismo microservicio
  - Topic y key correctos (userId para ordenamiento)
  - Preservación de datos del evento
  - Keys diferentes para usuarios diferentes

- ✅ **KafkaConsumerIntegrationTest** (6 tests): Consumer aislado (Notifications Service)
  - Usa KafkaTemplate para simular eventos de "User Service" + Consumer REAL
  - NO usa Publisher de la app (estaría en otro microservicio)
  - Consumo y procesamiento correcto
  - Circuit Breaker con EmailService
  - Múltiples eventos (orden, null key)

- ✅ **KafkaDLTIntegrationTest** (5 tests): Dead Letter Topic con reintentos
  - Usa KafkaTemplate para simular eventos de "User Service"
  - Mensajes fallidos van al DLT después de reintentos
  - Headers de error (topic, exception, stacktrace)
  - DLT Consumer procesa sin reintentar
  - Mensajes exitosos NO van al DLT
  - Preservación de datos del evento

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
[EmbeddedKafka] 🚀 Starting Embedded Kafka broker...
[EmbeddedKafka] ✅ Kafka broker started on localhost:9093
...
Tests run: 58, Failures: 0, Errors: 0, Skipped: 0
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
| `./mvnw test` | Unit + Architecture (100 tests) | ❌ No | Build rápido, CI/CD |
| `./mvnw test -Dtest=HexagonalArchitectureTest` | Solo Architecture (21 tests) | ❌ No | Validar arquitectura |
| `./mvnw test -Dtest=CreateUserServiceTest` | Solo CreateUser unit (6 tests) | ❌ No | Test específico |
| `./mvnw test -Dtest=JwtTokenProviderTest` | Solo JWT Provider (10 tests) | ❌ No | Test específico |
| `./mvnw test -Pintegration-tests` | **Todos** (Unit + Integration + Architecture, 116 tests) | ✅ Sí | Validación completa |
| `./mvnw test -Pintegration-tests -Dtest=*IntegrationTest` | Solo Integration (16 tests) | ✅ Sí | Tests de integración |
| `./mvnw clean install` | Unit + Architecture (100 tests) | ❌ No | Build sin Docker |
| `./mvnw clean install -Pintegration-tests` | Todos los tests (116 tests) | ✅ Sí | Build completo |

**Desglose de tests:**
- **Unit Tests**: 79 tests
  - User Service: 10 tests (CreateUser: 6, GetUser: 4)
  - Kafka: 27 tests (EmailService: 7, Publisher: 6, Consumer: 7, DLT: 7)
  - Security/JWT: 42 tests (JwtTokenProvider: 10, Role: 32)
- **Architecture Tests**: 21 tests (ArchUnit - validación de arquitectura hexagonal)
- **Integration Tests**: 16 test files (requieren Docker funcionando)
  - User Service: 2 files (Controller: 10, Repository: 13 = 23 test cases)
  - Kafka: 3 files (Publisher: 4, Consumer: 6, DLT: 5 = 15 test cases)
  - Security: 1 file (SecurityIntegration: 10 test cases)

**Total: 116 tests (100 sin Docker + 16 con Docker)**

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
   - Todos los tests con Testcontainers (73 tests: Unit + Architecture + Integration)
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

## 📖 Conceptos Clave

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

## 🚀 Deployment a Producción

### Docker

El proyecto incluye un **Dockerfile multi-stage** optimizado para producción:

```bash
# Build imagen
docker build -t hexarch:1.0.0 .

# Run localmente
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/hexarch_db \
  -e JWT_SECRET=your-production-secret-256-bits \
  -e SPRING_PROFILES_ACTIVE=prod \
  hexarch:1.0.0
```

**Características**:
- ✅ **Multi-stage build**: Imagen final ~200MB (vs ~800MB single-stage)
- ✅ **Non-root user**: Seguridad mejorada (usuario `spring`)
- ✅ **JVM tuning**: Configurado para contenedores (`UseContainerSupport`, `MaxRAMPercentage`)
- ✅ **Health check integrado**: Kubernetes-compatible
- ✅ **Optimizado para cache**: Layers separados para dependencies y código

### Kubernetes

#### Health Probes

El servicio expone endpoints estándar de Kubernetes:

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hexarch-user-service
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: hexarch
        image: hexarch:1.0.0
        ports:
        - containerPort: 8080

        # Liveness Probe: ¿La app está viva?
        # Si falla, Kubernetes REINICIA el pod
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 40
          periodSeconds: 10
          failureThreshold: 3

        # Readiness Probe: ¿La app está lista para tráfico?
        # Si falla, Kubernetes DEJA de enviar requests al pod
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
          failureThreshold: 3

        env:
        - name: SPRING_DATASOURCE_URL
          value: jdbc:postgresql://postgres-service:5432/hexarch_db
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: hexarch-secrets
              key: jwt-secret

        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

#### Graceful Shutdown

La aplicación está configurada para **graceful shutdown**:

```yaml
# application.yaml (ya configurado)
server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

**Qué significa**:
1. Kubernetes envía `SIGTERM` al pod
2. Spring Boot deja de aceptar nuevos requests
3. Espera hasta 30s a que requests en curso terminen
4. Si después de 30s aún hay requests, las fuerza a terminar
5. Shutdown completo

**Beneficio**: Zero downtime en rolling updates

#### ConfigMap y Secrets

```yaml
# secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: hexarch-secrets
type: Opaque
stringData:
  jwt-secret: "your-base64-encoded-256-bit-secret"
  db-password: "your-db-password"

---
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: hexarch-config
data:
  application.yaml: |
    spring:
      application:
        name: hexarch
      kafka:
        bootstrap-servers: kafka-cluster:9092
    management:
      tracing:
        sampling:
          probability: 0.1  # 10% sampling en producción
```

#### Service

```yaml
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: hexarch-user-service
spec:
  type: ClusterIP
  selector:
    app: hexarch
  ports:
  - name: http
    port: 80
    targetPort: 8080
  - name: actuator
    port: 8081
    targetPort: 8080
```

#### Horizontal Pod Autoscaler (HPA)

```yaml
# hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: hexarch-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: hexarch-user-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

**Comportamiento**:
- Mínimo 2 pods (alta disponibilidad)
- Máximo 10 pods (control de costos)
- Escala cuando CPU > 70% o Memory > 80%

### Observability en Producción

#### Distributed Tracing con Zipkin

```yaml
# deployment.yaml (agregar)
env:
- name: MANAGEMENT_ZIPKIN_TRACING_ENDPOINT
  value: "http://zipkin-service:9411/api/v2/spans"
- name: MANAGEMENT_TRACING_SAMPLING_PROBABILITY
  value: "0.1"  # 10% en producción (reduce overhead)
```

**Acceder a Zipkin UI**:
```bash
kubectl port-forward svc/zipkin 9411:9411
# Abrir http://localhost:9411
```

#### Prometheus + Grafana

```yaml
# servicemonitor.yaml (Prometheus Operator)
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: hexarch-metrics
spec:
  selector:
    matchLabels:
      app: hexarch
  endpoints:
  - port: actuator
    path: /actuator/prometheus
    interval: 30s
```

**Dashboards Grafana** (ya incluidos en `monitoring/grafana/dashboards/`):
- HTTP request rate y latencias
- JVM memory y GC
- Database connection pool
- Kafka producer/consumer metrics

### CI/CD con GitHub Actions

El proyecto incluye **5 workflows automatizados**:

1. **Build** → Verifica compilación
2. **CI Tests** → Unit tests + cobertura 85%+
3. **Architecture Tests** → Valida 21 reglas de Hexagonal Architecture
4. **Integration Tests** → Testcontainers (PostgreSQL + Kafka)
5. **Security Scan** (opcional) → OWASP dependency check

**Ver documentación completa**: [docs/10-CI-CD-Pipeline.md](docs/10-CI-CD-Pipeline.md)

---

## 🔄 Kafka + Circuit Breaker + DLT

El proyecto implementa **comunicación asíncrona entre microservicios** usando **Apache Kafka** con patrones de resilencia empresariales:

### 🚀 Flujo Completo

```
User Service                    Notifications Service
     │                                  │
     │  1. createUser()                 │
     │                                  │
     ├─→ 2. Publish UserCreatedEvent    │
     │      ↓                            │
     │    Kafka Topic                    │
     │    "user.created"                 │
     │      ↓                            │
     │                          3. Consume Event
     │                                  │
     │                          4. EmailService
     │                             (Circuit Breaker)
     │                                  │
     │                          ┌───────┴────────┐
     │                          │                 │
     │                    ✅ SUCCESS         ❌ FAILURE
     │                       (Email sent)     (After 3 retries)
     │                                              │
     │                                              ↓
     │                                     Dead Letter Topic
     │                                     "user.created.dlt"
     │                                              │
     │                                              ↓
     │                                     DLT Consumer
     │                                     (Log for investigation)
```

### 📡 Kafka Producer/Consumer

**Publisher (User Service):**
- Publica `UserCreatedEvent` al topic `user.created`
- Usa `userId` como key para garantizar orden (particionamiento)
- Fire-and-forget: no bloquea el flujo principal

**Consumer (Notifications Service):**
- Consume eventos de `user.created`
- Procesa llamando a `EmailService.sendWelcomeEmail()`
- Protegido por Circuit Breaker (Resilience4j)

### 🛡️ Circuit Breaker Pattern

Previene fallos en cascada cuando servicios externos (ej: SMTP server) están caídos:

**Estados:**
- **CLOSED**: Funcionamiento normal
- **OPEN**: Después de N fallos, rechaza llamadas inmediatamente
- **HALF_OPEN**: Después de wait-duration, permite llamadas de prueba

**Configuración:**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      emailService:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
```

**Fallback automático:**
```java
// Si Circuit Breaker está OPEN, ejecuta:
public void sendEmailFallback(String email, String username, Throwable ex) {
    log.warn("Circuit breaker OPEN - Email no enviado a {}", email);
    // Loguea error, puede encolar para retry manual
}
```

### 💀 Dead Letter Topic (DLT)

Maneja mensajes que fallan después de múltiples reintentos:

**Flujo:**
1. Mensaje falla en `user.created` consumer
2. Spring Kafka reintenta automáticamente (ej: 3 intentos con backoff)
3. Después de N fallos, envía al topic `user.created.dlt`
4. DLT Consumer recibe mensaje con headers de error:
   - `kafka_dlt-original-topic`: topic original
   - `kafka_dlt-exception-message`: mensaje de error
   - `kafka_dlt-exception-stacktrace`: stack trace completo
5. DLT Consumer **loguea el error** para investigación manual

**Beneficios:**
- ✅ No pierde mensajes (persisten en DLT)
- ✅ No bloquea la cola (eventos exitosos siguen procesándose)
- ✅ Trazabilidad completa (headers con info del error)
- ✅ Investigación posterior (puede reprocessarse manualmente)

Ver **[docs/02-DDD-Guide.md](docs/02-DDD-Guide.md)** sección "Circuit Breaker" para detalles completos.

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
