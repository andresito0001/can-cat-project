package com.udo.can_cat.atenciones.infrastructure.persistence;

import com.udo.can_cat.atenciones.domain.entity.AtencionInsumo;
import com.udo.can_cat.atenciones.domain.repository.AtencionInsumoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AtencionInsumoRepositoryImpl implements AtencionInsumoRepository {

    private final AtencionInsumoJpaRepository jpa;

    public AtencionInsumoRepositoryImpl(AtencionInsumoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<AtencionInsumo> guardarTodos(List<AtencionInsumo> insumos) {
        if (insumos == null || insumos.isEmpty()) return List.of();
        List<AtencionInsumoJpaEntity> entidades = insumos.stream().map(AtencionInsumoRepositoryImpl::aJpa).toList();
        return jpa.saveAll(entidades).stream().map(AtencionInsumoRepositoryImpl::aDominio).toList();
    }

    @Override
    public List<AtencionInsumo> buscarPorAtencionIds(List<Integer> idsAtencion) {
        if (idsAtencion == null || idsAtencion.isEmpty()) return List.of();
        return jpa.findByIdAtencionIn(idsAtencion).stream().map(AtencionInsumoRepositoryImpl::aDominio).toList();
    }

    static AtencionInsumoJpaEntity aJpa(AtencionInsumo i) {
        AtencionInsumoJpaEntity e = new AtencionInsumoJpaEntity();
        e.setId(i.getId() != null ? i.getId().value() : null);
        e.setIdAtencion(i.getIdAtencion());
        e.setIdProducto(i.getIdProducto());
        e.setCantidad(i.getCantidad());
        e.setPrecioUnitarioUsd(i.getPrecioUnitarioUsd());
        e.setCreatedAt(LocalDateTime.now());
        return e;
    }

    static AtencionInsumo aDominio(AtencionInsumoJpaEntity e) {
        AtencionInsumo i = new AtencionInsumo();
        i.setId(e.getId() != null ? new AtencionInsumo.AtencionInsumoId(e.getId()) : null);
        i.setIdAtencion(e.getIdAtencion());
        i.setIdProducto(e.getIdProducto());
        i.setCantidad(e.getCantidad());
        i.setPrecioUnitarioUsd(e.getPrecioUnitarioUsd());
        i.setCreatedAt(e.getCreatedAt());
        return i;
    }
}