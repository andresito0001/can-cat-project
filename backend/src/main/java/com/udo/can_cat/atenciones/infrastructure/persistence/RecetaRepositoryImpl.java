package com.udo.can_cat.atenciones.infrastructure.persistence;

import com.udo.can_cat.atenciones.domain.entity.Receta;
import com.udo.can_cat.atenciones.domain.repository.RecetaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class RecetaRepositoryImpl implements RecetaRepository {

    private final RecetaJpaRepository jpa;

    public RecetaRepositoryImpl(RecetaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Receta guardar(Receta receta) {
        RecetaJpaEntity guardada = jpa.saveAndFlush(aJpa(receta));
        return aDominio(guardada);
    }

    @Override
    public Optional<Receta> buscarPorId(Receta.RecetaId id) {
        return jpa.findById(id.value()).map(RecetaRepositoryImpl::aDominio);
    }

    @Override
    public Optional<Receta> buscarPorAtencionId(Integer idAtencion) {
        return jpa.findByIdAtencion(idAtencion).map(RecetaRepositoryImpl::aDominio);
    }

    @Override
    public List<Receta> buscarPorAtencionIds(List<Integer> idsAtencion) {
        if (idsAtencion == null || idsAtencion.isEmpty()) return List.of();
        return jpa.findByIdAtencionIn(idsAtencion).stream().map(RecetaRepositoryImpl::aDominio).toList();
    }

    @Override
    public long contarPorFecha(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay();
        return jpa.countByFechaEmisionBetween(inicio, fin);
    }

    static RecetaJpaEntity aJpa(Receta r) {
        RecetaJpaEntity e = new RecetaJpaEntity();
        e.setId(r.getId() != null ? r.getId().value() : null);
        e.setIdAtencion(r.getIdAtencion());
        e.setCodigoReceta(r.getCodigoReceta());
        e.setIndicacionesGenerales(r.getIndicacionesGenerales());
        e.setFechaEmision(r.getFechaEmision() != null ? r.getFechaEmision() : LocalDateTime.now());
        e.setCreatedAt(LocalDateTime.now());
        return e;
    }

    static Receta aDominio(RecetaJpaEntity e) {
        Receta r = new Receta();
        r.setId(e.getId() != null ? new Receta.RecetaId(e.getId()) : null);
        r.setIdAtencion(e.getIdAtencion());
        r.setCodigoReceta(e.getCodigoReceta());
        r.setIndicacionesGenerales(e.getIndicacionesGenerales());
        r.setFechaEmision(e.getFechaEmision());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}