# Hexarch API Collections

Colecciones profesionales de API para **Bruno** y **Postman**, listas para importar y probar todos los endpoints del proyecto Hexarch.

**📚 IMPORTANTE:** Si ves los tests en Bruno/Postman y te preguntas "¿qué son?", lee **[TESTING_GUIDE.md](TESTING_GUIDE.md)** - Guía completa desde cero con ejemplos y mejores prácticas.

---

## 📦 Contenido

- ✅ Colecciones listas para importar (Bruno + Postman)
- ✅ Todos los endpoints documentados con ejemplos
- ✅ Tests automáticos incluidos
- ✅ Variables de entorno configuradas
- ✅ Guía completa de testing desde cero
- ✅ Ejemplos de cURL para terminal

```
api-collections/
├── bruno/hexarch-api/          # Colección de Bruno (Git-friendly)
│   ├── Users/                   # Endpoints de usuarios
│   ├── Monitoring/              # Endpoints de actuator
│   └── environments/            # Entornos (local, production)
├── postman/                     # Colección de Postman
│   ├── hexarch-api-collection.json
│   └── hexarch-environments.json
├── README.md                    # 👈 Este archivo
└── TESTING_GUIDE.md             # 📚 Guía completa de testing
```

---

## ⚡ Quick Start (5 minutos)

### 1. Iniciar la Aplicación

```bash
# Iniciar PostgreSQL
docker-compose up -d

# Iniciar la aplicación
./mvnw spring-boot:run

# Verificar que está corriendo
curl http://localhost:8080/actuator/health
```

### 2. Importar Colección

#### Opción A: Bruno (Recomendado)

**Por qué Bruno:**
- ✅ Open source y gratuito
- ✅ Git-friendly (archivos de texto plano)
- ✅ Sin necesidad de cuenta

**Pasos:**
1. Descargar: https://www.usebruno.com/
2. Abrir Bruno → "Open Collection"
3. Seleccionar carpeta: `api-collections/bruno/hexarch-api`
4. Elegir entorno "local" (dropdown superior derecha)
5. ¡Listo! Ejecuta "Create User" en la carpeta Users

#### Opción B: Postman

**Pasos:**
1. Abrir Postman → "Import" → "Upload Files"
2. Seleccionar ambos archivos:
   - `api-collections/postman/hexarch-api-collection.json`
   - `api-collections/postman/hexarch-environments.json`
3. Elegir entorno "Local" (dropdown superior derecha)
4. ¡Listo! Ejecuta "Create User"

#### Opción C: cURL (Terminal)

```bash
# Crear usuario
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "email": "test@example.com"}'

# Obtener usuario (reemplaza {id} con el ID retornado)
curl http://localhost:8080/api/v1/users/{id}
```

### 3. Flujo de Prueba

1. **Create User** → Status 201, guarda `userId` automáticamente
2. **Get User by ID** → Status 200, usa el `userId` guardado
3. **Health Check** → Status 200, verifica que todo está "UP"

**💡 Tip:** Los tests se ejecutan automáticamente y validan las respuestas. Mira la pestaña "Tests" en Bruno/Postman.

---

## 📋 Endpoints Disponibles

### Users API (CQRS)

#### POST /api/v1/users - Create User (Command)

Crea un nuevo usuario en el sistema.

**Request:**
```json
{
  "username": "johndoe",
  "email": "john@example.com"
}
```

**Response 201 Created:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "email": "john@example.com",
  "enabled": true,
  "createdAt": "2024-01-15T10:30:00.123Z"
}
```

**Validaciones:**
- Username: 3-50 caracteres, solo alfanuméricos, guiones y guiones bajos
- Email: formato válido, máximo 100 caracteres
- Ambos deben ser únicos

**Errores:**
- `400 Bad Request`: Validación fallida (username/email inválido)
- `409 Conflict`: Usuario ya existe
- `500 Internal Server Error`: Error del servidor

#### GET /api/v1/users/{id} - Get User by ID (Query)

Obtiene un usuario por su ID (UUID).

**Response 200 OK:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "email": "john@example.com",
  "enabled": true,
  "createdAt": "2024-01-15T10:30:00.123Z"
}
```

