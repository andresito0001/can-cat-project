package com.udo.can_cat.usuarios.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.udo.can_cat.usuarios.domain.entity.Personal;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;

@Entity
@Table(name = "personal", uniqueConstraints = {
    @UniqueConstraint(name = "uk_personal_usuario", columnNames = "id_usuario"),
    @UniqueConstraint(name = "uk_personal_codigo", columnNames = "codigo_empleado")
})
public class PersonalJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_personal")
    private Integer idPersonal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, foreignKey = @ForeignKey(name = "fk_personal_usuario"))
    private UsuarioJpaEntity usuario;

    @Column(name = "codigo_empleado", length = 20, nullable = false, unique = true)
    private String codigoEmpleado;

    @Column(name = "cargo", length = 50, nullable = false)
    private String cargo;

    @Column(name = "especialidad", length = 100)
    private String especialidad;

    @Column(name = "fecha_contratacion", nullable = false)
    private LocalDate fechaContratacion;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "horario_atencion", columnDefinition = "jsonb")
    private Map<String, Object> horarioAtencion = new HashMap<>();

    @Column(name = "licencia_profesional", length = 50)
    private String licenciaProfesional;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.horarioAtencion == null) {
            this.horarioAtencion = new HashMap<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static PersonalJpaEntity fromDomain(Personal domain) {
        if (domain == null) {
            return null;
        }

        PersonalJpaEntity entity = new PersonalJpaEntity();

        if (domain.getPersonalId() != null) {
            entity.setIdPersonal(domain.getPersonalId().value());
        }

        if (domain.getUsuarioId() != null) {
            UsuarioJpaEntity usuarioRef = new UsuarioJpaEntity();
            usuarioRef.setIdUsuario(domain.getUsuarioId().value());
            entity.setUsuario(usuarioRef);
        }

        entity.setCodigoEmpleado(domain.getCodigoEmpleado().value());
        entity.setCargo(domain.getCargo().getDbValue()); 
        
        if (domain.getEspecialidad() != null) {
            entity.setEspecialidad(domain.getEspecialidad().value());
        }

        entity.setFechaContratacion(domain.getFechaContratacion().value());
        entity.setActivo(domain.isActivo());

        if (domain.getHorarioAtencion() != null) {
            entity.setHorarioAtencion(new HashMap<>(domain.getHorarioAtencion().value()));
        }

        if (domain.getLicenciaProfesional() != null) {
            entity.setLicenciaProfesional(domain.getLicenciaProfesional().value());
        }

        if (domain.getCreatedAt() != null) {
            entity.setCreatedAt(domain.getCreatedAt());
        }
        if (domain.getUpdatedAt() != null) {
            entity.setUpdatedAt(domain.getUpdatedAt());
        }

        return entity;
    }

    public Personal toDomain() {
        return new Personal(
                new Personal.PersonalId(this.idPersonal),
                new UsuarioId(this.usuario.getIdUsuario()),
                new Personal.CodigoEmpleado(this.codigoEmpleado),
                Personal.Cargo.fromDbValue(this.cargo), 
                this.especialidad != null ? new Personal.Especialidad(this.especialidad) : null,
                new Personal.FechaContratacion(this.fechaContratacion),
                this.activo,
                new Personal.HorarioAtencion(this.horarioAtencion),
                this.licenciaProfesional != null ? new Personal.LicenciaProfesional(this.licenciaProfesional) : null,
                this.createdAt,
                this.updatedAt
        );
    }

    public Integer getIdPersonal() { return idPersonal; }
    public UsuarioJpaEntity getUsuario() { return usuario; }
    public String getCodigoEmpleado() { return codigoEmpleado; }
    public String getCargo() { return cargo; }
    public String getEspecialidad() { return especialidad; }
    public LocalDate getFechaContratacion() { return fechaContratacion; }
    public boolean getActivo() { return activo; }
    public Map<String, Object> getHorarioAtencion() { return horarioAtencion; }
    public String getLicenciaProfesional() { return licenciaProfesional; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setIdPersonal(Integer idPersonal) { this.idPersonal = idPersonal; }
    public void setUsuario(UsuarioJpaEntity usuario) { this.usuario = usuario; }
    public void setCodigoEmpleado(String codigoEmpleado) { this.codigoEmpleado = codigoEmpleado; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public void setFechaContratacion(LocalDate fechaContratacion) { this.fechaContratacion = fechaContratacion; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public void setHorarioAtencion(Map<String, Object> horarioAtencion) { this.horarioAtencion = horarioAtencion; }
    public void setLicenciaProfesional(String licenciaProfesional) { this.licenciaProfesional = licenciaProfesional; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}