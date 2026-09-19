package com.utp.biblioteca.controller;

import com.utp.biblioteca.entities.Autor;
import com.utp.biblioteca.service.AutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/autores")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AutorController {

    private final AutorService autorService;

    @GetMapping
    public ResponseEntity<List<Autor>> listarAutores() {
        return ResponseEntity.ok(autorService.listarAutores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Autor> obtenerPorId(@PathVariable Integer id) {
        return autorService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Autor> registrarAutor(@RequestBody Autor autor) {
        Autor nuevoAutor = autorService.registrarAutor(autor);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoAutor);
    }
}

