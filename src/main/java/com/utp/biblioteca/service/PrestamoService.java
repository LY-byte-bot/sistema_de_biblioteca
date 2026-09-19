package com.utp.biblioteca.service;

import com.utp.biblioteca.dto.PrestamoRequestDTO;
import com.utp.biblioteca.entities.Prestamo;

import java.util.List;
import java.util.Optional;

public interface PrestamoService {
    Prestamo registrarPrestamo(PrestamoRequestDTO solicitud);
    List<Prestamo> listarPrestamosActivos();
    List<Prestamo> listarPorUsuario(Integer idUsuario);
    Optional<Prestamo> obtenerPorId(Integer id);
}

