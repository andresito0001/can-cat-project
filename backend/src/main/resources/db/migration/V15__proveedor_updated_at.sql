-- =====================================================================
-- V15__proveedor_updated_at.sql
-- Añade updated_at a proveedor para trazabilidad.
-- =====================================================================

ALTER TABLE proveedor
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Trigger para mantener updated_at automáticamente
DROP TRIGGER IF EXISTS update_proveedor_updated_at ON proveedor;
CREATE TRIGGER update_proveedor_updated_at
    BEFORE UPDATE ON proveedor
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();