package com.utp.biblioteca.service.impl;

import com.utp.biblioteca.dto.DevolucionRequestDTO;
import com.utp.biblioteca.entities.Devolucion;
import com.utp.biblioteca.entities.Libro;
import com.utp.biblioteca.entities.Prestamo;
import com.utp.biblioteca.repository.DevolucionRepository;
import com.utp.biblioteca.repository.LibroRepository;
import com.utp.biblioteca.repository.PrestamoRepository;
import com.utp.biblioteca.service.DevolucionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DevolucionServiceImpl implements DevolucionService {

    private static final BigDecimal TARIFA_MULTA_POR_DIA = new BigDecimal("2.50");

    private final DevolucionRepository devolucionRepository;
    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;

    @Override
    @Transactional
    public Devolucion procesarDevolucion(DevolucionRequestDTO solicitud) {
        Prestamo prestamo = prestamoRepository.findById(solicitud.getIdPrestamo())
                .orElseThrow(() -> new IllegalArgumentException("Préstamo no encontrado con ID: " + solicitud.getIdPrestamo()));

        if ("DEVUELTO".equalsIgnoreCase(prestamo.getEstado())) {
            throw new IllegalStateException("El préstamo con ID " + prestamo.getIdPrestamo() + " ya fue devuelto previamente.");
        }

        LocalDate fechaDevolucion = LocalDate.now();

        // Cálculo de días de retraso y multa
        long diferenciaDias = ChronoUnit.DAYS.between(prestamo.getFechaVencimiento(), fechaDevolucion);
        int diasRetraso = diferenciaDias > 0 ? (int) diferenciaDias : 0;
        BigDecimal montoMulta = BigDecimal.ZERO;
        if (diasRetraso > 0) {
            montoMulta = BigDecimal.valueOf(diasRetraso).multiply(TARIFA_MULTA_POR_DIA);
        }

        // Actualizar estado del préstamo
        prestamo.setEstado("DEVUELTO");
        prestamoRepository.save(prestamo);

        // Reponer stock disponible del libro
        Libro libro = prestamo.getLibro();
        if (libro != null) {
            int stockActual = libro.getStockDisponible() != null ? libro.getStockDisponible() : 0;
            libro.setStockDisponible(stockActual + 1);
            libroRepository.save(libro);
        }

        // Crear registro de devolución
        Devolucion devolucion = new Devolucion();
        devolucion.setPrestamo(prestamo);
        devolucion.setFechaDevolucion(fechaDevolucion);
        devolucion.setEstadoEjemplar(solicitud.getEstadoEjemplar() != null ? solicitud.getEstadoEjemplar() : "BUENO");
        devolucion.setDiasRetraso(diasRetraso);
        devolucion.setMontoMulta(montoMulta);
        devolucion.setObservacion(solicitud.getObservacion());

        return devolucionRepository.save(devolucion);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Devolucion> obtenerPorPrestamo(Integer idPrestamo) {
        return devolucionRepository.findByPrestamoIdPrestamo(idPrestamo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Devolucion> obtenerPorId(Integer id) {
        return devolucionRepository.findById(id);
    }
}

