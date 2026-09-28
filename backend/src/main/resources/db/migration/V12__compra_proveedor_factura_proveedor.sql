-- =====================================================================
-- V12__compra_proveedor_factura_proveedor.sql
-- Añade el número de factura FÍSICA del proveedor a compra_proveedor.
-- El `numero_orden` sigue siendo interno y autogenerado (ENT-YYYYMMDD-NNNN).
-- =====================================================================

ALTER TABLE compra_proveedor
    ADD COLUMN IF NOT EXISTS numero_factura_proveedor VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_compra_factura_proveedor
    ON compra_proveedor(numero_factura_proveedor);

COMMENT ON COLUMN compra_proveedor.numero_factura_proveedor IS
    'Número de factura física del proveedor (Ej: FAC-2024-00123). '
    'Diferente de numero_orden que es interno y autogenerado.';