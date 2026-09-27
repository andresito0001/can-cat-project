-- =====================================================================
-- V11__simplificar_estados_cita.sql
-- Elimina 'Pagada' como estado de la cita.
-- El dinero vive en `pago.estado_pago`, la agenda en `cita.id_estado`.
--
-- Transiciones resultantes:
--   Pendiente_Pago → Confirmada  (al verificar pago o cobrar en mostrador)
--   Pendiente_Pago → Cancelada   (al rechazar pago o expirar)
--   Confirmada     → En_Atencion (veterinario inicia)
--   En_Atencion    → Completada  (veterinario guarda)
-- =====================================================================

-- 1. Migrar citas existentes en 'Pagada' a 'Confirmada'
UPDATE cita
SET id_estado = (SELECT id_estado FROM estado_cita WHERE nombre = 'Confirmada')
WHERE id_estado = (SELECT id_estado FROM estado_cita WHERE nombre = 'Pagada');

-- 2. Recrear índice parcial sin el ID de Pagada
--    (se usa el literal 6 = Cancelada; no se puede subquery en índice parcial)
DROP INDEX IF EXISTS idx_cita_veterinario_horario_activa;

CREATE UNIQUE INDEX idx_cita_veterinario_horario_activa
ON cita (id_veterinario, fecha_cita, hora_inicio)
WHERE id_veterinario IS NOT NULL
  AND id_estado <> 6;

-- 3. Eliminar el estado Pagada (ahora huérfano)
DELETE FROM estado_cita WHERE nombre = 'Pagada';