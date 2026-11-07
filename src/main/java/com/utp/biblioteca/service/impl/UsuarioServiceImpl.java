package com.utp.biblioteca.service.impl;

import com.utp.biblioteca.entities.Usuario;
import com.utp.biblioteca.repository.UsuarioRepository;
import com.utp.biblioteca.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private static final List<String> ROLES_PERMITIDOS = List.of("ADMIN", "LECTOR", "BIBLIOTECARIO");

    @Override
    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        String correoLimpio = usuario.getCorreo() != null ? usuario.getCorreo().trim().toLowerCase() : "";
        usuario.setCorreo(correoLimpio);

        if (usuario.getDni() != null) {
            usuario.setDni(usuario.getDni().trim());
        }

        if (usuario.getRol() != null) {
            String r = usuario.getRol().trim().toUpperCase();
            if ("ADMINISTRADOR".equals(r)) r = "ADMIN";
            if (!ROLES_PERMITIDOS.contains(r)) {
                throw new IllegalArgumentException("El rol solo puede ser ADMIN, LECTOR o BIBLIOTECARIO");
            }
            usuario.setRol(r);
        } else {
            usuario.setRol("LECTOR");
        }

        if (usuarioRepository.existsByCorreoIgnoreCase(correoLimpio)) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo: " + correoLimpio);
        }
        if (usuario.getDni() != null && !usuario.getDni().isBlank() && usuarioRepository.existsByDni(usuario.getDni())) {
            throw new IllegalArgumentException("Ya existe un usuario con el DNI: " + usuario.getDni());
        }

        // Encriptar contraseña con BCrypt de Spring Security si aún no está encriptada
        if (usuario.getPasswordHash() != null && !usuario.getPasswordHash().isBlank()) {
            String passRaw = usuario.getPasswordHash().trim();
            if (!passRaw.startsWith("$2a$") && !passRaw.startsWith("$2b$") && !passRaw.startsWith("$2y$")) {
                usuario.setPasswordHash(passwordEncoder.encode(passRaw));
            }
        }

        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return Optional.empty();
        }
        return usuarioRepository.findByCorreoIgnoreCase(correo.trim());
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
                String r = datos.getRol().trim().toUpperCase();
                if ("ADMINISTRADOR".equals(r)) r = "ADMIN";
                if (!ROLES_PERMITIDOS.contains(r)) {
                    throw new IllegalArgumentException("El rol solo puede ser ADMIN, LECTOR o BIBLIOTECARIO");
                }
                usuario.setRol(r);
            }
            if (datos.getEstado() != null) {
                usuario.setEstado(datos.getEstado());
            }
            if (datos.getPasswordHash() != null && !datos.getPasswordHash().isBlank()) {
                String raw = datos.getPasswordHash().trim();
                if (!raw.startsWith("$2a$") && !raw.startsWith("$2b$") && !raw.startsWith("$2y$")) {
                    usuario.setPasswordHash(passwordEncoder.encode(raw));
                } else {
                    usuario.setPasswordHash(raw);
                }
            }
            return usuarioRepository.save(usuario);
        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public boolean darDeBajaUsuario(Integer id) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setEstado(false);
            usuarioRepository.save(usuario);
            return true;
        }).orElse(false);
    }
}
