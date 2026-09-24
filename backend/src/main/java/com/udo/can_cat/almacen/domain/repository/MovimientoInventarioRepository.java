package com.udo.can_cat.almacen.domain.repository;

import java.util.List;
import com.udo.can_cat.almacen.domain.entity.MovimientoInventario;

public interface MovimientoInventarioRepository {

    MovimientoInventario guardar(MovimientoInventario movimiento);
    List<MovimientoInventario> buscarRecientes(int limite);
}