package com.udo.can_cat.atenciones.infrastructure.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.udo.can_cat.atenciones.application.dto.RecetaPdfDTO;
import org.springframework.stereotype.Component;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Component
public class RecetaPdfGenerator {

    private static final Color TEAL_DARK = new Color(15, 118, 110);
    private static final Color GRAY_LIGHT = new Color(241, 245, 249);
    private static final Color GRAY_TEXT = new Color(100, 116, 139);
    private static final Color WHITE = Color.WHITE;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generar(RecetaPdfDTO d) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            // ─── ENCABEZADO CLÍNICA ───
            Font clinicFont = new Font(Font.HELVETICA, 16, Font.BOLD, TEAL_DARK);
            Paragraph clinic = new Paragraph("Clínica Veterinaria Can-Cat", clinicFont);
            clinic.setAlignment(Element.ALIGN_CENTER);
            document.add(clinic);

            Font subFont = new Font(Font.HELVETICA, 9, Font.NORMAL, GRAY_TEXT);
            Paragraph sub = new Paragraph(
                    "URBANIZACIÓN VENEZUELA, LECHERÍA, ESTADO ANZOÁTEGUI • Tel: (0281) 248-4500", subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            document.add(sub);

            document.add(new Paragraph(" "));
            document.add(new LineSeparator(1f, 100f, TEAL_DARK, Element.ALIGN_CENTER, 0f));
            document.add(new Paragraph(" "));

            // ─── TÍTULO + CÓDIGO + FECHA ───
            Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Paragraph title = new Paragraph("RÉCIPE MÉDICO VETERINARIO", titleFont);
            title.setAlignment(Element.ALIGN_RIGHT);
            document.add(title);

            Font infoFont = new Font(Font.HELVETICA, 9, Font.NORMAL);
            Paragraph codigo = new Paragraph("Código: " + texto(d.codigoReceta()), infoFont);
            codigo.setAlignment(Element.ALIGN_RIGHT);
            document.add(codigo);
            Paragraph fecha = new Paragraph(
                    "Fecha de emisión: " + (d.fechaEmision() != null ? d.fechaEmision().format(FECHA) : "—"),
                    infoFont);
            fecha.setAlignment(Element.ALIGN_RIGHT);
            document.add(fecha);
            document.add(new Paragraph(" "));

            // ─── PACIENTE ───
            Font sectionFont = new Font(Font.HELVETICA, 10, Font.BOLD, TEAL_DARK);
            document.add(new Paragraph("Paciente", sectionFont));
            document.add(new Paragraph("Mascota: " + texto(d.mascotaNombre())
                    + "   Especie/Raza: " + texto(d.mascotaEspecie()) + " / " + texto(d.mascotaRaza())
                    + "   Sexo: " + sexo(d.mascotaSexo()), infoFont));
            document.add(new Paragraph("Dueño: " + texto(d.clienteNombre())
                    + "   Documento: " + texto(d.clienteDocumento()), infoFont));
            if (d.diagnostico() != null && !d.diagnostico().isBlank()) {
                document.add(new Paragraph("Diagnóstico: " + d.diagnostico(), infoFont));
            }
            document.add(new Paragraph(" "));

            // ─── PRESCRIPCIÓN (Rp/) ───
            document.add(new Paragraph("Rp/", new Font(Font.HELVETICA, 12, Font.BOLD, TEAL_DARK)));
            document.add(new Paragraph(" "));

            float[] cols = {28f, 16f, 12f, 12f, 14f, 18f};
            PdfPTable table = new PdfPTable(cols);
            table.setWidthPercentage(100);

            Font thFont = new Font(Font.HELVETICA, 8, Font.BOLD, WHITE);
            Font tdFont = new Font(Font.HELVETICA, 8, Font.NORMAL);

            addCell(table, "Medicamento", thFont, TEAL_DARK, WHITE, Element.ALIGN_LEFT);
            addCell(table, "Concentración", thFont, TEAL_DARK, WHITE, Element.ALIGN_LEFT);
            addCell(table, "Dosis", thFont, TEAL_DARK, WHITE, Element.ALIGN_CENTER);
            addCell(table, "Vía", thFont, TEAL_DARK, WHITE, Element.ALIGN_CENTER);
            addCell(table, "Frecuencia", thFont, TEAL_DARK, WHITE, Element.ALIGN_LEFT);
            addCell(table, "Duración", thFont, TEAL_DARK, WHITE, Element.ALIGN_LEFT);

            int i = 1;
            for (RecetaPdfDTO.ItemPdfDTO it : d.items()) {
                Color bg = (i % 2 == 0) ? GRAY_LIGHT : WHITE;
                addCell(table, texto(it.medicamento()), tdFont, bg, Color.BLACK, Element.ALIGN_LEFT);
                addCell(table, texto(it.concentracion()), tdFont, bg, Color.BLACK, Element.ALIGN_LEFT);
                addCell(table, texto(it.dosis()), tdFont, bg, Color.BLACK, Element.ALIGN_CENTER);
                addCell(table, texto(it.viaAdministracion()), tdFont, bg, Color.BLACK, Element.ALIGN_CENTER);
                addCell(table, texto(it.frecuencia()), tdFont, bg, Color.BLACK, Element.ALIGN_LEFT);
                addCell(table, texto(it.duracion()), tdFont, bg, Color.BLACK, Element.ALIGN_LEFT);
                i++;
            }
            document.add(table);
            document.add(new Paragraph(" "));

            // ─── INDICACIONES GENERALES ───
            if (d.indicacionesGenerales() != null && !d.indicacionesGenerales().isBlank()) {
                document.add(new Paragraph("Indicaciones Generales", sectionFont));
                document.add(new Paragraph(d.indicacionesGenerales(), infoFont));
                document.add(new Paragraph(" "));
            }

            // ─── FIRMA DEL VETERINARIO (D5) ───
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new LineSeparator(0.5f, 40f, GRAY_TEXT, Element.ALIGN_CENTER, 0f));
            document.add(new Paragraph(" "));

