package com.udo.can_cat.almacen.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoriaProductoJpaRepository extends JpaRepository<CategoriaProductoJpaEntity, Integer> {
    Optional<CategoriaProductoJpaEntity> findByNombreIgnoreCase(String nombre);
}