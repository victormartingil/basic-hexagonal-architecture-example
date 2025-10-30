# 📊 Monitoring & Observability - Hexarch

## Índice
- [¿Qué es Observabilidad?](#qué-es-observabilidad)
- [Los 3 Pilares: Logs, Métricas y Trazas](#los-3-pilares-logs-métricas-y-trazas)
- [Logs: Cuándo usar cada nivel](#logs-cuándo-usar-cada-nivel)
- [Métricas: Prometheus + Grafana](#métricas-prometheus--grafana)
- [Trazas Distribuidas: Zipkin + Micrometer](#trazas-distribuidas-zipkin--micrometer)
- [Correlation ID: Tracing de Negocio](#correlation-id-tracing-de-negocio)
- [Setup Local](#setup-local)
- [Ejemplos Prácticos en el Código](#ejemplos-prácticos-en-el-código)
- [Alerting](#alerting)

---

## ¿Qué es Observabilidad?

**Observabilidad** es la capacidad de **entender el estado interno** de un sistema basándose en sus salidas externas (logs, métricas, trazas).

### **Monitoring vs Observability**

| Concepto | Definición | Ejemplo |
|----------|------------|---------|
| **Monitoring** | Verificar si el sistema funciona (**known unknowns**) | "¿La CPU está > 80%?" |
| **Observability** | Investigar **por qué** falla (**unknown unknowns**) | "¿Por qué este request tardó 5 segundos?" |

**En producción necesitas AMBOS**:
- **Monitoring**: Dashboards con métricas clave (latencia, error rate, throughput)
- **Observability**: Herramientas para explorar y correlacionar eventos (logs + traces)

---

## Los 3 Pilares: Logs, Métricas y Trazas

### **1. LOGS** 📝
**Definición**: Eventos discretos con timestamp que describen lo que pasó.

**Características**:
- **Texto estructurado** (mejor JSON para parsing)
- **Timestamp** + **Nivel** (INFO, WARN, ERROR) + **Mensaje**
- **Contexto**: CorrelationId, UserId, TraceId

**Ejemplo**:
```
2024-01-15 10:30:00 [f47ac10b,1a2b3c4d] 550e8400 INFO - User created: username=johndoe
│                   │           │        │     │
Timestamp           TraceId     CorrId   Level Message
```

**Cuándo usar**:
- ✅ Debugging: "¿Qué pasó justo antes del error?"
- ✅ Auditoría: "¿Quién modificó este recurso?"
- ✅ Troubleshooting: Buscar patrones en fallos

**Herramientas**: ELK Stack (Elasticsearch + Logstash + Kibana), Splunk, CloudWatch Logs

---

### **2. MÉTRICAS** 📈
**Definición**: Valores numéricos agregados a lo largo del tiempo.

**Características**:
- **Time-series data**: (timestamp, value)
- **Agregaciones**: sum, avg, percentiles (p50, p99)
- **Eficientes**: Ocupan poco espacio (vs logs)

**Tipos de métricas**:

| Tipo | Descripción | Ejemplo |
|------|-------------|---------|
| **Counter** | Contador que solo crece | `users_created_total` |
| **Gauge** | Valor instantáneo (sube/baja) | `users_active_count` |
| **Histogram** | Distribución de valores | `http_request_duration_seconds` |
| **Summary** | Similar a histogram + percentiles | `http_request_duration_summary` |

**Cuándo usar**:
- ✅ Dashboards en tiempo real
- ✅ Alertas (CPU > 80%, Error rate > 5%)
- ✅ SLOs (Service Level Objectives): "p99 latency < 500ms"

**Herramientas**: Prometheus, Grafana, Datadog, New Relic

---

### **3. TRAZAS DISTRIBUIDAS** 🔗
**Definición**: Seguimiento de un request a través de múltiples servicios.

**Conceptos**:
- **Trace**: Request completo (ej: "Crear usuario desde API hasta BD")
- **Span**: Una operación dentro del trace (ej: "INSERT en PostgreSQL")
- **Trace ID**: Identificador único del trace
- **Span ID**: Identificador único del span

**Ejemplo visual**:
```
Trace ID: f47ac10b-8c42-11eb-8dcd-0242ac130003

┌─────────────────────────────────────────────────────────┐
│ Span 1: POST /api/v1/users         (200ms)             │
│  ├─ Span 2: Validate email          (5ms)              │
│  ├─ Span 3: Save to PostgreSQL     (50ms)              │
│  └─ Span 4: Publish Kafka event   (145ms)              │
│      └─ Span 5: Kafka Producer     (140ms)             │
└─────────────────────────────────────────────────────────┘
```

**Cuándo usar**:
- ✅ Identificar cuellos de botella (ej: "Kafka tarda 140ms")
- ✅ Debugging en microservicios (trazar request entre 5+ servicios)
- ✅ Entender flujo de requests

**Herramientas**: Zipkin, Jaeger, AWS X-Ray, Tempo

---

## Logs: Cuándo usar cada nivel

Spring Boot usa **SLF4J + Logback** con 5 niveles de log:

### **Niveles de Log**

| Nivel | Cuándo Usar | Ejemplo | En Producción |
|-------|-------------|---------|---------------|
| **TRACE** | Debugging muy detallado | `log.trace("Entering method calculateTax()")` | ❌ Desactivado |
| **DEBUG** | Información de desarrollo | `log.debug("Query executed: {}", sql)` | ❌ Desactivado |
| **INFO** | Eventos importantes | `log.info("User created: {}", userId)` | ✅ Activado |
| **WARN** | Problemas recuperables | `log.warn("Retry attempt 2/3 failed")` | ✅ Activado |
| **ERROR** | Errores que requieren atención | `log.error("Failed to send email", ex)` | ✅ Activado |

---

### **Reglas de Oro para Logging**

#### ✅ **1. INFO: Eventos de negocio importantes**

**Usar para**:
- Usuario creado/modificado/eliminado
- Transacción completada
- Inicio/fin de procesos batch
- Eventos de auditoría

**Ejemplo**:
```java
@Override
public UserResponse createUser(CreateUserCommand command) {
    log.info("Creating user: username={}, email={}", command.username(), command.email());

    User user = User.create(/* ... */);
    User savedUser = userRepository.save(user);

    log.info("User created successfully: userId={}, username={}",
             savedUser.getId(), savedUser.getUsername());

    return mapper.toResponse(savedUser);
}
```

**¿Cuántos logs INFO?**
- ❌ **MAL**: 1 log por cada línea de código (ruido)
- ✅ **BIEN**: 2-3 logs por operación importante (inicio, éxito, fin)

---

#### ✅ **2. DEBUG: Información de desarrollo**

**Usar para**:
- Valores de variables durante desarrollo
- SQL queries ejecutadas
- Parámetros de entrada a métodos
- Estado de objetos

**Ejemplo**:
```java
@Override
public Optional<User> findByEmail(String email) {
    log.debug("Searching user by email: {}", email);

    Optional<UserEntity> entity = jpaRepository.findByEmail(email);

    log.debug("User found: {}", entity.isPresent());

    return entity.map(userMapper::toDomain);
}
```

**Importante**:
- ❌ **NO** usar en producción (genera demasiados logs)
- ✅ Activar solo durante troubleshooting específico

---

#### ✅ **3. WARN: Problemas no críticos**

**Usar para**:
- Reintentos fallidos (antes del último intento)
- Configuración subóptima detectada
- Uso de valores por defecto
- Deprecation warnings

**Ejemplo**:
```java
@Retryable(maxAttempts = 3)
@Override
public void sendWelcomeEmail(String email) {
    try {
        emailService.send(email, "Welcome!");
        log.info("Welcome email sent to {}", email);
    } catch (EmailException ex) {
        log.warn("Failed to send email to {} (will retry)", email);
        throw ex; // Retry mechanism will catch this
    }
}
```

**¿Cuándo NO usar WARN?**
- ❌ Errores esperados (ej: usuario no encontrado → usar INFO)
- ❌ Validación fallida → lanzar exception, logear como ERROR si no se captura

---

#### ✅ **4. ERROR: Errores críticos**

**Usar para**:
- Exceptions no esperadas
- Fallos en servicios externos
- Inconsistencias de datos
- Cualquier cosa que requiera atención inmediata

**Ejemplo**:
```java
@Override
public void handleUserCreatedEvent(UserCreatedEvent event) {
    try {
        emailService.sendWelcomeEmail(event.email());
        log.info("Welcome email sent for user: {}", event.userId());
    } catch (Exception ex) {
        log.error("Failed to send welcome email for user: userId={}, error={}",
                  event.userId(), ex.getMessage(), ex);
        throw ex; // Será enviado a DLT
    }
}
```

**Reglas para ERROR logs**:
1. ✅ **Siempre incluir la exception**: `log.error("msg", exception)`
2. ✅ **Incluir contexto**: userId, transactionId, etc.
3. ✅ **NO logear la misma exception múltiples veces** (contamina logs)

**Anti-pattern común**:
```java
// ❌ MAL: Logea 3 veces la misma exception
try {
    service.doSomething();
} catch (Exception ex) {
    log.error("Error in layer 1", ex);  // ❌
    throw ex;
}

// En el caller:
try {
    layer1.call();
} catch (Exception ex) {
    log.error("Error in layer 2", ex);  // ❌ Duplicado
    throw ex;
}

// En el controller:
try {
    layer2.call();
} catch (Exception ex) {
    log.error("Error in controller", ex);  // ❌ Triplicado
    return ResponseEntity.status(500).build();
}
```

**✅ BIEN: Logea solo en el boundary**:
```java
// Domain/Application: Solo lanzan exceptions (NO logean)
public User createUser(...) {
    if (userRepository.existsByEmail(email)) {
        throw new EmailAlreadyExistsException(email);  // NO log aquí
    }
    return userRepository.save(user);
}

// Controller: Logea y maneja
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
    try {
        UserResponse response = useCase.execute(command);
        return ResponseEntity.status(201).body(response);
    } catch (EmailAlreadyExistsException ex) {
        log.warn("Email already exists: {}", request.email());  // Log en boundary
        return ResponseEntity.status(409).build();
    } catch (Exception ex) {
        log.error("Unexpected error creating user", ex);  // Log en boundary
        return ResponseEntity.status(500).build();
    }
}
```

---

#### ✅ **5. TRACE: Solo para librerías**

**NO usar en código de aplicación**. Reservado para:
- Spring Framework internals
- Hibernate SQL tracing
- Network debugging

**Activación temporal**:
```yaml
logging:
  level:
    org.hibernate.SQL: TRACE  # Ver SQL queries
    org.springframework.web: TRACE  # Ver requests HTTP
```

---

### **Formato de Logs en Hexarch**

**Configurado en `application.yaml`**:
```yaml
logging:
  pattern:
    # Formato: timestamp [traceId,spanId] correlationId level - message
    console: "%d{yyyy-MM-dd HH:mm:ss} [%X{traceId},%X{spanId}] %X{correlationId} %5p - %msg%n"
```

**Ejemplo de log**:
```
2024-01-15 10:30:00 [f47ac10b,1a2b3c4d] 550e8400-e29b-41d4 INFO  - User created: userId=123, username=johndoe
│                   │           │        │                    │     │
Timestamp           TraceId     SpanId   CorrelationId        Level Message
```

**Ventajas**:
- ✅ Buscar todos los logs de un request específico: `correlationId:550e8400-e29b-41d4`
- ✅ Ver trace completo en Zipkin: `traceId:f47ac10b`
- ✅ Debugging rápido: correlacionar logs entre servicios

---

## Métricas: Prometheus + Grafana

### **¿Qué es Prometheus?**

**Prometheus** es un sistema de **monitorización basado en métricas time-series**:
- Recolecta métricas mediante **pull** (scraping)
- Almacena en time-series DB
- Query language: **PromQL**
- Integración con Grafana para visualización

### **¿Qué es Grafana?**

**Grafana** es una plataforma de **visualización y dashboards**:
- Conecta a múltiples datasources (Prometheus, InfluxDB, ElasticSearch)
- Dashboards interactivos
- Alertas configurables

---

### **Setup en Hexarch**

**1. Dependencias en `pom.xml`**:
```xml
<!-- Actuator: Expone métricas -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Prometheus: Formato de métricas -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**2. Configuración en `application.yaml`**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics

  endpoint:
    prometheus:
      enabled: true

  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${ENVIRONMENT:local}
```

**3. Endpoint de métricas**:
```bash
curl http://localhost:8080/actuator/prometheus
```

**Respuesta**:
```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="PS Eden Space",} 1.23456789E8

# HELP http_server_requests_seconds Duration of HTTP requests
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{exception="None",method="POST",status="201",uri="/api/v1/users",} 42.0
http_server_requests_seconds_sum{exception="None",method="POST",status="201",uri="/api/v1/users",} 2.1
```

---

### **Métricas Automáticas (Actuator)**

Spring Boot Actuator **ya expone métricas automáticamente**:

| Métrica | Descripción |
|---------|-------------|
| `jvm_memory_used_bytes` | Memoria JVM usada |
| `jvm_threads_live_threads` | Threads activos |
| `jvm_gc_pause_seconds` | Pausas de Garbage Collector |
| `http_server_requests_seconds` | Latencia de requests HTTP |
| `http_server_requests_seconds_count` | Total de requests por endpoint |
| `spring_kafka_listener_seconds` | Latencia de Kafka listeners |
| `hikaricp_connections_active` | Conexiones DB activas |
| `logback_events_total` | Total de logs por nivel |

**No necesitas código adicional para estas métricas** ✅

---

### **Métricas Customizadas**

Para métricas de **negocio específicas**, usa `MeterRegistry`:

#### **Ejemplo 1: Contador de usuarios creados**

**`CreateUserUseCase.java`**:
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final EventPublisherPort eventPublisher;
    private final MeterRegistry meterRegistry;  // ← Inyectar

    @Override
    public UserResponse execute(CreateUserCommand command) {
        // ... validaciones ...

        User savedUser = userRepository.save(user);

        // 📊 Métrica custom: contador de usuarios creados
        meterRegistry.counter("users.created.total",
                              "status", "success",
                              "environment", environment)
                     .increment();

        log.info("User created: userId={}, username={}", savedUser.getId(), savedUser.getUsername());

        eventPublisher.publish(userCreatedEvent);

        return mapper.toResponse(savedUser);
    }
}
```

**Métrica expuesta**:
```
# HELP users_created_total Total number of users created
# TYPE users_created_total counter
users_created_total{status="success",environment="production"} 42.0
```

---

#### **Ejemplo 2: Gauge de usuarios activos**

**`UserMetricsService.java`**:
```java
@Service
@RequiredArgsConstructor
public class UserMetricsService {

    private final UserRepositoryPort userRepository;
    private final MeterRegistry meterRegistry;

    @PostConstruct
    public void registerMetrics() {
        // 📊 Gauge: valor que fluctúa (sube/baja)
        Gauge.builder("users.active.count", userRepository, repo -> repo.countByEnabled(true))
             .description("Number of active users")
             .register(meterRegistry);
    }
}
```

**Métrica expuesta**:
```
# HELP users_active_count Number of active users
# TYPE users_active_count gauge
users_active_count 156.0
```

---

#### **Ejemplo 3: Timer de operaciones**

**`EmailService.java`**:
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final MeterRegistry meterRegistry;

    @CircuitBreaker(name = "emailService", fallbackMethod = "sendEmailFallback")
    @Retry(name = "emailService")
    public void sendWelcomeEmail(String email) {
        // 📊 Timer: mide duración de operación
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            // Simular envío de email (aquí iría integración con SMTP/SES)
            Thread.sleep(100);

            sample.stop(meterRegistry.timer("email.send.duration",
                                            "type", "welcome",
                                            "status", "success"));

            log.info("Welcome email sent to {}", email);
        } catch (Exception ex) {
            sample.stop(meterRegistry.timer("email.send.duration",
                                            "type", "welcome",
                                            "status", "failure"));
            throw new EmailServiceException("Failed to send email", ex);
        }
    }
}
```

**Métrica expuesta**:
```
# HELP email_send_duration_seconds Email send duration
# TYPE email_send_duration_seconds summary
email_send_duration_seconds_count{type="welcome",status="success"} 42.0
email_send_duration_seconds_sum{type="welcome",status="success"} 4.2
email_send_duration_seconds{type="welcome",status="success",quantile="0.5",} 0.098
email_send_duration_seconds{type="welcome",status="success",quantile="0.99",} 0.15
```

---

### **Dashboards en Grafana**

**1. Añadir Prometheus como datasource**:
- URL: `http://prometheus:9090`
- Access: Server (default)

**2. Importar dashboard JVM (Micrometer)**:
- Dashboard ID: `4701` (JVM Micrometer)
- [https://grafana.com/grafana/dashboards/4701](https://grafana.com/grafana/dashboards/4701)

**3. Dashboard custom para Users**:

**Panel 1: Total usuarios creados**
```promql
# Query PromQL
rate(users_created_total[5m])
```

**Panel 2: Usuarios activos (gauge)**
```promql
users_active_count
```

**Panel 3: Latencia p99 de POST /users**
```promql
histogram_quantile(0.99,
  rate(http_server_requests_seconds_bucket{uri="/api/v1/users", method="POST"}[5m])
)
```

**Panel 4: Error rate**
```promql
rate(http_server_requests_seconds_count{status=~"5.."}[5m]) /
rate(http_server_requests_seconds_count[5m]) * 100
```

**Captura de pantalla** (ejemplo):
```
┌─────────────────────────────────────────────────────────────┐
│ Users Dashboard                             [Last 1 hour ▼] │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐│
│  │ Total Users     │  │ Active Users    │  │ Error Rate   ││
│  │      1,234      │  │       156       │  │    0.5%      ││
│  └─────────────────┘  └─────────────────┘  └──────────────┘│
│                                                               │
│  ┌──────────────────────────────────────────────────────────┐│
│  │ POST /users - p99 Latency                                ││
│  │ ▁▂▃▄▅▆▇█▇▆▅▄▃▂▁▂▃▄▅▆▇█ 120ms                            ││
│  └──────────────────────────────────────────────────────────┘│
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

## Trazas Distribuidas: Zipkin + Micrometer

### **¿Qué es Zipkin?**

**Zipkin** es un sistema de **distributed tracing** que:
- Recolecta spans de múltiples servicios
- Correlaciona spans en un trace completo
- Visualiza el flujo de requests
- Identifica cuellos de botella

### **Setup en Hexarch**

**1. Dependencias en `pom.xml`**:
```xml
<!-- Micrometer Tracing (OpenTelemetry compatible) -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>

<!-- Zipkin Reporter -->
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

**2. Configuración en `application.yaml`**:
```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% en dev, 0.1 (10%) en prod

  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

**3. Acceder a Zipkin UI**:
```bash
# URL
http://localhost:9411/zipkin/

# Buscar traces por:
# - serviceName: hexarch
# - traceId: f47ac10b-8c42-11eb-8dcd-0242ac130003
# - minDuration: > 500ms (encontrar lentos)
```

---

### **Visualización de Traces**

**Trace completo: POST /api/v1/users**

```
┌────────────────────────────────────────────────────────────────┐
│ Trace: f47ac10b-8c42-11eb-8dcd-0242ac130003 (250ms total)     │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│ ┌─ POST /api/v1/users ─────────────────────────── (250ms) ───┐│
│ │                                                              ││
│ │  ┌─ CreateUserUseCase.execute() ────────────── (200ms) ───┐││
│ │  │                                                          │││
│ │  │  ┌─ PostgresUserRepository.save() ───────── (40ms) ───┐│││
│ │  │  │  ├─ INSERT INTO users ...                           ││││
│ │  │  └──────────────────────────────────────────────────────┘│││
│ │  │                                                          │││
│ │  │  ┌─ KafkaEventPublisher.publish() ────────── (150ms) ──┐│││
│ │  │  │  ├─ kafka.send(user.created)                        ││││
│ │  │  └──────────────────────────────────────────────────────┘│││
│ │  └──────────────────────────────────────────────────────────┘││
│ └──────────────────────────────────────────────────────────────┘│
└────────────────────────────────────────────────────────────────┘

🔍 Insight: Kafka tarda 150ms de 250ms totales (60% del tiempo)
```

**Análisis**:
- Total: 250ms
- PostgreSQL: 40ms (16%)
- Kafka: 150ms (60%) ← **Cuello de botella**
- Lógica aplicación: 10ms (4%)
- Overhead: 50ms (20%)

**Acción**: Optimizar Kafka (async, batching, compression)

---

### **Spans Automáticos**

Spring Boot **ya crea spans automáticamente** para:
- ✅ HTTP requests (`spring-webmvc`)
- ✅ JPA queries (`spring-data-jpa`)
- ✅ Kafka producers/consumers (`spring-kafka`)
- ✅ RestTemplate/WebClient calls
- ✅ JDBC queries

**No necesitas código adicional** ✅

---

### **Spans Manuales (Custom)**

Para operaciones específicas, usa `@NewSpan` o `Tracer`:

```java
@Service
@RequiredArgsConstructor
public class ComplexBusinessLogic {

    private final Tracer tracer;

    public void processOrder(Order order) {
        // Crear span manual
        Span span = tracer.nextSpan().name("process-order");

        try (Tracer.SpanInScope ws = tracer.withSpan(span.start())) {
            // Añadir tags al span
            span.tag("order.id", order.getId());
            span.tag("order.amount", String.valueOf(order.getAmount()));

            // Lógica de negocio compleja
            validateOrder(order);
            calculateTax(order);
            saveOrder(order);

        } finally {
            span.end();
        }
    }
}
```

---

## Correlation ID: Tracing de Negocio

### **¿Qué es Correlation ID?**

**Correlation ID** es un **identificador de negocio** que:
- Correlaciona logs entre múltiples servicios
- Es independiente de Trace ID (técnico)
- Se propaga en header HTTP: `X-Correlation-ID`

**Diferencias**:

| Concepto | Propósito | Scope | Ejemplo |
|----------|-----------|-------|---------|
| **Trace ID** | Tracing técnico | Request HTTP completo | `f47ac10b-8c42-11eb` |
| **Correlation ID** | Tracing de negocio | Proceso completo (múltiples requests) | `order-2024-001` |

**Ejemplo**:
```
Usuario hace pedido:
1. POST /orders → correlationId: order-2024-001
2. Kafka: OrderCreatedEvent → correlationId: order-2024-001
3. Payment Service procesa → correlationId: order-2024-001
4. Notification Service envía email → correlationId: order-2024-001

Buscar logs: "order-2024-001" → Ves TODO el flujo
```

---

### **Implementación en Hexarch**

**`CorrelationIdFilter.java`**:
```java
@Component
@Order(1) // Ejecutar PRIMERO
@Slf4j
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Obtener o generar Correlation ID
        String correlationId = extractOrGenerateCorrelationId(request);

        // 2. Añadir al MDC para logs
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

        // 3. Añadir header a response
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 4. Limpiar MDC (evita memory leaks)
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    private String extractOrGenerateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
            log.debug("Generated new Correlation ID: {}", correlationId);
        } else {
            log.debug("Using existing Correlation ID: {}", correlationId);
        }

        return correlationId;
    }
}
```

**Uso**:
```bash
# Request CON Correlation ID
curl -H "X-Correlation-ID: my-custom-id-123" \
     http://localhost:8080/api/v1/users/550e8400

# Logs:
2024-01-15 10:30:00 [f47ac10b,1a2b3c4d] my-custom-id-123 INFO - Fetching user: 550e8400
2024-01-15 10:30:00 [f47ac10b,1a2b3c4d] my-custom-id-123 INFO - User found: johndoe

# Request SIN Correlation ID (se genera automáticamente)
curl http://localhost:8080/api/v1/users/550e8400

# Response header:
X-Correlation-ID: 7c3e1f2a-9b8d-4e7f-a1c2-3d4e5f6a7b8c
```

---

## Gestión de Logs: ELK vs Loki

### **¿Dónde ver los logs?**

#### **Desarrollo Local** 🖥️
Los logs se muestran en la **consola** (stdout):
```bash
./mvnw spring-boot:run
# Ves logs en tiempo real:
2024-01-15 10:30:00 [f47ac10b,1a2b3c4d] 550e8400 INFO - User created: userId=123
```

#### **Producción** 🏢
En producción necesitas **agregación de logs** porque:
- ❌ Múltiples instancias (10+ pods) → No puedes hacer `kubectl logs` en cada uno
- ❌ Logs efímeros → Si el pod muere, los logs se pierden
- ❌ Sin búsqueda → No puedes buscar "todos los logs con correlationId=X"

**Solución**: Sistema centralizado de logs

---

### **Opción 1: ELK Stack (Elasticsearch + Logstash + Kibana)** 🔥

**Componentes**:
```
┌─────────────┐     ┌──────────────┐     ┌─────────────────┐     ┌─────────┐
│   App       │────▶│  Logstash    │────▶│ Elasticsearch   │◀────│ Kibana  │
│ (Spring)    │     │ (Procesa)    │     │ (Almacena)      │     │ (UI)    │
└─────────────┘     └──────────────┘     └─────────────────┘     └─────────┘
```

**Pros**:
- ✅ Muy maduro (10+ años en producción)
- ✅ Búsqueda full-text potente (encuentra cualquier palabra en logs)
- ✅ Dashboards ricos en Kibana
- ✅ Integración con APM, Machine Learning, alertas avanzadas

**Contras**:
- ❌ **Pesado**: Elasticsearch consume ~2GB RAM mínimo
- ❌ **Complejo**: Requiere expertise para configurar/mantener
- ❌ **Costoso**: Infraestructura cara a escala

**Cuándo usar**:
- Empresas grandes con equipo dedicado
- Necesitas búsqueda full-text avanzada
- Ya tienes Elasticsearch en la empresa

---

### **Opción 2: Grafana Loki + Promtail** ⭐ **Recomendado**

**Componentes**:
```
┌─────────────┐     ┌──────────────┐     ┌─────────────────┐
│   App       │────▶│  Promtail    │────▶│     Loki        │
│ (Spring)    │     │ (Agente)     │     │ (Almacena)      │
│  stdout     │     │ Lee logs     │     │ Logs indexados  │
└─────────────┘     └──────────────┘     └─────────────────┘
                                                   │
                                                   ▼
                                          ┌─────────────────┐
                                          │    Grafana      │
                                          │ Logs + Métricas │
                                          │    + Traces     │
                                          └─────────────────┘
```

**Pros**:
- ✅ **Ligero**: ~200MB RAM (10x menos que Elasticsearch)
- ✅ **Simple**: Configuración mínima
- ✅ **Unified Observability**: Todo en Grafana (logs + métricas + traces)
- ✅ **Correlación fácil**: Ver logs y métricas del mismo timestamp
- ✅ **Gratis y open-source**

**Contras**:
- ❌ No hace búsqueda full-text (usa labels e índices)
- ❌ Menos features que Kibana (pero suficientes para 90% casos)

**Cuándo usar**:
- Proyectos pequeños/medianos
- Quieres simplicidad
- Ya usas Grafana para métricas (sinergia)

**Diferencia clave**:
- **Elasticsearch**: Indexa TODO el texto → Puedes buscar cualquier palabra
- **Loki**: Indexa solo labels (app, level, host) → Más rápido y ligero

---

### **Comparativa Rápida**

| Aspecto | ELK Stack | Loki + Grafana |
|---------|-----------|----------------|
| **Setup** | Complejo | Simple |
| **RAM** | ~2GB+ | ~200MB |
| **Búsqueda** | Full-text | Labels + grep |
| **UI** | Kibana | Grafana |
| **Curva aprendizaje** | Alta | Baja |
| **Costo infra** | Alto | Bajo |
| **Observability** | Solo logs | Logs + Métricas + Traces |

---

## Setup Local

### **Docker Compose completo (con Loki)**

**`docker-compose-observability.yml`**:
```yaml
version: '3.8'

services:
  # PostgreSQL
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: hexarch_db
      POSTGRES_USER: hexarch
      POSTGRES_PASSWORD: hexarch123
    ports:
      - "5432:5432"

  # Kafka + Zookeeper
  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.6.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  # Prometheus (métricas)
  prometheus:
    image: prom/prometheus:v2.50.0
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.enable-lifecycle'

  # Loki (almacena logs)
  loki:
    image: grafana/loki:2.9.0
    ports:
      - "3100:3100"
    command: -config.file=/etc/loki/local-config.yaml
    volumes:
      - ./monitoring/loki/loki-config.yml:/etc/loki/local-config.yaml

  # Promtail (envía logs a Loki)
  promtail:
    image: grafana/promtail:2.9.0
    volumes:
      - /var/log:/var/log
      - ./monitoring/promtail/promtail-config.yml:/etc/promtail/config.yml
      - ./logs:/var/log/hexarch  # Logs de la app
    command: -config.file=/etc/promtail/config.yml

  # Grafana (dashboards: logs + métricas + traces)
  grafana:
    image: grafana/grafana:10.3.0
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_USER: admin
      GF_SECURITY_ADMIN_PASSWORD: admin
    volumes:
      - ./monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./monitoring/grafana/datasources:/etc/grafana/provisioning/datasources

  # Zipkin (distributed tracing)
  zipkin:
    image: openzipkin/zipkin:3.0
    ports:
      - "9411:9411"
```

---

### **Configuración de Loki**

**`monitoring/loki/loki-config.yml`**:
```yaml
auth_enabled: false

server:
  http_listen_port: 3100

ingester:
  lifecycler:
    address: 127.0.0.1
    ring:
      kvstore:
        store: inmemory
      replication_factor: 1
  chunk_idle_period: 5m
  chunk_retain_period: 30s

schema_config:
  configs:
    - from: 2020-10-24
      store: boltdb
      object_store: filesystem
      schema: v11
      index:
        prefix: index_
        period: 168h

storage_config:
  boltdb:
    directory: /tmp/loki/index
  filesystem:
    directory: /tmp/loki/chunks

limits_config:
  enforce_metric_name: false
  reject_old_samples: true
  reject_old_samples_max_age: 168h

chunk_store_config:
  max_look_back_period: 0s

table_manager:
  retention_deletes_enabled: false
  retention_period: 0s
```

---

### **Configuración de Promtail**

**`monitoring/promtail/promtail-config.yml`**:
```yaml
server:
  http_listen_port: 9080
  grpc_listen_port: 0

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  # Logs de Hexarch (Spring Boot)
  - job_name: hexarch
    static_configs:
      - targets:
          - localhost
        labels:
          job: hexarch
          app: hexarch-user-service
          __path__: /var/log/hexarch/spring.log

    # Pipeline para parsear logs
    pipeline_stages:
      # Regex para extraer campos del log
      - regex:
          expression: '^(?P<timestamp>\S+\s+\S+) \[(?P<traceId>[^\,]+),(?P<spanId>[^\]]+)\] (?P<correlationId>\S+) (?P<level>\S+)\s+-\s+(?P<message>.*)$'

      # Extraer labels para indexar (búsquedas rápidas)
      - labels:
          level:
          traceId:
          correlationId:

      # Timestamp del log
      - timestamp:
          source: timestamp
          format: '2006-01-02 15:04:05'
```

---

### **Configuración de Grafana Datasources**

**`monitoring/grafana/datasources/datasources.yml`**:
```yaml
apiVersion: 1

datasources:
  # Prometheus (métricas)
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: false

  # Loki (logs)
  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100
    isDefault: true
    jsonData:
      maxLines: 1000

  # Zipkin (traces)
  - name: Zipkin
    type: zipkin
    access: proxy
    url: http://zipkin:9411
```

---

### **Configurar Spring Boot para escribir logs a archivo**

**`application.yaml`**:
```yaml
logging:
  file:
    name: logs/spring.log  # Promtail leerá de aquí
    max-size: 100MB
    max-history: 30  # Mantener 30 días

  pattern:
    # Mismo formato que consola (para parsear con Promtail)
    file: "%d{yyyy-MM-dd HH:mm:ss} [%X{traceId},%X{spanId}] %X{correlationId} %5p - %msg%n"
    console: "%d{yyyy-MM-dd HH:mm:ss} [%X{traceId},%X{spanId}] %X{correlationId} %5p - %msg%n"
```

---

### **Comandos**

```bash
# 1. Crear directorios
mkdir -p monitoring/{loki,promtail,grafana/datasources,prometheus}
mkdir -p logs

# 2. Crear archivos de configuración (copiar YAML de arriba)

# 3. Levantar infraestructura
docker-compose -f docker-compose-observability.yml up -d

# 4. Verificar servicios
docker-compose ps

# 5. Ejecutar aplicación
./mvnw spring-boot:run

# 6. Acceder a UIs
# Prometheus: http://localhost:9090
# Grafana:    http://localhost:3000 (admin/admin)
# Zipkin:     http://localhost:9411
# Loki API:   http://localhost:3100/metrics
```

---

## Buscar Logs en Grafana

### **1. Acceder a Grafana**
```
http://localhost:3000
Login: admin / admin
```

### **2. Ir a Explore**
```
Menú lateral → Explore (ícono de brújula)
Datasource: Loki
```

### **3. Ejemplos de Queries LogQL**

#### **Todos los logs de la app**:
```logql
{job="hexarch"}
```

#### **Solo logs de ERROR**:
```logql
{job="hexarch", level="ERROR"}
```

#### **Buscar por Correlation ID**:
```logql
{job="hexarch", correlationId="550e8400-e29b-41d4"}
```

#### **Buscar por Trace ID**:
```logql
{job="hexarch", traceId="f47ac10b"}
```

#### **Filtrar por contenido (grep-like)**:
```logql
{job="hexarch"} |= "User created"
```

#### **Logs de los últimos 5 minutos con ERROR**:
```logql
{job="hexarch", level="ERROR"} [5m]
```

#### **Contar errores por minuto**:
```logql
rate({job="hexarch", level="ERROR"}[1m])
```

---

### **4. Correlación: Logs + Métricas + Traces**

**Escenario**: Usuario reporta "la creación de usuario está lenta"

**Paso 1**: Ver métrica de latencia
```
Datasource: Prometheus
Query: histogram_quantile(0.99, rate(http_server_requests_seconds_bucket{uri="/api/v1/users"}[5m]))
```

**Paso 2**: Identificar request lento
```
Datasource: Zipkin
Search: minDuration > 500ms
```

**Paso 3**: Ver logs de ese trace
```
Datasource: Loki
Query: {job="hexarch", traceId="f47ac10b"}
```

**Resultado**: Ves TODA la historia del request lento 🎯

---

### **5. Dashboard Unificado en Grafana**

**Panel 1: Métricas**
```
Requests/sec, Error rate, Latency p99
```

**Panel 2: Logs en tiempo real**
```logql
{job="hexarch"} | level="ERROR"
```

**Panel 3: Trace spans**
```
Zipkin: requests más lentos
```

**Captura de pantalla** (ejemplo):
```
┌────────────────────────────────────────────────────────────┐
│ Hexarch Observability Dashboard          [Last 15 min ▼]  │
├────────────────────────────────────────────────────────────┤
│ 📊 MÉTRICAS                                                │
│ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐          │
│ │ Req/s   │ │ Errors  │ │ p99     │ │ Users   │          │
│ │  42     │ │  0.5%   │ │ 120ms   │ │  1,234  │          │
│ └─────────┘ └─────────┘ └─────────┘ └─────────┘          │
│                                                            │
│ 📝 LOGS (últimos 50)                                       │
│ ┌──────────────────────────────────────────────────────┐  │
│ │ 10:30:00 [f47ac] 550e INFO  - User created: id=123  │  │
│ │ 10:30:01 [a23bc] 7f2a WARN  - Retry attempt 2/3     │  │
│ │ 10:30:02 [c45de] 9d4b ERROR - Email send failed     │  │
│ └──────────────────────────────────────────────────────┘  │
│                                                            │
│ 🔗 TRACES (slowest 10)                                     │
│ ┌──────────────────────────────────────────────────────┐  │
│ │ POST /users - 520ms [Ver span timeline]             │  │
│ │ GET /users/123 - 380ms [Ver span timeline]          │  │
│ └──────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────┘
```

---

## Soluciones Cloud (Managed)

Si tu empresa usa **cloud providers**, puedes usar servicios managed que eliminan el trabajo de mantener la infraestructura:

### **AWS**

**CloudWatch**:
- **CloudWatch Logs**: Almacena logs de ECS/EKS/Lambda
- **CloudWatch Metrics**: Métricas custom + métricas de AWS services
- **CloudWatch Insights**: Queries sobre logs (SQL-like)
- **X-Ray**: Distributed tracing

**Setup**:
```yaml
# application.yaml
logging:
  config: classpath:logback-spring-cloudwatch.xml

management:
  metrics:
    export:
      cloudwatch:
        namespace: Hexarch
        enabled: true
```

**Pros**:
- ✅ Cero mantenimiento
- ✅ Integración nativa con servicios AWS
- ✅ Escalabilidad automática

**Contras**:
- ❌ Caro a escala (factura por GB de logs)
- ❌ UI no tan buena como Grafana/Kibana
- ❌ Vendor lock-in

---

### **Azure**

**Azure Monitor**:
- **Log Analytics**: Logs centralizados (usa KQL query language)
- **Application Insights**: APM + distributed tracing
- **Metrics**: Métricas custom

**Setup**:
```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>applicationinsights-spring-boot-starter</artifactId>
</dependency>
```

```yaml
# application.yaml
azure:
  application-insights:
    instrumentation-key: ${APPINSIGHTS_INSTRUMENTATIONKEY}
```

---

### **Google Cloud Platform**

**Cloud Logging (antes Stackdriver)**:
- Logs centralizados
- Integración con GKE, Cloud Run, App Engine
- Queries con Log Explorer

**Cloud Trace**: Distributed tracing

---

### **Datadog / New Relic / Splunk** 💰

Soluciones **SaaS todo-en-uno**:

**Datadog**:
```yaml
# application.yaml
management:
  metrics:
    export:
      datadog:
        api-key: ${DD_API_KEY}
        application-key: ${DD_APP_KEY}
        enabled: true
```

**Pros**:
- ✅ UI excelente (mejor que cloud providers)
- ✅ APM + Logs + Métricas + Traces unificado
- ✅ Alerting avanzado
- ✅ Machine Learning para anomaly detection

**Contras**:
- ❌ **Muy caro**: $15-50 USD/host/mes
- ❌ Vendor lock-in

**Cuándo usar**: Empresas con presupuesto que quieren lo mejor sin complicaciones

---

### **Comparativa: On-Premise vs Cloud**

| Aspecto | Loki/ELK (Self-hosted) | Cloud Managed (AWS/Azure/GCP) | SaaS (Datadog/New Relic) |
|---------|------------------------|-------------------------------|--------------------------|
| **Costo** | Infra + tiempo de setup | Pay-per-use (puede ser caro) | Caro (~$20/host/mes) |
| **Mantenimiento** | Tú lo mantienes | Proveedor cloud | Proveedor SaaS |
| **Escalabilidad** | Manual | Automática | Automática |
| **UI** | Grafana/Kibana (muy buena) | Básica | Excelente |
| **Vendor Lock-in** | ❌ No | ⚠️ Medio | ✅ Sí |
| **Setup** | Complejo | Medio | Simple (agente) |

---

### **Recomendación según contexto**

#### **Startup/Proyecto personal**:
→ **Grafana Loki** (gratis, simple, suficiente)

#### **Empresa pequeña/mediana** (< 50 servicios):
→ **Loki** si tienes DevOps o **CloudWatch/Azure Monitor** si usas cloud

#### **Empresa grande** (50+ servicios):
→ **ELK Stack** (self-hosted) o **Datadog** (si tienes presupuesto)

#### **Regulaciones estrictas** (banca, salud):
→ **ELK on-premise** (control total de datos)

---

## Mejores Prácticas de Logs en Producción

### **1. Structured Logging (JSON)**

**Problema**: Logs de texto son difíciles de parsear
```
2024-01-15 10:30:00 User johndoe created with ID 123
```

**Solución**: JSON structured logs
```json
{
  "timestamp": "2024-01-15T10:30:00.123Z",
  "level": "INFO",
  "traceId": "f47ac10b",
  "correlationId": "550e8400",
  "message": "User created",
  "userId": "123",
  "username": "johndoe"
}
```

**Configurar en Spring Boot**:
```xml
<!-- pom.xml -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

```xml
<!-- logback-spring.xml -->
<appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <includeMdcKeyName>traceId</includeMdcKeyName>
        <includeMdcKeyName>spanId</includeMdcKeyName>
        <includeMdcKeyName>correlationId</includeMdcKeyName>
    </encoder>
</appender>
```

**Beneficio**: Fácil de parsear y buscar en Elasticsearch/Loki

---

### **2. Log Retention Policy**

**No guardes logs para siempre** (costoso y poco útil):

| Tipo | Retention | Razón |
|------|-----------|-------|
| **DEBUG** | 1 día | Solo para troubleshooting activo |
| **INFO** | 30 días | Suficiente para análisis reciente |
| **WARN** | 90 días | Detectar problemas recurrentes |
| **ERROR** | 180 días | Cumplimiento y análisis de incidentes |

**Configurar en Loki**:
```yaml
limits_config:
  retention_period: 720h  # 30 días
```

**Configurar en Elasticsearch**:
```bash
# Borrar índices más viejos de 30 días
curator delete indices --older-than 30 --time-unit days
```

---

### **3. Log Sampling**

**Problema**: Demasiados logs en producción (millones/día)

**Solución**: Sample logs de INFO, loguea todos los ERROR

```java
@Component
public class SamplingLogger {

    private final Logger log = LoggerFactory.getLogger(SamplingLogger.class);
    private final Random random = new Random();
    private final double sampleRate = 0.1; // 10%

    public void infoSampled(String message, Object... args) {
        if (random.nextDouble() < sampleRate) {
            log.info(message, args);
        }
    }

    public void error(String message, Throwable ex) {
        log.error(message, ex);  // SIEMPRE loguea errores
    }
}
```

---

### **4. Sensitive Data Masking**

**NUNCA loguees datos sensibles**:
- ❌ Passwords
- ❌ Números de tarjeta de crédito
- ❌ API keys / tokens
- ❌ PII (Personally Identifiable Information): SSN, DNI, etc.

**MAL**:
```java
log.info("User login: username={}, password={}", username, password);  // ❌ NUNCA
```

**BIEN**:
```java
log.info("User login: username={}", username);  // ✅ Sin password

// Si necesitas loguear email (PII), enmascara
log.info("User created: email={}", maskEmail(email));
// Output: "User created: email=j***@example.com"
```

**Enmascarar automáticamente**:
```java
public class SensitiveDataConverter extends ClassicConverter {
    private static final Pattern EMAIL = Pattern.compile("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        return EMAIL.matcher(message).replaceAll("***@$2");
    }
}
```

---

### **5. Correlation ID Best Practices**

**Propagar Correlation ID a TODOS los servicios downstream**:

```java
@Component
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {

        // Obtener Correlation ID del MDC
        String correlationId = MDC.get("correlationId");

        // Añadirlo al header del request saliente
        if (correlationId != null) {
            request.getHeaders().add("X-Correlation-ID", correlationId);
        }

        return execution.execute(request, body);
    }
}
```

**Resultado**: Puedes trazar un request a través de 10+ microservicios con un solo ID 🎯

---

### **6. Log Levels en Producción**

**Configuración recomendada**:
```yaml
logging:
  level:
    root: INFO  # Default para todo

    # Tu aplicación: INFO
    com.example.hexarch: INFO

    # Librerías externas: WARN (reducir ruido)
    org.springframework: WARN
    org.hibernate: WARN
    com.zaxxer.hikari: WARN

    # DEBUG solo para troubleshooting
    # com.example.hexarch.user.infrastructure: DEBUG  # Descomentar si necesitas debug
```

**Cambio dinámico** (sin reiniciar app):
```bash
# Activar DEBUG temporalmente para un package
curl -X POST http://localhost:8080/actuator/loggers/com.example.hexarch.user \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# Volver a INFO
curl -X POST http://localhost:8080/actuator/loggers/com.example.hexarch.user \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "INFO"}'
```

---

## Ejemplos Prácticos en el Código

### **Ubicación en Hexarch**

```
src/main/java/
├── user/
│   └── application/
│       └── service/
│           └── CreateUserUseCase.java       ← Logs INFO + Métrica counter
│
├── notifications/
│   └── application/
│       └── service/
│           └── EmailService.java            ← Logs WARN/ERROR + Timer metric
│
├── shared/
│   └── infrastructure/
│       └── web/
│           └── CorrelationIdFilter.java     ← Correlation ID propagation
│
└── config/
    └── MetricsConfig.java                   ← Métricas customizadas
```

---

### **Ver métricas en acción**

**1. Crear usuario**:
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username": "johndoe", "email": "john@example.com"}'
```

**2. Ver logs** (consola):
```
2024-01-15 10:30:00 [f47ac10b,1a2b3c4d] 550e8400 INFO  - Creating user: username=johndoe
2024-01-15 10:30:00 [f47ac10b,1a2b3c4d] 550e8400 INFO  - User created: userId=123
2024-01-15 10:30:00 [f47ac10b,1a2b3c4d] 550e8400 INFO  - Publishing event: UserCreatedEvent
2024-01-15 10:30:00 [f47ac10b,1a2b3c4d] 550e8400 INFO  - Event published to Kafka topic: user.created
```

**3. Ver métricas**:
```bash
curl http://localhost:8080/actuator/prometheus | grep users_created
# users_created_total{status="success",environment="local"} 1.0
```

**4. Ver trace en Zipkin**:
- URL: http://localhost:9411
- Buscar trace: `f47ac10b-8c42-11eb-8dcd-0242ac130003`
- Ver timeline de spans

---

## Alerting

### **Alertas en Prometheus**

**`monitoring/prometheus/alerts.yml`**:
```yaml
groups:
  - name: hexarch_alerts
    interval: 30s
    rules:
      # CPU alta
      - alert: HighCPUUsage
        expr: process_cpu_usage > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High CPU usage detected"
          description: "CPU usage is {{ $value }}% (threshold: 80%)"

      # Error rate alta
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m]) > 0.05
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }}% (threshold: 5%)"

      # Latencia p99 alta
      - alert: HighLatency
        expr: histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m])) > 0.5
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High p99 latency detected"
          description: "p99 latency is {{ $value }}s (threshold: 500ms)"
