# Guía de Maven Multimódulo

**Nota**: Este proyecto NO usa Maven multimódulo (es un proyecto monolítico simple). Esta guía explica **cuándo y por qué** considerarías usar multimódulo en el futuro.

## 📚 Índice

1. [¿Qué es Maven Multimódulo?](#qué-es-maven-multimódulo)
2. [¿Cuándo SÍ usar Multimódulo?](#cuándo-sí-usar-multimódulo)
3. [¿Cuándo NO usar Multimódulo?](#cuándo-no-usar-multimódulo)
4. [Estructura de Ejemplo](#estructura-de-ejemplo)
5. [Ventajas y Desventajas](#ventajas-y-desventajas)
6. [Alternativas](#alternativas)

---

## ¿Qué es Maven Multimódulo?

Maven multimódulo es una estructura de proyecto donde tienes un **proyecto padre** (parent POM) y múltiples **subproyectos** (módulos), cada uno con su propio `pom.xml`.

### Ejemplo Visual

**Proyecto actual (monolítico)**:
```
hexarch/
├── pom.xml                    # Un solo POM
└── src/
    ├── main/java/
    │   └── com/example/hexarch/
    │       ├── user/          # Bounded Context 1
    │       ├── notifications/ # Bounded Context 2
    │       └── shared/        # Código compartido
    └── test/java/
```

**Proyecto multimódulo** (hipotético):
```
hexarch/
├── pom.xml                    # Parent POM (no tiene código)
├── hexarch-domain/            # Módulo 1
│   ├── pom.xml
│   └── src/main/java/         # Solo capa Domain
├── hexarch-application/       # Módulo 2
│   ├── pom.xml
│   └── src/main/java/         # Solo capa Application
├── hexarch-infrastructure/    # Módulo 3
│   ├── pom.xml
│   └── src/main/java/         # Solo capa Infrastructure
└── hexarch-boot/              # Módulo 4
    ├── pom.xml
    └── src/main/java/         # Main class + config
```

---

## ¿Cuándo SÍ usar Multimódulo?

### ✅ Escenario 1: Aplicación Grande con Múltiples Deployables

**Cuándo**:
- Tienes múltiples aplicaciones que **comparten código**
- Cada aplicación se despliega independientemente

**Ejemplo**:
```
mi-empresa/
├── pom.xml                    # Parent
├── shared-domain/             # Módulo compartido
│   └── User.java, Order.java
├── api-rest/                  # App 1: API REST
│   └── SpringBootApplication (port 8080)
├── api-graphql/               # App 2: API GraphQL
│   └── SpringBootApplication (port 8081)
├── batch-processor/           # App 3: Jobs batch
│   └── SpringBootApplication (cron)
└── admin-panel/               # App 4: Admin web
    └── SpringBootApplication (port 8082)
```

**Por qué funciona**:
- `shared-domain` se compila una vez
- Cada app importa `shared-domain` como dependencia
- Si cambias `User.java`, recompila una vez y todas las apps lo usan

### ✅ Escenario 2: Enforzar Arquitectura Hexagonal con Maven

**Cuándo**:
- Quieres que Maven **fuerze** que Domain no dependa de Infrastructure

**Ejemplo**:
```
hexarch/
├── hexarch-domain/            # No dependencies (Java puro)
│   └── pom.xml → <dependencies></dependencies>  # Vacío
├── hexarch-application/       # Depends on: domain
│   └── pom.xml → <dependency>hexarch-domain</dependency>
└── hexarch-infrastructure/    # Depends on: domain + application + Spring Boot
    └── pom.xml → <dependencies>
        <dependency>hexarch-domain</dependency>
        <dependency>hexarch-application</dependency>
        <dependency>spring-boot-starter</dependency>
```

**Ventaja**: Es **imposible** que `hexarch-domain` importe código de `hexarch-infrastructure` porque Maven no lo permite.

**¿Es mejor que ArchUnit?**
- **Maven multimódulo**: Prevención compile-time (falla al compilar)
- **ArchUnit**: Detección test-time (falla en tests)

Ambos son válidos. ArchUnit es más simple, multimódulo es más estricto.

### ✅ Escenario 3: Librería Reutilizable

**Cuándo**:
- Quieres publicar módulos independientes en Maven Central

**Ejemplo**:
```
my-library/
├── pom.xml
├── my-library-core/           # JAR publicable independiente
│   └── pom.xml
├── my-library-spring-boot-starter/  # Starter de Spring Boot
│   └── pom.xml
└── my-library-examples/       # Ejemplos (no publicados)
    └── pom.xml
```

**Ventaja**: Usuarios pueden importar solo lo que necesitan:
```xml
<!-- Usuario solo importa core -->
<dependency>
    <groupId>com.mycompany</groupId>
    <artifactId>my-library-core</artifactId>
</dependency>
```

### ✅ Escenario 4: Equipos Grandes Trabajando en Paralelo

**Cuándo**:
- Equipos diferentes trabajan en módulos diferentes
- Quieres builds más rápidos (compilar solo lo que cambió)

**Ejemplo**:
```
mega-app/
├── team-a-module/   # Equipo A (5 devs)
├── team-b-module/   # Equipo B (5 devs)
└── team-c-module/   # Equipo C (5 devs)
```

**Ventaja**:
```bash
# Compilar solo módulo de Team A
mvn install -pl team-a-module

# Compilar módulos de Team A y B (si Team B depende de A)
mvn install -pl team-a-module,team-b-module -am
```

---

## ¿Cuándo NO usar Multimódulo?

### ❌ Escenario 1: Proyecto Pequeño/Mediano (como este)

**Características del proyecto actual**:
- 1 aplicación desplegable
- ~50 clases de producción
- 1 equipo pequeño (1-5 devs)

**Por qué NO necesitas multimódulo**:
- Complejidad innecesaria (más `pom.xml` que mantener)
- Build no es lento (compila en segundos)
- ArchUnit ya valida la arquitectura

**Regla general**: Si tu build tarda menos de 2 minutos, NO necesitas multimódulo.

### ❌ Escenario 2: Separación Solo por Capa

**Mal uso común**:
```
hexarch/
├── hexarch-domain/         # Solo porque "es una capa"
├── hexarch-application/    # Solo porque "es una capa"
└── hexarch-infrastructure/ # Solo porque "es una capa"
```

**Por qué es mala idea**:
- No hay beneficio real (sigues desplegando 1 JAR)
- Complejidad aumenta sin ganancia
- Navegación en IDE más difícil (saltar entre módulos)

**Mejor**: Usa paquetes Java (`domain/`, `application/`, `infrastructure/`) como en este proyecto.

### ❌ Escenario 3: "Porque lo vi en otro proyecto"

**Síntoma**: Copias estructura sin entender el porqué.

**Realidad**:
- Empresa con 500 desarrolladores → multimódulo tiene sentido
- Startup con 5 desarrolladores → multimódulo es overkill

**Pregunta clave**: ¿Qué problema específico estás resolviendo con multimódulo?

Si la respuesta es "ninguno, solo quiero separar capas", NO lo uses.

---

## Estructura de Ejemplo

### Estructura Recomendada (Multimódulo)

```
mi-proyecto/
├── pom.xml                           # Parent POM
│   └── <packaging>pom</packaging>
│
├── mi-proyecto-domain/               # Módulo: Domain
│   ├── pom.xml
│   └── src/main/java/
│       └── com/example/domain/
│           ├── User.java
│           └── Order.java
│
├── mi-proyecto-application/          # Módulo: Application
│   ├── pom.xml
│   │   └── <dependency>mi-proyecto-domain</dependency>
│   └── src/main/java/
│       └── com/example/application/
│           └── CreateUserService.java
│
├── mi-proyecto-infrastructure/       # Módulo: Infrastructure
│   ├── pom.xml
│   │   └── <dependency>mi-proyecto-application</dependency>
│   └── src/main/java/
│       └── com/example/infrastructure/
│           └── UserController.java
│
└── mi-proyecto-boot/                 # Módulo: Boot (Main class)
    ├── pom.xml
    │   └── <dependency>mi-proyecto-infrastructure</dependency>
    └── src/main/java/
        └── Application.java
```

### Parent POM (Raíz)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>mi-proyecto</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>  <!-- ← Importante: pom -->

    <!-- Módulos -->
    <modules>
        <module>mi-proyecto-domain</module>
        <module>mi-proyecto-application</module>
        <module>mi-proyecto-infrastructure</module>
        <module>mi-proyecto-boot</module>
    </modules>

    <!-- Versiones centralizadas -->
    <properties>
        <java.version>21</java.version>
        <spring-boot.version>3.5.7</spring-boot.version>
    </properties>

    <!-- Dependency Management (versiones compartidas) -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

### Module POM (Ejemplo: Domain)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <!-- Parent -->
    <parent>
        <groupId>com.example</groupId>
        <artifactId>mi-proyecto</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>mi-proyecto-domain</artifactId>
    <packaging>jar</packaging>

    <!-- Sin dependencies: Domain es Java puro -->
    <dependencies>
        <!-- Solo Lombok, nada de Spring -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

---

## Ventajas y Desventajas

### ✅ Ventajas

| Ventaja | Descripción |
|---------|-------------|
| **Separación física** | Imposible importar código de módulo no declarado |
| **Builds incrementales** | Maven compila solo módulos modificados |
| **Reutilización** | Un módulo puede usarse en múltiples apps |
| **Despliegue independiente** | Cada módulo puede ser un JAR/WAR separado |
| **Versionado independiente** | `domain-1.0.0`, `application-1.1.0` |
| **CI/CD optimizado** | Pipeline puede compilar solo módulos afectados |

### ❌ Desventajas

| Desventaja | Descripción |
|------------|-------------|
| **Complejidad** | Más `pom.xml` que mantener (4-5 archivos vs 1) |
| **Navegación en IDE** | Saltar entre módulos es menos fluido |
| **Refactoring** | Mover clase entre módulos requiere actualizar POMs |
| **Curva de aprendizaje** | Juniors tardan más en entender la estructura |
| **Overhead inicial** | Más tiempo setup inicial |
| **Dependencias circulares** | Fácil crear dependencias circulares por error |

---

## Alternativas

### Alternativa 1: Paquetes Java (como este proyecto)

**Estructura**:
```
src/main/java/com/example/hexarch/
├── user/
│   ├── domain/
│   ├── application/
│   └── infrastructure/
└── shared/
```

**Validación**: ArchUnit tests

**Ventajas**:
- ✅ Simple
- ✅ Fácil de navegar en IDE
- ✅ Refactoring fácil

**Desventajas**:
- ❌ No fuerza separación compile-time (ArchUnit lo hace en test-time)

### Alternativa 2: Monorepo con Múltiples Aplicaciones

**Estructura**:
```
monorepo/
├── apps/
│   ├── api-rest/
│   ├── api-graphql/
│   └── batch-jobs/
└── libs/
    ├── shared-domain/
    └── shared-utils/
```

**Herramientas**: Nx, Gradle

**Cuándo**: Múltiples aplicaciones que comparten código.

### Alternativa 3: Microservicios

**Estructura**:
```
repositorios separados:
- user-service/        (repo 1)
- order-service/       (repo 2)
- notification-service/ (repo 3)
```

**Cuándo**: Equipos independientes, deployment independiente, escalado independiente.

---

## Decisión: ¿Cuándo migrar de monolito a multimódulo?

### Checklist de Evaluación

Considera multimódulo si **3 o más** son verdaderas:

- [ ] Build tarda más de 5 minutos
- [ ] Tienes múltiples aplicaciones desplegables que comparten código
- [ ] Equipos diferentes trabajan en módulos claramente separados
- [ ] Necesitas publicar módulos como librerías independientes
- [ ] Quieres enforzar arquitectura compile-time (además de ArchUnit)
- [ ] Tienes más de 100 clases de producción
- [ ] Tu equipo entiende Maven multimódulo y está dispuesto a la complejidad

### Señales de que NO necesitas multimódulo

- ✅ Build tarda menos de 2 minutos
- ✅ Solo 1 aplicación desplegable
- ✅ Equipo pequeño (1-5 devs)
- ✅ ArchUnit ya valida tu arquitectura
- ✅ Menos de 50-100 clases de producción

**Para este proyecto**: NO necesitas multimódulo. La estructura actual con paquetes Java + ArchUnit es suficiente.

---

## Ejemplos del Mundo Real

### Proyecto Pequeño: Este Template (❌ No necesita multimódulo)
- 1 aplicación
- ~50 clases
- Build: 30 segundos
- **Decisión**: Paquetes Java + ArchUnit

### Proyecto Mediano: E-commerce (⚠️ Depende)
- 3 aplicaciones: Admin, API REST, Batch jobs
- ~200 clases
- Build: 2-3 minutos
- **Decisión**: Considerar multimódulo si las apps comparten mucho código

### Proyecto Grande: Netflix OSS (✅ Necesita multimódulo)
- 50+ librerías publicadas (Hystrix, Eureka, Ribbon, etc.)
- Miles de clases
- Build: 10+ minutos sin caché
- **Decisión**: Multimódulo obligatorio

---

## Conclusión

**Para este proyecto**: NO uses multimódulo. La estructura actual es apropiada.

**Cuándo considerar multimódulo**:
1. Múltiples apps desplegables
2. Quieres publicar librerías
3. Build muy lento
4. Equipos grandes

**Principio**: Empieza simple (paquetes Java), migra a multimódulo **solo si lo necesitas**.

**Recuerda**: Más arquitectura ≠ Mejor arquitectura. YAGNI (You Aren't Gonna Need It) aplica también a Maven multimódulo.

---

**Recursos**:
- [Maven Multi-Module Projects](https://maven.apache.org/guides/mini/guide-multiple-modules.html)
- [Gradle Multi-Project Builds](https://docs.gradle.org/current/userguide/multi_project_builds.html)
- [Nx Monorepo](https://nx.dev/)
