-- =====================================================================
-- V17__seed_inventario_inicial.sql
-- Inventario inicial coherente con los servicios de la clínica:
--   Consulta · Vacunación · Desparasitación · Cirugía · Estética
-- Idempotente: no duplica SKUs existentes.
-- =====================================================================

-- 1) Restock de productos existentes que quedaron en 0 (por pruebas)
UPDATE producto
SET stock_actual = GREATEST(stock_minimo * 4, 20),
    updated_at   = CURRENT_TIMESTAMP
WHERE stock_actual = 0
  AND activo = TRUE;

-- 2) Nuevos productos — categorías:
--    1 = Medicamento  ·  2 = Alimento  ·  3 = Accesorio
-- Uso de subqueries para no depender de IDs hardcodeados.

-- ═══════════════════════════════════════════════════════════════════
-- VACUNAS (coherentes con el servicio "Vacunación")
-- ═══════════════════════════════════════════════════════════════════
INSERT INTO producto (id_categoria, codigo_sku, nombre, descripcion, unidad_medida,
                      precio_venta, costo_adquisicion, stock_actual, stock_minimo,
                      stock_maximo, requiere_receta, activo)
VALUES
    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'VAC-003','Vacuna Polivalente Canina',
     'Parvovirus, Distemper, Hepatitis, Parainfluenza, Leptospira',
     'Unidad',18.00,9.50,20,5,60,TRUE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'VAC-004','Vacuna Leptospira Bivalente',
     'Protección contra L. canicola e icterohaemorrhagiae',
     'Unidad',14.00,7.00,15,4,40,TRUE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'VAC-005','Vacuna Bordetella (KC)',
     'Tos de las perreras, administración intranasal',
     'Unidad',16.00,8.50,10,3,30,TRUE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'VAC-006','Vacuna Leucemia Felina (FeLV)',
     'Protección contra el virus de la leucemia felina',
     'Unidad',20.00,10.50,10,3,30,TRUE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'VAC-007','Vacuna Triple Felina',
     'Herpesvirus, Calicivirus y Panleucopenia',
     'Unidad',15.00,7.50,12,3,35,TRUE,TRUE)
ON CONFLICT (codigo_sku) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════
-- ANTIPARASITARIOS (servicio "Desparasitación")
-- ═══════════════════════════════════════════════════════════════════
INSERT INTO producto (id_categoria, codigo_sku, nombre, descripcion, unidad_medida,
                      precio_venta, costo_adquisicion, stock_actual, stock_minimo,
                      stock_maximo, requiere_receta, activo)
VALUES
    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-007','Prazicuantel 50mg',
     'Antiparasitario interno, tenicida',
     'Unidad',1.20,0.50,60,15,150,TRUE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-008','Pirantel Pamoato 50mg',
     'Antiparasitario interno para nematodos',
     'Unidad',0.90,0.35,80,20,200,TRUE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-009','Febendazol 100mg',
     'Antiparasitario interno de amplio espectro',
     'Unidad',1.10,0.45,70,20,180,TRUE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-010','Fipronil Spray 100ml',
     'Antiparasitario externo, pulgas y garrapatas',
     'Unidad',14.00,6.50,12,4,30,TRUE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-011','Selamectina Pipeta',
     'Antiparasitario externo tópico mensual',
     'Unidad',16.00,7.00,15,4,35,TRUE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-012','Imidacloprid + Moxidectina (Advocate)',
     'Antiparasitario externo e interno combinado',
     'Unidad',18.00,8.50,10,3,25,TRUE,TRUE)
ON CONFLICT (codigo_sku) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════
-- ANTIBIÓTICOS
-- ═══════════════════════════════════════════════════════════════════
INSERT INTO producto (id_categoria, codigo_sku, nombre, descripcion, unidad_medida,
                      precio_venta, costo_adquisicion, stock_actual, stock_minimo,
                      stock_maximo, requiere_receta, activo)
VALUES
    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-013','Enrofloxacina 50mg',
     'Antibiótico de amplio espectro (quinolona)',
     'Unidad',1.50,0.60,50,15,120,TRUE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-014','Cefalexina 250mg',
     'Antibiótico cefalosporínico para infecciones de piel',
     'Unidad',2.00,0.85,40,12,100,TRUE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-015','Doxiciclina 100mg',
     'Antibiótico para Ehrlichia, Mycoplasma, Bordetella',
     'Unidad',1.80,0.75,30,10,80,TRUE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-016','Trimetoprim + Sulfadiazina 200/40mg',
     'Antibiótico combinado para infecciones urinarias',
     'Unidad',1.30,0.55,35,10,90,TRUE,TRUE)
