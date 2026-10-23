package com.utp.biblioteca.controller;

import com.utp.biblioteca.entities.Libro;
import com.utp.biblioteca.repository.LibroRepository;
import com.utp.biblioteca.repository.PrestamoRepository;
import com.utp.biblioteca.service.LibroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/libros")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LibroController {

    private final LibroService libroService;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;

    @GetMapping
    public ResponseEntity<List<Libro>> listarTodos() {
        return ResponseEntity.ok(libroService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Libro> obtenerPorId(@PathVariable Integer id) {
        return libroService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Libro>> buscarPorFiltros(@RequestParam(name = "query", required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(libroService.buscarPorFiltros(query));
    }

    @PostMapping
    public ResponseEntity<?> registrarLibro(@Valid @RequestBody Libro libro, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errores);
        }

        if (libro.getIsbn() != null && libroRepository.findByIsbn(libro.getIsbn()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("isbn", "Ya existe un libro registrado con este ISBN"));
        }

        Libro nuevoLibro = libroService.registrarLibro(libro);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoLibro);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarLibro(@PathVariable Integer id, @Valid @RequestBody Libro libro, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errores);
        }

        try {
            Libro libroActualizado = libroService.actualizarLibro(id, libro);
            return ResponseEntity.ok(libroActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarLibro(@PathVariable Integer id) {
        if (!libroRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Libro no encontrado con ID: " + id));
        }

        boolean tienePrestamosActivos = !prestamoRepository.findByLibroIdLibroAndEstado(id, "ACTIVO").isEmpty();
        if (tienePrestamosActivos) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "No se puede dar de baja el libro porque tiene préstamos activos."));
        }

        libroService.eliminarLibro(id);
        return ResponseEntity.ok(Map.of("mensaje", "Libro eliminado exitosamente"));
    }
}

