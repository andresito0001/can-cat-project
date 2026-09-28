package com.udo.can_cat.almacen.application.dto;

import jakarta.validation.constraints.*;

public record CrearProveedorRequestDTO(
        @NotBlank(message = "El RIF es obligatorio")
        @Pattern(regexp = "^[JGVEPjgyep]-\\d{8}-\\d$",
                 message = "RIF inválido. Formato esperado: J-12345678-9")
        String rif,

        @NotBlank @Size(max = 150)
        String nombreEmpresa,

        @Size(max = 100)
        String nombreContacto,

        @Size(max = 20)
        String telefono,

        @Email(message = "Correo inválido")
        @Size(max = 100)
        String correo,

        String direccion,

        @NotBlank
        @Pattern(regexp = "^(Medicamentos|Alimentos|Mixto)$",
                 message = "Tipo de suministro inválido")
        String tipoSuministro
) {}