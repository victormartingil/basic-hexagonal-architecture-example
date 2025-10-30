# GitHub Actions - Workflows del Proyecto

## 📋 Índice
1. [Resumen de Workflows](#resumen-de-workflows)
2. [Tipos de Triggers](#tipos-de-triggers)
3. [Workflows Implementados](#workflows-implementados)
4. [Cómo Ejecutar Workflows Manualmente](#cómo-ejecutar-workflows-manualmente)
5. [Configuración y Costos](#configuración-y-costos)

---

## 1. Resumen de Workflows

Este proyecto tiene **5 workflows de GitHub Actions** configurados:

| Workflow | Trigger | Duración | Descripción |
|----------|---------|----------|-------------|
| **CI Tests** | PRs + Push main | ~2 min | Unit tests (sin Docker) |
| **Build** | PRs + Push main | ~3 min | Compilación y empaquetado |
| **Architecture** | PRs + Push main | ~1 min | Validación con ArchUnit |
| **Integration Tests** | PRs + Manual + Semanal | ~5 min | Tests con Testcontainers |
| **E2E Tests** | PRs + Manual | ~5 min | Tests E2E con Karate |

---

## 2. Tipos de Triggers

GitHub Actions permite ejecutar workflows automáticamente o manualmente:

### 2.1. Pull Request (PR)
```yaml
on:
  pull_request:
    branches: [ main ]
    paths:  # Opcional: solo si cambian estos archivos
      - 'src/**'
      - 'pom.xml'
```

**Cuándo se ejecuta:** Al crear o actualizar un PR hacia `main`

**Ejemplo:**
```bash
# 1. Crear branch y hacer cambios
git checkout -b feature/new-feature
git add .
git commit -m "Add new feature"
git push origin feature/new-feature

# 2. Crear PR en GitHub
# → Se ejecutan automáticamente: CI, Build, Architecture, Integration, E2E
```

**Workflows que usan esto:**
- ✅ CI Tests
- ✅ Build
- ✅ Architecture
- ✅ Integration Tests
- ✅ E2E Tests

---

### 2.2. Push to Branch
```yaml
on:
  push:
    branches: [ main ]
```

**Cuándo se ejecuta:** Al hacer push directo a `main` (o merge de PR)

**Ejemplo:**
```bash
# Merge del PR
git checkout main
git merge feature/new-feature
git push origin main
# → Se ejecutan: CI, Build, Architecture
```

**Workflows que usan esto:**
- ✅ CI Tests
- ✅ Build
- ✅ Architecture

---

### 2.3. Workflow Dispatch (Manual)
```yaml
on:
  workflow_dispatch:
    inputs:
      test_mode:
        type: choice
        options:
          - local
          - docker
```

**Cuándo se ejecuta:** Manualmente desde GitHub UI

**Cómo ejecutar:**
1. Ir a GitHub → **Actions**
2. Seleccionar el workflow (ej: "E2E Tests")
3. Click en **"Run workflow"**
4. Seleccionar opciones (si las hay)
5. Click en **"Run workflow"** (botón verde)

**Workflows que usan esto:**
- ✅ Integration Tests
- ✅ E2E Tests

---

### 2.4. Schedule (Cron)
```yaml
on:
  schedule:
    - cron: '0 3 * * 1'  # Cada lunes a las 3am UTC
```

**Cuándo se ejecuta:** Automáticamente según el schedule

**Formato cron:**
```
┌───────────── minuto (0 - 59)
│ ┌───────────── hora (0 - 23)
│ │ ┌───────────── día del mes (1 - 31)
│ │ │ ┌───────────── mes (1 - 12)
│ │ │ │ ┌───────────── día de la semana (0 - 6) (0 = domingo)
│ │ │ │ │
│ │ │ │ │
* * * * *
```

**Ejemplos comunes:**
- `0 3 * * 1` - Cada lunes a las 3am
- `0 0 * * *` - Diario a medianoche
- `0 */6 * * *` - Cada 6 horas

**Workflows que usan esto:**
- ✅ Integration Tests (semanal)

---

## 3. Workflows Implementados

### 3.1. CI Tests
**Archivo:** `.github/workflows/ci.yml`

```yaml
on:
  pull_request:
    branches: [ main ]
  push:
    branches: [ main ]
```

**Qué hace:**
1. Checkout código
2. Setup JDK 21
3. Ejecuta: `./mvnw test` (solo unit tests, sin Docker)
4. Sube reportes de tests

**Duración:** ~2 minutos

**Cuándo falla:**
- Algún unit test falla
- Errores de compilación

---

### 3.2. Build
**Archivo:** `.github/workflows/build.yml`

```yaml
on:
  pull_request:
    branches: [ main ]
  push:
    branches: [ main ]
```

**Qué hace:**
1. Checkout código
2. Setup JDK 21
3. Ejecuta: `./mvnw clean package -DskipTests`
4. Sube el JAR como artifact

**Duración:** ~3 minutos

**Cuándo falla:**
- Errores de compilación
- Problemas con MapStruct
- Problemas con OpenAPI Generator

---

### 3.3. Architecture
**Archivo:** `.github/workflows/architecture.yml`

```yaml
on:
  pull_request:
    branches: [ main ]
  push:
    branches: [ main ]
```

**Qué hace:**
1. Checkout código
2. Setup JDK 21
3. Ejecuta tests de ArchUnit
4. Valida arquitectura hexagonal

**Duración:** ~1 minuto

**Cuándo falla:**
- Domain importa clases de infrastructure
- Application depende de detalles de infraestructura
- Violaciones de naming conventions

**Tests ejecutados:**
- Domain debe estar aislado
- Application independiente de Infrastructure
- Flujo de dependencias correcto
- Naming conventions (Controllers, Services, Repositories)

---

### 3.4. Integration Tests
**Archivo:** `.github/workflows/integration-tests.yml`

```yaml
on:
  workflow_dispatch:      # Manual
  pull_request:           # PRs a main
    branches: [ main ]
  schedule:               # Semanal
    - cron: '0 3 * * 1'
```

**Qué hace:**
1. Checkout código
2. Setup JDK 21
3. Setup Docker Buildx
4. Ejecuta: `./mvnw test -Pintegration-tests`
   - Testcontainers levanta PostgreSQL y Kafka
   - Se ejecutan todos los `*IntegrationTest.java`

**Duración:** ~5 minutos

**Cuándo falla:**
- Tests de integración fallan
- Problemas con Testcontainers
- Problemas de conectividad con BD o Kafka

**Tests incluidos:**
- UserControllerIntegrationTest (10 tests)
- JpaUserRepositoryAdapterIntegrationTest
- KafkaConsumerIntegrationTest
- KafkaPublisherIntegrationTest
- SecurityIntegrationTest
- EmailServiceIntegrationTest

---

### 3.5. E2E Tests
**Archivo:** `.github/workflows/e2e-tests.yml`

```yaml
on:
  workflow_dispatch:      # Manual (con opciones)
    inputs:
      test_mode:
        type: choice
        options:
          - local       # Testcontainers (default)
          - docker      # Docker Compose
  pull_request:           # PRs a main (solo modo local)
    branches: [ main ]
```

**Dos modos de ejecución:**

#### Modo LOCAL (default, PRs)
1. Checkout código
2. Setup JDK 21
3. Setup Docker
4. Ejecuta: `./mvnw test -Pe2e-tests`
   - Testcontainers levanta PostgreSQL y Kafka
   - @SpringBootTest levanta la app en puerto aleatorio
   - Karate ejecuta tests E2E contra `http://localhost:{port}`

**Duración:** ~5 minutos

#### Modo DOCKER (manual)
1. Checkout código
2. Setup JDK 21
3. Levanta docker-compose (app + PostgreSQL + Kafka + Zipkin)
4. Build y start de la aplicación
5. Ejecuta: `./mvnw test -Pe2e-tests-docker`
   - Karate ejecuta tests contra `http://localhost:8080`

**Duración:** ~8-10 minutos

**Cuándo falla:**
- Scenarios de Karate fallan
- Problemas de conectividad HTTP
- Timeout esperando que la app arranque

---

## 4. Cómo Ejecutar Workflows Manualmente

### 4.1. Desde GitHub UI

1. **Ir a GitHub:**
   - Abre tu repositorio en GitHub
   - Click en pestaña **"Actions"**

2. **Seleccionar workflow:**
   - En el panel izquierdo, verás la lista de workflows
   - Click en el workflow que quieras ejecutar (ej: "E2E Tests")

3. **Ejecutar:**
   - Click en botón **"Run workflow"** (arriba a la derecha)
   - Selecciona el branch (usualmente `main`)
   - Selecciona opciones si las hay (ej: test_mode = local/docker)
   - Click en botón verde **"Run workflow"**

4. **Ver resultados:**
   - El workflow aparecerá en la lista
   - Click en él para ver los logs en tiempo real
   - ✅ Verde = éxito
   - ❌ Rojo = falló

### 4.2. Desde CLI con GitHub CLI

Instalar GitHub CLI:
```bash
# macOS
brew install gh

# Login
gh auth login
```

Ejecutar workflow:
```bash
# Ver workflows disponibles
gh workflow list

# Ejecutar workflow
gh workflow run "E2E Tests"

# Ejecutar con inputs
gh workflow run "E2E Tests" -f test_mode=docker

# Ver status
gh run list
gh run view <run-id>
```

---

## 5. Configuración y Costos

### 5.1. Límites de GitHub Actions

| Plan | Minutos/mes | Almacenamiento |
|------|-------------|----------------|
| **Free** (público) | Ilimitado ✅ | 500 MB |
| **Free** (privado) | 2000 min | 500 MB |
| **Pro** | 3000 min | 1 GB |
| **Team** | 3000 min | 2 GB |
| **Enterprise** | 50000 min | 50 GB |

### 5.2. Consumo Estimado de Este Proyecto

**Por PR (con todos los workflows):**
- CI Tests: 2 min
- Build: 3 min
- Architecture: 1 min
- Integration Tests: 5 min
- E2E Tests: 5 min
- **Total: ~16 minutos por PR**

**Con 10 PRs al mes:** 160 minutos (~8% de 2000)

### 5.3. Optimización de Costos

**Workflows que se ejecutan automáticamente en PRs:**
- ✅ CI Tests (rápido, esencial)
- ✅ Build (rápido, esencial)
- ✅ Architecture (rápido, esencial)
- ✅ Integration Tests (lento pero importante)
- ✅ E2E Tests (lento pero importante)

**Estrategias para ahorrar minutos:**

1. **Usar cache de Maven:**
   ```yaml
   - uses: actions/setup-java@v4
     with:
       cache: 'maven'  # ✅ Cachea ~/.m2/repository
   ```

2. **Filtrar por paths:**
   ```yaml
   pull_request:
     paths:
       - 'src/**'
       - 'pom.xml'
     # No se ejecuta si solo cambias README.md
   ```

3. **Desactivar workflows opcionales:**
   - Comentar `pull_request:` trigger
   - Dejar solo `workflow_dispatch:` para manual

4. **Usar concurrency:**
   ```yaml
   concurrency:
     group: ${{ github.workflow }}-${{ github.ref }}
     cancel-in-progress: true  # Cancela runs viejos
   ```

---

## 6. Testcontainers vs Docker Compose en CI/CD

### 6.1. Resumen

Este proyecto soporta **dos enfoques** para ejecutar tests que requieren infraestructura (PostgreSQL, Kafka):

| Enfoque | Uso | Configuración | Velocidad | Gratis en GitHub |
|---------|-----|---------------|-----------|------------------|
| **Testcontainers** | Integration & E2E | Automática | ⚡ Rápido (5 min) | ✅ Sí |
| **Docker Compose** | E2E (validación final) | Manual | 🐢 Lento (8-10 min) | ✅ Sí |

### 6.2. Testcontainers (Recomendado)

**¿Qué es?**
- Librería Java que levanta contenedores Docker automáticamente
- Los contenedores se crean/destruyen durante la ejecución de tests
- No requiere docker-compose.yml

**Ventajas:**
- ✅ **Todo automático**: Un solo comando (`./mvnw test`)
- ✅ **Puertos aleatorios**: No hay conflictos
- ✅ **Auto-cleanup**: Contenedores se eliminan solos
- ✅ **Gratis en GitHub Actions**: Docker viene preinstalado
- ✅ **Rápido**: ~5 minutos (vs ~10 min Docker Compose)
- ✅ **No requiere setup manual**: No más 3 terminales

**Desventajas:**
- ❌ No valida la imagen Docker final (solo código Java)
- ❌ Requiere Java localmente

**Ejemplo en código:**
```java
@SpringBootTest
@Testcontainers
class MyIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void myTest() {
        // Testcontainers ya levantó PostgreSQL automáticamente
    }
}
```

**Workflows que lo usan:**
- ✅ Integration Tests (`.github/workflows/integration-tests.yml`)
- ✅ E2E Tests - Modo LOCAL (`.github/workflows/e2e-tests.yml`)

**Comandos:**
```bash
# Integration tests
./mvnw test -Pintegration-tests

# E2E tests
./mvnw test -Pe2e-tests
```

**Arquitectura:**
```
┌────────────────────────────────────────────────────────────┐
│ TESTCONTAINERS MODE                                        │
├────────────────────────────────────────────────────────────┤
│ Test Runner (JUnit)                                        │
│ └─ @SpringBootTest (puerto aleatorio)                     │
│    └─ Conecta a contenedores Testcontainers               │
├────────────────────────────────────────────────────────────┤
│ Testcontainers (gestiona ciclo de vida)                   │
│ ├─ PostgreSQL Container (puerto aleatorio)                │
│ └─ Kafka Container (puerto aleatorio)                     │
└────────────────────────────────────────────────────────────┘
```

---

### 6.3. Docker Compose

**¿Qué es?**
- Stack completo de contenedores definido en `docker-compose.yml`
- Incluye la aplicación empaquetada como imagen Docker
- Requiere build de la imagen Docker

**Ventajas:**
- ✅ **Validación de imagen Docker**: Testea el JAR empaquetado
- ✅ **Entorno idéntico a producción**: Networking, volúmenes, etc.
- ✅ **No requiere Java local**: Solo Docker
- ✅ **Observabilidad completa**: Incluye Zipkin, Prometheus, Grafana

**Desventajas:**
- ❌ **Lento**: Build de imagen (~3 min) + startup (~2 min)
- ❌ **Setup manual**: Requiere múltiples comandos
- ❌ **Conflictos de puertos**: Puerto 8080 debe estar libre
- ❌ **Debugging complejo**: Logs en contenedores

**Workflow que lo usa:**
- ✅ E2E Tests - Modo DOCKER (`.github/workflows/e2e-tests.yml`)

**Comandos:**
```bash
# 1. Build de la aplicación
./mvnw clean package -DskipTests

# 2. Build de la imagen Docker
docker build -t hexarch:latest .

# 3. Levantar stack completo
docker-compose up -d

# 4. Ejecutar E2E tests
./mvnw test -Pe2e-tests-docker
```

**Arquitectura:**
```
┌────────────────────────────────────────────────────────────┐
│ DOCKER COMPOSE MODE                                        │
├────────────────────────────────────────────────────────────┤
│ Test Runner (Karate - fuera de Docker)                    │
│ └─ HTTP → http://localhost:8080                           │
├────────────────────────────────────────────────────────────┤
│ Docker Compose (docker-compose.yml)                       │
│ ├─ hexarch-app       (puerto 8080)                        │
│ ├─ postgres          (puerto 5432)                        │
│ ├─ kafka             (puerto 9092)                        │
│ └─ zipkin            (puerto 9411)                        │
└────────────────────────────────────────────────────────────┘
```

---

### 6.4. ¿Cuándo usar cada uno?

#### Usa Testcontainers si:
- ✅ Desarrollo día a día
- ✅ CI/CD (GitHub Actions, GitLab CI, Jenkins)
- ✅ Pull Request validation
- ✅ Quieres feedback rápido
- ✅ No te importa validar la imagen Docker

#### Usa Docker Compose si:
- ✅ Validación pre-release
- ✅ Testing de observabilidad (Zipkin, métricas)
- ✅ Debugging de networking entre servicios
- ✅ Demo/presentación del sistema completo
- ✅ Validación de imagen Docker final

---

### 6.5. Comparación de Rendimiento

**Test:** Ejecutar Integration Tests + E2E Tests

| Métrica | Testcontainers | Docker Compose |
|---------|----------------|----------------|
| Setup inicial | 30-40s | 5-8 min |
| Tests execution | 3-4 min | 2-3 min |
| **Total** | **~5 min** | **~10 min** |
| Cleanup | Automático | Manual |
| Uso en GitHub Actions | Gratis ✅ | Gratis ✅ |

**Conclusión:** Testcontainers es 2x más rápido y requiere menos configuración.

---

### 6.6. Configuración en Este Proyecto

#### Integration Tests (Testcontainers)
- **Profile Maven:** `integration-tests`
- **Comando:** `./mvnw test -Pintegration-tests`
- **Workflow:** `.github/workflows/integration-tests.yml`
- **Tests:** `**/IntegrationTest.java`
- **Trigger:** PRs a main + Manual + Semanal

#### E2E Tests - Modo Local (Testcontainers)
- **Profile Maven:** `e2e-tests`
- **Comando:** `./mvnw test -Pe2e-tests`
- **Workflow:** `.github/workflows/e2e-tests.yml` (modo: local)
- **Tests:** `KarateE2ETestcontainersTest.java`
- **Trigger:** PRs a main + Manual

#### E2E Tests - Modo Docker (Docker Compose)
- **Profile Maven:** `e2e-tests-docker`
- **Comando:** `./mvnw test -Pe2e-tests-docker`
- **Workflow:** `.github/workflows/e2e-tests.yml` (modo: docker)
- **Tests:** `KarateE2EDockerTest.java`
- **Trigger:** Solo manual

---

### 6.7. Recomendaciones

**Para CI/CD (GitHub Actions):**
```yaml
# ✅ RECOMENDADO: Testcontainers
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - run: ./mvnw test -Pintegration-tests
      - run: ./mvnw test -Pe2e-tests
```

**Para validación pre-release:**
```yaml
# ✅ RECOMENDADO: Docker Compose
jobs:
  e2e-full:
    runs-on: ubuntu-latest
    steps:
      - run: docker-compose up -d
      - run: ./mvnw test -Pe2e-tests-docker
```

**Para desarrollo local:**
```bash
# Opción 1: Testcontainers (más rápido)
./mvnw test -Pintegration-tests

# Opción 2: Docker Compose (más completo)
docker-compose up -d
./mvnw spring-boot:run
# En otra terminal:
./mvnw test -Dtest=KarateE2ELocalTest
```

---

## 7. Troubleshooting

### Problema: Workflow no se ejecuta en PR
**Síntomas:** PR creado pero no aparecen checks

**Causas comunes:**
1. El trigger no incluye `pull_request:`
2. El path filter excluye tus cambios
3. El workflow está en una branch diferente

**Solución:**
```yaml
# Verificar que el workflow tenga:
on:
  pull_request:
    branches: [ main ]
```

### Problema: Integration tests fallan con "ryuk"
**Síntomas:**
```
Container startup failed for image testcontainers/ryuk:0.12.0
```

**Solución:**
- En local: Ya está configurado (`.testcontainers.properties`)
- En GitHub Actions: No debería pasar (Docker viene configurado)

### Problema: E2E tests timeout
**Síntomas:**
```
Timeout waiting for application to start
```

**Solución:**
1. Aumentar timeout en workflow
2. Verificar logs de la aplicación
3. Asegurarse que el puerto está correcto

### Problema: Workflows consumen muchos minutos
**Síntomas:** Se agotan los 2000 minutos/mes

**Soluciones:**
1. Desactivar E2E en PRs (dejar solo manual)
2. Combinar workflows (ejecutar todo en un solo job)
3. Usar self-hosted runners (gratis pero requiere servidor)

---

## 7. Referencias

### Archivos de Workflows
- `.github/workflows/ci.yml`
- `.github/workflows/build.yml`
- `.github/workflows/architecture.yml`
- `.github/workflows/integration-tests.yml`
- `.github/workflows/e2e-tests.yml`

### Documentación Externa
- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Workflow Syntax](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions)
- [GitHub Actions Pricing](https://docs.github.com/en/billing/managing-billing-for-github-actions/about-billing-for-github-actions)

### Documentación Relacionada del Proyecto
- `docs/12-Architecture-Tests-ArchUnit.md` - Tests de arquitectura
- `docs/13-E2E-Testing-Karate.md` - Tests E2E con Karate
- `README.md` - Badges de workflows

---

**Última actualización:** 2025-10-30
