package com.utp.biblioteca.reports;

import com.utp.biblioteca.entities.Autor;
import com.utp.biblioteca.entities.Libro;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class LibroExporterExcel {

    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private final List<Libro> listaLibros;

    public LibroExporterExcel(List<Libro> listaLibros) {
        this.listaLibros = listaLibros;
        this.workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Libros");
        ReporteHeaderUtil.agregarCabezalExcel(workbook, sheet, "Catálogo General de Libros", 9, listaLibros.size());

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
        createCell(row, 1, "ISBN", style);
        createCell(row, 2, "Título", style);
        createCell(row, 3, "Editorial", style);
        createCell(row, 4, "Año", style);
        createCell(row, 5, "Categoría", style);
        createCell(row, 6, "Autores", style);
        createCell(row, 7, "Stock Total", style);
        createCell(row, 8, "Stock Disponible", style);
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer val) {
            cell.setCellValue(val);
        } else if (value instanceof Boolean val) {
            cell.setCellValue(val);
        } else if (value instanceof Long val) {
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

        for (Libro libro : listaLibros) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            String catNombre = libro.getCategoria() != null ? libro.getCategoria().getNombre() : "Sin Categoría";
            String autoresStr = (libro.getAutores() != null && !libro.getAutores().isEmpty())
                    ? libro.getAutores().stream()
                        .map(a -> a.getNombres() + " " + a.getApellidos())
                        .collect(Collectors.joining(", "))
                    : "Anónimo";

            createCell(row, columnCount++, libro.getIdLibro(), style);
            createCell(row, columnCount++, libro.getIsbn(), style);
            createCell(row, columnCount++, libro.getTitulo(), style);
            createCell(row, columnCount++, libro.getEditorial() != null ? libro.getEditorial() : "-", style);
            createCell(row, columnCount++, libro.getAnioPublicacion(), style);
            createCell(row, columnCount++, catNombre, style);
            createCell(row, columnCount++, autoresStr, style);
            createCell(row, columnCount++, libro.getStockTotal(), style);
            createCell(row, columnCount++, libro.getStockDisponible(), style);
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
