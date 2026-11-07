package com.utp.biblioteca.controller;

import com.utp.biblioteca.dto.DevolucionRequestDTO;
import com.utp.biblioteca.dto.PrestamoRequestDTO;
import com.utp.biblioteca.dto.ReservaRequestDTO;
import com.utp.biblioteca.entities.*;
import com.utp.biblioteca.repository.ContenidoDigitalRepository;
import com.utp.biblioteca.repository.HistorialBusquedaRepository;
import com.utp.biblioteca.repository.PrestamoRepository;
import com.utp.biblioteca.service.*;
import com.utp.biblioteca.util.PdfGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final LibroService libroService;
    private final UsuarioService usuarioService;
    private final PrestamoService prestamoService;
    private final DevolucionService devolucionService;
    private final ReservaService reservaService;
    private final CategoriaService categoriaService;
    private final AutorService autorService;
    private final PasswordEncoder passwordEncoder;
    private final ContenidoDigitalRepository contenidoDigitalRepository;
    private final HistorialBusquedaRepository historialBusquedaRepository;
    private final PrestamoRepository prestamoRepository;
    private final ArchivoStorageService archivoStorageService;

    // 7.1. Login y Verificación de Roles
    @GetMapping({"/", "/login"})
    public String login(@RequestParam(name = "error", required = false) String error,
                        @RequestParam(name = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Credenciales incorrectas o cuenta inactiva. Verifica tus datos.");
        }
        if (logout != null) {
            model.addAttribute("mensajeExito", "Has cerrado sesiÃ³n correctamente.");
        }
        return "login";
    }

    private boolean esAdminOAutorizado(jakarta.servlet.http.HttpSession session) {
        if (session == null) return false;
        Usuario u = (Usuario) session.getAttribute("usuarioActual");
        if (u == null || u.getRol() == null) return false;
        String rol = u.getRol().toUpperCase().trim();
        return rol.contains("ADMIN") || rol.contains("BIBLIOTECARIO");
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
        String passIngresada = password != null ? password.trim() : "";
        boolean coincide = passwordEncoder.matches(passIngresada, passGuardada) || passIngresada.equals(passGuardada);
        if (!coincide) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "ContraseÃ±a incorrecta"));
        }

        session.setAttribute("usuarioActual", u);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Inicio de sesiÃ³n exitoso",
                "idUsuario", u.getIdUsuario(),
                "nombre", u.getNombre(),
                "rol", u.getRol(),
                "estado", u.getEstado(),
                "usuario", u
        ));
    }

    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.http.HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler logoutHandler = 
            new org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/login?logout=true";
    }

    // 7.2. Registro de Usuarios
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
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        try {
            String nom = (nombre != null) ? nombre.trim() : "";
            String ape = (apellidos != null) ? apellidos.trim() : "";
            if (nom.isEmpty()) {
                throw new IllegalArgumentException("El nombre es obligatorio.");
            }
            if (ape.isEmpty()) {
                throw new IllegalArgumentException("Los apellidos son obligatorios.");
            }
            String regexLetras = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$";
            if (!nom.matches(regexLetras)) {
                throw new IllegalArgumentException("El nombre solo debe contener letras (sin números ni caracteres especiales).");
            }
            if (!ape.matches(regexLetras)) {
                throw new IllegalArgumentException("Los apellidos solo deben contener letras (sin números ni caracteres especiales).");
            }

            String dniLimpio = (dni != null) ? dni.trim().replaceAll("\\s+", "") : "";
            if (!dniLimpio.matches("^[0-9]{8}$")) {
                throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos numéricos.");
            }

            String telLimpio = (telefono != null) ? telefono.trim().replaceAll("[^0-9]", "") : "";
            if (telLimpio.length() == 11 && telLimpio.startsWith("51")) {
                telLimpio = telLimpio.substring(2);
            }
            if (!telLimpio.matches("^[0-9]{9}$")) {
                throw new IllegalArgumentException("El celular debe tener exactamente 9 dígitos numéricos.");
            }

            String correoLimpio = (correo != null) ? correo.trim().toLowerCase() : "";
            if (!correoLimpio.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new IllegalArgumentException("El formato del correo electrónico es inválido.");
            }

            String pass = (passwordHash != null) ? passwordHash.trim() : "";
            String passConf = (confirmPassword != null) ? confirmPassword.trim() : "";
            if (pass.length() < 6) {
                throw new IllegalArgumentException("La contraseña debe tener un mínimo de 6 caracteres.");
            }
            if (!pass.equals(passConf)) {
                throw new IllegalArgumentException("Las contraseñas ingresadas no coinciden.");
            }

            Usuario usuario = new Usuario();
            usuario.setNombre(nom + " " + ape);
            usuario.setDni(dniLimpio);
            usuario.setTelefono(telLimpio);
            usuario.setCorreo(correoLimpio);
            usuario.setPasswordHash(pass);
            usuario.setRol("LECTOR");
            usuario.setEstado(true);
            usuario.setDireccion("Arequipa, Perú");
            usuario.setFechaCreacion(LocalDateTime.now());

            usuarioService.registrarUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Tu cuenta ha sido creada con éxito! Ya puedes iniciar sesión con tu correo y contraseña.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar el registro: " + e.getMessage());
            return "registro";
        }
    }

    // 7.3. Catálogo General de Libros
    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(name = "query", required = false) String query,
                           @RequestParam(name = "categoriaId", required = false) Integer categoriaId,
                           @RequestParam(name = "disponibilidad", required = false) String disponibilidad,
                           jakarta.servlet.http.HttpSession session,
                           Model model) {
        List<Libro> todosLosLibros = libroService.listarTodos();
        List<Libro> libros;

        if (query != null && !query.trim().isEmpty()) {
            libros = libroService.buscarPorFiltros(query.trim());
            // Guardar auditoría en tbl_historial_busqueda
            try {
                Usuario u = (Usuario) session.getAttribute("usuarioActual");
                HistorialBusqueda hb = new HistorialBusqueda();
                hb.setTermino(query.trim());
                hb.setUsuario(u);
                hb.setFechaHora(LocalDateTime.now());
                historialBusquedaRepository.save(hb);
            } catch (Exception ignored) {}
        } else {
            libros = new ArrayList<>(todosLosLibros);
        }

        if (categoriaId != null && categoriaId > 0) {
            libros = libros.stream()
                    .filter(l -> (l.getCategoria() != null && l.getCategoria().getIdCategoria().equals(categoriaId)) ||
                                 (l.getCategorias() != null && l.getCategorias().stream().anyMatch(c -> c.getIdCategoria().equals(categoriaId))))
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

        model.addAttribute("libros", libros);
        model.addAttribute("categorias", categoriaService.listarCategorias());
        model.addAttribute("query", query);
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("disponibilidad", disponibilidad != null ? disponibilidad : "todos");

        return "catalogo";
    }

    // 7.4. Ficha Técnica y Detalle del Libro
    @GetMapping("/libro/{id}")
    public String detalleLibro(@PathVariable Integer id, Model model) {
        return libroService.obtenerPorId(id)
                .map(libro -> {
                    model.addAttribute("libro", libro);
                    return "libro-detalle";
                })
                .orElse("redirect:/catalogo");
    }

    // Descarga Real del PDF del Libro
    @GetMapping("/libros/descargar/{idLibro}")
    @ResponseBody
    public ResponseEntity<byte[]> descargarExtractoLibro(@PathVariable Integer idLibro) {
        Libro libro = libroService.obtenerPorId(idLibro).orElse(null);
        if (libro == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            List<ContenidoDigital> contenidos = contenidoDigitalRepository.findByLibroIdLibro(idLibro);
            if (!contenidos.isEmpty()) {
                String rutaRelativa = contenidos.get(0).getUrlArchivo();
                if (rutaRelativa != null && rutaRelativa.startsWith("/")) {
                    rutaRelativa = rutaRelativa.substring(1);
                }
                Path path = Paths.get(rutaRelativa);
                if (Files.exists(path)) {
                    byte[] bytes = Files.readAllBytes(path);
                    String filename = libro.getTitulo().replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf";
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(bytes);
                }
            }

            // Generador de Extracto en memoria
            byte[] pdfGenerado = PdfGenerator.generarExtractoPdf(libro);
            String filename = "Extracto_" + libro.getTitulo().replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfGenerado);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 7.5. Mis Préstamos y Multas (Lector)
    @GetMapping("/prestamos")
    public String misPrestamos(jakarta.servlet.http.HttpSession session, Model model) {
        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioActual");
        List<Prestamo> prestamosActivos = new ArrayList<>();
        List<Devolucion> devoluciones = new ArrayList<>();
        List<Reserva> reservas = new ArrayList<>();
        BigDecimal totalMulta = BigDecimal.ZERO;

        if (usuarioActual != null && usuarioActual.getIdUsuario() != null) {
            prestamosActivos = prestamoService.listarActivosPorUsuario(usuarioActual.getIdUsuario());
            devoluciones = devolucionService.listarPorUsuario(usuarioActual.getIdUsuario());
            reservas = reservaService.listarPorUsuario(usuarioActual.getIdUsuario());

            for (Devolucion d : devoluciones) {
                if (d.getMontoMulta() != null) {
                    totalMulta = totalMulta.add(d.getMontoMulta());
                }
            }

            LocalDate hoy = LocalDate.now();
            for (Prestamo p : prestamosActivos) {
                if (p.getFechaVencimiento() != null && p.getFechaVencimiento().isBefore(hoy)) {
                    long dias = ChronoUnit.DAYS.between(p.getFechaVencimiento(), hoy);
                    totalMulta = totalMulta.add(BigDecimal.valueOf(dias).multiply(new BigDecimal("2.50")));
                }
            }
        }
        model.addAttribute("prestamos", prestamosActivos);
        model.addAttribute("prestamosActivos", prestamosActivos);
        model.addAttribute("devoluciones", devoluciones);
        model.addAttribute("reservas", reservas);
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
            PrestamoRequestDTO solicitud = new PrestamoRequestDTO();
            solicitud.setIdLibro(idLibro);
            solicitud.setIdUsuario(usuarioActual.getIdUsuario());
            solicitud.setDiasPrestamo(7);
            prestamoService.registrarPrestamo(solicitud);
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Préstamo solicitado con éxito! El ejemplar ha sido reservado para ti por 24 horas para su retiro en mostrador.");
            return "redirect:/prestamos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/libro/" + idLibro;
        }
    }

    // Solicitud Real de Reserva (Cola de Espera)
    @PostMapping("/reservas/solicitar/{idLibro}")
    public String solicitarReserva(@PathVariable Integer idLibro,
                                   jakarta.servlet.http.HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioActual");
        if (usuarioActual == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para solicitar una reserva.");
            return "redirect:/login";
        }
        try {
            ReservaRequestDTO dto = new ReservaRequestDTO();
            dto.setIdLibro(idLibro);
            dto.setIdUsuario(usuarioActual.getIdUsuario());
            reservaService.crearReserva(dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Reserva registrada con éxito! Has ingresado a la cola de espera de este ejemplar. Te notificaremos cuando esté disponible.");
            return "redirect:/prestamos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/libro/" + idLibro;
        }
    }

    // Cancelar Reserva de Lector
    @PostMapping("/reservas/cancelar/{idReserva}")
    public String cancelarReservaUsuario(@PathVariable Integer idReserva,
                                         jakarta.servlet.http.HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        Usuario usuarioActual = (Usuario) session.getAttribute("usuarioActual");
        if (usuarioActual == null) {
            return "redirect:/login";
        }
        try {
            reservaService.cancelarReserva(idReserva);
            redirectAttributes.addFlashAttribute("mensajeExito", "La reserva ha sido cancelada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar la reserva: " + e.getMessage());
        }
        return "redirect:/prestamos";
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
            solicitud.setObservacion(observacion != null && !observacion.isBlank() ? observacion.trim() : "Devolución física en mostrador");

            Devolucion dev = devolucionService.procesarDevolucion(solicitud);

            String mensaje = "Devolución del libro procesada correctamente.";
            if (dev.getMontoMulta() != null && dev.getMontoMulta().compareTo(BigDecimal.ZERO) > 0) {
                mensaje += " Se registró una multa de S/. " + dev.getMontoMulta() + " por " + dev.getDiasRetraso() + " días de retraso.";
            } else {
                mensaje += " Ejemplar reintegrado al catálogo físico.";
            }

            redirectAttributes.addFlashAttribute("mensajeExito", mensaje);
            redirectAttributes.addFlashAttribute("idDevolucionGenerada", dev.getIdDevolucion());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar la devolución: " + e.getMessage());
        }

        return "redirect:/admin/prestamos";
    }

    // 7.7. Panel Admin: Catalogación y Gestión de Libros
    @GetMapping("/admin/catalogacion")
    public String adminCatalogacion(
            @RequestParam(name = "tab", required = false, defaultValue = "formulario") String tab,
            @RequestParam(name = "exito", required = false) Boolean exito,
            @RequestParam(name = "eliminado", required = false) Boolean eliminado,
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
        model.addAttribute("eliminado", eliminado != null && eliminado);
        model.addAttribute("error", error);
        return "admin-catalogacion";
    }

    @PostMapping("/admin/libros/guardar")
    public String guardarLibroAdmin(
            @ModelAttribute Libro libro,
            @RequestParam(name = "categoriaId", required = false) Integer categoriaId,
            @RequestParam(name = "autorNombre", required = false) String autorNombre,
            @RequestParam(name = "tipoExtracto", required = false, defaultValue = "DEFAULT") String tipoExtracto,
            @RequestParam(name = "archivoPdf", required = false) MultipartFile archivoPdf,
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

            if (categoriaId != null && categoriaId > 0) {
                categoriaService.obtenerPorId(categoriaId).ifPresent(cat -> {
                    libro.setCategoria(cat);
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

            Libro libroGuardado = libroService.registrarLibro(libro);

            // Manejo de Extracto PDF: Personalizado vs AutomÃ¡tico por defecto
            if ("PERSONALIZADO".equalsIgnoreCase(tipoExtracto) && archivoPdf != null && !archivoPdf.isEmpty()) {
                try {
                    String rutaArchivo = archivoStorageService.guardarPdf(archivoPdf, "libro_" + libroGuardado.getIdLibro());
                    ContenidoDigital cd = new ContenidoDigital();
                    cd.setLibro(libroGuardado);
                    cd.setUrlArchivo(rutaArchivo);
                    cd.setTipoArchivo("application/pdf");
                    cd.setChecksum(Long.toHexString(archivoPdf.getSize()));
                    cd.setFechaIndexacion(LocalDateTime.now());
                    contenidoDigitalRepository.save(cd);
                } catch (Exception ex) {
                    System.err.println("Error al guardar archivo digital: " + ex.getMessage());
                }
            }

            redirectAttributes.addAttribute("exito", true);
            redirectAttributes.addAttribute("tab", "inventario");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Error al registrar el libro: " + e.getMessage());
            redirectAttributes.addAttribute("tab", "formulario");
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
            if (eliminado) {
                redirectAttributes.addAttribute("eliminado", true);
                redirectAttributes.addAttribute("tab", "inventario");
            } else {
                redirectAttributes.addAttribute("error", "No se encontró el libro solicitado para eliminar.");
                redirectAttributes.addAttribute("tab", "inventario");
            }
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "No se puede dar de baja el libro porque posee registros históricos o activos asociados.");
            redirectAttributes.addAttribute("tab", "inventario");
        }
        return "redirect:/admin/catalogacion";
    }

    // 7.8. Telemetría Actuator y Métricas Reales del Sistema
    @GetMapping("/admin/telemetria")
    public String adminTelemetria(jakarta.servlet.http.HttpSession session, Model model) {
        if (!esAdminOAutorizado(session)) {
            return "redirect:/login";
        }

        List<Libro> todosLibros = libroService.listarTodos();
        List<Prestamo> prestamosActivos = prestamoService.listarPrestamosActivos();
        List<Devolucion> todasDevoluciones = devolucionService.listarTodas();
        List<Reserva> todasReservas = reservaService.listarTodas();

        long totalEjemplares = todosLibros.stream().mapToLong(l -> l.getStockTotal() != null ? l.getStockTotal() : 0).sum();
        long stockDisponible = todosLibros.stream().mapToLong(l -> l.getStockDisponible() != null ? l.getStockDisponible() : 0).sum();

        BigDecimal totalMultasRecaudadas = todasDevoluciones.stream()
                .map(d -> d.getMontoMulta() != null ? d.getMontoMulta() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate hoy = LocalDate.now();
        long prestamosVencidos = prestamosActivos.stream()
                .filter(p -> p.getFechaVencimiento() != null && p.getFechaVencimiento().isBefore(hoy))
                .count();

        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        long horas = uptimeMs / (1000 * 60 * 60);
        long minutos = (uptimeMs % (1000 * 60 * 60)) / (1000 * 60);
        String uptimeStr = horas + "h " + minutos + "m";
        String jvmVersion = System.getProperty("java.version");

        long memUsadaMb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        long memTotalMb = Runtime.getRuntime().totalMemory() / (1024 * 1024);

        // Libros más prestados reales
        Map<String, Long> conteo = prestamoRepository.findAll().stream()
                .filter(p -> p.getLibro() != null)
                .collect(Collectors.groupingBy(p -> p.getLibro().getTitulo(), Collectors.counting()));

        List<Map<String, Object>> rankingLibros = conteo.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("titulo", e.getKey());
                    item.put("prestamos", e.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        model.addAttribute("totalLibros", todosLibros.size());
        model.addAttribute("totalEjemplares", totalEjemplares);
        model.addAttribute("stockDisponible", stockDisponible);
        model.addAttribute("totalPrestamos", prestamosActivos.size());
        model.addAttribute("prestamosVencidos", prestamosVencidos);
        model.addAttribute("totalDevoluciones", todasDevoluciones.size());
        model.addAttribute("totalMultasRecaudadas", totalMultasRecaudadas);
        model.addAttribute("totalReservas", todasReservas.size());
        model.addAttribute("uptime", uptimeStr);
        model.addAttribute("jvmVersion", jvmVersion);
        model.addAttribute("memoria", memUsadaMb + " MB / " + memTotalMb + " MB");
        model.addAttribute("rankingLibros", rankingLibros);

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
                    .filter(l -> (l.getCategoria() != null && l.getCategoria().getIdCategoria().equals(categoriaId)) ||
                                 (l.getCategorias() != null && l.getCategorias().stream().anyMatch(c -> c.getIdCategoria().equals(categoriaId))))
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
        model.addAttribute("query", query);
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("disponibilidad", disponibilidad != null ? disponibilidad : "todos");
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
            @RequestParam(name = "tipoExtracto", required = false, defaultValue = "CONSERVAR") String tipoExtracto,
            @RequestParam(name = "archivoPdf", required = false) MultipartFile archivoPdf,
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

            // GestiÃ³n de extracto digital al editar
            List<ContenidoDigital> contenidosActuales = contenidoDigitalRepository.findByLibroIdLibro(id);
            if ("DEFAULT".equalsIgnoreCase(tipoExtracto)) {
                // Volver a extracto por defecto: eliminar archivos fÃ­sicos y registro en BD
                for (ContenidoDigital cd : contenidosActuales) {
                    archivoStorageService.eliminarArchivo(cd.getUrlArchivo());
                }
                contenidoDigitalRepository.deleteAll(contenidosActuales);
            } else if ("PERSONALIZADO".equalsIgnoreCase(tipoExtracto) && archivoPdf != null && !archivoPdf.isEmpty()) {
                // Reemplazar con nuevo extracto PDF personalizado
                for (ContenidoDigital cd : contenidosActuales) {
                    archivoStorageService.eliminarArchivo(cd.getUrlArchivo());
                }
                contenidoDigitalRepository.deleteAll(contenidosActuales);

                String rutaArchivo = archivoStorageService.guardarPdf(archivoPdf, "libro_" + id);
                ContenidoDigital nuevoCd = new ContenidoDigital();
                Libro lRef = new Libro();
                lRef.setIdLibro(id);
                nuevoCd.setLibro(lRef);
                nuevoCd.setUrlArchivo(rutaArchivo);
                nuevoCd.setTipoArchivo("application/pdf");
                nuevoCd.setChecksum(Long.toHexString(archivoPdf.getSize()));
                nuevoCd.setFechaIndexacion(LocalDateTime.now());
                contenidoDigitalRepository.save(nuevoCd);
            }

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
