# Hexagonal Architecture - Proyecto Educativo Completo

[![CI Tests](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/ci.yml/badge.svg)](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/ci.yml)
[![Build](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/build.yml/badge.svg)](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/build.yml)
[![Architecture](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/architecture.yml/badge.svg)](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/architecture.yml)
[![Integration Tests](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/integration-tests.yml/badge.svg)](https://github.com/victormartingil/basic-hexagonal-architecture-example/actions/workflows/integration-tests.yml)

> **Nota:** Los badges de SonarCloud están deshabilitados por defecto. Ver sección "Code Quality" para configuración opcional.

---

## ⚡ Quick Start (5 minutos)

**¿Primera vez aquí? Empieza por la práctica:**

```bash
# 1. Clonar el repositorio (si no lo has hecho)
git clone https://github.com/victormartingil/basic-hexagonal-architecture-example.git
cd basic-hexagonal-architecture-example

# 2. Levantar infraestructura (PostgreSQL + Kafka)
docker-compose up -d

# 3. Compilar y ejecutar la aplicación
./mvnw spring-boot:run

# 4. En otra terminal, crear tu primer usuario
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username": "johndoe", "email": "john@example.com"}'

# 5. Obtener el usuario creado (reemplaza el ID con el que recibiste)
curl -X GET http://localhost:8080/api/v1/users/{id}

# 6. Ver Swagger UI (documentación interactiva)
open http://localhost:8080/swagger-ui.html
```

✅ **¿Funcionó?** ¡Excelente! Ahora puedes:
- 📖 **Entender el código**: Lee [01-Hexagonal-Architecture-Guide.md](docs/01-Hexagonal-Architecture-Guide.md) para comprender qué acabas de ejecutar
- 🎯 **Seguir la ruta de aprendizaje**: Ve a la sección [Cómo Aprender con Este Repositorio](#-cómo-aprender-con-este-repositorio)
- 🧪 **Explorar los tests**: Ejecuta `./mvnw test` para ver los 100 tests unitarios y de arquitectura

❌ **¿Problemas?** Consulta la [Guía de Troubleshooting](docs/14-Troubleshooting-Guide.md)

---

## 📖 ¿Qué es Este Proyecto?

Proyecto de ejemplo que implementa **Arquitectura Hexagonal** (Ports & Adapters) con **CQRS** (Command Query Responsibility Segregation) y **Domain-Driven Design (DDD)**, demostrando flujos completos de **CreateUser** (Command) y **GetUser** (Query).

Este proyecto está diseñado como **plantilla y tutorial exhaustivo** para desarrolladores que quieran entender:
- 🏛️ **Arquitectura Hexagonal** (Ports & Adapters)
- 📦 **Domain-Driven Design** (DDD)
- ⚡ **CQRS** (separación de comandos y consultas)
- 📋 **API-First** con OpenAPI y AsyncAPI
- 🏛️ **Tests de Arquitectura** con ArchUnit
- 📝 **Nomenclaturas y convenciones profesionales**

---

## ✨ Características Destacadas

### **🏗️ Arquitectura y Diseño**
- **🏛️ Arquitectura Hexagonal**: Separación clara en 3 capas (Domain, Application, Infrastructure)
- **🏛️ ArchUnit Tests**: 21 tests que validan automáticamente las reglas arquitecturales
- **🎯 CQRS Completo**: Ejemplos de Commands (Write) y Queries (Read)
- **📦 Domain-Driven Design**: Value Objects, Aggregates, Domain Events
- **📊 Diagramas Mermaid**: Visualizaciones profesionales en las guías (GitHub-friendly)

### **📡 APIs y Eventos**
- **📋 API-First**: Especificación OpenAPI (REST) con generación automática de DTOs
- **🔍 Swagger UI**: Documentación interactiva de la API REST
- **📡 AsyncAPI**: Documentación completa de eventos Kafka (user.events, DLT)
- **🔄 Apache Kafka**: Producer/Consumer con reintentos y Dead Letter Topic (DLT)
- **📡 Domain Events**: Spring Events (in-memory) + Kafka (async integration events)

### **🔐 Seguridad y Resiliencia**
- **🔐 Spring Security + JWT**: Autenticación stateless con roles (ADMIN, MANAGER, VIEWER, SUPPLIER)
- **🌐 CORS**: Configuración con profiles (dev/prod) y documentación completa
- **🛡️ Circuit Breaker**: Resilience4j para prevenir fallos en cascada (fallback automático)

### **📊 Observabilidad (Los 3 Pilares)**
- **📝 Logs**: Estructurados con Correlation ID + Trace ID (SLF4J + Logback)
- **📈 Métricas**: Prometheus + Grafana + métricas customizadas de negocio
- **🔗 Trazas**: Distributed tracing con Zipkin + Micrometer
- **🩺 Spring Actuator**: Health, metrics, prometheus endpoints

### **✅ Testing Completo**
- **126+ Tests Totales**: Unit (79), Integration (16), Architecture (21), E2E (10+)
- **🥒 E2E con Karate**: Tests end-to-end con Gherkin/BDD (local y docker modes)
- **🐳 Testcontainers**: Integration tests con PostgreSQL y Embedded Kafka
- **📊 Test Pyramid**: 65% Unit, 15% Integration, 10% E2E (recomendado)
- **📊 JaCoCo**: Cobertura de código 80%+ (enforced)
- **Kafka Tests**: Separados por Publisher/Consumer siguiendo best practices

### **🚀 CI/CD y DevOps**
- **⚙️ GitHub Actions**: 5 workflows automatizados (CI, Build, Architecture, Integration, E2E)
- **🐳 Docker**: Multi-stage optimizado + Docker Compose para stack completo
- **🐘 PostgreSQL + Flyway**: Migraciones de BD automáticas
- **🔧 Spring Boot 3.5**: Java 21, Records, Lombok, MapStruct

### **📚 Documentación Extensa**
- **7000+ líneas de documentación técnica**: Guías detalladas con ejemplos prácticos
- **16 guías completas**: Desde conceptos básicos hasta patrones avanzados
- **📖 Ejemplos de código comentados**: Explicaciones inline del "por qué"
- **🎓 Rutas de aprendizaje**: Por nivel (Junior, Mid, Senior)

---

## 📚 Guías de Aprendizaje Completas

**IMPORTANTE:** Lee estas guías en orden para aprovechar al máximo el proyecto.

### **🎯 Guías Fundamentales** (Empieza aquí)

1. **[01-Hexagonal-Architecture-Guide.md](docs/01-Hexagonal-Architecture-Guide.md)** - 🏛️ **EMPIEZA AQUÍ**
   - ¿Qué es y por qué usarla?
   - Las 3 capas explicadas con diagramas Mermaid
   - Puertos y Adaptadores
   - Flujo completo paso a paso
   - Ejemplos prácticos

2. **[02-DDD-Guide.md](docs/02-DDD-Guide.md)** - Guía completa de Domain-Driven Design
   - ¿Qué es DDD?
   - Building Blocks (Entity, Value Object, Aggregate, Repository)
   - Domain Events vs Integration Events (Spring Events + Kafka)
   - Circuit Breaker Pattern (Resilience4j) - Estados, configuración, fallbacks
   - Dead Letter Topic (DLT) - Manejo de mensajes fallidos en Kafka
   - Particiones, claves y ordenamiento en Kafka

3. **[03-Modern-Java-Guide.md](docs/03-Modern-Java-Guide.md)** - Guía de Java Moderno
   - Optional (adiós NullPointerException)
   - Streams (procesar colecciones)
   - Lambdas y programación funcional
   - Colecciones (cuándo usar List, Set, Map)
   - Records, Inmutabilidad, var, try-with-resources

### **💡 Guías de Decisión** (¿Es esto para mí?)

4. **[04-When-To-Use-This-Architecture.md](docs/04-When-To-Use-This-Architecture.md)** - ⭐ **MUY IMPORTANTE**
   - ¿Es esto sobreingeniería?
   - Cuándo SÍ usar esta arquitectura
   - Cuándo NO usar esta arquitectura
   - Comparación con otras arquitecturas (Layered, Clean, Microservicios)
   - Proceso de decisión (checklist)

### **🔧 Guías de Implementación**

5. **[05-Conventional-Commits-Guide.md](docs/05-Conventional-Commits-Guide.md)** - 📝 Conventional Commits
   - Formato: `tipo(scope): [ticket] descripción`
   - Tipos de commits (feat, fix, docs, refactor, etc.)
   - 100+ ejemplos prácticos por categoría
   - Pre-commit hooks automáticos

6. **[06-Spring-Security-JWT.md](docs/06-Spring-Security-JWT.md)** - 🔐 Spring Security + JWT
   - Conceptos: Autenticación vs Autorización, Stateless vs Stateful
   - ¿Qué es JWT?: Estructura, firma, funcionamiento con diagramas
   - Implementación completa con roles (ADMIN, MANAGER, VIEWER, SUPPLIER)
   - Autorización por endpoint: Matriz de permisos
   - Best Practices: Secret key, HTTPS, refresh tokens
   - Troubleshooting: Solución a errores comunes

7. **[07-Monitoring-Observability.md](docs/07-Monitoring-Observability.md)** - 📊 Observabilidad (Los 3 Pilares)
   - Logs estructurados con Correlation ID + Trace ID
   - Métricas con Prometheus + Grafana
   - Distributed Tracing con Zipkin
   - Setup completo paso a paso
   - Best practices de producción

8. **[08-Bibliografia.md](docs/08-Bibliografia.md)** - 📚 Libros y Recursos Recomendados
   - Los 5 libros imprescindibles
   - Libros por tema (DDD, Clean Code, Testing, Java)
   - Artículos esenciales y blogs recomendados

9. **[09-API-Versioning-Strategy.md](docs/09-API-Versioning-Strategy.md)** - 📋 Estrategias de Versionado de APIs
   - Cuándo y cómo versionar APIs
   - Estrategias: URL, Header, Content Negotiation
   - Ejemplos prácticos

10. **[10-CI-CD-Pipeline.md](docs/10-CI-CD-Pipeline.md)** - 🚀 CI/CD con GitHub Actions
    - 5 workflows automatizados explicados
    - Estrategia de testing en CI/CD
    - Cómo extender los workflows

11. **[11-Glossary.md](docs/11-Glossary.md)** - 📖 Glosario de Términos
    - Términos técnicos explicados
    - Conceptos de DDD, Arquitectura Hexagonal, CQRS

12. **[12-Code-Quality-JaCoCo-SonarQube.md](docs/12-Code-Quality-JaCoCo-SonarQube.md)** - 📊 Code Quality y Testing
    - JaCoCo: Cómo funciona y cómo medir cobertura
    - SonarQube/SonarCloud: Setup completo paso a paso
    - Interpretación de métricas y reportes

### **🆕 Guías Adicionales**

13. **[13-E2E-Testing-Karate.md](docs/13-E2E-Testing-Karate.md)** - 🥒 Tests E2E con Karate
    - **Test Pyramid**: Percentajes recomendados (Unit 65%, Integration 15%, E2E 10%)
    - Qué debe testear cada tipo de test (Unit vs Integration vs E2E)
    - ¿Qué son los tests E2E y cuándo usarlos?
    - Setup de Karate para tests BDD (Gherkin)
    - Tests contra localhost (modo local) vs Docker (CI/CD)
    - Ejemplos prácticos (create-user.feature, get-user.feature)

14. **[14-Troubleshooting-Guide.md](docs/14-Troubleshooting-Guide.md)** - 🔧 Guía de Troubleshooting
    - Errores comunes y soluciones
    - Docker no funciona
    - Tests fallan
    - Aplicación no levanta
    - Problemas de conectividad

15. **[15-Maven-Multimodule-Guide.md](docs/15-Maven-Multimodule-Guide.md)** - 📦 Maven Multimódulo
    - ¿Qué es y cuándo usar multimódulo?
    - Ventajas y desventajas
    - Ejemplos de cuándo es apropiado
    - Estructura típica

16. **[16-Contract-Testing-Guide.md](docs/16-Contract-Testing-Guide.md)** - 🤝 Contract Testing
    - ¿Qué es Contract Testing?
    - Cuándo usar Contract Testing vs Integration Testing
    - Herramientas: Pact, Spring Cloud Contract
    - Ejemplos prácticos

### **🤖 Para Desarrolladores con IA**

17. **[.ai-guidelines.md](.ai-guidelines.md)** - Guidelines para GitHub Copilot, Cursor, Claude
    - Reglas arquitecturales obligatorias
    - Nomenclatura exacta a seguir
    - Patrones de implementación
    - Checklist de validación

---

## 🎓 Cómo Aprender con Este Repositorio

Este repositorio está diseñado para el **aprendizaje autodidacta progresivo**. Cada guía está numerada y estructurada para construir conocimiento de forma incremental.

### 📖 Orden de Estudio Recomendado

#### **Fase 1: Fundamentos** (4-6 horas)
Comprende los conceptos base antes de escribir código.

1. **[01-Hexagonal-Architecture-Guide.md](docs/01-Hexagonal-Architecture-Guide.md)** - _Tiempo: 1-1.5h_
   - Qué es Arquitectura Hexagonal (Ports & Adapters)
   - Capas: Domain, Application, Infrastructure
   - Inversión de dependencias
   - **Acción**: Leer + Analizar estructura del proyecto `User`

2. **[02-DDD-Guide.md](docs/02-DDD-Guide.md)** - _Tiempo: 1.5-2h_
   - Value Objects, Entities, Aggregates
   - Domain Events, Repository pattern
   - **Acción**: Revisar `User.java`, `Email.java`, `Username.java`

3. **[03-Modern-Java-Guide.md](docs/03-Modern-Java-Guide.md)** - _Tiempo: 1-1.5h_
   - Optional, Streams, Records
   - **Acción**: Ejecutar `./mvnw test` y analizar tests

#### **Fase 2: Decisión y Convenciones** (1-2 horas)

4. **[04-When-To-Use-This-Architecture.md](docs/04-When-To-Use-This-Architecture.md)** - _Tiempo: 45 min_
   - Evalúa si esta arquitectura es apropiada para tu proyecto

5. **[05-Conventional-Commits-Guide.md](docs/05-Conventional-Commits-Guide.md)** - _Tiempo: 30-45 min_
   - Formato de commits profesional
   - **Acción**: Revisar historial `git log --oneline`

#### **Fase 3: Implementación Avanzada** (3-4 horas)

6. **[06-Spring-Security-JWT.md](docs/06-Spring-Security-JWT.md)** - _Tiempo: 2-2.5h_
   - Spring Security Filter Chain
   - JWT stateless authentication
   - **Acción**: Probar endpoints con Postman/Bruno

7. **[07-Monitoring-Observability.md](docs/07-Monitoring-Observability.md)** - _Tiempo: 1-1.5h_
   - Logs, Métricas, Trazas
   - **Acción**: Levantar stack de observabilidad y explorar Grafana

#### **Fase 4: Testing y Calidad** (2-3 horas)

8. **[12-Code-Quality-JaCoCo-SonarQube.md](docs/12-Code-Quality-JaCoCo-SonarQube.md)** - _Tiempo: 1h_
   - JaCoCo: Cobertura de tests
   - **Acción**: Ejecutar `./mvnw clean verify`, revisar reportes

9. **[13-E2E-Testing-Karate.md](docs/13-E2E-Testing-Karate.md)** - _Tiempo: 1-2h_
   - Tests E2E con Karate
   - **Acción**: Ejecutar tests E2E

#### **Fase 5: Referencias** (consulta según necesidad)

10-17. **Guías complementarias**: Maven Multimódulo, Contract Testing, Troubleshooting, etc.

---

### 🎯 Rutas de Aprendizaje por Nivel

#### **🟢 Junior (0-2 años experiencia)**

**Objetivo**: Comprender los fundamentos y patrones básicos.

- **Tiempo estimado**: 12-16 horas (distribuido en 2-3 semanas)
- **Enfoque**: Leer guías 01-03 → Ejecutar tests → Leer código existente
- **Recomendación**: No intentes implementar desde cero. Primero comprende el código existente.

**Orden recomendado**:
1. Quick Start (arriba) → ver que funciona
2. Guía 01 → Entender arquitectura hexagonal
3. Guía 02 → Entender DDD (Value Objects, Aggregates)
4. Explorar código: `User.java`, `Email.java`, `CreateUserService.java`
5. Ejecutar tests: `./mvnw test`
6. Guía 14 → Troubleshooting (si algo falla)

#### **🟡 Mid-Level (2-5 años experiencia)**

**Objetivo**: Comprender decisiones arquitectónicas y patrones avanzados.

- **Tiempo estimado**: 8-12 horas (distribuido en 1-2 semanas)
- **Enfoque**: Leer todas las guías → Analizar decisiones de diseño
- **Recomendación**: Enfócate en el **por qué** de cada patrón, no solo en el **cómo**.

**Orden recomendado**:
1. Quick Start → Familiarízate con el proyecto
2. Guías 01-04 → Fundamentos + Cuándo usar esta arquitectura
3. Guías 05-07 → Convenciones + Seguridad + Observabilidad
4. Analizar decisiones: ¿Por qué Value Objects? ¿Por qué CQRS?
5. Explorar tests de arquitectura: `HexagonalArchitectureTest.java`
6. Guías 12-13 → Calidad de código y E2E testing

#### **🔴 Senior (5+ años experiencia)**

**Objetivo**: Evaluar arquitectura como template para producción.

- **Tiempo estimado**: 4-6 horas (rápida lectura analítica)
- **Enfoque**: Revisar decisiones arquitectónicas → Identificar trade-offs → Proponer mejoras
- **Recomendación**: Cuestiona cada decisión. ¿Es válida para tu contexto empresarial?

**Evaluación**:
1. ¿La separación de capas es correcta para tu organización?
2. ¿El manejo de eventos escala para tu volumetría?
3. ¿La estrategia de testing cubre casos de producción?
4. ¿Qué falta para usar esto en producción enterprise?

**Orden recomendado**:
1. Explorar estructura: `tree src/` o IDE
2. Revisar tests de arquitectura: `HexagonalArchitectureTest.java`
3. Guía 04 → Evaluar trade-offs
4. Guías 15-16 → Maven Multimódulo + Contract Testing (decisiones avanzadas)
5. Identificar gaps para tu caso de uso específico

---

### 🧠 Conceptos Clave por Guía

| Guía | Conceptos Principales | Dificultad |
|------|----------------------|------------|
| **01-Hexagonal** | Inversión de dependencias, Ports & Adapters, Capas limpias | ⭐⭐ |
| **02-DDD** | Value Objects, Aggregates, Domain Events, Ubiquitous Language | ⭐⭐⭐ |
| **03-Modern-Java** | Optional, Streams, Records, Lambdas | ⭐⭐ |
| **04-When-To-Use** | Trade-offs, Decisiones arquitectónicas, Comparativas | ⭐⭐ |
| **05-Commits** | Conventional Commits, Semantic Versioning | ⭐ |
| **06-Security** | JWT, Spring Security, Stateless auth, RBAC | ⭐⭐⭐⭐ |
| **07-Observability** | Logs, Métricas, Trazas, Prometheus, Grafana | ⭐⭐⭐ |
| **12-Code-Quality** | JaCoCo, SonarQube, Code coverage | ⭐⭐ |
| **13-E2E-Karate** | Tests E2E, Gherkin, BDD | ⭐⭐⭐ |
| **14-Troubleshooting** | Debugging, Solución de problemas | ⭐ |

---

## 🛠️ Patrones de Diseño Implementados

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

---

## ✅ Mejores Prácticas Aplicadas

El código sigue estándares de **empresas Fortune 500**:

### **1. Arquitectura**
- ✅ Separación clara de responsabilidades (Domain, Application, Infrastructure)
- ✅ Inversión de dependencias (Dependency Inversion Principle)
- ✅ Código independiente de frameworks (Domain sin Spring)

### **2. Código Limpio**
- ✅ Inmutabilidad por defecto (Records, `final` fields)
- ✅ Value Objects para validaciones de dominio
- ✅ Naming explícito (no abreviaturas, no comentarios innecesarios)

### **3. Testing**
- ✅ Cobertura 80%+ (JaCoCo enforced)
- ✅ Tests independientes (no comparten estado)
- ✅ Tests de integración con infraestructura real (Testcontainers)
- ✅ Tests E2E con Karate (BDD)

### **4. Seguridad**
- ✅ Autenticación JWT stateless (no sesiones HTTP)
- ✅ Autorización por roles (RBAC)
- ✅ CORS configurado con profiles (dev/prod)
- ✅ Secrets externalizados (application.yaml, nunca hardcoded)

### **5. Resiliencia**
- ✅ Circuit Breaker para dependencias externas
- ✅ Dead Letter Topic para eventos fallidos
- ✅ Retry con backoff exponencial

### **6. Observabilidad Completa (Los 3 Pilares)**
- ✅ **Logs**: Estructurados con Correlation ID + Trace ID (SLF4J + Logback)
- ✅ **Métricas**: Prometheus + Grafana + métricas customizadas de negocio
- ✅ **Trazas**: Distributed tracing con Zipkin + Micrometer
- ✅ Spring Actuator (health, metrics, prometheus)

---

## 🏗️ Estructura del Proyecto

```
src/main/java/com/example/hexarch/
├── user/                                    # Bounded Context: User
│   ├── domain/                              # CAPA 1: DOMINIO (Lógica de negocio pura)
│   │   ├── model/
│   │   │   ├── User.java                    # Aggregate Root
│   │   │   └── valueobject/
│   │   │       ├── Email.java               # Value Object
│   │   │       └── Username.java            # Value Object
│   │   ├── event/
│   │   │   └── UserCreatedEvent.java        # Domain Event
│   │   └── exception/
│   │       ├── DomainException.java
│   │       ├── ValidationException.java
│   │       └── UserAlreadyExistsException.java
│   │
│   ├── application/                         # CAPA 2: APLICACIÓN (Casos de uso)
│   │   ├── port/
│   │   │   ├── input/                       # Input Ports (Lo que la app ofrece)
│   │   │   │   ├── CreateUserUseCase.java   # Interface del caso de uso
│   │   │   │   ├── CreateUserCommand.java   # DTO entrada (Command)
│   │   │   │   ├── GetUserUseCase.java
│   │   │   │   ├── GetUserQuery.java        # DTO entrada (Query)
│   │   │   │   └── UserResult.java          # DTO salida
│   │   │   └── output/                      # Output Ports (Lo que la app necesita)
│   │   │       ├── UserRepository.java      # Interface del repositorio
│   │   │       └── UserEventPublisher.java  # Interface del publicador
│   │   └── service/
│   │       ├── CreateUserService.java       # Implementa CreateUserUseCase
│   │       └── GetUserService.java          # Implementa GetUserUseCase
│   │
│   └── infrastructure/                      # CAPA 3: INFRAESTRUCTURA (Detalles técnicos)
│       └── adapter/
│           ├── input/                       # Adaptadores de entrada
│           │   └── rest/
│           │       ├── UserController.java
│           │       ├── dto/generated/       # DTOs generados por OpenAPI
│           │       └── mapper/
│           │           └── UserRestMapper.java  # MapStruct
│           └── output/                      # Adaptadores de salida
│               ├── persistence/
│               │   ├── JpaUserRepositoryAdapter.java
│               │   ├── UserEntity.java      # Entidad JPA
│               │   ├── SpringDataUserRepository.java
│               │   └── mapper/
│               │       └── UserEntityMapper.java
│               └── event/
│                   ├── KafkaUserEventPublisherAdapter.java
│                   ├── SpringEventUserEventPublisherAdapter.java
│                   └── CompositeUserEventPublisherAdapter.java
│
├── notifications/                           # Bounded Context: Notifications
│   ├── application/service/
│   │   └── EmailService.java                # Service con Circuit Breaker
│   └── infrastructure/kafka/consumer/
│       ├── UserEventsKafkaConsumer.java
│       └── UserCreatedEventDLTConsumer.java # Dead Letter Topic consumer
│
└── shared/                                  # Código compartido
    ├── domain/security/
    │   └── Role.java                        # Roles de la aplicación
    └── infrastructure/
        ├── exception/
        │   └── GlobalExceptionHandler.java  # Manejo global de excepciones
        ├── security/
        │   ├── SecurityConfig.java          # Configuración de Spring Security
        │   ├── CorsConfig.java              # Configuración de CORS
        │   └── jwt/JwtTokenProvider.java
        └── web/
            └── CorrelationIdFilter.java     # Filter para Correlation ID
```

**Principio clave**: Las dependencias apuntan hacia adentro:
```
Infrastructure → Application → Domain
```

---

## 📋 Nomenclaturas

### Patrones de Nombres

| Tipo | Patrón | Ejemplo | Ubicación |
|------|--------|---------|-----------|
| **UseCase** (Interface) | `{Accion}{Entidad}UseCase` | `CreateUserUseCase` | `application/port/input/` |
| **Service** (Implementación) | `{Accion}{Entidad}Service` | `CreateUserService` | `application/service/` |
| **Command** | `{Accion}{Entidad}Command` | `CreateUserCommand` | `application/port/input/` |
| **Query** | `{Accion}{Entidad}Query` | `GetUserQuery` | `application/port/input/` |
| **Result** | `{Entidad}Result` | `UserResult` | `application/port/input/` |
| **Repository** (Interface) | `{Entidad}Repository` | `UserRepository` | `application/port/output/` |
| **Controller** | `{Entidad}Controller` | `UserController` | `infrastructure/.../rest/` |
| **Request DTO** | `{Accion}{Entidad}Request` | `CreateUserRequest` | `infrastructure/.../dto/` |
| **Response DTO** | `{Entidad}Response` | `UserResponse` | `infrastructure/.../dto/` |
| **Entity** (JPA) | `{Entidad}Entity` | `UserEntity` | `infrastructure/.../persistence/` |
| **Repository Adapter** | `Jpa{Entidad}RepositoryAdapter` | `JpaUserRepositoryAdapter` | `infrastructure/.../persistence/` |
| **Spring Data Repo** | `SpringData{Entidad}Repository` | `SpringDataUserRepository` | `infrastructure/.../persistence/` |

**Referencia completa**: Ver [01-Hexagonal-Architecture-Guide.md](docs/01-Hexagonal-Architecture-Guide.md#nomenclatura)

---

## 🔄 Flujo Completo: CreateUser

```
1. HTTP Request (POST /api/v1/users)
   ↓
2. UserController (Infrastructure - Input Adapter)
   ├─ @Valid CreateUserRequest (Bean Validation)
   ├─ Mapper: CreateUserRequest → CreateUserCommand
   ↓
3. CreateUserService (Application - Use Case)
   ├─ Verifica: username y email únicos (UserRepository)
   ├─ Crea: User.create() (Domain)
   ├─ Guarda: userRepository.save(user) (Output Port)
   ├─ Publica: userEventPublisher.publish(event) (Output Port)
   └─ Retorna: UserResult
   ↓
4. JpaUserRepositoryAdapter (Infrastructure - Output Adapter)
   ├─ Mapper: User (Domain) → UserEntity (JPA)
   ├─ Persiste: SpringDataUserRepository.save()
   └─ Mapper: UserEntity → User
   ↓
5. UserController
   ├─ Mapper: UserResult → UserResponse
   └─ Retorna: ResponseEntity<UserResponse> (201 CREATED)
```

**Diagrama visual completo**: Ver [01-Hexagonal-Architecture-Guide.md](docs/01-Hexagonal-Architecture-Guide.md#flujo-completo-paso-a-paso)

---

## 🧪 Tests

El proyecto incluye **3 tipos de tests** (total 116 tests):

### **1. Unit Tests (79 tests)** - Sin Docker
Prueban lógica de negocio de forma aislada con mocks.

```bash
# Ejecutar unit tests (rápido, ~1-2 min)
./mvnw test
```

**Qué prueban:**
- Lógica de CreateUserService y GetUserService
- Validaciones de Value Objects (Email, Username)
- Circuit Breaker con Resilience4j (EmailService)
- JWT Token Provider
- Kafka Publisher/Consumer (con mocks)

### **2. Architecture Tests (21 tests)** - Sin Docker
Validan automáticamente las reglas de arquitectura hexagonal con ArchUnit.

```bash
# Ejecutar solo architecture tests
./mvnw test -Dtest=HexagonalArchitectureTest
```

**Qué validan:**
- ✅ Domain NO depende de Application ni Infrastructure
- ✅ Application NO depende de Infrastructure
- ✅ Nomenclatura correcta (Commands, Queries, UseCases)
- ✅ Ubicación correcta de clases en paquetes
- ✅ Controllers anotados con @RestController
- ✅ Services anotados con @Service

### **3. Integration Tests (16 tests)** - Requieren Docker
Prueban el flujo completo con Testcontainers (PostgreSQL + Embedded Kafka).

```bash
# Ejecutar todos los tests (incluyendo integration tests, ~3-5 min)
./mvnw test -Pintegration-tests
```

**Qué prueban:**
- Flujo HTTP completo (REST → Service → Repository → DB)
- Persistencia con JPA y PostgreSQL real
- Kafka Producer/Consumer con Embedded Kafka
- Dead Letter Topic (DLT)
- Security con JWT end-to-end

### **4. E2E Tests con Karate** - Contra app corriendo

```bash
# Ejecutar E2E tests contra localhost
./mvnw test -Pe2e-tests-local

# Ejecutar E2E tests contra Docker
./mvnw test -Pe2e-tests-docker
```

**Qué prueban:**
- Flujos de usuario completos (BDD con Gherkin)
- Crear usuario → Obtener usuario → Validaciones
- Tests de contrato de API

**Ver guía completa**: [13-E2E-Testing-Karate.md](docs/13-E2E-Testing-Karate.md)

### **Resumen de Comandos**

| Comando | Tests Ejecutados | Requiere Docker | Tiempo | Uso |
|---------|------------------|-----------------|--------|-----|
| `./mvnw test` | Unit + Architecture (100) | ❌ No | ~1-2 min | Build rápido, CI/CD |
| `./mvnw test -Pintegration-tests` | Todos (116) | ✅ Sí | ~3-5 min | Validación completa |
| `./mvnw test -Pe2e-tests-local` | E2E contra localhost | ❌ No | ~2-3 min | E2E rápido |
| `./mvnw test -Pe2e-tests-docker` | E2E contra Docker | ✅ Sí | ~4-6 min | E2E realista |

**Troubleshooting**: Si los tests fallan, consulta [14-Troubleshooting-Guide.md](docs/14-Troubleshooting-Guide.md)

---

## 🚀 Deployment a Producción

### **Docker**

```bash
# Build imagen (multi-stage optimizada)
docker build -t hexarch:1.0.0 .

# Run imagen
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/hexarch_db \
  -e JWT_SECRET=your-production-secret-256-bits \
  -e SPRING_PROFILES_ACTIVE=prod \
  hexarch:1.0.0
```

**Características**:
- ✅ Multi-stage build: Imagen final ~200MB
- ✅ Non-root user (seguridad)
- ✅ JVM tuning para contenedores
- ✅ Health check integrado

### **Kubernetes**

El proyecto incluye manifests de ejemplo en `k8s/`:
- `deployment.yaml`: Deployment con liveness/readiness probes
- `service.yaml`: ClusterIP service
- `secrets.yaml`: Secrets para JWT
- `configmap.yaml`: Configuración de la aplicación
- `hpa.yaml`: Horizontal Pod Autoscaler

**Características**:
- ✅ Graceful shutdown (30s timeout)
- ✅ Liveness/Readiness probes (Spring Actuator)
- ✅ HPA: Auto-scaling basado en CPU/memoria
- ✅ Secrets management: JWT_SECRET externalizado

**Ver guía completa**: README incluye sección de deployment completa más abajo.

---

## 📊 Observabilidad: Los 3 Pilares

### **1️⃣ Logs Estructurados** 📝

```
2024-01-15 10:30:00 [f47ac10b,1a2b3c4d] 550e8400 INFO - User created: userId=123
│                   │           │        │     │
Timestamp           TraceId     SpanId   CorrId Level → Message
```

**Dónde ver**:
- Desarrollo: Consola (stdout)
- Producción: Grafana Loki o ELK Stack

### **2️⃣ Métricas** 📈

```bash
# Ver métricas en endpoint Prometheus
curl http://localhost:8080/actuator/prometheus

# Dashboards en Grafana
open http://localhost:3000
```

**Métricas incluidas**:
- `users.created.total`: Total de usuarios creados
- `http.server.requests.seconds`: Latencia de HTTP requests
- `jvm.memory.used`: Memoria JVM usada
- Métricas de Kafka (producer/consumer)

### **3️⃣ Trazas Distribuidas** 🔗

```bash
# Zipkin UI
open http://localhost:9411
```

**Ver guía completa**: [07-Monitoring-Observability.md](docs/07-Monitoring-Observability.md)

### **Setup Rápido de Observabilidad**

```bash
# 1. Levantar stack completo (Prometheus + Grafana + Zipkin)
docker-compose up -d

# 2. Ejecutar aplicación
./mvnw spring-boot:run

# 3. Acceder a dashboards
# - Grafana: http://localhost:3000 (admin/admin)
# - Prometheus: http://localhost:9090
# - Zipkin: http://localhost:9411

# 4. Generar tráfico
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username": "johndoe", "email": "john@example.com"}'

# 5. Ver métricas
curl http://localhost:8080/actuator/prometheus | grep users_created
```

---

## 🚀 CI/CD con GitHub Actions

El proyecto incluye **5 workflows automatizados**:

1. **🧪 CI Tests** (`ci.yml`) - Tests unitarios + arquitectura (cada push/PR)
2. **🏗️ Build** (`build.yml`) - Compilación + JAR artifact (cada push/PR)
3. **🏛️ Architecture** (`architecture.yml`) - ArchUnit validation (cada push/PR)
4. **🐳 Integration Tests** (`integration-tests.yml`) - Tests con Docker (PRs a main + manual)
5. **🥒 E2E Tests** (`e2e-tests.yml`) - Tests E2E con Karate (manual)

**Estrategia**:
```
Pull Request → main
│
├─→ ✅ CI Tests (Unit + Architecture) ~1-2 min
├─→ ✅ Build (Compila JAR) ~1-2 min
├─→ ✅ Architecture (ArchUnit) ~30-60 seg
└─→ ✅ Integration Tests (Docker) ~3-5 min (solo en PRs a main)
```

**Ver guía completa**: [10-CI-CD-Pipeline.md](docs/10-CI-CD-Pipeline.md)

---

## 📖 Conceptos Clave

### **1. Domain Layer (Dominio)**
- **Sin frameworks**: Solo Java puro
- **Inmutable**: Objetos no cambian después de crearse
- **Factory Methods**: `create()` para nuevo, `reconstitute()` para existente
- **Validaciones**: El dominio se valida a sí mismo
- **Value Objects**: Conceptos del dominio con validación propia (Email, Username)

**Ejemplo**:
```java
User user = User.create("johndoe", "john@example.com");
// Internamente crea Value Objects: Username y Email
// Si datos inválidos, lanza ValidationException
```

### **2. Application Layer (Aplicación)**
- **Orquesta**: Coordina dominio y puertos, no contiene lógica compleja
- **Input Ports**: Interfaces que expone (Use Cases)
- **Output Ports**: Interfaces que necesita (Repositories, Event Publishers)
- **Commands/Queries**: DTOs que transportan datos

### **3. Infrastructure Layer (Infraestructura)**
- **Adaptadores de Entrada**: Controllers, Consumers
- **Adaptadores de Salida**: Repositories, Event Publishers, HTTP Clients
- **Detalles Técnicos**: JPA, REST, Kafka

### **4. Inversión de Dependencias**
```
Infrastructure → Application → Domain
   (depende)       (depende)
```

Las dependencias apuntan hacia adentro, pero el flujo de datos va en ambas direcciones.

**Ver explicación completa**: [01-Hexagonal-Architecture-Guide.md](docs/01-Hexagonal-Architecture-Guide.md)

---

## 🎯 Ventajas de Esta Arquitectura

1. **Testeable**: Fácil mockear dependencias en tests unitarios
2. **Mantenible**: Cambios técnicos no afectan la lógica de negocio
3. **Flexible**: Puedes cambiar BD, framework, etc. sin tocar el dominio
4. **Clara**: Separación de responsabilidades evidente
5. **Expresiva**: Value Objects y excepciones de dominio hacen el código más legible
6. **Escalable**: Fácil evolucionar a microservicios

**Cuándo NO usarla**: Ver [04-When-To-Use-This-Architecture.md](docs/04-When-To-Use-This-Architecture.md)

---

## 🤔 Preguntas Frecuentes

**¿Es esto sobreingeniería para mi proyecto?**
→ Lee [04-When-To-Use-This-Architecture.md](docs/04-When-To-Use-This-Architecture.md) para decidir.

**¿Por qué tantos DTOs y archivos?**
→ Cada DTO representa una frontera diferente (REST, Application, Domain, DB). Esto mantiene el dominio puro y desacoplado.

**¿Qué libros debería leer?**
→ Revisa [08-Bibliografia.md](docs/08-Bibliografia.md) para una guía completa.

**¿Tengo un error y no sé qué hacer?**
→ Consulta [14-Troubleshooting-Guide.md](docs/14-Troubleshooting-Guide.md) para soluciones a errores comunes.

**¿Cómo configuro GitHub Copilot para este proyecto?**
→ Usa [.ai-guidelines.md](.ai-guidelines.md) como contexto en tu IDE.

**¿Cuándo usar Maven Multimódulo?**
→ Lee [15-Maven-Multimodule-Guide.md](docs/15-Maven-Multimodule-Guide.md) para entender cuándo es apropiado.

---

## 📚 Referencias y Recursos

### **Libros Recomendados (Top 5)**

1. **"Clean Code"** - Robert C. Martin
2. **"Domain-Driven Design Distilled"** - Vaughn Vernon
3. **"Clean Architecture"** - Robert C. Martin
4. **"Get Your Hands Dirty on Clean Architecture"** - Tom Hombergs
5. **"Effective Java"** - Joshua Bloch

Ver [08-Bibliografia.md](docs/08-Bibliografia.md) para la lista completa con enlaces y descripciones.

### **Documentación Externa**

- [Hexagonal Architecture - Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/) (artículo original)
- [Domain-Driven Design - Eric Evans](https://www.domainlanguage.com/ddd/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Testcontainers](https://www.testcontainers.org/)

---

## 💡 Contribuir

Este es un proyecto educativo. Si encuentras errores o tienes sugerencias:

1. Abre un issue describiendo el problema o mejora
2. Si quieres contribuir código, abre un PR con:
   - Descripción clara del cambio
   - Tests que validen el cambio
   - Documentación actualizada (si aplica)

**Importante**: Mantén el enfoque educativo. Prioriza claridad sobre cleverness.

---

## 📄 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

---

## 🙏 Agradecimientos

A la comunidad de desarrolladores que promueve arquitecturas limpias, DDD y mejores prácticas.

Especial agradecimiento a:
- Alistair Cockburn (Hexagonal Architecture)
- Eric Evans (Domain-Driven Design)
- Robert C. Martin (Clean Architecture)

---

**¡Feliz aprendizaje! 🚀**

Si este proyecto te ayudó, dale una ⭐ en GitHub.
