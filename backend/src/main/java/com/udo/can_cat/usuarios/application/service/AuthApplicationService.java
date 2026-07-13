package com.udo.can_cat.usuarios.application.service;

import com.udo.can_cat.usuarios.application.dto.AuthResponseDTO;
import com.udo.can_cat.usuarios.application.dto.LoginRequestDTO;
import com.udo.can_cat.usuarios.domain.entity.Usuario;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
import com.udo.can_cat.usuarios.infrastructure.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthApplicationService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthApplicationService(UsuarioRepository usuarioRepository,
                                  PasswordEncoder passwordEncoder,
                                  JwtTokenProvider jwtTokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        System.out.println("=========================================");
        System.out.println(">>> INTENTO DE LOGIN RECIBIDO");
        System.out.println(">>> Email: " + dto.email());
        System.out.println(">>> Password: " + dto.contrasena());
        System.out.println(">>> HASH REAL PARA 'secreto123': " + passwordEncoder.encode("secreto123"));
        System.out.println("=========================================");
        
        Usuario usuario = usuarioRepository.findByCorreoElectronico(dto.email())
                .orElseThrow(() -> {
                    System.out.println("❌ ERROR: El email NO existe en la base de datos.");
                    return new RuntimeException("Credenciales inválidas");
                });

        System.out.println("USUARIO ENCONTRADO EN BD. ID: " + usuario.getId().value());

        String hashEnBaseDeDatos = usuario.getContrasenaHash(); 
        System.out.println(">>> Hash en BD: " + hashEnBaseDeDatos);

        if (!passwordEncoder.matches(dto.contrasena(), hashEnBaseDeDatos)) {
            System.out.println("ERROR: La contraseña no coincide con el hash.");
            throw new RuntimeException("Credenciales inválidas");
        }

        System.out.println("CONTRASEÑA CORRECTA. Generando token...");
        
        String token = jwtTokenProvider.generateToken(usuario);
        return new AuthResponseDTO(token, "Autenticación exitosa");
    }
}