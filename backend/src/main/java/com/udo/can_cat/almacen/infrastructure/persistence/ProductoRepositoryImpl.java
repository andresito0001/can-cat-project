package com.udo.can_cat.almacen.infrastructure.persistence;

import com.udo.can_cat.almacen.domain.entity.Producto;
import com.udo.can_cat.almacen.domain.repository.ProductoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductoRepositoryImpl implements ProductoRepository {

    private final ProductoJpaRepository jpa;

    public ProductoRepositoryImpl(ProductoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean existePorSku(String codigoSku) {
        return jpa.existsByCodigoSku(codigoSku);
    }

    @Override
    public List<Producto> buscarConStockBajo() {
        return jpa.encontrarConStockBajo().stream().map(ProductoRepositoryImpl::aDominio).toList();
    }

    @Override
    public Optional<Producto> findById(Producto.ProductoId id) {
        return jpa.findById(id.value()).map(ProductoRepositoryImpl::aDominio);
    }

    @Override
    public List<Producto> buscarPorIds(List<Producto.ProductoId> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        List<Integer> valores = ids.stream().map(Producto.ProductoId::value).toList();
        return jpa.findAllById(valores).stream().map(ProductoRepositoryImpl::aDominio).toList();
    }

    @Override
    public List<Producto> buscarActivos(String filtro) {
        List<ProductoJpaEntity> entidades;
        if (filtro == null || filtro.isBlank()) {
            entidades = jpa.findByActivoTrueOrderByNombreAsc();
        } else {
            entidades = jpa.buscarActivosPorFiltro(filtro.trim());
        }
        return entidades.stream().map(ProductoRepositoryImpl::aDominio).toList();
    }

    @Override
    public String nombreCategoria(Integer idCategoria) {
        if (idCategoria == null) return null;
        return jpa.nombreCategoriaPorId(idCategoria);
    }

    @Override
    public boolean descontarStock(Producto.ProductoId id, int cantidad) {
        return jpa.descontarStock(id.value(), cantidad) > 0;
    }

    static Producto aDominio(ProductoJpaEntity e) {
        Producto p = new Producto();
        p.setId(e.getId() != null ? new Producto.ProductoId(e.getId()) : null);
        p.setIdCategoria(e.getIdCategoria());
        p.setIdProveedorPredeterminado(e.getIdProveedorPredeterminado());
        p.setCodigoSku(e.getCodigoSku());
        p.setNombre(e.getNombre());
        p.setDescripcion(e.getDescripcion());
        p.setUnidadMedida(e.getUnidadMedida());
        p.setPrecioVenta(e.getPrecioVenta());
        p.setCostoAdquisicion(e.getCostoAdquisicion());
        p.setStockActual(e.getStockActual());
        p.setStockMinimo(e.getStockMinimo());
        p.setStockMaximo(e.getStockMaximo());
        p.setRequiereReceta(e.getRequiereReceta());
        p.setActivo(e.getActivo());
        p.setCreatedAt(e.getCreatedAt());
        p.setUpdatedAt(e.getUpdatedAt());
        return p;
    }


    @Override
    public Producto guardar(Producto producto) {
        ProductoJpaEntity e = new ProductoJpaEntity();
        e.setId(producto.getId() != null ? producto.getId().value() : null);
        e.setIdCategoria(producto.getIdCategoria());
        e.setIdProveedorPredeterminado(producto.getIdProveedorPredeterminado());
        e.setCodigoSku(producto.getCodigoSku());
        e.setNombre(producto.getNombre());
        e.setDescripcion(producto.getDescripcion());
        e.setUnidadMedida(producto.getUnidadMedida());
        e.setPrecioVenta(producto.getPrecioVenta());
        e.setCostoAdquisicion(producto.getCostoAdquisicion());
        e.setStockActual(producto.getStockActual());
        e.setStockMinimo(producto.getStockMinimo());
        e.setStockMaximo(producto.getStockMaximo());
        e.setRequiereReceta(producto.getRequiereReceta());
        e.setActivo(producto.getActivo());
        e.setCreatedAt(producto.getCreatedAt() != null ? producto.getCreatedAt() : java.time.LocalDateTime.now());
        e.setUpdatedAt(java.time.LocalDateTime.now());
        
        return aDominio(jpa.save(e));
    }

    @Override
    public void cambiarEstado(Producto.ProductoId id, boolean activo) {
        jpa.cambiarEstadoProducto(id.value(), activo);
    }

    @Override
    public long contarPorPrefijo(String prefijo) {
        if (prefijo == null || prefijo.isBlank()) return 0L;
        return jpa.countByCodigoSkuStartingWith(prefijo);
    }

    @Override
    public List<Producto> buscarTodos(String filtro) {
        List<ProductoJpaEntity> entidades;
        if (filtro == null || filtro.isBlank()) {
            entidades = jpa.findAllByOrderByNombreAsc();
        } else {
            entidades = jpa.buscarTodosPorFiltro(filtro.trim());
        }
        return entidades.stream().map(ProductoRepositoryImpl::aDominio).toList();
    }
}