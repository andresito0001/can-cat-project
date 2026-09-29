package com.udo.can_cat.shared.banco;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class DatosBancariosController {

    private final DatosBancariosProperties props;

    public DatosBancariosController(DatosBancariosProperties props) {
        this.props = props;
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
}