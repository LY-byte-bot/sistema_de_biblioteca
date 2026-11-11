package com.utp.biblioteca.controller;

import com.utp.biblioteca.entities.*;
import com.utp.biblioteca.reports.*;
import com.utp.biblioteca.repository.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReporteController {

    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;
    private final DevolucionRepository devolucionRepository;
    private final ReservaRepository reservaRepository;
    private final AutorRepository autorRepository;
    private final CategoriaRepository categoriaRepository;
    private final RolRepository rolRepository;
    private final ContenidoDigitalRepository contenidoDigitalRepository;
    private final HistorialBusquedaRepository historialBusquedaRepository;

    /**
     * Panel administrativo central de reportes de todas las tablas de la base de datos.
     */
    @GetMapping("/admin/reportes")
    public String panelReportes(Model model) {
        model.addAttribute("totalLibros", libroRepository.count());
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalPrestamos", prestamoRepository.count());
        model.addAttribute("totalDevoluciones", devolucionRepository.count());
        model.addAttribute("totalReservas", reservaRepository.count());
        model.addAttribute("totalAutores", autorRepository.count());
        model.addAttribute("totalCategorias", categoriaRepository.count());
        model.addAttribute("totalRoles", rolRepository.count());
        model.addAttribute("totalContenidos", contenidoDigitalRepository.count());
        model.addAttribute("totalHistorial", historialBusquedaRepository.count());
        return "admin-reportes";
    }

    // ==========================================
    // 1. REPORTES: LIBROS (tbl_libro)
    // ==========================================
    @GetMapping({"/reportes/libros/pdf", "/libros/export/pdf", "/export/pdf"})
    public void exportarLibrosPdf(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "libros", "pdf", "application/pdf");
        List<Libro> lista = libroRepository.findAll();
        LibroExporterPDF exporter = new LibroExporterPDF(lista);
        exporter.export(response);
    }

    @GetMapping({"/reportes/libros/excel", "/libros/export/excel", "/export/excel"})
    public void exportarLibrosExcel(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "libros", "xlsx", "application/octet-stream");
        List<Libro> lista = libroRepository.findAll();
        LibroExporterExcel exporter = new LibroExporterExcel(lista);
        exporter.export(response);
    }

    // ==========================================
    // 2. REPORTES: USUARIOS (tbl_usuario)
    // ==========================================
    @GetMapping({"/reportes/usuarios/pdf", "/usuarios/export/pdf"})
    public void exportarUsuariosPdf(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "usuarios", "pdf", "application/pdf");
        List<Usuario> lista = usuarioRepository.findAll();
        UsuarioExporterPDF exporter = new UsuarioExporterPDF(lista);
        exporter.export(response);
    }

    @GetMapping({"/reportes/usuarios/excel", "/usuarios/export/excel"})
    public void exportarUsuariosExcel(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "usuarios", "xlsx", "application/octet-stream");
        List<Usuario> lista = usuarioRepository.findAll();
        UsuarioExporterExcel exporter = new UsuarioExporterExcel(lista);
        exporter.export(response);
    }

    // ==========================================
    // 3. REPORTES: PRÉSTAMOS (tbl_prestamo)
    // ==========================================
    @GetMapping({"/reportes/prestamos/pdf", "/prestamos/export/pdf"})
    public void exportarPrestamosPdf(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "prestamos", "pdf", "application/pdf");
        List<Prestamo> lista = prestamoRepository.findAll();
        PrestamoExporterPDF exporter = new PrestamoExporterPDF(lista);
        exporter.export(response);
    }

    @GetMapping({"/reportes/prestamos/excel", "/prestamos/export/excel"})
    public void exportarPrestamosExcel(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "prestamos", "xlsx", "application/octet-stream");
        List<Prestamo> lista = prestamoRepository.findAll();
        PrestamoExporterExcel exporter = new PrestamoExporterExcel(lista);
        exporter.export(response);
    }

    // ==========================================
    // 4. REPORTES: DEVOLUCIONES (tbl_devolucion)
    // ==========================================
    @GetMapping({"/reportes/devoluciones/pdf", "/devoluciones/export/pdf"})
    public void exportarDevolucionesPdf(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "devoluciones", "pdf", "application/pdf");
        List<Devolucion> lista = devolucionRepository.findAll();
        DevolucionExporterPDF exporter = new DevolucionExporterPDF(lista);
        exporter.export(response);
    }

    @GetMapping({"/reportes/devoluciones/excel", "/devoluciones/export/excel"})
    public void exportarDevolucionesExcel(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "devoluciones", "xlsx", "application/octet-stream");
        List<Devolucion> lista = devolucionRepository.findAll();
        DevolucionExporterExcel exporter = new DevolucionExporterExcel(lista);
        exporter.export(response);
    }

    // ==========================================
    // 5. REPORTES: RESERVAS (tbl_reserva)
    // ==========================================
    @GetMapping({"/reportes/reservas/pdf", "/reservas/export/pdf"})
    public void exportarReservasPdf(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "reservas", "pdf", "application/pdf");
        List<Reserva> lista = reservaRepository.findAll();
        ReservaExporterPDF exporter = new ReservaExporterPDF(lista);
        exporter.export(response);
    }

    @GetMapping({"/reportes/reservas/excel", "/reservas/export/excel"})
    public void exportarReservasExcel(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "reservas", "xlsx", "application/octet-stream");
        List<Reserva> lista = reservaRepository.findAll();
        ReservaExporterExcel exporter = new ReservaExporterExcel(lista);
        exporter.export(response);
    }

    // ==========================================
    // 6. REPORTES: AUTORES (tbl_autor)
    // ==========================================
    @GetMapping({"/reportes/autores/pdf", "/autores/export/pdf"})
    public void exportarAutoresPdf(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "autores", "pdf", "application/pdf");
        List<Autor> lista = autorRepository.findAll();
        AutorExporterPDF exporter = new AutorExporterPDF(lista);
        exporter.export(response);
    }

    @GetMapping({"/reportes/autores/excel", "/autores/export/excel"})
    public void exportarAutoresExcel(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "autores", "xlsx", "application/octet-stream");
        List<Autor> lista = autorRepository.findAll();
        AutorExporterExcel exporter = new AutorExporterExcel(lista);
        exporter.export(response);
    }

    // ==========================================
    // 7. REPORTES: CATEGORÍAS (tbl_categoria)
    // ==========================================
    @GetMapping({"/reportes/categorias/pdf", "/categorias/export/pdf"})
    public void exportarCategoriasPdf(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "categorias", "pdf", "application/pdf");
        List<Categoria> lista = categoriaRepository.findAll();
        CategoriaExporterPDF exporter = new CategoriaExporterPDF(lista);
        exporter.export(response);
    }

    @GetMapping({"/reportes/categorias/excel", "/categorias/export/excel"})
    public void exportarCategoriasExcel(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "categorias", "xlsx", "application/octet-stream");
        List<Categoria> lista = categoriaRepository.findAll();
        CategoriaExporterExcel exporter = new CategoriaExporterExcel(lista);
        exporter.export(response);
    }

    // ==========================================
    // 8. REPORTES: ROLES (tbl_rol)
    // ==========================================
    @GetMapping({"/reportes/roles/pdf", "/roles/export/pdf"})
    public void exportarRolesPdf(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "roles", "pdf", "application/pdf");
        List<Rol> lista = rolRepository.findAll();
        RolExporterPDF exporter = new RolExporterPDF(lista);
        exporter.export(response);
    }

    @GetMapping({"/reportes/roles/excel", "/roles/export/excel"})
    public void exportarRolesExcel(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "roles", "xlsx", "application/octet-stream");
        List<Rol> lista = rolRepository.findAll();
        RolExporterExcel exporter = new RolExporterExcel(lista);
        exporter.export(response);
    }

    // ==========================================
    // 9. REPORTES: CONTENIDOS DIGITALES (tbl_contenido_digital)
    // ==========================================
    @GetMapping({"/reportes/contenidos/pdf", "/contenidos/export/pdf"})
    public void exportarContenidosPdf(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "contenidos_digitales", "pdf", "application/pdf");
        List<ContenidoDigital> lista = contenidoDigitalRepository.findAll();
        ContenidoDigitalExporterPDF exporter = new ContenidoDigitalExporterPDF(lista);
        exporter.export(response);
    }

    @GetMapping({"/reportes/contenidos/excel", "/contenidos/export/excel"})
    public void exportarContenidosExcel(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "contenidos_digitales", "xlsx", "application/octet-stream");
        List<ContenidoDigital> lista = contenidoDigitalRepository.findAll();
        ContenidoDigitalExporterExcel exporter = new ContenidoDigitalExporterExcel(lista);
        exporter.export(response);
    }

    // ==========================================
    // 10. REPORTES: HISTORIAL DE BÚSQUEDAS (tbl_historial_busqueda)
    // ==========================================
    @GetMapping({"/reportes/historial/pdf", "/historial/export/pdf"})
    public void exportarHistorialPdf(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "historial_busquedas", "pdf", "application/pdf");
        List<HistorialBusqueda> lista = historialBusquedaRepository.findAll();
        HistorialBusquedaExporterPDF exporter = new HistorialBusquedaExporterPDF(lista);
        exporter.export(response);
    }

    @GetMapping({"/reportes/historial/excel", "/historial/export/excel"})
    public void exportarHistorialExcel(HttpServletResponse response) throws IOException {
        prepararRespuestaHeader(response, "historial_busquedas", "xlsx", "application/octet-stream");
        List<HistorialBusqueda> lista = historialBusquedaRepository.findAll();
        HistorialBusquedaExporterExcel exporter = new HistorialBusquedaExporterExcel(lista);
        exporter.export(response);
    }

    // ==========================================
    // ENDPOINT DINÁMICO POR NOMBRE DE TABLA
    // ==========================================
    @GetMapping("/reportes/{tabla}/pdf")
    public void exportarDinamicoPdf(@PathVariable String tabla, HttpServletResponse response) throws IOException {
        switch (tabla.toLowerCase().trim()) {
            case "libros" -> exportarLibrosPdf(response);
            case "usuarios" -> exportarUsuariosPdf(response);
            case "prestamos" -> exportarPrestamosPdf(response);
            case "devoluciones" -> exportarDevolucionesPdf(response);
            case "reservas" -> exportarReservasPdf(response);
            case "autores" -> exportarAutoresPdf(response);
            case "categorias" -> exportarCategoriasPdf(response);
            case "roles" -> exportarRolesPdf(response);
            case "contenidos" -> exportarContenidosPdf(response);
            case "historial" -> exportarHistorialPdf(response);
            default -> exportarLibrosPdf(response);
        }
    }

    @GetMapping("/reportes/{tabla}/excel")
    public void exportarDinamicoExcel(@PathVariable String tabla, HttpServletResponse response) throws IOException {
        switch (tabla.toLowerCase().trim()) {
            case "libros" -> exportarLibrosExcel(response);
            case "usuarios" -> exportarUsuariosExcel(response);
            case "prestamos" -> exportarPrestamosExcel(response);
            case "devoluciones" -> exportarDevolucionesExcel(response);
            case "reservas" -> exportarReservasExcel(response);
            case "autores" -> exportarAutoresExcel(response);
            case "categorias" -> exportarCategoriasExcel(response);
            case "roles" -> exportarRolesExcel(response);
            case "contenidos" -> exportarContenidosExcel(response);
            case "historial" -> exportarHistorialExcel(response);
            default -> exportarLibrosExcel(response);
        }
    }

    /**
     * Configura el Content-Type y cabeceras HTTP Content-Disposition con fecha y hora.
     */
    private void prepararRespuestaHeader(HttpServletResponse response, String nombreArchivo, String extension, String contentType) {
        response.setContentType(contentType);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormat.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=" + nombreArchivo + "_" + currentDateTime + "." + extension;
        response.setHeader(headerKey, headerValue);
    }
}
