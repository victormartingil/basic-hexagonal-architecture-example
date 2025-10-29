# Code Quality: JaCoCo + SonarQube

Este documento explica cómo medir y mejorar la calidad del código usando **JaCoCo** (cobertura de tests) y **SonarQube/SonarCloud** (análisis de calidad).

---

## 📊 JaCoCo - Code Coverage

### ¿Qué es JaCoCo?

**JaCoCo** (Java Code Coverage) es una herramienta que mide qué porcentaje del código está cubierto por tests.

**Métricas que mide:**
- **Line Coverage**: % de líneas ejecutadas por tests
- **Branch Coverage**: % de ramas (if/else, switch) ejecutadas
- **Method Coverage**: % de métodos ejecutados
- **Class Coverage**: % de clases ejecutadas

### ¿Cómo Funciona?

```
1. mvnw test
   ↓
2. JaCoCo Agent intercepta la ejecución
   ↓
3. Registra qué líneas se ejecutan
   ↓
4. Genera reporte HTML + XML
   ↓
5. Calcula % de cobertura
```

### Ejecutar JaCoCo Localmente

```bash
# Ejecutar tests y generar reporte de cobertura
./mvnw clean test

# Ver reporte HTML (abre en navegador)
open target/site/jacoco/index.html

# O en Linux/Windows
xdg-open target/site/jacoco/index.html  # Linux
start target/site/jacoco/index.html     # Windows
```

**Output esperado:**
```
[INFO] --- jacoco:0.8.12:report (report) @ hexarch ---
[INFO] Loading execution data file /path/to/target/jacoco.exec
[INFO] Analyzed bundle 'hexarch' with 32 classes
```

### Interpretar el Reporte de JaCoCo

El reporte HTML muestra:

```
📦 Packages
├── 🟢 user.application.service (95% coverage)
│   ├── CreateUserService.java (100%)
│   └── GetUserService.java (90%)
├── 🟡 user.domain.model (75% coverage)
│   └── User.java (75%)
└── 🔴 user.infrastructure.adapter (60% coverage)
    └── UserController.java (60%)
```

**Colores:**
- 🟢 **Verde** (80-100%): Excelente cobertura
- 🟡 **Amarillo** (60-79%): Cobertura aceptable
- 🔴 **Rojo** (<60%): Cobertura insuficiente

### Configuración de JaCoCo

**En `pom.xml`:**

