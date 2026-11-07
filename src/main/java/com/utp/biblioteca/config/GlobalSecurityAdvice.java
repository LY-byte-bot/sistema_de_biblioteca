package com.utp.biblioteca.config;

import com.utp.biblioteca.entities.Usuario;
import com.utp.biblioteca.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalSecurityAdvice {

    private final UsuarioRepository usuarioRepository;

    @ModelAttribute("usuarioActual")
    public Usuario getUsuarioActual(Authentication authentication, HttpSession session) {
        if (session == null) {
            return null;
        }

        Usuario u = (Usuario) session.getAttribute("usuarioActual");
        if (u != null) {
            return u;
        }

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            u = usuarioRepository.findByCorreoIgnoreCase(authentication.getName()).orElse(null);
            if (u != null) {
                session.setAttribute("usuarioActual", u);
            }
            return u;
        }

        return null;
    }
}
