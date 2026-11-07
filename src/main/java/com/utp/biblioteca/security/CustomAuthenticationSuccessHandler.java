package com.utp.biblioteca.security;

import com.utp.biblioteca.entities.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession();

        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            Usuario u = userDetails.getUsuario();
            session.setAttribute("usuarioActual", u);
        }

        boolean esAdminOBibliotecario = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN") || auth.equals("ROLE_BIBLIOTECARIO"));

        if (esAdminOBibliotecario) {
            response.sendRedirect(request.getContextPath() + "/admin/catalogo");
        } else {
            response.sendRedirect(request.getContextPath() + "/catalogo");
        }
    }
}
