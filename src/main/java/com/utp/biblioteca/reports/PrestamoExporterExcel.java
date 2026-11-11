package com.utp.biblioteca.reports;

import com.utp.biblioteca.entities.Prestamo;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PrestamoExporterExcel {

    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private final List<Prestamo> listaPrestamos;

    public PrestamoExporterExcel(List<Prestamo> listaPrestamos) {
        this.listaPrestamos = listaPrestamos;
        this.workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Prestamos");
        ReporteHeaderUtil.agregarCabezalExcel(workbook, sheet, "Registro Histórico de Préstamos", 8, listaPrestamos.size());

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

        createCell(row, 0, "ID", style);
        createCell(row, 1, "DNI Lector", style);
        createCell(row, 2, "Lector / Solicitante", style);
        createCell(row, 3, "ISBN", style);
        createCell(row, 4, "Título del Libro", style);
        createCell(row, 5, "Fecha Préstamo", style);
        createCell(row, 6, "Fecha Vencimiento", style);
        createCell(row, 7, "Estado", style);
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

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Prestamo p : listaPrestamos) {
            Row row = sheet.createRow(rowCount++);
            int col = 0;

            String dni = (p.getUsuario() != null && p.getUsuario().getDni() != null) ? p.getUsuario().getDni() : "-";
            String usuarioNom = p.getUsuario() != null ? p.getUsuario().getNombre() : "Usuario N/A";
            String isbn = (p.getLibro() != null && p.getLibro().getIsbn() != null) ? p.getLibro().getIsbn() : "-";
            String libroTit = p.getLibro() != null ? p.getLibro().getTitulo() : "Libro N/A";
            String fPrestamo = p.getFechaPrestamo() != null ? p.getFechaPrestamo().format(dtf) : "-";
            String fVenc = p.getFechaVencimiento() != null ? p.getFechaVencimiento().format(dtf) : "-";

            createCell(row, col++, p.getIdPrestamo(), style);
            createCell(row, col++, dni, style);
            createCell(row, col++, usuarioNom, style);
            createCell(row, col++, isbn, style);
            createCell(row, col++, libroTit, style);
            createCell(row, col++, fPrestamo, style);
            createCell(row, col++, fVenc, style);
            createCell(row, col++, p.getEstado() != null ? p.getEstado() : "-", style);
        }

        for (int i = 0; i < 8; i++) {
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
