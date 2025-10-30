# Spring Security + JWT - Guía Completa

> **Objetivo**: Entender cómo funciona la autenticación y autorización con JWT en este proyecto, desde los conceptos básicos hasta la implementación completa con Spring Security.

---

## 📚 Tabla de Contenidos

1. [Conceptos Fundamentales](#1-conceptos-fundamentales)
2. [¿Qué es JWT?](#2-qué-es-jwt)
3. [Spring Security - Arquitectura](#3-spring-security---arquitectura)
4. [Implementación en el Proyecto](#4-implementación-en-el-proyecto)
5. [Roles y Autorización](#5-roles-y-autorización)
6. [Flujo Completo de una Request](#6-flujo-completo-de-una-request)
7. [Ejemplos Prácticos](#7-ejemplos-prácticos)
8. [Testing de Seguridad](#8-testing-de-seguridad)
9. [Best Practices](#9-best-practices)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. Conceptos Fundamentales

### 1.1. Autenticación vs Autorización

Dos conceptos diferentes que a menudo se confunden:

```
┌─────────────────────────────────────────────────────────────┐
│ AUTENTICACIÓN (Authentication)                              │
│ "¿QUIÉN eres?"                                              │
│                                                             │
│ ✅ Verificar identidad del usuario                          │
│ ✅ Login con username/password                              │
│ ✅ Generar token JWT después de validar credenciales       │
│                                                             │
│ Ejemplo: El usuario introduce "admin" + "password123"      │
│          El sistema valida y devuelve un token JWT         │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ AUTORIZACIÓN (Authorization)                                │
│ "¿QUÉ puedes hacer?"                                        │
│                                                             │
│ ✅ Verificar permisos del usuario                           │
│ ✅ Comprobar roles (ADMIN, MANAGER, VIEWER, etc.)          │
│ ✅ Permitir o denegar acceso a recursos                    │
│                                                             │
│ Ejemplo: Usuario con rol VIEWER intenta crear un usuario   │
│          Sistema rechaza: 403 Forbidden (sin permisos)     │
└─────────────────────────────────────────────────────────────┘
```

**En este proyecto:**
- **Autenticación**: Validar token JWT en cada request
- **Autorización**: Verificar que el usuario tiene el rol adecuado para el endpoint

### 1.2. Stateless vs Stateful

```
┌─────────────────────────────────────────────────────────────┐
│ STATEFUL (Sesiones HTTP tradicionales)                      │
│                                                             │
│ Cliente → Login → Servidor crea sesión → Cookie de sesión  │
│                                                             │
│ ❌ Servidor guarda estado en memoria/DB                    │
│ ❌ Difícil escalar horizontalmente                         │
│ ❌ Problemas en arquitectura de microservicios             │
│ ✅ Sesión se puede invalidar desde el servidor             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ STATELESS (JWT) ← LO QUE USAMOS                            │
│                                                             │
│ Cliente → Login → Token JWT → Cliente guarda token         │
│                                                             │
│ ✅ Servidor NO guarda estado                               │
│ ✅ Fácil escalar horizontalmente                           │
│ ✅ Perfecto para microservicios                            │
│ ❌ Token no se puede invalidar antes de expirar            │
└─────────────────────────────────────────────────────────────┘
```

**¿Por qué JWT?**
- Ideal para **API REST** y **microservicios**
- No requiere base de datos de sesiones
- El token contiene toda la información necesaria
- Permite **Single Sign-On (SSO)** entre servicios

---

## 2. ¿Qué es JWT?

### 2.1. Estructura de un JWT

JWT (JSON Web Token) tiene **3 partes** separadas por puntos:

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbi51c2VyIiwicm9sZXMiOlsiQURNSU4iXX0.SflKxwRJ...
│                    │                                              │
│    HEADER          │           PAYLOAD                           │  SIGNATURE
│                    │                                              │
│ Algoritmo usado    │ Datos del usuario (claims)                  │ Firma para validar
```

#### 2.1.1. Header (Encabezado)

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

- `alg`: Algoritmo de firma (HMAC-SHA256 en nuestro caso)
- `typ`: Tipo de token (JWT)

#### 2.1.2. Payload (Carga útil)

```json
{
  "sub": "admin.user",              // Subject (username)
  "roles": ["ADMIN", "MANAGER"],    // Claims custom (roles)
  "iat": 1699000000,                // Issued At (timestamp creación)
  "exp": 1699086400                 // Expiration (timestamp expiración)
}
```

**Claims estándar:**
- `sub` (subject): Identifica al usuario (username)
- `iat` (issued at): Cuándo se creó el token
- `exp` (expiration): Cuándo expira el token

**Claims custom:**
- `roles`: Lista de roles del usuario (definido por nosotros)

#### 2.1.3. Signature (Firma)

```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)
```

- Garantiza que el token **NO ha sido modificado**
- Solo se puede crear con la **secret key** del servidor
- Si alguien modifica el payload, la firma no coincide → token inválido

### 2.2. ¿Cómo funciona JWT?

```mermaid
sequenceDiagram
    participant Client as Cliente
    participant Auth as Auth Service
    participant API as User Service

    Note over Client,API: 1. AUTENTICACIÓN (Login)
    Client->>Auth: POST /auth/login<br/>{username, password}
    Auth->>Auth: Validar credenciales
    Auth->>Auth: Generar JWT<br/>(incluye username + roles)
    Auth-->>Client: 200 OK<br/>{token: "eyJhbGc..."}

    Note over Client,API: 2. AUTORIZACIÓN (Request con token)
    Client->>API: GET /api/users/123<br/>Authorization: Bearer eyJhbGc...
    API->>API: Validar JWT<br/>(firma + expiración)
    API->>API: Extraer username + roles
    API->>API: Verificar permisos
    API-->>Client: 200 OK<br/>{user data}

    Note over Client,API: 3. TOKEN INVÁLIDO
    Client->>API: GET /api/users/456<br/>Authorization: Bearer INVALID_TOKEN
    API->>API: Validar JWT → FALLA
    API-->>Client: 401 Unauthorized
```

**Flujo paso a paso:**

1. **Login (Autenticación)**:
   - Cliente envía username + password
   - Auth Service valida credenciales
   - Auth Service genera JWT con username y roles
   - Cliente guarda el token (localStorage, cookies, etc.)

2. **Request con token (Autorización)**:
   - Cliente incluye token en header `Authorization: Bearer {token}`
   - API valida firma del token (con secret key)
   - API verifica que no ha expirado
   - API extrae username y roles del payload
   - API verifica que el usuario tiene permisos para el endpoint

3. **Token inválido o expirado**:
   - API rechaza con `401 Unauthorized`
   - Cliente debe obtener un nuevo token (re-login)

---

## 3. Spring Security - Arquitectura

### 3.1. Filter Chain (Cadena de Filtros)

Spring Security funciona con una **cadena de filtros** que interceptan cada HTTP request:

```
HTTP Request
    ↓
┌─────────────────────────────────────┐
│ SecurityContextPersistenceFilter    │ ← Crea SecurityContext
└─────────────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│ *** JwtAuthenticationFilter ***     │ ← NUESTRO FILTRO
│ (Valida JWT y crea Authentication)  │ ← Extrae token del header
└─────────────────────────────────────┘ ← Valida firma y expiration
    ↓                                     ← Extrae username y roles
┌─────────────────────────────────────┐ ← Guarda en SecurityContext
│ UsernamePasswordAuthFilter          │
└─────────────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│ FilterSecurityInterceptor           │ ← Verifica autorización
│ (Verifica roles y permisos)         │ ← hasRole("ADMIN"), etc.
└─────────────────────────────────────┘
    ↓
Controller (si autorizado)
    ↓
HTTP Response
```

**Nuestro filtro custom (`JwtAuthenticationFilter`):**
1. Extrae token del header `Authorization: Bearer {token}`
2. Valida el token (firma + expiración)
3. Extrae username y roles
4. Crea un `Authentication` object
5. Lo guarda en `SecurityContextHolder`
6. Spring Security usa esto para autorización

### 3.2. SecurityContext

```java
// SecurityContext es un ThreadLocal que guarda el Authentication
SecurityContext context = SecurityContextHolder.getContext();

// Authentication contiene:
// - Principal: username del usuario
// - Credentials: password (null en nuestro caso, usamos JWT)
// - Authorities: roles del usuario (ROLE_ADMIN, etc.)
Authentication authentication = context.getAuthentication();

String username = authentication.getName();              // "admin.user"
Collection<? extends GrantedAuthority> roles =
    authentication.getAuthorities();                     // [ROLE_ADMIN, ROLE_MANAGER]
```

---

## 4. Implementación en el Proyecto

### 4.1. Estructura de Archivos

```
src/main/java/com/example/hexarch/
│
├── shared/
│   ├── domain/
│   │   └── security/
│   │       └── Role.java                    ← Enum: ADMIN, MANAGER, VIEWER, SUPPLIER
│   │
│   └── infrastructure/
│       └── security/
│           ├── SecurityConfig.java          ← Configuración Spring Security
│           └── jwt/
│               ├── JwtProperties.java       ← Configuración JWT (secret, expiration)
│               ├── JwtTokenProvider.java    ← Genera y valida tokens
│               ├── JwtAuthenticationFilter.java  ← Filtro HTTP
│               └── JwtAuthenticationEntryPoint.java  ← Manejo de errores 401
│
src/main/resources/
└── application.yaml                         ← Configuración JWT
```

### 4.2. Role Enum (Domain)

```java
// shared/domain/security/Role.java
public enum Role {
    ADMIN,      // Acceso completo
    MANAGER,    // Puede crear/leer usuarios
    VIEWER,     // Solo lectura
    SUPPLIER;   // Acceso limitado

    public String getAuthority() {
        return "ROLE_" + this.name();  // Spring Security espera prefijo ROLE_
    }
}
```

**¿Por qué en `shared/domain`?**
- Los roles son un concepto de negocio (dominio)
- Se usan en múltiples bounded contexts (User, Auth, etc.)
- Son independientes de Spring Security (framework)

### 4.3. JwtTokenProvider (Core Component)

```java
// shared/infrastructure/security/jwt/JwtTokenProvider.java
@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    // 1. Genera un token JWT
    public String generateToken(String username, List<Role> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
            .subject(username)                           // "admin.user"
            .claim("roles", roleNames)                   // ["ADMIN", "MANAGER"]
            .issuedAt(now)                               // Timestamp creación
            .expiration(expiryDate)                      // Timestamp expiración
            .signWith(secretKey, Jwts.SIG.HS256)        // Firma HMAC-SHA256
            .compact();
    }

    // 2. Valida un token JWT
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    // 3. Extrae username del token
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    // 4. Extrae roles del token
    public List<Role> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        List<String> roleNames = claims.get("roles", List.class);
        return roleNames.stream()
            .map(Role::fromString)
            .collect(Collectors.toList());
    }
}
```

### 4.4. JwtAuthenticationFilter (HTTP Filter)

```java
// shared/infrastructure/security/jwt/JwtAuthenticationFilter.java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extraer token del header Authorization
        String token = extractTokenFromRequest(request);

        // 2. Validar token
        if (token != null && jwtTokenProvider.validateToken(token)) {

            // 3. Extraer información del token
            String username = jwtTokenProvider.getUsernameFromToken(token);
            List<Role> roles = jwtTokenProvider.getRolesFromToken(token);

            // 4. Convertir roles a GrantedAuthorities
            List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toList());

            // 5. Crear Authentication
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    authorities
                );

            // 6. Guardar en SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 7. Continuar con el siguiente filtro
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // Quitar "Bearer "
        }
        return null;
    }
}
```

### 4.5. SecurityConfig (Configuración Principal)

```java
// shared/infrastructure/security/SecurityConfig.java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Desactivar CSRF (no necesario con JWT)
            .csrf(AbstractHttpConfigurer::disable)

            // 2. Configurar manejo de errores
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )

            // 3. STATELESS (no sesiones)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 4. Configurar autorización
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers("/actuator/health", "/swagger-ui/**").permitAll()

                // Endpoints protegidos
                .requestMatchers(HttpMethod.POST, "/api/users")
                    .hasAnyRole("ADMIN", "MANAGER")  // Solo ADMIN o MANAGER

                .requestMatchers(HttpMethod.GET, "/api/users/**")
                    .authenticated()  // Cualquier usuario autenticado

                .anyRequest().authenticated()
            )

            // 5. Añadir filtro JWT
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
```

### 4.6. Configuración (application.yaml)

```yaml
jwt:
  # Clave secreta para firmar tokens (256 bits mínimo)
  secret: 5JYzCkNP8xQ2mVnRwTaFbGdKeShXrUvYy3t6v9yBxE4=

  # Tiempo de expiración en milisegundos (24 horas)
  expiration: 86400000
