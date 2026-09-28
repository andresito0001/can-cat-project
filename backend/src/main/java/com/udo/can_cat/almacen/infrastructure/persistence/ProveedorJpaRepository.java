package com.udo.can_cat.almacen.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProveedorJpaRepository extends JpaRepository<ProveedorJpaEntity, Integer> {
    List<ProveedorJpaEntity> findByActivoTrue();
    Optional<ProveedorJpaEntity> findByRif(String rif);
    boolean existsByRif(String rif);
    boolean existsByRifAndIdNot(String rif, Integer id);
}