package com.udo.can_cat.usuarios.infrastructure.controller;

import com.udo.can_cat.usuarios.application.dto.AuthResponseDTO;
import com.udo.can_cat.usuarios.application.dto.LoginRequestDTO;
import com.udo.can_cat.usuarios.application.service.AuthApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        AuthResponseDTO response = authApplicationService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}