package com.utp.biblioteca.reports;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.utp.biblioteca.entities.Devolucion;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.Color;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DevolucionExporterPDF {

    private final List<Devolucion> listaDevoluciones;

    public DevolucionExporterPDF(List<Devolucion> listaDevoluciones) {
        this.listaDevoluciones = listaDevoluciones;
    }

    private void writeTableHeader(PdfPTable table) {
        String[] headers = {"ID Dev.", "ID Prést.", "Lector", "Libro Devuelto", "F. Devolución", "Días Ret.", "Multa (S/.)", "Estado Ejemp.", "Observación"};

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

        for (Devolucion d : listaDevoluciones) {
            Color rowBg = alt ? altColor : Color.WHITE;
            alt = !alt;

            String idPres = (d.getPrestamo() != null && d.getPrestamo().getIdPrestamo() != null)
                    ? String.valueOf(d.getPrestamo().getIdPrestamo()) : "-";
            String lector = (d.getPrestamo() != null && d.getPrestamo().getUsuario() != null)
                    ? d.getPrestamo().getUsuario().getNombre() : "-";
            String libro = (d.getPrestamo() != null && d.getPrestamo().getLibro() != null)
                    ? d.getPrestamo().getLibro().getTitulo() : "-";
            String fDev = d.getFechaDevolucion() != null ? d.getFechaDevolucion().format(dtf) : "-";
            String multaStr = d.getMontoMulta() != null ? String.format("S/. %.2f", d.getMontoMulta()) : "S/. 0.00";

            addCell(table, String.valueOf(d.getIdDevolucion()), font, rowBg, Element.ALIGN_CENTER);
            addCell(table, idPres, font, rowBg, Element.ALIGN_CENTER);
            addCell(table, lector, font, rowBg, Element.ALIGN_LEFT);
            addCell(table, libro, font, rowBg, Element.ALIGN_LEFT);
            addCell(table, fDev, font, rowBg, Element.ALIGN_CENTER);
            addCell(table, String.valueOf(d.getDiasRetraso() != null ? d.getDiasRetraso() : 0), font, rowBg, Element.ALIGN_CENTER);
            addCell(table, multaStr, font, rowBg, Element.ALIGN_RIGHT);
            addCell(table, d.getEstadoEjemplar() != null ? d.getEstadoEjemplar() : "-", font, rowBg, Element.ALIGN_CENTER);
            addCell(table, d.getObservacion() != null ? d.getObservacion() : "-", font, rowBg, Element.ALIGN_LEFT);
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

        ReporteHeaderUtil.agregarCabezalPdf(document, "Registro de Devoluciones y Multas", listaDevoluciones.size());

        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{1.2f, 1.2f, 3.8f, 4.0f, 2.0f, 1.5f, 1.8f, 2.0f, 3.0f});
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
