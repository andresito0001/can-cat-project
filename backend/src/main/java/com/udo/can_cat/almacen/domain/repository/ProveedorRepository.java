package com.udo.can_cat.almacen.domain.repository;

import com.udo.can_cat.almacen.domain.entity.Proveedor;
import java.util.List;
import java.util.Optional;

public interface ProveedorRepository {
    Optional<Proveedor> findById(Proveedor.ProveedorId id);
    List<Proveedor> buscarActivos();
    List<Proveedor> findAll();
    Proveedor guardar(Proveedor proveedor);
    boolean existePorRif(String rif);
    boolean existePorRifExcluyendo(String rif, Integer idExcluir);
}