-- ============================================
-- V1__schema_inicial.sql
-- Modelo simplificado para Clinica Veterinaria
-- PostgreSQL 15+
-- ============================================

-- Extensiones
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================
-- 1. CATALOGOS BASE
-- ============================================

CREATE TABLE rol (
    id_rol SERIAL PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    permisos_json JSONB DEFAULT '[]'::jsonb,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE metodo_pago (
    id_metodo_pago SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    datos_requeridos JSONB DEFAULT '{}'::jsonb,
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE especie (
    id_especie SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE raza (
    id_raza SERIAL PRIMARY KEY,
    id_especie INTEGER NOT NULL REFERENCES especie(id_especie) ON DELETE RESTRICT,
    nombre VARCHAR(100) NOT NULL,
    caracteristicas VARCHAR(500),
    UNIQUE(id_especie, nombre)
);

CREATE TABLE categoria_producto (
    id_categoria SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    requiere_prescripcion BOOLEAN DEFAULT FALSE
);

CREATE TABLE estado_cita (
    id_estado SERIAL PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE,
    color_ui CHAR(7) NOT NULL DEFAULT '#000000',
    orden_flujo INTEGER NOT NULL DEFAULT 0,
    es_final BOOLEAN DEFAULT FALSE
);

-- ============================================
-- 2. USUARIOS Y PERSONAS
-- ============================================

CREATE TABLE usuario (
    id_usuario SERIAL PRIMARY KEY,
    id_rol INTEGER NOT NULL REFERENCES rol(id_rol),
    correo_electronico VARCHAR(100) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'Activo' CHECK (estado IN ('Activo', 'Inactivo', 'Bloqueado')),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cliente (
    id_cliente SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL UNIQUE REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    nombre_completo VARCHAR(150) NOT NULL,
    documento_identidad VARCHAR(20) NOT NULL UNIQUE,
    telefono_principal VARCHAR(20) NOT NULL,
    telefono_secundario VARCHAR(20),
    direccion TEXT,
    ciudad VARCHAR(50),
    fecha_nacimiento DATE,
    preferencias_notificacion JSONB DEFAULT '{"email": true, "sms": false}'::jsonb,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE personal (
    id_personal SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL UNIQUE REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    codigo_empleado VARCHAR(20) NOT NULL UNIQUE,
    cargo VARCHAR(50) NOT NULL CHECK (cargo IN ('Veterinario', 'Recepcionista', 'Encargado_Almacen', 'Administrador')),
    especialidad VARCHAR(100),
    fecha_contratacion DATE NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    horario_atencion JSONB DEFAULT '{}'::jsonb,
    licencia_profesional VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 3. MASCOTAS
-- ============================================

CREATE TABLE mascota (
    id_mascota SERIAL PRIMARY KEY,
    id_cliente INTEGER NOT NULL REFERENCES cliente(id_cliente) ON DELETE RESTRICT,
    id_especie INTEGER NOT NULL REFERENCES especie(id_especie),
    id_raza INTEGER REFERENCES raza(id_raza) ON DELETE SET NULL,
    nombre VARCHAR(50) NOT NULL,
    fecha_nacimiento DATE,
    sexo CHAR(1) NOT NULL CHECK (sexo IN ('M', 'H')),
    color VARCHAR(30),
    peso_actual DECIMAL(5,2),
    esterilizado BOOLEAN DEFAULT FALSE,
    alergias_conocidas TEXT,
    condiciones_preexistentes TEXT,
    activo BOOLEAN DEFAULT TRUE,
    fallecido BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 4. AGENDA
-- ============================================

CREATE TABLE cita (
    id_cita SERIAL PRIMARY KEY,
    id_mascota INTEGER NOT NULL REFERENCES mascota(id_mascota) ON DELETE RESTRICT,
    id_veterinario INTEGER REFERENCES personal(id_personal) ON DELETE SET NULL,
    id_estado INTEGER NOT NULL REFERENCES estado_cita(id_estado),
    fecha_cita DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME,
    motivo_consulta TEXT NOT NULL,
    tipo_atencion VARCHAR(30) NOT NULL CHECK (tipo_atencion IN ('Consulta', 'Vacunacion', 'Cirugia', 'Estetica')),
    costo_estimado DECIMAL(10,2) NOT NULL DEFAULT 0,
    observaciones_recepcion TEXT,
    fecha_solicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Evitar solapamiento de citas por veterinario
CREATE UNIQUE INDEX idx_cita_veterinario_horario 
ON cita(id_veterinario, fecha_cita, hora_inicio) 
WHERE id_veterinario IS NOT NULL;

-- ============================================
-- 5. CLINICA (Atencion + Historial fusionado)
-- ============================================

CREATE TABLE atencion_clinica (
    id_atencion SERIAL PRIMARY KEY,
    id_cita INTEGER NOT NULL UNIQUE REFERENCES cita(id_cita) ON DELETE RESTRICT,
    id_veterinario INTEGER NOT NULL REFERENCES personal(id_personal),
    fecha_hora_inicio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_hora_fin TIMESTAMP,
    motivo_detallado TEXT,
    sintomas_observados TEXT,
    diagnostico_principal TEXT,
    diagnosticos_diferenciales TEXT,
    tratamiento_prescrito TEXT,
    observaciones_generales TEXT,
    proxima_cita_recomendada DATE,
    estado_atencion VARCHAR(20) NOT NULL DEFAULT 'En_Proceso' CHECK (estado_atencion IN ('En_Proceso', 'Finalizada', 'Derivada')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Entradas de historial directamente vinculadas a mascota (sin tabla historial_clinico intermedia)
CREATE TABLE entrada_historial (
    id_entrada SERIAL PRIMARY KEY,
    id_mascota INTEGER NOT NULL REFERENCES mascota(id_mascota) ON DELETE RESTRICT,
    id_atencion INTEGER UNIQUE REFERENCES atencion_clinica(id_atencion) ON DELETE SET NULL,
    tipo_registro VARCHAR(20) NOT NULL CHECK (tipo_registro IN ('Consulta', 'Vacuna', 'Examen', 'Cirugia')),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resumen_ejecutivo TEXT NOT NULL,
    documentos_adjuntos JSONB DEFAULT '[]'::jsonb,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 6. INVENTARIO
-- ============================================

CREATE TABLE proveedor (
    id_proveedor SERIAL PRIMARY KEY,
    rif VARCHAR(20) NOT NULL UNIQUE,
    nombre_empresa VARCHAR(150) NOT NULL,
    nombre_contacto VARCHAR(100),
    telefono VARCHAR(20),
    correo VARCHAR(100),
    direccion TEXT,
    tipo_suministro VARCHAR(20) CHECK (tipo_suministro IN ('Medicamentos', 'Alimentos', 'Mixto')),
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE producto (
    id_producto SERIAL PRIMARY KEY,
    id_categoria INTEGER NOT NULL REFERENCES categoria_producto(id_categoria),
    id_proveedor_predeterminado INTEGER REFERENCES proveedor(id_proveedor) ON DELETE SET NULL,
    codigo_sku VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    unidad_medida VARCHAR(20) NOT NULL DEFAULT 'Unidad' CHECK (unidad_medida IN ('Unidad', 'Kg', 'ml', 'caja', 'lt')),
    precio_venta DECIMAL(10,2) NOT NULL CHECK (precio_venta >= 0),
    costo_adquisicion DECIMAL(10,2) CHECK (costo_adquisicion >= 0),
    stock_actual INTEGER NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    stock_minimo INTEGER NOT NULL DEFAULT 5 CHECK (stock_minimo >= 0),
    stock_maximo INTEGER CHECK (stock_maximo IS NULL OR stock_maximo >= stock_minimo),
    requiere_receta BOOLEAN DEFAULT FALSE,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE movimiento_inventario (
    id_movimiento SERIAL PRIMARY KEY,
    id_producto INTEGER NOT NULL REFERENCES producto(id_producto),
    id_personal INTEGER NOT NULL REFERENCES personal(id_personal),
    tipo_movimiento VARCHAR(20) NOT NULL CHECK (tipo_movimiento IN ('Entrada', 'Salida', 'Ajuste', 'Vencimiento')),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    fecha_movimiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    motivo VARCHAR(50) NOT NULL CHECK (motivo IN ('Compra', 'Venta', 'Consumo_Clinica', 'Perdida', 'Ajuste')),
    documento_referencia VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE compra_proveedor (
    id_compra SERIAL PRIMARY KEY,
    id_proveedor INTEGER NOT NULL REFERENCES proveedor(id_proveedor),
    id_personal INTEGER NOT NULL REFERENCES personal(id_personal),
    numero_orden VARCHAR(50) NOT NULL UNIQUE,
    fecha_orden DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_recepcion DATE,
    estado_compra VARCHAR(30) NOT NULL DEFAULT 'Solicitada' CHECK (estado_compra IN ('Solicitada', 'Recibida_Parcial', 'Recibida_Total', 'Cancelada')),
    monto_total DECIMAL(10,2) DEFAULT 0,
    observaciones_recepcion TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE detalle_compra (
    id_detalle SERIAL PRIMARY KEY,
    id_compra INTEGER NOT NULL REFERENCES compra_proveedor(id_compra) ON DELETE CASCADE,
    id_producto INTEGER NOT NULL REFERENCES producto(id_producto),
    cantidad_solicitada INTEGER NOT NULL CHECK (cantidad_solicitada > 0),
    cantidad_recibida INTEGER DEFAULT 0 CHECK (cantidad_recibida >= 0),
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal DECIMAL(10,2) GENERATED ALWAYS AS (cantidad_recibida * precio_unitario) STORED,
    fecha_vencimiento_lote DATE,
    numero_lote VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 7. FACTURACION Y PAGOS
-- ============================================

CREATE TABLE factura (
    id_factura SERIAL PRIMARY KEY,
    id_cliente INTEGER NOT NULL REFERENCES cliente(id_cliente),
    id_cita INTEGER UNIQUE REFERENCES cita(id_cita) ON DELETE SET NULL,
    id_personal INTEGER NOT NULL REFERENCES personal(id_personal),
    numero_control VARCHAR(50) NOT NULL UNIQUE,
    fecha_emision TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0,
    porcentaje_descuento DECIMAL(5,2) DEFAULT 0 CHECK (porcentaje_descuento BETWEEN 0 AND 100),
    monto_descuento DECIMAL(10,2) GENERATED ALWAYS AS (subtotal * porcentaje_descuento / 100) STORED,
    porcentaje_iva DECIMAL(5,2) DEFAULT 16.00 CHECK (porcentaje_iva >= 0),
    monto_iva DECIMAL(10,2) GENERATED ALWAYS AS ((subtotal - (subtotal * porcentaje_descuento / 100)) * porcentaje_iva / 100) STORED,
    total_neto DECIMAL(10,2) GENERATED ALWAYS AS (
        subtotal - (subtotal * porcentaje_descuento / 100) + 
        ((subtotal - (subtotal * porcentaje_descuento / 100)) * porcentaje_iva / 100)
    ) STORED,
    estado_factura VARCHAR(20) NOT NULL DEFAULT 'Emitida' CHECK (estado_factura IN ('Emitida', 'Anulada', 'Nota_Credito')),
    metodo_pago_principal VARCHAR(50),
    observaciones_fiscales TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE detalle_factura (
    id_detalle SERIAL PRIMARY KEY,
    id_factura INTEGER NOT NULL REFERENCES factura(id_factura) ON DELETE CASCADE,
    tipo_item VARCHAR(30) NOT NULL CHECK (tipo_item IN ('Servicio_Consulta', 'Producto_Farmacia', 'Examen_Lab')),
    id_referencia INTEGER NOT NULL, -- ID de producto o codigo de servicio
    descripcion VARCHAR(255) NOT NULL,
    cantidad INTEGER NOT NULL DEFAULT 1 CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal DECIMAL(10,2) GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
    descuento_aplicado DECIMAL(10,2) DEFAULT 0 CHECK (descuento_aplicado >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pago (
    id_pago SERIAL PRIMARY KEY,
    id_factura INTEGER NOT NULL REFERENCES factura(id_factura),
    id_metodo_pago INTEGER NOT NULL REFERENCES metodo_pago(id_metodo_pago),
    id_cliente INTEGER NOT NULL REFERENCES cliente(id_cliente),
    monto DECIMAL(10,2) NOT NULL CHECK (monto > 0),
    fecha_pago TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    referencia_transaccion VARCHAR(100),
    comprobante_adjunto VARCHAR(500), -- URL
    estado_pago VARCHAR(30) NOT NULL DEFAULT 'Pendiente_Verificacion' CHECK (estado_pago IN ('Pendiente_Verificacion', 'Confirmado', 'Rechazado')),
    verificado_por INTEGER REFERENCES personal(id_personal) ON DELETE SET NULL,
    fecha_verificacion TIMESTAMP,
    observaciones_verificacion TEXT,
    metadata_json JSONB DEFAULT '{}'::jsonb, -- Datos especificos del metodo de pago
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 8. INDICES DE RENDIMIENTO
-- ============================================

CREATE INDEX idx_usuario_correo ON usuario(correo_electronico);
CREATE INDEX idx_usuario_rol ON usuario(id_rol);
CREATE INDEX idx_cliente_documento ON cliente(documento_identidad);
CREATE INDEX idx_cliente_usuario ON cliente(id_usuario);
CREATE INDEX idx_mascota_cliente ON mascota(id_cliente);
CREATE INDEX idx_mascota_especie ON mascota(id_especie);
CREATE INDEX idx_mascota_activo ON mascota(activo) WHERE activo = TRUE;
CREATE INDEX idx_cita_mascota ON cita(id_mascota);
CREATE INDEX idx_cita_fecha ON cita(fecha_cita);
CREATE INDEX idx_cita_estado ON cita(id_estado);
CREATE INDEX idx_cita_veterinario ON cita(id_veterinario);
CREATE INDEX idx_atencion_cita ON atencion_clinica(id_cita);
CREATE INDEX idx_atencion_veterinario ON atencion_clinica(id_veterinario);
CREATE INDEX idx_entrada_mascota ON entrada_historial(id_mascota);
CREATE INDEX idx_entrada_atencion ON entrada_historial(id_atencion);
CREATE INDEX idx_producto_categoria ON producto(id_categoria);
CREATE INDEX idx_producto_sku ON producto(codigo_sku);
CREATE INDEX idx_producto_stock ON producto(stock_actual, stock_minimo) WHERE stock_actual <= stock_minimo;
CREATE INDEX idx_movimiento_producto ON movimiento_inventario(id_producto);
CREATE INDEX idx_factura_cliente ON factura(id_cliente);
CREATE INDEX idx_factura_numero ON factura(numero_control);
CREATE INDEX idx_factura_cita ON factura(id_cita) WHERE id_cita IS NOT NULL;
CREATE INDEX idx_pago_factura ON pago(id_factura);
CREATE INDEX idx_pago_estado ON pago(estado_pago);
CREATE INDEX idx_detalle_factura ON detalle_factura(id_factura);

-- ============================================
-- 9. DATOS INICIALES (Catalogos)
-- ============================================

INSERT INTO rol (nombre_rol, descripcion, permisos_json) VALUES
('Administrador', 'Control total del sistema', '["*"]'::jsonb),
('Recepcionista', 'Agenda, facturacion y pagos', '["citas:*", "facturas:*", "pagos:*", "clientes:read"]'::jsonb),
('Veterinario', 'Atencion clinica y historial', '["atenciones:*", "historial:*", "citas:read", "productos:read"]'::jsonb),
('Encargado_Almacen', 'Gestion de inventario', '["productos:*", "movimientos:*", "compras:*", "proveedores:*"]'::jsonb),
('Cliente', 'Acceso a su mascota y citas', '["mascotas:own", "citas:own", "facturas:own"]'::jsonb);

INSERT INTO metodo_pago (nombre, descripcion, datos_requeridos) VALUES
('Transferencia', 'Pago por transferencia bancaria', '{"banco": "string", "numero_cuenta": "string", "referencia": "string"}'::jsonb),
('Pago_Movil', 'Pago via Pago Movil', '{"telefono": "string", "banco": "string", "referencia": "string"}'::jsonb),
('Efectivo', 'Pago en efectivo', '{}'::jsonb),
('Tarjeta', 'Pago con tarjeta de credito/debito', '{"ultimos_digitos": "string", "lote": "string"}'::jsonb);

INSERT INTO estado_cita (nombre, color_ui, orden_flujo, es_final) VALUES
('Pendiente_Pago', '#FFC107', 1, FALSE),
('Pagada', '#17A2B8', 2, FALSE),
('Confirmada', '#28A745', 3, FALSE),
('En_Atencion', '#FD7E14', 4, FALSE),
('Completada', '#6C757D', 5, TRUE),
('Cancelada', '#DC3545', 99, TRUE);

INSERT INTO categoria_producto (nombre, descripcion, requiere_prescripcion) VALUES
('Medicamento', 'Farmacos y tratamientos', TRUE),
('Alimento', 'Alimentos y dietas especiales', FALSE),
('Accesorio', 'Collares, jaulas, juguetes', FALSE),
('Servicio', 'Servicios clinicos', FALSE);

INSERT INTO especie (nombre, descripcion) VALUES
('Canino', 'Perros'),
('Felino', 'Gatos'),
('Ave', 'Aves domesticas y exoticas'),
('Roedor', 'Hamsters, conejos, cobayas'),
('Reptil', 'Tortugas, serpientes, lagartos');

INSERT INTO raza (id_especie, nombre, caracteristicas) VALUES
(1, 'Mestizo', 'Mezcla de razas'),
(1, 'Pastor Aleman', 'Perro guardian, inteligente'),
(1, 'Labrador', 'Amigable, activo'),
(2, 'Mestizo', 'Mezcla de razas'),
(2, 'Siames', 'Vocal, sociable'),
(2, 'Persa', 'Pelaje largo, tranquilo'),
(3, 'Canario', 'Cantor, pequeno'),
(3, 'Perico', 'Sociable, colorido'),
(4, 'Hamster', 'Nocturno, pequeno'),
(4, 'Conejo', 'Social, herbivoro');

-- ============================================
-- 10. FUNCIONES DE AUDITORIA
-- ============================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Aplicar triggers de updated_at
CREATE TRIGGER update_usuario_updated_at BEFORE UPDATE ON usuario FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_cliente_updated_at BEFORE UPDATE ON cliente FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_personal_updated_at BEFORE UPDATE ON personal FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_mascota_updated_at BEFORE UPDATE ON mascota FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_cita_updated_at BEFORE UPDATE ON cita FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_atencion_updated_at BEFORE UPDATE ON atencion_clinica FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_producto_updated_at BEFORE UPDATE ON producto FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_compra_updated_at BEFORE UPDATE ON compra_proveedor FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_factura_updated_at BEFORE UPDATE ON factura FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();