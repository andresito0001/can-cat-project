-- =====================================================================
-- V13__seed_proveedores.sql
-- Proveedores base para el módulo de almacén.
-- Idempotente: usa ON CONFLICT sobre el RIF.
-- =====================================================================

INSERT INTO proveedor (rif, nombre_empresa, nombre_contacto, telefono, correo, direccion, tipo_suministro, activo)
VALUES
    ('J-12345678-9', 'Distribuidora VetMed C.A.',
     'María López', '0212-5551234', 'ventas@vetmed.com',
     'Av. Libertador, Caracas', 'Medicamentos', TRUE),

    ('J-98765432-1', 'Alimentos Premium Animal',
     'Pedro Ramírez', '0212-5559876', 'pedidos@premiumanimal.com',
     'Zona Industrial, Valencia', 'Alimentos', TRUE),

    ('J-55555555-5', 'Insumos Clínicos del Centro',
     'Ana Torres', '0241-8887766', 'contacto@insumosclinicos.com',
     'Calle Comercio, Maracay', 'Mixto', TRUE)
ON CONFLICT (rif) DO NOTHING;

-- Reporte
SELECT 'proveedores totales' AS metrica, COUNT(*)::text AS valor FROM proveedor;