package com.utp.biblioteca.repository;

import com.utp.biblioteca.entities.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Integer> {

    Optional<Libro> findByIsbn(String isbn);

    List<Libro> findByTituloContainingIgnoreCase(String titulo);

    @Query("SELECT DISTINCT l FROM Libro l " +
           "LEFT JOIN l.autores a " +
           "LEFT JOIN l.categorias c " +
           "WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(COALESCE(l.resumen, '')) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(COALESCE(l.editorial, '')) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(a.nombres) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(a.apellidos) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Libro> buscarPorFiltros(@Param("query") String query);
}

