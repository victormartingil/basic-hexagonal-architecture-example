# GitHub Actions Workflows

Este directorio contiene los workflows de CI/CD para el proyecto.

## Workflows Activos

El proyecto incluye **4 workflows principales** que se ejecutan automáticamente:

1. 🧪 **CI Tests** - Tests unitarios y de arquitectura (rápido, sin Docker)
2. 🏗️ **Build** - Compilación y generación de JAR
3. 🏛️ **Architecture** - Validación de reglas arquitecturales con ArchUnit
4. 🐳 **Integration Tests** - Tests de integración con Testcontainers

## Workflows Opcionales

5. 📊 **SonarCloud** - Análisis de calidad (DESHABILITADO por defecto, requiere cuenta)

---

## Workflows Detallados

### 1. 🧪 CI - Unit & Architecture Tests (`ci.yml`)

**Cuándo se ejecuta:**
- Push a `main` o `develop`
- Pull Requests hacia `main` o `develop`

**Qué hace:**
- Ejecuta tests unitarios (10 tests)
- Ejecuta tests de arquitectura con ArchUnit (21 tests)
- No requiere Docker
- Tiempo estimado: 1-2 minutos

**Badge:**
```markdown
![CI Tests](https://github.com/USERNAME/REPO/actions/workflows/ci.yml/badge.svg)
```

---

### 2. 🐳 Integration Tests (`integration-tests.yml`)

**Cuándo se ejecuta:**
- Manualmente desde GitHub UI
- Pull Requests hacia `main`
- Semanalmente (lunes a las 3am UTC)

**Qué hace:**
- Ejecuta TODOS los tests (unit + integration + architecture)
- Usa Testcontainers para levantar PostgreSQL
- Requiere Docker
- Tiempo estimado: 3-5 minutos

**Badge:**
```markdown
![Integration Tests](https://github.com/USERNAME/REPO/actions/workflows/integration-tests.yml/badge.svg)
```

**Cómo ejecutar manualmente:**
1. Ve a la pestaña "Actions" en GitHub
2. Selecciona "Integration Tests (with Docker)"
3. Click en "Run workflow"
4. Selecciona la rama y click en "Run workflow"

---

### 3. 🏗️ Build & Verify (`build.yml`)

**Cuándo se ejecuta:**
- Push a `main` o `develop`
- Pull Requests hacia `main` o `develop`

**Qué hace:**
- Compila el proyecto completo
- Genera el JAR ejecutable
- Verifica que el build sea exitoso
- Sube el JAR como artifact (disponible 7 días)
- Tiempo estimado: 1-2 minutos

**Badge:**
```markdown
![Build](https://github.com/USERNAME/REPO/actions/workflows/build.yml/badge.svg)
```

---

### 4. 🏛️ Architecture Validation (`architecture.yml`)

**Cuándo se ejecuta:**
- Push a `main` o `develop`
- Pull Requests hacia `main` o `develop`

**Qué hace:**
- Ejecuta solo los tests de ArchUnit
- Valida las 21 reglas de arquitectura hexagonal
- Genera reporte detallado de reglas validadas
- Tiempo estimado: 30-60 segundos

**Badge:**
```markdown
![Architecture](https://github.com/USERNAME/REPO/actions/workflows/architecture.yml/badge.svg)
```

---

### 5. 📊 SonarCloud Analysis (`sonarcloud.yml.disabled`) - OPCIONAL

> **⚠️ WORKFLOW DESHABILITADO POR DEFECTO**
>
> Este workflow está deshabilitado porque requiere cuenta en SonarCloud y configuración de secrets.
> Es **OPCIONAL** para aprender arquitectura hexagonal.

**Estado:** ❌ Deshabilitado (archivo renombrado a `.disabled`)

**¿Por qué está deshabilitado?**
- Requiere cuenta gratuita en SonarCloud
- Requiere configuración de secrets en GitHub
- NO es necesario para aprender arquitectura hexagonal
- JaCoCo (incluido) ya proporciona análisis de cobertura local

**¿Cuándo habilitarlo?**
- ✅ Si quieres aprender herramientas empresariales
- ✅ Si quieres mostrar métricas en tu portfolio
- ✅ Si necesitas análisis automático de calidad en PRs

