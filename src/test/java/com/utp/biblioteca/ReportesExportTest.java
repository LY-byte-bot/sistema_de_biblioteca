package com.utp.biblioteca;

import com.utp.biblioteca.entities.*;
import com.utp.biblioteca.reports.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportesExportTest {

    @Test
    @DisplayName("Debe generar reporte PDF y Excel de Libros con cabezal")
    void testExportLibros() throws IOException {
        Libro libro = new Libro();
        libro.setIdLibro(1);
        libro.setIsbn("978-0132350884");
        libro.setTitulo("Clean Code");
        libro.setEditorial("Prentice Hall");
        libro.setAnioPublicacion(2008);
        libro.setStockTotal(10);
        libro.setStockDisponible(8);

        Categoria cat = new Categoria(1, "Software", "Ingeniería de Software", null);
        libro.setCategoria(cat);

        Autor autor = new Autor(1, "Robert C.", "Martin", null);
        libro.setAutores(List.of(autor));

        List<Libro> lista = List.of(libro);

        // PDF
        MockHttpServletResponse resPdf = new MockHttpServletResponse();
        LibroExporterPDF pdfExporter = new LibroExporterPDF(lista);
        pdfExporter.export(resPdf);
        byte[] pdfBytes = resPdf.getContentAsByteArray();
        assertTrue(pdfBytes.length > 0);
        // Validar cabecera PDF
        String pdfHeader = new String(pdfBytes, 0, Math.min(5, pdfBytes.length));
        assertEquals("%PDF-", pdfHeader);

        // Excel
        MockHttpServletResponse resExcel = new MockHttpServletResponse();
        LibroExporterExcel excelExporter = new LibroExporterExcel(lista);
        excelExporter.export(resExcel);
        byte[] excelBytes = resExcel.getContentAsByteArray();
        assertTrue(excelBytes.length > 0);
    }

    @Test
    @DisplayName("Debe generar reporte PDF y Excel de Usuarios")
    void testExportUsuarios() throws IOException {
        Usuario u = new Usuario();
        u.setIdUsuario(1);
        u.setDni("76543210");
        u.setNombre("Carlos Mendoza");
        u.setCorreo("carlos@utp.edu.pe");
        u.setTelefono("987654321");
        u.setRol("LECTOR");
        u.setEstado(true);
        u.setFechaCreacion(LocalDateTime.now());

        List<Usuario> lista = List.of(u);

        MockHttpServletResponse resPdf = new MockHttpServletResponse();
        new UsuarioExporterPDF(lista).export(resPdf);
        assertTrue(resPdf.getContentAsByteArray().length > 0);

        MockHttpServletResponse resExcel = new MockHttpServletResponse();
        new UsuarioExporterExcel(lista).export(resExcel);
        assertTrue(resExcel.getContentAsByteArray().length > 0);
    }

    @Test
    @DisplayName("Debe generar reporte PDF y Excel de Préstamos y Devoluciones")
    void testExportPrestamosYDevoluciones() throws IOException {
        Usuario u = new Usuario();
        u.setIdUsuario(2);
        u.setDni("12345678");
        u.setNombre("Ana Lopez");

        Libro l = new Libro();
        l.setIdLibro(2);
        l.setTitulo("Estructuras de Datos");
        l.setIsbn("978-6071505712");

        Prestamo p = new Prestamo();
        p.setIdPrestamo(100);
        p.setUsuario(u);
        p.setLibro(l);
        p.setFechaPrestamo(LocalDate.now().minusDays(5));
        p.setFechaVencimiento(LocalDate.now().plusDays(2));
        p.setEstado("ACTIVO");

        Devolucion d = new Devolucion();
        d.setIdDevolucion(50);
        d.setPrestamo(p);
        d.setFechaDevolucion(LocalDate.now());
        d.setDiasRetraso(0);
        d.setMontoMulta(BigDecimal.ZERO);
        d.setEstadoEjemplar("BUENO");
        d.setObservacion("Devuelto a tiempo sin daños");

        // Préstamo PDF y Excel
        MockHttpServletResponse resPresPdf = new MockHttpServletResponse();
        new PrestamoExporterPDF(List.of(p)).export(resPresPdf);
        assertTrue(resPresPdf.getContentAsByteArray().length > 0);

        MockHttpServletResponse resPresExcel = new MockHttpServletResponse();
        new PrestamoExporterExcel(List.of(p)).export(resPresExcel);
        assertTrue(resPresExcel.getContentAsByteArray().length > 0);

        // Devolución PDF y Excel
        MockHttpServletResponse resDevPdf = new MockHttpServletResponse();
        new DevolucionExporterPDF(List.of(d)).export(resDevPdf);
        assertTrue(resDevPdf.getContentAsByteArray().length > 0);

        MockHttpServletResponse resDevExcel = new MockHttpServletResponse();
        new DevolucionExporterExcel(List.of(d)).export(resDevExcel);
        assertTrue(resDevExcel.getContentAsByteArray().length > 0);
    }

    @Test
    @DisplayName("Debe generar reporte PDF y Excel de Reservas, Autores y Categorías")
    void testExportReservasAutoresCategorias() throws IOException {
        Usuario u = new Usuario();
        u.setNombre("Estudiante Prueba");
        u.setDni("87654321");

        Libro l = new Libro();
        l.setTitulo("Algoritmos Avanzados");

        Reserva r = new Reserva();
        r.setIdReserva(1);
        r.setUsuario(u);
        r.setLibro(l);
        r.setFechaSolicitud(LocalDateTime.now());
        r.setEstado("PENDIENTE");

        Autor a = new Autor(1, "Donald", "Knuth", List.of(l));
        Categoria c = new Categoria(1, "Computación", "Ciencias de la Computación", List.of(l));

        MockHttpServletResponse res1 = new MockHttpServletResponse();
        new ReservaExporterPDF(List.of(r)).export(res1);
        assertTrue(res1.getContentAsByteArray().length > 0);

        MockHttpServletResponse res2 = new MockHttpServletResponse();
        new ReservaExporterExcel(List.of(r)).export(res2);
        assertTrue(res2.getContentAsByteArray().length > 0);

        MockHttpServletResponse res3 = new MockHttpServletResponse();
        new AutorExporterPDF(List.of(a)).export(res3);
        assertTrue(res3.getContentAsByteArray().length > 0);

        MockHttpServletResponse res4 = new MockHttpServletResponse();
        new AutorExporterExcel(List.of(a)).export(res4);
        assertTrue(res4.getContentAsByteArray().length > 0);

        MockHttpServletResponse res5 = new MockHttpServletResponse();
        new CategoriaExporterPDF(List.of(c)).export(res5);
        assertTrue(res5.getContentAsByteArray().length > 0);

        MockHttpServletResponse res6 = new MockHttpServletResponse();
        new CategoriaExporterExcel(List.of(c)).export(res6);
        assertTrue(res6.getContentAsByteArray().length > 0);
    }

    @Test
    @DisplayName("Debe generar reporte PDF y Excel de Roles, Contenidos Digitales e Historial")
    void testExportRolesContenidosHistorial() throws IOException {
        Rol rol = new Rol(1, "ADMIN", "Acceso completo al sistema");
        ContenidoDigital cd = new ContenidoDigital();
        cd.setIdContenido(1);
        cd.setTipoArchivo("application/pdf");
        cd.setChecksum("sha256-abc123456");
        cd.setFechaIndexacion(LocalDateTime.now());

        HistorialBusqueda hb = new HistorialBusqueda();
        hb.setIdBusqueda(1);
        hb.setTermino("Spring Boot");
        hb.setFiltros("categoria=1");
        hb.setFechaHora(LocalDateTime.now());

        MockHttpServletResponse r1 = new MockHttpServletResponse();
        new RolExporterPDF(List.of(rol)).export(r1);
        assertTrue(r1.getContentAsByteArray().length > 0);

        MockHttpServletResponse r2 = new MockHttpServletResponse();
        new RolExporterExcel(List.of(rol)).export(r2);
        assertTrue(r2.getContentAsByteArray().length > 0);

        MockHttpServletResponse r3 = new MockHttpServletResponse();
        new ContenidoDigitalExporterPDF(List.of(cd)).export(r3);
        assertTrue(r3.getContentAsByteArray().length > 0);

        MockHttpServletResponse r4 = new MockHttpServletResponse();
        new ContenidoDigitalExporterExcel(List.of(cd)).export(r4);
        assertTrue(r4.getContentAsByteArray().length > 0);

        MockHttpServletResponse r5 = new MockHttpServletResponse();
        new HistorialBusquedaExporterPDF(List.of(hb)).export(r5);
        assertTrue(r5.getContentAsByteArray().length > 0);

        MockHttpServletResponse r6 = new MockHttpServletResponse();
        new HistorialBusquedaExporterExcel(List.of(hb)).export(r6);
        assertTrue(r6.getContentAsByteArray().length > 0);
    }
}