ON CONFLICT (codigo_sku) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════
-- ANTIINFLAMATORIOS Y ANALGÉSICOS
-- ═══════════════════════════════════════════════════════════════════
INSERT INTO producto (id_categoria, codigo_sku, nombre, descripcion, unidad_medida,
                      precio_venta, costo_adquisicion, stock_actual, stock_minimo,
                      stock_maximo, requiere_receta, activo)
VALUES
    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-017','Carprofeno 25mg',
     'AINE para dolor post-quirúrgico y osteoartritis',
     'Unidad',2.20,0.90,40,12,100,TRUE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-018','Ketoprofeno 20mg',
     'AINE de acción rápida para inflamación aguda',
     'Unidad',2.00,0.85,30,10,80,TRUE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-019','Dexametasona 2mg/ml',
     'Corticoide inyectable para cuadros inflamatorios severos',
     'ml',0.80,0.30,50,15,120,TRUE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-020','Dipirona 500mg/ml',
     'Analgésico y antipirético inyectable',
     'ml',0.70,0.28,40,12,100,TRUE,TRUE)
ON CONFLICT (codigo_sku) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════
-- SUEROS Y SUPLEMENTOS
-- ═══════════════════════════════════════════════════════════════════
INSERT INTO producto (id_categoria, codigo_sku, nombre, descripcion, unidad_medida,
                      precio_venta, costo_adquisicion, stock_actual, stock_minimo,
                      stock_maximo, requiere_receta, activo)
VALUES
    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-021','Suero Fisiológico 500ml',
     'Solución NaCl 0.9% estéril para fluidoterapia',
     'Unidad',5.00,2.20,20,6,50,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-022','Suero Glucosado 5% 500ml',
     'Solución dextrosa 5% para reposición',
     'Unidad',5.50,2.40,15,5,40,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-023','Complejo Vitamínico B Inyectable',
     'Vitaminas B1, B6, B12 para convalecencia',
     'ml',1.80,0.70,30,10,80,TRUE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Medicamento'),
     'MED-024','Omega-3 Cápsulas',
     'Suplemento para salud dérmica y articular',
     'Unidad',0.60,0.22,60,20,150,FALSE,TRUE)
ON CONFLICT (codigo_sku) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════
-- INSUMOS CLÍNICOS (cirugía, consulta, procedimientos)
-- ═══════════════════════════════════════════════════════════════════
INSERT INTO producto (id_categoria, codigo_sku, nombre, descripcion, unidad_medida,
                      precio_venta, costo_adquisicion, stock_actual, stock_minimo,
                      stock_maximo, requiere_receta, activo)
VALUES
    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-005','Sutura Nylon 2-0',
     'Hilo de sutura no absorbible, aguja curva',
     'Unidad',4.50,1.80,25,8,60,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-006','Sutura Catgut 3-0',
     'Hilo de sutura absorbible para planos internos',
     'Unidad',5.00,2.00,20,6,50,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-007','Hoja de Bisturí #10',
     'Hoja estéril para mango #3 y #4',
     'Unidad',0.80,0.25,50,15,120,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-008','Mango de Bisturí #4',
     'Mango metálico reutilizable para hoja de bisturí',
     'Unidad',6.00,2.50,5,2,15,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-009','Paño Quirúrgico Estéril',
     'Campo estéril para cirugías',
     'Unidad',3.00,1.10,30,10,80,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-010','Sonda Endotraqueal 4.0mm',
     'Sonda para anestesia en pacientes pequeños',
     'Unidad',5.50,2.20,10,3,25,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-011','Sonda Endotraqueal 6.0mm',
     'Sonda para anestesia en pacientes medianos',
     'Unidad',6.00,2.40,10,3,25,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-012','Sonda Endotraqueal 8.0mm',
     'Sonda para anestesia en pacientes grandes',
     'Unidad',6.50,2.60,8,3,20,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-013','Jeringas 1ml',
     'Jeringas de insulina, con aguja fina',
     'Unidad',0.40,0.12,80,25,200,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-014','Jeringas 3ml',
     'Jeringas con aguja para administración IM',
     'Unidad',0.35,0.11,100,30,250,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-015','Jeringas 10ml',
     'Jeringas para lavado y administración IV',
     'Unidad',0.45,0.15,70,20,180,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-016','Algodón Hidrofílico 100g',
     'Rollo de algodón para limpieza y aplicación',
     'Unidad',2.50,1.00,25,8,60,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-017','Povidona Yodada 100ml',
     'Solución antiséptica para preparación quirúrgica',
     'Unidad',3.00,1.20,20,6,50,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-018','Clorhexidina 2% 100ml',
     'Solución antiséptica de amplio espectro',
     'Unidad',3.20,1.30,20,6,50,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-019','Cinta Adhesiva Médica',
     'Rollo de cinta para fijación de apósitos y sondas',
     'Unidad',1.80,0.70,30,10,80,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-020','Guantes Nitrilo T/S',
     'Guantes de examen, talla pequeña',
     'Unidad',0.50,0.20,50,20,150,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-021','Guantes Nitrilo T/L',
     'Guantes de examen, talla grande',
     'Unidad',0.50,0.20,50,20,150,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-022','Torundas de Algodón',
     'Paquete de torundas para limpieza',
     'Unidad',1.20,0.45,40,12,100,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'INS-023','Baja Lenguas',
     'Paletas de madera para examen oral',
     'Unidad',0.10,0.03,100,30,250,FALSE,TRUE)