```xml
<properties>
    <!-- Cobertura mínima requerida (80%) -->
    <jacoco.coverage.minimum>0.80</jacoco.coverage.minimum>
</properties>

<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <!-- 1. Preparar agente antes de tests -->
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>

        <!-- 2. Generar reporte después de tests -->
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>

        <!-- 3. Verificar cobertura mínima -->
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Exclusiones de JaCoCo

**Qué excluir y por qué:**

```xml
<configuration>
    <excludes>
        <!-- 1. Código generado por OpenAPI -->
        <exclude>**/target/generated-sources/**</exclude>
        <exclude>**/dto/generated/**</exclude>
        <exclude>**/api/generated/**</exclude>

        <!-- 2. JPA Entities (solo mapeo DB) -->
        <exclude>**/*Entity.class</exclude>

        <!-- 3. Clases de configuración -->
        <exclude>**/*Application.class</exclude>
        <exclude>**/*Config.class</exclude>
        <exclude>**/*Configuration.class</exclude>
    </excludes>
</configuration>
```

**Razones:**
- **Código generado**: No tiene sentido testear código autogenerado
- **Entities**: Son POJOs simples, sin lógica de negocio
- **Config classes**: Solo definen beans, difíciles de testear

### Comandos Útiles

```bash
# Ver cobertura sin verificar mínimo
./mvnw jacoco:report

# Verificar que se cumpla cobertura mínima (80%)
./mvnw jacoco:check

# Limpiar reportes anteriores
./mvnw clean

# Generar reporte sin ejecutar tests
./mvnw jacoco:report-aggregate
```

### Mejorar la Cobertura

**Estrategias:**

1. **Identificar código sin tests:**
   - Abrir `target/site/jacoco/index.html`
   - Buscar clases con cobertura <80%
   - Priorizar clases de dominio y aplicación

2. **Escribir tests para lógica crítica:**
   ```java
   // ❌ Sin tests
   public boolean isValidUser(User user) {
       return user.isEnabled() && user.getEmail() != null;
   }

   // ✅ Con tests
   @Test
   void shouldReturnTrue_whenUserIsValid() {
       User user = User.create("john", "john@test.com");
       assertTrue(isValidUser(user));
   }
   ```

3. **Tests de ramas (if/else):**
   ```java
   // Necesitas 2 tests para cubrir ambas ramas
   if (user.isEnabled()) {
       // Test 1: user enabled
   } else {
       // Test 2: user disabled
   }
   ```

---

## 🔍 SonarQube / SonarCloud (OPCIONAL)

> **⚠️ NOTA IMPORTANTE**
>
> **SonarCloud es OPCIONAL** para este proyecto educativo. El workflow está **DESHABILITADO por defecto**.
>
> - ✅ JaCoCo (incluido) ya proporciona análisis de cobertura local
> - ✅ NO es necesario para aprender arquitectura hexagonal
> - ✅ Requiere cuenta gratuita + configuración de secrets
> - ✅ Ver [SONARCLOUD_SETUP.md](../.github/workflows/SONARCLOUD_SETUP.md) para habilitarlo

### ¿Qué es SonarQube?

**SonarQube** es una plataforma que analiza código para detectar:
- 🐛 **Bugs**: Errores potenciales
- 🔒 **Vulnerabilidades**: Problemas de seguridad
- 💩 **Code Smells**: Código difícil de mantener
- 📊 **Cobertura**: Integración con JaCoCo
- 📏 **Métricas**: Complejidad, duplicación, etc.

**SonarCloud** = SonarQube en la nube (gratis para proyectos open source)

### ¿Cuándo usar SonarCloud?

**✅ SÍ, si quieres:**
- Aprender herramientas empresariales profesionales
- Mostrar métricas de calidad en tu portfolio
- Análisis automático en cada PR/push
- Detectar problemas de seguridad automáticamente

**❌ NO necesario si:**
- Solo quieres aprender arquitectura hexagonal
- No quieres configurar cuenta externa
- JaCoCo local es suficiente para tu caso

### Setup de SonarCloud (GitHub) - OPCIONAL

#### Paso 1: Crear Cuenta en SonarCloud

1. Ve a https://sonarcloud.io
2. Login con GitHub
3. Click en "+" → "Analyze new project"
4. Selecciona tu repositorio
5. Copia los valores:
   - **Organization**: `YOUR_SONAR_ORG`
   - **Project Key**: `YOUR_PROJECT_KEY`

#### Paso 2: Configurar Secrets en GitHub

1. Ve a tu repositorio en GitHub
2. Settings → Secrets and variables → Actions
3. Añade estos secrets:
   - `SONAR_TOKEN`: Token de SonarCloud (generar en SonarCloud → My Account → Security)
   - `SONAR_PROJECT_KEY`: Tu project key
   - `SONAR_ORGANIZATION`: Tu organización

#### Paso 3: Actualizar pom.xml

```xml
<properties>
    <!-- Cambiar estos valores -->
    <sonar.organization>YOUR_SONAR_ORG</sonar.organization>
    <sonar.projectKey>YOUR_PROJECT_KEY</sonar.projectKey>
</properties>
```

**Reemplazar:**
- `YOUR_SONAR_ORG` con tu organización de SonarCloud
- `YOUR_PROJECT_KEY` con tu project key

### Ejecutar SonarQube Localmente

```bash
# 1. Ejecutar tests con cobertura
./mvnw clean verify

# 2. Analizar código y enviar a SonarCloud
./mvnw sonar:sonar \
  -Dsonar.projectKey=YOUR_PROJECT_KEY \
  -Dsonar.organization=YOUR_SONAR_ORG \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=YOUR_SONAR_TOKEN
```

**Ver resultados:**
- Dashboard: https://sonarcloud.io/dashboard?id=YOUR_PROJECT_KEY

### Configuración de SonarQube

**En `pom.xml`:**

```xml
<properties>
    <!-- Configuración básica -->
    <sonar.host.url>https://sonarcloud.io</sonar.host.url>
    <sonar.organization>YOUR_SONAR_ORG</sonar.organization>
    <sonar.projectKey>YOUR_PROJECT_KEY</sonar.projectKey>

    <!-- Integración con JaCoCo -->
    <sonar.java.coveragePlugin>jacoco</sonar.java.coveragePlugin>
    <sonar.jacoco.reportPath>${project.basedir}/target/jacoco.exec</sonar.jacoco.reportPath>

    <!-- Exclusiones -->
    <sonar.exclusions>
        **/target/generated-sources/**,
        **/dto/generated/**,
        **/api/generated/**,
        **/*Entity.java,
        **/*Application.java
    </sonar.exclusions>

    <!-- Exclusiones de cobertura -->
    <sonar.coverage.exclusions>
        **/target/generated-sources/**,
        **/dto/generated/**,
        **/*Entity.java,
        **/*Config.java,
        **/*Configuration.java
    </sonar.coverage.exclusions>
