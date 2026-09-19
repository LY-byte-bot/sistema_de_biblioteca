package com.utp.biblioteca.service;

import com.utp.biblioteca.dto.ReservaRequestDTO;
import com.utp.biblioteca.entities.Reserva;

import java.util.List;
import java.util.Optional;

public interface ReservaService {
    Reserva crearReserva(ReservaRequestDTO solicitud);
    Reserva cancelarReserva(Integer idReserva);
    List<Reserva> listarPorUsuario(Integer idUsuario);
    List<Reserva> listarTodas();
    Optional<Reserva> obtenerPorId(Integer id);
}

