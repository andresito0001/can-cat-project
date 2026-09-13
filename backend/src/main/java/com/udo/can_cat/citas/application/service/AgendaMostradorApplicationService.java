package com.udo.can_cat.citas.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.udo.can_cat.citas.application.dto.AgendarMostradorRequestDTO;
import com.udo.can_cat.citas.application.dto.AgendarMostradorResponseDTO;
import com.udo.can_cat.citas.application.dto.CitaAgendaDTO;
import com.udo.can_cat.citas.application.dto.CitaEstadoResponseDTO;
import com.udo.can_cat.citas.application.dto.CobrarCitaMostradorRequestDTO;
import com.udo.can_cat.citas.domain.entity.Cita;
import com.udo.can_cat.citas.domain.entity.EstadoCita;
import com.udo.can_cat.citas.domain.entity.Servicio;
import com.udo.can_cat.citas.domain.exception.CitaNoEncontradaException;
import com.udo.can_cat.citas.domain.exception.HorarioNoDisponibleException;
import com.udo.can_cat.citas.domain.exception.MascotaNoPerteneceAlClienteException;
import com.udo.can_cat.citas.domain.exception.OperacionNoPermitidaException;
import com.udo.can_cat.citas.domain.repository.CitaRepository;
import com.udo.can_cat.citas.domain.repository.EstadoCitaRepository;
import com.udo.can_cat.citas.domain.repository.ServicioRepository;
import com.udo.can_cat.facturacion.domain.entity.DetalleFactura;
import com.udo.can_cat.facturacion.domain.entity.Factura;
import com.udo.can_cat.facturacion.domain.entity.MetodoPago;
import com.udo.can_cat.facturacion.domain.entity.Pago;
import com.udo.can_cat.facturacion.domain.exception.FacturacionException;
import com.udo.can_cat.facturacion.domain.repository.DetalleFacturaRepository;
import com.udo.can_cat.facturacion.domain.repository.FacturaRepository;
import com.udo.can_cat.facturacion.domain.repository.MetodoPagoRepository;
import com.udo.can_cat.facturacion.domain.repository.PagoRepository;
import com.udo.can_cat.mascotas.domain.entity.Mascota;
import com.udo.can_cat.mascotas.domain.repository.MascotaRepository;
import com.udo.can_cat.shared.tasa.TasaCambioException;
import com.udo.can_cat.shared.tasa.TasaCambioService;
import com.udo.can_cat.usuarios.domain.entity.Cliente;
import com.udo.can_cat.usuarios.domain.entity.Personal;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import com.udo.can_cat.usuarios.domain.repository.ClienteRepository;
import com.udo.can_cat.usuarios.domain.repository.PersonalRepository;

/**
 * CU 4.6.1.11 — Gestionar Cita (mostrador) + Cobro de citas pendientes +
 * Transición de estados por recepción.
 */
