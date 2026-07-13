package com.udo.can_cat.usuarios.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;

public class Cliente {
    private ClienteId clienteId;
    private UsuarioId usuarioId;
    private String nombreCompleto;
    private String documentoIdentidad;
    private String telefonoPrincipal;
    private String telefonoSecundario;
    private String direccion;
    private String ciudad;
    private LocalDate fechaNacimiento;
    private String preferenciasNotificacion;    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Cliente(ClienteId clienteId, UsuarioId usuarioId, String nombreCompleto,
                     String documentoIdentidad, String telefonoPrincipal, 
                     String telefonoSecundario, String direccion, String ciudad,
                     LocalDate fechaNacimiento, String preferenciasNotificacion,
                     LocalDateTime fechaRegistro, LocalDateTime ultimoAcceso) {
        this.clienteId = clienteId;
        this.usuarioId = Objects.requireNonNull(usuarioId, "El usuario no existe");
        this.nombreCompleto = Objects.requireNonNull(nombreCompleto);
        this.documentoIdentidad = Objects.requireNonNull(documentoIdentidad);
        this.telefonoPrincipal = Objects.requireNonNull(telefonoPrincipal);
        this.telefonoSecundario = telefonoSecundario;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.fechaNacimiento = fechaNacimiento;
        this.preferenciasNotificacion = preferenciasNotificacion != null ? preferenciasNotificacion : "{\"email\": true, \"sms\": false}";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static Cliente crear(UsuarioId usuarioId, String nombreCompleto,
                                String documentoIdentidad, String telefonoPrincipal,
                                String telefonoSecundario, String direccion,
                                String ciudad, LocalDate fechaNacimiento,
                                String preferenciasNotificacion) {
        return new Cliente(null, usuarioId, nombreCompleto, documentoIdentidad,
                telefonoPrincipal, telefonoSecundario, direccion, ciudad,
                fechaNacimiento, preferenciasNotificacion, null, null);
    }

    public ClienteId getId() { return clienteId; }
    public UsuarioId getUsuarioId() { return usuarioId; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public String getTelefonoPrincipal() { return telefonoPrincipal; }
    public String getTelefonoSecundario() { return telefonoSecundario; }
    public String getDireccion() { return direccion; }
    public String getCiudad() { return ciudad; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getPreferenciasNotificacion() { return preferenciasNotificacion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public record ClienteId(Integer value) {
        public ClienteId {
            Objects.nonNull(value);
        }
    } 
}

