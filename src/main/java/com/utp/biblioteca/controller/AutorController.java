package com.utp.biblioteca.controller;

import com.utp.biblioteca.entities.Autor;
import com.utp.biblioteca.service.AutorService;
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
@RequestMapping("/api/autores")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AutorController {

    private final AutorService autorService;

    // 1. GET - Listar todos los autores
    @GetMapping
    public ResponseEntity<List<Autor>> listarAutores() {
        return ResponseEntity.ok(autorService.listarAutores());
    }

    // 1. GET - Obtener autor por ID
    @GetMapping("/{id}")
    public ResponseEntity<Autor> obtenerPorId(@PathVariable Integer id) {
        return autorService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 2. POST - Registrar autor (con validación y seguridad de rol)
    @PostMapping
    public ResponseEntity<?> registrarAutor(@Valid @RequestBody Autor autor, 
                                            BindingResult result,
                                            @RequestHeader(name = "X-Rol", required = false) String xRol) {
        if (xRol != null && xRol.trim().equalsIgnoreCase("LECTOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: El rol LECTOR no tiene permisos para crear autores. Se requiere rol ADMIN o BIBLIOTECARIO."));
        }
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errores);
        }

        Autor nuevoAutor = autorService.registrarAutor(autor);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoAutor);
    }

    // 3. PUT - Actualizar autor
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarAutor(@PathVariable Integer id, 
                                             @Valid @RequestBody Autor autor, 
                                             BindingResult result,
                                             @RequestHeader(name = "X-Rol", required = false) String xRol) {
        if (xRol != null && xRol.trim().equalsIgnoreCase("LECTOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: El rol LECTOR no tiene permisos para actualizar autores. Se requiere rol ADMIN o BIBLIOTECARIO."));
        }
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errores);
        }

        try {
            Autor actualizado = autorService.actualizarAutor(id, autor);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // 4. DELETE - Eliminar autor
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarAutor(@PathVariable Integer id,
                                           @RequestHeader(name = "X-Rol", required = false) String xRol) {
        if (xRol != null && xRol.trim().equalsIgnoreCase("LECTOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: El rol LECTOR no tiene permisos para eliminar autores. Se requiere rol ADMIN o BIBLIOTECARIO."));
        }

        boolean eliminado = autorService.eliminarAutor(id);
        if (eliminado) {
            return ResponseEntity.ok(Map.of("mensaje", "Autor eliminado exitosamente"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Autor no encontrado con ID: " + id));
    }
}

