package com.udo.can_cat.almacen.infrastructure.persistence;

import com.udo.can_cat.almacen.domain.entity.CategoriaProducto;
import com.udo.can_cat.almacen.domain.repository.CategoriaProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoriaProductoRepositoryImpl implements CategoriaProductoRepository {
    private final CategoriaProductoJpaRepository jpa;

    @Override
    public Optional<CategoriaProducto> buscarPorNombre(String nombre) {
        return jpa.findByNombreIgnoreCase(nombre).map(CategoriaProductoJpaEntity::toDomain);
    }
}