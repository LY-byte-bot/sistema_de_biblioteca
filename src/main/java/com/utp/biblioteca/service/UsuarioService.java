package com.utp.biblioteca.service;

import com.utp.biblioteca.entities.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    Usuario registrarUsuario(Usuario usuario);
    Optional<Usuario> obtenerPorCorreo(String correo);
    Optional<Usuario> obtenerPorId(Integer id);
    List<Usuario> listarUsuarios();
}

