package com.udo.can_cat.almacen.infrastructure.persistence;
import com.udo.can_cat.almacen.domain.entity.Proveedor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "proveedor")
public class ProveedorJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "tipo_suministro", length = 50)
    private String tipoSuministro;

    private Boolean activo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters y Setters (genera los básicos con tu IDE)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getRif() { return rif; }
    public void setRif(String rif) { this.rif = rif; }
    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Proveedor toDomain() {
        Proveedor p = new Proveedor();
        p.setId(new Proveedor.ProveedorId(this.id));
        p.setRif(this.rif);
        p.setNombreEmpresa(this.nombreEmpresa);
        p.setActivo(this.activo);
        return p;
    }
}