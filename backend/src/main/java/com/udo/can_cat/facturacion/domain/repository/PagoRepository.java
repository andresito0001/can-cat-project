package com.udo.can_cat.facturacion.domain.repository;

import com.udo.can_cat.facturacion.domain.entity.Pago;
import java.util.Optional;
import java.util.List;

public interface PagoRepository {
    Optional<Pago> buscarPorId(Integer id);
    Optional<Pago> buscarPorFacturaId(Integer idFactura);
    Pago guardar(Pago pago);
    List<Pago> buscarPorEstado(String estado);
    void actualizar(Pago pago);
    boolean existePagoConfirmadoParaCita(Integer idCita);
    Optional<Pago> buscarPorFacturaCita(Integer idCita);
    boolean existePagoActivoParaCita(Integer idCita);
}