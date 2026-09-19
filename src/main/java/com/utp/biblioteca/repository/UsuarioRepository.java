package com.utp.biblioteca.repository;

import com.utp.biblioteca.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByDni(String dni);
    boolean existsByCorreo(String correo);
    boolean existsByDni(String dni);
}

