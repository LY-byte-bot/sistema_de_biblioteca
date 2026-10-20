package com.utp.biblioteca.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tbl_devolucion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_devolucion")
    private Integer idDevolucion;

    @Column(name = "fecha_devolucion", nullable = false)
    private LocalDate fechaDevolucion;

    @Column(name = "estado_ejemplar", length = 50)
    private String estadoEjemplar;

    @PositiveOrZero(message = "Los días de retraso no pueden ser negativos")
    @Column(name = "dias_retraso")
    private Integer diasRetraso;

    @PositiveOrZero(message = "El monto de multa no puede ser negativo")
    @Column(name = "monto_multa", precision = 10, scale = 2)
    private BigDecimal montoMulta;

    @Column(name = "observacion", length = 255)
    private String observacion;

    @OneToOne
    @JoinColumn(name = "id_prestamo", unique = true, nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Prestamo prestamo;
}

