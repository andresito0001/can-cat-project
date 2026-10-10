ALTER TABLE cita
    ADD COLUMN IF NOT EXISTS porcentaje_iva DECIMAL(5,2) NOT NULL DEFAULT 16.00;

COMMENT ON COLUMN cita.porcentaje_iva IS
    'IVA aplicado al costo_usd de la cita. Snapshot al momento de crear la cita.';