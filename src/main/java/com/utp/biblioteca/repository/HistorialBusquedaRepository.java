package com.utp.biblioteca.repository;

import com.utp.biblioteca.entities.HistorialBusqueda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialBusquedaRepository extends JpaRepository<HistorialBusqueda, Integer> {
    List<HistorialBusqueda> findByUsuarioIdUsuario(Integer idUsuario);
}

