package com.udo.can_cat.facturacion.infrastructure.email;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ByteArrayResource;

@Service
public class FacturaEmailService {

    private static final Logger log = LoggerFactory.getLogger(FacturaEmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public FacturaEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envía la factura por correo. Retorna true si fue exitoso, false si falló.
     */
    public boolean enviarFactura(String toEmail, byte[] pdfBytes, String numeroControl, String clienteNombre) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Comprobante de Pago - Factura " + numeroControl);

            String html = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                      <div style="background: #0F766E; color: white; padding: 20px; border-radius: 8px 8px 0 0;">
                        <h2 style="margin: 0;">Clínica Veterinaria Can-Cat</h2>
                      </div>
                      <div style="border: 1px solid #E2E8F0; padding: 24px; border-radius: 0 0 8px 8px;">
                        <p style="color: #1E293B;">Hola, <strong>%s</strong></p>
                        <p style="color: #475569;">Su comprobante de pago ha sido generado exitosamente.</p>
                        <table style="width: 100%%; border-collapse: collapse; margin: 16px 0;">
                          <tr>
                            <td style="padding: 8px; color: #64748B; border-bottom: 1px solid #F1F5F9;">Factura</td>
                            <td style="padding: 8px; font-weight: 600; border-bottom: 1px solid #F1F5F9;">%s</td>
                          </tr>
                          <tr>
                            <td style="padding: 8px; color: #64748B; border-bottom: 1px solid #F1F5F9;">Estado</td>
                            <td style="padding: 8px; font-weight: 600; color: #D97706; border-bottom: 1px solid #F1F5F9;">Pendiente de verificación</td>
                          </tr>
                        </table>
                        <p style="color: #64748B; font-size: 14px;">La factura está adjunta en formato PDF. Puede descargarla también desde su panel.</p>
                      </div>
                      <p style="text-align: center; color: #94A3B8; font-size: 12px; margin-top: 16px;">
                        Can-Cat &mdash; Sistema de Gestión Veterinaria
                      </p>
                    </div>
                    """.formatted(clienteNombre, numeroControl);

            helper.setText(html, true);
            helper.addAttachment("Factura-" + numeroControl + ".pdf",
                    new ByteArrayResource(pdfBytes));

            mailSender.send(message);
            log.info("Factura {} enviada a {}", numeroControl, toEmail);
            return true;

        } catch (Exception e) {
            log.error("Error enviando factura {} por correo a {}: {}", numeroControl, toEmail, e.getMessage());
            return false;
        }
    }
}