package com.utp.biblioteca.service.impl;

import com.utp.biblioteca.dto.PrestamoRequestDTO;
import com.utp.biblioteca.entities.Libro;
import com.utp.biblioteca.entities.Prestamo;
import com.utp.biblioteca.entities.Usuario;
import com.utp.biblioteca.repository.LibroRepository;
import com.utp.biblioteca.repository.PrestamoRepository;
import com.utp.biblioteca.repository.UsuarioRepository;
import com.utp.biblioteca.service.PrestamoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrestamoServiceImpl implements PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;

    @Override
    @Transactional
    public Prestamo registrarPrestamo(PrestamoRequestDTO solicitud) {
        Usuario usuario = usuarioRepository.findById(solicitud.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + solicitud.getIdUsuario()));

        Libro libro = libroRepository.findById(solicitud.getIdLibro())
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado con ID: " + solicitud.getIdLibro()));

        if (libro.getStockDisponible() == null || libro.getStockDisponible() <= 0) {
            throw new IllegalStateException("No hay stock disponible para prestar el libro: " + libro.getTitulo());
        }

        // Reducir stock disponible
        libro.setStockDisponible(libro.getStockDisponible() - 1);
        libroRepository.save(libro);

        int dias = (solicitud.getDiasPrestamo() != null && solicitud.getDiasPrestamo() > 0)
                ? solicitud.getDiasPrestamo()
                : 7;

        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setLibro(libro);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaVencimiento(LocalDate.now().plusDays(dias));
        prestamo.setEstado("ACTIVO");

        return prestamoRepository.save(prestamo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prestamo> listarPrestamosActivos() {
        return prestamoRepository.findByEstado("ACTIVO");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prestamo> listarPorUsuario(Integer idUsuario) {
        return prestamoRepository.findByUsuarioIdUsuario(idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Prestamo> obtenerPorId(Integer id) {
        return prestamoRepository.findById(id);
    }
}

