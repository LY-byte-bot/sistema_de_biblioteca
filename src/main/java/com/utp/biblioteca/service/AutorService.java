package com.utp.biblioteca.service;

import com.utp.biblioteca.entities.Autor;

import java.util.List;
import java.util.Optional;

public interface AutorService {
    List<Autor> listarAutores();
    Autor registrarAutor(Autor autor);
    Optional<Autor> obtenerPorId(Integer id);
}

