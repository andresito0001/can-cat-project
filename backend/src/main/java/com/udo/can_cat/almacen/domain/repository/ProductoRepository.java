package com.udo.can_cat.almacen.domain.repository;

import com.udo.can_cat.almacen.domain.entity.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository {
    Producto guardar(Producto producto);

    boolean existePorSku(String codigoSku);
    
    List<Producto> buscarConStockBajo();

    Optional<Producto> findById(Producto.ProductoId id);

    List<Producto> buscarPorIds(List<Producto.ProductoId> ids);

    List<Producto> buscarActivos(String filtro);

    String nombreCategoria(Integer idCategoria);

    boolean descontarStock(Producto.ProductoId id, int cantidad);
}