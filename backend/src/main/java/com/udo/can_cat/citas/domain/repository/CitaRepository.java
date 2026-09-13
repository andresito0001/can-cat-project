package com.udo.can_cat.citas.domain.repository;

import com.udo.can_cat.citas.domain.entity.Cita;
import com.udo.can_cat.citas.infrastructure.persistence.CitaJpaEntity;
import com.udo.can_cat.usuarios.domain.entity.Cliente.ClienteId;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository {
    
    Cita guardar(Cita cita);
    Optional<Cita> buscarPorId(Integer id);

    /**
     * Retorna las citas de un veterinario en una fecha que están en alguno
     * de los estados indicados (estados "activos" que ocupan espacio).
     */
    List<Cita> buscarActivasPorVeterinarioYFecha(Integer idVeterinario,
                                                  LocalDate fecha,
                                                  List<Integer> idsEstadosActivos);

    /**
     * Retorna todas las citas asociadas a las mascotas cuyos IDs se reciben.
     */
    List<Cita> buscarPorIdsMascotas(List<Integer> idsMascotas);

    /**
     * Verifica si existe alguna cita activa que se solape con el rango horario
     * indicado para el veterinario en la fecha dada.
     * Dos rangos [s1,e1) y [s2,e2) se solapan si s1 < e2 AND s2 < e1.
     */
    boolean existeSolapamiento(Integer idVeterinario,
                               LocalDate fecha,
                               LocalTime horaInicio,
                               LocalTime horaFin,
                               List<Integer> idsEstadosActivos);
    
    List<Object[]> findCitasWithMascotaNombreByClienteId(ClienteId clienteId);
    public Cita toDomain(CitaJpaEntity entity);
    
    List<Cita> buscarActivasPorRangoFechas(LocalDate fechaInicio,
                                           LocalDate fechaFin,
                                           List<Integer> idsEstadosActivos);

    List<Cita> buscarPorIdEstado(Integer idEstado);

}