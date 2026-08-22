package com.udo.can_cat.usuarios.application.service;

public interface EmailService {
    
    void enviarCorreoRecuperacionPassword(String correoDestino, String token);
}