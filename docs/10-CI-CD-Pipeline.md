# 🚀 CI/CD Pipeline - GitHub Actions

## Índice
- [Overview](#overview)
- [Workflows Disponibles](#workflows-disponibles)
- [Diagrama de Ejecución](#diagrama-de-ejecución)
- [Configuración](#configuración)
- [Añadir Nuevos Checks](#añadir-nuevos-checks)
- [Troubleshooting](#troubleshooting)

---

## Overview

Este proyecto usa **GitHub Actions** para automatizar validaciones de calidad en cada push/PR.

**Beneficios**:
- ✅ Detectar errores antes de merge
- ✅ Validar arquitectura automáticamente
- ✅ Garantizar cobertura de tests (85%+)
- ✅ Ejecutar tests de integración con Testcontainers
- ✅ Prevenir regresiones

---

## Workflows Disponibles

### 1. **CI Tests** (`.github/workflows/ci.yml`)

**Cuándo se ejecuta**:
- ✅ Cada `push` a cualquier rama
- ✅ Cada `pull request`

**Qué hace**:
```yaml
1. Checkout código
2. Setup Java 21
3. Cache Maven dependencies
4. mvnw clean test
   ├─ Unit tests (79 tests)
   ├─ JaCoCo coverage (min 85%)
   └─ Fail si coverage < 85%
```

**Duración**: ~2-3 minutos

**Badge**:
```markdown
[![CI Tests](https://github.com/USER/REPO/actions/workflows/ci.yml/badge.svg)](https://github.com/USER/REPO/actions/workflows/ci.yml)
```

---

### 2. **Build** (`.github/workflows/build.yml`)

**Cuándo se ejecuta**:
- ✅ Cada `push` a cualquier rama
- ✅ Cada `pull request`

**Qué hace**:
```yaml
1. Checkout código
2. Setup Java 21
3. Cache Maven dependencies
4. mvnw clean compile
   ├─ Compila código fuente
   ├─ Genera código OpenAPI
   └─ Verifica dependencias
```

**Duración**: ~1-2 minutos

**Fallo común**: Error de compilación (typos, imports incorrectos)

---

### 3. **Architecture Tests** (`.github/workflows/architecture.yml`)

**Cuándo se ejecuta**:
- ✅ Cada `push` a cualquier rama
- ✅ Cada `pull request`

**Qué hace**:
```yaml
1. Checkout código
2. Setup Java 21
3. Cache Maven dependencies
4. mvnw test -Dtest=HexagonalArchitectureTest
   ├─ Valida 21 reglas arquitecturales
   ├─ Domain sin dependencias de infra
   ├─ Naming conventions
   ├─ Package structure
   └─ Fail si se viola arquitectura
```

**Duración**: ~1 minuto

**Reglas validadas**:
- Domain no debe depender de Infrastructure
- Services deben implementar Use Cases
- Repositories deben estar en output ports
- Controllers deben estar en input adapters
- Value Objects deben ser inmutables
- Entities deben tener factory methods

---

### 4. **Integration Tests** (`.github/workflows/integration-tests.yml`)

**Cuándo se ejecuta**:
- ✅ Cada `push` a cualquier rama
- ✅ Cada `pull request`

**Qué hace**:
```yaml
1. Checkout código
2. Setup Java 21
3. Setup Docker (para Testcontainers)
4. Cache Maven dependencies
5. mvnw verify
   ├─ Levanta PostgreSQL (Testcontainers)
   ├─ Levanta Kafka (Testcontainers)
   ├─ Ejecuta integration tests (16 tests)
   └─ Cleanup contenedores
```

**Duración**: ~5-7 minutos (más lento por Docker)

**Tests ejecutados**:
- Kafka Consumer Integration (DLT, retry, fallback)
- Security Integration (JWT, roles, 401/403)
- Database Integration (PostgreSQL con Flyway)

---

## Diagrama de Ejecución

```
┌─────────────────────────────────────────────────────────────┐
│                    Developer Workflow                        │
└─────────────────────────────────────────────────────────────┘
                               │
                               │ git push origin feature-branch
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      GitHub Actions                          │
└─────────────────────────────────────────────────────────────┘
                               │
                               ├──► [1] CI Tests (2-3 min)
                               │    └─ Unit tests + coverage
                               │
                               ├──► [2] Build (1-2 min)
                               │    └─ Compilation check
                               │
                               ├──► [3] Architecture Tests (1 min)
                               │    └─ ArchUnit validation
                               │
                               └──► [4] Integration Tests (5-7 min)
                                    └─ Testcontainers + E2E
                               │
                               ▼
                        ┌─────────────┐
                        │ ALL PASS ✅ │
                        └─────────────┘
                               │
                               ▼
                     PR Ready to Merge 🎉
```

---

## Configuración

### Secrets Requeridos

**Ninguno** (por ahora)

Este proyecto NO requiere secrets porque:
- Tests usan Testcontainers (PostgreSQL/Kafka ephemeral)
- No se conecta a servicios externos
- No se despliega automáticamente

**Si añades deployment**:
```yaml
# Settings > Secrets and variables > Actions > New repository secret

DOCKER_USERNAME=myuser
DOCKER_PASSWORD=xxx
AWS_ACCESS_KEY_ID=xxx
AWS_SECRET_ACCESS_KEY=xxx
```

### Variables de Entorno

Configuradas en workflows:
```yaml
env:
  JAVA_VERSION: 21
  MAVEN_OPTS: "-Xmx512m"
```

---

## Añadir Nuevos Checks

### Ejemplo 1: Añadir Linter (Checkstyle)

**1. Crear workflow** `.github/workflows/lint.yml`:
```yaml
name: Lint

on: [push, pull_request]

jobs:
  checkstyle:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: 21
          distribution: 'temurin'

      - name: Run Checkstyle
        run: ./mvnw checkstyle:check
```

**2. Actualizar README**:
```markdown
[![Lint](https://github.com/USER/REPO/actions/workflows/lint.yml/badge.svg)](...)
```

---

### Ejemplo 2: Añadir Dependency Check (OWASP)

**1. Añadir plugin a `pom.xml`**:
```xml
<plugin>
  <groupId>org.owasp</groupId>
  <artifactId>dependency-check-maven</artifactId>
  <version>9.0.0</version>
</plugin>
```

**2. Crear workflow** `.github/workflows/security.yml`:
```yaml
name: Security Scan

on:
  schedule:
    - cron: '0 2 * * 1'  # Weekly on Monday 2 AM
  push:
    branches: [main]

jobs:
  dependency-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: 21
          distribution: 'temurin'

      - name: OWASP Dependency Check
        run: ./mvnw dependency-check:check

      - name: Upload Report
        uses: actions/upload-artifact@v4
        with:
          name: dependency-check-report
          path: target/dependency-check-report.html
```

---

### Ejemplo 3: Auto-Deploy a Staging

**1. Crear workflow** `.github/workflows/deploy-staging.yml`:
```yaml
name: Deploy to Staging

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4

      - name: Build Docker Image
        run: docker build -t hexarch:${{ github.sha }} .

      - name: Push to Registry
        run: |
          echo "${{ secrets.DOCKER_PASSWORD }}" | docker login -u "${{ secrets.DOCKER_USERNAME }}" --password-stdin
          docker push hexarch:${{ github.sha }}

      - name: Deploy to Kubernetes
        run: |
          kubectl set image deployment/hexarch hexarch=hexarch:${{ github.sha }}
```

---

## Troubleshooting

### Error: "Tests Failed"

**Síntoma**:
```
[ERROR] Tests run: 100, Failures: 5, Errors: 0
```

**Solución**:
1. Ver logs del workflow en GitHub Actions
2. Ejecutar localmente: `./mvnw test`
3. Fix tests
4. Push again

---

### Error: "Coverage Below Threshold"

**Síntoma**:
```
[ERROR] Coverage check failed: 78% < 85%
```

**Solución**:
1. Ver reporte JaCoCo: `target/site/jacoco/index.html`
2. Identificar clases sin coverage
3. Añadir tests
4. Verificar: `./mvnw clean verify`

---

### Error: "Architecture Test Failed"

**Síntoma**:
```
[ERROR] Architecture Violation: Domain depends on Infrastructure
```

**Solución**:
1. Ver stack trace: cuál clase viola la regla
2. Refactorizar:
   - Mover clase a package correcto
   - Eliminar imports prohibidos
   - Usar interfaces/ports
3. Verificar: `./mvnw test -Dtest=HexagonalArchitectureTest`

---

### Error: "Testcontainers Failed to Start"

**Síntoma**:
```
[ERROR] Could not start container postgres:16-alpine
```

**Solución**:
GitHub Actions tiene Docker pre-instalado, pero:
1. Verificar que workflow tenga `runs-on: ubuntu-latest`
2. No usar `services:` (Testcontainers maneja esto)
3. Aumentar timeout si es lento:
   ```yaml
   - name: Integration Tests
     run: ./mvnw verify
     timeout-minutes: 15
   ```

---

## Best Practices

### 1. Fail Fast

Ejecutar checks rápidos primero:
```
1. Build (1 min) ──► Si falla, no ejecutar tests
2. Architecture (1 min) ──► Si falla, no ejecutar integration
3. Unit Tests (3 min)
4. Integration Tests (7 min) ──► Solo si todo lo anterior pasa
```

### 2. Cache Dependencies

```yaml
- name: Cache Maven packages
  uses: actions/cache@v4
  with:
    path: ~/.m2
    key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
```

**Ahorro**: 30-60 segundos por run

### 3. Matrix Testing (Opcional)

Probar en múltiples versiones de Java:
```yaml
strategy:
  matrix:
    java: [21, 22, 23]
steps:
  - uses: actions/setup-java@v4
    with:
      java-version: ${{ matrix.java }}
```

### 4. Slack Notifications (Opcional)

```yaml
- name: Notify Slack
  if: failure()
  uses: slackapi/slack-github-action@v1
  with:
    payload: |
      {
        "text": "❌ Build failed on ${{ github.repository }}"
      }
  env:
    SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK }}
```

---

## Métricas

### Tiempos de Ejecución Típicos

| Workflow | Tiempo | Cache Hit | Cache Miss |
|----------|--------|-----------|------------|
| Build | 1-2 min | 45s | 1m 30s |
| CI Tests | 2-3 min | 1m 20s | 2m 45s |
| Architecture | 1 min | 30s | 1m 10s |
| Integration | 5-7 min | 4m 30s | 7m 15s |

**Total parallel**: ~7 minutos (el más lento)

### Success Rate

Objetivo: **95%+ success rate**

Si < 95%:
- Investigar tests flaky (intermittentes)
- Mejorar configuración Testcontainers
- Aumentar timeouts si es infraestructura lenta

---

## Recursos

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Testcontainers in CI](https://www.testcontainers.org/supported_docker_environment/continuous_integration/)
- [Maven CI Best Practices](https://maven.apache.org/guides/mini/guide-using-ci-friendly-versions.html)
