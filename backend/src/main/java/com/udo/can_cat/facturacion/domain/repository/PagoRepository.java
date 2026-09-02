package com.udo.can_cat.facturacion.domain.repository;

import com.udo.can_cat.facturacion.domain.entity.Pago;
import java.util.Optional;

public interface PagoRepository {
    Optional<Pago> buscarPorId(Integer id);
    Optional<Pago> buscarPorFacturaId(Integer idFactura);
    Pago guardar(Pago pago);
}