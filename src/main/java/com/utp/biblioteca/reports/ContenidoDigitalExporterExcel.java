package com.utp.biblioteca.reports;

import com.utp.biblioteca.entities.ContenidoDigital;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ContenidoDigitalExporterExcel {

    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private final List<ContenidoDigital> listaContenidos;

    public ContenidoDigitalExporterExcel(List<ContenidoDigital> listaContenidos) {
        this.listaContenidos = listaContenidos;
        this.workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("ContenidosDigitales");
        ReporteHeaderUtil.agregarCabezalExcel(workbook, sheet, "Repositorio de Contenidos Digitales", 6, listaContenidos.size());

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

        createCell(row, 0, "ID Contenido", style);
        createCell(row, 1, "ID Libro", style);
        createCell(row, 2, "Título del Libro", style);
        createCell(row, 3, "Tipo Archivo", style);
        createCell(row, 4, "Checksum / Hash", style);
        createCell(row, 5, "Fecha Indexación", style);
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

        for (ContenidoDigital cd : listaContenidos) {
            Row row = sheet.createRow(rowCount++);
            int col = 0;

            Integer idLibro = cd.getLibro() != null ? cd.getLibro().getIdLibro() : null;
            String tituloLibro = cd.getLibro() != null ? cd.getLibro().getTitulo() : "-";
            String fIdx = cd.getFechaIndexacion() != null ? cd.getFechaIndexacion().format(dtf) : "-";

            createCell(row, col++, cd.getIdContenido(), style);
            createCell(row, col++, idLibro != null ? idLibro : "-", style);
            createCell(row, col++, tituloLibro, style);
            createCell(row, col++, cd.getTipoArchivo() != null ? cd.getTipoArchivo() : "-", style);
            createCell(row, col++, cd.getChecksum() != null ? cd.getChecksum() : cd.getUrlArchivo(), style);
            createCell(row, col++, fIdx, style);
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
