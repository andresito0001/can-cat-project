-- =====================================================================
-- V7__atencion_clinica_recetas_insumos.sql
-- CU 4.6.1.9 «Gestionar Atención Clínica»
-- Tablas base V1 (atencion_clinica, entrada_historial, producto,
-- movimiento_inventario) están VÍRGENES: los ALTER son seguros (S1).
-- =====================================================================

-- 1) id_mascota PRIMERO (lo usa el índice del punto 2 y el historial D8)
ALTER TABLE atencion_clinica
    ADD COLUMN id_mascota INTEGER NOT NULL
    REFERENCES mascota(id_mascota) ON DELETE RESTRICT;

-- 2) Constantes vitales + campos del CU (mapeo F3)
ALTER TABLE atencion_clinica
    ADD COLUMN anamnesis TEXT NOT NULL,
    ADD COLUMN peso_kg DECIMAL(6,2) NOT NULL
        CHECK (peso_kg > 0 AND peso_kg <= 999.99),
    ADD COLUMN temperatura_c DECIMAL(4,1) NOT NULL
        CHECK (temperatura_c BETWEEN 30 AND 45),
    ADD COLUMN frec_cardiaca INTEGER NOT NULL
        CHECK (frec_cardiaca BETWEEN 20 AND 400),
    ADD COLUMN frec_respiratoria INTEGER,
    ADD COLUMN indicaciones_dueno TEXT;

CREATE INDEX idx_atencion_mascota_fecha
    ON atencion_clinica(id_mascota, fecha_hora_inicio DESC);

-- 3) Récipe digital (D5). UNIQUE en id_atencion: 1 récipe por atención.
CREATE TABLE receta (
    id_receta              SERIAL PRIMARY KEY,
    id_atencion            INTEGER NOT NULL UNIQUE
                           REFERENCES atencion_clinica(id_atencion) ON DELETE CASCADE,
    codigo_receta          VARCHAR(30) NOT NULL UNIQUE,
    indicaciones_generales TEXT,
    fecha_emision          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE receta_item (
    id_item            SERIAL PRIMARY KEY,
    id_receta          INTEGER NOT NULL REFERENCES receta(id_receta) ON DELETE CASCADE,
    medicamento        VARCHAR(150) NOT NULL,
    concentracion      VARCHAR(100),
    dosis              VARCHAR(100) NOT NULL,
    via_administracion VARCHAR(50),
    frecuencia         VARCHAR(100) NOT NULL,
    duracion           VARCHAR(100) NOT NULL,
    orden              INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_receta_item_receta ON receta_item(id_receta);

-- 4) Insumos aplicados en la atención (snapshot de precio, D14)
CREATE TABLE atencion_insumo (
    id_consumo          SERIAL PRIMARY KEY,
    id_atencion         INTEGER NOT NULL
                        REFERENCES atencion_clinica(id_atencion) ON DELETE CASCADE,
    id_producto         INTEGER NOT NULL REFERENCES producto(id_producto),
    cantidad            INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario_usd DECIMAL(10,2) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_atencion_insumo_atencion ON atencion_insumo(id_atencion);

-- 5) Seed de productos de prueba
-- ⚠️ Verificar IDs de categoria_producto (1=Medicamento, 3=Accesorio).
--    Si difieren, sustituir el literal por:
--    (SELECT id_categoria FROM categoria_producto WHERE nombre = 'Medicamento')
INSERT INTO producto
    (id_categoria, codigo_sku, nombre, descripcion, unidad_medida,
     precio_venta, stock_actual, stock_minimo, requiere_receta)
VALUES
    (1,'VAC-001','Vacuna Antirrábica','Vacuna antirrábica canina/felina inactivada','Unidad',12.00,25,5,TRUE),
    (1,'VAC-002','Vacuna Trivalente Felina','Panleucopenia, rinotraqueítis y calicivirus','Unidad',15.00,10,3,TRUE),
    (1,'MED-001','Amoxicilina 250mg','Antibiótico de amplio espectro','Unidad',0.80,100,10,TRUE),
    (1,'MED-002','Metronidazol 500mg','Antimicrobiano/antiprotozoario','Unidad',0.60,50,10,TRUE),
    (1,'MED-003','Meloxicam 1.5mg/ml','Antiinflamatorio no esteroideo','ml',2.50,30,5,TRUE),
    (1,'MED-004','Ivermectina 1%','Antiparasitario','ml',3.00,15,3,TRUE),
    (1,'MED-005','Suero Lactato Ringer 500ml','Fluidoterapia IV','Unidad',6.00,8,4,FALSE),
    (3,'INS-001','Gasas Estériles 10x10','Gasas esterilizadas individuales','Unidad',1.50,40,10,FALSE),
    (3,'INS-002','Jeringas 5ml','Jeringas desechables con aguja','Unidad',0.30,60,20,FALSE),
    (3,'INS-003','Guantes Nitrilo T/M','Guantes de examen','Unidad',0.50,0,20,FALSE); -- stock 0 → probar alterno B