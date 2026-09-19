package com.utp.biblioteca.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
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

    @Column(name = "isbn", unique = true, length = 20)
    private String isbn;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "resumen", columnDefinition = "TEXT")
    private String resumen;

    @Column(name = "anio_publicacion")
    private Integer anioPublicacion;

    @Column(name = "editorial", length = 100)
    private String editorial;

    @Column(name = "idioma", length = 50)
    private String idioma;

    @Column(name = "stock_total")
    private Integer stockTotal;

    @Column(name = "stock_disponible")
    private Integer stockDisponible;

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

