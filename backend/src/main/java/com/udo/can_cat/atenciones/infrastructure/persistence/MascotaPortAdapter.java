package com.udo.can_cat.atenciones.infrastructure.persistence;

import com.udo.can_cat.atenciones.application.port.MascotaPort;
import com.udo.can_cat.atenciones.domain.exception.MascotaNoEncontradaException;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
public class MascotaPortAdapter implements MascotaPort {

    private static final String SELECT_BASE = """
            SELECT m.id_mascota, m.nombre, esp.nombre, r.nombre, m.sexo, m.fecha_nacimiento,
                   m.peso_actual, m.esterilizado, m.activo, m.fallecido,
                   cl.id_cliente, cl.nombre_completo, cl.documento_identidad, cl.telefono_principal
            FROM mascota m
            LEFT JOIN especie esp ON esp.id_especie = m.id_especie
            LEFT JOIN raza r ON r.id_raza = m.id_raza
            JOIN cliente cl ON cl.id_cliente = m.id_cliente
            """;

    private final EntityManager em;

    public MascotaPortAdapter(EntityManager em) {
        this.em = em;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<MascotaInfo> buscarPorId(Integer idMascota) {
        List<Object[]> filas = em.createNativeQuery(SELECT_BASE + " WHERE m.id_mascota = :id")
                .setParameter("id", idMascota)
                .getResultList();
        if (filas.isEmpty()) return Optional.empty();
        return Optional.of(aMascotaInfo(filas.get(0)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MascotaInfo> buscarPorFiltro(String filtro, int limite) {
        List<Object[]> filas = em.createNativeQuery(SELECT_BASE + """
                        WHERE (LOWER(m.nombre) LIKE :filtro
                               OR LOWER(cl.nombre_completo) LIKE :filtro
                               OR LOWER(cl.documento_identidad) LIKE :filtro)
                        ORDER BY m.nombre ASC
                        LIMIT :limite
                        """)
                .setParameter("filtro", "%" + filtro.toLowerCase() + "%")
                .setParameter("limite", limite)
                .getResultList();
        return filas.stream().map(MascotaPortAdapter::aMascotaInfo).toList();
    }

    @Override
    public void actualizarPeso(Integer idMascota, BigDecimal pesoKg) {
        int filas = em.createNativeQuery(
                        "UPDATE mascota SET peso_actual = :peso WHERE id_mascota = :id")
                .setParameter("peso", pesoKg)
                .setParameter("id", idMascota)
                .executeUpdate();
        if (filas != 1) {
            throw new MascotaNoEncontradaException(idMascota);
        }
    }

    private static MascotaInfo aMascotaInfo(Object[] f) {
        return new MascotaInfo(
                SqlConverters.aEntero(f[0]),
                SqlConverters.aTexto(f[1]), SqlConverters.aTexto(f[2]),
                SqlConverters.aTexto(f[3]), SqlConverters.aTexto(f[4]),   // ← sexo CHAR(1)
                SqlConverters.aFecha(f[5]), SqlConverters.aDecimal(f[6]),
                SqlConverters.aBooleano(f[7]), SqlConverters.aBooleano(f[8]),
                SqlConverters.aBooleano(f[9]),
                SqlConverters.aEntero(f[10]),
                SqlConverters.aTexto(f[11]), SqlConverters.aTexto(f[12]),
                SqlConverters.aTexto(f[13]));
    }
}