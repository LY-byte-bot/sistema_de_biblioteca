package com.utp.biblioteca.reports;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.utp.biblioteca.entities.Usuario;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.Color;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UsuarioExporterPDF {

    private final List<Usuario> listaUsuarios;

    public UsuarioExporterPDF(List<Usuario> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    private void writeTableHeader(PdfPTable table) {
        String[] headers = {"ID", "DNI", "Nombre Completo", "Correo Electrónico", "Teléfono", "Rol", "Estado", "F. Creación"};

        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        font.setColor(Color.WHITE);
        font.setSize(9);

        for (String h : headers) {
            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(new Color(13, 110, 253));
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPhrase(new Phrase(h, font));
            table.addCell(cell);
        }
    }

    private void writeTableData(PdfPTable table) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setSize(8);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        boolean alt = false;
        Color altColor = new Color(245, 247, 250);

        for (Usuario u : listaUsuarios) {
            Color rowBg = alt ? altColor : Color.WHITE;
            alt = !alt;

            String fechaStr = u.getFechaCreacion() != null ? u.getFechaCreacion().format(dtf) : "-";
            String estadoStr = Boolean.TRUE.equals(u.getEstado()) ? "ACTIVO" : "INACTIVO";

            addCell(table, String.valueOf(u.getIdUsuario()), font, rowBg, Element.ALIGN_CENTER);
            addCell(table, u.getDni() != null ? u.getDni() : "-", font, rowBg, Element.ALIGN_CENTER);
            addCell(table, u.getNombre() != null ? u.getNombre() : "-", font, rowBg, Element.ALIGN_LEFT);
            addCell(table, u.getCorreo() != null ? u.getCorreo() : "-", font, rowBg, Element.ALIGN_LEFT);
            addCell(table, u.getTelefono() != null ? u.getTelefono() : "-", font, rowBg, Element.ALIGN_CENTER);
            addCell(table, u.getRol() != null ? u.getRol() : "-", font, rowBg, Element.ALIGN_CENTER);
            addCell(table, estadoStr, font, rowBg, Element.ALIGN_CENTER);
            addCell(table, fechaStr, font, rowBg, Element.ALIGN_CENTER);
        }
    }

    private void addCell(PdfPTable table, String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    public void export(HttpServletResponse response) throws IOException {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        ReporteHeaderUtil.agregarCabezalPdf(document, "Padrón Oficial de Usuarios del Sistema", listaUsuarios.size());

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{1.0f, 2.0f, 5.0f, 5.0f, 2.5f, 2.5f, 2.0f, 2.5f});
        table.setSpacingBefore(10);

        writeTableHeader(table);
        writeTableData(table);

        document.add(table);

        Paragraph pie = new Paragraph("\nReporte generado por el Sistema de Biblioteca UTP.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY));
        pie.setAlignment(Paragraph.ALIGN_RIGHT);
        document.add(pie);

        document.close();
    }
}
