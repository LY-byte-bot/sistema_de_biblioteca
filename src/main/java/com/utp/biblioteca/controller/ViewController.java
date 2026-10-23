package com.utp.biblioteca.controller;

import com.utp.biblioteca.entities.Autor;
import com.utp.biblioteca.entities.Libro;
import com.utp.biblioteca.entities.Prestamo;
import com.utp.biblioteca.entities.Usuario;
import com.utp.biblioteca.service.AutorService;
import com.utp.biblioteca.service.CategoriaService;
import com.utp.biblioteca.service.LibroService;
import com.utp.biblioteca.service.PrestamoService;
import com.utp.biblioteca.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final LibroService libroService;
    private final UsuarioService usuarioService;
    private final PrestamoService prestamoService;
    private final CategoriaService categoriaService;
    private final AutorService autorService;

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
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            model.addAttribute("error", "Ya existe una cuenta registrada con este correo electrónico o DNI.");
            return "registro";
        } catch (Exception e) {
            model.addAttribute("error", "No se pudo registrar: " + e.getMessage());
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

    // 7.9. Panel Admin: Catálogo de Libros con Edición y Eliminación
    @GetMapping("/admin/catalogo")
    public String adminCatalogo(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "categoriaId", required = false) Integer categoriaId,
            @RequestParam(name = "disponibilidad", required = false) String disponibilidad,
            @RequestParam(name = "actualizado", required = false) Boolean actualizado,
            @RequestParam(name = "eliminado", required = false) Boolean eliminado,
            @RequestParam(name = "error", required = false) String error,
            Model model) {

        List<Libro> todosLosLibros = libroService.listarTodos();
        List<Libro> libros;

        if (query != null && !query.trim().isEmpty()) {
            libros = libroService.buscarPorFiltros(query.trim());
        } else {
            libros = new ArrayList<>(todosLosLibros);
        }

        if (categoriaId != null && categoriaId > 0) {
            libros = libros.stream()
                    .filter(l -> l.getCategorias() != null && l.getCategorias().stream().anyMatch(c -> c.getIdCategoria().equals(categoriaId)))
                    .toList();
        }

        if ("disponibles".equalsIgnoreCase(disponibilidad)) {
            libros = libros.stream()
                    .filter(l -> l.getStockDisponible() != null && l.getStockDisponible() > 0)
                    .toList();
        } else if ("agotados".equalsIgnoreCase(disponibilidad)) {
            libros = libros.stream()
                    .filter(l -> l.getStockDisponible() == null || l.getStockDisponible() <= 0)
                    .toList();
        }

        long totalLibros = todosLosLibros.size();
        long totalEjemplares = todosLosLibros.stream().mapToLong(l -> l.getStockTotal() != null ? l.getStockTotal() : 0).sum();
        long disponibles = todosLosLibros.stream().filter(l -> l.getStockDisponible() != null && l.getStockDisponible() > 0).count();
        long sinStock = todosLosLibros.stream().filter(l -> l.getStockDisponible() == null || l.getStockDisponible() <= 0).count();

        model.addAttribute("libros", libros);
        model.addAttribute("categorias", categoriaService.listarCategorias());
        model.addAttribute("autores", autorService.listarAutores());
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("disponibilidadSeleccionada", disponibilidad != null ? disponibilidad : "todos");
        model.addAttribute("totalLibros", totalLibros);
        model.addAttribute("totalEjemplares", totalEjemplares);
        model.addAttribute("disponibles", disponibles);
        model.addAttribute("sinStock", sinStock);
        model.addAttribute("actualizado", actualizado != null && actualizado);
        model.addAttribute("eliminado", eliminado != null && eliminado);
        model.addAttribute("error", error);

        return "admin-catalogo";
    }

    @PostMapping("/admin/catalogo/editar/{id}")
    public String editarLibroAdmin(
            @PathVariable Integer id,
            @ModelAttribute Libro libroForm,
            @RequestParam(name = "categoriaId", required = false) Integer categoriaId,
            @RequestParam(name = "autorId", required = false) Integer autorId,
            @RequestParam(name = "autorNombre", required = false) String autorNombre,
            RedirectAttributes redirectAttributes) {
        try {
            if (categoriaId != null && categoriaId > 0) {
                categoriaService.obtenerPorId(categoriaId).ifPresent(cat -> {
                    libroForm.setCategorias(new ArrayList<>(List.of(cat)));
                });
            }

            if (autorId != null && autorId > 0) {
                autorService.obtenerPorId(autorId).ifPresent(aut -> {
                    libroForm.setAutores(new ArrayList<>(List.of(aut)));
                });
            } else if (autorNombre != null && !autorNombre.trim().isEmpty()) {
                String nombreCompleto = autorNombre.trim();
                Autor autorObj = new Autor();
                int lastSpace = nombreCompleto.lastIndexOf(" ");
                if (lastSpace > 0) {
                    autorObj.setNombres(nombreCompleto.substring(0, lastSpace));
                    autorObj.setApellidos(nombreCompleto.substring(lastSpace + 1));
                } else {
                    autorObj.setNombres(nombreCompleto);
                    autorObj.setApellidos("");
                }
                Autor autorGuardado = autorService.registrarAutor(autorObj);
                libroForm.setAutores(new ArrayList<>(List.of(autorGuardado)));
            }

            if (libroForm.getStockTotal() != null && libroForm.getStockDisponible() != null) {
                if (libroForm.getStockDisponible() > libroForm.getStockTotal()) {
                    libroForm.setStockDisponible(libroForm.getStockTotal());
                }
            } else if (libroForm.getStockDisponible() == null && libroForm.getStockTotal() != null) {
                libroForm.setStockDisponible(libroForm.getStockTotal());
            }

            libroService.actualizarLibro(id, libroForm);
            redirectAttributes.addAttribute("actualizado", true);
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Error al actualizar el libro: " + e.getMessage());
        }
        return "redirect:/admin/catalogo";
    }

    @PostMapping("/admin/catalogo/eliminar/{id}")
    public String eliminarLibroAdminCatalogo(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            boolean eliminado = libroService.eliminarLibro(id);
            if (eliminado) {
                redirectAttributes.addAttribute("eliminado", true);
            } else {
                redirectAttributes.addAttribute("error", "No se encontró el libro solicitado para eliminar.");
            }
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "No se puede dar de baja el libro porque posee registros históricos o activos de préstamos asociados.");
        }
        return "redirect:/admin/catalogo";
    }
}