**Errores:**
- `404 Not Found`: Usuario no encontrado
- `500 Internal Server Error`: Error del servidor

---

### Monitoring API (Actuator)

#### GET /actuator/health - Health Check

Retorna el estado de salud de la aplicación.

**Response 200 OK:**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

#### GET /actuator/info - Application Info

Retorna información de la aplicación (nombre, versión, descripción).

#### GET /actuator/metrics - Metrics

Lista todas las métricas disponibles (JVM, sistema, HTTP, base de datos).

---

## 🔧 Variables de Entorno

Las colecciones usan estas variables (ya configuradas en los entornos):

| Variable | Descripción | Valor Local | Auto-actualiza |
|----------|-------------|-------------|----------------|
| `baseUrl` | URL base de la API | `http://localhost:8080` | No |
| `apiVersion` | Versión de la API | `v1` | No |
| `userId` | ID del último usuario creado | - | ✅ Sí (al crear usuario) |

**Actualización automática:**

Al ejecutar "Create User" con los scripts incluidos, el `userId` se guarda automáticamente para usar en "Get User by ID". No necesitas copiar/pegar IDs manualmente.

**Modificar variables:**

- **Bruno**: Click en el ícono de entornos (esquina superior derecha) → Editar → Guardar
- **Postman**: Click en el ícono del ojo → "Edit" → Modificar valores → "Save"

---

## ✅ Tests Automáticos

> **📚 Para aprender a escribir tests desde cero, lee [TESTING_GUIDE.md](TESTING_GUIDE.md)**
>
> Esta guía incluye:
> - ¿Qué son los tests y por qué son importantes?
> - Sintaxis completa de Bruno (objetos, métodos, assertions)
> - **⭐ Acciones principales (recetas)**: Cómo setear variables, acceder a datos, logging, etc.
> - Diferencia entre tests y scripts
> - 9 tipos de tests comunes con ejemplos
> - 10 mejores prácticas profesionales
> - Ejemplos completos comentados paso a paso
> - Comparación con Postman
> - Casos avanzados y troubleshooting

Todas las requests incluyen tests automáticos que verifican:

### Create User
- ✅ Status code es 201
- ✅ Response tiene todos los campos requeridos
- ✅ ID es UUID válido
- ✅ Email tiene formato válido
- ✅ Usuario habilitado por defecto
- ✅ Timestamp en formato ISO 8601
- ✅ Response coincide con request
- ✅ **Guarda `userId` automáticamente**

### Get User by ID
- ✅ Status code es 200
- ✅ Response tiene estructura correcta
- ✅ ID coincide con el solicitado
- ✅ Todos los campos tienen tipos correctos
- ✅ Formatos válidos (UUID, email)

### Health Check
- ✅ Status code es 200
- ✅ Estado general es "UP"
- ✅ Base de datos está "UP"
- ✅ Todos los componentes existen

**Ver resultados:** Los tests se ejecutan automáticamente después de cada request. Mira la pestaña "Tests" para ver si pasaron o fallaron.

---

## 💻 Ejemplos con cURL

### Crear Usuario

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com"
  }'
```

### Guardar ID y Obtener Usuario

```bash
# Guardar el ID en una variable
USER_ID=$(curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "email": "test@example.com"}' \
  | jq -r '.id')

echo "Created user with ID: $USER_ID"

# Usar la variable para obtener el usuario
curl http://localhost:8080/api/v1/users/$USER_ID | jq '.'
```

### Health Check

```bash
curl http://localhost:8080/actuator/health | jq '.'
```

### Pretty Print con jq

Si no tienes `jq` instalado:
```bash
# macOS
brew install jq

# Ubuntu/Debian
sudo apt-get install jq

