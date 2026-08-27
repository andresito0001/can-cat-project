package com.udo.can_cat.usuarios.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${spring.mail.username:veterinariacancat123@gmail.com}")
    private String emailFrom;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void enviarCorreoRecuperacionPassword(String correoDestino, String token) {
        // try {
        //     String enlace = frontendUrl + "/reset-password?token=" + token;

        //     SimpleMailMessage message = new SimpleMailMessage();
        //     message.setFrom(emailFrom);
        //     message.setTo(correoDestino);
        //     message.setSubject("Recuperación de Contraseña - CanCat Veterinaria");
        //     message.setText(buildEmailContent(enlace));

        //     mailSender.send(message);
            
        //     logger.info("Correo de recuperación enviado a: {}", correoDestino);
        // } catch (Exception e) {
        //     logger.error("Error al enviar correo de recuperación a {}: {}", 
        //         correoDestino, e.getMessage());
        //     throw new RuntimeException("No se pudo enviar el correo de recuperación");
        // }

        String enlace = frontendUrl + "/auth/nueva-contrasena?token=" + token;
    
        // === MODO DESARROLLO: Loguea en consola ===
        logger.info("╔════════════════════════════════════════════════════════════╗");
        logger.info("║                    (Modo Desarrollo)                       ║");
        logger.info("╠════════════════════════════════════════════════════════════╣");
        logger.info("║  Para:      {}", correoDestino);
        logger.info("║  Asunto:    Recuperación de Contraseña");
        logger.info("║  Enlace:    {}", enlace);
        logger.info("╚════════════════════════════════════════════════════════════╝");
    }

    private String buildEmailContent(String enlace) {
        return """
                ¡Hola!
                
                Ha solicitado la recuperación de su contraseña en CanCat Veterinaria.
                
                Para restablecer su contraseña, haga clic en el siguiente enlace:
                
                %s
                
                Este enlace expirará en 24 horas por razones de seguridad.
                
                Si no solicitó este cambio, puede ignorar este correo de forma segura.
                
                Saludos cordiales,
                El equipo de CanCat Veterinaria
                """.formatted(enlace);
    }
}