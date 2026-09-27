package com.udo.can_cat.citas.application.service;

import com.udo.can_cat.citas.application.port.PagoVerificacionPort;
import com.udo.can_cat.citas.domain.entity.Cita;
import com.udo.can_cat.citas.domain.entity.EstadoCita;
import com.udo.can_cat.citas.domain.exception.CitaNoEncontradaException;
import com.udo.can_cat.citas.domain.exception.TransicionInvalidaException;
import com.udo.can_cat.citas.domain.repository.CitaRepository;
import com.udo.can_cat.citas.domain.repository.EstadoCitaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class CitaTransitionService {

    private static final Logger log = LoggerFactory.getLogger(CitaTransitionService.class);

    // ─── Estados canónicos ───
    public static final String PENDIENTE_PAGO = "Pendiente_Pago";
    public static final String CONFIRMADA     = "Confirmada";
    public static final String EN_ATENCION    = "En_Atencion";
    public static final String COMPLETADA     = "Completada";
    public static final String CANCELADA      = "Cancelada";

    // ─── Transiciones válidas (origen → destinos permitidos) ───
    private static final Map<String, Set<String>> TRANSICIONES_VALIDAS = Map.of(
        PENDIENTE_PAGO, Set.of(CONFIRMADA, CANCELADA),
        CONFIRMADA,     Set.of(EN_ATENCION, CANCELADA),
        EN_ATENCION,    Set.of(COMPLETADA),
        COMPLETADA,     Set.of(),
        CANCELADA,      Set.of()
    );

    // ─── Roles autorizados por transición ───
    private static final Map<String, Set<String>> ROLES_POR_TRANSICION = Map.of(
        key(PENDIENTE_PAGO, CONFIRMADA),  Set.of("Recepcionista", "Administrador", "Sistema"),
        key(PENDIENTE_PAGO, CANCELADA),   Set.of("Recepcionista", "Administrador", "Sistema", "Cliente"),
        key(CONFIRMADA,     EN_ATENCION), Set.of("Veterinario"),
        key(CONFIRMADA,     CANCELADA),   Set.of("Recepcionista", "Administrador", "Cliente"),
        key(EN_ATENCION,    COMPLETADA),  Set.of("Veterinario")
    );

    private static String key(String origen, String destino) {
        return origen + "→" + destino;
    }

    private final CitaRepository citaRepo;
    private final EstadoCitaRepository estadoRepo;
    private final PagoVerificacionPort pagoVerifPort;

    public CitaTransitionService(CitaRepository citaRepo,
                                 EstadoCitaRepository estadoRepo,
                                 PagoVerificacionPort pagoVerifPort) {
        this.citaRepo = citaRepo;
        this.estadoRepo = estadoRepo;
        this.pagoVerifPort = pagoVerifPort;
    }

    /**
     * Ejecuta una transición de estado sobre una cita aplicando los 3 guards:
     *   1. La transición debe estar declarada como válida.
     *   2. El rol del actor debe estar autorizado para esa transición.
     *   3. Las precondiciones de negocio deben cumplirse.
     */
    public Cita transicionar(Integer idCita, String destino, Actor actor) {
        Cita cita = citaRepo.buscarPorId(idCita)
                .orElseThrow(() -> new CitaNoEncontradaException(
                        "Cita no encontrada con ID: " + idCita));

        String origen = nombreEstado(cita.getIdEstado());

        // ─── Guard 1: transición válida ───
        Set<String> permitidos = TRANSICIONES_VALIDAS.getOrDefault(origen, Set.of());
        if (!permitidos.contains(destino)) {
            throw new TransicionInvalidaException(String.format(
                    "Transición no permitida: %s → %s (cita %d)", origen, destino, idCita));
        }

        // ─── Guard 2: rol autorizado ───
        Set<String> roles = ROLES_POR_TRANSICION.getOrDefault(key(origen, destino), Set.of());
        if (!roles.contains(actor.rol())) {
            throw new AccessDeniedException(String.format(
                    "El rol '%s' no puede ejecutar la transición %s → %s",
                    actor.rol(), origen, destino));
        }

        // ─── Guard 3: precondiciones de negocio ───
        validarPrecondiciones(cita, destino, actor);

        // ─── Efecto ───
        Integer idEstadoDestino = estadoRepo.buscarPorNombre(destino)
                .orElseThrow(() -> new IllegalStateException(
                        "Estado no encontrado en BD: " + destino))
                .getId();

        cita.setIdEstado(idEstadoDestino);
        Cita actualizada = citaRepo.guardar(cita);

        log.info("Cita {} transicionada: {} → {} por {}#{}",
                idCita, origen, destino, actor.rol(), actor.id());

        return actualizada;
    }

    private void validarPrecondiciones(Cita cita, String destino, Actor actor) {
        switch (destino) {
            case CONFIRMADA -> {
                if (!pagoVerifPort.existePagoConfirmadoParaCita(cita.getId())) {
                    throw new TransicionInvalidaException(
                            "No se puede confirmar una cita sin un pago verificado");
                }
            }
            case CANCELADA -> {
                // El cliente NO puede cancelar si ya tiene pago activo
                // (Pendiente_Verificacion o Confirmado). Debe contactar a recepción.
                if ("Cliente".equals(actor.rol())
                        && pagoVerifPort.existePagoActivoParaCita(cita.getId())) {
                    throw new TransicionInvalidaException(
                            "No puedes cancelar una cita con un pago registrado. " +
                            "Contacta a recepción para verificar o rechazar tu pago.");
                }
            }
            case EN_ATENCION, COMPLETADA -> {
                if (!"Veterinario".equals(actor.rol())) {
                    throw new AccessDeniedException(
                            "Solo el veterinario asignado puede iniciar o completar la atención");
                }
                if (cita.getIdVeterinario() == null
                        || !cita.getIdVeterinario().equals(actor.id())) {
                    throw new AccessDeniedException(
                            "Solo el veterinario asignado a esta cita puede ejecutar la transición");
                }
            }
            default -> { }
        }
    }

    private String nombreEstado(Integer idEstado) {
        return estadoRepo.buscarTodos().stream()
                .filter(e -> e.getId().equals(idEstado))
                .findFirst()
                .map(EstadoCita::getNombre)
                .orElseThrow(() -> new IllegalStateException(
                        "Estado no encontrado para id: " + idEstado));
    }

    // ─────────────────────────────────────────────────────────
    // Actor — quién ejecuta la transición
    // ─────────────────────────────────────────────────────────
    public record Actor(String rol, Integer id) {
        public static Actor sistema()            { return new Actor("Sistema", null); }
        public static Actor recepcionista(int p) { return new Actor("Recepcionista", p); }
        public static Actor veterinario(int p)   { return new Actor("Veterinario", p); }
        public static Actor cliente(int c)       { return new Actor("Cliente", c); }
        public static Actor admin(int p)         { return new Actor("Administrador", p); }
    }
}