package com.utp.biblioteca.repository;

import com.utp.biblioteca.entities.ContenidoDigital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContenidoDigitalRepository extends JpaRepository<ContenidoDigital, Integer> {
    List<ContenidoDigital> findByLibroIdLibro(Integer idLibro);
}

