package com.utp.biblioteca.config;

import com.utp.biblioteca.entities.*;
import com.utp.biblioteca.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;
    private final CategoriaRepository categoriaRepository;
    private final PrestamoRepository prestamoRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Inicializar usuarios si no existen
        if (usuarioRepository.count() == 0) {
            Usuario lector = new Usuario();
            lector.setNombre("Ivan Benjamin Mamani Villanueva");
            lector.setCorreo("ivan.mamani@gmail.com");
            lector.setPasswordHash("123456");
            lector.setRol("LECTOR");
            lector.setEstado("ACTIVO");
            lector.setDni("72330451");
            lector.setTelefono("+51 958 123 456");
            lector.setDireccion("Av. Independencia 123, Cercado, Arequipa");
            usuarioRepository.save(lector);

            Usuario admin = new Usuario();
            admin.setNombre("Omar Julio Valencia Gallegos");
            admin.setCorreo("admin@biblioteca.edu.pe");
            admin.setPasswordHash("admin123");
            admin.setRol("ADMINISTRADOR");
            admin.setEstado("ACTIVO");
            admin.setDni("01234567");
            admin.setTelefono("+51 987 654 321");
            admin.setDireccion("Campus UTP Arequipa");
            usuarioRepository.save(admin);
        }

        // 2. Inicializar categorías si no existen
        if (categoriaRepository.count() == 0) {
            categoriaRepository.save(new Categoria(null, "Ingeniería de Sistemas y Software", "Libros de arquitectura, metodologías y diseño", new ArrayList<>()));
            categoriaRepository.save(new Categoria(null, "Desarrollo Web", "Tecnologías frontend, backend, frameworks y APIs", new ArrayList<>()));
            categoriaRepository.save(new Categoria(null, "Bases de Datos", "Sistemas relacionales, NoSQL y modelado de datos", new ArrayList<>()));
            categoriaRepository.save(new Categoria(null, "Inteligencia Artificial", "Machine learning, redes neuronales y algoritmos", new ArrayList<>()));
            categoriaRepository.save(new Categoria(null, "Redes y Telecomunicaciones", "Infraestructura, protocolos y seguridad informática", new ArrayList<>()));
        }

        // 3. Inicializar autores si no existen
        if (autorRepository.count() == 0) {
            autorRepository.save(new Autor(null, "Martin", "Fowler", new ArrayList<>()));
            autorRepository.save(new Autor(null, "Rod", "Johnson", new ArrayList<>()));
            autorRepository.save(new Autor(null, "Evan", "You", new ArrayList<>()));
            autorRepository.save(new Autor(null, "C. J.", "Date", new ArrayList<>()));
            autorRepository.save(new Autor(null, "Erich", "Gamma", new ArrayList<>()));
            autorRepository.save(new Autor(null, "Andrew S.", "Tanenbaum", new ArrayList<>()));
            autorRepository.save(new Autor(null, "Robert C.", "Martin", new ArrayList<>()));
        }

        // 4. Inicializar libros del informe si no existen
        if (libroRepository.count() == 0) {
            Autor fowler = autorRepository.findByApellidosContainingIgnoreCase("Fowler").stream().findFirst().orElse(null);
            Autor johnson = autorRepository.findByApellidosContainingIgnoreCase("Johnson").stream().findFirst().orElse(null);
            Autor evan = autorRepository.findByApellidosContainingIgnoreCase("You").stream().findFirst().orElse(null);
            Autor date = autorRepository.findByApellidosContainingIgnoreCase("Date").stream().findFirst().orElse(null);
            Autor gamma = autorRepository.findByApellidosContainingIgnoreCase("Gamma").stream().findFirst().orElse(null);
            Autor tanenbaum = autorRepository.findByApellidosContainingIgnoreCase("Tanenbaum").stream().findFirst().orElse(null);
            Autor bob = autorRepository.findByApellidosContainingIgnoreCase("Martin").stream().findFirst().orElse(null);

            Categoria ingSoft = categoriaRepository.findByNombreIgnoreCase("Ingeniería de Sistemas y Software").orElse(null);
            Categoria devWeb = categoriaRepository.findByNombreIgnoreCase("Desarrollo Web").orElse(null);
            Categoria db = categoriaRepository.findByNombreIgnoreCase("Bases de Datos").orElse(null);

            Libro l1 = new Libro();
            l1.setIsbn("978-0134757599");
            l1.setTitulo("Arquitectura Web con Spring Boot 3");
            l1.setResumen("Guía completa de diseño y construcción de arquitecturas backend robustas con Spring Boot 3.x, Spring Data JPA y PostgreSQL. Trata patrones de microservicios, seguridad con JWT y motores de búsqueda con Hibernate Search.");
            l1.setAnioPublicacion(2024);
            l1.setEditorial("Addison-Wesley Professional");
            l1.setIdioma("Español");
            l1.setStockTotal(5);
            l1.setStockDisponible(3);
            List<Autor> a1 = new ArrayList<>();
            if (fowler != null) a1.add(fowler);
            if (johnson != null) a1.add(johnson);
            l1.setAutores(a1);
            if (ingSoft != null) l1.setCategorias(List.of(ingSoft));
            libroRepository.save(l1);

            Libro l2 = new Libro();
            l2.setIsbn("978-1491950296");
            l2.setTitulo("Vue.js 3 y Diseño de Frontend Reactivo");
            l2.setResumen("Manual exhaustivo de desarrollo de aplicaciones SPA modernas utilizando Vue 3, Composition API, Bootstrap 5 y consumo de APIs REST mediante Axios.");
            l2.setAnioPublicacion(2023);
            l2.setEditorial("O'Reilly Media");
            l2.setIdioma("Español");
            l2.setStockTotal(3);
            l2.setStockDisponible(2);
            if (evan != null) l2.setAutores(List.of(evan));
            if (devWeb != null) l2.setCategorias(List.of(devWeb));
            libroRepository.save(l2);

            Libro l3 = new Libro();
            l3.setIsbn("978-0321197849");
            l3.setTitulo("Bases de Datos PostgreSQL Avanzado");
            l3.setResumen("Tratado sobre diseño relacional, normalización estricta en 3FN, transacciones ACID, optimización de índices B-Tree y consultas JPQL complejas.");
            l3.setAnioPublicacion(2022);
            l3.setEditorial("Pearson Educación");
            l3.setIdioma("Español");
            l3.setStockTotal(4);
            l3.setStockDisponible(0); // Agotado para probar reservas
            if (date != null) l3.setAutores(List.of(date));
            if (db != null) l3.setCategorias(List.of(db));
            libroRepository.save(l3);

            Libro l4 = new Libro();
            l4.setIsbn("978-0201633610");
            l4.setTitulo("Patrones de Diseño de Software");
            l4.setResumen("Elementos de software reutilizable orientado a objetos. Principios SOLID, patrones creacionales, estructurales y de comportamiento.");
            l4.setAnioPublicacion(2021);
            l4.setEditorial("Addison-Wesley Professional");
            l4.setIdioma("Español");
            l4.setStockTotal(6);
            l4.setStockDisponible(4);
            if (gamma != null) l4.setAutores(List.of(gamma));
            if (ingSoft != null) l4.setCategorias(List.of(ingSoft));
            libroRepository.save(l4);

            Libro l5 = new Libro();
            l5.setIsbn("978-0132392273");
            l5.setTitulo("Sistemas Distribuidos: Principios y Paradigmas");
            l5.setResumen("Texto fundamental sobre arquitectura de sistemas distribuidos, sincronización, replicación y tolerancia a fallos en entornos de computación moderna.");
            l5.setAnioPublicacion(2023);
            l5.setEditorial("Pearson Educación");
            l5.setIdioma("Español");
            l5.setStockTotal(5);
            l5.setStockDisponible(5);
            if (tanenbaum != null) l5.setAutores(List.of(tanenbaum));
            if (ingSoft != null) l5.setCategorias(List.of(ingSoft));
            libroRepository.save(l5);

            Libro l6 = new Libro();
            l6.setIsbn("978-0132350884");
            l6.setTitulo("Clean Code: Manual de Estilo Ágil");
            l6.setResumen("Guía práctica de artesanía de software y buenas prácticas de codificación limpia, refactorización y pruebas unitarias.");
            l6.setAnioPublicacion(2020);
            l6.setEditorial("Prentice Hall");
            l6.setIdioma("Español");
            l6.setStockTotal(4);
            l6.setStockDisponible(3);
            if (bob != null) l6.setAutores(List.of(bob));
            if (ingSoft != null) l6.setCategorias(List.of(ingSoft));
            libroRepository.save(l6);
        }

        // 5. Inicializar préstamos de demostración para el usuario Iván si no existen
        if (prestamoRepository.count() == 0) {
            Usuario usuario = usuarioRepository.findByCorreo("ivan.mamani@gmail.com").orElse(null);
            Libro libro1 = libroRepository.findByTituloContainingIgnoreCase("Patrones de Diseño").stream().findFirst().orElse(null);
            Libro libro2 = libroRepository.findByTituloContainingIgnoreCase("Arquitectura Web").stream().findFirst().orElse(null);

            if (usuario != null && libro1 != null) {
                // Préstamo vencido (venció hace 3 días -> genera S/. 7.50 o S/. 6.00 de multa)
                Prestamo p1 = new Prestamo();
                p1.setUsuario(usuario);
                p1.setLibro(libro1);
                p1.setFechaPrestamo(LocalDate.now().minusDays(10));
                p1.setFechaVencimiento(LocalDate.now().minusDays(3));
                p1.setEstado("ACTIVO");
                prestamoRepository.save(p1);
            }

            if (usuario != null && libro2 != null) {
                // Préstamo vigente (vence en 5 días)
                Prestamo p2 = new Prestamo();
                p2.setUsuario(usuario);
                p2.setLibro(libro2);
                p2.setFechaPrestamo(LocalDate.now().minusDays(2));
                p2.setFechaVencimiento(LocalDate.now().plusDays(5));
                p2.setEstado("ACTIVO");
                prestamoRepository.save(p2);
            }
        }
    }
}
