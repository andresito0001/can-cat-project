package com.udo.can_cat.citas.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import com.udo.can_cat.citas.application.port.PagoVerificacionPort;
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
import com.udo.can_cat.shared.impuestos.ImpuestosProperties;
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
 *
 * Todas las transiciones de estado DELEGAN a CitaTransitionService para
 * mantener una única fuente de verdad sobre transiciones y roles.
 */
@Service
public class AgendaMostradorApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AgendaMostradorApplicationService.class);
    private static final String PREFIJO_FACTURA = "FC";

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
    private final PagoVerificacionPort pagoVerifPort;             
    private final CitaTransitionService citaTransitionService;    
    private final ImpuestosProperties impuestos;

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
                                             TasaCambioService tasaCambioService,
                                             PagoVerificacionPort pagoVerifPort,
                                             CitaTransitionService citaTransitionService,
                                            ImpuestosProperties impuestos) { 
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
        this.pagoVerifPort = pagoVerifPort;
        this.citaTransitionService = citaTransitionService;
        this.impuestos = impuestos;
    }

    // ================================================================
    // AGENDA (CU paso 2 — rango de fechas)
    // ================================================================

    @Transactional(readOnly = true)
    public List<CitaAgendaDTO> consultarAgenda(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Integer> estadosActivos = estadoCitaRepo.buscarIdsEstadosActivos();
        return mapearCitas(citaRepo.buscarActivasPorRangoFechas(fechaInicio, fechaFin, estadosActivos));
    }

    // ================================================================
    // CITAS PENDIENTES DE PAGO (para "Cobrar en Mostrador")
    // ================================================================
    // Solo las que NO tengan pago online en curso ni confirmado.
    // Las que tienen pago Pendiente_Verificacion van a la pestaña
    // "Pagos por verificar" del recepcionista, NO aquí.

    @Transactional(readOnly = true)
    public List<CitaAgendaDTO> listarPendientesPago() {
        EstadoCita pendiente = estadoCitaRepo.buscarPorNombre("Pendiente_Pago")
                .orElseThrow(() -> new OperacionNoPermitidaException(
                        "Estado 'Pendiente_Pago' no encontrado"));

        return mapearCitas(
                citaRepo.buscarPorIdEstado(pendiente.getId()).stream()
                        // ← FIX 2: excluir citas con pago activo (verificación o confirmado)
                        .filter(c -> !pagoVerifPort.existePagoActivoParaCita(c.getId()))
                        .toList()
        );
    }

    // ================================================================
    // AGENDAR + COBRAR + FACTURAR (CU pasos 3 al 8)
    // ================================================================
    // Este método CREA una cita nueva ya Confirmada (nace con pago). No es
    // una transición, es un alta directa. No hay transición que delegar.

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

        BigDecimal costoBase = servicio.getPrecioUsd();
        BigDecimal costoUsd  = costoBase.multiply(impuestos.getFactorIva())
                                        .setScale(2, RoundingMode.HALF_UP);
        BigDecimal costoBs   = costoUsd.multiply(tasa).setScale(2, RoundingMode.HALF_UP);

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
        cita.setPorcentajeIva(impuestos.getIvaPorcentaje());


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
    // Cobra presencialmente una cita que estaba Pendiente_Pago SIN pago
    // online en curso (esas las filtra listarPendientesPago()).
    // Delega la transición a CitaTransitionService.

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

        // Defensa: no permitir cobrar dos veces una cita con pago activo
        if (pagoVerifPort.existePagoActivoParaCita(idCita)) {
            throw new OperacionNoPermitidaException(
                    "Esta cita ya tiene un pago registrado. Ve a 'Pagos por verificar' " +
                    "para validarlo, o contacta al administrador.");
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

        // Crear factura + pago Confirmado ANTES de la transición
        Factura factura = guardarFacturaYPago(cita, servicio, cliente.getId().value(),
                metodoPago, recepcionista, request.referenciaTransaccion(), request.datosPago());

        // Nota en observaciones (antes de la transición)
        String nota = "Cobrada en mostrador (" + metodoPago.getNombre() + ") por "
                + (recepcionista != null ? recepcionista.getNombreCompleto() : "recepción");
        cita.setObservacionesRecepcion(cita.getObservacionesRecepcion() != null
                ? cita.getObservacionesRecepcion() + "\n" + nota : nota);
        citaRepo.guardar(cita);

        // ─── Transición vía CitaTransitionService ───
        // El transition service validará: rol, transición válida, y que
        // exista un pago Confirmado para la cita (que ya creamos arriba).
        Integer personalId = recepcionista != null
                ? recepcionista.getPersonalId().value() : null;
        citaTransitionService.transicionar(
                idCita,
                CitaTransitionService.CONFIRMADA,
                CitaTransitionService.Actor.recepcionista(personalId)
        );

        log.info("Cobro mostrador: cita={} → Confirmada, factura={}",
                idCita, factura.getNumeroControl());

        return new AgendarMostradorResponseDTO(
                cita.getId(), "Confirmada", factura.getId(), factura.getNumeroControl(),
                "Cita cobrada y facturada correctamente.",
                construirResumen(cliente, mascota, vet, servicio, cita, metodoPago));
    }

    // ================================================================
    // CAMBIO DE ESTADO MANUAL (recepción)
    // ================================================================
    // Delega toda la validación de transiciones y roles al
    // CitaTransitionService. Ya no mantiene su propia matriz.

    @Transactional
    public CitaEstadoResponseDTO cambiarEstado(Integer idCita, String estadoDestino) {
        // Validar que la cita existe (para poder dar un 404 limpio antes de la transición)
        citaRepo.buscarPorId(idCita)
                .orElseThrow(() -> new CitaNoEncontradaException("Cita no encontrada con ID: " + idCita));

        Personal recepcionista = resolverPersonalActual();
        Integer personalId = recepcionista != null
                ? recepcionista.getPersonalId().value() : null;

        // Delegar la transición (valida destino, rol y precondiciones)
        citaTransitionService.transicionar(
                idCita,
                normalizarEstado(estadoDestino),
                CitaTransitionService.Actor.recepcionista(personalId)
        );

        // Nota de auditoría en observaciones
        Cita cita = citaRepo.buscarPorId(idCita).orElseThrow();
        String nota = "Estado cambiado a " + estadoDestino + " por "
                + (recepcionista != null ? recepcionista.getNombreCompleto() : "recepción")
                + " (" + LocalDate.now() + ")";
        cita.setObservacionesRecepcion(cita.getObservacionesRecepcion() != null
                ? cita.getObservacionesRecepcion() + "\n" + nota : nota);
        citaRepo.guardar(cita);

        return new CitaEstadoResponseDTO(idCita, estadoDestino,
                "Estado actualizado a '" + estadoDestino + "'.");
    }

    /**
     * Normaliza un estado destino recibido como String al valor canónico
     * que espera el CitaTransitionService. Lanza 400 si el estado es inválido.
     */
    private String normalizarEstado(String estadoDestino) {
        if (estadoDestino == null || estadoDestino.isBlank()) {
            throw new OperacionNoPermitidaException("El estado destino es obligatorio");
        }
        return switch (estadoDestino.trim()) {
            case "Confirmada"   -> CitaTransitionService.CONFIRMADA;
            case "En_Atencion"  -> CitaTransitionService.EN_ATENCION;
            case "Completada"   -> CitaTransitionService.COMPLETADA;
            case "Cancelada"    -> CitaTransitionService.CANCELADA;
            default -> throw new OperacionNoPermitidaException(
                    "Estado destino inválido: '" + estadoDestino + "'. " +
                    "Valores permitidos: Confirmada, En_Atencion, Completada, Cancelada.");
        };
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

                    // ─── Desglose IVA (snapshot de la cita, con fallback al % vigente) ───
                    BigDecimal ivaPct = cita.getPorcentajeIva() != null
                            ? cita.getPorcentajeIva()
                            : impuestos.getIvaPorcentaje();

                    BigDecimal totalConIva = cita.getCostoUsd() != null
                            ? cita.getCostoUsd()
                            : BigDecimal.ZERO;

                    BigDecimal factor = BigDecimal.ONE.add(
                            ivaPct.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
                    );

                    BigDecimal subtotal = totalConIva.divide(factor, 2, RoundingMode.HALF_UP);
                    BigDecimal ivaMonto = totalConIva.subtract(subtotal);

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
                            cita.getCostoBs(),
                            subtotal,
                            ivaMonto,
                            ivaPct
                    );
                })
                .toList();
    }

    private Factura guardarFacturaYPago(Cita cita, Servicio servicio, Integer idCliente,
                                        MetodoPago metodoPago, Personal recepcionista,
                                        String referenciaTransaccion, Map<String, String> datosPago) {
        
        BigDecimal totalCita = cita.getCostoUsd();
        BigDecimal subtotal  = impuestos.extraerSubtotalDeTotal(totalCita);
        
        Factura factura = new Factura();
        factura.setIdCliente(idCliente);
        factura.setIdCita(cita.getId());
        factura.setIdPersonal(cita.getIdVeterinario());
        factura.setNumeroControl(generarNumeroControl());
        factura.setSubtotal(subtotal);
        factura.setPorcentajeDescuento(BigDecimal.ZERO);
        factura.setPorcentajeIva(impuestos.getIvaPorcentaje());
        factura.setEstadoFactura("Emitida");
        factura.setMetodoPagoPrincipal(metodoPago.getNombre());
        factura = facturaRepo.guardar(factura);

        DetalleFactura detalle = new DetalleFactura();
        detalle.setIdFactura(factura.getId());
        detalle.setTipoItem("Servicio_Consulta");
        detalle.setIdReferencia(cita.getIdServicio());
        detalle.setDescripcion(servicio.getNombre());
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(subtotal);
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
        pago.setVerificadoPor(recepcionista != null ? recepcionista.getPersonalId().value() : null);
        pago.setFechaVerificacion(LocalDateTime.now());
        pago.setMetadataJson(datosPago != null ? new LinkedHashMap<>(datosPago) : Map.of());
        pagoRepo.guardar(pago);

        return factura;
    }

    private AgendarMostradorResponseDTO.ResumenMostradorDTO construirResumen(
            Cliente cliente, Mascota mascota, Personal vet, Servicio servicio,
            Cita cita, MetodoPago metodoPago) {

        // ─── Desglose IVA ───
        BigDecimal ivaPct = cita.getPorcentajeIva() != null
                ? cita.getPorcentajeIva()
                : impuestos.getIvaPorcentaje();

        BigDecimal totalConIva = cita.getCostoUsd() != null
                ? cita.getCostoUsd()
                : BigDecimal.ZERO;

        BigDecimal factor = BigDecimal.ONE.add(
                ivaPct.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
        );

        BigDecimal subtotal = totalConIva.divide(factor, 2, RoundingMode.HALF_UP);
        BigDecimal ivaMonto = totalConIva.subtract(subtotal);

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
                subtotal,               
                ivaMonto,               
                ivaPct,                 
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