package com.udo.can_cat.almacen.infrastructure.persistence;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MovimientoInventarioJpaRepository extends JpaRepository<MovimientoInventarioJpaEntity, Integer> {
    @Query("SELECT m FROM MovimientoInventarioJpaEntity m ORDER BY m.fechaMovimiento DESC")
    List<MovimientoInventarioJpaEntity> findTop10ByOrderByFechaMovimientoDesc(Pageable pageable);
}