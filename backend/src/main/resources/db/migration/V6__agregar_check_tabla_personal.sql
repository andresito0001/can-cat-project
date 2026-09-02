-- Estandarizar el dato existente al mismo casing
UPDATE personal SET especialidad = 'Cirugia' WHERE especialidad = 'CIRUGIA';

-- Agregar CHECK para que nadie inserte valores inválidos
--    NULL está permitido porque recepcionistas/admins no tienen especialidad
ALTER TABLE personal ADD CONSTRAINT chk_especialidad 
  CHECK (especialidad IS NULL OR especialidad IN ('Consulta', 'Vacunacion', 'Cirugia', 'Estetica'));