package com.udo.can_cat.facturacion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MetodoPagoJpaRepository extends JpaRepository<MetodoPagoJpaEntity, Integer> {
    List<MetodoPagoJpaEntity> findByActivoTrue();
}