package com.udo.can_cat.atenciones.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CitaPort {

    Optional<CitaInfo> buscarPorId(Integer idCita);

    /** Agenda del veterinario: citas Confirmada/En_Atencion de la fecha (D7). */
    List<CitaAgendaInfo> buscarAgendaVeterinario(Integer idVeterinario, LocalDate fecha);

    /** Cambia el estado de la cita por NOMBRE de estado. La validación de la
     *  transición la hace el ApplicationService (D2), este método solo ejecuta. */
    void actualizarEstado(Integer idCita, String estadoNombre);

    record CitaInfo(Integer idCita, Integer idMascota, Integer idVeterinario, Integer idServicio,
                    LocalDate fechaCita, LocalTime horaInicio, LocalTime horaFin,
                    String motivoConsulta, String tipoAtencion, BigDecimal costos,
                    String estadoNombre, String servicioNombre) {}

    record CitaAgendaInfo(Integer idCita, LocalDate fechaCita, LocalTime horaInicio, LocalTime horaFin,
                          String motivoConsulta, String servicioNombre,
                          String estadoNombre, String estadoColor,
                          Integer idMascota, String mascotaNombre, String especie, String raza,
                          Integer idCliente, String clienteNombre, String clienteDocumento,
                          String clienteTelefono) {}
}