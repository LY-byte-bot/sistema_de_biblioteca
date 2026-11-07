package com.utp.biblioteca.util;

import com.utp.biblioteca.entities.Libro;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PdfGenerator {

    /**
     * Genera un archivo PDF válido en bytes con la ficha técnica y extracto de muestra del libro.
     */
    public static byte[] generarExtractoPdf(Libro libro) {
        String titulo = libro.getTitulo() != null ? libro.getTitulo() : "Libro de la Biblioteca";
        String isbn = libro.getIsbn() != null ? libro.getIsbn() : "S/N";
        String editorial = libro.getEditorial() != null ? libro.getEditorial() : "Editorial Académica";
        String resumen = libro.getResumen() != null ? libro.getResumen() : "Sin sinopsis registrada.";
        if (resumen.length() > 300) {
            resumen = resumen.substring(0, 297) + "...";
        }

        // Construir contenido PDF en texto plano PostScript / PDF 1.4 compatible
        StringBuilder sb = new StringBuilder();
        sb.append("%PDF-1.4\n");
        sb.append("1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n");
        sb.append("2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n");
        sb.append("3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n");
        sb.append("4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n");

        // Contenido de la página
        StringBuilder stream = new StringBuilder();
        stream.append("BT\n");
        stream.append("/F1 18 Tf\n");
        stream.append("50 720 Td (BIBLIOTECA ACADEMICA DEL SUR) Tj\n");
        stream.append("/F1 12 Tf\n");
        stream.append("0 -30 Td (Extracto Digital de Consulta y Ficha Tecnica) Tj\n");
        stream.append("0 -40 Td (Titulo: ").append(sanitizar(titulo)).append(") Tj\n");
        stream.append("0 -25 Td (ISBN: ").append(sanitizar(isbn)).append(") Tj\n");
        stream.append("0 -25 Td (Editorial: ").append(sanitizar(editorial)).append(") Tj\n");
        stream.append("0 -25 Td (Anio de Edicion: ").append(libro.getAnioPublicacion() != null ? libro.getAnioPublicacion() : 2024).append(") Tj\n");
        stream.append("0 -40 Td (--- SINOPSIS DEL CONTENIDO ---) Tj\n");
        stream.append("/F1 10 Tf\n");
        stream.append("0 -25 Td (").append(sanitizar(resumen)).append(") Tj\n");
        stream.append("0 -50 Td (Nota: Este extracto digital se provee para fines academicos y de investigacion.) Tj\n");
        stream.append("0 -20 Td (Arequipa, Peru - 2026) Tj\n");
        stream.append("ET\n");

        byte[] streamBytes = stream.toString().getBytes(StandardCharsets.ISO_8859_1);

        sb.append("5 0 obj << /Length ").append(streamBytes.length).append(" >> stream\n");
        sb.append(stream.toString());
        sb.append("endstream\nendobj\n");

        sb.append("xref\n");
        sb.append("0 6\n");
        sb.append("0000000000 65535 f \n");
        sb.append("0000000009 00000 n \n");
        sb.append("0000000058 00000 n \n");
        sb.append("0000000115 00000 n \n");
        sb.append("0000000244 00000 n \n");
        sb.append("0000000318 00000 n \n");
        sb.append("trailer << /Size 6 /Root 1 0 R >>\n");
        sb.append("startxref\n");
        sb.append("500\n");
        sb.append("%%EOF\n");

        return sb.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    private static String sanitizar(String txt) {
        if (txt == null) return "";
        return txt.replace("(", "[").replace(")", "]").replace("\\", "/");
    }
}
