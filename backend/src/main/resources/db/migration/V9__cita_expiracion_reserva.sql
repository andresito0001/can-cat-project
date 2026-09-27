-- =====================================================================
-- V9__cita_expiracion_reserva.sql
-- Añade expiración a citas Pendiente_Pago para liberar slots abandonados.
-- =====================================================================

ALTER TABLE cita ADD COLUMN IF NOT EXISTS expira_en TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_cita_expira_en
    ON cita(expira_en)
    WHERE expira_en IS NOT NULL;

COMMENT ON COLUMN cita.expira_en IS
    'Fecha límite para pagar una cita en Pendiente_Pago. NULL para otros estados.';