```

---

### **Notificaciones en Grafana**

**Configurar Slack**:
```yaml
# monitoring/grafana/alerting/notification-channels.yml
apiVersion: 1

notifiers:
  - name: slack
    type: slack
    uid: slack1
    settings:
      url: https://hooks.slack.com/services/YOUR/WEBHOOK/URL
      recipient: '#alerts'
      username: Grafana
```

---

## Checklist de Observabilidad

### ✅ **Logs**
- [x] Usar SLF4J + Logback
- [x] Formato estructurado con correlationId
- [x] Nivel correcto (INFO para eventos, ERROR para fallos)
- [x] NO logear la misma exception múltiples veces
- [x] Incluir contexto (userId, traceId, correlationId)

### ✅ **Métricas**
- [x] Actuator habilitado con endpoint `/actuator/prometheus`
- [x] Métricas customizadas de negocio (users.created.total)
- [x] Tags para filtrar (environment, status)
- [x] Dashboards en Grafana

### ✅ **Trazas**
- [x] Micrometer Tracing configurado
- [x] Zipkin endpoint configurado
- [x] Sampling rate ajustado (100% dev, 10% prod)
- [x] Correlation ID propagado entre servicios

### ✅ **Alerting**
- [x] Alertas configuradas (CPU, error rate, latency)
- [x] Notificaciones a Slack/PagerDuty
- [x] Runbooks documentados (qué hacer cuando alerta se dispara)

---

## Recursos

- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Zipkin Documentation](https://zipkin.io/)
- [OpenTelemetry](https://opentelemetry.io/)
- [SLF4J Documentation](https://www.slf4j.org/manual.html)

---

**Última actualización**: 2025-10-30
