package com.udo.can_cat.atenciones.domain.repository;

import com.udo.can_cat.atenciones.domain.entity.AtencionClinica;
import java.util.List;
import java.util.Optional;

public interface AtencionClinicaRepository {

    AtencionClinica guardar(AtencionClinica atencion);
    Optional<AtencionClinica> buscarPorId(AtencionClinica.AtencionId id);
    Optional<AtencionClinica> buscarPorCitaId(Integer idCita);
    boolean existePorCitaId(Integer idCita);
    List<AtencionClinica> buscarPorCitaIds(List<Integer> idsCita);
    List<AtencionClinica> buscarPorMascotaId(Integer idMascota);
}