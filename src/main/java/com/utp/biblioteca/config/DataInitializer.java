package com.utp.biblioteca.config;

import com.utp.biblioteca.entities.Autor;
import com.utp.biblioteca.entities.Categoria;
import com.utp.biblioteca.entities.Rol;
import com.utp.biblioteca.entities.Usuario;
import com.utp.biblioteca.repository.AutorRepository;
import com.utp.biblioteca.repository.CategoriaRepository;
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
    private final CategoriaRepository categoriaRepository;
    private final AutorRepository autorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Roles Base
        if (rolRepository.count() == 0) {
            rolRepository.save(new Rol(null, "ADMIN", "Administrador del sistema de biblioteca"));
            rolRepository.save(new Rol(null, "LECTOR", "Usuario lector de la biblioteca"));
            rolRepository.save(new Rol(null, "BIBLIOTECARIO", "Personal bibliotecario"));
            log.info("Roles iniciales creados.");
        }

        // 2. Semillero de Usuario Administrador / Bibliotecario
        String correoAdmin = "admin@biblioteca.edu.pe";
        if (!usuarioRepository.existsByCorreoIgnoreCase(correoAdmin)) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador General");
            admin.setCorreo(correoAdmin);
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setDni("00000001");
            admin.setTelefono("999888777");
            admin.setRol("ADMIN");
            admin.setEstado(true);
            admin.setDireccion("Biblioteca Académica del Sur - Arequipa");
            admin.setFechaCreacion(LocalDateTime.now());
            usuarioRepository.save(admin);
            log.info(">>> [DataInitializer] Usuario Administrador creado: {} / clave: admin123", correoAdmin);
        }

        // 3. Semillero de Categorías Base
        if (categoriaRepository.count() == 0) {
            String[] categorias = {
                "Ingeniería de Sistemas y Software",
                "Bases de Datos y Persistencia",
                "Arquitectura Cloud y DevOps",
                "Inteligencia Artificial",
                "Desarrollo Web Full-Stack"
            };
            for (String cat : categorias) {
                Categoria c = new Categoria();
                c.setNombre(cat);
                categoriaRepository.save(c);
            }
            log.info(">>> [DataInitializer] Categorías base inicializadas.");
        }

        // 4. Semillero de Autores Base
        if (autorRepository.count() == 0) {
            Autor a1 = new Autor();
            a1.setNombres("Robert C.");
            a1.setApellidos("Martin");
            autorRepository.save(a1);

            Autor a2 = new Autor();
            a2.setNombres("Martin");
            a2.setApellidos("Fowler");
            autorRepository.save(a2);

            log.info(">>> [DataInitializer] Autores base inicializados.");
        }

        // 5. Migrar contraseñas existentes a BCrypt para compatibilidad con Spring Security
        List<Usuario> usuarios = usuarioRepository.findAll();
        for (Usuario u : usuarios) {
            String pass = u.getPasswordHash();
            if (pass != null && !pass.isBlank() && !pass.startsWith("$2a$") && !pass.startsWith("$2b$") && !pass.startsWith("$2y$")) {
                u.setPasswordHash(passwordEncoder.encode(pass.trim()));
                usuarioRepository.save(u);
                log.info("Contraseña migrada a BCrypt para el usuario: {}", u.getCorreo());
            }
        }
    }
}