@Service
public class AgendaMostradorApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AgendaMostradorApplicationService.class);
    private static final String PREFIJO_FACTURA = "FC";

    /** Matriz de transiciones de estado permitidas (Completada/Cancelada son finales). */
    private static final Map<String, Set<String>> TRANSICIONES_PERMITIDAS = Map.of(
            "Pendiente_Pago", Set.of("Pagada", "Confirmada", "Cancelada"),
            "Pagada",         Set.of("Confirmada", "Cancelada"),
            "Confirmada",     Set.of("En_Atencion", "Completada", "Cancelada"),
            "En_Atencion",    Set.of("Completada", "Cancelada"));

    private final CitaRepository citaRepo;
    private final EstadoCitaRepository estadoCitaRepo;
    private final ServicioRepository servicioRepo;
    private final MascotaRepository mascotaRepo;
    private final ClienteRepository clienteRepo;
    private final PersonalRepository personalRepo;
    private final FacturaRepository facturaRepo;
    private final DetalleFacturaRepository detalleFacturaRepo;
    private final PagoRepository pagoRepo;
    private final MetodoPagoRepository metodoPagoRepo;
    private final TasaCambioService tasaCambioService;

    public AgendaMostradorApplicationService(CitaRepository citaRepo,
                                             EstadoCitaRepository estadoCitaRepo,
                                             ServicioRepository servicioRepo,
                                             MascotaRepository mascotaRepo,
                                             ClienteRepository clienteRepo,
                                             PersonalRepository personalRepo,
                                             FacturaRepository facturaRepo,
                                             DetalleFacturaRepository detalleFacturaRepo,
                                             PagoRepository pagoRepo,
                                             MetodoPagoRepository metodoPagoRepo,
                                             TasaCambioService tasaCambioService) {
        this.citaRepo = citaRepo;
        this.estadoCitaRepo = estadoCitaRepo;
        this.servicioRepo = servicioRepo;
        this.mascotaRepo = mascotaRepo;
        this.clienteRepo = clienteRepo;
        this.personalRepo = personalRepo;
        this.facturaRepo = facturaRepo;
        this.detalleFacturaRepo = detalleFacturaRepo;
        this.pagoRepo = pagoRepo;
        this.metodoPagoRepo = metodoPagoRepo;
        this.tasaCambioService = tasaCambioService;
    }

    // ================================================================
    // AGENDA (CU paso 2 — ahora con rango de fechas)
    // ================================================================

    @Transactional(readOnly = true)
    public List<CitaAgendaDTO> consultarAgenda(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Integer> estadosActivos = estadoCitaRepo.buscarIdsEstadosActivos();
        return mapearCitas(citaRepo.buscarActivasPorRangoFechas(fechaInicio, fechaFin, estadosActivos));
    }

    // ================================================================
    // CITAS PENDIENTES DE PAGO (para "Cobrar en Mostrador")
    // ================================================================

    @Transactional(readOnly = true)
    public List<CitaAgendaDTO> listarPendientesPago() {
        EstadoCita pendiente = estadoCitaRepo.buscarPorNombre("Pendiente_Pago")
                .orElseThrow(() -> new OperacionNoPermitidaException(
                        "Estado 'Pendiente_Pago' no encontrado"));
        return mapearCitas(citaRepo.buscarPorIdEstado(pendiente.getId()));
    }

    // ================================================================
    // AGENDAR + COBRAR + FACTURAR (CU pasos 3 al 8)
    // ================================================================

    @Transactional
    public AgendarMostradorResponseDTO agendarYFacturar(AgendarMostradorRequestDTO request) {
        Personal recepcionista = resolverPersonalActual();

        Cliente cliente = clienteRepo.findById(new Cliente.ClienteId(request.idCliente()))
                .orElseThrow(() -> new OperacionNoPermitidaException(
                        "El cliente indicado no existe. Verifique los datos o regístrelo."));

        Mascota mascota = mascotaRepo.findByClienteId(cliente.getId()).stream()
                .filter(m -> m.getId().value().equals(request.idMascota()))
                .findFirst()
                .orElseThrow(() -> new MascotaNoPerteneceAlClienteException(
                        "La mascota no está asociada al cliente seleccionado"));

        Personal vet = personalRepo.findByIdAndCargoAndActivo(
                        new Personal.PersonalId(request.idVeterinario()),
                        Personal.Cargo.VETERINARIO, true)
                .orElseThrow(() -> new HorarioNoDisponibleException("Veterinario no encontrado o inactivo"));

        Servicio servicio = servicioRepo.buscarPorId(request.idServicio())
                .filter(Servicio::getActivo)
                .orElseThrow(() -> new HorarioNoDisponibleException("Servicio no encontrado o inactivo"));

        MetodoPago metodoPago = validarMetodoPresencial(request.idMetodoPago(), request.datosPago());

        LocalTime horaFin = request.horaInicio().plusMinutes(servicio.getDuracionMinutos());
        List<Integer> estadosActivos = estadoCitaRepo.buscarIdsEstadosActivos();
        if (citaRepo.existeSolapamiento(request.idVeterinario(), request.fechaCita(),
                request.horaInicio(), horaFin, estadosActivos)) {
            throw new HorarioNoDisponibleException(
                    "El bloque de horario seleccionado ya no se encuentra disponible. " +
                    "Por favor, seleccione otro horario.");
        }

        BigDecimal tasa;
        try {
            tasa = tasaCambioService.obtenerTasaOficial();
        } catch (TasaCambioException e) {
            throw new OperacionNoPermitidaException(e.getMessage());
        }
        BigDecimal costoUsd = servicio.getPrecioUsd();
        BigDecimal costoBs = costoUsd.multiply(tasa).setScale(2, RoundingMode.HALF_UP);

        EstadoCita estadoConfirmada = estadoCitaRepo.buscarPorNombre("Confirmada")
                .orElseThrow(() -> new OperacionNoPermitidaException(
                        "Estado 'Confirmada' no encontrado en el sistema"));

        Cita cita = new Cita();
        cita.setIdMascota(request.idMascota());
        cita.setIdVeterinario(request.idVeterinario());
        cita.setIdServicio(request.idServicio());
        cita.setIdEstado(estadoConfirmada.getId());
        cita.setFechaCita(request.fechaCita());
        cita.setHoraInicio(request.horaInicio());
        cita.setHoraFin(horaFin);
        cita.setMotivoConsulta(request.motivoConsulta());
        cita.setTipoAtencion(servicio.getTipoAtencion());
        cita.setCostoUsd(costoUsd);
        cita.setCostoBs(costoBs);
        cita.setTasaCambioAplicada(tasa);
        cita.setCostoEstimado(costoBs);
        cita.setObservacionesRecepcion(recepcionista != null
                ? "Agendada y cobrada en mostrador por " + recepcionista.getNombreCompleto()
                : "Agendada y cobrada en mostrador");
        cita = citaRepo.guardar(cita);

        Factura factura = guardarFacturaYPago(cita, servicio, cliente.getId().value(),
                metodoPago, recepcionista, request.referenciaTransaccion(), request.datosPago());

        log.info("Mostrador: cita={} Confirmada, factura={}", cita.getId(), factura.getNumeroControl());

        return new AgendarMostradorResponseDTO(
                cita.getId(), "Confirmada", factura.getId(), factura.getNumeroControl(),
                "Cita agendada y facturada correctamente.",
                construirResumen(cliente, mascota, vet, servicio, cita, metodoPago));
    }

    // ================================================================
    // COBRAR CITA PENDIENTE DE PAGO (módulo "Cobrar en Mostrador")
    // ================================================================

    @Transactional
    public AgendarMostradorResponseDTO cobrarCitaPendiente(Integer idCita,
                                                           CobrarCitaMostradorRequestDTO request) {
        Personal recepcionista = resolverPersonalActual();

        Cita cita = citaRepo.buscarPorId(idCita)
                .orElseThrow(() -> new CitaNoEncontradaException("Cita no encontrada con ID: " + idCita));

        String estadoActual = nombreEstadoPorId(cita.getIdEstado());
        if (!"Pendiente_Pago".equals(estadoActual)) {
            throw new OperacionNoPermitidaException(
                    "Solo se pueden cobrar en mostrador citas en estado 'Pendiente_Pago'. " +
                    "Estado actual: " + estadoActual);
        }

        MetodoPago metodoPago = validarMetodoPresencial(request.idMetodoPago(), request.datosPago());

        Servicio servicio = servicioRepo.buscarPorId(cita.getIdServicio())
                .filter(Servicio::getActivo)
                .orElseThrow(() -> new FacturacionException("Servicio de la cita no encontrado"));

        Mascota mascota = mascotaRepo.findById(new Mascota.MascotaId(cita.getIdMascota()))
                .orElseThrow(() -> new OperacionNoPermitidaException("La mascota de la cita no existe"));

        Cliente cliente = clienteRepo.findById(mascota.getClienteId())
                .orElseThrow(() -> new OperacionNoPermitidaException("El cliente de la cita no existe"));

        Personal vet = personalRepo.findById(new Personal.PersonalId(cita.getIdVeterinario()))
                .orElseThrow(() -> new OperacionNoPermitidaException("El veterinario de la cita no existe"));

        Factura factura = guardarFacturaYPago(cita, servicio, cliente.getId().value(),
                metodoPago, recepcionista, request.referenciaTransaccion(), request.datosPago());

        EstadoCita confirmada = estadoCitaRepo.buscarPorNombre("Confirmada")
                .orElseThrow(() -> new OperacionNoPermitidaException(
                        "Estado 'Confirmada' no encontrado"));
        cita.setIdEstado(confirmada.getId());
        String nota = "Cobrada en mostrador (" + metodoPago.getNombre() + ") por "
                + (recepcionista != null ? recepcionista.getNombreCompleto() : "recepción");
        cita.setObservacionesRecepcion(cita.getObservacionesRecepcion() != null
                ? cita.getObservacionesRecepcion() + "\n" + nota : nota);
        citaRepo.guardar(cita);

        log.info("Cobro mostrador: cita={} → Confirmada, factura={}", idCita, factura.getNumeroControl());

        return new AgendarMostradorResponseDTO(
                cita.getId(), "Confirmada", factura.getId(), factura.getNumeroControl(),
                "Cita cobrada y facturada correctamente.",
                construirResumen(cliente, mascota, vet, servicio, cita, metodoPago));
    }

    // ================================================================
    // CAMBIO DE ESTADO MANUAL (recepción)
    // ================================================================

    @Transactional
    public CitaEstadoResponseDTO cambiarEstado(Integer idCita, String estadoDestino) {
        Cita cita = citaRepo.buscarPorId(idCita)
                .orElseThrow(() -> new CitaNoEncontradaException("Cita no encontrada con ID: " + idCita));

        String actual = nombreEstadoPorId(cita.getIdEstado());
        Set<String> destinos = TRANSICIONES_PERMITIDAS.get(actual);
        if (destinos == null || !destinos.contains(estadoDestino)) {
            throw new OperacionNoPermitidaException(
                    "Transición inválida: no se puede pasar una cita de '" + actual +
                    "' a '" + estadoDestino + "'");
        }

        EstadoCita destino = estadoCitaRepo.buscarPorNombre(estadoDestino)
                .orElseThrow(() -> new OperacionNoPermitidaException(
                        "Estado '" + estadoDestino + "' no encontrado en el sistema"));

        Personal recepcionista = resolverPersonalActual();
        String nota = "Estado cambiado a " + estadoDestino + " por "
                + (recepcionista != null ? recepcionista.getNombreCompleto() : "recepción")
                + " (" + LocalDate.now() + ")";
        cita.setObservacionesRecepcion(cita.getObservacionesRecepcion() != null
                ? cita.getObservacionesRecepcion() + "\n" + nota : nota);
        cita.setIdEstado(destino.getId());
        citaRepo.guardar(cita);

        log.info("Cita {}: {} → {}", idCita, actual, estadoDestino);

        return new CitaEstadoResponseDTO(idCita, estadoDestino,
                "Estado actualizado a '" + estadoDestino + "'.");
    }

    // ================================================================
    // PRIVADOS
    // ================================================================

    private Personal resolverPersonalActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Integer usuarioId)) return null;
        return personalRepo.findByUsuarioId(new UsuarioId(usuarioId)).orElse(null);
    }

    private MetodoPago validarMetodoPresencial(Integer idMetodoPago, Map<String, String> datosPago) {
        MetodoPago metodoPago = metodoPagoRepo.buscarPorId(idMetodoPago)
                .orElseThrow(() -> new FacturacionException("Método de pago no encontrado"));
        if ("Transferencia".equals(metodoPago.getNombre())) {
            throw new FacturacionException("La transferencia no es un método de pago presencial");
        }
        validarDatosPago(metodoPago, datosPago);
        return metodoPago;
    }

    private List<CitaAgendaDTO> mapearCitas(List<Cita> citas) {
        if (citas.isEmpty()) return List.of();

        Map<Integer, Mascota> mascotas = new HashMap<>();
        for (Cita cita : citas) {
            mascotaRepo.findById(new Mascota.MascotaId(cita.getIdMascota()))
                    .ifPresent(m -> mascotas.put(m.getId().value(), m));
        }
        Map<Integer, Cliente> clientes = new HashMap<>();
        for (Mascota m : mascotas.values()) {
            clienteRepo.findById(m.getClienteId())
                    .ifPresent(c -> clientes.put(c.getId().value(), c));
        }
        Map<Integer, String> vets = cargarNombresVeterinarios(citas);
        Map<Integer, String> servicios = cargarNombresServicios(citas);
        Map<Integer, EstadoCita> estados = estadoCitaRepo.buscarTodos().stream()
                .collect(Collectors.toMap(EstadoCita::getId, e -> e));

        return citas.stream()
                .map(cita -> {
                    Mascota mascota = mascotas.get(cita.getIdMascota());
                    Cliente cliente = mascota != null ? clientes.get(mascota.getClienteId().value()) : null;
                    EstadoCita estado = estados.get(cita.getIdEstado());
                    return new CitaAgendaDTO(
                            cita.getId(),
                            cita.getIdVeterinario(),
                            vets.getOrDefault(cita.getIdVeterinario(), "Por asignar"),
                            cliente != null ? cliente.getId().value() : null,
                            cliente != null ? cliente.getNombreCompleto() : "Desconocido",
                            cliente != null ? cliente.getDocumentoIdentidad() : "-",
                            cita.getIdMascota(),
                            mascota != null ? mascota.getNombre() : "Desconocida",
                            servicios.getOrDefault(cita.getIdServicio(), "Desconocido"),
                            cita.getMotivoConsulta(),
                            cita.getFechaCita() != null ? cita.getFechaCita().toString() : "-",
                            cita.getHoraInicio() != null ? cita.getHoraInicio().toString() : "-",
                            cita.getHoraFin() != null ? cita.getHoraFin().toString() : "-",
                            estado != null ? estado.getNombre() : "Desconocido",
                            estado != null ? estado.getColorUi() : "#6C757D",
                            cita.getCostoUsd(),
                            cita.getCostoBs()
                    );
                })
                .toList();
    }

    private Factura guardarFacturaYPago(Cita cita, Servicio servicio, Integer idCliente,
                                        MetodoPago metodoPago, Personal recepcionista,
                                        String referenciaTransaccion, Map<String, String> datosPago) {
        Factura factura = new Factura();
        factura.setIdCliente(idCliente);
        factura.setIdCita(cita.getId());
        factura.setIdPersonal(cita.getIdVeterinario());
        factura.setNumeroControl(generarNumeroControl());
        factura.setSubtotal(cita.getCostoUsd());
        factura.setPorcentajeDescuento(BigDecimal.ZERO);
        factura.setPorcentajeIva(new BigDecimal("16.00"));
        factura.setEstadoFactura("Emitida");
        factura.setMetodoPagoPrincipal(metodoPago.getNombre());
        factura = facturaRepo.guardar(factura);

        DetalleFactura detalle = new DetalleFactura();
        detalle.setIdFactura(factura.getId());
        detalle.setTipoItem("Servicio_Consulta");
        detalle.setIdReferencia(cita.getIdServicio());
        detalle.setDescripcion(servicio.getNombre());
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(cita.getCostoUsd());
        detalle.setDescuentoAplicado(BigDecimal.ZERO);
        detalleFacturaRepo.guardar(detalle);

        Pago pago = new Pago();
        pago.setIdFactura(factura.getId());
        pago.setIdMetodoPago(metodoPago.getId());
        pago.setIdCliente(idCliente);
        pago.setMonto(factura.getTotalNeto());
        pago.setFechaPago(LocalDateTime.now());
        pago.setReferenciaTransaccion(referenciaTransaccion);
        pago.setEstadoPago("Confirmado");
        // ⚠️ Si tu entidad Pago no tiene estos setters, borra las 2 líneas (columnas nullable).
        pago.setVerificadoPor(recepcionista != null ? recepcionista.getPersonalId().value() : null);
        pago.setFechaVerificacion(LocalDateTime.now());
        pago.setMetadataJson(datosPago != null ? new LinkedHashMap<>(datosPago) : Map.of());
        pagoRepo.guardar(pago);

        return factura;
    }

    private AgendarMostradorResponseDTO.ResumenMostradorDTO construirResumen(
            Cliente cliente, Mascota mascota, Personal vet, Servicio servicio,
            Cita cita, MetodoPago metodoPago) {
        return new AgendarMostradorResponseDTO.ResumenMostradorDTO(
                cliente.getNombreCompleto(),
                cliente.getDocumentoIdentidad(),
                mascota.getNombre(),
                vet.getNombreCompleto(),
                servicio.getNombre(),
                cita.getFechaCita().toString(),
                cita.getHoraInicio().toString(),
                cita.getHoraFin().toString(),
                cita.getCostoUsd(),
                cita.getCostoBs(),
                cita.getTasaCambioAplicada(),
                metodoPago.getNombre()
        );
    }

    private String nombreEstadoPorId(Integer idEstado) {
        return estadoCitaRepo.buscarTodos().stream()
                .filter(e -> e.getId().equals(idEstado))
                .findFirst().map(EstadoCita::getNombre)
                .orElse("Desconocido");
    }

    private Map<Integer, String> cargarNombresVeterinarios(List<Cita> citas) {
        Set<Personal.PersonalId> ids = citas.stream()
                .map(Cita::getIdVeterinario)
                .filter(Objects::nonNull)
                .map(Personal.PersonalId::new)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        return personalRepo.findAllByIds(ids).stream()
                .collect(Collectors.toMap(p -> p.getPersonalId().value(), Personal::getNombreCompleto));
    }

    private Map<Integer, String> cargarNombresServicios(List<Cita> citas) {
        Set<Integer> ids = citas.stream()
                .map(Cita::getIdServicio)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        return servicioRepo.buscarPorIds(ids).stream()
                .collect(Collectors.toMap(Servicio::getId, Servicio::getNombre));
    }

    private String generarNumeroControl() {
        LocalDate hoy = LocalDate.now();
        String fechaStr = hoy.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long correlativo = facturaRepo.contarPorFecha(hoy) + 1;
        return String.format("%s-%s-%06d", PREFIJO_FACTURA, fechaStr, correlativo);
    }

    private void validarDatosPago(MetodoPago metodoPago, Map<String, String> datosPago) {
        Map<String, String> requeridos = metodoPago.getDatosRequeridos();
        if (requeridos == null || requeridos.isEmpty()) return;
        if (datosPago == null) {
            throw new FacturacionException("Debe proporcionar los datos requeridos para el método de pago: "
                    + metodoPago.getNombre());
        }
        for (String campo : requeridos.keySet()) {
            String valor = datosPago.get(campo);
            if (valor == null || valor.isBlank()) {
                throw new FacturacionException("El campo '" + campo + "' es obligatorio para "
                        + metodoPago.getNombre());
            }
        }
    }
}