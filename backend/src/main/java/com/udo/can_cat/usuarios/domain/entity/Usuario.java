package com.udo.can_cat.usuarios.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import com.udo.can_cat.usuarios.domain.entity.Rol.RolId;

public class Usuario {
    private UsuarioId id;
    private RolId rolId;          
    private String correoElectronico;
    private String contrasenaHash;
    private EstadoUsuario estado;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimoAcceso;

    public Usuario(UsuarioId id, RolId rolId, String correoElectronico,
                    String contrasenaHash, EstadoUsuario estado,
                    LocalDateTime fechaRegistro, LocalDateTime ultimoAcceso) {
        this.id = id;
        this.rolId = Objects.requireNonNull(rolId, "El rol es obligatorio");
        this.correoElectronico = Objects.requireNonNull(correoElectronico);
        this.contrasenaHash = Objects.requireNonNull(contrasenaHash);
        this.estado = estado != null ? estado : EstadoUsuario.Activo;
        this.fechaRegistro = fechaRegistro != null ? fechaRegistro : LocalDateTime.now();
        this.ultimoAcceso = ultimoAcceso;
    }

    // Factory method
    public static Usuario crear(RolId rolId, String correo, String contrasenaHash) {
        return new Usuario(null, rolId, correo, contrasenaHash,
                EstadoUsuario.Activo, LocalDateTime.now(), null);
    }

    // Comportamiento: cambiar estado
    public void activar() {
        this.estado = EstadoUsuario.Activo;
    }

    public void bloquear() {
        this.estado = EstadoUsuario.Bloqueado;
    }

    public void desactivar() {
        this.estado = EstadoUsuario.Inactivo;
    }

    // Getters
    public UsuarioId getId() { return id; }
    public RolId getRolId() { return rolId; }
    public String getCorreoElectronico() { return correoElectronico; }
    public String getContrasenaHash() { return contrasenaHash; }
    public EstadoUsuario getEstado() { return estado; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }

    public record UsuarioId(Integer value) {
        public UsuarioId {
            Objects.requireNonNull(value, "UsuarioId no puede ser nulo");
            if (value <= 0) {
                throw new IllegalArgumentException("UsuarioId debe ser positivo");
            }
        }
    }

    public enum EstadoUsuario {
        Activo, Inactivo, Bloqueado
    }
}