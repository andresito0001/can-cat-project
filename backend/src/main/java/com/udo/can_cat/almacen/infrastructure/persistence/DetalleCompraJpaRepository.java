package com.udo.can_cat.almacen.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

// TODO(CU-4.6.1.12): por ahora sin métodos derivados (solo guardar).
public interface DetalleCompraJpaRepository extends JpaRepository<DetalleCompraJpaEntity, Integer> {
}