</properties>
```

### Exclusiones de SonarQube

**Tipos de exclusiones:**

1. **`sonar.exclusions`**: Excluye archivos del análisis completo
   ```xml
   <sonar.exclusions>
       **/dto/generated/**,     <!-- DTOs generados -->
       **/*Entity.java,          <!-- JPA Entities -->
       **/*Application.java      <!-- Main class -->
   </sonar.exclusions>
   ```

2. **`sonar.coverage.exclusions`**: Solo excluye de cobertura (sí analiza calidad)
   ```xml
   <sonar.coverage.exclusions>
       **/*Config.java,          <!-- Config classes -->
       **/*Mapper.java           <!-- Mappers (MapStruct) -->
   </sonar.coverage.exclusions>
   ```

3. **`sonar.test.exclusions`**: Excluye archivos de test
   ```xml
   <sonar.test.exclusions>
       **/architecture/**,       <!-- ArchUnit tests -->
       **/*IntegrationTest.java  <!-- Integration tests -->
   </sonar.test.exclusions>
   ```

### Reglas y Quality Gates

**Quality Gate por defecto:**
- ✅ **0 Bugs** (nuevos)
- ✅ **0 Vulnerabilities** (nuevas)
- ✅ **0 Security Hotspots** (nuevos)
- ✅ **Coverage ≥ 80%** (nuevo código)
- ✅ **Code Smells ≤ 5%** (ratio de deuda técnica)

**Personalizar Quality Gate:**
1. Ve a SonarCloud → Quality Gates
2. Create → Define condiciones personalizadas
3. Aplica a tu proyecto

### Tipos de Issues

**1. Bugs (🐛):**
```java
// ❌ Bug: NullPointerException potencial
public String getName() {
    return user.getName().toUpperCase();  // user puede ser null
}

// ✅ Fix: Validación
public String getName() {
    return user != null ? user.getName().toUpperCase() : "";
}
```

**2. Vulnerabilities (🔒):**
```java
// ❌ Vulnerabilidad: SQL Injection
String query = "SELECT * FROM users WHERE id = " + userId;

// ✅ Fix: Prepared Statement
String query = "SELECT * FROM users WHERE id = ?";
```

**3. Code Smells (💩):**
```java
// ❌ Code Smell: Método muy largo (>20 líneas)
public void processUser() {
    // 50 líneas de código...
}

// ✅ Fix: Extraer métodos
public void processUser() {
    validateUser();
    saveUser();
    notifyUser();
}
```

### Métricas Importantes

**En SonarCloud Dashboard:**

```
📊 Métricas Clave
├── Reliability: A (0 bugs)
├── Security: A (0 vulnerabilities)
├── Maintainability: A (0 code smells)
├── Coverage: 85.2%
├── Duplications: 0.0%
└── Technical Debt: 2h
```

**Interpretación:**
- **A/B**: Excelente/Bueno
- **C/D/E**: Mejorar urgentemente
- **Technical Debt**: Tiempo estimado para resolver issues

### Comandos Útiles

```bash
# Análisis completo (tests + sonar)
./mvnw clean verify sonar:sonar

