package com.utp.biblioteca.reports;

import com.utp.biblioteca.entities.Usuario;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UsuarioExporterExcel {

    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private final List<Usuario> listaUsuarios;

    public UsuarioExporterExcel(List<Usuario> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
        this.workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Usuarios");
        ReporteHeaderUtil.agregarCabezalExcel(workbook, sheet, "Padrón de Usuarios del Sistema", 8, listaUsuarios.size());

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
        createCell(row, 1, "DNI", style);
        createCell(row, 2, "Nombre Completo", style);
        createCell(row, 3, "Correo Electrónico", style);
        createCell(row, 4, "Teléfono / Celular", style);
        createCell(row, 5, "Rol", style);
        createCell(row, 6, "Estado", style);
        createCell(row, 7, "Fecha Registro", style);
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer val) {
            cell.setCellValue(val);
        } else if (value instanceof Boolean val) {
            cell.setCellValue(val ? "ACTIVO" : "INACTIVO");
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

        for (Usuario u : listaUsuarios) {
            Row row = sheet.createRow(rowCount++);
            int col = 0;

            String fechaStr = u.getFechaCreacion() != null ? u.getFechaCreacion().format(dtf) : "-";

            createCell(row, col++, u.getIdUsuario(), style);
            createCell(row, col++, u.getDni() != null ? u.getDni() : "-", style);
            createCell(row, col++, u.getNombre() != null ? u.getNombre() : "-", style);
            createCell(row, col++, u.getCorreo() != null ? u.getCorreo() : "-", style);
            createCell(row, col++, u.getTelefono() != null ? u.getTelefono() : "-", style);
            createCell(row, col++, u.getRol() != null ? u.getRol() : "-", style);
            createCell(row, col++, Boolean.TRUE.equals(u.getEstado()) ? "ACTIVO" : "INACTIVO", style);
            createCell(row, col++, fechaStr, style);
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
