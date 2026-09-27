package com.udo.can_cat.citas.application.port;

public interface PagoVerificacionPort {
    boolean existePagoConfirmadoParaCita(Integer idCita);
    boolean existePagoActivoParaCita(Integer idCita);
}