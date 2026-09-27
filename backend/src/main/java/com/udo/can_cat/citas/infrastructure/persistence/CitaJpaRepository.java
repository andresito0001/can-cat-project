package com.udo.can_cat.citas.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CitaJpaRepository extends JpaRepository<CitaJpaEntity, Integer> {

    // ────────────────────────────────────────────────────────────
    // Citas activas de un vet en una fecha (excluye expiradas)
    // ────────────────────────────────────────────────────────────
    @Query("SELECT c FROM CitaJpaEntity c " +
           "WHERE c.idVeterinario = :vetId " +
           "AND c.fechaCita = :fecha " +
           "AND c.idEstado IN :estados " +
           "AND (c.expiraEn IS NULL OR c.expiraEn > :ahora)")
    List<CitaJpaEntity> findByVetFechaAndEstadosActivos(
            @Param("vetId") Integer idVeterinario,
            @Param("fecha") LocalDate fecha,
            @Param("estados") List<Integer> estados,
            @Param("ahora") LocalDateTime ahora);

    // ────────────────────────────────────────────────────────────
    // Solapamiento (excluye expiradas)
    // ────────────────────────────────────────────────────────────
    @Query("SELECT COUNT(c) > 0 FROM CitaJpaEntity c " +
           "WHERE c.idVeterinario = :vetId " +
           "AND c.fechaCita = :fecha " +
           "AND c.idEstado IN :estados " +
           "AND c.horaInicio < :horaFin " +
           "AND c.horaFin > :horaInicio " +
           "AND (c.expiraEn IS NULL OR c.expiraEn > :ahora)")
    boolean existeSolapamiento(
            @Param("vetId") Integer idVeterinario,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("estados") List<Integer> estados,
            @Param("ahora") LocalDateTime ahora);

    // ────────────────────────────────────────────────────────────
    // Citas por mascotas
    // ────────────────────────────────────────────────────────────
    @Query("SELECT c FROM CitaJpaEntity c WHERE c.idMascota IN :ids")
    List<CitaJpaEntity> findByIdsMascotas(@Param("ids") List<Integer> ids);

    // ────────────────────────────────────────────────────────────
    // Citas + nombre de mascota por cliente
    // ────────────────────────────────────────────────────────────
    @Query("SELECT c, m.nombre FROM CitaJpaEntity c " +
        "JOIN MascotaJpaEntity m ON m.id = c.idMascota " +
        "WHERE m.idCliente = :clienteId " +
        "ORDER BY c.fechaCita DESC, c.horaInicio DESC")
    List<Object[]> findCitasWithMascotaNombreByClienteId(@Param("clienteId") Integer clienteId);

    // ────────────────────────────────────────────────────────────
    // Citas activas en rango de fechas
    // ────────────────────────────────────────────────────────────
    @Query("SELECT c FROM CitaJpaEntity c " +
           "WHERE c.fechaCita BETWEEN :fechaInicio AND :fechaFin " +
           "AND c.idEstado IN :estados")
    List<CitaJpaEntity> buscarActivasPorRangoFechas(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("estados") List<Integer> estados);

    // ────────────────────────────────────────────────────────────
    // Citas por estado
    // ────────────────────────────────────────────────────────────
    @Query("SELECT c FROM CitaJpaEntity c WHERE c.idEstado = :idEstado")
    List<CitaJpaEntity> buscarPorIdEstado(@Param("idEstado") Integer idEstado);

    // ────────────────────────────────────────────────────────────
    // NUEVO: Pendiente_Pago con expiraEn ya cumplido
    // ────────────────────────────────────────────────────────────
    @Query("SELECT c FROM CitaJpaEntity c " +
           "WHERE c.expiraEn IS NOT NULL " +
           "AND c.expiraEn <= :ahora " +
           "AND c.idEstado = (SELECT e.id FROM EstadoCitaJpaEntity e " +
           "                  WHERE e.nombre = 'Pendiente_Pago')")
    List<CitaJpaEntity> buscarPendientesExpiradas(@Param("ahora") LocalDateTime ahora);

    @Modifying
    @Transactional
    @Query("UPDATE CitaJpaEntity c " +
        "SET c.idEstado = (SELECT e.id FROM EstadoCitaJpaEntity e WHERE e.nombre = 'Cancelada'), " +
        "    c.expiraEn = NULL, " +
        "    c.updatedAt = :ahora " +
        "WHERE c.idVeterinario = :vetId " +
        "AND c.fechaCita = :fecha " +
        "AND c.horaInicio = :horaInicio " +
        "AND c.expiraEn IS NOT NULL " +
        "AND c.expiraEn <= :ahora")
    int cancelarExpiradasEnSlot(@Param("vetId") Integer idVeterinario,
                                @Param("fecha") LocalDate fecha,
                                @Param("horaInicio") LocalTime horaInicio,
                                @Param("ahora") LocalDateTime ahora);

}