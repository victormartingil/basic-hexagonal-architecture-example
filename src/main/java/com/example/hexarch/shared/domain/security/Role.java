package com.example.hexarch.shared.domain.security;

/**
 * ROLE - Enum de Roles del Sistema
 *
 * Define los roles disponibles en la aplicación para autorización.
 *
 * EN ARQUITECTURA HEXAGONAL:
 * - Este enum está en "shared/domain" porque es un concepto transversal
 * - Se usa en múltiples bounded contexts (User, Auth, etc.)
 * - Es parte del Domain Model pero compartido entre contextos
 *
 * ROLES:
 * - ADMIN: Administrador del sistema (acceso total)
 * - MANAGER: Gestor (puede crear usuarios, ver datos)
 * - VIEWER: Visualizador (solo puede ver datos)
 * - SUPPLIER: Proveedor (acceso limitado a recursos específicos)
 *
 * SPRING SECURITY:
 * Spring Security prefiere que los roles tengan prefijo "ROLE_"
 * pero lo añadiremos automáticamente en la configuración.
 * Aquí usamos nombres limpios: ADMIN, no ROLE_ADMIN.
 *
 * USO EN JWT:
 * Los roles se incluyen en el token JWT como claim "roles": ["ADMIN", "MANAGER"]
 * y se extraen para configurar las authorities de Spring Security.
 */
public enum Role {
    /**
     * Administrador del sistema
     * - Acceso completo
     * - Puede crear, leer, actualizar y eliminar usuarios
     * - Puede acceder a todas las operaciones
     */
    ADMIN,

    /**
     * Gestor
     * - Puede crear y leer usuarios
     * - No puede eliminar usuarios
     * - Acceso a operaciones de gestión
     */
    MANAGER,

    /**
     * Visualizador
     * - Solo lectura
     * - Puede ver información de usuarios
     * - No puede realizar operaciones de escritura
     */
    VIEWER,

    /**
     * Proveedor
     * - Acceso limitado a recursos específicos
     * - Puede ver solo sus propios datos
     * - Acceso restringido
     */
    SUPPLIER;

    /**
     * Obtiene el nombre del rol con prefijo ROLE_ (para Spring Security)
     *
     * Spring Security espera authorities como "ROLE_ADMIN", "ROLE_MANAGER", etc.
     *
     * @return Nombre del rol con prefijo ROLE_
     */
    public String getAuthority() {
        return "ROLE_" + this.name();
    }

    /**
     * Convierte un string a Role (case-insensitive)
     *
     * @param roleName Nombre del rol (con o sin prefijo ROLE_)
     * @return Role correspondiente
     * @throws IllegalArgumentException Si el rol no existe
     */
    public static Role fromString(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }

        // Quitar prefijo ROLE_ si existe
        String cleanName = roleName.toUpperCase().replace("ROLE_", "");

        try {
            return Role.valueOf(cleanName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid role: " + roleName + ". Valid roles: ADMIN, MANAGER, VIEWER, SUPPLIER"
            );
        }
    }
}
