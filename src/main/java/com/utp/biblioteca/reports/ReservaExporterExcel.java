package com.utp.biblioteca.reports;

import com.utp.biblioteca.entities.Reserva;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReservaExporterExcel {

    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private final List<Reserva> listaReservas;

    public ReservaExporterExcel(List<Reserva> listaReservas) {
        this.listaReservas = listaReservas;
        this.workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Reservas");
        ReporteHeaderUtil.agregarCabezalExcel(workbook, sheet, "Control de Reservas de Ejemplares", 6, listaReservas.size());

        Row row = sheet.createRow(4);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        createCell(row, 0, "ID Reserva", style);
        createCell(row, 1, "DNI Lector", style);
        createCell(row, 2, "Lector Solicitante", style);
        createCell(row, 3, "Título del Libro", style);
        createCell(row, 4, "Fecha Solicitud", style);
        createCell(row, 5, "Estado Reserva", style);
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer val) {
            cell.setCellValue(val);
        } else if (value != null) {
            cell.setCellValue(value.toString());
        } else {
            cell.setCellValue("-");
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    private void writeDataLines() {
        int rowCount = 5;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Reserva r : listaReservas) {
            Row row = sheet.createRow(rowCount++);
            int col = 0;

            String dni = (r.getUsuario() != null && r.getUsuario().getDni() != null) ? r.getUsuario().getDni() : "-";
            String lector = r.getUsuario() != null ? r.getUsuario().getNombre() : "Lector N/A";
            String libro = r.getLibro() != null ? r.getLibro().getTitulo() : "Libro N/A";
            String fSol = r.getFechaSolicitud() != null ? r.getFechaSolicitud().format(dtf) : "-";

            createCell(row, col++, r.getIdReserva(), style);
            createCell(row, col++, dni, style);
            createCell(row, col++, lector, style);
            createCell(row, col++, libro, style);
            createCell(row, col++, fSol, style);
            createCell(row, col++, r.getEstado() != null ? r.getEstado() : "-", style);
        }

        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}