**Cómo habilitarlo:**
Ver guía completa en [`SONARCLOUD_SETUP.md`](SONARCLOUD_SETUP.md)

**Resumen rápido:**
1. Crear cuenta en https://sonarcloud.io
2. Configurar secrets en GitHub (SONAR_TOKEN, etc.)
3. Renombrar `sonarcloud.yml.disabled` → `sonarcloud.yml`
4. Descomentar triggers en el workflow
5. Push para activar

---

## Estrategia de CI/CD

### Pull Requests (PRs)

Cuando se crea un PR, se ejecutan automáticamente:
- ✅ **CI Tests** (rápido, sin Docker)
- ✅ **Build** (verifica que compile)
- ✅ **Architecture** (valida reglas)
- ✅ **Integration Tests** (solo PRs a `main`)

### Push a Main/Develop

En cada push se ejecutan:
- ✅ **CI Tests**
- ✅ **Build**
- ✅ **Architecture**

### Ejecución Manual

Puedes ejecutar manualmente:
- 🐳 **Integration Tests** (requiere Docker)

### Ejecución Programada

- 📅 **Integration Tests** se ejecutan semanalmente (lunes 3am UTC)

---

## Configuración Local

Para replicar los workflows localmente:

```bash
# CI Tests (rápido, sin Docker)
./mvnw test

# Integration Tests (requiere Docker)
./mvnw test -Pintegration-tests

# Build
./mvnw clean install

# Architecture only
./mvnw test -Dtest=HexagonalArchitectureTest
```

---

## Badges en el README

Añade estos badges al README principal:

```markdown
[![CI Tests](https://github.com/USERNAME/REPO/actions/workflows/ci.yml/badge.svg)](https://github.com/USERNAME/REPO/actions/workflows/ci.yml)
[![Integration Tests](https://github.com/USERNAME/REPO/actions/workflows/integration-tests.yml/badge.svg)](https://github.com/USERNAME/REPO/actions/workflows/integration-tests.yml)
[![Build](https://github.com/USERNAME/REPO/actions/workflows/build.yml/badge.svg)](https://github.com/USERNAME/REPO/actions/workflows/build.yml)
[![Architecture](https://github.com/USERNAME/REPO/actions/workflows/architecture.yml/badge.svg)](https://github.com/USERNAME/REPO/actions/workflows/architecture.yml)
```

**Reemplaza:**
- `USERNAME` con tu usuario de GitHub
- `REPO` con el nombre del repositorio

---

## Troubleshooting

### Los tests fallan en GitHub Actions pero pasan localmente

1. **Verificar versión de Java:** El workflow usa Java 21 (Temurin)
2. **Limpiar caché de Maven:** Elimina `.m2/repository` localmente
3. **Verificar variables de entorno:** Los workflows no tienen variables de entorno secretas

### Integration Tests fallan con error de Docker

Los workflows de GitHub Actions tienen Docker disponible por defecto.
Si fallan, puede ser por:
- Timeout de Testcontainers (aumentar en `testcontainers.properties`)
- Rate limit de Docker Hub (considerar usar GitHub Container Registry)

### Build falla en GitHub Actions

1. **Verificar OpenAPI spec:** `src/main/resources/openapi/user-api.yaml` debe ser válido
2. **Verificar dependencias:** Todas las dependencias deben estar en Maven Central
3. **Logs detallados:** Añade `-X` al comando Maven en el workflow

---

## Análisis de Calidad Alternativo (sin SonarCloud)

Si prefieres análisis local sin cuenta:

**JaCoCo** (ya incluido):
```bash
./mvnw clean test
open target/site/jacoco/index.html
```

**Otras herramientas** (añadir al pom.xml):
- SpotBugs: Detecta bugs comunes
- PMD: Análisis de código estático
- Checkstyle: Verificación de estilo de código

---

## Mejoras Futuras

- [ ] Añadir workflow de release automático
- [ ] Despliegue automático a staging/production
- [ ] Notificaciones a Slack/Discord
- [ ] Performance testing con JMeter/Gatling
- [ ] Security scanning con Dependabot
