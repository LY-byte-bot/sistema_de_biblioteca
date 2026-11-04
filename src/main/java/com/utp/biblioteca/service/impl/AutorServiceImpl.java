package com.utp.biblioteca.service.impl;

import com.utp.biblioteca.entities.Autor;
import com.utp.biblioteca.repository.AutorRepository;
import com.utp.biblioteca.service.AutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AutorServiceImpl implements AutorService {

    private final AutorRepository autorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Autor> listarAutores() {
        return autorRepository.findAll();
    }

    @Override
    @Transactional
    public Autor registrarAutor(Autor autor) {
        return autorRepository.save(autor);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Autor> obtenerPorId(Integer id) {
        return autorRepository.findById(id);
    }

    @Override
    @Transactional
    public Autor actualizarAutor(Integer id, Autor autorActualizado) {
        return autorRepository.findById(id).map(existente -> {
            existente.setNombres(autorActualizado.getNombres());
            existente.setApellidos(autorActualizado.getApellidos());
            return autorRepository.save(existente);
        }).orElseThrow(() -> new RuntimeException("Autor no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public boolean eliminarAutor(Integer id) {
        if (autorRepository.existsById(id)) {
            autorRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

