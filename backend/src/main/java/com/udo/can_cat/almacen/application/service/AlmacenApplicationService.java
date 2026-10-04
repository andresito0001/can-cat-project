package com.udo.can_cat.almacen.application.service;

import com.udo.can_cat.almacen.application.dto.*;
import com.udo.can_cat.almacen.domain.entity.*;
import com.udo.can_cat.almacen.domain.exception.*;
import com.udo.can_cat.almacen.domain.repository.*;
import com.udo.can_cat.almacen.application.port.PersonalPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlmacenApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AlmacenApplicationService.class);
    private static final String PREFIJO_ORDEN = "ENT";

    private final ProductoRepository productoRepo;
    private final ProveedorRepository proveedorRepo;
    private final MovimientoInventarioRepository movimientoRepo;
    private final CategoriaProductoRepository categoriaRepo;
    private final CompraProveedorRepository compraProveedorRepo;
    private final DetalleCompraRepository detalleCompraRepo;
    private final PersonalPort personalPort;

    // ═══════════════════════════════════════════════════════════════
    // LISTAR PRODUCTOS
    // ═══════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarProductos(String filtro, boolean incluirInactivos) {
        List<Producto> productos = incluirInactivos
                ? productoRepo.buscarTodos(filtro)
                : productoRepo.buscarActivos(filtro);
        if (productos.isEmpty()) return List.of();

        Set<Integer> idsCategoria = productos.stream()
                .map(Producto::getIdCategoria)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, String> categorias = new HashMap<>();
        for (Integer id : idsCategoria) {
            categorias.put(id, productoRepo.nombreCategoria(id));
        }

        return productos.stream()
                .map(p -> toDTO(p, categorias.getOrDefault(p.getIdCategoria(), "—")))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoDTO obtenerProducto(Integer id) {
        Producto p = productoRepo.findById(new Producto.ProductoId(id))
                .orElseThrow(() -> new ProductoNoEncontradoException(id));
        String categoria = p.getIdCategoria() != null
                ? productoRepo.nombreCategoria(p.getIdCategoria())
                : "—";
        return toDTO(p, categoria);
    }

    // ═══════════════════════════════════════════════════════════════
    // CREAR PRODUCTO (con SKU autogenerado)
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public ProductoDTO crearProducto(CrearProductoRequestDTO request) {
        // 1. Resolver SKU: autogenerar si no viene
        String sku = (request.codigoSku() == null || request.codigoSku().isBlank())
                ? generarSku(request.categoria())
                : request.codigoSku().trim().toUpperCase();

        // 2. Validar unicidad
        if (productoRepo.existePorSku(sku)) {
            throw new SkuDuplicadoException(sku);
        }

        // 3. Resolver categoría
        Integer idCategoria = categoriaRepo.buscarPorNombre(request.categoria())
                .map(c -> c.getId().value())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Categoría no encontrada: " + request.categoria()));

        // 4. Construir entidad
        Producto nuevo = new Producto();
        nuevo.setCodigoSku(sku);
        nuevo.setNombre(request.nombre().trim());
        nuevo.setDescripcion(request.descripcion());
        nuevo.setIdCategoria(idCategoria);
        nuevo.setUnidadMedida(request.presentacion());
        nuevo.setPrecioVenta(request.precioVenta());
        nuevo.setCostoAdquisicion(request.costoAdquisicion() != null
                ? request.costoAdquisicion() : request.precioVenta());
        nuevo.setStockActual(request.stockActual());
        nuevo.setStockMinimo(request.stockMinimo());
        nuevo.setStockMaximo(request.stockMaximo());
        nuevo.setRequiereReceta(Boolean.TRUE.equals(request.requiereReceta()));
        nuevo.setActivo(true);
        nuevo.setIdProveedorPredeterminado(request.idProveedorPredeterminado());

        Producto guardado = productoRepo.guardar(nuevo);
        log.info("Producto creado: SKU={}, nombre={}", sku, guardado.getNombre());

        return toDTO(guardado, request.categoria());
    }

    /**
     * Genera un SKU único con prefijo por categoría y correlativo de 4 dígitos.
     * Ejemplos: MED-0001, ALI-0003, ACC-0012.
     */
    private String generarSku(String categoria) {
        String prefijo = switch (categoria) {
            case "Medicamento" -> "MED";
            case "Alimento"    -> "ALI";
            case "Accesorio"   -> "ACC";
            case "Servicio"    -> "SRV";
            default            -> "PRD";
        };

        String prefijoConGuion = prefijo + "-";
        long siguiente = productoRepo.contarPorPrefijo(prefijoConGuion) + 1;

        // Salvaguarda: si por alguna razón el correlativo se repite, avanzar
        String sku;
        do {
            sku = String.format("%s-%04d", prefijo, siguiente++);
        } while (productoRepo.existePorSku(sku));

        return sku;
    }

    // ═══════════════════════════════════════════════════════════════
    // ACTUALIZAR PRODUCTO
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public ProductoDTO actualizarProducto(Integer id, ActualizarProductoRequestDTO request) {
        Producto producto = productoRepo.findById(new Producto.ProductoId(id))
                .orElseThrow(() -> new ProductoNoEncontradoException(id));

        producto.setNombre(request.nombre().trim());
        producto.setDescripcion(request.descripcion());

        Integer idCategoria = categoriaRepo.buscarPorNombre(request.categoria())
                .map(c -> c.getId().value())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Categoría no encontrada: " + request.categoria()));
        producto.setIdCategoria(idCategoria);

        producto.setUnidadMedida(request.presentacion());
        producto.setPrecioVenta(request.precioVenta());
        producto.setCostoAdquisicion(request.costoAdquisicion() != null
                ? request.costoAdquisicion() : request.precioVenta());
        producto.setStockMinimo(request.stockMinimo());
        producto.setStockMaximo(request.stockMaximo());
        producto.setRequiereReceta(Boolean.TRUE.equals(request.requiereReceta()));
        producto.setIdProveedorPredeterminado(request.idProveedorPredeterminado());

        productoRepo.guardar(producto);
        return toDTO(producto, request.categoria());
    }

    @Transactional
    public void cambiarEstadoProducto(Integer id, boolean activo) {
        Producto producto = productoRepo.findById(new Producto.ProductoId(id))
                .orElseThrow(() -> new ProductoNoEncontradoException(id));
        productoRepo.cambiarEstado(producto.getId(), activo);
    }

    // ═══════════════════════════════════════════════════════════════
    // REGISTRAR ENTRADA — con CompraProveedor + DetalleCompra + Movimientos
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public EntradaResponseDTO registrarEntrada(RegistrarEntradaRequestDTO request) {
        // 1. Validar proveedor
        Proveedor proveedor = proveedorRepo.findById(new Proveedor.ProveedorId(request.idProveedor()))
                .orElseThrow(() -> new ProveedorNoEncontradoException(request.idProveedor()));

        // 2. Obtener ID del personal autenticado
        Integer usuarioId = (Integer) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Integer idPersonal = personalPort.obtenerIdPersonalPorUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo identificar al personal autenticado"));

        // 3. Validar todas las líneas + acumular totales
        List<Producto> productosPorLinea = new ArrayList<>(request.lineas().size());
        BigDecimal montoTotal = BigDecimal.ZERO;
        int totalUnidades = 0;

        for (var linea : request.lineas()) {
            Producto producto = productoRepo.findById(new Producto.ProductoId(linea.idProducto()))
                    .orElseThrow(() -> new ProductoNoEncontradoException(linea.idProducto()));

            if (!Boolean.TRUE.equals(producto.getActivo())) {
                throw new EntradaInvalidaException();
            }

            productosPorLinea.add(producto);
            BigDecimal subtotal = linea.precioUnitario()
                    .multiply(BigDecimal.valueOf(linea.cantidadRecibida()));
            montoTotal = montoTotal.add(subtotal);
            totalUnidades += linea.cantidadRecibida();
        }

        // 4. Resolver la factura del proveedor (opcional)
        //    Si el usuario no la proporciona, generamos un placeholder trazable.
        LocalDate hoy = LocalDate.now();
        long correlativo = compraProveedorRepo.contarPorFechaOrden(hoy) + 1;

        String facturaProveedor = (request.numeroFactura() != null 
                && !request.numeroFactura().isBlank())
            ? request.numeroFactura().trim()
            : "SIN-FACTURA-" + hoy.format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + String.format("%04d", correlativo);

        // 5. Autogenerar número de orden interno
        String numeroOrden = String.format("%s-%s-%04d", PREFIJO_ORDEN,
                hoy.format(DateTimeFormatter.BASIC_ISO_DATE), correlativo);

        // 6. Crear cabecera de compra
        CompraProveedor compra = new CompraProveedor();
        compra.setIdProveedor(proveedor.getId().value());
        compra.setIdPersonal(idPersonal);
        compra.setNumeroOrden(numeroOrden);
        compra.setNumeroFacturaProveedor(facturaProveedor);   // ← persistir
        compra.setFechaOrden(hoy);
        compra.setFechaRecepcion(request.fechaRecepcion());
        compra.setEstadoCompra("Recibida_Total");
        compra.setMontoTotal(montoTotal);
        compra.setObservacionesRecepcion(request.observaciones());
        CompraProveedor compraGuardada = compraProveedorRepo.guardar(compra);

        // 7. Procesar cada línea: sumar stock + DetalleCompra + Movimiento
        for (int i = 0; i < request.lineas().size(); i++) {
            var linea = request.lineas().get(i);
            Producto producto = productosPorLinea.get(i);

            // 7.1 Sumar stock
            producto.agregarStock(linea.cantidadRecibida());
            productoRepo.guardar(producto);

            // 7.2 Detalle de la compra
            DetalleCompra detalle = new DetalleCompra();
            detalle.setIdCompra(compraGuardada.getId().value());
            detalle.setIdProducto(producto.getId().value());
            detalle.setCantidadSolicitada(linea.cantidadRecibida());
            detalle.setCantidadRecibida(linea.cantidadRecibida());
            detalle.setPrecioUnitario(linea.precioUnitario());
            detalle.setNumeroLote(linea.numeroLote());
            detalle.setFechaVencimientoLote(linea.fechaVencimientoLote());
            detalleCompraRepo.guardar(detalle);

            // 7.3 Movimiento de inventario (referencia = factura del proveedor, más útil en auditoría)
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setIdProducto(producto.getId().value());
            movimiento.setIdPersonal(idPersonal);
            movimiento.setTipoMovimiento(MovimientoInventario.TIPO_ENTRADA);
            movimiento.setCantidad(linea.cantidadRecibida());
            movimiento.setMotivo(MovimientoInventario.MOTIVO_COMPRA);
            movimiento.setDocumentoReferencia(facturaProveedor);
            movimiento.setFechaMovimiento(LocalDateTime.now());
            movimientoRepo.guardar(movimiento);
        }

        log.info("Entrada registrada: orden={}, facturaProv={}, lineas={}, unidades={}, monto={}",
                numeroOrden, facturaProveedor, request.lineas().size(),
                totalUnidades, montoTotal);

        return new EntradaResponseDTO(
                compraGuardada.getId().value(),
                numeroOrden,
                request.lineas().size(),
                totalUnidades,
                montoTotal,
                "Inventario actualizado correctamente."
        );
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ALERTAS
    // ═══════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<ProductoAlertaDTO> obtenerAlertasStockBajo() {
        return productoRepo.buscarConStockBajo().stream()
                .map(p -> new ProductoAlertaDTO(
                        p.getId().value(), p.getCodigoSku(), p.getNombre(),
                        productoRepo.nombreCategoria(p.getIdCategoria()),
                        p.getStockActual(), p.getStockMinimo(),
                        p.getStockMinimo() - p.getStockActual(),
                        (p.getStockActual() == 0) ? "critica" : "baja",
                        p.getActivo()
                ))
                .toList();
    }

    // ═══════════════════════════════════════════════════════════════
    // PROVEEDORES
    // ═══════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<ProveedorDTO> listarProveedores() {
        return proveedorRepo.buscarActivos().stream()
                .map(this::toProveedorDTO)
                .toList();
    }
    
    // ═══════════════════════════════════════════════════════════════
    // MOVIMIENTOS
    // ═══════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<MovimientoInventarioDTO> listarMovimientosRecientes() {
        List<MovimientoInventario> movimientos = movimientoRepo.buscarRecientes(20);
        if (movimientos.isEmpty()) return List.of();

        // Cache de nombres de productos
        Set<Integer> idsProducto = movimientos.stream()
                .map(MovimientoInventario::getIdProducto)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, String> nombresProducto = new HashMap<>();
        for (Integer id : idsProducto) {
            productoRepo.findById(new Producto.ProductoId(id))
                    .ifPresent(p -> nombresProducto.put(id, p.getNombre()));
        }

        return movimientos.stream()
                .map(m -> new MovimientoInventarioDTO(
                        m.getId().value(),
                        m.getIdProducto(),
                        nombresProducto.getOrDefault(m.getIdProducto(), "Producto #" + m.getIdProducto()),
                        m.getTipoMovimiento(),
                        m.getMotivo(),
                        m.getCantidad(),
                        m.getFechaMovimiento(),
                        m.getDocumentoReferencia(),
                        null // nombrePersonal: se puede enriquecer en Fase 3
                ))
                .toList();
    }

    // ═══════════════════════════════════════════════════════════════
    // CRUD PROVEEDORES
    // ═══════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<ProveedorDTO> listarTodosProveedores() {
        return proveedorRepo.findAll().stream()
                .map(this::toProveedorDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProveedorDTO obtenerProveedor(Integer id) {
        Proveedor p = proveedorRepo.findById(new Proveedor.ProveedorId(id))
                .orElseThrow(() -> new ProveedorNoEncontradoException(id));
        return toProveedorDTO(p);
    }

    @Transactional
    public ProveedorDTO crearProveedor(CrearProveedorRequestDTO request) {
        String rif = request.rif().trim().toUpperCase();

        if (!Proveedor.esRifValido(rif)) {
            throw new EntradaInvalidaException("RIF inválido. Formato esperado: J-12345678-9");
        }
        if (proveedorRepo.existePorRif(rif)) {
            throw new EntradaInvalidaException("Ya existe un proveedor con el RIF " + rif);
        }

        Proveedor nuevo = new Proveedor();
        nuevo.setRif(rif);
        nuevo.setNombreEmpresa(request.nombreEmpresa().trim());
        nuevo.setNombreContacto(limpiar(request.nombreContacto()));
        nuevo.setTelefono(limpiar(request.telefono()));
        nuevo.setCorreo(limpiar(request.correo()));
        nuevo.setDireccion(limpiar(request.direccion()));
        nuevo.setTipoSuministro(request.tipoSuministro());
        nuevo.setActivo(true);

        Proveedor guardado = proveedorRepo.guardar(nuevo);
        log.info("Proveedor creado: id={}, RIF={}, empresa={}",
                guardado.getId().value(), guardado.getRif(), guardado.getNombreEmpresa());
        return toProveedorDTO(guardado);
    }

    @Transactional
    public ProveedorDTO actualizarProveedor(Integer id, ActualizarProveedorRequestDTO request) {
        Proveedor proveedor = proveedorRepo.findById(new Proveedor.ProveedorId(id))
                .orElseThrow(() -> new ProveedorNoEncontradoException(id));

        proveedor.setNombreEmpresa(request.nombreEmpresa().trim());
        proveedor.setNombreContacto(limpiar(request.nombreContacto()));
        proveedor.setTelefono(limpiar(request.telefono()));
        proveedor.setCorreo(limpiar(request.correo()));
        proveedor.setDireccion(limpiar(request.direccion()));
        proveedor.setTipoSuministro(request.tipoSuministro());

        Proveedor guardado = proveedorRepo.guardar(proveedor);
        log.info("Proveedor actualizado: id={}, empresa={}", id, guardado.getNombreEmpresa());
        return toProveedorDTO(guardado);
    }

    @Transactional
    public ProveedorDTO cambiarEstadoProveedor(Integer id, boolean activo) {
        Proveedor proveedor = proveedorRepo.findById(new Proveedor.ProveedorId(id))
                .orElseThrow(() -> new ProveedorNoEncontradoException(id));

        proveedor.setActivo(activo);
        Proveedor guardado = proveedorRepo.guardar(proveedor);
        log.info("Proveedor {} {}: id={}",
                activo ? "activado" : "desactivado", guardado.getNombreEmpresa(), id);
        return toProveedorDTO(guardado);
    }

    private ProveedorDTO toProveedorDTO(Proveedor p) {
        return new ProveedorDTO(
                p.getId() != null ? p.getId().value() : null,
                p.getRif(),
                p.getNombreEmpresa(),
                p.getNombreContacto(),
                p.getTelefono(),
                p.getCorreo(),
                p.getDireccion(),
                p.getTipoSuministro(),
                p.getActivo(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    private String limpiar(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    // ═══════════════════════════════════════════════════════════════
    // MAPPER PRIVADO
    // ═══════════════════════════════════════════════════════════════

    private ProductoDTO toDTO(Producto p, String categoria) {
        return new ProductoDTO(
                p.getId() != null ? p.getId().value() : null,
                p.getCodigoSku(),
                p.getNombre(),
                p.getDescripcion(),
                categoria,
                p.getIdCategoria(),
                p.getUnidadMedida(),
                p.getPrecioVenta(),
                p.getCostoAdquisicion(),
                p.getStockActual(),
                p.getStockMinimo(),
                p.getStockMaximo(),
                p.getRequiereReceta(),
                p.getActivo(),
                p.getIdProveedorPredeterminado(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}