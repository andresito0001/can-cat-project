package com.udo.can_cat.atenciones.infrastructure.persistence;

import com.udo.can_cat.atenciones.application.port.PersonalPort;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class AtencionesPersonalPortAdapter implements PersonalPort {

    private static final String SELECT_BASE = """
            SELECT p.id_personal, p.nombre_completo, p.especialidad, p.licencia_profesional
            FROM personal p
            WHERE p.activo = TRUE
            """;

    private final EntityManager em;

    public AtencionesPersonalPortAdapter(EntityManager em) {
        this.em = em;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<PersonalInfo> buscarPorUsuarioId(Integer usuarioId) {
        List<Object[]> filas = em.createNativeQuery(SELECT_BASE + " AND p.id_usuario = :usuarioId")
                .setParameter("usuarioId", usuarioId)
                .getResultList();
        return filas.isEmpty() ? Optional.empty() : Optional.of(aPersonalInfo(filas.get(0)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<PersonalInfo> buscarPorId(Integer personalId) {
        List<Object[]> filas = em.createNativeQuery(SELECT_BASE + " AND p.id_personal = :personalId")
                .setParameter("personalId", personalId)
                .getResultList();
        return filas.isEmpty() ? Optional.empty() : Optional.of(aPersonalInfo(filas.get(0)));
    }

    private static PersonalInfo aPersonalInfo(Object[] f) {
        return new PersonalInfo(SqlConverters.aEntero(f[0]), (String) f[1], (String) f[2], (String) f[3]);
    }
}