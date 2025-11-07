package com.example.hexarch.user.domain.exception;

import com.example.hexarch.shared.domain.exception.ErrorCode;

/**
 * DOMAIN LAYER - Base Exception
 *
 * Excepción base para todas las excepciones de dominio.
 * Todas las excepciones del dominio deben heredar de esta clase.
 *
 * ¿POR QUÉ USAR EXCEPCIONES DE DOMINIO PERSONALIZADAS?
 *
 * En lugar de usar RuntimeException directamente, creamos excepciones de dominio por:
 *
 * 1. EXPRESIVIDAD Y CLARIDAD:
 *    - throw new UserAlreadyExistsException(username);
 *      vs throw new RuntimeException("User already exists");
 *    - El código se lee mejor y es más autodocumentado
 *    - El tipo de la excepción comunica la intención
 *
 * 2. MANEJO ESPECÍFICO:
 *    - Podemos capturar y manejar cada tipo de forma diferente
 *    - UserAlreadyExistsException → HTTP 409 Conflict
 *    - ValidationException → HTTP 400 Bad Request
 *    - Con RuntimeException genérico, todo sería HTTP 500
 *
 * 3. CÓDIGOS DE ERROR ESTRUCTURADOS:
 *    - Cada excepción usa ErrorCode enum (type-safe)
 *    - El cliente puede identificar programáticamente el tipo de error
 *    - Útil para i18n (internacionalización) y logging
 *
 * 4. TIPO SEGURO:
 *    - El compilador ayuda a detectar qué excepciones puede lanzar un método
 *    - Podemos usar instanceof o pattern matching en los handlers
 *    - ErrorCode enum evita errores de tipeo
 *
 * 5. ENCAPSULACIÓN DE LÓGICA DE NEGOCIO:
 *    - Las excepciones de dominio representan violaciones de reglas de negocio
 *    - Son parte del lenguaje ubicuo (Ubiquitous Language) del dominio
 *    - No dependen de frameworks externos
 *
 * 6. MANTENIBILIDAD:
 *    - Fácil agregar comportamiento adicional (logging, métricas, etc.)
 *    - Fácil cambiar el mapeo a HTTP status codes
 *    - Un solo lugar para modificar el comportamiento de un tipo de error
 *
 * EJEMPLO DE USO:
 *
 * // En el dominio
 * if (userRepository.existsByUsername(username)) {
 *     throw new UserAlreadyExistsException(username);  // Expresivo y claro
 * }
 *
 * // En el GlobalExceptionHandler
 * @ExceptionHandler(UserAlreadyExistsException.class)
 * public ResponseEntity<ErrorResponse> handle(UserAlreadyExistsException ex) {
 *     return ResponseEntity.status(409).body(...);  // Mapeo específico
 * }
 *
 * HERENCIA DE RuntimeException:
 * - Hereda de RuntimeException (no checked) porque:
 *   - Las reglas de negocio pueden fallar en cualquier momento
 *   - No queremos obligar a todos los métodos a declararlas con "throws"
 *   - Son errores de los que la aplicación NO debe recuperarse automáticamente
 */
public abstract class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * Constructor con ErrorCode
     *
     * @param errorCode código de error del enum ErrorCode
     */
    public DomainException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * Constructor con ErrorCode y mensaje formateado
     *
     * @param errorCode código de error del enum ErrorCode
     * @param args argumentos para formatear el mensaje
     */
    public DomainException(ErrorCode errorCode, Object... args) {
        super(errorCode.formatMessage(args));
        this.errorCode = errorCode;
    }

    /**
     * Obtiene el ErrorCode enum
     *
     * @return ErrorCode del error
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Obtiene el código de error como String
     *
     * @return código de error (ej: USER_001, USER_002)
     */
    public String getErrorCodeAsString() {
        return errorCode.getCode();
    }
}
