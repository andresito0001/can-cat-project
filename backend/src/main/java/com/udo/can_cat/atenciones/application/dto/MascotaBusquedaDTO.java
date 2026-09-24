package com.udo.can_cat.atenciones.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Resultado de /api/mascotas/buscar (6.2): paciente + dueño enriquecidos. */
public record MascotaBusquedaDTO(
        Integer idMascota, String nombre, String especie, String raza, String sexo,
        LocalDate fechaNacimiento, String edad, BigDecimal pesoActualKg,
        Boolean activo, Boolean fallecido,
        Integer idCliente, String clienteNombre, String clienteDocumento, String clienteTelefono) {}