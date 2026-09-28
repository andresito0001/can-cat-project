package com.udo.can_cat.almacen.infrastructure.persistence;

import com.udo.can_cat.almacen.domain.entity.Proveedor;
import com.udo.can_cat.almacen.domain.repository.ProveedorRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProveedorRepositoryImpl implements ProveedorRepository {

    private final ProveedorJpaRepository jpa;

    public ProveedorRepositoryImpl(ProveedorJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Proveedor> findById(Proveedor.ProveedorId id) {
        return jpa.findById(id.value()).map(ProveedorJpaEntity::toDomain);
    }

    @Override
    public List<Proveedor> buscarActivos() {
        return jpa.findByActivoTrue().stream()
                .map(ProveedorJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Proveedor> findAll() {
        return jpa.findAll().stream()
                .map(ProveedorJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Proveedor guardar(Proveedor proveedor) {
        ProveedorJpaEntity entity = ProveedorJpaEntity.fromDomain(proveedor);
        ProveedorJpaEntity saved = jpa.save(entity);
        return saved.toDomain();
    }

    @Override
    public boolean existePorRif(String rif) {
        return jpa.existsByRif(rif);
    }

    @Override
    public boolean existePorRifExcluyendo(String rif, Integer idExcluir) {
        return jpa.existsByRifAndIdNot(rif, idExcluir);
    }
}