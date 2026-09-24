package com.udo.can_cat.almacen.domain.repository;

import com.udo.can_cat.almacen.domain.entity.CompraProveedor;
import java.time.LocalDate;
import java.util.Optional;

public interface CompraProveedorRepository {
    CompraProveedor guardar(CompraProveedor compra);
    Optional<CompraProveedor> findById(CompraProveedor.CompraId id);
    long contarPorFechaOrden(LocalDate fecha);
}