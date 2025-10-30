# Guía de Conventional Commits

## 📚 Índice

1. [¿Qué son Conventional Commits?](#qué-son-conventional-commits)
2. [Formato Básico](#formato-básico)
3. [Tipos de Commits](#tipos-de-commits)
4. [Integración con JIRA](#integración-con-jira)
5. [Workflow del Equipo](#workflow-del-equipo)
6. [Ejemplos Prácticos](#ejemplos-prácticos)
7. [Métricas DORA](#métricas-dora)
8. [Automatización](#automatización)
9. [Mejores Prácticas](#mejores-prácticas)

---

## ¿Qué son Conventional Commits?

**Conventional Commits** es una convención para escribir mensajes de commit **claros, estructurados y automatizables**.

### ¿Por qué usarlos?

❌ **Sin convención:**
```bash
git commit -m "fix stuff"
git commit -m "updates"
git commit -m "asdfasdf"
git commit -m "final version"
git commit -m "final version 2"
```

**Problemas:**
- Imposible saber qué cambió sin leer el código
- No se puede generar changelog automáticamente
- No se puede identificar breaking changes
- Dificulta code review
- No se puede rastrear qué ticket de JIRA se completó

✅ **Con Conventional Commits:**
```bash
git commit -m "feat(user): [CDOCK-16] add email validation"
git commit -m "fix(kafka): [CDOCK-23] resolve consumer offset reset issue"
git commit -m "docs(ddd): [CDOCK-24] add circuit breaker documentation"
```

**Ventajas:**
- ✅ Historial de commits legible
- ✅ Changelog automático (semantic versioning)
- ✅ Breaking changes identificables
- ✅ Code review más fácil
- ✅ Trazabilidad con JIRA
- ✅ Métricas DORA automatizables

---

## Formato Básico

### Estructura Completa

```
<type>(<scope>): [JIRA-TICKET] <subject>

<body>

<footer>
```

**¿Por qué el ticket después del scope?**
- ✅ **Ticket siempre visible** en `git log --oneline` (primeros 50 chars)
- ✅ **Mantiene scope técnico** (user, kafka, api) para organización
- ✅ **Compatible con JIRA/GitHub** (parsean `[TICKET-123]` en cualquier posición)
- ✅ **Fácil búsqueda**: `git log --grep="CDOCK-16"`

### Componentes

| Componente | Obligatorio | Descripción | Ejemplo |
|------------|-------------|-------------|---------|
| `type` | ✅ Sí | Tipo de cambio | `feat`, `fix`, `docs` |
| `scope` | ⚠️ Recomendado | Área del código afectada | `user`, `kafka`, `api` |
| `JIRA-TICKET` | ✅ Sí (para este equipo) | ID del ticket de JIRA | `[CDOCK-16]` |
| `subject` | ✅ Sí | Descripción corta (max 50 chars) | `add email validation` |
| `body` | ❌ Opcional | Descripción detallada | Párrafos explicativos |
| `footer` | ❌ Opcional | Breaking changes, referencias | `BREAKING CHANGE: ...` |

---

## Tipos de Commits

### Tipos Principales

| Tipo | Uso | Genera release | Ejemplo |
|------|-----|----------------|---------|
| `feat` | Nueva funcionalidad | 📦 MINOR | `feat(user): [CDOCK-16] add password reset` |
| `fix` | Corrección de bug | 🐛 PATCH | `fix(kafka): [CDOCK-23] resolve null pointer` |
| `docs` | Documentación | ❌ No | `docs(readme): [CDOCK-24] update setup guide` |
| `style` | Formato de código (sin cambio lógico) | ❌ No | `style(user): [CDOCK-25] format code with prettier` |
| `refactor` | Refactorización (sin cambio funcional) | ❌ No | `refactor(service): [CDOCK-26] extract email logic` |
| `test` | Tests (nuevos o modificados) | ❌ No | `test(user): [CDOCK-27] add unit tests for validation` |
| `chore` | Tareas de mantenimiento | ❌ No | `chore(deps): [CDOCK-28] upgrade spring boot to 3.5.8` |
| `perf` | Mejora de performance | 📦 PATCH | `perf(db): [CDOCK-29] add index to users table` |
| `ci` | Cambios en CI/CD | ❌ No | `ci(github): [CDOCK-30] add deployment workflow` |
| `build` | Cambios en build system | ❌ No | `build(maven): [CDOCK-31] update compiler plugin` |

### Semantic Versioning

Conventional Commits sigue **Semantic Versioning** (SemVer):

```
MAJOR.MINOR.PATCH
  1  .  2  .  3

- MAJOR (1.x.x): Breaking changes (BREAKING CHANGE: en footer)
- MINOR (x.2.x): Nuevas funcionalidades (feat)
- PATCH (x.x.3): Bug fixes (fix, perf)
```

**Ejemplos:**

```bash
# Version actual: 1.2.3

# Commit con breaking change → 2.0.0
git commit -m "feat(api)!: [CDOCK-25] change user endpoint response format

BREAKING CHANGE: User endpoint now returns nested object instead of flat structure"

# Commit con nueva feature → 1.3.0
git commit -m "feat(user): [CDOCK-26] add user profile picture upload"

# Commit con bug fix → 1.2.4
git commit -m "fix(auth): [CDOCK-27] resolve token expiration issue"
```

---

## Integración con JIRA

### Formato del Ticket

En este equipo, los tickets de JIRA siguen el formato:

```
CDOCK-{número}
```

**Ejemplos:**
- `CDOCK-16` - Implementar validación de email
- `CDOCK-23` - Resolver bug en Kafka consumer
- `CDOCK-24` - Añadir documentación de Circuit Breaker

### Ubicación del Ticket

El ticket de JIRA se coloca **después del scope y antes del subject**, entre corchetes:

```bash
# ✅ Correcto (ticket después del scope, antes del subject)
git commit -m "feat(user): [CDOCK-16] add email validation"

# ✅ También visible en git log --oneline:
# abc1234 feat(user): [CDOCK-16] add email va...  ← Ticket visible ✅

# ❌ Incorrecto (ticket al final - se trunca)
git commit -m "feat(user): add email validation [CDOCK-16]"
# abc1234 feat(user): add email validation [CD...  ← Ticket truncado ❌

# ❌ Incorrecto (ticket al principio, fuera del formato)
git commit -m "[CDOCK-16] feat(user): add email validation"

# ❌ Incorrecto (sin corchetes)
git commit -m "feat(user): CDOCK-16 add email validation"
```

### Múltiples Tickets

Si un commit afecta múltiples tickets (evitar si es posible):

```bash
git commit -m "feat(api): [CDOCK-16] [CDOCK-17] add user and order endpoints"
```

### Tickets Relacionados

Si el commit está relacionado con otros tickets (pero no los completa):

```bash
git commit -m "feat(user): [CDOCK-16] add email validation

Related to CDOCK-15 (user registration epic)"
```

### Integración Automática con JIRA

JIRA puede actualizar automáticamente el estado del ticket basándose en commits:

**Smart Commits:**

```bash
# Transicionar ticket a "In Progress"
git commit -m "feat(user): [CDOCK-16] add email validation #in-progress"

# Transicionar ticket a "Done" y registrar tiempo
git commit -m "fix(kafka): [CDOCK-23] resolve offset issue #done #time 2h"

# Añadir comentario al ticket
git commit -m "docs(readme): [CDOCK-24] update setup instructions #comment Updated docs with new env vars"
```

**Configuración en JIRA:**
1. Settings → Applications → DVCS accounts
2. Vincular repositorio de GitHub/GitLab/Bitbucket
3. Habilitar Smart Commits

---

## Workflow del Equipo

### Branching Strategy

```
main (producción)
  ↑
  └── feature/CDOCK-16-add-email-validation
  └── feature/CDOCK-23-fix-kafka-consumer
  └── hotfix/CDOCK-99-critical-security-fix
```

### Nomenclatura de Ramas

```
<type>/<JIRA-TICKET>-<short-description>
```

**Tipos de ramas:**

| Tipo | Uso | Base | Merge a |
|------|-----|------|---------|
| `feature/` | Nueva funcionalidad | `main` | `main` (vía PR) |
| `fix/` | Bug fix | `main` | `main` (vía PR) |
| `hotfix/` | Fix crítico en producción | `main` | `main` (directo o PR) |
| `refactor/` | Refactorización | `main` | `main` (vía PR) |
| `docs/` | Documentación | `main` | `main` (vía PR) |

**Ejemplos:**

```bash
# Feature
git checkout -b feature/CDOCK-16-add-email-validation

# Bug fix
git checkout -b fix/CDOCK-23-resolve-kafka-offset-issue

# Hotfix crítico
git checkout -b hotfix/CDOCK-99-fix-sql-injection

# Refactorización
git checkout -b refactor/CDOCK-45-extract-email-service

# Documentación
git checkout -b docs/CDOCK-50-add-ddd-guide
```

### Flujo Completo de Trabajo

```
1. Crear rama desde main
   git checkout main
   git pull origin main
   git checkout -b feature/CDOCK-16-add-email-validation

2. Desarrollar (commits frecuentes)
   # Primera implementación
   git add .
   git commit -m "feat(user): [CDOCK-16] implement email validation logic"
   git push origin feature/CDOCK-16-add-email-validation

   # Añadir tests
   git add .
   git commit -m "test(user): [CDOCK-16] add email validation tests"
   git push origin feature/CDOCK-16-add-email-validation

   # Documentación
   git add .
   git commit -m "docs(user): [CDOCK-16] add email validation docs"
   git push origin feature/CDOCK-16-add-email-validation

3. Crear Pull Request a main
   - Título: "[CDOCK-16] Add email validation"
   - Descripción:
     ## Summary
     Implements email validation for user registration

     ## Changes
     - Added Email value object with validation
     - Added unit tests
     - Updated documentation

     ## JIRA
     [CDOCK-16](https://jira.company.com/browse/CDOCK-16)

     ## Testing
     - [x] Unit tests passing
     - [x] Integration tests passing
     - [x] Manual testing completed

4. Code Review
   - Al menos 1 aprobación requerida
   - CI/CD debe pasar (tests, build, quality gates)

5. Merge a main
   - Squash commits (opcional, según política del equipo)
   - Merge commit message:
     "feat(user): add email validation [CDOCK-16] (#42)"

6. Desplegar a producción
   - main → deploy automático (CI/CD)
   - Tag de release: v1.3.0

7. Limpiar rama local
   git checkout main
   git pull origin main
   git branch -d feature/CDOCK-16-add-email-validation
```

---

## Ejemplos Prácticos

### Backend: Features

```bash
# Nueva API endpoint
git commit -m "feat(api): [CDOCK-16] add GET /users/{id} endpoint"

# Nueva validación
git commit -m "feat(user): [CDOCK-17] add username uniqueness validation"

# Nueva integración
git commit -m "feat(kafka): [CDOCK-18] add user-created event publisher"

# Nueva tabla en BD
git commit -m "feat(db): [CDOCK-19] add user_profiles table"

# Nuevo servicio
git commit -m "feat(email): [CDOCK-20] add email notification service"
```

### Backend: Bug Fixes

```bash
# Fix en lógica de negocio
git commit -m "fix(user): [CDOCK-23] resolve duplicate email creation"

# Fix en integración
git commit -m "fix(kafka): [CDOCK-24] resolve consumer offset reset issue"

# Fix en query
git commit -m "fix(db): [CDOCK-25] resolve N+1 query in user repository"

# Fix en validación
git commit -m "fix(api): [CDOCK-26] resolve 500 error on invalid input"

# Fix de seguridad
git commit -m "fix(auth): [CDOCK-27] resolve JWT token expiration bug"
```

### Backend: Refactorización

```bash
# Extraer lógica
git commit -m "refactor(user): [CDOCK-30] extract email sending to separate service"

# Optimización
git commit -m "refactor(db): [CDOCK-31] optimize user query with pagination"

# Renombrar
git commit -m "refactor(api): [CDOCK-32] rename UserDTO to UserResponse"

# Simplificar
git commit -m "refactor(service): [CDOCK-33] simplify user creation logic"
```

### Backend: Tests

```bash
# Unit tests
git commit -m "test(user): [CDOCK-40] add unit tests for email validation"

# Integration tests
git commit -m "test(api): [CDOCK-41] add integration tests for user endpoints"

# Test coverage
git commit -m "test(service): [CDOCK-42] increase coverage to 90%"

# Fix flaky test
git commit -m "test(kafka): [CDOCK-43] fix flaky consumer test"
```

### Backend: Documentación

```bash
# Guías
git commit -m "docs(architecture): [CDOCK-50] add hexagonal architecture guide"

# API docs
git commit -m "docs(api): [CDOCK-51] add OpenAPI specification"

# README
git commit -m "docs(readme): [CDOCK-52] update setup instructions"

# Inline docs
git commit -m "docs(service): [CDOCK-53] add javadoc to user service"
```

### Backend: Mantenimiento

```bash
# Dependencias
git commit -m "chore(deps): [CDOCK-60] upgrade spring boot to 3.5.8"

# Configuración
git commit -m "chore(config): [CDOCK-61] update database connection pool size"

# Limpieza
git commit -m "chore(cleanup): [CDOCK-62] remove deprecated user methods"

# Scripts
git commit -m "chore(scripts): [CDOCK-63] add database migration script"
```

### Breaking Changes

```bash
# Cambio en API
git commit -m "feat(api)!: [CDOCK-70] change user response format

BREAKING CHANGE: User endpoint now returns nested profile object.

Before:
{
  \"id\": 1,
  \"username\": \"john\",
  \"email\": \"john@example.com\",
  \"firstName\": \"John\",
  \"lastName\": \"Doe\"
}

After:
{
  \"id\": 1,
  \"username\": \"john\",
  \"email\": \"john@example.com\",
  \"profile\": {
    \"firstName\": \"John\",
    \"lastName\": \"Doe\"
  }
}

Migration guide: https://docs.company.com/migration/v2"

# Cambio en BD
git commit -m "feat(db)!: [CDOCK-71] rename users table to user_accounts

BREAKING CHANGE: Database table renamed from 'users' to 'user_accounts'.
Run migration script: db/migrations/V2__rename_users_table.sql"
```

---

## Métricas DORA

### ¿Qué son las Métricas DORA?

**DORA** (DevOps Research and Assessment) define 4 métricas clave para medir el rendimiento de equipos de software:

#### 1. Deployment Frequency (DF)
**¿Con qué frecuencia desplegamos a producción?**

| Nivel | Frecuencia | Target |
|-------|-----------|--------|
| 🌟 Elite | Varias veces al día | On-demand |
| ⭐ High | 1 vez al día - 1 vez por semana | Daily |
| 📊 Medium | 1 vez por semana - 1 vez al mes | Weekly |
| 📉 Low | Menos de 1 vez al mes | Monthly |

**Cómo medirlo:**
```bash
# Contar deploys a producción en los últimos 30 días
git log --since="30 days ago" --grep="deploy" --oneline | wc -l

# O contar merges a main (si main == producción)
git log --since="30 days ago" --merges --first-parent main --oneline | wc -l
```

#### 2. Lead Time for Changes (LT)
**¿Cuánto tarda un commit en llegar a producción?**

| Nivel | Tiempo | Target |
|-------|--------|--------|
| 🌟 Elite | < 1 hora | Minutos |
| ⭐ High | 1 día - 1 semana | Horas |
| 📊 Medium | 1 semana - 1 mes | Días |
| 📉 Low | > 1 mes | Semanas |

**Cómo medirlo:**
```bash
# Tiempo desde commit hasta deploy
# Commit time → Merge time → Deploy time

# Ejemplo: ver tiempo entre commit y merge a main
git log --first-parent main --pretty=format:"%h %cd" --date=iso
```

#### 3. Mean Time to Recovery (MTTR)
**¿Cuánto tardamos en recuperarnos de un fallo en producción?**

| Nivel | Tiempo | Target |
|-------|--------|--------|
| 🌟 Elite | < 1 hora | Minutos |
| ⭐ High | < 1 día | Horas |
| 📊 Medium | < 1 semana | 1 día |
| 📉 Low | > 1 semana | Días |

**Cómo medirlo:**
```bash
# Tiempo desde detección del bug hasta hotfix desplegado
# Incident reported → Hotfix deployed

# Ver hotfixes recientes
git log --all --grep="hotfix" --pretty=format:"%h %cd %s" --date=iso
```

#### 4. Change Failure Rate (CFR)
**¿Qué % de deployments causan fallos en producción?**

| Nivel | Tasa de Fallo | Target |
|-------|---------------|--------|
| 🌟 Elite | 0-15% | < 5% |
| ⭐ High | 16-30% | < 15% |
| 📊 Medium | 31-45% | < 30% |
| 📉 Low | > 45% | < 45% |

**Cómo medirlo:**
```bash
# (Deploys con fallos / Total de deploys) × 100

# Ver ratio de hotfixes vs features
hotfixes=$(git log --since="30 days ago" --grep="hotfix" --oneline | wc -l)
total=$(git log --since="30 days ago" --merges --first-parent main --oneline | wc -l)
echo "scale=2; ($hotfixes / $total) * 100" | bc
```

---

### Cómo Emitir Métricas DORA

#### Opción 1: Tags Git

Usar tags para marcar eventos importantes:

```bash
# Tag de deploy
git tag -a deploy/prod/2025-01-30-10-30 -m "Deploy to production [CDOCK-16]"
git push origin deploy/prod/2025-01-30-10-30

# Tag de incident
git tag -a incident/2025-01-30-15-45 -m "Database connection issue detected"
git push origin incident/2025-01-30-15-45

# Tag de recovery
git tag -a recovery/2025-01-30-16-20 -m "Hotfix deployed, service recovered"
git push origin recovery/2025-01-30-16-20
```

**Script para calcular métricas:**

```bash
#!/bin/bash
# dora-metrics.sh

# Deployment Frequency (últimos 30 días)
echo "📊 Deployment Frequency (last 30 days):"
git log --since="30 days ago" --tags --no-walk --pretty=format:"%h %cd %d" --date=iso | grep "deploy/prod" | wc -l

# Lead Time for Changes (promedio de últimos 10 merges)
echo ""
echo "⏱️  Lead Time for Changes (avg last 10 merges):"
git log --merges --first-parent main -10 --pretty=format:"%H" | while read merge_commit; do
    # Obtener commits en el merge
    first_commit=$(git log $merge_commit^..$merge_commit --pretty=format:"%H" --reverse | head -1)

    # Calcular diferencia de tiempo
    first_time=$(git show -s --format=%ct $first_commit)
    merge_time=$(git show -s --format=%ct $merge_commit)

    diff=$((merge_time - first_time))
    echo $diff
done | awk '{sum+=$1; count++} END {print sum/count/3600 " hours"}'

# Change Failure Rate (últimos 30 días)
echo ""
echo "🐛 Change Failure Rate (last 30 days):"
total=$(git log --since="30 days ago" --merges --first-parent main --oneline | wc -l)
hotfixes=$(git log --since="30 days ago" --grep="hotfix\|fix(critical)" --oneline | wc -l)
cfr=$(echo "scale=2; ($hotfixes / $total) * 100" | bc)
echo "$cfr%"

# MTTR (últimos 10 incidents)
echo ""
echo "🔧 Mean Time to Recovery (last 10 incidents):"
git tag -l "incident/*" --sort=-creatordate | head -10 | while read incident_tag; do
    incident_time=$(git log -1 --format=%ct $incident_tag)

    # Buscar siguiente recovery tag después del incident
    recovery_tag=$(git tag -l "recovery/*" --sort=creatordate --merged $incident_tag | head -1)

    if [ ! -z "$recovery_tag" ]; then
        recovery_time=$(git log -1 --format=%ct $recovery_tag)
        diff=$((recovery_time - incident_time))
        echo $diff
    fi
done | awk '{sum+=$1; count++} END {print sum/count/3600 " hours"}'
```

#### Opción 2: GitHub Actions + Custom Metrics

**.github/workflows/dora-metrics.yml:**

```yaml
name: DORA Metrics

on:
  push:
    branches: [main]
  pull_request:
    types: [closed]

jobs:
  track-metrics:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Calculate Lead Time
        if: github.event_name == 'pull_request' && github.event.pull_request.merged == true
        run: |
          PR_NUMBER=${{ github.event.pull_request.number }}
          FIRST_COMMIT=$(gh pr view $PR_NUMBER --json commits --jq '.commits[0].oid')
          MERGE_COMMIT=${{ github.event.pull_request.merge_commit_sha }}

          FIRST_COMMIT_TIME=$(git show -s --format=%ct $FIRST_COMMIT)
          MERGE_TIME=$(git show -s --format=%ct $MERGE_COMMIT)

          LEAD_TIME=$(( (MERGE_TIME - FIRST_COMMIT_TIME) / 3600 ))

          echo "Lead Time: $LEAD_TIME hours"

          # Enviar a sistema de métricas (Datadog, Prometheus, etc.)
          curl -X POST https://api.metrics.company.com/dora \
            -H "Content-Type: application/json" \
            -d '{
              "metric": "lead_time",
              "value": '"$LEAD_TIME"',
              "unit": "hours",
              "pr": '"$PR_NUMBER"',
              "jira": "${{ github.event.pull_request.title }}"
            }'

      - name: Track Deployment
        if: github.event_name == 'push' && github.ref == 'refs/heads/main'
        run: |
          # Incrementar contador de deployments
          curl -X POST https://api.metrics.company.com/dora \
            -H "Content-Type: application/json" \
            -d '{
              "metric": "deployment_frequency",
              "value": 1,
              "timestamp": "'"$(date -u +%Y-%m-%dT%H:%M:%SZ)"'"
            }'

      - name: Track Change Failure
        if: contains(github.event.head_commit.message, 'hotfix')
        run: |
          curl -X POST https://api.metrics.company.com/dora \
            -H "Content-Type: application/json" \
            -d '{
              "metric": "change_failure",
              "value": 1,
              "commit": "${{ github.sha }}",
              "message": "${{ github.event.head_commit.message }}"
            }'
```

#### Opción 3: Herramientas Especializadas

**Herramientas que calculan DORA automáticamente:**

1. **Sleuth** (https://sleuth.io)
   - Integración con GitHub/GitLab/Bitbucket + JIRA
   - Dashboard automático de métricas DORA
   - Alertas cuando métricas empeoran

2. **LinearB** (https://linearb.io)
   - Métricas DORA + métricas de equipo
   - Integración con herramientas de desarrollo

3. **Haystack** (https://usehaystack.io)
   - Métricas de productividad + DORA
   - Enfocado en equipos de ingeniería

4. **Faros** (https://faros.ai)
   - Plataforma open-source
   - Integración con múltiples fuentes

5. **Jira + Confluence + Bitbucket**
   - Dashboard nativo de Atlassian
   - Configuración: Jira → Reports → Deployment frequency

**Setup con Sleuth (ejemplo):**

```bash
# 1. Conectar repositorio
# Dashboard → Integrations → GitHub → Connect

# 2. Configurar deployment tracking
# Sleuth detecta automáticamente:
# - Merges a main como deployments
# - Tags con patrón deploy/* como deployments

# 3. Configurar incident tracking
# - Commits con "hotfix" o "fix(critical)" se marcan como incidents
# - Integración con PagerDuty / Opsgenie

# 4. Ver dashboard
# https://app.sleuth.io/your-org/your-project/dora
```

---

### Ejemplo: Dashboard de DORA Metrics

```
┌────────────────────────────────────────────────────────────────┐
│                     DORA Metrics - Q1 2025                      │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│  📊 Deployment Frequency                                        │
│      ████████████████████████ 45 deploys/month                 │
│      Target: 20+ deploys/month ✅                               │
│      Level: ⭐ HIGH                                             │
│                                                                 │
│  ⏱️  Lead Time for Changes                                      │
│      ████████░░░░░░░░░░░░░░░░ 2.3 days                         │
│      Target: < 3 days ✅                                        │
│      Level: ⭐ HIGH                                             │
│                                                                 │
│  🔧 Mean Time to Recovery                                       │
│      ██████░░░░░░░░░░░░░░░░░░ 4.2 hours                        │
│      Target: < 6 hours ✅                                       │
│      Level: ⭐ HIGH                                             │
│                                                                 │
│  🐛 Change Failure Rate                                         │
│      ████░░░░░░░░░░░░░░░░░░░░ 8.5%                             │
│      Target: < 15% ✅                                           │
│      Level: 🌟 ELITE                                            │
│                                                                 │
├────────────────────────────────────────────────────────────────┤
│  Overall Performance: ⭐ HIGH                                   │
│  Trending: 📈 IMPROVING                                         │
└────────────────────────────────────────────────────────────────┘
```

---

## Automatización

### Pre-commit Hooks

Validar formato de commits antes de commitear:

**.git/hooks/commit-msg:**

```bash
#!/bin/bash
# Validar formato de conventional commits

commit_msg_file=$1
commit_msg=$(cat "$commit_msg_file")

# Pattern: <type>(<scope>): [JIRA-TICKET] <subject>
pattern="^(feat|fix|docs|style|refactor|test|chore|perf|ci|build)(\([a-z]+\))?: \[CDOCK-[0-9]+\] .+$"

if ! echo "$commit_msg" | head -1 | grep -qE "$pattern"; then
    echo "❌ Error: Commit message no cumple con Conventional Commits"
    echo ""
    echo "Formato esperado:"
    echo "  <type>(<scope>): [CDOCK-XXX] <subject>"
    echo ""
    echo "Ejemplo:"
    echo "  feat(user): [CDOCK-16] add email validation"
    echo ""
    echo "Tipos válidos:"
    echo "  feat, fix, docs, style, refactor, test, chore, perf, ci, build"
    echo ""
    echo "IMPORTANTE: Ticket JIRA va después del scope y antes del subject"
    echo ""
    exit 1
fi

# Validar longitud del subject (sin incluir el ticket)
subject=$(echo "$commit_msg" | head -1 | sed -E 's/^[^:]+: \[[^]]+\] (.+)$/\1/')
if [ ${#subject} -gt 50 ]; then
    echo "⚠️  Warning: Subject es muy largo (${#subject} chars, max 50)"
    echo "  Subject: $subject"
    echo ""
    echo "Considera acortarlo para mejor legibilidad"
    echo ""
    # Permitir continuar (solo warning)
fi

echo "✅ Commit message válido"
exit 0
```

**Instalar hook:**

```bash
chmod +x .git/hooks/commit-msg
```

### Commitizen

Herramienta para guiar la creación de commits:

**Instalación:**

```bash
npm install -g commitizen cz-conventional-changelog

# O con pnpm
pnpm add -g commitizen cz-conventional-changelog
```

**Configuración en package.json:**

```json
{
  "config": {
    "commitizen": {
      "path": "cz-conventional-changelog"
    }
  }
}
```

**Uso:**

```bash
# En lugar de git commit
git cz

# Wizard interactivo:
# ? Select the type of change: feat
# ? What is the scope: user
# ? JIRA ticket: CDOCK-16
# ? Write a short description: add email validation
# ? Write a longer description: (press enter to skip)
# ? Are there any breaking changes? No

# Genera:
# feat(user): [CDOCK-16] add email validation
```

### Commitlint

Validar commits en CI/CD:

**.commitlintrc.json:**

```json
{
  "extends": ["@commitlint/config-conventional"],
  "rules": {
    "type-enum": [
      2,
      "always",
      [
        "feat",
        "fix",
        "docs",
        "style",
        "refactor",
        "test",
        "chore",
        "perf",
        "ci",
        "build"
      ]
    ],
    "scope-empty": [1, "never"],
    "subject-max-length": [1, "always", 50],
    "body-max-line-length": [1, "always", 72],
    "footer-max-line-length": [0]
  }
}
```

**.github/workflows/commitlint.yml:**

```yaml
name: Validate Commits

on:
  pull_request:
    types: [opened, synchronize, reopened]

jobs:
  commitlint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Setup Node
        uses: actions/setup-node@v4
        with:
          node-version: 20

      - name: Install commitlint
        run: |
          npm install -g @commitlint/cli @commitlint/config-conventional

      - name: Validate commits
        run: |
          npx commitlint --from ${{ github.event.pull_request.base.sha }} --to ${{ github.sha }} --verbose
```

---

## Mejores Prácticas

### ✅ DO: Hacer

1. **Usar el tipo correcto**
   ```bash
   ✅ feat(user): [CDOCK-16] add email validation
   ❌ fix(user): [CDOCK-16] add email validation  # No es un fix
   ```

2. **Subject en imperativo**
   ```bash
   ✅ feat(user): [CDOCK-16] add email validation
   ❌ feat(user): [CDOCK-16] added email validation
   ❌ feat(user): [CDOCK-16] adds email validation
   ```

3. **Subject en minúsculas**
   ```bash
   ✅ feat(user): [CDOCK-16] add email validation
   ❌ feat(user): [CDOCK-16] Add Email Validation
   ```

4. **Sin punto al final**
   ```bash
   ✅ feat(user): [CDOCK-16] add email validation
   ❌ feat(user): [CDOCK-16] add email validation.
   ```

5. **Commits atómicos**
   ```bash
   # ✅ Correcto: 1 commit = 1 cambio lógico
   feat(user): [CDOCK-16] add email validation
   test(user): [CDOCK-16] add email validation tests
   docs(user): [CDOCK-16] update email validation docs

   # ❌ Incorrecto: 1 commit = múltiples cambios no relacionados
   feat(user): [CDOCK-16] add email validation, fix bug, update docs
   ```

6. **Scope específico**
   ```bash
   ✅ feat(user): [CDOCK-16] add email validation
   ✅ feat(kafka): [CDOCK-16] add user-created event
   ❌ feat(backend): [CDOCK-16] add email validation  # Demasiado genérico
   ```

7. **Incluir JIRA ticket siempre (después del scope)**
   ```bash
   ✅ feat(user): [CDOCK-16] add email validation
   ❌ feat(user): add email validation  # Sin ticket
   ❌ feat(user): add email validation [CDOCK-16]  # Ticket al final (se trunca en git log)
   ```

### ❌ DON'T: Evitar

1. **Commits vagos**
   ```bash
   ❌ git commit -m "fix stuff"
   ❌ git commit -m "updates"
   ❌ git commit -m "wip"
   ❌ git commit -m "final version"
   ```

2. **Commits demasiado grandes**
   ```bash
   ❌ feat(api): [CDOCK-16] add user, order, and product endpoints

   # Mejor: dividir en 3 commits
   ✅ feat(api): [CDOCK-16] add user endpoints
   ✅ feat(api): [CDOCK-17] add order endpoints
   ✅ feat(api): [CDOCK-18] add product endpoints
   ```

3. **Subject demasiado largo**
   ```bash
   ❌ feat(user): [CDOCK-16] add email validation with regex pattern and custom error messages for invalid formats

   # Mejor: detalles en el body
   ✅ feat(user): [CDOCK-16] add email validation

      - Uses regex pattern for format validation
      - Custom error messages for different invalid formats
      - Supports international email addresses
   ```

4. **Breaking changes sin avisar**
   ```bash
   ❌ feat(api): [CDOCK-16] change user response format

   # Mejor: usar ! y BREAKING CHANGE
   ✅ feat(api)!: [CDOCK-16] change user response format

      BREAKING CHANGE: User endpoint now returns nested object
   ```

5. **Mezclar refactorización con features**
   ```bash
   ❌ feat(user): [CDOCK-16] add email validation and refactor service

   # Mejor: commits separados
   ✅ refactor(user): [CDOCK-16] extract email logic to separate service
   ✅ feat(user): [CDOCK-16] add email validation
   ```

---

## Resumen Rápido

### Template de Commit

```bash
<type>(<scope>): [CDOCK-XXX] <subject>

<body (opcional)>

<footer (opcional)>
```

**Ejemplo:**
```bash
feat(user): [CDOCK-16] add email validation
```

**Visible en git log --oneline:**
```
abc1234 feat(user): [CDOCK-16] add email va...  ✅ Ticket visible
```

### Tipos Comunes

| Comando | Uso |
|---------|-----|
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de bug |
| `docs` | Documentación |
| `refactor` | Refactorización |
| `test` | Tests |
| `chore` | Mantenimiento |

### Workflow

```bash
# 1. Crear rama
git checkout -b feature/CDOCK-16-add-email-validation

# 2. Commit
git commit -m "feat(user): [CDOCK-16] add email validation"

# 3. Push
git push origin feature/CDOCK-16-add-email-validation

# 4. PR a main
# 5. Merge (después de aprobación)
```

### DORA Targets

| Métrica | Target |
|---------|--------|
| **Deployment Frequency** | Daily |
| **Lead Time** | < 1 day |
| **MTTR** | < 1 hour |
| **Change Failure Rate** | < 15% |

---

## Referencias

- **Conventional Commits:** https://www.conventionalcommits.org/
- **Semantic Versioning:** https://semver.org/
- **DORA Metrics:** https://dora.dev/
- **DORA Quick Check:** https://dora.dev/quickcheck/
- **Accelerate Book:** https://itrevolution.com/product/accelerate/
- **Commitizen:** https://commitizen-tools.github.io/commitizen/
- **Commitlint:** https://commitlint.js.org/
