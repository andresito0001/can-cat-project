package com.udo.can_cat.citas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

@Repository
public interface ServicioJpaRepository extends JpaRepository<ServicioJpaEntity, Integer> {
    List<ServicioJpaEntity> findByActivoTrueOrderByTipoAtencionAscNombreAsc();
    List<ServicioJpaEntity> findByActivoTrueAndTipoAtencionOrderByNombreAsc(String tipoAtencion);
    @Query("SELECT s FROM ServicioJpaEntity s WHERE s.id IN :ids")
    List<ServicioJpaEntity> buscarPorIds(@Param("ids") Set<Integer> ids);
}