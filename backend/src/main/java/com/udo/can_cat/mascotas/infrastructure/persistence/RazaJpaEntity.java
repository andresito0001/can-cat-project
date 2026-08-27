package com.udo.can_cat.mascotas.infrastructure.persistence;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "raza", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_especie", "nombre"})
})
public class RazaJpaEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_raza")
    private Integer id;

    @Column(name = "id_especie", nullable = false)
    private Integer idEspecie;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "caracteristicas", length = 500)
    private String caracteristicas;

    public Integer getId() { return id; }
    public Integer getIdEspecie() { return idEspecie; }
    public String getNombre() { return nombre; }
    public String getCaracteristicas() { return caracteristicas; }
}