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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlmacenApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AlmacenApplicationService.class);

    private final ProductoRepository productoRepo;
    private final ProveedorRepository proveedorRepo;
    private final MovimientoInventarioRepository movimientoRepo;
    private final CategoriaProductoRepository categoriaRepo;
    private final PersonalPort personalPort;

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarProductos(String filtro) {
        List<Producto> productos = productoRepo.buscarActivos(filtro);
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
                .map(p -> new ProductoDTO(
                        p.getId().value(), p.getCodigoSku(), p.getNombre(),
                        categorias.getOrDefault(p.getIdCategoria(), "—"),
                        p.getUnidadMedida(), p.getPrecioVenta(),
                        p.getStockActual(), p.getRequiereReceta()))
                .toList();
    }

    @Transactional
    public void registrarEntrada(RegistrarEntradaRequestDTO request) {
        // 1. Validar proveedor
        if (proveedorRepo.findById(new Proveedor.ProveedorId(request.idProveedor())).isEmpty()) {
            throw new ProveedorNoEncontradoException(request.idProveedor());
        }

        // 2. Obtener ID del personal autenticado desde el JWT
        Integer usuarioId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer idPersonal = personalPort.obtenerIdPersonalPorUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalStateException("No se pudo identificar al personal autenticado"));

        // 3. Procesar cada línea de la entrada
        for (var linea : request.lineas()) {
            Producto producto = productoRepo.findById(new Producto.ProductoId(linea.idProducto()))
                    .orElseThrow(() -> new ProductoNoEncontradoException(linea.idProducto()));

            if (!producto.getActivo()) {
                throw new EntradaInvalidaException();
            }

            // Regla de negocio: agregar stock
            producto.agregarStock(linea.cantidadRecibida());
            productoRepo.guardar(producto);

            // Registrar el movimiento de inventario
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setIdProducto(producto.getId().value());
            movimiento.setIdPersonal(idPersonal);
            movimiento.setTipoMovimiento(MovimientoInventario.TIPO_ENTRADA);
            movimiento.setCantidad(linea.cantidadRecibida());
            movimiento.setMotivo(MovimientoInventario.MOTIVO_COMPRA);
            movimiento.setDocumentoReferencia(request.numeroFactura());
            movimiento.setFechaMovimiento(LocalDateTime.now());
            
            movimientoRepo.guardar(movimiento);
        }
        log.info("Entrada de inventario registrada por personal ID: {} con {} líneas", idPersonal, request.lineas().size());
    }

    @Transactional
    public ProductoDTO crearProducto(CrearProductoRequestDTO request) {
        if (productoRepo.existePorSku(request.codigoSku())) {
            throw new SkuDuplicadoException(request.codigoSku());
        }

        Integer idCategoria = categoriaRepo.buscarPorNombre(request.categoria())
                .map(c -> c.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + request.categoria()));

        Producto nuevo = new Producto();
        nuevo.setCodigoSku(request.codigoSku());
        nuevo.setNombre(request.nombre());
        nuevo.setDescripcion(request.descripcion());
        nuevo.setIdCategoria(idCategoria);
        nuevo.setUnidadMedida(request.presentacion());
        nuevo.setPrecioVenta(request.precioVenta());
        nuevo.setCostoAdquisicion(request.costoAdquisicion() != null ? request.costoAdquisicion() : request.precioVenta());
        nuevo.setStockActual(request.stockActual());
        nuevo.setStockMinimo(request.stockMinimo());
        nuevo.setStockMaximo(request.stockMaximo());
        nuevo.setRequiereReceta(request.requiereReceta() != null ? request.requiereReceta() : false);
        nuevo.setActivo(true);
        nuevo.setIdProveedorPredeterminado(request.idProveedorPredeterminado());

        Producto guardado = productoRepo.guardar(nuevo);

        return new ProductoDTO(
            guardado.getId().value(), guardado.getCodigoSku(), guardado.getNombre(),
            request.categoria(), guardado.getUnidadMedida(), guardado.getPrecioVenta(),
            guardado.getStockActual(), guardado.getRequiereReceta()
        );
    }

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

    @Transactional(readOnly = true)
    public List<ProveedorDTO> listarProveedores() {
        return proveedorRepo.buscarActivos().stream()
                .map(p -> new ProveedorDTO(
                        p.getId().value(),
                        p.getRif(),
                        p.getNombreEmpresa(),
                        "Contacto", // Puedes ajustar esto si agregas el campo al dominio
                        "0000-0000000", // Placeholder hasta que el dominio tenga telefono
                        "email@ejemplo.com", // Placeholder
                        "Mixto", // Placeholder
                        p.getActivo()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoInventarioDTO> listarMovimientosRecientes() {
        // Nota: Para un JOIN real con nombres, idealmente se hace en una query nativa o proyección.
        // Por simplicidad y para que compile ya, devolvemos los datos base.
        return movimientoRepo.buscarRecientes(10).stream()
                .map(m -> new MovimientoInventarioDTO(
                        m.getId().value(),
                        m.getIdProducto(),
                        "Producto", // Se puede mejorar con una query que haga JOIN
                        m.getTipoMovimiento(),
                        m.getMotivo(),
                        m.getCantidad(),
                        m.getFechaMovimiento(),
                        m.getDocumentoReferencia(),
                        "Personal" // Se puede mejorar con una query que haga JOIN
                ))
                .toList();
    }
}