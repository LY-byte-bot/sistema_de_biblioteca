package com.utp.biblioteca.service;

import com.utp.biblioteca.entities.Libro;

import java.util.List;
import java.util.Optional;

public interface LibroService {
    List<Libro> listarTodos();
    Optional<Libro> obtenerPorId(Integer id);
    Libro registrarLibro(Libro libro);
    Libro actualizarLibro(Integer id, Libro libro);
    List<Libro> buscarPorFiltros(String query);
    boolean eliminarLibro(Integer id);
}

