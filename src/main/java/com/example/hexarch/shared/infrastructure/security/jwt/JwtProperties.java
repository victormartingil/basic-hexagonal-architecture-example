package com.example.hexarch.shared.infrastructure.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT PROPERTIES - Configuración de JWT desde application.yaml
 *
 * ARQUITECTURA HEXAGONAL:
 * - Está en infrastructure porque es configuración técnica (no dominio)
 * - Está en "shared" porque JWT es transversal a toda la aplicación
 *
 * CONFIGURACIÓN EN application.yaml:
 * ```yaml
 * jwt:
 *   secret: tu-secret-key-debe-ser-muy-larga-y-segura-minimo-256-bits
 *   expiration: 86400000  # 24 horas en milisegundos
 * ```
 *
 * SEGURIDAD:
 * - El secret debe tener al menos 256 bits (32 caracteres) para HS256
 * - En producción, usar variables de entorno, NO hardcodear
 * - Rotar el secret periódicamente
 *
 * NOTA:
 * En este proyecto educativo, el secret está en application.yaml.
 * En producción REAL:
 * - Usar Spring Cloud Config Server
 * - Usar AWS Secrets Manager / Azure Key Vault / HashiCorp Vault
 * - Usar variables de entorno: ${JWT_SECRET}
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * Clave secreta para firmar tokens JWT
     *
     * REQUISITOS:
     * - Mínimo 256 bits (32 caracteres) para HMAC-SHA256
     * - Debe ser aleatoria y compleja
     * - NUNCA compartir ni commitear a Git
     *
     * GENERACIÓN DE SECRET:
     * ```bash
     * # Opción 1: OpenSSL
     * openssl rand -base64 32
     *
     * # Opción 2: Java (en tests)
     * KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
     * SecretKey secretKey = keyGen.generateKey();
     * String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
     * ```
     */
    private String secret;

    /**
     * Tiempo de expiración del token en milisegundos
     *
     * VALORES COMUNES:
     * - 1 hora: 3600000 ms
     * - 24 horas: 86400000 ms
     * - 7 días: 604800000 ms
     *
     * BEST PRACTICE:
     * - Tokens de acceso: corta duración (15 min - 1 hora)
     * - Refresh tokens: larga duración (7-30 días)
     *
     * En este proyecto usamos solo access tokens.
     */
    private long expiration;
}