# Solo análisis (sin tests)
./mvnw sonar:sonar

# Con configuración personalizada
./mvnw sonar:sonar \
  -Dsonar.verbose=true \
  -Dsonar.projectName="My Custom Name"

# Ver propiedades de Sonar
./mvnw sonar:help -Ddetail=true
```

### Workflow de GitHub Actions

**Automático en cada push/PR:**

```yaml
name: SonarCloud Analysis
on: [push, pull_request]

jobs:
  sonarcloud:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0  # Necesario para análisis completo

      - name: Build and test
        run: ./mvnw clean verify

      - name: SonarCloud Scan
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: ./mvnw sonar:sonar
```

---

## 🎯 Mejores Prácticas

### 1. Cobertura Mínima

**Recomendaciones por capa:**

```
Domain Layer:     90-100%  (lógica crítica)
Application Layer: 85-95%  (casos de uso)
Infrastructure:   70-80%  (adapters, menos crítico)
```

**Ajustar en `pom.xml`:**
```xml
<jacoco.coverage.minimum>0.80</jacoco.coverage.minimum>
```

### 2. Exclusiones Correctas

**Qué SIEMPRE excluir:**
- Código generado (OpenAPI, MapStruct)
- DTOs simples sin lógica
- Main application class
- Configuration classes

**Qué NO excluir:**
- Domain model (User, Value Objects)
- Services (CreateUserService)
- Mappers con lógica personalizada

### 3. CI/CD Integration

**Estrategia:**
```
PR → GitHub Actions
 ├── 1. Ejecutar tests
 ├── 2. Generar cobertura JaCoCo
 ├── 3. Análisis SonarCloud
 └── 4. Quality Gate check
      ├── ✅ Pass → Merge permitido
      └── ❌ Fail → Bloquear merge
```

### 4. Monitoreo Continuo

**Dashboard de SonarCloud:**
- Revisar semanalmente
- Priorizar bugs y vulnerabilities
- Mejorar code smells gradualmente

---

## 🚨 Troubleshooting

### JaCoCo no genera reporte

**Problema:** `target/site/jacoco/` no existe

**Solución:**
```bash
# 1. Limpiar proyecto
./mvnw clean

# 2. Ejecutar tests
./mvnw test

# 3. Verificar reporte
ls -la target/site/jacoco/
```

### SonarCloud falla en CI

**Problema:** Error de autenticación

**Solución:**
1. Verificar que `SONAR_TOKEN` esté en GitHub Secrets
2. Token debe tener permisos `Execute Analysis`
3. Verificar que el project key sea correcto

### Cobertura muy baja

**Problema:** JaCoCo muestra 30% coverage

**Estrategia de mejora:**
1. Identificar clases sin tests (`target/site/jacoco/index.html`)
2. Priorizar Domain y Application layers
3. Escribir tests para lógica crítica primero
4. Excluir código generado correctamente

### Quality Gate falla

**Problema:** SonarCloud bloquea el merge

**Pasos:**
1. Ver issues en SonarCloud dashboard
2. Filtrar por "New Code" (código nuevo)
3. Resolver en orden: Bugs → Vulnerabilities → Code Smells
4. Re-ejecutar análisis

---

## 📚 Recursos

**JaCoCo:**
- Documentación oficial: https://www.jacoco.org/jacoco/trunk/doc/
- Maven plugin: https://www.eclemma.org/jacoco/trunk/doc/maven.html

**SonarQube:**
- SonarCloud: https://sonarcloud.io
- Documentación: https://docs.sonarqube.org/
- Rules reference: https://rules.sonarsource.com/java/

**Best Practices:**
- Google Java Style Guide: https://google.github.io/styleguide/javaguide.html
- Clean Code principles
- SOLID principles
