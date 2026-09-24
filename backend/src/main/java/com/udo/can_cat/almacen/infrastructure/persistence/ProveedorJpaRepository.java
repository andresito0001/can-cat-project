package com.udo.can_cat.almacen.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProveedorJpaRepository extends JpaRepository<ProveedorJpaEntity, Integer> {
    List<ProveedorJpaEntity> findByActivoTrue();
}