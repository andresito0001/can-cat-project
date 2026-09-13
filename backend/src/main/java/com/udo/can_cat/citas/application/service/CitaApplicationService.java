package com.udo.can_cat.citas.application.service;

import com.udo.can_cat.citas.application.dto.*;
import com.udo.can_cat.citas.domain.entity.Cita;
import com.udo.can_cat.citas.domain.entity.EstadoCita;
import com.udo.can_cat.citas.domain.entity.Servicio;
import com.udo.can_cat.citas.domain.exception.*;
import com.udo.can_cat.citas.domain.repository.CitaRepository;
import com.udo.can_cat.citas.domain.repository.EstadoCitaRepository;
import com.udo.can_cat.citas.domain.repository.ServicioRepository;
import com.udo.can_cat.citas.infrastructure.persistence.CitaJpaEntity;
import com.udo.can_cat.mascotas.domain.entity.Mascota;
import com.udo.can_cat.mascotas.domain.repository.MascotaRepository;
import com.udo.can_cat.shared.tasa.TasaCambioException;
import com.udo.can_cat.shared.tasa.TasaCambioService;
import com.udo.can_cat.usuarios.domain.entity.Cliente;
import com.udo.can_cat.usuarios.domain.entity.Personal;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import com.udo.can_cat.usuarios.domain.repository.ClienteRepository;
import com.udo.can_cat.usuarios.domain.repository.PersonalRepository;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CitaApplicationService {

    private static final Logger log = LoggerFactory.getLogger(CitaApplicationService.class);

    private final CitaRepository citaRepository;
    private final EstadoCitaRepository estadoCitaRepo;
    private final ServicioRepository servicioRepo;
    private final MascotaRepository mascotaRepo;
    private final ClienteRepository clienteRepo;
    private final PersonalRepository personalRepo;
    private final UsuarioRepository usuarioRepo;
    private final TasaCambioService tasaCambioService;

    public CitaApplicationService(CitaRepository citaRepository,
                                  EstadoCitaRepository estadoCitaRepo,
                                  ServicioRepository servicioRepo,
                                  MascotaRepository mascotaRepo,
                                  ClienteRepository clienteRepo,
                                  PersonalRepository personalRepo,
                                  UsuarioRepository usuarioRepo,
                                  TasaCambioService tasaCambioService) {
        this.citaRepository = citaRepository;
        this.estadoCitaRepo = estadoCitaRepo;
        this.servicioRepo = servicioRepo;
        this.mascotaRepo = mascotaRepo;
        this.clienteRepo = clienteRepo;
        this.personalRepo = personalRepo;
        this.usuarioRepo = usuarioRepo;
        this.tasaCambioService = tasaCambioService;
    }

    // ================================================================
    // SERVICIOS ACTIVOS
    // ================================================================

    // public List<ServicioDTO> listarServiciosActivos() {
    //     return servicioRepo.buscarActivos().stream()
    //             .map(s -> new ServicioDTO(
    //                     s.getId(),
    //                     s.getNombre(),
    //                     s.getTipoAtencion(),
    //                     s.getDescripcion(),
    //                     s.getDuracionMinutos(),
    //                     s.getPrecioUsd()
    //             ))
    //             .toList();
    // }

    // Mantener el original para otros usos
    public List<ServicioDTO> listarServiciosActivos() {
        return listarServiciosActivos(null);
    }

    // Nuevo: con filtro opcional por especialidad
    public List<ServicioDTO> listarServiciosActivos(String especialidad) {
        List<Servicio> servicios;
        if (especialidad != null && !especialidad.isBlank()) {
            servicios = servicioRepo.buscarActivosPorTipoAtencion(especialidad);
        } else {
            servicios = servicioRepo.buscarActivos();
        }
        return servicios.stream()
                .map(s -> new ServicioDTO(
                        s.getId(),
                        s.getNombre(),
                        s.getTipoAtencion(),
                        s.getDescripcion(),
                        s.getDuracionMinutos(),
                        s.getPrecioUsd()
                ))
                .toList();
    }

    // ================================================================
    // SOLICITAR CITA
    // ================================================================

    @Transactional
    public SolicitarCitaResponseDTO solicitarCita(SolicitarCitaRequestDTO request) {
        // 1. Identificar cliente
        Integer idCliente = obtenerIdClienteActual();
        if (idCliente == null) {
            throw new OperacionNoPermitidaException(
                    "No se pudo identificar al cliente autenticado");
        }
        

        // 2. Validar que la mascota pertenece al cliente
        List<Mascota> mascotasDelCliente = mascotaRepo.findByClienteId(new Cliente.ClienteId(idCliente));
        boolean pertenece = mascotasDelCliente.stream()
                .anyMatch(m -> m.getId().value().equals(request.idMascota()));
        if (!pertenece) {
            throw new MascotaNoPerteneceAlClienteException(
                    "La mascota no pertenece al cliente autenticado");
        }

        // 3. Validar veterinario
        Personal vet = personalRepo.findByIdAndCargoAndActivo(
                new Personal.PersonalId(request.idVeterinario()),
                Personal.Cargo.VETERINARIO,
                true
        ).orElseThrow(() -> new HorarioNoDisponibleException("Veterinario no encontrado o inactivo"));    
        
        String nombreVet = vet.getNombreCompleto();

        // 4. Validar servicio
        Servicio servicio = servicioRepo.buscarPorId(request.idServicio())
                .filter(Servicio::getActivo)
                .orElseThrow(() -> new HorarioNoDisponibleException(
                        "Servicio no encontrado o inactivo"));

        // 5. Calcular hora fin
        LocalTime horaFin = request.horaInicio().plusMinutes(servicio.getDuracionMinutos());

        // 6. Verificar solapamiento
        List<Integer> estadosActivos = estadoCitaRepo.buscarIdsEstadosActivos();
        boolean haySolapamiento = citaRepository.existeSolapamiento(
                request.idVeterinario(),
                request.fechaCita(),
                request.horaInicio(),
                horaFin,
                estadosActivos);

        if (haySolapamiento) {
            throw new HorarioNoDisponibleException(
                    "El horario seleccionado ya no está disponible. " +
                    "Otra cita fue agendada mientras usted seleccionaba. " +
                    "Por favor, recargue la disponibilidad e intente con otro bloque.");
        }

        // 7. Obtener tasa y calcular costos
        BigDecimal tasa;
        try {
            tasa = tasaCambioService.obtenerTasaOficial();
        } catch (TasaCambioException e) {
            throw new OperacionNoPermitidaException(e.getMessage());
        }

        BigDecimal costoUsd = servicio.getPrecioUsd();
        BigDecimal costoBs = costoUsd.multiply(tasa).setScale(2, RoundingMode.HALF_UP);

        // 8. Crear cita en estado PENDIENTE_PAGO
        EstadoCita estadoPendientePago = estadoCitaRepo.buscarPorNombre("Pendiente_Pago")
                .orElseThrow(() -> new OperacionNoPermitidaException(
                        "Estado 'Pendiente_Pago' no encontrado en el sistema"));

        Cita cita = new Cita();
        cita.setIdMascota(request.idMascota());
        cita.setIdVeterinario(request.idVeterinario());
        cita.setIdServicio(request.idServicio());
        cita.setIdEstado(estadoPendientePago.getId());
        cita.setFechaCita(request.fechaCita());
        cita.setHoraInicio(request.horaInicio());
        cita.setHoraFin(horaFin);
        cita.setMotivoConsulta(request.motivoConsulta());
        cita.setTipoAtencion(servicio.getTipoAtencion());
        cita.setCostoUsd(costoUsd);
        cita.setCostoBs(costoBs);
        cita.setTasaCambioAplicada(tasa);
        cita.setCostoEstimado(costoBs);

        cita = citaRepository.guardar(cita);

        log.info("Cita {} creada en Pendiente_Pago - mascota={}, vet={}, fecha={} {}",
                cita.getId(), request.idMascota(), nombreVet,
                request.fechaCita(), request.horaInicio());

        // Nombre de la mascota para el resumen
        String nombreMascota = mascotasDelCliente.stream()
                .filter(m -> m.getId().value().equals(request.idMascota()))
                .map(Mascota::getNombre)
                .findFirst()
                .orElse("Desconocida");

        ResumenCitaDTO resumen = new ResumenCitaDTO(
                nombreMascota,
                nombreVet,
                servicio.getNombre(),
                request.fechaCita().toString(),
                request.horaInicio().toString(),
                horaFin.toString(),
                costoUsd,
                costoBs,
                tasa
        );

        return new SolicitarCitaResponseDTO(
                cita.getId(),
                "Pendiente_Pago",
                "Cita creada exitosamente. Por favor, complete el pago para confirmar su reserva.",
                resumen
        );
    }

    // ================================================================
    // CONFIRMAR PAGO (STUB)
    // ================================================================

    @Transactional
    public CitaEstadoResponseDTO confirmarPago(Integer idCita) {
        Integer idCliente = obtenerIdClienteActual();
        Cita cita = validarCitaPerteneceAlCliente(idCita, idCliente);

        String estadoActual = obtenerNombreEstado(cita.getIdEstado());

        if (!"Pendiente_Pago".equals(estadoActual)) {
            throw new OperacionNoPermitidaException(
                    "Solo se puede confirmar el pago de citas en estado 'Pendiente_Pago'. " +
                    "Estado actual: " + estadoActual);
        }

        EstadoCita estadoConfirmada = estadoCitaRepo.buscarPorNombre("Confirmada")
                .orElseThrow(() -> new OperacionNoPermitidaException(
                        "Estado 'Confirmada' no encontrado en el sistema"));

        cita.setIdEstado(estadoConfirmada.getId());
        citaRepository.guardar(cita);

        log.info("Cita {} cambiada a Confirmada (pago simulado)", idCita);

        return new CitaEstadoResponseDTO(
                idCita,
                "Confirmada",
                "Su cita ha sido agendada y pagada correctamente."
        );
    }

    // ================================================================
    // CANCELAR CITA
    // ================================================================

    @Transactional
    public CitaEstadoResponseDTO cancelarCita(Integer idCita) {
        Integer idCliente = obtenerIdClienteActual();
        Cita cita = validarCitaPerteneceAlCliente(idCita, idCliente);

        String estadoActual = obtenerNombreEstado(cita.getIdEstado());

        Set<String> estadosCancelables = Set.of("Pendiente_Pago", "Pagada", "Confirmada");
        if (!estadosCancelables.contains(estadoActual)) {
            throw new OperacionNoPermitidaException(
                    "No se puede cancelar una cita en estado '" + estadoActual + "'");
        }

        EstadoCita estadoCancelada = estadoCitaRepo.buscarPorNombre("Cancelada")
                .orElseThrow(() -> new OperacionNoPermitidaException(
                        "Estado 'Cancelada' no encontrado en el sistema"));

        cita.setIdEstado(estadoCancelada.getId());
        citaRepository.guardar(cita);

        log.info("Cita {} cancelada (estado anterior: {})", idCita, estadoActual);

        return new CitaEstadoResponseDTO(
                idCita,
                "Cancelada",
                "La cita ha sido cancelada exitosamente."
        );
    }
    
    // ================================================================
    // MIS CITAS
    // ================================================================

    /** Mantener compatibilidad con llamadas existentes sin filtro. */
    public List<MisCitasResponseDTO> obtenerMisCitas() {
        return obtenerMisCitas(null);
    }

    public List<MisCitasResponseDTO> obtenerMisCitas(String estadoFiltro) {
        Integer idCliente = obtenerIdClienteActual();
        if (idCliente == null) return List.of();

        List<Object[]> resultados = citaRepository.findCitasWithMascotaNombreByClienteId(new Cliente.ClienteId(idCliente));
        if (resultados.isEmpty()) return List.of();

        List<Cita> citas = new ArrayList<>();
        Map<Integer, String> nombresMascotas = new HashMap<>();
        for (Object[] fila : resultados) {
            CitaJpaEntity entity = (CitaJpaEntity) fila[0];
            Cita cita = citaRepository.toDomain(entity);
            String nombreMascota = (String) fila[1];
            citas.add(cita);
            nombresMascotas.put(cita.getIdMascota(), nombreMascota);
        }

        Map<Integer, String> nombresServicios = cargarNombresServicios(citas);
        Map<Integer, EstadoCita> estadosPorId = cargarEstados();
        Map<Integer, String> nombresVets = cargarNombresVeterinarios(citas);

        return citas.stream()
                .map(cita -> {
                    EstadoCita estado = estadosPorId.get(cita.getIdEstado());
                    return new MisCitasResponseDTO(
                            cita.getId(),
                            nombresMascotas.getOrDefault(cita.getIdMascota(), "Desconocida"),
                            nombresVets.getOrDefault(cita.getIdVeterinario(), "Por asignar"),
                            nombresServicios.getOrDefault(cita.getIdServicio(), "Desconocido"),
                            estado != null ? estado.getNombre() : "Desconocido",
                            estado != null ? estado.getColorUi() : "#6C757D",
                            cita.getFechaCita().toString(),
                            cita.getHoraInicio().toString(),
                            cita.getHoraFin() != null ? cita.getHoraFin().toString() : "",
                            cita.getCostoUsd(),
                            cita.getCostoBs()
                    );
                })
                .filter(dto -> estadoFiltro == null || estadoFiltro.isBlank()
                        || dto.estado().equalsIgnoreCase(estadoFiltro.trim()))
                .toList();
    }

    // ================================================================
    // FILTRAR BLOQUES LIBRES (usado por DisponibilidadApplicationService)
    // ================================================================

    public List<BloqueHorarioDTO> filtrarBloquesLibres(Integer idVeterinario,
                                                        java.time.LocalDate fecha,
                                                        List<BloqueHorarioDTO> bloques) {
        if (bloques.isEmpty()) return bloques;

        List<Integer> estadosActivos = estadoCitaRepo.buscarIdsEstadosActivos();
        List<Cita> citasExistentes = citaRepository.buscarActivasPorVeterinarioYFecha(
                idVeterinario, fecha, estadosActivos);

        if (citasExistentes.isEmpty()) return bloques;

        return bloques.stream()
                .filter(bloque -> !haySolapamientoConAlguna(bloque, citasExistentes))
                .toList();
    }

    // ================================================================
    // MÉTODOS PRIVADOS
    // ================================================================

//     private Integer obtenerIdClienteActual() {
//         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//         if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
//             return null;
//         }
//         String email = auth.getName();

//         return usuarioRepo.findByCorreoElectronico(email)
//                 .flatMap(u -> clienteRepo.findByUsuarioId(u.getId()))
//                 .map(c -> c.getId().value())
//                 .orElse(null);
//     }
    private Integer obtenerIdClienteActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        // El Principal es el ID del usuario (Integer), no el email
        Integer usuarioId;
        if (auth.getPrincipal() instanceof Integer) {
            usuarioId = (Integer) auth.getPrincipal();
        } else {
            // Fallback por si acaso
            return null;
        }

        return clienteRepo.findByUsuarioId(new UsuarioId(usuarioId))  // o como sea tu wrapper de ID
                .map(c -> c.getId().value())
                .orElse(null);
    }

    private Cita validarCitaPerteneceAlCliente(Integer idCita, Integer idCliente) {
        Cita cita = citaRepository.buscarPorId(idCita)
                .orElseThrow(() -> new CitaNoEncontradaException(
                        "Cita no encontrada con ID: " + idCita));

        var clienteId = new Cliente.ClienteId(idCliente);
        List<Mascota> mascotas = mascotaRepo.findByClienteId(clienteId);
        boolean pertenece = mascotas.stream()
                .anyMatch(m -> m.getId().value().equals(cita.getIdMascota()));

        if (!pertenece) {
            throw new MascotaNoPerteneceAlClienteException(
                    "La cita no pertenece al cliente autenticado");
        }
        return cita;
    }

    private String obtenerNombreEstado(Integer idEstado) {
        return estadosPorId().get(idEstado).getNombre();
    }

    /** Cache por llamada: carga todos los estados una sola vez. */
    private Map<Integer, EstadoCita> estadosPorId() {
        return cargarEstados();
    }

    private Map<Integer, EstadoCita> cargarEstados() {
        return estadoCitaRepo.buscarTodos().stream()
            .collect(Collectors.toMap(EstadoCita::getId, e -> e));
    }
    
    private Map<Integer, String> cargarNombresVeterinarios(List<Cita> citas) {
        Set<Personal.PersonalId> ids = citas.stream()
            .map(Cita::getIdVeterinario)
            .filter(Objects::nonNull)
            .map(Personal.PersonalId::new)
            .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        return personalRepo.findAllByIds(ids).stream()
            .collect(Collectors.toMap(
                p -> p.getPersonalId().value(),
                Personal::getNombreCompleto
            ));
    }

    private Map<Integer, String> cargarNombresServicios(List<Cita> citas) {
        Set<Integer> ids = citas.stream()
            .map(Cita::getIdServicio)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        return servicioRepo.buscarPorIds(ids).stream()
            .collect(Collectors.toMap(Servicio::getId, Servicio::getNombre));
    }

    private boolean haySolapamientoConAlguna(BloqueHorarioDTO bloque,
                                               List<Cita> citasExistentes) {
        LocalTime bloqueInicio = LocalTime.parse(bloque.horaInicio());
        LocalTime bloqueFin = LocalTime.parse(bloque.horaFin());

        for (Cita existente : citasExistentes) {
            if (existente.getHoraFin() == null) continue;
            if (bloqueInicio.isBefore(existente.getHoraFin())
                    && existente.getHoraInicio().isBefore(bloqueFin)) {
                return true;
            }
        }
        return false;
    }
}