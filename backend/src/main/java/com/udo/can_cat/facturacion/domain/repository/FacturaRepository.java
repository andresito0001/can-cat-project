package com.udo.can_cat.facturacion.domain.repository;

import com.udo.can_cat.facturacion.domain.entity.Factura;
import java.time.LocalDate;
import java.util.Optional;

public interface FacturaRepository {
    Optional<Factura> buscarPorId(Integer id);
    Optional<Factura> buscarPorCitaId(Integer idCita);
    Optional<Factura> buscarPorNumeroControl(String numeroControl);
    Factura guardar(Factura factura);
    long contarPorFecha(LocalDate fecha);
}