```

**⚠️ IMPORTANTE - Seguridad del Secret:**
- En **desarrollo**: OK en application.yaml
- En **producción**: usar variables de entorno
  ```yaml
  jwt:
    secret: ${JWT_SECRET}  # Variable de entorno
  ```
- O usar **AWS Secrets Manager**, **Azure Key Vault**, **HashiCorp Vault**

---

## 5. Roles y Autorización

### 5.1. Roles Implementados

```
┌────────────────────────────────────────────────────────────┐
│ ADMIN                                                      │
│ ✅ Acceso completo                                         │
│ ✅ Crear, leer, actualizar, eliminar usuarios             │
│ ✅ Acceso a todas las operaciones                         │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│ MANAGER                                                    │
│ ✅ Crear usuarios                                          │
│ ✅ Leer usuarios                                           │
│ ❌ NO puede eliminar usuarios                             │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│ VIEWER                                                     │
│ ✅ Solo lectura                                            │
│ ✅ Ver información de usuarios                            │
│ ❌ NO puede realizar operaciones de escritura             │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│ SUPPLIER                                                   │
│ ✅ Acceso limitado a recursos específicos                 │
│ ✅ Puede ver solo sus propios datos                       │
│ ❌ Acceso restringido                                     │
└────────────────────────────────────────────────────────────┘
```

### 5.2. Matriz de Permisos

| Endpoint | Método | ADMIN | MANAGER | VIEWER | SUPPLIER | Sin Auth |
|----------|--------|-------|---------|--------|----------|----------|
| `/api/users` | POST | ✅ | ✅ | ❌ | ❌ | ❌ |
| `/api/users/{id}` | GET | ✅ | ✅ | ✅ | ✅ | ❌ |
| `/actuator/health` | GET | ✅ | ✅ | ✅ | ✅ | ✅ |
| `/swagger-ui/**` | GET | ✅ | ✅ | ✅ | ✅ | ✅ |

**Leyenda:**
- ✅ Permitido
- ❌ Denegado (403 Forbidden si autenticado, 401 si no autenticado)

### 5.3. Múltiples Roles

Un usuario puede tener **múltiples roles**:

```java
// Generar token con múltiples roles
String token = jwtTokenProvider.generateToken(
    "multi.role.user",
    List.of(Role.ADMIN, Role.MANAGER, Role.VIEWER)
);

// JWT payload contendrá:
// {
//   "sub": "multi.role.user",
//   "roles": ["ADMIN", "MANAGER", "VIEWER"]
// }
```

Spring Security evaluará si el usuario tiene **al menos uno** de los roles requeridos:

```java
.requestMatchers(HttpMethod.POST, "/api/users")
    .hasAnyRole("ADMIN", "MANAGER")  // ✅ Usuario tiene ambos
```

---

## 6. Flujo Completo de una Request

### 6.1. Caso 1: Request Exitoso (Usuario con permisos)

```mermaid
sequenceDiagram
    participant C as Cliente
    participant F as JwtAuthenticationFilter
    participant JP as JwtTokenProvider
    participant SC as SecurityContext
    participant Ctrl as UserController

    C->>F: POST /api/users<br/>Authorization: Bearer eyJhbGc...
    F->>F: Extraer token del header
    F->>JP: validateToken(token)
    JP-->>F: true (válido)
    F->>JP: getUsernameFromToken(token)
    JP-->>F: "admin.user"
    F->>JP: getRolesFromToken(token)
    JP-->>F: [ADMIN, MANAGER]
    F->>F: Crear Authentication
    F->>SC: Guardar Authentication
    F->>Ctrl: Continuar (autorizado)
    Ctrl->>Ctrl: Procesar request
    Ctrl-->>C: 201 Created
```

**Paso a paso:**

1. Cliente envía request con token JWT
2. `JwtAuthenticationFilter` intercepta la request
3. Extrae token del header `Authorization`
4. Valida token con `JwtTokenProvider`
5. Extrae username y roles
6. Crea `Authentication` object
7. Lo guarda en `SecurityContext`
8. Spring Security verifica autorización (el usuario tiene rol ADMIN)
9. Request llega al controller
10. Controller procesa y devuelve respuesta

### 6.2. Caso 2: Request sin Token (401 Unauthorized)

```mermaid
sequenceDiagram
    participant C as Cliente
    participant F as JwtAuthenticationFilter
    participant AEP as AuthenticationEntryPoint

    C->>F: POST /api/users<br/>(sin header Authorization)
    F->>F: Extraer token → null
    F->>F: No hay Authentication
    Note over F: Request llega a FilterSecurityInterceptor
    F->>AEP: commence() - No autenticado
    AEP-->>C: 401 Unauthorized<br/>{error: "Unauthorized"}
```

### 6.3. Caso 3: Request con Rol Insuficiente (403 Forbidden)

```mermaid
sequenceDiagram
    participant C as Cliente (VIEWER)
    participant F as JwtAuthenticationFilter
    participant JP as JwtTokenProvider
    participant SC as SecurityContext

    C->>F: POST /api/users<br/>Authorization: Bearer eyJhbGc... (VIEWER)
    F->>JP: validateToken(token)
    JP-->>F: true
    F->>JP: getRolesFromToken(token)
    JP-->>F: [VIEWER]
    F->>SC: Guardar Authentication (VIEWER)
    Note over F,SC: Spring Security verifica:<br/>Requiere ADMIN o MANAGER<br/>Usuario tiene VIEWER
    F-->>C: 403 Forbidden
```

---

## 7. Ejemplos Prácticos

### 7.1. Generar Token JWT (para testing)

En un escenario real, el **Auth Service** genera tokens después del login. Para testing, podemos usar el `JwtTokenProvider`:

```java
@Autowired
private JwtTokenProvider jwtTokenProvider;

// Generar token para ADMIN
String adminToken = jwtTokenProvider.generateToken(
    "admin.user",
    List.of(Role.ADMIN)
);

// Generar token para MANAGER
String managerToken = jwtTokenProvider.generateToken(
    "manager.user",
    List.of(Role.MANAGER)
);

// Generar token para VIEWER
String viewerToken = jwtTokenProvider.generateToken(
    "viewer.user",
    List.of(Role.VIEWER)
);
```

### 7.2. Curl - Ejemplos de Requests

#### Endpoint Público (sin autenticación)

```bash
# GET /actuator/health - Público
curl http://localhost:8080/actuator/health

# Respuesta: 200 OK
{
  "status": "UP"
}
```

#### POST /api/users - Crear Usuario (requiere ADMIN o MANAGER)

```bash
# Sin token → 401 Unauthorized
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@test.com"
  }'

# Respuesta: 401 Unauthorized
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

```bash
# Con token ADMIN → 201 Created
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "username": "newuser",
    "email": "newuser@test.com"
  }'

# Respuesta: 201 Created
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "newuser",
  "email": "newuser@test.com",
  "createdAt": "2025-10-30T12:00:00Z"
}
```

```bash
# Con token VIEWER → 403 Forbidden
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "username": "newuser",
    "email": "newuser@test.com"
  }'

# Respuesta: 403 Forbidden
```

#### GET /api/users/{id} - Obtener Usuario (cualquier rol autenticado)

```bash
# Con cualquier token válido → 200 OK
curl http://localhost:8080/api/users/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# Respuesta: 200 OK
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "existinguser",
  "email": "existing@test.com"
}
```

### 7.3. Postman / Insomnia

1. **Crear una request**:
   - Method: `POST`
   - URL: `http://localhost:8080/api/users`

2. **Headers**:
   ```
   Content-Type: application/json
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
   ```

3. **Body** (JSON):
   ```json
   {
     "username": "postman.user",
     "email": "postman@test.com"
   }
   ```

---

## 8. Testing de Seguridad

### 8.1. Unit Tests

```java
// src/test/java/.../JwtTokenProviderTest.java
@Test
void shouldGenerateValidJwtToken() {
    // GIVEN
    String username = "testuser";
    List<Role> roles = List.of(Role.ADMIN, Role.MANAGER);

    // WHEN
    String token = jwtTokenProvider.generateToken(username, roles);

    // THEN
    assertThat(token).isNotNull();
    assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo(username);
}
```

### 8.2. Integration Tests

```java
// src/test/java/.../SecurityIntegrationTest.java
@Test
void shouldReturn401WhenCreatingUserWithoutToken() throws Exception {
    // WHEN & THEN
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"test\",\"email\":\"test@test.com\"}"))
        .andExpect(status().isUnauthorized());
}

@Test
void shouldCreateUserWithAdminRole() throws Exception {
    // GIVEN
    String adminToken = "Bearer " + jwtTokenProvider.generateToken(
        "admin.user",
        List.of(Role.ADMIN)
    );

    // WHEN & THEN
    mockMvc.perform(post("/api/users")
            .header("Authorization", adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"test\",\"email\":\"test@test.com\"}"))
        .andExpect(status().isCreated());
}
```

---

## 9. Best Practices

### 9.1. Seguridad del Secret Key

❌ **MAL** - Hardcodear en código:
```java
private static final String SECRET = "my-secret-key";
```

❌ **MAL** - Commitear a Git:
```yaml
jwt:
  secret: hardcoded-secret-in-git
```

✅ **BIEN** - Variables de entorno:
```yaml
jwt:
  secret: ${JWT_SECRET}
```

```bash
# .env (NO commitear)
JWT_SECRET=5JYzCkNP8xQ2mVnRwTaFbGdKeShXrUvYy3t6v9yBxE4=
```

✅ **MEJOR** - Secrets Manager:
- AWS Secrets Manager
- Azure Key Vault
- HashiCorp Vault
- Google Cloud Secret Manager

### 9.2. Tiempo de Expiración

```
Access Tokens (corta duración):
- 15 minutos: Muy seguro, requiere refresh frecuente
- 1 hora: Balance seguridad/UX
- 24 horas: Más cómodo, menos seguro

Refresh Tokens (larga duración):
- 7 días: Estándar
- 30 días: Para apps móviles
- 90 días: Máximo recomendado
```

**En este proyecto**: 24 horas (educativo, no producción)

### 9.3. HTTPS Obligatorio

⚠️ **NUNCA uses JWT sobre HTTP en producción**

```
HTTP (sin cifrar):
❌ Token visible en plain text
❌ Man-in-the-middle puede robar el token

HTTPS (cifrado):
✅ Token cifrado en tránsito
✅ Protección contra ataques
```

### 9.4. Refresh Tokens

Este proyecto usa **solo access tokens**. En producción, implementar **refresh tokens**:

```
┌─────────────────────────────────────────────────────────────┐
│ Access Token (corta duración, 15 min)                       │
│ - Se envía en cada request                                  │
│ - Se incluye en Authorization header                        │
│ - Si expira, usa refresh token para obtener uno nuevo      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ Refresh Token (larga duración, 7 días)                      │
│ - Se usa SOLO para obtener nuevo access token              │
│ - Se guarda de forma segura (httpOnly cookie)              │
│ - Si expira, usuario debe hacer login de nuevo             │
└─────────────────────────────────────────────────────────────┘
```

### 9.5. Invalidación de Tokens

JWT es **stateless**, por lo que **no se puede invalidar** antes de expirar.

**Soluciones:**

1. **Token Blacklist** (Redis):
   ```
   Cuando usuario hace logout:
   1. Añadir token a blacklist en Redis
   2. TTL = tiempo restante hasta expiración
   3. En cada request, verificar si token está en blacklist
   ```

2. **Token Versioning**:
   ```
   JWT payload: { "sub": "user", "version": 2 }
   DB: users table has token_version column

   Si cambias password o logout:
   1. Incrementar token_version en DB
   2. Tokens antiguos con version < 2 → inválidos
   ```

3. **Expiración Corta + Refresh Token**:
   ```
   Access token: 15 minutos
   Si roban el token, solo es válido 15 minutos
   ```

---

## 10. Troubleshooting

### 10.1. Error: "401 Unauthorized" en endpoint público

**Problema**: El endpoint debería ser público pero devuelve 401.

**Solución**: Verificar que el endpoint está en la lista de públicos:

```java
.requestMatchers("/actuator/health", "/swagger-ui/**").permitAll()
```

### 10.2. Error: "403 Forbidden" cuando debería funcionar

**Problema**: Usuario tiene el rol pero recibe 403.

**Causas posibles:**

1. **Prefijo ROLE_ faltante**:
   ```java
   // ❌ MAL
   .hasRole("ROLE_ADMIN")  // Spring añade ROLE_ automáticamente

   // ✅ BIEN
   .hasRole("ADMIN")  // Spring lo convierte a ROLE_ADMIN
   ```

2. **Token no tiene roles**:
   - Verificar que el token incluye claim "roles"
   - Usar un debugger JWT: https://jwt.io

3. **Método HTTP incorrecto**:
   ```java
   .requestMatchers(HttpMethod.POST, "/api/users")  // Solo POST
   ```

### 10.3. Error: "Invalid JWT signature"

**Problema**: Token rechazado por firma inválida.

**Causas:**

1. **Secret key diferente**:
   - Servidor A generó token con secret1
   - Servidor B intenta validar con secret2
   - **Solución**: Mismo secret en todos los servicios

2. **Token modificado manualmente**:
   - Si cambias el payload, la firma no coincide
   - **Solución**: Generar nuevo token

### 10.4. Error: "JWT token is expired"

**Problema**: Token expirado.

**Solución**: Cliente debe obtener nuevo token (re-login o refresh token).

```bash
# Verificar expiración del token
# En https://jwt.io, pegar el token y ver el claim "exp"
{
  "exp": 1699086400  # Unix timestamp
}
```

### 10.5. Spring Security bloquea todo (incluso endpoints públicos)

**Problema**: Después de añadir Spring Security, nada funciona.

**Causa**: Spring Security por defecto bloquea TODO.

**Solución**: Configurar `SecurityFilterChain` correctamente:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/public/**").permitAll()  // Públicos primero
        .anyRequest().authenticated()               // Resto autenticado
    );
    return http.build();
}
```

---

## 🎯 Resumen

### ✅ Lo que hemos implementado:

1. **JWT**: Tokens stateless con username y roles
2. **Spring Security**: Filtro custom para validar JWT
3. **Roles**: ADMIN, MANAGER, VIEWER, SUPPLIER
4. **Autorización**: Por endpoint y método HTTP
5. **Manejo de errores**: 401 Unauthorized, 403 Forbidden
6. **Tests**: Unit + Integration tests de seguridad

### 🔐 Conceptos clave:

- **Autenticación**: ¿Quién eres? (validar token)
- **Autorización**: ¿Qué puedes hacer? (verificar roles)
- **Stateless**: No sesiones, cada request tiene token
- **JWT**: Header.Payload.Signature
- **Filter Chain**: Cadena de filtros de Spring Security

### 📚 Próximos pasos:

1. Implementar **Refresh Tokens** (producción)
2. Añadir **Token Blacklist** (Redis) para logout
3. Implementar **Rate Limiting** (prevenir ataques)
4. Añadir **Audit Logging** (registrar accesos)
5. Configurar **CORS** (si tienes frontend en otro dominio)

---

## 📖 Referencias

- [Spring Security Official Docs](https://docs.spring.io/spring-security/reference/index.html)
- [JWT.io](https://jwt.io) - Debugger de tokens JWT
- [JJWT Library](https://github.com/jwtk/jjwt) - Librería Java para JWT
- [OWASP JWT Cheatsheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
- [Spring Security Architecture](https://spring.io/guides/topicals/spring-security-architecture)

---

**🎓 Ejercicio práctico:**

1. Genera un token JWT para un usuario VIEWER
2. Intenta crear un usuario con ese token
3. Verifica que recibes 403 Forbidden
4. Genera un token JWT para ADMIN
5. Crea un usuario exitosamente
6. Usa https://jwt.io para inspeccionar ambos tokens

¡Felicidades! Ahora entiendes cómo funciona la seguridad JWT en una aplicación Spring Boot real. 🚀
