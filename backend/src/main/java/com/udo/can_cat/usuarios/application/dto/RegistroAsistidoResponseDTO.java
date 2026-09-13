package com.udo.can_cat.usuarios.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistroAsistidoResponseDTO {

    private Integer idCliente;
    private Integer idUsuario;
    private String nombreCompleto;
    private String documentoIdentidad;
    private String correoElectronico;
    private Boolean invitacionEnviada;
    private String mensaje;

    public static RegistroAsistidoResponseDTO exito(Integer idCliente, Integer idUsuario,
                                                     String nombreCompleto, String documentoIdentidad,
                                                     String correoElectronico, boolean invitacionEnviada) {
        RegistroAsistidoResponseDTO dto = new RegistroAsistidoResponseDTO();
        dto.setIdCliente(idCliente);
        dto.setIdUsuario(idUsuario);
        dto.setNombreCompleto(nombreCompleto);
        dto.setDocumentoIdentidad(documentoIdentidad);
        dto.setCorreoElectronico(correoElectronico);
        dto.setInvitacionEnviada(invitacionEnviada);
        dto.setMensaje(invitacionEnviada
                ? "Cliente registrado satisfactoriamente. Se envió un enlace a su correo para definir la contraseña."
                : "Cliente registrado satisfactoriamente. No se pudo enviar el enlace; el cliente puede usar la opción \"¿Olvidaste tu contraseña?\".");
        return dto;
    }

    // Getters y Setters
    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public void setDocumentoIdentidad(String documentoIdentidad) { this.documentoIdentidad = documentoIdentidad; }

    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }

    public Boolean getInvitacionEnviada() { return invitacionEnviada; }
    public void setInvitacionEnviada(Boolean invitacionEnviada) { this.invitacionEnviada = invitacionEnviada; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}