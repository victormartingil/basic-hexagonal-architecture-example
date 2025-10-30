# 📦 API Versioning Strategy - Hexarch

## Índice
- [¿Por Qué Versionar APIs?](#por-qué-versionar-apis)
- [Estrategia de Versionado](#estrategia-de-versionado)
- [Semantic Versioning](#semantic-versioning)
- [Breaking Changes](#breaking-changes)
- [Deprecation Policy](#deprecation-policy)
- [Backward Compatibility](#backward-compatibility)
- [Ejemplos Prácticos](#ejemplos-prácticos)

---

## ¿Por Qué Versionar APIs?

En un entorno empresarial, las APIs son **contratos** entre servicios y clientes. Versionar correctamente permite:

1. **Evolución sin ruptura**: Añadir features sin romper clientes existentes
2. **Tiempo para migrar**: Clientes tienen tiempo (6+ meses) para actualizar
3. **Múltiples versiones activas**: v1 y v2 coexisten durante migración
4. **Auditoría**: Trazabilidad de cambios API en cada versión

---

## Estrategia de Versionado

### Formato Actual: URL Path Versioning

```
GET /api/v1/users/{id}
POST /api/v1/users
```

**Ventajas:**
- ✅ Explícito y visible en URL
- ✅ Fácil de cachear (CDN, proxies)
- ✅ Compatible con Swagger/OpenAPI
- ✅ Estándar de facto en REST APIs

**Alternativas NO usadas** (por qué):
- ❌ **Header versioning** (`Accept: application/vnd.hexarch.v1+json`): Más complejo, no visible en navegadores
- ❌ **Query parameter** (`/api/users?version=1`): Dificulta caching
- ❌ **Subdomain** (`v1.api.hexarch.com`): Complejidad de infraestructura

---

## Semantic Versioning

Seguimos **Semantic Versioning 2.0.0** adaptado para APIs REST:

```
MAJOR.MINOR.PATCH

v1.2.3
│ │ │
│ │ └─ PATCH: Bug fixes (no cambios en contrato API)
│ └─── MINOR: New features (backward compatible)
└───── MAJOR: Breaking changes (incompatible)
```

### MAJOR Version (v1 → v2)

**Cuándo incrementar**:
- Eliminar endpoint existente
- Cambiar URL de endpoint
- Cambiar tipo de dato de campo (string → number)
- Hacer campo obligatorio que antes era opcional
- Cambiar formato de error response
- Cambiar comportamiento core de endpoint

**Ejemplo**:
```json
// v1: email es opcional
POST /api/v1/users
{
  "username": "johndoe"
}

// v2: email es OBLIGATORIO (BREAKING CHANGE)
POST /api/v2/users
{
  "username": "johndoe",
  "email": "john@example.com"  // REQUIRED
}
```

### MINOR Version (v1.0 → v1.1)

**Cuándo incrementar**:
- Añadir nuevo endpoint
- Añadir campo opcional en request
- Añadir campo en response
- Añadir valores a enum existente
- Añadir nuevo error code

**Ejemplo**:
```json
// v1.0: solo username y email
POST /api/v1/users
{
  "username": "johndoe",
  "email": "john@example.com"
}

// v1.1: añade campo OPCIONAL phoneNumber (BACKWARD COMPATIBLE)
POST /api/v1/users
{
  "username": "johndoe",
  "email": "john@example.com",
  "phoneNumber": "+34600123456"  // OPTIONAL - clientes v1.0 siguen funcionando
}
```

### PATCH Version (v1.0.0 → v1.0.1)

**Cuándo incrementar**:
- Fix de bugs que NO cambian contrato
- Mejoras de performance
- Cambios internos (refactoring)
- Actualizaciones de dependencias

**Ejemplo**:
```
v1.0.0: GET /api/v1/users/{id} devuelve 500 si ID no existe
v1.0.1: GET /api/v1/users/{id} devuelve 404 correctamente (bug fix)
```

---

## Breaking Changes

### Checklist: ¿Es un Breaking Change?

Use this checklist to determine if a change is breaking:

| Cambio | Breaking? | Versión |
|--------|-----------|---------|
| Añadir nuevo endpoint | ❌ NO | MINOR |
| Eliminar endpoint | ✅ SÍ | MAJOR |
| Añadir campo opcional en request | ❌ NO | MINOR |
| Añadir campo en response | ❌ NO | MINOR |
| Eliminar campo de response | ✅ SÍ | MAJOR |
| Cambiar tipo de campo | ✅ SÍ | MAJOR |
| Hacer campo obligatorio | ✅ SÍ | MAJOR |
| Cambiar HTTP status code | ✅ SÍ | MAJOR |
| Cambiar formato de error | ✅ SÍ | MAJOR |
| Renombrar campo | ✅ SÍ | MAJOR |
| Añadir validación más estricta | ✅ SÍ | MAJOR |
| Relajar validación | ❌ NO | MINOR |

### Regla de Oro

> **Si un cliente v1.0 deja de funcionar con tu cambio, es BREAKING CHANGE**

---

## Deprecation Policy

### Proceso de Deprecación (6 meses)

```
┌──────────────┬──────────────┬──────────────┬──────────────┐
│   Month 1    │   Month 2-5  │   Month 6    │   Month 7+   │
│  ANNOUNCE    │   MIGRATE    │   SUNSET     │   REMOVE     │
├──────────────┼──────────────┼──────────────┼──────────────┤
│ • v2 release │ • v1 & v2    │ • Warning    │ • v1 removed │
│ • Docs       │   coexist    │   logs       │ • Only v2    │
│ • Migration  │ • Support    │ • Final      │   active     │
│   guide      │   both       │   reminder   │              │
└──────────────┴──────────────┴──────────────┴──────────────┘
```

### Timeline Completo

#### Mes 1-2: ANNOUNCEMENT
1. **Publicar v2 de la API**
2. **Documentar breaking changes**
   ```markdown
   # CHANGELOG.md

   ## v2.0.0 - 2024-01-15

   ### BREAKING CHANGES
   - `email` field is now required in POST /api/v2/users
   - `createdAt` format changed from timestamp to ISO 8601

   ### Migration Guide
   See docs/migration-v1-to-v2.md
   ```

3. **Añadir deprecation warnings en v1**
   ```java
   @GetMapping("/api/v1/users/{id}")
   @Deprecated(since = "1.5.0", forRemoval = true)
   public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
       log.warn("DEPRECATED: /api/v1/users is deprecated. Migrate to /api/v2/users");
       // ... existing logic
   }
   ```

4. **Header de deprecation en responses**
   ```http
   HTTP/1.1 200 OK
   Deprecation: true
   Sunset: Sat, 15 Jul 2024 00:00:00 GMT
   Link: </api/v2/users>; rel="successor-version"
   ```

#### Mes 3-5: COEXISTENCE & MIGRATION
- Ambas versiones (v1 y v2) funcionan simultáneamente
- Soporte completo para v1
- Logs de uso de v1 para identificar clientes que aún no migraron
- Contactar a equipos que usan v1

#### Mes 6: SUNSET WARNING
- Últimas 2 semanas: logs WARNING cada request a v1
- Emails a equipos con clientes pendientes
- Dashboard con métricas de uso v1 vs v2

#### Mes 7+: REMOVAL
- Eliminar código de v1
- Solo v2 activa
- Requests a v1 devuelven 410 Gone

---

## Backward Compatibility

### Principios de Compatibilidad

#### 1. Additive Changes (Seguros)

**Añadir campos opcionales**:
```json
// v1.0
{
  "id": "123",
  "username": "johndoe"
}

// v1.1 (backward compatible)
{
  "id": "123",
  "username": "johndoe",
  "phoneNumber": "+34600123456"  // NUEVO pero OPCIONAL
}
```

**Clientes v1.0**:
- Ignoran campo `phoneNumber` (JSON permite campos extra)
- Siguen funcionando sin cambios ✅

#### 2. Tolerant Reader Pattern

**Clientes deben implementar**:
```typescript
// MAL: Cliente estricto (se rompe con campos nuevos)
interface UserV1 {
  id: string;
  username: string;
}

// BIEN: Cliente tolerante (ignora campos desconocidos)
interface UserV1 {
  id: string;
  username: string;
  [key: string]: any;  // Permite campos futuros
}
```

#### 3. Robustness Principle (Postel's Law)

> **"Be conservative in what you send, be liberal in what you accept"**

**Aplicado a APIs**:
- **Request**: Validar estrictamente (rechazar datos inválidos)
- **Response**: Aceptar laxamente (clientes ignoran campos desconocidos)

---

## Ejemplos Prácticos

### Ejemplo 1: Añadir Feature (MINOR)

**Requisito**: Añadir soporte para avatar de usuario

**v1.2.0 (backward compatible)**:
```json
POST /api/v1/users
{
  "username": "johndoe",
  "email": "john@example.com",
  "avatarUrl": "https://example.com/avatar.jpg"  // OPCIONAL
}
```

**Response**:
```json
{
  "id": "123",
  "username": "johndoe",
  "email": "john@example.com",
  "avatarUrl": "https://example.com/avatar.jpg",  // NUEVO campo en response
  "enabled": true,
  "createdAt": "2024-01-15T10:30:00.123Z"
}
```

**Clientes v1.0 y v1.1**:
- Pueden seguir creando usuarios sin `avatarUrl` ✅
- Si reciben `avatarUrl` en response, simplemente lo ignoran ✅

---

### Ejemplo 2: Cambio Breaking (MAJOR)

**Requisito**: Email debe ser obligatorio (antes era opcional)

**v2.0.0 (breaking change)**:
```json
POST /api/v2/users
{
  "username": "johndoe",
  "email": "john@example.com"  // AHORA OBLIGATORIO
}
```

**Validación**:
```java
@PostMapping("/api/v2/users")
public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequestV2 request) {
    // ...
}

// CreateUserRequestV2.java
public record CreateUserRequestV2(
    @NotBlank String username,
    @NotBlank @Email String email  // REQUIRED (en v1 era opcional)
) {}
```

**Migration Guide**:
```markdown
# Migration Guide: v1 → v2

## Breaking Change: Email is now required

### v1 (deprecated)
POST /api/v1/users
{
  "username": "johndoe"
}

### v2 (required email)
POST /api/v2/users
{
  "username": "johndoe",
  "email": "john@example.com"  // REQUIRED
}

### Action Required
Update your clients to always send email field.
```

---

## Best Practices

### 1. Documentar TODO en OpenAPI

```yaml
# openapi.yaml
/api/v1/users:
  post:
    deprecated: true
    x-sunset: "2024-07-15"
    x-successor: "/api/v2/users"
    description: |
      **DEPRECATED**: This endpoint will be removed on July 15, 2024.
      Please migrate to /api/v2/users
```

### 2. Monitorizar Uso de Versiones

```java
@Component
@Aspect
public class ApiVersionMetrics {

    @Around("@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public Object trackApiVersion(ProceedingJoinPoint joinPoint) {
        String path = extractPath(joinPoint);
        if (path.contains("/api/v1/")) {
            metricsRegistry.counter("api.version.v1.usage").increment();
        } else if (path.contains("/api/v2/")) {
            metricsRegistry.counter("api.version.v2.usage").increment();
        }
        return joinPoint.proceed();
    }
}
```

**Grafana Dashboard**:
```
API v1 Usage: [████████████████░░] 80% (decreasing ✅)
API v2 Usage: [████░░░░░░░░░░░░░░] 20% (increasing ✅)
```

### 3. Automatizar Tests de Compatibilidad

```java
@Test
void v1ClientShouldStillWorkWithV2Server() {
    // Simular cliente v1.0 (solo envía username)
    CreateUserRequestV1 requestV1 = new CreateUserRequestV1("johndoe");

    // Server v2 debe aceptar y añadir defaults
    ResponseEntity<UserResponse> response = restTemplate
        .postForEntity("/api/v2/users", requestV1, UserResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
}
```

---

## Checklist de Release

Antes de lanzar una nueva versión:

### Pre-Release
- [ ] ¿Los cambios son MAJOR, MINOR o PATCH?
- [ ] ¿Hay breaking changes? → Actualizar a vMAJOR
- [ ] ¿Se añadieron features? → Actualizar a vMINOR
- [ ] ¿Solo bug fixes? → Actualizar a vPATCH
- [ ] Actualizar CHANGELOG.md
- [ ] Generar migration guide (si es MAJOR)

### Release
- [ ] Actualizar version en pom.xml
- [ ] Generar OpenAPI spec con nueva versión
- [ ] Actualizar AsyncAPI spec (si aplica)
- [ ] Ejecutar tests de compatibilidad
- [ ] Deploy en staging
- [ ] Validar con smoke tests

### Post-Release
- [ ] Anunciar en Slack/Teams
- [ ] Enviar email a stakeholders
- [ ] Actualizar documentación
- [ ] Añadir deprecation warnings a versiones antiguas
- [ ] Crear Grafana dashboard para monitoring

---

## Recursos

- [Semantic Versioning 2.0.0](https://semver.org/)
- [REST API Versioning Guide (Microsoft)](https://learn.microsoft.com/en-us/azure/architecture/best-practices/api-design#versioning-a-restful-web-api)
- [API Deprecation (Stripe)](https://stripe.com/docs/upgrades#api-versions)
- [Sunset HTTP Header (RFC 8594)](https://datatracker.ietf.org/doc/html/rfc8594)
