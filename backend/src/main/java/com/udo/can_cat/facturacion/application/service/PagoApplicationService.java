package com.udo.can_cat.facturacion.application.service;

import com.udo.can_cat.facturacion.application.dto.CobroFacturaRequestDTO;
import com.udo.can_cat.facturacion.application.dto.CobroFacturaResponseDTO;
import com.udo.can_cat.facturacion.application.dto.FacturaPendienteDTO;
import com.udo.can_cat.facturacion.application.port.CobroMostradorPort;
import com.udo.can_cat.facturacion.domain.exception.FacturaYaPagadaException;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.udo.can_cat.citas.application.service.CitaTransitionService;
import com.udo.can_cat.citas.domain.entity.Cita;
import com.udo.can_cat.citas.domain.entity.EstadoCita;
import com.udo.can_cat.citas.domain.entity.Servicio;
import com.udo.can_cat.citas.domain.exception.CitaNoEncontradaException;
import com.udo.can_cat.citas.domain.exception.OperacionNoPermitidaException;
import com.udo.can_cat.citas.domain.repository.CitaRepository;
import com.udo.can_cat.citas.domain.repository.EstadoCitaRepository;
import com.udo.can_cat.citas.domain.repository.ServicioRepository;
import com.udo.can_cat.facturacion.application.dto.HistorialPagoResponseDTO;
import com.udo.can_cat.facturacion.application.dto.MetodoPagoDTO;
import com.udo.can_cat.facturacion.application.dto.PagoPendienteVerificacionDTO;
import com.udo.can_cat.facturacion.application.dto.ProcesarPagoCitaRequestDTO;
import com.udo.can_cat.facturacion.application.dto.ProcesarPagoCitaResponseDTO;
import com.udo.can_cat.facturacion.application.dto.VerificarPagoRequestDTO;
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
import com.udo.can_cat.usuarios.domain.entity.Cliente;
import com.udo.can_cat.usuarios.domain.entity.Personal;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import com.udo.can_cat.usuarios.domain.repository.ClienteRepository;
import com.udo.can_cat.usuarios.domain.repository.PersonalRepository;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
import java.util.List;
import java.math.RoundingMode;
import com.udo.can_cat.facturacion.application.dto.EstadisticasCajaHoyDTO;


@Service
public class PagoApplicationService {

    private static final Logger log = LoggerFactory.getLogger(PagoApplicationService.class);
    private static final String PREFIJO_FACTURA = "FC";

    // Estados que manejamos explícitamente
    private static final String ESTADO_CITA_PENDIENTE_PAGO = "Pendiente_Pago";
    private static final String PAGO_PENDIENTE_VERIF     = "Pendiente_Verificacion";
    private static final String PAGO_CONFIRMADO          = "Confirmado";
    private static final String PAGO_RECHAZADO           = "Rechazado";
    private static final String FACTURA_EMITIDA          = "Emitida";
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
    private final UsuarioRepository usuarioRepo;
    private final CobroMostradorPort cobroMostradorPort;
    private final CitaTransitionService citaTransitionService;
    private final ImpuestosProperties impuestos;

    public PagoApplicationService(CitaRepository citaRepo,
                                  EstadoCitaRepository estadoCitaRepo,
                                  ServicioRepository servicioRepo,
                                  MascotaRepository mascotaRepo,
                                  ClienteRepository clienteRepo,
                                  PersonalRepository personalRepo,
                                  FacturaRepository facturaRepo,
                                  DetalleFacturaRepository detalleFacturaRepo,
                                  PagoRepository pagoRepo,
                                  MetodoPagoRepository metodoPagoRepo,
                                  UsuarioRepository usuarioRepository,
                                  CobroMostradorPort cobroMostradorPort,
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
        this.usuarioRepo = usuarioRepository;
        this.cobroMostradorPort = cobroMostradorPort;
        this.citaTransitionService = citaTransitionService;
        this.impuestos = impuestos;
    }

    // ═══════════════════════════════════════════════════
    // LISTAR MÉTODOS DE PAGO ONLINE
    // ═══════════════════════════════════════════════════

    public List<MetodoPagoDTO> listarMetodosOnline() {
        return metodoPagoRepo.buscarActivos().stream()
                .filter(m -> "Transferencia".equals(m.getNombre()) || "Pago_Movil".equals(m.getNombre()))
                .map(m -> new MetodoPagoDTO(
                        m.getId(),
                        m.getNombre(),
                        m.getDescripcion(),
                        m.getDatosRequeridos() != null ? m.getDatosRequeridos() : Map.of()
                ))
                .toList();
    }

