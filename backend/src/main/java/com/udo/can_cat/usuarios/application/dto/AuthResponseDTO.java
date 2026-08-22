package com.udo.can_cat.usuarios.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO {
    
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private UsuarioAuthDTO usuario;

    // Constructor estático para facilitar creación
    public static AuthResponseDTO of(String token, Long expiresIn, UsuarioAuthDTO usuario) {
        AuthResponseDTO response = new AuthResponseDTO();
        response.setAccessToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(expiresIn);
        response.setUsuario(usuario);
        return response;
    }

    // Getters y Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }

    public UsuarioAuthDTO getUsuario() { return usuario; }
    public void setUsuario(UsuarioAuthDTO usuario) { this.usuario = usuario; }

    // Clase anidada para datos del usuario
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UsuarioAuthDTO {
        private Integer id;
        private String correoElectronico;
        private String rol;
        private String nombreCompleto;

        public static UsuarioAuthDTO of(Integer id, String correo, String rol, String nombre) {
            UsuarioAuthDTO dto = new UsuarioAuthDTO();
            dto.setId(id);
            dto.setCorreoElectronico(correo);
            dto.setRol(rol);
            dto.setNombreCompleto(nombre);
            return dto;
        }

        // Getters y Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getCorreoElectronico() { return correoElectronico; }
        public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }

        public String getRol() { return rol; }
        public void setRol(String rol) { this.rol = rol; }

        public String getNombreCompleto() { return nombreCompleto; }
        public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    }
}