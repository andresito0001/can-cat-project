-- =====================================================================
-- V10__cita_slot_parcial.sql
-- Cambia la constraint de unicidad de slots:
--   - Antes: bloquea (vet, fecha, hora) SIEMPRE.
--   - Ahora: solo bloquea si la cita NO está cancelada.
-- Esto permite reutilizar un slot tras cancelación/expiración.
-- =====================================================================

DROP INDEX IF EXISTS idx_cita_veterinario_horario;

CREATE UNIQUE INDEX idx_cita_veterinario_horario_activa
    ON cita (id_veterinario, fecha_cita, hora_inicio)
    WHERE id_veterinario IS NOT NULL
      AND id_estado IN (1, 2, 3, 4);