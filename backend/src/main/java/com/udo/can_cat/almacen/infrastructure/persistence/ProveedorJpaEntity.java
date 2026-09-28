package com.udo.can_cat.almacen.infrastructure.persistence;

import com.udo.can_cat.almacen.domain.entity.Proveedor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "proveedor")
public class ProveedorJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String rif;

    @Column(name = "nombre_empresa", nullable = false, length = 150)
    private String nombreEmpresa;

    @Column(name = "nombre_contacto", length = 100)
    private String nombreContacto;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String correo;

    @Column(columnDefinition = "text")
    private String direccion;

    @Column(name = "tipo_suministro", length = 50)
    private String tipoSuministro;

    private Boolean activo;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ─── Ciclo de vida ───
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.activo == null) this.activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ─── Conversiones dominio ↔ JPA ───
    public static ProveedorJpaEntity fromDomain(Proveedor p) {
        if (p == null) return null;
        ProveedorJpaEntity e = new ProveedorJpaEntity();
        if (p.getId() != null) {
            e.id = p.getId().value();
        }
        e.rif = p.getRif();
        e.nombreEmpresa = p.getNombreEmpresa();
        e.nombreContacto = p.getNombreContacto();
        e.telefono = p.getTelefono();
        e.correo = p.getCorreo();
        e.direccion = p.getDireccion();
        e.tipoSuministro = p.getTipoSuministro();
        e.activo = p.getActivo() != null ? p.getActivo() : true;
        e.createdAt = p.getCreatedAt();
        e.updatedAt = p.getUpdatedAt();
        return e;
    }

    public Proveedor toDomain() {
        Proveedor p = new Proveedor();
        p.setId(new Proveedor.ProveedorId(this.id));
        p.setRif(this.rif);
        p.setNombreEmpresa(this.nombreEmpresa);
        p.setNombreContacto(this.nombreContacto);
        p.setTelefono(this.telefono);
        p.setCorreo(this.correo);
        p.setDireccion(this.direccion);
        p.setTipoSuministro(this.tipoSuministro);
        p.setActivo(this.activo);
        p.setCreatedAt(this.createdAt);
        p.setUpdatedAt(this.updatedAt);
        return p;
    }

    // ─── Getters y setters ───
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getRif() { return rif; }
    public void setRif(String rif) { this.rif = rif; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public String getNombreContacto() { return nombreContacto; }
    public void setNombreContacto(String nombreContacto) { this.nombreContacto = nombreContacto; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTipoSuministro() { return tipoSuministro; }
    public void setTipoSuministro(String tipoSuministro) { this.tipoSuministro = tipoSuministro; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}