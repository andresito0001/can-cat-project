
-- 1. Verificar que el rol Administrador exista (defensivo)
INSERT INTO rol (nombre_rol, descripcion, permisos_json)
VALUES ('Administrador', 'Control total del sistema', '["*"]'::jsonb)
ON CONFLICT (nombre_rol) DO NOTHING;

-- 2. Insertar el usuario Admin solo si no existe
INSERT INTO usuario (id_rol, correo_electronico, contrasena_hash, estado)
VALUES (
    (SELECT id_rol FROM rol WHERE nombre_rol = 'Administrador' LIMIT 1),
    'admin@cancat.com',
    '$2b$10$Dk5XOyIuJriCTonIhJSANOrFInBvOExq/DVG3x6F7r8EsM5L9HJd.',
    'Activo'
)
ON CONFLICT (correo_electronico) DO NOTHING;