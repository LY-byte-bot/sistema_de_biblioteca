package com.utp.biblioteca.repository;

import com.utp.biblioteca.entities.Devolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Integer> {
    Optional<Devolucion> findByPrestamoIdPrestamo(Integer idPrestamo);
}

