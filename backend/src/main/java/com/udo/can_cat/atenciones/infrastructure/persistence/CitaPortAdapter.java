package com.udo.can_cat.atenciones.infrastructure.persistence;

import com.udo.can_cat.atenciones.application.port.CitaPort;
import com.udo.can_cat.atenciones.domain.exception.OperacionAtencionInvalidaException;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CitaPortAdapter implements CitaPort {

    /** Colores oficiales del sistema (mismos valores que estadosCita.js). */
    private static final Map<String, String> COLORES_ESTADO = Map.of(
            EstadosCita.PENDIENTE_PAGO, "#FFC107",
            EstadosCita.PAGADA, "#17A2B8",
            EstadosCita.CONFIRMADA, "#28A745",
            EstadosCita.EN_ATENCION, "#FD7E14",
            EstadosCita.COMPLETADA, "#6C757D",
            EstadosCita.CANCELADA, "#DC3545");

    /**
     * Nombres de estado oficiales. Única fuente de verdad:
     * se interpolan en el SQL y se validan antes de actualizar.
     */
    static final class EstadosCita {
        static final String PENDIENTE_PAGO = "Pendiente_Pago";
        static final String PAGADA = "Pagada";
        static final String CONFIRMADA = "Confirmada";
        static final String EN_ATENCION = "En_Atencion";
        static final String COMPLETADA = "Completada";
        static final String CANCELADA = "Cancelada";

        private EstadosCita() {
        }
    }

    /** Posiciones de columna en el SELECT de la agenda (defensivas ante reordenamientos). */
    private static final int A_ID_CITA = 0;
    private static final int A_FECHA = 1;
    private static final int A_HORA_INICIO = 2;
    private static final int A_HORA_FIN = 3;
    private static final int A_MOTIVO = 4;
    private static final int A_SERVICIO = 5;
    private static final int A_ESTADO = 6;
    private static final int A_ID_MASCOTA = 7;
    private static final int A_MASCOTA = 8;
    private static final int A_ESPECIE = 9;
    private static final int A_RAZA = 10;
    private static final int A_ID_CLIENTE = 11;
    private static final int A_CLIENTE = 12;
    private static final int A_DOCUMENTO = 13;
    private static final int A_TELEFONO = 14;

    private static final String COLOR_POR_DEFECTO = "#64748B";

    private final EntityManager em;

    public CitaPortAdapter(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Optional<CitaInfo> buscarPorId(Integer idCita) {
        String sql = """
                SELECT c.id_cita, c.id_mascota, c.id_veterinario, c.id_servicio,
                       c.fecha_cita, c.hora_inicio, c.hora_fin,
                       c.motivo_consulta, c.tipo_atencion, c.costo_usd,
                       e.nombre AS estado, s.nombre AS servicio
                FROM cita c
                LEFT JOIN estado_cita e ON e.id_estado = c.id_estado
                LEFT JOIN servicio s   ON s.id_servicio = c.id_servicio
                WHERE c.id_cita = :idCita
                """;
        List<Object[]> filas = em.createNativeQuery(sql)
                .setParameter("idCita", idCita)
                .setMaxResults(1)
                .getResultList();

        if (filas.isEmpty()) {
            return Optional.empty();
        }

        Object[] f = filas.get(0);
        return Optional.of(new CitaInfo(
                SqlConverters.aEntero(f[0]), SqlConverters.aEntero(f[1]), SqlConverters.aEntero(f[2]),
                SqlConverters.aEntero(f[3]), SqlConverters.aFecha(f[4]), SqlConverters.aHora(f[5]),
                SqlConverters.aHora(f[6]), (String) f[7], (String) f[8], SqlConverters.aDecimal(f[9]),
                (String) f[10], (String) f[11]));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaAgendaInfo> buscarAgendaVeterinario(Integer idVeterinario, LocalDate fecha) {
        // Los nombres de estado se interpolan desde constantes: typo = excepción al arrancar, no en runtime.
        String sql = """
                SELECT c.id_cita, c.fecha_cita, c.hora_inicio, c.hora_fin, c.motivo_consulta,
                       s.nombre AS servicio, e.nombre AS estado,
                       m.id_mascota, m.nombre AS mascota, esp.nombre AS especie, r.nombre AS raza,
                       cl.id_cliente, cl.nombre_completo, cl.documento_identidad, cl.telefono_principal
                FROM cita c
                JOIN estado_cita e ON e.id_estado = c.id_estado
                LEFT JOIN servicio s ON s.id_servicio = c.id_servicio
                JOIN mascota m ON m.id_mascota = c.id_mascota
                LEFT JOIN especie esp ON esp.id_especie = m.id_especie
                LEFT JOIN raza r ON r.id_raza = m.id_raza
                JOIN cliente cl ON cl.id_cliente = m.id_cliente
                WHERE c.id_veterinario = :idVeterinario
                  AND c.fecha_cita = :fecha
                  AND e.nombre IN ('%s', '%s')
                ORDER BY c.hora_inicio ASC
                """.formatted(EstadosCita.CONFIRMADA, EstadosCita.EN_ATENCION);

        List<Object[]> filas = em.createNativeQuery(sql, Object[].class)
                .setParameter("idVeterinario", idVeterinario)
                .setParameter("fecha", fecha)
                .getResultList();

        List<CitaAgendaInfo> agenda = new ArrayList<>(filas.size());
        for (Object[] f : filas) {
            String estadoNombre = (String) f[A_ESTADO];
            agenda.add(new CitaAgendaInfo(
                    SqlConverters.aEntero(f[A_ID_CITA]),
                    SqlConverters.aFecha(f[A_FECHA]),
                    SqlConverters.aHora(f[A_HORA_INICIO]),
                    SqlConverters.aHora(f[A_HORA_FIN]),
                    (String) f[A_MOTIVO],
                    (String) f[A_SERVICIO],
                    estadoNombre,
                    COLORES_ESTADO.getOrDefault(estadoNombre, COLOR_POR_DEFECTO),
                    SqlConverters.aEntero(f[A_ID_MASCOTA]),
                    (String) f[A_MASCOTA],
                    (String) f[A_ESPECIE],
                    (String) f[A_RAZA],
                    SqlConverters.aEntero(f[A_ID_CLIENTE]),
                    (String) f[A_CLIENTE],
                    (String) f[A_DOCUMENTO],
                    (String) f[A_TELEFONO]));
        }
        return agenda;
    }

    @Override
    public void actualizarEstado(Integer idCita, String estadoNombre) {
        // 1. Validar que el estado existe ANTES de tocar la cita.
        //    Si no, el UPDATE nativo pondría id_estado = NULL silenciosamente.
        List<Object> estados = em.createNativeQuery(
                        "SELECT id_estado FROM estado_cita WHERE nombre = :nombre")
                .setParameter("nombre", estadoNombre)
                .setMaxResults(1)
                .getResultList();

        if (estados.isEmpty()) {
            throw new OperacionAtencionInvalidaException(
                    "Estado de cita no existe: '" + estadoNombre + "'");
        }

        Integer idEstado = SqlConverters.aEntero(estados.get(0));

        // 2. Actualizar con el id resuelto (sin subselect: un solo round-trip a la BD).
        int filas = em.createNativeQuery(
                        "UPDATE cita SET id_estado = :idEstado WHERE id_cita = :idCita")
                .setParameter("idEstado", idEstado)
                .setParameter("idCita", idCita)
                .executeUpdate();

        if (filas != 1) {
            throw new OperacionAtencionInvalidaException(
                    "No se pudo actualizar el estado de la cita " + idCita
                            + " a '" + estadoNombre + "'");
        }
    }
}