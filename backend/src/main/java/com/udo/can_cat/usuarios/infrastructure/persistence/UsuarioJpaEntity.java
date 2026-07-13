package com.udo.can_cat.usuarios.infrastructure.persistence;

import com.udo.can_cat.usuarios.domain.entity.Usuario;
import com.udo.can_cat.usuarios.domain.entity.Rol.RolId;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class UsuarioJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    @Column(name = "id_rol")
    private Integer idRol;

    @Column(unique = true, nullable = false, length = 100)
    private String correoElectronico;

    @Column(nullable = false, length = 255)
    private String contrasenaHash;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    public UsuarioJpaEntity() {}

    public static UsuarioJpaEntity fromDomain(Usuario usuario) {
        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        if (usuario.getId() != null) entity.idUsuario = usuario.getId().value();
        entity.idRol = usuario.getRolId().value();
        entity.correoElectronico = usuario.getCorreoElectronico();
        entity.contrasenaHash = usuario.getContrasenaHash();
        entity.estado = usuario.getEstado().name();
        entity.fechaRegistro = usuario.getFechaRegistro();
        entity.ultimoAcceso = usuario.getUltimoAcceso();
        return entity;
    }

    public Usuario toDomain() {
        return new Usuario (
                new Usuario.UsuarioId(idUsuario),
                new RolId(idRol),
                correoElectronico,
                contrasenaHash,
                Usuario.EstadoUsuario.valueOf(estado),
                fechaRegistro,
                ultimoAcceso
        );
    }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }
    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }
}