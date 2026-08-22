-- ============================================================
-- Migración V2: Tabla de tokens de recuperación de contraseña
-- ============================================================
-- Propósito: Rastrear tokens JWT de recuperación para garantizar
--            que cada enlace sea de un solo uso.

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    token VARCHAR(512) PRIMARY KEY,
    correo VARCHAR(255) NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_uso TIMESTAMP NULL
);

-- Índice para búsquedas por correo (útil si quieres invalidar tokens previos de un mismo usuario)
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_correo 
    ON password_reset_tokens(correo);

-- Índice para limpieza periódica de tokens expirados
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_expiracion 
    ON password_reset_tokens(fecha_expiracion);

-- Constraint: no permitir tokens usados sin fecha de uso (opcional, quítalo si prefieres flexibilidad)
-- ALTER TABLE password_reset_tokens 
--     ADD CONSTRAINT chk_fecha_uso CHECK (usado = FALSE OR fecha_uso IS NOT NULL);