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

    // 7.1. Login y Verificación de Roles
    @GetMapping({"/", "/login"})
    public String login() {
        return "login";
    }

    private boolean esAdminOAutorizado(jakarta.servlet.http.HttpSession session) {
        if (session == null) return false;
        Usuario u = (Usuario) session.getAttribute("usuarioActual");
        if (u == null || u.getRol() == null) return false;
        String rol = u.getRol().toUpperCase().trim();
        return rol.contains("ADMIN") || rol.contains("BIBLIOTECARIO");
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam(name = "correo", required = false) String correo,
                                @RequestParam(name = "password", required = false) String password,
                                jakarta.servlet.http.HttpSession session,
                                Model model) {
        if (correo == null || correo.trim().isEmpty()) {
            model.addAttribute("error", "Por favor ingresa tu correo electrónico.");
            return "login";
        }
        String correoLimpio = correo.trim();
        var usuarioOpt = usuarioService.obtenerPorCorreo(correoLimpio);
        if (usuarioOpt.isEmpty()) {
            model.addAttribute("error", "Usuario no registrado con ese correo electrónico.");
            return "login";
        }
        Usuario u = usuarioOpt.get();
        String passIngresada = password != null ? password.trim() : "";
        String passGuardada = u.getPasswordHash() != null ? u.getPasswordHash().trim() : "";
        if (!passIngresada.equals(passGuardada)) {
            model.addAttribute("error", "Contraseña incorrecta. Por favor verifica tus credenciales.");
            return "login";
        }
        session.setAttribute("usuarioActual", u);
        String rol = u.getRol() != null ? u.getRol().toUpperCase().trim() : "LECTOR";
        if (rol.contains("ADMIN") || rol.contains("BIBLIOTECARIO")) {
            return "redirect:/admin/catalogo";
        }
        return "redirect:/catalogo";
    }

    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // 7.2. Registro
    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute Usuario usuario, 
                                   @RequestParam(name = "apellidos", required = false) String apellidos,
                                   Model model) {
        try {
            if (usuario.getNombre() != null) {
                String nom = usuario.getNombre().trim();
                if (apellidos != null && !apellidos.trim().isEmpty()) {
                    nom += " " + apellidos.trim();
                }
                usuario.setNombre(nom);
            }
            if (usuario.getDni() != null) {
                usuario.setDni(usuario.getDni().trim().replaceAll("\\s+", ""));
            }
            if (usuario.getTelefono() != null) {
                String tel = usuario.getTelefono().trim().replaceAll("[^0-9]", "");
                if (tel.length() == 11 && tel.startsWith("51")) {
                    tel = tel.substring(2);
                }
                usuario.setTelefono(tel);
            }
            if (usuario.getCorreo() != null) {
                usuario.setCorreo(usuario.getCorreo().trim().toLowerCase());
            }
            if (usuario.getPasswordHash() != null) {
                usuario.setPasswordHash(usuario.getPasswordHash().trim());
            }
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

    // 7.5. Préstamos y Multas del Lector (Cada usuario con sus propios préstamos)
    @GetMapping("/prestamos")
    public String misPrestamos(jakarta.servlet.http.HttpSession session, Model model) {
        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioActual");
        List<Prestamo> prestamos;
        if (usuarioActual != null && usuarioActual.getIdUsuario() != null) {
            prestamos = prestamoService.listarPorUsuario(usuarioActual.getIdUsuario());
        } else {
            prestamos = new ArrayList<>();
        }
        model.addAttribute("prestamos", prestamos);
        return "prestamos";
    }

    @PostMapping("/prestamos/solicitar/{idLibro}")
    public String solicitarPrestamo(@PathVariable Integer idLibro, 
                                    jakarta.servlet.http.HttpSession session, 
                                    RedirectAttributes redirectAttributes) {
        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioActual");
        if (usuarioActual == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para solicitar un préstamo.");
            return "redirect:/login";
        }
        try {
            com.utp.biblioteca.dto.PrestamoRequestDTO solicitud = new com.utp.biblioteca.dto.PrestamoRequestDTO();
            solicitud.setIdLibro(idLibro);
            solicitud.setIdUsuario(usuarioActual.getIdUsuario());
            solicitud.setDiasPrestamo(7);
            prestamoService.registrarPrestamo(solicitud);
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Préstamo solicitado con éxito! El ejemplar ha sido reservado para ti.");
            return "redirect:/prestamos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo realizar el préstamo: " + e.getMessage());
            return "redirect:/libro/" + idLibro;
        }
    }

    // 7.6. Panel Admin: Préstamos & WhatsApp
    @GetMapping("/admin/prestamos")
    public String adminPrestamos(jakarta.servlet.http.HttpSession session, Model model) {
        if (!esAdminOAutorizado(session)) {
            return "redirect:/login";
        }
        List<Prestamo> prestamos = prestamoService.listarPrestamosActivos();
        model.addAttribute("prestamos", prestamos);
        return "admin-prestamos";
    }

    // 7.7. Panel Admin: Catalogación y Gestión de Libros (Registrar y Dar de Baja)
    @GetMapping("/admin/catalogacion")
    public String adminCatalogacion(@RequestParam(name = "tab", required = false, defaultValue = "formulario") String tab, 
                                    @RequestParam(name = "exito", required = false) Boolean exito,
                                    @RequestParam(name = "error", required = false) String error,
                                    jakarta.servlet.http.HttpSession session, 
                                    Model model) {
        if (!esAdminOAutorizado(session)) {
            return "redirect:/login";
        }
        model.addAttribute("libros", libroService.listarTodos());
        model.addAttribute("categorias", categoriaService.listarCategorias());
        model.addAttribute("tabActiva", tab);
        model.addAttribute("nuevoLibro", new Libro());
        model.addAttribute("exito", exito != null && exito);
        model.addAttribute("error", error);
        return "admin-catalogacion";
    }

    @PostMapping("/admin/libros/guardar")
    public String guardarLibroAdmin(
            @ModelAttribute Libro libro,
            @RequestParam(name = "categoriaId", required = false) Integer categoriaId,
            @RequestParam(name = "autorNombre", required = false) String autorNombre,
            jakarta.servlet.http.HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!esAdminOAutorizado(session)) {
            return "redirect:/login";
        }
        try {
            if (libro.getTitulo() != null) {
                libro.setTitulo(libro.getTitulo().trim());
            }
            if (libro.getIsbn() != null) {
                libro.setIsbn(libro.getIsbn().trim());
            }
            if (libro.getAnioPublicacion() == null) {
                libro.setAnioPublicacion(2024);
            }
            if (libro.getStockTotal() == null || libro.getStockTotal() < 0) {
                libro.setStockTotal(1);
            }
            if (libro.getStockDisponible() == null) {
                libro.setStockDisponible(libro.getStockTotal());
            }
            if (libro.getIdioma() == null || libro.getIdioma().isBlank()) {
                libro.setIdioma("Español");
            }
            if (libro.getPortadaUrl() != null) {
                libro.setPortadaUrl(libro.getPortadaUrl().trim());
            }

            if (categoriaId != null && categoriaId > 0) {
                categoriaService.obtenerPorId(categoriaId).ifPresent(cat -> {
                    libro.setCategorias(new ArrayList<>(List.of(cat)));
                });
            }

            if (autorNombre != null && !autorNombre.trim().isEmpty()) {
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
                libro.setAutores(new ArrayList<>(List.of(autorGuardado)));
            }

            libroService.registrarLibro(libro);
            redirectAttributes.addAttribute("exito", true);
            return "redirect:/admin/catalogacion?tab=inventario";
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            redirectAttributes.addAttribute("error", "Error: Ya existe un libro registrado con este ISBN.");
            return "redirect:/admin/catalogacion?tab=formulario";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "No se pudo registrar el libro: " + e.getMessage());
            return "redirect:/admin/catalogacion?tab=formulario";
        }
    }

    @PostMapping("/admin/libros/eliminar/{id}")
    public String eliminarLibroAdmin(@PathVariable Integer id, 
                                     jakarta.servlet.http.HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        if (!esAdminOAutorizado(session)) {
            return "redirect:/login";
        }
        try {
            boolean eliminado = libroService.eliminarLibro(id);
            if (eliminado) {
                redirectAttributes.addAttribute("exito", true);
            } else {
                redirectAttributes.addAttribute("error", "No se encontró el libro solicitado.");
            }
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "No se puede dar de baja el libro porque tiene préstamos asociados.");
        }
        return "redirect:/admin/catalogacion?tab=inventario";
    }

    // 7.8. Telemetría Actuator
    @GetMapping("/admin/telemetria")
    public String adminTelemetria(jakarta.servlet.http.HttpSession session, Model model) {
        if (!esAdminOAutorizado(session)) {
            return "redirect:/login";
        }
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
            jakarta.servlet.http.HttpSession session,
            Model model) {
        if (!esAdminOAutorizado(session)) {
            return "redirect:/login";
        }

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
            jakarta.servlet.http.HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!esAdminOAutorizado(session)) {
            return "redirect:/login";
        }
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
    public String eliminarLibroAdminCatalogo(@PathVariable Integer id, 
                                            jakarta.servlet.http.HttpSession session, 
                                            RedirectAttributes redirectAttributes) {
        if (!esAdminOAutorizado(session)) {
            return "redirect:/login";
        }
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
