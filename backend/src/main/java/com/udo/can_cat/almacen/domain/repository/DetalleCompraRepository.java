package com.udo.can_cat.almacen.domain.repository;

import com.udo.can_cat.almacen.domain.entity.DetalleCompra;
import java.util.List;

public interface DetalleCompraRepository {
    DetalleCompra guardar(DetalleCompra detalle);
    List<DetalleCompra> buscarPorCompraId(Integer idCompra);
}