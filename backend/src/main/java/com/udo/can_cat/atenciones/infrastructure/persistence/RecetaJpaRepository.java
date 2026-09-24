package com.udo.can_cat.atenciones.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecetaJpaRepository extends JpaRepository<RecetaJpaEntity, Integer> {

    Optional<RecetaJpaEntity> findByIdAtencion(Integer idAtencion);
    List<RecetaJpaEntity> findByIdAtencionIn(List<Integer> idsAtencion);
    long countByFechaEmisionBetween(LocalDateTime inicio, LocalDateTime fin);
}