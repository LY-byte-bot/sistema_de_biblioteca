package com.utp.biblioteca.reports;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.utp.biblioteca.entities.Categoria;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

public class CategoriaExporterPDF {

    private final List<Categoria> listaCategorias;

    public CategoriaExporterPDF(List<Categoria> listaCategorias) {
        this.listaCategorias = listaCategorias;
    }

    private void writeTableHeader(PdfPTable table) {
        String[] headers = {"ID Categoría", "Nombre", "Descripción Temática", "Libros Registrados"};

        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        font.setColor(Color.WHITE);
        font.setSize(10);

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
        font.setSize(9);

        boolean alt = false;
        Color altColor = new Color(245, 247, 250);

        for (Categoria c : listaCategorias) {
            Color rowBg = alt ? altColor : Color.WHITE;
            alt = !alt;

            int cantLibros = (c.getLibros() != null) ? c.getLibros().size() : 0;

            addCell(table, String.valueOf(c.getIdCategoria()), font, rowBg, Element.ALIGN_CENTER);
            addCell(table, c.getNombre() != null ? c.getNombre() : "-", font, rowBg, Element.ALIGN_LEFT);
            addCell(table, c.getDescripcion() != null ? c.getDescripcion() : "-", font, rowBg, Element.ALIGN_LEFT);
            addCell(table, String.valueOf(cantLibros), font, rowBg, Element.ALIGN_CENTER);
        }
    }

    private void addCell(PdfPTable table, String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    public void export(HttpServletResponse response) throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        ReporteHeaderUtil.agregarCabezalPdf(document, "Clasificación de Categorías Bibliográficas", listaCategorias.size());

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{1.5f, 4.0f, 4.5f, 2.0f});
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
