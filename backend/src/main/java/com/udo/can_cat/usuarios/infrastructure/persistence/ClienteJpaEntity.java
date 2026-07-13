package com.udo.can_cat.usuarios.infrastructure.persistence;

import com.udo.can_cat.usuarios.domain.entity.Cliente;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "cliente")
public class ClienteJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCliente;

    @Column(name = "id_usuario", unique = true, nullable = false)
    private Integer idUsuario;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "documento_identidad", unique = true, nullable = false, length = 20)
    private String documentoIdentidad;

    @Column(name = "telefono_principal", nullable = false, length = 20)
    private String telefonoPrincipal;

    @Column(name = "telefono_secundario", length = 20)
    private String telefonoSecundario;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Column(length = 50)
    private String ciudad;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferencias_notificacion", columnDefinition = "jsonb")
    private String preferenciasNotificacion;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ClienteJpaEntity() {}

    public static ClienteJpaEntity fromDomain(Cliente cliente) {
        ClienteJpaEntity entity = new ClienteJpaEntity();
        if (cliente.getId() != null) entity.idCliente = cliente.getId().value();
        entity.idUsuario = cliente.getUsuarioId().value();
        entity.nombreCompleto = cliente.getNombreCompleto();
        entity.documentoIdentidad = cliente.getDocumentoIdentidad();
        entity.telefonoPrincipal = cliente.getTelefonoPrincipal();
        entity.telefonoSecundario = cliente.getTelefonoSecundario();
        entity.direccion = cliente.getDireccion();
        entity.ciudad = cliente.getCiudad();
        entity.fechaNacimiento = cliente.getFechaNacimiento();
        entity.preferenciasNotificacion = cliente.getPreferenciasNotificacion();
        entity.createdAt = cliente.getCreatedAt();
        entity.updatedAt = cliente.getUpdatedAt();
        return entity;
    }

    public Cliente toDomain() {
        return new Cliente (
                new Cliente.ClienteId(idCliente),
                new UsuarioId(idUsuario),
                nombreCompleto,
                documentoIdentidad,
                telefonoPrincipal,
                telefonoSecundario,
                direccion,
                ciudad,
                fechaNacimiento,
                preferenciasNotificacion,
                createdAt,
                updatedAt
        );
    }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public void setDocumentoIdentidad(String documentoIdentidad) { this.documentoIdentidad = documentoIdentidad; }
    public String getTelefonoPrincipal() { return telefonoPrincipal; }
    public void setTelefonoPrincipal(String telefonoPrincipal) { this.telefonoPrincipal = telefonoPrincipal; }
    public String getTelefonoSecundario() { return telefonoSecundario; }
    public void setTelefonoSecundario(String telefonoSecundario) { this.telefonoSecundario = telefonoSecundario; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getPreferenciasNotificacion() { return preferenciasNotificacion; }
    public void setPreferenciasNotificacion(String preferenciasNotificacion) { this.preferenciasNotificacion = preferenciasNotificacion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}