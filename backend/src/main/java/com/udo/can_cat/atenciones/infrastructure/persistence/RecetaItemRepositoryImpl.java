package com.udo.can_cat.atenciones.infrastructure.persistence;

import com.udo.can_cat.atenciones.domain.entity.RecetaItem;
import com.udo.can_cat.atenciones.domain.repository.RecetaItemRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class RecetaItemRepositoryImpl implements RecetaItemRepository {

    private final RecetaItemJpaRepository jpa;

    public RecetaItemRepositoryImpl(RecetaItemJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<RecetaItem> guardarTodos(List<RecetaItem> items) {
        if (items == null || items.isEmpty()) return List.of();
        List<RecetaItemJpaEntity> entidades = items.stream().map(RecetaItemRepositoryImpl::aJpa).toList();
        return jpa.saveAll(entidades).stream().map(RecetaItemRepositoryImpl::aDominio).toList();
    }

    @Override
    public List<RecetaItem> buscarPorRecetaId(Integer idReceta) {
        return jpa.findByIdRecetaOrderByOrdenAsc(idReceta)
                .stream().map(RecetaItemRepositoryImpl::aDominio).toList();
    }

    static RecetaItemJpaEntity aJpa(RecetaItem i) {
        RecetaItemJpaEntity e = new RecetaItemJpaEntity();
        e.setId(i.getId() != null ? i.getId().value() : null);
        e.setIdReceta(i.getIdReceta());
        e.setMedicamento(i.getMedicamento());
        e.setConcentracion(i.getConcentracion());
        e.setDosis(i.getDosis());
        e.setViaAdministracion(i.getViaAdministracion());
        e.setFrecuencia(i.getFrecuencia());
        e.setDuracion(i.getDuracion());
        e.setOrden(i.getOrden() != null ? i.getOrden() : 1);
        return e;
    }

    static RecetaItem aDominio(RecetaItemJpaEntity e) {
        RecetaItem i = new RecetaItem();
        i.setId(e.getId() != null ? new RecetaItem.RecetaItemId(e.getId()) : null);
        i.setIdReceta(e.getIdReceta());
        i.setMedicamento(e.getMedicamento());
        i.setConcentracion(e.getConcentracion());
        i.setDosis(e.getDosis());
        i.setViaAdministracion(e.getViaAdministracion());
        i.setFrecuencia(e.getFrecuencia());
        i.setDuracion(e.getDuracion());
        i.setOrden(e.getOrden());
        return i;
    }
}