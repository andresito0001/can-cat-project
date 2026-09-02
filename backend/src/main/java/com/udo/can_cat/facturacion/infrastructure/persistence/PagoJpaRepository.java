package com.udo.can_cat.facturacion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PagoJpaRepository extends JpaRepository<PagoJpaEntity, Integer> {
    Optional<PagoJpaEntity> findByIdFactura(Integer idFactura);
}