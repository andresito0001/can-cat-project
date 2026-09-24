package com.udo.can_cat.atenciones.domain.repository;

import com.udo.can_cat.atenciones.domain.entity.Receta;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecetaRepository {

    Receta guardar(Receta receta);
    Optional<Receta> buscarPorId(Receta.RecetaId id);
    Optional<Receta> buscarPorAtencionId(Integer idAtencion);
    List<Receta> buscarPorAtencionIds(List<Integer> idsAtencion);
    long contarPorFecha(LocalDate fecha);
}