# Guía de Testing en Colecciones de API

Guía completa sobre cómo escribir y entender tests en colecciones de API, enfocada principalmente en **Bruno** (herramienta principal del equipo) con referencias a Postman como alternativa.

---

## 📚 Índice

1. [¿Qué son los Tests en Colecciones?](#qué-son-los-tests-en-colecciones)
2. [¿Por qué son Importantes?](#por-qué-son-importantes)
3. [Bruno: Estructura y Sintaxis](#bruno-estructura-y-sintaxis)
4. [Tests vs Scripts en Bruno](#tests-vs-scripts-en-bruno)
5. [⭐ Acciones Principales (Recetas Rápidas)](#-acciones-principales-recetas-rápidas)
6. [Tipos de Tests Comunes](#tipos-de-tests-comunes)
7. [Mejores Prácticas](#mejores-prácticas)
8. [Ejemplos Prácticos](#ejemplos-prácticos)
9. [Postman: Diferencias y Similitudes](#postman-diferencias-y-similitudes)
10. [Casos de Uso Avanzados](#casos-de-uso-avanzados)
11. [Troubleshooting](#troubleshooting)

---

## ¿Qué son los Tests en Colecciones?

Los **tests** (o assertions) en colecciones de API son **código JavaScript que valida automáticamente** que la respuesta de la API cumple con lo esperado.

### Sin Tests (Manual)
```bash
# 1. Ejecutas el request
POST /api/v1/users

# 2. Ves la respuesta manualmente
{
  "id": "123",
  "username": "john"
}

# 3. Verificas manualmente:
#    ✅ ¿El status es 201?
#    ✅ ¿Tiene el campo "id"?
#    ✅ ¿El username es correcto?
```

### Con Tests (Automático)
```javascript
// Los tests validan automáticamente
test("Status is 201", function() {
  expect(res.getStatus()).to.equal(201);
});

test("Response has ID", function() {
  expect(res.getBody().id).to.exist;
});

// ✅ Si todo pasa: Tests PASSED ✓
// ❌ Si algo falla: Tests FAILED ✗ con detalle del error
```

---

## ¿Por qué son Importantes?

### 1. **Detección Automática de Errores**
Sin tests, puedes no notar que algo está mal:
```json
// ¿Notarías este error?
{
  "id": "123",
  "usernme": "john",  // ❌ Typo: "usernme" en lugar de "username"
  "enabled": "true"    // ❌ String en lugar de boolean
}
```

Con tests, se detecta automáticamente:
```javascript
test("Response has username field", function() {
  expect(res.getBody()).to.have.property('username');
  // ❌ FAIL: Expected property 'username' to exist
});
```

### 2. **Contratos de API**
Los tests documentan y verifican el contrato de la API:
```javascript
// Este test documenta Y verifica que:
// - El email debe tener formato válido
// - El email debe ser un string
test("Email is valid format", function() {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  expect(res.getBody().email).to.match(emailRegex);
});
```

### 3. **Regresión**
Cuando cambias algo, los tests te dicen si rompiste algo:
```javascript
// Cambias el backend para retornar "active" en lugar de "enabled"
test("User should be enabled", function() {
  expect(res.getBody().enabled).to.be.true;
  // ❌ FAIL: Property 'enabled' not found
  // ✅ Te das cuenta del breaking change
});
```

### 4. **Onboarding y Documentación**

Los tests son documentación ejecutable:
```javascript
test("Status code should be 201 Created", function() {
  expect(res.getStatus()).to.equal(201);
});
// 👆 Los tests documentan que CREATE debe retornar 201, no 200
```

### 5. **CI/CD**
Puedes ejecutar las colecciones en pipelines:
```bash
# En CI/CD
bru run api-collections/bruno/hexarch-api --env local

# Si todos los tests pasan: ✅ Deploy OK
# Si algún test falla: ❌ No deployes, algo está roto
```

---

## Bruno: Estructura y Sintaxis

### Anatomía de un Request en Bruno

Un archivo `.bru` tiene varios bloques:

```javascript
// 1. METADATA
meta {
  name: Create User           // Nombre del request
  type: http                  // Tipo (http, graphql, etc.)
  seq: 1                      // Orden en la UI
}

// 2. REQUEST CONFIG
post {
  url: {{baseUrl}}/api/v1/users   // URL con variables
  body: json                       // Tipo de body
  auth: none                       // Autenticación
}

// 3. HEADERS
headers {
  Content-Type: application/json
  // Authorization: Bearer {{token}}
}

// 4. BODY (si aplica)
body:json {
  {
    "username": "johndoe",
    "email": "john@example.com"
  }
}

// 5. DOCUMENTATION (Opcional pero recomendado)
docs {
  # Create User

  Este endpoint crea un nuevo usuario...

  ## Expected Response
  201 Created con el objeto User
}

// 6. PRE-REQUEST SCRIPT (Opcional)
script:pre-request {
  // Se ejecuta ANTES del request
  const timestamp = Date.now();
  bru.setVar("timestamp", timestamp);
}

// 7. TESTS (Validaciones)
tests {
  // Se ejecutan DESPUÉS del request
  // SOLO para validar/assert

  test("Status is 201", function() {
    expect(res.getStatus()).to.equal(201);
  });

  test("Response has ID", function() {
    expect(res.getBody().id).to.exist;
  });
}

// 8. POST-RESPONSE SCRIPT (Opcional)
script:post-response {
  // Se ejecuta DESPUÉS del request
  // Para guardar variables, logging, etc.

  if (res.getStatus() === 201) {
    bru.setEnvVar("userId", res.getBody().id);
  }
}
```

### Objetos Disponibles en Bruno

En los bloques de scripts y tests, tienes acceso a:

| Objeto | Descripción | Ejemplo |
|--------|-------------|---------|
| **`req`** | El request enviado | `req.getBody()`, `req.getHeaders()` |
| **`res`** | La respuesta recibida | `res.getStatus()`, `res.getBody()` |
| **`bru`** | Utilidades de Bruno | `bru.setEnvVar()`, `bru.getVar()` |
| **`expect`** | Assertions (solo en tests) | `expect(value).to.equal(123)` |

#### Métodos de `res` (Response)

```javascript
// Status
res.getStatus()           // 201
res.getStatusText()       // "Created"

// Body
res.getBody()             // { id: "123", username: "john" }
res.getBody().id          // "123"

// Headers
res.getHeaders()          // { "content-type": "application/json", ... }
res.getHeader("content-type")  // "application/json"

// Timing
res.getResponseTime()     // 234 (milisegundos)
```

#### Métodos de `req` (Request)

```javascript
// Body
req.getBody()             // { username: "john", email: "..." }

// Headers
req.getHeaders()          // { "content-type": "application/json" }
req.getHeader("content-type")
```

#### Métodos de `bru` (Utilidades)

```javascript
// Variables de entorno (persisten entre requests)
bru.setEnvVar("userId", "123")
bru.getEnvVar("userId")           // "123"

// Variables temporales (solo para el request actual)
bru.setVar("temp", "value")
bru.getVar("temp")

// Variables de colección
bru.setVar("collectionVar", "value")
```

### Sintaxis de Assertions con `expect`

Bruno usa **Chai** para assertions:

```javascript
// Igualdad
expect(actual).to.equal(expected)
expect(actual).to.not.equal(expected)

// Tipos
expect(value).to.be.a('string')
expect(value).to.be.a('number')
expect(value).to.be.a('boolean')
expect(value).to.be.an('object')
expect(value).to.be.an('array')

// Booleanos
expect(value).to.be.true
expect(value).to.be.false

// Existencia
expect(object).to.have.property('field')
expect(value).to.exist
expect(value).to.be.null
expect(value).to.be.undefined

// Strings
expect(string).to.include('substring')
expect(string).to.match(/regex/)
expect(string).to.have.length(10)

// Números
expect(number).to.be.greaterThan(5)
expect(number).to.be.lessThan(10)
expect(number).to.be.at.least(5)
expect(number).to.be.at.most(10)

// Arrays
expect(array).to.have.lengthOf(3)
expect(array).to.include('item')
expect(array).to.be.empty

// Objetos
expect(object).to.deep.equal({ key: 'value' })
expect(object).to.have.property('key', 'value')
```

---

## Tests vs Scripts en Bruno

Esta es una distinción **MUY IMPORTANTE** a tener en cuenta:

### ❌ INCORRECTO: Mezclar Tests y Scripts

```javascript
tests {
  test("Status is 201", function() {
    expect(res.getStatus()).to.equal(201);
  });

  // ❌ MAL: Esto NO es un test, es un script
  if (res.getStatus() === 201) {
    bru.setEnvVar("userId", res.getBody().id);
  }
}
```

**Problema**: El bloque `tests` es solo para validaciones. Mezclar lógica de scripts aquí:
- Viola la separación de responsabilidades
- Hace difícil entender qué es un test vs qué es un script
- Puede causar confusión al leer los resultados de tests

### ✅ CORRECTO: Separar Tests y Scripts

```javascript
tests {
  // 🎯 SOLO VALIDACIONES/ASSERTIONS
  test("Status is 201", function() {
    expect(res.getStatus()).to.equal(201);
  });

  test("Response has ID", function() {
    expect(res.getBody().id).to.exist;
  });
}

script:post-response {
  // 🔧 SCRIPTS DE POST-PROCESAMIENTO
  if (res.getStatus() === 201) {
    bru.setEnvVar("userId", res.getBody().id);
  }
}
```

### Cuándo Usar Cada Uno

| Bloque | Cuándo Usar | Ejemplos |
|--------|-------------|----------|
| **`tests`** | Para **validar** que algo es correcto | Status codes, estructura de response, valores esperados |
| **`script:post-response`** | Para **hacer** algo con la respuesta | Guardar variables, logging, transformar datos |
| **`script:pre-request`** | Para **preparar** el request | Generar timestamps, tokens, datos dinámicos |

### Ejemplo Completo Real

```javascript
// Ejemplo: Create User
script:pre-request {
  // 🔧 Generar email único para cada ejecución
  const timestamp = Date.now();
  bru.setVar("uniqueEmail", `user${timestamp}@example.com`);
}

tests {
  // 🎯 Validar status
  test("Status code should be 201 Created", function() {
    expect(res.getStatus()).to.equal(201);
  });

  // 🎯 Validar estructura
  test("Response should have all required fields", function() {
    const body = res.getBody();
    expect(body).to.have.property('id');
    expect(body).to.have.property('username');
    expect(body).to.have.property('email');
    expect(body).to.have.property('enabled');
    expect(body).to.have.property('createdAt');
  });

  // 🎯 Validar tipos
  test("ID should be a valid UUID", function() {
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
    expect(res.getBody().id).to.match(uuidRegex);
  });

  // 🎯 Validar valores
  test("User should be enabled by default", function() {
    expect(res.getBody().enabled).to.be.true;
  });
}

script:post-response {
  // 🔧 Guardar userId para siguientes requests
  if (res.getStatus() === 201) {
    bru.setEnvVar("userId", res.getBody().id);
    console.log(`Created user with ID: ${res.getBody().id}`);
  }

  // 🔧 Log para debugging (solo en caso de error)
  if (res.getStatus() >= 400) {
    console.error(`Error: ${res.getBody().message}`);
  }
}
```

---

## ⭐ Acciones Principales (Recetas Rápidas)

Esta sección contiene las **acciones más comunes** al trabajar con Bruno. Son recetas listas para copiar y pegar.

### 1. Guardar Variable de Entorno (setEnvVar)

**Caso de uso:** Guardar el `userId` después de crear un usuario para usarlo en otros requests.

```javascript
script:post-response {
  // Guardar el ID del usuario creado
  if (res.getStatus() === 201) {
    const userId = res.getBody().id;
    bru.setEnvVar("userId", userId);
  }
}
```

**Uso posterior:**
```javascript
// En otro request, usar la variable guardada
get {
  url: {{baseUrl}}/api/v1/users/{{userId}}
}
```

**Variaciones comunes:**

```javascript
script:post-response {
  const body = res.getBody();

  // Guardar múltiples variables
  bru.setEnvVar("userId", body.id);
  bru.setEnvVar("userEmail", body.email);
  bru.setEnvVar("username", body.username);

  // Guardar token de autenticación
  bru.setEnvVar("authToken", body.token);

  // Guardar con condicional (solo si existe)
  if (body.refreshToken) {
    bru.setEnvVar("refreshToken", body.refreshToken);
  }

  // Log para confirmar
  console.log("✅ Saved userId:", body.id);
}
```

---

### 2. Obtener Variable de Entorno (getEnvVar)

**Caso de uso:** Leer una variable guardada previamente.

```javascript
script:pre-request {
  // Leer variable guardada
  const userId = bru.getEnvVar("userId");
  console.log("Using userId:", userId);

  // Verificar si existe
  if (!userId) {
    console.error("❌ userId not found. Run 'Create User' first.");
  }
}
```

**En tests:**
```javascript
tests {
  test("Returned ID should match the one we requested", function() {
    const requestedId = bru.getEnvVar("userId");
    const returnedId = res.getBody().id;
    expect(returnedId).to.equal(requestedId);
  });
}
```

---

### 3. Variables Temporales (setVar / getVar)

**Diferencia con setEnvVar:** Las variables temporales **NO persisten** entre requests, solo duran durante el request actual.

```javascript
script:pre-request {
  // Variable temporal (solo para este request)
  const timestamp = Date.now();
  bru.setVar("timestamp", timestamp);
  bru.setVar("uniqueEmail", `user${timestamp}@example.com`);
}

body:json {
  {
    "username": "user{{timestamp}}",
    "email": "{{uniqueEmail}}"
  }
}
```

**Cuándo usar cada una:**

| Tipo | Persiste | Uso |
|------|----------|-----|
| `setEnvVar()` | ✅ Sí (entre requests) | IDs, tokens, datos que necesitas en otros requests |
| `setVar()` | ❌ No (solo este request) | Timestamps, emails únicos, datos temporales |

---

### 4. Acceder al Request Body

**Caso de uso:** Comparar el request con el response.

```javascript
tests {
  test("Response should match request data", function() {
    // Obtener el body del request
    const requestBody = req.getBody();
    const responseBody = res.getBody();

    expect(responseBody.username).to.equal(requestBody.username);
    expect(responseBody.email).to.equal(requestBody.email);
  });
}
```

**Con campos nested:**
```javascript
script:post-response {
  const reqBody = req.getBody();

  console.log("Request username:", reqBody.username);
  console.log("Request address:", reqBody.address.city);
  console.log("Request tags:", reqBody.tags[0]);
}
```

---

### 5. Acceder al Response Body

**Caso de uso:** Leer datos del response para validar o guardar.

```javascript
script:post-response {
  // Response completo
  const body = res.getBody();

  // Campos específicos
  const id = res.getBody().id;
  const username = res.getBody().username;

  // Nested fields
  const street = res.getBody().address.street;

  // Arrays
  const firstTag = res.getBody().tags[0];
  const tagCount = res.getBody().tags.length;

  // Verificar si un campo existe
  if (res.getBody().hasOwnProperty('middleName')) {
    console.log("Middle name:", res.getBody().middleName);
  }
}
```

---

### 6. Leer y Usar Headers

**Caso de uso:** Obtener headers del request o response.

```javascript
script:post-response {
  // Headers del response
  const contentType = res.getHeader("content-type");
  const correlationId = res.getHeader("x-correlation-id");

  console.log("Content-Type:", contentType);
  console.log("Correlation ID:", correlationId);

  // Todos los headers
  const allHeaders = res.getHeaders();
  console.log("All headers:", allHeaders);

  // Headers del request
  const reqContentType = req.getHeader("content-type");
}
```

**En tests:**
```javascript
tests {
  test("Content-Type should be JSON", function() {
    expect(res.getHeader("content-type")).to.include("application/json");
  });

  test("Response should have correlation ID", function() {
    expect(res.getHeader("x-correlation-id")).to.exist;
  });
}
```

---

### 7. Logging y Debugging

**Caso de uso:** Ver qué está pasando durante la ejecución.

```javascript
script:post-response {
  // Logs básicos
  console.log("Status:", res.getStatus());
  console.log("Body:", res.getBody());

  // Log condicional (solo errores)
  if (res.getStatus() >= 400) {
    console.error("❌ ERROR:", res.getBody().message);
    console.error("Error code:", res.getBody().errorCode);
  }

  // Log de éxito
  if (res.getStatus() === 201) {
    console.log("✅ User created:", res.getBody().id);
  }

  // Log detallado para debugging
  console.log("=== DEBUG INFO ===");
  console.log("Request URL:", req.getUrl());
  console.log("Request Body:", req.getBody());
  console.log("Response Time:", res.getResponseTime(), "ms");
  console.log("Response Body:", JSON.stringify(res.getBody(), null, 2));
}
```

**Ver los logs:**
1. Abre la consola de Bruno (View → Toggle Console)
2. Los logs aparecen después de ejecutar el request

---

### 8. Generar Datos Dinámicos

**Caso de uso:** Crear emails únicos, timestamps, UUIDs para cada ejecución.

```javascript
script:pre-request {
  // Timestamp actual
  const timestamp = Date.now();
  bru.setVar("timestamp", timestamp);

  // Email único
  const uniqueEmail = `user${timestamp}@example.com`;
  bru.setVar("uniqueEmail", uniqueEmail);

  // Número random
  const random = Math.floor(Math.random() * 10000);
  bru.setVar("randomNumber", random);

  // UUID v4
  const uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
  bru.setVar("uuid", uuid);

  // Fecha actual (ISO)
  const now = new Date().toISOString();
  bru.setVar("currentDate", now);
}

body:json {
  {
    "username": "user{{randomNumber}}",
    "email": "{{uniqueEmail}}",
    "requestId": "{{uuid}}",
    "timestamp": "{{currentDate}}"
  }
}
```

---

### 9. Workflow: Encadenar Requests

**Caso de uso:** Usar datos de un request en el siguiente.

```javascript
// REQUEST 1: Create User
script:post-response {
  if (res.getStatus() === 201) {
    bru.setEnvVar("userId", res.getBody().id);
    bru.setEnvVar("userEmail", res.getBody().email);
    console.log("✅ User created, ID saved");
  }
}

// REQUEST 2: Get User (usa userId del Request 1)
get {
  url: {{baseUrl}}/api/v1/users/{{userId}}
}

tests {
  test("Email should match the one we created", function() {
    const createdEmail = bru.getEnvVar("userEmail");
    expect(res.getBody().email).to.equal(createdEmail);
  });
}

// REQUEST 3: Update User (usa userId del Request 1)
put {
  url: {{baseUrl}}/api/v1/users/{{userId}}
}

// REQUEST 4: Delete User (usa userId del Request 1)
delete {
  url: {{baseUrl}}/api/v1/users/{{userId}}
}
```

---

### 10. Validación Condicional

**Caso de uso:** Ejecutar código solo si se cumple una condición.

```javascript
script:post-response {
  const status = res.getStatus();

  // Solo en caso de éxito
  if (status >= 200 && status < 300) {
    bru.setEnvVar("userId", res.getBody().id);
    console.log("✅ Success");
  }

  // Solo en caso de error
  if (status >= 400) {
    console.error("❌ Error:", res.getBody().message);
    // No guardar userId si falló
  }

  // Diferentes acciones según el status
  switch(status) {
    case 200:
      console.log("✅ OK");
      break;
    case 201:
      console.log("✅ Created");
      bru.setEnvVar("resourceId", res.getBody().id);
      break;
    case 400:
      console.error("❌ Bad Request");
      break;
    case 404:
      console.error("❌ Not Found");
      break;
    default:
      console.log("Status:", status);
  }
}
```

---

### 11. Limpiar Variables

**Caso de uso:** Resetear el entorno entre pruebas.

```javascript
script:post-response {
  // Limpiar una variable específica
  bru.setEnvVar("userId", "");

  // O puedes eliminarla completamente (si Bruno lo soporta)
  // bru.deleteEnvVar("userId"); // Verificar en docs de Bruno

  console.log("✅ Variables cleaned");
}
```

---

### Resumen de Acciones Principales

| Acción | Código | Cuándo Usar |
|--------|--------|-------------|
| **Guardar variable** | `bru.setEnvVar("key", value)` | Después de crear recursos (userId, token) |
| **Leer variable** | `bru.getEnvVar("key")` | En requests que dependen de datos previos |
| **Variable temporal** | `bru.setVar("key", value)` | Datos únicos para el request actual |
| **Request body** | `req.getBody()` | Comparar request con response |
| **Response body** | `res.getBody()` | Leer datos del response |
| **Status code** | `res.getStatus()` | Validar o log condicional |
| **Headers** | `res.getHeader("name")` | Leer metadata del response |
| **Logging** | `console.log(...)` | Debugging y tracking |
| **Timestamp** | `Date.now()` | Datos únicos por ejecución |
| **Random** | `Math.random()` | Valores aleatorios |

---

### 📝 Receta: Patrón Completo (Create → Get → Verify)

```javascript
// ========================================
// REQUEST 1: Create User
// ========================================
meta {
  name: Create User
  type: http
  seq: 1
}

post {
  url: {{baseUrl}}/api/v1/users
  body: json
}

script:pre-request {
  // Generar email único
  const timestamp = Date.now();
  bru.setVar("uniqueEmail", `user${timestamp}@example.com`);
}

body:json {
  {
    "username": "testuser",
    "email": "{{uniqueEmail}}"
  }
}

tests {
  test("Status should be 201", function() {
    expect(res.getStatus()).to.equal(201);
  });
}

script:post-response {
  // Guardar ID y email para siguientes requests
  if (res.getStatus() === 201) {
    const body = res.getBody();
    bru.setEnvVar("userId", body.id);
    bru.setEnvVar("userEmail", body.email);
    console.log("✅ Created user:", body.id);
  }
}

// ========================================
// REQUEST 2: Get User
// ========================================
meta {
  name: Get User
  type: http
  seq: 2
}

get {
  url: {{baseUrl}}/api/v1/users/{{userId}}
}

tests {
  test("Status should be 200", function() {
    expect(res.getStatus()).to.equal(200);
  });

  test("Should return the same user we created", function() {
    const expectedId = bru.getEnvVar("userId");
    const expectedEmail = bru.getEnvVar("userEmail");

    expect(res.getBody().id).to.equal(expectedId);
    expect(res.getBody().email).to.equal(expectedEmail);
  });
}

script:post-response {
  console.log("✅ Retrieved user:", res.getBody().username);
}
```

---

**💡 Tip:**

Cuando necesites hacer algo nuevo:
1. Busca en esta sección "Acciones Principales"
2. Copia el ejemplo que se parezca a lo que necesitas
3. Modifica los valores según tu caso
4. Ejecuta y verifica en la consola de Bruno

**La mayoría de las veces, estas recetas resuelven el 80% de lo que necesitas hacer.**

---

## Tipos de Tests Comunes

### 1. Tests de Status Code

Validan que el servidor retorna el status HTTP correcto.

```javascript
// Success cases
test("Status code is 200 OK", function() {
  expect(res.getStatus()).to.equal(200);
});

test("Status code is 201 Created", function() {
  expect(res.getStatus()).to.equal(201);
});

test("Status code is 204 No Content", function() {
  expect(res.getStatus()).to.equal(204);
});

// Error cases
test("Status code is 400 Bad Request", function() {
  expect(res.getStatus()).to.equal(400);
});

test("Status code is 404 Not Found", function() {
  expect(res.getStatus()).to.equal(404);
});

test("Status code is 409 Conflict", function() {
  expect(res.getStatus()).to.equal(409);
});

// Rangos
test("Status code is 2xx success", function() {
  expect(res.getStatus()).to.be.at.least(200);
  expect(res.getStatus()).to.be.below(300);
});
```

**Por qué importa:**
- 200 vs 201: GET retorna 200, POST debe retornar 201
- 400 vs 404: Validación fallida vs recurso no encontrado
- 409 vs 400: Conflicto (duplicado) vs validación genérica

### 2. Tests de Estructura de Response

Validan que el response tiene los campos esperados.

```javascript
// Campo existe
test("Response has ID field", function() {
  expect(res.getBody()).to.have.property('id');
});

// Múltiples campos
test("Response has all required fields", function() {
  const body = res.getBody();
  expect(body).to.have.property('id');
  expect(body).to.have.property('username');
  expect(body).to.have.property('email');
  expect(body).to.have.property('enabled');
  expect(body).to.have.property('createdAt');
});

// Campo con valor específico
test("Response has correct username", function() {
  expect(res.getBody()).to.have.property('username', 'johndoe');
});

// Nested properties
test("Response has nested address fields", function() {
  const address = res.getBody().address;
  expect(address).to.have.property('street');
  expect(address).to.have.property('city');
  expect(address).to.have.property('zipCode');
});
```

**Por qué importa:**
- Cambios en el backend se detectan inmediatamente
- Documenta el contrato de la API
- Previene bugs de integración

### 3. Tests de Tipos de Datos

Validan que los campos tienen el tipo correcto.

```javascript
// String
test("Username is a string", function() {
  expect(res.getBody().username).to.be.a('string');
});

// Number
test("Age is a number", function() {
  expect(res.getBody().age).to.be.a('number');
});

// Boolean
test("Enabled is a boolean", function() {
  expect(res.getBody().enabled).to.be.a('boolean');
});

// Array
test("Tags is an array", function() {
  expect(res.getBody().tags).to.be.an('array');
});

// Object
test("Address is an object", function() {
  expect(res.getBody().address).to.be.an('object');
});

// Null
test("Optional field can be null", function() {
  expect(res.getBody().middleName).to.be.null;
});
```

**Por qué importa:**
```javascript
// Sin test de tipo, este bug pasa desapercibido:
{
  "enabled": "true"  // ❌ String en lugar de boolean
}

// Con test:
test("Enabled is boolean", function() {
  expect(res.getBody().enabled).to.be.a('boolean');
  // ❌ FAIL: Expected 'boolean' but got 'string'
});
```

### 4. Tests de Formato/Validación

Validan que los valores cumplen con formatos específicos.

```javascript
// UUID
test("ID is valid UUID", function() {
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
  expect(res.getBody().id).to.match(uuidRegex);
});

// Email
test("Email is valid format", function() {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  expect(res.getBody().email).to.match(emailRegex);
});

// ISO 8601 Timestamp
test("CreatedAt is valid ISO 8601 timestamp", function() {
  const isoRegex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/;
  expect(res.getBody().createdAt).to.match(isoRegex);
});

// URL
test("Avatar URL is valid", function() {
  const urlRegex = /^https?:\/\/.+/;
  expect(res.getBody().avatarUrl).to.match(urlRegex);
});

// Phone number
test("Phone is valid format", function() {
  const phoneRegex = /^\+\d{1,3}\d{9,15}$/;
  expect(res.getBody().phone).to.match(phoneRegex);
});

// Length
test("Username has valid length", function() {
  const username = res.getBody().username;
  expect(username.length).to.be.at.least(3);
  expect(username.length).to.be.at.most(50);
});
```

### 5. Tests de Lógica de Negocio

Validan reglas específicas del dominio.

```javascript
// Valor por defecto
test("New users should be enabled by default", function() {
  expect(res.getBody().enabled).to.be.true;
});

// Relación entre campos
test("Email domain should match company", function() {
  const email = res.getBody().email;
  const company = res.getBody().company;
  expect(email).to.include(`@${company}.com`);
});

// Cálculos
test("Total should be sum of items", function() {
  const items = res.getBody().items;
  const total = res.getBody().total;
  const sum = items.reduce((acc, item) => acc + item.price, 0);
  expect(total).to.equal(sum);
});

// Timestamps
test("CreatedAt should be recent", function() {
  const createdAt = new Date(res.getBody().createdAt);
  const now = new Date();
  const diffMs = now - createdAt;
  const diffMinutes = diffMs / 1000 / 60;
  expect(diffMinutes).to.be.lessThan(5); // Creado hace menos de 5 minutos
});
```

### 6. Tests de Request vs Response

Validan que el response coincide con el request.

```javascript
// Campos coinciden
test("Response username matches request", function() {
  const requestBody = req.getBody();
  const responseBody = res.getBody();
  expect(responseBody.username).to.equal(requestBody.username);
});

// Múltiples campos
test("Response matches request data", function() {
  const reqBody = req.getBody();
  const resBody = res.getBody();
  expect(resBody.username).to.equal(reqBody.username);
  expect(resBody.email).to.equal(reqBody.email);
});
```

### 7. Tests de Headers

Validan headers de la respuesta.

```javascript
// Content-Type
test("Content-Type is JSON", function() {
  expect(res.getHeader("content-type")).to.include("application/json");
});

// Custom headers
test("Response has correlation ID", function() {
  expect(res.getHeader("x-correlation-id")).to.exist;
});

// Cache headers
test("Response should not be cached", function() {
  expect(res.getHeader("cache-control")).to.equal("no-cache");
});
```

### 8. Tests de Performance

Validan que la API responde en tiempo razonable.

```javascript
// Tiempo de respuesta
test("Response time is less than 500ms", function() {
  expect(res.getResponseTime()).to.be.lessThan(500);
});

test("Response time is acceptable for GET", function() {
  expect(res.getResponseTime()).to.be.lessThan(200);
});
```

### 9. Tests de Error Responses

Validan que los errores tienen la estructura correcta.

```javascript
// Estructura de error estándar
test("Error response has correct structure", function() {
  const body = res.getBody();
  expect(body).to.have.property('status');
  expect(body).to.have.property('error');
  expect(body).to.have.property('message');
  expect(body).to.have.property('errorCode');
  expect(body).to.have.property('timestamp');
});

// Error code específico
test("Error code is correct", function() {
  expect(res.getBody().errorCode).to.equal('USER_404');
});

// Mensaje de error
test("Error message is descriptive", function() {
  expect(res.getBody().message).to.include('not found');
});

// Detalles de validación
test("Validation details are present", function() {
  expect(res.getBody().details).to.be.an('object');
  expect(res.getBody().details).to.have.property('username');
});
```

---

## Mejores Prácticas

### 1. **Un Test = Una Validación**

❌ **MAL: Test que valida múltiples cosas sin nombre claro**
```javascript
test("Check response", function() {
  expect(res.getStatus()).to.equal(201);
  expect(res.getBody().id).to.exist;
  expect(res.getBody().username).to.be.a('string');
  expect(res.getBody().enabled).to.be.true;
});
// Si falla, ¿qué parte falló?
```

✅ **BIEN: Tests específicos con nombres descriptivos**
```javascript
test("Status code should be 201 Created", function() {
  expect(res.getStatus()).to.equal(201);
});

test("Response should have user ID", function() {
  expect(res.getBody().id).to.exist;
});

test("Username should be a string", function() {
  expect(res.getBody().username).to.be.a('string');
});

test("User should be enabled by default", function() {
  expect(res.getBody().enabled).to.be.true;
});
// Si falla, sabes exactamente qué
```

### 2. **Nombres Descriptivos en Inglés**

❌ **MAL: Nombres vagos o en español**
```javascript
test("Test 1", function() { ... });
test("Verifica respuesta", function() { ... });
test("ok", function() { ... });
```

✅ **BIEN: Nombres claros y descriptivos en inglés**
```javascript
test("Status code should be 201 Created", function() { ... });
test("Response should have all required fields", function() { ... });
test("Email should be valid format", function() { ... });
```

**Patrón recomendado:**
```
"[Sujeto] should [acción/condición]"
"[Campo] should be [tipo/valor]"
"[Campo] should have [propiedad]"
```

### 3. **Tests en Orden Lógico**

```javascript
tests {
  // 1. Primero: Status code (lo más básico)
  test("Status code should be 201 Created", function() {
    expect(res.getStatus()).to.equal(201);
  });

  // 2. Segundo: Estructura (campos existen)
  test("Response should have all required fields", function() {
    const body = res.getBody();
    expect(body).to.have.property('id');
    expect(body).to.have.property('username');
    expect(body).to.have.property('email');
  });

  // 3. Tercero: Tipos de datos
  test("ID should be a string", function() {
    expect(res.getBody().id).to.be.a('string');
  });

  // 4. Cuarto: Formatos específicos
  test("ID should be valid UUID format", function() {
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
    expect(res.getBody().id).to.match(uuidRegex);
  });

  // 5. Quinto: Valores específicos y lógica de negocio
  test("User should be enabled by default", function() {
    expect(res.getBody().enabled).to.be.true;
  });

  // 6. Último: Performance (opcional)
  test("Response time should be acceptable", function() {
    expect(res.getResponseTime()).to.be.lessThan(500);
  });
}
```

**Por qué este orden:**
- Si el status es incorrecto, los demás tests no tienen sentido
- Si faltan campos, no puedes validar tipos o formatos
- Va de lo general a lo específico

### 4. **Usa Variables para Evitar Repetición**

❌ **MAL: Repetir `res.getBody()` en cada test**
```javascript
test("Username is string", function() {
  expect(res.getBody().username).to.be.a('string');
});

test("Username has length", function() {
  expect(res.getBody().username.length).to.be.greaterThan(0);
});

test("Username matches request", function() {
  expect(res.getBody().username).to.equal(req.getBody().username);
});
```

✅ **BIEN: Usar variables locales**
```javascript
tests {
  // Variable compartida entre tests
  const body = res.getBody();
  const reqBody = req.getBody();

  test("Username is string", function() {
    expect(body.username).to.be.a('string');
  });

  test("Username has length", function() {
    expect(body.username.length).to.be.greaterThan(0);
  });

  test("Username matches request", function() {
    expect(body.username).to.equal(reqBody.username);
  });
}
```

### 5. **Validar Campos Opcionales Correctamente**

❌ **MAL: Test falla si el campo opcional no existe**
```javascript
test("Middle name is string", function() {
  expect(res.getBody().middleName).to.be.a('string');
  // ❌ Falla si middleName es null o undefined
});
```

✅ **BIEN: Considerar que puede ser null/undefined**
```javascript
test("Middle name is string or null", function() {
  const middleName = res.getBody().middleName;
  if (middleName !== null && middleName !== undefined) {
    expect(middleName).to.be.a('string');
  } else {
    expect(middleName).to.satisfy(val => val === null || val === undefined);
  }
});

// O más simple:
test("Middle name is optional", function() {
  const body = res.getBody();
  if (body.hasOwnProperty('middleName') && body.middleName !== null) {
    expect(body.middleName).to.be.a('string');
  }
});
```

### 6. **Separar Tests de Scripts**

✅ **BIEN: Usar bloques correctos**
```javascript
tests {
  // SOLO validaciones
  test("Status is 201", function() {
    expect(res.getStatus()).to.equal(201);
  });
}

script:post-response {
  // SOLO scripts
  if (res.getStatus() === 201) {
    bru.setEnvVar("userId", res.getBody().id);
  }
}
```

### 7. **Tests Independientes**

Cada test debe ser independiente, no debe depender de otros tests:

❌ **MAL: Tests dependientes**
```javascript
let userId;

test("Get user ID", function() {
  userId = res.getBody().id;
  expect(userId).to.exist;
});

test("User ID is UUID", function() {
  // ❌ Depende del test anterior
  const uuidRegex = /^[0-9a-f]{8}-/;
  expect(userId).to.match(uuidRegex);
});
```

✅ **BIEN: Tests independientes**
```javascript
test("Response has user ID", function() {
  expect(res.getBody().id).to.exist;
});

test("User ID is valid UUID", function() {
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
  expect(res.getBody().id).to.match(uuidRegex);
});
```

### 8. **Tests de Regresión para Bugs**

Cuando encuentres un bug, escribe un test que lo detecte:

```javascript
// Bug encontrado: El backend retorna username en minúsculas
// cuando debería respetar el case original

test("Username case should be preserved", function() {
  const requestUsername = req.getBody().username;
  const responseUsername = res.getBody().username;
  expect(responseUsername).to.equal(requestUsername);
  // Este test detectará si el bug vuelve a aparecer
});
```

### 9. **Comentar Tests Complejos**

```javascript
test("Total discount should not exceed product price", function() {
  // Business rule: Maximum discount is 90% of the product price
  // If price is $100, max discount is $90
  const price = res.getBody().price;
  const discount = res.getBody().discount;
  expect(discount).to.be.at.most(price * 0.9);
});
```

### 10. **Evitar Números Mágicos**

❌ **MAL: Números sin contexto**
```javascript
test("Username length is valid", function() {
  expect(res.getBody().username.length).to.be.at.least(3);
  expect(res.getBody().username.length).to.be.at.most(50);
  // ¿De dónde salen 3 y 50?
});
```

✅ **BIEN: Constantes con nombres**
```javascript
test("Username length is valid", function() {
  const MIN_USERNAME_LENGTH = 3;
  const MAX_USERNAME_LENGTH = 50;

  const usernameLength = res.getBody().username.length;
  expect(usernameLength).to.be.at.least(MIN_USERNAME_LENGTH);
  expect(usernameLength).to.be.at.most(MAX_USERNAME_LENGTH);
});
```

---

## Ejemplos Prácticos

### Ejemplo 1: Create User (POST)

```javascript
meta {
  name: Create User
  type: http
  seq: 1
}

post {
  url: {{baseUrl}}/api/v1/users
  body: json
  auth: none
}

headers {
  Content-Type: application/json
}

body:json {
  {
    "username": "johndoe",
    "email": "john@example.com"
  }
}

docs {
  # Create User

  Creates a new user in the system.

  ## Expected Response
  - **201 Created**: User created successfully
  - **400 Bad Request**: Validation error
  - **409 Conflict**: User already exists
}

tests {
  // 1. Status code
  test("Status code should be 201 Created", function() {
    expect(res.getStatus()).to.equal(201);
  });

  // 2. Response structure
  test("Response should have all required fields", function() {
    const body = res.getBody();
    expect(body).to.have.property('id');
    expect(body).to.have.property('username');
    expect(body).to.have.property('email');
    expect(body).to.have.property('enabled');
    expect(body).to.have.property('createdAt');
  });

  // 3. Types
  test("ID should be a string", function() {
    expect(res.getBody().id).to.be.a('string');
  });

  test("Enabled should be a boolean", function() {
    expect(res.getBody().enabled).to.be.a('boolean');
  });

  // 4. Formats
  test("ID should be valid UUID format", function() {
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
    expect(res.getBody().id).to.match(uuidRegex);
  });

  test("Email should be valid format", function() {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    expect(res.getBody().email).to.match(emailRegex);
  });

  test("CreatedAt should be ISO 8601 timestamp", function() {
    const isoRegex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/;
    expect(res.getBody().createdAt).to.match(isoRegex);
  });

  // 5. Business logic
  test("User should be enabled by default", function() {
    expect(res.getBody().enabled).to.be.true;
  });

  // 6. Request vs Response
  test("Response username should match request", function() {
    expect(res.getBody().username).to.equal(req.getBody().username);
  });

  test("Response email should match request", function() {
    expect(res.getBody().email).to.equal(req.getBody().email);
  });
}

script:post-response {
  // Save userId for subsequent requests
  if (res.getStatus() === 201) {
    const userId = res.getBody().id;
    bru.setEnvVar("userId", userId);
    console.log(`✅ Created user with ID: ${userId}`);
  } else {
    console.error(`❌ Failed to create user: ${res.getBody().message}`);
  }
}
```

### Ejemplo 2: Get User (GET)

```javascript
meta {
  name: Get User by ID
  type: http
  seq: 2
}

get {
  url: {{baseUrl}}/api/v1/users/{{userId}}
  body: none
  auth: none
}

docs {
  # Get User by ID

  Retrieves a user by their unique identifier.

  ## Expected Response
  - **200 OK**: User found
  - **404 Not Found**: User not found
}

tests {
  // 1. Status code
  test("Status code should be 200 OK", function() {
    expect(res.getStatus()).to.equal(200);
  });

  // 2. Response structure
  test("Response should have all required fields", function() {
    const body = res.getBody();
    expect(body).to.have.property('id');
    expect(body).to.have.property('username');
    expect(body).to.have.property('email');
    expect(body).to.have.property('enabled');
    expect(body).to.have.property('createdAt');
  });

  // 3. Validate ID matches
  test("Returned ID should match requested ID", function() {
    const requestedId = bru.getEnvVar("userId");
    const returnedId = res.getBody().id;
    expect(returnedId).to.equal(requestedId);
  });

  // 4. Data types
  test("All fields should have correct types", function() {
    const body = res.getBody();
    expect(body.id).to.be.a('string');
    expect(body.username).to.be.a('string');
    expect(body.email).to.be.a('string');
    expect(body.enabled).to.be.a('boolean');
    expect(body.createdAt).to.be.a('string');
  });
}

script:post-response {
  // Log user info
  if (res.getStatus() === 200) {
    const user = res.getBody();
    console.log(`✅ User found: ${user.username} (${user.email})`);
  }
}
```

### Ejemplo 3: Validation Error (400)

```javascript
meta {
  name: Create User - Validation Error
  type: http
  seq: 3
}

post {
  url: {{baseUrl}}/api/v1/users
  body: json
  auth: none
}

body:json {
  {
    "username": "ab",
    "email": "not-an-email"
  }
}

docs {
  # Create User - Validation Error Test

  Tests that validation errors are properly returned.
  This request intentionally sends invalid data.
}

tests {
  // 1. Status code for validation error
  test("Status code should be 400 Bad Request", function() {
    expect(res.getStatus()).to.equal(400);
  });

  // 2. Error structure
  test("Error response should have standard structure", function() {
    const body = res.getBody();
    expect(body).to.have.property('status');
    expect(body).to.have.property('error');
    expect(body).to.have.property('message');
    expect(body).to.have.property('errorCode');
    expect(body).to.have.property('timestamp');
    expect(body).to.have.property('details');
  });

  // 3. Error code
  test("Error code should be VALIDATION_001", function() {
    expect(res.getBody().errorCode).to.equal('VALIDATION_001');
  });

  // 4. Validation details
  test("Details should contain validation errors", function() {
    const details = res.getBody().details;
    expect(details).to.be.an('object');
    expect(details).to.have.property('username');
    expect(details).to.have.property('email');
  });

  // 5. Error messages are descriptive
  test("Username error message should mention length", function() {
    const usernameError = res.getBody().details.username;
    expect(usernameError).to.include('size');
  });

  test("Email error message should mention format", function() {
    const emailError = res.getBody().details.email;
    expect(emailError).to.include('email');
  });
}
```

### Ejemplo 4: Health Check

```javascript
meta {
  name: Health Check
  type: http
  seq: 4
}

get {
  url: {{baseUrl}}/actuator/health
  body: none
  auth: none
}

tests {
  // 1. Status
  test("Status code should be 200 OK", function() {
    expect(res.getStatus()).to.equal(200);
  });

  // 2. Overall health
  test("Overall status should be UP", function() {
    expect(res.getBody().status).to.equal("UP");
  });

  // 3. Components exist
  test("Response should have components", function() {
    expect(res.getBody().components).to.be.an('object');
  });

  // 4. Database health
  test("Database should be UP", function() {
    expect(res.getBody().components.db.status).to.equal("UP");
  });

  // 5. Disk space
  test("Disk space should be UP", function() {
    expect(res.getBody().components.diskSpace.status).to.equal("UP");
  });

  // 6. Performance
  test("Health check should respond quickly", function() {
    expect(res.getResponseTime()).to.be.lessThan(1000);
  });
}

script:post-response {
  // Log health status
  const status = res.getBody().status;
  if (status === "UP") {
    console.log("✅ All systems operational");
  } else {
    console.error(`❌ Health check failed: ${status}`);

    // Log which components are down
    const components = res.getBody().components;
    for (const [name, component] of Object.entries(components)) {
      if (component.status !== "UP") {
        console.error(`  ❌ ${name}: ${component.status}`);
      }
    }
  }
}
```

---

## Postman: Diferencias y Similitudes

Aunque Bruno es nuestra herramienta principal, es importante conocer Postman por su uso extendido en la industria.

### Similitudes

Ambos usan:
- **JavaScript** para tests y scripts
- **Chai** para assertions (`expect`)
- Mismo concepto de pre-request y post-response scripts
- Variables de entorno

### Diferencias Clave

| Aspecto | Bruno | Postman |
|---------|-------|---------|
| **Formato** | Archivos `.bru` (texto plano) | JSON |
| **Git** | ✅ Git-friendly | ❌ Difícil de hacer diff |
| **Tests** | Bloque `tests { }` | Event `pm.test()` |
| **Variables** | `bru.setEnvVar()` | `pm.environment.set()` |
| **Request** | `req.getBody()` | `pm.request.body` |
| **Response** | `res.getBody()` | `pm.response.json()` |
| **Organización** | Archivos y carpetas | JSON único |

### Sintaxis en Postman

```javascript
// BRUNO
tests {
  test("Status is 201", function() {
    expect(res.getStatus()).to.equal(201);
  });
}

script:post-response {
  bru.setEnvVar("userId", res.getBody().id);
}

// POSTMAN (equivalente)
pm.test("Status is 201", function() {
  pm.response.to.have.status(201);
});

pm.environment.set("userId", pm.response.json().id);
```

### Objetos en Postman

```javascript
// Status
pm.response.code                    // 201
pm.response.status                  // "Created"

// Body
pm.response.json()                  // { id: "123", ... }
pm.response.json().id               // "123"
pm.response.text()                  // Raw text

// Headers
pm.response.headers.get("content-type")

// Request
pm.request.body.raw                 // Request body
JSON.parse(pm.request.body.raw)     // Parsed request

// Variables
pm.environment.set("key", "value")
pm.environment.get("key")
pm.globals.set("key", "value")
pm.globals.get("key")

// Timing
pm.response.responseTime            // milliseconds
```

### Assertions en Postman

```javascript
// Status code
pm.response.to.have.status(201);
pm.expect(pm.response.code).to.equal(201);

// Headers
pm.response.to.have.header("content-type");
pm.expect(pm.response.headers.get("content-type")).to.include("json");

// Body
pm.expect(pm.response.json()).to.have.property("id");
pm.expect(pm.response.json().username).to.be.a("string");

// Response time
pm.expect(pm.response.responseTime).to.be.below(500);
```

### Ejemplo Completo en Postman

```javascript
// Pre-request Script
const timestamp = Date.now();
pm.environment.set("uniqueEmail", `user${timestamp}@example.com`);

// Tests
pm.test("Status code is 201 Created", function() {
  pm.response.to.have.status(201);
});

pm.test("Response has all required fields", function() {
  const jsonData = pm.response.json();
  pm.expect(jsonData).to.have.property("id");
  pm.expect(jsonData).to.have.property("username");
  pm.expect(jsonData).to.have.property("email");
});

pm.test("ID is valid UUID", function() {
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
  pm.expect(pm.response.json().id).to.match(uuidRegex);
});

// Save variable
if (pm.response.code === 201) {
  pm.environment.set("userId", pm.response.json().id);
}
```

### Cuándo Usar Cada Uno

**Usa Bruno cuando:**
- Trabajas en equipo con Git
- Quieres ver diffs de cambios en PRs
- Prefieres archivos de texto plano
- Quieres sincronizar con el repo

**Usa Postman cuando:**
- La empresa ya lo usa
- Necesitas features avanzadas (monitors, mock servers)
- Colaboras con equipos no técnicos
- Necesitas la UI más pulida

---

## Casos de Uso Avanzados

### 1. Generar Datos Dinámicos

```javascript
script:pre-request {
  // Timestamp único
  const timestamp = Date.now();
  bru.setVar("timestamp", timestamp);

  // Email único
  bru.setVar("uniqueEmail", `user${timestamp}@example.com`);

  // Random number
  const random = Math.floor(Math.random() * 10000);
  bru.setVar("randomNumber", random);

  // UUID v4
  const uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
  bru.setVar("uuid", uuid);
}

body:json {
  {
    "username": "user{{timestamp}}",
    "email": "{{uniqueEmail}}"
  }
}
```

### 2. Workflow Entre Requests

```javascript
// Request 1: Create User
script:post-response {
  if (res.getStatus() === 201) {
    bru.setEnvVar("userId", res.getBody().id);
    bru.setEnvVar("userEmail", res.getBody().email);
  }
}

// Request 2: Get User (usa userId de Request 1)
get {
  url: {{baseUrl}}/api/v1/users/{{userId}}
}

// Request 3: Update User (usa userId de Request 1)
put {
  url: {{baseUrl}}/api/v1/users/{{userId}}
}

// Request 4: Delete User (usa userId de Request 1)
delete {
  url: {{baseUrl}}/api/v1/users/{{userId}}
}
```

### 3. Tests Condicionales

```javascript
tests {
  // Solo validar si el status es success
  if (res.getStatus() >= 200 && res.getStatus() < 300) {
    test("Success response should have data", function() {
      expect(res.getBody()).to.have.property('data');
    });
  }

  // Solo validar si el status es error
  if (res.getStatus() >= 400) {
    test("Error response should have error message", function() {
      expect(res.getBody()).to.have.property('message');
    });
  }
}
```

### 4. Assertions con Datos Dinámicos

```javascript
tests {
  test("Username should match the one created", function() {
    const expectedUsername = bru.getEnvVar("lastCreatedUsername");
    expect(res.getBody().username).to.equal(expectedUsername);
  });

  test("Email domain should be from our domain", function() {
    const email = res.getBody().email;
    const allowedDomains = ["example.com", "test.com", "demo.com"];
    const domain = email.split('@')[1];
    expect(allowedDomains).to.include(domain);
  });
}
```

### 5. Logging y Debugging

```javascript
script:post-response {
  console.log("=== REQUEST ===");
  console.log("Method:", req.getMethod());
  console.log("URL:", req.getUrl());
  console.log("Headers:", req.getHeaders());
  console.log("Body:", req.getBody());

  console.log("\n=== RESPONSE ===");
  console.log("Status:", res.getStatus(), res.getStatusText());
  console.log("Time:", res.getResponseTime(), "ms");
  console.log("Headers:", res.getHeaders());
  console.log("Body:", res.getBody());

  // Logging condicional
  if (res.getStatus() >= 400) {
    console.error("❌ ERROR:", res.getBody().message);
    console.error("Error Code:", res.getBody().errorCode);
    if (res.getBody().details) {
      console.error("Details:", res.getBody().details);
    }
  }
}
```

### 6. Validar Arrays

```javascript
tests {
  test("Response should be an array", function() {
    expect(res.getBody()).to.be.an('array');
  });

  test("Array should not be empty", function() {
    expect(res.getBody().length).to.be.greaterThan(0);
  });

  test("All items should have required fields", function() {
    const items = res.getBody();
    items.forEach(item => {
      expect(item).to.have.property('id');
      expect(item).to.have.property('name');
    });
  });

  test("Array should contain specific item", function() {
    const items = res.getBody();
    const hasItem = items.some(item => item.id === "123");
    expect(hasItem).to.be.true;
  });
}
```

### 7. Tests de Performance

```javascript
tests {
  test("Response time should be acceptable for GET", function() {
    expect(res.getResponseTime()).to.be.lessThan(200);
  });

  test("Response time should be acceptable for POST", function() {
    expect(res.getResponseTime()).to.be.lessThan(500);
  });
}

script:post-response {
  // Log performance metrics
  const time = res.getResponseTime();
  let status;

  if (time < 100) status = "🟢 Excellent";
  else if (time < 300) status = "🟡 Good";
  else if (time < 1000) status = "🟠 Acceptable";
  else status = "🔴 Slow";

  console.log(`Performance: ${status} (${time}ms)`);
}
```

---

## Troubleshooting

### Problema: Tests no se ejecutan

**Síntomas:**
- No ves resultados de tests
- Panel de tests vacío

**Soluciones:**
```javascript
// ✅ Verifica la sintaxis del bloque
tests {
  test("My test", function() {
    expect(res.getStatus()).to.equal(200);
  });
}

// ❌ No olvides los paréntesis y llaves
tests {
  test("My test", function() {  // ← function() aquí
    expect(res.getStatus()).to.equal(200);
  });  // ← llave de cierre
}
```

### Problema: `res.getBody()` es undefined

**Causa:** La respuesta no tiene body (ej: 204 No Content)

**Solución:**
```javascript
test("Status is 204", function() {
  expect(res.getStatus()).to.equal(204);
  // No intentes acceder a res.getBody() en 204
});

// O valida que existe
test("Response has body", function() {
  if (res.getStatus() !== 204) {
    expect(res.getBody()).to.exist;
  }
});
```

### Problema: Variables no se guardan

**Causa:** Usas `bru.setVar()` en lugar de `bru.setEnvVar()`

**Solución:**
```javascript
// ❌ No persiste entre requests
script:post-response {
  bru.setVar("userId", res.getBody().id);
}

// ✅ Persiste en el entorno
script:post-response {
  bru.setEnvVar("userId", res.getBody().id);
}
```

### Problema: Test falla con "Expected ... but got undefined"

**Causa:** Accedes a campo nested sin validar que existe

**Solución:**
```javascript
// ❌ Falla si address no existe
test("City is valid", function() {
  expect(res.getBody().address.city).to.be.a('string');
});

// ✅ Valida existencia primero
test("City is valid", function() {
  const body = res.getBody();
  expect(body).to.have.property('address');
  expect(body.address).to.have.property('city');
  expect(body.address.city).to.be.a('string');
});
```

### Problema: Regex no funciona

**Causa:** Olvidaste los `/` delimitadores

**Solución:**
```javascript
// ❌ String, no regex
const regex = "^[0-9]+$";

// ✅ Regex correcto
const regex = /^[0-9]+$/;

test("ID is numeric", function() {
  expect(res.getBody().id).to.match(regex);
});
```

### Problema: Tests pasan pero el script no se ejecuta

**Causa:** Script en bloque incorrecto

**Solución:**
```javascript
// ❌ Script en bloque de tests
tests {
  test("Status is 201", function() {
    expect(res.getStatus()).to.equal(201);
  });

  // Este código se ejecuta pero no es un test
  bru.setEnvVar("userId", res.getBody().id);
}

// ✅ Script en su propio bloque
tests {
  test("Status is 201", function() {
    expect(res.getStatus()).to.equal(201);
  });
}

script:post-response {
  bru.setEnvVar("userId", res.getBody().id);
}
```

---

## Checklist: ¿Son Buenos Mis Tests?

Usa este checklist para revisar tus tests:

### Básico
- [ ] ¿Valido el status code?
- [ ] ¿Valido que existen los campos requeridos?
- [ ] ¿Valido los tipos de datos?
- [ ] ¿Los nombres de tests son descriptivos?
- [ ] ¿Están en inglés?

### Intermedio
- [ ] ¿Valido formatos (UUID, email, timestamps)?
- [ ] ¿Valido que request y response coinciden?
- [ ] ¿Tests independientes (no dependen entre sí)?
- [ ] ¿Scripts separados de tests?
- [ ] ¿Uso variables en lugar de repetir código?

### Avanzado
- [ ] ¿Valido reglas de negocio?
- [ ] ¿Considero campos opcionales correctamente?
- [ ] ¿Tests para casos de error?
- [ ] ¿Tests de performance?
- [ ] ¿Logging útil para debugging?

### Profesional
- [ ] ¿Tests ordenados lógicamente?
- [ ] ¿Constantes para números mágicos?
- [ ] ¿Comentarios en tests complejos?
- [ ] ¿Workflow entre requests funciona?
- [ ] ¿Documentación en bloque `docs`?

---

## Recursos Adicionales

### Documentación Oficial
- [Bruno Documentation](https://docs.usebruno.com/)
- [Chai Assertion Library](https://www.chaijs.com/api/bdd/)
- [Postman Learning Center](https://learning.postman.com/)

### Ejemplos en Este Proyecto
- `Users/Create User.bru` - Ejemplo completo de POST
- `Users/Get User by ID.bru` - Ejemplo completo de GET
- `Monitoring/Health Check.bru` - Ejemplo de health check

### Otros Archivos Útiles
- `README.md` - Documentación completa de las colecciones
- `QUICK_START.md` - Guía rápida de 5 minutos
- `CURL_EXAMPLES.md` - Ejemplos con cURL

---

## Resumen

**Los tests en colecciones de API son código JavaScript que:**

1. ✅ **Validan automáticamente** que la API funciona correctamente
2. ✅ **Documentan** qué se espera de cada endpoint
3. ✅ **Previenen regresiones** detectando cuando algo se rompe
4. ✅ **Aceleran el desarrollo** porque no tienes que validar manualmente

**Mejores prácticas clave:**

1. **Un test = una validación** con nombre descriptivo
2. **Orden lógico**: Status → Estructura → Tipos → Formatos → Lógica
3. **Separar tests de scripts** (tests para validar, scripts para hacer)
4. **Usar variables** para evitar repetición
5. **Tests independientes** que no dependen entre sí

**Aprende haciendo:**

1. Empieza copiando los tests de los ejemplos
2. Modifica uno a la vez para entender qué hace
3. Escribe tests nuevos para endpoints nuevos
4. Revisa el checklist para mejorar

---

**¡Ahora tienes todo lo que necesitas para escribir tests profesionales en Bruno y Postman!** 🚀
