-- V14__completar_costo_productos.sql
UPDATE producto
SET costo_adquisicion = ROUND(precio_venta * 0.5, 2)
WHERE costo_adquisicion IS NULL OR costo_adquisicion = 0;