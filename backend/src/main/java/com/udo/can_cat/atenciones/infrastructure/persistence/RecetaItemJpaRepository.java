package com.udo.can_cat.atenciones.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecetaItemJpaRepository extends JpaRepository<RecetaItemJpaEntity, Integer> {

    List<RecetaItemJpaEntity> findByIdRecetaOrderByOrdenAsc(Integer idReceta);
}