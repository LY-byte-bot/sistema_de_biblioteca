package com.utp.biblioteca.reports;

import com.utp.biblioteca.entities.Categoria;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.List;

public class CategoriaExporterExcel {

    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private final List<Categoria> listaCategorias;

    public CategoriaExporterExcel(List<Categoria> listaCategorias) {
        this.listaCategorias = listaCategorias;
        this.workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Categorias");
        ReporteHeaderUtil.agregarCabezalExcel(workbook, sheet, "Clasificación de Categorías Bibliográficas", 4, listaCategorias.size());

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

        createCell(row, 0, "ID Categoría", style);
        createCell(row, 1, "Nombre de Categoría", style);
        createCell(row, 2, "Descripción", style);
        createCell(row, 3, "Cantidad de Libros", style);
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

        for (Categoria c : listaCategorias) {
            Row row = sheet.createRow(rowCount++);
            int col = 0;

            int cantLibros = (c.getLibros() != null) ? c.getLibros().size() : 0;

            createCell(row, col++, c.getIdCategoria(), style);
            createCell(row, col++, c.getNombre() != null ? c.getNombre() : "-", style);
            createCell(row, col++, c.getDescripcion() != null ? c.getDescripcion() : "-", style);
            createCell(row, col++, cantLibros, style);
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
