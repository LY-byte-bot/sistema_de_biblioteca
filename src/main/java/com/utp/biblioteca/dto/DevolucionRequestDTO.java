package com.utp.biblioteca.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DevolucionRequestDTO {
    private Integer idPrestamo;
    private String estadoEjemplar; // ej: "BUENO", "REGULAR", "DANADO"
    private String observacion;
}

