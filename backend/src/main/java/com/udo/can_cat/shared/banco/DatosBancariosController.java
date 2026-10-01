package com.udo.can_cat.shared.banco;

import com.udo.can_cat.shared.tasa.TasaCambioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/public")
public class DatosBancariosController {

    private final DatosBancariosProperties props;
    private final TasaCambioService tasaCambioService;

    public DatosBancariosController(DatosBancariosProperties props,
                                    TasaCambioService tasaCambioService) {
        this.props = props;
        this.tasaCambioService = tasaCambioService;
    }

    @GetMapping("/datos-bancarios")
    public ResponseEntity<DatosBancariosDTO> obtener() {
        return ResponseEntity.ok(new DatosBancariosDTO(
                props.getNombreTitular(),
                props.getRif(),
                new DatosBancariosDTO.BancoCuenta(
                        props.getBancoPrincipal().getCodigo(),
                        props.getBancoPrincipal().getNombre(),
                        props.getBancoPrincipal().getCuenta(),
                        props.getBancoPrincipal().getTipo()
                ),
                new DatosBancariosDTO.PagoMovil(
                        props.getPagoMovil().getBanco(),
                        props.getPagoMovil().getTelefono(),
                        props.getPagoMovil().getCedula()
                ),
                props.getHorarioAtencion(),
                props.getNota()
        ));
    }

    /**
     * GET /api/public/tasa-cambio
     * Expone la tasa oficial USD → VES para que el frontend pueda calcular
     * montos en bolívares antes de crear una cita.
     */
    @GetMapping("/tasa-cambio")
    public ResponseEntity<TasaCambioDTO> obtenerTasa() {
        BigDecimal tasa = tasaCambioService.obtenerTasaOficial();
        return ResponseEntity.ok(new TasaCambioDTO(tasa, LocalDateTime.now()));
    }

    public record TasaCambioDTO(BigDecimal tasa, LocalDateTime actualizadaEn) {}
}