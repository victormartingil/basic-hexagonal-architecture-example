-- FLYWAY MIGRATION - V1
-- Crea la tabla 'users' para almacenar usuarios

-- FLYWAY:
-- Flyway es una herramienta de migración de bases de datos que:
-- - Versiona cambios en el esquema de la BD
-- - Ejecuta migraciones en orden (V1, V2, V3, etc.)
-- - Mantiene un historial de migraciones aplicadas
-- - Garantiza que todos los ambientes tengan el mismo esquema
--
-- CONVENCIÓN DE NOMBRES:
-- - V{version}__{descripcion}.sql
-- - Ejemplo: V1__create_users_table.sql
-- - V2__add_role_to_users.sql
-- - V3__create_products_table.sql

-- Crear tabla users
CREATE TABLE users (
    -- Clave primaria: UUID para identificar únicamente cada usuario
    id UUID PRIMARY KEY,

    -- Username: único, no nulo, máximo 50 caracteres
    username VARCHAR(50) NOT NULL UNIQUE,

    -- Email: único, no nulo, máximo 100 caracteres
    email VARCHAR(100) NOT NULL UNIQUE,

    -- Enabled: indica si el usuario está activo
    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    -- Created at: fecha de creación, no nulo, no actualizable
    -- TIMESTAMP WITH TIME ZONE para almacenar en UTC (mejor práctica)
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Crear índices para mejorar el rendimiento de búsquedas
-- Índice en username (ya es único, pero mejora las consultas)
CREATE INDEX idx_users_username ON users(username);

-- Índice en email (ya es único, pero mejora las consultas)
CREATE INDEX idx_users_email ON users(email);

-- Comentarios en la tabla (documentación en la BD)
COMMENT ON TABLE users IS 'Tabla de usuarios del sistema';
COMMENT ON COLUMN users.id IS 'Identificador único del usuario';
COMMENT ON COLUMN users.username IS 'Nombre de usuario único';
COMMENT ON COLUMN users.email IS 'Email único del usuario';
COMMENT ON COLUMN users.enabled IS 'Indica si el usuario está habilitado';
COMMENT ON COLUMN users.created_at IS 'Timestamp de creación del usuario en UTC (TIMESTAMP WITH TIME ZONE)';
