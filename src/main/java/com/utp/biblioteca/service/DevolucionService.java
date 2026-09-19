package com.utp.biblioteca.service;

import com.utp.biblioteca.dto.DevolucionRequestDTO;
import com.utp.biblioteca.entities.Devolucion;

import java.util.Optional;

public interface DevolucionService {
    Devolucion procesarDevolucion(DevolucionRequestDTO solicitud);
    Optional<Devolucion> obtenerPorPrestamo(Integer idPrestamo);
    Optional<Devolucion> obtenerPorId(Integer id);
}