# Sin jq (alternativa)
curl http://localhost:8080/actuator/health | python -m json.tool
```

---

## 🎯 Flujos de Prueba Recomendados

### Flujo Básico (Happy Path)

1. **Health Check** - Verificar que la app está corriendo
2. **Create User** - Crear un nuevo usuario → Status 201
3. **Get User by ID** - Obtener el usuario creado → Status 200

### Flujo de Validación

4. **Create User con username corto** → Status 400
5. **Create User con email inválido** → Status 400
6. **Create User duplicado** → Status 409
7. **Get User no existente** → Status 404

### Flujo de Monitoreo

8. **Info** - Ver información de la aplicación
9. **Metrics** - Ver métricas disponibles

---

## 🏗️ Cómo Construir Buenas Colecciones

### Organización

```
Colección
├── Users (carpeta funcional)
│   ├── Create User
│   ├── Get User by ID
│   └── Update User
├── Authentication (carpeta funcional)
│   ├── Login
│   ├── Refresh Token
│   └── Logout
└── Monitoring (carpeta técnica)
    ├── Health Check
    ├── Info
    └── Metrics
```

**Mejores prácticas:**
- Agrupar por funcionalidad (Users, Auth, Orders, etc.)
- Nombres descriptivos en inglés
- Orden lógico (Create → Get → Update → Delete)
- Incluir casos de error

### Tests Esenciales

Para cada endpoint, incluir tests que validen:
1. **Status code** correcto
2. **Estructura** del response (campos requeridos)
3. **Tipos de datos** correctos
4. **Formatos** específicos (UUID, email, timestamps)
5. **Lógica de negocio** (valores por defecto, cálculos)

**Ejemplo mínimo:**
```javascript
tests {
  test("Status code should be 200", function() {
    expect(res.getStatus()).to.equal(200);
  });

  test("Response should have required fields", function() {
    expect(res.getBody()).to.have.property('id');
    expect(res.getBody()).to.have.property('username');
  });
}
```

### Scripts Útiles

```javascript
script:post-response {
  // Guardar IDs para siguientes requests
  if (res.getStatus() === 201) {
    bru.setEnvVar("resourceId", res.getBody().id);
  }

  // Logging para debugging
  console.log(`Status: ${res.getStatus()}`);
  console.log(`Response time: ${res.getResponseTime()}ms`);

  // Log de errores
  if (res.getStatus() >= 400) {
    console.error(`Error: ${res.getBody().message}`);
  }
}
```

**Ver más:** Lee [TESTING_GUIDE.md](TESTING_GUIDE.md) sección "⭐ Acciones Principales" para 11 recetas listas para copiar.

---

## 🚀 Uso Avanzado

### CI/CD

Ejecutar colecciones en pipelines:

```bash
# Newman (Postman CLI)
npm install -g newman
newman run api-collections/postman/hexarch-api-collection.json \
  -e api-collections/postman/hexarch-environments.json \
  --env-var "baseUrl=http://localhost:8080"

# Bruno CLI
npm install -g @usebruno/cli
bru run api-collections/bruno/hexarch-api --env local
```

### Múltiples Entornos

Las colecciones están preparadas para múltiples entornos:

- **Local**: `http://localhost:8080`
- **Development**: `https://api.dev.example.com`
- **Staging**: `https://api.staging.example.com`
- **Production**: `https://api.production.example.com`

Solo cambia el entorno en el dropdown y todos los requests usan la URL correcta automáticamente.

---

## 📚 Recursos

### Documentación de Este Proyecto

- **[TESTING_GUIDE.md](TESTING_GUIDE.md)** - 📚 Guía completa de testing (¡LÉELA PRIMERO!)
  - Conceptos fundamentales explicados desde cero
  - **⭐ Acciones principales (11 recetas listas para copiar)**
  - Sintaxis completa de Bruno
  - Tests vs Scripts (muy importante)
  - 9 tipos de tests comunes
  - 10 mejores prácticas
  - 4 ejemplos completos paso a paso
  - Comparación Bruno vs Postman
  - Casos avanzados y troubleshooting

### Documentación Externa

