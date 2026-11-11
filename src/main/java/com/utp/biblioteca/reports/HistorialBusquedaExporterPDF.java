package com.utp.biblioteca.reports;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.utp.biblioteca.entities.HistorialBusqueda;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.Color;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistorialBusquedaExporterPDF {

    private final List<HistorialBusqueda> listaHistorial;

    public HistorialBusquedaExporterPDF(List<HistorialBusqueda> listaHistorial) {
        this.listaHistorial = listaHistorial;
    }

    private void writeTableHeader(PdfPTable table) {
        String[] headers = {"ID", "Usuario", "Término Consultado", "Filtros Aplicados", "Fecha y Hora"};

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

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        boolean alt = false;
        Color altColor = new Color(245, 247, 250);

        for (HistorialBusqueda hb : listaHistorial) {
            Color rowBg = alt ? altColor : Color.WHITE;
            alt = !alt;

            String usuarioStr = (hb.getUsuario() != null)
                    ? hb.getUsuario().getNombre() + " (" + hb.getUsuario().getCorreo() + ")"
                    : "Anónimo (Invitado)";
            String fechaStr = hb.getFechaHora() != null ? hb.getFechaHora().format(dtf) : "-";

            addCell(table, String.valueOf(hb.getIdBusqueda()), font, rowBg, Element.ALIGN_CENTER);
            addCell(table, usuarioStr, font, rowBg, Element.ALIGN_LEFT);
            addCell(table, hb.getTermino() != null ? hb.getTermino() : "-", font, rowBg, Element.ALIGN_LEFT);
            addCell(table, hb.getFiltros() != null ? hb.getFiltros() : "Búsqueda libre", font, rowBg, Element.ALIGN_LEFT);
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
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        ReporteHeaderUtil.agregarCabezalPdf(document, "Auditoría de Historial de Búsquedas", listaHistorial.size());

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{1.0f, 3.5f, 3.5f, 3.0f, 2.5f});
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
