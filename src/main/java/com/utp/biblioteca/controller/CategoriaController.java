package com.utp.biblioteca.controller;

import com.utp.biblioteca.entities.Categoria;
import com.utp.biblioteca.repository.CategoriaRepository;
import com.utp.biblioteca.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final CategoriaRepository categoriaRepository;

    // 1. GET - Listar todas las categorías
    @GetMapping
    public ResponseEntity<List<Categoria>> listarCategorias() {
        return ResponseEntity.ok(categoriaService.listarCategorias());
    }

    // 1. GET - Obtener categoría por ID
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtenerPorId(@PathVariable Integer id) {
        return categoriaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 2. POST - Registrar categoría (con validación y seguridad de rol)
    @PostMapping
    public ResponseEntity<?> registrarCategoria(@Valid @RequestBody Categoria categoria, 
                                                BindingResult result,
                                                @RequestHeader(name = "X-Rol", required = false) String xRol) {
        if (xRol != null && xRol.trim().equalsIgnoreCase("LECTOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: El rol LECTOR no tiene permisos para crear categorías. Se requiere rol ADMIN o BIBLIOTECARIO."));
        }
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errores);
        }

        Categoria nuevaCategoria = categoriaService.registrarCategoria(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCategoria);
    }

    // 3. PUT - Actualizar categoría (con validación y seguridad de rol)
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCategoria(@PathVariable Integer id, 
                                                 @Valid @RequestBody Categoria categoria, 
                                                 BindingResult result,
                                                 @RequestHeader(name = "X-Rol", required = false) String xRol) {
        if (xRol != null && xRol.trim().equalsIgnoreCase("LECTOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: El rol LECTOR no tiene permisos para modificar categorías. Se requiere rol ADMIN o BIBLIOTECARIO."));
        }
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errores);
        }

        Optional<Categoria> existenteOpt = categoriaService.obtenerPorId(id);
        if (existenteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Categoría no encontrada con ID: " + id));
        }
        Categoria existente = existenteOpt.get();
        existente.setNombre(categoria.getNombre());
        existente.setDescripcion(categoria.getDescripcion());
        return ResponseEntity.ok(categoriaService.registrarCategoria(existente));
    }

    // 4. DELETE - Eliminar categoría (con seguridad de rol)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCategoria(@PathVariable Integer id,
                                               @RequestHeader(name = "X-Rol", required = false) String xRol) {
        if (xRol != null && xRol.trim().equalsIgnoreCase("LECTOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: El rol LECTOR no tiene permisos para eliminar categorías. Se requiere rol ADMIN o BIBLIOTECARIO."));
        }

        if (!categoriaRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Categoría no encontrada con ID: " + id));
        }
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.ok(Map.of("mensaje", "Categoría eliminada exitosamente (ON DELETE CASCADE)"));
    }
}
