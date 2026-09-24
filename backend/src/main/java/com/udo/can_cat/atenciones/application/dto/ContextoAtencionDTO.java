package com.udo.can_cat.atenciones.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
    
/**
 * F2: datos que carga el wizard. Si atencionGuardada != null → la vista entra
 * en modo SOLO LECTURA. ultimaAtencion = atención previa del paciente
 * (contexto clínico), excluyendo la de esta cita.
 */
public record ContextoAtencionDTO(
        CitaDTO cita,
        MascotaDTO mascota,
        DuenoDTO dueno,
        AtencionHistorialDTO atencionGuardada,
        AtencionResumenDTO ultimaAtencion,
        boolean primeraVez) {

    public record CitaDTO(Integer idCita, String motivoConsulta, String servicio, String tipoAtencion,
                         LocalDate fechaCita, LocalTime horaInicio, LocalTime horaFin,
                         String estado, BigDecimal costos) {}

    public record MascotaDTO(Integer idMascota, String nombre, String especie, String raza, String sexo,
                             LocalDate fechaNacimiento, String edad, BigDecimal pesoActualKg,
                             Boolean esterilizado, Boolean activo, Boolean fallecido) {}

    public record DuenoDTO(Integer idCliente, String nombreCompleto, String documentoIdentidad,
                          String telefonoPrincipal) {}

    public record AtencionResumenDTO(Integer idAtencion, LocalDateTime fecha, String diagnostico,
                                     String observaciones, String indicacionesDueno, BigDecimal pesoKg,
                                     BigDecimal temperaturaC, Integer frecCardiaca,
                                     String insumos, String codigoReceta) {}
}