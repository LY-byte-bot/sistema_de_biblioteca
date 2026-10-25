package com.utp.biblioteca.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        // Inicializador limpio: todos los datos se gestionan mediante la interfaz web
    }
}
