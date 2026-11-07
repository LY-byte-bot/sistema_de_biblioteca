package com.utp.biblioteca.config;

import com.utp.biblioteca.entities.Rol;
import com.utp.biblioteca.entities.Usuario;
import com.utp.biblioteca.repository.RolRepository;
import com.utp.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Inicializar roles si no existen
        if (rolRepository.count() == 0) {
            rolRepository.save(new Rol(null, "ADMIN", "Administrador del sistema de biblioteca"));
            rolRepository.save(new Rol(null, "LECTOR", "Usuario lector de la biblioteca"));
            rolRepository.save(new Rol(null, "BIBLIOTECARIO", "Personal bibliotecario"));
            log.info("Roles iniciales creados.");
        }

        // 2. Migrar contraseñas existentes a BCrypt para compatibilidad con Spring Security
        List<Usuario> usuarios = usuarioRepository.findAll();
        for (Usuario u : usuarios) {
            String pass = u.getPasswordHash();
            if (pass != null && !pass.isBlank() && !pass.startsWith("$2a$") && !pass.startsWith("$2b$") && !pass.startsWith("$2y$")) {
                u.setPasswordHash(passwordEncoder.encode(pass.trim()));
                usuarioRepository.save(u);
                log.info("Contraseña migrada a BCrypt para el usuario: {}", u.getCorreo());
            }
        }

        // 3. Crear usuario administrador de prueba si no hay ninguno registrado
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador General");
            admin.setCorreo("admin@biblioteca.utp.edu.pe");
            admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
            admin.setRol("ADMIN");
            admin.setEstado(true);
            admin.setDni("00000001");
            admin.setTelefono("999999991");
            admin.setDireccion("Campus UTP");
            admin.setFechaCreacion(LocalDateTime.now());
            usuarioRepository.save(admin);

            Usuario lector = new Usuario();
            lector.setNombre("Lector Estudiante");
            lector.setCorreo("lector@biblioteca.utp.edu.pe");
            lector.setPasswordHash(passwordEncoder.encode("Lector123!"));
            lector.setRol("LECTOR");
            lector.setEstado(true);
            lector.setDni("00000002");
            lector.setTelefono("999999992");
            lector.setDireccion("Campus UTP");
            lector.setFechaCreacion(LocalDateTime.now());
            usuarioRepository.save(lector);

            log.info("Usuarios por defecto creados (admin@biblioteca.utp.edu.pe / lector@biblioteca.utp.edu.pe).");
        }
    }
}
