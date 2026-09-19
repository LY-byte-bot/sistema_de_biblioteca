package com.utp.biblioteca.repository;

import com.utp.biblioteca.entities.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {
    List<Reserva> findByUsuarioIdUsuario(Integer idUsuario);
    List<Reserva> findByEstado(String estado);
}

