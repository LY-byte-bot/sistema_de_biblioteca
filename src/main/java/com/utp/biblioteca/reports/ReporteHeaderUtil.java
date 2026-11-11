package com.utp.biblioteca.reports;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReporteHeaderUtil {

    public static final String NOMBRE_BIBLIOTECA = "BIBLIOTECA ACADÉMICA DEL SUR";
    public static final String INSTITUCION = "UNIVERSIDAD TECNOLÓGICA DEL PERÚ (UTP)";
    public static final String DIRECCION = "Av. Tacna y Arica 160, Arequipa, Perú";
    public static final String CONTACTO = "Tel: +51 999 643 146 | Email: matiasfarmeaura@utp.edu.pe | Web: www.utp.edu.pe";

    /**
     * Inserta el cabezal con toda la información institucional de la biblioteca en un reporte PDF.
     */
    public static void agregarCabezalPdf(Document document, String tituloReporte, int totalRegistros) throws DocumentException {
        // Título Institucional
        Font fontInstitucion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(108, 117, 125));
        Paragraph pInst = new Paragraph(INSTITUCION, fontInstitucion);
        pInst.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(pInst);

        Font fontBiblioteca = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(13, 110, 253));
        Paragraph pBiblio = new Paragraph(NOMBRE_BIBLIOTECA, fontBiblioteca);
        pBiblio.setAlignment(Paragraph.ALIGN_CENTER);
        pBiblio.setSpacingAfter(4);
        document.add(pBiblio);

        Font fontDireccion = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
        Paragraph pDir = new Paragraph(DIRECCION + " • " + CONTACTO, fontDireccion);
        pDir.setAlignment(Paragraph.ALIGN_CENTER);
        pDir.setSpacingAfter(10);
        document.add(pDir);

        // Tabla con información del reporte y fecha
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100f);
        try {
            headerTable.setWidths(new float[]{3f, 2f});
        } catch (Exception ignored) {}

        PdfPCell c1 = new PdfPCell();
        c1.setBorder(Rectangle.BOTTOM);
        c1.setBorderColor(new Color(13, 110, 253));
        c1.setBorderWidth(2f);
        c1.setPaddingBottom(8);

        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(33, 37, 41));
        c1.addElement(new Paragraph(tituloReporte.toUpperCase(), fontTitulo));

        Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        c1.addElement(new Paragraph("Total de registros listados: " + totalRegistros, fontSub));

        PdfPCell c2 = new PdfPCell();
        c2.setBorder(Rectangle.BOTTOM);
        c2.setBorderColor(new Color(13, 110, 253));
        c2.setBorderWidth(2f);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setPaddingBottom(8);

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String fechaStr = df.format(new Date());

        Font fontFecha = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
        Paragraph pFecha = new Paragraph("Fecha de emisión:\n" + fechaStr, fontFecha);
        pFecha.setAlignment(Paragraph.ALIGN_RIGHT);
        c2.addElement(pFecha);

        headerTable.addCell(c1);
        headerTable.addCell(c2);
        headerTable.setSpacingAfter(15);
        document.add(headerTable);
    }

    /**
     * Inserta el cabezal con toda la información institucional de la biblioteca en una hoja Excel.
     */
    public static void agregarCabezalExcel(XSSFWorkbook workbook, XSSFSheet sheet, String tituloReporte, int totalColumnas, int totalRegistros) {
        int maxCol = Math.max(totalColumnas - 1, 3);

        // Estilo Título Principal
        CellStyle styleTitulo = workbook.createCellStyle();
        XSSFFont fontTitulo = workbook.createFont();
        fontTitulo.setBold(true);
        fontTitulo.setFontHeightInPoints((short) 14);
        fontTitulo.setColor(IndexedColors.WHITE.getIndex());
        styleTitulo.setFont(fontTitulo);
        styleTitulo.setAlignment(HorizontalAlignment.CENTER);
        styleTitulo.setVerticalAlignment(VerticalAlignment.CENTER);
        styleTitulo.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        styleTitulo.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Estilo Institución y Contacto
        CellStyle styleSub = workbook.createCellStyle();
        XSSFFont fontSub = workbook.createFont();
        fontSub.setItalic(true);
        fontSub.setFontHeightInPoints((short) 9);
        fontSub.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        styleSub.setFont(fontSub);
        styleSub.setAlignment(HorizontalAlignment.CENTER);
        styleSub.setVerticalAlignment(VerticalAlignment.CENTER);
        styleSub.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        styleSub.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Estilo Información del Reporte
        CellStyle styleInfo = workbook.createCellStyle();
        XSSFFont fontInfo = workbook.createFont();
        fontInfo.setBold(true);
        fontInfo.setFontHeightInPoints((short) 10);
        styleInfo.setFont(fontInfo);
        styleInfo.setAlignment(HorizontalAlignment.CENTER);
        styleInfo.setVerticalAlignment(VerticalAlignment.CENTER);

        // Fila 0: Nombre Biblioteca
        Row row0 = sheet.createRow(0);
        row0.setHeightInPoints(30);
        Cell cell0 = row0.createCell(0);
        cell0.setCellValue(INSTITUCION + " - " + NOMBRE_BIBLIOTECA);
        cell0.setCellStyle(styleTitulo);
        for (int i = 1; i <= maxCol; i++) {
            Cell c = row0.createCell(i);
            c.setCellStyle(styleTitulo);
        }
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, maxCol));

        // Fila 1: Dirección y Contacto
        Row row1 = sheet.createRow(1);
        row1.setHeightInPoints(18);
        Cell cell1 = row1.createCell(0);
        cell1.setCellValue(DIRECCION + " | " + CONTACTO);
        cell1.setCellStyle(styleSub);
        for (int i = 1; i <= maxCol; i++) {
            Cell c = row1.createCell(i);
            c.setCellStyle(styleSub);
        }
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, maxCol));

        // Fila 2: Título y Datos del Reporte
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String fechaStr = df.format(new Date());
        Row row2 = sheet.createRow(2);
        row2.setHeightInPoints(20);
        Cell cell2 = row2.createCell(0);
        cell2.setCellValue("REPORTE OFICIAL: " + tituloReporte.toUpperCase() + " | Emisión: " + fechaStr + " | Total Registros: " + totalRegistros);
        cell2.setCellStyle(styleInfo);
        for (int i = 1; i <= maxCol; i++) {
            Cell c = row2.createCell(i);
            c.setCellStyle(styleInfo);
        }
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, maxCol));

        // Fila 3 vacía para separación
        Row row3 = sheet.createRow(3);
        row3.setHeightInPoints(8);
    }
}
