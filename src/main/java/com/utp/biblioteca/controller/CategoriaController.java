package com.utp.biblioteca.controller;

import com.utp.biblioteca.entities.Categoria;
import com.utp.biblioteca.repository.CategoriaRepository;
import com.utp.biblioteca.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final CategoriaRepository categoriaRepository;

    @GetMapping
    public ResponseEntity<List<Categoria>> listarCategorias() {
        return ResponseEntity.ok(categoriaService.listarCategorias());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtenerPorId(@PathVariable Integer id) {
        return categoriaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Categoria> registrarCategoria(@RequestBody Categoria categoria) {
        Categoria nuevaCategoria = categoriaService.registrarCategoria(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCategoria);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCategoria(@PathVariable Integer id, @RequestBody Categoria categoria) {
        return categoriaService.obtenerPorId(id).map(existente -> {
            existente.setNombre(categoria.getNombre());
            existente.setDescripcion(categoria.getDescripcion());
            return ResponseEntity.ok(categoriaService.registrarCategoria(existente));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCategoria(@PathVariable Integer id) {
        if (!categoriaRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Categoría no encontrada con ID: " + id));
        }
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.ok(Map.of("mensaje", "Categoría y libros asociados eliminados exitosamente (ON DELETE CASCADE)"));
    }
}
