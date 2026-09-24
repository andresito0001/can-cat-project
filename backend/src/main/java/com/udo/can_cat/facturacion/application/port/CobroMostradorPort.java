package com.udo.can_cat.facturacion.application.port;

import com.udo.can_cat.facturacion.application.dto.FacturaPendienteDTO;
import java.util.List;

public interface CobroMostradorPort {

    List<FacturaPendienteDTO> listarFacturasPendientes();
}