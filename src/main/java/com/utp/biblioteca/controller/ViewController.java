package com.utp.biblioteca.controller;

import com.utp.biblioteca.entities.Autor;
import com.utp.biblioteca.entities.Libro;
import com.utp.biblioteca.entities.Prestamo;
import com.utp.biblioteca.entities.Usuario;
import com.utp.biblioteca.service.AutorService;
import com.utp.biblioteca.service.CategoriaService;
import com.utp.biblioteca.service.DevolucionService;
import com.utp.biblioteca.dto.DevolucionRequestDTO;
import com.utp.biblioteca.entities.Devolucion;
import java.math.BigDecimal;
import java.util.Map;
import com.utp.biblioteca.service.LibroService;
import com.utp.biblioteca.service.PrestamoService;
import com.utp.biblioteca.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final DevolucionService devolucionService;
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
        if (Boolean.FALSE.equals(u.getEstado())) {
            model.addAttribute("error", "Tu cuenta se encuentra inactiva (estado 0). Contacta al administrador.");
            return "login";
        }
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

    // Soporte PUT Login para Postman con JSON
    @PutMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> loginPutJson(@RequestBody Map<String, String> credenciales, jakarta.servlet.http.HttpSession session) {
        String correo = credenciales.get("correo");
        String password = credenciales.get("password");
        if (password == null) {
            password = credenciales.get("passwordHash");
        }

        if (correo == null || correo.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo es obligatorio"));
        }
        var usuarioOpt = usuarioService.obtenerPorCorreo(correo.trim());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuario no registrado"));
        }
        Usuario u = usuarioOpt.get();
        if (Boolean.FALSE.equals(u.getEstado())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Usuario inactivo (estado 0)"));
        }
        String passGuardada = u.getPasswordHash() != null ? u.getPasswordHash().trim() : "";
        if (password != null && !password.trim().equals(passGuardada)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Contraseña incorrecta"));
        }
        session.setAttribute("usuarioActual", u);
        return ResponseEntity.ok(Map.of(
            "mensaje", "Login exitoso vía PUT",
            "usuario", u
        ));
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
    public String procesarRegistro(@RequestParam(name = "nombre", required = false) String nombre,
                                   @RequestParam(name = "apellidos", required = false) String apellidos,
                                   @RequestParam(name = "dni", required = false) String dni,
                                   @RequestParam(name = "telefono", required = false) String telefono,
                                   @RequestParam(name = "correo", required = false) String correo,
                                   @RequestParam(name = "passwordHash", required = false) String passwordHash,
                                   @RequestParam(name = "confirmPassword", required = false) String confirmPassword,
                                   Model model) {
        if (nombre == null || nombre.trim().isEmpty() ||
            apellidos == null || apellidos.trim().isEmpty() ||
            dni == null || dni.trim().isEmpty() ||
            telefono == null || telefono.trim().isEmpty() ||
            correo == null || correo.trim().isEmpty() ||
            passwordHash == null || passwordHash.trim().isEmpty()) {
            model.addAttribute("error", "Todos los campos con asterisco (*) son obligatorios.");
            return "registro";
        }

        String nom = nombre.trim();
        String ape = apellidos.trim();
        String dniLimpio = dni.trim().replaceAll("\\s+", "");
        String telLimpio = telefono.trim().replaceAll("[^0-9]", "");
        String correoLimpio = correo.trim().toLowerCase();
        String pass = passwordHash.trim();
        String confPass = confirmPassword != null ? confirmPassword.trim() : "";

        if (!nom.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]{3,75}$")) {
            model.addAttribute("error", "El nombre debe tener entre 3 y 75 letras y espacios (sin números ni símbolos especiales).");
            return "registro";
        }
        if (!ape.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]{3,75}$")) {
            model.addAttribute("error", "Los apellidos deben tener entre 3 y 75 letras y espacios (sin números ni símbolos especiales).");
            return "registro";
        }
        if (!dniLimpio.matches("^[0-9]{8}$")) {
            model.addAttribute("error", "El DNI debe tener exactamente 8 dígitos numéricos.");
            return "registro";
        }
        if (!telLimpio.matches("^[0-9]{9}$")) {
            model.addAttribute("error", "El celular debe contener exactamente 9 dígitos numéricos.");
            return "registro";
        }
        if (!correoLimpio.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            model.addAttribute("error", "El formato del correo electrónico ingresado no es válido.");
            return "registro";
        }
        if (pass.length() < 6) {
            model.addAttribute("error", "La contraseña debe tener un mínimo de 6 caracteres.");
            return "registro";
        }
        if (!pass.equals(confPass)) {
            model.addAttribute("error", "Las contraseñas no coinciden. Por favor verifícalas.");
            return "registro";
        }

        try {
            Usuario usuario = new Usuario();
            usuario.setNombre(nom + " " + ape);
            usuario.setDni(dniLimpio);
            usuario.setTelefono(telLimpio);
            usuario.setCorreo(correoLimpio);
            usuario.setPasswordHash(pass);
            usuario.setRol("LECTOR");
            usuario.setEstado(true); // 1 = activo (booleano)

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

    // 7.3. Catálogo de Libros (Lector)
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

    // PUT Catálogo Lector: Permite editar el JSON en Postman
    @PutMapping(value = {"/catalogo", "/catalogo/{id}"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> editarCatalogoJson(@PathVariable(required = false) Integer id, @RequestBody Libro libro) {
        Integer libroId = (id != null) ? id : libro.getIdLibro();
        if (libroId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe proporcionar el idLibro en la URL o en el cuerpo JSON"));
        }
        try {
            Libro actualizado = libroService.actualizarLibro(libroId, libro);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
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
    public String misPrestamos(jakarta.servlet.http.HttpSession session, Model model) {
        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioActual");
        List<Prestamo> prestamosActivos = new ArrayList<>();
        List<Devolucion> devoluciones = new ArrayList<>();
        BigDecimal totalMulta = BigDecimal.ZERO;

        if (usuarioActual != null && usuarioActual.getIdUsuario() != null) {
            prestamosActivos = prestamoService.listarActivosPorUsuario(usuarioActual.getIdUsuario());
            devoluciones = devolucionService.listarPorUsuario(usuarioActual.getIdUsuario());

            for (Devolucion d : devoluciones) {
                if (d.getMontoMulta() != null) {
                    totalMulta = totalMulta.add(d.getMontoMulta());
                }
            }

            java.time.LocalDate hoy = java.time.LocalDate.now();
            for (Prestamo p : prestamosActivos) {
                if (p.getFechaVencimiento() != null && p.getFechaVencimiento().isBefore(hoy)) {
                    long dias = java.time.temporal.ChronoUnit.DAYS.between(p.getFechaVencimiento(), hoy);
                    totalMulta = totalMulta.add(BigDecimal.valueOf(dias).multiply(new BigDecimal("2.50")));
                }
            }
        }
        model.addAttribute("prestamos", prestamosActivos);
        model.addAttribute("prestamosActivos", prestamosActivos);
        model.addAttribute("devoluciones", devoluciones);
        model.addAttribute("totalMulta", totalMulta);
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
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Préstamo registrado exitosamente! Recuerda devolverlo en la fecha límite.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo realizar el préstamo: " + e.getMessage());
        }
        return "redirect:/prestamos";
    }

    // 7.6. Panel Admin: Préstamos
    @GetMapping("/admin/prestamos")
    public String adminPrestamos(jakarta.servlet.http.HttpSession session, Model model) {
        if (!esAdminOAutorizado(session)) {
            return "redirect:/login";
        }
        List<Prestamo> prestamos = prestamoService.listarPrestamosActivos();
        model.addAttribute("prestamos", prestamos);
        return "admin-prestamos";
    }

    // PUT Préstamos Admin: Permite editar el JSON en Postman
    @PutMapping(value = {"/admin/prestamos", "/admin/prestamos/{id}"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> editarAdminPrestamosJson(@PathVariable(required = false) Integer id, @RequestBody Map<String, Object> body) {
        Integer prestamoId = id;
        if (prestamoId == null && body.get("idPrestamo") != null) {
            prestamoId = Integer.valueOf(body.get("idPrestamo").toString());
        }
        if (prestamoId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe proporcionar el idPrestamo en la URL o en el cuerpo JSON"));
        }

        var prestamoOpt = prestamoService.obtenerPorId(prestamoId);
        if (prestamoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Préstamo no encontrado con ID: " + prestamoId));
        }
        Prestamo prestamo = prestamoOpt.get();

        if (body.containsKey("estado")) {
            String nuevoEstado = body.get("estado").toString();
            if ("DEVUELTO".equalsIgnoreCase(nuevoEstado)) {
                DevolucionRequestDTO devDto = new DevolucionRequestDTO();
                devDto.setIdPrestamo(prestamoId);
                devDto.setEstadoEjemplar(body.getOrDefault("estadoEjemplar", "BUENO").toString());
                devDto.setObservacion(body.getOrDefault("observacion", "Devolución actualizada vía PUT JSON").toString());
                try {
                    Devolucion dev = devolucionService.procesarDevolucion(devDto);
                    return ResponseEntity.ok(Map.of("mensaje", "Préstamo marcado como DEVUELTO con éxito", "devolucion", dev));
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
                }
            } else {
                prestamo.setEstado(nuevoEstado);
            }
        }
        if (body.containsKey("fechaVencimiento")) {
            prestamo.setFechaVencimiento(java.time.LocalDate.parse(body.get("fechaVencimiento").toString()));
        }
        return ResponseEntity.ok(Map.of("mensaje", "Préstamo actualizado exitosamente vía PUT", "prestamo", prestamo));
    }

    @GetMapping("/admin/prestamos/devolver")
    public String redirigirDevolucionGet() {
        return "redirect:/admin/prestamos";
    }

    @PostMapping("/admin/prestamos/devolver")
    public String registrarDevolucionAdmin(
            @RequestParam(name = "idPrestamo") Integer idPrestamo,
            @RequestParam(name = "estadoEjemplar", required = false, defaultValue = "BUENO") String estadoEjemplar,
            @RequestParam(name = "observacion", required = false) String observacion,
            jakarta.servlet.http.HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!esAdminOAutorizado(session)) {
            return "redirect:/login";
        }
        try {
            DevolucionRequestDTO solicitud = new DevolucionRequestDTO();
            solicitud.setIdPrestamo(idPrestamo);
            solicitud.setEstadoEjemplar(estadoEjemplar != null && !estadoEjemplar.isBlank() ? estadoEjemplar : "BUENO");
            solicitud.setObservacion(observacion != null && !observacion.isBlank() ? observacion.trim() : "Devolucion fisica en mostrador");

            Devolucion dev = devolucionService.procesarDevolucion(solicitud);

            String mensaje = "Devolucion confirmada: El prestamo PR-" + idPrestamo + " ha sido marcado como DEVUELTO y el ejemplar fue reingresado al catalogo.";
            if (dev.getMontoMulta() != null && dev.getMontoMulta().compareTo(BigDecimal.ZERO) > 0) {
                mensaje += " Se registro una multa de S/. " + dev.getMontoMulta() + " por " + dev.getDiasRetraso() + " dias de mora.";
            }
            redirectAttributes.addFlashAttribute("mensajeExito", mensaje);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar la devolucion: " + e.getMessage());
        }
        return "redirect:/admin/prestamos";
    }

    // 7.7. Panel Admin: Catalogación y Gestión de Libros
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
                categoriaService.obtenerPorId(categoriaId).ifPresent(libro::setCategoria);
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
            redirectAttributes.addAttribute("tab", "formulario");
            redirectAttributes.addAttribute("exito", true);
        } catch (Exception e) {
            redirectAttributes.addAttribute("tab", "formulario");
            redirectAttributes.addAttribute("error", "Error al guardar el libro: " + e.getMessage());
        }
        return "redirect:/admin/catalogacion";
    }

    @PostMapping("/admin/libros/eliminar/{id}")
    public String eliminarLibroAdminCatalogacion(@PathVariable Integer id, 
                                                jakarta.servlet.http.HttpSession session, 
                                                RedirectAttributes redirectAttributes) {
        if (!esAdminOAutorizado(session)) {
            return "redirect:/login";
        }
        try {
            boolean eliminado = libroService.eliminarLibro(id);
            redirectAttributes.addAttribute("tab", "bajas");
            if (eliminado) {
                redirectAttributes.addAttribute("exito", true);
            } else {
                redirectAttributes.addAttribute("error", "No se encontró el libro solicitado.");
            }
        } catch (Exception e) {
            redirectAttributes.addAttribute("tab", "bajas");
            redirectAttributes.addAttribute("error", "No se puede dar de baja el libro porque tiene préstamos activos asociados.");
        }
        return "redirect:/admin/catalogacion";
    }

    // 7.8. Panel Admin: Telemetría
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
                    .filter(l -> (l.getCategoria() != null && l.getCategoria().getIdCategoria().equals(categoriaId))
                            || (l.getCategorias() != null && l.getCategorias().stream().anyMatch(c -> c.getIdCategoria().equals(categoriaId))))
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

    // PUT Catálogo Admin: Permite editar el JSON en Postman
    @PutMapping(value = {"/admin/catalogo", "/admin/catalogo/{id}"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> editarAdminCatalogoJson(@PathVariable(required = false) Integer id, @RequestBody Libro libro) {
        Integer libroId = (id != null) ? id : libro.getIdLibro();
        if (libroId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe proporcionar el idLibro en la URL o en el cuerpo JSON"));
        }
        try {
            Libro actualizado = libroService.actualizarLibro(libroId, libro);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
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
                categoriaService.obtenerPorId(categoriaId).ifPresent(libroForm::setCategoria);
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
