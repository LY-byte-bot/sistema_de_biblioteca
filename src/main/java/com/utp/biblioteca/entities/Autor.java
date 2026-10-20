package com.utp.biblioteca.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_autor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_autor")
    private Integer idAutor;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Máximo 100 caracteres")
    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Máximo 100 caracteres")
    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @ManyToMany(mappedBy = "autores")
    @JsonIgnoreProperties("autores")
    private List<Libro> libros = new ArrayList<>();
}

