package com.utp.biblioteca.controller;

import com.utp.biblioteca.dto.DevolucionRequestDTO;
import com.utp.biblioteca.dto.PrestamoRequestDTO;
import com.utp.biblioteca.entities.Devolucion;
import com.utp.biblioteca.entities.Prestamo;
import com.utp.biblioteca.service.DevolucionService;
import com.utp.biblioteca.service.PrestamoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PrestamoController {

    private final PrestamoService prestamoService;
    private final DevolucionService devolucionService;

    @PostMapping("/solicitar")
    public ResponseEntity<?> solicitarPrestamo(@RequestBody PrestamoRequestDTO solicitud) {
        try {
            Prestamo nuevoPrestamo = prestamoService.registrarPrestamo(solicitud);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPrestamo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/usuario/{id}")
    public ResponseEntity<List<Prestamo>> listarPorUsuario(@PathVariable Integer id) {
        return ResponseEntity.ok(prestamoService.listarPorUsuario(id));
    }

    @PostMapping("/devolver")
    public ResponseEntity<?> devolverPrestamo(@RequestBody DevolucionRequestDTO solicitud) {
        try {
            Devolucion devolucion = devolucionService.procesarDevolucion(solicitud);
            return ResponseEntity.ok(devolucion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Prestamo>> listarActivos() {
        return ResponseEntity.ok(prestamoService.listarPrestamosActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prestamo> obtenerPorId(@PathVariable Integer id) {
        return prestamoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

