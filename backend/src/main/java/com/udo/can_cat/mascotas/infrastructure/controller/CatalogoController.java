package com.udo.can_cat.mascotas.infrastructure.controller;

import java.util.List;
import com.udo.can_cat.mascotas.application.dto.EspecieDTO;
import com.udo.can_cat.mascotas.application.dto.RazaDTO;
import com.udo.can_cat.mascotas.application.service.CatalogoApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/especies")
public class CatalogoController {

    private final CatalogoApplicationService catalogoService;

    public CatalogoController(CatalogoApplicationService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    public ResponseEntity<List<EspecieDTO>> listarEspecies() {
        return ResponseEntity.ok(catalogoService.listarEspecies());
    }

    @GetMapping("/{idEspecie}/razas")
    public ResponseEntity<List<RazaDTO>> listarRazasPorEspecie(
            @PathVariable Integer idEspecie) {
        return ResponseEntity.ok(catalogoService.listarRazasPorEspecie(idEspecie));
    }
}