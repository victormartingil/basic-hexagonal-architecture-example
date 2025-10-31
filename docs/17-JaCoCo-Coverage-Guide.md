# JaCoCo - Cobertura de Tests

## ¿Qué es JaCoCo?

**JaCoCo** (Java Code Coverage) es una herramienta de análisis de cobertura de código para Java. Mide qué porcentaje del código ha sido ejecutado por los tests.

## ¿Por qué es importante la cobertura de código?

La cobertura de código te ayuda a:
- ✅ **Identificar código sin testear** - Descubres qué partes de tu aplicación no tienen tests
- ✅ **Mejorar la calidad** - Más tests = menos bugs en producción
- ✅ **Refactorizar con confianza** - Si tienes buena cobertura, puedes refactorizar sin miedo a romper algo
- ✅ **Documentar el comportamiento** - Los tests sirven como documentación viva de cómo funciona tu código

## Configuración actual en este proyecto

### Ubicación de la configuración

La configuración de JaCoCo está en `pom.xml`, dentro del plugin `jacoco-maven-plugin`.

### Cobertura mínima requerida

```xml
<!-- En el pom.xml, sección <properties> -->
<jacoco.coverage.minimum>0.80</jacoco.coverage.minimum>                    <!-- Líneas: 80% -->
<jacoco.coverage.minimum.branches>0.65</jacoco.coverage.minimum.branches>  <!-- Branches: 65% -->
```

Este proyecto requiere:
- **80% de cobertura de líneas** (line coverage)
- **65% de cobertura de branches** (branch coverage - estándar de la industria)

### Tipos de cobertura

#### 1. Line Coverage (Cobertura de Líneas)
Mide qué % de líneas de código han sido ejecutadas por los tests.

**Ejemplo:**
```java
public int divide(int a, int b) {
    if (b == 0) {              // Línea 1
        throw new Exception(); // Línea 2 - NO CUBIERTA si nunca testeas con b=0
    }
    return a / b;              // Línea 3
}
```

Si solo haces `divide(10, 2)` en tus tests:
- ✅ Línea 1 cubierta (se evalúa el if)
- ❌ Línea 2 NO cubierta (nunca se lanza la excepción)
- ✅ Línea 3 cubierta (se ejecuta el return)
- **Cobertura de líneas: 67%** (2 de 3 líneas)

#### 2. Branch Coverage (Cobertura de Ramas)
Mide qué % de decisiones (if, switch, loops) han sido probadas en todos sus posibles caminos.

**Ejemplo:**
```java
public String getStatus(boolean active) {
    if (active) {           // Decisión con 2 ramas
        return "Active";    // Rama TRUE
    } else {
        return "Inactive";  // Rama FALSE
    }
}
```

Si solo testeas `getStatus(true)`:
- ✅ Rama TRUE cubierta
- ❌ Rama FALSE NO cubierta
- **Cobertura de branches: 50%** (1 de 2 ramas)

Para tener 100% necesitas testear ambos casos: `true` y `false`.

## Comandos útiles

### 1. Ejecutar tests y generar reporte
```bash
./mvnw clean test
```

Esto:
1. Limpia el proyecto (`clean`)
2. Ejecuta todos los tests (`test`)
3. Genera el reporte de cobertura en `target/site/jacoco/index.html`

### 2. Ejecutar tests y verificar cobertura mínima
```bash
./mvnw clean install
```

Esto hace lo mismo que `test` pero además:
- Verifica que la cobertura cumple el mínimo (80%)
- **Falla el build** si no se cumple el requisito
- Genera el JAR final

### 3. Ver el reporte de cobertura
```bash
open target/site/jacoco/index.html
```

El reporte HTML muestra:
- % de cobertura por paquete
- % de cobertura por clase
- Líneas cubiertas (verde) vs no cubiertas (rojo)
- Ramas cubiertas (amarillo/verde) vs no cubiertas (rojo)

## Exclusiones de cobertura

### ¿Qué se excluye de la cobertura?

No todo el código debe ser testeado. Este proyecto excluye:

