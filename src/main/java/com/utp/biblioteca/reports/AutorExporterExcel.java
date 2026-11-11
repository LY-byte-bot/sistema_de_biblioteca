package com.utp.biblioteca.reports;

import com.utp.biblioteca.entities.Autor;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.List;

public class AutorExporterExcel {

    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private final List<Autor> listaAutores;

    public AutorExporterExcel(List<Autor> listaAutores) {
        this.listaAutores = listaAutores;
        this.workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Autores");
        ReporteHeaderUtil.agregarCabezalExcel(workbook, sheet, "Padrón de Autores Registrados", 4, listaAutores.size());

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

        createCell(row, 0, "ID Autor", style);
        createCell(row, 1, "Nombres", style);
        createCell(row, 2, "Apellidos", style);
        createCell(row, 3, "Cantidad de Obras", style);
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

        for (Autor a : listaAutores) {
            Row row = sheet.createRow(rowCount++);
            int col = 0;

            int cantObras = (a.getLibros() != null) ? a.getLibros().size() : 0;

            createCell(row, col++, a.getIdAutor(), style);
            createCell(row, col++, a.getNombres() != null ? a.getNombres() : "-", style);
            createCell(row, col++, a.getApellidos() != null ? a.getApellidos() : "-", style);
            createCell(row, col++, cantObras, style);
        }

        for (int i = 0; i < 4; i++) {
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
