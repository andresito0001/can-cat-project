package com.udo.can_cat.usuarios.domain.entity;

import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

public class Personal {
    private PersonalId personalId;
    private UsuarioId usuarioId;
    private CodigoEmpleado codigoEmpleado;
    private Cargo cargo;
    private Especialidad especialidad;
    private FechaContratacion fechaContratacion;
    private boolean activo;
    private HorarioAtencion horarioAtencion;
    private LicenciaProfesional licenciaProfesional;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Personal(PersonalId personalId,
                    UsuarioId usuarioId,
                    CodigoEmpleado codigoEmpleado,
                    Cargo cargo,
                    Especialidad especialidad,
                    FechaContratacion fechaContratacion,
                    boolean activo,
                    HorarioAtencion horarioAtencion,
                    LicenciaProfesional licenciaProfesional,
                    LocalDateTime createdAt,
                    LocalDateTime updatedAt) {
        this.personalId = personalId;
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId no puede ser nulo");
        this.codigoEmpleado = Objects.requireNonNull(codigoEmpleado, "codigoEmpleado no puede ser nulo");
        this.cargo = Objects.requireNonNull(cargo, "cargo no puede ser nulo");
        this.especialidad = especialidad;
        this.fechaContratacion = Objects.requireNonNull(fechaContratacion, "fechaContratacion no puede ser nulo");
        this.activo = activo;
        this.horarioAtencion = horarioAtencion != null ? horarioAtencion : HorarioAtencion.vacio();
        this.licenciaProfesional = licenciaProfesional;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    // Getters
    public PersonalId getPersonalId() { return personalId; }
    public UsuarioId getUsuarioId() { return usuarioId; }
    public CodigoEmpleado getCodigoEmpleado() { return codigoEmpleado; }
    public Cargo getCargo() { return cargo; }
    public Especialidad getEspecialidad() { return especialidad; }
    public FechaContratacion getFechaContratacion() { return fechaContratacion; }
    public boolean isActivo() { return activo; }
    public HorarioAtencion getHorarioAtencion() { return horarioAtencion; }
    public LicenciaProfesional getLicenciaProfesional() { return licenciaProfesional; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public record PersonalId(Integer value) {
        public PersonalId {
            Objects.requireNonNull(value, "PersonalId no puede ser nulo");
            if (value <= 0) {
                throw new IllegalArgumentException("PersonalId debe ser positivo");
            }
        }
    }

    public record CodigoEmpleado(String value) {
        public CodigoEmpleado {
            Objects.requireNonNull(value, "CodigoEmpleado no puede ser nulo");
            if (value.isBlank()) {
                throw new IllegalArgumentException("CodigoEmpleado no puede estar vacío");
            }
            if (value.length() > 20) {
                throw new IllegalArgumentException("CodigoEmpleado no puede exceder 20 caracteres");
            }
        }
    }

    public enum Cargo {
        VETERINARIO("Veterinario"),
        RECEPCIONISTA("Recepcionista"),
        ENCARGADO_ALMACEN("Encargado_Almacen"),
        ADMINISTRADOR("Administrador");

        private final String dbValue;

        Cargo(String dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue;
        }

        public static Cargo fromDbValue(String value) {
            Objects.requireNonNull(value, "Cargo no puede ser nulo");
            for (Cargo c : values()) {
                if (c.dbValue.equals(value)) {
                    return c;
                }
            }
            throw new IllegalArgumentException("Cargo inválido: " + value);
        }
    }

    public record Especialidad(String value) {
        public Especialidad {
            if (value != null && value.length() > 100) {
                throw new IllegalArgumentException("Especialidad no puede exceder 100 caracteres");
            }
        }
    }

    public record FechaContratacion(LocalDate value) {
        public FechaContratacion {
            Objects.requireNonNull(value, "FechaContratacion no puede ser nula");
            if (value.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("FechaContratacion no puede ser futura");
            }
        }
    }

    public record HorarioAtencion(Map<String, Object> value) {
        public HorarioAtencion {
            value = value == null ? Map.of() : Map.copyOf(value);
        }

        public static HorarioAtencion vacio() {
            return new HorarioAtencion(Map.of());
        }
    }

    public record LicenciaProfesional(String value) {
        public LicenciaProfesional {
            if (value != null && value.length() > 50) {
                throw new IllegalArgumentException("LicenciaProfesional no puede exceder 50 caracteres");
            }
        }
    }
}