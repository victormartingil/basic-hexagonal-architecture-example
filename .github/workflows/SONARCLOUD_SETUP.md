# SonarCloud Setup (OPCIONAL)

Este workflow está **DESHABILITADO por defecto** porque requiere configuración de cuenta en SonarCloud.

## ¿Por qué está deshabilitado?

- SonarCloud requiere cuenta (gratuita para open source)
- Requiere configuración de secrets en GitHub
- Es **OPCIONAL** para aprender arquitectura hexagonal
- JaCoCo (incluido) ya proporciona análisis de cobertura local

## ¿Cuándo habilitarlo?

✅ **SÍ, si quieres:**
- Aprender herramientas empresariales profesionales
- Mostrar métricas de calidad en tu portfolio
- Análisis automático de bugs/vulnerabilities
- Quality Gates y reportes en PRs

❌ **NO necesario si:**
- Solo quieres aprender arquitectura hexagonal
- No tienes cuenta en SonarCloud
- Prefieres mantener el proyecto simple

## Cómo habilitar SonarCloud

### 1. Crear cuenta en SonarCloud

1. Ve a https://sonarcloud.io
2. Sign up con tu cuenta de GitHub
3. Importa tu repositorio
4. Obtén:
   - **Organization key** (ej: `tu-usuario`)
   - **Project key** (ej: `tu-usuario_nombre-repo`)
   - **Token** (Settings → Security → Generate Token)

### 2. Configurar secrets en GitHub

1. Ve a tu repositorio en GitHub
2. Settings → Secrets and variables → Actions
3. Añade estos secrets:
   - `SONAR_TOKEN` = token de SonarCloud
   - `SONAR_PROJECT_KEY` = project key de SonarCloud
   - `SONAR_ORGANIZATION` = organization key de SonarCloud

### 3. Actualizar pom.xml

Edita `pom.xml` y reemplaza:

```xml
<sonar.organization>tu-organizacion-sonarcloud</sonar.organization>
<sonar.projectKey>tu-usuario_nombre-proyecto</sonar.projectKey>
```

Con tus valores reales obtenidos en el paso 1.

### 4. Habilitar el workflow

Renombra el archivo:

```bash
mv .github/workflows/sonarcloud.yml.disabled .github/workflows/sonarcloud.yml
```

Edita `.github/workflows/sonarcloud.yml` y **descomenta** las líneas:

```yaml
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]
  workflow_dispatch:
```

### 5. Añadir badges al README (opcional)

Añade estos badges en la cabecera del README:

```markdown
[![SonarCloud](https://github.com/TU-USUARIO/TU-REPO/actions/workflows/sonarcloud.yml/badge.svg)](https://github.com/TU-USUARIO/TU-REPO/actions/workflows/sonarcloud.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=TU_PROJECT_KEY&metric=alert_status)](https://sonarcloud.io/dashboard?id=TU_PROJECT_KEY)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=TU_PROJECT_KEY&metric=coverage)](https://sonarcloud.io/dashboard?id=TU_PROJECT_KEY)
```

Reemplaza `TU-USUARIO`, `TU-REPO` y `TU_PROJECT_KEY` con tus valores.

### 6. Verificar funcionamiento

1. Haz un push a tu rama
2. Ve a Actions en GitHub
3. Verifica que el workflow "SonarCloud Analysis" se ejecuta sin errores
4. Ve al dashboard de SonarCloud para ver el análisis

## Ejecutar análisis localmente

```bash
# Análisis completo (reemplaza <tu-token> con tu token)
./mvnw clean verify sonar:sonar \
  -Dsonar.token=<tu-token>
```

## Troubleshooting

### Error: "Unauthorized" o "Invalid token"

- Verifica que el token esté correctamente configurado en GitHub Secrets
- Genera un nuevo token en SonarCloud si es necesario

### Error: "Project not found"

- Verifica que el project key en `pom.xml` coincida exactamente con SonarCloud
- Asegúrate de que el proyecto esté importado en SonarCloud

### Quality Gate falla

- Revisa el reporte en SonarCloud dashboard
- Ajusta las reglas del Quality Gate si es necesario
- Verifica que la cobertura sea suficiente (configurada en 80%)

## Alternativas sin cuenta

Si prefieres análisis local sin cuenta:

1. **JaCoCo** (ya incluido):
   ```bash
   ./mvnw clean test
   open target/site/jacoco/index.html
   ```

2. **SpotBugs** (añadir al pom.xml):
   - Detecta bugs comunes
   - No requiere cuenta

3. **PMD** (añadir al pom.xml):
   - Análisis de código estático
   - No requiere cuenta

## Documentación oficial

- SonarCloud: https://sonarcloud.io/documentation
- SonarQube (local): https://www.sonarqube.org/
- JaCoCo: https://www.jacoco.org/
