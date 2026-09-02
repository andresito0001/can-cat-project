ALTER TABLE personal ADD COLUMN nombre_completo VARCHAR(150) NOT NULL;

CREATE INDEX idx_personal_nombre ON personal(nombre_completo);