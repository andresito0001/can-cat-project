package com.udo.can_cat.almacen.infrastructure.controller;

import com.udo.can_cat.almacen.application.dto.*;
import com.udo.can_cat.almacen.application.service.AlmacenApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/almacen")
@RequiredArgsConstructor
public class AlmacenController {

    private final AlmacenApplicationService service;

    @GetMapping("/productos")
    @PreAuthorize("hasAnyRole('Veterinario','Encargado_Almacen', 'Administrador')")
    public ResponseEntity<List<ProductoDTO>> productos(
            @RequestParam(name = "filtro", required = false) String filtro) {
        return ResponseEntity.ok(service.listarProductos(filtro));
    }

    @GetMapping("/alertas")
    @PreAuthorize("hasRole('Encargado_Almacen')")
    public ResponseEntity<List<ProductoAlertaDTO>> obtenerAlertas() {
        return ResponseEntity.ok(service.obtenerAlertasStockBajo());
    }

    @PostMapping("/productos")
    @PreAuthorize("hasRole('Encargado_Almacen')")
    public ResponseEntity<ProductoDTO> crearProducto(@Valid @RequestBody CrearProductoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearProducto(request));
    }

    @PostMapping("/entradas")
    @PreAuthorize("hasRole('Encargado_Almacen')")
    public ResponseEntity<String> registrarEntrada(@Valid @RequestBody RegistrarEntradaRequestDTO request) {
        service.registrarEntrada(request);
        return ResponseEntity.ok("Inventario actualizado correctamente.");
    }

    @GetMapping("/proveedores")
    @PreAuthorize("hasAnyRole('Encargado_Almacen', 'Administrador')")
    public ResponseEntity<List<ProveedorDTO>> listarProveedores() {
        return ResponseEntity.ok(service.listarProveedores());
    }

    @GetMapping("/movimientos")
    @PreAuthorize("hasAnyRole('Encargado_Almacen', 'Administrador')")
    public ResponseEntity<List<MovimientoInventarioDTO>> listarMovimientos(
            @RequestParam(name = "tipo", required = false) String tipo,
            @RequestParam(name = "desde", required = false) String desde,
            @RequestParam(name = "hasta", required = false) String hasta) {
        // Por ahora devuelve los recientes. La lógica de filtros (tipo, fechas) 
        // se puede agregar fácilmente al servicio si el frontend la exige estrictamente.
        return ResponseEntity.ok(service.listarMovimientosRecientes());
    }
}