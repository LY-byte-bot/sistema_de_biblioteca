package com.utp.biblioteca.reports;

import com.utp.biblioteca.entities.HistorialBusqueda;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistorialBusquedaExporterExcel {

    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private final List<HistorialBusqueda> listaHistorial;

    public HistorialBusquedaExporterExcel(List<HistorialBusqueda> listaHistorial) {
        this.listaHistorial = listaHistorial;
        this.workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("HistorialBusquedas");
        ReporteHeaderUtil.agregarCabezalExcel(workbook, sheet, "Auditoría de Historial de Búsquedas", 5, listaHistorial.size());

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

        createCell(row, 0, "ID Búsqueda", style);
        createCell(row, 1, "Usuario / Solicitante", style);
        createCell(row, 2, "Término de Consulta", style);
        createCell(row, 3, "Filtros Aplicados", style);
        createCell(row, 4, "Fecha y Hora", style);
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

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        for (HistorialBusqueda hb : listaHistorial) {
            Row row = sheet.createRow(rowCount++);
            int col = 0;

            String usuarioStr = (hb.getUsuario() != null)
                    ? hb.getUsuario().getNombre() + " (" + hb.getUsuario().getCorreo() + ")"
                    : "Anónimo (Invitado)";
            String fechaStr = hb.getFechaHora() != null ? hb.getFechaHora().format(dtf) : "-";

            createCell(row, col++, hb.getIdBusqueda(), style);
            createCell(row, col++, usuarioStr, style);
            createCell(row, col++, hb.getTermino() != null ? hb.getTermino() : "-", style);
            createCell(row, col++, hb.getFiltros() != null ? hb.getFiltros() : "Búsqueda libre", style);
            createCell(row, col++, fechaStr, style);
        }

        for (int i = 0; i < 5; i++) {
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
