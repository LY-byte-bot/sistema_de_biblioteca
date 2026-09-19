package com.utp.biblioteca.repository;

import com.utp.biblioteca.entities.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Integer> {
    List<Autor> findByApellidosContainingIgnoreCase(String apellidos);
    List<Autor> findByNombresContainingIgnoreCase(String nombres);
}

