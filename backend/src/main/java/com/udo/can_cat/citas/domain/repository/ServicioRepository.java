package com.udo.can_cat.citas.domain.repository;

import com.udo.can_cat.citas.domain.entity.Servicio;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ServicioRepository {
    Optional<Servicio> buscarPorId(Integer id);
    List<Servicio> buscarActivos();
    List<Servicio> buscarPorIds(Set<Integer> ids);
    List<Servicio> buscarActivosPorTipoAtencion(String tipoAtencion);
}