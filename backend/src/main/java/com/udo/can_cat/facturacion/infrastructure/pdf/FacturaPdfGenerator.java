package com.udo.can_cat.facturacion.infrastructure.pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.udo.can_cat.facturacion.application.service.PagoApplicationService.DatosPdf;
import org.springframework.stereotype.Component;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class FacturaPdfGenerator {

    private static final Color TEAL_DARK = new Color(15, 118, 110);
    private static final Color GRAY_LIGHT = new Color(241, 245, 249);
    private static final Color GRAY_TEXT = new Color(100, 116, 139);
    private static final Color WHITE = Color.WHITE;

    public byte[] generar(DatosPdf datos) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            // ─── HEADER CLÍNICA ───
            Font clinicFont = new Font(Font.HELVETICA, 16, Font.BOLD, TEAL_DARK);
            Paragraph clinic = new Paragraph("Clínica Veterinaria Can-Cat", clinicFont);
            clinic.setAlignment(Element.ALIGN_CENTER);
            document.add(clinic);

            Font subFont = new Font(Font.HELVETICA, 9, Font.NORMAL, GRAY_TEXT);
            Paragraph sub = new Paragraph("URBANIZACIÓN VENEZUELA, LECHERÍA, ESTADO ANZOÁTEGUI • Tel: (0281) 248-4500", subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            document.add(sub);

            // ─── LÍNEA SEPARADORA ───
            document.add(new Paragraph(" "));
            
            LineSeparator line = new LineSeparator();
            line.setLineWidth(1f);
            line.setPercentage(100f);
            line.setLineColor(TEAL_DARK);
            line.setAlignment(Element.ALIGN_CENTER);
            document.add(line);
            document.add(new Paragraph(" "));

            // ─── TÍTULO FACTURA ───
            Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Paragraph title = new Paragraph("FACTURA", titleFont);
            title.setAlignment(Element.ALIGN_RIGHT);
            document.add(title);

            Font infoFont = new Font(Font.HELVETICA, 9, Font.NORMAL);
            Paragraph nc = new Paragraph("N° Control: " + datos.numeroControl(), infoFont);
            nc.setAlignment(Element.ALIGN_RIGHT);
            document.add(nc);

            Paragraph fecha = new Paragraph("Fecha: " + datos.fechaEmision(), infoFont);
            fecha.setAlignment(Element.ALIGN_RIGHT);
            document.add(fecha);

            document.add(new Paragraph(" "));

            // ─── DATOS CLIENTE ───
            Font sectionFont = new Font(Font.HELVETICA, 10, Font.BOLD, TEAL_DARK);
            document.add(new Paragraph("Datos del Cliente", sectionFont));
            document.add(new Paragraph("Nombre: " + datos.clienteNombre(), infoFont));
            document.add(new Paragraph("Documento: " + datos.clienteDocumento(), infoFont));
            document.add(new Paragraph("Dirección: " + datos.clienteDireccion(), infoFont));
            document.add(new Paragraph("Ciudad: " + datos.clienteCiudad(), infoFont));
            document.add(new Paragraph("Teléfono: " + datos.clienteTelefono(), infoFont));

            document.add(new Paragraph(" "));

            // ─── DATOS SERVICIO ───
            document.add(new Paragraph("Datos del Servicio", sectionFont));
            document.add(new Paragraph("Veterinario: " + datos.veterinarioNombre(), infoFont));
            document.add(new Paragraph("Mascota: " + datos.mascotaNombre(), infoFont));
            document.add(new Paragraph("Fecha de cita: " + datos.fechaCita() + " " + datos.horaInicio(), infoFont));
            document.add(new Paragraph("Motivo: " + datos.motivoConsulta(), infoFont));

            document.add(new Paragraph(" "));

            // ─── TABLA DETALLES ───
            document.add(new Paragraph("Detalle de Servicios", sectionFont));
            document.add(new Paragraph(" "));

            float[] cols = {5f, 55f, 12f, 14f, 14f};
            PdfPTable table = new PdfPTable(cols);
            table.setWidthPercentage(100);

            Font thFont = new Font(Font.HELVETICA, 8, Font.BOLD, WHITE);
            Font tdFont = new Font(Font.HELVETICA, 8, Font.NORMAL);

            // Header
            addCell(table, "#", thFont, TEAL_DARK, WHITE, Element.ALIGN_CENTER);
            addCell(table, "Descripción", thFont, TEAL_DARK, WHITE, Element.ALIGN_LEFT);
            addCell(table, "Cant.", thFont, TEAL_DARK, WHITE, Element.ALIGN_CENTER);
            addCell(table, "P. Unit.", thFont, TEAL_DARK, WHITE, Element.ALIGN_RIGHT);
            addCell(table, "Subtotal", thFont, TEAL_DARK, WHITE, Element.ALIGN_RIGHT);

            // Rows
            int i = 1;
            for (var d : datos.detalles()) {
                Color bg = (i % 2 == 0) ? GRAY_LIGHT : WHITE;
                addCell(table, String.valueOf(i++), tdFont, bg, Color.BLACK, Element.ALIGN_CENTER);
                addCell(table, d.getDescripcion(), tdFont, bg, Color.BLACK, Element.ALIGN_LEFT);
                addCell(table, String.valueOf(d.getCantidad()), tdFont, bg, Color.BLACK, Element.ALIGN_CENTER);
                addCell(table, formatMoney(d.getPrecioUnitario()), tdFont, bg, Color.BLACK, Element.ALIGN_RIGHT);
                addCell(table, formatMoney(d.getSubtotal()), tdFont, bg, Color.BLACK, Element.ALIGN_RIGHT);
            }

            document.add(table);
            document.add(new Paragraph(" "));

            // ─── TOTALES ───
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(45);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            Font totalLabel = new Font(Font.HELVETICA, 9, Font.NORMAL);
            Font totalValue = new Font(Font.HELVETICA, 9, Font.NORMAL);

            addTotalRow(totalsTable, "Subtotal:", formatMoney(datos.subtotal()), totalLabel, totalValue);
            addTotalRow(totalsTable, "IVA (" + datos.porcentajeIva().setScale(0, RoundingMode.HALF_UP) + "%):",
                    formatMoney(calcularIva(datos.subtotal(), datos.porcentajeIva())), totalLabel, totalValue);

            // Total neto en negrita
            Font totalBold = new Font(Font.HELVETICA, 11, Font.BOLD, TEAL_DARK);
            addTotalRow(totalsTable, "TOTAL:", formatMoney(datos.totalNeto()), totalLabel, totalBold);

            document.add(totalsTable);
            document.add(new Paragraph(" "));

            // ─── MÉTODO DE PAGO ───
            document.add(new Paragraph("Método de pago: " + datos.metodoPago(), infoFont));
            document.add(new Paragraph("Estado: " + (datos.stadoPago() != null
                            ? datos.stadoPago().replace("_", " ") : "Emitida"),
                            new Font(Font.HELVETICA, 9, Font.ITALIC, GRAY_TEXT)));
            
                            // ─── PIE ───
            document.add(new Paragraph(" "));
            document.add(new LineSeparator(0.5f, 100f, GRAY_TEXT, Element.ALIGN_CENTER, 0f));
            Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, GRAY_TEXT);
            Paragraph footer = new Paragraph("Este comprobante está sujeto a verificación. Gracias por su preferencia.", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de factura", e);
        }
    }

    private void addCell(PdfPTable table, String text, Font font, Color bg, Color fg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(GRAY_LIGHT);
        cell.setHorizontalAlignment(align);
        cell.setPadding(5f);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell lCell = new PdfPCell(new Phrase(label, labelFont));
        lCell.setBorder(Rectangle.NO_BORDER);
        lCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        lCell.setPaddingRight(8f);
        table.addCell(lCell);

        PdfPCell vCell = new PdfPCell(new Phrase(value, valueFont));
        vCell.setBorder(Rectangle.NO_BORDER);
        vCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vCell.setPaddingLeft(8f);
        table.addCell(vCell);
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return "$" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString() + " USD";
    }

    private BigDecimal calcularIva(BigDecimal subtotal, BigDecimal porcentaje) {
        if (subtotal == null || porcentaje == null) return BigDecimal.ZERO;
        return subtotal.multiply(porcentaje).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }
}