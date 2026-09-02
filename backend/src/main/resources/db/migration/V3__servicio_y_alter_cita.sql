-- src/main/resources/db/migration/V3__servicio_y_alter_cita.sql

-- ============================================================
-- 1. TABLA DE SERVICIOS
-- ============================================================
CREATE TABLE servicio (
    id_servicio       SERIAL PRIMARY KEY,
    nombre            VARCHAR(100) NOT NULL UNIQUE,
    tipo_atencion     VARCHAR(30)  NOT NULL CHECK (tipo_atencion IN ('Consulta','Vacunacion','Cirugia','Estetica')),
    descripcion       TEXT,
    duracion_minutos  INTEGER      NOT NULL DEFAULT 30 CHECK (duracion_minutos > 0),
    precio_usd        DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (precio_usd >= 0),
    activo            BOOLEAN      DEFAULT TRUE,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 2. DATOS INICIALES: SERVICIOS
-- ============================================================
INSERT INTO servicio (nombre, tipo_atencion, descripcion, duracion_minutos, precio_usd) VALUES
('Consulta General',       'Consulta',   'Consulta veterinaria general para diagnóstico y evaluación',        30,  15.00),
('Consulta de Control',    'Consulta',   'Revisión de seguimiento posterior a tratamiento',                    20,  10.00),
('Vacunación',             'Vacunacion', 'Aplicación de vacunas según esquema de inmunización',              20,  12.00),
('Desparasitación',        'Vacunacion', 'Administración de antiparasitarios internos y externos',           15,   8.00),
('Cirugía Menor',          'Cirugia',    'Castración, extirpación de tumores pequeños, etc.',                120, 80.00),
('Cirugía Mayor',          'Cirugia',    'Procedimientos quirúrgicos complejos con anestesia general',      180, 150.00),
('Baño y Corte',           'Estetica',   'Servicio completo de higiene y estética',                           60,  20.00),
('Corte de Uñas e Higiene','Estetica',   'Corte de uñas, limpieza de oídos y expresado de glándulas anal',  30,   8.00),
('Esterilización', 'Cirugia', 'Procedimiento quirúrgico de esterilización/castración para el control reproductivo y salud general', 90, 60.00);

-- ============================================================
-- 3. ALTER TABLE cita — Columnas nuevas
-- ============================================================

-- Relación con servicio
ALTER TABLE cita ADD COLUMN id_servicio INTEGER NOT NULL DEFAULT 0;
ALTER TABLE cita DROP COLUMN id_servicio;
ALTER TABLE cita ADD COLUMN id_servicio INTEGER NOT NULL REFERENCES servicio(id_servicio) ON DELETE RESTRICT;

-- Desglose de costos
ALTER TABLE cita ADD COLUMN costo_usd DECIMAL(10,2) NOT NULL DEFAULT 0.00;
ALTER TABLE cita ADD COLUMN costo_bs DECIMAL(12,2) NOT NULL DEFAULT 0.00;
ALTER TABLE cita ADD COLUMN tasa_cambio_aplicada DECIMAL(10,4) NOT NULL DEFAULT 0.00;

-- hora_fin: ahora obligatoria (siempre se calcula desde el servicio)
ALTER TABLE cita ALTER COLUMN hora_fin SET NOT NULL;

-- Constraint: hora_fin > hora_inicio
ALTER TABLE cita ADD CONSTRAINT chk_horas_cita CHECK (hora_fin > hora_inicio);

-- ============================================================
-- 4. ÍNDICES
-- ============================================================
CREATE INDEX idx_cita_vet_fecha ON cita(id_veterinario, fecha_cita);