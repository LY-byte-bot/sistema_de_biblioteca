package com.utp.biblioteca.controller;

import com.utp.biblioteca.entities.Libro;
import com.utp.biblioteca.entities.Prestamo;
import com.utp.biblioteca.entities.Usuario;
import com.utp.biblioteca.service.CategoriaService;
import com.utp.biblioteca.service.LibroService;
import com.utp.biblioteca.service.PrestamoService;
import com.utp.biblioteca.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final LibroService libroService;
    private final UsuarioService usuarioService;
    private final PrestamoService prestamoService;
    private final CategoriaService categoriaService;

    // 7.1. Login
    @GetMapping({"/", "/login"})
    public String login() {
        return "login";
    }

    // 7.2. Registro
    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute Usuario usuario, Model model) {
        try {
            usuario.setRol("LECTOR");
            usuario.setEstado("ACTIVO");
            usuarioService.registrarUsuario(usuario);
            model.addAttribute("mensajeExito", "¡Registro exitoso! Ya puedes iniciar sesión con tu cuenta de lector.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        }
    }

    // 7.3. Catálogo de Libros
    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(name = "query", required = false) String query, Model model) {
        List<Libro> libros;
        if (query != null && !query.trim().isEmpty()) {
            libros = libroService.buscarPorFiltros(query.trim());
        } else {
            libros = libroService.listarTodos();
        }

        model.addAttribute("libros", libros);
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("categorias", categoriaService.listarCategorias());
        return "catalogo";
    }

    // 7.4. Información del Libro
    @GetMapping("/libro/{id}")
    public String detalleLibro(@PathVariable Integer id, Model model) {
        Libro libro = libroService.obtenerPorId(id).orElse(null);
        if (libro == null) {
            return "redirect:/catalogo";
        }
        model.addAttribute("libro", libro);
        return "libro-detalle";
    }

    // 7.5. Préstamos y Multas del Lector
    @GetMapping("/prestamos")
    public String misPrestamos(Model model) {
        List<Prestamo> prestamos = prestamoService.listarPrestamosActivos();
        model.addAttribute("prestamos", prestamos);
        return "prestamos";
    }

    // 7.6. Panel Admin: Préstamos & WhatsApp
    @GetMapping("/admin/prestamos")
    public String adminPrestamos(Model model) {
        List<Prestamo> prestamos = prestamoService.listarPrestamosActivos();
        model.addAttribute("prestamos", prestamos);
        return "admin-prestamos";
    }

    // 7.7. Panel Admin: Catalogación y Gestión de Libros (Registrar y Dar de Baja)
    @GetMapping("/admin/catalogacion")
    public String adminCatalogacion(@RequestParam(name = "tab", required = false, defaultValue = "formulario") String tab, Model model) {
        model.addAttribute("libros", libroService.listarTodos());
        model.addAttribute("categorias", categoriaService.listarCategorias());
        model.addAttribute("tabActiva", tab);
        model.addAttribute("nuevoLibro", new Libro());
        return "admin-catalogacion";
    }

    @PostMapping("/admin/libros/guardar")
    public String guardarLibroAdmin(@ModelAttribute Libro libro) {
        if (libro.getStockDisponible() == null) {
            libro.setStockDisponible(libro.getStockTotal());
        }
        libroService.registrarLibro(libro);
        return "redirect:/admin/catalogacion?tab=inventario";
    }

    @PostMapping("/admin/libros/eliminar/{id}")
    public String eliminarLibroAdmin(@PathVariable Integer id) {
        libroService.eliminarLibro(id);
        return "redirect:/admin/catalogacion?tab=inventario";
    }

    // 7.8. Telemetría Actuator
    @GetMapping("/admin/telemetria")
    public String adminTelemetria(Model model) {
        model.addAttribute("totalLibros", libroService.listarTodos().size());
        model.addAttribute("totalPrestamos", prestamoService.listarPrestamosActivos().size());
        return "admin-telemetria";
    }
}
