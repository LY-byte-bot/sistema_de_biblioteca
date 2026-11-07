package com.utp.biblioteca.repository;

import com.utp.biblioteca.entities.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {
    List<Reserva> findByUsuarioIdUsuario(Integer idUsuario);
    List<Reserva> findByUsuarioIdUsuarioOrderByFechaSolicitudDesc(Integer idUsuario);
    List<Reserva> findByLibroIdLibroAndEstadoOrderByFechaSolicitudAsc(Integer idLibro, String estado);
    List<Reserva> findByEstado(String estado);
    boolean existsByUsuarioIdUsuarioAndLibroIdLibroAndEstado(Integer idUsuario, Integer idLibro, String estado);
}
