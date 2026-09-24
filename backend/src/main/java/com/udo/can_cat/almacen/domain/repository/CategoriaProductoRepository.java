package com.udo.can_cat.almacen.domain.repository;

import com.udo.can_cat.almacen.domain.entity.CategoriaProducto;
import java.util.Optional;

public interface CategoriaProductoRepository {
    Optional<CategoriaProducto> buscarPorNombre(String nombre);
}