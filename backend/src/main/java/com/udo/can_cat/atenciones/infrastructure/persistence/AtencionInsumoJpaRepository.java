package com.udo.can_cat.atenciones.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AtencionInsumoJpaRepository extends JpaRepository<AtencionInsumoJpaEntity, Integer> {

    List<AtencionInsumoJpaEntity> findByIdAtencionIn(List<Integer> idsAtencion);
}