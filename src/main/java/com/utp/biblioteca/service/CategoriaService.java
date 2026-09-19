package com.utp.biblioteca.service;

import com.utp.biblioteca.entities.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaService {
    List<Categoria> listarCategorias();
    Categoria registrarCategoria(Categoria categoria);
    Optional<Categoria> obtenerPorId(Integer id);
}

