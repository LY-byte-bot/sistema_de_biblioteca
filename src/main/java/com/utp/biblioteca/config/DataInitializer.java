package com.utp.biblioteca.config;

import com.utp.biblioteca.entities.Rol;
import com.utp.biblioteca.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;

    @Override
    public void run(String... args) throws Exception {
        if (rolRepository.count() == 0) {
            rolRepository.save(new Rol(null, "ADMIN", "Administrador del sistema de biblioteca"));
            rolRepository.save(new Rol(null, "LECTOR", "Usuario lector de la biblioteca"));
            rolRepository.save(new Rol(null, "BIBLIOTECARIO", "Personal bibliotecario"));
        }
    }
}
