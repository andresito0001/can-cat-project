package com.udo.can_cat.facturacion.domain.repository;

import com.udo.can_cat.facturacion.domain.entity.MetodoPago;
import java.util.List;
import java.util.Optional;

public interface MetodoPagoRepository {
    Optional<MetodoPago> buscarPorId(Integer id);
    List<MetodoPago> buscarActivos();
    MetodoPago guardar(MetodoPago metodoPago);
}