```xml
<excludes>
    <exclude>**/target/generated-sources/**</exclude>  <!-- Código generado automáticamente -->
    <exclude>**/dto/generated/**</exclude>             <!-- DTOs generados por OpenAPI -->
    <exclude>**/api/generated/**</exclude>             <!-- APIs generadas por OpenAPI -->
    <exclude>**/rest/api/**</exclude>                  <!-- Interfaces de API generadas -->
    <exclude>**/*Entity.class</exclude>                <!-- Entidades JPA (solo datos) -->
    <exclude>**/*Application.class</exclude>           <!-- Clase principal de Spring Boot -->
    <exclude>**/*Config.class</exclude>                <!-- Clases de configuración -->
    <exclude>**/*Configuration.class</exclude>         <!-- Clases de configuración -->
</excludes>
```

### ¿Por qué se excluyen estas clases?

1. **Código generado** - No tiene sentido testear código que no escribiste tú (generado por herramientas)
2. **Entidades JPA** - Son solo getters/setters, no tienen lógica de negocio
3. **Configuración** - Son declaraciones, no lógica a testear
4. **Clase Application** - Es solo el punto de entrada de Spring Boot

### Cómo añadir más exclusiones

Si quieres excluir más clases/paquetes, edita el `pom.xml`:

```xml
<excludes>
    <!-- Exclusiones existentes -->
    <exclude>**/*Entity.class</exclude>

    <!-- Añade tus exclusiones aquí -->
    <exclude>**/mypackage/NotImportant.class</exclude>
</excludes>
```

**Patrones comunes:**
- `**/*Test.class` - Excluye todas las clases de test
- `**/dto/**` - Excluye todo el paquete dto
- `**/MyClass.class` - Excluye una clase específica

## Interpretando el reporte

### Ejemplo de reporte

```
Package                           Missed Instr.  Cov.   Missed Branches  Cov.
────────────────────────────────────────────────────────────────────────────
user.application.service          0 of 185       100%   0 of 4           100%
user.domain.model                 26 of 52       50%    n/a              n/a
notifications.application         68 of 72       5%     4 of 4           0%
```

### ¿Qué significa cada columna?

- **Missed Instr.** - Instrucciones no cubiertas vs total
- **Cov.** - Porcentaje de cobertura
- **Missed Branches** - Ramas no cubiertas vs total
- **n/a** - No aplica (no hay ramas en ese código)

### Colores en el reporte HTML

- 🟢 **Verde** - Código cubierto por tests
- 🔴 **Rojo** - Código NO cubierto por tests
- 🟡 **Amarillo** - Rama parcialmente cubierta (solo se testea uno de los caminos)

## Estrategias para aumentar la cobertura

### 1. Identifica el código sin cubrir
```bash
# Genera el reporte
./mvnw clean test

# Abre el reporte
open target/site/jacoco/index.html
```

Navega por los paquetes y clases para ver qué líneas están en rojo.

### 2. Prioriza por impacto

**Prioridad ALTA** (testear primero):
- ✅ Lógica de negocio (Application/Domain layer)
- ✅ Validaciones
- ✅ Cálculos complejos
- ✅ Manejo de errores

**Prioridad MEDIA**:
- Adapters (Input/Output)
- Mappers
- Conversiones

**Prioridad BAJA** (considerar excluir):
- DTOs simples (solo getters/setters)
- Configuración
- Código generado

### 3. Cubre las ramas faltantes

Si ves cobertura de líneas alta pero de ramas baja:

```java
// Ejemplo: solo testeas el caso happy path
public String process(String input) {
    if (input == null) {        // ❌ Rama FALSE no cubierta
        return "default";
    }
    return input.toUpperCase();
}

// Necesitas testear ambos casos:
@Test
void shouldReturnDefault_whenInputIsNull() {
    assertEquals("default", process(null)); // Cubre rama TRUE
}

@Test
void shouldReturnUpperCase_whenInputIsValid() {
    assertEquals("HELLO", process("hello")); // Cubre rama FALSE
}
```

## Integración con CI/CD

### GitHub Actions (actualmente NO valida cobertura)

Los workflows actuales NO verifican cobertura:
- `.github/workflows/ci.yml` - Solo ejecuta `./mvnw test`
- `.github/workflows/build.yml` - Ejecuta `./mvnw clean install -DskipTests`

### Cómo habilitar validación en CI/CD

Modifica `.github/workflows/ci.yml`:

```yaml
- name: Run tests with coverage check
  run: ./mvnw clean install  # Cambia de 'test' a 'install'
```

Esto hará que el CI/CD falle si la cobertura < 80%.

### Generar badge de cobertura

Puedes añadir un badge en tu README:

```markdown
![Coverage](https://img.shields.io/badge/coverage-80%25-brightgreen)
```

## Cambiar el porcentaje mínimo

