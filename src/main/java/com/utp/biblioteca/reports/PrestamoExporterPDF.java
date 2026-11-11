package com.utp.biblioteca.reports;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.utp.biblioteca.entities.Prestamo;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.Color;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PrestamoExporterPDF {

    private final List<Prestamo> listaPrestamos;

    public PrestamoExporterPDF(List<Prestamo> listaPrestamos) {
        this.listaPrestamos = listaPrestamos;
    }

    private void writeTableHeader(PdfPTable table) {
        String[] headers = {"ID", "DNI", "Lector", "ISBN", "Libro", "F. Préstamo", "F. Vencimiento", "Estado"};

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

        for (Prestamo p : listaPrestamos) {
            Color rowBg = alt ? altColor : Color.WHITE;
            alt = !alt;

            String dni = (p.getUsuario() != null && p.getUsuario().getDni() != null) ? p.getUsuario().getDni() : "-";
            String usuarioNom = p.getUsuario() != null ? p.getUsuario().getNombre() : "Usuario N/A";
            String isbn = (p.getLibro() != null && p.getLibro().getIsbn() != null) ? p.getLibro().getIsbn() : "-";
            String libroTit = p.getLibro() != null ? p.getLibro().getTitulo() : "Libro N/A";
            String fPrestamo = p.getFechaPrestamo() != null ? p.getFechaPrestamo().format(dtf) : "-";
            String fVenc = p.getFechaVencimiento() != null ? p.getFechaVencimiento().format(dtf) : "-";

            addCell(table, String.valueOf(p.getIdPrestamo()), font, rowBg, Element.ALIGN_CENTER);
            addCell(table, dni, font, rowBg, Element.ALIGN_CENTER);
            addCell(table, usuarioNom, font, rowBg, Element.ALIGN_LEFT);
            addCell(table, isbn, font, rowBg, Element.ALIGN_CENTER);
            addCell(table, libroTit, font, rowBg, Element.ALIGN_LEFT);
            addCell(table, fPrestamo, font, rowBg, Element.ALIGN_CENTER);
            addCell(table, fVenc, font, rowBg, Element.ALIGN_CENTER);
            addCell(table, p.getEstado() != null ? p.getEstado() : "-", font, rowBg, Element.ALIGN_CENTER);
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

        ReporteHeaderUtil.agregarCabezalPdf(document, "Registro de Préstamos Bibliotecarios", listaPrestamos.size());

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{1.0f, 1.8f, 4.0f, 2.0f, 4.5f, 2.0f, 2.0f, 1.8f});
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
