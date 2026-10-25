package com.utp.biblioteca.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_libro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_libro")
    private Integer idLibro;

    @NotBlank(message = "El ISBN es obligatorio")
    @Size(min = 10, max = 20, message = "El ISBN debe tener entre 10 y 20 caracteres")
    @Column(name = "isbn", unique = true, length = 20)
    private String isbn;

    @NotBlank(message = "El título no puede estar vacío")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "resumen", columnDefinition = "TEXT")
    private String resumen;

    @NotNull(message = "El año de publicación es obligatorio")
    @Min(value = 1500, message = "El año debe ser posterior a 1500")
    @Max(value = 2026, message = "El año no puede ser superior a 2026")
    @Column(name = "anio_publicacion")
    private Integer anioPublicacion;

    @Column(name = "editorial", length = 100)
    private String editorial;

    @Column(name = "idioma", length = 50)
    private String idioma;

    @NotNull(message = "El stock total es obligatorio")
    @PositiveOrZero(message = "El stock total no puede ser negativo")
    @Column(name = "stock_total")
    private Integer stockTotal;

    @NotNull(message = "El stock disponible es obligatorio")
    @PositiveOrZero(message = "El stock disponible no puede ser negativo")
    @Column(name = "stock_disponible")
    private Integer stockDisponible;

    @Column(name = "portada_url", length = 500)
    private String portadaUrl;

    @ManyToMany
    @JoinTable(
        name = "tbl_libro_autor",
        joinColumns = @JoinColumn(name = "id_libro"),
        inverseJoinColumns = @JoinColumn(name = "id_autor")
    )
    @JsonIgnoreProperties("libros")
    private List<Autor> autores = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "tbl_libro_categoria",
        joinColumns = @JoinColumn(name = "id_libro"),
        inverseJoinColumns = @JoinColumn(name = "id_categoria")
    )
    @JsonIgnoreProperties("libros")
    private List<Categoria> categorias = new ArrayList<>();

    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("libro")
    private List<ContenidoDigital> contenidosDigitales = new ArrayList<>();
}

