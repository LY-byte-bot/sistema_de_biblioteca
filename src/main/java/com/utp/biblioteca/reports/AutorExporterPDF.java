package com.utp.biblioteca.reports;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.utp.biblioteca.entities.Autor;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

public class AutorExporterPDF {

    private final List<Autor> listaAutores;

    public AutorExporterPDF(List<Autor> listaAutores) {
        this.listaAutores = listaAutores;
    }

    private void writeTableHeader(PdfPTable table) {
        String[] headers = {"ID Autor", "Nombres", "Apellidos", "Cantidad de Obras en Catálogo"};

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

        for (Autor a : listaAutores) {
            Color rowBg = alt ? altColor : Color.WHITE;
            alt = !alt;

            int cantObras = (a.getLibros() != null) ? a.getLibros().size() : 0;

            addCell(table, String.valueOf(a.getIdAutor()), font, rowBg, Element.ALIGN_CENTER);
            addCell(table, a.getNombres() != null ? a.getNombres() : "-", font, rowBg, Element.ALIGN_LEFT);
            addCell(table, a.getApellidos() != null ? a.getApellidos() : "-", font, rowBg, Element.ALIGN_LEFT);
            addCell(table, String.valueOf(cantObras), font, rowBg, Element.ALIGN_CENTER);
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

        ReporteHeaderUtil.agregarCabezalPdf(document, "Padrón Oficial de Autores Literarios", listaAutores.size());

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{1.5f, 4.0f, 4.0f, 2.5f});
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
