package com.udo.can_cat.atenciones.infrastructure.controller;

import com.udo.can_cat.atenciones.application.dto.RecetaPdfDTO;
import com.udo.can_cat.atenciones.application.service.RecetaApplicationService;
import com.udo.can_cat.atenciones.infrastructure.pdf.RecetaPdfGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recetas")
public class RecetasController {

    private final RecetaApplicationService service;
    private final RecetaPdfGenerator pdfGenerator;

    public RecetasController(RecetaApplicationService service, RecetaPdfGenerator pdfGenerator) {
        this.service = service;
        this.pdfGenerator = pdfGenerator;
    }

    /** D5: PDF descargable del récipe (404 si no existe, vía handler). */
    @GetMapping("/{idReceta}/descargar")
    @PreAuthorize("hasAnyRole('Veterinario','Recepcionista')")
    public ResponseEntity<byte[]> descargar(@PathVariable Integer idReceta) {
        RecetaPdfDTO datos = service.descargarReceta(idReceta);
        byte[] pdf = pdfGenerator.generar(datos);
        String nombreArchivo = "receta-"
                + (datos.codigoReceta() != null ? datos.codigoReceta() : idReceta) + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreArchivo + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(pdf);
    }
}