package com.utp.biblioteca.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_contenido_digital")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContenidoDigital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contenido")
    private Integer idContenido;

    @Column(name = "url_archivo", nullable = false, length = 500)
    private String urlArchivo;

    @Column(name = "tipo_archivo", length = 50)
    private String tipoArchivo;

    @Column(name = "checksum", length = 128)
    private String checksum;

    @Column(name = "fecha_indexacion")
    private LocalDateTime fechaIndexacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_libro")
    @JsonIgnoreProperties("contenidosDigitales")
    private Libro libro;

    @PrePersist
    public void prePersist() {
        if (this.fechaIndexacion == null) {
            this.fechaIndexacion = LocalDateTime.now();
        }
    }
}

