package com.utp.biblioteca.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrestamoRequestDTO {

    @NotNull(message = "El ID de usuario es obligatorio")
    private Integer idUsuario;

    @NotNull(message = "El ID de libro es obligatorio")
    private Integer idLibro;

    @Min(value = 1, message = "El préstamo debe ser de al menos 1 día")
    private Integer diasPrestamo; // Opcional, por defecto 7 días si es nulo
}

