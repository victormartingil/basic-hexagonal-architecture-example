# Guía de Troubleshooting - Hexarch

Esta guía contiene soluciones a los errores más comunes que encontrarás al trabajar con el proyecto.

## 📚 Índice

1. [Errores de Docker](#errores-de-docker)
2. [Errores de Compilación](#errores-de-compilación)
3. [Errores de Tests](#errores-de-tests)
4. [Errores de Aplicación](#errores-de-aplicación)
5. [Errores de Base de Datos](#errores-de-base-de-datos)
6. [Errores de Kafka](#errores-de-kafka)
7. [Errores de Seguridad/JWT](#errores-de-seguridadjwt)
8. [Errores de Puertos](#errores-de-puertos)
9. [Errores de Git/Maven](#errores-de-gitmaven)
10. [Consejos Generales de Debugging](#consejos-generales-de-debugging)

---

## 🐳 Errores de Docker

### ❌ Error: "Docker daemon is not running"

**Síntoma**:
```
Cannot connect to the Docker daemon at unix:///var/run/docker.sock.
Is the docker daemon running?
```

**Diagnóstico**:
```bash
# Verificar si Docker está corriendo
docker ps
```

**Solución**:
1. **macOS/Windows**: Abre Docker Desktop
2. **Linux**:
   ```bash
   sudo systemctl start docker
   sudo systemctl enable docker
   ```
3. Espera 30-60 segundos a que Docker inicie completamente
4. Verifica con `docker ps`

---

### ❌ Error: "Error response from daemon: Ports are not available"

**Síntoma**:
```
Error response from daemon: Ports are not available:
exposing port TCP 0.0.0.0:5432 -> 0.0.0.0:0:
listen tcp 0.0.0.0:5432: bind: address already in use
```

**Diagnóstico**:
```bash
# macOS/Linux: Ver qué proceso usa el puerto 5432
lsof -i :5432

# Windows PowerShell
netstat -ano | findstr :5432
```

**Solución**:

**Opción A: Matar el proceso que usa el puerto**
```bash
# Identificar PID (Process ID)
lsof -i :5432
# PID está en la segunda columna

# Matar el proceso
kill -9 <PID>

# Ejemplo:
# postgres  12345 user    6u  IPv4  0x1234  0t0  TCP *:postgresql (LISTEN)
kill -9 12345
```

**Opción B: Cambiar el puerto en docker-compose.yml**
```yaml
# docker-compose.yml
services:
  postgres:
    ports:
      - "5433:5432"  # Cambia 5432 a 5433 externamente
```

Luego actualiza `application.yaml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/hexarch_db
```

---

### ❌ Error: "Could not find a valid Docker environment"

**Síntoma** (durante tests con Testcontainers):
```
org.testcontainers.dockerclient.EnvironmentAndSystemPropertyClientProviderStrategy -
Could not find a valid Docker environment
```

**Diagnóstico**:
```bash
# 1. Docker está corriendo?
docker info

# 2. Permisos correctos? (Linux)
groups | grep docker

# 3. Socket de Docker accesible?
ls -l /var/run/docker.sock
```

**Solución**:

**macOS/Windows**:
1. Abre Docker Desktop
2. En Settings → Advanced → Enable Docker CLI
3. Reinicia Docker Desktop

**Linux**:
```bash
# Agregar tu usuario al grupo docker
sudo usermod -aG docker $USER

# Logout y login para aplicar cambios
# O ejecuta:
newgrp docker

# Verificar
docker ps
```

**IntelliJ/IDEs**:
- IntelliJ IDEA: Settings → Build, Execution, Deployment → Docker → Verify "Docker for Mac/Windows"
- VS Code: Asegúrate de que la extensión Docker esté instalada

---

### ❌ Error: "Cannot remove container" o "Container is in use"

**Síntoma**:
```
Error response from daemon:
cannot remove container hexarch-postgres-1:
container is in use - stop the container before removing
```

**Solución**:
```bash
# Forzar eliminación de todos los contenedores
docker-compose down -v --remove-orphans

# Si aún persiste, forzar eliminación
docker ps -a  # Ver todos los contenedores
docker rm -f <container-id>

# Limpiar todo (cuidado: elimina TODO)
docker system prune -a --volumes
```

---

## 🔨 Errores de Compilación

### ❌ Error: "Cannot find symbol" en clases generadas por OpenAPI

**Síntoma**:
```
error: cannot find symbol
  symbol:   class CreateUserRequest
  location: package com.example.hexarch.user.infrastructure.adapter.input.rest.dto.generated
```

**Causa**: OpenAPI Generator no ha generado los DTOs.

**Solución**:
```bash
# Limpiar y generar código
./mvnw clean generate-sources

# O compilar completo (genera automáticamente)
./mvnw clean install
```

**En IntelliJ IDEA**:
1. Maven panel (derecha) → hexarch → Lifecycle → clean
2. Luego: hexarch → Lifecycle → compile
3. Refresh Maven (icono de refresh en Maven panel)
4. File → Invalidate Caches → Invalidate and Restart

---

### ❌ Error: "package lombok does not exist"

**Síntoma**:
```
error: package lombok does not exist
import lombok.Getter;
```

**Causa**: Lombok no está configurado en el IDE.

**Solución**:

**IntelliJ IDEA**:
1. File → Settings → Plugins
2. Buscar "Lombok" → Install
3. Reiniciar IntelliJ
4. Settings → Build, Execution, Deployment → Compiler → Annotation Processors
5. Habilitar "Enable annotation processing"

**VS Code**:
1. Instalar extensión "Lombok Annotations Support for VS Code"
2. Recargar ventana (Cmd/Ctrl + Shift + P → Reload Window)

**Eclipse**:
1. Descargar lombok.jar: https://projectlombok.org/download
2. Ejecutar: `java -jar lombok.jar`
3. Seleccionar Eclipse installation → Install/Update

---

### ❌ Error: "Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin"

**Síntoma**:
```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.11.0:compile
Compilation failure: Compilation failure:
[ERROR] Source option 21 is no longer supported. Use 21 or later.
```

**Causa**: Java version incorrecta.

**Diagnóstico**:
```bash
# Ver versión de Java actual
java -version

# Ver versiones instaladas
# macOS/Linux
/usr/libexec/java_home -V

# Windows
where java
```

**Solución**:

**Opción A: Usar SDKMAN (Recomendado para macOS/Linux)**
```bash
# Instalar SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Instalar Java 21
sdk install java 21.0.1-tem

# Verificar
java -version
```

**Opción B: Descargar Java 21 manualmente**
- Oracle JDK: https://www.oracle.com/java/technologies/downloads/
- Adoptium (OpenJDK): https://adoptium.net/

**Opción C: Cambiar JAVA_HOME**
```bash
# macOS/Linux - Agregar a ~/.bash_profile o ~/.zshrc
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH=$JAVA_HOME/bin:$PATH

# Windows - Variables de entorno
setx JAVA_HOME "C:\Program Files\Java\jdk-21"
setx PATH "%JAVA_HOME%\bin;%PATH%"
```

---

## 🧪 Errores de Tests

### ❌ Error: Tests de integración fallan con "Address already in use"

**Síntoma**:
```
Caused by: java.net.BindException: Address already in use
```

**Causa**: Puerto de Testcontainers ocupado o tests corriendo en paralelo.

**Solución**:
```bash
# 1. Matar tests en ejecución
# macOS/Linux
pkill -f maven
pkill -f java

# 2. Liberar puertos
docker stop $(docker ps -q)

# 3. Ejecutar tests secuencialmente (no en paralelo)
./mvnw test -Pintegration-tests -DforkCount=1
```

---

### ❌ Error: "Table 'users' doesn't exist" en tests de integración

**Síntoma**:
```
Caused by: org.postgresql.util.PSQLException:
ERROR: relation "users" does not exist
```

**Causa**: Flyway no ejecutó las migraciones en Testcontainers.

**Solución**:

**1. Verificar que Flyway está habilitado**:
```yaml
# src/test/resources/application-test.yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

**2. Verificar que los scripts SQL existen**:
```bash
ls -la src/main/resources/db/migration/
# Debe existir: V1__create_users_table.sql
```

**3. Limpiar y reejecutar**:
```bash
./mvnw clean test -Pintegration-tests
```

---

### ❌ Error: "No tests were found"

**Síntoma**:
```
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0
```

**Causa**: Naming convention de tests incorrecta o profile no activo.

**Solución**:

**1. Verificar naming**:
- Unit tests: `*Test.java` (ej: `CreateUserServiceTest.java`)
- Integration tests: `*IntegrationTest.java` (ej: `UserControllerIntegrationTest.java`)

**2. Verificar perfil**:
```bash
# Tests de integración requieren profile activo
./mvnw test -Pintegration-tests

# O activar en pom.xml temporalmente
```

**3. Verificar anotaciones**:
```java
// ✅ Correcto
@Test
void shouldCreateUser_whenValidCommand() { }

// ❌ Incorrecto (falta @Test)
void shouldCreateUser_whenValidCommand() { }
```

---

### ❌ Error: ArchUnit tests fallan después de añadir código

**Síntoma**:
```
java.lang.AssertionError: Architecture Violation [Priority: MEDIUM] -
Rule 'no classes that reside in a package '..domain..' should depend on classes
that reside in a package '..infrastructure..'' was violated (1 times):
```

**Causa**: Violaste una regla de arquitectura hexagonal.

**Diagnóstico**:
```bash
# Ejecutar solo tests de arquitectura
./mvnw test -Dtest=HexagonalArchitectureTest

# Ver la violación específica en el log
```

**Solución**:

**Ejemplo de violación común**:
```java
// ❌ MAL: Domain importa Infrastructure
package com.example.hexarch.user.domain.model;

import org.springframework.stereotype.Component; // ❌ Spring es Infrastructure!

public class User {
    // ...
}
```

**Corrección**:
```java
// ✅ BIEN: Domain sin dependencias de frameworks
package com.example.hexarch.user.domain.model;

public class User {  // Sin anotaciones de Spring
    // ...
}
```

**Reglas principales**:
- ❌ Domain **NO** puede importar `org.springframework.*`
- ❌ Domain **NO** puede importar `jakarta.persistence.*`
- ❌ Application **NO** puede importar `infrastructure.*`
- ✅ Infrastructure **SÍ** puede importar `application.*` y `domain.*`

---

## 🚀 Errores de Aplicación

### ❌ Error: "Connection refused" al conectar a PostgreSQL

**Síntoma**:
```
org.postgresql.util.PSQLException: Connection to localhost:5432 refused.
Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
```

**Diagnóstico**:
```bash
# 1. ¿PostgreSQL está corriendo?
docker ps | grep postgres

# 2. ¿Está escuchando en el puerto correcto?
docker logs hexarch-postgres-1

# 3. ¿Puedes conectarte manualmente?
docker exec -it hexarch-postgres-1 psql -U postgres -d hexarch_db
```

**Solución**:

**Opción A: Levantar PostgreSQL**
```bash
docker-compose up -d postgres

# Verificar que esté healthy
docker ps
# STATUS debe ser "healthy"
```

**Opción B: Esperar a que PostgreSQL inicie**
```bash
# PostgreSQL tarda 5-10 segundos en estar listo
docker logs -f hexarch-postgres-1

# Esperar a ver:
# "database system is ready to accept connections"
```

**Opción C: Recrear contenedor**
```bash
docker-compose down postgres
docker-compose up -d postgres
```

---

### ❌ Error: "Port 8080 was already in use"

**Síntoma**:
```
***************************
APPLICATION FAILED TO START
***************************

Description:
Web server failed to start. Port 8080 was already in use.

Action:
Identify and stop the process that's listening on port 8080 or configure this application to listen on another port.
```

**Diagnóstico**:
```bash
# macOS/Linux
lsof -i :8080

# Windows PowerShell
netstat -ano | findstr :8080
```

**Solución**:

**Opción A: Matar el proceso**
```bash
# macOS/Linux
lsof -i :8080  # Ver PID
kill -9 <PID>

# Windows
netstat -ano | findstr :8080  # Ver PID
taskkill /PID <PID> /F
```

**Opción B: Cambiar puerto de la aplicación**
```yaml
# application.yaml
server:
  port: 8081  # Cambia a otro puerto
```

**Opción C: Ejecutar con otro puerto temporalmente**
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

---

### ❌ Error: "ApplicationContextException: Unable to start web server"

**Síntoma**:
```
org.springframework.context.ApplicationContextException:
Unable to start web server; nested exception is
org.springframework.boot.web.server.WebServerException:
Unable to start embedded Tomcat
```

**Diagnóstico**:
```bash
# Ver el stack trace completo
./mvnw spring-boot:run 2>&1 | tee app.log
cat app.log
```

**Causas comunes**:

**1. Puerto ocupado** → Ver error anterior

**2. Dependencia faltante**:
```bash
./mvnw dependency:tree
./mvnw clean install
```

**3. Configuración incorrecta**:
Revisar `application.yaml`:
- URLs de conexión (PostgreSQL, Kafka)
- Propiedades de Spring Boot
- Profiles activos

---

### ❌ Error: "Whitelabel Error Page - 404 Not Found"

**Síntoma**: Al acceder a `http://localhost:8080/api/v1/users`, vez "Whitelabel Error Page".

**Diagnóstico**:
```bash
# Ver logs de la aplicación
# Buscar líneas como:
# "Mapped \"{[/api/v1/users],methods=[POST]}\""

# Ver endpoints mapeados
curl http://localhost:8080/actuator/mappings
```

**Causas comunes**:

**1. Path incorrecto**:
```bash
# ✅ Correcto
curl http://localhost:8080/api/v1/users

# ❌ Incorrecto
curl http://localhost:8080/users
```

**2. Controller no encontrado**:
```java
// ¿El Controller tiene las anotaciones correctas?
@RestController  // ← ¿Está presente?
@RequestMapping("/api/v1/users")  // ← ¿Path correcto?
public class UserController {
    // ...
}
```

**3. Component scan**:
```java
// ¿Está en el paquete correcto?
// Debe estar en com.example.hexarch.* o subpaquetes
package com.example.hexarch.user.infrastructure.adapter.input.rest;
```

---

## 🗄️ Errores de Base de Datos

### ❌ Error: "Flyway failed to initialize" o "ValidationException: Detected failed migration"

**Síntoma**:
```
org.flywaydb.core.api.FlywayException:
Validate failed: Migration checksum mismatch for migration version 1
```

**Causa**: Script de migración cambió después de ejecutarse.

**Solución**:

**Desarrollo (database local)**:
```bash
# Opción A: Recrear BD desde cero
docker-compose down -v postgres
docker-compose up -d postgres

# Esperar 10 segundos
# Reiniciar app
./mvnw spring-boot:run
```

**Producción (NO hagas esto)**:
```bash
# ⚠️ Solo en desarrollo local
# Reparar Flyway (peligroso en prod)
./mvnw flyway:repair

# Mejor: Crear nueva migración V2__fix.sql
```

---

### ❌ Error: "unique constraint violation" al insertar usuario

**Síntoma**:
```
org.postgresql.util.PSQLException:
ERROR: duplicate key value violates unique constraint "users_username_key"
Detail: Key (username)=(johndoe) already exists.
```

**Causa**: Intentas crear un usuario que ya existe.

**Diagnóstico**:
```bash
# Conectar a PostgreSQL
docker exec -it hexarch-postgres-1 psql -U postgres -d hexarch_db

# Ver usuarios existentes
SELECT * FROM users;

# Salir
\q
```

**Solución**:

**Opción A: Usar otro username**
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username": "johndoe2", "email": "john2@example.com"}'
```

**Opción B: Eliminar usuario existente** (solo desarrollo)
```bash
docker exec -it hexarch-postgres-1 psql -U postgres -d hexarch_db -c "DELETE FROM users WHERE username='johndoe';"
```

**Opción C: Limpiar toda la BD** (solo desarrollo)
```bash
docker-compose down -v postgres
docker-compose up -d postgres
```

---

## 📡 Errores de Kafka

### ❌ Error: "Connection to node -1 could not be established"

**Síntoma**:
```
org.apache.kafka.common.errors.TimeoutException:
Failed to update metadata after 60000 ms.
```

**Diagnóstico**:
```bash
# ¿Kafka está corriendo?
docker ps | grep kafka

# Ver logs de Kafka
docker logs hexarch-kafka-1

# Ver logs de Zookeeper
docker logs hexarch-zookeeper-1
```

**Solución**:

**1. Levantar Kafka**
```bash
docker-compose up -d zookeeper kafka

# Esperar 30-60 segundos (Kafka tarda en iniciar)
```

**2. Verificar orden de inicio** (Zookeeper primero):
```bash
# Reiniciar en orden correcto
docker-compose down kafka zookeeper
docker-compose up -d zookeeper
sleep 10
docker-compose up -d kafka
```

**3. Verificar conectividad**:
```bash
# Ejecutar comando dentro del contenedor de Kafka
docker exec -it hexarch-kafka-1 kafka-topics.sh \
  --list \
  --bootstrap-server localhost:9092
```

---

### ❌ Error: "Topic 'user.events' not found"

**Síntoma**:
```
org.apache.kafka.common.errors.UnknownTopicOrPartitionException:
This server does not host this topic-partition.
```

**Causa**: El topic no existe (normalmente se crea automáticamente).

**Solución**:

**Opción A: Crear topic manualmente**
```bash
docker exec -it hexarch-kafka-1 kafka-topics.sh \
  --create \
  --topic user.events \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1
```

**Opción B: Habilitar auto-creación** (en application.yaml):
```yaml
spring:
  kafka:
    producer:
      properties:
        allow.auto.create.topics: true
```

**Verificar topics existentes**:
```bash
docker exec -it hexarch-kafka-1 kafka-topics.sh \
  --list \
  --bootstrap-server localhost:9092
```

---

## 🔐 Errores de Seguridad/JWT

### ❌ Error: "401 Unauthorized" al acceder a endpoint protegido

**Síntoma**:
```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Unauthorized",
  "path": "/api/v1/users"
}
```

**Causa**: Falta token JWT o es inválido.

**Solución**:

**1. Generar token JWT** (usando endpoint de autenticación):
```bash
# Si tienes endpoint /auth/login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}'

# Respuesta:
# {"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}
```

**2. Usar token en requests**:
```bash
export TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"username": "johndoe", "email": "john@example.com"}'
```

**3. Verificar configuración de Security**:
Ver `SecurityConfig.java`:
```java
// ¿Los endpoints públicos están configurados?
.requestMatchers("/api/v1/users/**").permitAll()  // ← Público
.requestMatchers("/actuator/**").permitAll()       // ← Público
```

---

### ❌ Error: "Invalid JWT signature" o "JWT expired"

**Síntoma**:
```
io.jsonwebtoken.security.SignatureException:
JWT signature does not match locally computed signature.
```

**Causa**: JWT_SECRET incorrecto o token expirado.

**Solución**:

**1. Verificar JWT_SECRET**:
```bash
# Ver secret actual
echo $JWT_SECRET

# Si está vacío, establecer uno
export JWT_SECRET="your-secret-key-at-least-256-bits-long-for-security"
```

**2. Generar nuevo token**:
```bash
# El token anterior ya no es válido
# Generar uno nuevo con el secret correcto
```

**3. Aumentar tiempo de expiración** (para desarrollo):
```yaml
# application-dev.yaml
jwt:
  expiration: 86400000  # 24 horas (en milisegundos)
```

---

## 🔌 Errores de Puertos

### ❌ Resumen de puertos usados y cómo liberarlos

| Puerto | Servicio | Comando para verificar |
|--------|----------|------------------------|
| 8080 | Spring Boot | `lsof -i :8080` |
| 5432 | PostgreSQL | `lsof -i :5432` |
| 9092 | Kafka | `lsof -i :9092` |
| 2181 | Zookeeper | `lsof -i :2181` |
| 9090 | Prometheus | `lsof -i :9090` |
| 3000 | Grafana | `lsof -i :3000` |
| 9411 | Zipkin | `lsof -i :9411` |

**Liberar todos los puertos**:
```bash
# Detener Docker Compose
docker-compose down

# Verificar que no haya contenedores corriendo
docker ps

# Verificar puertos liberados
lsof -i :8080,5432,9092,2181,9090,3000,9411
```

---

## 🔧 Errores de Git/Maven

### ❌ Error: "BUILD FAILURE" con mensaje críptico

**Diagnóstico**:
```bash
# Ver más detalles del error
./mvnw clean install -X  # -X = debug mode

# Ver stack trace completo
./mvnw clean install -e  # -e = show errors
```

**Solución**:

**1. Limpiar caché de Maven**:
```bash
# Eliminar caché local
rm -rf ~/.m2/repository

# Recompilar
./mvnw clean install
```

**2. Actualizar Maven wrapper**:
```bash
# Si mvnw está corrupto
mvn -N wrapper:wrapper
./mvnw --version
```

**3. Verificar permisos**:
```bash
# macOS/Linux
chmod +x mvnw

# Ejecutar
./mvnw clean install
```

---

### ❌ Error: "Git hook pre-commit failed"

**Síntoma**:
```
hint: The '.git/hooks/pre-commit' hook was ignored because it's not set as executable.
hint: You can disable this warning with `git config advice.ignoredHook false`.
```

**Solución**:
```bash
# Dar permisos de ejecución
chmod +x .git/hooks/pre-commit

# Verificar
ls -la .git/hooks/pre-commit

# Commitear
git commit -m "feat: add new feature"
```

---

## 🧭 Consejos Generales de Debugging

### 1. **Logs son tu mejor amigo**

```bash
# Ejecutar con logs verbosos
./mvnw spring-boot:run -X

# Guardar logs en archivo
./mvnw spring-boot:run 2>&1 | tee app.log

# Buscar errores en logs
cat app.log | grep -i "error\|exception\|failed"
```

### 2. **Usa logging en tu código**

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CreateUserService {
    private static final Logger log = LoggerFactory.getLogger(CreateUserService.class);

    public UserResult execute(CreateUserCommand command) {
        log.info("Creating user with username: {}", command.username());

        try {
            // ... lógica
            log.info("User created successfully: userId={}", user.getId());
        } catch (Exception e) {
            log.error("Failed to create user: {}", command.username(), e);
            throw e;
        }
    }
}
```

### 3. **Debugging con IntelliJ IDEA**

1. Poner breakpoint (click izquierda del número de línea)
2. Run → Debug 'Application'
3. Cuando se detenga:
   - Ver variables locales
   - Step Over (F8): siguiente línea
   - Step Into (F7): entrar en método
   - Step Out (Shift+F8): salir del método
   - Resume (F9): continuar

### 4. **Aislar el problema**

```bash
# ¿Es problema de compilación?
./mvnw clean compile

# ¿Es problema de tests?
./mvnw test -Dtest=CreateUserServiceTest

# ¿Es problema de la app?
./mvnw spring-boot:run

# ¿Es problema de Docker?
docker-compose up -d
docker ps
docker logs hexarch-postgres-1
```

### 5. **Validar entorno**

```bash
# Checklist rápido
java -version          # ¿Java 21?
./mvnw --version      # ¿Maven wrapper funciona?
docker --version      # ¿Docker instalado?
docker ps             # ¿Docker corriendo?
docker-compose ps     # ¿Servicios corriendo?
```

### 6. **Empezar desde cero** (último recurso)

```bash
# Limpiar TODO
docker-compose down -v
rm -rf ~/.m2/repository/com/example/hexarch
./mvnw clean
rm -rf target/

# Empezar de nuevo
docker-compose up -d
./mvnw clean install
./mvnw spring-boot:run
```

---

## 📞 ¿Aún Tienes Problemas?

Si ninguna de estas soluciones funciona:

1. **Revisa los logs completos**: `./mvnw spring-boot:run 2>&1 | tee app.log`
2. **Busca el error específico**: Copia el mensaje de error y busca en Google/Stack Overflow
3. **Revisa la configuración**: `application.yaml`, `docker-compose.yml`
4. **Verifica versiones**: Java, Maven, Docker
5. **Pregunta en GitHub Issues**: [Abrir issue](https://github.com/victormartingil/basic-hexagonal-architecture-example/issues)

---

**Última actualización**: 2025-01-15

**Nota**: Esta guía se actualiza continuamente con errores comunes reportados por usuarios. Si encuentras un error no documentado aquí, por favor abre un issue en GitHub.
