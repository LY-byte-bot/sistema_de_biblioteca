package com.utp.biblioteca.repository;

import com.utp.biblioteca.entities.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Integer> {
    List<Prestamo> findByUsuarioIdUsuario(Integer idUsuario);
    List<Prestamo> findByEstado(String estado);
    List<Prestamo> findByLibroIdLibroAndEstado(Integer idLibro, String estado);
}

