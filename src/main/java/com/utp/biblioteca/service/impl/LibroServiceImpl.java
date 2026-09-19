package com.utp.biblioteca.service.impl;

import com.utp.biblioteca.entities.Libro;
import com.utp.biblioteca.repository.LibroRepository;
import com.utp.biblioteca.service.LibroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LibroServiceImpl implements LibroService {

    private final LibroRepository libroRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Libro> listarTodos() {
        return libroRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Libro> obtenerPorId(Integer id) {
        return libroRepository.findById(id);
    }

    @Override
    @Transactional
    public Libro registrarLibro(Libro libro) {
        if (libro.getContenidosDigitales() != null) {
            libro.getContenidosDigitales().forEach(cd -> cd.setLibro(libro));
        }
        return libroRepository.save(libro);
    }

    @Override
    @Transactional
    public Libro actualizarLibro(Integer id, Libro libroActualizado) {
        return libroRepository.findById(id).map(libroExistente -> {
            libroExistente.setIsbn(libroActualizado.getIsbn());
            libroExistente.setTitulo(libroActualizado.getTitulo());
            libroExistente.setResumen(libroActualizado.getResumen());
            libroExistente.setAnioPublicacion(libroActualizado.getAnioPublicacion());
            libroExistente.setEditorial(libroActualizado.getEditorial());
            libroExistente.setIdioma(libroActualizado.getIdioma());
            libroExistente.setStockTotal(libroActualizado.getStockTotal());
            libroExistente.setStockDisponible(libroActualizado.getStockDisponible());

            if (libroActualizado.getAutores() != null) {
                libroExistente.setAutores(libroActualizado.getAutores());
            }
            if (libroActualizado.getCategorias() != null) {
                libroExistente.setCategorias(libroActualizado.getCategorias());
            }
            if (libroActualizado.getContenidosDigitales() != null) {
                libroActualizado.getContenidosDigitales().forEach(cd -> cd.setLibro(libroExistente));
                libroExistente.getContenidosDigitales().clear();
                libroExistente.getContenidosDigitales().addAll(libroActualizado.getContenidosDigitales());
            }

            return libroRepository.save(libroExistente);
        }).orElseThrow(() -> new RuntimeException("Libro no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Libro> buscarPorFiltros(String query) {
        if (query == null || query.trim().isEmpty()) {
            return libroRepository.findAll();
        }
        return libroRepository.buscarPorFiltros(query.trim());
    }
}

