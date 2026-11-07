package com.utp.biblioteca.controller;

import com.utp.biblioteca.entities.Usuario;
import com.utp.biblioteca.repository.UsuarioRepository;
import com.utp.biblioteca.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. GET - Listar todos los usuarios
    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    // 1. GET - Obtener usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Integer id) {
        return usuarioService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 1. GET - Obtener usuario por Correo
    @GetMapping("/correo")
    public ResponseEntity<Usuario> obtenerPorCorreo(@RequestParam String correo) {
        return usuarioService.obtenerPorCorreo(correo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 2. POST - Autenticación / Login Seguro para Postman y clientes REST
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String correo = credenciales.get("correo");
        String password = credenciales.get("password");
        if (password == null) {
            password = credenciales.get("passwordHash");
        }

        if (correo == null || correo.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Correo y contraseña son obligatorios"));
        }

        Optional<Usuario> usuarioOpt = usuarioService.obtenerPorCorreo(correo.trim());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales incorrectas: Usuario no encontrado"));
        }

        Usuario u = usuarioOpt.get();
        if (Boolean.FALSE.equals(u.getEstado())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: Tu cuenta se encuentra INACTIVA (estado 0). Contacta al administrador."));
        }

        String passGuardada = (u.getPasswordHash() != null) ? u.getPasswordHash().trim() : "";
        String passIngresada = password.trim();
        boolean match = passwordEncoder.matches(passIngresada, passGuardada) || passIngresada.equals(passGuardada);
        if (!match) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales incorrectas: Contraseña inválida"));
        }

        return ResponseEntity.ok(Map.of(
            "mensaje", "Autenticación exitosa",
            "token", "AUTH-TOKEN-" + u.getIdUsuario() + "-" + u.getRol(),
            "rol", u.getRol(),
            "usuario", u
        ));
    }

    // 2. POST - Registrar nuevo usuario (con validaciones estrictas y chequeo de duplicados)
    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody Usuario usuario, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errores);
        }

        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            return ResponseEntity.badRequest().body(Map.of("correo", "Ya existe una cuenta con este correo electrónico"));
        }
        if (usuario.getDni() != null && !usuario.getDni().isBlank() && usuarioRepository.existsByDni(usuario.getDni())) {
            return ResponseEntity.badRequest().body(Map.of("dni", "Ya existe un usuario con este DNI"));
        }

        Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }

    // 3. PUT - Actualizar usuario
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Integer id, 
                                             @Valid @RequestBody Usuario usuario, 
                                             BindingResult result,
                                             @RequestHeader(name = "X-Rol", required = false) String xRol) {
        if (xRol != null && xRol.trim().equalsIgnoreCase("LECTOR") && usuario.getRol() != null && !usuario.getRol().equalsIgnoreCase("LECTOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: Un usuario con rol LECTOR no puede promoverse a ADMIN o BIBLIOTECARIO"));
        }
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errores);
        }

        try {
            Usuario actualizado = usuarioService.actualizarUsuario(id, usuario);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // 4. DELETE - Dar de baja lógica a un usuario (con seguridad de rol)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> darDeBajaUsuario(@PathVariable Integer id,
                                             @RequestHeader(name = "X-Rol", required = false) String xRol) {
        if (xRol != null && xRol.trim().equalsIgnoreCase("LECTOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: El rol LECTOR no tiene permisos para dar de baja a usuarios. Se requiere rol ADMIN o BIBLIOTECARIO."));
        }

        boolean baja = usuarioService.darDeBajaUsuario(id);
        if (baja) {
            return ResponseEntity.ok(Map.of("mensaje", "Usuario dado de baja exitosamente (estado = false / 0)"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario no encontrado con ID: " + id));
    }
}
