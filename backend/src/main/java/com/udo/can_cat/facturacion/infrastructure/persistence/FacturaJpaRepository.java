package com.udo.can_cat.facturacion.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FacturaJpaRepository extends JpaRepository<FacturaJpaEntity, Integer> {

    Optional<FacturaJpaEntity> findByIdCita(Integer idCita);

    Optional<FacturaJpaEntity> findByNumeroControl(String numeroControl);

    @Query("SELECT COUNT(f) FROM FacturaJpaEntity f WHERE DATE(f.fechaEmision) = :fecha")
    long countByFechaEmision(@Param("fecha") LocalDate fecha);
    
    // List<FacturaJpaEntity> findByIdClienteOrderByIdFacturaDesc(Integer idCliente);
}