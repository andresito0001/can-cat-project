-- =====================================================================
-- V16__personal_admin.sql
-- Crea el perfil de personal para admin@cancat.com.
--
-- Motivo: el módulo de almacén (movimientos, compras) requiere id_personal
-- como FK. El admin solo existía en `usuario` pero no en `personal`,
-- causando error 500 al ejecutar operaciones que registran auditoría.
-- =====================================================================

INSERT INTO personal (
    id_usuario,
    codigo_empleado,
    cargo,
    especialidad,
    fecha_contratacion,
    activo,
    horario_atencion,
    licencia_profesional,
    nombre_completo
)
SELECT
    u.id_usuario,
    'ADM-001',
    'Administrador',
    NULL,
    CURRENT_DATE,
    TRUE,
    '{}'::jsonb,
    NULL,
    'Administrador del Sistema'
FROM usuario u
WHERE u.correo_electronico = 'admin@cancat.com'
  AND NOT EXISTS (
      SELECT 1 FROM personal p WHERE p.id_usuario = u.id_usuario
  );

-- Reporte
SELECT
    u.correo_electronico,
    p.codigo_empleado,
    p.cargo,
    p.nombre_completo
FROM usuario u
LEFT JOIN personal p ON p.id_usuario = u.id_usuario
WHERE u.correo_electronico = 'admin@cancat.com';