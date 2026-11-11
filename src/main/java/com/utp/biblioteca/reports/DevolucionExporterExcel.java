package com.utp.biblioteca.reports;

import com.utp.biblioteca.entities.Devolucion;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DevolucionExporterExcel {

    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private final List<Devolucion> listaDevoluciones;

    public DevolucionExporterExcel(List<Devolucion> listaDevoluciones) {
        this.listaDevoluciones = listaDevoluciones;
        this.workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Devoluciones");
        ReporteHeaderUtil.agregarCabezalExcel(workbook, sheet, "Control de Devoluciones y Multas", 9, listaDevoluciones.size());

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

        createCell(row, 0, "ID Dev.", style);
        createCell(row, 1, "ID Préstamo", style);
        createCell(row, 2, "Lector", style);
        createCell(row, 3, "Libro Devuelto", style);
        createCell(row, 4, "Fecha Devolución", style);
        createCell(row, 5, "Días Retraso", style);
        createCell(row, 6, "Multa (S/.)", style);
        createCell(row, 7, "Estado Ejemplar", style);
        createCell(row, 8, "Observación", style);
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer val) {
            cell.setCellValue(val);
        } else if (value instanceof Number val) {
            cell.setCellValue(val.doubleValue());
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

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Devolucion d : listaDevoluciones) {
            Row row = sheet.createRow(rowCount++);
            int col = 0;

            Integer idPres = d.getPrestamo() != null ? d.getPrestamo().getIdPrestamo() : null;
            String lector = (d.getPrestamo() != null && d.getPrestamo().getUsuario() != null)
                    ? d.getPrestamo().getUsuario().getNombre() : "-";
            String libro = (d.getPrestamo() != null && d.getPrestamo().getLibro() != null)
                    ? d.getPrestamo().getLibro().getTitulo() : "-";
            String fDev = d.getFechaDevolucion() != null ? d.getFechaDevolucion().format(dtf) : "-";
            Double multa = d.getMontoMulta() != null ? d.getMontoMulta().doubleValue() : 0.0;

            createCell(row, col++, d.getIdDevolucion(), style);
            createCell(row, col++, idPres != null ? idPres : "-", style);
            createCell(row, col++, lector, style);
            createCell(row, col++, libro, style);
            createCell(row, col++, fDev, style);
            createCell(row, col++, d.getDiasRetraso() != null ? d.getDiasRetraso() : 0, style);
            createCell(row, col++, multa, style);
            createCell(row, col++, d.getEstadoEjemplar() != null ? d.getEstadoEjemplar() : "-", style);
            createCell(row, col++, d.getObservacion() != null ? d.getObservacion() : "-", style);
        }

        for (int i = 0; i < 9; i++) {
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
