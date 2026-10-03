package com.udo.can_cat.facturacion.domain.repository;

import com.udo.can_cat.facturacion.domain.entity.Factura;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FacturaRepository {
    Optional<Factura> buscarPorId(Integer id);
    Optional<Factura> buscarPorCitaId(Integer idCita);
    Optional<Factura> buscarPorNumeroControl(String numeroControl);
    Factura guardar(Factura factura);
    long contarPorFecha(LocalDate fecha);
    List<Factura> buscarPorClienteId(Integer idCliente);
    long contarFacturasEmitidasSinPago();
    long contarFacturasUrgentes(LocalDateTime corteAntesDe);
}