package com.udo.can_cat.facturacion.domain.repository;

import com.udo.can_cat.facturacion.domain.entity.DetalleFactura;
import java.util.List;

public interface DetalleFacturaRepository {
    List<DetalleFactura> buscarPorFacturaId(Integer idFactura);
    DetalleFactura guardar(DetalleFactura detalle);
}