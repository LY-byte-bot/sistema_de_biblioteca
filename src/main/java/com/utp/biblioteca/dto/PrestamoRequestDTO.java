package com.utp.biblioteca.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrestamoRequestDTO {
    private Integer idUsuario;
    private Integer idLibro;
    private Integer diasPrestamo; // Opcional, por defecto 7 días si es nulo
}

