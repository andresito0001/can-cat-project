package com.udo.can_cat.facturacion.infrastructure.persistence;

import com.udo.can_cat.citas.application.port.PagoVerificacionPort;
import com.udo.can_cat.facturacion.domain.repository.PagoRepository;
import org.springframework.stereotype.Component;

@Component
public class PagoVerificacionAdapter implements PagoVerificacionPort {

    private final PagoRepository pagoRepo;

    public PagoVerificacionAdapter(PagoRepository pagoRepo) {
        this.pagoRepo = pagoRepo;
    }

    @Override
    public boolean existePagoConfirmadoParaCita(Integer idCita) {
        return pagoRepo.existePagoConfirmadoParaCita(idCita);
    }
    
    @Override
    public boolean existePagoActivoParaCita(Integer idCita) {
        return pagoRepo.existePagoActivoParaCita(idCita);
    }
}