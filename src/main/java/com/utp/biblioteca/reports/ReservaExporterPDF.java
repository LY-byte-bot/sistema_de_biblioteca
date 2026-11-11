package com.utp.biblioteca.reports;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.utp.biblioteca.entities.Reserva;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.Color;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReservaExporterPDF {

    private final List<Reserva> listaReservas;

    public ReservaExporterPDF(List<Reserva> listaReservas) {
        this.listaReservas = listaReservas;
    }

    private void writeTableHeader(PdfPTable table) {
        String[] headers = {"ID Reserva", "DNI", "Lector Solicitante", "Libro Solicitado", "Fecha Solicitud", "Estado"};

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

        for (Reserva r : listaReservas) {
            Color rowBg = alt ? altColor : Color.WHITE;
            alt = !alt;

            String dni = (r.getUsuario() != null && r.getUsuario().getDni() != null) ? r.getUsuario().getDni() : "-";
            String lector = r.getUsuario() != null ? r.getUsuario().getNombre() : "Lector N/A";
            String libro = r.getLibro() != null ? r.getLibro().getTitulo() : "Libro N/A";
            String fSol = r.getFechaSolicitud() != null ? r.getFechaSolicitud().format(dtf) : "-";

            addCell(table, String.valueOf(r.getIdReserva()), font, rowBg, Element.ALIGN_CENTER);
            addCell(table, dni, font, rowBg, Element.ALIGN_CENTER);
            addCell(table, lector, font, rowBg, Element.ALIGN_LEFT);
            addCell(table, libro, font, rowBg, Element.ALIGN_LEFT);
            addCell(table, fSol, font, rowBg, Element.ALIGN_CENTER);
            addCell(table, r.getEstado() != null ? r.getEstado() : "-", font, rowBg, Element.ALIGN_CENTER);
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

        ReporteHeaderUtil.agregarCabezalPdf(document, "Registro de Reservas de Libros", listaReservas.size());

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{1.5f, 2.0f, 4.0f, 5.0f, 2.8f, 2.0f});
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