            Font firmaFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            Paragraph nombre = new Paragraph(texto(d.veterinarioNombre()), firmaFont);
            nombre.setAlignment(Element.ALIGN_CENTER);
            document.add(nombre);

            Font detFont = new Font(Font.HELVETICA, 8, Font.ITALIC, GRAY_TEXT);
            Paragraph detalle = new Paragraph(firmaDetalle(d), detFont);
            detalle.setAlignment(Element.ALIGN_CENTER);
            document.add(detalle);

            // ─── PIE ───
            document.add(new Paragraph(" "));
            document.add(new LineSeparator(0.5f, 100f, GRAY_TEXT, Element.ALIGN_CENTER, 0f));
            Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, GRAY_TEXT);
            Paragraph footer = new Paragraph(
                    "Documento generado digitalmente por Can-Cat. Conserve este récipe para futuras consultas.",
                    footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de récipe", e);
        }
    }

    private String firmaDetalle(RecetaPdfDTO d) {
        StringBuilder sb = new StringBuilder("Médico Veterinario");
        if (d.veterinarioEspecialidad() != null && !d.veterinarioEspecialidad().isBlank()) {
            sb.append(" — ").append(d.veterinarioEspecialidad());
        }
        if (d.veterinarioLicencia() != null && !d.veterinarioLicencia().isBlank()) {
            sb.append(" • Lic. N° ").append(d.veterinarioLicencia());
        }
        return sb.toString();
    }

    private void addCell(PdfPTable table, String text, Font font, Color bg, Color fg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(GRAY_LIGHT);
        cell.setHorizontalAlignment(align);
        cell.setPadding(5f);
        table.addCell(cell);
    }

    private String texto(String valor) {
        return (valor == null || valor.isBlank()) ? "—" : valor;
    }

    private String sexo(String s) {
        if (s == null) return "—";
        return switch (s.toUpperCase()) {
            case "M" -> "Macho";
            case "H", "F" -> "Hembra";
            default -> s;
        };
    }
}