- [Bruno Documentation](https://docs.usebruno.com/)
- [Chai Assertions](https://www.chaijs.com/api/bdd/) - Sintaxis de `expect()`
- [Postman Learning Center](https://learning.postman.com/docs/)
- [OpenAPI Specification](https://swagger.io/specification/)

### API Docs de la Aplicación

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

---

## ❓ Preguntas Frecuentes

**P: ¿Qué son los tests que veo en los archivos .bru?**
R: Lee [TESTING_GUIDE.md](TESTING_GUIDE.md) desde el principio. Explica qué son, por qué existen y cómo usarlos.

**P: ¿Cómo guardo el userId para usarlo en otros requests?**
R: Lee [TESTING_GUIDE.md](TESTING_GUIDE.md) sección "⭐ Acciones Principales" → "1. Guardar Variable de Entorno (setEnvVar)".

**P: ¿Cómo genero datos únicos (emails, timestamps)?**
R: Lee [TESTING_GUIDE.md](TESTING_GUIDE.md) sección "⭐ Acciones Principales" → "8. Generar Datos Dinámicos".

**P: ¿Cuál es la diferencia entre tests y scripts en Bruno?**
R: Lee [TESTING_GUIDE.md](TESTING_GUIDE.md) sección "Tests vs Scripts en Bruno" con ejemplos ❌ MAL vs ✅ BIEN.

**P: ¿Cómo hago que un request use datos de otro request?**
R: Lee [TESTING_GUIDE.md](TESTING_GUIDE.md) sección "⭐ Acciones Principales" → "9. Workflow: Encadenar Requests".

**P: ¿Bruno o Postman?**
R: **Bruno** para trabajo en equipo con Git. **Postman** si tu empresa ya lo usa. Ambos funcionan bien.

**P: Los tests fallan, ¿qué hago?**
R: Lee [TESTING_GUIDE.md](TESTING_GUIDE.md) sección "Troubleshooting" con problemas comunes y soluciones.

---

## 🐛 Troubleshooting

### Error: Connection refused

```bash
curl: (7) Failed to connect to localhost port 8080: Connection refused
```

**Solución:**
1. Verifica que la aplicación esté corriendo: `./mvnw spring-boot:run`
2. Verifica que PostgreSQL esté corriendo: `docker-compose ps`

### Error: Variables no se guardan

**Problema:** El `userId` no se guarda después de crear un usuario.

**Solución:**
1. Verifica que el status sea 201 (no 400 o 409)
2. Verifica que el bloque `script:post-response` esté presente
3. Abre la consola de Bruno (View → Toggle Console) para ver logs

### Error: Tests fallan

**Problema:** Los tests marcan error aunque el response parece correcto.

**Solución:**
1. Lee el mensaje de error del test (dice qué esperaba vs qué recibió)
2. Verifica que el campo existe con el nombre exacto (case-sensitive)
3. Verifica el tipo de dato (string vs number, etc.)
4. Lee [TESTING_GUIDE.md](TESTING_GUIDE.md) sección "Troubleshooting"

### Database connection failed

**Problema:** Health check muestra database status "DOWN".

**Solución:**
```bash
# Verificar que PostgreSQL está corriendo
docker-compose ps

# Si no está corriendo, iniciarlo
docker-compose up -d

# Verificar logs
docker-compose logs postgres
```

---

## 🎓 Para Aprender Más

### Ruta de Aprendizaje Recomendada

**Día 1:** Quick Start
1. Importar colección en Bruno
2. Ejecutar "Create User" y "Get User"
3. Ver los tests ejecutarse
4. Observar que el `userId` se guarda automáticamente

**Día 2:** Entender Tests
1. Leer [TESTING_GUIDE.md](TESTING_GUIDE.md) secciones 1-4
2. Ver los archivos `.bru` y entender cada bloque
3. Ejecutar requests y ver los logs en la consola de Bruno

**Día 3:** Acciones Principales
1. Leer [TESTING_GUIDE.md](TESTING_GUIDE.md) sección "⭐ Acciones Principales"
2. Copiar una receta y adaptarla
3. Crear tu propio request con variables

**Día 4:** Escribir Tests
1. Leer [TESTING_GUIDE.md](TESTING_GUIDE.md) secciones "Tipos de Tests" y "Mejores Prácticas"
2. Modificar un test existente
3. Añadir un test nuevo

**Día 5:** Flujos Completos
1. Crear un workflow completo (Create → Get → Verify)
2. Implementar logging para debugging
3. Probar casos de error

---

**¡Listo para empezar! Importa la colección y ejecuta tu primer request en menos de 5 minutos.** 🚀

**Recuerda:** Si tienes dudas sobre tests, variables o scripts, lee **[TESTING_GUIDE.md](TESTING_GUIDE.md)** - Todo está explicado desde cero con ejemplos.
