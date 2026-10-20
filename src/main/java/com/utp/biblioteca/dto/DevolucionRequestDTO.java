package com.utp.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DevolucionRequestDTO {

    @NotNull(message = "El ID de préstamo es obligatorio")
    private Integer idPrestamo;

    @NotBlank(message = "El estado del ejemplar es obligatorio")
    private String estadoEjemplar; // ej: "BUENO", "REGULAR", "DANADO"

    private String observacion;
}

