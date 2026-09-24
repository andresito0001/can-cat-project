package com.udo.can_cat.almacen.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface ProductoJpaRepository extends JpaRepository<ProductoJpaEntity, Integer> {

    List<ProductoJpaEntity> findByActivoTrueOrderByNombreAsc();

    @Query("""
            SELECT p FROM ProductoJpaEntity p
            WHERE p.activo = true
              AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filtro, '%'))
                   OR LOWER(p.codigoSku) LIKE LOWER(CONCAT('%', :filtro, '%')))
            ORDER BY p.nombre ASC
            """)
    List<ProductoJpaEntity> buscarActivosPorFiltro(@Param("filtro") String filtro);

    @Query(value = "SELECT nombre FROM categoria_producto WHERE id_categoria = :idCategoria", nativeQuery = true)
    String nombreCategoriaPorId(@Param("idCategoria") Integer idCategoria);

    @Modifying
    @Transactional
    @Query("""
            UPDATE ProductoJpaEntity p
            SET p.stockActual = p.stockActual - :cantidad
            WHERE p.id = :idProducto
              AND p.stockActual >= :cantidad
              AND p.activo = true
            """)
    int descontarStock(@Param("idProducto") Integer idProducto,
                       @Param("cantidad") Integer cantidad);

    boolean existsByCodigoSku(String codigoSku);

    @Query("""
            SELECT p FROM ProductoJpaEntity p
            WHERE p.activo = true AND p.stockActual <= p.stockMinimo
            ORDER BY p.nombre ASC
            """)
    List<ProductoJpaEntity> encontrarConStockBajo();
}