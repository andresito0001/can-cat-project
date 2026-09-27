package com.udo.can_cat.citas.domain.repository;

import com.udo.can_cat.citas.domain.entity.Cita;
import com.udo.can_cat.citas.infrastructure.persistence.CitaJpaEntity;
import com.udo.can_cat.usuarios.domain.entity.Cliente.ClienteId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository {

    Cita guardar(Cita cita);
    Optional<Cita> buscarPorId(Integer id);

    List<Cita> buscarActivasPorVeterinarioYFecha(Integer idVeterinario,
                                                  LocalDate fecha,
                                                  List<Integer> idsEstadosActivos);

    List<Cita> buscarPorIdsMascotas(List<Integer> idsMascotas);

    boolean existeSolapamiento(Integer idVeterinario,
                               LocalDate fecha,
                               LocalTime horaInicio,
                               LocalTime horaFin,
                               List<Integer> idsEstadosActivos);

    List<Object[]> findCitasWithMascotaNombreByClienteId(ClienteId clienteId);

    Cita toDomain(CitaJpaEntity entity);

    List<Cita> buscarActivasPorRangoFechas(LocalDate fechaInicio,
                                           LocalDate fechaFin,
                                           List<Integer> idsEstadosActivos);

    List<Cita> buscarPorIdEstado(Integer idEstado);

    /** Citas Pendiente_Pago cuya hora de expiración ya pasó. */
    List<Cita> buscarPendientesExpiradas(LocalDateTime ahora);

    /**
     * Cancela (soft) las citas Pendiente_Pago expiradas que ocupan exactamente
     * este slot (vet, fecha, hora_inicio). Se invoca antes de crear una cita
     * nueva para evitar choques con la constraint de unicidad.
     */
    int cancelarExpiradasEnSlot(Integer idVeterinario,
                                java.time.LocalDate fecha,
                                java.time.LocalTime horaInicio,
                                java.time.LocalDateTime ahora);
}