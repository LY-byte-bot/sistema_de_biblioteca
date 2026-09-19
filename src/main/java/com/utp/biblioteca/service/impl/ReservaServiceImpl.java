package com.utp.biblioteca.service.impl;

import com.utp.biblioteca.dto.ReservaRequestDTO;
import com.utp.biblioteca.entities.Libro;
import com.utp.biblioteca.entities.Reserva;
import com.utp.biblioteca.entities.Usuario;
import com.utp.biblioteca.repository.LibroRepository;
import com.utp.biblioteca.repository.ReservaRepository;
import com.utp.biblioteca.repository.UsuarioRepository;
import com.utp.biblioteca.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;

    @Override
    @Transactional
    public Reserva crearReserva(ReservaRequestDTO solicitud) {
        Usuario usuario = usuarioRepository.findById(solicitud.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + solicitud.getIdUsuario()));

        Libro libro = libroRepository.findById(solicitud.getIdLibro())
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado con ID: " + solicitud.getIdLibro()));

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setLibro(libro);
        reserva.setFechaSolicitud(LocalDateTime.now());
        reserva.setEstado("PENDIENTE");

        return reservaRepository.save(reserva);
    }

    @Override
    @Transactional
    public Reserva cancelarReserva(Integer idReserva) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + idReserva));

        reserva.setEstado("CANCELADA");
        return reservaRepository.save(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> listarPorUsuario(Integer idUsuario) {
        return reservaRepository.findByUsuarioIdUsuario(idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> listarTodas() {
        return reservaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reserva> obtenerPorId(Integer id) {
        return reservaRepository.findById(id);
    }
}

