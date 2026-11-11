package com.utp.biblioteca.reports;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.utp.biblioteca.entities.ContenidoDigital;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.Color;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ContenidoDigitalExporterPDF {

    private final List<ContenidoDigital> listaContenidos;

    public ContenidoDigitalExporterPDF(List<ContenidoDigital> listaContenidos) {
        this.listaContenidos = listaContenidos;
    }

    private void writeTableHeader(PdfPTable table) {
        String[] headers = {"ID", "ID Libro", "Título del Libro", "Tipo", "Checksum / Identificador", "Fecha Indexación"};

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

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        boolean alt = false;
        Color altColor = new Color(245, 247, 250);

        for (ContenidoDigital cd : listaContenidos) {
            Color rowBg = alt ? altColor : Color.WHITE;
            alt = !alt;

            String idLibro = (cd.getLibro() != null && cd.getLibro().getIdLibro() != null)
                    ? String.valueOf(cd.getLibro().getIdLibro()) : "-";
            String tituloLibro = cd.getLibro() != null ? cd.getLibro().getTitulo() : "-";
            String fIdx = cd.getFechaIndexacion() != null ? cd.getFechaIndexacion().format(dtf) : "-";
            String identificador = cd.getChecksum() != null ? cd.getChecksum() : cd.getUrlArchivo();

            addCell(table, String.valueOf(cd.getIdContenido()), font, rowBg, Element.ALIGN_CENTER);
            addCell(table, idLibro, font, rowBg, Element.ALIGN_CENTER);
            addCell(table, tituloLibro, font, rowBg, Element.ALIGN_LEFT);
            addCell(table, cd.getTipoArchivo() != null ? cd.getTipoArchivo() : "-", font, rowBg, Element.ALIGN_CENTER);
            addCell(table, identificador != null ? identificador : "-", font, rowBg, Element.ALIGN_LEFT);
            addCell(table, fIdx, font, rowBg, Element.ALIGN_CENTER);
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

        ReporteHeaderUtil.agregarCabezalPdf(document, "Inventario de Contenidos Digitales y PDFs", listaContenidos.size());

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{1.0f, 1.2f, 5.0f, 2.0f, 4.5f, 2.5f});
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
