package com.udo.can_cat.usuarios.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistroClienteResponseDTO {

    private Integer idUsuario;
    private Integer idCliente;
    private String correoElectronico;
    private String nombreCompleto;
    private String mensaje;

    // Factory method
    public static RegistroClienteResponseDTO exito(Integer idUsuario, Integer idCliente, 
                                                     String correo, String nombre) {
        RegistroClienteResponseDTO dto = new RegistroClienteResponseDTO();
        dto.setIdUsuario(idUsuario);
        dto.setIdCliente(idCliente);
        dto.setCorreoElectronico(correo);
        dto.setNombreCompleto(nombre);
        dto.setMensaje("Registro exitoso. Ya puede iniciar sesión.");
        return dto;
    }

    // Getters y Setters
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}