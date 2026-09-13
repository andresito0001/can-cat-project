package com.udo.can_cat.mascotas.infrastructure.controller;

import com.udo.can_cat.mascotas.application.dto.MascotaRegistradaResponseDTO;
import com.udo.can_cat.mascotas.application.dto.RegistrarMascotaRequestDTO;
import com.udo.can_cat.mascotas.application.service.MascotaApplicationService;
import com.udo.can_cat.mascotas.application.service.MascotaApplicationService.UsuarioAutenticado;
import com.udo.can_cat.usuarios.infrastructure.security.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    private final MascotaApplicationService mascotaService;
    private final JwtTokenProvider jwtTokenProvider;

    public MascotaController(MascotaApplicationService mascotaService,
                             JwtTokenProvider jwtTokenProvider) {
        this.mascotaService = mascotaService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Cliente', 'Recepcionista')")
    public ResponseEntity<MascotaRegistradaResponseDTO> registrarMascota(
            @RequestBody @Valid RegistrarMascotaRequestDTO request,
            HttpServletRequest httpRequest) {

        UsuarioAutenticado usuarioAutenticado = obtenerUsuarioAutenticado(httpRequest);
        MascotaRegistradaResponseDTO response = mascotaService.registrarMascota(request, usuarioAutenticado);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('Cliente')")
    public ResponseEntity<List<MascotaRegistradaResponseDTO>> listarMisMascotas(
            HttpServletRequest httpRequest) {

        UsuarioAutenticado usuarioAutenticado = obtenerUsuarioAutenticado(httpRequest);
        return ResponseEntity.ok(mascotaService.listarMascotasDeCliente(usuarioAutenticado));
    }

    /**
     * GET /api/mascotas/por-cliente/{idCliente}
     * CU 4.6.1.11 paso 3: mascotas del cliente seleccionado (rol Recepcionista).
     */
    @GetMapping("/por-cliente/{idCliente}")
    @PreAuthorize("hasRole('Recepcionista')")
    public ResponseEntity<List<MascotaRegistradaResponseDTO>> listarMascotasPorCliente(
            @PathVariable Integer idCliente) {
        return ResponseEntity.ok(mascotaService.listarMascotasPorIdCliente(idCliente));
    }

    private UsuarioAutenticado obtenerUsuarioAutenticado(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String rol = authentication.getAuthorities().stream()
                .map(auth -> {
                    String authority = auth.getAuthority();
                    return authority.startsWith("ROLE_") ? authority.substring(5) : authority;
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se pudo determinar el rol del usuario"));

        // Extraer correo desde el token (no del subject)
        String bearerToken = request.getHeader("Authorization");
        String token = (bearerToken != null && bearerToken.startsWith("Bearer "))
                ? bearerToken.substring(7)
                : null;
        String correo = jwtTokenProvider.getEmailFromToken(token);

        return new UsuarioAutenticado(correo, rol);
    }
}