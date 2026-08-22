package com.udo.can_cat.usuarios.infrastructure.persistence;

import jakarta.persistence.Table;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "password_reset_tokens")
public class TokenRecuperacionContrasenaJpaEntity {
    @Id
    @Column(length = 512)
    private String token;
    
    @Column(nullable = false)
    private String correo;
    
    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;
    
    @Column(nullable = false)
    private boolean usado = false;
    
    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    

    public void setCorreo(String correo) { this.correo = correo; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    public void setToken(String token) { this.token = token; }
    public void setUsado(boolean usado) { this.usado = usado; }

    public String getCorreo() { return correo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public String getToken() { return token; }
    public boolean isUsado() { return usado; }
    
}
