package com.utp.biblioteca.service.impl;

import com.utp.biblioteca.entities.Usuario;
import com.utp.biblioteca.repository.UsuarioRepository;
import com.utp.biblioteca.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo: " + usuario.getCorreo());
        }
        if (usuario.getDni() != null && !usuario.getDni().isBlank() && usuarioRepository.existsByDni(usuario.getDni())) {
            throw new IllegalArgumentException("Ya existe un usuario con el DNI: " + usuario.getDni());
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional
    public Usuario actualizarUsuario(Integer id, Usuario datos) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombre(datos.getNombre());
            usuario.setTelefono(datos.getTelefono());
            usuario.setDireccion(datos.getDireccion());
            if (datos.getRol() != null && !datos.getRol().isBlank()) {
                usuario.setRol(datos.getRol());
            }
            if (datos.getEstado() != null && !datos.getEstado().isBlank()) {
                usuario.setEstado(datos.getEstado());
            }
            return usuarioRepository.save(usuario);
        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public boolean darDeBajaUsuario(Integer id) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setEstado("INACTIVO");
            usuarioRepository.save(usuario);
            return true;
        }).orElse(false);
    }
}

