package com.utp.biblioteca.reports;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.utp.biblioteca.entities.Libro;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class LibroExporterPDF {

    private final List<Libro> listaLibros;

    public LibroExporterPDF(List<Libro> listaLibros) {
        this.listaLibros = listaLibros;
    }

    private void writeTableHeader(PdfPTable table) {
        String[] headers = {"ID", "ISBN", "Título", "Editorial", "Año", "Categoría", "Autores", "Stock Tot.", "Stock Disp."};

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

        boolean alt = false;
        Color altColor = new Color(245, 247, 250);

        for (Libro libro : listaLibros) {
            Color rowBg = alt ? altColor : Color.WHITE;
            alt = !alt;

            String catNombre = libro.getCategoria() != null ? libro.getCategoria().getNombre() : "-";
            String autoresStr = (libro.getAutores() != null && !libro.getAutores().isEmpty())
                    ? libro.getAutores().stream()
                        .map(a -> a.getNombres() + " " + a.getApellidos())
                        .collect(Collectors.joining(", "))
                    : "Anónimo";

            addCell(table, String.valueOf(libro.getIdLibro()), font, rowBg, Element.ALIGN_CENTER);
            addCell(table, libro.getIsbn() != null ? libro.getIsbn() : "-", font, rowBg, Element.ALIGN_CENTER);
            addCell(table, libro.getTitulo() != null ? libro.getTitulo() : "-", font, rowBg, Element.ALIGN_LEFT);
            addCell(table, libro.getEditorial() != null ? libro.getEditorial() : "-", font, rowBg, Element.ALIGN_LEFT);
            addCell(table, libro.getAnioPublicacion() != null ? String.valueOf(libro.getAnioPublicacion()) : "-", font, rowBg, Element.ALIGN_CENTER);
            addCell(table, catNombre, font, rowBg, Element.ALIGN_LEFT);
            addCell(table, autoresStr, font, rowBg, Element.ALIGN_LEFT);
            addCell(table, String.valueOf(libro.getStockTotal() != null ? libro.getStockTotal() : 0), font, rowBg, Element.ALIGN_CENTER);
            addCell(table, String.valueOf(libro.getStockDisponible() != null ? libro.getStockDisponible() : 0), font, rowBg, Element.ALIGN_CENTER);
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
        // Formato apaisado (Landscape) para tabla ancha de 9 columnas
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // 1. Cabezal institucional con datos de la biblioteca
        ReporteHeaderUtil.agregarCabezalPdf(document, "Catálogo General de Libros e Inventario", listaLibros.size());

        // 2. Tabla de datos
        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{1.0f, 2.2f, 4.5f, 2.5f, 1.2f, 2.8f, 3.5f, 1.5f, 1.5f});
        table.setSpacingBefore(10);

        writeTableHeader(table);
        writeTableData(table);

        document.add(table);

        // 3. Nota de pie
        Paragraph pie = new Paragraph("\nDocumento generado automáticamente por el Sistema Integrado de Gestión de Biblioteca - UTP.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY));
        pie.setAlignment(Paragraph.ALIGN_RIGHT);
        document.add(pie);

        document.close();
    }
}
