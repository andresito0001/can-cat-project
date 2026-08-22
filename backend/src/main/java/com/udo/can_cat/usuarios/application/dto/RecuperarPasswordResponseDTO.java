package com.udo.can_cat.usuarios.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecuperarPasswordResponseDTO {
    
    private String mensaje;
    private TipoResultado tipo;
    private String correo;

    public enum TipoResultado {
        ENLACE_ENVIADO,
        BLOQUEADO_POR_POLITICA,
        CORREO_NO_REGISTRADO
    }

    // Factory methods
    public static RecuperarPasswordResponseDTO enlaceEnviado(String correo) {
        RecuperarPasswordResponseDTO response = new RecuperarPasswordResponseDTO();
        response.setMensaje("Se ha enviado un enlace de recuperación a su correo electrónico");
        response.setTipo(TipoResultado.ENLACE_ENVIADO);
        response.setCorreo(correo);
        return response;
    }

    public static RecuperarPasswordResponseDTO bloqueadoPorPolitica() {
        RecuperarPasswordResponseDTO response = new RecuperarPasswordResponseDTO();
        response.setMensaje("Por políticas de la clínica, el personal interno debe contactar al Administrador para restablecer sus credenciales");
        response.setTipo(TipoResultado.BLOQUEADO_POR_POLITICA);
        return response;
    }

    public static RecuperarPasswordResponseDTO correoNoRegistrado() {
        RecuperarPasswordResponseDTO response = new RecuperarPasswordResponseDTO();
        response.setMensaje("Si el correo está registrado en nuestro sistema, recibirá instrucciones para recuperar su contraseña");
        response.setTipo(TipoResultado.CORREO_NO_REGISTRADO);
        return response;
    }

    // Getters y Setters
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public TipoResultado getTipo() { return tipo; }
    public void setTipo(TipoResultado tipo) { this.tipo = tipo; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}