    // ═══════════════════════════════════════════════════
    // ESTADÍSTICAS DEL DÍA (KPIs de Caja)
    // ═══════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public EstadisticasCajaHoyDTO estadisticasHoy() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.plusDays(1).atStartOfDay();
        LocalDateTime corte24h = LocalDateTime.now().minusHours(24);

        BigDecimal totalUsd = pagoRepo.sumarPagosConfirmadosEntre(inicio, fin);
        long cantidad = pagoRepo.contarPagosConfirmadosEntre(inicio, fin);
        long pendientes = facturaRepo.contarFacturasEmitidasSinPago();
        long urgentes = facturaRepo.contarFacturasUrgentes(corte24h);
        long porVerificar = pagoRepo.contarPagosPendientesVerificacion();

        return new EstadisticasCajaHoyDTO(
                totalUsd != null ? totalUsd : BigDecimal.ZERO,
                (int) cantidad,
                (int) pendientes,
                (int) urgentes,
                (int) porVerificar
        );
    }

    // ═══════════════════════════════════════════════════
    // PROCESAR PAGO ONLINE DE CITA
    // ═══════════════════════════════════════════════════
    // El pago queda en Pendiente_Verificacion.
    // La cita permanece en Pendiente_Pago.
    // El recepcionista, al verificar (verificarPago), transiciona a Confirmada.

    @Transactional
    public ProcesarPagoCitaResponseDTO procesarPagoCita(ProcesarPagoCitaRequestDTO request) {
        // 1. Identificar cliente
        Integer idCliente = obtenerIdClienteActual();
        if (idCliente == null) {
            throw new OperacionNoPermitidaException("No se pudo identificar al cliente autenticado");
        }

        // 2. Buscar y validar cita
        Cita cita = citaRepo.buscarPorId(request.idCita())
                .orElseThrow(() -> new CitaNoEncontradaException(
                        "Cita no encontrada con ID: " + request.idCita()));

        String estadoActual = obtenerNombreEstado(cita.getIdEstado());
        if (!ESTADO_CITA_PENDIENTE_PAGO.equals(estadoActual)) {
            throw new OperacionNoPermitidaException(
                    "Solo se puede pagar citas en estado 'Pendiente_Pago'. Estado actual: " + estadoActual);
        }

        // Validar que la cita pertenece al cliente
        var clienteId = new Cliente.ClienteId(idCliente);
        boolean pertenece = mascotaRepo.findByClienteId(clienteId).stream()
                .anyMatch(m -> m.getId().value().equals(cita.getIdMascota()));
        if (!pertenece) {
            throw new OperacionNoPermitidaException("La cita no pertenece al cliente autenticado");
        }

        // 3. Validar método de pago
        MetodoPago metodoPago = metodoPagoRepo.buscarPorId(request.idMetodoPago())
                .orElseThrow(() -> new FacturacionException("Método de pago no encontrado"));
        validarDatosPago(metodoPago, request.datosPago());

        if (request.referenciaTransaccion() != null) {
            String ref = request.referenciaTransaccion().trim();
            if (!ref.matches("^\\d{4,20}$")) {
                throw new FacturacionException("La referencia de transacción debe tener entre 4 y 20 dígitos.");
            }
        }

        // 4. Buscar servicio para el detalle
        Servicio servicio = servicioRepo.buscarPorId(cita.getIdServicio())
                .filter(Servicio::getActivo)
                .orElseThrow(() -> new FacturacionException("Servicio de la cita no encontrado"));

        // 5. Generar número de control
        String numeroControl = generarNumeroControl();

        // 6. Crear factura
        BigDecimal totalCita = cita.getCostoUsd();
        BigDecimal subtotal  = impuestos.extraerSubtotalDeTotal(totalCita);
        
        Factura factura = new Factura();
        factura.setIdCliente(idCliente);
        factura.setIdCita(cita.getId());
        factura.setIdPersonal(cita.getIdVeterinario());
        factura.setNumeroControl(numeroControl);
        factura.setSubtotal(subtotal);
        factura.setPorcentajeDescuento(BigDecimal.ZERO);
        factura.setPorcentajeIva(impuestos.getIvaPorcentaje());
        factura.setEstadoFactura(FACTURA_EMITIDA);
        factura.setMetodoPagoPrincipal(metodoPago.getNombre());
        factura = facturaRepo.guardar(factura);

        // 7. Crear detalle de factura
        DetalleFactura detalle = new DetalleFactura();
        detalle.setIdFactura(factura.getId());
        detalle.setTipoItem("Servicio_Consulta");
        detalle.setIdReferencia(cita.getIdServicio());
        detalle.setDescripcion(servicio.getNombre());
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(cita.getCostoUsd());
        detalle.setDescuentoAplicado(BigDecimal.ZERO);
        detalleFacturaRepo.guardar(detalle);

        // 8. Crear pago en Pendiente_Verificacion
        
        Pago pago = new Pago();
        pago.setIdFactura(factura.getId());
        pago.setIdMetodoPago(metodoPago.getId());
        pago.setIdCliente(idCliente);
        pago.setMonto(factura.getTotalNeto());
        pago.setFechaPago(LocalDateTime.now());
        pago.setReferenciaTransaccion(request.referenciaTransaccion());
        pago.setEstadoPago(PAGO_PENDIENTE_VERIF);
        pago.setMetadataJson(request.datosPago() != null
                ? request.datosPago().entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (Object) e.getValue(),
                        (a, b) -> a,
                        LinkedHashMap::new))
                : Map.of());

        pagoRepo.guardar(pago);

        // ─── 9. NO se cambia el estado de la cita ───
        // La cita permanece en Pendiente_Pago hasta que un recepcionista
        // verifique el pago (verificarPago → transiciona a Confirmada).
        // El dinero y la agenda son conceptos independientes.

        log.info("Pago online procesado (pendiente verificación): cita={}, factura={}, numeroControl={}, metodo={}",
                cita.getId(), factura.getId(), numeroControl, metodoPago.getNombre());

        if (cita.getExpiraEn() != null) {
            cita.setExpiraEn(null);
            citaRepo.guardar(cita);
        }
        
        BigDecimal montoUsd = factura.getTotalNeto();
        BigDecimal tasa = cita.getTasaCambioAplicada();
        BigDecimal montoBs = (tasa != null)
                ? montoUsd.multiply(tasa).setScale(2, RoundingMode.HALF_UP)
                : null;

        return new ProcesarPagoCitaResponseDTO(
                factura.getId(),
                numeroControl,
                ESTADO_CITA_PENDIENTE_PAGO,
                "Su pago fue registrado y está siendo verificado. " +
                "Recibirá una confirmación cuando el pago sea aprobado por recepción.",
                true,
                null,
                montoUsd,
                montoBs,
                tasa
        );
    }

    

    // ═══════════════════════════════════════════════════
    // DATOS PARA GENERAR PDF
    // ═══════════════════════════════════════════════════

    public record DatosPdf(
            String numeroControl,
            String fechaEmision,
            String clienteNombre,
            String clienteDocumento,
            String clienteDireccion,
            String clienteCiudad,
            String clienteTelefono,
            String veterinarioNombre,
            String mascotaNombre,
            String motivoConsulta,
            String fechaCita,
            String horaInicio,
            List<DetalleFactura> detalles,
            BigDecimal subtotal,
            BigDecimal porcentajeIva,
            BigDecimal totalNeto,
            String metodoPago,
            String clienteEmail,
            String stadoPago,
            Map<String, Object> metadataPago,
            String referenciaPago
    ) {}

    public DatosPdf obtenerDatosPdf(Integer idFactura) {
        Factura factura = facturaRepo.buscarPorId(idFactura)
                .orElseThrow(() -> new FacturacionException("Factura no encontrada: " + idFactura));

        List<DetalleFactura> detalles = detalleFacturaRepo.buscarPorFacturaId(idFactura);

        Cliente cliente = clienteRepo.findById(new Cliente.ClienteId(factura.getIdCliente()))
                .orElseThrow(() -> new FacturacionException("Cliente no encontrado"));

        String emailCliente = usuarioRepo.findById(cliente.getUsuarioId())
                .map(com.udo.can_cat.usuarios.domain.entity.Usuario::getCorreoElectronico)
                .orElse(null);

        String vetNombre = personalRepo.findById(new Personal.PersonalId(factura.getIdPersonal()))
                .map(Personal::getNombreCompleto)
                .orElse("No asignado");

        Pago pago = pagoRepo.buscarPorFacturaId(idFactura).orElse(null);
        String estadoPago = pago != null ? pago.getEstadoPago() : factura.getEstadoFactura();
        Map<String, Object> metadataPago = pago != null ? pago.getMetadataJson() : null;
        String referenciaPago = pago != null ? pago.getReferenciaTransaccion() : null;

        String mascotaNombre = "-";
        String motivoConsulta = "-";
        String fechaCita = "-";
        String horaInicio = "-";
        if (factura.getIdCita() != null) {
            try {
                Cita cita = citaRepo.buscarPorId(factura.getIdCita()).orElse(null);
                if (cita != null) {
                    motivoConsulta = cita.getMotivoConsulta();
                    fechaCita = cita.getFechaCita() != null ? cita.getFechaCita().toString() : "-";
                    horaInicio = cita.getHoraInicio() != null ? cita.getHoraInicio().toString() : "-";
                    Optional<Mascota> mascotaOpt = mascotaRepo
                            .findByClienteId(new Cliente.ClienteId(cliente.getId().value()))
                            .stream()
                            .filter(m -> m.getId().value().equals(cita.getIdMascota()))
                            .findFirst();
                    mascotaNombre = mascotaOpt.map(Mascota::getNombre).orElse("-");
                }
            } catch (Exception ignored) {}
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return new DatosPdf(
                factura.getNumeroControl(),
                factura.getFechaEmision() != null ? factura.getFechaEmision().format(dtf) : "-",
                cliente.getNombreCompleto(),
                cliente.getDocumentoIdentidad(),
                cliente.getDireccion() != null ? cliente.getDireccion() : "-",
                cliente.getCiudad() != null ? cliente.getCiudad() : "-",
                cliente.getTelefonoPrincipal(),
                vetNombre,
                mascotaNombre,
                motivoConsulta,
                fechaCita,
                horaInicio,
                detalles,
                factura.getSubtotal(),
                // factura.getPorcentajeIva() != null ? factura.getPorcentajeIva() : new BigDecimal("16.00"),
                factura.getPorcentajeIva() != null
                    ? factura.getPorcentajeIva()
                    : impuestos.getIvaPorcentaje(),
                factura.getTotalNeto(),
                factura.getMetodoPagoPrincipal(),
                emailCliente,
                estadoPago,
                metadataPago,
                referenciaPago
        );
    }

    // ═══════════════════════════════════════════════════
    // HISTORIAL DE PAGOS DEL CLIENTE
    // ═══════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<HistorialPagoResponseDTO> obtenerHistorialPagos() {
        Integer idCliente = obtenerIdClienteActual();
        if (idCliente == null) {
            throw new OperacionNoPermitidaException("No se pudo identificar al cliente autenticado");
        }

        List<Factura> facturas = facturaRepo.buscarPorClienteId(idCliente);
        if (facturas.isEmpty()) return List.of();

        return facturas.stream().map(factura -> {
            String estadoCita = null;
            if (factura.getIdCita() != null) {
                estadoCita = citaRepo.buscarPorId(factura.getIdCita())
                        .map(c -> obtenerNombreEstado(c.getIdEstado()))
                        .orElse(null);
            }

            String concepto = detalleFacturaRepo.buscarPorFacturaId(factura.getId()).stream()
                    .findFirst()
                    .map(DetalleFactura::getDescripcion)
                    .orElse("Servicio veterinario");

            Pago pago = pagoRepo.buscarPorFacturaId(factura.getId()).orElse(null);

            LocalDateTime fecha = null;
            if (pago != null && pago.getFechaPago() != null) {
                fecha = pago.getFechaPago();
            } else if (factura.getFechaEmision() != null) {
                fecha = factura.getFechaEmision();
            } else {
                fecha = factura.getCreatedAt();
            }

        BigDecimal montoBs = null;
        Cita citaAsociada = null;
        if (factura.getIdCita() != null) {
        citaAsociada = citaRepo.buscarPorId(factura.getIdCita()).orElse(null);
        if (citaAsociada != null && citaAsociada.getTasaCambioAplicada() != null
                && factura.getTotalNeto() != null) {
                montoBs = factura.getTotalNeto()
                        .multiply(citaAsociada.getTasaCambioAplicada())
                        .setScale(2, RoundingMode.HALF_UP);
        }
        if (citaAsociada != null) {
                estadoCita = obtenerNombreEstado(citaAsociada.getIdEstado());
        }
        }

        return new HistorialPagoResponseDTO(
                factura.getId(),
                factura.getNumeroControl(),
                fecha != null ? fecha.toString() : null,
                concepto,
                factura.getTotalNeto(),
                montoBs,
                pago != null ? pago.getEstadoPago() : factura.getEstadoFactura(),
                factura.getMetodoPagoPrincipal(),
                pago != null ? pago.getReferenciaTransaccion() : null,
                factura.getIdCita(),
                estadoCita
        );
        }).toList();
    }

    // ═══════════════════════════════════════════════════
    // MÉTODOS DE PAGO PRESENCIALES
    // ═══════════════════════════════════════════════════

    public List<MetodoPagoDTO> listarMetodosPresenciales() {
        return metodoPagoRepo.buscarActivos().stream()
                .filter(m -> !"Transferencia".equals(m.getNombre()))
                .map(m -> new MetodoPagoDTO(
                        m.getId(),
                        m.getNombre(),
                        m.getDescripcion(),
                        m.getDatosRequeridos() != null ? m.getDatosRequeridos() : Map.of()
                ))
                .toList();
    }

    // ═══════════════════════════════════════════════════
    // COBRO EN MOSTRADOR
    // ═══════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<FacturaPendienteDTO> listarFacturasPendientes() {
        return cobroMostradorPort.listarFacturasPendientes();
    }

    @Transactional
    public CobroFacturaResponseDTO cobrarFactura(Integer idFactura, CobroFacturaRequestDTO request) {
        Factura factura = facturaRepo.buscarPorId(idFactura)
                .orElseThrow(() -> new FacturacionException("Factura no encontrada: " + idFactura));

        if (!FACTURA_EMITIDA.equals(factura.getEstadoFactura())) {
            throw new FacturacionException("Solo se pueden cobrar facturas en estado 'Emitida' "
                    + "(estado actual: " + factura.getEstadoFactura() + ")");
        }
        if (pagoRepo.buscarPorFacturaId(idFactura).isPresent()) {
            throw new FacturaYaPagadaException(factura.getNumeroControl());   // → 409
        }

        MetodoPago metodoPago = metodoPagoRepo.buscarPorId(request.idMetodoPago())
                .orElseThrow(() -> new FacturacionException("Método de pago no encontrado"));
        if ("Transferencia".equals(metodoPago.getNombre())) {
            throw new FacturacionException("El método 'Transferencia' no es válido para el cobro en mostrador");
        }
        validarDatosPago(metodoPago, request.datosPago());

        // Cachear el verificador (se usa 2 veces)
        Integer verificadorId = obtenerPersonalActual();

        BigDecimal monto = calcularTotal(factura.getSubtotal(), factura.getPorcentajeIva());

        Pago pago = new Pago();
        pago.setIdFactura(factura.getId());
        pago.setIdMetodoPago(metodoPago.getId());
        pago.setIdCliente(factura.getIdCliente());
        pago.setMonto(monto);
        pago.setFechaPago(LocalDateTime.now());
        pago.setReferenciaTransaccion(request.referenciaTransaccion());
        pago.setEstadoPago(PAGO_CONFIRMADO);
        pago.setMetadataJson(request.datosPago() != null
                ? new LinkedHashMap<>(request.datosPago()) : Map.of());
        pago.setVerificadoPor(verificadorId);
        pago.setFechaVerificacion(LocalDateTime.now());
        pagoRepo.guardar(pago);

        // ─── Transicionar la cita a Confirmada SOLO si está Pendiente_Pago ───
        // Si es una factura de insumos de una atención completada (idCita → Completada),
        // no se toca el estado de la cita (la agenda ya avanzó).
        if (factura.getIdCita() != null) {
            String estadoCitaActual = citaRepo.buscarPorId(factura.getIdCita())
                    .map(c -> obtenerNombreEstado(c.getIdEstado()))
                    .orElse(null);
            if (ESTADO_CITA_PENDIENTE_PAGO.equals(estadoCitaActual)) {
                citaTransitionService.transicionar(
                        factura.getIdCita(),
                        CitaTransitionService.CONFIRMADA,
                        CitaTransitionService.Actor.recepcionista(verificadorId)
                );
            }
        }

        factura.setMetodoPagoPrincipal(metodoPago.getNombre());
        facturaRepo.guardar(factura);

        log.info("Factura cobrada en mostrador: idFactura={}, numeroControl={}, metodo={}, monto={}",
                idFactura, factura.getNumeroControl(), metodoPago.getNombre(), monto);

        return new CobroFacturaResponseDTO(
                idFactura, factura.getNumeroControl(), monto, metodoPago.getNombre(),
                PAGO_CONFIRMADO, "Cobro registrado correctamente");
    }

    // ═══════════════════════════════════════════════════
    // VERIFICACIÓN DE PAGOS ONLINE
    // ═══════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<PagoPendienteVerificacionDTO> listarPagosPendientesVerificacion() {
        List<Pago> pagos = pagoRepo.buscarPorEstado(PAGO_PENDIENTE_VERIF);
        if (pagos.isEmpty()) return List.of();
        return pagos.stream().map(this::toPagoVerificacionDTO).toList();
    }

    @Transactional
    public PagoPendienteVerificacionDTO verificarPago(Integer idPago,
                                                       VerificarPagoRequestDTO req) {
        Pago pago = pagoRepo.buscarPorId(idPago)
                .orElseThrow(() -> new FacturacionException("Pago no encontrado: " + idPago));

        if (!PAGO_PENDIENTE_VERIF.equals(pago.getEstadoPago())) {
            throw new FacturacionException("Este pago ya fue procesado (estado actual: "
                    + pago.getEstadoPago() + ")");
        }

        Integer verificadorId = obtenerPersonalActual();
        boolean aprobado = Boolean.TRUE.equals(req.aprobado());

        pago.setEstadoPago(aprobado ? PAGO_CONFIRMADO : PAGO_RECHAZADO);
        pago.setVerificadoPor(verificadorId);
        pago.setFechaVerificacion(LocalDateTime.now());
        pago.setObservacionesVerificacion(req.observaciones());
        pagoRepo.actualizar(pago);

        // ─── Delegar la transición de la cita al Transition Service ───
        Factura factura = facturaRepo.buscarPorId(pago.getIdFactura()).orElse(null);
        if (factura != null && factura.getIdCita() != null) {
            String estadoCitaActual = citaRepo.buscarPorId(factura.getIdCita())
                    .map(c -> obtenerNombreEstado(c.getIdEstado()))
                    .orElse(null);

            // Solo transicionar si la cita está en un estado transicionable.
            // Facturas de insumos de atenciones completadas no deben tocarse.
            if (ESTADO_CITA_PENDIENTE_PAGO.equals(estadoCitaActual)) {
                var actor = CitaTransitionService.Actor.recepcionista(verificadorId);
                citaTransitionService.transicionar(
                        factura.getIdCita(),
                        aprobado ? CitaTransitionService.CONFIRMADA : CitaTransitionService.CANCELADA,
                        actor
                );
            }
        }

        log.info("Pago verificado: idPago={}, aprobado={}, verificador={}",
                idPago, aprobado, verificadorId);

        // Construir DTO del pago ya verificado (no de la lista de pendientes)
        return toPagoVerificacionDTO(pago);
    }

    /**
     * Convierte un Pago a su DTO de verificación, resolviendo factura, cliente y mascota.
     * Reutilizado por listarPagosPendientesVerificacion y verificarPago.
     */
    private PagoPendienteVerificacionDTO toPagoVerificacionDTO(Pago pago) {
        Factura factura = facturaRepo.buscarPorId(pago.getIdFactura()).orElse(null);
        Cliente cliente = factura != null
                ? clienteRepo.findById(new Cliente.ClienteId(factura.getIdCliente())).orElse(null)
                : null;

        String mascotaNombre = null;
        BigDecimal montoBs = null;

        if (factura != null && factura.getIdCita() != null) {
            Cita cita = citaRepo.buscarPorId(factura.getIdCita()).orElse(null);
            if (cita != null) {
                mascotaNombre = mascotaRepo.findById(
                        new com.udo.can_cat.mascotas.domain.entity.Mascota.MascotaId(cita.getIdMascota()))
                        .map(Mascota::getNombre)
                        .orElse(null);

                // Calcular monto en Bs con la tasa aplicada a la cita
                if (cita.getTasaCambioAplicada() != null && pago.getMonto() != null) {
                    montoBs = pago.getMonto()
                            .multiply(cita.getTasaCambioAplicada())
                            .setScale(2, RoundingMode.HALF_UP);
                }
            }
        }

        String metodoNombre = metodoPagoRepo.buscarPorId(pago.getIdMetodoPago())
                .map(MetodoPago::getNombre).orElse("—");

        return new PagoPendienteVerificacionDTO(
                pago.getId(),
                pago.getIdFactura(),
                factura != null ? factura.getNumeroControl() : "—",
                pago.getMonto(),
                montoBs,
                metodoNombre,
                pago.getReferenciaTransaccion(),
                pago.getMetadataJson(),
                pago.getFechaPago(),
                cliente != null ? cliente.getNombreCompleto() : "—",
                cliente != null ? cliente.getDocumentoIdentidad() : "—",
                cliente != null ? cliente.getTelefonoPrincipal() : "—",
                factura != null ? factura.getIdCita() : null,
                mascotaNombre
        );
    }

    // ═══════════════════════════════════════════════════
    // CONTROL DE ACCESO A FACTURAS
    // ═══════════════════════════════════════════════════

    public void validarAccesoFactura(Integer idFactura) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return;
        boolean esCliente = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_Cliente".equals(a.getAuthority()));
        if (!esCliente) return;

        Integer idCliente = obtenerIdClienteActual();
        Factura factura = facturaRepo.buscarPorId(idFactura)
                .orElseThrow(() -> new FacturacionException("Factura no encontrada: " + idFactura));
        if (idCliente == null || !factura.getIdCliente().equals(idCliente)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "No tiene acceso a esta factura");
        }
    }

    // ═══════════════════════════════════════════════════
    // PRIVADOS
    // ═══════════════════════════════════════════════════

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
            throw new FacturacionException("Debe proporcionar los datos requeridos para el método de pago: " + metodoPago.getNombre());
        }

        for (String campo : requeridos.keySet()) {
            String valor = datosPago.get(campo);
            if (valor == null || valor.isBlank()) {
                throw new FacturacionException("El campo '" + campo + "' es obligatorio para " + metodoPago.getNombre());
            }

            switch (campo) {
                case "telefono" -> validarTelefonoVE(valor);
                case "numero_cuenta" -> validarCuentaVE(valor);
                case "ultimos_digitos" -> validarUltimosDigitos(valor);
                case "lote" -> validarLote(valor);
            }
        }
    }


    private void validarTelefonoVE(String valor) {
        String s = valor.replaceAll("[\\s\\-()]", "");
        if (!s.matches("^04\\d{9}$")) {
            throw new FacturacionException("Teléfono inválido. Formato: 04XX-XXXXXXX");
        }
    }

    private void validarCuentaVE(String valor) {
        String s = valor.replaceAll("[\\s\\-]", "");
        if (!s.matches("^\\d{20}$")) {
            throw new FacturacionException("Número de cuenta inválido. Debe tener 20 dígitos.");
        }
    }

    private void validarUltimosDigitos(String valor) {
        if (!valor.matches("^\\d{4}$")) {
            throw new FacturacionException("Los últimos dígitos deben ser exactamente 4 números.");
        }
    }

    private void validarLote(String valor) {
        if (!valor.matches("^\\d{4,10}$")) {
            throw new FacturacionException("El lote debe tener entre 4 y 10 dígitos.");
        }
    }

    private BigDecimal calcularTotal(BigDecimal subtotal, BigDecimal porcentajeIva) {
        BigDecimal st = subtotal != null ? subtotal : BigDecimal.ZERO;
            BigDecimal iva = porcentajeIva != null ? porcentajeIva : impuestos.getIvaPorcentaje();
            return st.multiply(BigDecimal.ONE.add(
                        iva.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP);
    }

    /** Patrón 1.2: recepcionista autenticado (principal Integer = usuarioId). */
    private Integer obtenerPersonalActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        if (!(auth.getPrincipal() instanceof Integer usuarioId)) return null;
        return personalRepo.findByUsuarioId(new UsuarioId(usuarioId))
                .map(p -> p.getPersonalId().value())
                .orElse(null);
    }

    private Integer obtenerIdClienteActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        Integer usuarioId;
        if (auth.getPrincipal() instanceof Integer) {
            usuarioId = (Integer) auth.getPrincipal();
        } else {
            return null;
        }
        return clienteRepo.findByUsuarioId(new UsuarioId(usuarioId))
                .map(c -> c.getId().value())
                .orElse(null);
    }

    private String obtenerNombreEstado(Integer idEstado) {
        return estadoCitaRepo.buscarTodos().stream()
                .filter(e -> e.getId().equals(idEstado))
                .findFirst()
                .map(EstadoCita::getNombre)
                .orElse("Desconocido");
    }
}