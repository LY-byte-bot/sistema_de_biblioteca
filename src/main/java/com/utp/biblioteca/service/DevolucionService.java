package com.utp.biblioteca.service;

import com.utp.biblioteca.dto.DevolucionRequestDTO;
import com.utp.biblioteca.entities.Devolucion;

import java.util.List;
import java.util.Optional;

public interface DevolucionService {
    Devolucion procesarDevolucion(DevolucionRequestDTO solicitud);
    Optional<Devolucion> obtenerPorPrestamo(Integer idPrestamo);
    Optional<Devolucion> obtenerPorId(Integer id);
    List<Devolucion> listarPorUsuario(Integer idUsuario);
    List<Devolucion> listarTodas();
}
