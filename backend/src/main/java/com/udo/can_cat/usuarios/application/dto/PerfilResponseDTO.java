package com.udo.can_cat.usuarios.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Vista unificada del perfil del usuario autenticado.
 * Solo uno de los dos bloques (cliente o personal) viene informado,
 * según el tipo de usuario.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PerfilResponseDTO(
        // ─── Datos comunes ───
        Integer idUsuario,
        String correoElectronico,
        String rol,
        String nombreCompleto,
        String tipoUsuario,      // "Cliente" | "Personal"
        Integer idEntidad,       // idCliente | idPersonal
        LocalDateTime fechaRegistro,
        LocalDateTime ultimoAcceso,

        // ─── Bloque específico: Cliente ───
        DatosCliente cliente,

        // ─── Bloque específico: Personal ───
        DatosPersonal personal
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DatosCliente(
            String documentoIdentidad,
            String telefonoPrincipal,
            String telefonoSecundario,
            String direccion,
            String ciudad,
            LocalDate fechaNacimiento,
            Boolean editable
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DatosPersonal(
            String codigoEmpleado,
            String cargo,
            String especialidad,
            String licenciaProfesional,
            LocalDate fechaContratacion,
            Boolean activo,
            Map<String, Object> horarioAtencion
    ) {}
}