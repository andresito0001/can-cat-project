package com.udo.can_cat.facturacion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetalleFacturaJpaRepository extends JpaRepository<DetalleFacturaJpaEntity, Integer> {
    List<DetalleFacturaJpaEntity> findByIdFactura(Integer idFactura);
}