ON CONFLICT (codigo_sku) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════
-- ESTÉTICA (coherentes con "Baño y Corte")
-- ═══════════════════════════════════════════════════════════════════
INSERT INTO producto (id_categoria, codigo_sku, nombre, descripcion, unidad_medida,
                      precio_venta, costo_adquisicion, stock_actual, stock_minimo,
                      stock_maximo, requiere_receta, activo)
VALUES
    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'ACC-002','Shampoo Medicado Antipulgas',
     'Shampoo con permetrina para tratamiento de ectoparásitos',
     'Unidad',8.00,3.20,15,5,40,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'ACC-003','Shampoo Hipoalergénico',
     'Shampoo suave para pieles sensibles',
     'Unidad',7.50,3.00,12,4,30,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'ACC-004','Shampoo Antifúngico',
     'Shampoo con ketoconazol para dermatofitosis',
     'Unidad',9.00,3.80,10,3,25,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'ACC-005','Cortaúñas para Mascotas',
     'Cortaúñas profesional con seguro',
     'Unidad',6.50,2.50,8,3,20,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Accesorio'),
     'ACC-006','Peine Metálico Deslanador',
     'Peine para desenredo y eliminación de pelo muerto',
     'Unidad',5.50,2.20,10,3,25,FALSE,TRUE)
ON CONFLICT (codigo_sku) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════
-- ALIMENTOS TERAPÉUTICOS
-- ═══════════════════════════════════════════════════════════════════
INSERT INTO producto (id_categoria, codigo_sku, nombre, descripcion, unidad_medida,
                      precio_venta, costo_adquisicion, stock_actual, stock_minimo,
                      stock_maximo, requiere_receta, activo)
VALUES
    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Alimento'),
     'ALI-003','Hill''s Prescription Diet Renal 2kg',
     'Dieta terapéutica para enfermedad renal crónica',
     'caja',32.00,18.00,6,2,15,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Alimento'),
     'ALI-004','Hill''s Prescription Diet GI 2kg',
     'Dieta para trastornos gastrointestinales',
     'caja',30.00,17.00,6,2,15,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Alimento'),
     'ALI-005','Royal Canin Gastrointestinal 2kg',
     'Dieta para digestión sensible',
     'caja',28.00,16.00,5,2,12,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Alimento'),
     'ALI-006','Alimento Húmedo Urinary',
     'Dieta húmeda para salud urinaria felina',
     'Unidad',3.50,1.50,24,8,60,FALSE,TRUE),

    ((SELECT id_categoria FROM categoria_producto WHERE nombre='Alimento'),
     'ALI-007','Hill''s Sensitive Skin 7.5kg',
     'Dieta para piel sensible y alergias alimentarias',
     'caja',58.00,32.00,4,1,10,FALSE,TRUE)
ON CONFLICT (codigo_sku) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════
-- Reporte final
-- ═══════════════════════════════════════════════════════════════════
SELECT
    cp.nombre AS categoria,
    COUNT(*) AS total_productos,
    SUM(p.stock_actual) AS unidades_totales,
    COUNT(*) FILTER (WHERE p.stock_actual <= p.stock_minimo) AS con_alerta
FROM producto p
JOIN categoria_producto cp ON cp.id_categoria = p.id_categoria
WHERE p.activo = TRUE
GROUP BY cp.nombre
ORDER BY cp.nombre;