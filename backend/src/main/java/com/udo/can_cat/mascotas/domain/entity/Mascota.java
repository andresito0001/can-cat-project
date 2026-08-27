package com.udo.can_cat.mascotas.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import com.udo.can_cat.usuarios.domain.entity.Cliente.ClienteId;

public class Mascota {

    private MascotaId mascotaId;
    private ClienteId clienteId;
    private Especie.EspecieId especieId;
    private Raza.RazaId razaId;
    private String nombre;
    private LocalDate fechaNacimiento;
    private Sexo sexo;
    private String color;
    private BigDecimal pesoActual;
    private boolean esterilizado;
    private boolean activo;
    private boolean fallecido;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Sexo { M, H }

    public Mascota(MascotaId mascotaId, ClienteId clienteId, Especie.EspecieId especieId,
                   Raza.RazaId razaId, String nombre, LocalDate fechaNacimiento,
                   Sexo sexo, String color, BigDecimal pesoActual, boolean esterilizado,
                   boolean activo, boolean fallecido, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.mascotaId = mascotaId;
        this.clienteId = Objects.requireNonNull(clienteId, "El ID del cliente es obligatorio");
        this.especieId = Objects.requireNonNull(especieId, "El ID de la especie es obligatorio");
        this.razaId = razaId;
        this.nombre = Objects.requireNonNull(nombre, "El nombre de la mascota es obligatorio");
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = Objects.requireNonNull(sexo, "El sexo de la mascota es obligatorio");
        this.color = color;
        this.pesoActual = pesoActual;
        this.esterilizado = esterilizado;
        this.activo = activo;
        this.fallecido = fallecido;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static Mascota crear(ClienteId clienteId, Especie.EspecieId especieId,
                                Raza.RazaId razaId, String nombre, LocalDate fechaNacimiento,
                                Sexo sexo, String color, BigDecimal pesoActual, boolean esterilizado) {
        return new Mascota(null, clienteId, especieId, razaId, nombre, fechaNacimiento,
                sexo, color, pesoActual, esterilizado, true, false, null, null);
    }

    public MascotaId getId() { return mascotaId; }
    public ClienteId getClienteId() { return clienteId; }
    public Especie.EspecieId getEspecieId() { return especieId; }
    public Raza.RazaId getRazaId() { return razaId; }
    public String getNombre() { return nombre; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public Sexo getSexo() { return sexo; }
    public String getColor() { return color; }
    public BigDecimal getPesoActual() { return pesoActual; }
    public boolean isEsterilizado() { return esterilizado; }
    public boolean isActivo() { return activo; }
    public boolean isFallecido() { return fallecido; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public record MascotaId(Integer value) {
        public MascotaId {
            Objects.requireNonNull(value, "El ID de mascota no puede ser nulo");
        }
    }
}