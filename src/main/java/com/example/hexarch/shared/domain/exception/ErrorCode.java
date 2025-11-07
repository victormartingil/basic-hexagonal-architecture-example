package com.example.hexarch.shared.domain.exception;

/**
 * SHARED LAYER - Error Code Catalog
 *
 * Enum centralizado que contiene TODOS los códigos de error de la aplicación
 * junto con sus mensajes descriptivos.
 *
 * VENTAJAS DE USAR UN ENUM:
 *
 * 1. CENTRALIZACIÓN:
 *    - Un único lugar para todos los códigos y mensajes de error
 *    - Fácil de mantener y documentar
 *    - Evita duplicación de códigos
 *
 * 2. TYPE-SAFETY (Seguridad de tipos):
 *    - El compilador asegura que solo se usen códigos existentes
 *    - No más Strings mágicos hardcodeados ("USER_001", "USER_002", etc.)
 *    - Autocompletado en el IDE
 *
 * 3. CONSISTENCIA:
 *    - Imposible tener códigos duplicados (el enum no lo permite)
 *    - Formato consistente en todos los errores
 *    - Evita errores tipográficos
 *
 * 4. MANTENIBILIDAD:
 *    - Fácil ver todos los errores de un vistazo
 *    - Cambiar un mensaje actualiza todas las referencias
 *    - Fácil detectar códigos no utilizados
 *
 * 5. INTERNACIONALIZACIÓN (i18n):
 *    - Base para implementar múltiples idiomas
 *    - Se puede combinar con ResourceBundle
 *    - Código de error constante, mensaje variable por idioma
 *
 * 6. AUTO-DOCUMENTACIÓN:
 *    - Lista completa de errores posibles
 *    - Nombres descriptivos en lugar de strings
 *    - Útil para documentación de API
 *
 * ANTES (sin enum):
 * throw new ValidationException("Username muy corto", "USER_002");  // String mágico
 *
 * DESPUÉS (con enum):
 * throw new ValidationException(ErrorCode.USERNAME_TOO_SHORT);      // Type-safe
 *
 * ESTRUCTURA:
 * - Cada valor tiene: código (USER_001), mensaje ("Username no puede estar vacío")
 * - Agrupados por categoría: validación, dominio, infraestructura
 * - Métodos útiles: getCode(), getMessage(), formatMessage()
 */
public enum ErrorCode {

    // ========================================
    // VALIDATION ERRORS (USER_001 - USER_005)
    // Errores de validación de Value Objects
    // ========================================

    /**
     * Username vacío o null
     * HTTP 400 - Bad Request
     * Parámetro opcional: valor recibido
     */
    USERNAME_EMPTY("USER_001", "Username no puede estar vacío (recibido: '%s')"),

    /**
     * Username demasiado corto (< 3 caracteres)
     * HTTP 400 - Bad Request
     * Parámetros: username recibido, longitud actual
     */
    USERNAME_TOO_SHORT("USER_002", "Username '%s' debe tener al menos 3 caracteres (actual: %d)"),

    /**
     * Username demasiado largo (> 50 caracteres)
     * HTTP 400 - Bad Request
     * Parámetros: username recibido, longitud actual
     */
    USERNAME_TOO_LONG("USER_003", "Username '%s' no puede tener más de 50 caracteres (actual: %d)"),

    /**
     * Email vacío o null
     * HTTP 400 - Bad Request
     * Parámetro opcional: valor recibido
     */
    EMAIL_EMPTY("USER_004", "Email no puede estar vacío (recibido: '%s')"),

    /**
     * Email con formato inválido
     * HTTP 400 - Bad Request
     * Parámetro: email recibido
     */
    EMAIL_INVALID_FORMAT("USER_005", "Email '%s' no tiene un formato válido"),

    // ========================================
    // DOMAIN ERRORS (USER_006, USER_404)
    // Errores de reglas de negocio
    // ========================================

    /**
     * Usuario ya existe en el sistema
     * HTTP 409 - Conflict
     */
    USER_ALREADY_EXISTS("USER_006", "User with username '%s' already exists"),

    /**
     * Usuario no encontrado
     * HTTP 404 - Not Found
     */
    USER_NOT_FOUND("USER_404", "User with ID '%s' not found"),

    // ========================================
    // INFRASTRUCTURE ERRORS (VALIDATION_001, TYPE_MISMATCH_001, INTERNAL_001)
    // Errores de la capa de infraestructura
    // ========================================

    /**
     * Error de validación de Bean Validation (@Valid)
     * HTTP 400 - Bad Request
     */
    BEAN_VALIDATION_ERROR("VALIDATION_001", "Los datos de entrada no son válidos"),

    /**
     * Error de conversión de tipos en parámetros
     * HTTP 400 - Bad Request
     */
    TYPE_MISMATCH("TYPE_MISMATCH_001", "El parámetro '%s' tiene un formato inválido. Valor recibido: '%s'. Tipo esperado: %s"),

    /**
     * Error interno no esperado
     * HTTP 500 - Internal Server Error
     */
    INTERNAL_ERROR("INTERNAL_001", "Ha ocurrido un error inesperado. Por favor, contacte al administrador.");

    // ========================================
    // CAMPOS Y CONSTRUCTOR
    // ========================================

    private final String code;
    private final String message;

    /**
     * Constructor privado del enum
     *
     * @param code código único del error (ej: USER_001)
     * @param message mensaje descriptivo del error
     */
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    // ========================================
    // GETTERS
    // ========================================

    /**
     * Obtiene el código único del error
     *
     * @return código del error (ej: "USER_001")
     */
    public String getCode() {
        return code;
    }

    /**
     * Obtiene el mensaje descriptivo del error
     *
     * @return mensaje del error
     */
    public String getMessage() {
        return message;
    }

    // ========================================
    // MÉTODOS ÚTILES
    // ========================================

    /**
     * Formatea el mensaje con parámetros dinámicos
     *
     * Útil para mensajes que incluyen valores variables:
     * - "User with username '%s' already exists"
     * - "El parámetro '%s' tiene un formato inválido"
     *
     * EJEMPLO:
     * ErrorCode.USER_ALREADY_EXISTS.formatMessage("john_doe")
     * → "User with username 'john_doe' already exists"
     *
     * @param args argumentos para formatear el mensaje
     * @return mensaje formateado
     */
    public String formatMessage(Object... args) {
        return String.format(message, args);
    }

    /**
     * Representación en String del ErrorCode
     *
     * @return código del error
     */
    @Override
    public String toString() {
        return code;
    }
}