### Reducir el requisito (de 80% a 70%)

Si consideras que 80% es muy estricto, puedes bajarlo:

**Opción 1: Cambiar las variables en el pom.xml**
```xml
<!-- En la sección <properties> -->
<jacoco.coverage.minimum>0.70</jacoco.coverage.minimum>                    <!-- Cambiar líneas: de 0.80 a 0.70 -->
<jacoco.coverage.minimum.branches>0.60</jacoco.coverage.minimum.branches>  <!-- Cambiar branches: de 0.65 a 0.60 -->
```

**Opción 2: Valores diferentes para líneas y branches**

La configuración actual ya usa variables diferentes:
```xml
<!-- En properties -->
<jacoco.coverage.minimum>0.80</jacoco.coverage.minimum>                    <!-- Líneas: 80% -->
<jacoco.coverage.minimum.branches>0.65</jacoco.coverage.minimum.branches>  <!-- Branches: 65% -->

<!-- En la configuración del plugin -->
<limits>
    <limit>
        <counter>LINE</counter>
        <value>COVEREDRATIO</value>
        <minimum>${jacoco.coverage.minimum}</minimum>  <!-- Usa la variable de líneas -->
    </limit>
    <limit>
        <counter>BRANCH</counter>
        <value>COVEREDRATIO</value>
        <minimum>${jacoco.coverage.minimum.branches}</minimum>  <!-- Usa la variable de branches -->
    </limit>
</limits>
```

**Ventaja**: Centralizas los valores en un solo lugar (properties) para fácil mantenimiento.

### Aumentar el requisito (de 80% a 90%)

Si quieres ser más estricto:

```xml
<!-- En la sección <properties> -->
<jacoco.coverage.minimum>0.90</jacoco.coverage.minimum>                    <!-- Cambiar líneas: de 0.80 a 0.90 -->
<jacoco.coverage.minimum.branches>0.80</jacoco.coverage.minimum.branches>  <!-- Cambiar branches: de 0.65 a 0.80 -->
```

## Tipos de contadores disponibles

JaCoCo puede medir diferentes aspectos:

```xml
<counter>LINE</counter>         <!-- Líneas de código -->
<counter>BRANCH</counter>       <!-- Ramas (decisiones if/switch) -->
<counter>INSTRUCTION</counter>  <!-- Instrucciones bytecode -->
<counter>COMPLEXITY</counter>   <!-- Complejidad ciclomática -->
<counter>METHOD</counter>       <!-- Métodos -->
<counter>CLASS</counter>        <!-- Clases -->
```

**Recomendación:** Usa `LINE` y `BRANCH` (como está configurado actualmente).

## Troubleshooting

### Problema: "Coverage is 0%"
**Causa:** Los tests no se están ejecutando o JaCoCo no está instrumentando las clases.

**Solución:**
```bash
# Limpia todo y vuelve a ejecutar
./mvnw clean test
```

### Problema: "Código generado cuenta en la cobertura"
**Causa:** La exclusión no está bien configurada.

**Solución:** Verifica que el patrón coincida con el path de tus clases:
```xml
<exclude>**/ruta/a/tu/codigo/generado/**</exclude>
```

### Problema: "Build falla por cobertura baja"
**Causa:** No tienes suficientes tests.

**Solución:**
1. Genera el reporte: `./mvnw clean test`
2. Abre `target/site/jacoco/index.html`
3. Identifica clases con baja cobertura
4. Añade tests para esas clases
5. Vuelve a ejecutar: `./mvnw clean install`

## Referencias

- [Documentación oficial de JaCoCo](https://www.jacoco.org/jacoco/trunk/doc/)
- [Maven JaCoCo Plugin](https://www.eclemma.org/jacoco/trunk/doc/maven.html)
- [Guía de cobertura de tests](https://martinfowler.com/bliki/TestCoverage.html)

## Conclusión

**JaCoCo es tu aliado para mantener la calidad del código.** No se trata de tener 100% de cobertura (eso es casi imposible y poco práctico), sino de:
- ✅ Testear la lógica crítica de negocio
- ✅ Asegurar que las validaciones funcionan
- ✅ Cubrir los casos edge (bordes)
- ✅ Poder refactorizar con confianza

**Recomendación:** 80% es un buen objetivo. Menos de 70% suele indicar falta de tests. Más de 90% puede ser overkill a menos que sea código crítico (finanzas, salud, seguridad).
