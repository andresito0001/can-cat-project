package com.udo.can_cat.citas.infrastructure.persistence;

import com.udo.can_cat.citas.domain.entity.Servicio;
import com.udo.can_cat.citas.domain.repository.ServicioRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class ServicioRepositoryImpl implements ServicioRepository {

    private final ServicioJpaRepository jpaRepository;

    public ServicioRepositoryImpl(ServicioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Servicio> buscarPorId(Integer id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Servicio> buscarActivos() {
        return jpaRepository.findByActivoTrueOrderByTipoAtencionAscNombreAsc()
                .stream()
                .map(this::toDomain)
                .toList();
    }
    
    @Override
    public List<Servicio> buscarPorIds(Set<Integer> ids) {
        return jpaRepository.buscarPorIds(ids)
            .stream()
            .map(this::toDomain) 
            .toList();
    }

    @Override
    public List<Servicio> buscarActivosPorTipoAtencion(String tipoAtencion) {
        return jpaRepository.findByActivoTrueAndTipoAtencionOrderByNombreAsc(tipoAtencion)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Servicio toDomain(ServicioJpaEntity e) {
        Servicio s = new Servicio();
        s.setId(e.getId());
        s.setNombre(e.getNombre());
        s.setTipoAtencion(e.getTipoAtencion());
        s.setDescripcion(e.getDescripcion());
        s.setDuracionMinutos(e.getDuracionMinutos());
        s.setPrecioUsd(e.getPrecioUsd());
        s.setActivo(e.getActivo());
        s.setCreatedAt(e.getCreatedAt());
        s.setUpdatedAt(e.getUpdatedAt());
        return s;
    }
}