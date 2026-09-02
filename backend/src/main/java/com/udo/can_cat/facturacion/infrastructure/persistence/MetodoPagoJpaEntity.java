package com.udo.can_cat.facturacion.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.udo.can_cat.facturacion.domain.entity.MetodoPago;

@Entity
@Table(name = "metodo_pago")
public class MetodoPagoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo_pago")
    private Integer idMetodoPago;

    @Column(name = "nombre", length = 50, nullable = false, unique = true)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_requeridos", columnDefinition = "jsonb")
    private Map<String, String> datosRequeridos = new HashMap<>();

    @Column(name = "activo")
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        if (this.datosRequeridos == null) this.datosRequeridos = new HashMap<>();
    }

    public MetodoPago toDomain() {
        MetodoPago d = new MetodoPago();
        d.setId(idMetodoPago);
        d.setNombre(nombre);
        d.setDescripcion(descripcion);
        d.setDatosRequeridos(datosRequeridos);
        d.setActivo(activo);
        return d;
    }

    public static MetodoPagoJpaEntity fromDomain(MetodoPago d) {
        MetodoPagoJpaEntity e = new MetodoPagoJpaEntity();
        if (d.getId() != null) e.setIdMetodoPago(d.getId());
        e.setNombre(d.getNombre());
        e.setDescripcion(d.getDescripcion());
        e.setDatosRequeridos(d.getDatosRequeridos());
        e.setActivo(d.getActivo());
        return e;
    }

    // Getters y setters
    public Integer getIdMetodoPago() { return idMetodoPago; }
    public void setIdMetodoPago(Integer idMetodoPago) { this.idMetodoPago = idMetodoPago; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Map<String, String> getDatosRequeridos() { return datosRequeridos; }
    public void setDatosRequeridos(Map<String, String> datosRequeridos) { this.datosRequeridos = datosRequeridos; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}