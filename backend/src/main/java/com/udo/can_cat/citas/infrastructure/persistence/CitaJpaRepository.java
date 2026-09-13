package com.udo.can_cat.citas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaJpaRepository extends JpaRepository<CitaJpaEntity, Integer> {

    @Query("""
            SELECT c FROM CitaJpaEntity c
            WHERE c.idVeterinario = :idVet
              AND c.fechaCita = :fecha
              AND c.idEstado IN :estadosActivos
            ORDER BY c.horaInicio ASC
            """)
    List<CitaJpaEntity> findByVetFechaAndEstadosActivos(
            @Param("idVet") Integer idVet,
            @Param("fecha") LocalDate fecha,
            @Param("estadosActivos") List<Integer> estadosActivos);

    @Query("""
            SELECT c FROM CitaJpaEntity c
            WHERE c.idMascota IN :idsMascotas
            ORDER BY c.fechaCita DESC, c.horaInicio DESC
            """)
    List<CitaJpaEntity> findByIdsMascotas(@Param("idsMascotas") List<Integer> idsMascotas);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM CitaJpaEntity c
            WHERE c.idVeterinario = :idVet
              AND c.fechaCita = :fecha
              AND c.idEstado IN :estadosActivos
              AND c.horaInicio < :horaFin
              AND c.horaFin > :horaInicio
            """)
    boolean existeSolapamiento(
        @Param("idVet") Integer idVet,
        @Param("fecha") LocalDate fecha,
        @Param("horaInicio") LocalTime horaInicio,
        @Param("horaFin") LocalTime horaFin,
        @Param("estadosActivos") List<Integer> estadosActivos);


    @Query("SELECT c, m.nombre FROM CitaJpaEntity c, MascotaJpaEntity m " +
        "WHERE c.idMascota = m.id AND m.idCliente = :clienteId")
    List<Object[]> findCitasWithMascotaNombreByClienteId(@Param("clienteId") Integer clienteId);

    @Query("SELECT c FROM CitaJpaEntity c " +
        "WHERE c.fechaCita >= :inicio AND c.fechaCita <= :fin " +
        "AND c.idEstado IN :estados ORDER BY c.fechaCita ASC, c.horaInicio ASC")
        java.util.List<CitaJpaEntity> buscarActivasPorRangoFechas(
    @Param("inicio") LocalDate inicio,
    @Param("fin") LocalDate fin,
    @Param("estados") List<Integer> estados);
    @org.springframework.data.jpa.repository.Query("SELECT c FROM CitaJpaEntity c " +
        "WHERE c.idEstado = :estado ORDER BY c.fechaCita ASC, c.horaInicio ASC")
    List<CitaJpaEntity> buscarPorIdEstado (
                @org.springframework.data.repository.query.Param("estado") Integer estado);
}