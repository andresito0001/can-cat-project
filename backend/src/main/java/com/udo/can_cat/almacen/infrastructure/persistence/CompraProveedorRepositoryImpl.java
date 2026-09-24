package com.udo.can_cat.almacen.infrastructure.persistence;

import com.udo.can_cat.almacen.domain.entity.CompraProveedor;
import com.udo.can_cat.almacen.domain.repository.CompraProveedorRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public class CompraProveedorRepositoryImpl implements CompraProveedorRepository {

    private final CompraProveedorJpaRepository jpa;

    public CompraProveedorRepositoryImpl(CompraProveedorJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public CompraProveedor guardar(CompraProveedor compra) {
        CompraProveedorJpaEntity entidad = aJpa(compra);
        CompraProveedorJpaEntity guardado = jpa.save(entidad);
        return aDominio(guardado);
    }

    @Override
    public Optional<CompraProveedor> findById(CompraProveedor.CompraId id) {
        return jpa.findById(id.value()).map(this::aDominio);
    }

    @Override
    public long contarPorFechaOrden(LocalDate fecha) {
        return jpa.countByFechaOrden(fecha);
    }

    private CompraProveedorJpaEntity aJpa(CompraProveedor compra) {
        CompraProveedorJpaEntity e = new CompraProveedorJpaEntity();
        e.setId(compra.getId() != null ? compra.getId().value() : null);
        e.setIdProveedor(compra.getIdProveedor());
        e.setIdPersonal(compra.getIdPersonal());
        e.setNumeroOrden(compra.getNumeroOrden());
        e.setFechaOrden(compra.getFechaOrden());
        e.setFechaRecepcion(compra.getFechaRecepcion());
        e.setEstadoCompra(compra.getEstadoCompra());
        e.setMontoTotal(compra.getMontoTotal());
        e.setObservacionesRecepcion(compra.getObservacionesRecepcion());
        e.setCreatedAt(compra.getCreatedAt());
        e.setUpdatedAt(compra.getUpdatedAt());
        return e;
    }

    private CompraProveedor aDominio(CompraProveedorJpaEntity e) {
        CompraProveedor compra = new CompraProveedor();
        compra.setId(e.getId() != null ? new CompraProveedor.CompraId(e.getId()) : null);
        compra.setIdProveedor(e.getIdProveedor());
        compra.setIdPersonal(e.getIdPersonal());
        compra.setNumeroOrden(e.getNumeroOrden());
        compra.setFechaOrden(e.getFechaOrden());
        compra.setFechaRecepcion(e.getFechaRecepcion());
        compra.setEstadoCompra(e.getEstadoCompra());
        compra.setMontoTotal(e.getMontoTotal());
        compra.setObservacionesRecepcion(e.getObservacionesRecepcion());
        compra.setCreatedAt(e.getCreatedAt());
        compra.setUpdatedAt(e.getUpdatedAt());
        return compra;
    }
}